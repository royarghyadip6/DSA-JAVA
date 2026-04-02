package Java8_Features.Problems;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class P_03_Employee {
    public static void main(String[] args) {

        List<Employee> employees = Arrays.asList(
                new Employee("Arjun", 28, 65000.0),
                new Employee("Sanya", 24, 50000.0),
                new Employee("Vikram", 35, 98000.0),
                new Employee("Meera", 30, 75000.0),
                new Employee("Rohan", 22, 45000.0),
                new Employee("Anjali", 29, 82000.0),
                new Employee("Kabir", 40, 120000.0),
                new Employee("Neha", 27, 60000.0),
                new Employee("Kabir", 40, 120000.0),
                new Employee("Ishan", 32, 88000.0),
                new Employee("Tara", 26, 55000.0)
        );

         HashMap<String,Double> employeeMap= (HashMap<String, Double>) employees.stream()
                .filter(employee ->  employee.getSalary() < 50000 || employee.getSalary() > 70000)
                .collect(Collectors.toMap(Employee::getName, Employee::getSalary, (existingValue, newValue) -> newValue));

         System.out.println(employeeMap);
    }
}

class Employee {
    private String name;
    private int age;
    private double salary;

    public Employee(String name, int age, double salary) {
        this.name = name;
        this.age = age;
        this.salary = salary;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", salary=" + salary +
                '}';
    }
}