# 3. How to Learn and Use This Course

## Simple idea

System design is not memorising 50 diagrams.

It is learning a **repeatable way to think**, then practising it on real problems (URL shortener, chat, YouTube, and so on).

```text
Watch  →  Write in your own words  →  Explain out loud  →  Next topic
```

If you only watch videos, the ideas will fade. If you explain them simply, they stay.

---

## How to use these notes with the videos

1. Skim the note (5 minutes)
2. Watch the matching course video
3. Come back and check: *Did I understand this in simple words?*
4. Say the idea out loud, as if teaching a junior developer

Do **not** skip ahead to Uber or YouTube. Those case studies use later building blocks.

---

## A simple study loop (use this for every later section)

```text
1. What problem does this idea solve?
2. How does it work in one sentence?
3. Where have I seen it? (WhatsApp, IRCTC, Zomato, Spring app)
4. What is the cost / downside?
5. What would I say in an interview in 30 seconds?
```

Example for later (not now): *Caching* — "Store frequent data in fast memory so the database is not hit every time. Downside: data can be stale."

---

## Interview mindset (keep this for the whole course)

When someone says "Design X", do **not** start with Kafka, Kubernetes, or 20 boxes.

Do this instead:

```text
1. Clarify  —  Who uses it? How many users? What must it do?
2. Simple design  —  App → API → Database
3. Improve  —  Scale, speed, failure, cost
4. Trade-offs  —  Why this choice, not that one
```

A junior answer dumps technology names.

A strong answer tells a **story**: requirements → simple system → then upgrades.

---

## Words you will hear next (preview only)

You do **not** need to master these today. Just recognise the names. Later folders teach them properly.

| Word | Everyday meaning |
|------|------------------|
| **Scalability** | Can the system grow when more users come? |
| **Availability** | Is the app up when the user opens it? |
| **Reliability** | Does it do the right thing, every time? |
| **Performance / latency** | How quickly does one request finish? |
| **Throughput** | How many requests can we finish per second? |
| **Trade-off** | You gain one thing, you give up another |

Tiny example:

```text
One powerful server          =  simple, but one crash stops everything
Many smaller servers         =  more work to run, but traffic can grow
```

That choice is a **trade-off**. System design is full of these choices. There is no free lunch.

---

## How this course is shaped (so you do not get lost)

```text
Fundamentals (you are here)
        ↓
How machines talk (Networking, Protocols, Web)
        ↓
How we structure apps (Architectural Patterns)
        ↓
How we handle growth, data, speed, failure, security
        ↓
A blueprint (repeatable interview steps)
        ↓
Case studies (TinyURL, Chat, YouTube, Uber, ...)
```

Each folder in [00_COURSE_MAP.md](../00_COURSE_MAP.md) is one of these steps.

---

## Practical tips for you (Java backend learner)

- Map every new idea to something you know: Spring Boot, REST, MySQL, Kafka, Redis.
- Keep a private "one-pager" per topic: definition, diagram, one example, one trade-off.
- After a section, close the notes and redraw the diagram from memory.
- When you are ready for interviews, practise **speaking** for 30–40 minutes, not only reading.

---

## Interview lines

- I start with **requirements**, then a **simple design**, then I **scale it**.
- I always mention a **trade-off** (speed vs cost, simplicity vs flexibility).
- I avoid dumping tools. I explain **why** a piece exists.
- Availability = "is it up?" Reliability = "does it do the right thing?"
- This course is a ladder. I will not jump to case studies before the building blocks.

---

## Interview Q&A — 6 to 8 years

These questions are about how a 6–8 year engineer behaves in the room. Try them out loud, then open the answer.

### S1. You have 35 minutes and the prompt is huge. Where do you spend the time?

<details>
<summary>Show Answer</summary>

**Answer:**

Five minutes on scope and one number. Five on a thin picture of every box. Fifteen on the one path that can lose money or corrupt data. Five on what breaks and what you would cut. Five left for their questions. I say this plan out loud so they can redirect me.

**Corner cases:** They interrupt with "what about search?" I park search as a second store and return to the hard path. They are silent. I still state assumptions instead of waiting. I finish early with a pretty diagram and no failure story. That is a weak senior answer. Leave time for "what if this call is retried?"

</details>

### S2. You do not know the tool they just named. What do you say?

<details>
<summary>Show Answer</summary>

**Answer:**

I say what job the box does, not the brand. "I need a log that many readers can take in order. I have used Kafka for that. If you use something else, the job is the same: keep the message until the worker finishes, and make a second delivery safe." Guessing an API I do not know is worse than naming the job.

**Corner cases:** They want a comparison I have not operated. I compare on the axis I do know (order, retention, consumer groups) and I mark the rest as something I would check. Inventing a feature of a product is a fast way to lose trust.

</details>

### S3. How do you show you have operated systems, not only drawn them?

<details>
<summary>Show Answer</summary>

**Answer:**

I mention one operational fact with the design choice. Connection pools saturate before CPU does. A failover you have not rehearsed will fail on the day. A cache without a stampede lock takes the database down at expiry. Deploys need a readiness check so a process is not sent traffic before it can serve. One true story beats five tool names.

**Corner cases:** Do not tell a long war story. Two sentences, then back to their prompt. Do not blame "the network" with no next step. Say what you would measure and what you would change.

</details>

### S4. They push back on your choice. How do you respond?

<details>
<summary>Show Answer</summary>

**Answer:**

I repeat the requirement their push changes. "If we must survive a whole region dying, my single primary is wrong. I would add a second region and accept that a write in flight can be lost up to the replication lag, or I would block the write until both regions ack." I do not defend the first drawing. I update it.

**Corner cases:** The push is a trick ("just use a blockchain"). I ask which requirement it meets that the simpler store does not. If they insist on a bad idea as a constraint, I design inside it and name the cost. Getting stuck arguing is worse than adapting.

</details>

**Senior recap:** spend the time on one hard path, name jobs not brands, and change the design when the requirement changes.

## Check yourself

Before you leave this folder, you should be able to say:

1. What system design is (vs writing Java code)
2. Why interviews and production both need it
3. The 4-step interview habit: clarify → simple design → improve → trade-offs

You are done with **Fundamentals**.

**Next:** [02_Networking_and_Communication](../02_Networking_and_Communication/00_Section_Overview.md).
