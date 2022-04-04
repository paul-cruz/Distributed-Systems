import java.util.List;
import java.util.Random;
import java.util.ArrayList;

public class IPNBytes {
    public static void main(String[] args) {
        long start = System.nanoTime();
        int n = Integer.parseInt(args[0]);
        if (n < 1) {
            System.exit(1);
        }
        byte[] str = new byte[n * 4];
        List<Integer> indexes = new ArrayList<Integer>();
        generateRandomIPNString(str);
        /*
         * for (byte b : str) {
         * System.out.printf("%c ", (char) b);
         * }
         * System.out.println();
         */
        System.out.println(countIPNOcurrences(str, indexes));
        for (Integer i : indexes) {
            System.out.printf("%d, ", i);
        }
        System.out.println();
        System.out.println(System.nanoTime() - start);
    }

    public static void generateRandomIPNString(byte[] str) {
        for (int i = 0; i < str.length; i += 4) {
            str[i] = getRandomUpperChar();
            str[i + 1] = getRandomUpperChar();
            str[i + 2] = getRandomUpperChar();
            str[i + 3] = (byte) 32;
        }
    }

    public static byte getRandomUpperChar() {
        return (byte) ((byte) (int) (Math.random() * (90 - 65)) + 65);
    }

    public static int countIPNOcurrences(byte[] str, List<Integer> indexes) {
        int count = 0;
        for (int i = 0; i < str.length; i += 4) {
            if (str[i] == 73 && str[i + 1] == 80 && str[i + 2] == 78) {
                count++;
                indexes.add(i);
            }
        }
        return count;
    }
}