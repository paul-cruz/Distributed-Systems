
// Código Java para probar la serialización y deserialización con ayuda de la clase SerializationUtils.java
import java.io.Serializable;

class Demo implements Serializable {
	public int a;
	public String b;

	// Default constructor
	public Demo(int a, String b) {
		this.a = a;
		this.b = b;
	}
}