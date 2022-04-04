/*
* Nombre: Juan Paul Cruz Cruz
* Grupo: 4CM12
* Proyecto 2 
* Desarrollo de Sistemas Dstribuidos
*/

import java.awt.*;
import javax.swing.*;
import java.util.List;
import javax.swing.Timer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class SortbyArea implements Comparator<PoligonoReg> {

    public int compare(PoligonoReg a, PoligonoReg b) {
        if (b.obtieneArea() > a.obtieneArea()) {
            return -1;
        } else if (b.obtieneArea() == a.obtieneArea()) {
            return 0;
        } else {
            return 1;
        }
        // return (int) (a.obtieneArea() - b.obtieneArea());
    }
}

public class Gui extends JFrame {
    JPanel panel;
    boolean sorter;
    List<PoligonoReg> poligonos;
    private List<Polygon> poligonosPolygon;
    private int counter;

    public Gui(int n) {
        poligonos = new ArrayList<PoligonoReg>();
        poligonosPolygon = new ArrayList<Polygon>();

        panel = new JPanel();
        add(panel);
        panel.setPreferredSize(new Dimension(860, 600));

        setTitle("Proyecto 2");
        setSize(new Dimension(860, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

        for (int i = 0; i < n; i++) {
            int lados = (int) ((Math.random() * (20 - 3)) + 3);
            poligonos.add(new PoligonoReg(lados));
        }

        SimpleDraw();
        Collections.sort(poligonos, new SortbyArea());
        sorter = false;
        Timer routine = DrawRoutine();
        routine.setInitialDelay(3000);
        routine.start();
    }

    private void SimpleDraw() {
        int size;
        int positionX;
        int positionY;

        for (PoligonoReg pR : poligonos) {
            Polygon polygon = new Polygon();
            size = (int) ((Math.random() * (200 - 50)) + 50);
            positionX = (int) ((Math.random() * (500 - 100)) + 100);
            positionY = (int) ((Math.random() * (500 - 100)) + 100);
            for (Coordenada c : pR.getVertices()) {
                polygon.addPoint((int) ((c.abcisa() * size) + positionX),
                        (int) ((c.ordenada() * size) + positionY));
            }
            poligonosPolygon.add(polygon);
        }

        repaint();
    }

    private Timer DrawRoutine() {
        counter = 0;
        return new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!sorter) {
                    sorter = true;
                    poligonosPolygon.clear();
                }
                if (counter < poligonos.size()) {
                    Polygon polygon = new Polygon();
                    PoligonoReg currentPolygon = poligonos.get(counter);
                    int radio = currentPolygon.getRadio();
                    for (Coordenada c : currentPolygon.getVertices()) {
                        polygon.addPoint((int) ((c.abcisa() * radio) + 400),
                                (int) ((c.ordenada() * radio) + 300));
                    }
                    poligonosPolygon.add(polygon);
                    counter++;
                } else {
                    ((Timer) e.getSource()).stop();
                }
                repaint();
            }
        });

    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.setColor(Color.RED);
        for (Polygon polygon : poligonosPolygon) {
            g.drawPolygon(polygon);
        }
    }

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        if (n < 1) {
            System.exit(1);
        }
        new Gui(n);
    }
}