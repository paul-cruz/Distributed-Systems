package networking;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;


class Aggregator {
  private WebClient webClient;

  //Constructor que instancia un WebClient
  public Aggregator() {
    this.webClient = new WebClient();
  }

  // Recibe lista de endpoints y lista de tareas
  public CompletableFuture<List<String>> sendTasksToWorkers(String WORKER_ADDRESS_1) {
    //Manejo de comunicación asincrona
    CompletableFuture<List<String>> future = new CompletableFuture();
    // Y enviamos las tareas asyncronas.
    future = webClient.sendTask(WORKER_ADDRESS_1);

    return future;
  }
}