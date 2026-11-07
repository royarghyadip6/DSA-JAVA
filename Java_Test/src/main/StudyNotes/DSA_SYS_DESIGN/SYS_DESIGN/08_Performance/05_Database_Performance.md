# 5. Database Performance

## Simple idea

The database is the usual bottleneck. The app looks slow because one query scans too much, opens a new connection every time, or tries to do all the work on one machine.

This lecture repeats replication, sharding, and CAP from [storage](../07_Storage_Database/00_Section_Overview.md), then adds the levers you use every day: indexes, query shape, pools, and precomputed reads.

---

## Replication, from a performance angle

A **leader** takes writes. **Followers** take reads.

- Read-heavy pages get faster because reads spread out.
- Writes do not get faster. They can get slightly slower, because the leader must ship the log.
- Followers can lag. A user who just saved may read a stale replica. Send "must be fresh" reads to the leader.

**Leader–leader** (the slides say master–master) lets more than one node accept writes. That can place writes near a region. It also creates conflicts when both sides update the same row. It is not the default for balances.

---

## Sharding vs partitioning

Both cut data into smaller pieces. The course uses the words this way:

| | Partitioning | Sharding |
|--|--------------|----------|
| Where | Inside **one** database | Across **many** databases or servers |
| Example | Orders split by month in PostgreSQL | Users 0–1M on server A, the rest on server B |
| Why | The planner skips old partitions | One machine cannot hold the rows or the write rate |

PostgreSQL and MySQL can partition a table and still be one server. MongoDB's "sharding" is the across-servers kind. Vendors mix the words. In an interview, say whether you mean **one instance** or **many nodes**.

Range partitions help "last 7 days" queries. Hash shards spread writes and make cross-shard joins painful. Details of range, hash, and consistent hashing are in [replication and sharding](../07_Storage_Database/03_Replication_Sharding_Polyglot.md).

---

## CAP, and why people accept stale reads

During a network split you choose to **wait for the correct value** or **answer with a possibly old one**. You do not "drop partitions" to go faster. Partitions happen. That correction is in [storage and CAP](../07_Storage_Database/01_Storage_and_CAP.md).

The performance link:

- Strong consistency (typical SQL, or a quorum read) can add latency and can refuse work during a split.
- Eventual consistency (Cassandra-style, default DynamoDB reads) stays up and often has lower read latency, and a replica may be behind.

Pick stale-ok for a feed. Do not pick it for a balance.

---

## Indexes

An index is a side structure that finds rows without reading the whole table.

| Type | Good for | Weak at |
|------|----------|---------|
| **B-tree** | Equality and ranges (`age > 30`, `ORDER BY`) | Write cost on every update of that column |
| **Hash** | Equality only (`id = 123`) | Ranges, ordering |
| **Full-text** | Words inside a document | Not a substitute for a numeric filter |
| **Bitmap** | Few distinct values in an analytics warehouse (`status`, `country`) | High-cardinality columns, and heavy OLTP updates |

B-tree is the default you mean in PostgreSQL and MySQL, including for equality. Hash indexes exist and still do not do ranges. Bitmap indexes show up in analytical engines (classic Oracle warehouses). PostgreSQL may combine B-tree indexes with a bitmap **scan** internally. That is not the same as creating a bitmap index.

Indexes speed reads and slow writes, and they take disk. Index columns you filter, join, and sort. Do not index every column "to be safe".

---

## Normalization vs denormalization

| | Normalize | Denormalize |
|--|-----------|-------------|
| Shape | One fact in one place. Other tables point at it | Copy fields so a screen is one read |
| Wins | Clean updates, less anomaly | Faster reads, fewer joins |
| Loses | Joins on a hot read path | Extra storage, you must update every copy |
| Home | OLTP (orders, payments) | Reports, feeds, a Redis document, a warehouse |

You can keep the source of truth normalized and publish a denormalized read model. That is a normal split, not a contradiction.

---

## Connection pooling

Opening a database connection costs a network handshake and memory on the server. A **pool** (HikariCP in typical Spring Boot) keeps a set of open connections and lends them out.

- Throughput goes up because requests do not pay setup cost every time.
- The pool must be **bounded**. One thousand app threads and one thousand database connections will stall PostgreSQL.
- Size the pool from the database's real capacity, then match the app's worker count to it.

---

## A slow query

1. Read the **execution plan** (`EXPLAIN`). A sequential scan on a big table is the usual clue.
2. Index the columns in `WHERE`, `JOIN`, and `ORDER BY` that the plan needs.
3. Select the columns you use. `SELECT *` drags extra data and can block an index-only read.
4. Prefer the join you mean. A wide outer join multiplies rows.
5. Do not return a million rows to the app. Paginate.
6. Watch for **N+1**: one query for orders, then one query per order for its lines. That is 1 + N round trips. Fix it with a join or an `IN` / batch load. This is a common JPA surprise.

Deep `OFFSET` pagination gets slower as the page number grows, because the database still walks the skipped rows. For a large feed, use **keyset** pagination: "give me 20 rows after this id", not "skip 100000".

---

## Materialized views

A normal view runs the query every time. A **materialized view** stores the result.

Use it for a daily sales total or a dashboard that can be minutes old. Refresh on a schedule or after the batch load. Readers do not recompute the joins. The data is stale until the next refresh, and the refresh itself has a cost.

---

## Batching and large data

| Technique | Why |
|-----------|-----|
| **Batch writes** | One transaction with 500 inserts beats 500 round trips |
| **Pagination** | A small page protects memory and the UI |
| **Partition or shard** | The scan touches less data, or another machine |
| **Cache** | Hot keys never reach the database |
| **Warehouse / lake** | Heavy reports do not run on the checkout database |

Handle size in layers. The user-facing query stays small. The historical scan runs somewhere else ([big data](../07_Storage_Database/06_Big_Data.md)).

---

## Interview lines

- Replicas scale reads. They do not scale writes. Shards scale data and writes.
- Partitioning is often one database. Sharding is many nodes. Say which you mean.
- B-tree for equality and range. Full-text for words. Indexes tax every write.
- Pool connections. Read the query plan. Kill N+1. Paginate.
- Strong consistency costs latency under failure. Stale reads are a product decision.

---

## Interview Q&A

### 1. Why does replication matter for performance?

<details>
<summary>Show Answer</summary>

**Answer:**

Copies let you spread **read** traffic and keep serving if a node dies. The leader still takes the writes, and shipping those writes adds overhead. Replicas can lag, so a read-your-writes path should hit the leader. It shines for read-heavy products, not as a way to make inserts faster.

</details>

### 2. Sharding vs partitioning?

<details>
<summary>Show Answer</summary>

**Answer:**

In this course, **partitioning** splits a table inside one database (by month, by range, by hash) so queries skip irrelevant chunks. **Sharding** splits rows across servers so one machine is not the ceiling. Each shard is its own database. The words overlap in vendor docs. State whether the split is one instance or many machines.

</details>

### 3. How does CAP affect performance?

<details>
<summary>Show Answer</summary>

**Answer:**

A partition will happen. If you insist on the latest write, you may wait or return an error, which looks like worse latency and availability. If you answer from a local replica, reads stay fast and may be stale. Cassandra-style designs take that trade. A ledger should not. You are not choosing to abandon partition tolerance.

</details>

### 4. Index types?

<details>
<summary>Show Answer</summary>

**Answer:**

- **B-tree** — equality and ranges. The default in PostgreSQL and MySQL.
- **Hash** — equality only. No range.
- **Full-text** — keyword search in text.
- **Bitmap** — low-cardinality columns in analytical databases.

Add indexes for real filters and joins. Every extra index slows inserts and updates.

</details>

### 5. Normalization vs denormalization?

<details>
<summary>Show Answer</summary>

**Answer:**

Normalization removes repeated facts so updates stay correct, at the cost of joins. Denormalization copies data so a read is one lookup, at the cost of storage and the risk of copies drifting. OLTP stays normalized. Reporting and hot read models are often denormalized.

</details>

### 6. How does connection pooling help?

<details>
<summary>Show Answer</summary>

**Answer:**

The pool reuses open connections instead of connecting and disconnecting per request. That cuts latency and lets more requests through. In Spring, HikariCP is the usual pool. Keep it smaller than "one connection per thread", or the database spends its time scheduling connections instead of queries.

</details>

### 7. How do you optimize a slow query?

<details>
<summary>Show Answer</summary>

**Answer:**

Read `EXPLAIN`. Stop full scans of large tables by indexing filter, join, and sort columns. Select only needed columns. Avoid N+1 query loops. Paginate. Denormalize a read model only when the joins are the proven cost. Measure again after the change.

</details>

### 8. What is a materialized view?

<details>
<summary>Show Answer</summary>

**Answer:**

A stored result of a query. Readers hit that table instead of repeating a heavy join. You refresh it on a schedule. It is ideal for reports that can be slightly old. It is the wrong tool for a balance that must be current.

</details>

### 9. How do you handle large datasets?

<details>
<summary>Show Answer</summary>

**Answer:**

Partition or shard so one query or one server does not hold everything. Paginate, preferably by keyset once offsets get deep. Cache hot rows. Batch writes. Run big analytics on a warehouse or lake, not on the checkout database.

</details>

### 10. Consistency vs performance?

<details>
<summary>Show Answer</summary>

**Answer:**

Strong consistency can add waiting and, during a partition, errors. That is the right cost for payments. Eventual consistency usually gives higher availability and cheaper reads, and users can see old data for a while. That is acceptable for a feed or a catalog count. Choose per data set, not once for the whole company.

</details>

**Q&A recap:** spread reads, index the real filters, pool connections, and do not scan a year of rows to paint one screen.

## Interview Q&A — 6 to 8 years

### S1. You added an index and the write path got slower. When is that the right trade?

<details>
<summary>Show Answer</summary>

**Answer:**

When the read was on the user path and the write can afford the extra maintenance. Every index speeds a filter and slows insert and update. I index the columns in the WHERE and JOIN of the hot query, in an order that matches the leftmost lookup. I do not index every column "to be safe." I drop indexes that the planner never picks. A unique index is also a correctness tool, not only a speed tool.

**Corner cases:** A function on the column (`WHERE lower(email) =`) that cannot use the plain index. I index the expression. A leading wildcard `LIKE %foo` that will not use a normal btree. Low-cardinality columns (a boolean) that the planner ignores. An index that fits in test data and does not fit in memory in production, so it random-reads the disk. A composite index that my query skips the first column of, so it does not apply. Write amplification on a hot row that updates an indexed column every view. I stop indexing the view counter and I batch it.

</details>

### S2. Connection pooling was added and the database is now at max connections. How?

<details>
<summary>Show Answer</summary>

**Answer:**

Each app node has a pool. Ten nodes times a pool of 50 is 500 sessions, plus workers, plus migrations. I size the pool so the sum is under the database limit, with headroom. Idle connections still count. I use a transaction-mode pooler if clients hold connections while they do other work. I do not open a connection per request with no pool, and I do not set the pool to "unlimited."

**Corner cases:** A leak that checks a connection out and never returns it, so the pool fills and every request waits. A pool timeout that is longer than the HTTP timeout, so the user is gone and the server is still waiting for a connection. Prepared-statement limits with a transaction pooler that swaps backends. A health check that takes a pooled connection every second from every node. A failover where all nodes reconnect at once and stampede the new primary. I jitter reconnects.

</details>

### S3. The query is "fast" and the screen is slow. What shape of query is lying to you?

<details>
<summary>Show Answer</summary>

**Answer:**

N+1: one query for the list and one query per row. Each is 2 ms and 200 rows is 400 ms plus round trips. I join or I batch `WHERE id IN (...)` with a cap. I also look at rows examined. A query that returns 20 rows and examines 2 million is not fast. It is lucky while the buffer cache is warm. Offset pagination (`OFFSET 100000`) is this bug in disguise.

**Corner cases:** A select star that pulls wide JSON or TOAST columns I do not display. A lock wait that does not show up in the query's own plan. The time is in `wait_event`. A report query on the primary that blocks vacuum or fills the cache and evicts the checkout query. I move it to a replica. A plan that changes at night because statistics were stale. I see a nested loop over a million rows. I update stats and I pin the index if I must.

</details>

### S4. When do you denormalize or add a materialized view, and how does it lie?

<details>
<summary>Show Answer</summary>

**Answer:**

When a read is hot and the join is large, and the product can tolerate a defined delay. I copy the display name onto the order, or I refresh a summary table. I name who updates the copy (a trigger, a job, or the same transaction). If it is the same transaction, it cannot drift and it slows the write. If it is async, it can drift and the write stays fast. I need a rebuild path when the job was down.

**Corner cases:** A materialized view refreshed with a lock that stalls reads every hour. A summary that double-counts when the job retries. A denormalized stock column that becomes the thing people decrement without the real invariant. The copy must not become the authority for money. A view that is "materialized" in the app's memory on one node, so the other nodes disagree. Time windows that are cached and miss the late event, so yesterday's total changes after we published it.

</details>

**Senior recap:** index the real predicate and count the write cost, size pools as a fleet sum, batch N+1, and say how a denormalized copy catches up.

Next course section: **Reliability, availability, and disaster recovery** — [09_Reliability_Availability_DR](../09_Reliability_Availability_DR/00_Section_Overview.md)
