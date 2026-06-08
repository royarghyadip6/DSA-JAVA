import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Test1 {

    public static void main(String[] args) {
        List<Integer> listInteger = Arrays.asList(1, 2, 3, 4, 5, 60, 10, 20, 25, 50);
        List<String> listString = Arrays.asList("apple", "bat", "cat", "banana");
        List<String> words = Arrays.asList("apple","banana","apple","orange","banana","apple");
        int[] arrInt = {1, 2, 3, 4, 5, 6};

        List<Developer> team = Arrays.asList(
                new Developer("Alice", Arrays.asList("Java", "Python", "SQL", "React")),
                new Developer("Bob", Arrays.asList("Java", "JavaScript", "React", "C#")),
                new Developer("Charlie", Arrays.asList("React", "Java", "SQL", "Go"))
        );

        List<Employee> employees = Arrays.asList(
                new Employee(1, "Alice", "IT", 120000L, "Senior Developer"),
                new Employee(2, "Bob", "HR", 60000L, "Recruiter"),
                new Employee(3, "Charlie", "IT", 95000L, "Java Developer"),
                new Employee(4, "David", "Finance", 110000L, "Analyst"),
                new Employee(5, "Eve", "Marketing", 75000L, "SEO Specialist"),
                new Employee(6, "Frank", "IT", 150000L, "Solution Architect"),
                new Employee(7, "Grace", "HR", 65000L, "HR Generalist"),
                new Employee(8, "Heidi", "Sales", 85000L, "Account Manager"),
                new Employee(9, "Ivan", "Finance", 105000L, "Controller"),
                new Employee(10, "Judy", "Sales", 90000L, "Sales Lead"),
                new Employee(11, "Tom", "IT", 96000L, "Java Developer")
        );

        List<Integer> list = List.of(1,2,3,4);

        Spliterator<Integer> s = list.spliterator();

        s.tryAdvance(System.out::print);
//        s.tryAdvance(System.out::print);
        


    }

}

class Developer {
    String name;
    List<String> languages;

    Developer(String name, List<String> languages) {
        this.name = name;
        this.languages = languages;
    }
}

class Employee {
    private int id;
    private String name;
    private String dept;
    private Long salary;
    private String role;


    public Employee(int id, String name, String dept, Long salary, String role) {
        this.id = id;
        this.name = name;
        this.dept = dept;
        this.salary = salary;
        this.role = role;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDept() {
        return dept;
    }

    public void setDept(String dept) {
        this.dept = dept;
    }

    public Long getSalary() {
        return salary;
    }

    public void setSalary(Long salary) {
        this.salary = salary;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", dept='" + dept + '\'' +
                ", salary=" + salary +
                ", role='" + role + '\'' +
                '}';
    }
}