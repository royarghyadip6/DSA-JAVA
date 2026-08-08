# 48. JVM Advanced Questions

## 48. JVM Advanced Questions

Advanced JVM topics beyond basics — complements `009_JVM_Architecture.md`.

## Class Loading

---

# 1. Explain complete class loading process.

<details>
<summary>Show Answer</summary>

**Answer:**

```text
.class file
    ↓ Loading    — read bytecode, create Class object
    ↓ Linking    — Verification → Preparation → Resolution
    ↓ Initialization — static fields + static blocks
    ↓ Ready for use
```

```java
class Employee {
    static int count = 10;
    static { System.out.println("init"); }
}
// Loading: Employee.class loaded
// Linking: verify, allocate static field memory, resolve refs
// Initialization: count=10, static block runs
```

**Interview Point:** Loading → Linking (Verify, Prepare, Resolve) → Initialization. Static blocks run only in Initialization.

</details>

---

# 2. Loading?

<details>
<summary>Show Answer</summary>

**Answer:**

**Loading** = read `.class` binary, create `Class` object in Metaspace, store method/field metadata.

```text
Find class → read bytes → define Class object → parent delegation first
```

**Interview Point:** Loading creates Class metadata — does NOT run static blocks yet.

</details>

---

# 3. Linking?

<details>
<summary>Show Answer</summary>

**Answer:**

**Linking** = verify bytecode + prepare static field memory + resolve symbolic references.

| Sub-phase | Job |
|-----------|-----|
| Verification | Bytecode safety check |
| Preparation | Allocate static fields (default values) |
| Resolution | Symbolic refs → direct refs (can be lazy) |

**Interview Point:** Linking has three sub-phases. Preparation sets static defaults (0, null).

</details>

---

# 4. Initialization?

<details>
<summary>Show Answer</summary>

**Answer:**

**Initialization** = execute static variable assignments and **static blocks** — runs once per class when first used.

```java
static int x = compute(); // runs here
static { ... }           // runs here
```

Triggers: `new`, static field access, static method call, reflection, subclass init triggers parent init.

**Interview Point:** Initialization = static blocks execute. Once per class loader.

</details>

---

## Advanced

---

# 5. Verification?

<details>
<summary>Show Answer</summary>

**Answer:**

Bytecode **Verifier** checks: valid opcodes, no stack overflow, type safety, no illegal memory access.

```text
.class → Verifier → pass/fail (SecurityException)
```

Prevents malicious/corrupt bytecode from crashing JVM.

**Interview Point:** Verification = security gate before execution. Part of Linking.

</details>

---

# 6. Preparation?

<details>
<summary>Show Answer</summary>

**Answer:**

**Preparation** allocates memory for **static fields** and sets **default values** (0, false, null) — not explicit initializer values yet.

```java
static int count = 100;  // Preparation: count = 0
                         // Initialization: count = 100
```

**Interview Point:** Preparation = default values. Initialization = actual assigned values + static blocks.

</details>

---

# 7. Resolution?

<details>
<summary>Show Answer</summary>

**Answer:**

**Resolution** converts **symbolic references** in constant pool to **direct memory references**.

```text
"Employee" (symbol) → actual Class/method/field address
```

Can be **lazy** — resolved when first used, not necessarily at link time.

**Interview Point:** Resolution = symbolic to direct refs. Often lazy at first use.

</details>

---

## Runtime Questions

---

# 8. JIT Compiler?

<details>
<summary>Show Answer</summary>

**Answer:**

**JIT (Just-In-Time)** compiles **hot bytecode** to **native machine code** at runtime — much faster than interpreting every time.

```text
Cold code → Interpreter (slow)
Hot code  → JIT compiles → native code (fast, cached)
```

HotSpot tracks method call counts — "hot" methods get compiled.

**Interview Point:** JIT = compile hot paths to native code. Key to Java performance.

</details>

---

# 9. Interpreter vs JIT?

<details>
<summary>Show Answer</summary>

**Answer:**

| | Interpreter | JIT |
|---|-------------|-----|
| Speed | Slow — interprets each bytecode | Fast — native machine code |
| Startup | Fast — no compile wait | Slower warmup |
| Memory | Lower | Code cache for native code |
| Use | Cold/new code | Frequently executed code |

```text
Both work together in HotSpot JVM
```

**Interview Point:** Interpreter for startup; JIT for hot code. Tiered compilation uses both.

</details>

---

# 10. HotSpot JVM?

<details>
<summary>Show Answer</summary>

**Answer:**

**HotSpot** is Oracle/OpenJDK's default JVM — identifies **hot spots** (frequently executed code) and JIT-compiles them.

```text
Features: HotSpot detection, generational GC, tiered compilation,
          compressed oops, G1/ZGC collectors
```

Default JVM for Java 8+ (replaced older client/server JVM split).

**Interview Point:** HotSpot = default JVM. Name from "hot spot" code optimization.

</details>

---

# 11. Tiered Compilation?

<details>
<summary>Show Answer</summary>

**Answer:**

**Tiered compilation** uses **multiple JIT levels** — start interpreted, then C1 (fast compile), then C2 (aggressive optimize) for hottest code.

```text
Level 0: Interpreter
Level 1-3: C1 compiler (quick, less optimization)
Level 4:   C2 compiler (slow compile, max optimization)
```

`-XX:TieredStopAtLevel` controls max tier. Balances startup time vs peak performance.

**Interview Point:** Tiered = interpreter → C1 → C2. Better startup than C2-only.

</details>

---

## Memory Questions

---

# 12. Metaspace vs PermGen?

<details>
<summary>Show Answer</summary>

**Answer:**

| | PermGen (Java 7) | Metaspace (Java 8+) |
|---|------------------|---------------------|
| Location | Heap (fixed size) | Native memory |
| Stores | Class metadata | Class metadata |
| OOM | PermGen Space | Metaspace |
| Size | `-XX:MaxPermSize` | `-XX:MaxMetaspaceSize` (default unlimited) |

**Interview Point:** Metaspace replaced PermGen — native memory, grows dynamically. Class metadata not in heap.

</details>

---

# 13. Memory leaks?

<details>
<summary>Show Answer</summary>

**Answer:**

**Memory leak** = objects **no longer needed** but still **referenced** → GC cannot collect → heap grows → OOM.

| Common Causes | Fix |
|---------------|-----|
| Static collections growing | Remove entries, bounded cache |
| ThreadLocal not removed | `remove()` in finally |
| Unclosed connections | try-with-resources |
| Listener not deregistered | Remove on destroy |
| Custom cache no eviction | TTL/LRU limit |

```java
// Leak: static map holds all users forever
static Map<String, User> cache = new HashMap<>();
```

**Interview Point:** Leak = unreachable logically but still referenced. ThreadLocal + static collections = top causes.

</details>

---

# 14. Heap Dump?

<details>
<summary>Show Answer</summary>

**Answer:**

**Heap dump** = snapshot of all objects on heap at a point in time — for finding memory leaks and large objects.

```bash
# Generate heap dump
jmap -dump:live,format=b,file=heap.hprof <pid>

# Or on OOM automatically
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=/logs/heap.hprof
```

**Analyze with:** Eclipse MAT, VisualVM, jhat — dominator tree, leak suspects.

**Interview Point:** Heap dump for OOM investigation. MAT dominator tree finds leak roots.

</details>

---

# 15. Thread Dump?

<details>
<summary>Show Answer</summary>

**Answer:**

**Thread dump** = snapshot of all threads — state, stack trace, locks held/waiting.

```bash
jstack <pid>           # thread dump
kill -3 <pid>          # Linux signal
jcmd <pid> Thread.print
```

**Use for:** Deadlocks, threads stuck, high CPU (find busy thread stack).

```text
"BLOCKED" threads → deadlock or lock contention
"WAITING" on pool → thread pool exhausted
```

**Interview Point:** Thread dump for deadlock and stuck threads. jstack + look for BLOCKED cycles.

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> Class load: Loading→Linking(Verify,Prepare,Resolve)→Init. JIT compiles hot code. HotSpot tiered compilation. Metaspace not PermGen. Heap dump = objects; thread dump = stacks/deadlocks.

</details>
