# 06 — Scalability in System Design

This folder matches the course section **Scalability in System Design**.

Notes are based on the Section 6 slides and the four interview PDFs. Each topic file has **theory first**, then **interview Q&A**.

Section 2 introduced **why** a load balancer exists. This section goes deeper: how you grow a system, which balancer to pick, and how the cloud adds or removes machines for you.

---

## Why this section exists

A design that works for 100 users can fall over at 100,000.

```text
More users, more data, or a sale-day spike
        ↓
The system must stay fast and available
        ↓
Without spending money on idle machines all year
```

**Words in this folder**

| Word | Meaning |
|------|---------|
| **Scalability** | The product still feels fine when more people arrive |
| **Vertical scaling** | Buy a bigger computer for the same program |
| **Horizontal scaling** | Run more copies of the program and share the visitors |
| **Bottleneck** | The one part that is full while everything else still has room |
| **Autoscaling** | The cloud adds or removes copies from a rule, such as "CPU is high" |

A diagram of "many servers behind one door" is horizontal scaling. The door is the load balancer from section 2.

---

## Read in this order

| # | File | What is inside |
|---|------|----------------|
| 1 | [01_What_is_Scalability.md](01_What_is_Scalability.md) | Meaning, why systems grow, latency, bottlenecks, cost + Q&A |
| 2 | [02_Scaling_Strategies.md](02_Scaling_Strategies.md) | Vertical, horizontal, diagonal, when each fails + Q&A |
| 3 | [03_Load_Balancing.md](03_Load_Balancing.md) | L4 vs L7, algorithms, hardware vs cloud + Q&A |
| 4 | [04_Autoscaling.md](04_Autoscaling.md) | Triggers, AWS/Azure/GCP, cost, cold starts + Q&A |

---

## Section cheat sheet

| Idea | One line |
|------|----------|
| Scalability | Handle more work without becoming slow or down |
| Vertical | Make one machine bigger |
| Horizontal | Add more machines behind a distributor |
| Diagonal | Grow the box first, then add boxes |
| Load balancer | One front door, many workers, skip dead ones |
| Layer 4 | Route by IP and port. Fast |
| Layer 7 | Route by URL, header, cookie. Smarter |
| Autoscaling | Add or remove machines from metrics, not by hand |
| Cost control | Min and max limits, or the cloud bill runs away |

---

## What is next

**Storage — Database and Storage**: [07_Storage_Database](../07_Storage_Database/00_Section_Overview.md)

---

## Source PDFs used

- `System Design - updated - Section 6.pdf`
- `Interview Questions - Scalability.pdf`
- `Interview Questions - Scaling strategies.pdf`
- `Interview Questions & Answers on Load Balancing.pdf`
- `Interview questions - Autoscaling & Cloud Best Practices.pdf`
