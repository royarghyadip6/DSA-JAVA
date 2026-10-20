# 3. Microservices Architecture

## Simple idea

Microservices divide a product into **small, loosely coupled, independently deployable services**, each owning a business capability.

```text
E-commerce
  ├─ User Service       → User DB
  ├─ Order Service      → Order DB
  ├─ Payment Service    → Payment DB
  └─ Inventory Service  → Inventory DB
```

The important word is not “small.” It is **independent**: clear ownership, API, data, deployment, and scaling.

---

## Monolith vs microservices

| | Monolith | Microservices |
|--|----------|---------------|
| Deployment | One unit | Each service independently |
| Scaling | Whole app | Busy service only |
| Communication | Local method calls | Network calls / events |
| Data | Often one database | Database per service |
| Transactions | Local ACID is easy | Cross-service consistency is difficult |
| Operations | Simple | CI/CD, discovery, tracing, many deployments |
| Failure | Large blast radius | Can isolate failures—if resilience is designed |

> Microservices do not automatically create fault tolerance. A slow Payment Service can still exhaust Order Service unless you use timeouts, circuit breakers, and bulkheads.

---

## Finding service boundaries

### 1. Business capabilities

Split by what the business does: Orders, Payments, Users—not arbitrary technical layers such as “Controller Service.”

### 2. Domain-Driven Design (DDD)

Group related rules and language into a **bounded context**. Order owns order rules; Payment owns payment rules.

### 3. Single responsibility

A service should have one focused reason to change. It does not mean “one endpoint” or “one table.”

### 4. Data ownership

Each service owns its data. Other services use its API/events instead of joining its tables.

### 5. Right granularity

- Too large → distributed monolith-in-disguise
- Too small → “nano-services,” excessive calls and deployments

Begin with broader boundaries and split only when there is evidence.

---

## Communication

**Synchronous**

- REST: simple and widely compatible
- gRPC: lower overhead, typed binary contract
- caller waits; failures and latency can cascade

**Asynchronous**

- Kafka, RabbitMQ, SNS/SQS
- producer does not wait for consumers
- looser coupling and better buffering
- eventual consistency and debugging become harder

```text
Order → REST → Payment             (need answer now)
Order → OrderPlaced → Kafka        (notify inventory/email later)
```

---

## API Gateway and supporting infrastructure

The gateway provides one external door:

- routing and aggregation
- authentication/authorization
- rate limiting
- TLS termination
- monitoring

Microservices also commonly require:

- service discovery
- load balancing
- centralized configuration/secrets
- logging, metrics, and distributed tracing
- CI/CD and container orchestration

---

## Data consistency

Each service has its own DB, so one cross-service ACID transaction is unavailable.

| Strategy | Meaning |
|----------|---------|
| Eventual consistency | Updates arrive over time |
| Saga | Local transactions + compensating actions |
| Event sourcing | Store changes as events |
| 2PC | Strong coordination; blocking and less scalable, used cautiously |

Example saga:

```text
Create order → Reserve inventory → Charge payment
                         failure → Release inventory / cancel order
```

Use idempotency so a retried payment event does not charge twice.

---

## Scaling

- horizontal instances behind a load balancer
- auto-scaling (Kubernetes/ECS)
- read replicas for read-heavy databases
- sharding for very large write/data load

Only scale the hot service:

```text
Order × 3     Payment × 4     Search × 20
```

---

## Deployment strategies

| Strategy | Idea |
|----------|------|
| CI/CD | Test and deploy each service automatically |
| Blue-green | Old and new environments; switch all traffic |
| Canary | Send a small percentage to the new version |
| Service mesh | Traffic security, policy, telemetry (Istio/Linkerd) |

---

## Observability

A request may cross ten services. One log file is no longer enough.

- centralized logs: ELK/Graylog
- distributed tracing: Jaeger/Zipkin, trace ID across services
- metrics and dashboards: Prometheus/Grafana
- liveness/readiness health probes

---

## Real examples from the course

- Netflix: streaming and personalization capabilities
- Uber: ride matching, payments, navigation
- Amazon: search, payment, recommendations, shipping

These companies evolved toward services after scale and organization demanded them.

---

## Interview Q&A

### 1. What are microservices, and how do they differ from a monolith?

<details>
<summary>Show Answer</summary>

**Answer:** Microservices are independently deployable services aligned to business capabilities. A monolith is one deployable. Services scale and release independently but add network, consistency, and operational complexity.

</details>

### 2. What are their benefits and challenges?

<details>
<summary>Show Answer</summary>

**Answer:**

Benefits: independent scaling/deployment, team autonomy, technology flexibility, smaller failure domains.

Challenges: distributed data, network latency, API coordination, security, tracing, and many deployments.

</details>

### 3. How do you identify and design microservices?

<details>
<summary>Show Answer</summary>

**Answer:** Decompose by business domain/DDD bounded contexts, give each service one focused responsibility and its own data, define clear APIs/events, and avoid boundaries that are too large or too small.

</details>

### 4. What is the role of an API Gateway?

<details>
<summary>Show Answer</summary>

**Answer:** It is the external reverse-proxy entry point for routing, aggregation, authentication, TLS, rate limits, monitoring, and sometimes load balancing. Examples: Kong, Nginx, Apigee, AWS API Gateway.

</details>

### 5. How do microservices communicate?

<details>
<summary>Show Answer</summary>

**Answer:** REST or gRPC synchronously when an immediate answer is needed; Kafka, RabbitMQ, SNS/SQS, or pub-sub asynchronously for decoupled workflows.

</details>

### 6. How do you ensure data consistency?

<details>
<summary>Show Answer</summary>

**Answer:** Accept eventual consistency where possible, use Sagas and compensating actions for workflows, idempotent events, and event sourcing where it fits. 2PC provides stronger coordination but reduces availability and scale.

</details>

### 7. What are common deployment strategies?

<details>
<summary>Show Answer</summary>

**Answer:** Automated CI/CD, blue-green for quick full switch/rollback, canary for gradual release, and optionally a service mesh for communication policy and telemetry.

</details>

### 8. How do you scale microservices?

<details>
<summary>Show Answer</summary>

**Answer:** Horizontally scale hot services behind load balancers, auto-scale, add DB read replicas for reads, and shard when one database partition cannot handle the data/write load.

</details>

### 9. Give real-world examples.

<details>
<summary>Show Answer</summary>

**Answer:** Netflix separates streaming/personalization, Uber separates ride matching/payment/navigation, and Amazon separates search/payment/shipping.

</details>

### 10. How do you monitor and debug them?

<details>
<summary>Show Answer</summary>

**Answer:** Centralized logs, a trace ID with Jaeger/Zipkin, Prometheus/Grafana metrics, alerts, and liveness/readiness probes. Without observability, distributed failures are extremely difficult to diagnose.

</details>

**Q&A recap:** business boundaries + own data + independent deployment; pay for that independence with distributed-system complexity.

Next: [04_Event_Driven_Architecture.md](04_Event_Driven_Architecture.md)
