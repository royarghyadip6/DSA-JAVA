# 14. Interview Mindset

## Simple idea

The interviewer is not collecting a perfect diagram. They are watching whether you can **narrow a fuzzy problem, do the arithmetic, and say why you picked one design over another**.

This is the closing lecture of the course. It repeats the [4-step blueprint](../11_System_Design_Blueprint/01_Four_Step_Approach.md) as the way you talk.

---

## What they score

| They look for | What it sounds like |
|---------------|---------------------|
| Clear thinking | You restate the problem before you draw |
| Tradeoffs | You say what got worse when something got better |
| Collaboration | You pause for their constraint instead of racing the whiteboard |
| Structure | Requirements, numbers, picture, tools. Same order every time |

Curiosity beats a memorized architecture. If you are unsure, say the assumption and keep going.

---

## The shape of the answer

1. Functional and non-functional requirements. What you will not build.
2. A few numbers. The first bottleneck.
3. The boxes and the main flow.
4. Tools, and the reason. SQL or NoSQL, strong or eventual consistency, one service or several, cache or precompute.

When they change the question ("now the celebrity has 50 million followers"), go back to the bottleneck and say what moves. Do not start the drawing over in silence.

Break a big product into flows. Zoom into one: the seat update, the inverted index, the WebSocket. You will not finish every box. Ask which flow they care about.

---

## Tradeoffs you should be able to say

| Choice | Say this kind of sentence |
|--------|---------------------------|
| SQL vs NoSQL | "Stock and payments stay in PostgreSQL. The feed is a wide-column store because the write pattern is an append." |
| Strong vs eventual | "The seat is a conditional update. The like count can lag." |
| Monolith vs microservices | "One deployable for the MVP. Split the transcoder because it scales differently from the API." |
| Cache vs precompute | "Fan-out on write makes the read cheap and hurts for a celebrity, so those accounts are merged on read." |

No tool is always right. Tie it to the number you estimated.

---

## How to practice

- Time yourself. About 35–45 minutes, on paper or in Excalidraw.
- Record the explanation and listen for steps you skipped.
- Do one mock with another person. Let them add a constraint halfway.
- Remote: share a diagram. In person: talk while you draw. The words matter as much as the boxes.

Use the same requirements and scale template until it is boring. Then the interesting part is the tradeoff, which is the part they remember.

The course path was protocols, APIs, and shapes of systems, then data, queues, delivery, and these case studies. Next is repetition: one case a week, a design you can defend, and reading other write-ups (the slides mention InfoQ and ByteByteGo) so your menu of patterns stays current.

A side project that has a real bottleneck teaches more than another diagram of the same feed.

---

## Interview lines

- Ask, assume out loud, then design.
- Four steps, even when the prompt is one sentence.
- When the prompt changes, say which box breaks.
- A tradeoff sentence beats a tool list.

---

## Interview Q&A

### 1. How do you start when the prompt is vague?

<details>
<summary>Show Answer</summary>

**Answer:**

Ask who the user is, which action must work, and what "good" means (latency, correctness, scale). State a number if they do not give one. List a non-goal so you do not design the whole company. Then estimate and draw the one path that is hardest. Invite them to redirect you.

</details>

### 2. What if they change the requirements halfway?

<details>
<summary>Show Answer</summary>

**Answer:**

Stop and name the impact. "If this account has 50 million followers, fan-out on write is no longer viable. I keep the post write, and I build that user's contribution at read time." Change the bottleneck, the data flow, and the tool only if the tool was the reason it broke. Say what you are now giving up.

</details>

### 3. How do you show depth without boiling the ocean?

<details>
<summary>Show Answer</summary>

**Answer:**

Sketch every box in one minute, then go deep on the risky one: the conditional seat update, the inverted index, the socket registry, the chunked upload. Mention failure, idempotency, and the consistency choice there. Leave the rest as "standard gateway, auth, and logs" unless they ask.

</details>

**Q&A recap:** same four steps, assumptions out loud, one deep path, a tradeoff in a sentence.

## Interview Q&A — 6 to 8 years

### S1. The interviewer is senior and bored by your happy path. What do you add?

<details>
<summary>Show Answer</summary>

**Answer:**

The corner they have been burned by: duplicate request, hot key, partial failure, and a stale read after a write. I add one of those to the core path as soon as the boxes exist. I use numbers I can recompute. I say what I am not building. I ask which part they want deeper. A bored interviewer usually means I am still listing components.

**Corner cases:** I jump to the corner before I have a requirement, and I look unfocused. I tell a ten-minute production story that is not their prompt. I name-drop a failure mode I cannot finish ("we'd use CRDTs") and I cannot explain the user-visible result. I hide a disagreement with them to stay pleasant. I would rather update the design.

</details>

### S2. How do you handle "what if a whole region dies?" when you still have twenty minutes?

<details>
<summary>Show Answer</summary>

**Answer:**

I say whether this product needs to keep writing. If yes, I name the write owner (one region at a time, or a partition owner) and the RPO (replication lag). If no, I say we serve cached reads and we restore from the spare region with a practiced promote. I do not redraw every service. I change the data plane and the DNS or anycast cut, and I fence the old writer.

**Corner cases:** I answer "active-active" and now every key can be written in two places with no merge rule. I forget that object storage, the queue, and the database fail over on different clocks. I put the runbook only in the region that just died. I promise zero data loss on async replication. I state the lag instead.

</details>

### S3. They ask for the schema and the API in the same round. How do you split the time?

<details>
<summary>Show Answer</summary>

**Answer:**

One table or one document for the invariant, with the conditional write written out. One API for the risky action, with the idempotency key and the status codes (201, 409, 202). I skip a full catalog of CRUD. I mention the index that the hot read uses. If they wanted low-level classes, I switch and talk entities and the lock, and I say I am switching.

**Corner cases:** A schema with twenty tables and no transaction boundary. An API that returns 200 for a queued charge. A GET that mutates. A list API with no page cap. I add those as the senior details instead of more endpoints. I forget multi-tenant: every row needs the tenant, and the check is in the query, not only in the gateway.

</details>

### S4. What is a strong close in the last three minutes?

<details>
<summary>Show Answer</summary>

**Answer:**

I restate the requirement, the bottleneck, the tradeoff, and the next limit. "Checkout is a conditional stock update plus an idempotency key. Search is stale on purpose. This holds to the write rate of one primary. Past that I shard by product id and I refuse a cross-shard transaction on the hot path." Then I stop talking. I let them pull a thread.

**Corner cases:** A close that adds a new system I will not have time to defend. An apology tour ("I know this is not perfect"). A monologue of tools I did not connect to a requirement. No mention of how I would notice production is on fire (one metric: checkout error rate and p99). I keep the close to four sentences.

</details>

**Senior recap:** add one real failure early, answer region loss with a write owner and an RPO, show one schema and one API, and close with the limit of the design.

That is the end of the course notes. Pick a case in this folder and practice it with a timer.
