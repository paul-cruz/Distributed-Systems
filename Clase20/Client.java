import java.io.*;

class MultithreadingCall extends Thread {
    public void run() {
        try {
            Runtime rt = Runtime.getRuntime();
            String[] commands = { "curl", "-v", "--header", "X-Debug:true",
                    "http://34.123.121.119:8080/searchipn?numTokens=1757600&str=IPN" };
            Process proc = rt.exec(commands);

            BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            String s = null;
            System.out.println("Here is the standard error of the command (if any):\n");
            while ((s = stdError.readLine()) != null) {
                System.out.println(s);
            }
            /*
             * BufferedReader stdInput = new BufferedReader(new
             * InputStreamReader(proc.getInputStream()))
             * 
             * System.out.println("Here is the standard output of the command:\n");
             * String s = null;
             * while ((s = stdInput.readLine()) != null) {
             * System.out.println(s);
             * }
             * 
             */
        } catch (Exception e) {
            System.out.println("Exception is caught");
        }
    }
}

public class Client {
    public static void main(String[] args) {
        int n = 4;
        for (int i = 0; i < n; i++) {
            MultithreadingCall object = new MultithreadingCall();
            object.start();
        }
    }
}
