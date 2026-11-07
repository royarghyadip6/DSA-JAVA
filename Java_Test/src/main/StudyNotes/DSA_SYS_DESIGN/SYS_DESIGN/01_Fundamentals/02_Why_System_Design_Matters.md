# 2. Why System Design Matters

## Simple idea

A feature that works on **your machine for 1 user** can fail when **1 million users** open the app at the same time.

System design exists because real products must handle:

- Many users
- Failures (a server dies, a payment API is slow)
- Growth (today 1,000 users, next year 1 crore)

```text
Works on my laptop  ≠  works for India at 8 PM
```

---

## Why products fail without design

Think of IRCTC on Tatkal morning, or a food app during a cricket match.

If everything lives in **one** Spring Boot app and **one** database:

```text
All users  →  One server  →  One database
                    |
                    x  overloaded
```

What you see as a user:

- App hangs
- "Please try again"
- Payment deducted but order not placed

The Java code may be fine. The **shape** of the system was not ready.

---

## A small real example

**WhatsApp message** looks simple: type text, hit send.

Behind it, the design must answer:

- What if the receiver is offline?
- What if two people send at the same second?
- What if one data centre is down?
- How do we keep chats for years without making the app slow?

None of those answers is a single `if` statement. They are **design decisions**.

---

## Why interviews ask system design

For junior roles, companies test coding.

For mid and senior roles (the level you are aiming at), they also ask:

> "Design a URL shortener / news feed / chat app."

They are checking:

| They watch | What it means |
|------------|----------------|
| Can you clarify the problem? | You do not jump to drawing boxes |
| Can you start simple? | First a design for 1,000 users, then scale |
| Can you name trade-offs? | Fast vs cheap, simple vs flexible |
| Can you think about failure? | Servers die; networks lag |
| Can you communicate? | Clear talk, not buzzwords |

They are not asking you to memorise Google's real architecture. They want a **clear, reasonable plan**.

---

## Why it helps you at work (not only interviews)

Even if you are a Java backend developer today, system design helps you:

- Understand why the team uses Kafka, Redis, or an API gateway
- Estimate if a new API will hurt the database
- Talk with architects without getting lost
- Avoid "just add one more table / one more REST call" that later breaks production

```text
Without system design:  "It works in QA"
With system design:     "It will still work on festival sale day"
```

---

## Interview lines

- We design systems because **load, failure, and growth** change everything.
- Interviewers care more about **your thinking** than a perfect diagram.
- Start with a simple design. Then say: "If traffic grows, I would add …"
- Always mention **what can go wrong** (server down, slow DB, duplicate payment).
- System design is a daily skill for backend engineers, not only an interview topic.

---

## Interview Q&A — 6 to 8 years

These questions are for someone who has seen production break. Try the answer out loud, then open it.

### S1. The feature works in QA. What still makes you nervous before a sale day?

<details>
<summary>Show Answer</summary>

**Answer:**

QA has few users, a warm cache, and no partial failure. Sale day has a burst, slow downstreams, and retries. I look at the hottest row or lock, the connection pool, and what happens when payment is slow. I want a limit on how many requests I accept, and a queue for work the user can wait for.

**Corner cases:** The test database is empty, so a query that scans every order looks fast. A connection pool of 20 is fine in QA and stalls when 2,000 requests wait on one slow query. Caches are cold at the start of the sale, so the database sees the full load for the first minute. A retry storm from the app multiplies that load.

</details>

### S2. How do you explain a tradeoff to a product manager who wants all three: fast, cheap, and never wrong?

<details>
<summary>Show Answer</summary>

**Answer:**

Pick the rule that loses the company money if it is wrong, and spend there. Stock and payments stay correct and a bit slower. The product page can be a few seconds stale and very fast. Say the cost of the third wish in plain words: "always fresh and always fast means we pay for a much bigger database, or we accept that some pages lie."

**Corner cases:** "Never wrong" for a like-count is waste. "A bit stale" for a bank balance is a defect. Legal or safety rules are not a tradeoff you can bargain. If they still want all three, write the assumption down so the later incident has an owner.

</details>

### S3. A junior says "we will add Kafka and Kubernetes so it scales." What do you ask?

<details>
<summary>Show Answer</summary>

**Answer:**

What number broke, and which box is the bottleneck? A queue does not make a slow query faster. It moves the wait. Kubernetes does not fix a design that stores all sessions on one server. I ask for the metric (p99 latency, error rate, pool wait) and the smallest change that moves that metric.

**Corner cases:** The bottleneck is a single hot key (one celebrity, one flash SKU). More servers all hit the same row. The bottleneck is a lock inside one process. More pods make it worse. The team cannot operate the new tool at 3 a.m. Operational cost is part of the design.

</details>

### S4. After an incident, what do you change in the design, not only in the runbook?

<details>
<summary>Show Answer</summary>

**Answer:**

I write the user-visible failure in one sentence, then the missing guard. Example: "a retried checkout charged twice" becomes an idempotency key and a unique payment row. "The site died when Redis died" becomes a timeout and a path that still serves a slower page. The runbook is how humans help. The design change is how the next time does not need a human.

**Corner cases:** Fixing only the alert means you page faster and still fail. A backup you have never restored is not a fix. Two teams "fix" the same bug in different services and now the rules disagree. One owner for the invariant (a seat is sold once) matters more than another dashboard.

</details>

**Senior recap:** load, partial failure, and retries are why the happy path is not a design.

## Check yourself

Name three reasons a working Spring Boot app can still fail in production:

1. Too many users at once
2. One part (DB / payment / SMS) goes down
3. Data grows so large that queries become slow

If that feels obvious, you already understand *why* this course exists.

Next: [03_How_to_Learn_and_Use_This_Course.md](03_How_to_Learn_and_Use_This_Course.md)
