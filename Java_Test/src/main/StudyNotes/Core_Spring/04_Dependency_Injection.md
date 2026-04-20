# 💉 Dependency Injection (DI) – In-Depth Notes

---

## 🔹 1. What is Dependency Injection?

👉 Dependency Injection (DI) is a technique where:

> An object receives its dependencies from an external source (Spring container) instead of creating them.

---

### 📌 Key Idea

❌ Don’t create dependencies

✔ Let Spring provide them

---

### 🔴 Without DI (Tight Coupling)

```java
class Car {
    Engine engine = new Engine(); // tightly coupled
}
```

❗ Problem:

* Car is directly dependent on Engine
* Hard to replace or test

---

### 🟢 With DI (Loose Coupling)

```java
class Car {
    Engine engine;

    Car(Engine engine) {
        this.engine = engine;
    }
}
```

✔ Dependency is injected

✔ Easy to change implementation

---

## 🔹 2. What is Dependency?

👉 A dependency is:

> Any object that another object needs to function

📌 Example:

* Car → needs Engine. So, Engine is a dependency

---

## 🔹 3. How Spring Injects Dependencies?

Spring:

* Creates objects (Beans)
* Injects required dependencies automatically

---

## 🔹 4. Types of Dependency Injection

---

# 🔸 4.1 Constructor Injection ⭐⭐⭐ (MOST IMPORTANT)

### ✔ Definition:

> Dependencies are provided through the constructor

---

### 📌 Example

```java
interface Engine {
    void start();
}

class PetrolEngine implements Engine {
    public void start() {
        System.out.println("Petrol Engine");
    }
}

class Car {
    private Engine engine;

    // Constructor Injection
    public Car(Engine engine) {
        this.engine = engine;
    }

    void drive() {
        engine.start();
    }
}
```

---

### ✔ Spring Annotation Example

```java
@Component
class Car {

    private final Engine engine;

    @Autowired
    public Car(Engine engine) {
        this.engine = engine;
    }
}
```

---

### ✔ Advantages

* ✔ Mandatory dependencies enforced
* ✔ Object always in valid state
* ✔ Easier testing (constructor-based mocks)
* ✔ Immutability possible (`final` fields)

---

### ❌ Disadvantages

* ❌ Large constructor if too many dependencies
* ❌ Less flexible for optional dependencies

---

---

# 🔸 4.2 Setter Injection

### ✔ Definition:

> Dependencies are provided using setter methods

---

### 📌 Example

```java
class Car {
    private Engine engine;

    // Setter Injection
    public void setEngine(Engine engine) {
        this.engine = engine;
    }
}
```

---

### ✔ Spring Annotation Example

```java
@Component
class Car {

    private Engine engine;

    @Autowired
    public void setEngine(Engine engine) {
        this.engine = engine;
    }
}
```

---

### ✔ Advantages

* ✔ Flexible
* ✔ Good for optional dependencies
* ✔ Can change dependency at runtime

---

### ❌ Disadvantages

* ❌ Object may be incomplete (engine not set)
* ❌ Harder to ensure mandatory dependencies
* ❌ Not immutable

---

---

## 🔹 5. Constructor vs Setter Injection

| Feature              | Constructor Injection | Setter Injection  |
|----------------------|-----------------------|-------------------|
| Mandatory dependency | ✔ Yes                 | ❌ No              |
| Immutability         | ✔ Possible            | ❌ No              |
| Readability          | ✔ Clear               | ⚠ Medium          |
| Flexibility          | ❌ Less                | ✔ More            |
| Recommended          | ⭐ Yes                 | ⚠ Use when needed |

---

## 🔹 6. Which One Should You Use?

👉 Default answer (Interview):

✔ Use **Constructor Injection**

👉 Use Setter Injection when:

* Dependency is optional
* Need to change value later

---

## 🔹 7. How DI Achieves Loose Coupling

👉 Instead of:

```java
Engine e = new PetrolEngine();
```

👉 You use:

```java
Engine e; // abstraction
```

✔ Spring decides implementation
✔ Classes are independent

---

## 🔹 8. Real-Life Analogy

### 🍽️ DI Example

* You order food
* You don’t cook it

👉 Kitchen (Spring) prepares and gives it

---

## 🔹 9. Important Interview Points

* DI is a way to implement IoC
* Promotes loose coupling
* Constructor Injection is preferred
* Avoid field injection (bad practice in interviews)

---

## 🔹 10. Common Mistakes

1. ❌ Creating objects using `new`
2. ❌ Using field injection everywhere
3. ❌ Ignoring interfaces

---

# 🎯 One-Line Summary

> “Dependency Injection means providing dependencies to a class instead of letting the class create them.”

---

# 💉 Spring Dependency Injection Demo (Constructor & Setter)

---

## 🔹 1. Project Overview

This example demonstrates:

* **Constructor Injection**
* **Setter Injection**
* How Spring injects dependencies using XML configuration

---

## 🔹 2. Maven Dependency

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-context</artifactId>
        <version>5.3.30</version>
    </dependency>
</dependencies>
```

---

## 🔹 3. Create Dependency Class

```java
package com.example;

public class Engine {

    public void start() {
        System.out.println("Engine started...");
    }
}
```

---

# 🔥 4. Constructor Injection Example

## ✔ Car Class

```java
package com.example;

public class CarConstructor {

    private Engine engine;

    // Constructor Injection
    public CarConstructor(Engine engine) {
        this.engine = engine;
    }

    public void drive() {
        engine.start();
        System.out.println("Car (Constructor) is running...");
    }
}
```

---

# 🔥 5. Setter Injection Example

## ✔ Car Class

```java
package com.example;

public class CarSetter {

    private Engine engine;

    // Setter Injection
    public void setEngine(Engine engine) {
        this.engine = engine;
    }

    public void drive() {
        engine.start();
        System.out.println("Car (Setter) is running...");
    }
}
```

---

## 🔹 6. XML Configuration (config.xml)

```xml
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="
       http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd">

    <!-- Dependency Bean -->
    <bean id="engine" class="com.example.Engine" />

    <!-- Constructor Injection -->
    <bean id="carConstructor" class="com.example.CarConstructor">
        <constructor-arg ref="engine"/>
    </bean>

    <!-- Setter Injection -->
    <bean id="carSetter" class="com.example.CarSetter">
        <property name="engine" ref="engine"/>
    </bean>

</beans>
```

---

## 🔹 7. Main Class to Run

```java
package com.example;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class App {

    public static void main(String[] args) {

        ApplicationContext context =
                new ClassPathXmlApplicationContext("config.xml");

        // Constructor Injection
        CarConstructor car1 = context.getBean("carConstructor", CarConstructor.class);
        car1.drive();

        // Setter Injection
        CarSetter car2 = context.getBean("carSetter", CarSetter.class);
        car2.drive();
    }
}
```

---

## 🔹 8. Output

```text
Engine started...
Car (Constructor) is running...

Engine started...
Car (Setter) is running...
```

---

# 💉 Spring Dependency Injection (Annotation-Based)

---

## 🔹 1. Project Overview

This example demonstrates:

* **Constructor Injection using annotations**
* **Setter Injection using annotations**
* Component scanning instead of XML

---

## 🔹 2. Maven Dependency

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-context</artifactId>
        <version>5.3.30</version>
    </dependency>
</dependencies>
```

---

## 🔹 3. Enable Component Scanning (Java Config)

```java
package com.example.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "com.example")
public class AppConfig {
}
```

---

## 🔹 4. Dependency Class

```java
package com.example;

import org.springframework.stereotype.Component;

@Component
public class Engine {

    public void start() {
        System.out.println("Engine started...");
    }
}
```

---

# 🔥 5. Constructor Injection (Recommended)

```java
package com.example;

import org.springframework.stereotype.Component;

@Component
public class CarConstructor {

    private final Engine engine;

    // Constructor Injection
    public CarConstructor(Engine engine) {
        this.engine = engine;
    }

    public void drive() {
        engine.start();
        System.out.println("Car (Constructor) is running...");
    }
}
```

---

# 🔥 6. Setter Injection

```java
package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CarSetter {

    private Engine engine;

    // Setter Injection
    @Autowired
    public void setEngine(Engine engine) {
        this.engine = engine;
    }

    public void drive() {
        engine.start();
        System.out.println("Car (Setter) is running...");
    }
}
```

---

## 🔹 7. Main Class

```java
package com.example;

import com.example.config.AppConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class App {

    public static void main(String[] args) {

        ApplicationContext context =
                new AnnotationConfigApplicationContext(AppConfig.class);

        // Constructor Injection
        CarConstructor car1 = context.getBean(CarConstructor.class);
        car1.drive();

        // Setter Injection
        CarSetter car2 = context.getBean(CarSetter.class);
        car2.drive();
    }
}
```

---

## 🔹 8. Output

```text
Engine started...
Car (Constructor) is running...

Engine started...
Car (Setter) is running...
```

---

# 🔥 Dependency Injection – Tricky Interview Questions

## 🔹 1. Why is Constructor Injection preferred over Setter Injection?

👉 Expected depth:

* Enforces mandatory dependencies
* Ensures object is always in a valid state
* Supports immutability (`final`)
* Better for testing

---

## 🔹 2. What happens if Spring finds multiple beans for the same type?

👉 Example:

```java
@Autowired
Engine engine;
```

But you have:

* PetrolEngine
* DieselEngine

👉 What happens?

* Spring throws **NoUniqueBeanDefinitionException**
* Solve using:

    * `@Qualifier`
    * `@Primary`

---

## 🔹 3. Can we use DI without Spring?

👉 Yes

✔ Using manual constructor/setter injection

But, Spring automates and manages it

---

## 🔹 4. Why is Field Injection considered bad practice?

```java
@Autowired
private Engine engine;
```

👉 Expected answer:

* Hard to test (no constructor)
* Breaks encapsulation
* Cannot make fields `final`
* Hidden dependencies

---

## 🔹 5. What if a dependency is optional?

👉 Best answer:

* Use Setter Injection
  OR
* `@Autowired(required = false)`
  OR
* `Optional<>`

---

## 🔹 6. What is Circular Dependency? Will DI handle it?

👉 Example:

* A depends on B
* B depends on A

✔ Answer:

* Constructor injection → ❌ fails
* Spring throws: BeanCurrentlyInCreationException

```java
@Component
class A {
  private final B b;
  
  public A(B b) {
      this.b = b;
  }
}

@Component
class B {
  private final A a;
  
  public B(A a) {
      this.a = a;
  }
}
```

Setter injection → ✔ works (Spring handles it)

Why it works:
*   Spring creates objects first (empty)
*   Then injects dependencies later
```java
@Component
class A {
    private B b;

    @Autowired
    public void setB(B b) {
        this.b = b;
    }
}

@Component
class B {
    private A a;

    @Autowired
    public void setA(A a) {
        this.a = a;
    }
}

```
---

## 🔹 7. What happens if dependency is not found?

👉 Spring throws **NoSuchBeanDefinitionException**

---

## 🔹 8. Can we inject primitive values using DI?

👉 Yes
✔ Using:

```java
@Value("10")
private int speed;
```

---

## 🔹 9. What is the difference between @Autowired and @Inject?

👉

* `@Autowired` → Spring specific
* `@Inject` → Java standard (JSR-330)
* Both perform DI

---

## 🔹 10. How does Spring resolve dependencies internally?

👉 High-level answer:

* Uses reflection
* Scans context
* Matches by type → then qualifier

---

# 🔥 Scenario-Based Questions (VERY IMPORTANT)

## 🔹 11. You have 5 dependencies in a class. Which injection type?

👉 Best answer:

* Constructor Injection
* But redesign if too many dependencies (code smell)

---

## 🔹 12. You need to change dependency at runtime. What will you use?

👉 Setter Injection

---

## 🔹 13. You want to make your class immutable. Which DI?

👉 Constructor Injection

---

## 🔹 14. How do you test a class using DI?

* Pass mock dependencies via constructor
* No need for Spring container

---

## 🔹 15. What if you remove @Autowired from constructor?

👉 Trick question ⚠️

✔ Answer:

* If only one constructor → Spring auto-injects
* If multiple constructors → need `@Autowired`

---

# ⚡ Rapid Fire (Tricky)

1. Can Constructor Injection cause circular dependency? → YES ❌
2. Is Setter Injection safe for mandatory dependency? → NO ❌
3. Can DI exist without IoC? → NO ❌
4. Is field injection recommended? → NO ❌
5. Can Spring inject interface? → YES ✔

---

# 🎯 Most Important Interview Answer

If interviewer asks:

👉 “Explain DI in one line”

✔ Say:

> “Dependency Injection is a technique where the Spring container provides required objects to a class instead of the class creating them.”

---

# 🚀 Pro Tip (Real Interview Insight)

Interviewers often trap with:

* Circular dependency
* Multiple beans
* Field injection

👉 If you can answer those confidently → you’re ahead of 80% candidates.

---

# 🚀 1. Project Structure (Industry Standard)

We’ll build a small flow:

> **Controller → Service → Repository → External Client**

```
com.example.app
│
├── controller
├── service
├── repository
├── client
├── model
└── config
```

---

# 🔹 2. Model (Simple POJO)

```java
public class User {
    private Long id;
    private String name;

    // getters & setters
}
```

---

# 🔹 3. Repository Layer

```java
@Repository
public class UserRepository {

    public User findById(Long id) {
        // simulate DB call
        return new User(id, "John");
    }
}
```

---

# 🔹 4. External Client (Another Dependency)

```java
@Component
public class NotificationClient {

    public void sendNotification(String message) {
        System.out.println("Notification sent: " + message);
    }
}
```

---

# 🔥 5. Service Layer (Constructor Injection – Industry Standard)

```java
@Service
public class UserService {

    private final UserRepository userRepository;
    private final NotificationClient notificationClient;

    // Constructor Injection (BEST PRACTICE)
    public UserService(UserRepository userRepository,
                       NotificationClient notificationClient) {
        this.userRepository = userRepository;
        this.notificationClient = notificationClient;
    }

    public User getUser(Long id) {
        User user = userRepository.findById(id);

        notificationClient.sendNotification("User fetched: " + user.getName());

        return user;
    }
}
```

✔ Notice:

* No `new` keyword
* Dependencies are injected
* Fields are `final` → immutable

---

# 🔹 6. Controller Layer

```java
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    // Constructor Injection again
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.getUser(id);
    }
}
```

---

# 🔥 7. Key Industry Practices (IMPORTANT)

## ✔ 1. Always Use Constructor Injection

* No `@Autowired` needed (Spring auto-detects)

---

## ✔ 2. Use Interfaces (Loose Coupling)

```java
public interface UserService {
    User getUser(Long id);
}
```

```java
@Service
public class UserServiceImpl implements UserService {
    // implementation
}
```

---

## ✔ 3. Avoid Field Injection ❌

```java
@Autowired
private UserService userService; // BAD PRACTICE
```

---

## ✔ 4. Use @Qualifier When Multiple Beans Exist

```java
@Service("emailService")
public class EmailNotificationService implements NotificationService {}
```

```java
@Service("smsService")
public class SmsNotificationService implements NotificationService {}
```

```java
public UserService(@Qualifier("emailService") NotificationService service) {
    this.service = service;
}
```

---

# 🔥 8. Real Scenario: Multiple Implementations

```java
public interface PaymentService {
    void pay();
}
```

```java
@Service
public class UpiPaymentService implements PaymentService {
    public void pay() {
        System.out.println("UPI Payment");
    }
}
```

```java
@Service
public class CardPaymentService implements PaymentService {
    public void pay() {
        System.out.println("Card Payment");
    }
}
```

👉 Inject specific one:

```java
public OrderService(@Qualifier("upiPaymentService") PaymentService paymentService) {
    this.paymentService = paymentService;
}
```

---

# 🔥 9. Testing (Why DI is Powerful)

```java
@Test
void testUserService() {
    UserRepository repo = mock(UserRepository.class);
    NotificationClient client = mock(NotificationClient.class);

    UserService service = new UserService(repo, client);

    // test without Spring
}
```

✔ No Spring needed
✔ Easy mocking

---

# 🎯 Final Industry-Level Understanding

👉 In real projects:

* Controller → handles request
* Service → business logic
* Repository → DB access
* Client → external systems

✔ All connected using DI
✔ No manual object creation

---

# ⚡ Interview One-Liner

> “In Spring Boot, Dependency Injection is used to wire Controller, Service, Repository, and other components together using constructor injection for better maintainability and testability.”

---
