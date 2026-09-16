# 04. Spring Boot Configuration

## Start here (simple English)

**In one sentence:** **Do not hardcode** ports, database URLs, and passwords. Put them in files or environment variables. The same JAR can run on your laptop and in production with different settings.

**Everyday picture:** A washing machine.

- The **machine** (your JAR) is the same in every house.
- The **dial** (config) is different: cold water here, hot water there.
- You do not rebuild the machine to wash on a different setting.

**The two files you will see first:**

```properties
# application.properties  (simple key=value)
server.port=8080
```

```yaml
# application.yml  (tree-shaped, same meaning)
server:
  port: 8080
```

Pick **one** format per app. YAML is common for bigger configs.

**Profiles** = named dials: `dev`, `test`, `prod`. File `application-prod.yml` is used when `spring.profiles.active=prod`.

**Who wins if the same key is set twice?** Later / “closer to runtime” wins. Example: a command-line `--server.port=8081` beats the value inside the JAR. Full list is below.

**Prefer this for a group of keys:**

```java
@ConfigurationProperties(prefix = "app.mail")
public record MailProperties(String from, String host, int port) {}
```

`@Value("${server.port}")` is fine for **one** key. Not for a whole mail server block.

Interview Q&A at the end is **5–8 year standard** (order, relaxed binding, secrets).

---

Configuration is how the same artifact runs on a laptop, in CI, and in Kubernetes.

---

## 1. Externalized configuration

Boot reads the same keys from many places. Your code should depend on the **Environment**, not on “the yaml file”.

Typical keys:

```yaml
server.port: 8080
spring.datasource.url: jdbc:postgresql://...
app.mail.from: noreply@acme.com
```

Code:

```java
@ConfigurationProperties(prefix = "app.mail")
public record MailProperties(String from, String host, int port) {}
```

---

## 2. Property source order (later / higher wins)

**Simple version:** If the same key is set in two places, **the place closer to “how you started the process” wins**. Typing `--server.port=8081` beats a value inside `application.yml` inside the JAR. An env var on Kubernetes usually beats the packaged yaml too.

Boot 2.4+ **Config Data API** still follows a well-known precedence. Simplified **high → low** (high overrides):

1. DevTools global settings (dev only)
2. `@TestPropertySource` / `@SpringBootTest(properties=…)` / `@DynamicPropertySource`
3. **Command-line** args `--server.port=8081`
4. JNDI (rare)
5. Java system properties (`-Dserver.port=8081`)
6. OS **environment variables** (`SERVER_PORT=8081`)
7. Random value (`${random.uuid}`)
8. Application properties **outside** the JAR (`./config/`, `./`)
9. Profile-specific application properties **inside** the JAR (`application-prod.yml`)
10. Application properties inside the JAR (`application.yml`)
11. `@PropertySource` on `@Configuration`
12. Default properties (`SpringApplication.setDefaultProperties`)

Exact lists evolve; interviewers want this idea: **runtime flags beat env vars beat files; files outside the JAR beat packaged files; profile-specific beat generic.**

Inspect at runtime:

```text
GET /actuator/env          (secure it)
GET /actuator/configprops
```

Or log `environment.getProperty("server.port")`.

### Config data locations (2.4+)

Default search:

```text
file:./config/
file:./
classpath:/config/
classpath:/
```

`spring.config.import` pulls extra files or optional docs:

```yaml
spring:
  config:
    import: optional:file:./secrets.properties,optional:configtree:/etc/config/
```

`optional:` avoids startup failure if missing. Config trees (Kubernetes mounts): filename = key, file content = value.

`spring.config.location` **replaces** defaults if you set it. `spring.config.additional-location` **adds**. Prefer import + additional-location over replacing, unless you know why.

---

## 3. `application.properties` vs `application.yml`

Same Environment. YAML is hierarchical; properties are flat.

```properties
app.mail.host=smtp.acme.com
app.mail.port=587
```

```yaml
app:
  mail:
    host: smtp.acme.com
    port: 587
```

**Do not** ship both `application.properties` **and** `application.yml` for the same keys — order between them is defined but confusing in reviews.

YAML gotchas: `on`/`off`/`yes`/`no` can become booleans. Quote `"on"` if it is a string. Indentation matters.

---

## 4. Relaxed binding

Boot maps Environment keys to Java with relaxed rules.

```java
private int connectTimeoutMs;
```

Any of these bind:

```text
app.mail.connect-timeout-ms
app.mail.connectTimeoutMs
app.mail.connect_timeout_ms
APP_MAIL_CONNECTTIMEOUTMS      (env: underscore, collapsed)
```

Environment variable rules (important on Kubernetes):

- Replace `.` with `_`
- Dash removed / uppercased
- `SPRING_PROFILES_ACTIVE=prod`
- `SPRING_DATASOURCE_URL=jdbc:...`

**Lists:**

```yaml
app.servers:
  - a
  - b
```

or `APP_SERVERS_0=a`, `APP_SERVERS_1=b`.

---

## 5. Profiles

Spring profiles are **runtime** variants (not Maven profiles).

Files:

```text
application.yml              always loaded (except if you restrict)
application-dev.yml          if spring.profiles.active=dev
application-prod.yml
```

Activate:

```bash
java -jar app.jar --spring.profiles.active=prod
# or
SPRING_PROFILES_ACTIVE=prod,datadog
```

```yaml
spring:
  profiles:
    active: dev
```

**Do not** commit `spring.profiles.active=prod` inside the JAR as the only way. Inject it from the platform.

### Group and include (Boot 2.4+)

```yaml
spring:
  profiles:
    group:
      prod: prod-db, prod-mq
```

```yaml
# application-prod.yml
spring:
  config:
    activate:
      on-profile: prod
    import: ...
```

Legacy `spring.profiles.include` still seen; prefer groups + document order. **Last-wins** for the same key among active profile documents.

`@Profile("prod")` on a `@Bean` or `@Component` — bean only exists in that profile. Prefer properties + `@ConditionalOnProperty` when the class should exist but behave differently.

`@ActiveProfiles("test")` in tests.

---

## 6. `@Value` vs `@ConfigurationProperties`

### `@Value`

```java
@Value("${app.mail.from}")
private String from;
```

Good for: one flag, SpEL (`#{…}`), defaults (`${app.x:true}`).

Bad for: a cluster of keys, validation, nested maps, documenting config for the team.

### `@ConfigurationProperties` (preferred)

```java
@ConfigurationProperties(prefix = "app.mail")
@Validated
public record MailProperties(
    @NotBlank String from,
    @NotBlank String host,
    @Min(1) int port
) {}
```

Register:

```java
@SpringBootApplication
@EnableConfigurationProperties(MailProperties.class)
public class App {}
```

or `@ConfigurationPropertiesScan`, or `@Component` on a **mutable** class (records are usually registered via `@EnableConfigurationProperties`).

Generate metadata for IDE completion:

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-configuration-processor</artifactId>
  <optional>true</optional>
</dependency>
```

**Immutable / records:** Boot 3 binds constructor / records natively. No setters required.

**Validation:** `@Validated` on the properties class + Jakarta constraints. Fail **at startup** if `app.mail.from` is missing — better than NPE on first email.

**Nested:**

```java
public record AppProperties(Mail mail, Security security) {
    public record Mail(String from, String host) {}
    public record Security(String jwtSecret) {}
}
```

---

## 7. Environment API

```java
@Service
public class FeatureService {
    private final Environment env;

    public boolean enabled() {
        return env.getProperty("app.feature.x", Boolean.class, false);
    }
}
```

Prefer injecting `MailProperties` over scattering `env.getProperty`.

`Environment` merges all `PropertySource`s. `ConfigurableEnvironment.getPropertySources()` shows the stack — useful in a debug session.

---

## 8. Secrets

Never:

```yaml
spring:
  datasource:
    password: SuperSecret in git
```

Do:

| Where the app runs | How to inject |
|--------------------|----------------|
| Laptop | `application-local.yml` **gitignored**, or direnv / `.env` not committed |
| CI | Pipeline secret variables |
| Kubernetes | Secret volume (`configtree`) or env from Secret |
| Cloud | AWS Secrets Manager / GCP Secret Manager / Azure Key Vault via Spring Cloud AWS / etc. |
| HashiCorp Vault | Spring Vault / `spring.config.import=vault://` |

Actuator `/env` **sanitizes** some keys (`password`, `secret`, `key`) but do not expose `/env` publicly.

`spring.datasource.password` from env `SPRING_DATASOURCE_PASSWORD`.

Encryption-in-repo (`jasypt`) is a last resort; the encryption key still has to live somewhere.

---

## 9. Common Boot properties you should recognize

```yaml
server.port: 8080
server.shutdown: graceful

spring:
  application:
    name: payments
  datasource:
    url: jdbc:postgresql://db:5432/payments
    username: payments
    hikari:
      maximum-pool-size: 20
  jpa:
    open-in-view: false
    hibernate:
      ddl-auto: validate
  jackson:
    default-property-inclusion: non_null
  threads:
    virtual:
      enabled: true   # Boot 3.2+

logging:
  level:
    com.acme.payments: INFO
    org.hibernate.SQL: WARN
```

`spring.jpa.hibernate.ddl-auto`: `none` / `validate` in prod. `update` is a trap. Migrations = Flyway/Liquibase (chapter 09).

`spring.jpa.open-in-view`: **false** in APIs you control. OSIV keeps the session open during view rendering and hides N+1 until production load.

---

## 10. Testing configuration

```java
@SpringBootTest
@ActiveProfiles("test")
class OrderIT { }
```

`src/test/resources/application-test.yml` overrides. `@TestPropertySource(properties = "app.mail.host=dummy")` for one test.

Testcontainers:

```java
@DynamicPropertySource
static void db(DynamicPropertyRegistry r) {
    r.add("spring.datasource.url", postgres::getJdbcUrl);
    r.add("spring.datasource.username", postgres::getUsername);
    r.add("spring.datasource.password", postgres::getPassword);
}
```

This registry is **high precedence** — it beats `application-test.yml`.

---

## 11. `@Configuration` + `@Bean` (manual beans)

When auto-config is not enough:

```java
@Configuration
public class HttpClientConfig {
    @Bean
    RestTemplate paymentsRestTemplate(RestTemplateBuilder builder, MailProperties ignored) {
        return builder
            .connectTimeout(Duration.ofSeconds(2))
            .readTimeout(Duration.ofSeconds(5))
            .build();
    }
}
```

Prefer `RestTemplateBuilder` / `WebClient.Builder` from Boot so you keep metrics and message converters.

`@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "true")` on a `@Bean` or `@Configuration` for optional integrations.

---

## 12. Production pitfalls

1. Packaging `application-prod.yml` with real passwords.
2. Mixing YAML booleans (`no` parsed as false).
3. Expecting Kubernetes env `server.port` to work — use `SERVER_PORT` or `SPRING_APPLICATION_JSON`.
4. Two profile files setting the same key — “last active profile wins”, people guess wrong.
5. `spring.config.location` wiping classpath `application.yml` accidentally.
6. `@Value` on a static field — does not inject.
7. Changing a `@ConfigurationProperties` field at runtime and expecting other nodes to see it — it is process-local.
8. Using `@PropertySource` for YAML without `YamlPropertySourceFactory`.

---

# Interview Q&A (5–8 year bar)

A fresher knows `application.yml`. A 5–8 year answer knows **which source won**, relaxed env names, and why `/actuator/env` is the debugger.

### Q1. How does Boot externalize config?

**Answer:** Keys in Environment from files, env vars, command line, imports, etc. Code binds via `@ConfigurationProperties` or `@Value`.

**Counter:** Which wins: env var or `application.yml`?  
**Answer:** Environment variable (higher precedence).

**Counter:** File outside the JAR vs inside?  
**Answer:** Outside wins (`./config/application.yml` over packaged).

---

### Q2. Full precedence you remember?

**Answer:** Command line > system properties > OS env > random > external files > packaged profile-specific files > packaged generic files > `@PropertySource` > defaults. Tests and DevTools sit at the top when present.

**Trap:** Reciting a 17-row table wrong. The **idea** of override order matters more; say you confirm with `/actuator/env`.

---

### Q3. How do you activate a profile?

**Answer:** `spring.profiles.active`, `--spring.profiles.active=`, env `SPRING_PROFILES_ACTIVE`. Multiple comma-separated.

**Counter:** Maven profile vs Spring profile?  
**Answer:** Build vs runtime (chapter 00).

**Counter:** How do you make a bean prod-only?  
**Answer:** `@Profile("prod")` or `@ConditionalOnProperty`.

---

### Q4. `@Value` vs `@ConfigurationProperties`?

**Answer:** One key vs typed prefix group with validation and metadata. Prefer properties classes for anything more than a single flag.

**Counter:** Does `@ConfigurationProperties` support SpEL?  
**Answer:** No. `@Value` does. That is a reason to keep `@Value` for expressions, not for config blocks.

---

### Q5. What is relaxed binding?

**Answer:** `app.connect-timeout`, `app.connectTimeout`, `APP_CONNECTTIMEOUT` all map to `connectTimeout`. Designed so env vars can bind.

**Counter:** Why did my K8s env not bind?  
**Answer:** Wrong underscore form. Check Boot relaxed-binding docs for lists and indexes (`APP_FOO_0_BAR`).

---

### Q6. How do you keep secrets out of git?

**Answer:** Env vars, K8s Secrets / configtree, cloud secret managers, Vault. Sanitize actuator. Gitignore local files.

**Counter:** Is encrypting passwords in `application.yml` with Jasypt enough?  
**Answer:** Only if key management is real. The key cannot sit in the same repo.

---

### Q7. `spring.config.import` vs `spring.config.location`?

**Answer:** `import` adds documents (files, vault, configtree). `location` **replaces** the default search locations if set. Prefer `import` / `additional-location`.

---

### Q8. How do you bind a YAML map to a class?

**Answer:** `@ConfigurationProperties` with nested records / classes or `Map<String, …>`. Enable the class as a bean. Add the configuration processor for hints.

**Counter:** Record without `@EnableConfigurationProperties` — is it a bean?  
**Answer:** Not unless registered. Binding ≠ bean registration.

---

### Q9. `ddl-auto=update` in prod?

**Answer:** No. Use Flyway/Liquibase. `validate` or `none` in prod.

---

### Q10. `open-in-view`?

**Answer:** Default `true` for MVC. Keeps JPA session open for the view. Hides lazy-loading in controllers, causes N+1 and connection hold. Set `false` for REST APIs.

---

### Q11. How do tests override config?

**Answer:** `application-test.yml` + `@ActiveProfiles`, `@TestPropertySource`, `@SpringBootTest(properties=…)`, `@DynamicPropertySource` (highest of these).

---

### Q12. Can you change port without rebuilding?

**Answer:** Yes — env, command line, external config. That is the point of externalization.

**Counter:** Can you change it **after** the server started?  
**Answer:** Not `server.port`. The connector is already bound. Restart (or a gateway in front).

---

### Q13. YAML and `application.properties` together?

**Answer:** Both load; precedence between them is easy to get wrong in a review. Pick one format per app.

---

### Q14. How do you debug “property not taking effect”?

**Answer:** `/actuator/env` and see which source won. Check profile not active, relaxed name, YAML indent, `@ConfigurationProperties` prefix typo, and whether you read the field before binding (static / too-early init).

---

### Q15. Spring Cloud Config — where does it fit?

**Answer:** A config **server** (git/vault) that clients import at startup (`spring.config.import=configserver:`). Same Environment afterwards. Refresh (`@RefreshScope`) is Cloud, not Boot core. Mention it; don’t claim Boot does live refresh alone.
