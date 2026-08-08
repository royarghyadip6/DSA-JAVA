# 21. Inter-Thread Communication

## 21. Inter-Thread Communication

## Most Asked

---

# 1. wait()?

<details>
<summary>Show Answer</summary>

**Answer:**

`wait()` is an `Object` method that **releases the monitor lock** and puts the current thread into **WAITING** state until another thread calls `notify()` or `notifyAll()` on the same object.

### Basic Usage

```java
synchronized (lock) {
    while (queue.isEmpty()) {
        lock.wait(); // release lock, wait for notification
    }
    Task task = queue.remove();
}
```

### Method Signatures

```java
public final void wait() throws InterruptedException
public final void wait(long timeoutMillis) throws InterruptedException
public final void wait(long timeoutMillis, int nanos) throws InterruptedException
```

### What Happens Internally

```text
1. Thread must hold monitor on object (inside synchronized)
2. wait() called
3. Thread releases monitor lock
4. Thread state → WAITING (or TIMED_WAITING with timeout)
5. Thread added to object's wait set
6. Another thread calls notify/notifyAll
7. Waiting thread moves to entry set (blocked)
8. Re-acquires lock when available
9. wait() returns — thread continues
```

### Producer-Consumer Example

```java
class SharedQueue {
    private final Queue<String> queue = new LinkedList<>();
    private final int MAX = 10;

    public synchronized void produce(String item) throws InterruptedException {
        while (queue.size() == MAX) {
            wait(); // queue full — wait for consumer
        }
        queue.add(item);
        notifyAll(); // tell consumers item available
    }

    public synchronized String consume() throws InterruptedException {
        while (queue.isEmpty()) {
            wait(); // queue empty — wait for producer
        }
        String item = queue.remove();
        notifyAll(); // tell producers space available
        return item;
    }
}
```

### Rules

```text
✅ Must be inside synchronized block on SAME object
✅ Releases lock when waiting
✅ Throws InterruptedException
✅ Re-acquires lock before returning
❌ Calling wait() without holding lock → IllegalMonitorStateException
```

**Interview Point:**

> `wait()` = release lock + enter WAITING. Must be in `synchronized`. Waits for `notify()`/`notifyAll()`. Always use in a **while loop** checking condition.

</details>

---

# 2. notify()?

<details>
<summary>Show Answer</summary>

**Answer:**

`notify()` wakes **one thread** waiting on the object's monitor—chosen arbitrarily from the wait set. The awakened thread must re-acquire the lock before continuing.

### Basic Usage

```java
synchronized (lock) {
    queue.add(item);
    lock.notify(); // wake ONE waiting thread
}
```

### What Happens

```text
1. Caller must hold monitor on object
2. notify() called
3. ONE thread in wait set is selected (arbitrary)
4. That thread moves from wait set → entry set
5. Thread state: WAITING → BLOCKED (waiting for lock)
6. When lock released → awakened thread acquires lock
7. wait() returns in that thread
```

### Example

```java
class MessageBox {
    private String message;

    public synchronized void put(String msg) {
        this.message = msg;
        notify(); // wake consumer waiting on wait()
    }

    public synchronized String get() throws InterruptedException {
        while (message == null) {
            wait(); // consumer waits here
        }
        String msg = message;
        message = null;
        return msg;
    }
}
```

### notify() vs notifyAll()

| | `notify()` | `notifyAll()` |
|---|------------|---------------|
| Threads awakened | **One** (arbitrary) | **All** waiting threads |
| Use when | Single waiter sufficient | Multiple waiters or different conditions |
| Risk | May wake wrong thread | More contention — all compete for lock |

### Must Hold Lock

```java
// ❌ IllegalMonitorStateException
lock.notify();

// ✅ Correct
synchronized (lock) {
    lock.notify();
}
```

**Interview Point:**

> `notify()` wakes **one** arbitrary waiting thread. Must hold same monitor. Awakened thread must re-acquire lock. Prefer `notifyAll()` when multiple conditions exist.

</details>

---

# 3. notifyAll()?

<details>
<summary>Show Answer</summary>

**Answer:**

`notifyAll()` wakes **all threads** waiting on the object's monitor—they all move to the entry set and compete to re-acquire the lock.

### Basic Usage

```java
synchronized (lock) {
    queue.add(item);
    lock.notifyAll(); // wake ALL waiting threads
}
```

### What Happens

```text
1. Caller holds monitor on object
2. notifyAll() called
3. ALL threads in wait set awakened
4. All move to entry set → BLOCKED state
5. One by one acquire lock as caller releases
6. Each re-checks condition in while loop
7. Only threads with true condition proceed
```

### Why notifyAll() Is Safer

```java
class BoundedBuffer {
    private final Queue<Task> queue = new LinkedList<>();
    private final int capacity = 10;

    public synchronized void produce(Task task) throws InterruptedException {
        while (queue.size() == capacity) {
            wait(); // producers wait when full
        }
        queue.add(task);
        notifyAll(); // wake BOTH producers and consumers
        // consumers need to know item added
        // producers need to know space may be full again
    }

    public synchronized Task consume() throws InterruptedException {
        while (queue.isEmpty()) {
            wait(); // consumers wait when empty
        }
        Task task = queue.remove();
        notifyAll(); // wake both — safe for multiple conditions
        return task;
    }
}
```

### notify() Problem with Multiple Conditions

```text
Producer and Consumer both wait on same lock
notify() wakes ONE thread arbitrarily
  → may wake producer when queue is still full
  → producer re-checks condition, waits again
  → consumer never notified → potential missed wakeup (with notify)

notifyAll() wakes everyone → all re-check conditions → correct
```

### Performance Consideration

```text
notifyAll() → all threads wake → contention
  Acceptable for most cases
  For high-performance: use separate locks/conditions (ReentrantLock + Condition)
```

**Interview Point:**

> `notifyAll()` wakes **all** waiters — safer when multiple conditions share one lock. Each thread re-checks condition in while loop. Prefer over `notify()` in production code.

</details>

---

## Frequently Asked

---

# 4. Why wait(), notify() belong to Object class?

<details>
<summary>Show Answer</summary>

**Answer:**

`wait()`, `notify()`, and `notifyAll()` belong to `Object` because **every object has an intrinsic monitor (lock)**—thread communication is tied to the **resource object**, not the thread itself.

### Core Reason

```text
wait/notify coordinate threads around a SHARED OBJECT
  → "wait until THIS object has data"
  → "notify threads waiting on THIS object"

Monitor = intrinsic lock on any Object
Every Object can be a synchronization/communication point
Therefore methods live on Object class
```

### Every Object Is a Monitor

```java
Object lock = new Object();   // monitor
String msg = "hello";         // also a monitor!
List<String> queue = new ArrayList<>(); // also a monitor!

synchronized (queue) {
    queue.wait();    // wait on queue object's monitor
    queue.notify();  // notify waiters on queue
}
```

### Object Class Monitor Methods

```java
// Object.java
public final void wait() throws InterruptedException
public final void notify()
public final void notifyAll()
```

### Design Philosophy

```text
Thread  = worker (executes code)
Object  = resource (holds data + monitor)

Communication pattern:
  Thread waits ON object (not on thread)
  Thread notifies THROUGH object
  Lock and wait set belong to object
```

### Contrast with Thread Methods

| Object methods | Thread methods |
|----------------|----------------|
| `wait/notify` — coordination | `sleep/join/yield` — thread control |
| About shared resource | About thread behavior |
| Must hold object's lock | No lock required |

**Interview Point:**

> wait/notify on `Object` because every object has a monitor. Threads coordinate **on the shared resource object**, not on Thread instances.

</details>

---

# 5. Why not Thread class?

<details>
<summary>Show Answer</summary>

**Answer:**

`wait()`/`notify()` are not on `Thread` because communication is about **which resource** threads wait on—not about the thread itself. Multiple threads can wait on the **same object's monitor**.

### Wrong Mental Model

```text
❌ "Thread A notifies Thread B directly"
✅ "Thread A signals the LOCK/RESOURCE that Thread B is waiting on"
```

### Multiple Threads on One Object

```java
Object lock = new Object();

// 5 consumer threads all wait on SAME object
for (int i = 0; i < 5; i++) {
    new Thread(() -> {
        synchronized (lock) {
            try { lock.wait(); } // all wait on lock object
            catch (InterruptedException e) { }
        }
    }).start();
}

// One notifyAll on lock wakes ALL 5 consumers
synchronized (lock) {
    lock.notifyAll();
}
```

### If wait() Were on Thread

```text
Problems:
  Which thread's wait() to call? Thread A or Thread B?
  How does producer know which consumer thread to notify?
  Multiple waiters on same condition — can't model cleanly
  Lock ownership tied to object, not thread
```

### Thread Class Has Different Role

```java
// Thread class — control thread execution
Thread.sleep(1000);    // pause this thread
thread.join();         // wait for thread to die
Thread.yield();        // hint to scheduler
thread.interrupt();    // signal thread to stop

// Object class — coordinate via shared resource
lock.wait();           // wait on resource
lock.notify();         // signal resource state changed
```

### Real Design

```text
Monitor (lock + wait set) lives on Object
  Entry set: threads blocked trying to acquire lock
  Wait set:  threads that called wait() and released lock

Thread is just the executor — Object is the coordination point
```

**Interview Point:**

> Not on Thread because many threads wait on **one shared object**. Producer doesn't know which consumer to notify — it signals the **resource's monitor**. Object = lock + wait set.

</details>

---

# 6. Difference between sleep() and wait()?

<details>
<summary>Show Answer</summary>

**Answer:**

`sleep()` pauses the current thread for a fixed time without releasing locks. `wait()` releases the monitor lock and waits until `notify()`/`notifyAll()` or timeout.

### Comparison Table

| | `Thread.sleep()` | `Object.wait()` |
|---|------------------|-----------------|
| Class | `Thread` (static) | `Object` |
| Lock release | ❌ **No** | ✅ **Yes** |
| Wake up | Timeout or interrupt | `notify()` / timeout / interrupt |
| synchronized required? | No | ✅ **Yes** — must hold lock |
| Thread state | TIMED_WAITING | WAITING / TIMED_WAITING |
| Purpose | Delay / pause | Thread coordination |
| Called on | Current thread | Monitor object |

### sleep() — No Lock Release

```java
synchronized (lock) {
    Thread.sleep(5000); // holds lock — others blocked!
}
```

### wait() — Releases Lock

```java
synchronized (lock) {
    lock.wait(); // releases lock — others can enter
}
```

### Side-by-Side

```java
// sleep — just pauses, no coordination
Thread.sleep(1000);

// wait — coordination pattern
synchronized (lock) {
    while (!condition) {
        lock.wait(); // release lock, wait for state change
    }
    // condition true — proceed
}

synchronized (lock) {
    condition = true;
    lock.notifyAll(); // wake waiters
}
```

### When to Use

| Use sleep() | Use wait() |
|-------------|------------|
| Rate limiting | Producer-consumer |
| Retry delay | Wait for condition |
| Polling interval | State change notification |
| Simple pause | Thread handoff |

**Interview Point:**

> **sleep** = pause, no lock release, static Thread method. **wait** = release lock, wait for notify, must be synchronized. Most common thread interview comparison.

</details>

---

# 7. Does wait() release lock?

<details>
<summary>Show Answer</summary>

**Answer:**

**Yes.** When `wait()` is called, the thread **releases the monitor lock** on the object and enters the wait set—other threads can acquire the lock.

### Proof

```java
Object lock = new Object();

Thread waiter = new Thread(() -> {
    synchronized (lock) {
        try {
            System.out.println("Waiter: calling wait() — releasing lock");
            lock.wait(); // RELEASES lock here
            System.out.println("Waiter: woke up, lock re-acquired");
        } catch (InterruptedException e) { }
    }
});

Thread worker = new Thread(() -> {
    synchronized (lock) {
        System.out.println("Worker: acquired lock while waiter waiting");
        // Can enter because waiter released lock via wait()
    }
});

waiter.start();
Thread.sleep(500); // waiter is in wait() — lock released
worker.start();    // worker gets lock immediately
```

### wait() Lock Lifecycle

```text
1. Thread holds lock (inside synchronized)
2. wait() called
3. Lock RELEASED immediately
4. Thread in WAITING state (no lock held)
5. notify/notifyAll called
6. Thread moves to entry set (BLOCKED)
7. Lock RE-ACQUIRED when available
8. wait() returns — thread holds lock again
```

### Why Release Is Essential

```text
Without releasing lock:
  Consumer waits for data (holds lock)
  Producer can't add data (needs same lock)
  → deadlock!

With release:
  Consumer waits (lock free)
  Producer acquires lock, adds data, notifies
  Consumer re-acquires, gets data
```

### wait() vs sleep() — Lock Behavior

```java
synchronized (lock) {
    lock.wait();        // ✅ releases lock
    Thread.sleep(1000); // ❌ keeps lock
}
```

**Interview Point:**

> **wait() releases lock** — critical for producer-consumer. Without release, waiting thread blocks everyone needing same lock → deadlock.

</details>

---

# 8. Does sleep() release lock?

<details>
<summary>Show Answer</summary>

**Answer:**

**No.** `Thread.sleep()` does **not release** any locks the thread holds—it keeps all `synchronized` locks while sleeping.

### Proof

```java
Object lock = new Object();

Thread t1 = new Thread(() -> {
    synchronized (lock) {
        try {
            System.out.println("T1: sleeping WITH lock held");
            Thread.sleep(10000); // holds lock entire 10 seconds!
            System.out.println("T1: awake");
        } catch (InterruptedException e) { }
    }
});

Thread t2 = new Thread(() -> {
    synchronized (lock) {
        System.out.println("T2: got lock"); // waits 10 seconds!
    }
});

t1.start();
Thread.sleep(500);
t2.start(); // BLOCKED — t1 still holds lock during sleep
```

### sleep() Behavior

```text
Thread holds lock
Thread.sleep(5000) called
  → Thread state: TIMED_WAITING
  → Lock: STILL HELD by sleeping thread
  → Other threads: BLOCKED trying to acquire lock
After 5 seconds:
  → Thread wakes, still holds lock
  → Eventually exits synchronized block → releases lock
```

### Dangerous Pattern

```java
// ❌ Holds lock during long sleep — blocks all other threads
public synchronized void process() {
    fetchFromDatabase();  // slow
    Thread.sleep(5000);   // holds lock unnecessarily
    updateCache();
}

// ✅ Don't sleep inside synchronized
public void process() {
    Data data = fetchFromDatabase(); // no lock
    synchronized (this) {
        updateCache(data); // minimal lock time
    }
}
```

### sleep() vs wait() Summary

| Method | Releases Lock? |
|--------|----------------|
| `Thread.sleep()` | ❌ No |
| `Object.wait()` | ✅ Yes |
| `Thread.join()` | ❌ No (doesn't hold the target's lock) |
| `Lock.unlock()` | ✅ Yes (explicit) |

**Interview Point:**

> **sleep() does NOT release lock.** Classic trap in interviews. If you need to release lock while waiting → use `wait()`, not `sleep()`.

</details>

---

## Advanced

---

# 9. Lost notification problem?

<details>
<summary>Show Answer</summary>

**Answer:**

The **lost notification** (missed wakeup) problem occurs when a `notify()` happens **before** a thread calls `wait()`—the notification is lost and the waiter may sleep forever.

### How It Happens

```text
Timeline:
1. Producer checks: queue not empty (condition true)
2. Producer adds item
3. Producer calls notify()     ← notification sent
4. Consumer not yet waiting    ← missed!
5. Consumer calls wait()       ← sleeps forever (no one will notify again)
```

### Broken Code

```java
// ❌ Lost notification — check OUTSIDE synchronized
if (queue.isEmpty()) {
    synchronized (lock) {
        lock.wait(); // may miss notify that happened before wait()
    }
}
```

### Fix — Always Check Inside synchronized with while

```java
// ✅ Condition checked inside lock, in while loop
synchronized (lock) {
    while (queue.isEmpty()) {
        lock.wait();
    }
    item = queue.remove();
}
```

### Why while Loop Fixes It

```text
Even if spurious wakeup or missed notify:
  Thread wakes → re-checks condition in while loop
  If still empty → waits again
  If notifyAll used → all re-check

Lost notify scenario with while:
  notify before wait → consumer waits
  BUT if data was added before wait, while check fails → doesn't wait
```

### notify() vs notifyAll() for Lost Notification

```java
// notify() — may wake wrong thread (another producer when queue full)
synchronized (lock) {
    queue.add(item);
    lock.notify(); // might wake producer instead of consumer!
}

// notifyAll() — all re-check their condition
synchronized (lock) {
    queue.add(item);
    lock.notifyAll(); // safer — all waiters re-evaluate
}
```

### Modern Alternative

```java
// BlockingQueue — handles notification internally
BlockingQueue<String> queue = new LinkedBlockingQueue<>();
queue.put(item);  // no lost notification
queue.take();     // blocks safely
```

**Interview Point:**

> Lost notification = notify before wait. Fix: check condition **inside synchronized** in **while loop**. Use `notifyAll()`. Or use `BlockingQueue` / `Condition` API.

</details>

---

# 10. Spurious wakeup?

<details>
<summary>Show Answer</summary>

**Answer:**

A **spurious wakeup** is when a thread waiting in `wait()` wakes up **without** `notify()`, `notifyAll()`, `interrupt()`, or timeout—OS/JVM can wake threads unexpectedly.

### What It Is

```text
Thread calls wait() → WAITING state
No notify() called
Thread suddenly wakes → wait() returns
This is a spurious wakeup — rare but possible
```

### Why It Happens

```text
POSIX threads (pthreads) allow spurious wakeups
Java wait() built on OS primitives
JVM specification acknowledges spurious wakeups can occur
Not a bug — must be handled in code
```

### Broken Code

```java
// ❌ if — assumes wakeup means condition is true
synchronized (lock) {
    if (queue.isEmpty()) {
        lock.wait();
    }
    // spurious wakeup with empty queue → remove() throws or returns wrong!
    queue.remove();
}
```

### Correct Code — while Loop

```java
// ✅ while — re-check condition after every wakeup
synchronized (lock) {
    while (queue.isEmpty()) {
        lock.wait();
    }
    queue.remove(); // safe — queue guaranteed non-empty
}
```

### Spurious Wakeup Handling Flow

```text
wait() returns (spurious or real notify)
    ↓
while loop re-checks condition
    ↓
Condition false → wait() again
Condition true  → proceed safely
```

### Standard Pattern (Always Use)

```java
synchronized (lock) {
    while (!conditionPredicate()) {
        lock.wait();
    }
    // condition guaranteed true here
    doWork();
}
```

### ReentrantLock Condition — Same Rule

```java
Condition condition = lock.newCondition();

lock.lock();
try {
    while (!conditionPredicate()) {
        condition.await(); // same spurious wakeup risk
    }
    doWork();
} finally {
    lock.unlock();
}
```

**Interview Point:**

> Spurious wakeup = wait() returns without notify. **Always use while loop**, not if, when calling wait(). Standard Java concurrency pattern.

</details>

---

# 11. Why wait() should be inside loop?

<details>
<summary>Show Answer</summary>

**Answer:**

`wait()` must be in a **while loop** (not `if`) to **re-check the condition** after every wakeup—handles spurious wakeups, lost notifications, and multiple waiters with different conditions.

### Three Reasons for while Loop

| Reason | Problem without loop |
|--------|----------------------|
| **Spurious wakeup** | Thread proceeds when condition false |
| **Lost notification** | notify before wait — wrong state |
| **Multiple waiters** | notify wakes wrong thread |

### Reason 1 — Spurious Wakeup

```java
// ❌ if — broken on spurious wakeup
if (queue.isEmpty()) {
    wait();
}
queue.remove(); // may run with empty queue!

// ✅ while — safe
while (queue.isEmpty()) {
    wait();
}
queue.remove(); // queue guaranteed non-empty
```

### Reason 2 — Multiple Waiters, Different Conditions

```java
class BoundedBuffer {
  // Producers wait when FULL, consumers wait when EMPTY
  // Both wait on same lock!

    public synchronized void produce(Item item) throws InterruptedException {
        while (queue.size() == MAX) { // while — not if
            wait(); // producers wait here
        }
        queue.add(item);
        notifyAll();
    }

    public synchronized Item consume() throws InterruptedException {
        while (queue.isEmpty()) { // while — not if
            wait(); // consumers wait here
        }
        return queue.remove();
    }
}
```

```text
notify() wakes ONE thread arbitrarily
  → may wake producer when queue still full
  → producer's while loop re-checks → still full → waits again
  → correct behavior only possible with while
```

### Reason 3 — Condition May Change Before Re-acquire

```text
Thread wakes from wait()
  → must re-acquire lock (not instant)
  → another thread may change state before lock acquired
  → while loop re-verifies condition after lock acquired
```

### The Standard Guarded Block Pattern

```java
synchronized (lock) {
    while (!conditionPredicate()) {
        lock.wait();
    }
    // perform action — condition guaranteed true
}
```

### if vs while

```java
// ❌ NEVER use if with wait()
if (!condition) { wait(); }

// ✅ ALWAYS use while with wait()
while (!condition) { wait(); }
```

**Interview Point:**

> wait() in **while loop** — mandatory pattern. Handles spurious wakeup, lost notify, multiple conditions. `if` with wait() is a bug. Memorize the guarded block pattern.

</details>

---

# 5–8 Year Interview Rapid Fire

### Q: wait() without synchronized?

<details>
<summary>Show Answer</summary>

**Answer:**

Throws **`IllegalMonitorStateException`**. Thread must hold the monitor on the object before calling `wait()`, `notify()`, or `notifyAll()`.

```java
lock.wait(); // ❌ IllegalMonitorStateException

synchronized (lock) {
    lock.wait(); // ✅ OK
}
```

</details>

---

### Q: notify() vs notifyAll() — which to use?

<details>
<summary>Show Answer</summary>

**Answer:**

Use **`notifyAll()`** by default — safer when multiple threads wait on different conditions. Use `notify()` only when you can prove exactly one waiter and one condition.

```text
Single consumer, single condition → notify() OK
Multiple producers/consumers → notifyAll()
Production code → prefer notifyAll()
```

</details>

---

### Q: Modern alternative to wait/notify?

<details>
<summary>Show Answer</summary>

**Answer:**

```java
// BlockingQueue — built-in wait/notify
BlockingQueue<Task> queue = new LinkedBlockingQueue<>();
queue.put(task);  // blocks if full
queue.take();     // blocks if empty

// ReentrantLock + Condition — more flexible
Lock lock = new ReentrantLock();
Condition notEmpty = lock.newCondition();
Condition notFull  = lock.newCondition();
```

Prefer these over raw wait/notify in production.

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> wait/notify on **Object** (every object has monitor). wait **releases lock**; sleep does not. Always wait in **while loop**. notifyAll safer than notify. Spurious wakeup + lost notification → while loop fixes both. Modern: BlockingQueue, Condition.

</details>
