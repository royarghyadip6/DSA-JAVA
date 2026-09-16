# 6. Introduction to Load Balancing

## Simple idea

One server is like **one cashier**. When the shop gets crowded, the queue explodes.

A **load balancer** is the person who says: "Counter 2 is free — go there."

```text
All users  →  Load balancer  →  Server A
                             →  Server B
                             →  Server C
```

Clients talk to **one entry point**. They do not pick a server themselves.

---

## The scaling problem

Every machine has a limit: CPU, RAM, disk, network.

As traffic grows:

- Latency goes up
- Requests queue
- Timeouts and 500s start

**Vertical scaling** = buy a bigger cashier desk (more CPU / RAM on the **same** box).

That helps for a while. Then:

- Hardware has a max size
- Bigger boxes get very expensive
- If that one box dies, **everything** dies

```text
Vertical  =  scale UP   (one fat server)
Horizontal = scale OUT  (many normal servers)
```

---

## Horizontal scaling creates a new problem

Adding servers adds capacity. But now:

- Who gets the next request?
- Users must not type `server3.internal`.
- Some servers must not sit idle while others melt.

**That problem is why load balancers exist.**

---

## What the load balancer does

- Becomes the **front door** of the system
- Spreads requests across healthy app servers
- Removes a dead server from the pool
- Makes maintenance safer (drain one box, others still serve)

```text
Before:  Users → Server  (single point of pain)
After:   Users → LB → many servers  (scale + spare)
```

This course intro focuses on **why** it exists. Algorithms (round robin, least connections, L4 vs L7) appear in IP / proxy / client-server Q&A. Remember them as "how it picks a server".

| Method | Simple meaning |
|--------|----------------|
| Round robin | Take turns |
| Least connections | Send to the quietest server |
| IP hash | Same client IP → same server (sticky-ish) |
| Layer 4 | Decide using IP + port (TCP) |
| Layer 7 | Decide using URL / headers / cookies (HTTP) |

DNS can also spread traffic (several IPs for one name). That is **DNS load balancing** — related, but not the same box as Nginx/ELB.

---

## Reliability, not only speed

Multiple servers = **redundancy**.

If Server B dies:

```text
LB health-check fails for B
LB stops sending traffic to B
A and C keep working
```

You can patch Server B without taking the whole app down.

---

## When to add a load balancer

Add it when:

- One server cannot keep up
- You need more than one app instance
- Business wants higher uptime
- You start **horizontal** scaling

A tiny internal tool with 10 users can wait. A public API on sale day cannot.

---

## Evolution story (say this in interviews)

```text
1. One server — simple, cheap
2. Traffic grows — that server hurts
3. Vertical scale — bigger box, still one failure point
4. Add more app servers — now traffic is messy
5. Put a load balancer in front — one VIP, many workers
```

That story is the whole lecture.

---

## Interview lines

- Load balancing solves "one machine cannot take all traffic".
- Vertical = bigger box (limits). Horizontal = more boxes (needs a distributor).
- LB = single entry + spread + skip dead servers.
- Introduce it when you scale out or need availability.

---

## Interview Q&A

Read the question. Answer out loud. Then open the answer.

This lecture is the **why**, not every algorithm. Still name round robin / L4 vs L7 if they ask how.

### 1. What problem does load balancing solve?

<details>
<summary>Show Answer</summary>

**Answer:**

Growing traffic on **one** server. A load balancer **spreads** requests across many servers so no single machine does all the work.

Result: better performance, room to grow, and the site can survive one server failing.

</details>

### 2. Why does a single server eventually become a bottleneck?

<details>
<summary>Show Answer</summary>

**Answer:**

CPU, RAM, disk, and network are **finite**. Past that: slow responses, queues, timeouts.

No box is infinitely powerful. Growth always hits a wall on one machine.

</details>

### 3. What is the difference between vertical scaling and horizontal scaling?

<details>
<summary>Show Answer</summary>

**Answer:**

| Vertical (scale **up**) | Horizontal (scale **out**) |
|-------------------------|----------------------------|
| More CPU/RAM on the **same** server | **Add servers** and share work |
| Simple (one machine) | Needs a way to split traffic |
| Hardware and money limits | Much larger ceiling |
| One failure takes everything | Other servers can continue |

</details>

### 4. Why is horizontal scaling often preferred over vertical scaling?

<details>
<summary>Show Answer</summary>

**Answer:**

Vertical is a **temporary** upgrade. Fat servers get expensive and remain **one** failure point.

Horizontal lets you keep adding capacity and survive a dead node — but then you need a load balancer.

</details>

### 5. What challenge arises when an application is deployed across multiple servers?

<details>
<summary>Show Answer</summary>

**Answer:**

**Who gets the next request?**

Users must not pick servers by hand. Without a distributor, some servers overload while others sit idle. That challenge **is** the load balancer.

</details>

### 6. What role does a load balancer play in a distributed system?

<details>
<summary>Show Answer</summary>

**Answer:**

It is the **single entry point**. Clients send to the LB; the LB forwards to a healthy backend.

This hides server count, enables scale-out, and is the base for high availability.

</details>

### 7. How does load balancing improve scalability?

<details>
<summary>Show Answer</summary>

**Answer:**

Add a new app instance → LB starts sending it traffic. Capacity grows in **small steps**. You do not keep buying a bigger and bigger single machine.

</details>

### 8. How does load balancing improve availability?

<details>
<summary>Show Answer</summary>

**Answer:**

The app no longer depends on **one** process. If a server is down for crash or patching, others still answer.

</details>

### 9. What happens if one server fails in a load-balanced architecture?

<details>
<summary>Show Answer</summary>

**Answer:**

Health check fails → LB **stops** routing to that server → remaining healthy servers take the load. Failure is **isolated**.

</details>

### 10. Why is load balancing considered a foundational component of modern system design?

<details>
<summary>Show Answer</summary>

**Answer:**

Scale, availability, reliability, and fault tolerance all need **more than one instance** plus a way to share traffic. Load balancing is that building block.

</details>

### 11. When should you introduce a load balancer into a system?

<details>
<summary>Show Answer</summary>

**Answer:**

When one server **cannot** meet speed, scale, or uptime needs. Especially as soon as you choose **horizontal** scaling. A 10-user internal tool can wait.

</details>

### 12. Walk me through the evolution from a single-server application to a load-balanced architecture.

<details>
<summary>Show Answer</summary>

**Answer:**

```text
1. One server — simple and cheap
2. Traffic grows — that server hurts
3. Vertical scale — bigger box, still one failure point
4. Add more app servers — traffic distribution is now the problem
5. Put a load balancer in front — one VIP, many workers, skip dead nodes
```

That evolution **is** the expected answer.

</details>

**If they go deeper:** round robin, least connections, IP hash. L4 = IP/port. L7 = URL/headers. DNS can also return multiple IPs.

**Q&A recap:** Bottleneck → vertical vs horizontal → LB as front door → scale + availability → evolution story.

Next: [07_API_Gateway.md](07_API_Gateway.md)
