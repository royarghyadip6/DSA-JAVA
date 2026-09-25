# Kafka Senior Interview (5–8 Years) — Spring Kafka + ESM / SNA / NE

Use this after [065_Kafka.md](065_Kafka.md). That file covers topics, offsets, acks, duplicates, and DLQ. This file is what a 5–8 year Java interview expects on top of that: request-reply, consumer threading, and real production failures.

Read a question out loud. Hide the answer. Speak for about one minute. Then open the answer and fix the gaps.

---

## How ESM talks to a Network Element

ESM never calls the Network Element (NE) directly. A deploy or a sync goes through Kafka to the SNA adapter. The SNA adapter talks to the NE (CLI or SNMP). The reply comes back on Kafka.

```text
GUI / REST
    → ESM controller (esm-ws)
    → Command / Task chain (esm-server-service)
    → AdapterProxyManager
    → KafkaESMUtil  (Spring ReplyingKafkaTemplate)
    → topic  ESM_ASYNCIF_REQUEST_{snaId}
    → snaadapter-a
    → NE
    → reply topic + correlation id
    → ESM matches the reply, or times out
```

Useful names when you explain a bug:

| Name | Role |
|------|------|
| `AdapterProxyManager` | Picks the SNA proxy for a `snaId` |
| `KafkaESMUtil` | Sends the request and waits for the reply |
| `ESM_ASYNCIF_REQUEST_{snaId}` | One request topic per SNA instance |
| `snaadapter-a` | SNA process that owns NE communication |
| `EsmKafkaListener` | ESM side that reads Kafka replies / events |
| `KafkaResponseTypeValidator` | Allow-list of response classes |
| `AllowedKafkaResponseClassDeserializer` | Rejects a payload whose type is not allowed |
| `KafkaEsmResponseContext` | Ties a reply back to the waiting request |
| `KafkaAsyncThreadWorker` / `KafkaOperationThreadWorker` / `KafkaMepThreadWorker` | Worker pools that run Kafka work |
| `ESMServer.log` | Look for `Kafka Thread` and `Rejected Kafka` |

Kafka gives **order inside one partition** and **at-least-once** delivery. The NE is outside Kafka. A retried message can run the same CLI twice. Your design has to live with that.

---

# Part 1 — Senior interview

---

# 1. How does Kafka request-reply work?

<details>
<summary>Show Answer</summary>

**Answer:**

Normal Kafka is one-way. The producer writes and moves on. The consumer reads later. ESM cannot do that for a deploy. It must know whether the NE accepted the command.

Request-reply adds a matching id:

1. ESM builds a record on `ESM_ASYNCIF_REQUEST_{snaId}`.
2. The record carries a **correlation id** (unique request id) and a **reply topic**.
3. SNA reads the request, talks to the NE, and writes the response to the reply topic with the **same correlation id**.
4. `ReplyingKafkaTemplate.sendAndReceive` waits until that id comes back, or until the timeout.

```java
ProducerRecord<String, NeCommand> record =
        new ProducerRecord<>("ESM_ASYNCIF_REQUEST_" + snaId, neName, command);

RequestReplyFuture<String, NeCommand, NeResponse> future =
        replyingKafkaTemplate.sendAndReceive(record);

NeResponse response = future.get(timeoutSeconds, TimeUnit.SECONDS);
```

Spring Kafka fills `KafkaHeaders.CORRELATION_ID` and `KafkaHeaders.REPLY_TOPIC` when you use `ReplyingKafkaTemplate`. ESM stores the waiting call in `KafkaEsmResponseContext` so the reply listener can complete the right future.

Three clocks matter:

| Clock | What it means |
|-------|----------------|
| Produce ack | The request is stored on the broker |
| SNA + NE time | CLI / SNMP round trip |
| `future.get` timeout | How long ESM will wait before it gives up |

A timeout means “no matching reply arrived in time.” It does not, by itself, tell you whether the NE applied the command. The command may still be running on the NE.

**Interview Point:**

> Request-reply = normal produce + a correlation id + a reply topic + a timeout. The id is how ESM finds its own answer among every other NE reply.

</details>

---

# 2. Why does the message key decide order?

<details>
<summary>Show Answer</summary>

**Answer:**

Kafka orders records **inside one partition only**. Two partitions are two independent logs. Consumers read them in parallel, so the global order is not guaranteed.

The default partitioner hashes the key:

```text
partition = hash(key) % numberOfPartitions
```

Same key always lands on the same partition (until you change the partition count). Different keys spread across partitions.

For a service deploy, the steps must stay in order on one NE:

```text
create port  →  create service  →  activate
```

Give every step the **same key** (`neName` or the service id). They share one partition, so SNA sees them in order.

If step 1 uses key `ne-1` and step 2 uses key `ne-1-svc`, they can sit on different partitions. Step 2 can reach the NE first. The NE then rejects “service before port.”

A null key does not keep order. The sticky partitioner batches null-key records, then may move to another partition. Use a real key whenever order matters.

Changing the partition count changes the hash result. Old key-to-partition mapping breaks until you plan a migration. Do that in a maintenance window, not during a live sync.

**Interview Point:**

> Order is per partition. Same business key = same partition = ordered steps. Different keys = parallel and unordered.

</details>

---

# 3. What is a hot partition, and why do extra consumers not fix it?

<details>
<summary>Show Answer</summary>

**Answer:**

A **hot partition** is one partition that gets far more traffic than the others. One NE, or one SNA, produces most of the records. One consumer is stuck on that partition while other consumers sit idle.

In one consumer group, **one partition is read by only one consumer**. Extra consumers help only when there is a free partition for them.

```text
3 partitions, 5 consumers  →  2 consumers get nothing
1 hot partition, 10 consumers  →  still 1 consumer on that hot partition
```

How you see it:

```bash
kafka-consumer-groups.sh --bootstrap-server <broker> --describe --group <group>
```

Look at **LAG** per partition. One partition with a huge lag and the others near zero is a hot partition.

Ways to cool it:

| Move | When it helps |
|------|----------------|
| More partitions, key by `neName` | Many NEs were crushed onto one partition |
| Fix the key | One bad key (constant `"sna"`) sends every NE to partition 0 |
| Speed up the handler | The NE or the DB is the bottleneck, not the consumer count |
| Separate topics | Sync traffic and deploy traffic stop sharing one clogged log |

Adding a consumer without adding a partition does not increase parallelism.

**Interview Point:**

> Consumers scale up to the partition count. A hot partition stays single-threaded until you split the key or the work.

</details>

---

# 4. What happens in a rebalance, and how do you make it less painful?

<details>
<summary>Show Answer</summary>

**Answer:**

A **rebalance** is the group coordinator giving partitions to consumers again. It runs when a member joins, leaves, crashes, or is kicked for being too slow.

**Eager** assignors (`RangeAssignor`, `RoundRobinAssignor`) revoke **every** partition, stop the world, then assign again. In-flight work on healthy partitions stops too. During a full NE sync that is a storm: every in-flight command is abandoned and later retried.

**Cooperative sticky** (`CooperativeStickyAssignor`) revokes only the partitions that must move. The rest keep consuming. Sticky means a consumer keeps the partitions it already had, so a restart does not shuffle the whole group.

```java
props.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG,
        CooperativeStickyAssignor.class.getName());
```

**Static membership** uses `group.instance.id`. If that instance restarts inside `session.timeout.ms`, the coordinator keeps its partitions. A rolling restart of ESM or SNA then does not rebalance the whole group.

```java
props.put(ConsumerConfig.GROUP_INSTANCE_ID_CONFIG, "esm-reply-listener-1");
```

Still finish or safely retry any record you already polled. A rebalance does not roll back work you already sent to the NE.

**Interview Point:**

> Eager rebalance stops everyone. Cooperative sticky moves only what it must. Static membership lets a quick restart keep its partitions.

</details>

---

# 5. Why does a slow handler get the consumer kicked?

<details>
<summary>Show Answer</summary>

**Answer:**

Two different timers. People mix them up.

| Config | Who checks it | What failure looks like |
|--------|----------------|-------------------------|
| `heartbeat.interval.ms` / `session.timeout.ms` | Background heartbeat thread | Process dead, network cut, heartbeat thread stuck |
| `max.poll.interval.ms` | Time between `poll()` calls | Your code is still inside the last poll, processing |
| `max.poll.records` | Records returned by one `poll()` | How much work you pick up before the next `poll()` |

Since Kafka 0.10.1 the heartbeat runs on a **background thread**. A slow `process()` method can keep heartbeats flowing and still break `max.poll.interval.ms`. The coordinator decides the consumer is stuck, removes it, and rebalances. Uncommitted records are delivered again.

Default `max.poll.interval.ms` is 5 minutes. An NE CLI that runs longer than that, on the poll thread, kicks the consumer.

```text
poll() → handle NE command for 8 minutes → next poll() is late
       → kicked → rebalance → same command delivered again
```

What you set in production:

- Keep `max.poll.records` small when each record can call an NE (for example 10–50, not 500).
- Do not raise `max.poll.interval.ms` to 30 minutes as the first fix. That only hides a blocked poll thread and makes a real crash take 30 minutes to detect.
- Move NE work off the poll thread (question 6) so `poll()` stays frequent.

**Interview Point:**

> Heartbeat says “the process is alive.” `max.poll.interval.ms` says “the poll loop is making progress.” A long NE call on the poll thread fails the second one.

</details>

---

# 6. The consumer is single-threaded. How do you do slow NE work safely?

<details>
<summary>Show Answer</summary>

**Answer:**

One consumer thread owns `poll()`, the assigned partitions, and offset commits. If that thread blocks on SNMP or CLI, you hit question 5.

Safe shape:

```text
poll thread:  poll → hand records to a bounded queue → pause those partitions → poll again
worker pool:  talk to SNA / NE
poll thread:  when the worker finishes → commit that offset → resume the partition
```

```java
consumer.poll(Duration.ofMillis(500));
for (ConsumerRecord<String, NeCommand> record : records) {
    TopicPartition tp = new TopicPartition(record.topic(), record.partition());
    consumer.pause(Collections.singleton(tp));
    workerPool.submit(() -> handleOnNe(record));
}
// later, on the poll thread, after the worker succeeds:
consumer.commitSync(Collections.singletonMap(tp, new OffsetAndMetadata(record.offset() + 1)));
consumer.resume(Collections.singleton(tp));
```

Commit rules:

| You commit… | Result |
|-------------|--------|
| Before the NE call returns | Crash loses the work (at-most-once) |
| After the NE call returns | Crash retries the work (at-least-once) |
| From a random worker thread with no lock | Concurrent `commit` with `poll` corrupts the client |

Commit on the poll thread. Bound the worker pool. When the queue is full, keep the partition paused. That is backpressure. An unbounded pool just moves the outage into memory and threads.

ESM’s `KafkaOperationThreadWorker` / `KafkaAsyncThreadWorker` exist for this split. The pool must have a limit. When the limit is hit, the log shows `Rejected Kafka`.

**Interview Point:**

> The poll thread only polls, pauses, and commits. NE calls run on a bounded pool. Commit after success, on the poll thread.

</details>

---

# 7. What do `acks=all`, `min.insync.replicas`, and unclean leader election actually guarantee?

<details>
<summary>Show Answer</summary>

**Answer:**

Durability is three settings working together. One of them alone is a weak promise.

| Setting | Where | Meaning |
|---------|--------|---------|
| `acks=all` (producer) | Producer | Leader waits until every **in-sync replica (ISR)** has the record |
| `min.insync.replicas` (topic) | Broker | Produce with `acks=all` fails if fewer than this many replicas are in the ISR |
| `unclean.leader.election.enable=false` | Broker | A replica that is **not** in the ISR must not become leader |

Typical production topic: replication factor **3**, `min.insync.replicas=2`, `acks=all`, unclean leader election **off**.

```text
RF=3, min.insync.replicas=2
  2 brokers acknowledge → produce succeeds → one broker can die and the record is still there
  only 1 broker left in ISR → produce fails with NotEnoughReplicas → better than a silent write
```

`acks=1` returns when the leader has the record. If the leader dies before followers copy it, the record is gone.

Unclean leader election **on** keeps the topic available by promoting a stale replica. That replica is missing records the old leader had acknowledged. For NE commands that is a lost deploy. Leave it off. Availability then drops until an in-sync replica is back, which is the right trade for configuration traffic.

**Interview Point:**

> `acks=all` waits for the ISR. `min.insync.replicas` stops a write when the ISR is too small. Unclean election off stops a stale broker from becoming leader and dropping acknowledged commands.

</details>

---

# 8. Idempotent producer, transactions, and app idempotency — which one covers the NE?

<details>
<summary>Show Answer</summary>

**Answer:**

They solve three different duplicate problems. Only the last one protects the NE.

**1. Idempotent producer** — duplicates caused by the producer retrying a batch.

```java
props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
// forces acks=all, retries high, max.in.flight.requests.per.connection <= 5
```

The broker stores `producerId + sequence` per partition and drops a replayed batch. This is exactly-once **into one partition**, for that producer session. It does not stop the consumer from processing twice.

**2. Kafka transaction** — one atomic write across “output records + offset commit.”

```java
producer.initTransactions();
producer.beginTransaction();
producer.send(outputRecord);
producer.sendOffsetsToTransaction(offsets, groupMetadata);
producer.commitTransaction();
```

If the app crashes, the offset and the output records commit together or not at all. This is exactly-once **from one Kafka topic to another Kafka topic**. The NE, the database, and SNMP are not in that transaction.

**3. App idempotency** — the SNA (or ESM) stores `requestId` and skips a repeat.

```text
first delivery:  requestId=8841 not seen → send CLI → save 8841
redelivery:      requestId=8841 already applied → return the same success → do not send CLI again
```

A `set` of a value is naturally safe to repeat. An `add` of a VLAN or a second `create` is not. Prefer set/replace on the NE, and dedupe on `requestId` before the CLI.

**Interview Point:**

> Producer idempotence dedupes broker retries. Transactions cover Kafka-to-Kafka. The NE needs its own `requestId` check, because it sits outside the Kafka transaction.

</details>

---

# 9. How does a poison message block a partition, and how do you park it?

<details>
<summary>Show Answer</summary>

**Answer:**

A **poison message** is a record the consumer cannot handle: bad bytes, unknown class, or a bug that always throws.

The consumer reads in offset order. If offset 41 throws every time, the consumer never commits 42. That partition freezes. If this consumer also owns other partitions, it may stop calling `poll()` while it retries 41, and **those partitions freeze too** once `max.poll.interval.ms` is hit.

`ErrorHandlingDeserializer` catches the deserializer exception and hands the failure to the error handler instead of killing the poll loop.

```java
props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS,
        AllowedKafkaResponseClassDeserializer.class.getName());

@Bean
public DefaultErrorHandler errorHandler(KafkaTemplate<Object, Object> template) {
    DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(template);
    DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, new FixedBackOff(1_000L, 3));
    handler.addNotRetryableExceptions(DeserializationException.class, IllegalArgumentException.class);
    return handler;
}
```

Flow:

```text
main topic → try 3 times with backoff → still bad?
        → <topic>.DLT   (original offset, exception, stack trace in headers)
        → commit past the bad record so the partition moves
```

Do not retry a validation or deserialization error forever. Retry transient NE timeouts. Park poison records. Alert when the DLT gets new messages. Replay only after the code can read that payload.

**Interview Point:**

> One bad record stops its partition, and can stall every partition on that consumer. Deserialize safely, retry only transient errors, then publish to a DLT and move on.

</details>

---

# 10. How do you read consumer lag, and what do you refuse to do?

<details>
<summary>Show Answer</summary>

**Answer:**

**Lag** is how far the committed offset sits behind the end of the log.

```text
lag = log-end-offset − committed-offset
```

```bash
kafka-consumer-groups.sh --bootstrap-server <broker> --describe --group esm-sna-reply
```

Read it **per partition**, not as one number. A group lag of 10,000 on one SNA partition and 0 on the others means one adapter or one NE is behind.

What high lag usually means in this system:

| Pattern | Likely cause |
|---------|----------------|
| Lag grows on every partition together | ESM or SNA process is down, or the pool is saturated |
| Lag on one partition only | Hot NE, or that partition’s consumer is stuck on a poison record |
| Lag jumps, then falls | Normal bulk sync. Watch the slope, not a single spike |
| Lag flat, jobs still In Progress | Workers are rejected or waiting on the NE; the consumer may already have committed |

What you do:

- Scale consumers only up to the partition count.
- Speed the handler or split the key if one partition is hot.
- Fix poison records and pool rejections before you touch offsets.

What you do not do in an incident: `--reset-offsets --to-latest` to “clear the lag.” That **skips** the NE commands still in the log. Sync will look green and the network will be wrong.

**Interview Point:**

> Lag is end offset minus committed offset, per partition. Reset-to-latest deletes unfinished NE work. Find the slow partition instead.

</details>

---

# 11. Retention, compaction, and a disk that fills even when “old files should be gone”

<details>
<summary>Show Answer</summary>

**Answer:**

A topic keeps data until a cleanup policy says otherwise.

**Delete policy** (`cleanup.policy=delete`): drop old segments using `retention.ms` and `retention.bytes`. Kafka decides age from the **largest record timestamp inside the segment**, not from the file’s modification time.

**Compact policy** (`cleanup.policy=compact`): keep the latest record per key. Use it for “current state” (last known NE admin state), not for a command log. A null value is a tombstone; the key is removed after `delete.retention.ms`.

Command topics (deploy, sync request) should stay **delete**, with a retention long enough to debug a failed job (often days, not forever).

Disk still fills when:

| Cause | What you see |
|-------|----------------|
| `retention.bytes=-1` and a long `retention.ms` | No size cap. Growth continues until time retention catches up |
| Record timestamps in the future | Segment `maxTimestamp` is years ahead, so time retention never fires |
| Huge produce retries / error logs on the broker disk | The data volume is fine; the **log files** filled the disk |
| Consumer stopped and you also disabled retention | The log is an unbounded queue |

Check the segment’s `maxTimestamp` before you blame “too many NEs.” A clock-skewed NE or PM collector can pin a segment. Fix the clock, then delete or rewrite the bad segment in a controlled way. Truncating without fixing the clock fills the disk again.

**Interview Point:**

> Retention follows the record timestamp, not the file time. Future timestamps pin segments. `retention.bytes=-1` means there is no size cap.

</details>

---

# 12. How do you change the payload between ESM and SNA without breaking the consumer?

<details>
<summary>Show Answer</summary>

**Answer:**

ESM and SNA are separate deployables. One of them will restart first. For a while they run different jars. The bytes on the topic must still be readable by both.

Rules that keep a rolling upgrade alive:

| Change | Safe? |
|--------|--------|
| Add an optional field the old reader ignores | Yes |
| Remove a field the old reader requires | No. Stop writers first, or keep the field |
| Rename a class the deserializer instantiates | No. That is a new type |
| Trust whatever class name is inside the payload | No. That is remote code execution |

ESM does **not** open a raw `ObjectInputStream` on the reply. `AllowedKafkaResponseClassDeserializer` plus `KafkaResponseTypeValidator` accept only classes on an allow-list. An unknown type fails closed.

```text
SNA sends NewNeResponse   (new jar)
ESM allow-list has only OldNeResponse
    → deserializer rejects
    → record goes to error / DLT
    → the rest of the partition can move
```

Ship the allow-list update **with or before** the SNA that emits the new type. During the window, tolerate both the old and the new type. After both sides are on the new jar, remove the old type.

JSON with an explicit `type` header is easier to evolve than Java serialization. If the platform stays on Java serialization, the allow-list is the safety control. A “fix” that deserializes any class makes the consumer able to read the new payload and also able to execute a gadget class from a hostile record.

**Interview Point:**

> Add fields, keep old types during the rollout, allow-list every class. Unknown type fails the record, not the process, and never opens Java deserialization to every class on the classpath.

</details>

---

# 13. Why do you need an outbox when the DB and Kafka must agree?

<details>
<summary>Show Answer</summary>

**Answer:**

A job row and a Kafka send are two systems. They do not share one commit.

```text
A) save job as In Progress → crash → Kafka send never happens → job stuck forever
B) send Kafka first → crash → DB roll back → SNA applies the NE command and ESM has no job
```

The **outbox** puts the business write and the “please publish” write in **one database transaction**:

```text
BEGIN
  update service set state = 'DEPLOYING'
  insert into outbox(request_id, topic, key, payload, status)
COMMIT
```

A separate relay reads `outbox` rows and produces to `ESM_ASYNCIF_REQUEST_{snaId}`. After the broker acks, the relay marks the row `SENT`. If the relay crashes after the ack and before the mark, it sends again. The SNA dedupes on `request_id` (question 8).

```java
@Transactional
public void startDeploy(DeployCommand command) {
    jobRepository.markDeploying(command.getJobId());
    outboxRepository.insert(command.getRequestId(), topic, command.getNeName(), payload);
}
```

Do not hold the DB transaction open across `sendAndReceive`. The outbox commit is short. The NE wait happens later, outside the transaction.

**Interview Point:**

> One DB transaction writes the job and the outbox row. A relay publishes after commit. Retries are safe because the NE side dedupes on `requestId`.

</details>

---

# 14. How do you apply backpressure when the NE is slower than the topic?

<details>
<summary>Show Answer</summary>

**Answer:**

Kafka will accept records faster than an NE can execute CLI. If ESM or SNA buffers without a limit, you get a full heap, a full thread pool, or a consumer that stops polling.

Backpressure means the fast side waits or rejects, with a clear failure, instead of growing forever.

```text
NE slow
  → worker queue reaches its bound
  → pause the partition (consumer stops pulling more)
  → producer eventually blocks or gets a timeout
  → caller marks the job Failed or Retry, with a reason
```

Places to put the bound:

| Layer | Bound |
|-------|--------|
| Consumer | `max.poll.records` + `pause` when the queue is full |
| Worker pool | Fixed size + bounded queue. Rejection is logged (`Rejected Kafka`) |
| Producer | `max.block.ms` so a full buffer fails the call instead of hanging the Tomcat thread |
| Per NE | A semaphore so one NE cannot take every worker |

A bulk sync of hundreds of NEs must not start hundreds of blocking waits on the HTTP threads. Accept the job, enqueue it, and let the pool drain at the rate the NE can handle.

**Interview Point:**

> Bound the queue, pause the consumer, and fail or retry the job with a reason. An unbounded buffer turns a slow NE into an ESM outage.

</details>

---

# 15. What do you watch in production?

<details>
<summary>Show Answer</summary>

**Answer:**

Watch signals that change **before** the user says “sync is stuck.”

| Signal | Why it matters here |
|--------|---------------------|
| Consumer lag per partition | One SNA or one NE falling behind |
| Under-replicated partitions | A broker is behind. `acks=all` will get slow, then fail |
| Offline partitions | No leader. Produces and consumes stop for that topic |
| Produce error rate (`NotEnoughReplicas`, timeout) | Writes are not durable right now |
| Reply timeout rate | SNA or NE is slow, or the reply listener is down |
| `Rejected Kafka` count in `ESMServer.log` | Worker pool is full. New NE work is being dropped |
| DLT / error-topic rate | Poison payloads or a bad release |
| Request time vs NE time | Separates “Kafka is slow” from “the NE CLI is slow” |
| Disk free on the broker, and segment `maxTimestamp` | Retention not deleting, often future timestamps |
| Thread count on `snaadapter-a` | A stuck NE session can leak threads and stall that SNA |

A useful alert is “lag on `ESM_ASYNCIF_REQUEST_*` rising for 10 minutes **and** rejected-task count > 0.” Lag alone during a planned full sync is often normal.

On a page, capture: group describe, the stuck partition, a stack of the poll thread and the worker pool, and whether the NE session is open. Restarting Tomcat without that evidence only clears the symptom.

**Interview Point:**

> Lag, under-replicated partitions, reply timeouts, and rejected workers. Split “Kafka slow” from “NE slow” before you restart anything.

</details>

---

# Part 2 — Production problems (ESM → SNA → NE)

Each answer is the story you tell in an interview: what the user saw, what you opened, what was actually wrong, what you changed.

---

# 16. Bulk sync stays In Progress and the log says `Rejected Kafka`. What happened?

<details>
<summary>Show Answer</summary>

**Answer:**

**Symptom**

Operators start a full sync or a bulk supervision. Many NEs stay **In Progress**. `ESMServer.log` shows `Kafka Thread` workers, then `Rejected Kafka`. Restarting the screen does not finish the jobs.

**What you check**

- Count of `Rejected Kafka` versus `Kafka Thread` in `ESMServer.log`.
- Pool settings for `KafkaOperationThreadWorker`, `KafkaAsyncThreadWorker`, and `KafkaMepThreadWorker` (size and queue capacity).
- Whether the HTTP thread that started the sync is still blocked, or it already returned after the reject.
- Job table: state was set to In Progress **before** the task was accepted by the pool.

**Root cause**

The worker pool is bounded (that part is correct). The rejection path is not. The job was marked In Progress, then `submit` threw because the queue was full. Nothing updated the job to Failed. The task never reached SNA, so no reply will ever arrive.

**Fix**

- On rejection, mark that NE **Failed** with reason `worker pool full`, or put it on a retry queue that the pool pulls later.
- Keep the pool bounded. Do not “fix” this by an unbounded queue.
- Cap how many NE syncs one action may start, and drain at the rate SNA can talk to NEs.
- Split sync workers from deploy workers so a full sync cannot reject a single urgent deploy.

**Interview Point:**

> `Rejected Kafka` means the task never ran. The job row must leave In Progress in the same failure path. A bigger unbounded pool only moves the outage to memory.

</details>

---

# 17. The database pool is exhausted, but the SQL is fast. Why?

<details>
<summary>Show Answer</summary>

**Answer:**

**Symptom**

Deploy and sync start failing with pool timeout. Active DB sessions sit near the c3p0 max. The SQL itself is short. Thread dumps show many threads inside `ReplyingKafkaTemplate.sendAndReceive`, and each of them still owns a JDBC connection.

**What you check**

- c3p0 `maxPoolSize` versus threads blocked in `sendAndReceive` / `future.get`.
- Whether a `@Transactional` service method calls Kafka **before** the transaction ends.
- How long those transactions have been open (they match the NE timeout, often tens of seconds, not milliseconds).

**Root cause**

The code holds a DB connection for the whole NE round trip:

```text
begin transaction
  update job
  sendAndReceive   ← waits on SNA and the NE for 30–60s
commit
```

Each in-flight NE command pins one connection. A bulk sync pins the whole pool. New requests cannot even load a page that needs the DB. The NE is slow; the outage shows up as a database outage.

**Fix**

- Commit the DB work first (job row + outbox). Release the connection.
- Wait for Kafka **outside** the transaction.
- When the reply arrives, open a **new** short transaction to store the result.
- Bound the number of waits so they cannot exceed what you meant to allow.

**Interview Point:**

> A connection checked out across `sendAndReceive` turns NE latency into pool exhaustion. Commit, release, then wait.

</details>

---

# 18. One slow NE causes duplicate CLI on other NEs. How?

<details>
<summary>Show Answer</summary>

**Answer:**

**Symptom**

One NE is unreachable and its CLI hangs. A few minutes later the consumer group rebalances. Other NEs, which had already been configured, receive the same command again. SNA logs show two applies for one `requestId`.

**What you check**

- Consumer logs: `max.poll.interval.ms` expired, member left the group.
- Whether the NE call runs on the **poll thread**.
- Offset commit time: committed only after the CLI returns, so the hung record is uncommitted.
- Which other partitions that same consumer owned.

**Root cause**

The poll thread blocked on one NE longer than `max.poll.interval.ms`. The coordinator kicked the consumer and gave **all** of its partitions to someone else. Uncommitted records on the healthy NEs were delivered again. Kafka did what at-least-once always does.

Raising the interval to “longer than the slowest NE” keeps a dead consumer in the group for that whole time and still duplicates when the process finally dies.

**Fix**

- Move the CLI off the poll thread. Pause that NE’s partition. Keep polling.
- Dedupe in SNA on `requestId` so a second delivery does not push a second CLI.
- Set `max.poll.records` low enough that one batch cannot contain an hour of NE work.
- Use cooperative sticky assignment so a restart does not reshuffle healthy partitions.

**Interview Point:**

> A stuck poll fails `max.poll.interval.ms` and redelivers every uncommitted record that consumer owned. Pause and hand off; dedupe on `requestId`.

</details>

---

# 19. Many operations time out together. Is the NE down?

<details>
<summary>Show Answer</summary>

**Answer:**

**Symptom**

Lots of deploy and sync operations flip to timeout at the same moment. One NE being down does not explain all of them. The stack contains `IllegalMonitorStateException` inside `ReplyingKafkaTemplate.sendAndReceive`. A Tomcat restart clears it. The next heavy run can bring it back.

**What you check**

Three different failures look similar on the GUI. Separate them:

| What you find | What it means |
|---------------|----------------|
| One NE, SNA log shows CLI timeout, other NEs fine | That NE or its path is down |
| One SNA’s request topic lag grows, other SNAs fine | That `snaadapter-a` is stuck or dead |
| All SNAs, `IllegalMonitorStateException` on `sendAndReceive`, no matching NE timeout | The Kafka **producer client inside ESM** is broken |

For the third case, take several thread dumps **while it is hung** (10–15, a few seconds apart). Look at the producer sender thread and threads blocked in `sendAndReceive`. A restart that “fixes” it is a workaround. Without the dumps you cannot tell a monitor bug from a stuck sender.

**Root cause**

`IllegalMonitorStateException` means a thread called `wait` / `notify` in an illegal state on the producer’s internal monitor. The reply future never completes, so every waiter hits its timeout. The NE may be healthy the whole time. Restarting Tomcat rebuilds the producer, which is why it looks fixed.

**Fix**

- Treat this as an ESM Kafka-client failure. Collect the dumps before restart when you can.
- Recreate a broken producer instead of leaving all waiters blocked until the process is killed.
- On the timeout path, fail the job with the exception class in the reason, so the next person does not chase the NE.
- Keep SNA and NE checks for the cases that really are SNA and NE.

**Interview Point:**

> A timeout is “the future did not complete.” Match it to one NE, one SNA, or `IllegalMonitorStateException` on `sendAndReceive`. The restart is a workaround; the dumps are the evidence.

</details>

---

# 20. The deploy was retried and the NE now has duplicate config. Why did exactly-once not save you?

<details>
<summary>Show Answer</summary>

**Answer:**

**Symptom**

A deploy timed out. The user, or the job retry, ran it again. The NE now has two services, two VLANs, or two MEP entries. Kafka producer metrics show retries. The idempotent producer was already enabled.

**What you check**

- Whether both attempts used the **same** `requestId`.
- SNA log: did it send CLI twice?
- The CLI verb: `create` / `add` versus `set` / `replace`.
- Confirmation that `enable.idempotence=true` only dedupes the produce, and there is no transactional wrapper around the NE.

**Root cause**

At-least-once delivery. The first request reached SNA and the NE. The reply was lost or slow, so ESM timed out and sent again. The idempotent producer stopped duplicate **batches** on the broker. It did not stop a second **business command**. Kafka transactions would commit the Kafka write atomically with a Kafka offset. They do not include CLI on the NE.

**Fix**

- One `requestId` per user action. Retries reuse it.
- SNA stores applied ids. A repeat returns the first result and does not open a second CLI session.
- Model NE changes as set/replace where the device allows it.
- Where the device only has `add`, the SNA must check “already exists” before adding.
- The UI retry button must not mint a new id for the same user action.

**Interview Point:**

> Exactly-once into Kafka does not cover the NE. Same `requestId`, dedupe in SNA, and prefer `set` over `add`.

</details>

---

# 21. Why is Java deserialization of an SNA reply dangerous, and what does ESM do instead?

<details>
<summary>Show Answer</summary>

**Answer:**

**Symptom**

A security review flags the Kafka reply path. The payload can carry a Java class name. A listener that does a plain `ObjectInputStream.readObject()` will instantiate whatever class the bytes name, as long as that class is on the classpath.

**What you check**

- The reply deserializer: `AllowedKafkaResponseClassDeserializer`.
- The allow-list: `KafkaResponseTypeValidator`.
- Tests that a class **outside** the list throws, and a class **on** the list passes.
- That the error is a validation failure, not a half-built object passed into the task chain.

**Root cause**

Kafka is a trusted pipe only if every producer is trusted and the bytes cannot be altered. A reply topic is still a deserialization boundary. Java serialization can run code during `readObject` (gadget chains). One bad record is then remote code execution inside ESM, not just a failed sync.

**Fix**

- Allow-list response classes. Reject everything else in `KafkaResponseTypeValidator` before the object is used.
- Wrap the deserializer with `ErrorHandlingDeserializer` so a reject is a parked record, not a dead consumer.
- Add a new response type to the allow-list in the same change that introduces it.
- Keep a unit test with a disallowed class name and with a truncated payload.

Turning the allow-list off to “unblock a release” removes the control. Fix the missing class name instead.

**Interview Point:**

> Treat the SNA reply as untrusted bytes. `AllowedKafkaResponseClassDeserializer` and `KafkaResponseTypeValidator` accept only known types. Anything else fails closed.

</details>

---

# 22. After an upgrade, one bad reply freezes every NE on that SNA. Why?

<details>
<summary>Show Answer</summary>

**Answer:**

**Symptom**

Right after an SNA or ESM upgrade, replies stop for every NE on one SNA. `ESMServer.log` shows a deserialization error or “type not allowed” on a loop. Lag on the reply partition climbs. Older NEs that still reply with the old class are stuck behind the new record.

**What you check**

- The exception class: unknown type versus a transient SNA error.
- Whether the consumer retries that offset forever.
- The allow-list on the ESM jar that is actually running (not the jar you just built).
- DLT: is the bad record parked, or is it still at the head of the partition?

**Root cause**

The new SNA emitted a response class the running ESM does not allow. The consumer hits that offset, throws, and does not commit. Every later reply on that partition, including healthy NEs, waits behind it. If the error handler retries deserialization forever, the poll loop also stalls the consumer’s other partitions.

**Fix**

- Mark deserialization and “type not allowed” as **not retryable**.
- Publish that record to the DLT with the class name and the original offset in headers.
- Commit past it so the rest of the NEs move.
- Put the new class on the allow-list and roll ESM **before** or **with** the SNA that sends it.
- Replay the DLT record only after the allow-list contains that class.

**Interview Point:**

> An unknown reply type is a poison record. Park it, commit past it, and keep the rest of the SNA moving. Retrying a class error only freezes the partition.

</details>

---

# 23. The consumer is up, but the message disappears. `Unable to find processing bean`. What does that mean?

<details>
<summary>Show Answer</summary>

**Answer:**

**Symptom**

A sync for one feature (for example protection-group sync) never finishes. The topic has a consumer group and the lag stays low, so the record **was read**. The log says `Unable to find processing bean` for a name like `nepgsyncreq`. No NE command was sent.

**What you check**

- The topic list the listener subscribes to.
- The map from topic (or command type) to Spring bean. Look for a bean that is commented out while the topic is still subscribed.
- Offset movement: if the offset advanced, the record will not be read again.

**Root cause**

A half-finished change. The consumer still subscribes to the topic. The handler bean was removed or commented out of the map. The listener catches “no bean,” logs the error, and commits. From Kafka’s point of view the group is healthy. From the NE’s point of view the command was dropped.

**Fix**

- Ship the topic and the bean in the same release. Removing a handler means unsubscribing from that topic in the same commit.
- At startup, fail the process if a subscribed topic has no bean. A loud crash is better than a silent drop.
- Do not commit a record you did not route, unless you have parked it on a DLT on purpose.
- After the fix, replay from the DLT or re-run the sync. The original record is already past the committed offset.

**Interview Point:**

> Low lag plus “no processing bean” means the record was consumed and thrown away. A subscribed topic must have a handler, or startup must fail.

</details>

---

# 24. One SNA syncs, the other times out, and the topic appears and disappears. What raced?

<details>
<summary>Show Answer</summary>

**Answer:**

**Symptom**

Alarm sync or NE sync works on one SNA instance and times out on another. Logs around startup show the topic “exists” and “does not exist” a few milliseconds apart. Someone ran `createKafkaPartitionSNA.sh`. A second run sometimes “fixes” it.

**What you check**

- The script: it often starts `kafka-topics --create` in the background and immediately describes the topic.
- Topic describe for `ESM_ASYNCIF_REQUEST_{snaId}`: partition count, leader, ISR.
- Whether the SNA that fails is producing to a topic that has no leader yet.

**Root cause**

Topic creation is not instant. The script checks before the controller has finished. One SNA binds to a ready topic. Another SNA starts against a topic that is still being created, or against the wrong partition count. Its requests never get a stable leader, so every NE on that SNA times out. The script “working” on a retry is a race, not a fix.

**Fix**

- Create the topic with the final partition count **before** SNA and ESM start.
- Wait until metadata shows a leader and a full ISR for every partition. Then start the adapter.
- Make the script idempotent: if the topic exists, `alter` the partition count upward only when you really intend to add partitions.
- Do not background `--create` and race it with `--describe`.

Adding partitions later also changes key routing (question 2). Do that on purpose, not as a side effect of a flaky script.

**Interview Point:**

> “Exists” and “does not exist” milliseconds apart is a create race. Wait for a leader on every partition, then start SNA. A second manual run is only a workaround.

</details>

---

# 25. The NE says the service arrived before the port. The producer logged both sends in order. How?

<details>
<summary>Show Answer</summary>

**Answer:**

**Symptom**

A task chain creates a port, then a service, then activates. ESM logs the sends in that order. The NE rejects the service: the port does not exist yet. A retry later works, which makes it look like a timing glitch.

**What you check**

- The **key** on each `ProducerRecord`, not the log order on the ESM node.
- Partition and offset of each step (the send callback prints them).
- Whether two steps hashed to two partitions.

**Root cause**

Log order on the producer is the order of `send()` calls. Kafka order is the order **inside one partition**. The port used key `ne-1` and the service used key `ne-1-service` (or a null key). They landed on different partitions. SNA has one consumer thread per partition, so the service partition can be read first.

```text
send(port)    key=ne-1         → partition 0  offset 10
send(service) key=ne-1-service → partition 2  offset 4   ← consumed first
```

**Fix**

- Use one key for the whole chain on that NE, usually `neName`.
- Keep the task chain’s next step **after** the reply for the previous step, which ESM already wants. The key still has to match, because retries and parallel jobs for the same NE must queue behind each other.
- After any partition-count change, confirm the key still maps where you think.

**Interview Point:**

> Producer log order is not partition order. Same `neName` key for every step of that NE, and wait for the reply before the next step.

</details>

---

# 26. Should the record key be `snaId` or `neName`?

<details>
<summary>Show Answer</summary>

**Answer:**

**Symptom**

This is the design question behind several incidents: syncs run one NE at a time even though the SNA handles many NEs, or two deploys to the same NE overlap and leave half-built config.

**What you check**

- Topic is already `ESM_ASYNCIF_REQUEST_{snaId}`, so the SNA instance is chosen by the **topic**, not by the key.
- Partition count of that topic.
- The key used by `KafkaESMUtil`.

**Root cause**

The topic selects the SNA. The key selects the partition inside that SNA.

| Key | What you get |
|-----|----------------|
| Constant, or only `snaId` | One hot partition. Every NE on that SNA is ordered behind every other NE. Extra consumers idle |
| `neName` | Parallel NEs, ordered commands **inside** one NE |
| `serviceId` with no NE in the key | Two services on the same NE can pass each other and interleave CLI |

`neName` is the right default for this path. The SNA can work on many NEs at once, and one NE still sees a single ordered stream. Use a finer key only when two flows on the same NE are truly independent and the NE itself allows that.

**Fix**

- Key by `neName`.
- Set partition count high enough for the busiest SNA (a common starting point is well above 1, and at least the number of NEs you want in parallel, with a sane cap).
- Keep one consumer per partition up to that count. More consumers than partitions do nothing.
- If one NE is huge, fix that NE’s handler; do not put all NEs back on one key.

**Interview Point:**

> The topic picks the SNA. The key picks the order. `neName` gives parallel NEs and a stable order on each NE.

</details>

---

# 27. Full sync: lag grows on one SNA only. How many consumers do you add?

<details>
<summary>Show Answer</summary>

**Answer:**

**Symptom**

During a planned full sync, one SNA falls behind. The GUI shows that SNA’s NEs In Progress long after the others finished. Someone asks to double the ESM consumer pods.

**What you check**

```bash
kafka-consumer-groups.sh --bootstrap-server <broker> --describe --group <esm-or-sna-group>
```

- Lag **per partition** on `ESM_ASYNCIF_REQUEST_{snaId}` and on the reply topic.
- Partition count versus members in the group. Members with no assignment are already wasted.
- SNA CPU, NE session count, and `Rejected Kafka` on that instance only.
- One partition lagging while its neighbors are fine: a hot NE or a poison record, not “need more pods.”

**Root cause**

Lag on a single SNA is local. Either that `snaadapter-a` cannot push CLI fast enough, or one partition inside its topic is hot. New ESM pods join the group and get partitions only if some partition has no owner. If every partition already has a consumer, the new pods sit idle and the lag does not move.

**Fix**

- If members < partitions and several partitions have lag: add consumers up to the partition count.
- If one partition has all the lag: inspect that key (`neName`). Speed that NE path or isolate it. Adding consumers does not split one partition.
- If the SNA host is busy or rejecting work: back off the sync. More consumers will only reject more tasks.
- Watch the lag slope. A bulk sync **should** build lag and then drain. Page when it stops draining.

**Interview Point:**

> Add consumers only until each partition has one. One hot partition needs a better key or a faster handler, not another idle pod.

</details>

---

# 28. The Kafka disk is full. The segment files look old. Why were they not deleted?

<details>
<summary>Show Answer</summary>

**Answer:**

**Symptom**

The broker volume hits 100%. Topics that carry NE or PM telemetry are the large ones. `ls` shows segment files whose modification time is days ago. Retention is set to a day or two, so they “should” be gone. After a manual delete, the disk fills again.

**What you check**

- Topic `retention.ms` and `retention.bytes` (`-1` means no byte cap).
- Segment **max timestamp**, not file mtime. Kafka’s retention clock is the largest timestamp stored in the segment.
- Whether those timestamps are months or years ahead (bad NE clock, or a collector writing event time from the device).
- Broker **log files** versus topic data. A full disk is sometimes MirrorMaker or adapter error logs, not the log segments. `du` the data directory and the log directory separately.

**Root cause**

Delete retention drops a segment when `now - maxRecordTimestamp` is past `retention.ms`. A future timestamp makes the segment look young forever. `retention.bytes=-1` never trims by size. Deleting files by hand does not fix the clock, so new segments pin the same way.

**Fix**

- Correct the NE or collector clock. Stop producing future timestamps.
- Set a real `retention.bytes` on telemetry topics so one bad clock cannot fill the disk.
- Keep command topics (deploy / sync) on a modest time retention. They do not need infinite history.
- Split “topic data” from “process logs” in the alert. They have different owners.
- After the clock is fixed, roll and delete the poisoned segments in a planned step.

**Interview Point:**

> Kafka retains by the record timestamp inside the segment. Future NE timestamps pin the file. Cap `retention.bytes`, then fix the clock, or the disk fills again.

</details>

---

# 29. Rolling upgrade: SNA is on the new jar, ESM is still on the old one. Replies fail. What do you do?

<details>
<summary>Show Answer</summary>

**Answer:**

**Symptom**

During the rollout, half the SNA instances are new. Their replies fail in ESM with “type not allowed” or a class-not-found style error from `AllowedKafkaResponseClassDeserializer`. Old SNAs still work. Someone suggests switching the deserializer to a raw `ObjectInputStream` so any class loads.

**What you check**

- Which response class the new SNA writes (`KafkaEsmResponseContext` / the reply payload type).
- Whether that class is on the allow-list **in the ESM image that is running**.
- Order of the rollout: SNA first versus ESM first.
- DLT depth. The failed replies should be parked, not blocking the partition (question 22).

**Root cause**

The new reply type and the allow-list were not released in an order both sides can read. ESM is doing the right thing by rejecting the unknown class. Opening Java deserialization to every class would make this deploy succeed and would accept a hostile payload on the next one.

**Fix**

- Add the new class to `KafkaResponseTypeValidator` and roll **ESM first** (or in the same window, before SNA sends the new type).
- Keep the old type on the list until every SNA is upgraded.
- Fail closed on unknown types. Park them on the DLT.
- After ESM is updated, replay the DLT.
- Roll back SNA if it is already sending a type ESM will never allow in this release. Do not widen the deserializer.

**Interview Point:**

> Roll the allow-list before the new reply type. Unknown classes fail closed and go to the DLT. Raw `ObjectInputStream` is not a compatibility mode.

</details>

---

# 30. The operation stays In Progress until someone restarts Tomcat. What should own the timeout?

<details>
<summary>Show Answer</summary>

**Answer:**

**Symptom**

A sync or a deploy stays **In Progress** for hours. SNA finished, or SNA never got the request. The GUI clears only after an ESM (Tomcat) restart. There is no Failed row and no reason.

**What you check**

- The waiter: `KafkaEsmResponseContext` / the `sendAndReceive` future. Is the thread still alive?
- The job row: who sets In Progress, and who is allowed to set Failed.
- Whether a timeout exception is caught and logged, with no update to the job.
- After a restart: in-memory futures are gone, so a startup sweep must reap jobs that are still In Progress and older than the NE timeout.

**Root cause**

The only owner of “this call is done” was an in-memory future. When that thread died, or the timeout was swallowed, nobody wrote Failed. Restart drops the memory and a startup path finally notices the stale row. Until then the operator has a job that can never complete, because the reply’s correlation id has nobody waiting.

**Fix**

- Every exit path writes a terminal state: Success, Failed, or Timed Out, with a one-line reason (NE timeout, pool rejected, type not allowed, producer error).
- Put the deadline in the **job row** (started-at + allowed NE time), not only in `future.get`.
- A periodic sweeper marks rows past the deadline as Timed Out, even if the waiter thread is gone.
- On startup, run that sweeper before you accept new work.
- The reply listener, if a late reply still arrives, must see “job already timed out” and not apply a second CLI. Dedupe on `requestId`.

**Interview Point:**

> In Progress needs an owner outside the waiting thread. Deadline on the job row, a sweeper, and a terminal state on every exit. Restart is not the timeout handler.

</details>

---

## Quick revision list

Speak these in order. If you can say each line without notes, you are ready for the senior round.

1. ESM → `KafkaESMUtil` → `ESM_ASYNCIF_REQUEST_{snaId}` → `snaadapter-a` → NE → reply matched by correlation id.
2. Order lives in one partition. Key those steps with `neName`.
3. One consumer per partition. Extra consumers do not split a hot partition.
4. Cooperative sticky rebalance, plus `group.instance.id` for a quiet restart.
5. Heartbeat means the process is alive. `max.poll.interval.ms` means the poll loop is moving.
6. Poll thread pauses and commits. A bounded pool talks to the NE.
7. `acks=all` + `min.insync.replicas=2` + unclean leader election off.
8. Producer idempotence and Kafka transactions stop at Kafka. The NE dedupes on `requestId`.
9. A poison record parks on the DLT. It must not pin the partition.
10. Lag is per partition. Reset-to-latest throws away NE commands.
11. Retention uses the record timestamp. Future timestamps fill the disk.
12. Allow-list reply classes. Roll that list before the new type.
13. Outbox: one DB transaction, then publish. Do not hold c3p0 across `sendAndReceive`.
14. `Rejected Kafka` fails the job. It does not leave it In Progress.
15. A timeout is one NE, one SNA, or a broken producer (`IllegalMonitorStateException`). The job row records which.
