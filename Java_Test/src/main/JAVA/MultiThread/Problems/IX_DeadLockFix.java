package MultiThread.Problems;

public class IX_DeadLockFix {
    static final Object lock1 = new Object();
    static final Object lock2 = new Object();
    public static void main(String[] args) {
        Runnable r1 = new Runnable() {
            @Override
            public void run() {
                synchronized (lock1) {
                    System.out.println(Thread.currentThread().getName() +" locked lock1");
                    synchronized (lock2) {
                        System.out.println(Thread.currentThread().getName() +" locked lock2");
                    }
                }
            }
        };
        Runnable r2 = new Runnable() {
            @Override
            public void run() {
                synchronized (lock2) {
                    System.out.println(Thread.currentThread().getName() +" locked lock2");
                    synchronized (lock1) {
                        System.out.println(Thread.currentThread().getName() +" locked lock1");
                    }
                }
            }
        };

        Thread t1 = new Thread(r1);
        //Thread t2 = new Thread(r2); // DeadLock
        Thread t2 = new Thread(r1);   // Fix for deadLock use same runnable
        t1.start();
        t2.start();

    }

}
