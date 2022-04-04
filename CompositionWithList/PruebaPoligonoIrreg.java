
public class PruebaPoligonoIrreg {
    public static int TEST_NUM = 10;
    public static PoligonoIrreg POL_TEST = new PoligonoIrreg();

    public static void main(String[] args) {
        // long start = System.nanoTime();
        System.out.println("Crear con New");
        testNew();
        routine();
        // System.out.println("Tiempo de new: " + (System.nanoTime() - start));
        System.out.println("Usar setter");
        // start = System.nanoTime();
        testSet();
        routine();
        // System.out.println("Tiempo de setters: " + (System.nanoTime() - start));
    }

    public static void routine() {
        POL_TEST.toString();
        System.out.println("Ordenamiento");
        POL_TEST.ordenaVertices();
        POL_TEST.toString();
    }

    public static void testNew() {
        for (int i = 0; i < TEST_NUM; i++) {
            double x = generateRandomNum();
            double y = generateRandomNum();
            Coordenada c = new Coordenada(x, y);
            POL_TEST.anadeVertice(c);
        }
        // printRes();
    }

    public static void testSet() {
        for (int i = 0; i < TEST_NUM; i++) {
            double x = generateRandomNum();
            double y = generateRandomNum();
            POL_TEST.anadeVertice(x, y, i);
        }
        // printRes();
    }

    public static void printRes() {
        System.out.println("Conjunto de vertices:");
        System.out.println(POL_TEST);
    }

    public static double generateRandomNum() {
        return Math.random() * (100.0 + 100.0) - 100.0;
    }

}
