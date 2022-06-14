import java.util.LinkedList;
import java.util.Collections;

public class Queue {
    LinkedList queue = new LinkedList<String>();

    public void push(String value) {
        queue.addFirst(value);
    }

    public String pop() {
        if (isEmpty()) {
            return null;
        }
        return queue.removeLast().toString();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }
}
