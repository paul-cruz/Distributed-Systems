import java.util.List;

public class RequestsHandler {
    Queue availableServers = new Queue();

    RequestsHandler(List<String> hosts) {
        for (String host : hosts) {
            this.availableServers.push(host);
        }
    }

    synchronized String getNextRequest(String endpoint) {
        try {
            String host = availableServers.pop();
            while (host == null) {
                System.out.println("Not host available, waiting...");
                Thread.sleep(1000);
            }
            String requestUrl = Utils.formatRequest(String.format("http://%s/%s", host, endpoint));
            // System.out.println("Request Handler: returning " + requestUrl);
            return requestUrl;
        } catch (Exception e) {
            System.out.println(e);
            return "ERROR!";
        }
    }

    synchronized void setAvailableServer(String serverAddress) {
        availableServers.push(serverAddress);
    }
}
