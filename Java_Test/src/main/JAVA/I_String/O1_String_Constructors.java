package I_String;

public class O1_String_Constructors {
    public static void main(String[] args) {
//        public String()
        String str = new String();
        System.out.println("String length "+str.length());

//        public String(String original)
        String str1 = new String("Arghyadip");
        System.out.println(str1);

//        public String(char[] value)
        char [] chars2 = {'a', 'b', 'c', 'd', 'e'};
        String str2 = new String(chars2);
        System.out.println("Character array to String : "+str2);

//        public String(char[] value,int offset,int count)
        char [] chars3 = {'a', 'b', 'c', 'd', 'e'};
        String str3 = new String(chars3,2,3);
        System.out.println("Character Array with offset and count: "+str3);

//        public String(int[] codePoints,int offset,int count)
        int[] codePoints = {72, 101, 108, 108, 111}; // Unicode for "Hello"
        String str4 = new String(codePoints, 0, 5);
        System.out.println(str4); // Output: Hello


    }
}
