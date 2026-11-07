# 6. Big Data

## Simple idea

**Big data** is data that is too large, too fast, or too messy for one traditional database to store and query comfortably.

A bank ledger of a few million rows is a database problem. Click logs, video events, and sensor streams from a whole product are a big-data problem: land the data cheaply, then process it in batches or as a stream.

---

## The Vs

The slides list **six**. Interview answers often ask for **five** and skip variability. Know both.

| V | Meaning | Example |
|---|---------|---------|
| **Volume** | A lot of bytes | Years of click logs |
| **Velocity** | Arrives quickly | Events per second from every app server |
| **Variety** | Many shapes | JSON events, CSV, images, text |
| **Veracity** | Trust and quality | Duplicates, late events, bad sensors |
| **Value** | A decision you can act on | Which feature to keep |
| **Variability** | The meaning or rate changes | A field that means different things per app version, or traffic that spikes on sale day |

The usual "5 Vs" answer is volume, velocity, variety, veracity, and value.

---

## Why a normal database struggles

- a single primary cannot store or index petabytes cheaply
- rows are a poor layout for "sum this column over a year"
- schemas are rigid for logs that gain fields every release
- the database is busy serving the product. Analytics queries compete with users

So the product database stays for transactions. Analytics moves to a **lake** or a **warehouse**.

---

## Lake, files, and formats

```text
Apps → Kafka or Kinesis → object storage or HDFS
                              files in Parquet or ORC
                         → Spark / Flink / Presto or Trino
```

| Piece | Role |
|-------|------|
| **HDFS** | Cluster file system for large scans, with data locality |
| **S3 (object)** | The usual cloud lake. Cheap, durable, shared by many tools |
| **Parquet / ORC** | Columnar files. Read only the columns you aggregate |
| **Delta Lake** | A table layer on those files: ACID transactions, schema checks, and **time travel** (query an older version) |

Plain files in a bucket are not a table. Delta Lake (or Apache Iceberg / Hudi, the same idea) stops a half-written batch from being visible and lets you undo a bad load.

---

## Batch vs stream

| | Batch | Stream |
|--|-------|--------|
| When | Every hour or night | As events arrive |
| Tools | Spark, classic Hadoop MapReduce | Kafka, Flink, Spark Streaming, Kinesis |
| Fit | Reports, model training, backfill | Fraud, alerts, live dashboards |
| Latency | Minutes to hours | Milliseconds to seconds |

**Fraud:** decide on the stream (Flink or similar) so you can block a payment now. Use batch later to retrain the model and to recompute history. Both, not one.

---

## A terabyte-a-day log design

```text
Services
   → Kafka (buffer, replay)
   → writers flush compressed Parquet to s3://logs/dt=YYYY-MM-DD/hr=HH/
   → Spark batch for daily aggregates
   → Flink for live error-rate alerts
   → Athena, Presto, or Redshift Spectrum for ad-hoc SQL
```

- partition by date and hour so queries skip files
- keep raw data; aggregate in a second layer
- lifecycle cold partitions to a cheaper storage class
- do not insert every log line into the user-facing PostgreSQL

---

## Tools you should be able to place

| Tool | Place it here |
|------|----------------|
| **Spark** | Batch and micro-batch over files or streams |
| **Flink** | True streaming, state, event time |
| **Kafka** | The log that producers and consumers share |
| **Presto / Trino** | SQL over files in S3 or HDFS |
| **HDFS vs S3** | You operate HDFS next to compute. S3 is the managed shared lake |

---

## Interview lines

- Big data is volume, velocity, and variety, plus trust (veracity) and a useful result (value). Variability is the sixth V from the slides.
- Keep OLTP in SQL. Land analytics in a lake.
- Batch for deep jobs. Stream for decisions that cannot wait.
- Delta Lake adds transactions and time travel on top of lake files.

---

## Interview Q&A

### 1. What are the five Vs? (And the sixth?)

<details>
<summary>Show Answer</summary>

**Answer:**

Volume, velocity, variety, veracity, and value. The course also lists variability: the shape or the arrival rate changes over time. If the interviewer asks for five, give the first list and mention variability if they want the extra one.

</details>

### 2. Why can't a traditional database handle big data?

<details>
<summary>Show Answer</summary>

**Answer:**

Volume and velocity outgrow one machine, variety fights a fixed schema, and analytical scans fight the transactions your users depend on. Cost and indexing models assume data that still fits an OLTP database.

</details>

### 3. HDFS vs cloud object storage?

<details>
<summary>Show Answer</summary>

**Answer:**

HDFS is a cluster file system with large blocks, replication, and compute next to the data. S3-style object storage is an API, managed durability, and the common cloud lake. HDFS fits on-premises Hadoop. S3 fits shared, elastic analytics. Both want large columnar files, not millions of tiny ones.

</details>

### 4. Common big-data workloads?

<details>
<summary>Show Answer</summary>

**Answer:**

Log analytics, recommendations, fraud, clickstream metrics, ETL into a warehouse, and training or scoring machine-learning models.

</details>

### 5. Batch vs real-time, and which for fraud?

<details>
<summary>Show Answer</summary>

**Answer:**

Batch (Spark, Hadoop) processes a bounded set on a schedule. Streaming (Flink, Kafka, Spark Streaming) processes events continuously. Fraud detection needs the stream so the decision is in time. Batch still trains models and repairs history.

</details>

### 6. What is Delta Lake?

<details>
<summary>Show Answer</summary>

**Answer:**

An open table format on a data lake. It adds ACID commits so readers do not see a partial write, schema enforcement so a bad file does not silently change columns, and time travel so you can query or roll back to an earlier version.

</details>

### 7. How would you design a terabyte-scale log platform?

<details>
<summary>Show Answer</summary>

**Answer:**

Ingest with Kafka or Kinesis. Write partitioned, compressed Parquet to S3 or HDFS. Run Spark for batch aggregates and Flink for live alerts. Query with Athena, Presto, or Trino. Keep the path off the transactional database, and tier old partitions to cheaper storage.

</details>

### 8. Frameworks you should recognise?

<details>
<summary>Show Answer</summary>

**Answer:**

Spark for large batch and micro-batch jobs, Flink for streaming, Kafka as the durable event log, and Presto or Trino for interactive SQL on the lake. Hadoop MapReduce is the older batch engine Spark largely replaced.

</details>

**Q&A recap:** separate the product database from the lake, pick batch or stream by how late the answer can be, and store columns, not one row file per event.

## Interview Q&A — 6 to 8 years

### S1. The warehouse must not be allowed to take down checkout. How do you draw that line?

<details>
<summary>Show Answer</summary>

**Answer:**

Checkout writes to the product database only. Analytics reads a copy: a stream or a replica, then columnar files in a lake. A heavy report never holds a lock on the orders table. If the pipeline is down, checkout still works and the dashboard goes stale. I say how stale. I do not point the BI tool at the primary.

**Corner cases:** A logical replication slot that is not consumed and fills the primary's disk, which does take checkout down. I monitor slot lag and I cap it. A dual-write from the app to Kafka and SQL where SQL commits and Kafka does not, so revenue reports drift. Outbox fixes that. A backfill query that scans the primary "just this once" during the sale. PII copied to a lake bucket the whole company can read. The copy needs the same access rules.

</details>

### S2. Batch or stream? What corner case flips the choice?

<details>
<summary>Show Answer</summary>

**Answer:**

Batch when the business can wait minutes or hours and the job is a full recompute (yesterday's revenue). Stream when a person or a fraud rule must react in seconds. I often do both: stream for the fresh tail, batch to correct it. Streaming systems duplicate and reorder. If I cannot define the late event rule, I should stay on batch.

**Corner cases:** An event that arrives 6 hours late because a phone was offline. The hourly batch already published the total. I need a restatement, or I accept a wrong hour. Stream processing with no watermark grows state forever. A "real-time" dashboard that runs a giant SQL every 10 seconds is batch load with a stream sticker. Windowed counts that double-count a retried event. I count by event id.

</details>

### S3. Why do columns and file formats matter more than the cluster brand?

<details>
<summary>Show Answer</summary>

**Answer:**

Analytic queries touch a few columns of a huge table. Columnar files (Parquet or similar) with partitions (date, then a coarse key) let the job skip the rest. Tiny JSON files per event waste seeks and metadata. I compact to large files. Partition by something I filter on. I do not partition so finely that I have millions of files.

**Corner cases:** Partitioning by a high-cardinality id creates the small-file problem again. A schema change that writes a new column and an old job that fails on it, or silently drops it. Time zones in the partition key so one day of data is split across two folders and a report misses a slice. Compression that is great for size and too slow for the ingest SLA. Stats on files that are stale, so the optimizer scans everything anyway.

</details>

### S4. A number in the lake does not match the product database. What do you say before you "fix" SQL?

<details>
<summary>Show Answer</summary>

**Answer:**

I assume lag, duplicates, or a different definition before I assume the primary is wrong. I compare event time versus processing time, I dedupe by id, and I check whether the report included refunds, test traffic, or a second region. The fix is usually the pipeline or the definition. I do not update the product database to match a report.

**Corner cases:** Late data after the report was emailed to finance. I need a versioned restatement, not a silent change. A daylight-saving day with 23 or 25 hours. A currency summed without a currency code. A delete that the product did and the lake only appends, so the user still appears. Idempotent merges, not only inserts. Two teams defining "active user" differently. I write the definition next to the number.

</details>

**Senior recap:** checkout does not wait on the lake, late events need a rule, columnar files with coarse partitions, and reconcile definitions before you touch the primary.

Next course section: **Performance** — [08_Performance](../08_Performance/00_Section_Overview.md)
