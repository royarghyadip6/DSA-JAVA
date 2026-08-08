# 53. Top 50 Must-Master Questions (Recap)

Interview rapid-fire recap (Java 5–8 YOE). Short answers only — not deep dives.

---

## 1. HashMap internal working

<details>
<summary>Show Answer</summary>

**Java 8+:** Array of buckets. `hash(key)` → index. Collision: **linked list**; if chain > 8 and table ≥ 64 → **red-black tree**. Load factor 0.75 → resize (double capacity, rehash).

| Step | What happens |
|------|----------------|
| `put` | Compute hash → bucket index → add/update node |
| `get` | Same hash → walk list/tree |
| Resize | 2× array; re-distribute nodes |

```java
Map<String, Integer> map = new HashMap<>();
map.put("a", 1);  // hash → bucket → node
```

**One-liner:** Array of buckets + list/tree on collision; resize at 0.75 load factor.

</details>

---

## 2. ConcurrentHashMap internal working

<details>
<summary>Show Answer</summary>

**Java 8+:** Array of bins. **Reads** mostly lock-free. **Writes** lock **only the bin** (synchronized on first node) or CAS into empty bin. Long chains **treeify**. Size via striped `CounterCell`s — not one global lock.

| | `HashMap` | `ConcurrentHashMap` |
|---|-----------|---------------------|
| Threads | Not safe | Thread-safe |
| Null | Key/value OK | **No** nulls |
| Locking | N/A | Per-bin / CAS |

```java
map.putIfAbsent(key, value);  // atomic check-then-act
```

**One-liner:** CHM locks per bin (not whole map); reads mostly lock-free; no null key/value.

</details>

---

## 3. ArrayList internal structure

<details>
<summary>Show Answer</summary>

Backed by **dynamic `Object[]`**. `add` appends; when `size == capacity`, grow ~**1.5×** (copy to new array). Random access O(1); insert/delete middle O(n) due to shift.

| Op | Cost |
|----|------|
| `get(i)` | O(1) |
| `add(end)` | Amortized O(1) |
| `add(0, x)` / remove middle | O(n) |

```java
List<String> list = new ArrayList<>(16);  // initial capacity hint
```

**One-liner:** Resizable array; fast index access; costly middle insert/remove.

</details>

---

## 4. String immutability

<details>
<summary>Show Answer</summary>

`String` is **final**; internal `char[]`/`byte[]` not exposed for mutation. Every "change" creates a **new** object (`concat`, `replace`, `+`).

**Why:** Thread safety, hash cache, string pool safety, security (params, class names).

```java
String s = "hi";
s = s + "!";  // new String object; old "hi" may stay in pool
```

**One-liner:** Strings never mutate — operations return new objects; enables pool and safe sharing.

</details>

---

## 5. equals() and hashCode()

<details>
<summary>Show Answer</summary>

**Contract (for hash-based collections):**
- If `a.equals(b)` → `a.hashCode() == b.hashCode()`
- Consistent `equals`; reflexive, symmetric, transitive
- **Override both together** or break `HashMap`/`HashSet`

```java
@Override public boolean equals(Object o) { /* same class, same fields */ }
@Override public int hashCode() { return Objects.hash(field1, field2); }
```

**One-liner:** Equal objects must have equal hash codes — always override `equals` and `hashCode` together.

</details>

---

## 6. Comparable vs Comparator

<details>
<summary>Show Answer</summary>

| | `Comparable<T>` | `Comparator<T>` |
|---|-----------------|---------------|
| Where | Inside class (`compareTo`) | External (`compare`) |
| Method | `compareTo` | `compare` |
| Sort | Natural order | Custom / multiple orders |
| Package | `java.lang` | `java.util` |

```java
list.sort(Comparator.comparing(Person::getAge).reversed());
```

**One-liner:** `Comparable` = natural order in class; `Comparator` = flexible external sorting.

</details>

---

## 7. Generics and Type Erasure

<details>
<summary>Show Answer</summary>

Generics exist at **compile time** only. Compiler erases type params to **bounds** (usually `Object` or bound class). No `new T()`, no `instanceof List<String>` at runtime.

```java
List<String> list = new ArrayList<>();
// bytecode sees List / Object; cast inserted on get
```

**One-liner:** Generics are compile-time checks; erased to raw types at runtime (type erasure).

</details>

---

## 8. Lambda internals

<details>
<summary>Show Answer</summary>

Lambda = syntactic sugar for a **functional interface** (single abstract method). Implemented via **`invokedynamic`** + `LambdaMetafactory` → generates a class or method handle at runtime (not anonymous inner class every time).

```java
Runnable r = () -> System.out.println("hi");  // SAM of Runnable
```

**One-liner:** Lambdas compile to invokedynamic + SAM adapter, not always a new anonymous class.

</details>

---

## 9. Stream API internals

<details>
<summary>Show Answer</summary>

Pipeline: **source** → intermediate ops (lazy) → **terminal** op (triggers execution). Uses **Spliterator** to traverse; may fuse ops; parallel streams use **ForkJoinPool**.

| Kind | Examples | Lazy? |
|------|----------|-------|
| Intermediate | `filter`, `map`, `sorted` | Yes |
| Terminal | `collect`, `forEach`, `reduce` | No — runs pipeline |

```java
list.stream().filter(x -> x > 0).map(String::valueOf).collect(Collectors.toList());
```

**One-liner:** Streams are lazy until terminal op; parallel uses common ForkJoinPool.

</details>

---

## 10. map vs flatMap

<details>
<summary>Show Answer</summary>

| | `map` | `flatMap` |
|---|-------|-----------|
| Transform | 1 → 1 | 1 → 0..many (flatten) |
| Return | `Stream<R>` per element | `Stream<R>` merged into one |
| Use | Simple mapping | Nested lists/Optionals/streams |

```java
// map: List<List<Integer>> stays nested
lists.stream().map(List::size);
// flatMap: flatten then map
lists.stream().flatMap(List::stream).map(x -> x * 2);
```

**One-liner:** `map` transforms each element; `flatMap` maps then flattens one level.

</details>

---

## 11. Optional

<details>
<summary>Show Answer</summary>

Container for **nullable value** — forces explicit handling. Prefer **return type** for "maybe absent"; avoid fields/parameters/lists of `Optional`.

| Method | Role |
|--------|------|
| `of` / `ofNullable` | Create |
| `orElse` / `orElseGet` | Default if empty |
| `map` / `flatMap` | Chain without null checks |
| `ifPresent` | Side effect |

```java
return findUser(id).map(User::getName).orElse("unknown");
```

**One-liner:** Use Optional for return values to avoid NPE — not for fields or method params.

</details>

---

## 12. JVM architecture

<details>
<summary>Show Answer</summary>

| Area | Role |
|------|------|
| **Class Loader** | Load `.class` bytes |
| **Runtime Data Areas** | Method area, heap, stacks, PC registers, native stack |
| **Execution Engine** | Interpreter + JIT (C1/C2) + GC |
| **JNI / Native** | Bridge to OS/native libs |

Bytecode → (interpret / JIT compile) → native code on CPU.

**One-liner:** JVM = class loading + memory areas + interpreter/JIT execution + GC.

</details>

---

## 13. Class loading

<details>
<summary>Show Answer</summary>

**Phases:** Loading → Linking (verify, prepare, resolve) → Initialization (run `<clinit>`).

**Delegation model:** Bootstrap → Platform → Application. Child asks parent first.

```java
Class<?> c = Class.forName("com.example.MyClass");  // triggers loading/init
```

**One-liner:** Parent-delegation loaders; loading links bytecode then runs static init once.

</details>

---

## 14. Heap vs Stack

<details>
<summary>Show Answer</summary>

| | **Stack** (per thread) | **Heap** (shared) |
|---|------------------------|-------------------|
| Holds | Frames, local refs, primitives | Objects, arrays |
| Size | Smaller, fixed-ish | Larger, GC-managed |
| Lifetime | Method call scope | Until GC collects |
| Error | `StackOverflowError` | `OutOfMemoryError` |

```java
int x = 5;           // primitive on stack
Object o = new Object();  // ref on stack, object on heap
```

**One-liner:** Stack = per-thread call frames; heap = shared objects collected by GC.

</details>

---

## 15. Garbage Collection

<details>
<summary>Show Answer</summary>

GC reclaims **unreachable** heap objects. Generational: **Young** (Eden + Survivor) frequent minor GC; **Old** major/full GC. Roots: stacks, static refs, JNI, etc.

| Concept | Meaning |
|---------|---------|
| Minor GC | Young generation |
| Major/Full GC | Old gen (often stop-the-world) |
| Promotion | Long-lived → old gen |

**One-liner:** GC traces from roots; young gen collected often; old gen = heavier collections.

</details>

---

## 16. G1 GC

<details>
<summary>Show Answer</summary>

**G1** (Garbage-First): divides heap into **regions**. Tracks garbage per region; collects regions with most garbage first. Target pause times (`-XX:MaxGCPauseMillis`). Mixed GC for old + young regions. Default on Java 9+.

**One-liner:** G1 = region-based, pause-time-oriented collector; good default for large heaps.

</details>

---

## 17. Memory leaks

<details>
<summary>Show Answer</summary>

In Java: objects still **reachable** but no longer needed → GC never frees them.

| Common causes | Fix |
|---------------|-----|
| Static collections holding refs | Remove / weak refs |
| Unclosed resources | try-with-resources |
| `ThreadLocal` in pools | `remove()` in `finally` |
| Listeners not unregistered | Explicit remove |
| Cache without eviction | TTL / size limits |

**One-liner:** Leak = forgotten strong references — fix with cleanup, weak refs, or bounded caches.

</details>

---

## 18. Thread lifecycle

<details>
<summary>Show Answer</summary>

States: **NEW** → **RUNNABLE** → **BLOCKED** / **WAITING** / **TIMED_WAITING** → **TERMINATED**.

| State | Typical cause |
|-------|---------------|
| BLOCKED | Waiting for monitor lock |
| WAITING | `wait()`, `join()` (no timeout) |
| TIMED_WAITING | `sleep()`, `wait(timeout)` |

```java
Thread t = new Thread(task);
t.start();  // NEW → RUNNABLE
```

**One-liner:** Thread moves NEW → RUNNABLE → (blocked/waiting) → TERMINATED.

</details>

---

## 19. Synchronization

<details>
<summary>Show Answer</summary>

**`synchronized`** = mutual exclusion + visibility on same monitor. Reentrant — same thread can re-enter.

```java
synchronized (lock) {
    // only one thread at a time; unlock on exit (even on exception)
}
```

Static sync locks on **Class** object; instance sync locks on **`this`**.

**One-liner:** `synchronized` = one thread in monitor + happens-before on unlock/lock.

</details>

---

## 20. volatile

<details>
<summary>Show Answer</summary>

Guarantees **visibility** and **ordering** for reads/writes of one field. **Not** atomic for compound ops (`i++`).

```java
volatile boolean shutdown = false;  // writer thread sets; readers see immediately
```

**One-liner:** `volatile` = visibility + ordering for one field; not a mutex for read-modify-write.

</details>

---

## 21. Java Memory Model

<details>
<summary>Show Answer</summary>

JMM defines **when** writes by one thread are visible to another and what reorderings are allowed.

Without sync/`volatile`/atomics: stale reads and reordering possible.

**Happens-before** edges: monitor unlock→lock, volatile write→read, `start`/`join`, transitivity.

**One-liner:** JMM = cross-thread visibility and ordering rules; sync/volatile/atomics create guarantees.

</details>

---

## 22. CAS

<details>
<summary>Show Answer</summary>

**Compare-And-Swap:** atomically if value == expected, set to new; else fail. Basis of lock-free atomics. Retry on failure. **ABA problem** — use `AtomicStampedReference`.

```java
atomicRef.compareAndSet(expected, update);
```

**One-liner:** CAS = atomic check-then-set; powers lock-free structures (watch ABA).

</details>

---

## 23. AtomicInteger

<details>
<summary>Show Answer</summary>

Lock-free **atomic** int via CAS: `incrementAndGet`, `compareAndSet`, `addAndGet`. Prefer over `synchronized` for simple counters.

```java
AtomicInteger count = new AtomicInteger(0);
count.incrementAndGet();
```

**One-liner:** `AtomicInteger` = CAS-based lock-free counter; not for multi-field invariants.

</details>

---

## 24. ReentrantLock

<details>
<summary>Show Answer</summary>

Explicit `Lock` — must `unlock()` in `finally`. Extras: `tryLock`, fairness, `lockInterruptibly`, multiple `Condition`s.

```java
lock.lock();
try { /* critical */ } finally { lock.unlock(); }
```

**One-liner:** Use `ReentrantLock` when you need tryLock, fairness, or multiple conditions.

</details>

---

## 25. ExecutorService

<details>
<summary>Show Answer</summary>

Abstraction over thread pool — submit tasks without manual `new Thread`. Always **`shutdown()`** + `awaitTermination`.

```java
ExecutorService es = Executors.newFixedThreadPool(4);
es.submit(() -> doWork());
es.shutdown();
```

**One-liner:** ExecutorService manages threads for you — always shut it down when done.

</details>

---

## 26. ThreadPoolExecutor

<details>
<summary>Show Answer</summary>

Core knobs: **corePoolSize**, **maxPoolSize**, **queue**, **RejectedExecutionHandler**.

| Queue full + threads | Behavior |
|----------------------|----------|
| < core | Create thread |
| ≥ core, < max | Create up to max |
| At max | Reject per handler |

```java
new ThreadPoolExecutor(4, 8, 60L, SECONDS, new ArrayBlockingQueue<>(100));
```

**One-liner:** TPE = core/max threads + queue + rejection policy — tune all four.

</details>

---

## 27. Future

<details>
<summary>Show Answer</summary>

Represents async result from `submit(Callable)`. **`get()`** blocks; `cancel(mayInterrupt)` tries to stop.

```java
Future<Integer> f = executor.submit(() -> compute());
Integer result = f.get(5, TimeUnit.SECONDS);
```

**One-liner:** `Future` = blocking handle to async result; use `get` with timeout in production.

</details>

---

## 28. CompletableFuture

<details>
<summary>Show Answer</summary>

Non-blocking composition: `supplyAsync`, `thenApply`, `thenCompose`, `thenCombine`, `allOf`, `exceptionally`.

```java
CompletableFuture.supplyAsync(this::fetch)
    .thenCompose(id -> fetchDetail(id))
    .exceptionally(ex -> fallback);
```

**One-liner:** CompletableFuture = async pipeline with compose and error handling — not just blocking `get`.

</details>

---

## 29. ForkJoinPool

<details>
<summary>Show Answer</summary>

**Work stealing:** each worker has a deque; idle threads steal tasks from others. Used by **parallel streams** and default `supplyAsync` pool.

```java
ForkJoinPool.commonPool().invoke(new RecursiveTask<>() { ... });
```

**One-liner:** ForkJoinPool balances divide-and-conquer via work-stealing deques.

</details>

---

## 30. Concurrent Collections

<details>
<summary>Show Answer</summary>

| Class | Use |
|-------|-----|
| `ConcurrentHashMap` | Thread-safe map |
| `CopyOnWriteArrayList` | Read-heavy, rare writes |
| `BlockingQueue` impls | Producer-consumer |
| `ConcurrentLinkedQueue` | Lock-free queue |

Iterators often **weakly consistent** (no `ConcurrentModificationException`).

**One-liner:** Use concurrent collections instead of `Collections.synchronized*` + manual sync for scalability.

</details>

---

## 31. Producer Consumer

<details>
<summary>Show Answer</summary>

Producer adds; consumer removes; block when buffer full/empty. Production code: **`BlockingQueue`**.

```java
BlockingQueue<Task> q = new ArrayBlockingQueue<>(100);
q.put(task);    // blocks if full
Task t = q.take();  // blocks if empty
```

**One-liner:** `BlockingQueue` is the standard producer-consumer — `put`/`take` handle wait/notify.

</details>

---

## 32. ThreadLocal

<details>
<summary>Show Answer</summary>

Per-thread copy of a value (e.g. user context). Keys weak, **values strong** — leak risk in thread pools if not **`remove()`**.

```java
try { USER_CTX.set(ctx); } finally { USER_CTX.remove(); }
```

**One-liner:** ThreadLocal = per-thread variable; always `remove()` in pools/filters.

</details>

---

## 33. Exception handling

<details>
<summary>Show Answer</summary>

**Checked** — must catch/declare (`IOException`). **Unchecked** — `RuntimeException` + errors. Catch specific types; never swallow; use try-with-resources.

```java
try (InputStream in = open()) { read(in); }
catch (IOException e) { log.error("...", e); throw new ServiceException(e); }
```

**One-liner:** Catch specific exceptions, log with context, rethrow or wrap — don't empty catch blocks.

</details>

---

## 34. Custom exceptions

<details>
<summary>Show Answer</summary>

Extend `Exception` (checked) or `RuntimeException` (unchecked). Provide constructors with message + cause. Use domain-specific types for API clarity.

```java
public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String id) { super("Order not found: " + id); }
}
```

**One-liner:** Custom exceptions = meaningful API errors; include message and cause; pick checked vs unchecked deliberately.

</details>

---

## 35. Serialization

<details>
<summary>Show Answer</summary>

Object → byte stream (`Serializable`, `serialVersionUID`). **Security risk** — prefer JSON/Protobuf. `transient` skips fields; custom `readObject`/`writeObject` possible.

```java
class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private transient String password;  // not serialized
}
```

**One-liner:** Java serialization is brittle and unsafe — use JSON/Protobuf unless legacy requires it.

</details>

---

## 36. Reflection

<details>
<summary>Show Answer</summary>

Inspect/instantiate/call at runtime via `Class`, `Method`, `Field`. Powerful but breaks encapsulation, slower, no compile-time checks.

```java
Method m = clazz.getDeclaredMethod("foo");
m.setAccessible(true);
m.invoke(instance);
```

**One-liner:** Reflection = runtime introspection; use sparingly (frameworks, testing, tools).

</details>

---

## 37. Annotations

<details>
<summary>Show Answer</summary>

Metadata on code — processed at **compile** (`@Override`), **runtime** (Spring `@Autowired`), or **bytecode** (Lombok). Retention + `@Target` define lifecycle and placement.

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Audited { }
```

**One-liner:** Annotations attach metadata; effect depends on processor (compiler, runtime scanner, bytecode gen).

</details>

---

## 38. Immutable class

<details>
<summary>Show Answer</summary>

**Recipe:** `final` class, `final` fields, no setters, defensive copy in ctor/getter, don't leak mutable internals.

```java
public final class Money {
    private final BigDecimal amount;
    public Money(BigDecimal a) { this.amount = a; }
    public BigDecimal amount() { return amount; }
}
```

**One-liner:** Immutable = final class + final fields + no escape of mutable state.

</details>

---

## 39. Singleton

<details>
<summary>Show Answer</summary>

One instance app-wide. **Enum singleton** (Joshua Bloch) — serialization-safe. Or holder idiom / double-checked locking with `volatile`.

```java
public enum Config { INSTANCE;
    public String getUrl() { return "..."; }
}
```

**One-liner:** Prefer enum singleton; if lazy, use holder or DCL with `volatile`.

</details>

---

## 40. Factory Pattern

<details>
<summary>Show Answer</summary>

Encapsulates object creation — caller asks for interface, factory picks implementation (`Simple Factory`, `Factory Method`, `Abstract Factory`).

```java
PaymentProcessor p = PaymentFactory.create("UPI");
```

**One-liner:** Factory hides `new` and concrete types — caller depends on abstraction.

</details>

---

## 41. Builder Pattern

<details>
<summary>Show Answer</summary>

Step-by-step construction for many optional params. Avoid telescoping constructors. Immutable product.

```java
User user = User.builder().name("A").email("a@x.com").build();
```

**One-liner:** Builder = fluent construction when ctor has too many optional fields.

</details>

---

## 42. Strategy Pattern

<details>
<summary>Show Answer</summary>

Family of algorithms interchangeable at runtime — inject `Strategy` interface instead of `if/else` or `switch`.

```java
pricingService.setStrategy(new DiscountStrategy());
double price = pricingService.calculate(cart);
```

**One-liner:** Strategy = swap behavior via interface — Open/Closed in practice.

</details>

---

## 43. Observer Pattern

<details>
<summary>Show Answer</summary>

Subject notifies observers on state change. Java: `Observable`/`Observer` (deprecated) → prefer custom pub/sub, `PropertyChangeListener`, or reactive streams.

```java
orderSubject.register(listener);
orderSubject.notifyObservers(orderPlacedEvent);
```

**One-liner:** Observer = publish state changes to interested listeners decoupled from subject.

</details>

---

## 44. SOLID Principles

<details>
<summary>Show Answer</summary>

| Letter | Principle |
|--------|-----------|
| **S** | Single Responsibility — one reason to change |
| **O** | Open/Closed — extend without modifying |
| **L** | Liskov Substitution — subtypes honor contract |
| **I** | Interface Segregation — small focused interfaces |
| **D** | Dependency Inversion — depend on abstractions |

**One-liner:** SOLID = small classes, extend via new code, safe subtyping, lean interfaces, inject abstractions.

</details>

---

## 45. Spring IoC concepts

<details>
<summary>Show Answer</summary>

**IoC** — framework creates/manages objects. **DI** — dependencies injected (constructor preferred). Container = `ApplicationContext` holds **beans** and wires them.

```java
@Service
public class OrderService {
    private final OrderRepo repo;
    public OrderService(OrderRepo repo) { this.repo = repo; }  // constructor DI
}
```

**One-liner:** Spring IoC container creates beans and injects dependencies — you don't `new` collaborators.

</details>

---

## 46. Spring Bean lifecycle

<details>
<summary>Show Answer</summary>

Instantiate → populate properties → `BeanNameAware` / `BeanFactoryAware` → `@PostConstruct` / `InitializingBean` → bean ready → `@PreDestroy` / `DisposableBean` on shutdown.

```java
@PostConstruct void init() { }
@PreDestroy void cleanup() { }
```

**One-liner:** Bean lifecycle = create → inject → init callbacks → use → destroy callbacks.

</details>

---

## 47. Spring Boot internals

<details>
<summary>Show Answer</summary>

**Auto-configuration** via `@EnableAutoConfiguration` + `spring.factories` / `AutoConfiguration.imports` — conditional beans (`@ConditionalOnClass`, etc.). **Embedded server** (Tomcat). **Starter** deps bundle transitives. `SpringApplication.run` bootstraps context.

**One-liner:** Boot = opinionated auto-config + embedded server + starters on top of Spring Framework.

</details>

---

## 48. Microservices basics

<details>
<summary>Show Answer</summary>

Small **independently deployable** services around business capability. Own DB (ideally). Communicate via REST/events. Trade-offs: agility vs distributed complexity (network, consistency, observability).

| Plus | Minus |
|------|-------|
| Independent scale/deploy | Distributed transactions harder |
| Team autonomy | Ops overhead |

**One-liner:** Microservices = small deployable services with bounded context — pay distributed-system tax.

</details>

---

## 49. REST API design

<details>
<summary>Show Answer</summary>

Resources as nouns + HTTP verbs. Proper status codes (200, 201, 400, 404, 409, 500). Versioning (`/v1`), pagination, HATEOAS optional. Idempotent: GET, PUT, DELETE; POST usually not.

```http
GET    /orders/{id}
POST   /orders
PUT    /orders/{id}
DELETE /orders/{id}
```

**One-liner:** REST = nouns + HTTP verbs + correct status codes + consistent versioning and errors.

</details>

---

## 50. Production troubleshooting (heap dump, thread dump, GC logs)

<details>
<summary>Show Answer</summary>

| Tool | When |
|------|------|
| **Thread dump** (`jstack`, `jcmd`) | Deadlocks, stuck threads, high CPU |
| **Heap dump** (`jmap`, `-XX:+HeapDumpOnOOM`) | Memory leak, OOM analysis (MAT, VisualVM) |
| **GC logs** (`-Xlog:gc*`) | Pause times, promotion failures, tuning |

```bash
jcmd <pid> Thread.print
jmap -dump:live,format=b,file=heap.hprof <pid>
```

**One-liner:** Thread dump for locks/CPU; heap dump for memory; GC logs for pause and allocation issues.

</details>

---

## Cheat Sheet — All 50

| # | Topic | Remember |
|---|--------|----------|
| 1 | HashMap | Buckets + list/tree; resize 0.75 |
| 2 | ConcurrentHashMap | Per-bin lock/CAS; no nulls |
| 3 | ArrayList | Dynamic array; O(1) get |
| 4 | String immutability | New object on change; pool safe |
| 5 | equals/hashCode | Override both together |
| 6 | Comparable vs Comparator | Natural vs external sort |
| 7 | Generics / erasure | Compile-time only |
| 8 | Lambda | invokedynamic + SAM |
| 9 | Stream internals | Lazy until terminal; Spliterator |
| 10 | map vs flatMap | 1→1 vs flatten |
| 11 | Optional | Return type; not fields |
| 12 | JVM architecture | Loader + heap/stack + JIT + GC |
| 13 | Class loading | Parent delegation; init once |
| 14 | Heap vs Stack | Objects vs frames |
| 15 | GC | Young/old; reachability |
| 16 | G1 GC | Regions; pause-time goal |
| 17 | Memory leaks | Forgotten strong refs |
| 18 | Thread lifecycle | NEW→RUNNABLE→…→TERMINATED |
| 19 | Synchronization | Monitor + visibility |
| 20 | volatile | Visibility; not i++ |
| 21 | JMM | Cross-thread visibility rules |
| 22 | CAS | Lock-free atomic update |
| 23 | AtomicInteger | CAS counter |
| 24 | ReentrantLock | tryLock / Conditions |
| 25 | ExecutorService | Pool tasks; shutdown |
| 26 | ThreadPoolExecutor | core/max/queue/reject |
| 27 | Future | Blocking async result |
| 28 | CompletableFuture | Compose + callbacks |
| 29 | ForkJoinPool | Work stealing |
| 30 | Concurrent Collections | CHM, COW, BlockingQueue |
| 31 | Producer-Consumer | BlockingQueue |
| 32 | ThreadLocal | remove() in pools |
| 33 | Exception handling | Specific catch; try-with-resources |
| 34 | Custom exceptions | Message + cause |
| 35 | Serialization | Prefer JSON; transient |
| 36 | Reflection | Runtime introspection |
| 37 | Annotations | Metadata + processors |
| 38 | Immutable class | final + no leak |
| 39 | Singleton | Enum or holder |
| 40 | Factory | Hide concrete creation |
| 41 | Builder | Many optional params |
| 42 | Strategy | Pluggable algorithms |
| 43 | Observer | Notify on change |
| 44 | SOLID | S O L I D |
| 45 | Spring IoC | Container + DI |
| 46 | Bean lifecycle | PostConstruct → PreDestroy |
| 47 | Spring Boot | Auto-config + embedded server |
| 48 | Microservices | Small services; distributed cost |
| 49 | REST API | Nouns + verbs + status codes |
| 50 | Prod troubleshooting | jstack / heap / GC logs |

---

<details>
<summary>Interview One-Liner</summary>

**Top 50 in one breath:** HashMap buckets+tree; CHM per-bin; ArrayList array; immutable String; equals+hashCode together; Comparable vs Comparator; generics erased; lambda invokedynamic; lazy streams; flatMap flattens; Optional for returns; JVM loader+JIT+GC; class loaders delegate; heap objects stack frames; generational GC; G1 regions; leaks = strong refs; thread states; synchronized monitor; volatile visibility; JMM happens-before; CAS atomics; AtomicInteger counters; ReentrantLock extras; shutdown executors; TPE core/max/queue; Future blocks; CF composes; ForkJoin steals; concurrent collections; BlockingQueue P-C; ThreadLocal.remove; catch specific; custom exceptions; avoid Java serialization; reflection sparingly; annotation processors; immutable final fields; enum singleton; factory hides new; builder for options; strategy swaps algo; observer notifies; SOLID; Spring IoC+DI; bean init/destroy; Boot auto-config; microservices trade-offs; REST nouns+verbs; jstack/jmap/GC logs for prod.

</details>
