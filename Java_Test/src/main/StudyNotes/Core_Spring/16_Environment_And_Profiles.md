# ⚡ Environment & Profiles in Spring

---

# 🔹 1. What is Environment in Spring?

👉

> The **Environment** abstraction provides access to application configuration such as:

* Properties (`application.properties`)
* System variables
* Active profiles

---

## 📌 Interface

```java
org.springframework.core.env.Environment
```

---

## 📌 Example

```java
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;

@Component
class MyService {

    @Autowired
    private Environment env;

    public void print() {
        System.out.println(env.getProperty("app.name"));
    }
}
```

---

## 🧠 Key Points

* Central place to read configuration
* Supports multiple sources
* Used internally by Spring

---

# 🔹 2. What are Profiles?

👉

> Profiles allow you to **activate specific configurations based on environment**

---

## 🧠 Why needed?

Different environments need different configs:

| Environment | DB       | Logging |
| ----------- | -------- | ------- |
| Dev         | Local DB | Debug   |
| Prod        | Real DB  | Error   |

---

---

# 🔹 3. @Profile Annotation

---

## 👉 What it does?

> Tells Spring **which bean/config to load based on active profile**

---

## 📌 Example

```java
@Component
@Profile("dev")
class DevDatabase {

    public DevDatabase() {
        System.out.println("Dev DB connected");
    }
}
```

---

```java
@Component
@Profile("prod")
class ProdDatabase {

    public ProdDatabase() {
        System.out.println("Prod DB connected");
    }
}
```

---

## 🧪 Activate Profile

```properties
spring.profiles.active=dev
```

---

## ✔ Output

```text
Dev DB connected
```

---

# 🔹 4. Java Config Example

```java
@Configuration
class AppConfig {

    @Bean
    @Profile("dev")
    public DataSource devDb() {
        return new DevDataSource();
    }

    @Bean
    @Profile("prod")
    public DataSource prodDb() {
        return new ProdDataSource();
    }
}
```

---

# 🔹 5. application.properties per Profile

---

## 📌 Files

```text
application-dev.properties
application-prod.properties
```

---

## 📌 Example

### application-dev.properties

```properties
db.url=jdbc:h2:mem:test
```

---

### application-prod.properties

```properties
db.url=jdbc:mysql://prod-db
```

---

## 📌 Usage

```java
@Value("${db.url}")
private String dbUrl;
```

✔ Automatically picks correct file

---

# 🔹 6. Activating Profiles

---

## ✔ Method 1: properties file

```properties
spring.profiles.active=prod
```

---

## ✔ Method 2: JVM argument

```bash
-Dspring.profiles.active=dev
```

---

## ✔ Method 3: Programmatically

```java
SpringApplication app = new SpringApplication(App.class);
app.setAdditionalProfiles("dev");
```

---

# 🔹 7. Environment + Profile Together

---

## 📌 Example

```java
@Component
class ProfileChecker {

    @Autowired
    private Environment env;

    public void check() {
        System.out.println(Arrays.toString(env.getActiveProfiles()));
    }
}
```

---

# 🔥 8. Real Dev vs Prod Setup

---

## ✔ Dev

* H2 DB
* Debug logs
* Mock services

---

## ✔ Prod

* MySQL/PostgreSQL
* Limited logs
* Real services

---

## 📌 Example

```java
@Service
@Profile("dev")
class MockPaymentService {
}
```

```java
@Service
@Profile("prod")
class RealPaymentService {
}
```

---

# ⚡ Important Interview Points

* Profiles = environment-based configuration
* `@Profile` controls bean creation
* `Environment` reads properties
* Spring Boot auto-loads profile-specific files

---

# ❗ Common Mistakes

* Not activating profile
* Wrong property file naming
* Multiple beans without profile → conflict

---

# 🎯 One-Line Answer

> “Spring profiles allow environment-specific bean configuration, and the Environment abstraction provides access to properties and active profiles.”

---

# 🚀 Real-World Insight

👉 Every production system uses:

* dev
* test
* staging
* prod

👉 Profiles make switching seamless

---

# 🔥 Bonus (Advanced)

## Multiple Profiles

```properties
spring.profiles.active=dev,test
```

---

## Conditional Profiles

```java
@Profile("dev & cloud")
```

---

---

# 🔥 Tricky Interview Questions – Spring Profiles

---

## 🔹 1. What happens if no profile is active?

👉

> Spring uses the **default profile** (`default`)

✔ Beans without `@Profile` will load
✔ Beans with `@Profile` will NOT load

---

## 🔹 2. What if two beans of same type exist in different profiles?

👉
✔ Only the **active profile’s bean** is loaded
✔ No conflict if only one profile is active

---

## 🔹 3. What if multiple profiles are active?

```properties
spring.profiles.active=dev,test
```

👉
✔ Beans from BOTH profiles will load

⚠️ Risk:

> Conflict if same type beans exist

---

## 🔹 4. What if multiple beans match after activating profiles?

👉

> ❌ `NoUniqueBeanDefinitionException`

✔ Fix:

* `@Primary`
* `@Qualifier`

---

## 🔹 5. Can we use @Profile on methods?

👉 ✔ Yes

```java
@Bean
@Profile("dev")
public DataSource devDb() {}
```

---

## 🔹 6. Can we use @Profile on class?

👉 ✔ Yes (most common)

```java
@Profile("prod")
@Component
class ProdService {}
```

---

## 🔹 7. Can a bean belong to multiple profiles?

👉 ✔ Yes

```java
@Profile({"dev", "test"})
```

✔ Loads in either dev OR test

---

## 🔹 8. Can we use AND condition in profiles?

👉 ✔ Yes

```java
@Profile("dev & cloud")
```

✔ Both must be active

---

## 🔹 9. Can we exclude a profile?

👉 ✔ Yes

```java
@Profile("!prod")
```

✔ Active in all except prod

---

## 🔹 10. What happens if wrong profile name is set?

👉
✔ No beans for that profile will load
✔ App may fail if dependency missing

---

# ⚡ Scenario-Based Questions

---

## 🔹 11. Your app is not picking dev config. Why?

👉 Possible reasons:

* Profile not activated
* Wrong file name (`application-dev.properties`)
* Typo in profile name

---

## 🔹 12. application-dev.properties not loading. Why?

👉

* Missing:

```properties
spring.profiles.active=dev
```

---

## 🔹 13. Can we activate profile at runtime?

👉 ✔ Yes

* JVM argument
* Environment variable
* Programmatically

---

## 🔹 14. What is the priority of configuration?

👉

1. Command-line args
2. Environment variables
3. application.properties

---

## 🔹 15. Can we have default profile?

👉 ✔ Yes

```properties
spring.profiles.default=dev
```

---

# 🔥 Advanced Questions

---

## 🔹 16. Difference between @Profile and conditional annotations?

👉

* `@Profile` → environment-based
* `@Conditional` → custom logic

---

## 🔹 17. What happens if no bean matches active profile?

👉

> ❌ Application fails (dependency injection error)

---

## 🔹 18. Can profiles be nested?

👉 ❌ Not directly
✔ But can combine using expressions

---

## 🔹 19. Why profiles are important in microservices?

👉

> Different environments require different configs (DB, API endpoints, security)

---

## 🔹 20. Can profiles affect performance?

👉
✔ Yes (wrong config → heavy resources loaded)

---

# 🎯 Killer Question

## ❓ “What happens if you activate multiple profiles with same bean?”

👉 Strong answer:

> “Spring will load all matching beans, which may cause NoUniqueBeanDefinitionException unless resolved using @Primary or @Qualifier.”

---

# 🚀 Pro Tip

Interviewers LOVE:

* Multiple profile conflict
* Default profile behavior
* @Profile expressions (`!`, `&`)
* Real-world config issues

👉 If you explain **conflict scenarios clearly**, you stand out.

---
