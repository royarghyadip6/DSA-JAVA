# 🔁 Inversion of Control (IoC) – Detailed Notes

---

## 🔹 1. What is IoC?

👉 **Inversion of Control (IoC)** is a design principle where:

> The control of object creation is transferred from the programmer to the Spring container.

---

### 📌 Traditional Way

```java
Engine engine = new Engine();
Car car = new Car(engine);
```

👉 You (developer) are responsible for:

* Creating objects
* Managing dependencies

---

### 📌 Spring Way (IoC)

```java
Car car = container.getBean(Car.class);
```

👉 Spring:

* Creates objects
* Injects dependencies
* Manages lifecycle

✔ Control is **inverted** (shifted to Spring)

---

## 🔹 2. Why IoC is Needed?

Without IoC:

* Tight coupling ❌
* Hard to test ❌
* Difficult to maintain ❌

With IoC:

* Loose coupling ✔
* Easy testing ✔
* Better scalability ✔

---

## 🔹 3. Traditional Object Creation vs Spring Container

### 🔴 Traditional Approach

```java
class Car {
    Engine engine = new Engine(); // tightly coupled
}
```

❗ Problems:

* Cannot replace Engine easily
* Code changes required for every modification

---

### 🟢 IoC Approach (Spring)

```java
class Car {
    Engine engine;

    Car(Engine engine) {
        this.engine = engine;
    }
}
```

✔ Spring injects `Engine`
✔ Car is independent

---

## 🔹 4. What is Spring Container in IoC?

👉 Spring Container is responsible for:

* Creating beans
* Managing dependencies
* Providing objects

📌 Types:

* BeanFactory (basic)
* ApplicationContext (advanced)

---

## 🔹 5. Types of IoC

IoC can be achieved in two ways:

---

# 🔸 5.1 Dependency Injection (DI) ⭐⭐⭐

👉 **Most common and important**

### ✔ Definition:

> Dependencies are provided (injected) by the container.

---

### 📌 Example

```java
class Car {
    Engine engine;

    Car(Engine engine) {
        this.engine = engine;
    }
}
```

Spring:

* Creates Engine
* Injects into Car

---

### ✔ Types of DI

1. Constructor Injection
2. Setter Injection

---

### ✔ Key Idea

👉 Don’t create dependencies
👉 Receive them

---

---

# 🔸 5.2 Dependency Lookup (DL)

👉 Less commonly used

### ✔ Definition:

> Object itself asks the container for dependencies

---

### 📌 Example

```java
ApplicationContext context = new ClassPathXmlApplicationContext("config.xml");

Engine engine = context.getBean(Engine.class);
```

---

### ✔ Key Idea

👉 You **pull** dependency from container
👉 Not injected automatically

---

## 🔹 6. DI vs Dependency Lookup

| Feature                  | Dependency Injection | Dependency Lookup     |
|--------------------------|----------------------|-----------------------|
| Who provides dependency? | Container            | Object asks container |
| Style                    | Push                 | Pull                  |
| Usage                    | Most common          | Rare                  |
| Coupling                 | Low                  | Slightly higher       |

---

## 🔹 7. Real-Life Analogy

### 🍽️ Dependency Injection

Waiter brings food to your table
👉 You don’t go to kitchen

---

### 🏃 Dependency Lookup

You go to kitchen and get food
👉 You fetch dependency

---

## 🔹 8. Key Takeaways (Very Important)

* IoC = Control goes to Spring
* DI = Spring injects dependencies
* DL = You fetch dependencies
* DI is preferred over DL

---

## 🔹 9. Interview One-Liners

👉 IoC:

> “It is the principle where Spring manages object creation instead of the developer.”

👉 DI:

> “Dependencies are injected by the container.”

👉 DL:

> “Object retrieves dependencies from the container.”

---

## 🔹 10. Common Mistake

❌ Thinking IoC = DI

✔ Correct:

> DI is a way to achieve IoC

---

# 🎯 IoC Quiz (Round 1 – Basics)

### 1. What is Inversion of Control?

---

### 2. Who controls object creation in IoC?

---

### 3. What problem does IoC solve?

---

### 4. Is IoC a concept or an implementation?

---

### 5. What is the relationship between IoC and DI?

---

# ⚡ IoC Quiz (Round 2 – Understanding)

### 6. What happens if IoC is not used in an application?

---

### 7. In IoC, do we use `new` keyword frequently? Why?

---

### 8. What is the role of Spring container in IoC?

---

### 9. Give one real-life example of IoC.

---

### 10. Which is better: Dependency Injection or Dependency Lookup? Why?

---

# 🔥 IoC Quiz (Round 3 – Tricky / Interview Level)

### 11. Can IoC exist without Spring?

---

### 12. Is Dependency Lookup also IoC?

---

### 13. Why is Dependency Injection preferred over Dependency Lookup?

---

### 14. Does IoC reduce coupling or increase it? Explain briefly.

---

### 15. What will happen if dependencies are not injected in IoC?

---
