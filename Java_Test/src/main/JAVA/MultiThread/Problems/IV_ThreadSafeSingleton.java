package MultiThread.Problems;

// Ensure only one instance of a class is created even in a multithreaded environment.

public class IV_ThreadSafeSingleton {
    public static void main(String[] args) {
        Runnable task = () -> {
            Singleton obj = Singleton.getInstance();
            System.out.println(Thread.currentThread().getName() + " -> " + obj.hashCode());
        };

        Thread t1 = new Thread(task, "T1");
        Thread t2 = new Thread(task, "T2");

        t1.start();
        t2.start();
    }
}
class Singleton {
    // 1. Private static variable (volatile ensures visibility across threads)
    private static volatile Singleton instance;

    // 2. Private constructor (prevents other classes from using 'new')
    private Singleton(){}

    // 3. Public static method to get the single instance
    public static Singleton getInstance() {
        if (instance == null) { // First check (no locking)
            synchronized (Singleton.class) {
                if (instance == null)   // Second check (with locking)
                    instance = new Singleton();
            }
        }
        return instance;
    }
}