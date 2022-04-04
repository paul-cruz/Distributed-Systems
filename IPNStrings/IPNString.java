import java.util.List;
import java.util.Random;
import java.util.ArrayList;

public class IPNString {
    public static void main(String[] args) {
        long start = System.nanoTime();
        int n = Integer.parseInt(args[0]);
        if (n < 1) {
            System.exit(1);
        }
        StringBuilder str = new StringBuilder();
        List<Integer> indexes = new ArrayList<Integer>();
        generateRandomIPNString(str, n * 4);
        // System.out.println(str);
        System.out.println(countIPNOcurrences(str, indexes));
        for (Integer i : indexes) {
            System.out.printf("%d, ", i);
        }
        System.out.println();
        System.out.println(System.nanoTime() - start);
    }

    public static void generateRandomIPNString(StringBuilder str, int n) {
        for (int i = 0; i < n; i += 4) {
            str.append(getRandomUpperChar());
            str.append(getRandomUpperChar());
            str.append(getRandomUpperChar());
            str.append(" ");
        }
    }

    public static char getRandomUpperChar() {
        return (char) ((Math.random() * (90 - 65)) + 65);
    }

    public static int countIPNOcurrences(StringBuilder str, List<Integer> indexes) {
        int i = str.indexOf("IPN"), count = 0;
        while (i >= 0) {
            indexes.add(i);
            count++;
            i = str.indexOf("IPN", i + 1);
        }
        return count;
    }
}