import java.util.Random;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

public class Cliente {
    public static void main(String[] args) {
        // curl -v --data "1757600,token a buscar" ip_servidor:puerto/endpoint
        for (int i = 0; i < 4; i++) {
            Thread request = new Thread(new RequestThread());
            request.start();
        }
    }
}

class RequestThread implements Runnable {
    public void run() {
        try {
            String requestToken = "";
            String cmd = "";
            Random r = new Random();
            // Getting the token for this request
            for (int i = 0; i < 3; i++) {
                requestToken += (char) (r.nextInt(26) + 65);
            }
            // Command to make a request to the server..
            cmd = "curl -v --header X-Debug:true --data 1757600," + requestToken + " 34.135.168.155:8080/searchipn";
            System.out.println(cmd);
            // Making a the request to the server
            Process proc = Runtime.getRuntime().exec(cmd);
            // Getting the server messages
            InputStream stdErr = proc.getErrorStream();
            InputStreamReader isr = new InputStreamReader(stdErr);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while ((line = br.readLine()) != null)
                System.out.println(line);
            /*
             * int exitVal = proc.waitFor();
             * System.out.println("Process exitValue: " + exitVal);
             */
        } catch (Exception e) {
            // Throwing an exception
            System.out.println("Exception is caught");
        }
    }
}