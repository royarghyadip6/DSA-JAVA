# 39. Annotations

## 39. Annotations

## Basics

---

# 1. What are annotations?

<details>
<summary>Show Answer</summary>

**Answer:**

**Annotations** are **metadata tags** added to code—classes, methods, fields, parameters—that provide information to the compiler, tools, or runtime frameworks **without changing program logic**.

### Simple Idea

```text
Like labels on boxes:
  @Fragile  → handle with care
  @Entity   → Hibernate knows this is a DB table
  @Override → compiler checks you're really overriding
```

```java
@Entity
@Table(name = "employees")
public class Employee {

    @Id
  @GeneratedValue
    private Long id;

    @Column(name = "emp_name")
    private String name;
}
```

**Interview Point:**

> Annotations = metadata on code. Processed by compiler, tools, or runtime (Spring, Hibernate).

</details>

---

# 2. Why annotations introduced?

<details>
<summary>Show Answer</summary>

**Answer:**

Annotations replaced **XML configuration** and **marker interfaces** with **cleaner, type-safe metadata** directly on code.

### Before Annotations

```xml
<!-- Spring XML config -->
<bean id="userService" class="com.app.UserService">
    <property name="repo" ref="userRepository"/>
</bean>
```

### With Annotations

```java
@Service
public class UserService {
    @Autowired
    private UserRepository repo;
}
```

### Benefits

| Benefit | Detail |
|---------|--------|
| **Readable** | Metadata next to code |
| **Type-safe** | Compile-time checks (@Override) |
| **Less XML** | Spring Boot — almost no XML |
| **Framework integration** | Spring, JPA, JAX-RS |

**Interview Point:**

> Annotations reduce XML config, add compile-time checks, enable framework magic (@Autowired, @Entity).

</details>

---

# 3. Built-in annotations?

<details>
<summary>Show Answer</summary>

**Answer:**

Java provides built-in annotations in `java.lang` and `java.lang.annotation`:

| Annotation | Package | Purpose |
|------------|---------|---------|
| `@Override` | java.lang | Compiler checks method override |
| `@Deprecated` | java.lang | Marks obsolete API |
| `@SuppressWarnings` | java.lang | Suppress compiler warnings |
| `@FunctionalInterface` | java.lang | Single abstract method interface |
| `@SafeVarargs` | java.lang | Suppress varargs warnings |
| `@Retention` | java.lang.annotation | How long annotation kept |
| `@Target` | java.lang.annotation | Where annotation can be used |
| `@Documented` | java.lang.annotation | Include in JavaDoc |
| `@Inherited` | java.lang.annotation | Subclasses inherit annotation |

**Interview Point:**

> Core: @Override, @Deprecated, @SuppressWarnings, @FunctionalInterface. Meta: @Target, @Retention.

</details>

---

## Common Annotations

---

# 4. @Override

<details>
<summary>Show Answer</summary>

**Answer:**

`@Override` tells compiler this method **overrides a parent method**—compiler errors if signature doesn't match.

```java
class Animal {
    void speak() { System.out.println("..."); }
}

class Dog extends Animal {
    @Override
    void speak() { System.out.println("Bark"); } // ✅

    @Override
    void sppek() { } // ❌ compile error — typo, no parent method
}
```

Without `@Override`, typo creates new method silently — bug at runtime.

**Interview Point:**

> @Override = compile-time override check. Catches typos and signature mistakes.

</details>

---

# 5. @Deprecated

<details>
<summary>Show Answer</summary>

**Answer:**

`@Deprecated` marks API as **obsolete**—compiler warns when used. Often with `@deprecated` JavaDoc explaining replacement.

```java
@Deprecated(since = "2.0", forRemoval = true)
public void oldMethod() {
    // use newMethod() instead
}

public void newMethod() { }
```

```java
service.oldMethod(); // compiler warning: deprecated
```

**Interview Point:**

> @Deprecated warns consumers. forRemoval = will be removed in future version.

</details>

---

# 6. @SuppressWarnings

<details>
<summary>Show Answer</summary>

**Answer:**

`@SuppressWarnings` tells compiler to **ignore specific warnings** for that element.

```java
@SuppressWarnings("unchecked")
List<String> list = (List<String>) rawList;

@SuppressWarnings({"deprecation", "unused"})
public void legacy() { }
```

Common values: `"unchecked"`, `"deprecation"`, `"rawtypes"`, `"all"`.

**Interview Point:**

> SuppressWarnings silences compiler warnings. Use narrowly — don't blanket @SuppressWarnings("all") everywhere.

</details>

---

# 7. @FunctionalInterface

<details>
<summary>Show Answer</summary>

**Answer:**

`@FunctionalInterface` marks interface with **exactly one abstract method**—enables lambda usage. Compiler enforces single abstract method rule.

```java
@FunctionalInterface
interface Calculator {
    int calculate(int a, int b);

    default int add(int a, int b) { return a + b; } // default OK
    // void second(); // ❌ second abstract — compile error
}

Calculator mult = (a, b) -> a * b;
```

**Interview Point:**

> @FunctionalInterface = one abstract method, lambda-ready. Compiler enforces rule.

</details>

---

## Meta Annotations

---

# 8. @Target

<details>
<summary>Show Answer</summary>

**Answer:**

`@Target` defines **where** a custom annotation can be applied.

```java
@Target(ElementType.METHOD)
public @interface GetMapping {
    String value();
}

@Target({ElementType.FIELD, ElementType.METHOD})
public @interface Autowired { }
```

| ElementType | Applies To |
|-------------|------------|
| `TYPE` | Class, interface, enum |
| `METHOD` | Method |
| `FIELD` | Field |
| `PARAMETER` | Method parameter |
| `CONSTRUCTOR` | Constructor |
| `LOCAL_VARIABLE` | Local variable |

**Interview Point:**

> @Target limits where annotation can be used. @GetMapping only on methods.

</details>

---

# 9. @Retention

<details>
<summary>Show Answer</summary>

**Answer:**

`@Retention` defines **how long** annotation information is kept—SOURCE, CLASS, or RUNTIME.

```java
@Retention(RetentionPolicy.RUNTIME)
public @interface Entity {
    String tableName() default "";
}
```

```text
SOURCE  → discarded by compiler (e.g. @Override)
CLASS   → in .class file, not available at runtime
RUNTIME → available at runtime via reflection — Spring/JPA need this
```

**Interview Point:**

> @Retention controls lifecycle. Framework annotations need RUNTIME.

</details>

---

# 10. @Inherited

<details>
<summary>Show Answer</summary>

**Answer:**

`@Inherited` means annotation on **parent class** is automatically inherited by **subclasses**.

```java
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@interface Secured { }

@Secured
class BaseController { }

class UserController extends BaseController {
    // inherits @Secured automatically
}
```

Only applies to **class-level** annotations. Not inherited for methods/fields.

**Interview Point:**

> @Inherited = subclass gets parent's class-level annotation. Method annotations not inherited.

</details>

---

# 11. @Documented

<details>
<summary>Show Answer</summary>

**Answer:**

`@Documented` includes the annotation in **generated JavaDoc** for annotated elements.

```java
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiOperation {
    String value();
}
```

Without `@Documented`, annotation appears in code but not in JavaDoc output.

**Interview Point:**

> @Documented = show annotation in JavaDoc. Useful for API documentation annotations.

</details>

---

# 12. @Repeatable

<details>
<summary>Show Answer</summary>

**Answer:**

`@Repeatable` allows the **same annotation multiple times** on one element—Java 8+.

```java
@Repeatable(Schedules.class)
@Retention(RetentionPolicy.RUNTIME)
@interface Schedule {
    String day();
}

@interface Schedules {
    Schedule[] value();
}

@Schedule(day = "Monday")
@Schedule(day = "Friday")
public void runJob() { }
```

**Interview Point:**

> @Repeatable allows multiple same annotations. Needs container annotation (Schedules).

</details>

---

## Advanced

---

# 13. RetentionPolicy values?

<details>
<summary>Show Answer</summary>

**Answer:**

Three `RetentionPolicy` values:

| Policy | Kept Until | Example |
|--------|------------|---------|
| `SOURCE` | Compile time only | `@Override`, Lombok annotations |
| `CLASS` | In .class file, not runtime | Default if not specified |
| `RUNTIME` | Full runtime via reflection | `@Entity`, `@Autowired` |

```java
@Retention(RetentionPolicy.RUNTIME) // required for Spring to read at runtime
public @interface Component { }
```

**Interview Point:**

> SOURCE=compiler only. CLASS=bytecode only. RUNTIME=reflection at runtime. Frameworks need RUNTIME.

</details>

---

# 14. SOURCE vs CLASS vs RUNTIME?

<details>
<summary>Show Answer</summary>

**Answer:**

```text
SOURCE   → .java file only → gone after compile
           Compiler uses it (@Override check)
           Lombok generates code then discards annotation

CLASS    → stored in .class file → JVM ignores at runtime
           Default retention
           Bytecode tools can read

RUNTIME  → in .class + available via reflection at runtime
           Spring scans @Component at startup
           JPA reads @Entity when building session factory
```

```java
// Spring needs RUNTIME to find beans
@Retention(RetentionPolicy.RUNTIME)
@Component
public class UserService { }
```

**Interview Point:**

> SOURCE=compile only. CLASS=bytecode, no runtime reflection. RUNTIME=frameworks read via reflection.

</details>

---

# 15. How custom annotations are created?

<details>
<summary>Show Answer</summary>

**Answer:**

Define with `@interface`, add meta-annotations, optional elements (attributes).

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditLog {
    String action();           // required element
    String module() default "general";  // optional with default
    boolean enabled() default true;
}

// Usage
@AuditLog(action = "CREATE_ORDER", module = "orders")
public void createOrder(Order order) { }
```

### Process at Runtime

```java
Method m = clazz.getMethod("createOrder", Order.class);
if (m.isAnnotationPresent(AuditLog.class)) {
    AuditLog log = m.getAnnotation(AuditLog.class);
    System.out.println(log.action());
}
```

**Interview Point:**

> Custom annotation = @interface + @Target + @Retention. Elements are methods. Read via reflection if RUNTIME.

</details>

---

## Framework Questions

---

# 16. How Spring processes annotations?

<details>
<summary>Show Answer</summary>

**Answer:**

Spring scans classpath at **startup**, finds classes with stereotype annotations, reads metadata via reflection.

```text
1. @SpringBootApplication triggers component scan
2. Scan base package for @Component, @Service, @Controller, @Repository
3. Read class annotations + @Autowired fields via reflection
4. Build BeanDefinition — store in ApplicationContext
5. Create beans, wire dependencies, call @PostConstruct
6. Cache everything — no re-scan per request
```

```java
@ComponentScan(basePackages = "com.myapp")
// finds all @Service, @Controller in com.myapp.*
```

**Interview Point:**

> Spring scans at startup, reflects annotations, builds bean registry. Cached — not per-request.

</details>

---

# 17. How @Autowired works internally?

<details>
<summary>Show Answer</summary>

**Answer:**

Spring's `AutowiredAnnotationBeanPostProcessor` finds `@Autowired` fields/constructors/setters and injects matching beans from container.

```text
1. Bean instance created (constructor)
2. Post-processor scans fields for @Autowired
3. Resolve type from field type (UserRepository)
4. Find matching bean in ApplicationContext
5. Reflection: field.setAccessible(true); field.set(bean, dependency)
6. Bean fully wired
```

```java
@Service
public class OrderService {
    @Autowired  // Spring reflects this field after construction
    private OrderRepository repo;
}
```

**Prefer constructor injection** — no reflection on fields, clearer dependencies.

**Interview Point:**

> @Autowired = post-processor reflects fields/constructors, resolves bean by type, injects via reflection. Constructor injection preferred.

</details>

---

# 18. How @ComponentScan works?

<details>
<summary>Show Answer</summary>

**Answer:**

`@ComponentScan` tells Spring which packages to scan for stereotype annotations and register as beans.

```java
@SpringBootApplication
// includes @ComponentScan on same package and sub-packages
public class Application { }

// Or explicit:
@Configuration
@ComponentScan(basePackages = {"com.app.service", "com.app.repo"})
public class AppConfig { }
```

### What It Finds

```text
@Component, @Service, @Controller, @Repository
@Configuration classes
Custom annotations meta-annotated with @Component
```

### Classpath Scanning

```text
Scan classpath → read .class files → check annotations (ASM/reflection)
→ register BeanDefinition for each candidate
→ no need to load every class fully at first pass (Spring uses ASM)
```

**Interview Point:**

> ComponentScan finds stereotype-annotated classes in packages. @SpringBootApplication includes scan of app's package. ASM for fast classpath scanning.

</details>

---

# 5–8 Year Interview Rapid Fire

### Q: Annotation vs comment?

<details>
<summary>Show Answer</summary>

**Answer:**

Comment is for humans only — ignored by compiler. Annotation is structured metadata — compiler, tools, and runtime can process it.

</details>

---

### Q: Can annotation have methods?

<details>
<summary>Show Answer</summary>

**Answer:**

Annotation **elements** look like methods but are attributes with defaults. `String value()` in annotation is an element, not a real method.

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> Annotations = metadata tags. @Target where, @Retention how long. RUNTIME for Spring/JPA. Custom = @interface + meta-annotations. Spring scans @Component at startup, @Autowired injects via reflection.

</details>
