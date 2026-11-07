# 1. Storage and the CAP Theorem

## Simple idea

If the data disappears when the process stops, the product cannot remember users, payments, or history.

**Storage** is how data survives. The choice changes speed, cost, and what happens when a machine or a network link dies.

---

## Structured vs unstructured

| | Structured | Unstructured |
|--|------------|----------------|
| Shape | Rows and columns, known schema | No fixed table |
| Examples | Customers, orders, balances | Photos, video, PDFs, raw logs |
| Usual home | SQL | Object storage, sometimes a document DB |

Semi-structured sits in between: JSON and XML have fields, but the shape can change.

---

## Four storage categories

| Kind | How you use it | Example | Good for |
|------|----------------|---------|----------|
| **Database** | Queries and transactions | PostgreSQL, MongoDB | Profiles, orders, search keys |
| **Object** | Put/get a blob by key, over HTTP | Amazon S3 | Images, video, backups |
| **File** | Folders and file names | NFS, a shared drive | Shared documents, home directories |
| **Block** | Raw disk volumes, no files of its own | AWS EBS | Database disks, VM disks |

```text
Block  =  the disk
File   =  folders on that disk
Object =  a key such as photos/42.jpg, plus metadata
Database =  rows you can filter and join
```

---

## Properties you must name

| Property | Meaning | Everyday picture |
|----------|---------|------------------|
| **Durability** | Data is still there after a crash | The receipt is in the book, not only in someone's head |
| **Availability** | A request gets a response | The shop is open |
| **Consistency** | A read sees the latest committed write | The price on the shelf matches the till |
| **Atomicity** | A set of changes fully happens, or none do | Transfer 100: debit and credit both succeed, or neither does |

Atomicity matters in databases. A half-finished money move corrupts the books. Object storage usually does not give you a multi-object transaction.

---

## Scale vs reliability vs speed

You do not get unlimited growth, zero data loss, and instant reads at the same time for free.

- More copies improve survival and read speed, and add lag or cost.
- A single powerful database is simple and consistent, and it becomes a ceiling and a single failure point.
- A worldwide store stays up during a network split only if you accept that some reads may be slightly old.

---

## CAP theorem

In a **distributed** system, during a **network partition** (some nodes cannot talk), you cannot fully have all three:

| Letter | Meaning |
|--------|---------|
| **C** Consistency | Every read sees the latest write |
| **A** Availability | Every request gets a response (not an error) |
| **P** Partition tolerance | The system keeps running when the network splits |

Partitions happen at real scale. So the practical choice is not "drop P". It is:

```text
When the network is split, do you refuse a request (keep C)
or answer with data that might be stale (keep A)?
```

| Type | During a partition | Example from the course | Use when |
|------|--------------------|-------------------------|----------|
| **CP** | Prefer correct data. May reject the call | HBase. A write that cannot be confirmed is not served | Balances, inventory that must not oversell |
| **AP** | Prefer an answer. It may be stale | DynamoDB default reads, Cassandra-style designs | Feeds, catalogs, "good enough" content |
| **CA** | Consistent and available only if the network never splits | One PostgreSQL node | Not a plan for a multi-data-centre system |

A single-node database can feel like CA because there is no partition inside one process. The moment you replicate across machines, P is real.

DynamoDB can do strongly consistent reads when you ask. The course point is the **default**: favour staying up, with eventual consistency.

---

## How you choose C or A

- **Money, stock counts, tickets** — wrong data is worse than a short error. Lean CP / SQL transactions.
- **Feed, likes, catalog pictures** — a slightly old view is fine. Lean AP so the site stays open.

Say this in interviews: "I accept partitions. For this data I choose consistency. For that data I choose availability."

---

## Photo app and logs (design sketches)

**Photos vs user metadata**

```text
Photos     →  object storage (large blobs)
Users, likes, comments  →  a database (query by user id)
```

**Logs and metrics**

- raw logs → object storage (S3)
- metrics → a time-series or columnar store (the PDF names Druid, InfluxDB)
- batch jobs → a distributed file system such as HDFS, or the same object store

---

## Interview lines

- Storage keeps data after the process dies. It drives speed, cost, and failure behaviour.
- Database, object, file, and block solve different access patterns.
- Durability is "still there". Availability is "got an answer". Consistency is "latest write". Atomicity is "all or nothing".
- CAP: during a partition, choose C or A. CA is the single-node case.

---

## Interview Q&A

### 1. Why is storage critical?

<details>
<summary>Show Answer</summary>

**Answer:**

Applications must keep data across restarts and crashes: profiles, payments, history. The storage design changes scale, latency, availability, and cost.

</details>

### 2. Structured vs unstructured data?

<details>
<summary>Show Answer</summary>

**Answer:**

Structured data has a schema, like SQL rows (customers, transfers). Unstructured data does not: images, video, documents, many logs. JSON is semi-structured: fields exist, but they can change.

</details>

### 3. What storage types exist, and when do you use them?

<details>
<summary>Show Answer</summary>

**Answer:**

- **Databases** — queries and transactions
- **Object storage** — blobs such as media and backups (S3)
- **File storage** — shared folders (NFS, SMB)
- **Block storage** — raw disks for a database or a VM (EBS)

</details>

### 4. Durability, availability, and consistency?

<details>
<summary>Show Answer</summary>

**Answer:**

Durability: data survives crashes. Availability: requests get a response. Consistency: a read returns the latest committed write.

</details>

### 5. What is atomicity, and where does it matter?

<details>
<summary>Show Answer</summary>

**Answer:**

A group of changes completes entirely or not at all. It matters in transactions, such as moving money between two accounts. A partial update corrupts the data.

</details>

### 6. Can a system be highly available and strongly consistent?

<details>
<summary>Show Answer</summary>

**Answer:**

Not always, once the network can split. CAP says that during a partition you pick consistency or availability. You can have both only while every node can still talk, which a single database can do and a wide distributed system cannot promise.

</details>

### 7. What is the CAP theorem?

<details>
<summary>Show Answer</summary>

**Answer:**

A distributed system cannot guarantee consistency, availability, and partition tolerance all at once. Partitions are real, so the design choice is whether to stop serving or to serve possibly stale data when nodes cannot communicate.

</details>

### 8. CP vs AP, with examples?

<details>
<summary>Show Answer</summary>

**Answer:**

**CP** — HBase. If replicas cannot confirm, it will not serve a possibly wrong value. Fit: banking.

**AP** — DynamoDB's default reads, Couchbase. The system answers during a partition; the value may be old. Fit: feeds and catalogs.

</details>

### 9. Why is CA impractical when you are distributed?

<details>
<summary>Show Answer</summary>

**Answer:**

CA assumes the network does not partition. Real clusters lose packets and links. Without partition tolerance you are not fault-tolerant across machines. A standalone PostgreSQL node is the usual CA example.

</details>

### 10. How do you choose consistency or availability?

<details>
<summary>Show Answer</summary>

**Answer:**

Financial writes need consistency. A social feed or a video catalog can prefer availability and catch up. Split the data: do not force one choice on every table.

</details>

### 11. Photo-sharing storage?

<details>
<summary>Show Answer</summary>

**Answer:**

Photos in object storage. User and social metadata in a database chosen for the queries (SQL if relationships and transactions matter, a document store if profiles are flexible).

</details>

### 12. Analytics pipeline for logs and metrics?

<details>
<summary>Show Answer</summary>

**Answer:**

Append-only and horizontal. Raw logs in object storage, metrics in a time-series or columnar database, and a distributed file system or the object store for batch jobs.

</details>

### 13. Object vs file vs block?

<details>
<summary>Show Answer</summary>

**Answer:**

Object storage is a flat key space, API access, and it scales to huge unstructured sets. File storage is folders and shared directories. Block storage is fast raw I/O for disks under a database or operating system.

</details>

**Q&A recap:** pick the container for the data, then pick C or A when the network breaks.

## Interview Q&A — 6 to 8 years

### S1. Someone says the system is "CA" because the network never fails. What do you answer?

<details>
<summary>Show Answer</summary>

**Answer:**

Partitions happen: a deploy, a bad route, a full disk that looks like a dead peer. CA means "I have not chosen." I choose CP or AP for the partition window. For money and seats I choose CP: refuse the write I cannot make consistent. For likes and presence I choose AP: accept the write and repair later. I name the repair.

**Corner cases:** A short partition where both sides accept writes and the heal overwrites a payment. That is an AP choice I did not mean to make. A CP system that times out so long the user retries on a second channel and double-submits. Quorum reads that still see stale data if I read from a node that was partitioned and came back without catching up. I fence old leaders. "Eventual" with no bound is not a design. I say "usually under N seconds, and this is how a conflict resolves."

</details>

### S2. How do you pick block, file, or object for one piece of data?

<details>
<summary>Show Answer</summary>

**Answer:**

Block for a database disk that needs random read-write at a low level. File for a program that must see folders and partial updates on a shared disk. Object for a blob I write once and read by key: image, video, backup. I do not put a million tiny status updates in object storage, and I do not put 4K video in a relational row.

**Corner cases:** Object storage with a read-modify-write of a 5 GB file is the wrong tool. I write a new key. A file share as the lock service for a cluster splits when the network does. A database on a network file system can corrupt if two nodes mount it. Block replication is not application-level consistency. The app can still see a torn logical write if it did not fsync the right way.

</details>

### S3. Durability, availability, and consistency are all "up." How do you separate them in an incident?

<details>
<summary>Show Answer</summary>

**Answer:**

Availability: can I answer? Consistency: is the answer the one the rules require? Durability: after you said ack, is the data still there after a crash? A replica can be up and stale (available, not consistent). A primary can refuse writes to avoid a split (consistent, not available). A cache can be fast and empty after a restart (not durable). I say which one the product pays me for.

**Corner cases:** An ack before fsync or before replica ack. A crash then loses the last orders and I was not actually durable. A user reads their own write from a replica that does not have it yet. That is a consistency bug even though both nodes are "up." Multi-region async replication can lose the last seconds on failover. I state that loss as the RPO, not as a surprise.

</details>

### S4. Where do you allow a stale read, and where do you refuse it?

<details>
<summary>Show Answer</summary>

**Answer:**

I allow stale on feeds, search, and counters the user does not pay from. I refuse stale on a balance check that gates a payment, on a permission revocation, and on "did my write succeed?" right after the write. The pattern is: write to primary, read critical paths from primary, read the rest from replicas or cache with a TTL I can say out loud.

**Corner cases:** A permission cache of 10 minutes lets a fired admin keep acting. Security decisions need a short TTL or an explicit purge. Read-your-writes fails when the next request hits another region. I pin that user to the primary for a moment or I return the written value from the client. A stale search index that still shows a deleted illegal listing is a product and legal bug, not an acceptable CAP choice. I add a filter against the source of truth for that case.

</details>

**Senior recap:** pick CP or AP for the partition, put blobs in objects and invariants in a database, and say which reads may be stale.

Next: [02_SQL_vs_NoSQL.md](02_SQL_vs_NoSQL.md)
