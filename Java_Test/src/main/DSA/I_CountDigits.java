/*
* Count number of digits present in a given number
*
* | Method           | Time Complexity | Space Complexity | Notes                           |
* | ---------------- | --------------- | ---------------- | ------------------------------- |
* | Loop (your code) | O(log n)        | O(1)             | Simple, safe for all integers   |
* | Log-based        | O(1)            | O(1)             | Fast, but requires `Math.log10` |
*
*  */

import java.util.Scanner;

public class I_CountDigits {
    public static void main(String[] args) {
        System.out.println("Count number of digits present in a given number");
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter a Number: ");
        int number = sc.nextInt();

        // Loop Based
        int result = getDigitCount(number);
        System.out.println("Total count of digit is :"+result);

        // Log-based
        int result1 =getOptimizedDigitCount(number);
        System.out.println("Total count of digit is :"+result1);
    }

    public static int getDigitCount(int no){
        int count = 0;
        while (no > 0) {
            count++;
            no/=10;
        }
        return count;
    }

    public static int getOptimizedDigitCount(int no) {
        if (no == 0) return 1;
        return (int) Math.floor(Math.log10(no)) + 1;
    }

}
