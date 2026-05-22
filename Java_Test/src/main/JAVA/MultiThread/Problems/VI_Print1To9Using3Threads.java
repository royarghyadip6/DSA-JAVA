package MultiThread.Problems;


class NumberPrinter {

    private int number = 1;

    public synchronized void printNumbers(int remainder) {

        int MAX = 9;
        while (number <= MAX) {

            while (number % 3 != remainder && number <= MAX) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            if (number <= MAX) {
                System.out.println(Thread.currentThread().getName() + " : " + number);
                number++;
                notifyAll();
            }
        }
    }
}

public class VI_Print1To9Using3Threads {

    public static void main(String[] args) {

        NumberPrinter printer = new NumberPrinter();

        Thread t1 = new Thread(() -> printer.printNumbers(1), "Thread-1");
        Thread t2 = new Thread(() -> printer.printNumbers(2), "Thread-2");
        Thread t3 = new Thread(() -> printer.printNumbers(0), "Thread-3");

        t1.start();
        t2.start();
        t3.start();
    }
}

