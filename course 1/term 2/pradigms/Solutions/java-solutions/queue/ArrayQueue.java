package queue;

import java.util.function.Predicate;

public class ArrayQueue extends AbstractQueue {
    // :NOTE: extra fields
    private int head = 0;
    private int tail = 0;
    private Object[] elements = new Object[5];

    public void enqueueImpl(Object element) {
        ensureCapacity(size);
        tail %= elements.length;
        elements[tail++] = element;
    }

    public Object elementImpl() {
        if (head == elements.length) {
            return elements[0];
        }
        return elements[head];
    }

    public Object dequeueImpl() {
        head %= elements.length;
        Object res = elements[head];
        head++;
        if (size == 0) {
            clear();
        }
        return res;
    }

    public void clearImpl() {
        for (int i = head; i != tail; i++) {
            if (i == elements.length) {
                i = 0;
            }
            elements[i] = null;
        }
        head = 0;
        tail = 0;
        size = 0;
    }


    private int countIfImpl(final Predicate<Object> p) {
        int count = 0;
        for (int i = head; i != tail; i++) {
            i %= elements.length;

            if (p.test(elements[i])) {
                count++;
            }
        }
        return count;
    }

    // Pre: p != null;
    // Post: n' = n && immutable(n);
    public int countIf(final Predicate<Object> p) {
        assert p != null;
        int count = 0;
        if (size == elements.length) {
            for (Object item : elements) {
                if (p.test(item)) {
                    count++;
                }
            }
        } else {
            for (int i = head; i != tail; i++) {
                i %= elements.length;

                if (p.test(elements[i])) {
                    count++;
                }
            }
        }
        return count;
    }

//    @Override
//    protected void dedupImpl() {
//        head = 0;
//        tail = size;
//    }

    // Pre: true
    // Post: n' = n && immutable(n)
    private void ensureCapacity(int capacity) {
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
