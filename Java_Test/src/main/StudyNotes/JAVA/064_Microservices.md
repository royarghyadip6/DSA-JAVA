# 64. Microservices

Production depth for **5–8 year** interviews (Spring Cloud). All 17 questions from `000_JAVA_Questions.md` #64.

---

## Basics

---

# 1. What are Microservices?

<details>
<summary>Show Answer</summary>

**Answer:**

**Microservices** is an architectural style where an application is built as a **collection of small, independently deployable services**. Each service owns a **bounded business capability**, runs in its **own process**, and communicates over **lightweight protocols** (usually HTTP/REST, messaging).

### Simple Idea

```text
Monolith:  One big WAR/JAR — all features together
Microservices: Order Service + Payment Service + Inventory Service — separate deployables
```

### Characteristics

| Trait | Detail |
|-------|--------|
| **Single responsibility** | One service = one business domain (orders, users) |
| **Independent deployment** | Deploy payment without touching catalog |
| **Own data store** | Database per service (ideal) — no shared tables |
| **Decentralized** | Teams own services end-to-end |
| **Resilience** | Failure isolated; circuit breakers, timeouts |
| **Observable** | Distributed tracing, centralized logs |

### Spring Boot Service Example

```java
@SpringBootApplication
public class OrderServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}

@RestController
@RequestMapping("/orders")
public class OrderController {
    @PostMapping
    public Order create(@RequestBody OrderRequest req) {
        return orderService.create(req);
    }
}
```

### Production Reality

```text
✅ Start monolith; split when team scale / deploy pain / scaling needs justify cost
✅ Each service: CI/CD pipeline, health checks, API contract (OpenAPI)
❌ "Microservices for hello world" — operational overhead is real
```

**Interview Point:**

> Microservices = **small, autonomous, independently deployable** services with **own data** and **network communication**. Not a silver bullet — adds distributed complexity. Spring Boot + Spring Cloud is the common Java stack.

</details>

---

# 2. Monolith vs Microservices?

<details>
<summary>Show Answer</summary>

**Answer:**

| Dimension | Monolith | Microservices |
|-----------|----------|---------------|
| **Deployment** | Single unit (one JAR/WAR) | Many independent services |
| **Scaling** | Scale entire app | Scale hot service only (e.g., payment) |
| **Technology** | One stack | Polyglot possible per service |
| **Data** | Shared DB common | DB per service (ideal) |
| **Complexity** | Simple dev/debug | Distributed: network, tracing, consistency |
| **Team** | One codebase | Team per service (Conway's law) |
| **Transactions** | Local ACID easy | Distributed transactions hard → Saga |
| **Startup cost** | Low | High (K8s, gateway, discovery, CI/CD) |
| **Failure** | One bug can crash all | Blast radius smaller per service |

### When to Choose

```text
Monolith first:
  - MVP, small team, unclear domain boundaries
  - Strong consistency everywhere, simple ops

Microservices when:
  - Independent scaling (read-heavy catalog vs write-heavy orders)
  - Multiple teams need parallel release
  - Different SLAs/tech per domain
  - Domain boundaries are clear (DDD bounded contexts)
```

### Strangler Fig Migration

```text
1. Put API Gateway in front of monolith
2. Extract one bounded context (e.g., notifications) as new service
3. Route traffic gradually; monolith shrinks over time
```

**Interview Point:**

> Monolith = simpler ops, ACID, debug. Microservices = independent deploy/scale/teams but **distributed complexity**. Say: **"Monolith first, extract when pain is real"** — shows senior judgment.

</details>

---

# 3. Advantages?

<details>
<summary>Show Answer</summary>

**Answer:**

### Key Advantages

| Advantage | Production benefit |
|-----------|-------------------|
| **Independent deployment** | Ship payment fix without regression-testing entire monolith |
| **Independent scaling** | Scale `search-service` on Black Friday, not whole app |
| **Technology diversity** | Python ML service + Java order service |
| **Team autonomy** | Team owns build-test-deploy-monitor for one service |
| **Fault isolation** | Recommendation down ≠ checkout down (with resilience patterns) |
| **Faster CI/CD** | Smaller builds, targeted tests |
| **Clear boundaries** | DDD bounded contexts map to services |

### Example: E-commerce

```text
Black Friday:
  - Scale inventory-service: 20 pods
  - Scale order-service: 10 pods
  - Leave admin-service: 2 pods

Payment bug fix:
  - Deploy payment-service only
  - Rollback in minutes without touching catalog
```

### Organizational (Conway's Law)

```text
Service boundaries align with team boundaries
→ Reduced cross-team coordination per release
→ Faster feature delivery at scale (when mature)
```

**Interview Point:**

> Top 3 advantages: **independent deploy**, **independent scale**, **team autonomy**. Always pair with awareness of operational cost — mature DevOps/K8s/SRE required.

</details>

---

# 4. Challenges?

<details>
<summary>Show Answer</summary>

**Answer:**

### Major Challenges

| Challenge | What goes wrong |
|-----------|-----------------|
| **Distributed complexity** | Network latency, partial failures, timeouts |
| **Data consistency** | No cross-service ACID — need Saga/eventual consistency |
| **Testing** | Integration tests need many services or contract tests |
| **Observability** | One request spans 10 services — need trace IDs |
| **Deployment/Ops** | K8s, service mesh, many pipelines |
| **API versioning** | Breaking changes break consumers |
| **Security** | AuthN/Z at gateway + service-to-service (mTLS, JWT) |
| **Duplication** | Shared libs vs copy-paste vs anti-corruption layer |
| **Debugging** | Stack trace split across logs |

### Production Pain Examples

```text
❌ Cascading failure: inventory slow → thread pool exhausted → order service down
   Fix: timeouts, circuit breaker (Resilience4j), bulkhead

❌ Dual writes: update order DB + call payment — one succeeds, one fails
   Fix: Saga + outbox pattern + idempotency keys

❌ "Distributed monolith": services tightly coupled, must deploy together
   Fix: async events, stable APIs, avoid chatty sync calls
```

```java
// Always set timeouts on inter-service calls
@Bean
public RestTemplate restTemplate(RestTemplateBuilder builder) {
    return builder
        .setConnectTimeout(Duration.ofSeconds(2))
        .setReadTimeout(Duration.ofSeconds(5))
        .build();
}
```

**Interview Point:**

> Challenges = **consistency, observability, ops overhead, failure modes**. Senior answer: mention **Saga, circuit breaker, distributed tracing (Micrometer/Zipkin), contract testing (Pact)** as mitigations.

</details>

---

## Service Communication

---

# 5. REST communication?

<details>
<summary>Show Answer</summary>

**Answer:**

**REST** (Representational State Transfer) is the most common **synchronous** inter-service style: HTTP methods on **resource URLs**, typically JSON payloads.

### REST Conventions

| Method | Purpose | Idempotent |
|--------|---------|------------|
| `GET` | Read | Yes |
| `POST` | Create | No |
| `PUT` | Full replace | Yes |
| `PATCH` | Partial update | No |
| `DELETE` | Remove | Yes |

### Spring Boot — RestTemplate / RestClient

```java
@Service
public class InventoryClient {
    private final RestTemplate restTemplate;

    public InventoryClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public StockResponse checkStock(String sku) {
        return restTemplate.getForObject(
            "http://inventory-service/api/stock/{sku}",
            StockResponse.class,
            sku
        );
    }
}
```

### Production Best Practices

```text
✅ Use service name (via discovery) not hardcoded IP
✅ Timeouts + retries (careful — only idempotent GET/PUT)
✅ Circuit breaker (Resilience4j)
✅ Correlation ID header (X-Request-Id) for tracing
✅ Version APIs: /api/v1/orders
✅ Idempotency-Key header on POST (payments)
❌ Chatty REST (N+1 calls) — batch APIs or async events
```

```java
// Correlation ID propagation
@Component
public class CorrelationFilter implements ClientHttpRequestInterceptor {
  @Override
  public ClientHttpResponse intercept(HttpRequest req, byte[] body, ClientHttpRequestExecution exec) {
    req.getHeaders().add("X-Request-Id", MDC.get("traceId"));
    return exec.execute(req, body);
  }
}
```

**Interview Point:**

> REST = HTTP + resources + stateless. Production needs **timeouts, circuit breaker, idempotency, versioning, tracing headers**. Prefer **async events** for fire-and-forget side effects.

</details>

---

# 6. Feign Client?

<details>
<summary>Show Answer</summary>

**Answer:**

**OpenFeign** (Spring Cloud) is a **declarative HTTP client** — define an interface with annotations; Spring generates the implementation at runtime. Less boilerplate than `RestTemplate`.

### Setup

```xml
<!-- pom.xml -->
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

```java
@SpringBootApplication
@EnableFeignClients
public class OrderServiceApplication { }

@FeignClient(name = "inventory-service", path = "/api/stock")
public interface InventoryClient {
    @GetMapping("/{sku}")
    StockResponse getStock(@PathVariable String sku);
}

@Service
@RequiredArgsConstructor
public class OrderService {
    private final InventoryClient inventoryClient;

    public void placeOrder(String sku) {
        StockResponse stock = inventoryClient.getStock(sku);
        // ...
    }
}
```

### Feign + Load Balancing + Eureka

```text
@FeignClient(name = "inventory-service")
  → Spring Cloud LoadBalancer resolves name via Eureka
  → Round-robin across healthy instances
```

### Production Config

```yaml
# application.yml
feign:
  client:
    config:
      default:
        connectTimeout: 2000
        readTimeout: 5000
  circuitbreaker:
    enabled: true   # Resilience4j integration (Spring Cloud 2020+)

spring:
  cloud:
    openfeign:
      okhttp:
        enabled: true   # better connection pooling than default HttpURLConnection
```

| Feign | RestTemplate / WebClient |
|-------|--------------------------|
| Declarative interface | Imperative / reactive code |
| Easy CRUD clients | More control |
| Sync (blocking) by default | WebClient = reactive/non-blocking |

**Interview Point:**

> Feign = **declarative REST client** with Eureka + LoadBalancer integration. Pair with **timeouts and circuit breaker**. Blocking — for reactive stacks use **WebClient** or Spring 6 `@HttpExchange`.

</details>

---

# 7. WebClient?

<details>
<summary>Show Answer</summary>

**Answer:**

**WebClient** is Spring's **non-blocking, reactive** HTTP client (part of WebFlux). Uses **Reactor** (`Mono`/`Flux`) — fits reactive pipelines and high-concurrency I/O with fewer threads.

### Basic Usage

```java
@Configuration
public class WebClientConfig {
    @Bean
    public WebClient inventoryWebClient(WebClient.Builder builder) {
        return builder
            .baseUrl("http://inventory-service")
            .defaultHeader("Accept", "application/json")
            .build();
    }
}

@Service
@RequiredArgsConstructor
public class OrderService {
    private final WebClient inventoryWebClient;

    public Mono<StockResponse> checkStock(String sku) {
        return inventoryWebClient.get()
            .uri("/api/stock/{sku}", sku)
            .retrieve()
            .onStatus(HttpStatusCode::isError, resp ->
                resp.bodyToMono(String.class)
                    .flatMap(body -> Mono.error(new InventoryException(body))))
            .bodyToMono(StockResponse.class)
            .timeout(Duration.ofSeconds(5));
    }
}
```

### WebClient vs Feign vs RestTemplate

| Client | Model | When to use |
|--------|-------|-------------|
| **RestTemplate** | Blocking (legacy) | Avoid in new code |
| **Feign** | Blocking, declarative | Simple sync microservice calls |
| **WebClient** | Non-blocking reactive | WebFlux, high concurrency, streaming |

### Production Notes

```java
// Connection pooling with Reactor Netty
@Bean
public WebClient.Builder webClientBuilder() {
    HttpClient httpClient = HttpClient.create()
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000)
        .responseTimeout(Duration.ofSeconds(5));
    return WebClient.builder()
        .clientConnector(new ReactorClientHttpConnector(httpClient));
}

// Block only at the edge (MVC controller calling reactive client)
StockResponse stock = inventoryWebClient.getStock(sku).block();  // avoid deep in reactive chain
```

**Interview Point:**

> WebClient = **reactive, non-blocking** HTTP client. Use with **WebFlux** or when you need async I/O efficiency. Don't block in reactive chains — use `flatMap` to compose calls.

</details>

---

## Service Discovery

---

# 8. Eureka?

<details>
<summary>Show Answer</summary>

**Answer:**

**Netflix Eureka** is a **service registry** for microservices. Services **register** themselves on startup and **heartbeat** to stay alive. Clients **lookup** instances by service name instead of hardcoded hosts.

### Components

| Component | Role |
|-----------|------|
| **Eureka Server** | Registry — stores service instances |
| **Eureka Client** | Registers + fetches registry (services & consumers) |

### Eureka Server

```java
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
```

```yaml
# eureka-server application.yml
server:
  port: 8761
eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
```

### Eureka Client (Service)

```java
@SpringBootApplication
@EnableDiscoveryClient   // or @EnableEurekaClient (older)
public class OrderServiceApplication { }
```

```yaml
spring:
  application:
    name: order-service
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

### Production Considerations

```text
✅ Run Eureka cluster (2–3 nodes) for HA
✅ Self-preservation mode — protects against mass deregistration during network blip
⚠️ Netflix Eureka in maintenance mode — new projects often use K8s DNS, Consul, or Nacos
✅ Pair with Spring Cloud LoadBalancer (replaces Ribbon)
❌ Stale instances if heartbeat missed — client retry + health checks
```

**Interview Point:**

> Eureka = **service registry** with register/heartbeat/discover. Spring Cloud: `@EnableEurekaServer` + `@EnableDiscoveryClient`. Know it's **maintenance mode** — mention K8s service discovery as modern alternative.

</details>

---

# 9. Service Registry?

<details>
<summary>Show Answer</summary>

**Answer:**

A **service registry** is a central catalog of **running service instances** (host, port, health, metadata). Consumers query it to find where to send requests.

### Flow

```text
1. order-service starts → registers with Eureka: order-service @ 10.0.1.5:8080
2. payment-service starts → registers: payment-service @ 10.0.1.6:8080
3. order-service calls payment-service by NAME (not IP)
4. LoadBalancer picks healthy instance from registry
5. Heartbeat every 30s — miss 3 → instance evicted
```

### Registry Options

| Registry | Ecosystem |
|----------|-----------|
| **Eureka** | Spring Cloud / Netflix (legacy but common in interviews) |
| **Consul** | HashiCorp — health checks, KV, mesh |
| **etcd / K8s DNS** | Kubernetes native |
| **Nacos** | Alibaba — config + discovery |
| **Zookeeper** | Older Kafka/Curator stacks |

### Health Integration

```yaml
# Spring Boot Actuator health propagated to Eureka
management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: always

eureka:
  instance:
    prefer-ip-address: true
    lease-renewal-interval-in-seconds: 30
```

**Interview Point:**

> Service registry = **phone book for microservices**. Register on start, heartbeat, deregister on shutdown. Enables **dynamic scaling** — new pods auto-discovered without config change.

</details>

---

# 10. Client-side Discovery?

<details>
<summary>Show Answer</summary>

**Answer:**

In **client-side discovery**, the **calling service** queries the registry, gets a list of instances, and **chooses one** (load balance) — then calls it directly.

### Client-side vs Server-side

| | Client-side | Server-side |
|---|-------------|-------------|
| **Who picks instance?** | Client (LoadBalancer) | Load balancer / API Gateway |
| **Example** | Eureka + Feign + Spring Cloud LoadBalancer | AWS ELB, K8s Service + Ingress |
| **Traffic path** | Client → instance directly | Client → LB → instance |
| **Coupling** | Client needs discovery lib | Client only knows LB URL |

### Client-side Flow

```text
Order Service (Feign)
  → asks Eureka: "instances of payment-service?"
  → gets [10.0.1.6:8080, 10.0.1.7:8080]
  → LoadBalancer picks 10.0.1.6
  → HTTP POST http://10.0.1.6:8080/api/payments
```

```java
@FeignClient(name = "payment-service")  // name resolved via discovery + LB
public interface PaymentClient {
    @PostMapping("/api/payments")
    PaymentResponse charge(@RequestBody PaymentRequest req);
}
```

### Trade-offs

```text
Pros:  No extra network hop; simple in dev; works well with Eureka
Cons:  Every client needs discovery logic; language-specific libs
```

**Interview Point:**

> Client-side discovery = **client queries registry + load balances**. Spring Cloud LoadBalancer + Feign is classic example. Contrast with **server-side** (K8s Ingress, API Gateway routing) where client is dumb.

</details>

---

## API Gateway

---

# 11. Why API Gateway?

<details>
<summary>Show Answer</summary>

**Answer:**

An **API Gateway** is a **single entry point** for all external clients (web, mobile, partners). It routes requests to internal microservices and handles **cross-cutting concerns** so each service doesn't duplicate them.

### Without vs With Gateway

```text
Without:
  Mobile app → order-service:8081
  Mobile app → payment-service:8082
  Mobile app → inventory-service:8083
  (client knows all URLs, auth repeated, CORS mess)

With:
  Mobile app → api.mycompany.com (Gateway) → routes to internal services
```

### Gateway Responsibilities

| Concern | Gateway handles |
|---------|-----------------|
| **Routing** | `/orders/**` → order-service |
| **AuthN/Z** | JWT validation, API keys |
| **Rate limiting** | Protect backends from abuse |
| **SSL termination** | HTTPS at edge |
| **Request aggregation** | Combine multiple service calls (BFF pattern) |
| **CORS** | Single place for cross-origin |
| **Logging/metrics** | Central access logs |
| **Protocol translation** | HTTP → gRPC (if needed) |

**Interview Point:**

> API Gateway = **single front door** for clients. Offloads **routing, auth, rate limit, SSL, CORS** from microservices. Internal services stay private (no public exposure).

</details>

---

# 12. Spring Cloud Gateway?

<details>
<summary>Show Answer</summary>

**Answer:**

**Spring Cloud Gateway** is a reactive API Gateway built on **Spring WebFlux + Project Reactor + Netty**. It replaces older Zuul 1.x for new Spring Cloud projects.

### Key Concepts

| Concept | Purpose |
|---------|---------|
| **Route** | ID + destination URI + predicates + filters |
| **Predicate** | Match condition (path, header, method) |
| **Filter** | Modify request/response (add header, strip prefix, rate limit) |

### Configuration Example

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: order-route
          uri: lb://order-service          # lb = LoadBalancer + Eureka
          predicates:
            - Path=/api/orders/**
          filters:
            - StripPrefix=1
            - AddRequestHeader=X-Gateway, true

        - id: payment-route
          uri: lb://payment-service
          predicates:
            - Path=/api/payments/**
```

### Custom Filter (Java)

```java
@Component
public class AuthFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (token == null || !jwtValidator.isValid(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() { return -1; }
}
```

### vs Zuul

```text
Zuul 1.x: Servlet blocking, maintenance
Spring Cloud Gateway: Reactive, WebFlux, active development
```

**Interview Point:**

> Spring Cloud Gateway = **reactive gateway** with routes/predicates/filters. `lb://service-name` integrates **Eureka + LoadBalancer**. Use for auth, routing, rate limiting at the edge.

</details>

---

# 13. Gateway benefits?

<details>
<summary>Show Answer</summary>

**Answer:**

### Benefits Summary

| Benefit | Production impact |
|---------|-------------------|
| **Single entry point** | Clients decoupled from internal topology |
| **Security** | JWT/OAuth validation once at edge; services trust internal network/mTLS |
| **Hide internals** | Internal hostnames/ports never exposed |
| **Cross-cutting** | Rate limit, logging, CORS, request ID — DRY |
| **Protocol bridge** | External REST → internal gRPC |
| **A/B & canary** | Route % traffic to new version |
| **BFF aggregation** | One mobile API call → gateway fans out to 3 services |
| **Resilience** | Circuit breaker / retry at edge (with care) |

### BFF Pattern (Backend for Frontend)

```text
Mobile BFF Gateway:  lightweight payloads, aggregated cart+user+promo
Web BFF Gateway:     richer data, different cache rules
Admin BFF Gateway:   admin auth, audit logging
```

### Caution

```text
❌ Don't put business logic in gateway — becomes god component
❌ Don't make gateway a sync bottleneck — keep aggregation minimal
✅ Gateway = infra concerns; domain logic stays in services
```

**Interview Point:**

> Gateway benefits: **security, routing, decoupling, cross-cutting in one place**. Warn against **fat gateway** — no business rules; use **BFF** if mobile/web need different APIs.

</details>

---

## Distributed Systems

---

# 14. Distributed transaction?

<details>
<summary>Show Answer</summary>

**Answer:**

A **distributed transaction** spans **multiple services/databases** with **atomicity** — all succeed or all rollback. Classic tool: **2PC (Two-Phase Commit)** with XA transactions.

### The Problem

```text
Place order flow:
  1. order-service: INSERT order     ✅
  2. payment-service: charge card  ✅
  3. inventory-service: reserve    ❌ OUT OF STOCK

Without pattern: order + payment committed, inventory failed → inconsistent state
```

### 2PC (XA) — Why Avoid in Microservices

| Phase | Action |
|-------|--------|
| Prepare | All participants vote ready |
| Commit | Coordinator commits all — or abort all |

```text
❌ Blocking — locks held during prepare
❌ Coordinator SPOF
❌ Not supported well across HTTP services
❌ Tight coupling — all must be available
```

```java
// @Transactional across DBs via JTA/XA — rare in microservices
// Most teams avoid XA; use Saga instead
```

### Better Alternatives

| Pattern | Approach |
|---------|----------|
| **Saga** | Sequence of local TXs + compensating actions |
| **Outbox + events** | Local TX writes DB + event; async consumers |
| **Idempotency** | Safe retries on duplicate requests |
| **Eventual consistency** | Accept temporary inconsistency; reconcile |

**Interview Point:**

> Distributed ACID (2PC/XA) **doesn't fit microservices**. Say: **Saga + eventual consistency + idempotency + outbox pattern**. Local transaction per service, compensate on failure.

</details>

---

# 15. Saga Pattern?

<details>
<summary>Show Answer</summary>

**Answer:**

A **Saga** is a sequence of **local transactions** across services. If a step fails, **compensating transactions** undo previous steps — achieving **eventual consistency** without 2PC.

### Saga Flow (Order Example)

```text
Step 1: order-service   → CREATE order (PENDING)     ✅
Step 2: payment-service → CHARGE payment            ✅
Step 3: inventory-service → RESERVE stock            ❌ FAIL

Compensate:
  → payment-service: REFUND
  → order-service: CANCEL order
```

### Saga Styles

| Style | Coordinator | Coupling |
|-------|-------------|----------|
| **Choreography** | Events only — no central boss | Loose, harder to trace |
| **Orchestration** | Central saga orchestrator | Clear flow, single place for logic |

### Compensation Rules

```text
✅ Compensating action must be idempotent (retry safe)
✅ Not true undo — business meaning (REFUND not DELETE charge row)
✅ Saga log / state machine tracks current step
```

```java
// Orchestrator pseudo-state
enum SagaState { STARTED, PAYMENT_DONE, INVENTORY_FAILED, COMPENSATING, COMPLETED }

// Each step publishes event or calls next service
// On failure → trigger compensating commands in reverse order
```

**Interview Point:**

> Saga = **local TXs + compensations** for cross-service workflows. Two flavors: **choreography** (events) vs **orchestration** (central coordinator). Always mention **idempotent compensations**.

</details>

---

# 16. Choreography Saga?

<details>
<summary>Show Answer</summary>

**Answer:**

**Choreography saga** has **no central coordinator**. Each service listens to **events** and decides what to do next. Services are decoupled — they only know their event contracts.

### Event Flow

```text
order-service:
  publishes OrderCreated

payment-service:
  listens OrderCreated → charges → publishes PaymentCompleted OR PaymentFailed

inventory-service:
  listens PaymentCompleted → reserves → publishes InventoryReserved OR InventoryFailed

order-service:
  listens InventoryReserved → marks CONFIRMED
  listens PaymentFailed / InventoryFailed → marks CANCELLED (or triggers refund via event)
```

### Kafka Example (conceptual)

```java
// payment-service listener
@KafkaListener(topics = "order-created")
public void onOrderCreated(OrderCreatedEvent event) {
    try {
        paymentService.charge(event.getOrderId(), event.getAmount());
        kafkaTemplate.send("payment-completed", new PaymentCompletedEvent(event.getOrderId()));
    } catch (Exception e) {
        kafkaTemplate.send("payment-failed", new PaymentFailedEvent(event.getOrderId()));
    }
}
```

### Pros & Cons

| Pros | Cons |
|------|------|
| Loose coupling | Hard to see full flow |
| No orchestrator SPOF | Cyclic dependencies risk |
| Simple per service | Debugging/tracing harder |
| Scales naturally | Contract changes affect many consumers |

**Interview Point:**

> Choreography = **dance without conductor** — pure event-driven. Good for **simple flows, few steps**. Bad when flow is complex — use **orchestration** instead.

</details>

---

# 17. Orchestration Saga?

<details>
<summary>Show Answer</summary>

**Answer:**

**Orchestration saga** uses a **central orchestrator** (saga manager) that tells each service **what to do next** and handles **compensation** on failure. Flow is explicit and traceable.

### Orchestrator Flow

```text
SagaOrchestrator:
  1. command → order-service: CreateOrder
  2. command → payment-service: Charge
  3. command → inventory-service: Reserve
  4. on failure at step 3:
       command → payment-service: Refund
       command → order-service: Cancel
```

### Implementation Options

| Tool | Style |
|------|-------|
| **Custom orchestrator** | Spring `@Service` + state machine |
| **Temporal / Camunda** | Workflow engine with durable execution |
| **Axon Framework** | CQRS + saga support |
| **Seata** | AT/TCC/Saga (Alibaba) |

```java
@Service
@RequiredArgsConstructor
public class PlaceOrderSaga {
    private final OrderClient orderClient;
    private final PaymentClient paymentClient;
    private final InventoryClient inventoryClient;

    public void execute(PlaceOrderCommand cmd) {
        String orderId = orderClient.create(cmd);
        try {
            paymentClient.charge(orderId, cmd.getAmount());
            inventoryClient.reserve(orderId, cmd.getSku());
            orderClient.confirm(orderId);
        } catch (InventoryException e) {
            paymentClient.refund(orderId);
            orderClient.cancel(orderId);
            throw e;
        }
    }
}
```

### Pros & Cons

| Pros | Cons |
|------|------|
| Clear, readable flow | Orchestrator is coupling point |
| Easy monitoring/debug | Orchestrator must be HA |
| Central compensation logic | Can become god service if not careful |

### Choreography vs Orchestration — When?

```text
Choreography:  2–3 steps, event-native org, loose teams
Orchestration: complex flows, strict ordering, compliance audit trail
```

**Interview Point:**

> Orchestration = **central brain** commands services and runs compensations. Prefer for **complex sagas** (5+ steps, branching). Mention **Temporal/Camunda** for production-grade durable workflows.

</details>

---

# 5–8 Year Interview Rapid Fire

### Q: Database per service?

<details>
<summary>Show Answer</summary>

**Answer:**

Each microservice should own its **private database** (schema or DB). **No shared tables** across services — share data via **API or events**, not FK joins.

</details>

---

### Q: Feign vs WebClient?

<details>
<summary>Show Answer</summary>

**Answer:**

Feign = **declarative, blocking**, easy sync calls. WebClient = **reactive, non-blocking**, better for WebFlux/high concurrency.

</details>

---

### Q: Eureka still recommended?

<details>
<summary>Show Answer</summary>

**Answer:**

Netflix Eureka is in **maintenance mode**. Still valid in Spring Cloud interviews. Production greenfield often uses **Kubernetes DNS**, **Consul**, or **Nacos**.

</details>

---

### Q: What replaces Netflix Ribbon?

<details>
<summary>Show Answer</summary>

**Answer:**

**Spring Cloud LoadBalancer** (`spring-cloud-starter-loadbalancer`). Works with `@LoadBalanced RestTemplate`, Feign, and Gateway `lb://` URIs.

</details>

---

### Q: API Gateway vs Load Balancer?

<details>
<summary>Show Answer</summary>

**Answer:**

Load balancer = **distribute traffic** to instances. API Gateway = **L7 routing + auth + rate limit + aggregation** — smarter edge for APIs.

</details>

---

### Q: Why not 2PC/XA across microservices?

<details>
<summary>Show Answer</summary>

**Answer:**

Blocking, tight coupling, poor availability, doesn't work over HTTP. Use **Saga** with **eventual consistency** instead.

</details>

---

### Q: Choreography vs Orchestration saga?

<details>
<summary>Show Answer</summary>

**Answer:**

Choreography = **events, no central coordinator** (loose, hard to trace). Orchestration = **central orchestrator** commands steps (clear flow, better for complex sagas).

</details>

---

### Q: Circuit breaker purpose?

<details>
<summary>Show Answer</summary>

**Answer:**

Stop calling a **failing downstream** service — fail fast, prevent thread exhaustion and cascading failure. **Resilience4j** with Spring Cloud.

</details>

---

### Q: Outbox pattern one-liner?

<details>
<summary>Show Answer</summary>

**Answer:**

Write **business data + outbound event in same local DB transaction**; separate poller publishes to Kafka — guarantees **at-least-once** without dual-write problem.

</details>

---

### Q: Strangler fig pattern?

<details>
<summary>Show Answer</summary>

**Answer:**

Gradually **replace monolith** by routing slices to new microservices via gateway — monolith shrinks over time.

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> Microservices = small, independently deployable services with own data. Communicate via **REST/Feign/WebClient**; discover via **Eureka/registry**; expose via **API Gateway**. No distributed ACID — use **Saga** (choreography = events, orchestration = central coordinator) + **idempotency + outbox**. Always add **timeouts, circuit breaker, distributed tracing**.

</details>
