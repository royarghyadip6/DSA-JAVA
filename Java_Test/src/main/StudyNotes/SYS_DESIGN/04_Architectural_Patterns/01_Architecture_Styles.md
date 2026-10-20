# 1. Software Architecture Patterns and Styles

## Simple idea

Software architecture is the **high-level structure** of a system: its components, their responsibilities, and how they communicate.

Architecture directly affects:

- **scalability** — can it handle more traffic?
- **performance** — how quickly does it respond?
- **maintainability** — can teams safely change it?
- **reliability** — what happens when something fails?
- **cost** — how much infrastructure and operational work does it need?

---

## Main styles

### 1. Monolithic architecture

All features are packaged and deployed as **one application**.

```text
One Spring Boot application
  ├─ Users
  ├─ Orders
  ├─ Payments
  └─ Inventory
         ↓
     One database
```

**Advantages**

- easy to develop, test, and deploy initially
- local method calls are fast
- simple transactions
- low infrastructure cost

**Disadvantages as it grows**

- every deployment releases the whole application
- one hot feature forces the whole app to scale
- tightly coupled code becomes difficult to change
- one severe failure can affect everything
- many developers conflict in one codebase

**Use it for:** MVPs, small teams, simple CRUD products, and domains whose boundaries are not clear yet.

> A monolith is not automatically bad. A well-structured modular monolith is often the safest starting point.

---

### 2. Layered architecture

Responsibilities are separated into logical layers.

```text
Presentation (Controller)
        ↓
Business (Service)
        ↓
Data Access (Repository)
        ↓
Database
```

**Advantages:** clear separation of concerns, familiar Java/Spring hierarchy, easier testing and maintenance.

**Disadvantages:** every request may pass through unnecessary layers; layers can become tightly coupled; scaling one business area separately may still be difficult.

**Use it for:** enterprise systems, banking, CRM, and normal web applications.

Layered architecture describes **code responsibilities**. Multi-tier architecture often deploys those responsibilities on **different machines**. The terms overlap, but they are not always identical.

---

### 3. Client-server architecture

Clients request resources; servers provide them.

```text
Browser / Mobile App  →  Server  →  Database
```

It describes the communication relationship, not necessarily the internal structure of the server. A client-server application can also be layered, monolithic, or microservices-based.

---

### 4. Microservices architecture

The product is divided by **business capability** into independently deployable services.

```text
Order Service   Payment Service   Inventory Service
     DB               DB                 DB
```

**Advantages:** independent deployment and scaling, team autonomy, fault isolation, technology flexibility.

**Costs:** network latency, distributed consistency, tracing, security, deployment automation, and much higher operational complexity.

Use it when the domain and organization are large enough to justify those costs.

---

### 5. Event-driven architecture

Components publish facts called **events**. Other components react asynchronously.

```text
Order Service  →  OrderPlaced  →  Broker
                                  ├─ Payment
                                  ├─ Inventory
                                  └─ Notification
```

**Advantages:** loose coupling, asynchronous work, high scalability, easy addition of new consumers.

**Costs:** eventual consistency, duplicate and out-of-order events, harder debugging, and broker operations.

Use it for notifications, IoT, audits, trading data, and e-commerce workflows.

---

## How architecture affects scale and speed

- A monolith scales as a whole, which can waste resources.
- Microservices scale only the busy service, but network calls add latency.
- Layers improve maintainability, but too many hops add overhead.
- Events make the user-facing request faster by moving work to the background, but results may not be immediate.

Architecture always involves a **trade-off**.

---

## How business needs choose the architecture

| Business situation | Sensible direction |
|--------------------|-------------------|
| Need an MVP quickly | Monolith / layered monolith |
| Small budget and team | Avoid distributed complexity |
| Independent teams and releases | Microservices |
| One feature receives most traffic | Independently scalable service |
| Real-time, high-volume background work | Event-driven |
| Strong separation but no independent deployments | Layered / multi-tier |

Do not choose microservices because Netflix uses them. Netflix has Netflix-sized problems and teams.

---

## Interview lines

- Architecture is the structure and interaction of major components.
- Monolith is simple; microservices trade simplicity for independent deployment and scale.
- Layered separates responsibilities. Client-server describes who requests and who serves.
- Event-driven trades immediate consistency and easy tracing for loose coupling and asynchronous scale.
- Business needs and team structure matter as much as technology.

---

## Interview Q&A

### 1. What is the difference between monolithic and microservices architecture?

<details>
<summary>Show Answer</summary>

**Answer:**

| Monolith | Microservices |
|----------|---------------|
| One deployable | Many independent deployables |
| Scale whole application | Scale one service |
| Simple operations | Complex distributed operations |
| Local transactions | Distributed consistency is difficult |
| Best for smaller/early systems | Best when boundaries and teams justify it |

Start with a monolith unless independent deployment, ownership, or scaling solves a real problem.

</details>

### 2. When would you choose layered architecture over microservices?

<details>
<summary>Show Answer</summary>

**Answer:**

Choose layered when the system is small or medium, independent scaling is unnecessary, budget/time is limited, or an existing enterprise application already uses layers. It gives separation without network and DevOps overhead.

</details>

### 3. What are the pros and cons of event-driven architecture?

<details>
<summary>Show Answer</summary>

**Answer:**

Pros: loose coupling, asynchronous processing, scalability, easy addition of consumers.

Cons: eventual consistency, tracing/debugging difficulty, duplicates/order problems, and broker overhead.

</details>

### 4. How does architecture impact scalability and performance?

<details>
<summary>Show Answer</summary>

**Answer:**

Architecture decides what can scale independently and how many network hops a request makes. Microservices allow targeted scaling but add remote-call latency. Events remove slow work from the request path but are eventually consistent. Layers improve organization but can add overhead.

</details>

### 5. How do business requirements influence architectural decisions?

<details>
<summary>Show Answer</summary>

**Answer:**

Speed to market, expected growth, uptime, budget, compliance, team size, and release independence determine the right trade-off. A startup may need a monolith now; a large product with independent teams may need services.

</details>

### 6. Layered vs client-server architecture?

<details>
<summary>Show Answer</summary>

**Answer:**

Client-server describes communication: a client requests and a server responds. Layered describes internal responsibility: presentation, business, and data access. A server in a client-server system may itself use layers.

</details>

### 7. What challenges appear as a monolith grows?

<details>
<summary>Show Answer</summary>

**Answer:**

Tight coupling, whole-application scaling, risky full deployments, slower builds, large-team coordination, and a larger failure blast radius.

</details>

### 8. How do you choose monolith or microservices for a new system?

<details>
<summary>Show Answer</summary>

**Answer:**

Prefer a modular monolith for a new, uncertain, small-team product. Choose microservices when business boundaries are clear and you truly need independent team ownership, releases, technology, or scaling.

</details>

### 9. What role does fault tolerance play in microservices?

<details>
<summary>Show Answer</summary>

**Answer:**

Service separation can reduce the blast radius, but isolation is **not automatic**. Timeouts, circuit breakers, retries with backoff, bulkheads, load balancing, and fallback behavior prevent one slow service from causing a cascading failure.

</details>

### 10. How do event-driven systems handle real-time data?

<details>
<summary>Show Answer</summary>

**Answer:**

Producers publish events as soon as changes occur. Brokers such as Kafka or RabbitMQ deliver them to consumers asynchronously, allowing consumers to react without blocking the producer.

</details>

**Q&A recap:** choose by requirements, not fashion; every style has a cost.

Next: [02_Multi_Tier_Architecture.md](02_Multi_Tier_Architecture.md)
