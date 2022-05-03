import java.io.File;
import java.util.Map;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.Headers;
import java.util.concurrent.Executors;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;

public class WebServer {
    private static final String MULTIPLICA_ENDPOINT = "/multiplica";
    private final int port;
    private HttpServer server;

    public static void main(String[] args) {
        int serverPort = 8080;
        if (args.length == 1) {
            serverPort = Integer.parseInt(args[0]);
        }
        WebServer WebServer = new WebServer(serverPort);
        WebServer.startServer();
        System.out.println("WebServer listening in port " + serverPort);
    }

    public WebServer(int port) {
        this.port = port;
    }

    public void startServer() {
        try {
            this.server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        HttpContext multiplicaContext = server.createContext(MULTIPLICA_ENDPOINT);

        multiplicaContext.setHandler(this::handleMultiplicaRequest);

        server.setExecutor(Executors.newFixedThreadPool(8));

        server.start();
    }

    private void handleMultiplicaRequest(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("get")) {
            exchange.close();
            return;
        }

        // Checking for debug headers
        Headers headers = exchange.getRequestHeaders();
        boolean isDebugMode = false;
        if (headers.containsKey("X-Debug") && headers.get("X-Debug").get(0).equalsIgnoreCase("true")) {
            isDebugMode = true;
        }

        long startTime = System.nanoTime();

        // Getting word query param
        Map<String, String> queryMap = queryToMap(exchange.getRequestURI().getQuery());
        if (queryMap == null) {
            sendErrorResponse("factor1 and factor2 params needed".getBytes(), exchange);
            return;
        }
        int factor1 = Integer.parseInt(queryMap.get("factor1"));
        int factor2 = Integer.parseInt(queryMap.get("factor2"));

        String WORKER_ADDRESS_1 = "localhost:5000";
        Aggregator aggregator = new Aggregator();
        List<Integer> pair = Arrays.asList(factor1, factor2);

        List<String> results = aggregator.sendTasksToWorkers(Arrays.asList(WORKER_ADDRESS_1),
                Arrays.asList(pair));

        for (String result : results) {
            System.out.println("Webserver: result fromm request " + result);
        }

        // Sending time if debug
        if (isDebugMode) {
            String debugMessage = String.format("The operation took %d nanoseconds", System.nanoTime() - startTime);
            exchange.getResponseHeaders().put("X-Debug-Info", Arrays.asList(debugMessage));
        }

        sendResponse(String.valueOf(results.get(0)).getBytes(), exchange);
    }

    private void sendResponse(byte[] responseBytes, HttpExchange exchange) throws IOException {

        exchange.sendResponseHeaders(200, responseBytes.length);

        OutputStream outputStream = exchange.getResponseBody();

        outputStream.write(responseBytes);
        outputStream.flush();
        outputStream.close();
        exchange.close();
    }

    private void sendErrorResponse(byte[] responseBytes, HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(400, responseBytes.length);
        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write(responseBytes);
        outputStream.flush();
        outputStream.close();
        exchange.close();
    }

    public Map<String, String> queryToMap(String query) {
        if (query == null) {
            return null;
        }
        Map<String, String> result = new HashMap<>();
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                result.put(entry[0], entry[1]);
            } else {
                result.put(entry[0], "");
            }
        }
        return result;
    }

}