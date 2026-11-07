# 4. Notification System

## Simple idea

Something happens in another service ("order shipped"). This system decides **who** to tell, **on which channel**, and **with which words**, then tries until the provider accepts it, once.

Channels: email, SMS, push, in-app.

---

## Requirements

- Take events from many producers.
- One event can become several notifications.
- Respect per-user preferences, quiet hours, and rules such as GDPR or do-not-disturb.
- Templates and translations.
- Retry, then a dead-letter queue.
- APIs to list in-app notifications and to send a campaign.

**Qualities:** millions per day and bursts, **at-least-once** delivery, near real time (a few seconds), encrypted personal data, easy to add a channel, logs without storing the whole message body in the clear.

**Hard parts:** providers rate-limit you. Push fails if the app was removed. A flash sale is a burst. A retry can send the same SMS twice if you are not idempotent. SMS is the expensive, regulated channel.

---

## Numbers

```text
10 million active users
× 5 events each per day
× 2 channels
= 100 million notifications per day
Peak × 3
```

About **1,200 per second** average, and a few thousand at peak. That is a queue problem, not a single mail loop.

Bottlenecks: ingestion, template CPU, provider limits, preference lookups, and log volume.

---

## Design

```text
Order service
  → ingestor (Kafka or SQS)
  → orchestrator
        asks Preference service (Redis + PostgreSQL)
        asks Template service (cached templates)
  → a queue per channel
  → workers → SendGrid / Twilio / FCM / in-app store
  → delivery tracker
  → dead-letter queue after N tries
```

The order API publishes an event and returns. It does not wait for Twilio.

| API | Who |
|-----|-----|
| `GET /notifications?userId=` | In-app list, paginated, read or unread |
| `POST /notifications/read` | Mark ids read |
| `POST /notification` | Admin or campaign send. Restricted role |
| `GET /delivery-report?eventId=` | What happened to one event |

JWT or OAuth on all of them. Admin routes check a role. Rate-limit at the gateway.

**Idempotency.** Every delivery has a key such as `eventId + userId + channel`. The worker records "sent" before or in the same step as a successful provider response. A second delivery of the same key does not call Twilio again. At-least-once from Kafka is safe only with that record.

Preferences are a high-QPS read. Cache them. Invalidate on change. Quiet hours are a preference, not a special case in every worker.

Templates are cached. Rendering a unique email per user is CPU. Do it in the worker, not in the producer's request.

Scale workers on queue depth (Kubernetes HPA or KEDA). Lambda is fine for spiky, low-duty channels. PostgreSQL for preferences. S3 or the database for template files. Prometheus and logs for "provider returned 429".

---

## Final design

Follow one event downward. A shop says "order shipped." The message waits in a queue. The orchestrator decides email or SMS from the person's settings. A separate worker talks to the outside company. A failed send goes to the retry box at the bottom, not back to the shop.

```text
Order / user / payment / auth
        | business event
        v
Event ingestor (validate, auth, rate limit, idempotency key)
        |
        v
Kafka or SQS
        |
        v
Orchestrator
   preferences (Redis + PostgreSQL)
   templates (cache)
   delivery status (PostgreSQL)
        |
        +--------+--------+--------+
        v        v        v        v
     Email Q   SMS Q   Push Q   In-app Q
        |        |        |        |
     Email     SMS      Push     In-app
     worker    worker   worker   worker
        |        |        |        |
     SendGrid  Twilio   FCM      in-app store --> app
     or SES    or SNS   or APNs

Worker failure --> retry queue --> dead letter
Metrics and logs watch the queues, not only the API.
```

## Interview lines

- Buffer in Kafka. One worker pool per channel, because SMS limits are not email limits.
- At-least-once plus an idempotency key. Dead-letter the poison message.
- Preferences and quiet hours before you render. Do not log full phone numbers and message bodies.

---

## Interview Q&A

### 1. How do you send 100 million notifications a day?

<details>
<summary>Show Answer</summary>

**Answer:**

Producers write events to Kafka or SQS and do not wait. An orchestrator loads preferences and a template, then enqueues one job per channel. Workers call SendGrid, Twilio, or FCM at a pace those providers allow. Scale the workers with the backlog. Peak is about 3×, so the queue absorbs the burst.

</details>

### 2. How do you avoid duplicate SMS on retry?

<details>
<summary>Show Answer</summary>

**Answer:**

The broker is at-least-once. Give each send a stable id (event, user, channel). If that id is already marked sent, acknowledge and stop. Record the provider's acceptance. After several failures, move the job to a dead-letter queue. Do not retry forever inside the request that placed the order.

</details>

### 3. What is different about each channel?

<details>
<summary>Show Answer</summary>

**Answer:**

Email is cheaper and slower. SMS is expensive and often legally limited, so it needs the tightest rate limit and the clearest opt-in. Push depends on a device token and fails quietly if the app is gone. In-app is just a row the app polls or receives while the user is online. Preferences choose the channel before any of those calls.

</details>

**Q&A recap:** queue, preferences, template, idempotent worker, dead letter.

## Interview Q&A — 6 to 8 years

### S1. The provider times out. Did the user get the SMS? How do you avoid three copies?

<details>
<summary>Show Answer</summary>

**Answer:**

I do not know. Timeout is unknown, not "failed." I store a provider message id when I have one, and I retry with the same idempotency key. If the provider supports a key, I send it. If not, I check my own row: SENDING older than a threshold can be retried once, and a webhook that says DELIVERED closes it. A second worker that picks up the same row must lock it (status = SENDING where status = QUEUED).

**Corner cases:** A success webhook arrives after I already retried, so the user got two and my row says one. I store both provider ids and I alert on duplicates. I still prefer a rare duplicate to a lost security OTP only if the product says so. For marketing, I prefer a miss to a duplicate. A retry storm when the provider is down: I backoff and I stop before I exhaust the user's patience and my bill. An at-least-once queue and a worker that sends and then crashes before ack. The lock plus the provider key is the fix, not "exactly-once" on the slide.

</details>

### S2. A user turns off email while a million-message campaign is in flight. What must be true?

<details>
<summary>Show Answer</summary>

**Answer:**

Preference is checked at send time, not only at enqueue time. The worker loads the latest preference. Unsubscribe is a fast write that the next send sees. Messages already handed to the provider cannot be pulled back. I define that clearly. A campaign job that pre-rendered a huge recipient list must re-check or it will violate the opt-out.

**Corner cases:** A preference cache of 10 minutes that ignores an unsubscribe. Security alerts may ignore the marketing opt-out, and marketing must not use the security template path to sneak through. A shared inbox or a family plan where one opt-out is not the other's. I key preference by user id and channel, not by device only. A template change mid-campaign so half the users get the old legal text. I version the template on the message row when I enqueue it, and I decide whether opt-out or template version wins. Quiet hours: a send at 3 a.m. local time. I store the user's zone and I delay. A wrong zone is the corner. Default to a safe window if the zone is missing.

</details>

### S3. One poison message blocks a partition of password resets. How do you isolate that?

<details>
<summary>Show Answer</summary>

**Answer:**

Separate queues by priority and type. Password reset is not behind a marketing blast and not behind one bad payload. After N failures I dead-letter the bad message with the error, and I ack it so the partition moves. I alert. I do not retry forever on the hot partition. Reset tokens have a short life, so a delayed reset should be dropped, not delivered an hour later. The user can request again.

**Corner cases:** A dead-letter queue nobody reads, so OTPs vanish and the metric still looks "processed." I count dead letters as a page. A bulkhead missing so the marketing worker pool and the OTP pool share one database connection limit and one provider rate limit. I split provider credentials and rate budgets. A large recipient fan-out in one message that OOMs the worker. I fan out to per-user messages upstream. Idempotency of a replayed dead letter: replaying must not send a second OTP if the first actually went out. I check provider state before replaying.

</details>

### S4. How do you know you are meeting the product, not only draining the queue?

<details>
<summary>Show Answer</summary>

**Answer:**

I measure accept rate by the provider, delivery rate if they report it, end-to-end lag from enqueue to accept, and the opt-out error rate. Queue depth alone is not success. I trace one user by notification id. I can answer "did we send it?" from my row without reading every log. Cost per send is a metric, because a retry bug is a bill.

**Corner cases:** Provider "accepted" and the handset never got it. I do not promise delivery I cannot see. A clock-skewed schedule that dumps a day of digests in one minute and hits a provider cap. I smooth the schedule. Template localization missing, so I send the wrong language or I fail the send and retry forever. I fall back once and then dead-letter. Duplicate device tokens after reinstall, so one user gets two pushes. I dedupe tokens. A broadcast that ignores per-user rate limits and looks like spam. I cap per user per hour for non-critical types.

</details>

**Senior recap:** unknown timeouts retry with a key, preferences are checked at send, OTPs do not share a queue with marketing, and accepted-by-provider is the metric you can defend.

Next: [05_Chat.md](05_Chat.md)
