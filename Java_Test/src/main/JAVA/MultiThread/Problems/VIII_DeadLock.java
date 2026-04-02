package MultiThread.Problems;

/*
    Thread 1 → holds Lock A → waiting for Lock B
    Thread 2 → holds Lock B → waiting for Lock A
* */

public class VIII_DeadLock {
    static final Object a = new Object();
    static final Object b = new Object();

    public static void main(String[] args) {

        Thread t1 = new Thread(()->{
            synchronized (a) {
                System.out.println(Thread.currentThread().getName() +" acquires obj a > "+a);
                synchronized (b) {
                    System.out.println(Thread.currentThread().getName() +" acquires obj b> "+b);
                }
            }
        });
        Thread t2 = new Thread(()->{
            synchronized (b) {
                System.out.println(Thread.currentThread().getName() +" acquires obj b > "+b);
                synchronized (a) {
                    System.out.println(Thread.currentThread().getName() +" acquires obj a > "+a);
                }
            }
        });
        t1.start();
        t2.start();
    }
}
