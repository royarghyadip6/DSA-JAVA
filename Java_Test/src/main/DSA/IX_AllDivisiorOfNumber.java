import java.util.Scanner;

public class IX_AllDivisiorOfNumber {
    public static void main(String[] args) {
        System.out.println("Find divisor of a given number");
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter a Number: ");
        int number = sc.nextInt();

        // Naive (n)
        String result = getAllDivisior(number);
        System.out.println("getAllDivisior: Divisors of "+number+" are: "+result);
        
        // Optimized (sqrt(n))
        String result1 = getAllDivisiorOptimized(number);
        System.out.println("getAllDivisiorOptimized: Divisors of "+number+" are: "+result1);

        // Optimized2 (2 sqrt(n) = sqrt(n))
        String result2 = getAllDivisiorOptimized2(number);
        System.out.println("getAllDivisiorOptimized: Divisors of "+number+" are: "+result2);
    }

    private static String getAllDivisior(int number) {
        String res = "";
        for (int i = 1; i <= number; i++) {
            if (number%i == 0) {
                res += i + " ";
            }
        }
        return res;
    }


    private static String getAllDivisiorOptimized(int number) {
        String str = "";
        String reverseStr = "";
        for (int i = 1; i*i <= number; i++) {
            if (number%i == 0) {
                str += i + " ";
                if (i != number/i) {
                    reverseStr = " " + number/i + reverseStr;
                }

            }
        }
        return str + reverseStr;
    }

    private static String getAllDivisiorOptimized2(int number) {
        String str = "";
        int i ;
        for (i = 1; i*i <= number; i++) {
            if (number%i == 0) {
                str += i + " ";
            }
        }
        for ( ; i >=1 ; i--) {
            if (number%i == 0) {
                str += number/i + " ";
            }
        }
        return str;
    }
}
