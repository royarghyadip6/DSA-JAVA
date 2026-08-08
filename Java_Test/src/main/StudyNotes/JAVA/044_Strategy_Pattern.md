# 44. Strategy Pattern

## 44. Strategy Pattern

## Frequently Asked

---

# 1. What is Strategy Pattern?

<details>
<summary>Show Answer</summary>

**Answer:**

**Strategy Pattern** defines a family of algorithms, encapsulates each one, and makes them **interchangeable**—the client picks the strategy at runtime without changing client code.

### Simple Idea

```text
Navigation app:
  Strategy = route algorithm (fastest, shortest, avoid tolls)
  Switch strategy without rewriting the app
```

```java
interface PaymentStrategy {
    void pay(double amount);
}

class CreditCardPayment implements PaymentStrategy { ... }
class UpiPayment implements PaymentStrategy { ... }

class PaymentService {
    private PaymentStrategy strategy;

    public void setStrategy(PaymentStrategy strategy) {
        this.strategy = strategy;
    }

    public void checkout(double amount) {
        strategy.pay(amount); // delegates to chosen strategy
    }
}
```

**Interview Point:**

> Strategy = interchangeable algorithms behind one interface. Select behavior at runtime. Encapsulates if/else logic.

</details>

---

# 2. Why use Strategy Pattern?

<details>
<summary>Show Answer</summary>

**Answer:**

| Without Strategy | With Strategy |
|------------------|---------------|
| Giant if/else or switch | Each algorithm in its own class |
| Hard to add new behavior | Add new class, plug in |
| Client knows all implementations | Client uses interface only |
| Violates Open/Closed | Open for extension |

```java
// ❌ Without strategy
public void pay(String type, double amount) {
    if ("card".equals(type)) { /* card logic */ }
    else if ("upi".equals(type)) { /* upi logic */ }
    else if ("wallet".equals(type)) { /* wallet logic */ }
    // grows forever
}

// ✅ With strategy
paymentService.setStrategy(strategyFactory.get(type));
paymentService.checkout(amount);
```

**Interview Point:**

> Strategy eliminates growing conditionals. New payment type = new class, no change to PaymentService.

</details>

---

# 3. Open Closed Principle relation?

<details>
<summary>Show Answer</summary>

**Answer:**

**Open/Closed Principle (OCP)** = open for **extension**, closed for **modification**. Strategy is a classic OCP pattern.

```text
Closed for modification:
  PaymentService.checkout() never changes

Open for extension:
  Add BitcoinPayment implements PaymentStrategy
  Wire in via Spring @Qualifier or factory
  No edit to PaymentService code
```

```java
@Service
public class PaymentService {
    public void pay(Order order, PaymentStrategy strategy) {
        strategy.pay(order.getTotal()); // stable — never modified for new types
    }
}

// New strategy — extension only
@Component
class CryptoPayment implements PaymentStrategy { ... }
```

**Interview Point:**

> Strategy supports OCP — extend with new strategy classes, don't modify client. Classic interview link.

</details>

---

## Scenario Questions

---

# 4. Payment system design?

<details>
<summary>Show Answer</summary>

**Answer:**

```java
public interface PaymentStrategy {
    PaymentResult pay(PaymentRequest request);
}

@Component
class StripePayment implements PaymentStrategy {
    public PaymentResult pay(PaymentRequest req) {
        return stripeClient.charge(req);
    }
}

@Component
class RazorpayPayment implements PaymentStrategy {
    public PaymentResult pay(PaymentRequest req) {
        return razorpayClient.charge(req);
    }
}

@Service
public class CheckoutService {
    private final Map<String, PaymentStrategy> strategies;

    public CheckoutService(List<PaymentStrategy> list) {
        this.strategies = list.stream()
            .collect(Collectors.toMap(s -> s.getType(), s -> s));
    }

    public PaymentResult checkout(String method, PaymentRequest req) {
        PaymentStrategy strategy = strategies.get(method);
        if (strategy == null) throw new IllegalArgumentException(method);
        return strategy.pay(req);
    }
}
```

```text
Customer picks "stripe" or "razorpay" → strategy selected → pay()
Add PayPal = new PaymentStrategy class + register — no CheckoutService change
```

**Interview Point:**

> Map of strategies by type key. Spring injects all implementations. OCP in production payment routing.

</details>

---

# 5. Notification system design?

<details>
<summary>Show Answer</summary>

**Answer:**

```java
public interface NotificationStrategy {
    void send(NotificationMessage message);
    String channel();
}

@Component
class EmailNotification implements NotificationStrategy {
    public void send(NotificationMessage msg) { emailClient.send(msg); }
    public String channel() { return "EMAIL"; }
}

@Component
class SmsNotification implements NotificationStrategy {
    public void send(NotificationMessage msg) { smsClient.send(msg); }
    public String channel() { return "SMS"; }
}

@Service
public class NotificationService {
    private final Map<String, NotificationStrategy> channels;

    public NotificationService(List<NotificationStrategy> strategies) {
        channels = strategies.stream()
            .collect(Collectors.toMap(NotificationStrategy::channel, s -> s));
    }

    public void notifyUser(String channel, NotificationMessage msg) {
        channels.get(channel).send(msg);
    }

    public void notifyAllChannels(NotificationMessage msg) {
        channels.values().forEach(s -> s.send(msg));
    }
}
```

**Interview Point:**

> Each channel = strategy. User preference picks strategy. Multi-channel = loop all strategies.

</details>

---

# 6. Sorting strategy example?

<details>
<summary>Show Answer</summary>

**Answer:**

```java
public interface SortStrategy {
    void sort(List<Integer> data);
}

class QuickSortStrategy implements SortStrategy {
    public void sort(List<Integer> data) { /* quicksort */ }
}

class MergeSortStrategy implements SortStrategy {
    public void sort(List<Integer> data) { /* mergesort */ }
}

class Sorter {
    private SortStrategy strategy;

    public void setStrategy(SortStrategy strategy) {
        this.strategy = strategy;
    }

    public void sort(List<Integer> data) {
        strategy.sort(data);
    }
}

// Runtime choice
if (data.size() < 100) {
    sorter.setStrategy(new QuickSortStrategy());
} else {
    sorter.setStrategy(new MergeSortStrategy());
}
sorter.sort(numbers);
```

```text
Java Comparator is strategy pattern:
  list.sort(Comparator.comparing(Employee::getSalary));
  different comparators = different sort strategies
```

**Interview Point:**

> Comparator = strategy for sorting. Swap algorithm at runtime. Classic textbook + Java Collections example.

</details>

---

# 5–8 Year Interview Rapid Fire

### Q: Strategy vs State pattern?

<details>
<summary>Show Answer</summary>

**Answer:**

**Strategy** = client **chooses** algorithm. **State** = object **changes** behavior automatically based on internal state (e.g. order: NEW → PAID → SHIPPED).

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> Strategy = interchangeable algorithms via interface. Kills if/else chains. Supports OCP. Payment routing, notifications, Comparator — all strategy. Client delegates to strategy at runtime.

</details>
