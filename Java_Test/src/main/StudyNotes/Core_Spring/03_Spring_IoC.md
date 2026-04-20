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

The **Spring Container** is the core of the Spring Framework. It is responsible for:

* Creating objects (beans)
* Managing their lifecycle
* Injecting dependencies (Dependency Injection)
* Configuring and wiring everything together

👉 In simple terms:
**Spring Container = Factory + Manager + Injector of your objects**

It reads configuration metadata (XML, Java Config, Annotations) and builds the entire application.

---

## 🔹 Types of Spring Container

There are **two main types**:

1. **BeanFactory** → Basic container
2. **ApplicationContext** → Advanced container (used in real projects)

---

## 🔸 1. BeanFactory (Basic Container)

### 📌 What is it?

`BeanFactory` is the **simplest container** in Spring.

👉 It provides:

* Basic Dependency Injection
* Lazy loading of beans (created only when needed)

---

### ⚙️ Key Characteristics

* **Lazy Initialization** (default)
* Lightweight
* No advanced features (events, i18n, etc.)
* Mostly used internally in Spring

---

### 🧠 BeanFactory – Internal Working (Lazy, Basic)

1. Load configuration (XML/annotations)
2. Create **BeanDefinition** objects
3. Store in registry (Map<beanId, BeanDefinition>)
4. ❗ No bean created at startup
5. `getBean()` called → triggers creation
6. Instantiate bean (Reflection)
7. Inject dependencies (constructor/setter)
8. Initialize bean (init methods)
9. Store in singleton cache (if singleton)
10. Return bean

---

### 💻 Example (BeanFactory)

```java
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

public class MainApp {
    public static void main(String[] args) {
        BeanFactory factory = new XmlBeanFactory(
                new ClassPathResource("beans.xml"));

        HelloWorld obj = (HelloWorld) factory.getBean("helloBean");
        obj.sayHello();
    }
}
```

### beans.xml

```xml
<beans>
    <bean id="helloBean" class="com.example.HelloWorld"/>
</beans>
```

---

### 🔍 Important Behavior

```java
System.out.println("Before getBean()");
HelloWorld obj = (HelloWorld) factory.getBean("helloBean");
System.out.println("After getBean()");
```

👉 Object is created **only at getBean() call**

---

### ❌ Limitations

* No support for:

    * Event handling
    * Internationalization (i18n)
    * AOP auto-proxying
* Deprecated in modern usage

---

## 🔸 2. ApplicationContext (Advanced Container)

### 📌 What is it?

`ApplicationContext` is a **superset of BeanFactory**.

👉 It includes everything BeanFactory does + many enterprise features.

---

### ⚙️ Key Features

* **Eager Initialization** (default)
* Built-in support for:

    * AOP
    * Event handling
    * Internationalization (i18n)
    * Annotation-based configuration
* Widely used in **Spring Boot**

---

### 🧠 ApplicationContext – Internal Working (Eager, Advanced)

1. Load configuration
2. Create & register BeanDefinitions
3. Create internal **BeanFactory**
4. Run **BeanFactoryPostProcessor** (modify definitions)
5. Register **BeanPostProcessor** (for AOP, hooks)
6. 🚀 Instantiate all singleton beans (startup)
7. For each bean:

    * Instantiate
    * Inject dependencies
    * Aware interfaces
    * Pre-init (BeanPostProcessor)
    * Init methods
    * Post-init (proxy/AOP)
8. Store in singleton cache
9. Initialize event system & i18n
10. Context ready

---

### 💻 Example (ApplicationContext)

```java
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class MainApp {
    public static void main(String[] args) {
        ApplicationContext context =
                new ClassPathXmlApplicationContext("beans.xml");

        HelloWorld obj = context.getBean("helloBean", HelloWorld.class);
        obj.sayHello();
    }
}
```

---

### 🔍 Important Behavior

```java
System.out.println("Before context creation");
ApplicationContext context =
    new ClassPathXmlApplicationContext("beans.xml");
System.out.println("After context creation");
```

👉 Beans are created **during context initialization (startup)**

---

# 🔥 Types of ApplicationContext

### 1. ClassPathXmlApplicationContext

* Loads XML from classpath

```java
ApplicationContext ctx =
    new ClassPathXmlApplicationContext("beans.xml");
```

---

### 2. FileSystemXmlApplicationContext

* Loads XML from file system

```java
ApplicationContext ctx =
    new FileSystemXmlApplicationContext("C:/config/beans.xml");
```

---

### 3. AnnotationConfigApplicationContext (MOST IMPORTANT)

* Used with Java Config + Annotations

```java
@Configuration
@ComponentScan("com.example")
class AppConfig {}

public class MainApp {
    public static void main(String[] args) {
        ApplicationContext ctx =
            new AnnotationConfigApplicationContext(AppConfig.class);

        MyService service = ctx.getBean(MyService.class);
        service.doWork();
    }
}
```

---

# 🔥 BeanFactory vs ApplicationContext (Interview Table)

| Feature             | BeanFactory    | ApplicationContext |
|---------------------|----------------|--------------------|
| Initialization      | Lazy           | Eager (default)    |
| Performance         | Faster startup | Slightly slower    |
| Memory usage        | Less           | More               |
| Enterprise features | ❌ No           | ✅ Yes              |
| AOP support         | ❌              | ✅                  |
| Event handling      | ❌              | ✅                  |
| i18n support        | ❌              | ✅                  |
| Usage               | Rare           | Very common        |

---

# 🧠 When to Use What?

### Use BeanFactory (Rare)

* Memory-constrained systems
* Very basic applications

### Use ApplicationContext (99% cases)

* Spring Boot apps
* Enterprise systems
* Microservices

---

# ⚡ Real-World Insight (Important for Interviews)

In real projects (especially telecom systems like those in Nokia or Ericsson):

* You **never directly use BeanFactory**
* Everything runs on **ApplicationContext**
* Spring Boot internally uses it

---

# 🔥 Key Interview Questions

1. Why is ApplicationContext preferred over BeanFactory?
2. What is lazy vs eager initialization?
3. Can ApplicationContext behave like BeanFactory? (Yes)
4. How does Spring container manage lifecycle?
5. What happens during context startup?

---

## 🔹 5. Types of IoC

IoC can be achieved in two ways:

---

## 🔸 5.1 Dependency Injection (DI)

* Container **injects dependencies into your class**

### 🧠 Internal Idea

* Object **does NOT create or search dependencies**
* Dependencies are **provided from outside (Spring Container)**

### 💻 Example

```java
class Engine {}

class Car {
    Engine engine;

    // DI via constructor
    Car(Engine engine) {
        this.engine = engine;
    }
}
```

👉 Spring injects `Engine` into `Car`

### ⚡ Key Points

* Loose coupling ✅
* Easy testing ✅
* Cleaner design ✅
* Recommended approach ✅

---

## 🔸 5.2 Dependency Lookup (DL)

* Object **manually asks container for dependency**

### 🧠 Internal Idea

* Object **controls fetching dependency**
* Uses `getBean()` from container

### 💻 Example

```java
ApplicationContext context = ...;

class Car {
    void drive() {
        Engine engine = context.getBean(Engine.class);
    }
}
```

👉 `Car` is **looking up dependency itself**

### ⚡ Key Points

* Tight coupling ❌
* Hard to test ❌
* Breaks IoC principle ❌
* Not recommended ❌

---

## 🔹 6. DI vs Dependency Lookup

| Feature                 | DI          | DL           |
|-------------------------|-------------|--------------|
| Who provides dependency | Container   | Class itself |
| Coupling                | Loose       | Tight        |
| Control                 | Container   | Developer    |
| Testability             | Easy        | Hard         |
| Usage                   | Recommended | Rare         |

---

## 🔹 7. Real-Life Analogy

### 🍽️ Dependency Injection

Waiter brings food to your table
👉 You don’t go to kitchen

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
