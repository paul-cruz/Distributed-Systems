import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ThreadTask implements Runnable {
    WebClient webClient;
    RequestsHandler handler;
    String book, phrase;
    Map<String, Double> results;

    ThreadTask(String book, String phrase, RequestsHandler handler, Map<String, Double> results) {
        this.book = book;
        this.phrase = phrase;
        this.handler = handler;
        this.results = results;
        this.webClient = new WebClient();
    }

    public static Thread startAndRun(String book, String phrase, RequestsHandler handler, Map<String, Double> results) {
        Thread thread = new Thread(new ThreadTask(book, phrase, handler, results));
        thread.start();
        return thread;
    }

    public void run() {
        String reqEndpoint = String.format("getPhraseImportance?bookTitle=%s&searchString=%s", this.book, this.phrase);
        String req = handler.getNextRequest(reqEndpoint);
        if (req == null) {
            return;
        }
        CompletableFuture<String> serverCall = webClient
                .sendGetTask(req);
        String host = req.replace("http://", "");
        host = host.substring(0, host.indexOf("/"));
        Double result = Double.valueOf(serverCall.join());
        handler.setAvailableServer(host);
        System.out.println(phrase + " has " + result + " of importance on " + book);
        this.results.put(book, result);
    }
}
