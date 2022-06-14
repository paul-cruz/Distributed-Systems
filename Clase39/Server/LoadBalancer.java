import java.io.File;
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.util.HashMap;
import java.io.FileReader;
import java.util.ArrayList;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.io.BufferedReader;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.Headers;
import java.util.concurrent.Executors;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;

public class LoadBalancer {
    private static final String SEARCHBOOKS_ENDPOINT = "/searchBooks";
    private final int port;
    private HttpServer server;
    private List<String> hosts;
    private RequestsHandler requestHandler;
    private List<String> booksList;

    public static void main(String[] args) {
        int serverPort = 8080;
        if (args.length < 1) {
            exitError("At least one server needed");
        }
        LoadBalancer loadBalancer = new LoadBalancer(serverPort, args);
        loadBalancer.startServer();
        System.out.println("LoadBalancer listening in port " + serverPort);
    }

    public LoadBalancer(int port, String[] hosts) {
        this.port = port;
        this.hosts = new ArrayList<String>();
        for (String host : hosts) {
            if (!isValidIPAddress(host)) {
                exitError("Invalid ip: " + host);
            }
            this.hosts.add(host);
        }
        this.requestHandler = new RequestsHandler(this.hosts);
        this.booksList = new ArrayList<String>();
        fillBooksList();
    }

    public void startServer() {
        try {
            this.server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        HttpContext searchContext = server.createContext(SEARCHBOOKS_ENDPOINT);

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

        // Getting phrase query param
        Map<String, String> queryMap = queryToMap(exchange.getRequestURI().getQuery());
        String phrase = queryMap.get("phrase");
        if (queryMap == null || phrase == null) {
            sendErrorResponse("phrase param needed".getBytes(), exchange);
            return;
        }

        List<Thread> tasks = new ArrayList<Thread>();
        Map<String, Double> results = new HashMap<String, Double>();
        try {
            for (String book : booksList) {
                ThreadTask.startAndRun(book, phrase, requestHandler, results).join();
            }
        } catch (InterruptedException exc) {
            System.out.println("Main thread interrumpted");
        }

        /*
         * for (Map.Entry<String, Double> entry : results.entrySet()) {
         * System.out.println("Key = " + entry.getKey() +
         * ", Value = " + entry.getValue());
         * }
         */

        List<Map.Entry<String, Double>> list = new ArrayList<>(results.entrySet());
        list.removeIf(p -> p.getValue() == 0.0);
        list.sort(Map.Entry.<String, Double>comparingByValue().reversed());
        List<String> listOfBooks = new ArrayList<>();
        list.forEach(l -> listOfBooks.add(l.getKey()));

        // Sending time if debug
        if (isDebugMode) {
            String debugMessage = String.format("The operation took %d nanoseconds", System.nanoTime() - startTime);
            exchange.getResponseHeaders().put("X-Debug-Info", Arrays.asList(debugMessage));
        }

        sendResponse(SerializationUtils.serialize(listOfBooks), exchange);
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

    private void fillBooksList() {
        try {
            BufferedReader br = Utils.execCmd("ls ./books/");
            String line = null;
            while ((line = br.readLine()) != null) {
                // System.out.println("New book: " + line);
                this.booksList.add(line);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void exitError(String msg) {
        System.out.println(msg);
        System.exit(1);
    }

    public static boolean isValidIPAddress(String ip) {
        String zeroTo255 = "(\\d{1,2}|(0|1)\\"
                + "d{2}|2[0-4]\\d|25[0-5])";
        String regex = zeroTo255 + "\\."
                + zeroTo255 + "\\."
                + zeroTo255 + "\\."
                + zeroTo255 + ":\\d{0,5}$";
        Pattern p = Pattern.compile(regex);
        if (ip == null) {
            return false;
        }
        Matcher m = p.matcher(ip);
        return m.matches();
    }
}