# ⚡ Spring Autowiring – Complete Study Notes

---

## 🔹 1. What is Autowiring?

👉 **Autowiring** means:

> Spring automatically resolves and injects dependencies between beans.

✔ You don’t manually wire objects

✔ Spring decides **which bean to inject**

---

## 🔹 2. Why Autowiring?

Without autowiring:

```xml
<bean id="car" class="Car">
    <property name="engine" ref="engine"/>
</bean>
```

👉 With autowiring:

* No need to explicitly connect beans
* Cleaner code
* Less configuration

---

## 🔹 3. How Spring Resolves Dependencies?

Spring tries in this order:

1. **By Type** (most common)
2. **By Name**
3. **Qualifier / Primary (if conflict)**

---

# 🔥 4. Types of Autowiring (XML-based)

---

## 🔸 1. byName

👉 Matches property name with bean id

### 📌 Example

```java
class Car {
    private Engine engine;

    public void setEngine(Engine engine) {
        this.engine = engine;
    }
}
```

```xml
<bean id="engine" class="Engine"/>

<bean id="car" class="Car" autowire="byName"/>
```

✔ `engine` → matches `engine` bean id

---

## 🔸 2. byType

👉 Matches by class type

```xml
<bean id="engine" class="Engine"/>

<bean id="car" class="Car" autowire="byType"/>
```

✔ Injects based on type

⚠️ Problem:

* Fails if multiple beans of same type

---

## 🔸 3. constructor

👉 Matches constructor arguments

```xml
<bean id="car" class="Car" autowire="constructor"/>
```

✔ Uses constructor injection

---

## 🔸 4. no (default)

👉 No autowiring

```xml
<bean id="car" class="Car"/>
```

✔ Manual wiring required

---

# 🚀 5. Annotation-Based Autowiring (Most Important)

---

## 🔸 @Autowired (Core)

👉 Injects dependency automatically

### 📌 Example (Constructor Injection – Recommended)

```java
@Component
class Engine {
}

@Component
class Car {

    private final Engine engine;

    @Autowired  // optional if single constructor
    public Car(Engine engine) {
        this.engine = engine;
    }
}
```

---

## 🔸 Setter Injection

```java
@Autowired
public void setEngine(Engine engine) {
    this.engine = engine;
}
```

---

## 🔸 Field Injection (Not Recommended)

```java
@Autowired
private Engine engine;
```

❌ Hard to test

❌ Hidden dependency

---

# 🔥 6. Handling Multiple Beans (Very Important)

---

## ❗ Problem

```java
@Component
class PetrolEngine {}

@Component
class DieselEngine {}
```

```java
@Autowired
Engine engine; // ❌ Ambiguous
```

---

## ✔ Solution 1: @Qualifier

```java
@Autowired
@Qualifier("petrolEngine")
private Engine engine;
```

---

## ✔ Solution 2: @Primary

```java
@Component
@Primary
class PetrolEngine {}
```

✔ Default bean selected

---

# 🔹 7. Optional Dependencies

```java
@Autowired(required = false)
private Engine engine;
```

✔ Won’t fail if bean is missing

---

# 🔹 8. Lazy Autowiring

```java
@Autowired
@Lazy
private Engine engine;
```

✔ Injected only when used

---

# 🔹 9. Autowiring with Java Config

```java
@Configuration
class AppConfig {

    @Bean
    public Engine engine() {
        return new Engine();
    }

    @Bean
    public Car car(Engine engine) { // auto-injected
        return new Car(engine);
    }
}
```

✔ Spring automatically injects parameters

---

# 🔥 10. Common Errors

---

## ❌ NoSuchBeanDefinitionException

👉 No matching bean found

---

## ❌ NoUniqueBeanDefinitionException

👉 Multiple beans found

✔ Fix using:

* `@Qualifier`
* `@Primary`

---

# ⚡ 11. Best Practices

* ✔ Prefer **constructor injection**
* ✔ Avoid field injection
* ✔ Use `@Qualifier` for multiple beans
* ✔ Keep dependencies explicit

---

# 🎯 12. One-Line Answer

> “Autowiring is a feature in Spring that automatically injects dependencies between beans based on type, name, or qualifiers, reducing manual configuration.”

---

---

Got it—here are **clean, interview-ready answers** on **Spring Autowiring**. No fluff, just what you should say.

---

# 🔥 Core Interview Q&A – Autowiring

---

## 🔹 1. What is Autowiring in Spring?

👉

> “Autowiring is a feature in Spring that automatically injects dependencies between beans based on type, name, or qualifiers, reducing manual configuration.”

---

## 🔹 2. How does Spring resolve dependencies?

👉

> “Spring primarily resolves dependencies by type. If multiple beans of the same type exist, it uses qualifiers or primary designation to resolve ambiguity.”

---

## 🔹 3. What are the types of Autowiring?

👉
**XML-based:**

* byName
* byType
* constructor
* no (default)

**Annotation-based:**

* `@Autowired` (most used)

---

## 🔹 4. Difference between byName and byType?

👉

* **byName** → matches property name with bean id
* **byType** → matches by class type

---

## 🔹 5. What happens if multiple beans of same type exist?

👉

> “Spring throws NoUniqueBeanDefinitionException.”

✔ Solution:

* `@Qualifier`
* `@Primary`

---

## 🔹 6. What is @Qualifier?

👉

> “@Qualifier is used to specify which exact bean should be injected when multiple beans of the same type exist.”

---

## 🔹 7. What is @Primary?

👉

> “@Primary marks a bean as the default choice when multiple candidates are available.”

---

## 🔹 8. Constructor vs Setter Autowiring?

👉

* Constructor → mandatory dependency, immutable
* Setter → optional dependency, flexible

✔ Preferred: **Constructor Injection**

---

## 🔹 9. Why is field injection not recommended?

👉

* Hard to test
* Breaks encapsulation
* Hidden dependencies

---

## 🔹 10. Can @Autowired be used on constructor?

👉

> “Yes, and it is optional if there is only one constructor.”

---

## 🔹 11. What happens if no bean is found?

👉

> “Spring throws NoSuchBeanDefinitionException.”

---

## 🔹 12. Can we make dependency optional?

👉

```java
@Autowired(required = false)
```

✔ Prevents failure if bean is missing

---

## 🔹 13. What is lazy autowiring?

👉

> “Using @Lazy delays the injection until the bean is actually used.”

---

## 🔹 14. Can Spring autowire without annotations?

👉

> “Yes, using XML autowire modes like byName, byType, and constructor.”

---

## 🔹 15. How does autowiring work in @Bean methods?

👉

> “Spring automatically injects dependencies into @Bean method parameters based on type.”

---

# ⚡ Rapid Fire

* Default autowiring? → byType
* Multiple beans issue? → Use @Qualifier
* Best injection type? → Constructor
* Missing bean? → Exception
* Optional dependency? → `required=false`

---

# 🎯 Killer Question

## ❓ “How does Spring choose which bean to inject?”

👉

> “Spring first matches by type. If multiple beans are found, it resolves using @Qualifier or @Primary.”

---

