package MultiThread.Problems;

/**
 Goal:
 Two threads print numbers 1 → 10
 Thread 1 → prints odd numbers
 Thread 2 → prints even numbers
 Output must be in order

 Example output:
 OddThread : 1
 EvenThread : 2
 OddThread : 3
 EvenThread : 4
 ...
 * */

public class I_EvenOddNumber {
    public static void main(String[] args) {
        PrintNumbers printNumbers = new PrintNumbers();

        Thread thEven = new Thread(()->printNumbers.printEven(),"Even-Thread");
        Thread thOdd = new Thread(()->printNumbers.printOdd(),"Odd-Thread");

        thEven.start();
        thOdd.start();
    }
}

class PrintNumbers{

    int min = 1;
    int max = 10;

    synchronized public void printEven(){
        System.out.println("Print Even");
        try{
            while (min<= max ) {
                if (min%2 != 0) {
                    System.out.println("even Waiting start");
                    wait();
                    System.out.println("> EVEN WAITING END <");
                } else {
                    System.out.println("Even "+Thread.currentThread().getName()+" > "+min);
                    min++;
                    notify();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    synchronized public void printOdd() {
        System.out.println("Print Odd");
        try {
            while (min<= max ) {
                if (min%2 == 0) {
                    System.out.println("odd Waiting start ");
                    wait();
                    System.out.println(">ODD WAITING END <");
                } else {
                    System.out.println("Odd "+Thread.currentThread().getName()+" > "+min);
                    min++;
                    notify();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}