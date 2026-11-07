# 3. Replication, Sharding, and Polyglot Persistence

## Simple idea

One database server runs out of disk, CPU, or the ability to survive a crash.

```text
Replication  =  extra copies of the same data (survive, and read faster)
Sharding     =  split the data so each machine holds a part (write and size)
Polyglot     =  different databases for different jobs
```

Copies do not make writes faster. Splits do not magically keep joins easy.

---

## Vertical vs horizontal scaling

| | Vertical | Horizontal |
|--|----------|------------|
| What you do | Bigger CPU, RAM, disk on one machine | More machines |
| Fits | The data still fits on one node and you need simple consistency | Data or traffic no longer fits one node |
| Limit | Hardware has a ceiling, and one machine is still one failure | You must partition, replicate, and route |

Start vertical while one PostgreSQL box is enough. Move horizontal when it is not.

---

## Leader-follower replication

One **leader** (primary) takes writes. One or more **followers** copy the leader's log and serve reads.

```text
Client write  →  Leader  →  replication log  →  Follower A
                                          →  Follower B
Client read   →  Follower (or the leader if you need the latest)
```

| Mode | Behaviour | Cost |
|------|-----------|------|
| **Asynchronous** | Leader says "committed" before followers catch up | Fast writes. A crash can lose the newest writes. Reads can be stale |
| **Synchronous** | Leader waits until a follower has the write | Stronger durability and fresher reads. Slower, and a dead follower can stall writes |

If the leader dies, a follower must be promoted. That election or manual failover means a window where writes fail. Automatic failover needs care so you do not end up with two leaders.

---

## Read replicas

Read replicas are followers used to **scale reads**.

- they apply the leader's log
- they do not accept client writes
- they help dashboards, search-like reads, and traffic spikes on `SELECT`

They do **not** increase write throughput. They add **replication lag** (a user saves a profile and immediately reads an old copy). The application must decide which reads can be stale and which must hit the leader.

The course lists "read replicas" separately from "leader-follower". Treat read replicas as that pattern pointed at read scale, not as a different invention.

---

## Sharding

**Horizontal sharding** splits **rows**. Users 1–1 million on shard A, the rest on shard B.

**Vertical sharding** splits **tables or columns**. Profiles on one database, payments on another. That is also a step toward polyglot persistence.

You shard when one node cannot hold the data or the write rate, and you accept harder queries, rebalancing, and cross-shard transactions.

### How you pick the shard

| Strategy | Rule | Strength | Weakness |
|----------|------|----------|----------|
| **Range** | `userId` 1–1000 on shard 1 | Range queries stay together | One hot range can overload a shard |
| **Hash** | `hash(key) % N` | Spreads load | Adding a shard moves almost every key. Range queries scatter |
| **Consistent hashing** | Keys and nodes sit on a ring | Adding a node moves only a slice of keys | More complex. Needs virtual nodes to stay even |
| **Geo** | Users in India on the India shard | Low latency for local users | Cross-region features and compliance get harder |

**Hash modulo N vs consistent hashing**

```text
hash % 4        add a 5th node → almost every key moves
consistent hash add a 5th node → only keys on that arc move
```

Cassandra, Dynamo-style stores, and Redis Cluster use consistent hashing (or a close variant).

---

## CAP on these systems

| System | Usual interview label | What to say |
|--------|----------------------|-------------|
| MongoDB | CP when configured for majority writes and reads | Tunable. Strong settings refuse rather than diverge |
| Cassandra | AP | Stays up across failures. You choose the consistency level per query |
| DynamoDB | AP by default | Eventually consistent reads unless you request a strongly consistent read |

---

## Polyglot persistence

```text
Orders and payments   →  PostgreSQL     (transactions)
Product documents     →  MongoDB        (flexible catalog)
Search                →  Elasticsearch  (text)
Sessions and hot keys →  Redis          (speed)
```

**Benefits:** each access pattern gets a fitting store, and teams can scale the hot one.

**Costs:** more systems to run, data copied between them, and no single transaction across all of them. A payment in SQL and a search document in Elasticsearch can be briefly out of date. You need events or jobs to sync them.

**Netflix-style split (from the interview PDF)**

- Cassandra for write-heavy, multi-region viewing data
- MySQL for billing
- Elasticsearch for search
- DynamoDB for some metadata

**Uber-style split**

- PostgreSQL or MySQL for core trips and payments
- Redis for live location and sessions
- a warehouse (BigQuery or Hadoop) for analytics, not for the checkout path

---

## Interview lines

- Replicate to survive and to read. Shard to grow writes and data size.
- Async replication is fast and can lag or lose the last writes. Sync replication waits.
- Read replicas do not take writes.
- Range shards keep ranges and create hotspots. Consistent hashing limits how many keys move.
- Polyglot means the right database per job, plus a plan to keep them in step.

---

## Interview Q&A

### 1. Vertical vs horizontal scaling for databases?

<details>
<summary>Show Answer</summary>

**Answer:**

Vertical means a bigger machine. It is simple and consistent, and it hits a hardware ceiling and a single point of failure.

Horizontal means more machines. It handles more data and traffic, and it requires sharding, replication, and more operations work.

</details>

### 2. What is leader-follower replication?

<details>
<summary>Show Answer</summary>

**Answer:**

The leader takes writes and ships a log to followers. Followers serve reads. If the leader dies, a follower is promoted. Async mode is fast and can lag. Sync mode waits for a follower and is safer and slower.

</details>

### 3. Pros and cons of read replicas?

<details>
<summary>Show Answer</summary>

**Answer:**

Pros: more read capacity, and reads can continue if you still have a replica.

Cons: replication lag, no extra write capacity, and the app must route "must be fresh" reads to the leader.

</details>

### 4. Range-based vs hash-based sharding?

<details>
<summary>Show Answer</summary>

**Answer:**

Range sharding keeps ordered keys together, so range scans are natural, and a popular range becomes a hotspot.

Hash sharding spreads keys evenly and makes range queries hit many shards. With `hash % N`, adding a shard reshuffles most keys.

</details>

### 5. What is consistent hashing, and why does it matter?

<details>
<summary>Show Answer</summary>

**Answer:**

Nodes and keys sit on a ring. A key belongs to the next node clockwise. When you add or remove a node, only the keys on the nearby arc move, not the whole dataset. That is why it is used in Cassandra, Dynamo-style systems, and Redis Cluster.

</details>

### 6. CAP examples for MongoDB, Cassandra, and DynamoDB?

<details>
<summary>Show Answer</summary>

**Answer:**

MongoDB can be run as CP with majority read and write concern. Cassandra is generally AP and lets you set consistency per request. DynamoDB is AP on the default read path, with an option for a strongly consistent read.

</details>

### 7. What is polyglot persistence? Benefits and challenges?

<details>
<summary>Show Answer</summary>

**Answer:**

Use several databases in one system. Example: PostgreSQL for orders, MongoDB for the catalog, Elasticsearch for search, Redis for cache.

Benefits: each store matches its workload. Challenges: operational load, duplicated data, and no single ACID transaction across all of them.

</details>

### 8. How do Netflix and Uber use this?

<details>
<summary>Show Answer</summary>

**Answer:**

Netflix: Cassandra for high-write, multi-region data, MySQL for billing, Elasticsearch for search, DynamoDB for some metadata.

Uber: a relational database for core transactional data, Redis for real-time location and sessions, and a warehouse for analytics.

</details>

**Q&A recap:** copy for safety and reads, split for size and writes, and use more than one database when the jobs differ.

## Interview Q&A — 6 to 8 years

### S1. A replica is promoted and you briefly have two primaries. What do you design so that ends?

<details>
<summary>Show Answer</summary>

**Answer:**

Fencing. The old primary must be unable to accept writes: a lock in the data path, a generation number, or a STONITH step that cuts it off before the new primary takes traffic. Async replication means the new primary is missing the last commits. I state that loss (RPO) or I use sync replication and I accept the latency and the availability hit when the replica is slow.

**Corner cases:** An application still holding a connection to the old primary. DNS still pointing at it. A replica that was delayed an hour and is promoted by a tired human. Split brain on a partition where both sides think they are primary. Automated failover that flaps. I require a quorum and a cooldown. Read replicas used for a unique check (email not taken) that then inserts on the primary and races.

</details>

### S2. How do you pick a shard key you will not regret?

<details>
<summary>Show Answer</summary>

**Answer:**

The key is in almost every query, spreads writes, and does not need cross-shard transactions on the hot path. User id or tenant id is often right. A sequence or "today" is often wrong because the latest shard takes every insert. I plan a way to split a hot shard later (a prefix plus a bucket). I do not start with a key I chose because it was the primary key of the old table.

**Corner cases:** A celebrity tenant bigger than one shard. I isolate them. Resharding live traffic without a dual-write or a consistent hash move. Queries from support ("find by email") that now scatter to every shard. I keep a lookup table. A transaction that updates inventory in shard A and the order in shard B. I put both under the same key or I make one of them async. Hashing the key so well that range queries become impossible. I say that cost.

</details>

### S3. Polyglot persistence solved scale and created an incident. What was the missing piece?

<details>
<summary>Show Answer</summary>

**Answer:**

Two stores, no owner of the truth, no repair. The order is in SQL and the search document is in the index. If the index update fails, users cannot find a product they can still buy, or they can find one that is gone. I pick a source of truth, I update the other from an outbox, and I run a repair that can rebuild the copy. I do not dual-write from the app with two independent commits and hope.

**Corner cases:** A cache, a search index, and a warehouse that disagree in three different ways. I document which one answers which question. A delete that must be forgotten (privacy) has to reach every copy, including backups and logs, not only the primary row. A join I "removed" by copying data means a schema change must roll out to every copy. Lag on the copy makes read-your-writes fail for search. After a write I can read from SQL for that user for a few seconds.

</details>

### S4. Consistent hashing is supposed to save you during a rescale. What still hurts?

<details>
<summary>Show Answer</summary>

**Answer:**

Only the keys that move should move, not the whole dataset. Virtual nodes spread that move so one new server does not take a full neighbor's data in one spike. The hurt that remains: hot keys still hot-spot on one node, the move itself is load, and clients with a stale ring send writes to the old owner. I version the ring and I forward or reject during the move.

**Corner cases:** A key that is a single popular video. Hashing does not split one key. I add an application-level bucket. Replication factor three plus a rebalance can overload the network. A bug that treats a node leave as "delete the range." I drain and copy before I drop. Hashing user id while some requests only have an email. I need a stable id before I place the write.

</details>

**Senior recap:** fence old primaries, shard by a key you query and that spreads, one source of truth with a repair path, and hashing does not split a hot key.

Next: [04_Object_Storage.md](04_Object_Storage.md)
