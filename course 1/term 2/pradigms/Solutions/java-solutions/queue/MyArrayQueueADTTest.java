package queue;

import java.util.function.Predicate;

public class MyArrayQueueADTTest {
    public static void fill(ArrayQueueADT queue, String prefix, int size) {
        for (int i = 0; i < size+1; i++) {
            ArrayQueueADT.enqueue(queue, prefix + i);
        }
    }

    public static void test(ArrayQueueADT queue, String prefix) {
        System.out.println("======QUEUE_BASE_TEST======");
        for (int i = 0; i < ArrayQueueADT.size(queue)/2; i++) {
            System.out.println(
                    ArrayQueueADT.size(queue) + " " +
                            ArrayQueueADT.element(queue) + " " +
                            ArrayQueueADT.dequeue(queue)
            );
        }
        fill(queue, prefix, 20);
        while (!ArrayQueueADT.isEmpty(queue)) {
            System.out.println(
                    ArrayQueueADT.size(queue) + " " +
                            ArrayQueueADT.element(queue) + " " +
                            ArrayQueueADT.dequeue(queue)
            );
        }

        fill(queue, prefix, 20);
        System.out.println("======QUEUE_CLEAR_TEST======");
        System.out.println("before cleaning size: " + ArrayQueueADT.size(queue) + " isEmpty: " + ArrayQueueADT.isEmpty(queue));
        ArrayQueueADT.clear(queue);
        System.out.println("after cleaning size: " + ArrayQueueADT.size(queue) + " isEmpty: " + ArrayQueueADT.isEmpty(queue));

        fill(queue, prefix, 20);
        System.out.println("======QUEUE_COUNTIF_TEST======");
        Predicate<Object> isString = i -> i.getClass() == String.class;
        System.out.println("Expected: 21, Actual: " + ArrayQueueADT.countIf(queue, isString));
    }

    public static void main(String[] args) {
        ArrayQueueADT queue1 = ArrayQueueADT.create();
        ArrayQueueADT queue2 = ArrayQueueADT.create();
        fill(queue1, "q1_", 20);
        fill(queue2, "q2_", 20);
        System.out.println("\n======Q1_TEST======");
        test(queue1, "q1_");
        System.out.println("\n======Q2_TEST======");
        test(queue2, "q2_");
    }
}
