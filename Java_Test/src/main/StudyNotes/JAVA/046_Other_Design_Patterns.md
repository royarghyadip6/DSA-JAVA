# 46. Other Important Design Patterns

## 46. Other Important Design Patterns

Catalog of remaining patterns + key interview comparisons.

---

## Creational

---

# 1. Singleton

<details>
<summary>Show Answer</summary>

**Answer:**

**One instance** globally — private constructor + `getInstance()`. Enum singleton best practice.

```java
public enum Config { INSTANCE; }
```

**One-liner:** One instance only. Enum or Bill Pugh holder. Spring `@Service` replaces manual singleton.

</details>

---

# 2. Factory

<details>
<summary>Show Answer</summary>

**Answer:**

**Centralized object creation** — client asks factory for product by type, doesn't call `new` on concrete class.

```java
Notification n = NotificationFactory.create("email");
```

**One-liner:** Hide `new`. Factory picks concrete type. Spring ApplicationContext = mega factory.

</details>

---

# 3. Abstract Factory

<details>
<summary>Show Answer</summary>

**Answer:**

Creates **families of related objects** — UI kit (Windows button + checkbox + dialog) vs Mac kit.

```java
interface UIFactory {
    Button createButton();
    Checkbox createCheckbox();
}
```

**One-liner:** Factory Method = one product. Abstract Factory = whole compatible product family.

</details>

---

# 4. Builder

<details>
<summary>Show Answer</summary>

**Answer:**

**Step-by-step** construction of complex objects — fluent API, optional fields, build-time validation.

```java
Order.builder().customerId("C1").quantity(2).build();
```

**One-liner:** Fixes telescoping constructors. Lombok @Builder. OkHttp Request.Builder.

</details>

---

# 5. Prototype

<details>
<summary>Show Answer</summary>

**Answer:**

**Clone existing object** instead of creating from scratch — `clone()` or copy constructor.

```java
Employee copy = original.clone();
// or prototype registry
Employee e = prototypeRegistry.get("manager").copy();
```

**Use when:** Object creation is expensive; need many similar objects.

**One-liner:** Clone prototype instead of new. `clone()` or manual copy. Spring prototype bean scope.

</details>

---

## Structural

---

# 6. Adapter

<details>
<summary>Show Answer</summary>

**Answer:**

**Wraps incompatible interface** so client can use it — bridge between old and new systems.

```java
// Legacy XML parser → expected PaymentReader interface
class XmlPaymentAdapter implements PaymentReader {
    private LegacyXmlParser parser;
    public Payment read(String data) {
        XmlRecord xml = parser.parse(data);
        return mapToPayment(xml); // adapt format
    }
}
```

**One-liner:** Adapter = convert incompatible interface to what client expects. Legacy integration classic use.

</details>

---

# 7. Decorator

<details>
<summary>Show Answer</summary>

**Answer:**

**Wraps object** to add behavior dynamically — same interface, stacked layers.

```java
InputStream in = new BufferedInputStream(
    new GZipInputStream(
        new FileInputStream("file.gz")));
// each decorator adds behavior
```

**Java examples:** `BufferedInputStream`, `Collections.synchronizedList()`, Spring `@Cacheable` wrappers.

**One-liner:** Decorator = wrap to add behavior. Same interface. Stackable. Java IO streams classic example.

</details>

---

# 8. Facade

<details>
<summary>Show Answer</summary>

**Answer:**

**Simple interface** hiding complex subsystem — one entry point for many classes.

```java
class OrderFacade {
    public void placeOrder(OrderRequest req) {
        validate(req);
        inventory.reserve(req);
        payment.charge(req);
        shipping.schedule(req);
        notification.send(req);
        // client calls one method — facade orchestrates
    }
}
```

**One-liner:** Facade = simplify complex subsystem. One method does everything behind the scenes.

</details>

---

# 9. Proxy

<details>
<summary>Show Answer</summary>

**Answer:**

**Placeholder** controlling access to real object — lazy loading, security, logging, caching.

```java
// Spring AOP proxy — intercepts method calls
@Transactional  // proxy adds transaction around real method
public void saveOrder(Order order) { repo.save(order); }

// Hibernate lazy proxy — loads entity only when accessed
```

| Proxy Type | Use |
|------------|-----|
| Virtual | Lazy load expensive object |
| Protection | Access control |
| Remote | RMI / network object |
| **Spring AOP** | Transactions, security, logging |

**One-liner:** Proxy = stand-in with same interface. Spring AOP, Hibernate lazy loading, caching proxies.

</details>

---

# 10. Bridge

<details>
<summary>Show Answer</summary>

**Answer:**

**Split abstraction from implementation** — two hierarchies connected by composition (not inheritance explosion).

```text
Abstraction:     RemoteControl ──uses──> Device (interface)
Implementation:  TV, Radio implement Device

RemoteControl doesn't extend TV/Radio — bridges to Device
```

**vs Adapter:** Bridge designed upfront to separate layers. Adapter fixes existing incompatible code.

**One-liner:** Bridge = abstraction + implementation as separate hierarchies. Avoids inheritance explosion.

</details>

---

## Behavioral

---

# 11. Strategy

<details>
<summary>Show Answer</summary>

**Answer:**

**Interchangeable algorithms** behind one interface — payment methods, sort comparators.

```java
strategy.pay(amount); // CreditCard, UPI, Wallet strategies
```

**One-liner:** Strategy = swap algorithm at runtime. OCP. Comparator is strategy.

</details>

---

# 12. Observer

<details>
<summary>Show Answer</summary>

**Answer:**

**One-to-many notify** on state change — Spring `@EventListener`, GUI listeners, Kafka pub/sub.

```java
publisher.publishEvent(new OrderPlacedEvent(order));
```

**One-liner:** Subject notifies observers. Spring events in-app. Kafka distributed pub/sub.

</details>

---

# 13. Command

<details>
<summary>Show Answer</summary>

**Answer:**

**Encapsulate request as object** — undo, queue, log operations.

```java
interface Command { void execute(); void undo(); }

class PlaceOrderCommand implements Command {
    public void execute() { orderService.place(order); }
    public void undo() { orderService.cancel(order); }
}

// Job queue, transaction rollback, macro recording
```

**Examples:** Runnable, ExecutorService tasks, CQRS write commands.

**One-liner:** Command = action as object. Supports undo, queue, logging. Runnable is command pattern.

</details>

---

# 14. State

<details>
<summary>Show Answer</summary>

**Answer:**

Object **changes behavior** when internal **state changes** — state classes replace big if/else.

```java
interface OrderState {
    void next(Order order);
}

class NewState implements OrderState {
    public void next(Order order) { order.setState(new PaidState()); }
}

class PaidState implements OrderState {
    public void next(Order order) { order.setState(new ShippedState()); }
}
```

**One-liner:** State = behavior changes with internal state. Order lifecycle NEW→PAID→SHIPPED.

</details>

---

# 15. Template Method

<details>
<summary>Show Answer</summary>

**Answer:**

**Base class defines skeleton** — subclasses override specific steps without changing structure.

```java
abstract class DataImporter {
    public final void importData() {  // template method
        openConnection();
        readData();
        transform();
        save();
        closeConnection();
    }
    abstract void readData();
    abstract void transform();
}
```

**Java:** `AbstractList`, `HttpServlet` (service method), JdbcTemplate.

**One-liner:** Template Method = fixed steps in base class, subclasses fill in hooks. Inheritance-based.

</details>

---

# 16. Chain of Responsibility

<details>
<summary>Show Answer</summary>

**Answer:**

**Chain of handlers** — request passes through until one handles it.

```java
abstract class AuthHandler {
    AuthHandler next;
    public void setNext(AuthHandler next) { this.next = next; }
    public boolean handle(Request req) {
        if (canHandle(req)) return process(req);
        if (next != null) return next.handle(req);
        return false;
    }
}
// BasicAuth → JwtAuth → RoleAuth chain
```

**Examples:** Servlet filters, Spring Security filter chain, logging levels.

**One-liner:** Chain = pass request along handlers until one processes. Servlet filter chain, Spring Security.

</details>

---

## Interview Questions

---

# 17. Adapter vs Facade?

<details>
<summary>Show Answer</summary>

**Answer:**

| | Adapter | Facade |
|---|---------|--------|
| Purpose | **Convert** incompatible interface | **Simplify** complex subsystem |
| Interface | Changes/wraps to match client | New simplified API |
| Classes involved | Usually one legacy class | Many subsystem classes |
| Analogy | Power plug adapter | Hotel concierge |

```text
Adapter:  LegacyXmlParser → PaymentReader interface (different interface)
Facade:   OrderFacade.placeOrder() hides 5 services (simpler interface)
```

**One-liner:** Adapter = interface conversion. Facade = simplify many classes into one API.

</details>

---

# 18. Decorator vs Proxy?

<details>
<summary>Show Answer</summary>

**Answer:**

| | Decorator | Proxy |
|---|-----------|-------|
| Intent | **Add** responsibilities | **Control access** to object |
| Creation | Client may wrap explicitly | Client may not know it's proxy |
| Layers | Multiple decorators stacked | Usually one proxy |
| Examples | BufferedInputStream | Spring @Transactional proxy |

```text
Decorator:  adds compression + buffering to stream
Proxy:      lazy load entity, add transaction, security check
```

**One-liner:** Decorator adds features. Proxy controls access (lazy, security, AOP). Structure similar, intent different.

</details>

---

# 19. Strategy vs State?

<details>
<summary>Show Answer</summary>

**Answer:**

| | Strategy | State |
|---|----------|-------|
| Who picks behavior | **Client** chooses strategy | **Object** transitions state internally |
| Relationship | Strategy injected/set | State changes itself |
| Example | Pick payment method | Order moves NEW→PAID automatically |

```java
// Strategy — client sets
service.setStrategy(new UpiPayment());

// State — object transitions
order.getState().next(order); // state object changes order's state
```

**One-liner:** Strategy = client picks algorithm. State = object auto-changes behavior via internal state machine.

</details>

---

# 20. Template Method vs Strategy?

<details>
<summary>Show Answer</summary>

**Answer:**

| | Template Method | Strategy |
|---|-----------------|----------|
| Mechanism | **Inheritance** — subclass overrides steps | **Composition** — delegate to strategy object |
| Structure | Base class has algorithm skeleton | Interface + implementations |
| Change behavior | Override method in subclass | Swap strategy object |
| Flexibility at runtime | Harder (fixed subclass) | Easy (swap strategy) |

```text
Template Method: abstract class DataImporter.importData()
Strategy:        Sorter uses SortStrategy interface
```

**One-liner:** Template Method = inheritance hooks. Strategy = composition swap. Strategy more flexible at runtime.

</details>

---

# Pattern Quick Reference — Cheat Sheet

| Type | Pattern | Remember |
|------|---------|----------|
| Creational | Singleton | One instance |
| Creational | Factory | Hide new |
| Creational | Abstract Factory | Product family |
| Creational | Builder | Step-by-step build |
| Creational | Prototype | Clone |
| Structural | Adapter | Interface convert |
| Structural | Decorator | Add behavior wrap |
| Structural | Facade | Simplify subsystem |
| Structural | Proxy | Access control stand-in |
| Structural | Bridge | Abstraction + impl split |
| Behavioral | Strategy | Swap algorithm |
| Behavioral | Observer | Notify listeners |
| Behavioral | Command | Action as object |
| Behavioral | State | Behavior by state |
| Behavioral | Template Method | Skeleton in base class |
| Behavioral | Chain of Responsibility | Handler chain |

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> Creational: Singleton, Factory, Builder. Structural: Adapter (convert), Facade (simplify), Decorator (add), Proxy (control). Behavioral: Strategy (swap algo), Observer (notify), State (auto transition). Adapter≠Facade. Decorator≠Proxy. Strategy≠State. Template≠Strategy.

</details>
