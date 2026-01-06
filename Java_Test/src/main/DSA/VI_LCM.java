import java.util.Scanner;

/*
 * LCM > Least Common Multiple
 * we are given a and b two number. We need to find the lowest common multiple.
 *
 * Time complexity is equal with GCD.
 * */
public class VI_LCM {
    public static void main(String[] args) {
        System.out.println("Least Common Multiple for two number");
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter Number1 : ");
        int a = sc.nextInt();
        System.out.print("Enter Number2 : ");
        int b = sc.nextInt();

        int result = getMySolution_LCM(a,b);
        System.out.println("getMySolution_LCM: Least Common Multiple for "+a +" and "+b +" is: "+result);

        int resultGFG1 = getGFGSolution_LCM(a,b);
        System.out.println("getOptimizedGFGSolution_LCM: Least Common Multiple for "+a +" and "+b +" is: "+resultGFG1);

        int resultCGPT = getOptimizedSolution_LCM(a,b);
        System.out.println("getOptimizedSolution_LCM: Least Common Multiple for "+a +" and "+b +" is: "+resultCGPT);
    }

    private static int getMySolution_LCM(int a, int b) {
        int number = Math.max(a,b);
        int result = 0;
        for (int i = number; i < a*b; i++) {
            if (i%a==0 && i%b==0) {
                result = i;
                break;
            }
        }
        return result;
    }

    /*
    * (a*b) = lcm(a,b) * gcd(a,b)
    * lcm(a,b) = (a*b)/gcd(a,b)
    * */
    private static int getGFGSolution_LCM(int a, int b) {
        int gcd = getGcd(a,b);
        return (a*b)/gcd;
    }

    private static int getGcd(int a, int b) {
        if (b == 0) {
            return a;
        } else {
            return getGcd(b, a%b);
        }
    }

    private static int getOptimizedSolution_LCM(int a, int b) {
        int ab = a*b;
        while (b != 0) {
            int temp = b;
            b = a%b;
            a = temp;
        }
        System.out.println("GCD is :"+a);
        return ab/a;
    }
}
