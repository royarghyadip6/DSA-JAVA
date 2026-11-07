# 1. System Reliability

## Simple idea

**Reliability** is the system doing the right job, for a long time, and recovering when something breaks.

```text
High reliability  =  it fails rarely  +  it comes back quickly
```

Uptime alone is not enough. A site that returns errors all day is "up" and still unreliable. Reliability includes correctness, staying consistent enough for the product, surviving faults, and being reachable.

---

## Four words that get mixed

| Word | Question it answers | Everyday picture |
|------|---------------------|------------------|
| **Reliability** | Does it keep doing the job correctly? | The ATM gives the right cash, month after month |
| **Availability** | Can users reach it right now? | The ATM is powered on and responds |
| **Durability** | After a success, is the data still there later? | The money is still in the account after a power cut |
| **Fault tolerance** | Can it continue when a part fails? | One power supply dies and the machine stays on |

Amazon S3 is the durability example from the interview notes: a disk can die and the object remains, because copies exist. That does not mean every request is fast. Durability is "not lost". Availability is "reachable now".

SLA, SLO, and SLI were defined in [performance](../08_Performance/01_System_Performance.md). Here the SLA is the reliability contract: uptime, response time, error rate, and what happens if you miss it.

---

## MTBF and MTTR

| Metric | Meaning |
|--------|---------|
| **MTBF** (Mean Time Between Failures) | Average time the system runs before the next failure |
| **MTTR** (Mean Time To Recovery) | Average time to restore service after a failure |

```text
Availability ≈ MTBF / (MTBF + MTTR)
```

Raise MTBF (fail less often) or cut MTTR (heal faster). Both move availability up. A system that fails every day but restarts in two seconds can still post a high uptime number. Users may still feel it. Watch both.

---

## The nines

The slides give one figure: **99.9% uptime is about 8.76 hours down per year**. Interviews ask for the rest. A year here is 365 days.

| Promise | Downtime per year | Often called |
|---------|-------------------|--------------|
| 99% | ~3.7 days | two nines |
| 99.9% | ~8.8 hours | three nines |
| 99.99% | ~53 minutes | four nines |
| 99.999% | ~5 minutes | five nines |

That budget includes deploys, failover, and bad config, not only hardware. Four nines leaves about an hour for the whole year. A 30-minute manual recovery eats most of it.

---

## What you design in

| Choice | Why it helps |
|--------|----------------|
| Redundancy | No single server, disk, or zone is the only copy |
| Health checks | Bad instances stop receiving traffic |
| Retries | A blip does not fail the user — with a limit, so you do not stampede |
| Circuit breakers | A sick dependency does not take everyone down |
| Replication and quorum | Data survives a node, and a write is accepted only when enough copies agree |

Retries without a timeout and a circuit breaker make an outage worse. Those patterns sit with [microservices](../04_Architectural_Patterns/03_Microservices_Architecture.md).

---

## Distributed and cloud systems

Networks partition. Nodes die. Copies can lag. You already chose consistency or availability for each dataset in [CAP](../07_Storage_Database/01_Storage_and_CAP.md).

Cloud hardware is someone else's machines. They reboot. Design for **transient** failures (try again), **self-healing** (replace a dead instance), and **chaos tests** (kill a node on purpose before customers do).

"Design for failure" means the failure path is a feature, not a surprise.

---

## A four-nines sketch

99.99% is about 53 minutes of downtime a year. A practical design:

- at least two instances, in more than one availability zone
- a load balancer that only sends traffic to healthy nodes
- stateless app servers so a new one can replace a dead one
- a database with a standby that promotes without a human (for example RDS Multi-AZ)
- graceful degradation so a side feature can die without taking checkout with it
- alerts, and a practiced failover

Multi-zone covers a data-centre failure inside one region. It does **not** cover the whole region going away. That is a disaster-recovery decision in the later files. Extra regions cost money and make consistency harder. Do not add them until the downtime budget or the business requires them.

Chaos engineering finds gaps. It does not create the nines by itself.

---

## A service that falls over often

1. Look at metrics and logs: memory leak, CPU, error spike, dependency timeout.
2. Compare how often it fails with the MTBF you expected.
3. Check the last deploy and the last infra change.
4. Add a timeout, a bulkhead, and a circuit breaker so one service cannot sink the rest.
5. Trace one failed request end to end.
6. Write a short postmortem: cause, fix, and what will catch it next time.

Horizontal scale, shards, caches, and queues (sections 6–8) remove the overload that looks like "random" crashes.

---

## Performance vs reliability

Faster and safer pull apart. A live query on the primary is fresh and can knock the primary over at peak. A 15-second cache is slightly stale and keeps the site up.

Say it in interviews as a tradeoff you chose on purpose: what got slower, what stopped falling over, and how you measured it. The PDF's dashboard story is a **sample**, not your biography. Use your own project if you have one.

---

## Do not over-build

- Start from the SLA. Five nines for a hobby blog is wasted money.
- Fix the few failures that cause most of the pain.
- Prefer a managed database and a load balancer over a private consensus cluster you must staff at night.
- Add redundancy where a failure hurts. Leave the rest simple.

---

## Interview lines

- Reliability is correct service over time. Availability is "reachable". Durability is "data still there".
- Availability is about MTBF / (MTBF + MTTR). Fail less, or recover faster.
- 99.9% is about 8.8 hours down per year. 99.99% is about 53 minutes.
- Design for failure: redundancy, health checks, limited retries, circuit breakers.
- A replica in another zone is the usual four-nines step. Another region is for losing the region.

---

## Interview Q&A

### 1. What is system reliability, and why does it matter?

<details>
<summary>Show Answer</summary>

**Answer:**

Reliability is the system doing its job correctly over time, including when pieces fail. It protects downtime, bad user experience, and lost transactions. Banking, health, and any cloud system where one failure can spread need it on purpose, not as an accident.

</details>

### 2. Availability vs durability?

<details>
<summary>Show Answer</summary>

**Answer:**

Availability means users can reach a working system, such as a website that answers. Durability means committed data is not lost, such as an object in S3 that survives a disk failure.

The ATM picture: availability is whether the machine is on. Durability is whether your balance is still in the bank. A system can be durable and down, or up and losing writes.

</details>

### 3. MTBF and MTTR?

<details>
<summary>Show Answer</summary>

**Answer:**

MTBF is the average time between failures. MTTR is the average time to restore service. Availability is about MTBF / (MTBF + MTTR). High MTBF and low MTTR raise that number. Fast recovery can hide frequent failures in the uptime percentage, so track both.

</details>

### 4. How do SLAs shape reliability?

<details>
<summary>Show Answer</summary>

**Answer:**

An SLA is the contract: uptime such as 99.9%, response time, error rate, support response, and penalties. It turns "be reliable" into a number the team can design and staff for. The internal SLO should be tighter than the SLA so you notice trouble before the contract breaks.

</details>

### 5. How would you design for 99.99% availability?

<details>
<summary>Show Answer</summary>

**Answer:**

Four nines allows about 53 minutes of downtime a year, so recovery must be automatic. Remove single points of failure. Run at least two instances across zones, behind a load balancer with health checks. Keep app servers stateless and autoscaled. Use a database that fails over inside the region. Degrade non-critical features instead of failing the whole request. Alert, and practice the failover. Multi-region is the next step when you must survive a whole region, not the first step for four nines.

</details>

### 6. A microservice dies often. What do you do?

<details>
<summary>Show Answer</summary>

**Answer:**

Read metrics and logs for leaks, CPU, and error patterns. Compare the failure rate with the expected MTBF. Check recent deploys and config. Stop the blast radius with timeouts, bulkheads, and circuit breakers. Add traces so one failure shows its cause. Afterward, write a postmortem so the same outage does not repeat.

</details>

### 7. Reliability under heavy traffic and lots of data?

<details>
<summary>Show Answer</summary>

**Answer:**

Scale stateless services horizontally. Shard or partition the data. Queue work that can wait. Cache hot reads. Autoscale so MTTR stays low. Rate-limit so a spike cannot take the core down. A typical shape is Kafka for events, Redis for cache, and read replicas for queries, with the primary protected from that read load.

</details>

### 8. Redundancy and failover in the cloud?

<details>
<summary>Show Answer</summary>

**Answer:**

Run in more than one zone or region. Let the load balancer drop unhealthy targets. Use managed multi-AZ databases. Shift traffic with health-checked DNS (Route 53, Azure Traffic Manager) when a whole site is dead. Keep backups as well as replicas. Redundancy removes the single point of failure. Failover is the automatic switch.

</details>

### 9. A time you chose between performance and reliability?

<details>
<summary>Show Answer</summary>

**Answer:**

Describe a real change: the fast path, the incident it caused, the safer path, and the number that improved. A sample, not your story: a dashboard queried the primary for live numbers, then fell over at peak. A 15-second cache made data slightly stale and kept the site up. Say what you gave up and what you gained.

</details>

### 10. High reliability without over-engineering?

<details>
<summary>Show Answer</summary>

**Answer:**

Set the SLA first. Fix the small set of causes behind most failures. Improve in a loop: monitor, find the cause, fix it. Prefer managed services. Skip extra regions, custom consensus, and extra caches until a measured failure says you need them.

</details>

**Q&A recap:** fail rarely, recover fast, and spend the downtime budget on purpose.

## Interview Q&A — 6 to 8 years

### S1. You are asked for 99.99% and the dependency you call is 99.9%. What do you say?

<details>
<summary>Show Answer</summary>

**Answer:**

My checkout cannot be more available than the payment provider unless I change the product. I queue the payment and confirm later, or I fail over to a second provider, or I lower the promise. Adding my own replicas does not fix a single downstream. I add the downtimes. Two independent 99.9% dependencies in series are worse than either one. In parallel, a backup path can help only if it does not share the failure.

**Corner cases:** A "redundant" pair in one availability zone. A shared database, a shared DNS, or a shared deploy pipeline that fails both at once. Correlated failures are not independent. I do not multiply probabilities as if they were. A retry that spends the whole error budget of the dependency and makes us look down too. Maintenance windows nobody put in the SLO. The number is about unplanned time, and I say whether planned deploys count. A status page that calls the same stack and dies with it, so we cannot even declare the incident.

</details>

### S2. MTTR is two hours and MTBF is a month. Where do you spend the next sprint?

<details>
<summary>Show Answer</summary>

**Answer:**

On detection and recovery, not on a mythical zero-defect rewrite. Page on a user-visible symptom. Make rollback one command. Kill the long manual step (a person SSHing, a restore nobody has practiced). Often cutting MTTR from 2 hours to 10 minutes beats chasing the last bug. I still fix the top repeating cause. I just do not ignore the clock.

**Corner cases:** A fast restart that loses in-flight payments because we had no idempotency. We recovered the process and not the business. An alert that fires after customers have already posted on social media. Time-to-detect was the real MTTR. A runbook that is wrong, so the fast human makes it worse. Game days. A fix that improves MTBF by adding a component whose failure we cannot see.

</details>

### S3. How do you spend a downtime budget on purpose?

<details>
<summary>Show Answer</summary>

**Answer:**

99.9% is about 40 minutes a month. I decide what may use it: deploys, failover tests, a dependency I do not control. I do not let one risky experiment spend the year. Feature flags and canaries keep most deploys inside the budget. If the budget is gone, I freeze risky changes. I say this in the interview as an operational choice, not as a math trivia answer.

**Corner cases:** A monthly number that hides a single 8-hour outage the previous month. Customers remember the long one. Partial outages (checkout down, browse up) that a binary "site up" probe calls success. Regional budgets: the EU was down all morning and the global average is fine. Error budget burn from retries and client bugs that are not our deploys. I still own the user-visible number. Counting planned maintenance as free when the contract did not exclude it.

</details>

### S4. Reliability and availability disagree in a real incident. Give the example you would use.

<details>
<summary>Show Answer</summary>

**Answer:**

The site returns 200 and the receipt is wrong (available, not reliable). Or the site refuses checkouts for ten minutes to avoid double-selling the last tickets (reliable, a hit to availability). I pick the second for money and inventory. I pick a degraded page (stale catalog, no buy button) over a wrong charge. I say which errors I would rather show the user.

**Corner cases:** A circuit breaker that opens and fails every request, including ones that do not need the dead dependency. I bulkhead. A queue that makes us "available" while we silently drop jobs. A read-only mode I never built, so the only options are full down or full wrong. Multi-region writes that stay available and merge two inventories into a negative number. That is the reliability bug hiding inside an availability win.

</details>

**Senior recap:** you cannot be more available than a serial dependency, cut MTTR with rollback and detection, spend the error budget on purpose, and prefer a refused sale to a wrong one.

Next: [02_High_Availability_and_Failover.md](02_High_Availability_and_Failover.md)
