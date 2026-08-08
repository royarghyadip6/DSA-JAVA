# 57. Spring Boot

## 57. Spring Boot

## Basics

---

# 1. What is Spring Boot?

<details>
<summary>Show Answer</summary>

**Answer:**

**Spring Boot** is an extension of the Spring Framework that simplifies building **production-ready** applications with **minimal configuration**, **embedded servers**, and **auto-configuration**.

```java
@SpringBootApplication
public class MyApp {
    public static void main(String[] args) {
        SpringApplication.run(MyApp.class, args);  // that's it — app runs
    }
}
```

```text
Spring Framework = powerful but needs lots of XML/Java config
Spring Boot      = opinionated defaults + auto-config + starters
                 = get running in minutes, not days
```

| Feature | Detail |
|---------|--------|
| **Auto-configuration** | Beans created based on classpath |
| **Starters** | One dependency pulls related libs |
| **Embedded server** | Tomcat/Jetty/Undertow built-in |
| **Actuator** | Health, metrics, monitoring endpoints |
| **No XML** | Convention over configuration |

**Interview Point:**

> Spring Boot = Spring + auto-config + starters + embedded server. Build standalone, production-ready apps with minimal setup.

</details>

---

# 2. Why Spring Boot?

<details>
<summary>Show Answer</summary>

**Answer:**

| Problem (Plain Spring) | Spring Boot Solution |
|------------------------|---------------------|
| Lots of XML/Java config | Auto-configuration |
| Manual dependency versions | Starter POMs with tested versions |
| Deploy WAR to external Tomcat | Embedded Tomcat — run as JAR |
| Boilerplate setup code | `@SpringBootApplication` does it all |
| No built-in monitoring | Actuator endpoints |
| Dependency version conflicts | Spring Boot BOM manages versions |

```text
Before Boot:  pom.xml + web.xml + dispatcher-servlet.xml + server setup
With Boot:    one @SpringBootApplication + application.properties
```

**Interview Point:**

> Boot removes boilerplate — auto-config, starters, embedded server, opinionated defaults. Faster development and deployment.

</details>

---

# 3. Difference between Spring and Spring Boot?

<details>
<summary>Show Answer</summary>

**Answer:**

| Aspect | Spring Framework | Spring Boot |
|--------|-----------------|-------------|
| **Configuration** | Manual — XML or @Configuration | Auto-configuration |
| **Dependencies** | Pick versions yourself | Starters + BOM |
| **Server** | Deploy WAR to external server | Embedded server in JAR |
| **Boilerplate** | More setup code | Minimal — convention over config |
| **Use case** | Fine-grained control | Rapid development |
| **Relationship** | Core framework | Built ON TOP of Spring |

```text
Spring Boot does NOT replace Spring
Spring Boot USES Spring Framework + adds convenience layer
You can use Spring without Boot (rare in new projects)
```

**Interview Point:**

> Spring = core IoC, DI, AOP. Spring Boot = Spring + auto-config + starters + embedded server. Boot is not a replacement — it's an accelerator.

</details>

---

# 4. Advantages of Spring Boot?

<details>
<summary>Show Answer</summary>

**Answer:**

| Advantage | Benefit |
|-----------|---------|
| **Rapid development** | Project running in minutes |
| **Auto-configuration** | Smart defaults based on classpath |
| **Starters** | One dependency, many libraries |
| **Embedded server** | `java -jar app.jar` — no external Tomcat |
| **Production-ready** | Actuator for health, metrics |
| **Microservices friendly** | Lightweight, cloud-native |
| **No XML** | Java + properties/yml only |
| **DevTools** | Hot reload during development |
| **Testing support** | `@SpringBootTest`, test slices |

```bash
# Deploy anywhere — just a JAR
java -jar myapp.jar --spring.profiles.active=prod
```

**Interview Point:**

> Key advantages: speed, auto-config, starters, embedded server, actuator, cloud-ready. Industry standard for new Java backend projects.

</details>

---

## Auto Configuration

---

# 5. What is auto-configuration?

<details>
<summary>Show Answer</summary>

**Answer:**

**Auto-configuration** automatically creates and configures beans based on **classpath dependencies**, **existing beans**, and **property settings** — you don't write config for common setups.

```text
Classpath has spring-boot-starter-data-jpa + H2 driver
  → Boot auto-configures DataSource, EntityManagerFactory, TransactionManager

Classpath has spring-boot-starter-web
  → Boot auto-configures DispatcherServlet, embedded Tomcat, Jackson

You add spring-boot-starter-security
  → Boot auto-configures security filter chain, login page
```

```java
// You write this
@SpringBootApplication
public class App { }

// Boot automatically configures:
// - DataSource (if JDBC on classpath)
// - JpaTransactionManager (if JPA on classpath)
// - Tomcat (if web starter on classpath)
```

**Interview Point:**

> Auto-config = conditional bean setup based on what's on classpath. `@ConditionalOnClass`, `@ConditionalOnMissingBean` drive it. Override by defining your own bean.

</details>

---

# 6. How auto-configuration works?

<details>
<summary>Show Answer</summary>

**Answer:**

```text
1. @SpringBootApplication includes @EnableAutoConfiguration
2. Spring Boot reads META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
   (older: META-INF/spring.factories)
3. Each auto-config class has @Conditional annotations
4. Conditions checked: classpath? property? existing bean?
5. If conditions match → beans registered automatically
6. Your @Bean definitions override auto-config ( @ConditionalOnMissingBean )
```

```java
// Simplified auto-config class (internal)
@Configuration
@ConditionalOnClass(DataSource.class)
@ConditionalOnMissingBean(DataSource.class)
public class DataSourceAutoConfiguration {
    @Bean
    public DataSource dataSource(DataSourceProperties props) {
        return DataSourceBuilder.create().build();
    }
}
```

| Condition Annotation | Checks |
|---------------------|--------|
| `@ConditionalOnClass` | Class on classpath? |
| `@ConditionalOnMissingBean` | Bean not already defined? |
| `@ConditionalOnProperty` | Property set? |
| `@ConditionalOnWebApplication` | Web app? |

**Interview Point:**

> Auto-config = conditional @Configuration classes loaded from spring.factories/AutoConfiguration.imports. Conditions decide what gets configured. Your beans take priority.

</details>

---

# 7. @EnableAutoConfiguration?

<details>
<summary>Show Answer</summary>

**Answer:**

`@EnableAutoConfiguration` tells Spring Boot to **enable auto-configuration** — load and apply all auto-config classes from classpath.

```java
@SpringBootApplication
// equals:
@Configuration
@EnableAutoConfiguration  // ← triggers auto-config
@ComponentScan
public class App { }
```

```java
// Exclude specific auto-config if needed
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class App { }
```

```properties
# Exclude via properties
spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
```

**Interview Point:**

> @EnableAutoConfiguration = switch on auto-config. Included in @SpringBootApplication. Exclude unwanted configs via exclude attribute or properties.

</details>

---

# 8. Spring Factories mechanism?

<details>
<summary>Show Answer</summary>

**Answer:**

Spring Boot discovers auto-configuration classes via files in `META-INF`:

```text
Spring Boot 2.x:  META-INF/spring.factories
Spring Boot 3.x:  META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

```properties
# spring.factories (Boot 2.x style)
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
  org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,\
  org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration,\
  org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
```

```text
How it works:
  1. @EnableAutoConfiguration triggers factory loading
  2. Spring reads all spring.factories / AutoConfiguration.imports from all JARs
  3. Collects list of auto-config classes
  4. Applies @Conditional checks on each
  5. Registers matching configurations
```

**Interview Point:**

> Auto-config classes listed in spring.factories (Boot 2) or AutoConfiguration.imports (Boot 3). Spring scans all JARs on classpath at startup.

</details>

---

## Starter Dependencies

---

# 9. What are starters?

<details>
<summary>Show Answer</summary>

**Answer:**

**Starters** are **curated dependency bundles** — one Maven/Gradle dependency pulls in everything needed for a feature.

```xml
<!-- One starter = web + Tomcat + Jackson + Spring MVC -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- One starter = JPA + Hibernate + JDBC + Transaction -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```

| Starter | Brings In |
|---------|-----------|
| `spring-boot-starter-web` | Spring MVC, Tomcat, Jackson |
| `spring-boot-starter-data-jpa` | JPA, Hibernate, JDBC |
| `spring-boot-starter-security` | Spring Security |
| `spring-boot-starter-test` | JUnit, Mockito, AssertJ |
| `spring-boot-starter-actuator` | Health, metrics endpoints |

**Interview Point:**

> Starters = one dependency for a feature stack. No version conflicts — managed by Spring Boot parent BOM.

</details>

---

# 10. Why starters introduced?

<details>
<summary>Show Answer</summary>

**Answer:**

Before starters, developers had to **manually pick compatible versions** of many libraries — painful and error-prone.

```text
Problem:
  Need web app → add spring-webmvc 5.3.x, tomcat 9.x, jackson 2.13.x
  Version mismatch → ClassNotFoundException, NoSuchMethodError
  Hours wasted on dependency hell

Solution:
  spring-boot-starter-web → Boot team tested all versions together
  One line in pom.xml → everything works
```

| Benefit | Detail |
|---------|--------|
| **Version management** | BOM ensures compatible versions |
| **Less pom.xml** | One starter vs 10 dependencies |
| **Tested combinations** | Spring team validates the stack |
| **Discoverability** | Name tells you what you get |

**Interview Point:**

> Starters solve dependency hell — curated, version-tested bundles. Parent POM / BOM manages all versions centrally.

</details>

---

## Frequently Asked

---

# 11. @SpringBootApplication?

<details>
<summary>Show Answer</summary>

**Answer:**

`@SpringBootApplication` is a **convenience annotation** that combines three annotations — the **entry point** for every Spring Boot app.

```java
@SpringBootApplication
public class OrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }
}
```

```text
@SpringBootApplication =
    @Configuration          → Java config source
  + @EnableAutoConfiguration → enable auto-config
  + @ComponentScan          → scan current package + sub-packages
```

```java
// Equivalent explicit form
@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = "com.myapp")
public class OrderApplication { }
```

**Interview Point:**

> @SpringBootApplication = @Configuration + @EnableAutoConfiguration + @ComponentScan. Main class annotation for Boot apps.

</details>

---

# 12. Components inside @SpringBootApplication?

<details>
<summary>Show Answer</summary>

**Answer:**

| Annotation | Purpose |
|------------|---------|
| **@Configuration** | Class can define `@Bean` methods |
| **@EnableAutoConfiguration** | Enable Spring Boot auto-config |
| **@ComponentScan** | Scan for @Component, @Service, @Repository, @Controller |

```java
@SpringBootApplication(scanBasePackages = "com.myapp")
// or exclude auto-config:
@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
```

```text
@ComponentScan default: scans package of annotated class + all sub-packages
  com.myapp.OrderApplication → scans com.myapp.* and com.myapp.service.* etc.
  Does NOT scan com.otherpackage — put main class in root package
```

**Interview Point:**

> Three in one: @Configuration, @EnableAutoConfiguration, @ComponentScan. Place main class in root package so sub-packages are scanned.

</details>

---

# 13. Embedded Tomcat?

<details>
<summary>Show Answer</summary>

**Answer:**

Spring Boot includes **Tomcat as an embedded server** inside the JAR — no need to install or deploy to external Tomcat.

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <!-- includes embedded Tomcat by default -->
</dependency>
```

```bash
# Build fat JAR
mvn package
# Run — Tomcat starts inside the JAR on port 8080
java -jar myapp.jar
```

```properties
server.port=9090
server.servlet.context-path=/api
```

| Server | Starter |
|--------|---------|
| **Tomcat** (default) | spring-boot-starter-web |
| **Jetty** | exclude Tomcat, add jetty starter |
| **Undertow** | exclude Tomcat, add undertow starter |

**Interview Point:**

> Embedded Tomcat = server inside JAR. `java -jar` starts app + server. Default port 8080. Swap server via Maven exclusions.

</details>

---

# 14. Can Spring Boot run without Tomcat?

<details>
<summary>Show Answer</summary>

**Answer:**

**Yes** — several ways:

### 1. Non-web application (no server at all)

```xml
<!-- No web starter = no embedded server -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter</artifactId>  <!-- core only -->
</dependency>
```

### 2. Different embedded server

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <exclusions>
        <exclusion>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-tomcat</artifactId>
        </exclusion>
    </exclusions>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jetty</artifactId>
</dependency>
```

### 3. Deploy as WAR to external server

```java
@SpringBootApplication
public class App extends SpringBootServletInitializer {
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(App.class);
    }
}
```

**Interview Point:**

> Boot can run without Tomcat: use non-web starter (CLI/batch), swap to Jetty/Undertow, or deploy WAR to external server.

</details>

---

## Profiles

---

# 15. What are Profiles?

<details>
<summary>Show Answer</summary>

**Answer:**

**Profiles** let you define **environment-specific configuration** — dev, test, staging, prod — and activate the right one at runtime.

```properties
# application.properties (common)
app.name=OrderService

# application-dev.properties
spring.datasource.url=jdbc:h2:mem:devdb
logging.level.root=DEBUG

# application-prod.properties
spring.datasource.url=jdbc:postgresql://prod-db:5432/orders
logging.level.root=WARN
```

```bash
# Activate profile
java -jar app.jar --spring.profiles.active=prod

# Or in application.properties
spring.profiles.active=dev
```

```java
@Profile("dev")
@Service
public class MockPaymentService implements PaymentService { }

@Profile("prod")
@Service
public class RealPaymentService implements PaymentService { }
```

**Interview Point:**

> Profiles = environment-specific config and beans. Activate via `spring.profiles.active`. Files: application-{profile}.properties.

</details>

---

# 16. application.properties vs application.yml?

<details>
<summary>Show Answer</summary>

**Answer:**

Both configure Spring Boot apps — **same purpose**, different format.

### application.properties

```properties
server.port=8080
spring.datasource.url=jdbc:postgresql://localhost/mydb
spring.datasource.username=admin
spring.jpa.hibernate.ddl-auto=update
```

### application.yml

```yaml
server:
  port: 8080
spring:
  datasource:
    url: jdbc:postgresql://localhost/mydb
    username: admin
  jpa:
    hibernate:
      ddl-auto: update
```

| | properties | yml |
|---|-----------|-----|
| **Format** | Flat key=value | Hierarchical YAML |
| **Readability** | Fine for few props | Better for nested config |
| **Priority** | Lower if both exist | Higher if both exist |
| **Industry** | Both widely used | yml more popular in microservices |

**Interview Point:**

> Both work — same config keys. YAML is more readable for nested settings. If both present, yml overrides properties.

</details>

---

# 17. Environment specific configuration?

<details>
<summary>Show Answer</summary>

**Answer:**

Spring Boot supports multiple ways to manage **per-environment config**:

```text
1. Profile-specific files:
   application-dev.properties
   application-prod.properties

2. Environment variables:
   SPRING_DATASOURCE_URL=jdbc:postgresql://prod-db/orders

3. Command-line args:
   java -jar app.jar --server.port=9090

4. External config file:
   java -jar app.jar --spring.config.location=file:/etc/myapp/

5. Cloud config server (Spring Cloud Config)
```

### Property Priority (highest wins)

```text
1. Command-line arguments
2. Java System properties
3. OS environment variables
4. application-{profile}.properties
5. application.properties
```

```properties
# prod profile
spring.profiles.active=prod
spring.datasource.url=${DB_URL}  # from env variable
```

**Interview Point:**

> Use profiles + profile-specific files for env config. Externalize secrets via env vars. Command-line args override everything.

</details>

---

## Actuator

---

# 18. What is Actuator?

<details>
<summary>Show Answer</summary>

**Answer:**

**Spring Boot Actuator** adds **production-ready monitoring and management endpoints** — health checks, metrics, env info, without writing custom code.

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

```properties
management.endpoints.web.exposure.include=health,info,metrics,env
management.endpoint.health.show-details=always
```

| Endpoint | Purpose |
|----------|---------|
| `/actuator/health` | App health status |
| `/actuator/metrics` | Performance metrics |
| `/actuator/info` | App info (version, etc.) |
| `/actuator/env` | Environment properties |
| `/actuator/loggers` | View/change log levels |

```text
Used in production for:
  Kubernetes liveness/readiness probes
  Monitoring dashboards (Prometheus, Grafana)
  Ops team health checks
```

**Interview Point:**

> Actuator = production monitoring endpoints. Health, metrics, env. Secure them in production — don't expose all endpoints publicly.

</details>

---

# 19. Health endpoint?

<details>
<summary>Show Answer</summary>

**Answer:**

`/actuator/health` reports the **application's health status** — used by load balancers and Kubernetes for liveness/readiness probes.

```json
// GET /actuator/health
{
  "status": "UP",
  "components": {
    "db": { "status": "UP", "details": { "database": "PostgreSQL" } },
    "diskSpace": { "status": "UP", "details": { "free": 5368709120 } },
    "ping": { "status": "UP" }
  }
}
```

```properties
management.endpoint.health.show-details=when-authorized
management.health.db.enabled=true
```

| Status | Meaning |
|--------|---------|
| **UP** | Component healthy |
| **DOWN** | Component failed (DB unreachable) |
| **OUT_OF_SERVICE** | Intentionally disabled |
| **UNKNOWN** | Cannot determine |

```yaml
# Kubernetes readiness probe
readinessProbe:
  httpGet:
    path: /actuator/health
    port: 8080
```

**Interview Point:**

> /actuator/health = UP/DOWN status. Checks DB, disk, custom components. Critical for K8s probes and load balancer health checks.

</details>

---

# 20. Metrics endpoint?

<details>
<summary>Show Answer</summary>

**Answer:**

`/actuator/metrics` exposes **application performance metrics** — JVM, HTTP requests, DB connections, custom business metrics.

```bash
# List all metrics
GET /actuator/metrics

# Specific metric
GET /actuator/metrics/jvm.memory.used
GET /actuator/metrics/http.server.requests
```

```json
{
  "name": "http.server.requests",
  "measurements": [
    { "statistic": "COUNT", "value": 1523 },
    { "statistic": "TOTAL_TIME", "value": 45.2 }
  ],
  "availableTags": [
    { "tag": "uri", "values": ["/api/orders", "/api/users"] },
    { "tag": "status", "values": ["200", "404", "500"] }
  ]
}
```

```java
// Custom business metric
@Service
public class OrderService {
    private final Counter orderCounter;

    public OrderService(MeterRegistry registry) {
        this.orderCounter = registry.counter("orders.created");
    }

    public Order create(Order o) {
        orderCounter.increment();
        return repo.save(o);
    }
}
```

**Interview Point:**

> /actuator/metrics = JVM, HTTP, DB metrics. Integrate with Prometheus via micrometer. Add custom metrics with MeterRegistry.

</details>

---

## Advanced

---

# 21. How Spring Boot starts internally?

<details>
<summary>Show Answer</summary>

**Answer:**

```text
1. main() calls SpringApplication.run(App.class, args)
2. Create SpringApplication instance
3. Deduce application type (Servlet, Reactive, None)
4. Load ApplicationContextInitializers
5. Load ApplicationListeners
6. Prepare Environment (properties, profiles, args)
7. Print banner
8. Create ApplicationContext (AnnotationConfigServletWebServerApplicationContext)
9. Prepare context — register sources, load bean definitions
10. Refresh context:
    - Process @Configuration classes
    - Run BeanFactoryPostProcessors
    - Apply @EnableAutoConfiguration — load auto-config classes
    - Component scan
    - Instantiate singleton beans
    - Start embedded web server (Tomcat)
11. Call ApplicationRunner / CommandLineRunner beans
12. Application ready — accepting requests
```

```java
@SpringBootApplication
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
        // Steps 1-12 happen inside this one call
    }
}
```

**Interview Point:**

> Boot startup: SpringApplication.run → create context → auto-config → component scan → create beans → start embedded server → ready.

</details>

---

# 22. What happens when SpringApplication.run() executes?

<details>
<summary>Show Answer</summary>

**Answer:**

`SpringApplication.run()` is the **single entry point** that bootstraps the entire application:

```java
public static ConfigurableApplicationContext run(Class<?> primarySource, String... args) {
    return new SpringApplication(primarySource).run(args);
}
```

### Detailed Steps

```text
Phase 1 — Setup:
  ├── Create SpringApplication
  ├── Set web application type (SERVLET / REACTIVE / NONE)
  ├── Load spring.factories listeners & initializers
  └── Create and configure Environment (merge all property sources)

Phase 2 — Context Creation:
  ├── Create ApplicationContext
  ├── Apply ApplicationContextInitializers
  ├── Load primary @Configuration source (your @SpringBootApplication class)
  └── Refresh context (the big step)

Phase 3 — Context Refresh:
  ├── Parse @Configuration classes
  ├── Process @EnableAutoConfiguration (conditional auto-config beans)
  ├── Component scan (@Component, @Service, etc.)
  ├── Register BeanDefinitions
  ├── Instantiate all singleton beans (constructor injection)
  ├── Run BeanPostProcessors (@Autowired, @PostConstruct)
  └── Start embedded Tomcat (WebServerStartStopLifecycle)

Phase 4 — Ready:
  ├── Publish ApplicationReadyEvent
  ├── Execute ApplicationRunner / CommandLineRunner
  └── Return ApplicationContext — app is LIVE
```

```java
@Component
public class StartupRunner implements ApplicationRunner {
    @Override
    public void run(ApplicationArguments args) {
        log.info("App started with profiles: {}", args.getOptionNames());
    }
}
```

**Interview Point:**

> run() = create SpringApplication → build Environment → refresh ApplicationContext → auto-config + scan + create beans → start Tomcat → run ApplicationRunners → app live.

</details>

---

# 5–8 Year Interview Rapid Fire

### Q: How to disable auto-configuration?

<details>
<summary>Show Answer</summary>

**Answer:**

`@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)` or `spring.autoconfigure.exclude` in properties.

</details>

---

### Q: spring-boot-starter-parent purpose?

<details>
<summary>Show Answer</summary>

**Answer:**

Parent POM that provides **dependency version management (BOM)**, default plugin config, and Java version — no version numbers needed in dependencies.

</details>

---

### Q: Fat JAR vs Thin JAR?

<details>
<summary>Show Answer</summary>

**Answer:**

**Fat/uber JAR** = your code + all dependencies + embedded server in one JAR. Spring Boot default. **Thin JAR** = only your code; dependencies separate.

</details>

---

### Q: Default Spring Boot port?

<details>
<summary>Show Answer</summary>

**Answer:**

**8080** — change with `server.port` in properties or `--server.port=9090`.

</details>

---

### Q: Actuator endpoints secure in production?

<details>
<summary>Show Answer</summary>

**Answer:**

**Must secure them** — expose only needed endpoints, use Spring Security, don't expose `/actuator/env` or `/actuator/beans` publicly. Health endpoint often allowed for load balancers.

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> Spring Boot = Spring + auto-config + starters + embedded server. @SpringBootApplication = @Configuration + @EnableAutoConfiguration + @ComponentScan. Auto-config via spring.factories with @Conditional. Starters = curated dependency bundles. Profiles for env-specific config. Actuator for health/metrics. SpringApplication.run() → context refresh → beans → Tomcat → ready.

</details>
