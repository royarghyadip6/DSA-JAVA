/*
* Find prime factors of a given number
* */

import java.util.Scanner;

public class VIII_PrimeFactor {
    public static void main(String[] args) {
        System.out.println("Find prime factors of a given number");
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter a Number: ");
        int number = sc.nextInt();

        // Naive (n log n)
        String result = getPrimeFactor(number);
        System.out.println("Prime Factors of "+number+" are: "+result);


        // Optimized
        String result1 = getPrimeFactorOptimized(number);
        System.out.println("getPrimeFactorOptimized : Prime Factors of "+number+" are: "+result1);

        //
        String result2 = getPrimeFactorEfficient(number);
        System.out.println("getPrimeFactorEfficient : Prime Factors of "+number+" are: "+result2);

    }
    
    private static String getPrimeFactor(int number) {
        StringBuilder str = new StringBuilder();
        for (int i = 2; i < number; i++) {
            if (isPrime(i) ) {
                int temp = i;
                while (number%temp==0) {
                    str.append(i).append(" ");
                    temp*=i;
                }
            }
        }
        return new String(str);
    }

    private static boolean isPrime(int number1) {
        if (number1 == 1) {
            return false;
        }
        if (number1 == 2 || number1 == 3) {
            return true;
        }
        if (number1%2 == 0 || number1%3 == 0) {
            return false;
        } else {
            for (int j = 5; j*j <= number1 ; j+=6) {
                if (number1%j == 0 || number1%(j+2) == 0) {
                    return false;
                }
            }
            return true;
        }
    }


    private static String getPrimeFactorOptimized(int number) {
        String result = "";
        if (number <= 1) {
            return result;
        }
        for (int i = 2; i*i <= number; i++) {
            while (number%i == 0){
                result += i + " ";
                number/=i;
            }
        }
        if (number >1) {
            result += number;
        }

        return result;
    }

    private static String getPrimeFactorEfficient(int number) {
        String result = "";
        if ( number <= 1 ) {
            return result;
        }
        while (number%2 == 0 ) {
            result += "2 ";
            number/=2;
        }
        while (number%3 == 0 ) {
            result += "3 ";
            number/=3;
        }
        for (int i = 5; i <= number; i+=6) {
            if (number%i == 0 || number%(i+2) == 0) {
                result += i + " ";

            }

        }
        return "";
    }

}
