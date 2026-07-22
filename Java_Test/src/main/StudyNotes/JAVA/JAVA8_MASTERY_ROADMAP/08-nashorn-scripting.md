# Phase 8 — Nashorn JavaScript Engine (Legacy)

**Duration:** 3–5 days | **Goal:** Understand JS-in-Java for legacy maintenance

[← Phase 7](07-io-utilities-misc.md) | [Index](README.md) | **Next:** [Phase 9 — JVM & Platform →](09-jvm-platform.md)

> ⚠️ **Important:** Nashorn was **removed in JDK 15** (JEP 372). Learn this only for maintaining legacy Java 8–14 systems. New projects: use GraalJS or external script engines.

---

## Theory (Easy to Remember)

### 1. What Is Nashorn?

- **Nashorn** = JavaScript engine bundled with JDK 8–14.
- Access via **`javax.script`** API (`ScriptEngineManager`).
- Lets Java apps **run JavaScript** — rules, templates, user scripts.
- Replaced old Rhino engine (better performance).

**Memory trick:** *"Nashorn = Java speaks JavaScript. GraalJS = replacement after JDK 14."*

---

### 2. Key Classes

| Class | Package | Purpose |
|-------|---------|---------|
| `NashornScriptEngineFactory` | `jdk.nashorn.api.scripting` | Create Nashorn engine |
| `ScriptEngine` | `javax.script` | Execute scripts |
| `Invocable` | `javax.script` | Call JS functions from Java |
| `JSObject` | `jdk.nashorn.api.scripting` | JS object in Java |
| `ClassFilter` | `jdk.nashorn.api.scripting` | Restrict Java class access from JS |

---

### 3. Basic Flow

1. Create `ScriptEngineManager`
2. Get engine by name `"nashorn"` or `"JavaScript"`
3. `engine.eval("script")` — run JS
4. `Invocable.invokeFunction("fn", args)` — call JS from Java
5. Bind Java objects: `engine.put("name", javaObject)`

---

### 4. Security Warning

- Running user scripts = **code injection risk**.
- Use **`ClassFilter`** to whitelist Java classes accessible from JS.
- Sandbox scripts in production; never `eval` untrusted input without isolation.

---

## Examples

### Run simple script

```java
ScriptEngineManager manager = new ScriptEngineManager();
ScriptEngine engine = manager.getEngineByName("nashorn");

engine.eval("var greeting = 'Hello from JS'; greeting;");
Object result = engine.get("greeting");
System.out.println(result); // Hello from JS
```

### Call Java from JavaScript

```java
engine.put("name", "Alice");
engine.eval("var msg = 'Hello, ' + name;");
```

### Invoke JS function from Java

```java
Invocable invocable = (Invocable) engine;
engine.eval("function add(a, b) { return a + b; }");
Object sum = invocable.invokeFunction("add", 10, 20); // 30.0
```

### ClassFilter for security

```java
NashornScriptEngineFactory factory = new NashornScriptEngineFactory();
ScriptEngine engine = factory.getScriptEngine(new ClassFilter() {
  public boolean exposeToScripts(String className) {
    return className.startsWith("com.myapp.safe.");
  }
});
```

---

## Practice Exercises

| # | Exercise | Difficulty |
|---|----------|------------|
| 1 | Evaluate JS expression and print result from Java | Easy |
| 2 | Pass Java `List` to JS and iterate in script | Medium |
| 3 | Call JS function that returns JSON string; parse in Java | Medium |
| 4 | Implement ClassFilter allowing only `java.lang.String` | Medium |
| 5 | Document migration path from Nashorn to GraalJS | Hard |

---

## Interview Q&A (5–8 Years Experience)

### Q1. Why was Nashorn removed from JDK?

**Answer:** Maintenance cost, ECMAScript spec lag, GraalVM's **GraalJS** offered better performance and modern JS support. JEP 372 deprecated Nashorn in 11, removed in 15. Oracle encourages standalone GraalJS or other engines.

---

### Q2. Nashorn vs Rhino?

**Answer:** Nashorn (JDK 8+) compiles JS to JVM bytecode via `invokedynamic` — much faster. Rhino was older, interpreted. Nashorn replaced Rhino as default.

---

### Q3. How do you migrate Nashorn code to Java 8+ without Nashorn?

**Answer:** Options:
1. **GraalJS** standalone dependency
2. Rewrite scripts in Java (rules engines: Drools, MVEL)
3. External Node.js service for JS logic
4. Stay on JDK 11 LTS with Nashorn module (deprecated) until planned migration

---

### Q4. Security risks of embedding scripting?

**Answer:** Arbitrary code execution, access to Java classes (`Java.type('java.io.File')`), file system, network. Mitigate: `ClassFilter`, SecurityManager (legacy), separate process sandbox, no user-supplied scripts in prod without review.

---

### Q5. javax.script API — engine-agnostic?

**Answer:** Yes. `ScriptEngineManager` discovers engines via `META-INF/services`. Same API works with Nashorn, Groovy, JRuby — swap engine by classpath. Portability depends on engine-specific features (Nashorn's `Java` global is Nashorn-specific).

---

## Self-Check

- [ ] I know Nashorn is removed in JDK 15+
- [ ] I understand ClassFilter security model
- [ ] I can explain migration options to interviewer

**Next:** [Phase 9 — JVM & Platform →](09-jvm-platform.md)
