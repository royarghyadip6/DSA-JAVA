# 08 — Performance: Concepts, Tools, and Techniques

This folder matches the course section **Performance — Concepts, Tools & Techniques**.

Notes use the Section 8 slides and the interview PDFs. Each file is **theory first**, then **interview Q&A**.

---

## Why this section exists

A feature that is correct but slow still fails. Users leave, and the system falls over on a busy day.

```text
How fast is one request?     →  latency
How many can we finish?      →  throughput
Are the slow ones hidden?    →  percentiles (P95, P99)
What do we promise?          →  SLA / SLO / SLI
```

Then you make it faster: cache, queue, async work, and a healthier database.

**Words in this folder**

| Word | Meaning |
|------|---------|
| **Latency** | Wait time for one request |
| **Throughput** | How many requests finish each second |
| **P95 / P99** | The slow requests. P95 means 95% of people were at least this fast. The rest waited longer |
| **Cache** | A fast copy of a hot answer |
| **Async** | The user is not kept waiting while the slow work happens |
| **Thread** | One worker inside a program. Many threads share the program's memory |

A picture of a queue means "accept the request, do the heavy work later."

---

## Read in this order

| # | File | What is inside |
|---|------|----------------|
| 1 | [01_System_Performance.md](01_System_Performance.md) | Latency, throughput, percentiles, testing + Q&A |
| 2 | [02_Caching.md](02_Caching.md) | Where to cache, write strategies, eviction + Q&A |
| 3 | [03_Messaging_and_Queues.md](03_Messaging_and_Queues.md) | Queues, delivery guarantees, RabbitMQ vs Kafka + Q&A |
| 4 | [04_Concurrency_and_Parallelism.md](04_Concurrency_and_Parallelism.md) | Threads, async I/O, races, deadlocks + Q&A |
| 5 | [05_Database_Performance.md](05_Database_Performance.md) | Indexes, queries, pooling, reads vs writes + Q&A |

---

## One-page picker

| Symptom | First place to look |
|---------|---------------------|
| One call feels slow | Latency, traces, the slow span (DB, remote call) |
| Site dies only at peak | Throughput, pools, queues, autoscale |
| Average looks fine, users complain | P95 and P99 |
| Same data fetched again and again | Cache (browser, CDN, Redis) |
| User waits on email, PDF, or a payment side effect | Queue |
| CPU is busy, many threads stuck | Thread pool, locks, deadlock |
| One SQL call scans a huge table | Index, query plan, pagination |

---

## What is next

**Reliability, availability, and disaster recovery**: [09_Reliability_Availability_DR](../09_Reliability_Availability_DR/00_Section_Overview.md)

---

## Source PDFs used

- `System Design - updated - Section 8.pdf`
- System Performance Q&A
- Caching for Speed Optimization Q&A
- Messaging & Queues for Decoupling Q&A
- Concurrency & Parallelism Q&A
- Database Performance Optimization Techniques Q&A
