package MultiThread;

public class Th8_Suspend extends Thread
    {
        public void run()
        {
            for(int i=1; i<5; i++)
            {
                try
                {
                    // thread to sleep for 500 milliseconds
                    sleep(500);
                    System.out.println(Thread.currentThread().getName());
                }catch(InterruptedException e){System.out.println(e);}
                System.out.println(i);
            }
        }
        public static void main(String args[])
        {
            // creating three threads
            Th8_Suspend t1=new Th8_Suspend ();
            Th8_Suspend t2=new Th8_Suspend ();
            Th8_Suspend t3=new Th8_Suspend ();
            // call run() method
            t1.start();
            t2.start();
            // suspend t2 thread
            t2.suspend();
            // call run() method
            t3.start();
            t2.resume();
            t3.stop();
        }
}


/*
* these methods are depricated
*
* */