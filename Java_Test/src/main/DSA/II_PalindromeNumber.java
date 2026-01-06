import java.util.Scanner;

/*
* Check if given number is palindrome or not
*
* | Method                        | Time Complexity | Space Complexity | Notes                                 |
* | ----------------------------- | --------------- | ---------------- | ------------------------------------- |
* | full reverse                  | O(log n)        | O(1)             | Simple and reliable                   |
*
* */
public class II_PalindromeNumber {
    public static void main(String[] args) {
        System.out.println("Check if given number is palindrome or not");
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter a Number: ");
        int number = sc.nextInt();

        // full reverse method
        boolean result = checkPalindrome(number);
        System.out.println("Number is palindrome :"+result);
    }

    private static boolean checkPalindrome(int number) {
        int reverse=0;
        int duplicate = number;
        while (number > 0) {
            int temp = number%10;
            reverse = reverse*10 + temp;
            number/=10;
        }
        return reverse == duplicate;
    }
}