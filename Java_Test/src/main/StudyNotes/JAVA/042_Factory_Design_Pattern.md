# 42. Factory Design Pattern

## 42. Factory Design Pattern

## Frequently Asked

---

# 1. What is Factory Pattern?

<details>
<summary>Show Answer</summary>

**Answer:**

The **Factory Pattern** creates objects **without exposing creation logic** to the client—the client asks the factory for an object by type/name, and the factory decides which concrete class to instantiate.

### Simple Idea

```text
Restaurant:
  Customer orders "pizza" — doesn't go to kitchen
  Waiter (factory) brings the right dish
  Customer doesn't know how pizza was made
```

```java
// Client — no new Pizza() or new Burger() directly
NotificationService notification = NotificationFactory.create("email");
notification.send("Hello");
```

### Structure

```text
Client → Factory.create(type) → Concrete Object
         hides: new EmailNotification() vs new SmsNotification()
```

### Types

| Type | Purpose |
|------|---------|
| **Simple Factory** | One factory method, if/else on type |
| **Factory Method** | Subclasses decide which object to create |
| **Abstract Factory** | Families of related objects |

**Interview Point:**

> Factory = centralize object creation. Client uses interface, factory picks concrete class. Decouples creation from usage.

</details>

---

# 2. Why Factory Pattern used?

<details>
<summary>Show Answer</summary>

**Answer:**

| Problem Without Factory | Factory Solves |
|-------------------------|----------------|
| `new` scattered everywhere | One place to create objects |
| Client knows concrete classes | Client only knows interface |
| Hard to add new types | Add new class + one factory line |
| Complex creation logic | Hide setup in factory |
| Switch implementation easily | Change factory, not all clients |

```java
// ❌ Without factory — client tied to concrete classes
if (type.equals("email")) {
    new EmailNotification().send(msg);
} else if (type.equals("sms")) {
    new SmsNotification().send(msg);
}

// ✅ With factory — client uses interface
Notification n = NotificationFactory.create(type);
n.send(msg);
```

### Benefits

```text
✅ Loose coupling — client depends on interface
✅ Open/Closed — add new types without changing client
✅ Single place for creation logic
✅ Easier testing — mock factory
```

**Interview Point:**

> Factory decouples creation from use. Supports OCP — extend products without changing clients. Centralizes complex instantiation.

</details>

---

# 3. Real-world examples?

<details>
<summary>Show Answer</summary>

**Answer:**

| Example | Factory Role |
|---------|--------------|
| **Spring BeanFactory** | Creates and manages bean instances |
| **Calendar.getInstance()** | Returns calendar for locale/timezone |
| **DriverManager.getConnection()** | Returns DB connection for driver URL |
| **NumberFormat.getCurrencyInstance()** | Returns formatter for currency |
| **ExecutorService factories** | `Executors.newFixedThreadPool(10)` |
| **Payment gateways** | Factory picks Stripe vs PayPal handler |
| **Document parsers** | Factory returns PDF vs XML parser |
| **Logger factories** | SLF4J LoggerFactory.getLogger() |

```java
// Java standard library
Calendar cal = Calendar.getInstance();
Connection conn = DriverManager.getConnection(url);
ExecutorService pool = Executors.newFixedThreadPool(5);

// Application
PaymentProcessor processor = PaymentFactory.getProcessor("stripe");
processor.charge(order);
```

**Interview Point:**

> Calendar.getInstance(), DriverManager, Executors, Spring BeanFactory — all factory patterns in Java ecosystem.

</details>

---

## Advanced

---

# 4. Factory vs Constructor?

<details>
<summary>Show Answer</summary>

**Answer:**

| | Constructor `new` | Factory |
|---|-------------------|---------|
| Creation location | Client code | Factory class |
| Knows concrete type | Client must know | Client knows interface only |
| Naming | Always `new ClassName()` | Meaningful method names |
| Return type | Fixed class | Can return subtype/interface |
| Subclass choice | Client decides | Factory decides |
| Complex setup | Clutters client | Hidden in factory |

```java
// Constructor — client picks concrete class
EmailNotification email = new EmailNotification();

// Factory — meaningful name, can return interface
Notification n = NotificationFactory.createEmailNotification();
Notification n2 = NotificationFactory.createFromConfig(properties);
```

### When Factory Wins

```text
✅ Multiple implementations of same interface
✅ Creation logic is complex (read config, cache, pool)
✅ Want to hide concrete class names
✅ Need to return existing instance (pool) vs new each time
```

### When Constructor Is Fine

```text
✅ Simple object, one implementation
✅ No need to hide creation
✅ DTOs, entities — new Employee() is clear
```

**Interview Point:**

> Constructor = direct, simple. Factory = hide concrete type, complex creation, return interface. Factory for polymorphic object creation.

</details>

---

# 5. Factory vs Abstract Factory?

<details>
<summary>Show Answer</summary>

**Answer:**

| | Factory (Method) | Abstract Factory |
|---|------------------|------------------|
| Creates | **One type** of product | **Family** of related products |
| Methods | One product per factory method | Multiple create methods |
| Use case | One interface, many implementations | UI kit, DB family (connection + statement + result set) |

### Factory Method — One Product

```java
interface Notification { void send(String msg); }

class EmailNotification implements Notification { ... }
class SmsNotification implements Notification { ... }

class NotificationFactory {
    static Notification create(String type) {
        if ("email".equals(type)) return new EmailNotification();
        if ("sms".equals(type)) return new SmsNotification();
        throw new IllegalArgumentException(type);
    }
}
```

### Abstract Factory — Product Family

```java
// Windows UI family vs Mac UI family
interface UIFactory {
    Button createButton();
    Checkbox createCheckbox();
    Dialog createDialog();
}

class WindowsUIFactory implements UIFactory {
    public Button createButton() { return new WindowsButton(); }
    public Checkbox createCheckbox() { return new WindowsCheckbox(); }
    public Dialog createDialog() { return new WindowsDialog(); }
}

class MacUIFactory implements UIFactory {
    public Button createButton() { return new MacButton(); }
    public Checkbox createCheckbox() { return new MacCheckbox(); }
    public Dialog createDialog() { return new MacDialog(); }
}
```

```text
Factory Method:     "Give me a notification" (one product line)
Abstract Factory:   "Give me a full UI kit" (button + checkbox + dialog — consistent family)
```

**Interview Point:**

> Factory Method = one product type. Abstract Factory = family of related objects that must work together. Abstract Factory often implemented as interface with multiple create methods.

</details>

---

# 6. Spring BeanFactory relation?

<details>
<summary>Show Answer</summary>

**Answer:**

Spring's **`BeanFactory`** and **`ApplicationContext`** are **Factory patterns** at framework scale—they create, configure, and manage all application objects (beans) instead of you calling `new`.

### How Spring Is a Factory

```text
You define:     @Service class UserService
Spring factory:  ApplicationContext creates UserService instance
                 wires @Autowired dependencies
                 returns bean when you ask for it
```

```java
// Without Spring — manual wiring
UserRepository repo = new UserRepository(dataSource);
EmailClient email = new EmailClient(config);
UserService service = new UserService(repo, email);

// With Spring — factory creates and wires
@Autowired
private UserService userService; // context.getBean(UserService.class) under the hood
```

### BeanFactory vs ApplicationContext

| | BeanFactory | ApplicationContext |
|---|-------------|-------------------|
| Bean creation | Lazy (on demand) | Mostly eager at startup |
| Features | Basic factory | + events, i18n, AOP |
| Production | Rarely used directly | **Standard** — @SpringBootApplication |

### Factory Methods in Spring

```java
@Configuration
public class AppConfig {
    @Bean
    public DataSource dataSource() {
        return new HikariDataSource(); // you define factory method
    }
}
// Spring calls dataSource() and registers result as bean
```

**Interview Point:**

> Spring IoC container = giant factory. BeanFactory/ApplicationContext create beans. @Bean methods are factory methods. Replaces manual new + wiring.

</details>

---

# 5–8 Year Interview Rapid Fire

### Q: Factory vs Builder?

<details>
<summary>Show Answer</summary>

**Answer:**

**Factory** = create **which type** of object (Email vs SMS). **Builder** = assemble **complex object** step by step with many optional fields (Order with 20 fields). Different problems.

</details>

---

### Q: Static factory method vs factory class?

<details>
<summary>Show Answer</summary>

**Answer:**

**Static factory method** — `Notification.createEmail()` on the class itself (like `Integer.valueOf`). **Factory class** — separate `NotificationFactory` — better when creation logic is large or many product types.

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> Factory hides object creation behind interface. Client asks factory, not `new`. Abstract Factory = product families. Spring ApplicationContext = factory for all beans. Use when multiple implementations or complex creation.

</details>
