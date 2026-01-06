package MultiThread;

/*
public class Th7_Sleep {
    public static void main(String[] args) throws InterruptedException{
        for (int i = 1; i <= 10; i++) {
            System.out.println("Slide "+i);
            Thread.sleep(1000);
        }
    }
}
*/

/*
* If a thread dont want to perform for a particular amount of time then we should go for sleep()
*  Signatures of Sleep() >
* public static native void sleep(long ms) throws InterruptedException
* public static void sleep(long ms,int ns) throws InterruptedException
*
*  Note: Every sleep() throws InterruptedException, which is checked exception
*  Hence when we are using sleep() compulsory we should handle InterruptedException, Either by try catch/ throws keyword.
*
*/

/*public class Th7_Sleep {
    public static void main(String[] args) {
        Th7_MyThread1 th1 = new Th7_MyThread1();
        th1.start();
        th1.interrupt();    // Comment_line1
        System.out.println("Main thread after interrupt");
    }
}

class Th7_MyThread1 extends Thread {
    @Override
    public void run() {
        try {
            for (int i = 0; i < 10; i++) {
                System.out.println("I am lazy thread.");
                Thread.sleep(2000);
            }
        } catch (InterruptedException e) {
            System.err.println("I got interrupted.");
        }
    }
}*/
/*
* Signature of interrupt() > public void interrupt()
* Q1 > How a thread can interrupt another thread?
* Ans > A thread can interrupt a sleeping/waiting thread by using interrupt() of thread class.
*   If we comment Comment_line1 then main thread wont interrupt child thread. In this case child will execute child thread 10 times.
*   If we are not commenting line 1then main thread interrupt child thread , in thid case output is
        Main thread after interrupt
        I am lazy thread.
        I got interrupted.
* Whenever we are calling interrupt(), If the target is not in sleeping/waiting state, then there is no impact of interrupt call immediately.
* Interrupt call will be waited until terget thread entered into sleeping or waiting state.
* If the target thread entered into sleeping or waiting state, then immediately interrupt call will interrupt the target thread.
* If the target thread never entered into sleeping or waiting state in its lifetime, then there is no impact of interrupt call.
* This is the only case where interrupt call will be wasted.
* */

public class Th7_Sleep {
    public static void main(String[] args) {
        Th7_MyThread1 th1 = new Th7_MyThread1();
        th1.start();
        System.out.println("Calling Interrupt()");
        th1.interrupt();
        System.out.println("Main thread end");
    }
}

class Th7_MyThread1 extends Thread {
    @Override
    public void run() {
        for (int i = 0; i < 50000; i++) {
            System.out.println("I am lazy thread. "+i);
        }
        try {
            System.out.println("Sleeping now.");
            Thread.sleep(2000);
            System.out.println("After sleeping");
        } catch (InterruptedException e) {
            System.err.println("I got interrupted.");
        }
    }
}

/*
| **Property**               | **yield()**                                                        | **join()**                                               | **sleep()**                                         |
| -------------------------- | ------------------------------------------------------------------ | -------------------------------------------------------- | --------------------------------------------------- |
| **Definition**             | Suggests the current thread is willing to pause and let others run | Waits for another thread to complete                     | Pauses current thread for a specified time          |
| **Thread Control**         | Affects current thread                                             | Affects current thread by waiting for another thread     | Affects current thread                              |
| **Time-based?**            | ❌ No                                                               | ❌ No                                                     | ✅ Yes (requires time in milliseconds)               |
| **Returns Early?**         | ✅ Can resume immediately if no other threads need CPU              | ❌ Only after the target thread finishes                  | ❌ Only after the specified time elapses             |
| **Interruptible?**         | ✅ Yes (via `InterruptedException`)                                 | ✅ Yes (via `InterruptedException`)                       | ✅ Yes (via `InterruptedException`)                  |
| **Use Case**               | To hint CPU scheduling; improve fairness in execution              | To wait until another thread completes before continuing | To delay execution without releasing the lock       |
| **Thread State Change**    | Runnable → Ready (depending on scheduler)                          | Current thread goes into **waiting** state               | Current thread goes into **timed waiting** state    |
| **Needs Another Thread?**  | ❌ No                                                               | ✅ Yes (needs a thread to join on)                        | ❌ No                                                |
| **Static Method?**         | ✅ Yes (`Thread.yield()`)                                           | ❌ No (`thread.join()`) requires a thread instance        | ✅ Yes (`Thread.sleep()`)                            |
| **Lock Held During Call?** | ✅ Yes (does not release locks)                                     | ✅ Yes (does not release locks unless manually released)  | ✅ Yes (locks not released unless manually released) |
*/