# 07 — Storage, Databases, and Big Data

This folder matches the course section **Storage — Database and Storage**.

Notes use the Section 7 slides and the interview PDFs. Each file is **theory first**, then **interview Q&A**.

---

## Why this section exists

Every product stores something: a user, a payment, a photo, a log line.

```text
What is the data?  →  structured row, or a file/blob
How correct must a read be?  →  latest write, or "good enough"
How big and how fast?  →  one database, many shards, or a data lake
```

The storage choice decides speed, correctness, failure behaviour, and cost.

**Words in this folder**

| Word | Meaning |
|------|---------|
| **SQL database** | Tables with a fixed shape. Good when a sale must be all-or-nothing. PostgreSQL, MySQL |
| **NoSQL database** | A store shaped for one access pattern. A document, a key, a wide row, or a graph |
| **Replication** | Extra copies of the same data |
| **Sharding** | Splitting the rows across machines so one machine does not hold everything |
| **Object storage** | A cupboard for whole files (photos, video), addressed by a name. Amazon S3 is the famous one |
| **CAP** | When the network breaks, you choose a correct answer or a fast answer. The note explains this slowly |

"Blob" means a file's raw bytes. "Schema" means the agreed columns.

---

## Read in this order

| # | File | What is inside |
|---|------|----------------|
| 1 | [01_Storage_and_CAP.md](01_Storage_and_CAP.md) | Storage types, durability, CAP + Q&A |
| 2 | [02_SQL_vs_NoSQL.md](02_SQL_vs_NoSQL.md) | Relational vs NoSQL, ACID, BASE + Q&A |
| 3 | [03_Replication_Sharding_Polyglot.md](03_Replication_Sharding_Polyglot.md) | Copies, shards, many databases + Q&A |
| 4 | [04_Object_Storage.md](04_Object_Storage.md) | S3-style objects, tiers, access + Q&A |
| 5 | [05_File_Systems_and_Distributed_Storage.md](05_File_Systems_and_Distributed_Storage.md) | Disks vs HDFS + Q&A |
| 6 | [06_Big_Data.md](06_Big_Data.md) | The Vs, batch vs stream, lakes + Q&A |

---

## One-page picker

| You are storing | Usual home |
|-----------------|------------|
| Money, orders, inventory with rules | SQL (PostgreSQL, MySQL) |
| Session, cache, simple lookup | Redis / DynamoDB |
| Flexible profile or catalog | MongoDB |
| Friends, fraud links | Graph (Neo4j) |
| Photos, video, backups | Object storage (S3) |
| A disk for a database or VM | Block storage |
| Shared folders, POSIX files | File storage / CephFS / GlusterFS |
| Petabyte analytics | HDFS, S3, Parquet, Spark |

Many real systems use **more than one**. That is polyglot persistence.

---

## What is next

**Performance**: [08_Performance](../08_Performance/00_Section_Overview.md)

---

## Source PDFs used

- `System Design - updated - Section 7.pdf`
- Introduction to Storage Q&A
- Advanced Database topics (SQL vs NoSQL, and sharding/replication)
- Object Storage, File Systems, and Big Data interview PDFs
