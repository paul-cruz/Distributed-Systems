public class PruebaRectangulo {

    public static void main(String[] args) {
        testCoordDoubles();
        testCoordObj();
    }

    public static void testCoordDoubles() {
        Rectangulo rect = new Rectangulo(2, 3, 5, 1);
        printRes(rect, "Calculando el área de un rectángulo dadas sus coordenadas como dobles en un plano cartesiano:");
    }

    public static void testCoordObj() {
        Coordenada c1 = new Coordenada(1, 4);
        Coordenada c2 = new Coordenada(2, 5);
        Rectangulo rect = new Rectangulo(c1, c2);
        printRes(rect, "Calculando el área de un rectángulo dadas sus coordenadas como objeto en un plano cartesiano:");
    }

    public static void printRes(Rectangulo rect, String msg) {
        double ancho, alto;
        System.out.println(msg);
        System.out.println(rect);
        alto = rect.superiorIzquierda().ordenada() - rect.inferiorDerecha().ordenada();
        ancho = rect.inferiorDerecha().abcisa() - rect.superiorIzquierda().abcisa();
        System.out.println("El área del rectángulo es = " + ancho * alto);
    }

}