package queue;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Predicate;

// Model: a[1]..a[n]
// Inv: n >= 0 && forall i=1..n: a[i] != null
// Let: immutable(k): forall i=1..k: a'[i] = a[i]
public class ArrayQueueModule {

    // :NOTE: extra fields
    private static int head = 0;
    private static int tail = 0;
    private static int size;
    private static Object[] elements = new Object[5];


    // :NOTE: удаление и вставка в начало
    // Pre: element != null
    // Post: n' = n + 1 &&
    //       a'[1] = element &&
    //       immutable(n)
    public static void enqueue(Object element) {
        Objects.requireNonNull(element);
        ensureCapacity(size + 1);
        size++;
        if (tail == elements.length) {
            tail = 0;
        }
        elements[tail++] = element;
    }

    // Pre: n > 0
    // Post: R = a[1] && n' = n && immutable(n)
    public static Object element() {
        assert size > 0;
        if (head == elements.length) {
            return elements[0];
        }
        return elements[head];
    }

    // Pre: n > 0
    // :NOTE: shift array
    // Post: R = a[1] && n' = n - 1 && immutable(2, n')
    public static Object dequeue() {
        assert size > 0;
        size--;
        // :NOTE: переписать нормально (fixed)
        if (head == elements.length) {
            head = 0;
        }
        Object res = elements[head];
        elements[head++] = null;
        if (size == 0) {
            clear();
        }
        return res;
    }

    // Pre: true
    // Post: R = n && n' = n && immutable(n)
    public static int size() {
        return size;
    }

    // Pre: true
    // Post: R = (n = 0) && n' = n && immutable(n)
    public static boolean isEmpty() {
        return size == 0;
    }

    // Pre: true
    // Post: n' = 0;
    public static void clear() {
        head = 0;
        tail = 0;
        size = 0;
        // :NOTE: не зачищаются ссылки
        // :NOTE: асимптотика (fixed)
    }


    // Pre: p != null;
    // Post: n' = n && immutable(n);
    public static int countIf(final Predicate<Object> p) {
        assert p != null;
        int count = 0;
        // :NOTE: асимптотика (fixed)
        for (int i = head; i != tail; i++) {
            if (i == elements.length) {
                i = 0;
            }
            if (p.test(elements[i])) {
                count++;
            }
        }

        return count;
    }

    // Pre: true
    // Post: n' = n && immutable(n)
    private static void ensureCapacity(int capacity) {
        if (capacity > elements.length) {
            Object[] temp = new Object[capacity * 2];
            if (head == elements.length) {
                head = 0;
            }
            System.arraycopy(elements, head, temp, 0, elements.length - head);

            if (head >= tail) {
                System.arraycopy(elements, 0, temp, elements.length - head, tail);
            }

            head = 0;
            tail = elements.length;
            elements = temp;
        }
    }
}