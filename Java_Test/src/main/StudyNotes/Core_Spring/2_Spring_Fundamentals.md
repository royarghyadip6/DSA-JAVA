
---

# 🌱 Spring Fundamentals (Core Concept Notes)

---

## 🔹 1. What is Spring Framework?

Spring is a **Java framework** used to build **enterprise-level applications** easily.

👉 It provides:

* Pre-built infrastructure
* Better code structure
* Loose coupling
* Easy testing

📌 In simple words:

> Spring helps you write **clean, maintainable, and scalable Java code**.

---

## 🔹 2. Why was Spring created?

Before Spring, Java applications had problems:

### ❌ Problems in Traditional Java

* Tight coupling between classes
* Object creation was manual (`new` keyword everywhere)
* Difficult to test
* Hard to manage large applications
* Code was not flexible

---

### 🔴 Example (Without Spring - Tight Coupling)

```java
class Engine {
    void start() {
        System.out.println("Engine started");
    }
}

class Car {
    Engine engine = new Engine(); // tightly coupled

    void drive() {
        engine.start();
        System.out.println("Car is running");
    }
}
```

❗ Problem:

* Car **depends directly** on Engine
* You cannot easily replace Engine with another implementation

---

## 🔹 3. What is Loose Coupling?

### ✔ Loose Coupling means:

Classes are **independent of each other**

👉 Instead of creating objects, they are **provided externally**

---

### 🟢 Example (Loose Coupling using Interface)

```java
interface Engine {
    void start();
}

class PetrolEngine implements Engine {
    public void start() {
        System.out.println("Petrol Engine");
    }
}

class DieselEngine implements Engine {
    public void start() {
        System.out.println("Diesel Engine");
    }
}

class Car {
    Engine engine;

    Car(Engine engine) {
        this.engine = engine;
    }

    void drive() {
        engine.start();
    }
}
```

✔ Now:

* Car doesn’t care which engine is used
* Easy to switch implementations

---

## 🔹 4. What is a POJO?

POJO = **Plain Old Java Object**

👉 A simple Java class:

* No special framework dependency
* No extends/implements required
* Just fields + methods

```java
class Student {
    private String name;

    public String getName() {
        return name;
    }
}
```

✔ Spring works mainly with POJOs

---

## 🔹 5. What is a Spring Container?

Spring Container is the **core engine of Spring**.

👉 It:

* Creates objects (beans)
* Injects dependencies
* Manages lifecycle

📌 Think:

> "Container = Factory + Manager of objects"

---

## 🔹 6. What is a Bean?

A Bean is simply:

> Any object managed by Spring

✔ Example:

```java
Car car = new Car();
```

If Spring creates it → it's a **Spring Bean**

---

## 🔹 7. Key Idea of Spring (VERY IMPORTANT)

### 👉 "Don't create objects manually"

Instead:

> Let Spring create and manage them

---

## 🔹 8. Inversion of Control (Intro)

Traditional:

```java
Engine e = new Engine();
```

Spring:

```java
Engine e = container.getBean(Engine.class);
```

✔ Control is moved from developer → Spring

📌 This is called:
👉 **Inversion of Control (IoC)**

---

## 🔹 9. Dependency Injection (Intro)

Dependency = object needed by another object

Example:

* Car needs Engine → Engine is a dependency

👉 Instead of creating:

```java
Engine e = new Engine();
```

Spring injects it:

```java
Car car = new Car(engine);
```

✔ This is called:
👉 **Dependency Injection (DI)**

---

## 🔹 10. Advantages of Spring

* Loose Coupling
* Easy Testing
* Better Code Maintenance
* Scalable Applications
* Reusable Components

---

## 🔹 11. Real-Life Analogy

Think of Spring like a **restaurant** 🍽️

* You (developer) = Customer
* Kitchen (Spring container) = Creates food (objects)
* You don’t cook → You just order

👉 You say:
"Give me Car with Engine"

Spring:
* Creates Engine
* Injects into Car
* Gives ready object

---

## 🔹 12. Summary (Important for Interview)

* Spring is a **lightweight framework**
* Promotes **loose coupling**
* Uses **IoC and DI**
* Manages objects called **Beans**
* Uses a **Container** to handle everything

---

# 🎯 Spring Fundamentals – Interview Questions

## 🔹 Basic Questions (Must Know)

### 1. What is Spring Framework?

👉 A lightweight Java framework used to build enterprise applications with features like IoC, DI, and AOP.

---

### 2. Why do we use Spring?

👉 To solve problems like:

* Tight coupling
* Difficult object management
* Poor testability

---

### 3. What is a POJO?

👉 A simple Java object without any dependency on frameworks.

---

### 4. What is a Spring Bean?

👉 Any object managed by the Spring container.

---

### 5. What is a Spring Container?

👉 The core component that:

* Creates objects
* Injects dependencies
* Manages lifecycle

---

## 🔹 Core Concept Questions (VERY IMPORTANT)

### 6. What is Tight Coupling?

👉 When one class directly depends on another class.

❗ Example:

```java
Engine e = new Engine();
```

---

### 7. What is Loose Coupling?

👉 Classes depend on abstraction (interfaces), not concrete classes.

---

### 8. How does Spring achieve Loose Coupling?

👉 Using:

* Dependency Injection
* Interfaces

---

### 9. What is Inversion of Control (IoC)?

👉 Control of object creation is transferred from developer to Spring container.

📌 One-liner:

> "Spring controls object creation instead of the programmer."

---

### 10. What is Dependency Injection (DI)?

👉 Injecting dependencies into a class instead of creating them inside the class.

---

## 🔹 Concept Difference Questions

### 11. Difference between IoC and DI?

👉 IoC = Concept
👉 DI = Implementation of IoC

---

### 12. Difference between Bean and POJO?

| POJO              | Bean                    |
| ----------------- | ----------------------- |
| Simple Java class | Managed by Spring       |
| Not controlled    | Controlled by container |

---

## 🔹 Scenario-Based Questions

### 13. What happens if you don’t use Spring?

👉 You will face:

* Tight coupling
* Manual object creation
* Difficult testing
* Poor scalability

---

### 14. How does Spring improve testability?

👉 Because of loose coupling:

* You can easily mock dependencies
* Replace implementations

---

### 15. Real-life example of Dependency Injection?

👉 Example:

* Car needs Engine
* Instead of building engine → engine is provided

---

## 🔹 Tricky / Follow-up Questions

### 16. Is Spring a framework or a library?

👉 Framework
✔ Because it controls the flow (IoC)

---

### 17. Why is Spring called lightweight?

👉 Because:

* No heavy dependencies
* Uses POJO
* Minimal configuration

---

### 18. What is the role of container in Spring?

👉 It acts like:

* Object factory
* Dependency injector
* Lifecycle manager

---

### 19. Can Spring work without XML?

👉 Yes
✔ Using annotations and Java configuration

---

### 20. What is the most important concept in Spring?

👉 IoC and Dependency Injection

---
