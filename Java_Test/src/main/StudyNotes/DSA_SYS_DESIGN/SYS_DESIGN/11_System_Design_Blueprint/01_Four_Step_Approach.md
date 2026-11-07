# 1. The 4-Step System Design Approach

## Simple idea

System design is building something that stays correct as it grows, without spending infinite money or adding machinery you cannot operate.

You balance **performance, cost, and complexity**. A clear picture of the boxes beats a list of product names.

In an interview, walk these four steps out loud. Do not jump to "I'll use Kafka".

```text
Step 1  Requirements and scope
Step 2  Scale and bottlenecks
Step 3  High-level design (services, APIs, how they talk)
Step 4  Technology and infrastructure, with the tradeoff named
```

---

## Step 1 — Problem and scope

Ask until the problem is small enough to design. Repeat it back.

**Functional requirements** are what a user can do. For the URL example on the slides:

- create a short link
- open the short link and land on the original URL
- (maybe) see click counts

**Non-functional requirements** are how well it must do that. Tie each one to a folder you already studied:

| Requirement | Question | Where you already learned it |
|-------------|----------|------------------------------|
| Performance | How fast is one redirect? | [Performance](../08_Performance/01_System_Performance.md) |
| Scalability | What happens at 10x traffic? | [Scalability](../06_Scalability/00_Section_Overview.md) |
| Reliability | What is still up when a node dies? | [Reliability](../09_Reliability_Availability_DR/00_Section_Overview.md) |
| Security | Who can create links, and can we abuse the redirect? | [Security](../10_Security/00_Section_Overview.md) |

**Constraints** are the walls: interview time, money, a law about where data lives, a team that can run only one database, "read-heavy", "must not lose a payment".

Also say what you will **not** build. A URL shortener does not need video upload. Cutting scope is part of the design.

If a number is missing, assume one and say it. "I'll assume 100 million new links a month. Stop me if that is wrong."

---

## Step 2 — Scale and bottlenecks

Estimate three things, roughly. The interviewer wants the method, not a perfect census.

```text
Traffic    how many requests per second, average and peak
Storage    how many bytes you keep, and for how long
Bandwidth  requests per second × size of one response
```

**Traffic**

```text
writes per second ≈ new items per day / 86,400
reads per second  ≈ writes × (reads per write)
peak              ≈ average × 2 to 5, unless you know the spike
```

There are about **86,400 seconds in a day** (24 × 60 × 60). Dividing a daily count by 100,000 is close enough in your head (a bit low).

**Worked example.** The slides use a URL shortener. These numbers are an assumption, not a figure from the PDF.

```text
100 million new URLs per month
≈ 100,000,000 / 30 / 86,400 ≈ 40 new URLs per second
If each short link is opened about 100 times:
≈ 4,000 redirects per second on average
Peak × 3 ≈ 120 writes/s and 12,000 reads/s
```

So this product is **read-heavy**. The redirect path is the bottleneck, not "create link".

**Storage**

```text
rows × bytes per row × years
100 million/month × 12 × 5 years = 6 billion rows
If one row is about 500 bytes → about 3 TB of raw rows
Replicas and indexes multiply that. Say the number, then say "× 3 copies".
```

**Bandwidth.** 12,000 redirects per second, and a redirect is a small response (about 1 KB), is on the order of 12 MB/s. Fine for app servers. A video product would not be.

**Bottlenecks** are the parts that fall over first as those numbers grow:

| Symptom | Usual bottleneck |
|---------|------------------|
| Every read hits one database | The primary database |
| One hot key (a celebrity URL) | That cache key or that shard |
| Huge files | Bandwidth and disk, not the SQL query |
| Slow work on the user request | CPU, or a downstream call you should queue |

Capacity planning is just those three estimates turned into "how many app servers, how much disk, how much cache". You refine it in step 4. You do not buy servers in step 1.

---

## Step 3 — High-level design

Draw boxes. One box is one job.

For the same URL example the slides use:

```text
Client
  →  API: create short link, and redirect
  →  store: code  →  original URL
  →  (optional) analytics, fed by a queue so the redirect stays fast
```

**APIs** are the public doors. Name the method, the path, and the one job:

| Call | Job |
|------|-----|
| `POST /shorten` | Take a long URL. Return a short code |
| `GET /{code}` | Look up the code. Redirect. This must be fast |
| `GET /{code}/stats` | Click counts. Can be slower and slightly old |

**How boxes talk**

| Style | When |
|-------|------|
| Synchronous REST | The user is waiting for the answer (the redirect) |
| Queue or events | The user is not waiting (click analytics, email) |
| WebSocket | The server must push (chat). Not this product |

A redirect is synchronous. Recording the click can be asynchronous. That split is the design. The protocols are in [section 3](../03_Protocols/00_Section_Overview.md) and [queues](../08_Performance/03_Messaging_and_Queues.md).

Stay at boxes and arrows until the picture is stable. Then choose products.

---

## Step 4 — Technology, on purpose

Pick a tool because of a requirement from step 1 and a number from step 2.

| Decision | For the URL example | Tradeoff |
|----------|---------------------|----------|
| Store | A key-value lookup (`code → url`) fits Redis or DynamoDB. SQL also works at this size | Key-value is a fast lookup. SQL is easier if you need ad-hoc reports in the same database |
| Cache | The hot codes sit in memory | A cache can be stale for a second. A redirect of an unchanged URL can tolerate that |
| Scale | Stateless API behind a load balancer. Autoscaling | You operate more copies. You do not scale by one giant server |
| Availability | Replicas, health checks, more than one zone | Extra cost. Failover is not the same as a backup |
| Analytics | A queue, then a batch or stream job | Counts are a few seconds late. The redirect path stays thin |

Say the tradeoff in one sentence: "I will cache the hot URLs so the database does not see 12,000 reads a second. A brand-new link might miss the cache once. That is acceptable."

Other products (chat, payments, video) will pick differently. Payments want SQL and a small RPO. Video wants object storage. The **process** stays. The **tools** change.

---

## How to spend the interview

```text
First      clarify requirements and non-goals     (step 1)
Then       one or two estimates and the bottleneck (step 2)
Most time  draw the diagram and the APIs          (step 3)
Then       name tools only where they matter      (step 4)
Last       "if this grows 10×, the next change is…"
```

Leave a minute to say what you did not design. That shows scope control.

---

## Mistakes

- Starting with the database logo.
- Designing every feature. Pick the core path.
- Silent guesses. Say the assumption.
- One database for redirects and heavy analytics.
- No tradeoff. Every choice costs something.
- Ignoring security and failure because nobody asked yet. Mention them briefly: auth on create, TLS, a replica, a backup.

---

## Interview lines

- Requirements, then numbers, then boxes, then tools.
- Functional is what it does. Non-functional is how well. Constraints are the walls.
- Estimate QPS, storage, and bandwidth. Point at the first bottleneck.
- The user path stays synchronous and thin. Everything else can be a queue.
- Name the tradeoff: cost, complexity, freshness, or consistency.

---

## Interview Q&A

The slides do not include a question sheet. These are the questions the four steps are for.

### 1. How do you approach a system design question?

<details>
<summary>Show Answer</summary>

**Answer:**

1. Clarify functional requirements, non-functional requirements, and constraints. Say what is out of scope.
2. Estimate traffic, storage, and bandwidth. Name the first bottleneck.
3. Draw the services, the APIs, and whether each hop is synchronous or a queue.
4. Choose storage, cache, and scaling only after that, and state the tradeoff.

I do not name Kafka or a database until step 4.

</details>

### 2. Functional vs non-functional vs constraints?

<details>
<summary>Show Answer</summary>

**Answer:**

Functional: the feature. Example: create a short URL and redirect. Non-functional: the quality. Example: a redirect at the speed users expect, still up when a node dies, safe against abuse. Constraints: time, budget, a law, or a limit such as "read-heavy" or "one region". Non-goals belong here too.

</details>

### 3. How do you estimate scale?

<details>
<summary>Show Answer</summary>

**Answer:**

State assumptions. Writes per second are new items per day divided by about 86,400. Multiply by reads per write. Peak is a small multiple of average, often 2 to 5, unless the product is spiky. Storage is rows times bytes times years, then times copies. Bandwidth is requests per second times response size. For a URL shortener at 100 million new links a month and 100 reads per write, expect on the order of 40 writes and 4,000 reads per second, and a few terabytes over several years. The redirect path is the bottleneck.

</details>

### 4. How do you find the bottleneck?

<details>
<summary>Show Answer</summary>

**Answer:**

Follow the busiest request. If every read hits one primary database, that database is the bottleneck. If one key is huge (one famous URL), that key is the bottleneck. If the payload is video, bandwidth and object storage dominate, not the row lookup. If the request does slow work the user does not need, that work should leave the request path. Fix the biggest wait first.

</details>

### 5. What do you put in the high-level design?

<details>
<summary>Show Answer</summary>

**Answer:**

The core services, the public APIs, and how the services talk. For a shortener: `POST /shorten`, `GET /{code}` for the redirect, and an optional stats API. The redirect is a synchronous lookup. Click analytics go on a queue so they do not slow the redirect. Draw that before you pick Redis or PostgreSQL.

</details>

### 6. How do you choose the technology?

<details>
<summary>Show Answer</summary>

**Answer:**

Match the tool to the access pattern and the number. A shortener is a key lookup on the hot path, so a key-value store or a cache in front of SQL fits. A ledger would be SQL instead. Add a load balancer and more than one instance because the API is stateless. Cache hot keys to cut database reads, and accept a rare stale miss. Say what you gave up: freshness, money, or operational complexity.

</details>

### 7. What if you do not know a number?

<details>
<summary>Show Answer</summary>

**Answer:**

Pick a reasonable assumption, say it, and keep going. "I will assume 100 million new URLs a month and a 100:1 read ratio." If the interviewer corrects you, redo the estimate. The method matters more than the guess. Do not block the design on a perfect figure.

</details>

### 8. What do you skip on purpose?

<details>
<summary>Show Answer</summary>

**Answer:**

Anything that is not on the core path, once you have named it as a non-goal. Do not design a custom hash-ring, a multi-region active-active database, and a machine-learning ranker in one interview. Mention the next step if scale grows: "at 10× I would shard the key space." Depth on the bottleneck beats breadth on every box.

</details>

**Q&A recap:** clarify, estimate, draw, then choose. Say the assumption and the tradeoff.

## Interview Q&A — 6 to 8 years

### S1. Your estimate is off by 10× halfway through. What do you redo, and what do you keep?

<details>
<summary>Show Answer</summary>

**Answer:**

I redo the bottleneck, not the whole speech. I say which assumption changed ("reads are 100,000 per second, not 10,000"). I check whether the single primary, the one cache, or the one partition still fits. If it does not, I add the next step only: replicas, a shard key, or a queue. I keep the product rules (a seat is sold once). A wrong number is fine. A design that cannot move when the number moves is not.

**Corner cases:** I "round" a 10× miss into the same drawing because I am attached to it. I add five new systems at once and cannot explain any of them. The 10× is on a hot key, so horizontal scale does not help, and I must say that. The interviewer's number includes a fan-out (one write becomes a million feed inserts). I estimate the fan-out, not only the user click. Peak versus average: I design for the peak I stated, and I say the average is what I use for the monthly bill.

</details>

### S2. How do you draw a failure on the diagram without a second hour?

<details>
<summary>Show Answer</summary>

**Answer:**

I pick the box the product cannot lose and I add one sentence: timeout, retry with a key, and what the user sees. Example: "payment timeout returns unknown, the order stays PENDING, the same idempotency key resumes." I mention one spare for that box or I explicitly accept a single writer with backups. I do not draw a second full architecture called "the DR diagram."

**Corner cases:** A retry arrow with no dedupe. A queue arrow with nobody owning the poison message. A cache with no stampede note on a hot key. Multi-region boxes I drew and then gave two writers by accident. A load balancer in front of one database, presented as high availability. I say the remaining single point out loud so they can push on it if they care.

</details>

### S3. They ask "which database?" in minute 10. How do you avoid a tool argument?

<details>
<summary>Show Answer</summary>

**Answer:**

I tie the tool to the access pattern I already wrote down. "Point lookup of a short code, mostly reads: a key-value cache in front of a durable primary." If they want a brand, I name one I can operate and the reason. If they ban it, I keep the access pattern and swap the brand. I do not open with five logos.

**Corner cases:** I name a brand whose feature I have wrong (for example claiming cross-row transactions a store does not have). I mark that as something I would check rather than inventing it. A tool that solves scale and breaks the invariant I opened with. I drop the tool. Picking a queue because the prompt said "large scale" when the user is waiting for the answer synchronously. I split sync and async.

</details>

### S4. What do you leave on the table so a senior interviewer trusts you?

<details>
<summary>Show Answer</summary>

**Answer:**

A stated non-goal, a stated 10× limit, and a stated consistency choice. "Search can be stale by a minute. Checkout cannot." I also leave an operational hook: how I would know this is failing (one metric) and how a retry is safe. Depth on that hook beats a complete zoo of services. If I truly do not know, I say what I would measure instead of inventing a number.

**Corner cases:** I skip auth entirely on a multi-tenant design. I at least say the tenant id is on every row and checked. I design the perfect path and never the duplicate delivery. I spend the last five minutes on a CDN for a problem that is a write conflict. I let them drive me into a corner and I argue instead of updating the picture. The trusted move is to change the picture and say what got worse.

</details>

**Senior recap:** change the bottleneck when the number changes, draw one failure with an idempotent retry, pick tools from the access pattern, and leave a non-goal plus a metric.

Next course section: **Case studies** — [12_Case_Studies](../12_Case_Studies/00_Section_Overview.md)
