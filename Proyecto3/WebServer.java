import java.io.File;
import java.util.Map;
import java.util.Arrays;
import java.util.HashMap;
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
    private static final String SEARCHWORDINFILE_ENDPOINT = "/searchWordInFile";
    private final int port;
    private HttpServer server;

    public static void main(String[] args) {
        int serverPort = 8080;
        if (args.length == 1) {
            serverPort = Integer.parseInt(args[0]);
        }
        WebServer webServer = new WebServer(serverPort);
        webServer.startServer();
        System.out.println("Server listening in port " + serverPort);
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
        HttpContext searchContext = server.createContext(SEARCHWORDINFILE_ENDPOINT);

        searchContext.setHandler(this::handleSearchRequest);

        server.setExecutor(Executors.newFixedThreadPool(8));

        server.start();
    }

    private void handleSearchRequest(HttpExchange exchange) throws IOException {
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
        String word = queryMap.get("word");
        if (queryMap == null || word == null) {
            sendErrorResponse("word param needed".getBytes(), exchange);
            return;
        }

        int res = searchWordInFile(word.toUpperCase());

        // Sending time if debug
        if (isDebugMode) {
            String debugMessage = String.format("The operation took %d nanoseconds", System.nanoTime() - startTime);
            exchange.getResponseHeaders().put("X-Debug-Info", Arrays.asList(debugMessage));
        }

        sendResponse(String.valueOf(res).getBytes(), exchange);
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

    private int searchWordInFile(String word) {
        File file = new File("./files/BIBLIA_COMPLETA.txt");
        int cont = 0;
        int l = 0;

        try {
            if (file.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String currentLine;
                while ((currentLine = br.readLine()) != null) {
                    String[] wordsInLine = currentLine.split(" ");
                    for (int i = 0; i < wordsInLine.length; i++) {
                        // System.out.println(wordsInLine[i].toUpperCase() + " - " + word);
                        String currentWord = wordsInLine[i].toUpperCase();
                        if (currentWord.equals(word) || currentWord.contains(word)) {
                            cont++;
                        }
                    }
                    l++;
                }
                br.close();
            }
            return cont;

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
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