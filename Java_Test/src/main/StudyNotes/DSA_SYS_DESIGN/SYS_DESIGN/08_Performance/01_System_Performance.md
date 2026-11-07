# 1. System Performance

## Simple idea

**Performance** is how well the system does its job **under load**.

It is not one number. A page can be fast for one user and fall over when a sale starts.

| Dimension | Question | Unit |
|-----------|----------|------|
| **Speed** | How long until this user gets an answer? | milliseconds |
| **Capacity** | How much work can we finish? | requests per second |
| **Efficiency** | How much CPU, memory, and money did that take? | usage and cost |

Performance is a feature. Slow pages raise bounce rate, cut revenue, and become outages when load grows.

---

## Latency vs throughput

| | Latency | Throughput |
|--|---------|------------|
| Meaning | Time for **one** request | How **many** requests finish per second |
| Feels like | "The page is snappy" | "The site survives the crowd" |
| Unit | ms, seconds | RPS, TPS |

Highway picture: latency is how long one car takes. Throughput is how many cars pass in an hour.

They are not the same knob.

- A queue makes **throughput** high (many cars) and **latency** worse (you wait in line).
- A tiny service can have **low latency** and still have **low throughput** (one clerk, no line, but only one customer at a time).

Pick the one the product needs. A checkout wants low latency. A nightly report wants high throughput and can be slow for one row.

---

## Scalability vs responsiveness

| | Scalability | Responsiveness |
|--|-------------|----------------|
| Meaning | More load without getting much worse | Each user still gets a quick answer |
| Tied to | Horizontal or vertical scale ([section 6](../06_Scalability/00_Section_Overview.md)) | Latency |

A design that scales by making every request wait 10 seconds is the wrong win. Keep the user path short, and move slow work off it.

---

## SLA, SLO, and SLI

| Name | Who it is for | Example |
|------|----------------|---------|
| **SLI** | The measurement | 93% of requests finished under 300 ms |
| **SLO** | Your internal target | 95% of requests under 300 ms |
| **SLA** | The promise to the customer, often with a penalty | 99.9% uptime |

```text
SLI  =  what the dashboard says right now
SLO  =  the target your team chases
SLA  =  the contract
```

Use a **tighter SLO than the SLA**. If the contract is 99.9% uptime, an internal target of 99.95% gives you time to react before you break the contract.

Uptime and latency are different promises. Do not mix them in one sentence. "99.9% uptime" does not mean "fast".

---

## Percentiles, not only the average

The average hides the bad requests.

```text
100 requests
  95 finish in 80 ms
   5 finish in 8 seconds
average ≈ 476 ms     looks acceptable
P95     = 80 ms      most users are fine
P99     = many seconds   some users are stuck
```

| Percentile | Meaning |
|------------|---------|
| **P50** | Half of requests are faster than this (the median) |
| **P95** | 95% are this fast or faster. The slow 5% are above it |
| **P99** | Tail latency. The slowest 1% |

Users remember the slow click. Track P95 and P99 on the paths that matter (login, search, pay).

---

## Testing vs monitoring

**Testing** is a planned push. **Monitoring** runs all the time in production.

| Test | What you do |
|------|-------------|
| **Load** | Expected traffic. Does it meet the SLO? |
| **Stress** | Past the limit. Where does it break, and how? |
| **Spike** | A sudden jump (a TV ad, a flash sale) |
| **Endurance** (soak) | Hours or days. Finds leaks and slow disk fill-up |

Tools from the interview notes: **JMeter**, **k6**, **Locust**.

**Monitoring**

| Kind | Examples | What you see |
|------|----------|----------------|
| APM | New Relic, Datadog | One request across services (a trace) |
| Metrics | Prometheus + Grafana, CloudWatch | Latency, RPS, CPU, errors |
| Logs | ELK (Elasticsearch, Logstash, Kibana) | What the code logged |
| Synthetic | A robot hits the URL | "Is it up from outside?" |
| RUM | Real user monitoring | What real browsers felt |

Alert when an SLO breaks, error rate jumps, or CPU and memory spike. Do not alert on every small wiggle.

---

## Finding a bottleneck

Walk the request. The slow part is usually one of these:

```text
Browser → CDN → load balancer → app → cache → database → another service
```

1. Look at traces (which span is slow).
2. Check cache hit ratio.
3. Check CPU, memory, disk, and network.
4. Run a load test so the limit shows up on purpose.
5. Read logs around the slow calls.

Fix the biggest wait first. Making a 2 ms function faster does nothing if the database call takes 800 ms.

---

## Staying responsive while you scale

- Cache what is read often (next file).
- Do email, resize, and reports **off** the user request (a queue).
- Keep app servers **stateless** so you can add copies.
- Limit traffic and shed load when you are drowning, so a spike does not kill everyone.
- Autoscale on a real signal (CPU, queue depth, RPS), and watch tail latency, not only the average.
- For a spike: CDN for static files, a queue to smooth bursts, a circuit breaker so one sick dependency does not cascade. Circuit breakers were introduced with microservices in [section 4](../04_Architectural_Patterns/03_Microservices_Architecture.md).

---

## Performance vs cost

Faster often means a bigger machine, more IOPS, or more replicas. That is a bill.

| Choice | Speed | Cost note |
|--------|-------|-----------|
| Bigger instance, provisioned IOPS | More headroom | You pay even when idle |
| Reserved capacity | Steady price | Waste if traffic is lower than you planned |
| On-demand / autoscale | Matches the spike | Cold start or scale-up delay |
| Serverless | Fine at low traffic | Cold start adds latency |
| Cache and batching | Fewer repeated calls | Often cheaper than more servers |

Spend to meet the SLO. Do not buy P99 of 10 ms if the product is happy at 300 ms.

---

## Interview lines

- Latency is one request. Throughput is requests per second. A queue trades latency for throughput.
- SLA is the contract. SLO is your target. SLI is the live number. Make the SLO stricter than the SLA.
- Averages hide pain. Watch P95 and P99.
- Test with load, stress, spike, and soak. Monitor with traces, metrics, and logs.
- On a spike: autoscale, CDN, queue, stateless apps, circuit breaker.

---

## Interview Q&A

### 1. Latency vs throughput?

<details>
<summary>Show Answer</summary>

**Answer:**

Latency is how long one request takes. Throughput is how many requests finish per second.

A highway: latency is one car's travel time. Throughput is cars per hour. Making one better can make the other worse. A queue raises how many jobs you accept and makes each job wait.

</details>

### 2. SLA, SLO, and SLI?

<details>
<summary>Show Answer</summary>

**Answer:**

- **SLA** — customer contract, for example 99.9% monthly uptime
- **SLO** — internal goal, often stricter, for example 99.95% uptime, or 95% of API calls under 200 ms
- **SLI** — the measured value, for example 93% of calls under 300 ms this hour

Uptime and latency are separate SLIs. The slide example is: SLA 99.9% uptime, SLO 95% of requests under 300 ms, SLI whatever you measured today.

</details>

### 3. Why P95 and P99?

<details>
<summary>Show Answer</summary>

**Answer:**

The average can look fine while a slice of users wait for seconds. P50 is the median. P95 means 95% of requests are at least this fast. P99 is the tail, the slow 1% that people feel. Tune the tail on important paths.

</details>

### 4. How do you find a bottleneck?

<details>
<summary>Show Answer</summary>

**Answer:**

Trace the request (Datadog, New Relic, or Grafana). Split time across the database, downstream calls, and cache misses. Check CPU, memory, disk, and network. Load-test to the limit. Read logs for the slow operation. Fix the largest wait first.

</details>

### 5. How do you stay responsive at scale?

<details>
<summary>Show Answer</summary>

**Answer:**

Keep the user path short. Cache reads. Put non-critical work on a queue. Rate-limit and shed load when saturated. Run stateless services so you can add instances. Autoscale, and alert on tail latency.

</details>

### 6. Tools for testing and monitoring?

<details>
<summary>Show Answer</summary>

**Answer:**

Testing: JMeter, k6, Locust (load, stress, spike, soak).

Monitoring: Prometheus and Grafana, Datadog, New Relic, CloudWatch. APM for traces. ELK for logs. Synthetic checks for uptime. RUM for real browsers. Alert on SLO misses, error rate, and resource spikes.

</details>

### 7. How do you handle a sudden spike?

<details>
<summary>Show Answer</summary>

**Answer:**

Autoscale (an AWS Auto Scaling group, or Kubernetes HPA). Serve static files from a CDN. Buffer work in Kafka or SQS. Use a circuit breaker so one failure does not fan out. Keep services stateless so new copies start quickly. Expect a delay while new instances boot.

</details>

### 8. Performance vs cost in the cloud?

<details>
<summary>Show Answer</summary>

**Answer:**

More speed usually means more instances, IOPS, or replicas. Reserved capacity is cheaper for steady load and wasteful if you overbuy. Autoscale and serverless track demand, with cold-start latency. Caching and batching cut work without a matching rise in servers. Match spend to the SLO, not to the fastest possible number.

</details>

**Q&A recap:** measure latency and the tail, promise an SLO inside the SLA, and spend money on the bottleneck.

## Interview Q&A — 6 to 8 years

### S1. Average latency is 80 ms and users say the app is unusable. What are you not measuring?

<details>
<summary>Show Answer</summary>

**Answer:**

The tail. A few slow requests (lock wait, cold cache, a bad tenant) dominate complaints while the average stays pretty. I look at p95 and p99, and I split them by route and by a coarse tenant tier. I also measure error rate and timeouts, because a fast failure is not a fast success. The SLO is a target inside the contract (SLA). I do not promise the average to a customer.

**Corner cases:** A p99 that looks fine because clients gave up at 1 second and those hangs are counted as cancellations, not latency. Sampling that drops the rare slow traces, which is exactly the tail. One endpoint's p99 mixed with health checks that take 1 ms, so the dashboard lies. A regional split: one country is slow and the global p99 hides it. Clock skew on the client makes client-side latency nonsense. I measure on the server span and, separately, real user timing.

</details>

### S2. How do you find the bottleneck without "optimizing everything"?

<details>
<summary>Show Answer</summary>

**Answer:**

One request's trace: edge, app, database, third party. I take the widest span. If it is a query, I look at rows read versus rows returned. If it is a pool, I look at wait time, not CPU. If it is a third party, I add a timeout and a fallback. I change one thing and I check the same percentile again. Two changes at once teach me nothing.

**Corner cases:** A flame graph of a CPU that is not the bottleneck, while threads sit idle on I/O. A database that is fast on the warm test and slow when the cache is cold after a deploy. A lock convoy that disappears when I attach a debugger. Load tests that hit one hot key or, the opposite, a perfectly uniform key the production mix does not have. N+1 calls that are each "fast." The sum is the bottleneck.

</details>

### S3. The SLO is 99.9% of checkouts under 300 ms. What do you do when you are burning it?

<details>
<summary>Show Answer</summary>

**Answer:**

I stop spending the error budget on risky deploys. I shed optional work (recommendations) before I shed checkout. I find the change that correlated with the burn. I page on burn rate (how fast the budget is disappearing), not on a single slow request. After the incident I either fix the path or I change the SLO if the promise was fantasy.

**Corner cases:** A composite SLO that lets checkout be broken while a cheap endpoint keeps the number green. A retry that makes latency worse and also creates duplicate orders while I "protect" the SLO. Multi-region: one region inside SLO, the user's region not. An SLO on the server that ignores a 2-second client render. I include the user-visible path if that is what we sold. Alert fatigue from paging p99 without a burn window.

</details>

### S4. When is a performance optimization the wrong senior move?

<details>
<summary>Show Answer</summary>

**Answer:**

When correctness or operability pays more. Shaving 5 ms with a cache that can serve another user's cart is a bad trade. A custom thread pool I cannot explain at 3 a.m. is a bad trade. I optimize the bottleneck that threatens the SLO or the bill, and I leave the rest readable. I also refuse to cache or denormalize data whose staleness breaks a rule (a balance, a revocation).

**Corner cases:** Micro-optimizing JSON while a missing index scans a billion rows. Adding capacity that hides an exponential retry bug until the next step-change. A CDN in front of a route that must be fresh, so the page is fast and wrong. Compressing a tiny payload and spending more CPU than the bytes were worth. I measure before and after, including CPU and error rate, not only the happy latency.

</details>

**Senior recap:** p99 and burn rate, one trace to the widest span, protect checkout before cosmetics, and do not buy speed with a correctness bug.

Next: [02_Caching.md](02_Caching.md)
