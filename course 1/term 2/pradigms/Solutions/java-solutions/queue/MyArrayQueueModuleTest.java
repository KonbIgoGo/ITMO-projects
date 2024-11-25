package queue;

import java.util.function.Predicate;

public class MyArrayQueueModuleTest {
    public static void fill(int size) {
        for (int i = 0; i < size+1; i++) {
            ArrayQueueModule.enqueue(i);
        }
    }

    public static void test() {
        System.out.println("======QUEUE_BASE_TEST======");
        for (int i = 0; i < ArrayQueueModule.size()/2; i++) {
            System.out.println(
                    ArrayQueueModule.size() + " " +
                            ArrayQueueModule.element() + " " +
                            ArrayQueueModule.dequeue()
            );
        }
        fill(20);
        while (!ArrayQueueModule.isEmpty()) {
            System.out.println(
                    ArrayQueueModule.size() + " " +
                            ArrayQueueModule.element() + " " +
                            ArrayQueueModule.dequeue()
            );
        }

        fill(20);
        System.out.println("======QUEUE_CLEAR_TEST======");
        System.out.println("before cleaning size: " + ArrayQueueModule.size() + " isEmpty: " + ArrayQueueModule.isEmpty());
        ArrayQueueModule.clear();
        System.out.println("after cleaning size: " + ArrayQueueModule.size() + " isEmpty: " + ArrayQueueModule.isEmpty());

        fill(20);
        System.out.println("======QUEUE_COUNTIF_TEST======");
        Predicate<Object> isInteger = i -> i.getClass() == Integer.class;
        System.out.println("Expected: 21, Actual: " + ArrayQueueModule.countIf(isInteger));
    }

    public static void main(String[] args) {
        fill(10);
        test();
    }
}
