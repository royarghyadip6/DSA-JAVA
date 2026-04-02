package MultiThread.Problems;

public class VII_Using3ThreadPrintABC {
    static PrintABC print = new PrintABC();
    public static void main(String[] args) {
        Thread thread1 = new Thread(()-> print.printABC('A',0), "thread1");
        Thread thread2 = new Thread(()-> print.printABC('B',1), "thread2");
        Thread thread3 = new Thread(()-> print.printABC('C',2), "thread3");
        thread1.start();
        thread2.start();
        thread3.start();
    }
}

class PrintABC  {

    int count = 0;
    synchronized public void printABC(char ch,int threadId) {
        while (count < 20) {
            try {
                while (count % 3 != threadId) {
                    wait();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println(Thread.currentThread().getName() +" > "+ch);
            count++;
            notifyAll();
        }
    }
}