# 2. Multi-Tier Architecture

## Simple idea

Multi-tier architecture separates an application into independently managed responsibilities.

```text
Presentation  →  Business Logic  →  Data
   React          Spring Boot        MySQL
```

It improves separation, security, maintainability, and the ability to scale different parts.

---

## 2-tier architecture

The client talks directly to the database.

```text
Desktop App  ↔  SQL Database
UI + logic       Data
```

**Advantages:** simple, quick, and fast for a few users.

**Problems:** database credentials live on clients, direct DB exposure is risky, logic is duplicated, and scaling is poor.

**Use:** small internal desktop tools—not public web applications.

---

## 3-tier architecture

A business/API tier sits between the UI and database.

```text
Browser / Mobile
       ↓
Spring Boot API
       ↓
MySQL / PostgreSQL
```

| Tier | Responsibility |
|------|----------------|
| Presentation | UI and user interaction |
| Business | Validation, rules, authentication, workflow |
| Data | Persistent storage and queries |

**Advantages:** DB is hidden, concerns are separated, backend instances can scale, maintenance is easier.

**Cost:** one more network/processing hop adds a little latency.

This is the standard shape of many web applications.

---

## N-tier architecture

N-tier adds specialized tiers when requirements justify them.

```text
Client
  ↓
CDN / Load Balancer
  ↓
API Gateway
  ↓
Business Services
  ↓
Cache
  ↓
Database / Object Storage
```

Possible tiers include authentication, gateway, cache, messaging, search, and reporting.

More tiers do **not** automatically make a better system. Every tier adds:

- network latency
- deployment and monitoring work
- infrastructure cost
- another security boundary

---

## Layer vs tier

- **Layer** is a logical code separation and may run in one JVM.
- **Tier** is commonly a separately deployed process/machine.

```text
One Spring Boot JAR:
Controller → Service → Repository       = three layers, one tier/deployable

Browser → API servers → DB servers      = three physical tiers
```

People sometimes use the terms interchangeably; explain your meaning in an interview.

---

## Scaling tiers

- **Vertical:** more CPU/RAM for one node
- **Horizontal:** more nodes behind a load balancer
- Each tier can scale separately: 20 APIs, 3 cache nodes, 2 DB replicas

Load balancers may sit:

- between clients and API servers
- between gateways and services
- in front of read replicas (with database-aware routing)

Algorithms: round robin, least connections, IP hash.

---

## Reducing latency

| Technique | Benefit |
|-----------|---------|
| Redis/Memcached | Avoid repeated DB work |
| CDN | Static content near users |
| Async queues | Move email/report work off the request path |
| Connection pool | Reuse DB connections |
| Fewer network hops | Remove unnecessary tiers |
| Load balancing | Avoid overloaded nodes |

An API Gateway centralizes external entry; it does not magically reduce every internal hop.

---

## Security between tiers

- TLS/mTLS to encrypt traffic
- JWT, OAuth, or service credentials
- zero-trust identity: every call proves itself
- network segmentation/private subnets
- DB accepts traffic only from the business tier
- firewalls and intrusion detection
- least-privilege accounts

```text
Internet → Public LB → Private API subnet → Private DB subnet
                                            (not internet-accessible)
```

---

## Fault-tolerant high-traffic design

```text
Users
  ↓
Multi-region DNS / CDN
  ↓
Load balancers
  ↓
Auto-scaled stateless APIs
  ↓
Cache + queues
  ↓
Replicated databases
```

Add health checks, circuit breakers, retries with backoff, database replication, and events for background work.

For banking, use ACID storage for money and strict access controls. Microservices are not automatically “ideal”; choose them only if organizational and scaling needs justify distributed complexity.

---

## Interview Q&A

### 1. What is multi-tier architecture, and why is it used?

<details>
<summary>Show Answer</summary>

**Answer:** It separates UI, business logic, and data into tiers. This improves maintainability, security, modularity, and independent scaling.

</details>

### 2. How does 2-tier differ from 3-tier?

<details>
<summary>Show Answer</summary>

**Answer:**

2-tier is client ↔ database: simple but insecure and hard to scale.

3-tier is client ↔ API/business ↔ database: DB is hidden, logic is centralized, and the API tier scales independently.

</details>

### 3. What are the components of 3-tier architecture?

<details>
<summary>Show Answer</summary>

**Answer:** Presentation (React/mobile), business logic (Java/.NET/Node), and data (PostgreSQL/MySQL/MongoDB).

</details>

### 4. Give an N-tier example.

<details>
<summary>Show Answer</summary>

**Answer:** A banking system may use web/mobile UI, load balancer, authentication/gateway, transaction services, Redis cache, SQL database, and audit/event tiers.

</details>

### 5. How does multi-tier improve scalability?

<details>
<summary>Show Answer</summary>

**Answer:** Each tier can scale vertically or horizontally according to its load. Stateless APIs can add instances without scaling the UI CDN or primary database equally.

</details>

### 6. What are the challenges of adding more tiers?

<details>
<summary>Show Answer</summary>

**Answer:** More latency, deployments, cost, security boundaries, failure points, and possible consistency problems. Add a tier only when it owns a useful responsibility.

</details>

### 7. How does load balancing work in N-tier systems?

<details>
<summary>Show Answer</summary>

**Answer:** Load balancers distribute calls among replicas at a tier using round robin, least connections, or IP hash. Health checks remove failed nodes.

</details>

### 8. How do you reduce latency?

<details>
<summary>Show Answer</summary>

**Answer:** Cache hot data, use a CDN, move slow work to queues, pool DB connections, remove unnecessary hops, and scale overloaded tiers.

</details>

### 9. When choose microservices over traditional N-tier?

<details>
<summary>Show Answer</summary>

**Answer:** When business capabilities need independent teams, releases, or scaling. Keep a layered/N-tier application when those needs do not justify distributed complexity.

</details>

### 10. How do you secure inter-tier communication?

<details>
<summary>Show Answer</summary>

**Answer:** TLS/mTLS, JWT/OAuth or service identity, least privilege, private networks, firewall rules, zero trust, and monitoring. Never expose the database directly.

</details>

### 11. How would you make a high-traffic multi-tier system fault-tolerant?

<details>
<summary>Show Answer</summary>

**Answer:** Multi-region entry, redundant load balancers, auto-scaled stateless services, replicated DBs, cache, queues, health checks, circuit breakers, and controlled retries.

</details>

**Q&A recap:** 2-tier is direct; 3-tier inserts business logic; N-tier adds only useful specialized tiers.

Next: [03_Microservices_Architecture.md](03_Microservices_Architecture.md)
