# 52. Coding & Scenario-Based Questions

## 52. Coding & Scenario-Based Questions (5–8 Years)

Hands-on coding and design scenarios for mid-to-senior Java interviews.

---

## Collections

---

# 1. Implement custom HashMap.

<details>
<summary>Show Answer</summary>

**Answer:**

Build a simplified `HashMap` using a **bucket array** + **linked list** for collisions. Core steps: `hash(key)` → bucket index → `put`/`get` with `equals()`.

### Layman Explanation

```text
Library with numbered shelves (buckets):
  hash("Java") → shelf 5
  put("Java", book) → place on shelf 5
  get("Java") → go to shelf 5 → find exact match with equals()
  Two books on same shelf? → linked list on that shelf
```

### Core Implementation

```java
public class MyHashMap<K, V> {

  private static final int DEFAULT_CAPACITY = 16;
  private static final float LOAD_FACTOR = 0.75f;

  private Node<K, V>[] table;
  private int size;

  @SuppressWarnings("unchecked")
  public MyHashMap() {
    table = new Node[DEFAULT_CAPACITY];
  }

  static class Node<K, V> {
    final K key;
    V value;
    Node<K, V> next;

    Node(K key, V value, Node<K, V> next) {
      this.key = key;
      this.value = value;
      this.next = next;
    }
  }

  private int index(K key) {
    return (key == null) ? 0 : Math.abs(key.hashCode()) % table.length;
  }

  public void put(K key, V value) {
    int idx = index(key);
    Node<K, V> head = table[idx];

    // Update existing key
    for (Node<K, V> node = head; node != null; node = node.next) {
      if (Objects.equals(node.key, key)) {
        node.value = value;
        return;
      }
    }

    // Add new node at head of chain
    table[idx] = new Node<>(key, value, head);
    size++;

    if (size > table.length * LOAD_FACTOR) {
      resize();
    }
  }

  public V get(K key) {
    int idx = index(key);
    for (Node<K, V> node = table[idx]; node != null; node = node.next) {
      if (Objects.equals(node.key, key)) {
        return node.value;
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  private void resize() {
    Node<K, V>[] old = table;
    table = new Node[old.length * 2];
    size = 0;

    for (Node<K, V> head : old) {
      for (Node<K, V> node = head; node != null; node = node.next) {
        put(node.key, node.value);
      }
    }
  }
}
```

### Key Concepts

| Step | What Happens |
|------|--------------|
| `hashCode()` | Spread bits → bucket index |
| Collision | Linked list at bucket (Java 8+ uses tree if chain > 8) |
| `equals()` | Find exact key in chain |
| Resize | Double capacity when load factor 0.75 exceeded |

**Interview Point:**

> Custom HashMap = array of buckets + collision chain. `hashCode()` for index, `equals()` for match. Resize at 0.75 load factor. Real `HashMap` also treeifies long chains.

</details>

---

# 2. Implement LRU Cache.

<details>
<summary>Show Answer</summary>

**Answer:**

An **LRU (Least Recently Used) cache** evicts the oldest unused entry when full. Classic interview solution: `LinkedHashMap` with **access-order** + `removeEldestEntry()`.

### Layman Explanation

```text
Bookshelf holds only 3 books:
  Read book A → A moves to "most recent" side
  Add book D → remove least recently used book (B)
```

### LinkedHashMap Solution

```java
public class LruCache<K, V> extends LinkedHashMap<K, V> {
  private final int maxSize;

  public LruCache(int maxSize) {
    super(maxSize, 0.75f, true); // accessOrder = true → LRU tracking
    this.maxSize = maxSize;
  }

  @Override
  protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
    return size() > maxSize; // evict head (least recently used)
  }
}

// Usage
LruCache<String, String> cache = new LruCache<>(3);
cache.put("A", "1");
cache.put("B", "2");
cache.put("C", "3");
cache.get("A");       // A becomes most recent
cache.put("D", "4");  // B evicted
```

### Thread-Safe Wrapper

```java
Map<String, User> cache = Collections.synchronizedMap(new LruCache<>(1000));
// Production: use Caffeine
```

### Manual LRU — HashMap + Doubly Linked List

```text
HashMap<key, Node>  → O(1) lookup
Doubly linked list  → O(1) move to tail on access, remove head on eviction
```

**Interview Point:**

> LRU = `LinkedHashMap(accessOrder=true)` + `removeEldestEntry()`. Know manual version: HashMap + doubly linked list. Production: Caffeine or Guava Cache.

</details>

---

# 3. Find duplicates efficiently.

<details>
<summary>Show Answer</summary>

**Answer:**

Find elements appearing **more than once** using `HashSet` tracking or `groupingBy` + `counting()`.

### Solution 1 — groupingBy (Streams)

```java
List<Integer> numbers = Arrays.asList(1, 2, 3, 2, 4, 3, 5, 1);

List<Integer> duplicates = numbers.stream()
    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
    .entrySet().stream()
    .filter(e -> e.getValue() > 1)
    .map(Map.Entry::getKey)
    .collect(Collectors.toList());
// [1, 2, 3]
```

### Solution 2 — Set Tracking (O(n))

```java
Set<Integer> seen = new HashSet<>();
List<Integer> duplicates = numbers.stream()
    .filter(n -> !seen.add(n))  // add returns false if already present
    .distinct()
    .collect(Collectors.toList());
```

### For Large Data — HashSet Only

```java
Set<Integer> seen = new HashSet<>();
Set<Integer> dups = new HashSet<>();
for (int n : numbers) {
  if (!seen.add(n)) dups.add(n);
}
```

**Interview Point:**

> `groupingBy` + `counting()` then filter count > 1. Faster one-pass: `Set.add()` returns false for duplicates. O(n) time, O(n) space.

</details>

---

# 4. Top K frequent elements.

<details>
<summary>Show Answer</summary>

**Answer:**

Count frequencies with `groupingBy`, then sort by count descending and `limit(k)`.

### Solution — Streams

```java
List<Integer> list = Arrays.asList(1, 1, 1, 2, 2, 3, 3, 3, 3, 4);
int k = 2;

Map<Integer, Long> freqMap = list.stream()
    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

List<Integer> topK = freqMap.entrySet().stream()
    .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
    .limit(k)
    .map(Map.Entry::getKey)
    .collect(Collectors.toList());
// [3, 1]
```

### Solution — PriorityQueue (Better for Large n, Small k)

```java
public List<Integer> topKFrequent(int[] nums, int k) {
  Map<Integer, Integer> freq = new HashMap<>();
  for (int n : nums) freq.merge(n, 1, Integer::sum);

  PriorityQueue<Map.Entry<Integer, Integer>> minHeap =
      new PriorityQueue<>(Comparator.comparingInt(Map.Entry::getValue));

  for (Map.Entry<Integer, Integer> e : freq.entrySet()) {
    minHeap.offer(e);
    if (minHeap.size() > k) minHeap.poll(); // keep k largest
  }

  return minHeap.stream().map(Map.Entry::getKey).collect(Collectors.toList());
}
```

### Complexity

| Approach | Time | Best When |
|----------|------|-----------|
| Sort all entries | O(n log n) | Small lists, interview simplicity |
| Min-heap size k | O(n log k) | Large n, small k |

**Interview Point:**

> Count with `groupingBy`, sort by value desc, `limit(k)`. Optimal: min-heap of size k → O(n log k). Mention Bucket Sort for O(n) when values bounded.

</details>

---

# 5. Group employees by department.

<details>
<summary>Show Answer</summary>

**Answer:**

Use `Collectors.groupingBy(Employee::getDepartment)` to build `Map<String, List<Employee>>`.

### Basic Grouping

```java
record Employee(String name, String department, int salary) {}

List<Employee> employees = List.of(
    new Employee("Alice", "IT", 80000),
    new Employee("Bob", "HR", 60000),
    new Employee("Carol", "IT", 90000)
);

Map<String, List<Employee>> byDept = employees.stream()
    .collect(Collectors.groupingBy(Employee::department));
// IT → [Alice, Carol], HR → [Bob]
```

### Count per Department

```java
Map<String, Long> countByDept = employees.stream()
    .collect(Collectors.groupingBy(
        Employee::department,
        Collectors.counting()));
// IT → 2, HR → 1
```

### Average Salary per Department

```java
Map<String, Double> avgSalary = employees.stream()
    .collect(Collectors.groupingBy(
        Employee::department,
        Collectors.averagingInt(Employee::salary)));
```

### Names Only per Department

```java
Map<String, List<String>> namesByDept = employees.stream()
    .collect(Collectors.groupingBy(
        Employee::department,
        Collectors.mapping(Employee::name, Collectors.toList())));
```

**Interview Point:**

> `groupingBy(Employee::getDepartment)` → `Map<String, List<Employee>>`. Chain `counting()`, `averagingInt()`, or `mapping()` for richer aggregations.

</details>

---

## Multithreading

---

# 6. Producer Consumer.

<details>
<summary>Show Answer</summary>

**Answer:**

**Producers** put items in a **shared buffer**; **consumers** take and process them. Must handle full buffer, empty buffer, and thread safety.

### Layman Explanation

```text
Bakery:
  Chef (producer) → puts cakes on shelf (buffer)
  Customer (consumer) → takes cake from shelf
  Shelf full → chef waits
  Shelf empty → customer waits
```

### Solution — BlockingQueue (Production)

```java
BlockingQueue<String> queue = new ArrayBlockingQueue<>(10);

// Producer
new Thread(() -> {
  try {
    for (int i = 0; i < 100; i++) {
      queue.put("Item-" + i); // blocks when full
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
      String item = queue.take(); // blocks when empty
      System.out.println("Consumed: " + item);
    }
  } catch (InterruptedException e) {
    Thread.currentThread().interrupt();
  }
}).start();
```

### Solution — wait/notify (Classic)

```java
class SharedBuffer {
  private final Queue<String> queue = new LinkedList<>();
  private final int CAPACITY = 10;

  public synchronized void produce(String item) throws InterruptedException {
    while (queue.size() == CAPACITY) wait(); // full → wait
    queue.add(item);
    notifyAll();
  }

  public synchronized String consume() throws InterruptedException {
    while (queue.isEmpty()) wait(); // empty → wait
    String item = queue.remove();
    notifyAll();
    return item;
  }
}
```

**Interview Point:**

> Prefer `BlockingQueue` — handles wait/notify internally. `put()` blocks when full, `take()` when empty. Classic pattern: synchronized + `while` + `wait()` + `notifyAll()`.

</details>

---

# 7. Deadlock example and solution.

<details>
<summary>Show Answer</summary>

**Answer:**

**Deadlock** = two or more threads permanently blocked, each waiting for a lock held by another.

### Deadlock Example

```java
Object lockA = new Object();
Object lockB = new Object();

// Thread 1: A → B
new Thread(() -> {
  synchronized (lockA) {
    synchronized (lockB) { // waits for B held by T2
      System.out.println("T1 got both");
    }
  }
}).start();

// Thread 2: B → A (opposite order → deadlock!)
new Thread(() -> {
  synchronized (lockB) {
    synchronized (lockA) { // waits for A held by T1
      System.out.println("T2 got both");
    }
  }
}).start();
```

### Solution 1 — Lock Ordering (Best Fix)

```java
// Both threads acquire locks in SAME order: A then B
void safeWork() {
  synchronized (lockA) {
    synchronized (lockB) {
      doWork();
    }
  }
}
```

### Solution 2 — tryLock with Timeout

```java
ReentrantLock lockA = new ReentrantLock();
ReentrantLock lockB = new ReentrantLock();

if (lockA.tryLock(1, TimeUnit.SECONDS)) {
  try {
    if (lockB.tryLock(1, TimeUnit.SECONDS)) {
      try { doWork(); }
      finally { lockB.unlock(); }
    }
  } finally { lockA.unlock(); }
}
```

### Solution 3 — Consistent Ordering via hashCode

```java
void transfer(Account from, Account to, int amount) {
  Account first  = from.hashCode() < to.hashCode() ? from : to;
  Account second = from.hashCode() < to.hashCode() ? to : from;
  synchronized (first) {
    synchronized (second) {
      from.debit(amount);
      to.credit(amount);
    }
  }
}
```

### Coffman Conditions (All 4 Required)

| Condition | Fix |
|-----------|-----|
| Mutual Exclusion | Use concurrent utilities |
| Hold and Wait | Acquire all locks at once |
| No Preemption | `tryLock()` with timeout |
| Circular Wait | **Lock ordering** ✅ |

**Interview Point:**

> Deadlock = circular wait. Fix: **lock ordering** (always same global order). Detect: `jstack` or `ThreadMXBean.findDeadlockedThreads()`. Break any one Coffman condition to prevent.

</details>

---

# 8. Thread-safe Singleton.

<details>
<summary>Show Answer</summary>

**Answer:**

Ensure **only one instance** exists even when multiple threads call `getInstance()` simultaneously.

### Bill Pugh Holder Pattern (Recommended)

```java
public class DatabaseConfig {

  private DatabaseConfig() {}

  private static class Holder {
    private static final DatabaseConfig INSTANCE = new DatabaseConfig();
  }

  public static DatabaseConfig getInstance() {
    return Holder.INSTANCE; // JVM loads Holder only when called — thread-safe
  }
}
```

### Enum Singleton (Best Practice)

```java
public enum AppConfig {
  INSTANCE;

  private final String appName = "OrderService";

  public String getAppName() { return appName; }
}

// Usage
AppConfig.INSTANCE.getAppName();
```

### Double-Checked Locking

```java
public class Singleton {
  private static volatile Singleton instance;

  private Singleton() {}

  public static Singleton getInstance() {
    if (instance == null) {
      synchronized (Singleton.class) {
        if (instance == null) {
          instance = new Singleton();
        }
      }
    }
    return instance;
  }
}
```

`volatile` is **required** — without it, another thread may see a partially constructed object.

**Interview Point:**

> Enum singleton = best (serialization + reflection safe). Bill Pugh holder = best class-based. Double-checked locking needs `volatile`. Spring `@Service` = singleton by default — manual pattern rarely needed.

</details>

---

# 9. Rate Limiter.

<details>
<summary>Show Answer</summary>

**Answer:**

A **rate limiter** controls how many requests a user/API can make in a time window — prevents abuse and protects downstream systems.

### Layman Explanation

```text
Highway toll:
  Only 100 cars per minute allowed
  Car 101 must wait until next minute
```

### Token Bucket Implementation

```java
public class TokenBucketRateLimiter {
  private final long capacity;
  private final long refillRate; // tokens per second
  private long tokens;
  private long lastRefillTime;

  public TokenBucketRateLimiter(long capacity, long refillRatePerSec) {
    this.capacity = capacity;
    this.refillRate = refillRatePerSec;
    this.tokens = capacity;
    this.lastRefillTime = System.nanoTime();
  }

  public synchronized boolean tryAcquire() {
    refill();
    if (tokens > 0) {
      tokens--;
      return true;
    }
    return false; // rate limited
  }

  private void refill() {
    long now = System.nanoTime();
    long elapsed = now - lastRefillTime;
    long tokensToAdd = (elapsed * refillRate) / 1_000_000_000L;
    if (tokensToAdd > 0) {
      tokens = Math.min(capacity, tokens + tokensToAdd);
      lastRefillTime = now;
    }
  }
}
```

### Using Guava

```java
RateLimiter limiter = RateLimiter.create(100.0); // 100/sec
if (limiter.tryAcquire()) {
  processRequest();
} else {
  throw new RateLimitException("Too many requests"); // HTTP 429
}
```

### Distributed — Redis

```text
Key: rate:user:123:minute:1709123400
INCR key → if count > 100 → reject
EXPIRE key 60 seconds
```

**Interview Point:**

> Token bucket allows bursts; fixed window is simpler but has edge spikes. Distributed = Redis counter. Return HTTP 429 + Retry-After. Guava/Resilience4j in Java apps.

</details>

---

# 10. Parallel file processing.

<details>
<summary>Show Answer</summary>

**Answer:**

Process many files in parallel using `ExecutorService` + `Future` — controlled concurrency, per-file error handling, and timeouts.

### Layman Explanation

```text
1000 documents to scan:
  Don't hire 1000 workers
  Hire 8 workers (thread pool)
  Each picks next file from pile
```

### Process Directory of Files

```java
@Service
public class FileProcessorService {

  private final ExecutorService executor =
      new ThreadPoolExecutor(
          8, 8, 0L, TimeUnit.MILLISECONDS,
          new LinkedBlockingQueue<>(100),
          new ThreadPoolExecutor.CallerRunsPolicy()
      );

  public ProcessingResult processDirectory(Path dir) throws Exception {
    List<Path> files = Files.list(dir).filter(Files::isRegularFile).toList();
    List<Future<FileResult>> futures = new ArrayList<>();

    for (Path file : files) {
      futures.add(executor.submit(() -> processFile(file)));
    }

    int success = 0, failed = 0;
    List<String> errors = new ArrayList<>();

    for (Future<FileResult> future : futures) {
      try {
        FileResult result = future.get(30, TimeUnit.SECONDS);
        if (result.isSuccess()) success++;
        else { failed++; errors.add(result.getError()); }
      } catch (TimeoutException e) {
        future.cancel(true);
        failed++;
        errors.add("Timeout");
      }
    }
    return new ProcessingResult(success, failed, errors);
  }

  private FileResult processFile(Path file) {
    try {
      String content = Files.readString(file);
      saveToDatabase(file.getFileName().toString(), parse(content));
      return FileResult.success();
    } catch (Exception e) {
      return FileResult.failure(e.getMessage());
    }
  }
}
```

### Pool Size Guide

| Work Type | Pool Size |
|-----------|-----------|
| CPU-bound (parse, compress) | `cores` or `cores + 1` |
| I/O-bound (DB, network) | `cores * 2` to `cores * 4` |

**Interview Point:**

> `ExecutorService` + `Future` for parallel files. Bounded queue + `CallerRunsPolicy`. Per-task timeout via `future.get(timeout)`. Don't `readAllLines` on huge files — use `Files.lines()` stream.

</details>

---

## Java 8

---

# 11. Find second highest salary.

<details>
<summary>Show Answer</summary>

**Answer:**

Sort salaries descending, skip first, take next — always use `distinct()` to handle duplicate salaries.

### Solution

```java
List<Employee> employees = getEmployees();

Optional<Integer> secondHighest = employees.stream()
    .map(Employee::getSalary)
    .distinct()
    .sorted(Comparator.reverseOrder())
    .skip(1)
    .findFirst();

System.out.println(secondHighest.orElse(-1));
```

### Second Highest Employee Object

```java
Optional<Employee> secondEarner = employees.stream()
    .sorted(Comparator.comparingInt(Employee::getSalary).reversed())
    .skip(1)
    .findFirst();
```

### Alternative — limit + reduce

```java
Optional<Integer> second = employees.stream()
    .map(Employee::getSalary)
    .distinct()
    .sorted(Comparator.reverseOrder())
    .limit(2)
    .reduce((first, second) -> second);
```

**Interview Point:**

> `distinct()` → `sorted(reverseOrder())` → `skip(1)` → `findFirst()`. Always `distinct()` for duplicate salaries. k=2 pattern for second highest.

</details>

---

# 12. Find nth highest salary.

<details>
<summary>Show Answer</summary>

**Answer:**

Generalize second-highest: sort descending, `skip(n-1)`, `findFirst()`.

### General kth Highest

```java
public Optional<Integer> findKthHighestSalary(List<Employee> employees, int k) {
  return employees.stream()
      .map(Employee::getSalary)
      .distinct()
      .sorted(Comparator.reverseOrder())
      .skip(k - 1)
      .findFirst();
}

// 3rd highest
findKthHighestSalary(employees, 3);
```

### kth Highest Employee

```java
Optional<Employee> kthEmployee = employees.stream()
    .sorted(Comparator.comparingInt(Employee::getSalary).reversed())
    .skip(k - 1)
    .findFirst();
```

### Using limit + reduce

```java
Optional<Integer> kth = employees.stream()
    .map(Employee::getSalary)
    .distinct()
    .sorted(Comparator.reverseOrder())
    .limit(k)
    .reduce((a, b) -> b); // last of top k
```

**Interview Point:**

> `distinct()` → `sorted(reversed())` → `skip(k-1)` → `findFirst()`. k=2 for second highest. Handle duplicates with `distinct()`.

</details>

---

# 13. Count word frequency.

<details>
<summary>Show Answer</summary>

**Answer:**

Use `Collectors.groupingBy` with `Collectors.counting()` to build a frequency map.

### Solution

```java
List<String> words = Arrays.asList("apple", "banana", "apple", "cherry", "banana", "apple");

Map<String, Long> frequency = words.stream()
    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
// {apple=3, banana=2, cherry=1}
```

### From a Sentence

```java
String sentence = "java is great and java is popular";
Map<String, Long> wordFreq = Arrays.stream(sentence.split("\\s+"))
    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
```

### Sorted by Frequency

```java
Map<String, Long> sortedFreq = words.stream()
    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
    .entrySet().stream()
    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
    .collect(Collectors.toMap(
        Map.Entry::getKey, Map.Entry::getValue,
        (a, b) -> a, LinkedHashMap::new));
```

**Interview Point:**

> `collect(groupingBy(identity(), counting()))` — standard frequency map. Sort entries by value for top words.

</details>

---

# 14. Find duplicate characters.

<details>
<summary>Show Answer</summary>

**Answer:**

Count character frequencies, then filter characters with count > 1.

### Solution — Streams

```java
String str = "programming";

Map<Character, Long> freq = str.chars()
    .mapToObj(c -> (char) c)
    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

List<Character> duplicates = freq.entrySet().stream()
    .filter(e -> e.getValue() > 1)
    .map(Map.Entry::getKey)
    .collect(Collectors.toList());
// [r, g, m]
```

### Solution — HashSet One-Pass

```java
Set<Character> seen = new HashSet<>();
String duplicates = str.chars()
    .mapToObj(c -> (char) c)
    .filter(c -> !seen.add(c))
    .distinct()
    .map(String::valueOf)
    .collect(Collectors.joining(", "));
// "r, g, m"
```

### Classic Loop

```java
Map<Character, Integer> map = new HashMap<>();
for (char ch : str.toCharArray()) {
  map.put(ch, map.getOrDefault(ch, 0) + 1);
}
map.forEach((ch, count) -> {
  if (count > 1) System.out.println(ch);
});
```

**Interview Point:**

> Build frequency map with `groupingBy` + `counting()`, filter count > 1. Or `Set.add()` returns false for duplicate chars.

</details>

---

# 15. Flatten nested collections.

<details>
<summary>Show Answer</summary>

**Answer:**

Use `flatMap` to flatten `List<List<T>>` into `List<T>`.

### Flatten List of Lists

```java
List<List<Integer>> nested = Arrays.asList(
    Arrays.asList(1, 2),
    Arrays.asList(3, 4),
    Arrays.asList(5)
);

List<Integer> flat = nested.stream()
    .flatMap(List::stream)
    .collect(Collectors.toList());
// [1, 2, 3, 4, 5]
```

### Flatten List of Optional

```java
List<Optional<String>> optionals = List.of(
    Optional.of("a"), Optional.empty(), Optional.of("b"));

List<String> present = optionals.stream()
    .flatMap(Optional::stream) // Java 9+
    .collect(Collectors.toList());
// [a, b]
```

### Flatten Nested Streams

```java
List<String> sentences = List.of("Hello World", "Java Streams");
List<String> words = sentences.stream()
    .flatMap(s -> Arrays.stream(s.split("\\s+")))
    .collect(Collectors.toList());
// [Hello, World, Java, Streams]
```

### Deep Nested Lists

```java
// Recursive flatten for arbitrary depth
public static <T> Stream<T> flatten(List<?> list) {
  return list.stream().flatMap(item ->
      item instanceof List<?> nested
          ? flatten(nested)
          : Stream.of((T) item));
}
```

**Interview Point:**

> `flatMap(List::stream)` for `List<List<T>>`. `map` = 1-to-1 transform; `flatMap` = flatten nested structures. Think SQL JOIN vs UNNEST.

</details>

---

## Design

---

# 16. Design Parking Lot.

<details>
<summary>Show Answer</summary>

**Answer:**

Model vehicle types, parking spots, tickets, and a central `ParkingLot` that assigns spots and calculates fees.

### Layman Explanation

```text
Mall parking:
  Car → compact spot
  Truck → large spot
  Motorcycle → any spot
  Entry gate issues ticket, exit gate calculates fee
```

### Core Classes

```java
enum VehicleType { MOTORCYCLE, CAR, TRUCK }
enum SpotType    { COMPACT, LARGE, HANDICAPPED }

class Vehicle {
  private final String plate;
  private final VehicleType type;
}

class ParkingSpot {
  private final int id;
  private final SpotType type;
  private Vehicle parkedVehicle;
  boolean isAvailable() { return parkedVehicle == null; }
}

class ParkingTicket {
  private final String ticketId;
  private final Vehicle vehicle;
  private final ParkingSpot spot;
  private final LocalDateTime entryTime;
}

class ParkingLot {
  private final Map<SpotType, Queue<ParkingSpot>> availableSpots;
  private final Map<String, ParkingTicket> activeTickets;

  public synchronized Optional<ParkingTicket> park(Vehicle vehicle) {
    SpotType needed = spotFor(vehicle.getType());
    ParkingSpot spot = availableSpots.get(needed).poll();
    if (spot == null) return Optional.empty(); // full

    ParkingTicket ticket = new ParkingTicket(vehicle, spot);
    spot.park(vehicle);
    activeTickets.put(ticket.getId(), ticket);
    return Optional.of(ticket);
  }

  public double exit(String ticketId) {
    ParkingTicket ticket = activeTickets.remove(ticketId);
    long hours = Duration.between(ticket.getEntryTime(), LocalDateTime.now()).toHours();
    releaseSpot(ticket.getSpot());
    return hours * hourlyRate(ticket.getVehicle().getType());
  }
}
```

### Design Decisions

| Concern | Approach |
|---------|----------|
| Spot assignment | Queue per spot type — O(1) allocate |
| Concurrency | `synchronized` on `park`/`exit` or `ReentrantLock` |
| Multi-floor | `ParkingFloor` contains spots; lot manages floors |
| Payment | Strategy pattern — hourly / flat rate |

**Interview Point:**

> Parking Lot = classic OOP design. Key classes: Vehicle, Spot, Ticket, ParkingLot. Use enums for types. Queue for available spots. Mention multi-floor extension and thread safety.

</details>

---

# 17. Design Notification Service.

<details>
<summary>Show Answer</summary>

**Answer:**

Send emails, SMS, push **asynchronously** — main request returns fast; notifications processed in background.

### Layman Explanation

```text
Restaurant:
  Waiter gives receipt immediately (main request done)
  SMS "order confirmed" sent later (async notification)
```

### Architecture

```text
  Order Service
       │ publish event (non-blocking)
       ▼
  [Message Queue / Kafka]
       ▼
  Notification Service
       ├── Email sender
       ├── SMS sender
       └── Push sender
```

### In-App — BlockingQueue + Workers

```java
@Service
public class NotificationService {

  private final BlockingQueue<Notification> queue =
      new LinkedBlockingQueue<>(5000);
  private final ExecutorService workers = Executors.newFixedThreadPool(4);

  @PostConstruct
  public void startWorkers() {
    for (int i = 0; i < 4; i++) {
      workers.submit(() -> {
        while (true) {
          try {
            Notification n = queue.take();
            send(n);
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            break;
          }
        }
      });
    }
  }

  public void notifyAsync(Notification notification) {
    if (!queue.offer(notification)) {
      log.error("Queue full — persist for retry: {}", notification);
    }
  }

  private void send(Notification n) {
    switch (n.getType()) {
      case EMAIL -> emailClient.send(n);
      case SMS   -> smsClient.send(n);
      case PUSH  -> pushClient.send(n);
    }
  }
}
```

### Spring @Async

```java
@Async("notificationExecutor")
public void sendOrderConfirmation(Order order) {
  emailClient.send(order.getUserEmail(), buildTemplate(order));
}
```

### Reliability Checklist

```text
✅ Idempotent sends (notification ID — no double-send)
✅ Retry with backoff (3 attempts)
✅ Dead letter queue for permanent failures
✅ Rate limit per channel (SMS provider limits)
✅ Don't block main transaction on notification failure
```

**Interview Point:**

> Async = queue or `@Async` or Kafka. Main request publishes event and returns. Workers send in background. Retry + dead letter for reliability. Idempotent notification IDs.

</details>

---

# 18. Design Payment Gateway.

<details>
<summary>Show Answer</summary>

**Answer:**

Abstract payment processing behind an interface — support multiple providers (Stripe, PayPal), handle idempotency, retries, and webhooks.

### Layman Explanation

```text
Checkout counter:
  Customer pays → gateway routes to Stripe or PayPal
  If Stripe fails → retry or fallback provider
  Receipt always matches one payment attempt (idempotent)
```

### Core Design

```java
interface PaymentGateway {
  PaymentResult charge(PaymentRequest request);
  PaymentResult refund(String transactionId, BigDecimal amount);
}

class StripeGateway implements PaymentGateway { ... }
class PayPalGateway implements PaymentGateway { ... }

@Service
class PaymentService {
  private final PaymentGateway gateway; // injected via @Primary or @Qualifier

  public PaymentResult processPayment(PaymentRequest req) {
    // Idempotency — same key = same result, no double charge
    if (idempotencyStore.exists(req.getIdempotencyKey())) {
      return idempotencyStore.get(req.getIdempotencyKey());
    }
    PaymentResult result = gateway.charge(req);
    idempotencyStore.save(req.getIdempotencyKey(), result);
    return result;
  }
}
```

### Architecture

```text
Client → Payment API → PaymentService
                          ├── Idempotency check (Redis/DB)
                          ├── PaymentGateway (Strategy pattern)
                          ├── Transaction log (audit)
                          └── Webhook handler (async confirmation)
```

### Key Concerns

| Concern | Solution |
|---------|----------|
| Double charge | Idempotency key per request |
| Provider failure | Retry + circuit breaker (Resilience4j) |
| Async confirmation | Webhook endpoint + signature verify |
| PCI compliance | Never store raw card — tokenize via provider |
| Reconciliation | Transaction log + daily settlement report |

**Interview Point:**

> Strategy pattern for multiple gateways. Idempotency key prevents double charge. Webhook for async status. Circuit breaker for failing provider. Never store raw card data.

</details>

---

# 19. Design URL Shortener.

<details>
<summary>Show Answer</summary>

**Answer:**

Map long URLs to short codes — fast redirect on read, encode/decode on write. Classic system design (bit.ly, TinyURL).

### Layman Explanation

```text
Long URL: https://example.com/very/long/path?page=1
Short:    https://short.ly/abc123
Click abc123 → lookup in DB → redirect to long URL
```

### Core Flow

```text
CREATE:  long URL → generate short code → store (code, longUrl) → return short URL
REDIRECT: short code → lookup → HTTP 302 redirect to long URL
```

### Encoding Strategies

| Strategy | Pros | Cons |
|----------|------|------|
| Base62 counter | Simple, unique | Predictable, single DB bottleneck |
| Hash (MD5/SHA) + truncate | Distributed | Collision risk — need retry |
| Random + DB check | Unpredictable | Extra DB lookup |

### Java Service Sketch

```java
@Service
public class UrlShortenerService {

  private static final String BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
  private final UrlRepository repo;

  public String shorten(String longUrl) {
    Optional<UrlMapping> existing = repo.findByLongUrl(longUrl);
    if (existing.isPresent()) return buildShort(existing.get().getCode());

    String code = generateUniqueCode();
    repo.save(new UrlMapping(code, longUrl, LocalDateTime.now()));
    return buildShort(code);
  }

  public String resolve(String code) {
    return repo.findByCode(code)
        .map(UrlMapping::getLongUrl)
        .orElseThrow(() -> new NotFoundException("URL not found"));
  }

  private String generateUniqueCode() {
  // Base62 encode of auto-increment ID, or random 7-char string
    return RandomStringUtils.randomAlphanumeric(7);
  }
}
```

### Scale Considerations

```text
Read-heavy (100:1):  Cache short code → long URL in Redis
DB:                  code (PK), long_url, created_at, expiry, click_count
Analytics:           Async click event → Kafka → counter
Custom alias:        User provides code — check uniqueness
Expiry:              TTL on Redis + scheduled DB cleanup
```

**Interview Point:**

> Shorten = generate unique code + store mapping. Redirect = cache-first lookup. Base62 encoding of counter is common. Redis cache for hot reads. Mention collision handling and analytics.

</details>

---

# 20. Design Inventory System.

<details>
<summary>Show Answer</summary>

**Answer:**

Track product stock with **atomic reserve/release** — prevent overselling in concurrent order processing.

### Layman Explanation

```text
Warehouse shelf:
  10 items in stock
  3 customers buy at same time → only 10 sold total
  System must not sell 11th item (overselling)
```

### Core Design

```java
@Entity
class Product {
  private String sku;
  private int availableStock;
  private int reservedStock; // held during checkout, not yet shipped
}

@Service
class InventoryService {

  // Atomic reserve — prevents overselling
  @Transactional
  public boolean reserve(String sku, int quantity) {
    int updated = jdbcTemplate.update(
        "UPDATE product SET available_stock = available_stock - ? " +
        "WHERE sku = ? AND available_stock >= ?",
        quantity, sku, quantity);
    return updated == 1; // 0 rows = insufficient stock
  }

  public void release(String sku, int quantity) {
    jdbcTemplate.update(
        "UPDATE product SET available_stock = available_stock + ? WHERE sku = ?",
        quantity, sku);
  }

  public void confirmShipment(String sku, int quantity) {
    jdbcTemplate.update(
        "UPDATE product SET reserved_stock = reserved_stock - ? WHERE sku = ?",
        quantity, sku);
  }
}
```

### Reserve Flow (Order Processing)

```text
1. Order placed → reserve(sku, qty)     — deduct available
2. Payment fails → release(sku, qty)    — restore stock
3. Payment OK → confirmShipment()       — move reserved → shipped
4. Cancel order → release(sku, qty)
```

### Concurrency Approaches

| Approach | When |
|----------|------|
| Optimistic locking (`@Version`) | Low contention |
| Pessimistic `SELECT FOR UPDATE` | High contention SKU |
| Atomic SQL `UPDATE ... WHERE stock >= ?` | ✅ Simple, effective |
| Redis decrement | High-throughput flash sales |

### Distributed Inventory

```text
Per-warehouse stock → aggregate view
Kafka events: StockReserved, StockReleased, StockDepleted
Saga pattern for multi-item orders across warehouses
```

**Interview Point:**

> Prevent overselling with atomic `UPDATE WHERE stock >= qty`. Reserve on order, release on cancel, confirm on ship. Optimistic locking for low contention; Redis for flash sales. Saga for distributed multi-warehouse.

</details>

---
