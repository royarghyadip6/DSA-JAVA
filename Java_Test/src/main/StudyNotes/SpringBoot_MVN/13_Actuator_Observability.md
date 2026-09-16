# 13. Actuator and Observability

## Start here (simple English)

**In one sentence:** Actuator is a **health and metrics panel** for the running app. Kubernetes and operators use it. Regular users of your API should **not**.

**Everyday picture:** A hospital patient monitor.

- **Liveness** = “is the heart still beating?” If no → **restart** the pod
- **Readiness** = “can this patient take visitors (traffic)?” If the database is down → **stop sending requests**, but maybe **don’t** kill the JVM
- **Metrics** = pulse, temperature over time (how many requests, how slow)
- **Tracing** = follow one patient through several wards (one request through several services)

**You add:**

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

Then (after you **expose** them) paths like `/actuator/health`. **Never** expose `heapdump` and `env` to the public internet.

Interview Q&A is **5–8 year standard**.

---

Production-ready is not “the JAR starts”. Ops must know **is this instance alive**, **can it take traffic**, **what is slow**, and **where a request went**.

---

## 1. What Actuator is

`spring-boot-starter-actuator` exposes **operational endpoints** on a management port/path: health, metrics, info, env, loggers, mappings, beans, threaddump, heapdump, …

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,loggers
  endpoint:
    health:
      probes:
        enabled: true
      show-details: when_authorized
  server:
    port: 8081   # optional separate port
```

**Never** `include: '*'` on the public internet. `heapdump`, `env`, `beans`, `shutdown` are sensitive.

Security: chapter 11 — separate `SecurityFilterChain` for `/actuator/**`.

---

## 2. Health, liveness, readiness (Kubernetes)

| Probe | Question | Boot endpoint |
|-------|----------|----------------|
| Liveness | Should the platform **kill and restart** this JVM? | `/actuator/health/liveness` |
| Readiness | Should the **load balancer send traffic**? | `/actuator/health/readiness` |
| Health | Aggregated view for humans | `/actuator/health` |

```yaml
management:
  endpoint:
    health:
      probes:
        enabled: true
      group:
        readiness:
          include: readinessState,db,redis
```

**Liveness** should be **cheap and process-local** (deadlock, broken internal state). Do **not** ping the database on liveness — a DB blip would restart every pod and make the outage worse.

**Readiness** **should** include DB, Redis, broker — if Postgres is down, stop sending HTTP that will 500.

States: `UP`, `DOWN`, `OUT_OF_SERVICE`, `UNKNOWN`.

Custom:

```java
@Component
public class PaymentsHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        return pingDownstream()
            ? Health.up().withDetail("payments", "reachable").build()
            : Health.down().withDetail("payments", "timeout").build();
    }
}
```

Prefer `ReactiveHealthIndicator` in WebFlux.

`HealthIndicator` named `db` is auto-registered from `DataSource`. Don’t duplicate it.

During startup, readiness is **OUT_OF_SERVICE** until `ApplicationReadyEvent`. That is why K8s should not route until readiness passes — not merely until the container started.

---

## 3. Info, loggers, mappings

- `/actuator/info` — from `info.*` properties, git plugin (`git-commit-id-plugin`), build info (`build-info` goal of Boot plugin).
- `/actuator/loggers` — change `com.acme.payments` to DEBUG **without restart** (ops feature; secure it).
- `/actuator/mappings` — every MVC mapping. Debug 404s.
- `/actuator/beans` — context dump. Debug wiring. Sensitive.
- `/actuator/conditions` — auto-config report (chapter 05).

---

## 4. Metrics (Micrometer)

Actuator’s `/actuator/metrics` is a snapshot. Prometheus scrapes `/actuator/prometheus` (`micrometer-registry-prometheus`).

```xml
<dependency>
  <groupId>io.micrometer</groupId>
  <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

Auto: JVM (memory, GC, threads), Tomcat, Hikari (`hikaricp.connections`), HTTP server requests (`http.server.requests`), extras if you use `RestTemplate`/`WebClient` from builders.

Custom:

```java
@Service
public class OrderService {
    private final Counter created;

    public OrderService(MeterRegistry registry) {
        this.created = Counter.builder("orders.created")
            .description("Orders created")
            .register(registry);
    }

    public void create(...) {
        created.increment();
    }
}
```

`Timer`, `DistributionSummary`, `Gauge` (gauge needs a live object — don’t allocate per request).

**Cardinality:** never put `userId` on a metric name/tag. You will explode Prometheus. Tags = bounded sets (`status`, `method`, `outcome`).

`http.server.requests` already has `uri` — **use path templates** (`/v1/orders/{id}`), not raw ids. Boot 2.2+ can time URIs; configure `management.observations.http.server.requests.name` / `http.server.request.autotime` depending on generation. If you see a unique time series per id, fix the `uri` tag.

---

## 5. Tracing (Micrometer Tracing / OpenTelemetry)

A **trace** = one request across services. A **span** = one hop (HTTP, JDBC).

Boot 3: Micrometer Tracing with OpenTelemetry or Brave.

```xml
<dependency>
  <groupId>io.micrometer</groupId>
  <artifactId>micrometer-tracing-bridge-otel</artifactId>
</dependency>
<dependency>
  <groupId>io.opentelemetry</groupId>
  <artifactId>opentelemetry-exporter-otlp</artifactId>
</dependency>
```

Propagators put `traceparent` on outbound HTTP. Zipkin/Tempo/Jaeger collect spans.

**Correlation:** put `traceId` in MDC so logs join traces (chapter 15 logging).

Sampling: 100% in staging, 5–10% in high-QPS prod unless you can afford it.

---

## 6. Separate management port

```yaml
management:
  server:
    port: 8081
```

K8s: container port 8080 for traffic, 8081 for probes — NetworkPolicy can lock 8081 to the cluster.

---

## 7. Production pitfalls

1. Exposing `heapdump` without auth.
2. DB in **liveness**.
3. Custom health that calls a slow HTTP API — readiness flaps.
4. High-cardinality metrics.
5. `show-details: always` leaking datasource URLs on `/health`.
6. No `git.properties` — you cannot tell which commit a pod runs.

---

# Interview Q&A (5–8 year bar)

A fresher knows `/actuator/health`. A 5–8 year answer splits liveness vs readiness, cardinality, and how Prometheus scrapes.

### Q1. What is Spring Boot Actuator?

**Answer:** Production endpoints: health, metrics, info, env, loggers, etc., via `starter-actuator`.

**Counter:** Is it on by default?  
**Answer:** Dependency required. Only `health`/`info` are exposed by default over web in recent Boot — still secure them.

---

### Q2. Liveness vs readiness vs health?

**Answer:** Liveness = restart the JVM. Readiness = include in LB. Health = aggregate. DB belongs in readiness, not liveness.

**Counter:** Pod starts, traffic 503?  
**Answer:** Readiness not UP yet (`ApplicationReadyEvent`) or a readiness indicator DOWN.

---

### Q3. How do you add a custom health check?

**Answer:** `HealthIndicator` bean (or `ReactiveHealthIndicator`). Include it in the readiness group if it should gate traffic.

---

### Q4. How do you expose Prometheus metrics?

**Answer:** `micrometer-registry-prometheus` + expose `prometheus` endpoint. Scrape from Prometheus.

**Counter:** Why not JSON `/metrics` for prod scrape?  
**Answer:** Prometheus wants exposition format. JSON is for humans/debug.

---

### Q5. How do you secure actuator?

**Answer:** Separate port, NetworkPolicy, `SecurityFilterChain` with `hasRole("OPS")`, expose only needed endpoints. `health` may be `permitAll` but **without** details.

---

### Q6. What is Micrometer?

**Answer:** A facade over metric backends (Prometheus, Datadog, …). You write `MeterRegistry` code once.

---

### Q7. Why is `http.server.requests` exploding series count?

**Answer:** URI tag includes raw ids. Use templated paths; deny high cardinality tags.

---

### Q8. Trace vs span vs MDC?

**Answer:** Trace = request id across services. Span = unit of work. MDC = thread-local log fields (`traceId`, `corrId`).

---

### Q9. Can you change log level without restart?

**Answer:** `POST /actuator/loggers/com.acme.payments` with `{"configuredLevel":"DEBUG"}`. Secure it. Doesn’t persist across pods unless you automate.

---

### Q10. `management.server.port`?

**Answer:** Bind actuator to another port so you can firewall it independently of the app port.

---

### Q11. Git info on `/info`?

**Answer:** `git-commit-id-plugin` + `management.info.git.enabled`. Ops can see commit SHA of the running JAR.

---

### Q12. Health DOWN but app “works”?

**Answer:** An indicator failed (disk space, Redis). Check `/health` details when authorized. Don’t blindly restart (that’s liveness).

---

### Q13. Observation API in Boot 3?

**Answer:** Micrometer Observation unifies metrics + traces for HTTP, JDBC. `ObservationRegistry`. Prefer it over ad-hoc timers *and* spans.

---

### Q14. Why not `include: '*'`?

**Answer:** `shutdown`, `heapdump`, `env` are attack surface. Explicit allow-list.

---

### Q15. How does this relate to K8s probes?

**Answer:** `livenessProbe.httpGet.path=/actuator/health/liveness`. `readinessProbe` → `/actuator/health/readiness`. Enable `management.endpoint.health.probes.enabled`.
