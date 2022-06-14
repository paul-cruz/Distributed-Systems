import java.io.File;
import java.util.Map;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.io.FileReader;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.Headers;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;

public class ProcessServer {
    private final int port;
    private HttpServer server;
    private String storageServerAddress;
    private static final String GETPHRASEIMPORTANCE_ENDPOINT = "/getPhraseImportance";

    public static void main(String[] args) {
        int serverPort = 7000;
        if (args.length < 1) {
            System.out.println("storageServerAddress needed");
            System.exit(1);
        }

        ProcessServer ProcessServer = new ProcessServer(serverPort, args[0]);
        ProcessServer.startServer();
        System.out.println("Process server listening in port " + serverPort);
    }

    public ProcessServer(int port, String storageServerAddress) {
        this.port = port;
        this.storageServerAddress = storageServerAddress;
    }

    public void startServer() {
        try {
            this.server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        HttpContext getPhraseImportanceContext = server.createContext(GETPHRASEIMPORTANCE_ENDPOINT);

        getPhraseImportanceContext.setHandler(this::handleGetPhraseImportanceRequest);

        server.setExecutor(Executors.newFixedThreadPool(8));

        server.start();
    }

    private void handleGetPhraseImportanceRequest(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("get")) {
            exchange.close();
            return;
        }

        Headers headers = exchange.getRequestHeaders();
        boolean isDebugMode = false;
        if (headers.containsKey("X-Debug") && headers.get("X-Debug").get(0).equalsIgnoreCase("true")) {
            isDebugMode = true;
        }

        long startTime = System.nanoTime();

        Map<String, String> queryMap = queryToMap(exchange.getRequestURI().getQuery());
        if (queryMap == null) {
            sendErrorResponse("Book title and searchString params needed".getBytes(), exchange);
            return;
        }
        String bookTitle = queryMap.get("bookTitle");
        String searchString = queryMap.get("searchString");

        if (isDebugMode) {
            String debugMessage = String.format("The operation took %d nanoseconds", System.nanoTime() - startTime);
            exchange.getResponseHeaders().put("X-Debug-Info", Arrays.asList(debugMessage));
        }

        sendResponse(String.valueOf(calculateResponse(bookTitle, searchString)).getBytes(), exchange);
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

    private double calculateResponse(String bookTitle, String searchString) {
        double pf = 0;
        int bookLenght = getBookLength(bookTitle);
        for (String word : searchString.split(" ")) {
            pf += ((double) getWordFreq(bookTitle, word) / (double) bookLenght) * getWordItf(bookTitle, word);
        }
        return pf;
    }

    private int getWordFreq(String bookTitle, String word) {
        try {
            Map<String, String> params = new HashMap<String, String>();
            params.put("bookTile", bookTitle);
            params.put("word", word);
            String request = buildRequestUrl(this.storageServerAddress, "getWordInBook", params);
            int freq = sendGetRequestToStorageServer(request);
            if (freq > -1) {
                return freq;
            }
            String countCmd = "cat ./books/" + bookTitle + " | awk '{print tolower($0)}' | grep " + word + " | wc -l";
            freq = Integer.valueOf(Utils.execCmd(countCmd).readLine());
            WordRegistry object = new WordRegistry(bookTitle, word, freq);
            byte[] serializado = SerializationUtils.serialize(object);
            String reqUrl = buildRequestUrl(this.storageServerAddress, "addWordInBook", null);
            String res = sendPostRequestToStorageServer(reqUrl, serializado);
            // System.out.println("ProcessServer: received " + res);
            // System.out.println("ProcessServer: " + word + " freq " + freq);
            return freq;
        } catch (Exception e) {
            System.out.println(e);
            return -1;
        }
    }

    private int getBookLength(String bookTitle) {
        try {
            Map<String, String> params = new HashMap<String, String>();
            params.put("bookTile", bookTitle);
            String request = buildRequestUrl(this.storageServerAddress, "getBookLength", params);
            int length = sendGetRequestToStorageServer(request);
            if (length > -1) {
                return length;
            }
            String countCmd = "wc -w ./books/" + bookTitle + " | awk '{print $1}'";
            length = Integer.valueOf(Utils.execCmd(countCmd).readLine());
            return length;
        } catch (Exception e) {
            System.out.println(e);
            return -1;
        }
    }

    private double getWordItf(String bookTitle, String word) {
        try {
            String countBooksCmd = "ls ./books/ | wc -l";
            int totalBooks = Integer.valueOf(Utils.execCmd(countBooksCmd).readLine());

            String countBooksWithWordCmd = "grep -ilR " + word + " ./books/ | wc -l";
            int totalBooksWithWord = Integer.valueOf(Utils.execCmd(countBooksWithWordCmd).readLine());
            double res = Math.log10((double) totalBooks / (double) totalBooksWithWord);
            // System.out.println("ProcessServer: totalBooks " + totalBooks);
            // System.out.println("ProcessServer: totalBooksWithWord " +
            // totalBooksWithWord);
            // System.out.println("ProcessServer: " + word + " itf " + res);
            return res;
        } catch (Exception e) {
            System.out.println(e);
            return -1;
        }
    }

    private int sendGetRequestToStorageServer(String reqUrl) {
        WebClient webClient = new WebClient();
        CompletableFuture<String> future = new CompletableFuture();
        System.out.println("ProcessServer: sending request " + reqUrl);
        future = webClient.sendGetTask(reqUrl);
        String result = future.join();
        return Integer.valueOf(result);
    }

    private String sendPostRequestToStorageServer(String reqUrl, byte[] serializatedObj) {
        WebClient webClient = new WebClient();
        CompletableFuture<String> future = new CompletableFuture();
        System.out.println("ProcessServer: sending request " + reqUrl);
        future = webClient.sendPostTask(reqUrl, serializatedObj);
        String result = future.join();
        return result;
    }

    private String buildRequestUrl(String host, String endpoint, Map<String, String> params) {
        String req = "http://" + host + "/" + endpoint;
        if (params != null && params.size() > 0) {
            req = req.concat("?");
            for (Map.Entry<String, String> entry : params.entrySet()) {
                req = req.concat(entry.getKey() + "=" + entry.getValue()) + "&";
            }
        }
        if (req.endsWith("&")) {
            req = req.substring(0, req.length() - 1);
        }
        return req;
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