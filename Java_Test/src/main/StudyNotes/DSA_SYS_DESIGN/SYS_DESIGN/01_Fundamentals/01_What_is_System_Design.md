# 1. What is System Design?

## Simple idea

**Coding** answers: *How do I write this feature?*

**System design** answers: *How do all the pieces work together when many users use the product?*

You already write Java classes, APIs, and database queries. System design zooms out. You decide:

- Which services exist
- How they talk
- Where data lives
- What happens when traffic grows or something breaks

```text
One Java class          =  one small brick
One Spring Boot API     =  one room
System design           =  the whole building
```

---

## A picture in your head

Imagine a food-delivery app (like Zomato).

A user taps **Place Order**. Many parts must work together:

```text
  Phone app
      |
      v
  Backend API
      |
      +---> Restaurant service
      +---> Payment service
      +---> Delivery-partner service
      +---> Database
      +---> SMS / push notification
```

Writing the `createOrder()` method is coding.

Deciding *this flow*, *this storage*, *this way to handle 1 lakh orders at dinner time* is **system design**.

---

## Coding vs System Design

| | Coding (what you already do) | System Design (what you are learning) |
|--|------------------------------|----------------------------------------|
| Question | How do I implement this? | How should the whole product be built? |
| Focus | Classes, methods, SQL, APIs | Users, traffic, data, failures, growth |
| Output | Working code | An architecture + trade-offs |
| Example | Write `OrderController` | Decide how orders, payments, and delivery scale |

Both matter. Good code inside a bad design still fails at scale.

---

## High-Level Design vs Low-Level Design (short)

```text
HLD  =  city map     (whole system: boxes, data, scale)
LLD  =  floor plan   (one service: classes, schema, patterns)
```

This course is mainly **HLD**. Java interviews often have a **separate LLD** round.

Full note with examples and Q&A: [04_HLD_vs_LLD.md](04_HLD_vs_LLD.md)

---

## What a "system" actually is

A **system** is not one program. It is a set of parts that share a goal.

For WhatsApp, the goal is: *send a message to a friend, quickly and reliably.*

Parts might include:

- The mobile app
- Servers that receive messages
- Storage for chat history
- A way to push the message to the other phone

You design the **connections** and **responsibilities**, not only one class.

---

## Interview lines

- System design is how we plan a product so it works for **many users**, not only for one laptop.
- Coding builds a feature. System design builds the **shape** of the product.
- HLD = boxes and arrows (this course). LLD = classes and code structure.
- In interviews they want: requirements → simple design → then improvements (scale, failure, cost).
- There is rarely one "correct" design. There are **trade-offs**. You must say *why* you chose something.

---

## Interview Q&A — 6 to 8 years

These questions are for someone who has built real systems. Try the answer out loud, then open it. Each answer names a corner case.

### S1. The interviewer says "design X" and stops. What do you do in the first three minutes?

<details>
<summary>Show Answer</summary>

**Answer:**

Do not draw boxes yet. Ask who uses it, which one action must be correct, and what "good" means (how slow is too slow, how much data loss is allowed, how many users). State one number if they do not give one. Name one thing you will not design today.

**Corner cases:** They say "you decide." Pick a number, say it is an assumption, and offer to change it. They keep adding features. Park each one as a non-goal so the core path stays visible. They want low-level design. Ask once: "Do you want the map of services, or the classes inside one service?"

</details>

### S2. Two designs both work. How do you pick, out loud?

<details>
<summary>Show Answer</summary>

**Answer:**

Name the thing you are protecting: correctness, latency, cost, or how hard it is to operate. Then say what you give up. "I keep stock in one SQL row so two buyers cannot both succeed. I give up write throughput, and I will shard only when that row is the bottleneck."

**Corner cases:** The cheaper design fails the correctness rule (double charge, double seat). Reject it even if it is faster. The correct design is too slow for the read path. Split the paths: strong write, slightly stale read. Do not pretend one database mood fits both.

</details>

### S3. A design works in the diagram and fails on the second request. What do you check?

<details>
<summary>Show Answer</summary>

**Answer:**

Retries. A timeout does not mean the first call failed. Payment, seat hold, and "create user" must be safe to run twice. Use an idempotency key or a unique constraint. Also check partial failure: the database saved the row and the email service timed out. The user must not be charged twice, and the email can be sent later from a queue.

**Corner cases:** The client retries with a new key, so you cannot see it is the same action. The key must be created before the first try and reused. Two regions both accept the write during a network split. Say which side you refuse. A unique index stops duplicates only if both tries hit the same database.

</details>

### S4. How do you talk about failure without designing a second company?

<details>
<summary>Show Answer</summary>

**Answer:**

Pick the one box whose death hurts the product. Say what the user sees, what retries, and what you refuse to do while it is down. "If the payment provider is down, I keep the seat hold until it expires. I do not mark the seat sold." Mention one spare (a second instance, a replica) and stop. Depth on one failure beats a speech about every box.

**Corner cases:** The spare is in the same rack, so one power cut kills both. Say you want a second zone for the data you cannot lose. Health checks that only ping the process will send traffic to a server whose database is dead. The check must touch the dependency that matters. A cache hides a dead database until the cache expires, then everything fails at once.

</details>

**Senior recap:** clarify the one hard path, name the tradeoff, make retries safe, and fail one box on purpose.

## Check yourself

Can you explain this in one sentence?

> System design is deciding the parts of a product, how they talk, where data lives, and how the product stays fast and reliable when many people use it.

If yes, read [04_HLD_vs_LLD.md](04_HLD_vs_LLD.md), then [02_Why_System_Design_Matters.md](02_Why_System_Design_Matters.md).
