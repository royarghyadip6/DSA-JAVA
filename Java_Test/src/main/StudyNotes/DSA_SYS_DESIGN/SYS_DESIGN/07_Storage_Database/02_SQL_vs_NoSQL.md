# 2. SQL vs NoSQL

## Simple idea

A **database** stores data so you can query it later. Two families:

```text
SQL     tables, fixed columns, strong transactions
NoSQL   documents, keys, wide rows, or graphs, built to spread out
```

Neither replaces the other. Banking and a social feed do not want the same database.

---

## Relational databases (SQL)

Examples: **MySQL, PostgreSQL, Oracle**.

- data lives in **tables** (rows and columns)
- a **schema** says the columns and types before you insert
- you ask questions with **SQL**
- **joins** combine tables (orders with customers)

**ACID**

| Letter | Meaning |
|--------|---------|
| **A**tomicity | The whole transaction commits, or nothing does |
| **C**onsistency | Rules stay true (balance cannot go negative if that is a rule) |
| **I**solation | Parallel transactions do not see each other's half-finished work |
| **D**urability | After commit, a crash does not erase it |

Use SQL when the shape is known, queries are relational, and a wrong or partial write is expensive: banks, ERP, inventory.

**Where SQL hurts**

- schemas that change every week
- spreading one database across many machines (joins and transactions get hard)
- deeply nested JSON as the main model

You can still scale SQL with replicas and sharding. It is just more work than a database that was built to shard.

---

## NoSQL families

| Family | Shape | Examples | Use |
|--------|--------|----------|-----|
| **Document** | JSON-like documents, nesting allowed | MongoDB | Profiles, content, catalogs |
| **Key-value** | One key → one value, very fast | Redis, DynamoDB | Cache, sessions, lookups |
| **Wide-column** | Rows with many dynamic columns, partitioned | Cassandra, HBase | Huge write volume, time-series style keys |
| **Graph** | Nodes and edges | Neo4j | Friends, fraud rings, "who knows whom" |

The course slides call Cassandra and HBase "columnar" and "good for analytics". Treat that carefully.

- **Wide-column** (Cassandra, HBase) is for distributed, high-write operational data.
- **Analytical columnar** files and warehouses (Parquet, Redshift, ClickHouse) store each column together so scans and sums are fast.

They are not the same tool. Cassandra is a poor fit for ad-hoc joins. Parquet is a poor fit for a shopping-cart lookup.

---

## BASE

NoSQL systems often describe themselves with **BASE** instead of ACID:

| Piece | Meaning |
|-------|---------|
| **Basically available** | The system tries to answer, even if the answer is old |
| **Soft state** | Replicas may disagree for a while |
| **Eventually consistent** | If writes stop, copies converge |

A like-count can be off by one for a second. A bank balance should not.

---

## CAP, again, on databases

- Distributed **SQL** clusters usually lean **CP**: they would rather pause than serve a torn write.
- Many **NoSQL** systems lean **AP**, with a knob for stronger reads.
- **MongoDB** can be tuned toward CP or AP.
- **Cassandra** is often described as AP with tunable consistency.
- **Neo4j** is treated as CP because a broken graph is the wrong answer.
- One **PostgreSQL** server is the CA example until you distribute it.

"SQL is always CP and NoSQL is always AP" is too crude. Say the default, then the setting.

---

## Which database for which product

| Product | Pick | Why |
|---------|------|-----|
| Financial ledger | PostgreSQL or MySQL | ACID, constraints, no double spend |
| Product catalog | MongoDB (or SQL if the schema is stable) | Nested attributes, frequent shape changes |
| Real-time chat presence / recent messages | Redis or DynamoDB | Low latency, high write rate. History that must be exact can still sit in SQL |

**MongoDB over PostgreSQL** when documents evolve, you want nested JSON, and you can tolerate tunable consistency. Stay on PostgreSQL when you need joins, constraints, and transactions.

---

## How you model data

**SQL** — normalize. A user lives in one table. Orders point at the user id. You avoid copying the address into every order unless you snapshot it on purpose.

**NoSQL** — model the **read**. If the screen needs a user plus their last five orders, you may store that together so one key fetch is enough. You accept duplication.

---

## Polyglot persistence (preview)

One system, several databases: SQL for payments, Redis for sessions, Elasticsearch for search. The next file goes deeper. The idea belongs here because the SQL-vs-NoSQL answer is often "both".

---

## Interview lines

- SQL: schema, joins, ACID. NoSQL: a data model chosen for scale and access pattern.
- ACID is all-or-nothing and durable. BASE is up now, consistent later.
- Document, key-value, wide-column, and graph are different NoSQL jobs.
- Ledger → SQL. Catalog → document. Hot lookups → key-value. Friend graph → graph.

---

## Interview Q&A

### 1. Key differences between SQL and NoSQL?

<details>
<summary>Show Answer</summary>

**Answer:**

| | SQL | NoSQL |
|--|-----|--------|
| Schema | Fixed | Flexible or dynamic |
| Shape | Tables | Documents, key-value, wide-column, graph |
| Query | SQL | APIs or query languages that vary |
| Integrity | ACID | Often BASE / eventual |
| Scale | Vertical first; sharding is extra work | Often built to scale out |
| Fit | Banking, ERP, inventory | High volume, evolving shape, cache, IoT |

</details>

### 2. ACID vs BASE?

<details>
<summary>Show Answer</summary>

**Answer:**

ACID: atomic, consistent with rules, isolated from other transactions, durable after commit.

BASE: basically available, soft state across replicas, eventually consistent. You trade a moment of disagreement for staying up and scaling out.

</details>

### 3. Types of NoSQL and when to use each?

<details>
<summary>Show Answer</summary>

**Answer:**

- **Document (MongoDB)** — nested profiles and content
- **Key-value (Redis, DynamoDB)** — cache, sessions, id lookups
- **Wide-column (Cassandra, HBase)** — very high write throughput
- **Graph (Neo4j)** — relationships, fraud, social links

For heavy analytical scans, prefer a columnar file or warehouse (Parquet, Redshift), not Cassandra.

</details>

### 4. When prefer MongoDB over PostgreSQL?

<details>
<summary>Show Answer</summary>

**Answer:**

When the document shape changes often, data is nested JSON, and you want fast iteration. PostgreSQL wins for multi-row transactions, constraints, and relational reports.

</details>

### 5. CAP trade-offs?

<details>
<summary>Show Answer</summary>

**Answer:**

During a partition you emphasise consistency (CP, example HBase: refuse rather than lie) or availability (AP, example DynamoDB default: answer, maybe stale). CA fits a single node, not a cluster that must survive a split.

</details>

### 6. Where do SQL and NoSQL sit in CAP?

<details>
<summary>Show Answer</summary>

**Answer:**

Single-node PostgreSQL or MySQL behaves as CA. Distributed SQL usually leans CP. DynamoDB and Couchbase lean AP. MongoDB and Cassandra can be tuned. Neo4j is used as CP when the graph must stay valid.

</details>

### 7. Ledger, catalog, and chat?

<details>
<summary>Show Answer</summary>

**Answer:**

- **Ledger** — SQL, because of ACID
- **Catalog** — document database, because attributes vary
- **Chat hot path** — key-value or document store for latency. Do not promise eventual consistency for a payment inside the same sentence

</details>

### 8. Limits of relational databases when distributed?

<details>
<summary>Show Answer</summary>

**Answer:**

Sharding and distributed joins are hard, schemas resist weekly change, a lone primary is a bottleneck and a failure point, and very high write rates are a poor fit without a redesign.

</details>

### 9. What is polyglot persistence?

<details>
<summary>Show Answer</summary>

**Answer:**

Use more than one database in one product: SQL for transactions, Redis for cache, a search engine for text. Each component gets a store that matches its job.

</details>

### 10. How does modelling differ?

<details>
<summary>Show Answer</summary>

**Answer:**

SQL normalises into related tables and defines the schema up front. NoSQL often denormalises and nests data so the common read is one fetch, even if some fields are copied.

</details>

**Q&A recap:** ACID tables when the rules matter; a NoSQL model when the access pattern and scale matter.

## Interview Q&A — 6 to 8 years

### S1. A team moved from SQL to NoSQL and lost money. What invariant did they drop?

<details>
<summary>Show Answer</summary>

**Answer:**

Probably a multi-row rule: "do not sell the last seat twice," "debit and credit match," "this email is unique." A document store will happily write two documents that contradict each other unless I model the invariant as one document or one conditional update. I keep the money path in SQL (or a store with a real transaction on the keys I need) and I use NoSQL where the access pattern is "one aggregate by key."

**Corner cases:** A unique index I forgot to recreate, so two accounts share an email. A transaction that existed in SQL is now a read-modify-write in the app, and two workers both pass the read. TTL deletes a document I still needed for an audit. Eventual secondary indexes return a row that was deleted, or hide a row that was just written. I do not gate checkout on an eventually consistent index.

</details>

### S2. How do you model a many-to-many relationship without joins?

<details>
<summary>Show Answer</summary>

**Answer:**

I duplicate on purpose along the read path. If the screen is "posts for this user," the user id is the partition key and a post copy lives there. The other direction ("who liked this post") is a different key or a different store. I accept two writes and a repair job when a display name changes. I do not pretend a join across partitions is free.

**Corner cases:** The duplicated name is wrong for days because the repair failed. I version the profile and store the version I copied. Hot partitions: one post with a million likes does not fit one key. I bucket the likes (`postId#bucket`). A "get everything about this user" query fans out to ten partitions and the tail latency is the slowest one. I cap fan-out or I precompute.

</details>

### S3. When is BASE the wrong word to say in an interview?

<details>
<summary>Show Answer</summary>

**Answer:**

When I cannot explain the conflict rule and the user-visible delay. "Eventually consistent" is not an answer. I say: last write wins on this field, or a counter is a merge, or a seat is not eventual at all. BASE is a fit for shopping carts, presence, and feeds. It is a bad fit for a ledger unless the ledger is a single append-only log with a defined order.

**Corner cases:** Last-write-wins drops a concurrent edit with no error. I mention that loss. A client clock decides the winner and a skewed laptop overwrites the real inventory. Use server order. A merge of two shopping carts duplicates a limited item. Counters that count a retried event twice. I store a set of event ids or I accept the error bound.

</details>

### S4. How do you choose the key before you choose the product?

<details>
<summary>Show Answer</summary>

**Answer:**

I write the queries first. "Get order by id," "list orders by user by time," "decrement stock by sku if stock > 0." The key and the index follow those sentences. If I need a transaction across orders and payments, they share a database. If I need infinite write scale on unrelated keys, I partition by that key. The brand (Postgres, Dynamo, Cassandra) is the last sentence.

**Corner cases:** A key I cannot know at write time (search by free text) does not belong in the primary store. A composite key so wide that every query hot-spots on a prefix (all events for today). A migration off SQL that still needs ad hoc reporting. I keep a replica or a lake for that, instead of forcing the online store to do both. A NoSQL item size limit that a "just embed the whole history" model will hit.

</details>

**Senior recap:** transactions for invariants, duplicate for the read you actually do, say the conflict rule, and pick the key from the query.

Next: [03_Replication_Sharding_Polyglot.md](03_Replication_Sharding_Polyglot.md)
