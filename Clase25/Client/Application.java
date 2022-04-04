
/*
 *  MIT License
 *
 *  Copyright (c) 2019 Michael Pogrebinsky - Distributed Systems & Cloud Computing with Java
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

import java.util.Arrays;
import java.util.List;

public class Application {
    public static int TEST_NUM = 10;
    public static PoligonoIrreg POLYGON = new PoligonoIrreg();
    private static final String WORKER_ADDRESS_1 = "http://localhost:8080/poligono";

    public static void main(String[] args) {
        clientRoutine();
    }

    public static double generateRandomNum() {
        return Math.random() * (100.0 + 100.0) - 100.0;
    }

    public static void addVertex() {
        double x = generateRandomNum();
        double y = generateRandomNum();
        Coordenada c = new Coordenada(x, y);
        POLYGON.anadeVertice(c);
    }

    public static void clientRoutine() {
        for (; true;) {
            Aggregator aggregator = new Aggregator();
            addVertex();
            System.out.println(POLYGON);
            List<byte[]> results = aggregator.sendTasksToWorkers(Arrays.asList(WORKER_ADDRESS_1),
                    Arrays.asList(POLYGON));

            for (byte[] obj : results) {
                POLYGON = (PoligonoIrreg) SerializationUtils.deserialize(obj);
                System.out.println("Objeto recibido");
                System.out.println(POLYGON);
                addVertex();
                System.out.println("Objeto a enviar");
                System.out.println(POLYGON);
            }
        }
    }
}
