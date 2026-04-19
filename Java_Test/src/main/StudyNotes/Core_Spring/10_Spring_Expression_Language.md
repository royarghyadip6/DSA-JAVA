# ⚡ Spring Expression Language (SpEL)

---

## 🔹 1. What is SpEL?

👉

> SpEL (Spring Expression Language) is a powerful expression language used to **dynamically query and manipulate object values at runtime in Spring**.

---

## 🧠 Simple Meaning

Instead of hardcoding values:

```java
price = 100;
```

👉 You can use expressions:

```java
price = #{50 + 50}
```

✔ Evaluated at runtime

---

# 🔹 2. Injecting Values using SpEL

---

## 📌 Example 1: Basic Injection

```java
@Component
class Product {

    @Value("#{100 + 200}")
    private int price;
}
```

👉 Output:

```text
price = 300
```

---

## 📌 Example 2: Inject value from another bean

```java
@Component
class Engine {
    public String type = "Petrol";
}
```

```java
@Component
class Car {

    @Value("#{engine.type}")
    private String engineType;
}
```

✔ Injects value from another bean

---

## 📌 Example 3: System properties

```java
@Value("#{systemProperties['user.name']}")
private String username;
```

---

## 📌 Example 4: Environment variables

```java
@Value("#{systemEnvironment['JAVA_HOME']}")
private String javaHome;
```

---

# 🔹 3. Using Expressions in Configuration

---

## 📌 Java Config Example

```java
@Bean
public Product product() {
    Product p = new Product();
    p.setPrice(#{10 * 20}); // conceptual (actual use via @Value)
    return p;
}
```

---

## 📌 XML Example

```xml
<bean id="product" class="Product">
    <property name="price" value="#{100 * 2}"/>
</bean>
```

---

# 🔹 Common SpEL Expressions

---

## ✔ Arithmetic

```java
@Value("#{10 + 5}")
```

---

## ✔ Boolean

```java
@Value("#{10 > 5}")
```

---

## ✔ Method Call

```java
@Value("#{T(java.lang.Math).random()}")
```

---

## ✔ String Operations

```java
@Value("#{'Hello'.toUpperCase()}")
```

---

## ✔ Collection Access

```java
@Value("#{myList[0]}")
```

---

# 🔥 Real Example (Complete)

```java
@Component
class Demo {

    @Value("#{20 + 30}")
    private int sum;

    @Value("#{T(Math).sqrt(16)}")
    private double squareRoot;

    @Value("#{systemProperties['os.name']}")
    private String os;
}
```

---

# ⚡ Important Interview Points

* SpEL uses `#{}`
* Evaluated at runtime
* Can access:

    * Beans
    * Properties
    * Methods
    * Collections
* Used mainly with `@Value`

---

# ❗ SpEL vs ${}

👉 Very common question:

* `${}` → property file values
* `#{}` → SpEL expressions

---

# 🎯 One-Line Answer

> “SpEL is a Spring expression language used to dynamically evaluate expressions and inject values at runtime using the #{ } syntax.”

---

# 🚀 When to Use?

* Dynamic configuration
* Conditional values
* Accessing bean properties

---