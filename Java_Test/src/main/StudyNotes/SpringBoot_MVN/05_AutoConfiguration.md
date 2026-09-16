# 05. Auto-Configuration Internals

## Start here (simple English)

**In one sentence:** If you add a library, Boot **tries to set up the usual beans** for that library. You did not write `new Tomcat()`. Boot did, because `starter-web` was on the classpath.

**Everyday picture:** You buy a printer. Windows/Mac **auto-installs a driver**. If you already installed your own driver, the OS **backs off**.

- **Classpath** = “the printer is plugged in” (the JAR is there)
- **`@ConditionalOnClass`** = “only if this class exists”
- **`@ConditionalOnMissingBean`** = “only if you did not already create this bean”
- **Exclude** = “do not auto-install this driver at all”

**Tiny example:**

```text
You add spring-boot-starter-web
  → Tomcat classes are on the classpath
  → Boot creates an embedded Tomcat
  → You can open http://localhost:8080
```

If you create your own `DataSource` `@Bean`, Boot usually **does not** create a second one. That is `OnMissingBean`.

You do **not** need to write auto-config on day 1. You **do** need to know it exists, how to **turn one class off**, and how to **debug** with `--debug`. Interview Q&A is **5–8 year standard**.

---

This is the chapter senior interviews want: **how Boot decided to create a DataSource, how you take that over, and how you ship a custom starter.**

---

## 1. The idea in one picture

```text
Maven put JARs on the classpath
        ↓
@EnableAutoConfiguration loads a list of @AutoConfiguration classes
        ↓
Each class is full of @ConditionalOnClass / OnMissingBean / OnProperty / …
        ↓
Conditions pass → @Bean methods register definitions
Conditions fail → class is skipped (you see this in auto-config report)
```

You did not write `TomcatServletWebServerFactory`. `ServletWebServerFactoryAutoConfiguration` did, because `starter-web` brought Tomcat classes.

---

## 2. Where the list lives

### Boot 3 (current)

File on the classpath (inside `spring-boot-autoconfigure.jar` and in **your** starter jars):

```text
META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

Plain text, one class per line:

```text
com.acme.payments.autoconfigure.PaymentsAutoConfiguration
```

### Boot 2.x (legacy, still asked)

```text
META-INF/spring.factories
```

```properties
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
  com.acme.payments.autoconfigure.PaymentsAutoConfiguration
```

**Interview line:** Boot 3 moved auto-config registration from `spring.factories` to `AutoConfiguration.imports`. `spring.factories` is still used for other keys (`ApplicationListener`, `EnvironmentPostProcessor`, `FailureAnalyzer`, …).

`@SpringBootApplication` includes `@EnableAutoConfiguration`, which imports `AutoConfigurationImportSelector`. That selector reads the files above, applies exclusions, and imports the classes.

---

## 3. `@AutoConfiguration` vs `@Configuration`

Boot 3:

```java
@AutoConfiguration
@ConditionalOnClass(DataSource.class)
@AutoConfigureAfter(JdbcTemplateAutoConfiguration.class)
@EnableConfigurationProperties(PaymentsProperties.class)
public class PaymentsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    PaymentsClient paymentsClient(PaymentsProperties props) {
        return new PaymentsClient(props.baseUrl());
    }
}
```

- `@AutoConfiguration` is a specialized `@Configuration` (typically `proxyBeanMethods = false`).
- It is **not** picked up by component scan. Only by the imports file.
- App code should stay `@Configuration`. `@AutoConfiguration` is for **starters**.

Ordering:

| Annotation | Meaning |
|------------|---------|
| `@AutoConfigureBefore` / `@After` | Relative to other auto-config classes |
| `@AutoConfigureOrder` | Numeric order (lower = earlier) |
| `@AutoConfiguration(after = …)` | Boot 3 convenience on the type |

Order matters when your `@ConditionalOnMissingBean` must run **after** Boot’s own bean would have been defined — or before.

---

## 4. Conditions you must know

Evaluated on the `@AutoConfiguration` class and on each `@Bean`.

| Annotation | Passes when |
|------------|-------------|
| `@ConditionalOnClass` | Class **name** is on the classpath (does not load your app classes wrongly) |
| `@ConditionalOnMissingClass` | Opposite |
| `@ConditionalOnBean` | Bean of type/name already in the factory |
| `@ConditionalOnMissingBean` | No such bean — **this is how user beans win** |
| `@ConditionalOnProperty` | Property equals `havingValue` (or exists, depending on `matchIfMissing`) |
| `@ConditionalOnResource` | Resource exists |
| `@ConditionalOnWebApplication` | Servlet or reactive web app |
| `@ConditionalOnNotWebApplication` | CLI/batch |
| `@ConditionalOnExpression` | SpEL |
| `@Conditional` | Custom `Condition` implementation |

**`@ConditionalOnMissingBean` trap:** it matches by **type** by default. If you define a `HikariDataSource` bean, `DataSource` is already present → Boot skips `DataSource` auto-config. If you define a **wrapper** that is not a `DataSource`, Boot still creates Hikari — then you have two pools.

```java
@Bean
@ConditionalOnMissingBean(DataSource.class)
DataSource dataSource(...) { ... }
```

**`@ConditionalOnClass` trap:** use the class **literal** only if that class is an **optional** compile dependency. In a starter, prefer:

```java
@ConditionalOnClass(name = "com.acme.sdk.Client")
```

or a dedicated `spring-boot-autoconfigure-processor` so you don’t hard-link.

**`@ConditionalOnBean` trap:** bean definition order. If the bean you depend on is defined in an auto-config that runs **later**, the condition fails at evaluation time. That is why `@AutoConfigureAfter` exists. Also, `OnBean` looks at **definitions**, not only fully created instances — but the timing is still a classic footgun.

---

## 5. How **you** override Boot

From least to most aggressive:

1. **Set a property** — `spring.jackson.serialization.write-dates-as-timestamps=false`. Auto-config reads properties.
2. **Define your own `@Bean`** of the same type — `@ConditionalOnMissingBean` makes Boot back off. Example: custom `ObjectMapper` (careful: you may drop Boot’s defaults; prefer `Jackson2ObjectMapperBuilderCustomizer`).
3. **`@ConditionalOnProperty` feature flag** in *your* config.
4. **Exclude** one auto-config class:

```java
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
```

```yaml
spring.autoconfigure.exclude: org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
```

5. **Don’t put the JAR on the classpath** — no Tomcat classes, no Tomcat auto-config.

Prefer (1) and (2). Exclude when the class creates **many** beans you do not want, or when `OnMissingBean` is not used (rare, but exists).

**Customizer beans** are the Boot 2.x/3.x style:

```java
@Bean
Jackson2ObjectMapperBuilderCustomizer json() {
    return b -> b.modules(new JavaTimeModule());
}
```

Same idea: `WebServerFactoryCustomizer<TomcatServletWebServerFactory>`, `RestTemplateCustomizer`.

---

## 6. Debug: what fired?

```yaml
debug: true
# or
logging.level.org.springframework.boot.autoconfigure: DEBUG
```

Startup log **Positive matches** / **Negative matches** / **Exclusions**.

```bash
java -jar app.jar --debug
```

`ConditionEvaluationReport` is the source of “why didn’t my DataSource auto-config run?”.

Actuator: `/actuator/conditions` (expose + secure it).

---

## 7. Writing a custom starter (the senior exercise)

Three modules is the clean shape; two is acceptable:

```text
payments-client              // plain SDK, no Spring
payments-spring-boot-autoconfigure
payments-spring-boot-starter // POM: depends on client + autoconfigure + boot
```

**autoconfigure module:**

```java
@AutoConfiguration
@ConditionalOnClass(PaymentsClient.class)
@EnableConfigurationProperties(PaymentsProperties.class)
@ConditionalOnProperty(prefix = "acme.payments", name = "enabled", matchIfMissing = true)
public class PaymentsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    PaymentsClient paymentsClient(PaymentsProperties props) {
        return new PaymentsClient(props.baseUrl(), props.token());
    }
}
```

```text
# META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
com.acme.payments.autoconfigure.PaymentsAutoConfiguration
```

**starter POM:** only dependencies, packaging `jar` with almost no classes.

App then:

```xml
<dependency>
  <groupId>com.acme</groupId>
  <artifactId>payments-spring-boot-starter</artifactId>
</dependency>
```

```yaml
acme.payments.base-url: https://payments.internal
```

Add `spring-boot-configuration-processor` for metadata. Add `spring-boot-autoconfigure-processor` if you use annotation-based conditions that should be indexed.

**Do not** component-scan the starter’s internal package from the app. The imports file is the contract.

---

## 8. `spring.factories` keys that are not auto-config

Still valid in Boot 3:

- `org.springframework.context.ApplicationListener`
- `org.springframework.boot.env.EnvironmentPostProcessor` (runs **before** context — config decryption)
- `org.springframework.boot.diagnostics.FailureAnalyzer`
- `org.springframework.boot.autoconfigure.template.TemplateAvailabilityProvider`

`EnvironmentPostProcessor` must be listed in `META-INF/spring/org.springframework.boot.env.EnvironmentPostProcessor.imports` in recent Boot, analogous to auto-config. Interviewers still say “spring.factories”. Answer with the generation you use.

---

## 9. Connection to Maven

Auto-config cannot see a library that Maven **did not put on the classpath**. Wrong scope (`test`, `provided`) → condition fails → “Boot didn’t configure Redis”.

Two versions of a library via mediation → auto-config compiled against API A, runtime is B → `NoSuchMethodError`. That is a Maven problem (chapter 00), not “Boot is broken”.

---

## 10. Production pitfalls

1. Defining `RestTemplate` `@Bean` without `RestTemplateBuilder` — you lose metrics, timeouts, message converters Boot would have set.
2. Excluding `DataSourceAutoConfiguration` but leaving `HibernateJpaAutoConfiguration` — louder failures.
3. `@ConditionalOnBean(Foo.class)` when `Foo` is also auto-configured later — silent skip.
4. Putting `@AutoConfiguration` in the **application** module and also scanning it — double registration.
5. Custom `ObjectMapper` `@Bean` replacing Boot’s and breaking JavaTime ISO-8601.
6. `matchIfMissing = false` on a property you never documented — starter dead until the user sets a flag.

---

# Interview Q&A (5–8 year bar)

A fresher says “Boot configures things automatically.” A 5–8 year answer names `AutoConfiguration.imports`, `@ConditionalOnMissingBean` type matching, and how to write a starter.

### Q1. How does auto-configuration work?

**Answer:** `@EnableAutoConfiguration` loads `@AutoConfiguration` classes from `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`. Each class/bean is gated by `@Conditional*`. Matching conditions register beans.

**Counter:** Boot 2 vs Boot 3 registration?  
**Answer:** Boot 2: `META-INF/spring.factories` key `EnableAutoConfiguration`. Boot 3: `AutoConfiguration.imports`.

**Counter:** Is that the same as component scan?  
**Answer:** No. Auto-config is an import list. Scan finds `@Component` in your packages.

---

### Q2. How do you disable one auto-config class?

**Answer:** `exclude` on `@SpringBootApplication`, or `spring.autoconfigure.exclude`.

**Counter:** How do you disable **all** auto-config?  
**Answer:** Don’t use `@EnableAutoConfiguration`. Almost never what you want.

---

### Q3. You defined a `DataSource` bean. Why might Hikari still appear?

**Answer:** Your bean might not implement `DataSource`, or you created it too late, or you excluded the wrong class. `@ConditionalOnMissingBean(DataSource.class)` keys off type. Check the auto-config report.

**Counter:** What is the right override style?  
**Answer:** Provide a `DataSource` bean, **or** use Boot’s datasource properties, **or** a `DataSourceBuilder`. Don’t fight with a random wrapper type.

---

### Q4. What does `@ConditionalOnMissingBean` mean?

**Answer:** Register this bean only if the context does not already have a bean of that type (or name, if specified). User-defined beans therefore win.

**Counter:** Evaluated on class vs method?  
**Answer:** Both. Class-level skip means **no** beans from that config. Method-level skip is per bean.

**Trap:** “It checks at runtime per request.” It is startup condition evaluation.

---

### Q5. `@ConditionalOnClass` — does it load the class?

**Answer:** It checks availability without necessarily initializing your application code. Used so `RedisAutoConfiguration` can live in `spring-boot-autoconfigure` even when Redis is optional.

**Counter:** Why `name = "…"` as a String?  
**Answer:** So the auto-config module compiles without a hard dependency on the optional JAR.

---

### Q6. How do you write a custom starter?

**Answer:** Auto-config module with `@AutoConfiguration` + `AutoConfiguration.imports` + `@ConditionalOnMissingBean` + properties. Starter POM depends on it. App adds the starter.

**Counter:** Why not `@ComponentScan` the library?  
**Answer:** You would pull internal components, fight packages, and skip conditions. Imports + conditions are the Boot contract.

---

### Q7. How do you see why a bean was not created?

**Answer:** `--debug` / `debug=true` condition report, or `/actuator/conditions`.

---

### Q8. `@AutoConfigureAfter` vs `@DependsOn`?

**Answer:** `@AutoConfigureAfter` orders **configuration classes** during auto-config import. `@DependsOn` orders **bean instantiation**. Conditions like `OnBean` need the former.

---

### Q9. You added `starter-data-jpa` but no database URL. What happens?

**Answer:** Auto-config tries to create a `DataSource`. Embedded DB (H2) if on classpath; otherwise startup **fails** with a FailureAnalyzer telling you to add URL or exclude `DataSourceAutoConfiguration`.

---

### Q10. Difference between setting a property and excluding auto-config?

**Answer:** Property **configures** the auto-config beans (pool size, port). Exclude **prevents** those beans from being defined. If you still need a DataSource, you must define it yourself after exclude.

---

### Q11. What is `Jackson2ObjectMapperBuilderCustomizer` for?

**Answer:** To tweak JSON **without** replacing the entire `ObjectMapper` bean, so Boot’s modules and defaults stay.

**Counter:** What if you declare `@Bean ObjectMapper` anyway?  
**Answer:** You own it. Easy to drop `JavaTimeModule` and ParameterNamesModule. Prefer customizer.

---

### Q12. Can auto-config create beans after the app started?

**Answer:** No. It participates in `refresh()`. Later you can register beans programmatically, but that is not auto-config.

---

### Q13. `proxyBeanMethods = false` on auto-config?

**Answer:** Lite configuration — faster startup, no CGLIB config subclass. `@Bean` methods should take collaborators as **parameters**, not call sibling methods.

---

### Q14. How does this interact with profiles?

**Answer:** `@Profile` and `@ConditionalOnProperty` both work on auto-config classes. Profile-specific user `@Bean`s still win via `OnMissingBean` if they are registered.

---

### Q15. Name three `spring.factories` / imports types besides auto-config.

**Answer:** `EnvironmentPostProcessor`, `ApplicationListener`, `FailureAnalyzer`.
