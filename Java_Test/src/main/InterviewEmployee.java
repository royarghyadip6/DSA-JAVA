package src.main;

import java.time.LocalDate;
import java.util.List;

public record InterviewEmployee(
        int id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        String department,
        String role,
        double salary,
        LocalDate hireDate,
        boolean isActive
) {
    public static List<InterviewEmployee> getAllEmployee() {
        return List.of(
                new InterviewEmployee(101, "Alice", "Smith", "alice.smith@company.com", "555-0101", "Engineering", "Software Engineer", 95000.00, LocalDate.of(2021, 3, 15), true),
                new InterviewEmployee(102, "Bob", "Jones", "bob.jones@company.com", "555-0102", "HR", "HR Manager", 78000.00, LocalDate.of(2018, 8, 22), true),
                new InterviewEmployee(103, "Charlie", "Brown", "charlie.brown@company.com", "555-0103", "Marketing", "Media Specialist", 62000.00, LocalDate.of(2023, 1, 10), false),
                new InterviewEmployee(104, "Diana", "Prince", "diana.prince@company.com", "555-0104", "Engineering", "Tech Lead", 125000.00, LocalDate.of(2019, 5, 14), true),
                new InterviewEmployee(105, "Ethan", "Miller", "ethan.miller@company.com", "555-0105", "Finance", "Financial Analyst", 84000.00, LocalDate.of(2020, 11, 2), true),
                new InterviewEmployee(106, "Fiona", "Davis", "fiona.davis@company.com", "555-0106", "Sales", "Sales Executive", 71000.00, LocalDate.of(2022, 6, 7), true),
                new InterviewEmployee(107, "George", "Wilson", "george.wilson@company.com", "555-0107", "Engineering", "QA Engineer", 69000.00, LocalDate.of(2021, 9, 19), true),
                new InterviewEmployee(108, "Hannah", "Taylor", "hannah.taylor@company.com", "555-0108", "Operations", "Operations Manager", 98000.00, LocalDate.of(2017, 4, 3), true),
                new InterviewEmployee(109, "Ian", "Anderson", "ian.anderson@company.com", "555-0109", "Support", "Support Specialist", 58000.00, LocalDate.of(2023, 2, 27), false),
                new InterviewEmployee(110, "Julia", "Thomas", "julia.thomas@company.com", "555-0110", "Product", "Product Manager", 115000.00, LocalDate.of(2019, 12, 11), true)
        );
    }
}
