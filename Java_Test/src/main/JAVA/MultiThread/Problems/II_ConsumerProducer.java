package MultiThread.Problems;

import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 Problem Statement
 We have two types of threads:
 Producer Thread > Produces data > Adds it to a shared buffer (queue)
 Consumer Thread > Consumes data > Removes it from the buffer

 Conditions
 If the buffer is full → Producer must wait
 If the buffer is empty → Consumer must wait

 Example flow:
 Producer → produces item → puts in queue
 Consumer → takes item → processes it
 * */

public class II_ConsumerProducer {
    public static void main(String[] args) {
        SharedBuffer  sharedBuffer = new SharedBuffer();

        Thread threadProducer = new Thread(() -> {
            int value = 0;
                try {
                    while (true) {
                        sharedBuffer.producer(value++);
                        Thread.sleep(500);
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
        });

        Thread threadConsumer = new Thread(() -> {
            while (true) {
                sharedBuffer.consumer();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        threadConsumer.start();
        threadProducer.start();
    }
}

class SharedBuffer {
    Queue<Integer> queue = new LinkedList<>();
    int capacity = 5;

    synchronized public void consumer () {
        while (queue.size() == 0) {
            try{
                System.out.println("Empty queue. Waiting for producer thread");
                wait();
            } catch (InterruptedException e){
                e.printStackTrace();
            }
        }
        System.out.println("Consumer : " + queue.poll());
        notifyAll();
    }

    synchronized public int producer (int value) {
        while (queue.size() == capacity) {
            try {
                System.out.println("Queue is full. Waiting for producer thread");
                wait();
            }  catch (InterruptedException e){
                e.printStackTrace();
            }
        }
        System.out.println("Producer : " + value);
        queue.add(value);
        notifyAll();
        return value;
    }
}


