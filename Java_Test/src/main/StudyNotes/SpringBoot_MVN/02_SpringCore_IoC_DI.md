# 🧠 TOPIC 2: Spring Core (IoC & Dependency Injection)

---

### 1. IoC (Inversion of Control)

**Inversion of Control (IOC)**: 
> The Spring IoC Container is the core of the Spring Framework. It is a design principle where the control of object creation and lifecycle is managed by a framework or container rather than by the developer. Spring IOC Container is responsible for creating, configuring, and managing the lifecycle of objects called beans.

👉 IoC (Inversion of Control) container manages:

* Object creation
* Dependency injection
* Lifecycle of beans

👉 The IoC Container retrieves object configuration from:

* XML Configuration Files
* Java Configuration Classes
* Java Annotations

> Since object creation and lifecycle management are handled by the IoC Container, developers do not need to manually instantiate dependencies. This reduces tight coupling in the application.

* Concept: Control is given to the container instead of developer
* Container manages object creation

👉 Example:

* ❌ Without Spring → `new Object()`
* ✅ With Spring → container creates object

---

### 2. DI (Dependency Injection)

**Dependency Injection**: 
> Dependency Injection (DI) is a key feature provided by Spring IoC. It is a design pattern and a part of IOC container. The Spring Core module injects dependencies into objects via different injection methods, ensuring that components are loosely coupled.

* Inject dependencies instead of creating them manually by developer.

Types of DI:
* **Constructor Injection**: Dependencies are provided through the class constructor. (Recommended for mandatory dependencies).
* **Setter Injection**: Dependencies are provided through public setter methods. (Good for optional dependencies).
* **Field Injection**: Dependencies are injected directly into fields using @Autowired. (Common in Spring Boot, though less preferred for unit testing).


1. Setter Dependency Injection (SDI)
   > In Setter Injection, dependencies are injected using setter methods. The property to be injected is declared inside the <property> tag in the XML configuration file.

Example:
```xml
<bean id="myBean" class="com.example.MyClass">
    <property name="dependency" ref="myDependency"/>
</bean>
```

2. Constructor Dependency Injection (CDI)
   > In Constructor Injection, dependencies are passed via the class constructor. The dependency is set using the <constructor-arg> tag in the XML configuration file.

Example:
```xml
<bean id="myBean" class="com.example.MyClass">
    <constructor-arg ref="myDependency"/>
</bean>
```

---

### 3. Spring Container

* Responsible for:

    * Creating objects (beans)
    * Managing lifecycle
    * Injecting dependencies

Types:

* BeanFactory (basic)
* ApplicationContext (advanced, mostly used)

#### BeanFactory (Basic Container)

👉 BeanFactory is the **simplest IoC container**

👉 It provides:

* Basic dependency injection
* Lazy initialization (default)

---

##### 🔹 Example

```java
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

public class Main {
    public static void main(String[] args) {
        BeanFactory factory =
            new XmlBeanFactory(new ClassPathResource("beans.xml"));

        MyBean bean = (MyBean) factory.getBean("myBean");
    }
}
```

---

##### 🔹 Key Points

* Beans created **only when requested**
* Lightweight container
* No advanced features

---

#### ApplicationContext (Advanced Container)

👉 ApplicationContext is **advanced version of BeanFactory**

👉 It provides:

* Eager initialization
* Enterprise features

---

##### 🔹 Example

```java
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {
    public static void main(String[] args) {

        ApplicationContext context =
            new ClassPathXmlApplicationContext("beans.xml");

        MyBean bean = context.getBean(MyBean.class);
    }
}
```

---

##### 🔹 Key Features

* Eager bean loading (by default)
* Internationalization (i18n)
* Event handling
* AOP integration

---

#### BeanFactory vs ApplicationContext

| Feature        | BeanFactory    | ApplicationContext   |
| -------------- | -------------- | -------------------- |
| Initialization | Lazy           | Eager                |
| Performance    | Faster startup | Slower startup       |
| Memory         | Low            | Higher               |
| Features       | Basic DI       | Full Spring features |
| AOP Support    | ❌              | ✅                    |
| Events         | ❌              | ✅                    |
| i18n           | ❌              | ✅                    |

---

#### Internal Working (Simplified)

👉 BeanFactory:

```text
getBean() → create bean → return
```

👉 ApplicationContext:

```text
Startup → scan config → create all beans → ready
```

---

#### When to Use

👉 BeanFactory:

* Memory-constrained systems
* Rarely used today

👉 ApplicationContext:

* Almost all Spring Boot apps
* Enterprise applications

---

#### Important Implementations

##### BeanFactory

* XmlBeanFactory (deprecated)

##### ApplicationContext

* ClassPathXmlApplicationContext
* AnnotationConfigApplicationContext
* Spring Boot uses this internally

---

#### 🎯 Interview Q&A

---

##### ❓ Difference between BeanFactory and ApplicationContext?

👉 Answer:

> BeanFactory is a basic IoC container with lazy initialization, while ApplicationContext is an advanced container that supports eager initialization along with features like AOP, event handling, and internationalization.

---

##### ❓ Why ApplicationContext is preferred?

👉 Answer:

> Because it provides enterprise features and pre-instantiates beans, making applications faster at runtime and more feature-rich.

---

##### ❓ What is lazy vs eager initialization?

###### 👉 Lazy:

* Bean created when needed

```java
class LazyExample {
    private String data;
    public String getData() {
        if (data == null) {
            data = "Loaded Data"; // created only when needed
        }
        return data;
    }
}
```

```java
@Lazy
@Component
public class MyBean {
    
}
```

🧠 How it Works
```text
App Start → No object created
First call → Object created
Next calls → Same object reused
```

✅ Advantages
* Saves memory
* Faster startup time
* Good for heavy objects

❌ Disadvantages
* Slight delay on first use
* Not thread-safe (unless handled)

###### 👉 Eager:

* Bean created at startup, even if not used

```java
class EagerExample {
    private String data = "Loaded Data"; // created immediately
    public String getData() {
        return data;
    }
}
```

```java
@Component
public class MyBean {
    
}
```
🧠 How it Works

```text
App Start → Object created
Later → Directly used
```

✅ Advantages
* No delay during usage
* Thread-safe (usually)
* Fail fast (errors detected early)

❌ Disadvantages
* Higher memory usage
* Slower startup time

| Feature       | Lazy Initialization     | Eager Initialization    |
|---------------|-------------------------|-------------------------|
| Creation Time | On demand               | At startup              |
| Startup Time  | Fast                    | Slow                    |
| Memory Usage  | Low                     | High                    |
| Performance   | Slight delay first time | Fast runtime            |
| Use Case      | Heavy/rare objects      | Frequently used objects |


---

# 🚀 Final Takeaway

* BeanFactory = Basic + Lazy
* ApplicationContext = Advanced + Eager

👉 In real-world:

> Always use ApplicationContext (Spring Boot uses it internally)

---

### 4. Beans

* Objects managed by Spring container

Annotations:

* `@Component`
* `@Service`
* `@Repository`
* `@Controller`

---

### 5. Bean Scope

#### 1️⃣ What is Bean Scope?

👉 Bean Scope defines:

> **How many instances of a bean are created and how long they live**

---

#### 2️⃣ Why Scope Matters

👉 Scope directly impacts:

* Memory usage
* Performance
* Thread safety

👉 Example:

* Shared object → faster but risky (thread issues)
* New object → safe but more memory

---

#### 3️⃣ Types of Bean Scopes

| Scope       | Description                |
|-------------|----------------------------|
| Singleton   | One instance per container |
| Prototype   | New instance every time    |
| Request     | One per HTTP request       |
| Session     | One per HTTP session       |
| Application | One per ServletContext     |
| WebSocket   | One per WebSocket session  |

---

#### 4️⃣ Singleton Scope (Default)

##### 📌 Definition

👉 Only **one instance** of bean is created for entire Spring container

---

##### 🔧 Example

```java
@Component
public class MyBean {
}
```

---

##### 🧠 Behavior

```text
App Start → Bean created once
Multiple calls → Same instance reused
```

---

##### ✅ Advantages

* Memory efficient
* Fast access
* Shared across app

---

##### ❌ Disadvantages

* Not thread-safe by default
* Shared state issues

---

##### ⚠️ Important

👉 Singleton ≠ Java Singleton

👉 It is **per Spring container**, not JVM-wide

---

#### 5️⃣ Prototype Scope

##### 📌 Definition

👉 New bean instance created **every time requested**

---

##### 🔧 Example

```java
@Component
@Scope("prototype")
public class MyBean {
}
```

---

##### 🧠 Behavior

```text
Each getBean() → New object created
```

---

##### ✅ Advantages

* Thread-safe (no shared state)
* Independent objects

---

##### ❌ Disadvantages

* Higher memory usage
* No lifecycle management after creation

---

##### ⚠️ Important

👉 Spring does NOT manage:

* Destruction lifecycle of prototype beans

---

#### 6️⃣ Web Scopes (Spring Web)

---

##### 🔹 Request Scope

👉 One bean per HTTP request

```java
@Component
@Scope(value = "request")
public class RequestBean {
}
```

👉 New object for every request

---

##### 🔹 Session Scope

👉 One bean per user session

```java
@Component
@Scope("session")
public class SessionBean {
}
```

👉 Shared across requests of same user

---

##### 🔹 Application Scope

👉 One bean per ServletContext

```java
@Component
@Scope("application")
public class AppBean {
}
```

---

##### 🔹 WebSocket Scope

👉 One bean per WebSocket session

---

#### 7️⃣ Custom Scope (Advanced)

👉 You can define your own scope

```java
@Scope("customScope")
@Component
public class CustomBean {
}
```

👉 Requires:

* Custom Scope implementation

---

#### 8️⃣ Bean Scope vs Thread Safety

| Scope       | Thread Safety         |
|-------------|-----------------------|
| Singleton   | ❌ Not safe by default |
| Prototype   | ✅ Safe                |
| Request     | ✅ Safe                |
| Session     | ⚠️ Depends            |
| Application | ❌ Shared              |

---

##### 🔥 Important Insight

👉 Singleton beans must be:

* Stateless
  OR
* Properly synchronized

---

#### 9️⃣ Common Pitfalls

---

##### ❌ Prototype inside Singleton

```java
@Autowired
private PrototypeBean bean;
```

👉 Problem:

* Injected only once (acts like singleton)

👉 Solution:

```java
@Autowired
private ObjectFactory<PrototypeBean> factory;
```

---

##### ❌ Mutable Singleton State

```java
private int counter;
```

👉 Causes race conditions

---

#### 1️⃣0️⃣ Internal Working

---

##### Singleton

```text
Container start → create bean → store in cache
```

---

##### Prototype

```text
Request → create new bean → return
(no caching)
```

---

#### 🎯 Interview Q&A

---

##### ❓ What is default bean scope?

👉 Singleton

---

##### ❓ Difference between Singleton and Prototype?

👉 Singleton → one shared instance

👉 Prototype → new instance per request

---

##### ❓ Why singleton is not thread-safe?

👉 Because multiple threads share same object

---

##### ❓ How to handle prototype inside singleton?

👉 Use:

* ObjectFactory
* Provider
* Lookup method

---

#### 🚀 Real-World Usage

| Scenario           | Scope     |
|--------------------|-----------|
| Service layer      | Singleton |
| DTO / temp objects | Prototype |
| Request data       | Request   |
| User data          | Session   |

---

### 6. Spring IOC vs Spring DI

| **Spring IoC (Inversion of Control)**                                                                                                                               | **Spring Dependency Injection (DI)**                                                                                                                               |
|---------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Spring IoC Container is the core of the Spring Framework. It creates the objects, configures and assembles their dependencies, and manages their entire life cycle. | Spring Dependency Injection is a way to inject the dependency of a framework component by the following ways of spring: Constructor Injection and Setter Injection |
| Spring helps in creating objects, managing objects, configurations, etc. because of IoC (Inversion of Control).                                                     | Spring framework helps in the creation of loosely-coupled applications because of Dependency Injection.                                                            |
| Spring IoC is achieved through Dependency Injection.                                                                                                                | Dependency Injection is the method of providing the dependencies, and Inversion of Control is the end result of Dependency Injection.                              |
| IoC is a design principle where the control flow of the program is inverted.                                                                                        | Dependency Injection is one of the subtypes of the IOC principle.                                                                                                  |
| Aspect-Oriented Programming is one way to implement Inversion of Control.                                                                                           | In case of any changes in business requirements, no code change is required.                                                                                       |


## 🎯 Interview Questions

### 🟢 Basic

1. What is IoC?
2. What is Dependency Injection?
3. What is a Spring Bean?
4. What is ApplicationContext?

### 🟡 Moderate

5. Difference between BeanFactory and ApplicationContext?
6. Types of Dependency Injection?
7. What is bean scope?
8. Difference between `@Component`, `@Service`, `@Repository`?

### 🔴 Tricky / Real Interview

9. Why is constructor injection preferred over field injection?
10. What happens if two beans of same type exist?
11. How does Spring resolve dependency injection internally?
