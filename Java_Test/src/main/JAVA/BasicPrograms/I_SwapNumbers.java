package BasicPrograms;

public class I_SwapNumbers {
    public static void main(String[] args) {
              // Swap without using 3rd variable
        int a = 10;
        int b = 20;
        System.out.println("a:"+a+" b:"+b);
//        a = a^b;
//        b= a^b;
//        a= a^b;
        a = a+b; //30
        b = a-b; //10
        a = a-b; //20
        System.out.println("a:"+a+" b:"+b);
    }
}
