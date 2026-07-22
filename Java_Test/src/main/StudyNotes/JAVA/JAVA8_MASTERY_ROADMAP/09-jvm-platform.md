# Phase 9 — JVM & Platform Changes (Java 8)

**Duration:** 1 week | **Goal:** Understand runtime impact of Java 8 — memory, bytecode, security

[← Phase 8](08-nashorn-scripting.md) | [Index](README.md) | **Next:** [Phase 10 — Production Mastery →](10-production-mastery.md)

---

## Theory (Easy to Remember)

### 1. PermGen → Metaspace

| PermGen (Java 7 and earlier) | Metaspace (Java 8+) |
|------------------------------|---------------------|
| Fixed size in heap | Native memory (mostly) |
| `OutOfMemoryError: PermGen` | `OutOfMemoryError: Metaspace` |
| `-XX:PermSize`, `MaxPermSize` | `-XX:MaxMetaspaceSize` |

- **Class metadata** (class definitions, method info) moved to **Metaspace**.
- Grows automatically — still can OOM if classloader leaks (reload classes in app servers).

**Memory trick:** *"PermGen died. Metaspace lives in native land."*

---

### 2. How Lambdas Work (Bytecode Level)

- Lambdas are **not** anonymous inner classes at runtime (usually).
- Compiler uses **`invokedynamic`** + **`LambdaMetafactory.metafactory`**.
- Bootstrap method generates call site once; JVM links to actual method handle.
- Result: less bytecode, better performance than anonymous classes.

---

### 3. Compact Profiles (Java 8)

- **compact1, compact2, compact3** — subset JRE for embedded/IoT.
- Rare in modern deployments but tested in certification.
- Full JRE = all profiles combined.

---

### 4. Security & TLS (Java 8)

- Stronger default TLS algorithms.
- **SNI** (Server Name Indication) support in `SSLParameters`.
- `SNIServerName`, `SNIHostName`, `SNIMatcher` classes.
- Weak ciphers deprecated over time in 8uXX updates.

---

### 5. Unicode 6.2

- Updated character properties, normalization.
- Affects `Character`, regex, locale-sensitive operations.

---

### 6. Deprecations to Know

- `SecurityManager` — eventually deprecated (Java 17+).
- RMI over HTTP — deprecated.
- Static RMI stub generation — deprecated.
- `_` as identifier — warning in 8, illegal in 9.

---

### 7. HashMap Improvement (Java 8)

- Poor hash collision: linked list → **red-black tree** when bucket > 8 elements.
- Better worst-case O(log n) vs O(n) for bad hash codes.
- Relevant for security (hash collision DoS) and performance interviews.

---

## Examples

### Metaspace monitoring

```bash
# JVM flags
java -XX:MaxMetaspaceSize=256m -XX:+PrintGCDetails MyApp

# jcmd / VisualVM — watch Metaspace usage
jcmd <pid> VM.metaspace
```

### Lambda bytecode (conceptual)

```java
// Source
Runnable r = () -> System.out.println("Hi");

// Roughly becomes invokedynamic call to LambdaMetafactory
// NOT a new Runnable.class file per lambda
```

### SNI client (conceptual)

```java
SSLParameters params = sslSocket.getSSLParameters();
params.setServerNames(List.of(new SNIHostName("www.example.com")));
sslSocket.setSSLParameters(params);
```

---

## Practice Exercises

| # | Exercise | Difficulty |
|---|----------|------------|
| 1 | Run app with `-XX:MaxMetaspaceSize=32m` and trigger class load OOM | Medium |
| 2 | Use `javap -c` to inspect lambda bytecode vs anonymous class | Hard |
| 3 | List TLS protocols enabled by default in Java 8 vs your org policy | Medium |
| 4 | Explain classloader leak scenario in Tomcat redeploy | Hard |
| 5 | Document JVM flags for production Java 8 deployment | Medium |

---

## Interview Q&A (5–8 Years Experience)

### Q1. What changed from PermGen to Metaspace and why?

**Answer:** PermGen was fixed-size heap area for class metadata — frequent `OutOfMemoryError: PermGen space` in apps with many classes or hot redeploy. Metaspace uses **native memory**, auto-expands, `-XX:MaxMetaspaceSize` caps it. Classloader garbage collection can reclaim metaspace when loader is collected — fixes some leaks if loader itself is GC'd.

---

### Q2. How are lambda expressions implemented in JVM?

**Answer:** `invokedynamic` instruction at call site. Bootstrap method `LambdaMetafactory.metafactory` links to **method handle** of captured body. First call links; subsequent calls are near-direct. Not 1:1 inner class per lambda — reduces metaspace pressure.

---

### Q3. What causes Metaspace OOM?

**Answer:**
- Classloader leak (app server redeploy without releasing loader)
- Dynamic proxy/code generation (CGLIB, bytecode libs) unbounded
- `-XX:MaxMetaspaceSize` too low
- Not same as heap OOM — check native memory

---

### Q4. Java 8 HashMap treeification — when and why?

**Answer:** When single bucket chain length ≥ **8** (and table size ≥ 64), linked list converts to **red-black tree**. When ≤ 6, untreeify. Mitigates hash collision attacks and improves worst-case lookup. Requires `Comparable` keys or same class with comparison.

---

### Q5. What is invokedynamic beyond lambdas?

**Answer:** Also used for: dynamic languages on JVM (JRuby, Nashorn), `String.concat` optimizations, record patterns (later Java). Extensible bootstrap mechanism — JVM links call site at runtime instead of fixed bytecode target.

---

### Q6. Compact profiles — still relevant?

**Answer:** Mostly historical/embedded. Modern deployments use full JDK or **jlink** custom runtimes (Java 9+). Know concept for legacy certification; jlink replaced profiles for modular trimming.

---

### Q7. TLS changes impact on legacy clients?

**Answer:** Java 8 updates disabled SSLv3, weak ciphers, MD5. Old clients may fail handshake. Fix: upgrade client TLS, enable compatible cipher suites explicitly (temporary), or use TLS termination at load balancer.

---

### Q8. Difference between Java 8 update (8u291) and baseline 8?

**Answer:** 8u releases add security patches, TLS changes, timezone data, bug fixes. Always use latest 8u in production. Behavior differences (e.g., LDAP, serialization filters in later 8u) — check release notes.

---

## Self-Check

- [ ] I can explain PermGen vs Metaspace to ops team
- [ ] I know lambda uses invokedynamic
- [ ] I understand HashMap treeification threshold
- [ ] I know Nashorn/TLS/RMI deprecation context

**Next:** [Phase 10 — Production Mastery →](10-production-mastery.md)
