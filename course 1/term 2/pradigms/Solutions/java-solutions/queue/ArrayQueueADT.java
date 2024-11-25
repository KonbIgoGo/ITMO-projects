package queue;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Predicate;

// Model: a[1]..a[n]
// Inv: n >= 0 && forall i=1..n: a[i] != null
// Let: immutable(k): forall i=1..k: a'[i] = a[i]
public class ArrayQueueADT {
    private int head = 0;
    private int tail = 0;
    private int size;
    public Object[] elements = new Object[5];
    public ArrayQueueADT() {

    }

    // Pre: true
    // Post: R.n = 0
    public static ArrayQueueADT create() {
        return new ArrayQueueADT();
    }

    // Pre: element != null && queue != null
    // Post: n' = n + 1 &&
    //       a'[1] = element &&
    //       immutable(n)
    public static void enqueue(ArrayQueueADT queue, Object element) {
        Objects.requireNonNull(element);
        ensureCapacity(queue, queue.size + 1);
        queue.size++;
        if (queue.tail == queue.elements.length) {
            queue.tail = 0;
        }
        queue.elements[queue.tail++] = element;
    }

    // Pre: n > 0 && queue != null
    // Post: R = a[1] && n' = n && immutable(n)
    public static Object element(ArrayQueueADT queue) {
        assert queue.size > 0;
        if (queue.head == queue.elements.length) {
            return  queue.elements[0];
        }
        return queue.elements[queue.head];
    }

    // Pre: n > 0 && queue != null
    // Post: R = a[1] && n' = n - 1 && immutable(n')
    public static Object dequeue(ArrayQueueADT queue) {
        assert queue.tail > -1;
        queue.size--;
        if (queue.head == queue.elements.length) {
            queue.head = 0;
        }
        Object res = queue.elements[queue.head];
        queue.elements[queue.head++] = null;
        if (queue.size == 0) {
            clear(queue);
        }
        return res;
    }

    // Pre: queue != null
    // Post: R = n && n' = n && immutable(n)
    public static int size(ArrayQueueADT queue) {
        return queue.size;
    }

    // Pre: queue != null
    // Post: R = (n = 0) && n' = n && immutable(n)
    public static boolean isEmpty(ArrayQueueADT queue) {
        return queue.size == 0;
    }

    // Pre: queue != null
    // Post: n' = 0;
    public static void clear(ArrayQueueADT queue) {
        queue.head = 0;
        queue.tail = 0;
        queue.size = 0;
    };

    // Pre: o != null;
    // Post: n' = n && immutable(n);
    public static int countIf(ArrayQueueADT queue, final Predicate<Object> p) {
        assert p != null;
        int count = 0;
        if (queue.size == queue.elements.length) {
            for (Object item : queue.elements) {
                if (p.test(item)) {
                    count++;
                }
            }
        } else {
            for (int i = queue.head; i != queue.tail; i++) {
                i %= queue.elements.length;

                if (p.test(queue.elements[i])) {
                    count++;
                }
            }
        }
        return count;
    }

    // Pre: queue != null
    // Post: n' = n && immutable(n)
    private static void ensureCapacity(ArrayQueueADT queue, int capacity) {
        if (capacity > queue.elements.length) {
            Object[] temp = new Object[capacity * 2];
            if (queue.head == queue.elements.length) {
                queue.head = 0;
            }
            System.arraycopy(queue.elements, queue.head, temp, 0, queue.elements.length - queue.head);

            if (queue.head >= queue.tail) {
                System.arraycopy(queue.elements, 0, temp, queue.elements.length - queue.head, queue.tail);
            }

            queue.head = 0;
            queue.tail = queue.elements.length;
            queue.elements = temp;
        }
    }
}
