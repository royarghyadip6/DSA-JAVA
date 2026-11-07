# 1. What Is Scalability?

## Simple idea

**Scalability** is the ability to take **more work** and still feel fast and reliable.

More work can mean:

- more users
- more data
- a short spike (ticket sale, Black Friday)

```text
100 users    →  site feels fine
1,00,000 users →  still fine, or the site falls over
```

A scalable system grows with demand. Throughput and availability stay acceptable. It does not mean "infinitely free" or "never slow."

---

## Why systems must grow

| Reason | Example |
|--------|---------|
| More users | Launch in a new country |
| More data | IoT sensors, analytics, years of orders |
| Peaks | Festival sale, exam-result day, match tickets |
| Promises | A latency or uptime SLA |

If you do not plan for this, the app gets slow, then errors, then downtime.

---

## Two directions (preview)

- **Vertical** — bigger CPU and RAM on **one** machine
- **Horizontal** — **more** machines sharing the work

The next file goes deep, including **diagonal** scaling (vertical first, then horizontal).

---

## What gets harder as you grow

### Latency

Latency is the wait from request to response.

It grows because:

- more network hops (gateway, service, service, database)
- slower queries on a bigger database
- synchronous calls that wait on every hop

Microservices make this worse if every click fans out to ten services.

**Reduce it**

- cache hot data (Redis)
- do non-urgent work in a queue
- CDN / edge for static files
- fewer chatty calls
- faster queries and indexes

### Bottlenecks

One slow piece slows everything behind it.

```text
App servers are fine
Database locks or one single-threaded job is not
        ↓
The whole product feels slow
```

Examples: DB locks, a memory cap, one thread doing all the work, a shared session on one box.

Bottlenecks hide at low traffic and show up only when load grows.

### Downtime

More machines means more things that can fail. Deploys and scale events can also blip users. High availability has to be designed (health checks, extra copies, careful rollouts). It does not appear just because you added servers.

### Cost

CPU, RAM, and bandwidth cost money. Autoscaling with no ceiling can blow the budget. Buying huge servers "just in case" wastes money on idle capacity.

---

## How you find a bottleneck

1. Watch metrics: CPU, memory, latency, errors (Prometheus, Grafana, Datadog, CloudWatch)
2. Trace one request across services (Jaeger, OpenTelemetry)
3. Load-test before the real spike (JMeter, k6)
4. Look for queues that grow, or one tier whose time grows much faster than the others

The slowest stage on the path is the one to fix first. Adding app servers does nothing if the database is the wall.

---

## Cost vs scale in the cloud

- autoscaling with a **minimum and a maximum**
- serverless (Lambda) for spiky, event-shaped work
- cache so the database is not hit every time
- spot or reserved instances where they fit
- look at real usage and shrink what you do not need

Scale when needed. Do not pay for peak capacity every night.

---

## Real stories from the course

**Twitter.** Early on, a monolith could not absorb traffic spikes. Users saw the "fail whale." Moving to a distributed design let them scale out.

**Zoom.** During COVID, daily participants jumped from about 10 million to over 300 million. Cloud capacity and autoscaling absorbed a sudden spike that a fixed server room could not.

---

## Interview lines

- Scalability = more load without losing speed or uptime.
- The enemies are latency, one bottleneck, more failure points, and cost.
- Find the bottleneck with metrics, traces, and a load test.
- Cache, async work, and fewer hops fight latency.
- Put a ceiling on cloud scale so the bill cannot run away.

---

## Interview Q&A

### 1. What does scalability mean in system design?

<details>
<summary>Show Answer</summary>

**Answer:**

It is the ability to handle more users, data, or traffic and still stay responsive and available. You grow by making a machine bigger (vertical) or by adding machines (horizontal). The system should keep throughput and uptime as demand rises.

</details>

### 2. Give a real example where scale mattered.

<details>
<summary>Show Answer</summary>

**Answer:**

Twitter's early monolith failed under spikes (the fail whale) until they moved to a distributed architecture that could scale out.

Zoom went from about 10 million to over 300 million daily participants in the COVID surge. Cloud infrastructure and autoscaling absorbed a jump no fixed fleet was sized for.

</details>

### 3. What are the main challenges as a system scales?

<details>
<summary>Show Answer</summary>

**Answer:**

- **Latency** — more hops and heavier data paths
- **Bottlenecks** — one slow DB, lock, or thread stalls everyone
- **Downtime** — more nodes and deploys mean more ways to fail
- **Cost** — cloud capacity is not free, and uncapped autoscale spends fast

</details>

### 4. How would you identify a bottleneck?

<details>
<summary>Show Answer</summary>

**Answer:**

Watch CPU, memory, and latency. Trace a request end to end. Load-test and see which queue or service slows first. Fix that stage before adding more of everything else.

</details>

### 5. Why does latency increase with scale, and how do you reduce it?

<details>
<summary>Show Answer</summary>

**Answer:**

More services, shards, and dependencies add network and merge time.

Mitigate with caching, queues for work that can wait, CDNs, fewer cross-service calls, and better queries.

</details>

### 6. How do you balance scalability with cost?

<details>
<summary>Show Answer</summary>

**Answer:**

Autoscale between a floor and a ceiling. Use serverless for uneven work, cache to protect the database, and spot or reserved capacity where it fits. Re-check real usage and shrink idle resources.

</details>

**Q&A recap:** growth without collapse; watch latency, bottlenecks, failures, and the bill.

## Interview Q&A — 6 to 8 years

### S1. Traffic doubled and the site got slower, not twice as slow. Why do systems fall off a cliff?

<details>
<summary>Show Answer</summary>

**Answer:**

Queues. Once a pool, a lock, or a CPU is full, new requests wait behind old ones and latency goes vertical. Timeouts then retry and add more load. I look for the saturated resource (pool wait, lock time, run queue), not the average CPU of the whole fleet. I shed load at the edge so the saturated box can drain.

**Corner cases:** A cache hit rate that drops at peak (working set no longer fits) turns a comfortable database into a down one. GC or a global lock looks fine at 30% CPU and dies at 60%. A batch job scheduled at the same hour as the peak. More app servers make a hot row worse, not better. p50 stays pretty while p99 is the outage users feel.

</details>

### S2. How do you define "it scales" so an interviewer can test your design?

<details>
<summary>Show Answer</summary>

**Answer:**

I state the number and the limit. "At 10× reads I add replicas and cache. At 10× writes on one key this design does not scale. I would split that key or queue the writes." I name a metric that should stay flat (p99 under 200 ms, error rate under 0.1%) and what I expect to grow (instance count, cost). A design that needs a human to keep the number true does not scale.

**Corner cases:** A design that scales data size but not request rate, or the reverse. A stateful protocol that scales only until reconnects. A per-tenant limit so one customer cannot spend the whole capacity. Headroom: I run out of a cloud quota (IPs, load balancer rules) before I run out of CPU. I mention the quota.

</details>

### S3. What do you refuse to scale out?

<details>
<summary>Show Answer</summary>

**Answer:**

A single-writer invariant I need to be correct: one ledger, one seat row, one auction price. I scale the reads around it and I keep the write path small and conditional. I also refuse to scale by copying the same bug to more regions (two writers, no conflict rule). And I do not scale a service that is slow because of a missing index. More CPUs will not fix a full table scan forever.

**Corner cases:** An auto-increment id or a single queue partition becomes the new ceiling. A leader election that flaps under load, so I have zero writers or two. A "just shard" answer with no shard key. If the key is `country=US` for 90% of users, I did not scale.

</details>

### S4. How do you talk about the bill while you talk about scale?

<details>
<summary>Show Answer</summary>

**Answer:**

Every extra nine, replica, and region has a price. I tie cost to the requirement. "This page can be 30 seconds stale, so I cache it instead of paying for a second primary." I watch cost per request, not only the monthly total. A retry storm is a bill event. So is a log line per packet and a trace of every health check.

**Corner cases:** Reserved capacity that sits idle after the sale. Cross-zone traffic I did not know was billed. A CDN that misses because every URL is unique, so I pay CDN prices for an origin proxy. Scaling to zero on a service with a cold start that violates the latency SLO. I would rather a small warm pool.

</details>

**Senior recap:** cliffs come from saturation and retries, say the 10× limit, do not scale out a single-writer rule, and cost is a requirement.

Next: [02_Scaling_Strategies.md](02_Scaling_Strategies.md)
