package MultiThread;

public class Th2_ImplimentingRunnable {
    public static void main(String[] args) {
        MyRunnable mr =new MyRunnable();
        Thread thread = new Thread(mr);
        thread.start();
        for (int i = 0; i < 10; i++) {
            System.out.println("Main thread");
        }
    }
}

class MyRunnable implements Runnable {
    @Override
    public void run() {
        for (int i = 0; i < 10; i++) {
            System.out.println("Child thread");
        }
    }
}

/*
* We can define a thread by implementing runnable interface.
* Runnable interface is present in java.lang package and it contains only run().
*
*   Case Study:
*   MyRunnable mr =new MyRunnable();
*   Thread t1 = new Thread();
*   Thread t2 = new Thread(mr);
* 1) t1.start() > a new thread will be created but no output. calling empty run().
* 2) t2.start() > a new thread will be created which is responsible to call run().
* 3) t1.run() > Calling empty run() just like normal method. thread will not create without start().
* 4) t2.run() > Thread will be created with output. calling run().
* 5) mr.start() > we will get compile time error. MyRunnable class does not have start capability.
* 6) mr.run() > No new thread will be created and MyRunnable run() will be executed like normal method call.
*
* Q. which approach is best to define a thread?
*  Among 2 ways to define a thread. implement runnable approach is recommended.
*  In 1st approach our class always extends Thread class, there is no chance to extend other class.
*  In the 2nd approach, while implementing Runnable interface, we can extend any other class Hence we wont miss any inherit benefits.
*
* Q2. Thread class constructor.
*   Thread thread = new Thread();
*   Thread thread = new Thread(Runnable r);
*   Thread thread = new Thread(String name);
*   Thread thread = new Thread(Runnable r,String name);
*   Thread thread = new Thread(ThreadGroup g,Runnable r);
*   Thread thread = new Thread(ThreadGroup g,String name);
*   Thread thread = new Thread(ThreadGroup g,Runnable r,String name);
*   Thread thread = new Thread(ThreadGroup g,Runnable r,String name,long StackSize);
*   Durga's approach to define a thread > valid but not recommended
*       MyThread thread = new MyThread();
*       Thread t = new Thread(thread);
*       t.start();
*
* Q3.
* */