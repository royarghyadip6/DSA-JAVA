/*
* GCD > Greatest Common Divisor
* HCF > Highest Common Factor
* Problem > We are given a and b two number. Find the largest divisor.
*
* The Euclidean Algorithm is a method to compute the greatest common divisor (GCD) of two integers.
* This is the implementation of euclidean algorithm using basic idea.
*         'b' be smaller than 'a'
*         gcd(a,b) = gcd(a-b,b)
*         Why?
*         Let g be the GCD of 'a' and 'b'
*         a=gx b=gy and GCD(x,y)=1
*         (a-b) = g(x-y)
*
* | Method              | Time Complexity        | Space Complexity       | Verdict                     |
* |---------------------|------------------------|------------------------|-----------------------------|
* | Brute-force         | O(min(a, b))           | O(1)                   | ❌ Too slow for large input |
* | Subtraction Euclid  | O(max(a, b))           | O(1)                   | ❌ Slower than brute in worst case |
* | Recursive Euclid    | O(log(min(a, b)))      | O(log(min(a, b)))      | ⚠️ Elegant but stack-heavy |
* | Iterative Euclid    | O(log(min(a, b)))      | O(1)                   | ✅ Best choice overall      |
*
* */

import java.util.Scanner;

public class V_GCDorHCF {
    public static void main(String[] args) {
        System.out.println("Greatest Common Divisor for two number");
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter Number1 : ");
        int a = sc.nextInt();
        System.out.print("Enter Number2 : ");
        int b = sc.nextInt();

        // Brute-force
        int result = getMySolution_GCD(a,b);
        System.out.println("getMySolution_GCD: Greatest Common Divisor for "+a +" and "+b +" is: "+result);

        // Subtraction Euclid
        int resultGFG1 = getBasicGFGSolution_GCD(a,b);
        System.out.println("getBasicGFGSolution_GCD: Greatest Common Divisor for "+a +" and "+b +" is: "+resultGFG1);

        // Recursive Euclid
        int resultGFG2 = getOptimizedGFGSolution_GCD(a,b);
        System.out.println("getOptimizedGFGSolution_GCD: Greatest Common Divisor for "+a +" and "+b +" is: "+resultGFG2);

        // Iterative Euclid
        int resultCGPT = getOptimizedIterativeSolution(a,b);
        System.out.println("getOptimizedIterativeSolution: Greatest Common Divisor for "+a +" and "+b +" is: "+resultCGPT);
    }


    private static int getMySolution_GCD(int a, int b) {
        int result = 1;
        int number = Math.min(a, b);

        for (int i = number; i > 1; i--) {
            if (a%i==0 && b%i==0) {
                result = i;
                break;
            }
        }
        return result;
    }


    private static int getBasicGFGSolution_GCD(int a, int b) {
        System.out.println("A:"+a+" B:"+b);
        while (a != b) {
            if(a > b) {
                a = a-b;
            } else {
                b = b-a;
            }
            System.out.println("A:"+a+" B:"+b);
        }
        return a;
    }

    private static int getOptimizedGFGSolution_GCD(int a, int b) {
        if (b==0) {
            System.out.println("A:" + a + "     B:" + b);
            return a;
        } else {
            System.out.println("A:" + a + "     B:" + b);
            return getOptimizedGFGSolution_GCD(b, a % b);
        }
    }

    private static int getOptimizedIterativeSolution(int a, int b) {
        while (b != 0) {
            int temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }

}
