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

## Check yourself

Can you explain this in one sentence?

> System design is deciding the parts of a product, how they talk, where data lives, and how the product stays fast and reliable when many people use it.

If yes, read [04_HLD_vs_LLD.md](04_HLD_vs_LLD.md), then [02_Why_System_Design_Matters.md](02_Why_System_Design_Matters.md).
