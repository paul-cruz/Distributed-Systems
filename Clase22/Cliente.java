import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Cliente {

  // Direcciones de los endpoints que se van a consultar.
  private static final String WORKER_ADDRESS_1 = "http://35.184.100.242:8080/searchipn";
  private static final String WORKER_ADDRESS_2 = "http://35.184.100.242:8080/searchipn";
  private static final String WORKER_ADDRESS_3 = "http://35.184.100.242:8080/searchipn";
  private static final String WORKER_ADDRESS_4 = "http://35.184.100.242:8080/searchipn";

  public static void main(String[] args) {
    Aggregator aggregator = new Aggregator();
    // Cadena de caracteres que se buscarán
    String task = "1757600,IPN";

    // Se envian las tareas a lso trabajadores. Nuestro método recibe 2 arreglos. El
    // de los endpoints y sus valores
    List<String> results = aggregator.sendTasksToWorkers(
        Arrays.asList(WORKER_ADDRESS_1, WORKER_ADDRESS_2, WORKER_ADDRESS_3, WORKER_ADDRESS_4),
        Arrays.asList(task, task, task, task));

    // Imprime los resultados.
    int promedio = 0;
    for (String result : results) {
      int time = Integer.parseInt(result.replaceAll("[^0-9]", ""));
      promedio += time;
      System.out.println(result);
    }
    System.out.println("\n\nEl promedio entre los 4 servidores es: " + promedio / results.size() + " ns");
  }

}

class Aggregator {
  private WebClient webClient;

  // Constructor que instancia un WebClient
  public Aggregator() {
    this.webClient = new WebClient();
  }

  // Recibe lista de endpoints y lista de tareas
  public List<String> sendTasksToWorkers(List<String> workersAddresses, List<String> tasks) {
    // Manejo de comunicación asincrona
    CompletableFuture<String>[] futures = new CompletableFuture[workersAddresses.size()];

    // Se itera cada endpoint y su tarea
    for (int i = 0; i < workersAddresses.size(); i++) {
      // Y se obtiene su dirección
      String workerAddress = workersAddresses.get(i);
      // Y su tarea
      String task = tasks.get(i);

      // Se almacenan las tareas en formato de bytes
      byte[] requestPayload = task.getBytes();
      // Y enviamos las tareas asyncronas.
      futures[i] = webClient.sendTask(workerAddress, requestPayload);
    }

    List<String> results = new ArrayList<String>();
    for (int i = 0; i < tasks.size(); i++) {
      results.add(futures[i].join());
    }
    // List<String> results =
    // Stream.of(futures).map(CompletableFuture::join).collect(Collectors.toList());

    return results;
  }
}

class WebClient {
  private HttpClient client;

  // Constructor
  public WebClient() {
    // Crea un objeto HTTPClient especificando la versión
    this.client = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_1_1)
        .build();
  }

  // Recibe la url y el payload a enviar al endpoint
  public CompletableFuture<String> sendTask(String url, byte[] requestPayload) {
    // Permite construir una solicitud HTTP con el método post y la dirección del
    // destino
    HttpRequest request = HttpRequest.newBuilder()
        .POST(HttpRequest.BodyPublishers.ofByteArray(requestPayload))
        .uri(URI.create(url))
        .header("X-Debug", "true")
        .build();

    // Regresamos la solicitud de manera asyncrona
    return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
        .thenApply(respuesta -> {
          // Solo regresamos el tiempo del servidor, si no no va a funcionar la extracción
          // de tiempos
          /*
           * return respuesta.body() + "\n" + respuesta.uri() + "\n" + respuesta.version()
           * + "\n "
           * + respuesta.headers().firstValue("x-debug-info").get() + " ns";
           */
          return respuesta.headers().firstValue("x-debug-info").get();
        });
  }
}