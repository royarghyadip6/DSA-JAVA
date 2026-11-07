# 4. Concurrency and Parallelism

## Simple idea

**Concurrency** means many tasks are **in progress** together. They may take turns on one CPU.

**Parallelism** means many tasks run **at the same instant** on more than one core.

```text
One cook, four orders     →  concurrency   (switch between pans)
Four cooks, four orders   →  parallelism   (true overlap)
```

Concurrency is about staying responsive. Parallelism is about finishing CPU work sooner.

A web server is concurrent: while one request waits on the database, another request can run. Video encoding is parallel: split the frames across cores.

---

## Processes vs threads

| | Process | Thread |
|--|---------|--------|
| Memory | Its own | Shares the process heap |
| Cost | Heavy to start and switch | Lighter |
| Safety | A crash is contained | One thread can corrupt what others see |
| Talk to each other | Pipes, sockets, messages | Just read the same objects (and that is the danger) |

In Java, one JVM is a process. Request handling uses threads inside it. `synchronized` and locks exist because those threads share the heap.

---

## Thread pools

Creating a thread per request is expensive, and unbounded threads will exhaust memory.

A **pool** creates a fixed set of workers and reuses them.

```text
request → queue → idle worker from the pool → run → worker goes back
```

Java: `ExecutorService`, and the pool inside Tomcat or another servlet container. Spring MVC requests sit on that pool. Do not start a raw `new Thread()` for each job.

If every worker is blocked on a slow database, new requests wait even when the CPU is idle. That is why pool size and database pool size must be chosen together, not "as large as possible".

---

## Async I/O vs threads

| Work | Better tool | Why |
|------|-------------|-----|
| Waiting on network, disk, or another service | Async / non-blocking I/O | The thread should not sit idle while the socket waits |
| Heavy CPU (image encode, a big calculation) | More cores, a worker pool | You need real parallel execution |

Async does not make CPU math faster. It stops threads from being stuck on I/O.

Java equivalents of the course's async/await list: `CompletableFuture`, Spring `@Async`, and WebFlux (event style). A message queue is still the right place for work that should not run inside the request at all (see the previous file).

**Node.js event loop.** One thread runs your JavaScript. When the stack is empty it takes the next callback. Network I/O is non-blocking on that loop. The libuv thread pool is for a smaller set of blocking jobs (many file-system calls, some DNS and crypto), not for every timer and not for normal sockets. The interview PDF lumps "I/O and timers" into that pool. Timers belong to the loop. The useful interview point stays: one thread can juggle many connections because it does not block on the network.

**Classic vs modern servers.** Old Apache-style servers used one process or thread per connection. That tops out. Nginx, Node, and modern Java servers use a small set of threads plus non-blocking I/O, or a bounded pool. "Apache cannot scale" is too strong for current Apache. The design lesson is: unbounded thread-per-connection does not scale.

---

## Race conditions

Two threads read `count = 1`, both add one, both write `2`. One update vanished.

```text
Thread A reads 1
Thread B reads 1
Thread A writes 2
Thread B writes 2     the other +1 is lost
```

Prevent it:

- a lock (`synchronized`, `ReentrantLock`) around the read-and-update
- an atomic (`AtomicInteger`, `ConcurrentHashMap`)
- no shared mutable state (each request uses its own data)

`ConcurrentHashMap` is the Java stand-in for the PDF's `ConcurrentDictionary`. A reader-writer lock fits when many threads read and few write.

---

## Deadlock

A holds lock 1 and wants lock 2. B holds lock 2 and wants lock 1. Neither moves.

Fix:

- always take locks in the **same order**
- use a **timeout**, and back off if you cannot get the lock
- hold locks for the smallest possible section
- prefer lock-free structures when they fit

To debug: capture thread stacks (a Java thread dump, `jstack`, or the debugger). Look for threads stuck in `BLOCKED` / waiting on each other's locks.

---

## A server that handles thousands of requests

```text
load balancer
   → many stateless app instances
        → bounded thread pool for requests
        → async or a small pool for outbound I/O
        → connection pool toward the database
        → Redis for hot reads
        → a queue for email and exports
```

Thousands of **open connections** is a concurrency problem (do not block a thread per idle socket). Thousands of **heavy CPU jobs** is a parallelism problem (you need cores, and you should not do that work on the request thread).

---

## Background jobs

Workers pull from RabbitMQ, Kafka, or SQS. Add more worker processes to go faster. Retry with a limit. Make the handler idempotent. Alert on lag. Same shape as the queue lecture, seen from the worker side.

---

## Interview lines

- Concurrent tasks overlap in time. Parallel tasks run at the same instant on many cores.
- Threads share memory and are cheap. Processes are isolated and heavier.
- Use a pool. Do not create unbounded threads.
- Async I/O for waiting. Threads and cores for CPU work.
- Races need a lock or an atomic. Deadlock needs a fixed lock order.

---

## Interview Q&A

### 1. Concurrency vs parallelism?

<details>
<summary>Show Answer</summary>

**Answer:**

Concurrency is many tasks in progress, taking turns if there is only one core. The goal is responsiveness. Example: a server interleaving HTTP requests.

Parallelism is tasks running at the same time on several cores. The goal is speed. Example: splitting a matrix or an image across cores.

You can be concurrent without being parallel. Parallel work is also concurrent.

</details>

### 2. Threads vs processes?

<details>
<summary>Show Answer</summary>

**Answer:**

A process has its own memory. It is safer and heavier. A thread shares the process memory and has its own stack. Threads start faster and communicate by sharing objects, which is also how race conditions happen.

</details>

### 3. Why a thread pool?

<details>
<summary>Show Answer</summary>

**Answer:**

Creating and destroying threads costs time. An unlimited number of them will exhaust memory. A pool reuses a bounded set of workers, so throughput stays predictable. Java servers do this with `ExecutorService` and the servlet container pool.

</details>

### 4. How would you handle thousands of concurrent requests?

<details>
<summary>Show Answer</summary>

**Answer:**

Put a load balancer in front of stateless instances. Use non-blocking I/O or a bounded request pool, not one new thread per connection. Pool database connections. Cache hot reads. Push slow work to a queue. Keep CPU-heavy work off the request path and on a separate worker pool.

</details>

### 5. Background jobs at scale?

<details>
<summary>Show Answer</summary>

**Answer:**

Producers publish to a queue. Worker processes consume, retry, and stay idempotent. Scale by adding workers. Measure queue lag and alert on it.

</details>

### 6. How do you debug a deadlock?

<details>
<summary>Show Answer</summary>

**Answer:**

Take a thread dump and see which locks each thread holds and wants. You are looking for a cycle. Fix by acquiring locks in one global order, timing out, shrinking the locked section, or using a concurrent collection so you do not hand-roll the locks.

</details>

### 7. What is a race condition?

<details>
<summary>Show Answer</summary>

**Answer:**

Two threads read and write the same data with no synchronization, so the result depends on timing. A lost increment is the classic bug. Prevent it with a lock, an atomic type, a concurrent collection, or by not sharing mutable state.

</details>

### 8. How do you keep shared memory thread-safe?

<details>
<summary>Show Answer</summary>

**Answer:**

Lock the critical section (`synchronized` or `ReentrantLock`). Use `AtomicInteger` or `ConcurrentHashMap` for the common cases. Prefer immutable objects. A reader-writer lock helps when reads dominate. In Spring, do not invent threads if `CompletableFuture` or the container pool already does the job.

</details>

### 9. How does the Node.js event loop work?

<details>
<summary>Show Answer</summary>

**Answer:**

One thread runs JavaScript. It finishes the current stack, then runs the next callback. Network I/O does not block that thread. A small libuv pool covers some blocking work such as many file operations. Timers are scheduled by the loop, not dumped onto that pool as generic I/O. That is why one Node process can hold many connections, and why CPU-heavy JavaScript still blocks everything else.

</details>

### 10. Threads vs async I/O?

<details>
<summary>Show Answer</summary>

**Answer:**

Threads (and several cores) speed up CPU-bound work. Async I/O stops a thread from waiting on the network or disk, which raises how many requests you can juggle. Use both: async or a small pool for I/O, a separate pool for heavy computation.

</details>

**Q&A recap:** overlap tasks to stay responsive. Use extra cores only for real computation. Lock shared data, in a consistent order.

## Interview Q&A — 6 to 8 years

### S1. The service has 200 threads and is slower than when it had 20. Why?

<details>
<summary>Show Answer</summary>

**Answer:**

Threads were waiting on the same database or lock, not computing. Extra threads added context switches, memory, and more connections than the database allows. I size the pool to the bottleneck's concurrency (how many queries the database can actually run), not to the number of users. CPU work can use about one busy thread per core. I/O work can use more, but only until the downstream saturates.

**Corner cases:** An unbounded `new Thread` per request instead of a pool. Under load the process dies from memory before it sheds load. A pool queue that is unbounded, so latency grows forever instead of failing fast. A deadlock that only appears when the pool is large enough for two orders to run at once. A synchronized block around a slow HTTP call, so one lock holds every thread. I move the call outside the lock. Virtual threads or async I/O do not help a synchronized JDBC driver that pins a real thread.

</details>

### S2. How do you close a race without a giant lock?

<details>
<summary>Show Answer</summary>

**Answer:**

I move the decision into one atomic update. "Set seat = sold where seat = held and holder = me." One statement, no lock in the app. If I must lock, I lock the smallest key (the seat, not the whole concert) and I take locks in a fixed order (lowest id first) so two transactions cannot deadlock. I set a lock timeout so a stuck holder gets an error instead of a pile-up.

**Corner cases:** Check-then-act in Java (`if map.contains` then `put`) that passes tests until two requests arrive. A `ConcurrentHashMap` that makes the map safe and does not make the business rule safe. Read-modify-write of a Redis value without `WATCH` or a Lua script. A lock with no timeout on a client that died. A transaction that locks rows in the order the HTTP JSON happened to list them. I sort first. Lost updates with two read-committed transactions that each add one to a counter. I use an atomic increment or a version column.

</details>

### S3. A deadlock shows up once a week. What do you change so it cannot hide?

<details>
<summary>Show Answer</summary>

**Answer:**

I make the lock order global and I log the cycle (who holds what, and the SQL). I add a timeout so the database kills one waiter and the request fails cleanly. I retry that request only if the transaction is idempotent. I stop holding locks while I call the network. If two services lock in opposite orders, I pick one orchestrator so only one service locks both resources.

**Corner cases:** A deadlock across a database lock and an application lock that the database viewer cannot see. A try-lock that fails and the code continues as if it succeeded. Retries that immediately deadlock again with no jitter. A thread dump taken too late, after the process was restarted. I want the deadlock error from the database, which names the statements. Gap locks and phantom reads that only appear at the isolation level production uses, not the one in the unit test.

</details>

### S4. Where do you use a queue instead of more threads inside the API?

<details>
<summary>Show Answer</summary>

**Answer:**

When the work should not sit on a request thread: image processing, fan-out, a slow partner. The API enqueues and returns. Workers pull at a rate the database can take. That is parallelism with a brake. Inside the request I only parallelize independent I/O that the user is waiting for (fetch profile and fetch balance), with a deadline, and I cancel the slower one when the deadline hits.

**Corner cases:** `parallelStream` on a shared fork-join pool that now starves the server's own tasks. A fan-out of 100 calls with no limit, so one request occupies 100 connections. A queue worker pool and an API pool that share one database and were each sized as if they owned it. I budget connections as a sum. Async code that swallows the exception and returns success. The user sees a done state and the worker never ran.

</details>

**Senior recap:** size pools to the real bottleneck, put the invariant in one atomic update, lock in one order with a timeout, and queue the work the request should not wait on.

Next: [05_Database_Performance.md](05_Database_Performance.md)
