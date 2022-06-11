
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Utils {
    public static BufferedReader execCmd(String shCmd) {
        try {
            String[] cmd = { "sh", "-c", shCmd };
            Process proc = Runtime.getRuntime().exec(cmd);
            InputStream stdIn = proc.getInputStream();
            InputStreamReader isr = new InputStreamReader(stdIn);
            BufferedReader br = new BufferedReader(isr);
            return br;
        } catch (Exception e) {
            return null;
        }
    }

    public static String formatRequest(String request) {
        request = request.replace(" ", "%20");
        return request;
    }
}
