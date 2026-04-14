import java.util.Objects;

class Emp
{
    Integer emp;

    public Integer getEmp() {
        return emp;
    }

    public void setEmp(Integer emp) {
        this.emp = emp;
    }

    public Emp(Integer emp) {
        this.emp = emp;
    }

    @Override
    public String toString() {
        return "Emp{" +
                "emp=" + emp +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Emp emp1 = (Emp) o;
        return Objects.equals(emp, emp1.emp);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(emp);
    }
}


public class Test2 {
    public static void main(String[] args) {
        String  str1 = "ABC";
        String  str2 = new String("ABC"); // heap
        String  str10 = "";
        String str3 = str1.intern(); //heap

        System.out.println(str1 == str2); // false
        System.out.println(str1.equals(str2)); // true
        System.out.println(str1 == str3); //
        System.out.println(str1.equals(str3)); //
        System.out.println(str1 == str10);  // true
        System.out.println("try : "+str3); // true
        System.out.println(str2 == str10);  // false
        System.out.println(str2.equals(str10));  // true

        Integer a = 158;
        Integer b = 158;
        Integer c = 300;
        Integer d = 300;

        System.out.println(a == b); // false
        System.out.println(a.equals(b)); // true
        System.out.println(c == d);    // false
        System.out.println(c.equals(d)); //true
        Emp emp1 = new Emp(1);
        Emp emp2 = new Emp(1);
        System.out.println(emp1.equals(emp2));

    }
}


 // String