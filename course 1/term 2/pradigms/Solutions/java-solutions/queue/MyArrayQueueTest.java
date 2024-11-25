package queue;

import java.util.Arrays;
import java.util.function.Predicate;

public class MyArrayQueueTest {
    public static void fill(ArrayQueue queue, String prefix, int size) {
        for (int i = 0; i < size+1; i++) {
            queue.enqueue(prefix + i);
        }
    }

    public static void test(ArrayQueue queue, String prefix) {
        System.out.println("======QUEUE_BASE_TEST======");
        for (int i = 0; i < queue.size()/2; i++) {
            System.out.println(
                    queue.size() + " " +
                            queue.element() + " " +
                            queue.dequeue()
            );
        }
        fill(queue, prefix, 20);
        while (!queue.isEmpty()) {
            System.out.println(
                    queue.size() + " " +
                            queue.element() + " " +
                            queue.dequeue()
            );
        }

        fill(queue, prefix, 20);
        System.out.println("======QUEUE_CLEAR_TEST======");
        System.out.println("before cleaning size: " + queue.size() + " isEmpty: " + queue.isEmpty());
        queue.clear();
        System.out.println("after cleaning size: " + queue.size() + " isEmpty: " + queue.isEmpty());

        fill(queue, prefix, 20);
        System.out.println("======QUEUE_COUNTIF_TEST======");
        Predicate<Object> isString = i -> i.getClass() == String.class;
        System.out.println("Expected: 21, Actual: " + queue.countIf(isString));
    }

    public static void main(String[] args) {
        ArrayQueue queue1 = new ArrayQueue();
        ArrayQueue queue2 = new ArrayQueue();
        fill(queue1, "q1_", 20);
        fill(queue2, "q2_", 20);

//        System.err.println(Arrays.toString(queue1.elements) + " " + queue1.head + " " + queue1.elements[queue1.tail]);
        System.out.println("\n======Q1_TEST======");
        test(queue1, "q1_");
        System.out.println("\n======Q2_TEST======");
        test(queue2, "q2_");
    }
}
