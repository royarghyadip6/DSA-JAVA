# 45. Observer Pattern

## 45. Observer Pattern

## Commonly Asked

---

# 1. What is Observer Pattern?

<details>
<summary>Show Answer</summary>

**Answer:**

**Observer Pattern** defines a **one-to-many** relationship—when one object (subject) changes state, **all dependent observers** are notified automatically.

### Simple Idea

```text
YouTube channel:
  Subject  = channel (posts new video)
  Observers = subscribers (get notified)
  One publish → many subscribers notified
```

```java
interface OrderObserver {
    void onOrderPlaced(Order order);
}

class EmailObserver implements OrderObserver {
    public void onOrderPlaced(Order order) {
        sendConfirmationEmail(order);
    }
}

class InventoryObserver implements OrderObserver {
    public void onOrderPlaced(Order order) {
        reserveStock(order);
    }
}

class OrderService {
    private final List<OrderObserver> observers = new ArrayList<>();

    public void placeOrder(Order order) {
        save(order);
        observers.forEach(o -> o.onOrderPlaced(order)); // notify all
    }
}
```

**Interview Point:**

> Observer = subject notifies multiple listeners on state change. Decouples event source from handlers.

</details>

---

# 2. Publisher Subscriber model?

<details>
<summary>Show Answer</summary>

**Answer:**

**Pub/Sub** is Observer pattern at **system scale**—publisher sends events to a **broker/topic**, subscribers listen without knowing the publisher directly.

```text
Observer (in-process):
  Subject ──direct notify──> Observer1, Observer2

Pub/Sub (distributed):
  Publisher → Topic (Kafka) → Subscriber1, Subscriber2
  Decoupled in time and space — async, scalable
```

| | Observer | Pub/Sub |
|---|----------|---------|
| Scope | Same application | Cross-service |
| Coupling | Subject knows observer list | Broker mediates |
| Async | Usually sync | Usually async |
| Examples | Java listeners, Spring events | Kafka, RabbitMQ |

```java
// Spring ApplicationEvent — observer in same JVM
applicationEventPublisher.publishEvent(new OrderPlacedEvent(order));
```

**Interview Point:**

> Observer = in-app direct notify. Pub/Sub = broker-mediated, distributed, async. Kafka is pub/sub at scale.

</details>

---

# 3. Real-world examples?

<details>
<summary>Show Answer</summary>

**Answer:**

| Example | Subject | Observers |
|---------|---------|-----------|
| **GUI buttons** | Button click | Multiple listeners |
| **Spring ApplicationEvent** | Event publisher | @EventListener handlers |
| **JMX notifications** | Managed resource | Listeners |
| **PropertyChangeListener** | JavaBeans property | UI updates |
| **RxJava / Reactor** | Observable stream | Subscribers |
| **Kafka consumers** | Topic | Consumer group |

```java
@EventListener
public void handleOrderPlaced(OrderPlacedEvent event) {
    sendEmail(event.getOrder());
}

@EventListener
public void updateInventory(OrderPlacedEvent event) {
    inventoryService.reserve(event.getOrder());
}
```

**Interview Point:**

> Spring @EventListener, GUI listeners, Kafka consumers — all observer/pub-sub variants.

</details>

---

## Framework Questions

---

# 4. Spring Event mechanism?

<details>
<summary>Show Answer</summary>

**Answer:**

Spring provides **ApplicationEvent** and **ApplicationEventPublisher** for in-process observer pattern.

```java
// 1. Define event
public class OrderPlacedEvent extends ApplicationEvent {
    private final Order order;
    public OrderPlacedEvent(Object source, Order order) {
        super(source);
        this.order = order;
    }
    public Order getOrder() { return order; }
}

// 2. Publish (subject)
@Service
public class OrderService {
    @Autowired ApplicationEventPublisher publisher;

    public void placeOrder(Order order) {
        save(order);
        publisher.publishEvent(new OrderPlacedEvent(this, order));
    }
}

// 3. Listen (observer)
@Component
public class OrderEmailListener {
    @EventListener
    public void onOrderPlaced(OrderPlacedEvent event) {
        emailService.sendConfirmation(event.getOrder());
    }

    @Async @EventListener  // async observer
    public void onOrderPlacedAsync(OrderPlacedEvent event) {
        analyticsService.track(event.getOrder());
    }
}
```

```text
✅ Decouples order service from email/analytics
✅ Multiple listeners per event
✅ @Async for non-blocking observers
✅ Same JVM — not distributed like Kafka
```

**Interview Point:**

> Spring Events = observer pattern. publishEvent + @EventListener. @Async for background handlers. TransactionalEventListener for after-commit.

</details>

---

# 5. Kafka/Event Driven Architecture relation?

<details>
<summary>Show Answer</summary>

**Answer:**

**Event-Driven Architecture (EDA)** uses **Observer/Pub-Sub at distributed scale**—Kafka is the message broker between services.

```text
Order Service (publisher)
    → Kafka topic: order-events
        → Email Service (subscriber)
        → Inventory Service (subscriber)
        → Analytics Service (subscriber)

Each service independent — observer pattern across microservices
```

| Spring Event | Kafka |
|--------------|-------|
| Same JVM | Cross-service |
| Sync by default | Async |
| Lost if app crashes | Durable log |
| Simple setup | Scalable, replayable |

```java
// Kafka producer — publish event
kafkaTemplate.send("order-events", order.getId(), order);

// Kafka consumer — observer
@KafkaListener(topics = "order-events")
public void handleOrder(Order order) {
    sendEmail(order);
}
```

**Interview Point:**

> Kafka = distributed observer/pub-sub. Durable, replayable, decoupled microservices. Spring Events for same-app; Kafka for cross-service.

</details>

---

# 5–8 Year Interview Rapid Fire

### Q: Observer vs Mediator?

<details>
<summary>Show Answer</summary>

**Answer:**

**Observer** = subject notifies observers directly (or via event bus). **Mediator** = all communication goes through central mediator—observers don't know each other. Chat room = mediator.

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> Observer = one subject, many listeners notified on change. Pub/Sub scales it with broker. Spring @EventListener in-app. Kafka for distributed event-driven microservices.

</details>
