# 2. Scaling Strategies — Vertical, Horizontal, Diagonal

## Simple idea

When the app is too small for the traffic, you have two moves:

```text
Vertical    =  scale UP     one stronger machine
Horizontal  =  scale OUT    more machines
Diagonal    =  UP first, then OUT
```

---

## Vertical scaling

Add CPU, RAM, or disk to the **same** server.

```text
16 GB RAM  →  64 GB RAM
4 cores    →  32 cores
```

**Why people start here**

- no load balancer required on day one
- no rewrite for shared state
- a database often grows this way first
- fast for an MVP

**Where it stops**

- there is a biggest box you can buy
- that box is a **single point of failure**
- a resize often needs a restart
- the big machine is expensive, and you pay for it even at night

---

## Horizontal scaling

Add **more** servers and share the work.

```text
Users → Load balancer → App 1
                      → App 2
                      → App 3
```

**You must have**

- a load balancer (or equivalent)
- a way to handle **state** (sticky sessions, or Redis, or no session on the box)
- copies that can fail independently

**You gain**

- more capacity by adding nodes
- one node can die and the others continue

**You pay**

- distributed-system work: discovery, deploys, consistency, extra latency

This is the usual path for stateless web apps and microservices.

---

## Diagonal scaling

Do not jump to 50 servers on day one.

```text
1. Make the one box bigger while traffic is small
2. When that box is near its limit, add more boxes
```

Cloud apps do this often: a larger instance type first, then an autoscaling group. It stays cheap early and does not trap you on one machine forever.

**Fits**

- a startup that should not build Kubernetes in week one
- bursty systems that need a quick vertical bump and long-term horizontal room
- Lambda-style apps that already scale out, with instance size chosen for each unit of work

---

## Trade-offs

| | Vertical | Horizontal |
|--|----------|------------|
| Performance ceiling | One machine | Grows as you add nodes (if the work splits) |
| Complexity | Low at first | Load balancer, state, orchestration |
| Failure | One box dies, the app dies | Other boxes can take over |
| Deploy | One place | Many copies, rolling updates |
| Cost | Huge machines cost a lot | Many small machines plus ops time |

**Horizontal is more scalable and safer long term. Vertical is the quicker, simpler win.**

Diagonal is how you get both: simple now, able to spread later.

---

## When to choose which

| Situation | Choice |
|-----------|--------|
| Startup, MVP, small team | Vertical |
| Need a fix this week, code is a monolith | Vertical |
| Team has not run distributed systems | Vertical for now |
| Traffic and uptime are the product | Horizontal |
| Cloud app that will grow for years | Diagonal |
| Stateless web tier | Horizontal behind a load balancer |

Course pictures: a small startup scales up. Twitter eventually had to scale out. Cloud-native apps mix both and let autoscaling add nodes.

---

## When more app servers do not help

Horizontal scaling fails if the work **cannot be split**.

- a global lock so only one request runs at a time
- a single-threaded job
- session state stuck in one process, and the load balancer is not sticky
- a database that is the real bottleneck

```text
10 app servers → 1 PostgreSQL primary
More app servers only create a longer queue at the database
```

For the database you need a different move: bigger primary (vertical), read replicas, or sharding. "Add a node" is not automatic.

---

## Problems you must solve when you scale out

| Problem | Practical fix |
|---------|----------------|
| Sessions | Redis, or sticky sessions, or JWT |
| Data consistency | Local transactions where you can; sagas or eventual consistency across services |
| Uneven traffic | Least connections, weights, or IP hash |
| Finding instances | DNS, Eureka, Consul, or the cloud's discovery |
| Deploys | Containers and Kubernetes (or the cloud's equivalent) |
| Extra latency | Cache, fewer remote calls |

---

## Interview lines

- Up = bigger box, simple, hits a ceiling, one failure point.
- Out = more boxes, needs a load balancer and statelessness, survives a dead node.
- Diagonal = up first, out when the box is not enough.
- More app servers do not fix a single-threaded lock or a maxed database.

---

## Interview Q&A

### 1. What is the difference between horizontal and vertical scaling?

<details>
<summary>Show Answer</summary>

**Answer:**

**Vertical:** more CPU, RAM, or disk on one machine. Example: database RAM from 16 GB to 64 GB. Simple, but capped, and that machine is a single point of failure.

**Horizontal:** more machines behind a distributor. Example: three app servers and a load balancer. Scales further and can survive one death, but you must handle state, deploys, and coordination.

</details>

### 2. What is diagonal scaling, and when is it a good idea?

<details>
<summary>Show Answer</summary>

**Answer:**

Start vertical while you are small. Move horizontal when one machine is not enough.

It fits startups and bursty cloud apps: low complexity early, a path to elasticity later, without a distributed design on day one.

</details>

### 3. What are the trade-offs in performance and complexity?

<details>
<summary>Show Answer</summary>

**Answer:**

Horizontal can keep adding capacity and can fail over, but it needs distributed design and costs more to operate.

Vertical is easier to run and deploy, and it stops at one machine's limit. A very large machine is also expensive.

</details>

### 4. When would horizontal scaling not help?

<details>
<summary>Show Answer</summary>

**Answer:**

When the work cannot run in parallel: a global lock, a single thread, or a stateful app with no shared session store.

Also when the bottleneck is a database that you only copied the app tier around. Extra PostgreSQL app connections do not speed a saturated primary. You need replicas, sharding, or a bigger database.

</details>

### 5. When would you choose vertical over horizontal?

<details>
<summary>Show Answer</summary>

**Answer:**

When you need a fast fix, the app is a tightly coupled monolith, the team is new to distributed systems, or traffic is still small. Use it when elasticity does not yet justify the operational cost.

</details>

### 6. What challenges come with horizontal scaling, and how do you solve them?

<details>
<summary>Show Answer</summary>

**Answer:**

- **State** — Redis or stateless tokens
- **Consistency** — sagas, eventual consistency, or CQRS where a single transaction cannot span services
- **Traffic split** — a real load balancer (least connections, weights, IP hash)
- **Discovery** — Consul, Eureka, or cloud DNS
- **Deploys** — Docker and Kubernetes
- **Latency** — cache and fewer remote calls

</details>

**Q&A recap:** up, out, then diagonal. Split only the work that can be split.

## Interview Q&A — 6 to 8 years

### S1. You scaled up the database twice. The third time does not help. What is the ceiling?

<details>
<summary>Show Answer</summary>

**Answer:**

One machine has a maximum CPU, a maximum number of connections, and one write-ahead log. A bigger box also means a bigger blast radius and a longer restart. I scale up once if the working set or the write rate still fits one well-sized primary. After that I split reads (replicas, cache) or I split writes (shard by a key that spreads). I say which limit I hit: disk IOPS, lock wait, or a single hot row.

**Corner cases:** A bigger machine with the same tiny connection pool and the same missing index. Vertical scale hides a bad query until the next jump, which is now a very expensive box. Licensing that is priced per core. A failover of a huge primary that takes 20 minutes to warm its cache, so the "bigger" database has a worse outage. I budget that warm-up.

</details>

### S2. Horizontal scale made latency worse. How is that possible?

<details>
<summary>Show Answer</summary>

**Answer:**

The new nodes added coordination. More app servers opened more database connections than the primary allows. More shards added a scatter-gather query that waits for the slowest shard. More cache nodes with no request collapsing stampeded the database. The work was not actually parallel. A global lock or a single queue partition still serializes it.

**Corner cases:** Cross-zone hops on every request after I spread nodes for availability. A chatty service that now calls ten peers instead of an in-process method. Uneven shard sizes so one node is hot and the others are idle. I look at the hottest node, not the average. A deploy that rolled all new nodes with a cold cache at once.

</details>

### S3. How do you split a workflow that "cannot" be split?

<details>
<summary>Show Answer</summary>

**Answer:**

I split by what can be stale or late, not by tables. The user-facing decision (seat sold or not) stays one conditional write. Email, search indexing, analytics, and thumbnails move to async workers. If the write itself does not fit one node, I partition by a key the request already has (user id, tenant id), and I forbid queries that need every partition unless they are offline reports.

**Corner cases:** A report that "just this once" joins all shards on the hot path. A transaction that updates two shards and now needs two-phase commit. I redesign so one request writes one shard. A tenant larger than one shard (a celebrity, a mega-customer). I isolate them on their own partition instead of pretending the hash is uniform. Time-based partitions that make "today" one hot shard.

</details>

### S4. Diagonal scaling sounds like "do both." What order do you actually use?

<details>
<summary>Show Answer</summary>

**Answer:**

I make one node reasonably large so I am not operating a hundred tiny noisy boxes. Then I add copies of that size. I do not jump to 200 small nodes to avoid picking a machine size, and I do not buy the largest machine to avoid thinking about a second copy. The second copy is for failure as much as for load. If I cannot run two, I have not scaled. I have a bigger single point.

**Corner cases:** Autoscaling tiny nodes that spend their life booting. A primary that is scaled up while replicas stay small, so a failover lands on a replica that cannot take the traffic. Different instance types with different network caps, so the cheap nodes drop packets. A stateful node I cannot horizontally copy (local disk). I move state off it before I promise to scale out.

</details>

**Senior recap:** vertical scale has a restart and a hot-row ceiling, horizontal scale can add coordination, split stale work first, and run two nodes of a sane size.

Next: [03_Load_Balancing.md](03_Load_Balancing.md)
