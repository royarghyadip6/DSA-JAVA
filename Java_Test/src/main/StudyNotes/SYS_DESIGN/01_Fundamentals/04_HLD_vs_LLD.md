# 4. High-Level Design vs Low-Level Design

## Simple idea

Both are **design**. They zoom to a different level.

```text
HLD  =  the city map
        Which areas exist? How do roads connect them?

LLD  =  the floor plan of one building
        Which rooms? Which doors? Which furniture?
```

You need **both**. A perfect class diagram cannot save a product that chose the wrong services. Beautiful boxes-and-arrows cannot save messy, untestable Java.

This Udemy course is mainly **HLD**. Your Java interviews often have a **separate LLD** round.

---

## One picture

Take **Place Order** on a food app.

```text
HLD (whole product)

  App → API Gateway → Order Service → Payment
                                   → Restaurant
                                   → Delivery
                                   → DB / Cache / Queue


LLD (inside Order Service)

  OrderController
       ↓
  OrderService          ← uses strategy for "pay now" vs "pay later"
       ↓
  OrderRepository       PaymentClient      EventPublisher
       ↓                     ↓                   ↓
  orders table          HTTP to Payment     Kafka topic
```

HLD answers: *What boxes exist, and how does an order travel?*

LLD answers: *What classes, methods, and tables implement the Order box?*

---

## High-Level Design (HLD)

**HLD** is the **architecture** of the system. You look at the product from 10,000 feet.

You decide:

| Decision | Example |
|----------|---------|
| What major parts exist? | App, API, Order, Payment, DB, cache, queue |
| How do they talk? | REST, gRPC, Kafka events |
| Where does data live? | MySQL for orders, Redis for sessions, S3 for images |
| How do we scale? | Stateless APIs + load balancer + read replicas |
| How do we survive failure? | Retry payment, queue notifications, multi-AZ |
| What are the numbers? | 10k orders/min, 100:1 read/write |

**Typical HLD output**

- Boxes-and-arrows diagram
- Functional + non-functional requirements
- API list (names and purpose, not every DTO field)
- Which storage for which data
- Rough capacity (users, QPS, storage)
- Trade-offs ("SQL for money, NoSQL for feed")

**Who uses it**

- System design interview ("Design WhatsApp / TinyURL")
- Architects and senior engineers before a big feature
- This course (Sections 2 → case studies)

Think: **what** we build, **which** pieces, **why** this shape.

---

## Low-Level Design (LLD)

**LLD** is the **close-up** of one feature or one service. You look at the code structure.

You decide:

| Decision | Example |
|----------|---------|
| Which classes? | `OrderService`, `PaymentGateway`, `OrderRepository` |
| Which interfaces? | `PaymentGateway` so Razorpay/Stripe can swap |
| Which patterns? | Strategy, Factory, Observer, Singleton (when truly needed) |
| Method flow? | Sequence: validate → reserve stock → pay → save → event |
| Exact schema? | `orders(id, user_id, status, amount, created_at)` |
| SOLID / OOP? | One class, one job; no god `OrderManager` |

**Typical LLD output**

- Class diagram
- Sequence diagram (who calls whom)
- Important method signatures
- Table / entity design
- Error and concurrency handling inside that module
- Sometimes a short coding exercise (parking lot, rate limiter, splitwise)

**Who uses it**

- LLD / machine-coding interview rounds
- Java backend interviews (design patterns + OOP)
- You, before writing a non-trivial Spring service

Think: **how** this box is implemented in code.

---

## Side-by-side

| | HLD | LLD |
|--|-----|-----|
| Zoom | Whole system | One module / feature |
| Question | How do pieces fit for many users? | How do we code this piece cleanly? |
| Diagram | Components, arrows, data stores | Classes, interfaces, sequence |
| Data | SQL vs NoSQL, cache, shards (idea) | Tables, columns, indexes, entities |
| APIs | `POST /orders` exists; auth at gateway | Request DTO, validation, service method |
| Scale | LB, replicas, queues, CDN | Connection pool, async, locking |
| Time in interview | ~45–60 min "Design X" | ~45–60 min "Design parking lot" |
| Failure talk | Region down, queue backup | Null, timeout, duplicate order id |
| This course | **Main focus** | Mentioned; not the main path |

---

## Same problem, two answers (URL shortener)

**HLD**

```text
Client → API
           ├─ POST /shorten  →  write DB + cache
           └─ GET  /abc12X   →  cache  →  DB  →  301 redirect

ID: Base62 of a unique number
Store: key-value (short_code → long_url)
Scale: cache hot links; many read replicas
```

**LLD** (inside the shorten service)

```text
UrlController.shorten(request)
    → UrlService.shorten(longUrl)
         → IdGenerator.nextId()          // Snowflake or DB sequence
         → Base62Encoder.encode(id)
         → UrlRepository.save(mapping)
         → Cache.put(code, longUrl)

Classes: UrlService, IdGenerator, Base62Encoder, UrlRepository
Watch: uniqueness, race on custom alias, validation
```

If the interviewer says "design URL shortener", start **HLD**. If they say "write classes for a URL shortener", that is **LLD**.

---

## Same problem, two answers (parking lot — classic LLD)

HLD would be tiny: app → API → DB.

Interviews use parking lot for **LLD**:

```text
ParkingLot
  floors[]
  park(vehicle) / unpark(ticket)

ParkingSpot     Vehicle        Ticket
  type          type, plate    spot, entryTime
  isFree

FeeCalculator   (strategy: hourly vs mall vs airport)
```

That is classes and rules, not Kafka and Kubernetes.

---

## How they connect in real work

```text
1. Product asks: "We need Place Order"
2. HLD:  which services, DB, queue, who calls whom
3. LLD:  for Order Service — classes, schema, APIs in detail
4. Code: Java / Spring Boot
```

Never skip HLD and jump to 20 classes. Never stop at a pretty diagram and hand vague work to developers.

A useful rule:

> HLD chooses the **boxes**. LLD designs the **inside** of a box. Coding **fills** the box.

---

## What interviewers expect

**HLD round (this course)**

1. Clarify (users, scale, must-have features)
2. Simple first design (client → API → DB)
3. Improve (cache, LB, queue, split services)
4. Say trade-offs

They rarely want a full class diagram.

**LLD round**

1. List entities and use cases
2. Classes + relationships
3. A sequence for the main flow
4. Edge cases (full lot, duplicate payment, concurrency)

They rarely want you to design a global CDN.

**Java backend (your profile)**

You will likely face **both**: one day TinyURL HLD, another day "design a rate limiter / notification dispatcher" with classes.

---

## Common mix-ups

| Mix-up | Better |
|--------|--------|
| Drawing 15 classes in an HLD round | Stay at services, data, traffic |
| Saying "I'll use Kafka" with no box purpose | Name the problem Kafka solves |
| LLD with only Spring annotations, no model | Start with domain objects |
| Treating HLD as "list of AWS services" | Start with requirements and flow |
| Treating LLD as "copy Singleton notes" | Model the real use cases first |

---

## Interview lines

- HLD = city map (components, data, scale). LLD = floor plan (classes, schema, patterns).
- This course trains HLD. LLD is OOP + design patterns on one feature.
- I start HLD with requirements and a simple flow, then scale it.
- I start LLD with entities and use cases, then classes and sequence.
- You cannot LLD all of YouTube in one hour. You HLD YouTube, then LLD one service (upload, or comments).

---

## Do SOLID, design patterns, ACID belong to "system design"?

Short answer: **they are all design knowledge**, but they sit at **different zoom levels**. This Udemy course does **not** teach all of them.

```text
SOLID + GoF patterns (Singleton, Factory, Strategy…)
        =  LLD / OOP  (how you write classes)

ACID (and BASE, transactions)
        =  database behaviour  (used inside HLD when you pick storage)

This course
        =  HLD  (boxes, traffic, scale, case studies)
```

| Topic | Is it system design? | In *this* course? | Where you already have notes |
|-------|----------------------|-------------------|------------------------------|
| **SOLID** | LLD — clean classes | **No** | `StudyNotes/JAVA/047_SOLID_Principles.md` |
| **Design patterns** (Factory, Observer, …) | LLD | **No** (Architectural Patterns here means monolith / microservices, not GoF) | `StudyNotes/JAVA/041`–`046` |
| **ACID** | Yes, as **storage / consistency** | **Likely later**, in Storage (folder `07`) — not a full DB-internals course | `StudyNotes/JAVA/063_SQL_Database.md`, Spring transactions |
| **CAP, sharding, cache, LB** | Core HLD | **Yes** | This `SYS_DESIGN` folder |

The same instructor has **other** Udemy courses for LLD, SOLID, and design patterns. Do not expect this one video series to replace those.

**How you use them together in an interview**

- "Design a payment system" (HLD) → you may **say** "orders need ACID, so SQL + transactions."
- You do **not** start explaining SRP and Factory unless they ask LLD for the Payment module.

---

## Interview Q&A

Read the question. Answer out loud. Then open the answer.

### 1. What is High-Level Design?

<details>
<summary>Show Answer</summary>

**Answer:**

HLD is the **architecture** of the whole system: major components, how they communicate, where data lives, and how the system scales and survives failure.

Output: component diagram, APIs at a glance, storage choices, capacity, trade-offs.

Example: for Zomato Place Order, HLD shows App, Order, Payment, Restaurant, DB, queue — not `OrderService.java` methods.

</details>

### 2. What is Low-Level Design?

<details>
<summary>Show Answer</summary>

**Answer:**

LLD is the **detailed design of one module**: classes, interfaces, patterns, method flow, and table/entity design.

Output: class diagram, sequence diagram, important signatures, schema.

Example: inside Order Service — `OrderController` → `OrderService` → `OrderRepository` + `PaymentClient`.

</details>

### 3. What is the difference between HLD and LLD?

<details>
<summary>Show Answer</summary>

**Answer:**

| HLD | LLD |
|-----|-----|
| Whole system | One feature / service |
| Boxes and arrows | Classes and sequences |
| Scale, storage, communication | SOLID, patterns, schema |
| "Design WhatsApp" | "Design a parking lot" |

HLD chooses the boxes. LLD designs inside a box.

</details>

### 4. Which one is asked in system design interviews?

<details>
<summary>Show Answer</summary>

**Answer:**

The round called **System Design** is almost always **HLD**.

**LLD** is a different round (sometimes called Low-Level Design or machine coding).

This Udemy course is HLD-first. Still learn LLD for Java interviews.

</details>

### 5. Can you give one example of both for the same feature?

<details>
<summary>Show Answer</summary>

**Answer:**

**URL shortener**

- HLD: API, unique-id generation, key-value store, cache, 301 redirect, read-heavy scale
- LLD: `UrlService`, `IdGenerator`, `Base62Encoder`, `UrlRepository`, uniqueness and races

**Place Order**

- HLD: Order / Payment / Restaurant services, DB, queue for SMS
- LLD: service classes, payment interface, `orders` table, saga/outbox if they go deep

</details>

### 6. What should I not do in an HLD round?

<details>
<summary>Show Answer</summary>

**Answer:**

- Jump to class diagrams and design patterns
- Dump tools (Kafka, K8s, DynamoDB) with no problem they solve
- Skip requirements and numbers
- Design every microservice in code-level detail

Stay at: flow → components → data → scale → failure → trade-offs.

</details>

### 7. What should I not do in an LLD round?

<details>
<summary>Show Answer</summary>

**Answer:**

- Draw a global AWS architecture
- Start coding with no entities
- Make one god class
- Ignore concurrency (two cars, one parking spot)

Stay at: use cases → objects → relationships → sequence → edge cases.

</details>

---

## Check yourself

Explain to a friend:

> HLD is the map of the city. LLD is the plan of one building. Interviews for "Design YouTube" want the map. Interviews for "Design a parking lot" want the building plan.

If that is clear, you are done with this note.

Back: [01_What_is_System_Design.md](01_What_is_System_Design.md)  
Next in Fundamentals: [02_Why_System_Design_Matters.md](02_Why_System_Design_Matters.md)
