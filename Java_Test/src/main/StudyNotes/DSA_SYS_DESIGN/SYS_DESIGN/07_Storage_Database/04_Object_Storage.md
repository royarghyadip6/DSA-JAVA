# 4. Object Storage

## Simple idea

An **object** is a blob plus a key plus metadata. You do not get folders that the disk understands. You get an API:

```text
PUT    bucket / photos/2026/user42/a.jpg
GET    that same key
DELETE that key
```

Amazon S3 is the reference design. Google Cloud Storage, Azure Blob Storage, MinIO, and Ceph (RADOS) follow the same idea. MinIO and Ceph can run in your own data centre.

---

## What is inside an object

| Part | Example |
|------|---------|
| Key | `videos/abc/720p.mp4` |
| Data | The bytes |
| Metadata | Content type, size, custom tags, checksum |

A **bucket** is the container. Keys can look like paths (`a/b/c.jpg`). That is a prefix, not a real directory tree.

---

## Where it fits

| Workload | Why object storage |
|----------|--------------------|
| User photos and video | Huge files, infrequent updates |
| Backups and archives | Cheap, durable, versioned |
| Data lakes | Land raw files, query later |
| Static websites | HTML, CSS, images behind a CDN |
| IoT and ML datasets | Many large files, parallel reads |

It is a weak choice for a database page file or for an app that rewrites the same small file thousands of times per second. That wants block storage.

---

## Latency vs throughput

- **Latency** — time to the first byte. Object storage is higher latency than a local disk or block volume because of the HTTP API and the distributed backend.
- **Throughput** — total bytes per second. Object storage wins when you read or write many large objects in parallel.

So: slow to open one tiny file, excellent at streaming a video or scanning a petabyte of logs.

---

## Consistency

Older S3 teaching said **eventual consistency**: a read right after an overwrite or delete might see the old object.

**Today, Amazon S3 is strongly consistent** for new PUTs, overwrites, and deletes. A read after a successful write returns the new object. The course slides still list eventual consistency as the tradeoff. In an interview, say the historical model, then say current S3 gives strong read-after-write consistency. Other object stores may still differ. Do not assume every S3-compatible API matches AWS.

Object storage still does not give you a transaction across many keys. Updating `video.mp4` and `video-metadata.json` can succeed for one and fail for the other.

---

## Storage classes and lifecycle

| Class (AWS names) | Idea | Typical use |
|-------------------|------|-------------|
| Standard | Fast, higher price | Hot images and site assets |
| Standard-IA / infrequent access | Cheaper storage, retrieval fee | Backups touched rarely |
| Glacier / Deep Archive | Very cheap, minutes to hours to read | Compliance archives |

A **lifecycle rule** moves or deletes objects by age:

```text
logs/  for 30 days     → Standard
then 60 days           → Infrequent Access
then 1 year            → Glacier
then delete at 7 years
```

---

## YouTube-style video design

```text
Upload API
   → store original under videos/{id}/original.mp4
   → transcode to 360p, 720p, 1080p as separate objects
   → CDN in front of the bucket
   → client gets a short-lived presigned URL, not a public bucket
```

Prefixes (`videos/{id}/`) keep one video's renditions together for listing. The CDN caches the popular renditions so users do not all hit the bucket.

---

## Petabyte logs

- write compressed objects (gzip or a columnar file) under `logs/date/hour/`
- lifecycle from Standard → IA → Glacier → Deep Archive
- **versioning** if you must recover an overwrite
- **object lock** (WORM) if a regulator says the file cannot be deleted early
- do not use the bucket as a database of individual log lines. Query with Athena, Spark, or similar

---

## Access control

| Mechanism | What it does |
|-----------|----------------|
| **IAM** | Who in the cloud account may call the bucket |
| **Bucket policy** | Rules on the bucket itself (which account, which prefix, which IP) |
| **Presigned URL** | A temporary link so a browser or another service can upload or download without your keys |
| **SSE-S3 / SSE-KMS** | Server-side encryption. KMS lets you control and audit the key |
| **CloudTrail** (AWS) | An audit log of who called the API |

Microservices should not share one long-lived access key. Give each service a role, and pass data with a presigned URL or by writing to a prefix that only that role can read.

---

## Cost

You pay for:

- **storage** (GB-month, class matters)
- **requests** (PUT, GET, LIST)
- **egress** (bytes leaving the cloud region)

Listing millions of keys and downloading them to another region are common surprise bills. Keep analytics in the same region as the bucket.

---

## Interview lines

- An object is bytes + key + metadata, accessed by API, stored in a bucket.
- Great for large, write-once, read-many data. Poor as a low-latency disk.
- Current S3 is strongly consistent for a single object. Multi-object transactions are still your problem.
- Lifecycle rules and storage classes control cost. Presigned URLs and IAM control access.

---

## Interview Q&A

### 1. What is object storage?

<details>
<summary>Show Answer</summary>

**Answer:**

A scalable store for unstructured data. Each object is the payload, a unique key, and metadata. You access it through an HTTP API, not through a mounted filesystem. S3, GCS, Azure Blob, MinIO, and Ceph are examples.

</details>

### 2. Where is it used in modern systems?

<details>
<summary>Show Answer</summary>

**Answer:**

Media, backups, data lakes, static sites, IoT dumps, and ML datasets. Anywhere you store large blobs and read them by key.

</details>

### 3. Latency vs throughput?

<details>
<summary>Show Answer</summary>

**Answer:**

Latency is higher than block or local disk because of the API and the distributed backend. Throughput is high when many clients move large objects in parallel. Use it for bulk data, not for database pages.

</details>

### 4. Consistency guarantees?

<details>
<summary>Show Answer</summary>

**Answer:**

Older designs, and many course slides, say eventual consistency on overwrite and delete. Amazon S3 now provides strong consistency for PUT, overwrite, and DELETE of a single object. You still do not get a transaction across several keys, and other vendors may differ.

</details>

### 5. Storage tiers and lifecycle policies?

<details>
<summary>Show Answer</summary>

**Answer:**

Tiers trade access speed for price: Standard, infrequent access, then archive (Glacier, Deep Archive). A lifecycle policy moves or expires objects by age so old logs do not stay on the expensive tier.

</details>

### 6. YouTube-scale video storage?

<details>
<summary>Show Answer</summary>

**Answer:**

Store each resolution as its own object under a video-id prefix. Put a CDN in front. Hand clients presigned URLs. Transcode asynchronously after the original upload lands.

</details>

### 7. Petabyte-scale logs?

<details>
<summary>Show Answer</summary>

**Answer:**

Compress and partition by time in the key. Move cold data down the lifecycle to archive tiers. Add versioning or object lock when you must keep or prove the files. Query in place instead of downloading everything.

</details>

### 8. How do microservices share object storage safely?

<details>
<summary>Show Answer</summary>

**Answer:**

Separate IAM roles per service, bucket policies on prefixes, presigned URLs for browser upload and download, encryption with SSE-S3 or SSE-KMS, and audit logs such as CloudTrail.

</details>

### 9. How do you secure access?

<details>
<summary>Show Answer</summary>

**Answer:**

IAM and bucket policies decide who can call the API. Presigned URLs grant temporary access. SSE-KMS encrypts data and lets you audit key use. Block public access unless a prefix is intentionally public, such as a website asset behind a CDN.

</details>

**Q&A recap:** object storage is the durable blob shelf. Keys, tiers, and short-lived URLs are the design.

## Interview Q&A — 6 to 8 years

### S1. A 20 GB upload fails at 90%. How do you design the retry?

<details>
<summary>Show Answer</summary>

**Answer:**

I do not restart the 20 GB. I upload parts (multipart). Each part has an id and a checksum. A retry sends only the missing parts. I complete the object only when every part is present, then I write the metadata row in my database. If I crash after the object exists and before the metadata write, a sweeper finishes or deletes the orphan. The client uses an idempotency key so two completes do not create two files.

**Corner cases:** Parts expire if I wait too long to complete (a user leaves a tab open). I state the lifetime. A complete that times out and is retried must return the same object, not an error that makes the client start over. Checksums catch a corrupt part. Without them I store silent damage. A proxy body limit smaller than one part. I size parts under that limit. Concurrent uploads of the same key. Last complete wins unless I use a unique key per attempt and flip a pointer.

</details>

### S2. You handed the client a pre-signed URL. What can go wrong after it leaves your API?

<details>
<summary>Show Answer</summary>

**Answer:**

Anyone with the URL can use it until it expires. I keep the life short (minutes for upload, a bit longer for a download the user already authorized). I bind the method and the content type. I do not put the URL in logs or referrer-leaking pages. Revocation is hard. If I need to kill access now, I use a URL that hits my service, or I rotate the object key.

**Corner cases:** Clock skew makes a valid URL look expired. A CDN caches a signed URL response and serves the bytes after I wanted them private. I set `Cache-Control: private` and a short TTL, or I do not put private objects on a shared cache. A URL in a chat transcript lives in backups. Least privilege: the signature allows GET on one key, not ListBucket. Listing is a common over-grant.

</details>

### S3. How do you delete an object when the law or the user says "forget me"?

<details>
<summary>Show Answer</summary>

**Answer:**

I delete the object, every version if versioning is on, and the metadata row. I also delete derived copies: thumbnails, CDN edges, search snippets, and warehouse files if they contain the bytes or the personal link. I put a tombstone so a late replication or a retry does not put the object back. Backups age out by policy, and I can say how long a backup may still hold the blob.

**Corner cases:** Versioning plus a lifecycle rule that I forgot, so the "deleted" object is still current in a noncurrent version. A CDN purge that fails in one region. An immutable retention lock (compliance mode) that cannot delete before the retain-until date. I must not promise instant forget on that bucket. A database replica that still has the key, and a worker that "repairs" by re-copying from a region that has not seen the delete. Delete must flow in the same direction as replication, with a generation.

</details>

### S4. Storage class saves money and breaks the product. Which corner do you mention?

<details>
<summary>Show Answer</summary>

**Answer:**

Cold tiers have slow first-byte time and retrieval fees. I do not put the image on a product page in deep archive. I age objects by last use: hot for recent, infrequent, then archive for compliance. A lifecycle rule does this. Restoring an archive is async. The UI must say "preparing" and not assume a GET works. Small files in a class with a minimum billable size can cost more, not less.

**Corner cases:** A rule that archives a prefix including a pointer object I still need for every request. A GET on an archived object that looks like a 500 to the app and gets retried into a bill. Cross-region replication of a cold object that thaws on the other side and doubles the fee. Versioning that keeps hot copies of every overwrite so the lifecycle never shrinks the bill. I expire noncurrent versions.

</details>

**Senior recap:** multipart with checksums, short signed URLs that grant one action, deletes that reach every copy, and cold storage only for data that can wait.

Next: [05_File_Systems_and_Distributed_Storage.md](05_File_Systems_and_Distributed_Storage.md)
