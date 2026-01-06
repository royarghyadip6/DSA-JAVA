/*
 * Calculate factorial for a given number
 *
 * | Method                  | Time Complexity | Space Complexity | Notes                                      |
 * |-------------------------|------------------|------------------|--------------------------------------------|
 * | Recursive Factorial     | O(n)             | O(n)             | Simple, but risk of StackOverflowError     |
 * | Iterative Factorial     | O(n)             | O(1)             | ✅ Preferred: fast, safe, and efficient     |
 * | Tail-Recursive (Java)   | O(n)             | O(n)             | Elegant, but Java lacks tail call opt.     |
 * | BigInteger Factorial    | O(n)             | O(log(result))   | Use for large numbers beyond `long` range  |
 * | Precomputed Lookup      | O(1)             | O(n)             | Fast repeated calls, requires prebuild     |
 * | Divide & Conquer (Advanced)| O(n)         | O(n)             | Parallelizable, overkill for small `n`     |
 *
 * */

import java.util.Scanner;

public class III_Factorial {
    public static void main(String[] args) {
        System.out.println("Calculate factorial for a given number");
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter a Number: ");
        int number = sc.nextInt();

        // Iterative Factorial
        int result = getFactorial(number);
        System.out.println("Loop: Factorial for "+number +" is: "+result);

        // Recursive Factorial
        result = getFactorialRecursion(number);
        System.out.println("Recursion: Factorial for "+number +" is: "+result);
    }

    private static int getFactorialRecursion(int number) {
        if (number == 0 || number == 1) {
            return 1;
        } else {
            return number * getFactorialRecursion(number-1);
        }
    }

    private static int getFactorial(int number) {
        int factorial = 1;
        if (number == 0 || number == 1){
            return 1;
        } else {
            for (int i = 2; i <= number; i++) {
                factorial *= i;
            }
        }
        return factorial;
    }
}
