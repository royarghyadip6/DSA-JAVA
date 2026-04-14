package MultiThread.Problems;
/*

📌 Problem Statement
You are given two threads:
    Thread A prints numbers from 1 to 5
    Thread B prints numbers from 6 to 10

👉 Requirement:
    Ensure Thread A completes execution first
    Only after that, Thread B should start

* */

import java.util.concurrent.atomic.AtomicInteger;

public class XII_Join_Problem {
    public static void main(String[] args) throws InterruptedException {
        Thread thread_A = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                System.out.println(Thread.currentThread().getName() + "\t" + i);
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
            }
        },"Thread_A");
        Thread thread_B = new Thread(() -> {

            for (int i = 0; i < 5; i++) {
                System.out.println(Thread.currentThread().getName() + "\t" + i);
            }
        },"Thread_B");
        Thread thread_C = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                System.out.println(Thread.currentThread().getName() + "\t" + i);
            }
        },"Thread_C");
        Thread thread_D = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                System.out.println(Thread.currentThread().getName() + "\t" + i);
            }
        },"Thread_D");

//        thread_A.start();
        thread_B.setPriority(10);
        thread_B.start();
        thread_A.start();
        thread_B.join();
        thread_C.start();
        System.out.println(">>>>>>>>>>>>>>>> Join End");
        thread_D.start();
    }
}

