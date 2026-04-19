
# 🧠 Spring Container – Internal Working

---

## 🔹 1. What is Spring Container?

👉 Spring Container is the **core engine of Spring Framework**. Spring Container is a factory that creates, manages, and wires objects together.

📌 It is responsible for:

* Creating objects (Beans)
* Injecting dependencies (DI)
* Managing bean lifecycle
* Configuring application

---

## 🔹 2. How Spring Container Works Internally

👉 When the application starts Spring does the follows:

> Reads configuration → Component Scan → Creates bean definitions → Instantiates beans → Injects dependencies → Manages lifecycle → Stores beans in memory

---

## 🔹 Spring Container Step-by-Step Internal Working Flow

### 🔸 Step 1: Container Initialization

```java id="j0n6fj"
ApplicationContext context =
    new AnnotationConfigApplicationContext(AppConfig.class);
```

👉 This line:

* Creates the Spring container
* Triggers the entire startup process

---

### 🔸 Step 2: Configuration Loading

Spring loads configuration from:

* XML
* Annotations (`@Component`, `@Service`)
* Java Config (`@Configuration`)

---

### 🔸 Step 3: Component Scanning

👉 Spring scans packages for:

* `@Component`
* `@Service`
* `@Repository`
* `@Controller`

✔ These classes are identified as **beans**

---

### 🔸 Step 4: Bean Definition Creation

👉 Spring does NOT create objects immediately.

It first creates **Bean Definitions** (metadata):

```text id="x4q7ec"
BeanDefinition:
- Class: Car
- Scope: Singleton
- Dependencies: Engine
```

✔ Stored in container (heap memory)

---

### 🔸 Step 5: Bean Instantiation

👉 Now Spring creates actual objects:

```java id="8jb1z7"
Car car = new Car();
```

✔ Happens:

* Eagerly (ApplicationContext)
* Lazily (BeanFactory)

---

### 🔸 Step 6: Dependency Injection

👉 Spring injects dependencies using :

* Constructor Injection
* Setter Injection

---

### 🔸 Step 7: Bean Initialization

👉 Spring performs initialization:

* Calls `@PostConstruct`
* Calls custom init methods
* Applies AOP proxies (if needed)

---

### 🔸 Step 8: Bean Storage (Caching)

👉 Beans are stored in:

```text id="x09vnm"
Singleton Cache (Map)
```

✔ Internally:

* `singletonObjects` map
* Stored in heap

---

### 🔸 Step 9: Bean Ready to Use

```java id="3zwj4t"
Car car = context.getBean(Car.class);
```

✔ Returned from cache (not created again)

---

### 🔸 Step 10: Bean Destruction (Shutdown)

When app stops:

* Calls `@PreDestroy`
* Releases resources

---

### 📌 Example

```java id="i9w1gk"
ApplicationContext context =
    new AnnotationConfigApplicationContext(AppConfig.class);

Car car = context.getBean(Car.class);
```

✔ You get a **fully ready object**

---

## 🔹 3. Types of Spring Container

Spring provides two main containers:

---

# 🔸 3.1 BeanFactory (Basic Container)

## ✔ Definition:

> The simplest container that provides basic DI functionality

---

## 📌 Example

```java id="1xvhq2"
BeanFactory factory =
    new ClassPathXmlApplicationContext("config.xml");

Car car = factory.getBean("car", Car.class);
```

---

## ✔ Features

* Basic dependency injection
* Lazy initialization (creates bean when needed)
* Lightweight
* No advanced features

---

## ❌ Limitations

* No AOP support
* No event handling
* No internationalization
* Not used in modern apps

---

# 🔸 3.2 ApplicationContext (Advanced Container) ⭐⭐⭐

## ✔ Definition:

> A more advanced container built on top of BeanFactory

---

## 📌 Example

```java id="3yqv3o"
ApplicationContext context =
    new AnnotationConfigApplicationContext(AppConfig.class);

Car car = context.getBean(Car.class);
```

---

## ✔ Features

* All BeanFactory features +
* Eager initialization (by default)
* AOP support
* Event handling
* Internationalization (i18n)
* Better integration with Spring Boot

---

## ✔ Types of ApplicationContext

* ClassPathXmlApplicationContext
* AnnotationConfigApplicationContext
* WebApplicationContext



---

## 🔹 4. BeanFactory vs ApplicationContext

| Feature        | BeanFactory | ApplicationContext |
|----------------|-------------|--------------------|
| Level          | Basic       | Advanced           |
| Initialization | Lazy        | Eager              |
| AOP Support    | ❌ No        | ✔ Yes              |
| Events         | ❌ No        | ✔ Yes              |
| i18n           | ❌ No        | ✔ Yes              |
| Usage          | Rare        | Widely used        |

---

## 🔹 5. Lazy vs Eager Initialization

### 💤 Lazy (BeanFactory)

* Bean created only when requested

```java id="cc5nwh"
Car car = factory.getBean(Car.class);
```

---

### ⚡ Eager (ApplicationContext)

* Beans created at startup

✔ Faster runtime

❗ Slower startup

---

## 🔹 6. Why ApplicationContext is Preferred?

👉 Because it provides:

* More features
* Better performance at runtime
* Enterprise-level capabilities

---

## 🔹 7. Real-Life Analogy

### 🏭 BeanFactory

* Makes product only when you order

### 🏢 ApplicationContext

* Prepares everything in advance

---

## 🔹 8. Key Interview Points

* BeanFactory is the **root interface**
* ApplicationContext extends BeanFactory
* ApplicationContext is used in real projects
* BeanFactory is mostly theoretical now

---

## 🔹 9. Common Mistake

❌ Thinking BeanFactory is used in real projects

✔ Reality:

> Almost all modern apps use ApplicationContext

---

## 🔹 10. One-Line Summary

👉 BeanFactory:

> Basic container with lazy loading

👉 ApplicationContext:

> Advanced container with extra features and eager loading

---

# 🌱 Simple Spring Application – BeanFactory vs ApplicationContext

---

## 🔹 1. Project Overview

This example demonstrates:

* How to use **BeanFactory**
* How to use **ApplicationContext**
* How Spring manages beans

---

## 🔹 2. Maven Dependency (pom.xml)

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

## 🔹 3. POJO Class (Bean)

```java
package com.example;

public class Car {

    public void drive() {
        System.out.println("Car is running...");
    }
}
```

---

## 🔹 4. XML Configuration (config.xml)

```xml
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="
       http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd">

    <bean id="car" class="com.example.Car" />

</beans>
```

---

# 🔥 5. Using BeanFactory (Basic Container)

```java
package com.example;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class BeanFactoryDemo {

    public static void main(String[] args) {

        BeanFactory factory =
            new ClassPathXmlApplicationContext("config.xml");

        System.out.println("Before getBean()");

        Car car = factory.getBean("car", Car.class);

        car.drive();
    }
}
```

---

## 🧠 Behavior

* Bean is created **only when `getBean()` is called**
* Lazy initialization

---

# 🚀 6. Using ApplicationContext (Advanced Container)

```java
package com.example;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ApplicationContextDemo {

    public static void main(String[] args) {

        ApplicationContext context =
            new ClassPathXmlApplicationContext("config.xml");

        System.out.println("Context loaded");

        Car car = context.getBean("car", Car.class);

        car.drive();
    }
}
```

---

## 🧠 Behavior

* Bean is created **at container startup**
* Eager initialization

---

# 🔍 7. Key Difference Demonstration

| Step           | BeanFactory    | ApplicationContext |
|----------------|----------------|--------------------|
| Bean Creation  | On `getBean()` | At startup         |
| Initialization | Lazy           | Eager              |
| Features       | Basic          | Advanced           |

---

# 🎯 8. Output Comparison

## BeanFactory Output:

```text
Before getBean()
Car is running...
```

## ApplicationContext Output:

```text
Context loaded
Car is running...
```

👉 Difference is internal (creation timing)

---

# 🧠 9. Important Notes

* Both use same configuration
* ApplicationContext is preferred in real projects
* BeanFactory is mostly for learning/internal use

---

# 🚀 10. Interview One-Liner

> “BeanFactory creates beans lazily when requested, while ApplicationContext creates them eagerly at startup and provides additional enterprise features.”

---


# 🔥 Spring Container – Tricky Interview Questions

## 🔹 1. Is `BeanFactory` still used in real projects?

👉 Expected answer:

* ❌ Rarely used directly
* ✔ Internally used by Spring
* ✔ Developers use `ApplicationContext`

---

## 🔹 2. What happens internally when Spring starts?

👉 Strong answer should include:

* Reads configuration
* Scans components
* Creates beans
* Injects dependencies
* Stores in container

⚠️ Weak answer = “Spring creates beans”

---

## 🔹 3. Why does `ApplicationContext` use eager initialization?

👉 Answer:

* To detect errors early (startup time)
* Improves runtime performance

---

## 🔹 4. Can we make `ApplicationContext` lazy?

👉 Yes ✔

```java
@Lazy
@Component
class Car {}
```

OR

```properties
spring.main.lazy-initialization=true
```

---

## 🔹 5. Which container supports AOP?

👉 Only:

* ✔ `ApplicationContext`

---

## 🔹 6. What is the relationship between `BeanFactory` and `ApplicationContext`?

👉

* `ApplicationContext` **extends** `BeanFactory`
* Adds more features

---

## 🔹 7. What happens if a bean fails during startup in ApplicationContext?

👉

* Application fails to start ❌
* Exception is thrown early

✔ This is actually an advantage

---

## 🔹 8. Does BeanFactory support Bean Lifecycle?

👉 Trick question ⚠️

✔ Yes (basic lifecycle)
❌ But lacks advanced features

---

## 🔹 9. Which container is faster?

👉 Depends:

* Startup → BeanFactory ✔ faster
* Runtime → ApplicationContext ✔ faster

---

## 🔹 10. Can we have multiple ApplicationContext in one app?

👉 Yes ✔

* Parent-child contexts possible

---

# 🔥 Scenario-Based Questions

## 🔹 11. Your application startup is slow. Why?

👉 Possible reasons:

* Eager initialization
* Too many beans
* Heavy dependencies

✔ Solution:

* Use `@Lazy`

---

## 🔹 12. You want bean creation only when needed. Which container?

👉 BeanFactory ✔
(or lazy config in ApplicationContext)

---

## 🔹 13. You need AOP + events. Which container?

👉 ApplicationContext ✔

---

## 🔹 14. You got `NoSuchBeanDefinitionException`. Why?

👉

* Bean not scanned
* Wrong configuration
* Missing annotation

---

## 🔹 15. You got `BeanCurrentlyInCreationException`. Why?

👉 Circular dependency
(especially with constructor injection)

---

# ⚡ Rapid Fire (High Probability)

1. ApplicationContext extends? → BeanFactory
2. Default behavior of ApplicationContext? → Eager loading
3. Default behavior of BeanFactory? → Lazy loading
4. Which is used in Spring Boot? → ApplicationContext
5. Can BeanFactory do DI? → Yes

---

# 🧠 Deep Understanding Question (VERY TRICKY)

## 🔹 16. If ApplicationContext is built on BeanFactory, why not just use BeanFactory?

👉 Strong answer:

> BeanFactory provides only basic DI, while ApplicationContext adds enterprise features like AOP, events, internationalization, and better lifecycle handling.

---

# 🎯 Killer Interview Question

## 🔹 17. What happens when you call `getBean()`?

1. Check singleton cache
2. If exists → return
3. If not:
    * Create bean
    * Inject dependencies
    * Initialize
    * Store in cache
    * Return

---

# 🚀 Pro Tip (Real Interview Insight)

Interviewers test:

* Lazy vs Eager
* Internal working
* Exceptions
* Real scenarios

👉 If you answer with **examples + reasoning**, you stand out.

---