import java.util.LinkedList;
import java.util.Queue;

public class Test1 {
    public static void main(String[] args) {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Singleton obj = Singleton.getInstance();
                System.out.println(Thread.currentThread().getName() + " " + obj.hashCode());
            }
        };
        Thread thread1 = new Thread(runnable, "thread1");
        Thread thread2 = new Thread(runnable, "thread2");

        thread2.start();
        thread1.start();
    }
}

class Singleton{
    private static volatile Singleton instance;

    private Singleton(){
        System.out.println("Create Singleton instance");
    }

    public static Singleton getInstance(){
        if(instance == null){
            synchronized (Singleton.class){
                if(instance == null){
                    instance = new Singleton();
                }
            }
        }
        return instance;
    }
}