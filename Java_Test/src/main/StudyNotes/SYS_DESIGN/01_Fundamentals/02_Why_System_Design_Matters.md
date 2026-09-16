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

## Check yourself

Name three reasons a working Spring Boot app can still fail in production:

1. Too many users at once
2. One part (DB / payment / SMS) goes down
3. Data grows so large that queries become slow

If that feels obvious, you already understand *why* this course exists.

Next: [03_How_to_Learn_and_Use_This_Course.md](03_How_to_Learn_and_Use_This_Course.md)
