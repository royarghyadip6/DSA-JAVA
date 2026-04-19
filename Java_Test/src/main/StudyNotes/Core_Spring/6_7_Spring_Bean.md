
# 🔹 What is a Bean?

👉 A **Spring Bean** is:

> An object that is created, managed, and maintained by the Spring container.

✔ Not just any object → only those handled by Spring
✔ Comes with lifecycle + dependency injection support

---

## 🔁 Bean Lifecycle (Core Flow)

Think in **3 main stages**:

> **Instantiation → Initialization → Destruction**

---

### 🔸 1. Instantiation

👉 Spring creates the object

```java
Car car = new Car();
```

✔ Done using reflection
✔ No `new` keyword in your code

---

### 🔸 2. Initialization

👉 Bean is prepared before use

What happens here:

* Dependencies are injected
* Initialization methods are called

### Ways to initialize:

```java
@PostConstruct
public void init() {}
```

or

```java
@Bean(initMethod = "init")
```

✔ Bean becomes **ready to use**

---

### 🔸 3. Destruction

👉 Before application shutdown

```java
@PreDestroy
public void destroy() {}
```

✔ Used to:

* Close DB connections
* Release resources

---

## 🔄 Lifecycle Flow (Simple)

```text
Create → Inject → Initialize → Use → Destroy
```

---

## ⚡ Important Interview Points

* Instantiation = object creation
* Initialization = dependency injection + setup
* Destruction = cleanup before shutdown
* `@PostConstruct` and `@PreDestroy` are lifecycle hooks

---

## 🎯 One-Line Answer

> “A Spring Bean is an object managed by the container, which goes through instantiation, initialization, and destruction phases during its lifecycle.”

---

---

# 🌱 Bean Scopes in Spring

👉 Scope = **how many instances of a bean Spring creates**

---

## 🔹 1. Singleton (Default)

### 👉 What it means

> Only **one instance per Spring container**
* Same object reused everywhere
* Default scope in Spring

### 📌 Example

```java
@Component
public class Car {
}
```

```java
Car c1 = context.getBean(Car.class);
Car c2 = context.getBean(Car.class);

System.out.println(c1 == c2); // true
```

---

## 🔹 2. Prototype

### 👉 What it means

> **New object every time** you request
* Multiple instances
* Spring creates but **does not manage destruction**

### 📌 Example

```java
@Component
@Scope("prototype")
public class Car {
}
```

```java
Car c1 = context.getBean(Car.class);
Car c2 = context.getBean(Car.class);

System.out.println(c1 == c2); // false
```

---

## 🔹 3. Request Scope (Web)

### 👉 What it means

> One bean per **HTTP request**
* New object created for each request
* Shared within that request only

### 📌 Example

```java
@Component
@Scope("request")
public class RequestBean {
}
```

### 📌 Real Example

* User sends API call → new bean created
* Same request → same bean
* New request → new bean

---

## 🔹 4. Session Scope (Web)

### 👉 What it means

> One bean per **user session**

* One object per logged-in user
* Shared across multiple requests of that user

### 📌 Example

```java
@Component
@Scope("session")
public class SessionBean {
}
```

### 📌 Real Example

* User logs in → session created
* Same user → same bean
* Different user → different bean

---

## ⚡ Quick Comparison

| Scope     | Instances | Lifetime         |
|-----------|-----------|------------------|
| Singleton | 1         | App lifetime     |
| Prototype | Many      | Until GC         |
| Request   | 1/request | One HTTP request |
| Session   | 1/user    | Session lifetime |

---

## 🎯 One-Line Answer

> “Bean scope defines how many instances of a bean Spring creates, such as singleton (one per container), prototype (new each time), request (per HTTP request), and session (per user session).”

---

# 🔥 Problem Scenario

👉 You have:

* A **Singleton bean**
* Depends on a **Prototype bean**

---

### 📌 Example

```java
@Component
@Scope("prototype")
class Engine {
}
```

```java
@Component
class Car {

    private final Engine engine;

    public Car(Engine engine) {
        this.engine = engine;
    }

    public void showEngine() {
        System.out.println(engine);
    }
}
```

---

### 🧪 Test

```java
Car c1 = context.getBean(Car.class);
c1.showEngine();

Car c2 = context.getBean(Car.class);
c2.showEngine();
```

---

## ❗ What Most People Expect (Wrong)

> “Prototype means new object every time → so new Engine each call”

❌ WRONG

---

## ✅ Actual Behavior

👉 Output will show **same Engine instance**

✔ Because:

* `Car` is singleton → created only once
* `Engine` is injected **only once at creation time**

---

## 🧠 Key Understanding

> Prototype inside Singleton behaves like Singleton

✔ Injection happens **only once**
✔ Not on every method call

---

## 🔥 Why This Happens

Spring does:

1. Create `Car` (singleton)
2. Inject `Engine` (prototype)
3. Store `Car` in container

👉 After that:

* No new Engine is created
* Same instance reused

---

## 🚀 How to Get New Prototype Every Time?

👉 You must **ask Spring explicitly each time**

---

### ✔ Option 1: ObjectProvider (Recommended)

```java
@Component
class Car {

    private final ObjectProvider<Engine> engineProvider;

    public Car(ObjectProvider<Engine> engineProvider) {
        this.engineProvider = engineProvider;
    }

    public void showEngine() {
        Engine engine = engineProvider.getObject();
        System.out.println(engine);
    }
}
```

✔ New Engine each call

---

### ✔ Option 2: @Lookup

```java
@Component
class Car {

    @Lookup
    public Engine getEngine() {
        return null; // Spring overrides this
    }
}
```

---

### ✔ Option 3: ApplicationContext (not preferred)

```java
@Autowired
ApplicationContext context;

Engine engine = context.getBean(Engine.class);
```

---

## ⚡ Interview Answer (Perfect)

> “When a prototype bean is injected into a singleton bean, only one instance is created at the time of singleton initialization. To get a new prototype instance each time, we must use ObjectProvider, @Lookup, or ApplicationContext.”

---


---

# ⚙️ Bean Configuration in Spring

👉 Bean configuration =

> **How you tell Spring what objects (beans) to create and how to wire them**

There are **3 main approaches**:

---

## 🔹 1. XML Configuration (Old / Legacy)

### 📌 Example

#### config.xml

```xml
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="
       http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd">

    <bean id="engine" class="com.example.Engine"/>

    <bean id="car" class="com.example.Car">
        <constructor-arg ref="engine"/>
    </bean>

</beans>
```

### 📌 Usage

```java
ApplicationContext context =
    new ClassPathXmlApplicationContext("config.xml");

Car car = context.getBean("car", Car.class);
```

### 🧠 Key Points

1. [ ] ✔ Full control
2. [ ] ✔ Clear wiring (explicit)
3. [ ] ❌ Very verbose
4. [ ] ❌ Hard to maintain

### 🎯 When used?

* Legacy applications
* XML-heavy enterprise systems

---

## 🔹 2. Annotation-Based Configuration (Most Used)

## 👉 Idea

Use annotations directly in Java classes

### 📌 Example

```java
@Component
class Engine {
}
```

```java
@Component
class Car {

    private final Engine engine;

    public Car(Engine engine) {
        this.engine = engine;
    }
}
```

### 📌 Enable scanning

```java
@Configuration
@ComponentScan("com.example")
class AppConfig {
}
```

### 📌 Usage

```java
ApplicationContext context =
    new AnnotationConfigApplicationContext(AppConfig.class);

Car car = context.getBean(Car.class);
```

### 🧠 Key Points

1. [ ] ✔ Automatic bean creation
2. [ ] ✔ Less configuration
3. [ ] ✔ Clean code
4. [ ] ❌ Less explicit control
5. [ ] ❌ Hidden wiring (can confuse beginners)

### 🎯 Common Annotations

* `@Component` → generic bean
* `@Service` → service layer
* `@Repository` → DAO layer
* `@Controller` → web layer
* `@Autowired` → inject dependency

---

## 🔹 3. Java-Based Configuration (@Configuration)

### 👉 Idea

Define beans using Java methods instead of XML

### 📌 Example

```java
@Configuration
class AppConfig {

    @Bean
    public Engine engine() {
        return new Engine();
    }

    @Bean
    public Car car() {
        return new Car(engine());
    }
}
```

### 📌 Usage

```java
ApplicationContext context =
    new AnnotationConfigApplicationContext(AppConfig.class);

Car car = context.getBean(Car.class);
```

### 🧠 Key Points

1. [ ] ✔ Type-safe (compile-time checking)
2. [ ] ✔ Full control
3. [ ] ✔ No XML needed
4. [ ] ✔ Used heavily in Spring Boot

### 🎯 When to use?

* When you need **manual control**
* Third-party classes (no annotations possible)

---

## 🔥 Comparison (Important)

| Feature     | XML      | Annotation   | Java Config |
|-------------|----------|--------------|-------------|
| Style       | External | Inside class | Java code   |
| Readability | Low      | High         | High        |
| Control     | High     | Medium       | High        |
| Usage       | Legacy   | Most common  | Modern      |

---

## 🧠 Important Interview Insights

#### ✔ @Component vs @Bean

* `@Component` → auto-detected
* `@Bean` → manually defined

---

### ✔ Can we mix all three?

👉 Yes ✔

* Real projects often mix them

---

### ✔ Which is best?

👉 Answer:

* Annotation + Java Config (modern approach)

---

## 🎯 One-Line Answer

> “Spring supports bean configuration using XML, annotations, and Java-based configuration, where annotations and Java config are preferred in modern applications due to better readability and maintainability.”

---

---

# 🔥 Spring Beans – Tricky Interview Questions

## 🔹 1. What is a Spring Bean?

👉 Don’t say just “object”.

✔ Better:

> “An object managed by the Spring container with lifecycle and dependency injection support.”

---

## 🔹 2. What is the default scope of a bean?

👉 **Singleton**

⚠️ Follow-up:

* Is it GoF Singleton?
  👉 ❌ No
  👉 ✔ One instance **per container**, not per JVM

---

## 🔹 3. When is a Singleton bean created?

👉 By default:

* ✔ At container startup (**eager initialization**)

⚠️ Exception:

* If `@Lazy` is used → created on first use

---

## 🔹 4. What about Prototype scope lifecycle?

👉 Trick question ⚠️

✔ Spring:

* Creates the bean
* Injects dependencies

❌ After that:

* Spring does NOT manage destruction

---

## 🔹 5. Will `@PreDestroy` work for Prototype beans?

👉 ❌ No

✔ Only works for Singleton beans

---

## 🔹 6. Can Singleton bean depend on Prototype bean?

👉 Yes ✔

But ⚠️:

* Prototype bean is created **only once at injection time**

👉 Not every time you use it

---

## 🔹 7. How to get new Prototype instance every time inside Singleton?

👉 Use:

* `ObjectFactory`
* `Provider`
* `@Lookup`

---

## 🔹 8. What happens if two beans have same name?

👉 Spring throws:

* **BeanDefinitionOverrideException** (in newer versions)

---

## 🔹 9. What is Bean lifecycle order?

👉 Expected answer:

* Instantiate
* Inject dependencies
* Initialize
* Ready
* Destroy

---

## 🔹 10. What is BeanPostProcessor?

👉 Advanced question

✔ It allows:

* Custom logic before and after bean initialization

---

## 🔹 11. What is the difference between @Component and @Bean?

👉

* `@Component` → class-level (auto-detection)
* `@Bean` → method-level (manual control)

---

## 🔹 12. Can we have multiple beans of same type?

👉 Yes ✔

👉 Use:

* `@Qualifier`
* `@Primary`

---

## 🔹 13. What happens if Spring cannot find a bean?

👉 Exception:

* **NoSuchBeanDefinitionException**

---

## 🔹 14. Can we make a bean immutable?

👉 Yes ✔

👉 Using:

* Constructor injection
* `final` fields

---

## 🔹 15. What is eager vs lazy initialization?

👉

* Eager → created at startup
* Lazy → created when needed

---

# ⚡ Rapid Fire (High Probability)

1. Default scope? → Singleton
2. Prototype managed fully? → No
3. Can bean have multiple scopes? → No
4. Destroy method called for prototype? → No
5. Can Spring manage lifecycle? → Yes

---

# 🎯 Killer Question (Very Common)

## ❓ “If a singleton bean depends on a prototype bean, how many instances are created?”

👉 Most people fail here.

✔ Correct answer:

> Only ONE prototype instance is injected (at creation time)

---

# 🚀 Pro Tip

Interviewers love:

* Prototype vs Singleton behavior
* Lifecycle edge cases
* Multiple bean conflicts

👉 If you answer with **“why”**, not just “what”, you stand out.

---

---

# 🔥 Tricky Interview Questions – Bean Configuration

## 🔹 1. What is the difference between `@Component` and `@Bean`?

👉 Expected answer:

* `@Component` → class-level, auto-detected via scanning
* `@Bean` → method-level, manually defined in `@Configuration`

⚠️ Follow-up:

> When would you use `@Bean`?

✔ When:

* Class is from third-party library
* You need custom creation logic

---

## 🔹 2. Can we use `@Component` and `@Bean` together?

👉 Yes ✔
But not on the same class for the same purpose.

✔ Real use:

* `@Component` for your classes
* `@Bean` for external/complex beans

---

## 🔹 3. What happens if `@ComponentScan` is missing?

👉

* Spring won’t detect beans
* You’ll get:
  **NoSuchBeanDefinitionException**

---

## 🔹 4. Can Spring work without XML?

👉 Yes ✔

* Using annotations + Java config

---

## 🔹 5. Can we mix XML, Annotation, and Java Config?

👉 Yes ✔
✔ Real-world apps often mix them

---

## 🔹 6. Which configuration is used in Spring Boot?

👉

* Mostly Annotation + Java Config
* XML is rarely used

---

## 🔹 7. What is `@Configuration` vs `@Component`?

👉

* `@Configuration` → used for defining beans (`@Bean`)
* `@Component` → general-purpose bean

⚠️ Important:

* `@Configuration` ensures **CGLIB proxying**

---

## 🔹 8. What happens if you remove `@Configuration` from config class?

👉 Tricky ⚠️

✔ `@Bean` methods will still work
❌ But:

* No proxying
* Multiple instances may be created unexpectedly

---

## 🔹 9. What is the role of `@ComponentScan`?

👉

* Scans packages for annotated classes
* Registers them as beans

---

## 🔹 10. How does Spring know which packages to scan?

👉

* Defined in `@ComponentScan`
* In Spring Boot → auto-scans from main class package

---

# 🔥 Scenario-Based Questions

## 🔹 11. You have two beans of same type. What happens?

👉

* Spring throws:
  **NoUniqueBeanDefinitionException**

✔ Solution:

* `@Qualifier`
* `@Primary`

---

## 🔹 12. You forgot `@Component` on a class. What happens?

👉

* Bean not created
* Injection fails

---

## 🔹 13. Can we create a bean without annotations?

👉 Yes ✔
Using:

* XML
* `@Bean` method

---

## 🔹 14. What is the order of configuration loading?

👉

* XML / Java Config loaded first
* Then component scanning
* Then bean creation

---

## 🔹 15. Why is Java Config better than XML?

👉

* Type-safe
* Refactoring-friendly
* Less verbose

---

# ⚡ Rapid Fire

1. Default config style in Spring Boot? → Annotation
2. Can `@Bean` exist without `@Configuration`? → Yes (but risky)
3. Who scans `@Component`? → `@ComponentScan`
4. XML still used? → Rarely
5. Can we define same bean twice? → No (conflict)

---

# 🎯 Killer Question (Very Common)

## ❓ “Why does `@Configuration` use proxy (CGLIB)?”

👉 Strong answer:

> “To ensure that `@Bean` methods return the same singleton instance even when called multiple times.”

---

# 🚀 Pro Tip

Interviewers love:

* `@Component` vs `@Bean`
* Missing annotations issues
* Multiple bean conflicts
* Proxy behavior

👉 If you explain **why**, not just **what**, you stand out.

---
