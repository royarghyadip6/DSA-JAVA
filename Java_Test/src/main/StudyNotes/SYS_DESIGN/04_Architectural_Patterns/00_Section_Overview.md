# 04 — Architectural Patterns

This folder matches the course section **Architectural Patterns (System Design Fundamentals)**.

Notes are based on the Section 4 slides and all four interview PDFs. Each topic file contains **theory first**, followed by **interview Q&A**.

---

## Why this section exists

An architecture describes:

- the major parts of a system
- what each part owns
- how those parts interact

```text
Requirements
     ↓
Architecture choice
     ↓
Scalability + performance + maintainability + cost
```

There is no architecture that is best for every product. A small application may need a monolith; a large product may combine tiers, microservices, and events.

---

## Read in this order

| # | File | What is inside |
|---|------|----------------|
| 1 | [01_Architecture_Styles.md](01_Architecture_Styles.md) | Monolith, layered, client-server, microservices, event-driven + Q&A |
| 2 | [02_Multi_Tier_Architecture.md](02_Multi_Tier_Architecture.md) | 2-tier, 3-tier, N-tier, scaling and security + Q&A |
| 3 | [03_Microservices_Architecture.md](03_Microservices_Architecture.md) | Service boundaries, communication, consistency, deployment + Q&A |
| 4 | [04_Event_Driven_Architecture.md](04_Event_Driven_Architecture.md) | Events, brokers, pub-sub, streams, retries, ordering + Q&A |

---

## Section cheat sheet

| Style | Simple meaning | Good starting use |
|-------|----------------|-------------------|
| **Monolith** | One deployable application | Small team, MVP, simple domain |
| **Layered / N-tier** | UI, business, and data responsibilities are separated | Typical enterprise/web application |
| **Client-server** | Client requests; server provides | Web, mobile, database applications |
| **Microservices** | Independently deployable services by business capability | Large domain, independent teams/scaling |
| **Event-driven** | Producers publish facts; consumers react later | Async workflows, high volume, loose coupling |

Real systems combine styles:

```text
Web / Mobile
     ↓
API Gateway
     ↓
Microservices (each service may use layers internally)
     ↓
Kafka events for asynchronous workflows
```

---

## How to choose

Ask these questions:

1. What does the business need now?
2. How much traffic and data are expected?
3. Which parts must scale independently?
4. How many teams deploy the product?
5. How much operational complexity can the company support?
6. What consistency and response-time guarantees are required?

> Start with the simplest architecture that meets the requirements. Add distribution only when it solves a real problem.

---

## What is next

**Web Concepts in System Design**: [05_Web_Concepts](../05_Web_Concepts/README.md)

---

## Source PDFs used

- `System Design - updated - Section 4.pdf`
- `Software Architecture Patterns & Styles.pdf`
- `Multi-Tier Architecture.pdf`
- `Microservices Architecture.pdf`
- `Event-driven architecture.pdf`
