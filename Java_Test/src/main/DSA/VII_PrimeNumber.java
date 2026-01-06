/*
* Write a Program to Check Given Number is Prime or not
*
* A prime number is a natural number greater than 1 that has exactly two distinct positive divisors
* 1 is not prime number
* 2 is the smallest prime number
*
* | Method                     | Time Complexity | Space Complexity | Notes                                      |
* |----------------------------|-----------------|------------------|--------------------------------------------|
* | Naive                      | O(n)            | O(1)             | ❌ Too slow for large numbers              |
* | Optimized (sqrt n)         | O(√n)           | O(1)             | ✅ Faster for large inputs                 |
* | Efficient (6k ± 1)         | O(√n)           | O(1)             | ✅ Fastest for large number                |
*
* */

import java.util.Scanner;

public class VII_PrimeNumber {
    public static void main(String[] args) {
        System.out.println("Check Given Number is Prime or not");
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter a Number: ");
        int number = sc.nextInt();

        // Naive
        boolean result = checkPrimeNumber(number);
        System.out.println(number+" is Prime Number? "+result);

        // Efficient
        boolean result1 = checkPrimeNumberEfficient(number);
        System.out.println(number+" is Prime Number? "+result1);

        // Optimized
        boolean result2 = checkPrimeNumberOptimized(number);
        System.out.println(number+" is Prime Number? "+result2);
    }

    private static boolean checkPrimeNumber(int number) {
        if (number == 1 ) {
            return false;
        }
        for (int i = 2; i < number; i++) {
            if (number%i==0) {
                System.out.println("Divided by :"+i);
                return false;
            }
        }
        return true;
    }

    private static boolean checkPrimeNumberEfficient(int number) {
        if (number == 1 ) {
            return false;
        }
        for (int i = 2; i*i <= number; i++) {
            if (number%i==0) {
                System.out.println("Divided by :"+i);
                return false;
            }
        }
        return true;
    }

    private static boolean checkPrimeNumberOptimized(int number) {
        if (number == 1 ) {
            return false;
        }
        if (number == 2 || number == 3 ) {
            return true;
        }
        if (number%2==0 || number%3==0 ) {
            System.out.println("Divided by 2 or 3");
            return false;
        } else {
            for (int i = 5; i*i <= number ; i+=6) {
                if (number%i==0 || number%(i+2)==0) {
                    System.out.println("Divided by :"+i);
                    return false;
                }
            }
            return true;
        }
    }
}
