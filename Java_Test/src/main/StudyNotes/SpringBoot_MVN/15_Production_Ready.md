# 15. Production-Ready Spring Boot

## Start here (simple English)

**In one sentence:** “It runs on my laptop” is not production. Production means: **logs you can search**, **a pool that does not melt the database**, **a shutdown that does not drop users**, and **a container that restarts cleanly**.

**Everyday picture:** Opening a shop.

| Shop need | Boot topic |
|-----------|------------|
| Cameras and receipts | JSON logs + correlation id (MDC) |
| Limited parking spots | Hikari connection pool (not one DB connection per HTTP thread) |
| “Sorry we are closing” then finish current customers | Graceful shutdown + Kubernetes readiness |
| Same shop design, different city | Profiles and env vars (chapter 04) |
| Fire alarm | Actuator health (chapter 13) |

You already met most of these pieces. This chapter **puts them on one checklist**.

Interview Q&A is **5–8 year standard**.

---

This chapter is the “we shipped it” list: logging, pools, shutdown, Docker layers, JVM flags, and 2 a.m. failures.

---

## 1. Logging and MDC

Boot default: **Logback** via `spring-boot-starter-logging`. Use `logback-spring.xml` (Spring profile extensions), not `logback.xml` if you want `<springProfile>`.

```xml
<!-- logback-spring.xml -->
<configuration>
  <include resource="org/springframework/boot/logging/logback/defaults.xml"/>
  <appender name="JSON" class="ch.qos.logback.core.ConsoleAppender">
    <encoder class="net.logstash.logback.encoder.LogstashEncoder"/>
  </appender>
  <root level="INFO">
    <appender-ref ref="JSON"/>
  </root>
</configuration>
```

In Kubernetes, **stdout JSON** → collector (Fluent Bit, OTel) → ELK/Loki. Don’t write rotating files inside ephemeral containers unless you know why.

**MDC** (Mapped Diagnostic Context) — thread-local fields on every log line:

```text
corrId, userId, traceId
```

Set `corrId` in a filter (chapter 06). Clear in `finally`. `@Async` must copy MDC (`TaskDecorator`).

Levels:

```yaml
logging:
  level:
    root: INFO
    com.acme.payments: INFO
    org.hibernate.SQL: WARN
```

`DEBUG` SQL in prod is a self-DDoS. Use Actuator loggers temporarily.

Never log payloads with PAN/PII/tokens. Guard with structured logging and allow-lists.

---

## 2. Graceful shutdown

```yaml
server:
  shutdown: graceful
spring:
  lifecycle:
    timeout-per-shutdown-phase: 30s
```

On SIGTERM (K8s preStop / `terminationGracePeriodSeconds`):

1. Boot stops accepting **new** HTTP
2. In-flight requests get `timeout-per-shutdown-phase`
3. Context closes: destroy beans, close Hikari

K8s: `preStop` sleep 2–5s so kube-proxy removes the pod from Endpoints **before** SIGTERM, plus readiness fail. Align `terminationGracePeriodSeconds` > shutdown timeout.

Without graceful shutdown: load balancer sends to a pod that is already dead → 502.

---

## 3. HikariCP sizing

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 10
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      keepalive-time: 120000
      leak-detection-threshold: 20000
```

**Formula people misuse:** `connections = ((core_count * 2) + effective_spindle_count)` (Hikari wiki). For a 4-core app talking to a shared Postgres with 100 max_connections and 10 app pods: **10–20 per pod**, not 200.

`connection-timeout` = how long a thread **waits for a free connection**. If Tomcat has 200 threads and pool is 20, 180 threads can sit here. That is a symptom of **slow queries** or **TX too wide** (HTTP inside `@Transactional`).

`max-lifetime` < DB/proxy idle timeout (AWS RDS often 350s wait_timeout stories — stay under).

Leak detection: logs stacks of connections not returned — usually a missing `close` or a stuck TX.

Actuator metric: `hikaricp.connections.active`, `.pending`, `.timeout`.

---

## 4. Tomcat / request threads

```yaml
server:
  tomcat:
    threads:
      max: 200
      min-spare: 10
    accept-count: 100
    connection-timeout: 20s
    max-http-form-post-size: 2MB
```

200 threads × blocking JDBC + pool 20 = 180 blocked. Scale **downstream** first (queries, pool), not Tomcat max.

Java 21 + Boot 3.2:

```yaml
spring:
  threads:
    virtual:
      enabled: true
```

Tomcat then uses virtual threads for requests. Blocking JDBC is cheaper in thread terms, but you can still **exhaust the DB pool**. Virtual threads do not remove Hikari limits.

---

## 5. Docker and layered JAR

Boot plugin:

```xml
<layers>
  <enabled>true</enabled>
</layers>
```

Extract in a Dockerfile:

```dockerfile
FROM eclipse-temurin:21-jre AS run
WORKDIR /app
COPY --from=extract /app/dependencies/ ./
COPY --from=extract /app/spring-boot-loader/ ./
COPY --from=extract /app/snapshot-dependencies/ ./
COPY --from=extract /app/application/ ./
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
```

Code changes rebuild the **thin** application layer; dependency layer stays cached.

Alternatively: `./mvnw spring-boot:build-image` (Buildpacks) — less Dockerfile control, good defaults.

**Don’t** run as root. **Don’t** copy the Maven `.m2` into the runtime image. **Do** set `-XX:MaxRAMPercentage=75.0` in containers instead of a hardcoded `-Xmx` when the cgroup limit is the source of truth.

```text
JAVA_TOOL_OPTIONS=-XX:MaxRAMPercentage=75.0 -XX:+UseG1GC
```

---

## 6. DevTools

`spring-boot-devtools`: restart + LiveReload + property defaults (template cache off).

The Boot plugin **excludes** it from the fat JAR by default. Still: never write production code that depends on DevTools classes. Don’t add it to the production Dockerfile classpath.

---

## 7. Config in Kubernetes

```text
Deployment env:
  SPRING_PROFILES_ACTIVE=prod
  SPRING_DATASOURCE_URL=jdbc:postgresql://payments-db:5432/payments
  SPRING_DATASOURCE_PASSWORD from secretKeyRef

optional:
  spring.config.import=optional:configtree:/etc/config/
```

Readiness/liveness as in chapter 13. Resource `requests/limits`. PDB so not all pods drain at once.

---

## 8. Common production failures (checklist)

| Symptom | Likely cause |
|---------|----------------|
| 502 on deploy | No graceful shutdown / readiness |
| Threads stuck, CPU idle | Waiting on Hikari / remote HTTP without timeout |
| `HikariPool ... Connection is not available` | Pool too small **or** TX too long **or** leak |
| `LazyInitializationException` | OSIV false + lazy in controller |
| N+1, 5s p99 | Missing entity graph |
| Duplicate scheduled work | Job on every replica |
| `NoSuchMethodError` | Maven mediation (chapter 00) |
| Random 401 after `@Async` | SecurityContext not propagated |
| Memory climbs | Cache without cap; unbounded queue; listener leak |
| “Works on my machine” YAML | Profile not active; env override |

HTTP clients: **always** set connect and read timeouts. Resilience4j for retries (idempotent only) and circuit breakers.

Flyway on deploy: run migrations **before** or at startup with a leadership story (two pods migrating at once — Flyway lock is usually OK; still monitor).

---

## 9. Observability recap

- JSON logs + MDC `traceId`
- Prometheus `/actuator/prometheus`
- Health probes split
- Tracing sampled
- Alerts on: error rate, p99 latency, Hikari pending, pod restarts, not on “CPU > 5%”

---

## 10. What “done” looks like for a service

1. Fat/layered JAR, non-root image
2. `SPRING_PROFILES_ACTIVE` from platform
3. Secrets from Secret, not git
4. Flyway + `ddl-auto=validate`
5. `open-in-view=false`
6. Timeouts on every outbound call
7. Bounded `@Async` pool
8. Scheduled jobs locked or externalized
9. Actuator on a locked port, probes correct
10. Graceful shutdown + K8s `terminationGracePeriodSeconds`
11. Tests: unit + slice + Testcontainers IT
12. `git.properties` / build-info on `/info`

---

# Interview Q&A (5–8 year bar)

A fresher knows `java -jar`. A 5–8 year answer walks SIGTERM, Hikari vs Tomcat threads, layered JAR, and a p99 debug path.

### Q1. How do you run Boot in production?

**Answer:** `java -jar` (or container ENTRYPOINT to `JarLauncher`) with env/profile, resource limits, graceful shutdown, actuator probes.

**Counter:** WAR on shared Tomcat?  
**Answer:** Supported via `SpringBootServletInitializer`, not the default for new services.

---

### Q2. Graceful shutdown — what happens on SIGTERM?

**Answer:** Stop new requests, wait for in-flight up to timeout, close context/pool. K8s must delay kill long enough and drop from LB first.

---

### Q3. How do you size Hikari?

**Answer:** From DB `max_connections` / number of pods / query time, not from Tomcat max threads. Watch `pending` and timeout metrics. Fix long TXs first.

**Counter:** Virtual threads so we can set pool to 500?  
**Answer:** The database disagrees. Pool is a **DB** resource.

---

### Q4. JSON logging vs `logging.file.name`?

**Answer:** In K8s, stdout. Files in containers vanish and are hard to scrape. Use a collector.

---

### Q5. What is MDC?

**Answer:** Thread-local map Logback prepends to every line. Correlation id, user, trace. Clear after the request. Copy across `@Async`.

---

### Q6. Layered JAR?

**Answer:** Splits dependencies vs application classes so Docker layer cache survives code-only changes.

---

### Q7. `MaxRAMPercentage` vs `-Xmx`?

**Answer:** In cgroup-limited containers, percent of **container** memory adapts. Hard `-Xmx` may exceed the limit → OOMKill, or waste.

---

### Q8. Why 502 during rolling update?

**Answer:** Pod received traffic after stop, or stopped before LB updated. Readiness + preStop + graceful shutdown.

---

### Q9. DevTools in prod?

**Answer:** Should not be on the classpath. Restart classloader and cache defaults are wrong for prod.

---

### Q10. How do you correlate logs and traces?

**Answer:** Micrometer Tracing + `traceId` in MDC + same id on outbound `traceparent`.

---

### Q11. Connection leak — how do you find it?

**Answer:** Hikari `leak-detection-threshold`, thread dump, look for `@Transactional` spanning WebClient, or unclosed `JdbcTemplate` connections in custom code.

---

### Q12. `open-in-view` in prod?

**Answer:** `false` for APIs. Fetch in the service.

---

### Q13. How do you rotate log levels in an incident?

**Answer:** Actuator `/loggers` (secured), or restart with env. Prefer actuator for minutes of DEBUG.

---

### Q14. Two DataSources in prod?

**Answer:** Two pools, two `EntityManagerFactory`s, named `transactionManager`s, `@Transactional(transactionManager="…")`. Don’t hope one annotation covers both DBs (chapter 10).

---

### Q15. Your p99 jumped after a release. Walk the debug path.

**Answer:** Metrics: `http.server.requests` by URI. Hikari active/pending. GC pauses. SQL (N+1). Downstream timers. Thread dump if threads blocked. Trace a single slow request. Don’t start with “add more pods” if the pool is saturated.

---

### Q16. Fat JAR vs Docker image?

**Answer:** Fat JAR is the artifact. Image is JAR + JRE + OS. Same app; image adds isolation and cgroup limits. Layered JAR makes images cheaper to rebuild.
