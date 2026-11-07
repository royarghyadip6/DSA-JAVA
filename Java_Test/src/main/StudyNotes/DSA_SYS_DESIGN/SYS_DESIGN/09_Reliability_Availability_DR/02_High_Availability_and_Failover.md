# 2. High Availability, Fault Tolerance, and Failover

## Simple idea

**High availability (HA)** means users can still use the system when something breaks. The trick is to remove single points of failure and to switch away from the broken part quickly.

```text
One server dies  →  another is already there  →  users barely notice
```

---

## Redundancy

**Redundancy** means a spare. Without it, one failure is an outage.

| Layer | Spare looks like |
|-------|------------------|
| Hardware | Second server, second disk, second power supply |
| Network | Two paths, two load balancers |
| Service | Two or more copies of the app |
| Data | A replica in another zone |

A spare that nobody can reach is not redundancy. Health checks and failover have to know it exists.

---

## N+1, active-active, active-passive

| Pattern | Shape | Strength | Cost |
|---------|--------|----------|------|
| **N+1** | You need N to carry the load, and you run N+1 | One failure and you still have capacity | One extra machine |
| **Active-active** | Every node serves live traffic | No idle box. One dies, the others already have the skill | Harder. Shared data, sessions, and writes must be safe |
| **Active-passive** | One node works. The other waits | Simpler. A clear primary | The spare is idle. Failover takes longer |

```text
Active-active     users → load balancer → app A and app B (both live)

Active-passive    users → app A (live)
                         app B (standby, takes over only if A dies)
```

Choose **active-active** when traffic is high and a pause is unacceptable, usually on the **stateless app** tier. Choose **active-passive** when a single writer is simpler: a database primary with a standby. Running two database primaries that both accept writes is a different problem (conflicts). Do not call that "just active-active" without a conflict plan. Section 7 covers leader-follower and leader-leader.

N+1 is a capacity rule. Active-active and active-passive are traffic rules. You can be N+1 and active-active at the same time: three app servers when two would be enough.

---

## Fault tolerance vs high availability

The interview PDF draws a hard line:

- **HA** — downtime is short. A failover may still cause a brief blip.
- **Fault tolerance** — the system keeps going through the failure. The user does not see a stop.

Use that contrast in interviews. Then be precise. "Zero downtime" is the goal, not a promise.

- Two power supplies in one machine can fail over with no user-visible gap. That is classic fault tolerance.
- A software failover (new database primary, new pod) often drops a few requests. If those seconds fit inside 99.99%, you still met HA.
- Fault tolerance is the **design** (redundancy, detection, no single point of failure). HA is the **result** you measure.

Graceful degradation is the cousin: you stay up by doing **less**, not by pretending nothing failed.

---

## Graceful degradation

When a part fails, drop the extra features and keep the core.

| Broken part | Still works |
|-------------|-------------|
| Recommendations service | Search and checkout |
| Payment provider timeout | Browse and cart. Pay later or show a clear retry |
| Video bandwidth | Lower resolution, playback continues |

A full-page error teaches users to leave. A smaller feature set teaches them the product is still there.

This is the user-visible side of circuit breakers and bulkheads. The breaker stops calling a dead service. Degradation decides what to show instead.

---

## Load balancers and failover

A load balancer sends traffic only to healthy nodes. That is what makes active-active survive a crash.

```text
health check fails on app B
   → balancer stops using B
   → A and C keep the traffic
   → platform replaces B
```

**Failover** is the switch to a standby when the primary is dead: another app instance, another database, or another region. Automatic failover is what keeps MTTR inside the downtime budget. A human with a runbook at 3 a.m. is a long MTTR.

Failover can be wrong if both sides think they are primary (**split brain**). Databases need a rule for who wins. Quorum, in the disaster-recovery note, is that rule.

---

## Health checks and self-healing

| Check | Question | If it fails |
|-------|----------|-------------|
| **Liveness** | Is the process alive? | Restart it |
| **Readiness** | Should it receive traffic? | Leave it up, but send no users yet |

Spring Boot Actuator exposes both for Kubernetes. Restarting a process that is merely overloaded (not dead) can make a stampede. Readiness is the right signal for "not ready for traffic".

**Self-healing** replaces or restarts the failed piece: a new virtual machine, a new container, a promoted database replica. It does not fix a bad deploy that crashes every new copy. That needs a rollback. Healing without a look at *why* it died just loops.

---

## Geography

- **Zone** — another data centre in the same region. Low delay. The usual HA target.
- **Region** — another part of the world. Survives a regional outage. Higher delay, harder consistency, more cost. That is disaster recovery, not the first HA knob.

---

## Interview lines

- HA is short downtime. Redundancy plus automatic failover is how you get it.
- Active-active shares live traffic. Active-passive keeps a standby. Databases usually want one writer.
- Fault tolerance means the failure is absorbed. HA means the uptime number is met. A short blip can still be HA.
- Degrade features. Do not take the whole product down.
- Health checks decide who gets traffic. Self-healing replaces the dead node. A bad release still needs a rollback.

---

## Interview Q&A

### 1. What is high availability?

<details>
<summary>Show Answer</summary>

**Answer:**

The system stays usable, with only short interruptions, when parts fail. Users expect the product at any hour. Downtime costs money and trust. HA is how you design so one failure is not a long outage.

</details>

### 2. Active-active vs active-passive?

<details>
<summary>Show Answer</summary>

**Answer:**

Active-active: every node serves traffic. One death is invisible if the others have spare capacity. It fits high traffic and stateless apps. It is harder when state and writes must stay correct.

Active-passive: one node works, the other waits and is promoted on failure. Simpler, and the spare is idle, so failover is slower. It fits a database primary.

Pick active-active for the app tier under heavy load. Pick active-passive when a single writer is the safe design, or when the extra cost of a hot spare you never use is still cheaper than two live writers.

</details>

### 3. Fault tolerance vs high availability?

<details>
<summary>Show Answer</summary>

**Answer:**

Fault tolerance is continuing through a component failure, often with no user-visible stop, as with redundant hardware. High availability is the measured outcome: little downtime. HA failover can still drop a few requests. If that gap is inside the SLO, the system is highly available without being perfectly fault tolerant. Say both: HA is the target, fault tolerance is the design that absorbs the fault.

</details>

### 4. What is graceful degradation?

<details>
<summary>Show Answer</summary>

**Answer:**

The system drops non-essential work and keeps the core. A shop can let people browse when payments are down. A video service can lower resolution instead of stopping playback. Users keep a useful product instead of a hard error.

</details>

### 5. How do load balancers help HA?

<details>
<summary>Show Answer</summary>

**Answer:**

They spread traffic so one node is not the bottleneck, and they stop sending users to a node that fails its health check. New nodes can join when load grows. The balancer is how active-active actually removes a dead server from the path. Run more than one balancer, or use a managed one, so the balancer itself is not the single point of failure.

</details>

### 6. What is failover?

<details>
<summary>Show Answer</summary>

**Answer:**

The automatic switch to a standby when the primary fails: another server, a database replica, or another region. It cuts downtime because a person does not have to do the switch. Example: the load balancer or DNS sends traffic to the secondary after health checks fail. Guard against split brain, where two nodes both act as primary.

</details>

### 7. Health monitoring and self-healing?

<details>
<summary>Show Answer</summary>

**Answer:**

Monitoring watches servers, databases, and services and alerts when they degrade. Self-healing restarts a process, replaces a virtual machine, or promotes a replica without a human. Liveness means "restart me". Readiness means "do not send traffic yet". Together they shrink MTTR. They do not undo a bad deploy. Roll that back, or every new instance will crash too.

</details>

**Q&A recap:** have a spare, send traffic only to healthy nodes, and let the switch happen without a person.

## Interview Q&A — 6 to 8 years

### S1. Active-active in two regions sounds highly available. What must be true first?

<details>
<summary>Show Answer</summary>

**Answer:**

A write rule. Either only one region writes a given key, or I can merge conflicts, or I accept lost writes. "Both regions take every order" without that rule becomes split brain. I also need health checks that can move traffic, data replication I have measured, and a way to fence the sick region so it does not keep taking payments after we called it dead.

**Corner cases:** Both regions healthy and a user writes in one and reads in the other before replication (read-your-writes). A region dies after accepting a payment the other region never saw. I must reconcile with the provider, not only with my replica. Active-active load balancers and a single-primary database, which means I am active-active only until the first write. Sticky users in a dead region because DNS TTL has not moved them. A failover that duplicates cron jobs in both regions. Jobs need a lease.

</details>

### S2. N+1 failed because the +1 could not take the load. What do you size?

<details>
<summary>Show Answer</summary>

**Answer:**

The spare must carry the production traffic, not a hello-world health check. If I run two nodes at 80% each, one failure leaves 160% on the survivor and it falls over too. I run each under 50% or I run three. I test by actually removing a node during a load test, including cache warm-up. A cold spare that boots in 15 minutes is not N+1 for a 30-second SLO.

**Corner cases:** The spare in the same failure domain (one power, one zone, one disk array). A connection pool sized for one node that now has two nodes' traffic and exhausts the database. A rate limit at a partner keyed to one IP, and the spare has a new IP. Licenses that allow one active node. An autoscaler that deletes the "idle" spare I was counting on. I set a minimum.

</details>

### S3. How do you fail over a database without a human and without two writers?

<details>
<summary>Show Answer</summary>

**Answer:**

A controller sees the primary miss checks, confirms it with a quorum so a network blip on one watcher is not enough, promotes a replica that is caught up within the RPO I promised, fences the old primary, then moves the writer endpoint. Applications reconnect with jitter. I would rather fail over in 30 seconds with a rehearsed script than in 30 minutes with a hero. If replication lag is beyond RPO, the automated path should stop and page, not promote a stale replica silently.

**Corner cases:** Promotion of a replica that cannot see the latest sync replica (split). Applications that cache the old IP. A primary that was not dead, only slow, and now both accept writes. I fence first. Long transactions that roll back on promotion and a client that does not retry safely. Read-only endpoints still pointing at a replica that is now the primary and getting write load it cannot take. The first failover of the year, done live, with an untested runbook. I practice.

</details>

### S4. Graceful degradation versus a hard outage. What do you turn off first?

<details>
<summary>Show Answer</summary>

**Answer:**

I turn off the features that are not the promise: recommendations, search facets, image resizing, analytics beacons. I keep login and the core write if I can. The core write, if its dependency is dead, becomes a clear error or a queued accept, not a spinner. Timeouts are short so a dead optional service does not hold threads. Users see a simpler product, not a random 500.

**Corner cases:** A shared thread pool where the optional slow call still starves checkout. I isolate pools (bulkheads). A fallback that is wrong (showing an empty cart as if the user deleted it). Fallbacks must be obviously degraded. A cache used as fallback that has another user's data. A feature flag I cannot flip because the flag service is the thing that is down. Flags for an outage need a local default. Health checks that fail the whole node because one optional dependency failed, so I throw away capacity I still needed.

</details>

**Senior recap:** active-active needs a conflict rule, the spare must fit the real load, fence before you promote, and shed optional features with isolated pools.

Next: [03_Backup_and_Recovery.md](03_Backup_and_Recovery.md)
