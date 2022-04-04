/*
* Nombre: Juan Paul Cruz Cruz
* Grupo: 4CM12
* Proyecto 2 
* Desarrollo de Sistemas Dstribuidos
*/

public class PoligonoReg extends PoligonoIrreg {
    private int numVertices;
    private double angulo;
    private int radio;

    public PoligonoReg(int numVertices) {
        super();
        this.numVertices = numVertices;
        this.angulo = 360.0d / numVertices;
        this.radio = (int) (Math.random() * (200 - 1)) + 1;
        double x, y, teta;

        for (int i = 0; i < numVertices; ++i) {
            teta = (2 * Math.PI / numVertices) * i;
            x = Math.cos(teta);
            y = Math.sin(teta);
            anadeVertice(new Coordenada(x, y));
        }
    }

    public int getRadio() {
        return this.radio;
    }

    public int getNumVertices() {
        return this.numVertices;
    }

    public double getAngulo() {
        return this.angulo;
    }

    public double obtieneArea() {
        return Math.abs((this.numVertices * Math.pow(this.radio, 2) * Math.sin(this.angulo)) / 2.0d);
    }
}
