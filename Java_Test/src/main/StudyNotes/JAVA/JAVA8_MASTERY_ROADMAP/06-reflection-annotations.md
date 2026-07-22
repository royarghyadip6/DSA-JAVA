# Phase 6 — Reflection & Annotations

**Duration:** 1 week | **Goal:** Parameter reflection, repeatable annotations, type annotations

[← Phase 5](05-concurrency-async.md) | [Index](README.md) | **Next:** [Phase 7 — I/O & Utilities →](07-io-utilities-misc.md)

---

## Theory (Easy to Remember)

### 1. Parameter Reflection (Java 8)

- Before Java 8: you could get parameter **types** but not reliable **names**.
- **`java.lang.reflect.Parameter`** = metadata for method/constructor parameters.
- **`Executable.getParameters()`** = array of `Parameter` objects.
- Parameter names need **`-parameters` compiler flag** or debug symbols — otherwise `arg0`, `arg1`.

**Memory trick:** *"Executable = Method + Constructor parent. Parameter = named slot in method signature."*

---

### 2. Executable Class

- **`Executable`** abstract base for **`Method`** and **`Constructor`**.
- Shared API: `getParameters()`, `getParameterCount()`, `getGenericParameterTypes()`, `isVarArgs()`.
- Reduces duplication between Method and Constructor reflection.

---

### 3. Repeatable Annotations

- Apply **same annotation multiple times** on one element.
- Requires:
  1. Annotation marked `@Repeatable(Container.class)`
  2. Container annotation that holds array of the repeatable annotation

```java
@Repeatable(Roles.class)
@interface Role { String value(); }

@interface Roles { Role[] value(); }

@Role("ADMIN") @Role("USER")
void secureMethod() {}
```

- Read via `getAnnotationsByType(Role.class)` — returns both.

---

### 4. Type Annotations (Java 8)

- Annotations on **any type use** — not just declarations.
- `ElementType.TYPE_USE` — generics, casts, `extends`, `throws`
- Used by pluggable type checkers (Checker Framework, nullness analysis)

```java
List<@NonNull String> names;
```

---

### 5. @Native Annotation

- Marks constants that JNI header generators should include.
- For native code integration — niche but part of Java 8.

---

### 6. MethodHandleInfo (java.lang.invoke)

- Public interface exposing metadata about direct method handles.
- `Lookup.revealDirect(MethodHandle)` — introspect handles.
- Advanced metaprogramming — used by frameworks, not daily app code.

---

## Examples

### Read parameter names

```java
public void greet(@NotNull String name, int age) { }

Method m = MyClass.class.getMethod("greet", String.class, int.class);
for (Parameter p : m.getParameters()) {
    System.out.println(p.getName() + " : " + p.getType());
    // With -parameters: name : String, age : int
}
```

### Repeatable annotations

```java
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Schedules.class)
@interface Schedule {
    String day();
}

@Retention(RetentionPolicy.RUNTIME)
@interface Schedules {
    Schedule[] value();
}

@Schedule(day = "MON") @Schedule(day = "WED")
public void runBatch() {}

// Reading
Method method = ...;
Schedule[] all = method.getAnnotationsByType(Schedule.class);
```

### Custom validation annotation (framework-style)

```java
@Retention(RUNTIME)
@Target(PARAMETER)
@interface ValidEmail {}

// Validator using reflection
public void validate(Object target) throws Exception {
    for (Method m : target.getClass().getMethods()) {
        Parameter[] params = m.getParameters();
        for (int i = 0; i < params.length; i++) {
            if (params[i].isAnnotationPresent(ValidEmail.class)) {
                Object[] args = ...; // invoke context
                String email = (String) args[i];
                if (!email.contains("@")) throw new IllegalArgumentException();
            }
        }
    }
}
```

---

## Practice Exercises

| # | Exercise | Difficulty |
|---|----------|------------|
| 1 | Print all parameter names of any class methods (with `-parameters`) | Easy |
| 2 | Create `@Author` repeatable annotation; read all authors from a class | Medium |
| 3 | Build mini `@NotNull` parameter validator using reflection | Medium |
| 4 | Compare `getAnnotation` vs `getAnnotationsByType` for repeatable | Medium |
| 5 | Document why parameter names missing without compiler flag | Easy |
| 6 | Use `Executable.isVarArgs()` to detect varargs methods | Easy |
| 7 | Design annotation processor sketch (conceptual) for `@Route` on methods | Hard |

---

## Interview Q&A (5–8 Years Experience)

### Q1. How do you get method parameter names at runtime?

**Answer:** Java 8+ `Method.getParameters()` → `Parameter.getName()`. Requires compiling with **`-parameters`** flag (Maven: `<parameters>true</parameters>`). Without it, names are `arg0`, `arg1` (from debug table if present, not guaranteed). Spring MVC and Jackson use this for `@RequestParam` binding without explicit name.

---

### Q2. What is the difference between `getAnnotation` and `getAnnotationsByType`?

**Answer:**
- **`getAnnotation`:** Direct annotation only; for repeatable, may return **container** or null depending on how applied.
- **`getAnnotationsByType`:** Flattens **repeatable** annotations into logical array. Preferred for `@Repeatable` types.

---

### Q3. Explain `@Repeatable` design.

**Answer:** Java doesn't allow duplicate annotation syntax without container. `@Repeatable` links annotation to container type (e.g., `@Roles`). Compiler generates container automatically when multiple present. Runtime uses `getAnnotationsByType` for unified view.

---

### Q4. What are type annotations used for?

**Answer:** Annotations on type **uses** (generics, casts). Enable static analysis tools (nullness, immutability, SQL injection types). Not enforced by JVM — need checker framework or annotation processor.

---

### Q5. Reflection vs Method Handles vs Bytecode generation?

**Answer:**
| Approach | Speed | Flexibility |
|----------|-------|-------------|
| Reflection | Slow (security checks) | Easy |
| MethodHandle | Faster, JVM optimized | Medium |
| Generated bytecode (ASM) | Fastest | Complex |

Java 8 lambdas use **invokedynamic** + MethodHandles, not reflection per call.

---

### Q6. What is `Executable` and why was it added?

**Answer:** Extract 14 common methods from `Method` and `Constructor` into abstract `Executable`. DRY for parameter reflection, modifiers, generic signature. `Constructor` and `Method` extend `Executable`, not `Member` directly for parameters API.

---

### Q7. Security concerns with reflection?

**Answer:** Bypasses encapsulation (`setAccessible(true)`). Java 9+ **module system** restricts deep reflection. SecurityManager (deprecated) could limit. In libraries: validate inputs, don't reflect on user-supplied class names without sandbox.

---

### Q8. How do annotation processors differ from runtime reflection?

**Answer:** **Annotation processors** run at **compile time** (APT), generate code or validate. **Runtime reflection** inspects classes after load. Processors: Lombok, MapStruct, Dagger. Zero runtime cost for generated code.

---

## Self-Check

- [ ] I can enable and use `-parameters`
- [ ] I understand repeatable annotation container pattern
- [ ] I know when reflection is too slow for hot paths

**Next:** [Phase 7 — I/O & Utilities →](07-io-utilities-misc.md)
