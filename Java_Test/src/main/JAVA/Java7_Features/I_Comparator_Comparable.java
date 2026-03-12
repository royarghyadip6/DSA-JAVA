package Java7_Features;

import java.util.*;


/**
 * With Comparator, you can create many comparator classes, each defining a different sorting rule.
 *
 * Sorting By Student Age
 */
class AgeComparator implements Comparator<Student> {
    @Override
    public int compare(Student o1, Student o2) {
        return Integer.compare(o1.age,o2.age);
    }
}

class NameComparator implements Comparator<Student> {
    @Override
    public int compare(Student o1, Student o2) {
        return o1.name.compareTo(o2.name);
    }
}


/**
 When a class implements Comparable, the sorting logic is written inside the class using compareTo().
 Because a class can implement only one compareTo() method, it defines only one natural sorting rule.
 */
class Student implements Comparable<Student>{
    String name;
    int age;
    int rollNo;
    String dept;
    String city;
    float score;

    /**
     *
     * @param name
     * @param age
     * @param rollNo
     * @param dept
     * @param city
     * @param score
     */
    public Student(String name, int age, int rollNo, String dept, String city, float score) {
        this.name = name;
        this.age = age;
        this.rollNo = rollNo;
        this.dept = dept;
        this.city = city;
        this.score = score;
    }

    /**
     * The list will always be sorted by any one variable.
     * Currently, it will always sort by city.
     * For roll : return this.rollNo - other.rollNo;
     * You cannot directly sort by name or age using Comparable unless you change the code.
     * @param other the object to be compared.
     * @return
     */
    @Override
    public int compareTo(Student other) {
        return this.city.compareTo(other.city);
    }

    @Override
    public String toString() {
        return "Student{" +
                "Name='" + name + '\'' +
                ", age=" + age +
                ", rollNo=" + rollNo +
                ", dept='" + dept + '\'' +
                ", city='" + city + '\'' +
                ", score=" + score +
                '}';
    }
}

public class I_Comparator_Comparable {
    public static void main(String[] args) {
        List<Student> students = Arrays.asList(
                new Student("Aarav Sharma", 20, 111, "CS", "Bangalore", 88.5f),
                new Student("Priya Singh", 24, 132, "ECE", "Delhi", 92.0f),
                new Student("Rahul Verma", 22, 143, "ME", "Mumbai", 75.5f),
                new Student("Sneha Gupta", 29, 124, "CS", "Pune", 95.0f),
                new Student("Vikram Rao", 23, 155, "ECE", "Chennai", 81.2f),
                new Student("Ananya Das", 21, 106, "ME", "Kolkata", 88.5f),
                new Student("Amit Patel", 22, 137, "CS", "Ahmedabad", 70.0f),
                new Student("Ishan Khan", 20, 108, "ECE", "Bangalore", 85.0f),
                new Student("Kavita Iyer", 25, 159, "ME", "Chennai", 91.5f),
                new Student("Rohan Shah", 22, 110, "CS", "Mumbai", 79.0f)
        );

        System.out.println("######## Comparable #########");
        Collections.sort(students);
        for (Student s : students){
            System.out.println(s);
        }

        System.out.println("######## Comparator #########");

        System.out.println("Comparing by name ");
        Collections.sort(students, new NameComparator()); // sort by name
        for (Student s : students){
            System.out.println(s);
        }

        System.out.println("Comparing by age ");
        Collections.sort(students, new AgeComparator());  // sort by age
        for (Student s : students){
            System.out.println(s);
        }

        System.out.println("Comparing by roll with lambda expression.");
        Collections.sort(students, (s1,s2) -> Integer.compare(s1.rollNo,s2.rollNo)); // sort by roll
        for (Student s : students){
            System.out.println(s);
        }

        System.out.println("Comparing by score with lambda expression.");
        students.sort(Comparator.comparingDouble(s -> s.score)); // sort by score
        for (Student s : students){
            System.out.println(s);
        }

    }
}


/*

| Feature                   | Comparable                      | Comparator                           |
| ------------------------- | ------------------------------- | ------------------------------------ |
| Package                   | `java.lang`                     | `java.util`                          |
| Method Used               | `compareTo()`                   | `compare()`                          |
| Sorting Logic             | Inside the class                | Outside the class                    |
| Sorting Type              | Natural / Default sorting       | Custom sorting                       |
| Number of Sorting Options | Only one sorting logic possible | Multiple sorting logics possible     |
| Modification of Class     | Class must implement Comparable | No need to modify the class          |
| Method Signature          | `int compareTo(T obj)`          | `int compare(T o1, T o2)`            |
| Example Sorting           | Student sorted by Roll No       | Student sorted by Name / Age / Marks |


* */