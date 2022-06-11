import java.util.Map;
import java.util.Arrays;
import java.util.HashMap;
import java.io.IOException;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.Headers;
import java.util.concurrent.Executors;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;

public class StorageServer {
    private final int port;
    private HttpServer server;
    private Map<String, Integer> booksLength;
    private Map<String, Map<String, Integer>> wordsFrecuencyInBooks;
    private static final String ADDWORDINBOOK_ENDPOINT = "/addWordInBook";
    private static final String GETBOOKLENGTH_ENDPOINT = "/getBookLength";
    private static final String GETWORDINBOOK_ENDPOINT = "/getWordInBook";

    public static void main(String[] args) {
        int serverPort = 5000;
        if (args.length == 1) {
            serverPort = Integer.parseInt(args[0]);
        }
        StorageServer StorageServer = new StorageServer(serverPort);
        StorageServer.startServer();
        System.out.println("Storage server listening in port " + serverPort);
    }

    public StorageServer(int port) {
        this.port = port;
        this.booksLength = new HashMap<String, Integer>();
        this.wordsFrecuencyInBooks = new HashMap<String, Map<String, Integer>>();
        calculateBooksLength();
    }

    private void calculateBooksLength() {
        try {
            BufferedReader br = Utils.execCmd("wc -w ./books/*");
            String line = null;
            while ((line = br.readLine()) != null) {
                String[] tokenizedLine = line.trim().split(" ");
                if (tokenizedLine[1].startsWith("./books/")) {
                    this.booksLength.put(tokenizedLine[1].replace("./books/", ""), Integer.valueOf(tokenizedLine[0]));
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void addWordInBook(String bookTitle, String word, Integer wordFreq) {
        Map<String, Integer> registeredBook = this.wordsFrecuencyInBooks.get(bookTitle);
        if (registeredBook == null) {
            registeredBook = new HashMap<String, Integer>();
        }
        registeredBook.put(word, wordFreq);
        this.wordsFrecuencyInBooks.put(bookTitle, registeredBook);
    }

    public int getBookLength(String bookTitle) {
        Integer bookLength = this.booksLength.get(bookTitle);
        if (bookLength == null) {
            return -1;
        }
        return bookLength;
    }

    public int getWordInBook(String bookTitle, String word) {
        Map<String, Integer> registeredBook = this.wordsFrecuencyInBooks.get(bookTitle);
        if (registeredBook == null) {
            return -1;
        }
        Integer wordLength = registeredBook.get(word);
        if (wordLength == null) {
            return -1;
        }
        return wordLength;
    }

    public void startServer() {
        try {
            this.server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        HttpContext addWordInBookContext = server.createContext(ADDWORDINBOOK_ENDPOINT);
        HttpContext getBookLengthContext = server.createContext(GETBOOKLENGTH_ENDPOINT);
        HttpContext getWordInBookContext = server.createContext(GETWORDINBOOK_ENDPOINT);

        addWordInBookContext.setHandler(this::handleAddWordInBookRequest);
        getBookLengthContext.setHandler(this::handleGetBookLengthRequest);
        getWordInBookContext.setHandler(this::handleGetWordInBookRequest);

        server.setExecutor(Executors.newFixedThreadPool(8));

        server.start();
    }

    private void handleAddWordInBookRequest(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("post")) {
            exchange.close();
            return;
        }

        Headers headers = exchange.getRequestHeaders();
        boolean isDebugMode = false;
        if (headers.containsKey("X-Debug") && headers.get("X-Debug").get(0).equalsIgnoreCase("true")) {
            isDebugMode = true;
        }

        long startTime = System.nanoTime();

        byte[] requestBytes = exchange.getRequestBody().readAllBytes();
        WordRegistry newWord = (WordRegistry) SerializationUtils.deserialize(requestBytes);
        addWordInBook(newWord.book, newWord.word, newWord.freq);

        if (isDebugMode) {
            String debugMessage = String.format("The operation took %d nanoseconds", System.nanoTime() - startTime);
            exchange.getResponseHeaders().put("X-Debug-Info", Arrays.asList(debugMessage));
        }

        sendResponse("OK".getBytes(), exchange);
    }

    private void handleGetBookLengthRequest(HttpExchange exchange) throws IOException {
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
            sendErrorResponse("Book title param needed".getBytes(), exchange);
            return;
        }
        String bookTitle = queryMap.get("bookTitle");

        if (isDebugMode) {
            String debugMessage = String.format("The operation took %d nanoseconds", System.nanoTime() - startTime);
            exchange.getResponseHeaders().put("X-Debug-Info", Arrays.asList(debugMessage));
        }

        sendResponse(String.valueOf(getBookLength(bookTitle)).getBytes(), exchange);
    }

    private void handleGetWordInBookRequest(HttpExchange exchange) throws IOException {
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
            sendErrorResponse("Book title and word params needed".getBytes(), exchange);
            return;
        }
        String bookTitle = queryMap.get("bookTitle");
        String word = queryMap.get("word");

        if (isDebugMode) {
            String debugMessage = String.format("The operation took %d nanoseconds", System.nanoTime() - startTime);
            exchange.getResponseHeaders().put("X-Debug-Info", Arrays.asList(debugMessage));
        }

        sendResponse(String.valueOf(getWordInBook(bookTitle, word)).getBytes(), exchange);
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