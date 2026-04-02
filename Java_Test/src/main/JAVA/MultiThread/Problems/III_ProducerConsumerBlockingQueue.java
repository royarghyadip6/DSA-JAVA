package MultiThread.Problems;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class III_ProducerConsumerBlockingQueue {

    public static void main(String[] args) {

        // Shared buffer
        BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(5);

        // Producer Thread
        Thread producer = new Thread(() -> {
            int value = 0;
            try {
                while (true) {
                    queue.put(value); // blocks if full
                    System.out.println("Produced: " + value);
                    value++;
                    Thread.sleep(500);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "Producer");

        // Consumer Thread
        Thread consumer = new Thread(() -> {
            try {
                while (true) {
                    int item = queue.take(); // blocks if empty
                    System.out.println("Consumed: " + item);
                    Thread.sleep(800);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "Consumer");

        producer.start();
        consumer.start();
    }
}

/*
Compact version

BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(5);
// Producer
        new Thread(() -> {
            try {
                int i = 0;
                while (true) queue.put(i++);
            } catch (Exception e) {}
        }).start();
// Consumer
        new Thread(() -> {
            try {
                while (true) System.out.println(queue.take());
            } catch (Exception e) {}
        }).start();

* */