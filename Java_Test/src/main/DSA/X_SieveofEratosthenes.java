import java.util.Scanner;

/*
* Sieve of Eratosthenes
* we are given a number find out all the prime number less than equal to the number.
*
* */
public class X_SieveofEratosthenes {
    public static void main(String[] args) {
        System.out.println("Find prime factors of a given number");
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter a Number: ");
        int inputNumber = sc.nextInt();

        // Naive (n log n)
        String result = getAllPrime(inputNumber);
        System.out.println("Prime Factors of "+inputNumber+" are: "+result);

    }

    private static String getAllPrime(int inputNumber) {
        String str ="";
        for (int i = 1; i <= inputNumber; i++) {
            if (isPrime(i)) {
                str += i +" ";
            }
        }
        return str;
    }

    private static boolean isPrime(int number) {
        if (number == 1) {
            return false;
        }
        if (number == 2 || number == 3 ) {
            return true;
        }
        if (number%2 == 0 || number%3 == 0) {
            return false;
        } else {
            for (int i = 5; i * i <= number; i += 6) {
                if (number % i == 0 || number % (i + 2) == 0) {
                    return false;
                }
            }
            return true;
        }
    }
}
