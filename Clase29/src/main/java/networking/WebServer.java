/*
 *  MIT License
 *
 *  Copyright (c) 2019 Michael Pogrebinsky - Distributed Systems & Cloud Computing with Java
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package networking;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import model.frontend.FrontendSearchRequest;
import model.frontend.FrontendSearchResponse;
import networking.WebClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.HashMap;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.StringTokenizer;
import java.util.concurrent.CompletableFuture;

public class WebServer {
    private static final String STATUS_ENDPOINT = "/status";
    private static final String HOME_PAGE_ENDPOINT = "/";
    private static final String HOME_PAGE_UI_ASSETS_BASE_DIR = "/ui_assets/";
    private static final String ENDPOINT_PROCESS = "/procesar_datos";

    private static final String WORKER_ADDRESS_1 = "http://35.238.5.39:8080/searchipn";
    private static final String WORKER_ADDRESS_2 = "http://34.69.3.154:8080/searchipn";
    private static final String WORKER_ADDRESS_3 = "http://34.67.89.61:8080/searchipn";

    private final int port;
    private HttpServer server;
    private final ObjectMapper objectMapper;

    public WebServer(int port) {
        this.port = port;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
    }

    public void startServer() {
        try {
            this.server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        HttpContext statusContext = server.createContext(STATUS_ENDPOINT);
        HttpContext taskContext = server.createContext(ENDPOINT_PROCESS);

        statusContext.setHandler(this::handleStatusCheckRequest);
        taskContext.setHandler(this::handleTaskRequest);

        // handle requests for resources
        HttpContext homePageContext = server.createContext(HOME_PAGE_ENDPOINT);
        homePageContext.setHandler(this::handleRequestForAsset);

        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();
    }

    private void handleRequestForAsset(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("get")) {
            exchange.close();
            return;
        }

        byte[] response;

        String asset = exchange.getRequestURI().getPath();

        if (asset.equals(HOME_PAGE_ENDPOINT)) {
            response = readUiAsset(HOME_PAGE_UI_ASSETS_BASE_DIR + "index.html");
        } else {
            response = readUiAsset(asset);
        }
        addContentType(asset, exchange);

        sendResponse(response, exchange);
    }

    private byte[] readUiAsset(String asset) throws IOException {
        InputStream assetStream = getClass().getResourceAsStream(asset);
        if (assetStream == null) {
            return new byte[] {};
        }

        return assetStream.readAllBytes();
    }

    private static void addContentType(String asset, HttpExchange exchange) {
        String contentType = "text/html";
        if (asset.endsWith("js")) {
            contentType = "text/javascript";
        } else if (asset.endsWith("css")) {
            contentType = "text/css";
        }
        exchange.getResponseHeaders().add("Content-Type", contentType);
    }

    private void handleTaskRequest(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("post")) {
            exchange.close();
            return;
        }

        try {
            FrontendSearchRequest frontendSearchRequest = objectMapper
                    .readValue(exchange.getRequestBody().readAllBytes(), FrontendSearchRequest.class);
            System.out.println("Los datos recibidos en el servidor web son:" + frontendSearchRequest.getSearchQuery());
            String frase = frontendSearchRequest.getSearchQuery();
            String task = "1757600," + frase;
            Aggregator aggregator = new Aggregator();
            List<String> workers = Arrays.asList(WORKER_ADDRESS_1, WORKER_ADDRESS_2, WORKER_ADDRESS_3);
            List<String> results = aggregator.sendTasksToWorkers(
                    workers,
                    Arrays.asList(task, task, task));
            String response = "";
            for (int i = 0; i < results.size(); i++) {
                String[] aux = results.get(i).replaceAll("Count: ", "").replaceAll("Time: ", "").replaceAll("\\n", ",")
                        .split(",");
                String currentIp = workers.get(i).replaceAll("http://", "");
                response = response.concat(
                        String.format("IP: %s   Coincidencias: %s   Tiempo: %s\n",
                                currentIp.substring(0, currentIp.indexOf("/")), aux[1], aux[3]));
            }
            /*
             * StringTokenizer st = new StringTokenizer(frase);
             * FrontendSearchResponse frontendSearchResponse = new
             * FrontendSearchResponse(frase, st.countTokens());
             */
            Map<String, String> json = new HashMap<String, String>();
            json.put("rsp", response);
            byte[] responseBytes = objectMapper.writeValueAsBytes(json);
            System.out.println(response);
            sendResponse(responseBytes, exchange);

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    private void handleStatusCheckRequest(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("get")) {
            exchange.close();
            return;
        }

        String responseMessage = "El servidor está vivo\n";
        sendResponse(responseMessage.getBytes(), exchange);
    }

    private void sendResponse(byte[] responseBytes, HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(200, responseBytes.length);
        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write(responseBytes);
        outputStream.flush();
        outputStream.close();
    }
}

class Aggregator {
    private WebClient webClient;

    public Aggregator() {
        this.webClient = new WebClient();
    }

    public List<String> sendTasksToWorkers(List<String> workersAddresses, List<String> tasks) {
        CompletableFuture<String>[] futures = new CompletableFuture[workersAddresses.size()];

        for (int i = 0; i < workersAddresses.size(); i++) {
            String workerAddress = workersAddresses.get(i);
            String task = tasks.get(i);
            byte[] requestPayload = task.getBytes();
            futures[i] = webClient.sendTask(workerAddress, requestPayload);
        }

        List<String> results = new ArrayList<String>();
        for (int i = 0; i < tasks.size(); i++) {
            results.add(futures[i].join());
        }
        return results;
    }
}