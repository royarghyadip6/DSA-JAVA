package MultiThread;

public class Th1_Basic {
    public static void main(String[] args) {
        Th1_MyThread th1MyThread = new Th1_MyThread();
        th1MyThread.start();
        // Currently Number of thread is 2
        for (int i = 0; i < 10; i++) {
            // Executed by main thread
            System.out.println("Main Thread "+i);
        }

//        Overriding of start()
        Th1_Mythread2 th2 = new Th1_Mythread2();
        th2.start();
    }
}


class Th1_MyThread extends Thread{
    @Override
    public void run() {
        // Executed by child thread
        System.out.println("No-Arg run method");
        for (int i = 0; i < 10; i++) {
            System.out.println("Child thread "+i);
        }
    }

    public void run(int a) {
        System.out.println("Arg run method");
    }
}

class Th1_Mythread2 extends Thread{

    //    Overriding of start()
    @Override
    public synchronized void start() {
        super.start(); // new thread is registered and run() will be called.
        System.out.println("child start method");
    }

    @Override
    public void run() {
        System.out.println("Child run method");
    }
}



/*
* Thread Scheduler - It is the part of JVM. It is responsible to schedule threads
* i.e if multiple threads are waiting to get chance of execution, then which order that will be executed is decided by thread scheduler.
* we can't expect exact algo followed by thread scheduler. it is varied by JVM to JVM. Hence we cant expect thread execution order and exact out put.
* Hence whenever situation comes to multithreading there is no guaranty for exact output. But we can provided several possible outputs.
*
*
*
* Q2. Difference between t.start() and t.run() ?
* In case of t.start a new thread will be created which is responsible for the execution of run method.
* But in case of t.run() a new thread wont be created and run() will be executed just like a normal method call by main thread.
* hence in above program, if we replace t.start() by t.run() then the output is child thread 10 times followed by main thread.
* the total output will be executed by main thread.
*
* Q3. Importance of thread class start().
* It is responsible to register the thread with thread scheduler and all other mandatory activities.
* Hence without executing the start(). there is no chance of starting a new thread in java.
* Due to this thread class start() is considered as heart of multithreading.
*       start() {
*           1. register this thread with thread scheduler
*           2. Perform all other activities
*           3. invoke run()
*       }
*
* Q4. Overloading of run() is possible or not?
*  Yes. Overloading of run() is always possible But thread class start() can invoke no arg run().
*  The other overloaded run() we have to call like a normal method call.
*
* Q5. If we are not overriding run() ?
* If we are not overriding run() then these class run() will be executed which have empty implementation.
* Hence we wont get output.
* Note: it is highly recomended to override run(). Otherwise dont go for multithreading concept.
*
* Q6. Overriding of start()
*   If we override start() then our start() will be executed just like a normal method call. New thread wont be created.
*   note: It is not recommended to override start(). otherwise dont go for multithreading concept.
*
* Q7. Thread Life Cycle
*   1. MT t = new MT()              > Born/New state
*   2. t.start()                    > Run/Runnable state
*   3. if TS allocates processor    > Running state
*   4. if run() completed           > Dead state
*
* Q8. After starting a thread if we are trying to restart same thread runtime exception (IllegalThreadStateException)
*   MT t = new MT()
*   t.start();
*   t.start(); // Exception will be thrown
* */
