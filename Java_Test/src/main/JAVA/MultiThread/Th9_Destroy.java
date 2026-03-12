package MultiThread;

public class Th9_Destroy extends Thread
{
    Th9_Destroy(String threadname, ThreadGroup tg)
    {
        super(tg, threadname);
        start();
    }
    public void run()
    {
        for (int i = 0; i < 2; i++)
        {
            try
            {
                Thread.sleep(10);
            }
            catch (InterruptedException ex) {
                System.out.println("Exception encounterted");}
        }
        System.out.println(Thread.currentThread().getName() +
                " finished executing");
    }
    public static void main(String arg[]) throws InterruptedException, SecurityException
    {
        // creating a ThreadGroup
        ThreadGroup g1 = new ThreadGroup("Parent thread");
        // creating a child ThreadGroup for parent ThreadGroup
        ThreadGroup g2 = new ThreadGroup(g1, "child thread");

        // creating a thread
        Th9_Destroy t1 = new Th9_Destroy("Thread-1", g1);
        // creating another thread
        Th9_Destroy t2 = new Th9_Destroy("Thread-2", g1);

        // block until other thread is finished
        t1.join();
        t2.join();

        // destroying child thread
        g2.destroy();
        System.out.println(g2.getName() + " destroyed");

        // destroying parent thread
        g1.destroy();
        System.out.println(g1.getName() + " destroyed");
    }
}