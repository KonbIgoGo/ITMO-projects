package queue;

import java.util.ArrayList;
import java.util.List;

public class LinkedQueue extends AbstractQueue {
    private Node head = null;
    private Node tail = null;


    @Override
    public void enqueueImpl(Object element) {
        if (head == null) {
            head = new Node(element, null);
        } else {
            if (head.next == null) {
                tail = new Node(element, null);
                head.next = tail;
            } else {
                Node newTail = new Node(element, null);
                tail.next = newTail;
                tail = newTail;
            }
        }
    }

    @Override
    public Object dequeueImpl() {
        Object res = head.value;
        head = head.next;
        return res;
    }

    @Override
    public Object elementImpl() {
        return head.value;
    }

    @Override
    public void clearImpl() {
        head = null;
        tail = null;
    }

//    public void dedup() {
//        List<Object> vals = new ArrayList<>();
//        while (size > 0) {
//            if (vals.isEmpty()) {
//                vals.add(dequeue());
//                continue;
//            }
//            Object res = dequeue();
//            if (!vals.get(vals.size() - 1).equals(res)) {
//                vals.add(res);
//            }
//        }
//        for (Object i : vals) {
//            enqueue(i);
//        }
//    }

    private static class Node {
        private final Object value;
        private Node next;

        public Node(Object value, Node next) {
            assert value != null;

            this.value = value;
            this.next = next;
        }
    }
}
