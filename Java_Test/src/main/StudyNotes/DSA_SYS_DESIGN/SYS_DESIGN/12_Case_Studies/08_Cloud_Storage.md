# 8. Cloud Storage (Drive / Dropbox)

## Simple idea

People upload files, see them on every device, share them, and get an old version back. The bytes are huge. The **metadata** (name, folder, who can open it) is what your API actually queries.

---

## Requirements

Upload and download any type. Sync across web, phone, and desktop. Folders and tags. Share links and shared folders. Versions and rollback. Trash, then restore.

**Qualities:** millions of users, petabytes, fast transfer, encryption and access control, cost control, visibility into sync lag.

The slides ask for **eleven nines of durability** (99.999999999%). That is a storage-durability target, the kind object storage advertises. It is **not** the same as uptime. Availability is a separate, smaller number. You still need a metadata tier that stays up.

**Constraints:** files up to about 5 GB, so uploads are **chunked and resumable**. Sync visible in under about 5 seconds. Dedup and lifecycle rules to save money. Permissions fine enough for "this person can edit this folder".

The slides also say end-to-end encryption **and** deduplication. Those fight. Dedup across users means the server sees the bytes, or at least a hash of them. True end-to-end encryption means the server cannot read the file, and cross-user dedup mostly goes away. Pick one for the interview. Server-side encryption with KMS matches dedup and sharing. End-to-end matches a privacy product and makes search and dedup a client problem.

---

## Numbers

| | Figure |
|--|--------|
| Users | 10 million active |
| Files | about 500 each → about **5 billion** files |
| Average size | 2 MB → about **10 PB** |
| Peak uploads | about 2,000 per second |
| Sync events | about 10,000 per second |

Both directions hurt. Uploads are big writes. Folder listings are small, frequent reads. Shared links add anonymous traffic.

A **single metadata database** becomes the hotspot. Large uploads time out if they are one request. Permission checks on every file can dominate. Cache public shares at the edge.

---

## Design

```text
POST /upload/initiate          → upload id
PUT  /upload/{id}/chunk        → one chunk + checksum, retry safe
POST /upload/{id}/complete     → assemble, then version and sync events

GET  /files/{id}               → download, after an ACL check
GET  /files/{id}/metadata
POST /files/{id}/share
GET  /files/shared/{token}
GET  /sync/updates             → push (WebSocket) or long poll
```

```text
Upload service     chunks, resume
Metadata service   tree, owner, ACL, version ids     (partition by user)
Storage service    object storage (S3, MinIO)
Dedup              hash a chunk, store it once if you can see plaintext
Versioning         new version when the hash changes
Sync service       "this folder changed" to other devices
Auth               every call
```

Chunks of about 5 MB. Each chunk has an id and a checksum. If the laptop sleeps, the client continues at the next missing chunk. Complete is idempotent.

A version is metadata plus the chunk list, not a full copy of unchanged chunks. Restore points at an old version id.

Sync: after commit, publish an event. Connected devices get it over a WebSocket. Others learn on the next poll. A sync cursor (last event id per device) stops duplicates.

Cache metadata in Redis. Cache hot downloads on a CDN. Invalidate when the file version changes. Do not CDN a private file without a short-lived signed URL.

PostgreSQL for users, ACL, and the folder tree, sharded by owner when one database is hot. A document store only for flexible extra attributes. Object storage for bytes. Kafka for "file committed" and post-processing (thumbnail, virus scan).

---

## Diagrams from the slides

The first picture answers "which database?" Structured facts go to SQL. Huge or flexible lists go to NoSQL. The file bytes go to object storage, which is the right side of the second picture. Follow an upload from the person, through a queue, into storage.

**Where each kind of data sits**

```text
                    Data type?
                    /        \
            relational        high volume
                 |                 |
                SQL              NoSQL
           /    |    \        /    |    \
        Users Metadata  Permissions  Chunks Versions Logs
           ACL
```

Users, folder metadata, and permissions are the SQL side. Chunk rows, version blobs, and a firehose of audit logs are the NoSQL side. The bytes themselves are not in either database. They are objects.

**Final picture**

```text
Web / phone / desktop
        |
        v
API Gateway ---- Auth service (JWT, RBAC)
        |
        +--> Upload queue --> Upload service ----+
        +--> Sync queue   --> Sync service ------+--> Storage service
        +--> Dedup queue  --> Dedup service -----+      |
        +--> Version queue -> Versioning service +      +--> Redis (hot chunks)
        |                                         |      +--> Object storage
        +--> Metadata service --> metadata cache --> metadata DB
                                                      (+ a SQL backup)
        |
        v
Event bus --> background workers (upload, sync, dedup, version, metadata)
        |
        v
Notification service (mail, push)          CDN serves completed files
```

## Interview lines

- Metadata and bytes are different systems.
- Resumable chunks with checksums. Complete is idempotent.
- Durability is eleven nines of not losing a stored object. That is not an uptime SLA.
- Say whether the server can read the file. Dedup and end-to-end encryption are not both free.

---

## Interview Q&A

### 1. How do large uploads work?

<details>
<summary>Show Answer</summary>

**Answer:**

Initiate a session, send chunks of about 5 MB with checksums, retry only the failed chunk, then call complete. The storage service assembles the object. Metadata records the version and the chunk list. A dropped network resumes from the last acknowledged chunk. Complete can be called twice and must not create two files.

</details>

### 2. How do other devices find out?

<details>
<summary>Show Answer</summary>

**Answer:**

The commit publishes a change event. The sync service pushes it to devices that hold a WebSocket, within a few seconds. Others poll with a cursor. Clients apply the new metadata and download only what changed. Permission is checked on the download, not only at share-link creation.

</details>

### 3. Where do the bytes live, and what is encrypted?

<details>
<summary>Show Answer</summary>

**Answer:**

Objects live in S3-class storage, replicated by that service. Metadata lives in a partitioned database. Server-side encryption with KMS protects disks and still lets you dedup by hash and share a folder. End-to-end encryption hides content from you and makes cross-user dedup and server search impractical. The slide's eleven nines refer to not losing the object, not to the site never being down.

</details>

**Q&A recap:** chunk the upload, query the metadata, push the sync, do not confuse durability with uptime.

## Interview Q&A — 6 to 8 years

### S1. The client uploads 8 of 10 chunks and disappears. What is stored, and what can resume?

<details>
<summary>Show Answer</summary>

**Answer:**

The 8 chunks are temporary objects with a checksum and an upload id. They are not a visible file. Resume asks for which parts are missing and sends only those. Complete writes the metadata row only when every part's checksum matches. A sweeper deletes uploads older than a limit. A retry of complete is idempotent and returns the same file id.

**Corner cases:** Two devices resume the same upload and one sends a different chunk 3. I reject a checksum mismatch or I version the upload attempt. A complete request that times out after the object was assembled. The client retries and must not see a hard error that starts a second file. I key complete by upload id. A chunk list in memory on one server, lost on restart. The list is in the database. Quota checked only at the start, so a user goes over quota on complete. I check again on complete and I fail without publishing the file. Hashing a huge file on the client that does not match the server's hash because of a proxy that altered bytes. I hash the stored object.

</details>

### S2. Two users edit the same file offline. What do you show when they both sync?

<details>
<summary>Show Answer</summary>

**Answer:**

I do not silently keep one. Each write creates a new version or a new object key. The metadata row points at the current version with a compare-and-set (update where version = the client's base version). If the base is stale, I return a conflict and I keep both versions. The product can auto-merge only if I know the file type. For an opaque blob I keep both and let the user choose.

**Corner cases:** Last-write-wins that deletes a user's work with no error. I mention that as the choice I am rejecting for shared folders. A folder rename on one device and a file add on the other. Path conflicts need the same version rule on the directory entry, not only on file bytes. A sync client that retries a failed metadata update with the old version and loops on 409. It should refetch. Clock-based "newest file" across devices with bad clocks. Server versions, not device clocks. A delete that races an edit. I keep a tombstone version so the edit does not resurrect a deleted file unnoticed, or I surface the conflict. I state which one.

</details>

### S3. How do you share a folder without copying the bytes into the other user's account?

<details>
<summary>Show Answer</summary>

**Answer:**

The object lives once. An access row says user B may read (or write) object or folder id. Download checks that row, then issues a short-lived signed URL or proxies the read. Revoke deletes the access row and stops new URLs. Already issued URLs live until they expire, so I keep that expiry short. I do not copy the blob on share, or a revoke cannot work and I pay double storage.

**Corner cases:** A signed URL leaked into logs or a chat, valid until expiry. I cannot revoke it instantly unless I rotate the object key. A child path that inherits a folder grant, and a move of the file out of the folder that should drop access. I check the current parent, or I store grants on the file and I recompute on move. A public link that outlives the employee's account. I tie the link to an owner and I disable it when the owner is disabled, if that is the policy. Listing a shared folder with a query that leaks sibling names. Authorization on every list entry. A share to a group that is huge. I do not copy the file id into a million permission rows on the hot path without a plan. Group membership is looked up, with a cache I can purge.

</details>

### S4. The metadata database is up and object storage is slow. What does the client experience?

<details>
<summary>Show Answer</summary>

**Answer:**

The file list still loads, because names and versions are metadata. Download and upload fail or stall. I set timeouts on blob calls and I retry a chunk, not the whole file. Durability (the blob is stored on several disks) is not the same as availability this minute. I show "stored, not reachable right now" rather than "deleted." I do not create a second metadata row because a download timed out.

**Corner cases:** A metadata commit without a successful complete, so the list shows a file whose bytes 404. Complete must be ordered: bytes first, then metadata, with a sweeper for the opposite orphan. A region failover of blobs that is ahead of metadata, so metadata points at a key that has not replicated. I store a region and a status on the row. A client that caches a signed URL past expiry and reports a generic network error. The client refreshes the URL once. Sync storms when a laptop wakes and sends every file. I limit concurrency per device and I use the version check so unchanged files are a cheap metadata call.

</details>

**Senior recap:** resumable chunks with checksums, compare-and-set versions so offline edits do not vanish, shares as access rows plus short URLs, and metadata availability separate from blob availability.

Next: [09_Video_YouTube.md](09_Video_YouTube.md)
