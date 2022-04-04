/*
* Nombre: Juan Paul Cruz Cruz
* Grupo: 4CM12
* Proyecto 2 
* Desarrollo de Sistemas Dstribuidos
*/

public class Coordenada {

    private double x, y;

    public Coordenada(double x, double y) {

        this.x = x;

        this.y = y;

    }

    // Metodo getter de x

    public double abcisa() {
        return this.x;
    }

    // Metodo getter de y

    public double ordenada() {
        return this.y;
    }

    public double magnitud() {
        return Math.sqrt(Math.pow(this.x, 2) + Math.pow(this.y, 2));
    }

    public void setX(double value) {
        this.x = value;
    }

    public void setY(double value) {
        this.y = value;
    }

    @Override

    public String toString() {

        return "[" + x + "," + y + "]";

    }

}