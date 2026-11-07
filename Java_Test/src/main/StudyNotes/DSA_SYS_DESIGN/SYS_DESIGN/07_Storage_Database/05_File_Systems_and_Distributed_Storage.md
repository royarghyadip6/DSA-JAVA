# 5. File Systems and Distributed Storage

## Simple idea

A **file system** turns a disk into folders and files: `ext4` and `XFS` on Linux, `NTFS` on Windows.

That works on **one machine**. When the files must live on many machines, you want a **distributed file system** (DFS): HDFS, CephFS, GlusterFS.

```text
One laptop     →  ext4
A Hadoop cluster reading terabytes  →  HDFS
Apps that want a normal mount across servers  →  CephFS or GlusterFS
```

---

## Local file systems

| System | Where | Notes |
|--------|--------|-------|
| ext4 | Linux | The usual default. Journaling helps recover after a crash |
| XFS | Linux | Strong with large files and parallel I/O |
| NTFS | Windows | The Windows default |

They know one node's disks. If that node dies, the files are gone unless you backed them up or replicated at another layer.

---

## HDFS (Hadoop Distributed File System)

Built for **big files** and **batch** reads, not for editing a file in place.

```text
Client
  → NameNode     (which blocks live on which machines; the map, not the bytes)
  → DataNodes    (the actual blocks, default 128 MB or 256 MB)
```

- a file is split into large blocks
- each block is copied, often **3** times, onto different nodes
- clients read a block from a DataNode, preferably one close to the compute job (**data locality**)
- DataNodes send heartbeats. If one dies, the NameNode orders another copy so the replica count returns to 3

The **NameNode** holds the metadata. If you run a single NameNode, it is a single point of failure. Production HDFS uses a standby NameNode (HA).

HDFS is a poor place for millions of tiny files (the NameNode must remember each one) and a poor place for random in-place updates. It is a good place for a write-once dataset that Spark or MapReduce scans.

---

## CephFS and GlusterFS

| | HDFS | CephFS | GlusterFS |
|--|------|--------|-----------|
| Main job | Analytics, write-once, huge files | General POSIX files, plus object and block in the same Ceph cluster | Simpler clustered file storage on ordinary disks |
| API | Hadoop-style, not a full everyday POSIX disk | POSIX: many apps can mount it | POSIX |
| Metadata | NameNode | Distributed (Ceph monitors and MDS) | Elastic hash, no single metadata server in the classic design |
| Fit | Spark / Hadoop | Unified cluster: files, RBD block, RGW object | Straightforward scale-out file share |

Ceph is one cluster that can present **object, block, and file**. That is why it shows up in private-cloud designs.

---

## Latency vs throughput

| | Latency | Throughput | Typical use |
|--|---------|------------|-------------|
| Local ext4 / block disk | Low | Limited by one machine | Database, boot disk |
| HDFS | Higher | Very high for large sequential reads | Batch analytics |
| Object storage | Higher | High for parallel large GETs | Media, lake, archive |

A web request that needs 5 ms should not open a file on HDFS. A nightly job that reads 50 TB should.

---

## Analytics at scale

- store data in **columnar** files such as **Parquet** or **ORC** so queries read only the columns they need
- keep compute next to the blocks when you use HDFS (data locality)
- add DataNodes to grow capacity and read bandwidth
- partition by date so a job can skip old directories

On cloud object storage you give up classic HDFS locality. Engines such as Spark still work; they pull data over the network, so file size and partitioning matter more.

---

## HDFS vs object storage

| | HDFS | Object (S3) |
|--|------|-------------|
| Access | Hadoop clients, data locality | HTTP API |
| Best at | Cluster-local batch | Shared lake, multi-service, cheap archive |
| Small files | Hurts the NameNode | Hurts request cost and listing |
| Operations | You run the cluster | The cloud runs it, or you run MinIO / Ceph |

Many lakes use S3 (or both): land files in the object store, process with Spark, and keep HDFS only if the cluster is still on-premises.

---

## Interview lines

- ext4/NTFS are one machine. HDFS, CephFS, and GlusterFS spread files across machines.
- HDFS: NameNode metadata, DataNode blocks, usually 3 copies, large blocks, batch throughput.
- CephFS is POSIX and part of a unified Ceph cluster. GlusterFS is the simpler commodity file cluster.
- Columnar formats and partitioning matter as much as the file system.

---

## Interview Q&A

### 1. How is a distributed file system different from a traditional one?

<details>
<summary>Show Answer</summary>

**Answer:**

ext4, XFS, and NTFS manage files on one computer. A DFS (HDFS, CephFS, GlusterFS) stripes and replicates data across many machines so capacity and read bandwidth grow with the cluster, and a single disk failure does not lose the file.

</details>

### 2. How does HDFS work?

<details>
<summary>Show Answer</summary>

**Answer:**

The NameNode stores the file map. DataNodes store blocks, commonly 128 MB or 256 MB, usually with 3 replicas. Clients ask the NameNode where a block is, then read the bytes from a DataNode. Heartbeats detect dead nodes, and under-replicated blocks are copied again. Run two NameNodes (active and standby) so metadata is not a single point of failure.

</details>

### 3. Latency vs throughput in these systems?

<details>
<summary>Show Answer</summary>

**Answer:**

Local and block storage have low latency for small random I/O. HDFS and object storage have higher latency and much higher throughput for large sequential or parallel reads. Match the tool to the job: database pages on block storage, batch scans on HDFS or a data lake.

</details>

### 4. CephFS vs GlusterFS vs HDFS?

<details>
<summary>Show Answer</summary>

**Answer:**

HDFS targets write-once analytical workloads and Hadoop/Spark. CephFS is a POSIX file system on Ceph, which also offers object and block. GlusterFS is a simpler scale-out POSIX store on commodity servers, without HDFS's NameNode model.

</details>

### 5. How do you scale an analytics workload?

<details>
<summary>Show Answer</summary>

**Answer:**

Use large columnar files (Parquet or ORC), partition by time or tenant, and add nodes so reads happen in parallel. On HDFS, schedule compute near the replicas. On object storage, keep jobs in the same region and avoid a flood of tiny files.

</details>

**Q&A recap:** one-disk file systems for one machine; a DFS when the dataset and the readers no longer fit that machine.

## Interview Q&A — 6 to 8 years

### S1. Two app servers write the same file on a shared drive. What do you expect to go wrong?

<details>
<summary>Show Answer</summary>

**Answer:**

A file system lock is easy to get wrong across machines, and a torn write is easy to read. I prefer one writer, or I write a new file and atomically rename it into place. Readers then see the old file or the new file, not a half-written one. If I need many writers and a real lock, I use a database or a coordination service, not a hope that the share's lock works over a blip.

**Corner cases:** NFS close-to-open consistency: a reader on another machine keeps a stale cache. A lock file left behind by a crashed process blocks everyone until a human deletes it. I use a lease with an expiry. Two regions mounting one volume is a split-brain waiting to happen. A backup that copies a file while it is being written stores the torn version. Quotas fill up at 2 a.m. and every node fails together because they share one disk.

</details>

### S2. HDFS-style storage is cheap for analytics and a bad place for the product API. Why?

<details>
<summary>Show Answer</summary>

**Answer:**

It wants large sequential reads and writes, big blocks, and batch jobs. The name node (metadata) is a special box. Small files overwhelm it. Latency is not what a user-facing GET wants. I land events as large columnar files and I query them offline. The product path stays on a database and an object store with a key lookup.

**Corner cases:** A million tiny JSON files, one per event, melt the metadata service. I compact them. A job that lists every file in a huge directory as its first step. A single metadata master: if it is down, the data may be safe and still unusable. I mention a standby. Replication factor three costs 3× disk. Dropping to two to save money means one more disk failure loses data. A compute job scheduled on the same disks as the live ingest, so ingest latency dies at report time.

</details>

### S3. How do you design the path from "a file landed" to "the job may read it"?

<details>
<summary>Show Answer</summary>

**Answer:**

The writer finishes the file, fsyncs, then publishes a manifest or renames from `file.tmp` to `file`. The job only reads committed names. A late reader must not see a partial file. I include a checksum in the manifest. If the job fails halfway, it can re-read the same immutable file. I do not append to a file a job might be reading.

**Corner cases:** A rename that is atomic on one file system and not on the distributed one I am actually using. I check. A clock-based folder (`/2026/11/07/14/`) written by two hosts with skewed clocks, so "hour 14" is missing rows or has them twice. A consumer that starts on an empty file because the create beat the write. Exactly the tmp-then-rename bug. Schema changes inside a directory of files. New files can be new schema. Old files stay. The job must read both.

</details>

### S4. When do you still pick a plain local disk?

<details>
<summary>Show Answer</summary>

**Answer:**

Scratch space, a local cache, a database engine that wants a real disk, and anything that must not depend on the network for every byte. I treat local disk as ephemeral if the machine can be replaced. Anything I cannot lose is replicated somewhere else before I ack. I do not use local disk as the only copy of an upload "for now."

**Corner cases:** A container that writes to a layer that vanishes on restart. A host disk that fills with logs and kills the database on the same disk. I separate volumes and I alert on free space before 100%. Instance store that is fast and wiped on stop. People put the only replica there. RAID is not a backup. It will faithfully store a bad delete on every mirror.

</details>

**Senior recap:** rename-into-place instead of shared writers, big files for distributed file systems, commit then publish, and local disk is scratch unless you replicated it.

Next: [06_Big_Data.md](06_Big_Data.md)
