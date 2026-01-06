package MultiThread;

public class Th5_Yield {
    public static void main(String[] args) {
        Th5_MyThread th = new Th5_MyThread();
        th.start();
        for (int i = 0; i < 10; i++) {
            System.out.println("Main thread");
        }
    }
}

class Th5_MyThread extends Thread {
    @Override
    public void run() {
        for (int i = 0; i < 10; i++) {
            System.out.println("Child thread");
            Thread.yield();
        }
    }
}


/*
* https://www.youtube.com/watch?v=AZuwWOURi2Y&list=PLd3UqWTnYXOmSRqEtbHuApVczTwaJVv-V&index=10
*
* Prevent thread from execution
* we can Prevent a thread execution by using the following methods
* 1) Yield()
* 2) Join()
* 3) Sleep()
*
* ## Yield()
*   Yield() causes to pass the current executing thread to give the chance for waiting thread of same priority.
* If there is no waiting thread or all waiting thread have low priority, then same thread can continue the execution.
* if multiple threads are waiting with same priority then which waiting thread will get the chance we can't expect it depends on thread scheduler.
* The thread which is yielded, when it will get the chance once again it depends on thread scheduler. we can't expect exactly.
*
* public static native void yield() | native means not implemented using java
*
*   1. MT t = new MT()              > Born/New state
*   2. t.start()                    > Run/Runnable state
*   3. if TS allocates processor    > Running state
*   4. if run() completed           > Dead state
*   5. if thread.yield() is called  > immediately it will move from running state to runnable state
*
*
* In the above program if we comment Thread.yield(); then both thread will execute simultaneously. which thread will complete 1st that we can't expect.
* if we dont comment then child always call yield method because of that main thread will get chance more number of time .the chance of completing main thread is 1st.
* some platform won't provide proper support for yield method.
*
* */