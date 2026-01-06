package MultiThread;

public class Th6_Join {
    public static void main(String[] args) throws InterruptedException {
/*
// Case 1 : Waiting of main thread util completion of child thread
        System.out.println("Waiting of main thread util completion of child thread");
        Th6_MyThread mth = new Th6_MyThread();
        mth.start();
        mth.join();
//        If we comment above line then both main and child thread will execute simultaneously, then we can't expect output.
//        If we are not commenting the above line then main thread calls join() on child thread object.
//        Hence main thread will wait until completing child thread. In this case output is child thread 10 times followed by main thread 10 times.
        for (int i = 0; i < 10; i++) {
            System.out.println("Main Thread 1");
        }
*/

/*
// Case 2 : Waiting of main thread till certain time
        System.out.println("Waiting of main thread till certain time");
        Th6_MyThread1 mth1 = new Th6_MyThread1();
        mth1.start();
        mth1.join(5000);
        for (int i = 0; i < 10; i++) {
            System.out.println("Main Thread 2");
        }
*/

/*
// Case 3 : Waiting of child thread until completing main thread
//        Here child thread calls join() main thread object, hence child thread has to wait untill completing main thread.
//        In this case, output is main thread 10 times followed by child thread 10 times.


        System.out.println("Waiting of child thread until completing main thread");
        Th6_MyThread2.mt = Thread.currentThread();
        Th6_MyThread2 mth2 = new Th6_MyThread2();
        mth2.start();
        for (int i = 0; i < 10; i++) {
            System.out.println("Main thread 3");
            Thread.sleep(1000);
        }
*/

/*
// Case 4 : If main thread calls join() on child thread object and child thread calls join() on main thread object then both threads will wait forever.
//        The program will stuck (this is something like deadlock)
        Th6_MyThread2.mt = Thread.currentThread();
        Th6_MyThread2 mth2 = new Th6_MyThread2();
        mth2.start();
        mth2.join();
*/

/*
// Case 5 : If a thread calls join() on the same thread itself then the program will be stuck.(this is something like deadlock)
//        In this case program will wait forever
        Thread.currentThread().join();
*/

    }
}

class Th6_MyThread extends Thread {
    @Override
    public void run() {
        for (int i = 0; i < 10; i++) {
            System.out.println("Child Thread 1");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

class Th6_MyThread1 extends Thread {
    @Override
    public void run() {
        for (int i = 0; i < 10; i++) {
            System.out.println("Child Thread 2");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

class Th6_MyThread2 extends Thread {
    static Thread mt;
    @Override
    public void run() {
        try {
            mt.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        for (int i = 0; i < 10; i++) {
            System.out.println("Child Thread 3 ");
        }
    }
}


/*
* If a thread wants to wait until completing some other thread, then we should go for join().
* for example, if a thread t1 wants to wait until completing t2, then t1 has to call t2.join().
* if t1 executes t2.join(), then immediately t1 will be entered into waiting state until t2 complets.
* once t2 completes then t1 can continue its execution.
*
*
* # Prototype of join()
* public final void join() throws InterruptedException
* public final void join(long ms) throws InterruptedException
* public final void join(long ms, int ns) throws InterruptedException
* Note: Every join() throws InterruptedException which is checked exception hence compulsorily this exception
*  either by try catch or by using throws, otherwise e will get compile time error.
*
* # LifeCycle of join() > Please check this image Th6_Join_Img1.png
*
* */