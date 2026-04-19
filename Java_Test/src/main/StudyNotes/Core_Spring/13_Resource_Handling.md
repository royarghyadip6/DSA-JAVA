# ⚡ Resource Handling in Spring

---

# 🔹 1. What is Resource Interface?

👉

> `Resource` is an abstraction in Spring used to **access different types of resources (files)** in a uniform way.

---

## 🧠 Why needed?

Instead of handling:

* File system
* Classpath
* URL

👉 separately ❌

Spring gives:

> One interface → `Resource` ✔

---

## 📌 Common Implementations

| Type      | Class                |
| --------- | -------------------- |
| File      | `FileSystemResource` |
| Classpath | `ClassPathResource`  |
| URL       | `UrlResource`        |

---

## 📌 Example

```java
import org.springframework.core.io.Resource;
import org.springframework.core.io.ClassPathResource;

Resource resource = new ClassPathResource("data.txt");
```

---

## 🧠 Key Methods

```java
resource.exists();
resource.getInputStream();
resource.getFilename();
```

---

# 🔹 2. Loading Files in Spring

---

# 🔸 A. Load from Classpath

## 👉 Most common

```java
Resource resource = new ClassPathResource("data.txt");
```

✔ Looks inside:

```text
src/main/resources
```

---

## 📌 Read File

```java
BufferedReader reader =
    new BufferedReader(new InputStreamReader(resource.getInputStream()));

String line = reader.readLine();
System.out.println(line);
```

---

# 🔸 B. Load from File System

```java
Resource resource =
    new FileSystemResource("C:/files/data.txt");
```

---

# 🔸 C. Load from URL

```java
Resource resource =
    new UrlResource("https://example.com/data.txt");
```

---

# 🔥 3. Using ResourceLoader (Flexible Way)

👉

> `ResourceLoader` loads resources dynamically

---

## 📌 Example

```java
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.beans.factory.annotation.Autowired;

@Component
class MyService {

    @Autowired
    private ResourceLoader resourceLoader;

    public void loadFile() throws Exception {
        Resource resource =
            resourceLoader.getResource("classpath:data.txt");

        System.out.println(resource.getFilename());
    }
}
```

---

## 🧠 Supported Prefixes

| Prefix       | Meaning          |
| ------------ | ---------------- |
| `classpath:` | From resources   |
| `file:`      | From file system |
| `http:`      | From URL         |

---

# 🔥 4. Using @Value (Simple Way)

```java
@Value("classpath:data.txt")
private Resource resource;
```

✔ Spring injects resource directly

---

# 🔁 Example

```java
@Component
class Demo {

    @Value("classpath:data.txt")
    private Resource resource;

    public void read() throws Exception {
        System.out.println(resource.getFilename());
    }
}
```

---

# ⚡ Important Interview Points

* `Resource` = abstraction over file types
* Use prefixes:

    * `classpath:`
    * `file:`
    * `http:`
* `ResourceLoader` = dynamic loading
* `@Value` = simplest way

---

# ❗ Common Mistakes

* Forgetting `classpath:` prefix
* Wrong file path
* Trying to access file outside resources

---

# 🎯 One-Line Answer

> “Spring’s Resource interface provides a unified way to access different types of resources like classpath files, filesystem files, and URLs.”

---

# 🚀 Real Use Cases

* Loading config files
* Reading templates
* Upload/download files
* Accessing external APIs

---
