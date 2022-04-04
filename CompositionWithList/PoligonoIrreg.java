import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

class Sortbymagnitud implements Comparator<Coordenada> {

    public int compare(Coordenada a, Coordenada b) {
        return (int) (a.magnitud() - b.magnitud());
    }
}

public class PoligonoIrreg {
    private List<Coordenada> vertices;

    public PoligonoIrreg() {
        vertices = new ArrayList<Coordenada>();
    }

    public void anadeVertice(Coordenada c) {
        vertices.add(c);
    }

    public void anadeVertice(double x, double y, int index) {
        if (vertices.get(index) == null || index >= vertices.size()) {
            System.out.println("Error");
            return;
        }
        vertices.get(index).setX(x);
        vertices.get(index).setY(y);
    }

    public void ordenaVertices() {
        Collections.sort(vertices, new Sortbymagnitud());
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append("{");
        for (Coordenada coordenada : vertices) {
            StringBuilder aux = new StringBuilder();
            aux.append(coordenada);
            aux.append(" Magnitud: ");
            aux.append(coordenada.magnitud());
            System.out.println(aux);
            res.append(coordenada).append(",");
        }
        res.append("}");
        return res.toString();
    }
}
