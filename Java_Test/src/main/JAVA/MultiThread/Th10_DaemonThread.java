package MultiThread;

public class Th10_DaemonThread extends Thread{

    public void run() {
        if (Thread.currentThread().isDaemon()) {
            System.out.println("This is daemon thread");
        } else {
            System.out.println("This is not a daemon thread");
        }
    }

    public static void main(String[] args) {

        Th10_DaemonThread t1 = new Th10_DaemonThread();
        t1.start();
        System.out.println("T1 isDaemon: "+t1.isDaemon());

        Th10_DaemonThread t2 = new Th10_DaemonThread();
        t2.setDaemon(true);
        t2.start();
        System.out.println("T2 isDaemon: "+t2.isDaemon());

    }
}


/*
*
* A daemon thread in Java is a background thread that provides support to user threads during program execution.
* These threads run in the background and automatically terminate when all user threads finish execution.
* In this chapter, we will learn what daemon threads are, how to create them, and how they behave in Java programs.
*
* https://www.tpointtech.com/daemon-thread-in-java
* */