/*
* Trailing zeros in factorial
* Count the numbers of zero from a factorial value
* | Method                    | Time Complexity | Space Complexity | Notes                                     |
* |---------------------------|-----------------|------------------|-------------------------------------------|
* | Count 5-factors (your code)| O(log n)       | O(1)             | ✅ Optimal method for trailing zeros       |
* | Brute-force (computing n!)| O(n) or worse   | O(n) or more     | ❌ Slow and causes overflow easily         |
*
* */

import java.util.Scanner;

public class IV_ZerosInFactorial {
    public static void main(String[] args) {
        System.out.println("Trailing zeros in factorial for a number");
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter a Number: ");
        int number = sc.nextInt();
        int result = getZerosInFactorial(number);
        System.out.println("Zeros at end for factorial of "+number +" is: "+result);

    }
    private static int getZerosInFactorial(int number) {
        int count = 0;

        /*
         * the below implementation is not possible
         * because for big number the factorial will be very large number
         *
         * */
//        double factorial = 1;
//        if (number == 0 || number == 1){
//            factorial =  1;
//        } else {
//            for (int i = 2; i <= number; i++) {
//                factorial *= i;
//            }
//        }
//        System.out.println("Factorial is :"+factorial);
//
//        while (factorial%10 == 0) {
//            count++;
//            factorial/=10;
//        }


        /*
        * Solution by me
        * 5 multipliers will make 0 at end of factorial number.
        * so below logic will check numbers divisible by 5, 25, 125, 625 etc.
        * for input 24 , only will be devided by 5
        * for input 100, only will be devided by 5 and 25
        * for input 126 , only will be devided by 5 ,25 and 125
        * for input 624 , only will be devided by 5 ,25 and 125
        * for input 626 , only will be devided by 5 ,25, 125 and 625
        * */
//        for (int i = 1; i < number; i++) {
//            double temp = Math.pow(5,i);
//            if (temp > number) {
//                break;
//            }
//            count += number/temp;
//        }


        /*
        * Efficient solution by GFG
        * time complexity = θ(log n)
        * */
        for (int j = 5; j <= number; j*=5) {
            count += number/j;
        }
        return count;
    }
}
