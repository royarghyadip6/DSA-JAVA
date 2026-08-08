# 31. Producer Consumer

## 31. Producer Consumer

## Most Asked Scenario

---

# 1. Explain Producer Consumer Problem.

<details>
<summary>Show Answer</summary>

**Answer:**

The **producer-consumer problem** is a classic threading scenario where **producers create data** and put it in a **shared buffer**, while **consumers take data** from that buffer and process it—both must work safely without crashing or losing data.

### Simple Real-Life Analogy

```text
Bakery kitchen:
  Chef (Producer)  → makes cakes → puts on shelf (shared buffer)
  Customer (Consumer) → takes cake from shelf → eats it

Problems to handle:
  Shelf full    → chef must wait (can't add more)
  Shelf empty   → customer must wait (nothing to take)
  Two chefs     → can't grab same spot at same time
```

### In Software

```text
Producer threads:  receive orders, read files, fetch API data
Shared buffer:       queue, list, buffer between them
Consumer threads:    process orders, write DB, send emails
```

### What Can Go Wrong Without Proper Design

| Problem | What Happens |
|---------|--------------|
| **Buffer full** | Producer keeps adding → overflow / lost data |
| **Buffer empty** | Consumer tries to read → error / crash |
| **Race condition** | Two threads modify buffer → corrupted data |
| **Lost update** | Producer and consumer clash → item lost |

### Broken Example — No Sync

```java
Queue<String> queue = new LinkedList<>(); // NOT thread-safe!

// Producer
queue.add(order); // Thread A and B may corrupt queue

// Consumer
String order = queue.remove(); // Exception if empty + race condition
```

### What We Need

```text
✅ Thread-safe shared buffer
✅ Producer waits when buffer is FULL
✅ Consumer waits when buffer is EMPTY
✅ Only one thread modifies buffer at critical moment
```

### Real Production Examples

```text
Order service:     HTTP receives order → queue → workers process payment
Log pipeline:      App writes log lines → queue → aggregator stores to DB
Kafka:             Producers publish → topic (buffer) → consumers read
ExecutorService:   submit task → queue → pool threads execute
```

**Interview Point:**

> Producer-consumer = shared buffer between makers and takers. Must handle full, empty, and thread safety. Classic concurrency interview scenario.

</details>

---

# 2. How to solve using wait/notify?

<details>
<summary>Show Answer</summary>

**Answer:**

Using `wait()` and `notify()`/`notifyAll()`, producers **wait when the buffer is full** and consumers **wait when it's empty**—all inside a `synchronized` block on the same lock object.

### Core Pattern — Guarded Blocks

```java
class SharedBuffer {
    private final Queue<String> queue = new LinkedList<>();
    private final int CAPACITY = 10;

    // PRODUCER
    public synchronized void produce(String item) throws InterruptedException {
        while (queue.size() == CAPACITY) {
            wait(); // buffer full — release lock, wait for consumer
        }
        queue.add(item);
        System.out.println("Produced: " + item);
        notifyAll(); // wake waiting consumers (and producers to re-check)
    }

    // CONSUMER
    public synchronized String consume() throws InterruptedException {
        while (queue.isEmpty()) {
            wait(); // buffer empty — release lock, wait for producer
        }
        String item = queue.remove();
        System.out.println("Consumed: " + item);
        notifyAll(); // wake waiting producers
        return item;
    }
}
```

### Why `while` Not `if`

```text
Spurious wakeup → thread wakes without notify
notify wakes wrong thread (producer when full)
while re-checks condition → safe
```

### Running Producer and Consumer

```java
SharedBuffer buffer = new SharedBuffer();

// Producer thread
new Thread(() -> {
    try {
        for (int i = 0; i < 20; i++) {
            buffer.produce("Item-" + i);
            Thread.sleep(100);
        }
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
}).start();

// Consumer thread
new Thread(() -> {
    try {
        while (true) {
            buffer.consume();
            Thread.sleep(300); // slower consumer
        }
    } catch (InterruptedException e) { }
}).start();
```

### wait/notify Flow

```text
Producer: buffer full → wait() → releases lock → WAITING
Consumer: takes item → notifyAll() → producer wakes → adds item

Consumer: buffer empty → wait() → releases lock → WAITING
Producer: adds item → notifyAll() → consumer wakes → takes item
```

### Rules to Remember

```text
✅ Same lock object for produce and consume
✅ wait() inside synchronized block
✅ while loop checking condition
✅ notifyAll() safer than notify() (multiple conditions)
✅ wait() releases lock — consumer can enter while producer waits
```

**Interview Point:**

> wait/notify solution: synchronized + while(condition) wait() + notifyAll(). Classic pattern but error-prone — prefer BlockingQueue in production.

</details>

---

# 3. How to solve using BlockingQueue?

<details>
<summary>Show Answer</summary>

**Answer:**

`BlockingQueue` solves producer-consumer **automatically**—`put()` blocks when full, `take()` blocks when empty. No manual `wait()`/`notify()` needed.

### Simple Solution

```java
BlockingQueue<String> queue = new ArrayBlockingQueue<>(10);

// Producer
new Thread(() -> {
    try {
        for (int i = 0; i < 100; i++) {
            queue.put("Item-" + i); // blocks if queue full (10 items)
            System.out.println("Produced: Item-" + i);
        }
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
}).start();

// Consumer
new Thread(() -> {
    try {
        while (true) {
            String item = queue.take(); // blocks if queue empty
            System.out.println("Consumed: " + item);
            process(item);
        }
    } catch (InterruptedException e) { }
}).start();
```

### What BlockingQueue Handles For You

| Concern | BlockingQueue |
|---------|---------------|
| Thread safety | ✅ Built-in |
| Wait when full | ✅ `put()` blocks |
| Wait when empty | ✅ `take()` blocks |
| Lock management | ✅ Internal |
| wait/notify | ✅ Not needed |

### Multiple Producers and Consumers

```java
BlockingQueue<Order> orderQueue = new LinkedBlockingQueue<>(1000);

// 5 producer threads (e.g., API handlers)
for (int i = 0; i < 5; i++) {
    executor.submit(() -> {
        Order order = receiveOrder();
        orderQueue.put(order); // safe — all producers share queue
    });
}

// 10 consumer threads (workers)
for (int i = 0; i < 10; i++) {
    executor.submit(() -> {
        while (running) {
            Order order = orderQueue.take();
            processOrder(order);
        }
    });
}
```

### Non-Blocking Options

```java
// Don't want to block forever?
boolean added = queue.offer(item);           // false if full
boolean added = queue.offer(item, 5, SECONDS); // timeout

String item = queue.poll();                  // null if empty
String item = queue.poll(5, SECONDS);        // timeout wait
```

### ExecutorService — Built-In Producer-Consumer

```java
// You are the producer
ExecutorService pool = Executors.newFixedThreadPool(10);

pool.submit(() -> processTask()); // put task in internal BlockingQueue
pool.submit(() -> processTask()); // workers (consumers) take and run
```

**Interview Point:**

> BlockingQueue = production-standard producer-consumer. `put()`/`take()` replace wait/notify. Used inside ExecutorService task queues.

</details>

---

## Advanced

---

# 4. Which approach preferred in production?

<details>
<summary>Show Answer</summary>

**Answer:**

In production, **`BlockingQueue`** (or higher-level tools built on it like `ExecutorService`) is preferred over manual `wait()`/`notify()`—less code, fewer bugs, and battle-tested implementations.

### Comparison

| Approach | Production Use |
|----------|----------------|
| **BlockingQueue** | ✅ **Preferred** — standard, safe, simple |
| **ExecutorService** | ✅ **Best** — queue + thread pool + lifecycle |
| **wait/notify** | ⚠️ Learning/interviews only — error-prone |
| **Kafka/RabbitMQ** | ✅ Distributed, multi-service scale |

### Production Pattern — ExecutorService

```java
@Service
public class OrderProcessor {

    private final ExecutorService workers =
        new ThreadPoolExecutor(
            10, 20, 60L, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(500),          // bounded buffer
            new ThreadPoolExecutor.CallerRunsPolicy() // backpressure
        );

    // Producer side — API receives order
    public void submitOrder(Order order) {
        workers.submit(() -> processOrder(order));
        // Internally: order goes to queue → worker takes it
    }

    @PreDestroy
    public void shutdown() {
        workers.shutdown();
        try {
            if (!workers.awaitTermination(30, TimeUnit.SECONDS)) {
                workers.shutdownNow();
            }
        } catch (InterruptedException e) {
            workers.shutdownNow();
        }
    }
}
```

### When to Use Each

```text
Same JVM, simple task queue:
  → ExecutorService + BlockingQueue (internal)

Same JVM, custom buffer logic:
  → BlockingQueue directly

Multiple services, durability, replay:
  → Kafka / RabbitMQ / Redis queue

Interview / understanding concurrency:
  → wait/notify (know it, don't ship it)
```

### Production Checklist

```text
✅ Bounded queue (prevent OOM)
✅ Rejection policy or CallerRunsPolicy (backpressure)
✅ Graceful shutdown (shutdown + awaitTermination)
✅ Monitor queue size (alert if near capacity)
✅ Handle InterruptedException properly
```

**Interview Point:**

> Production: **ExecutorService** for same-app tasks, **Kafka** for distributed. Manual wait/notify — know for interviews, avoid in code. Always bounded queue + shutdown hook.

</details>

---

# 5. Why BlockingQueue is better?

<details>
<summary>Show Answer</summary>

**Answer:**

`BlockingQueue` is better because it **bundles thread safety, blocking, and coordination** into one API—you avoid the bugs and complexity of hand-written `wait()`/`notify()`.

### BlockingQueue vs wait/notify

| | wait/notify | BlockingQueue |
|---|-------------|---------------|
| Code lines | 30–50+ | 5–10 |
| Spurious wakeup | Must handle with while | Handled internally |
| Lost notification | Possible if wrong | Not possible |
| Multiple conditions | Complex notifyAll | Built-in |
| Bug risk | High | Low |
| Reusable | Custom each time | Standard API |
| Testing | Hard | Easy |

### Bugs You Avoid

```java
// ❌ wait/notify bugs:
if (queue.isEmpty()) wait();     // if instead of while — broken
wait();                          // not in synchronized — exception
notify();                        // may wake wrong thread
// forgot notify after add — consumer sleeps forever

// ✅ BlockingQueue — none of these possible
queue.take(); // always correct
```

### Built-In Features

```text
put() / take()     — blocking
offer() / poll()   — non-blocking with timeout
remainingCapacity() — monitor queue health
clear(), size()    — management
Iterator           — inspection (careful)
```

### Performance

```text
BlockingQueue implementations are highly optimized:
  ArrayBlockingQueue  — circular array, single lock
  LinkedBlockingQueue — two locks (put/take separate)
  LinkedTransferQueue — direct handoff option

Hand-written wait/notify rarely matches this optimization
```

### Industry Standard

```text
ExecutorService      → uses BlockingQueue internally
ThreadPoolExecutor   → ArrayBlockingQueue or LinkedBlockingQueue
Spring @Async        → TaskExecutor → BlockingQueue
Java concurrent pkg  → designed around BlockingQueue
```

### When wait/notify Still Makes Sense

```text
✅ Custom condition beyond queue (rare)
✅ Interview demonstration of JMM concepts
✅ Legacy code maintenance
❌ New producer-consumer code → always BlockingQueue
```

**Interview Point:**

> BlockingQueue wins: simpler, safer, fewer bugs, optimized, industry standard. wait/notify teaches concepts but BlockingQueue ships in production.

</details>

---

# 5–8 Year Interview Rapid Fire

### Q: BlockingQueue put() vs offer()?

<details>
<summary>Show Answer</summary>

**Answer:**

`put()` **blocks** until space available — producer waits. `offer()` returns **false immediately** if full — no blocking. Use `put()` when task must not be lost; `offer()` when you can skip or retry.

</details>

---

### Q: Multiple consumers on one BlockingQueue?

<details>
<summary>Show Answer</summary>

**Answer:**

**Yes** — safe and common. Each `take()` returns a **different item** to whichever consumer gets it. Work is distributed across consumers automatically. Like multiple checkout counters sharing one queue of customers.

</details>

---

### Q: Producer faster than consumer — what happens?

<details>
<summary>Show Answer</summary>

**Answer:**

With **bounded** queue: producer blocks on `put()` when full — natural backpressure. With **unbounded** queue: queue grows → memory risk → OOM. Always use **bounded queue** + monitor queue size in production.

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> Producer-consumer = shared buffer, producers wait when full, consumers wait when empty. **BlockingQueue** (`put`/`take`) beats wait/notify in production. Use **ExecutorService** for task queues. Bounded queue + backpressure always.

</details>
