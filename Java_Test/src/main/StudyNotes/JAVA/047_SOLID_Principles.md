# 47. SOLID Principles

## 47. SOLID Principles

## Extremely Important

### S - Single Responsibility Principle

---

# 1. What is SRP?

<details>
<summary>Show Answer</summary>

**Answer:**

**Single Responsibility Principle (SRP)** — a class should have **only one reason to change** — one job, one responsibility.

### Simple Idea

```text
Bad:  Employee class — calculates salary + saves to DB + sends email
Good: Employee (data) + PayrollService + EmployeeRepository + EmailService
```

```java
// ❌ Violates SRP — two reasons to change: payroll rules + persistence
class Employee {
    void calculateSalary() { ... }
    void saveToDatabase() { ... }
}

// ✅ Each class one job
class Employee { /* data only */ }
class PayrollService { double calculate(Employee e) { ... } }
class EmployeeRepository { void save(Employee e) { ... } }
```

**Interview Point:**

> SRP = one class, one responsibility, one reason to change. Not "one method" — one cohesive job.

</details>

---

# 2. Violation examples?

<details>
<summary>Show Answer</summary>

**Answer:**

| Violation | Problem |
|-----------|---------|
| **God class** | UserService does CRUD + email + report + validation |
| **Fat controller** | Controller has business logic + DB calls |
| **Entity with logic** | JPA entity calculates tax and sends notification |
| **Utility dump** | Utils class with 50 unrelated static methods |

```java
// ❌ God service
@Service
class OrderService {
    void createOrder() { validate(); save(); charge(); email(); updateInventory(); }
    void generatePdfReport() { ... }
    void exportToExcel() { ... }
}

// ✅ Split responsibilities
OrderService, PaymentService, NotificationService, OrderReportService
```

**Interview Point:**

> God classes, fat controllers, entities with business logic — classic SRP violations. Split by reason to change.

</details>

---

### O - Open Closed Principle

---

# 3. What is OCP?

<details>
<summary>Show Answer</summary>

**Answer:**

**Open/Closed Principle** — software entities should be **open for extension** but **closed for modification**.

```text
Open for extension:   add new behavior via new classes
Closed for modification: don't edit existing stable code
```

```java
// ❌ Modify PaymentService for every new payment type
public void pay(String type, Order order) {
    if ("card".equals(type)) { ... }
    else if ("upi".equals(type)) { ... }  // edit again for PayPal
}

// ✅ Extend with new PaymentStrategy — PaymentService unchanged
public void pay(PaymentStrategy strategy, Order order) {
    strategy.pay(order);
}
```

**Interview Point:**

> OCP = extend via new classes/interfaces, don't modify working code. Strategy pattern is classic OCP.

</details>

---

# 4. How Strategy Pattern supports OCP?

<details>
<summary>Show Answer</summary>

**Answer:**

Strategy lets you add new algorithms by **adding new strategy classes** — client code stays unchanged.

```java
@Service
class CheckoutService {
    public PaymentResult checkout(PaymentStrategy strategy, Order order) {
        return strategy.pay(order); // never modified for new payment types
    }
}

// Extension — new file, no edit to CheckoutService
@Component
class PayPalPayment implements PaymentStrategy { ... }
```

```text
Closed:  CheckoutService.checkout() stable
Open:    new PayPalPayment, CryptoPayment classes
```

**Interview Point:**

> Strategy + interface = OCP in practice. New behavior = new class implementing interface.

</details>

---

### L - Liskov Substitution Principle

---

# 5. What is LSP?

<details>
<summary>Show Answer</summary>

**Answer:**

**Liskov Substitution Principle (LSP)** — subclasses must be **substitutable** for their parent without breaking the program.

```text
If code expects Animal, any Dog or Cat should work correctly
Subclass must honor parent's contract — not surprise caller
```

```java
// ❌ Violates LSP
class Bird { void fly() { ... } }
class Penguin extends Bird {
    void fly() { throw new UnsupportedOperationException(); } // breaks contract!
}

// Caller expects all Birds can fly — Penguin breaks it
```

### Rules

```text
✅ Subclass can extend, not restrict parent behavior
✅ Same method signatures, compatible return types
✅ Don't throw unexpected exceptions
✅ Don't change expected behavior
```

**Interview Point:**

> LSP = subclass must work wherever parent works. No surprising overrides that break expectations.

</details>

---

# 6. Real-world violation example?

<details>
<summary>Show Answer</summary>

**Answer:**

**Square extends Rectangle** — classic LSP violation.

```java
class Rectangle {
    void setWidth(int w) { width = w; }
    void setHeight(int h) { height = h; }
    int area() { return width * height; }
}

class Square extends Rectangle {
    void setWidth(int w) { width = w; height = w; } // breaks rectangle contract
    void setHeight(int h) { width = h; height = h; }
}

// Client code
Rectangle r = new Square();
r.setWidth(5);
r.setHeight(10);
// Expects area = 50, but Square forces 10x10 = 100 ❌
```

**Production example:**

```java
// ReadOnlyList that throws on add() — can't substitute List everywhere
List<String> list = new ReadOnlyList<>();
list.add("x"); // surprise exception — violates List contract
```

**Interview Point:**

> Square/Rectangle classic. Subclass that throws on parent methods = LSP violation. Prefer composition over bad inheritance.

</details>

---

### I - Interface Segregation Principle

---

# 7. What is ISP?

<details>
<summary>Show Answer</summary>

**Answer:**

**Interface Segregation Principle (ISP)** — clients should not be forced to depend on methods they **don't use**. Prefer **small, focused interfaces**.

```java
// ❌ Fat interface — Printer forced to implement scan()
interface Machine {
    void print();
    void scan();
    void fax();
}

class SimplePrinter implements Machine {
    void print() { ... }
    void scan() { throw new UnsupportedOperationException(); }
    void fax() { throw new UnsupportedOperationException(); }
}

// ✅ Segregated interfaces
interface Printer { void print(); }
interface Scanner { void scan(); }
interface Fax { void fax(); }

class SimplePrinter implements Printer { void print() { ... } }
class AllInOne implements Printer, Scanner, Fax { ... }
```

**Interview Point:**

> ISP = small interfaces. Don't force empty/stub implementations. Java functional interfaces follow ISP.

</details>

---

# 8. Fat interface problem?

<details>
<summary>Show Answer</summary>

**Answer:**

**Fat interface** = too many methods in one interface — implementations must provide methods they don't need.

| Problem | Effect |
|---------|--------|
| Empty stub methods | `throw UnsupportedOperationException` |
| Hard to mock | Mockito must stub unused methods |
| Tight coupling | Client depends on methods it never calls |
| Hard to implement | Every new method breaks all implementers |

```java
// Spring / modern Java — small interfaces
interface Runnable { void run(); }
interface Callable<V> { V call(); }
interface Comparator<T> { int compare(T a, T b); }

// Not one GiantWorkerInterface with 20 methods
```

**Interview Point:**

> Fat interface forces useless implementations. Split by role. @FunctionalInterface = one method = extreme ISP.

</details>

---

### D - Dependency Inversion Principle

---

# 9. What is DIP?

<details>
<summary>Show Answer</summary>

**Answer:**

**Dependency Inversion Principle (DIP)** — depend on **abstractions**, not concrete classes. High-level modules should not depend on low-level modules; both depend on abstractions.

```text
High level:  OrderService (business logic)
Low level:   OracleRepository (DB detail)

DIP: OrderService depends on UserRepository interface
     OracleRepository implements UserRepository
     Both depend on abstraction — not OrderService → Oracle directly
```

```java
// ❌ Depends on concrete class
class OrderService {
    private OracleOrderRepository repo = new OracleOrderRepository();
}

// ✅ Depends on abstraction
class OrderService {
    private final OrderRepository repo; // interface
    OrderService(OrderRepository repo) { this.repo = repo; }
}
```

**Interview Point:**

> DIP = depend on interfaces, not concrete classes. High and low level both use abstraction.

</details>

---

# 10. Dependency Injection relation?

<details>
<summary>Show Answer</summary>

**Answer:**

**Dependency Injection (DI)** is the **technique** that implements DIP — external framework/container **injects** dependencies instead of class creating them.

```text
DIP  = principle (depend on abstractions)
DI   = pattern (someone else provides the dependency)
IoC  = concept (control of creation inverted to container)
```

```java
@Service
class OrderService {
    private final OrderRepository repo;

    // Constructor injection — Spring provides implementation
    public OrderService(OrderRepository repo) {
        this.repo = repo;
    }
}
```

| Without DI | With DI |
|------------|---------|
| `new OracleRepo()` inside class | Interface injected by Spring |
| Hard to test | Mock interface in tests |
| Tight coupling | Loose coupling |

**Interview Point:**

> DIP = design principle. DI = how you achieve it. Spring @Autowired = DI container implements DIP.

</details>

---

## Advanced

---

# 11. Explain SOLID using project examples.

<details>
<summary>Show Answer</summary>

**Answer:**

| Principle | Project Example |
|-----------|-----------------|
| **S** | Split `ReportService` into `PdfReportGenerator`, `ExcelReportGenerator`, `ReportEmailSender` |
| **O** | Add `KafkaNotification` without editing `NotificationService` — new strategy class |
| **L** | Don't extend `HashMap` and break `put()` contract; use composition |
| **I** | `Readable`, `Writable` interfaces instead of one `FileOperations` with 15 methods |
| **D** | `PaymentService` depends on `PaymentGateway` interface; Stripe/PayPal are implementations |

```java
// Real microservice stack
@RestController  // S — only HTTP layer
class OrderController {
    private final OrderService orderService; // D — interface injection
}

@Service
class OrderService {
    public void place(Order o) {
        paymentGateway.charge(o);  // D — PaymentGateway interface
    }
}

@Component
class StripeGateway implements PaymentGateway { ... } // O — extend without edit OrderService
```

**Interview Point:**

> Give concrete splits from your project: service layers, payment strategies, repository interfaces, small REST controllers.

</details>

---

# 12. Which SOLID principle is most violated?

<details>
<summary>Show Answer</summary>

**Answer:**

**SRP** and **DIP** are violated most often in real projects.

| Most Violated | Why |
|---------------|-----|
| **SRP** | God services, fat controllers, utils dumping ground |
| **DIP** | Direct `new`, concrete class dependencies everywhere |
| **OCP** | Giant if/else instead of polymorphism |
| **ISP** | Less violated if using small Spring interfaces |
| **LSP** | Less common but subtle inheritance bugs |

```text
Typical legacy service:
  500-line OrderService — SRP violation
  new JdbcTemplate inside — DIP violation
  if paymentType equals — OCP violation
```

**Interview Point:**

> SRP (god classes) and DIP (concrete deps) most common. Fix with split services + interface injection.

</details>

---

# 5–8 Year Interview Rapid Fire

### Q: SOLID mnemonic?

<details>
<summary>Show Answer</summary>

**Answer:**

**S**ingle Responsibility — one job. **O**pen/Closed — extend don't modify. **L**iskov Substitution — subclass substitutable. **I**nterface Segregation — small interfaces. **D**ependency Inversion — depend on abstractions.

</details>

---

### Q: DIP vs DI vs IoC?

<details>
<summary>Show Answer</summary>

**Answer:**

**DIP** = principle. **DI** = inject dependencies (constructor/setter). **IoC** = inversion of control — container creates objects. Spring = IoC container doing DI to achieve DIP.

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> SRP one job. OCP extend via Strategy. LSP subclass substitutable. ISP small interfaces. DIP abstractions + DI injection. SRP and DIP violated most in legacy code.

</details>
