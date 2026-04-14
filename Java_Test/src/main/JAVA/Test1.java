import java.util.LinkedList;
import java.util.Queue;

public class Test1 {

    public static void main(String[] args) {
        String str1 = "abc";
        String str2 = new String("abc");
        System.out.println(str1 == str2);
        System.out.println(str1.equals(str2));

        String  str3 = "abc";
        String str4 = str3.intern();
        String  str5 = "abc";



        Integer a = 158;
        Integer b = new Integer(158);
        System.out.println(a == b);
        System.out.println(a.equals(b));

        Integer c = 14;
        System.out.println(c == 121);
        System.out.println(c == 130);



    }
}