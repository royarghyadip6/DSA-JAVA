# 13. Collaborative Documents (Google Docs)

## Simple idea

Several people edit one document at the same time. Each person sees the others' keystrokes quickly, and everyone ends on the **same text**, even if two people typed in the same spot.

---

## Requirements

Create, edit, and delete text documents. Live collaboration. Version history. Permissions (read or edit). Invite by link or email.

**Qualities:** sync in under about **100 ms**, millions of documents, **99.9%** uptime, reconnect after a blip, TLS, autosave, basic formatting only for the MVP.

**Assumptions:** modern browsers, mostly text, about **2–50 people** in a document, edits all day. The hard latency is global, not only inside one office.

---

## Numbers

| | Figure |
|--|--------|
| DAU | 10 million+ |
| Documents | about 100 per user |
| Concurrent editors | 200,000+ at peak |
| Events | about **10 billion a day** (on the order of 100,000 per second average) |
| While editing | about 5–20 operations per second per busy document |
| Size | about 100 KB per document, a few times more once you keep history |

Traffic spikes in office hours. A few documents (the meeting notes) are hot. Writes are small and constant. Permission checks sit on the connect path.

---

## Operations, not whole files

Do not POST the entire document on every key. Send an **operation**: "insert `a` at index 12" or "delete 3 characters at index 4".

Two people at the same index will conflict. Two tools fix that:

| | Operational transformation (OT) | CRDT |
|--|----------------------------------|------|
| Idea | The server rewrites the later operation so it still makes sense after the earlier one | The data type merges by itself, without a single referee |
| Needs | A server that orders operations. This is the classic Docs approach | More CPU and a careful data type (Yjs, Automerge). Better offline |
| Interview default | Easiest to explain if a server is always in the path | Say it if the interviewer requires offline edits |

```text
Alice inserts X at 0
Bob inserts Y at 0
Server applies Alice first → "X"
Bob's operation is shifted to index 1 → "XY"
Both clients apply the same ordered result
```

Clients must converge even if a message arrives late. Each operation has a document version. The server rejects or transforms anything based on a stale version.

---

## Design

```text
Editor in the browser
  WebSocket /ws/collab?doc_id=&token=
  → collab service (OT or CRDT, in memory, fan-out to the 2–50 sockets)
  → periodic snapshot to durable storage
  → version service keeps snapshots plus the op log

REST
  POST /documents
  GET  /documents/{id}
  POST /documents/{id}/operations     optional if you are not only on the socket
  GET  /documents/{id}/history
  POST /documents/{id}/collaborators
```

Check permission on connect and on save. A share link is a grant, not a public bucket.

Fan-out is easy because a document has dozens of people, not a million. The hard part is **CPU for transforms** and **write volume of the op log**. Compact the log into a snapshot every N operations so a reconnecting client does not replay a year of keystrokes. They load the snapshot, then the ops after it.

Pub/sub (Redis or Kafka) only if the people on one document are spread across more than one collab node. Prefer to **pin a hot document to a node** so ordering is local. If that node dies, clients reconnect, load the last snapshot, and continue.

PostgreSQL for metadata and ACLs. The document body can be a blob in object storage or a document store. Kafka for "snapshot written" and audit. TLS in transit. Encrypt the stored document at rest.

99.9% and a reconnect protocol matter more than a second region. The user's unsent keystrokes should sit in the browser until the socket returns.

---

## Final design

Typing does not go through a slow save. It goes to the WebSocket and the sync service, which merges two people's edits. The document file is saved on a timer by the versioning service. That is why the picture has a live path and a storage path.

```text
Web / mobile editors
        |                         ^ keystrokes of other people
        v                         |
API Gateway                  WebSocket servers
 (auth, rate limit)           (who is in this document)
        |
        +--> User service          (who, and which role)
        +--> Document service      (create, load, metadata)
        +--> Collaboration service (who may edit right now)
        +--> Sync service          (OT or CRDT, the live merge)
        +--> Versioning service    (snapshots and history)
        |
        +--> Redis (presence, hot session)
        +--> queue --> workers that write the slow copies
        |
        +--> PostgreSQL     users, roles, permissions
        +--> document store body, comments
        +--> object storage snapshots and attachments
```

The socket and the sync service are the live path. The snapshot is the recovery path.

## Interview lines

- Send operations, not the whole file.
- OT if one server orders edits. CRDT if you need offline merge. Pick one and walk through a conflict.
- Snapshot plus a short tail of ops. Pin the live document to a node.
- Permissions on the socket, not only on the share dialog.

---

## Interview Q&A

### 1. What do clients send?

<details>
<summary>Show Answer</summary>

**Answer:**

Small operations: insert or delete at a position, tagged with the version they were based on. The collab service transforms or merges them and broadcasts the result to the other sockets on that document. A full-text save happens on a timer as a snapshot. That keeps the live path under about 100 ms and keeps history as a log you can replay.

</details>

### 2. Two people type in the same place. Who wins?

<details>
<summary>Show Answer</summary>

**Answer:**

Neither overwrites the other blindly. With OT, the server orders the operations and shifts the later one's index so both characters survive. With a CRDT, the structure merges without a central order, which helps if both laptops were offline. Every client applies the same resolved operations, so the document converges. Say which one you picked. For an always-online MVP, OT or a single ordered log is the shorter design.

</details>

### 3. How does someone who disconnected catch up?

<details>
<summary>Show Answer</summary>

**Answer:**

They load the latest snapshot and the operations after that version, then open the socket again. You do not ship the entire op history. Compaction bounds that tail. Permission is checked again on reconnect. A dead collab node loses only the ops not yet snapshotted, so the snapshot interval is your recovery point. Clients also keep a short local buffer.

</details>

**Q&A recap:** ops, a transform or a CRDT, snapshots, pin the document.

## Interview Q&A — 6 to 8 years

### S1. Two people type in the same word offline. What do you promise when they reconnect?

<details>
<summary>Show Answer</summary>

**Answer:**

Both edits survive in a defined way. I do not pick a winner by wall clock. Operational transform adjusts indexes as ops arrive. A CRDT merges by structure. I say which one I use and what the user sees (both characters, a deterministic order). The server stores an ordered log of ops plus snapshots so a client does not replay ten years. A client sends an op id so a retry is not a second insert.

**Corner cases:** A client applies its own op locally and then applies the server's transformed op again, doubling the character. Each op has an id and the client dedupes. Two servers accept ops during a partition and later both have "the" document. I need a single sequencer per document, or a CRDT that can merge without a sequencer. I name that choice. A paste of a 20 MB string as one op that blocks the log. I cap op size. Cursor positions that point at the wrong place after a transform. The product bug users notice. I test the "insert at the same index" case out loud.

</details>

### S2. Why is a snapshot not enough, and why is the full log not enough?

<details>
<summary>Show Answer</summary>

**Answer:**

A snapshot is a fast load and a bound on replay. It is useless for a client that was offline and holds a concurrent edit, unless I still have the ops after the snapshot base. The full log is the truth and is too slow and too big to send to every open. I keep a snapshot every N ops or every few minutes, and I keep the ops after the latest snapshot for a window. Older ops can be compacted once no client needs them, or I keep them for history if the product has version history.

**Corner cases:** A snapshot written while an op is half-applied, so the snapshot and the log disagree. I snapshot at a log offset, atomically recorded. A client whose version is older than the compacted horizon. I force a full reload from the snapshot and I discard the client's unsent ops or I try to rebase them. I state which. A snapshot in object storage and a log in a database that restore to different times. I record the log offset inside the snapshot and I refuse a mismatched pair. Compaction that deletes an op a slow client still needed for undo. Undo is an inverse op in the log, not a local-only edit, if other people are present.

</details>

### S3. Presence and comments are in the same system. What do you deliberately not order?

<details>
<summary>Show Answer</summary>

**Answer:**

The document ops are ordered per document. Cursors, "user is viewing," and typing are best-effort and can be dropped. Comments can be their own log. I do not block a keystroke on a presence write. Permissions are checked on each op: a user removed mid-session can be disconnected and further ops are rejected. I do not rely on a cached "they were allowed at connect."

**Corner cases:** A permission revoke that races an in-flight op. The op may land once. I check auth at apply time and I compensate with an inverse op if the product requires it, or I accept one op after revoke. I say which. A comment anchored to a character index that moves when the paragraph is edited. I anchor to a stable id, not a raw index. A presence flood that costs more than the document. I throttle presence to a few updates a second per user. Two tabs from one user that fight as two authors. I treat them as two clients with one user id, both allowed, both ordered.

</details>

### S4. How do you recover a document that a bug corrupted an hour ago?

<details>
<summary>Show Answer</summary>

**Answer:**

I reload an earlier snapshot plus ops up to the last good offset, and I cut the bad ops. That is a new document version, not a silent rewrite of history clients still hold. Connected clients are told to reload. Offline clients that still have the bad ops must rebase or drop them. I keep the bad log aside for the postmortem. I do not "fix" the document by editing the database row without a new snapshot id.

**Corner cases:** A client that keeps sending ops based on the corrupt version and reintroduces the bug. The server rejects ops whose parent version is the bad one. A backup of the database that is missing the object-storage snapshot it points at. I test restore. A collaborative session during restore that writes to the old sequencer. I fence the document id (a generation) so old servers cannot append. Large documents where snapshotting locks the log for too long. I snapshot asynchronously and I record the exact offset. Encryption keys for a private doc: the snapshot is useless if the key was rotated without keeping the old key for that blob. Same overlap rule as any key rotation.

</details>

**Senior recap:** one sequencer or a real CRDT, snapshots tied to a log offset, presence is droppable, and recovery is a new version that old clients cannot append to.

Next: [14_Interview_Mindset.md](14_Interview_Mindset.md)
