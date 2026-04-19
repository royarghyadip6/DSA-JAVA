# ⚡ What is AOP (Aspect-Oriented Programming)?

> 👉 AOP is a technique used to **separate cross-cutting concerns** from business logic.

---

## 🔹 What are “cross-cutting concerns”?

These are functionalities used **across multiple places**:

* Logging
* Security
* Transactions
* Exception handling

---

## 📌 Problem without AOP

```java
class PaymentService {

    public void pay() {
        System.out.println("Logging...");
        System.out.println("Security check...");
        System.out.println("Processing payment...");
    }
}
```

❌ Logging & security are mixed with business logic

❌ Code becomes messy & duplicated

---

## ✅ With AOP

```java
class PaymentService {

    public void pay() {
        System.out.println("Processing payment...");
    }
}
```

👉 Logging & security handled separately ✔

---

# 🔹 Core Concepts (Must Know)

---
start

Great—let’s go **deep but structured**, one concept at a time with **clear explanation + code**. No fluff.

---

## ⚡ 1. Aspect

### 👉 What is it?

> An **Aspect** is a class that contains cross-cutting logic (logging, security, etc.)

### 📌 Example

```java
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {
}
```

### 🧠 Key Idea

* This class does **not contain business logic**
* It contains logic that applies **across multiple classes**

---

## ⚡ 2. Advice

### 👉 What is it?

> Advice defines **what action to perform** and **when to perform it**

### 🔹 Types of Advice

### ✔ @Before

Runs **before method execution**

```java
@Before("execution(* com.example.service.*.*(..))")
public void beforeAdvice() {
    System.out.println("Before method");
}
```

### ✔ @After

Runs **after method completes (success or exception)**

```java
@After("execution(* com.example.service.*.*(..))")
public void afterAdvice() {
    System.out.println("After method");
}
```

### ✔ @AfterReturning

Runs only if method succeeds

```java
@AfterReturning("execution(* com.example.service.*.*(..))")
public void afterReturning() {
    System.out.println("Method executed successfully");
}
```

### ✔ @AfterThrowing

Runs only if exception occurs

```java
@AfterThrowing("execution(* com.example.service.*.*(..))")
public void afterThrowing() {
    System.out.println("Exception occurred");
}
```

### ✔ @Around (Most Powerful)

Controls entire execution

```java
@Around("execution(* com.example.service.*.*(..))")
public Object around(ProceedingJoinPoint pjp) throws Throwable {

    System.out.println("Before");

    Object result = pjp.proceed(); // actual method call

    System.out.println("After");

    return result;
}
```

### 🧠 Key Idea

* `@Around` = full control
* Others = specific hooks

---

## ⚡ 3. Join Point

### 👉 What is it?

> A **point in program execution** where AOP can be applied

✔ Mostly **method execution**

### Example:

```java
public void pay() {}
```

👉 This method execution = **Join Point**

### 🧠 Important

* Not all join points are used in Spring
* Spring AOP supports only **method-level join points**

---

## ⚡ 4. Pointcut

### 👉 What is it?

> A **filter expression** to select specific join points

### 📌 Example

```java
@Pointcut("execution(* com.example.service.*.*(..))")
public void serviceMethods() {}
```

### Use with advice:

```java
@Before("serviceMethods()")
public void log() {
    System.out.println("Logging...");
}
```

### 🧠 Breakdown

```text
execution(* com.example.service.*.*(..))
```

* `*` → any return type
* `service.*` → any class
* `*(..)` → any method with any arguments

---

## ⚡ 5. Target

### 👉 What is it?

> The actual **business class** where logic exists

### 📌 Example

```java
@Component
public class PaymentService {

    public void pay() {
        System.out.println("Payment done");
    }
}
```

### 🧠 Key Idea

* This class is **not aware of AOP**
* Clean business logic

---

## ⚡ 6. Proxy

### 👉 What is it?

> A **wrapper object** created by Spring to apply AOP

### 🧠 How it works

Instead of calling:

```text
Client → PaymentService
```

Spring does:

```text
Client → Proxy → Aspect → PaymentService
```

### 📌 Example Flow

1. Client calls `pay()`
2. Proxy intercepts
3. Runs `@Before` advice
4. Calls actual method
5. Runs `@After` advice

---

## 🧠 Key Insight

* You never directly call the real object
* Proxy controls execution

---

## ⚡ 7. Full Example (Putting Everything Together)

### 📌 Service (Target)

```java
@Component
public class PaymentService {

    public void pay() {
        System.out.println("Payment processing...");
    }
}
```

### 📌 Aspect

```java
@Aspect
@Component
public class LoggingAspect {

    @Pointcut("execution(* PaymentService.pay(..))")
    public void paymentPointcut() {}

    @Before("paymentPointcut()")
    public void before() {
        System.out.println("Before payment");
    }

    @After("paymentPointcut()")
    public void after() {
        System.out.println("After payment");
    }
}
```

### 📌 Config

```java
@Configuration
@ComponentScan("com.example")
@EnableAspectJAutoProxy
public class AppConfig {
}
```

### 📌 Main

```java
ApplicationContext context =
    new AnnotationConfigApplicationContext(AppConfig.class);

PaymentService service = context.getBean(PaymentService.class);
service.pay();
```

### 🔍 Output

```text
Before payment
Payment processing...
After payment
```

## ⚡ Final Mental Model

* Aspect = cross-cutting logic
* Advice = when to run
* Join Point = where (method)
* Pointcut = filter
* Target = business class
* Proxy = executor

---

# 🔹 Types of Advice (Quick)

| Advice          | When it runs   |
|-----------------|----------------|
| @Before         | Before method  |
| @After          | After method   |
| @AfterReturning | After success  |
| @AfterThrowing  | On exception   |
| @Around         | Before + After |

---

# 🔥 Most Important: @Around

```java
@Around("execution(* PaymentService.pay(..))")
public Object around(ProceedingJoinPoint pjp) throws Throwable {
    System.out.println("Before");
    Object result = pjp.proceed();
    System.out.println("After");
    return result;
}
```

✔ Full control over method execution

---

# 🔹 How AOP Works Internally

👉 Spring creates a **proxy object**

Flow:

```text
Client → Proxy → Aspect → Target Method
```

✔ You never call the real object directly

---

# ⚡ Important Interview Points

* AOP = separation of concerns
* Uses **proxy pattern** internally
* Applied using **BeanPostProcessor**
* Works only on **Spring-managed beans**

---

# ❗ Limitations

* Works only on **public methods**
* Self-invocation doesn’t trigger AOP
* Only works for beans in container

---

# 🎯 One-Line Answer

> “AOP in Spring is used to separate cross-cutting concerns like logging and security using aspects and advice, implemented internally using proxy objects.”

---

# 🚀 Real Use Cases

* Logging
* Security (authentication)
* Transaction management (`@Transactional`)
* Performance monitoring

---

---

Perfect—AOP is where interviews get **really tricky**. These are the questions that separate surface-level knowledge from real understanding.

---

# 🔥 Tricky AOP Interview Questions

---

## 🔹 1. Does AOP work on private methods?

👉 ❌ No

> Spring AOP works only on **public methods** (proxy-based)

---

## 🔹 2. Does AOP work on self-invocation?

```java
public void methodA() {
    methodB(); // internal call
}
```

👉 ❌ No

✔ Because:

* Call doesn’t go through proxy
* It directly calls the method

---

## 🔹 3. Where exactly is AOP applied in lifecycle?

👉

> In `postProcessAfterInitialization()` via BeanPostProcessor

---

## 🔹 4. What is the difference between @Before and @Around?

👉

* `@Before` → runs before method
* `@Around` → controls full execution (can skip method)

---

## 🔹 5. Can @Around skip method execution?

👉 ✔ Yes

```java
return null; // method not executed
```

---

## 🔹 6. What is the default proxy mechanism?

👉 Depends:

* Interface present → JDK Proxy
* No interface → CGLIB Proxy

---

## 🔹 7. What is the difference between JDK and CGLIB proxy?

👉

* JDK → Creates a proxy based on interfaces
* CGLIB → Creates a proxy by extending the actual class (subclass proxy)

> Client → Proxy(interfaces/subclass) → Target Method

| Feature            | JDK Proxy              | CGLIB Proxy        |
|--------------------|------------------------|--------------------|
| Based on           | Interface              | Class (subclass)   |
| Requires interface | ✔ Yes                  | ❌ No               |
| Method coverage    | Interface methods only | All public methods |
| Final methods      | ✔ Works                | ❌ Not allowed      |
| Performance        | Slightly faster        | Slightly slower    |
| Default choice     | If interface exists    | Otherwise          |


---

## 🔹 8. Can we force CGLIB?

👉 ✔ Yes

```java
@EnableAspectJAutoProxy(proxyTargetClass = true)
```

---

## 🔹 9. What is JoinPoint vs ProceedingJoinPoint?

👉

* `JoinPoint` → basic info
* `ProceedingJoinPoint` → can control execution (`proceed()`)

---

## 🔹 10. What happens if multiple advices are applied?

👉

* Order is not guaranteed by default
  ✔ Use `@Order`

---

## 🔹 11. Does AOP work on static methods?

👉 ❌ No

---

## 🔹 12. Does AOP work on final methods?

👉 ❌ No (especially with CGLIB)

---

## 🔹 13. Can we apply AOP without Spring?

👉 ✔ Yes (AspectJ)

---

## 🔹 14. What is Pointcut expression syntax?

👉

```java
execution(* package.Class.method(..))
```

✔ Used to match methods

---

## 🔹 15. What happens if pointcut doesn’t match?

👉

* Advice is never executed

---

# ⚡ Scenario-Based Questions

---

## 🔹 16. Why is your AOP not working?

👉 Possible reasons:

* Missing `@EnableAspectJAutoProxy`
* Method not public
* Self-invocation
* Bean not managed by Spring

---

## 🔹 17. You added @Aspect but it’s not working. Why?

👉

* Missing `@Component`
* Not scanned by Spring

---

## 🔹 18. Can AOP be applied to all methods automatically?

👉 ✔ Yes
Using wildcard pointcut:

```java
execution(* *(..))
```

---

## 🔹 19. How does Spring decide which advice to run first?

👉
✔ Use:

```java
@Order(1)
```

---

## 🔹 20. What is the biggest limitation of Spring AOP?

👉

> It is proxy-based and works only on method execution, not field access or constructors.

---

# 🎯 Killer Question (Very Common)

## ❓ “Why doesn’t AOP work inside same class?”

👉 Strong answer:

> “Because Spring uses proxies, and internal method calls do not go through the proxy, so advice is not applied.”

---

# 🚀 Pro Tip

Interviewers LOVE:

* Self-invocation issue
* Proxy types (JDK vs CGLIB)
* Why AOP fails
* @Around vs others

👉 If you explain **proxy behavior clearly**, you stand out.

---
