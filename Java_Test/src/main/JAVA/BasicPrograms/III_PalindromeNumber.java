package BasicPrograms;

public class III_PalindromeNumber {
    public static void main(String[] args) {
        int num = 12345321;
        int original = num;
        int temp = 0;
        while (num>0) {
            temp = temp * 10 + num%10;
            num=num/10;
        }
        if (original == temp){
            System.out.println(original +" palindrome "+temp);
        } else {
            System.out.println(original +" not palindrome "+ temp);
        }
    }
}
