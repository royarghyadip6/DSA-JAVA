package BasicPrograms;

public class II_PalindromeString {
    public static void main(String[] args) {
        /*
        * Write a program to check the string is palindrome or not
        * */

        String str = "adhihda";
        String rev = "";
        for (int i = str.length()-1; i >= 0 ; i--) {
            rev += str.charAt(i);
        }
        if (str.equals(rev)){
            System.out.println(rev +" palindrome "+str);
        } else {
            System.out.println(rev +" not palindrome "+ str);
        }
    }
}
