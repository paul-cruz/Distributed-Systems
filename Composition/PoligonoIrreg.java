public class PoligonoIrreg {
    private Coordenada[] vertices;

    public PoligonoIrreg(int n) {
        vertices = new Coordenada[n];
    }

    public void anadeVertice(Coordenada c, int index) {
        if (index >= vertices.length) {
            return;
        }
        vertices[index] = c;
    }

    public void anadeVertice(double x, double y, int index) {
        if (vertices[index] == null || index >= vertices.length) {
            System.out.println("Error");
            return;
        }
        vertices[index].setX(x);
        vertices[index].setY(y);
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append("{");
        for (Coordenada c : this.vertices) {
            res.append(c).append(",");
        }
        res.append("}");
        return res.toString();
    }
}
