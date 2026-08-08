# 37. Serialization & Deserialization

## 37. Serialization & Deserialization

## Basics

---

# 1. What is Serialization?

<details>
<summary>Show Answer</summary>

**Answer:**

**Serialization** is converting a Java object into a **byte stream** so it can be saved to a file, sent over a network, or stored in a database.

### Simple Idea

```text
Object in memory  →  bytes on disk/network  →  can travel or persist
```

```java
Employee emp = new Employee("John", 30);

ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("emp.dat"));
out.writeObject(emp); // object → bytes
out.close();
```

**Interview Point:**

> Serialization = object to byte stream for storage or transmission.

</details>

---

# 2. Why Serialization used?

<details>
<summary>Show Answer</summary>

**Answer:**

| Use Case | Example |
|----------|---------|
| **Persistence** | Save object state to file |
| **Network transfer** | RMI, old distributed systems |
| **Session replication** | Clustered app servers |
| **Cache** | Redis with Java serialization (legacy) |
| **Message queues** | Older JMS patterns |

```text
Modern microservices: prefer JSON/Protobuf over Java serialization
Still asked in interviews — know basics and why it's discouraged now
```

**Interview Point:**

> Used to persist and transmit objects. Legacy in enterprise Java. Modern apps use JSON/Protobuf.

</details>

---

# 3. What is Deserialization?

<details>
<summary>Show Answer</summary>

**Answer:**

**Deserialization** is the reverse—rebuilding a Java object from a byte stream.

```java
ObjectInputStream in = new ObjectInputStream(new FileInputStream("emp.dat"));
Employee emp = (Employee) in.readObject(); // bytes → object
in.close();
```

```text
Serialize:   Object → bytes
Deserialize: bytes → Object (new instance in memory)
```

**Interview Point:**

> Deserialization reconstructs object from bytes. Class must implement Serializable and match serialVersionUID.

</details>

---

# 4. Serializable interface?

<details>
<summary>Show Answer</summary>

**Answer:**

`Serializable` is a **marker interface**—no methods. Implementing it tells JVM "this class can be serialized."

```java
public class Employee implements Serializable {
    private String name;
    private int age;
    // private static final long serialVersionUID = 1L;
}
```

### Rules

```text
✅ Class implements Serializable
✅ All non-transient fields must be serializable
✅ Parent must be Serializable (or Object)
❌ static fields not serialized
❌ transient fields skipped
```

**Interview Point:**

> Serializable = marker interface, no methods. JVM uses reflection to read/write fields.

</details>

---

# 5. Why Serializable is marker interface?

<details>
<summary>Show Answer</summary>

**Answer:**

A **marker interface** has **no methods**—it only **tags** the class with metadata for JVM/runtime tools.

```java
public interface Serializable {
    // empty — no methods
}
```

### Why Marker?

```text
JVM checks: instanceof Serializable → allow serialization
No contract methods — serialization done by ObjectOutputStream internally
Same idea: Cloneable, Remote (legacy RMI)
```

**Interview Point:**

> Marker interface = tag only. Serializable tells JVM the class is allowed to be serialized.

</details>

---

## Frequently Asked

---

# 6. serialVersionUID?

<details>
<summary>Show Answer</summary>

**Answer:**

`serialVersionUID` is a **long constant** that identifies the **version** of a serializable class—used during deserialization to verify compatibility.

```java
public class Employee implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private int salary;
}
```

```text
If UID in file matches class UID → deserialize OK
If mismatch → InvalidClassException
```

**Interview Point:**

> serialVersionUID = version ID for serialized form. Always declare explicitly in production classes.

</details>

---

# 7. Why serialVersionUID important?

<details>
<summary>Show Answer</summary>

**Answer:**

Without explicit UID, JVM **auto-generates** one based on class structure—**any field change** breaks old serialized data.

```java
// Version 1 — saved to file with auto-generated UID
class Employee implements Serializable {
    String name;
}

// Version 2 — added field → auto UID changes → old file fails!
class Employee implements Serializable {
    String name;
    int age; // new field
}
```

With explicit UID:

```java
private static final long serialVersionUID = 1L;
// Small compatible changes may still work (new fields = default values)
```

**Interview Point:**

> Explicit serialVersionUID prevents surprise InvalidClassException after class changes. Required for evolving APIs.

</details>

---

# 8. What happens if serialVersionUID changes?

<details>
<summary>Show Answer</summary>

**Answer:**

Deserialization throws **`InvalidClassException`** — JVM refuses to create object because versions don't match.

```text
Saved file: serialVersionUID = 1L
Current class: serialVersionUID = 2L
Result: InvalidClassException — cannot deserialize
```

### Recovery Options

```text
1. Keep same UID if change is compatible (add optional fields)
2. Custom readObject() for migration
3. Don't use Java serialization — use JSON with schema versioning
```

**Interview Point:**

> UID mismatch = InvalidClassException. Plan versioning or use JSON/Protobuf for evolving data.

</details>

---

## transient Keyword

---

# 9. What is transient?

<details>
<summary>Show Answer</summary>

**Answer:**

`transient` marks a field to be **skipped** during serialization—it won't be saved in the byte stream.

```java
public class User implements Serializable {
    private String username;
    private transient String password; // NOT serialized
    private transient Connection dbConn; // NOT serialized
}
```

After deserialize, transient fields are **null** (or default)—must reinitialize.

**Interview Point:**

> transient = exclude from serialization. Use for sensitive data and non-serializable resources.

</details>

---

# 10. Why transient used?

<details>
<summary>Show Answer</summary>

**Answer:**

| Reason | Example |
|--------|---------|
| **Security** | Password, token, secret |
| **Not serializable** | Thread, Connection, Socket |
| **Derived/cache** | Computed value — rebuild on load |
| **Large/temporary** | Skip bloating serialized form |

```java
public class Session implements Serializable {
    private String userId;
    private transient User cachedUser; // reload from DB after deserialize

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.cachedUser = userRepository.findById(userId); // reinitialize
    }
}
```

**Interview Point:**

> transient for secrets, non-serializable fields, and cached data. Reinitialize in readObject().

</details>

---

# 11. Can transient field be serialized?

<details>
<summary>Show Answer</summary>

**Answer:**

**No** — `transient` fields are **never** written to the byte stream by default serialization.

```java
class Data implements Serializable {
    String name = "John";
    transient int secret = 42;
}
// Serialized bytes contain name only
// After deserialize: name="John", secret=0 (default int)
```

Custom `writeObject()` could theoretically write transient—but default mechanism skips them.

**Interview Point:**

> Default serialization skips transient. Value lost unless custom writeObject/readObject restores it.

</details>

---

## static and Serialization

---

# 12. Are static variables serialized?

<details>
<summary>Show Answer</summary>

**Answer:**

**No** — static fields belong to the **class**, not the object instance. Serialization saves **instance state** only.

```java
class Counter implements Serializable {
    static int count = 0; // NOT serialized
    int value = 10;       // serialized
}
```

After deserialize, `count` reflects **current class value**, not value at serialize time.

**Interview Point:**

> Static fields not serialized — class-level, not per-object.

</details>

---

# 13. Why not?

<details>
<summary>Show Answer</summary>

**Answer:**

Static variables are stored in **Metaspace** (class metadata area), shared by all instances—serialization targets **one object's heap state**.

```text
Object serialization = snapshot of ONE instance's fields on heap
Static = one copy per class in JVM — not part of object snapshot

If you need class-level state persisted → separate config file or DB
```

**Interview Point:**

> Static = class level in Metaspace. Serialization = instance heap state only.

</details>

---

## Advanced

---

# 14. writeObject()?

<details>
<summary>Show Answer</summary>

**Answer:**

Custom `writeObject()` controls **how** instance data is written—override default serialization logic.

```java
private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject(); // serialize non-transient fields
    // custom logic
    out.writeObject(encrypt(password));
}
```

Must be `private` — JVM calls via reflection special mechanism.

**Interview Point:**

> writeObject() customizes serialization. Call defaultWriteObject() first unless fully custom.

</details>

---

# 15. readObject()?

<details>
<summary>Show Answer</summary>

**Answer:**

Custom `readObject()` controls **how** object is rebuilt—validate, decrypt, reinitialize transient fields.

```java
private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject(); // restore non-transient fields
    this.password = decrypt((String) in.readObject());
    this.dbConn = connectionPool.getConnection(); // reinit transient
}
```

**Interview Point:**

> readObject() for validation and reinitializing transient fields after deserialize.

</details>

---

# 16. Externalizable?

<details>
<summary>Show Answer</summary>

**Answer:**

`Externalizable` interface gives **full control** over serialization—you implement `writeExternal()` and `readExternal()` manually.

```java
public class Employee implements Externalizable {
    private String name;
    private int age;

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(name);
        out.writeInt(age);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        name = (String) in.readObject();
        age = in.readInt();
    }

    public Employee() {} // public no-arg constructor REQUIRED
}
```

**Interview Point:**

> Externalizable = manual read/write. Must have public no-arg constructor. More control than Serializable.

</details>

---

# 17. Serializable vs Externalizable?

<details>
<summary>Show Answer</summary>

**Answer:**

| | Serializable | Externalizable |
|---|--------------|----------------|
| Interface | Marker (no methods) | writeExternal / readExternal |
| Control | Default + optional writeObject | Full manual control |
| Performance | Reflection-based — slower | Can be faster (write only needed fields) |
| Constructor | No special requirement | Public no-arg required |
| Usage | Common default | Rare — performance-critical |

**Interview Point:**

> Serializable = automatic (reflection). Externalizable = manual control, public no-arg ctor required.

</details>

---

## Security Questions

---

# 18. Serialization vulnerabilities?

<details>
<summary>Show Answer</summary>

**Answer:**

Java deserialization can execute **malicious code** if attacker sends crafted byte stream—**gadget chains** trigger dangerous methods during deserialize.

### Famous Issues

```text
Apache Commons Collections gadget chains
Remote code execution via ObjectInputStream.readObject()
Any endpoint accepting serialized Java objects = high risk
```

### Mitigations

```text
✅ Don't deserialize untrusted data
✅ Use look-ahead deserialization filters (JEP 290)
✅ Whitelist allowed classes
✅ Use JSON instead of Java serialization
```

**Interview Point:**

> Deserialization of untrusted data = RCE risk. Never accept Java serialized objects from clients.

</details>

---

# 19. Why serialization discouraged in microservices?

<details>
<summary>Show Answer</summary>

**Answer:**

| Problem | Detail |
|---------|--------|
| **Security** | Deserialization attacks |
| **Language lock-in** | Only Java understands format |
| **Versioning** | serialVersionUID brittle |
| **Debugging** | Binary — not human readable |
| **Size** | Often larger than JSON/Protobuf |
| **Interop** | REST/JSON standard across services |

```text
Microservices: different languages, HTTP/JSON, schema evolution
Java serialization: Java-only, binary, security risk
```

**Interview Point:**

> Microservices use JSON/Protobuf — language-neutral, readable, safer, better versioning.

</details>

---

# 20. Alternatives to serialization?

<details>
<summary>Show Answer</summary>

**Answer:**

| Alternative | Use Case |
|-------------|----------|
| **JSON (Jackson/Gson)** | REST APIs, config, logs |
| **Protobuf / Avro** | High-performance messaging, Kafka |
| **XML** | Legacy enterprise (SOAP) |
| **Kryo** | Fast Java-only binary (trusted data) |
| **Records + JSON** | Modern Java DTOs |

```java
// JSON — standard for REST
ObjectMapper mapper = new ObjectMapper();
String json = mapper.writeValueAsString(employee);
Employee emp = mapper.readValue(json, Employee.class);

// Kafka with Avro/Protobuf — schema registry, evolution
```

**Interview Point:**

> JSON for REST. Protobuf/Avro for Kafka/high perf. Avoid Java native serialization for external APIs.

</details>

---

# 5–8 Year Interview Rapid Fire

### Q: Parent class not Serializable — child Serializable?

<details>
<summary>Show Answer</summary>

**Answer:**

Child can serialize **its own fields**. Parent fields reset to defaults — parent state **lost**. Parent must implement Serializable (or have no important state) for full object state.

</details>

---

### Q: Serializable vs Cloneable?

<details>
<summary>Show Answer</summary>

**Answer:**

Both marker interfaces. Serializable = object to bytes. Cloneable = duplicate object in memory via clone(). Different purposes.

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> Serialization = object to bytes via Serializable marker. serialVersionUID for versioning. transient skips fields. static not serialized. Security risk on untrusted deserialize. Microservices use JSON/Protobuf instead.

</details>
