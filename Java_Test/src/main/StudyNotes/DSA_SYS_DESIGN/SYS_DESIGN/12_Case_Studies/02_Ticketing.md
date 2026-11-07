# 2. Ticketing (BookMyShow)

## Simple idea

People browse events and buy seats. The hard part is **one seat, one buyer**, while a concert onsale dumps thousands of checkouts on you at once.

---

## Requirements

**Users:** browse events, see seats, reserve, pay, get email or SMS.

**Admins:** create events, venues, seat maps, prices.

**Qualities:** stay up during the onsale, answer quickly, no double booking, an audit log for fraud.

**Constraints from the slides:** about 5 million users, **100,000 concurrent** at peak, payments fail and must **release the seat**, more than one region if organizers are global.

---

## Numbers

| | Figure |
|--|--------|
| DAU | 1 million |
| Browse | about 10 million event reads per day |
| Bookings | 500,000 per day → about **6 per second** average |
| Onsale | up to **2,000 bookings per second** |

Average traffic is quiet. The bottleneck is a few hot events: seat races, write spikes, slow payment gateways, and a pile of confirmation messages.

---

## The seat rule

```text
available → held (about 5 minutes) → booked
                ↘ released if pay fails or the timer ends
```

Two ways to stop a race:

| Lock | Idea | Cost |
|------|------|------|
| **Pessimistic** | Lock the seat row until the transaction ends | Safe. Waiters pile up on a hot seat |
| **Optimistic** | Update only if the version or status is still "available" | No long lock. The loser retries |

The slides also put a **Redis key with a 5-minute TTL** on the hold so abandoned checkouts free themselves.

Redis alone is not the source of truth. If Redis restarts, two people can pass the hold. The **database** must still do:

```text
UPDATE seat SET status = 'held', holder = ?
WHERE id = ? AND status = 'available'
```

Zero rows updated means someone else won. Payment uses an **idempotency key** so a retry does not charge twice. A webhook or the TTL releases the hold if the gateway dies. Email and SMS go on a queue. They must not sit inside the booking transaction.

**CQRS** on the slides means the onsale read (what is free) can be a cache or a read model, and the write (the hold) goes through the consistent path. Do not sell a seat from a stale cache. Rebuild the cache from the database.

---

## Design

```text
Web / app → API gateway → Auth
                         → Event service      shows, venues, prices
                         → Seat inventory     free / held / booked
                         → Booking service    hold, then confirm
                         → Payment            Stripe or Razorpay, retries, webhooks
                         → Notification       email and SMS from a queue

PostgreSQL     users, bookings, payments, seat status
MongoDB        event documents and seat maps that change shape
Redis          hot availability and hold timers
Kafka          email, payment retry, audit
```

| API | Job |
|-----|-----|
| `POST /events`, `GET /events`, `GET /events/{id}` | Create and browse |
| `POST /bookings` | Hold seats. 201 or 400 if not enough seats |
| `GET /bookings/{id}` | Status. 404 if missing |

Take the user id from the token, not from a trusted field in the body.

Browse can be milliseconds. **Pay will not be.** Return "held, finish payment" quickly, and confirm when the gateway says so.

---

## Tech the slides name

Nginx or AWS API Gateway. OAuth2 and JWT. PostgreSQL for money and seats. MongoDB or Elasticsearch to search events. Redis for holds. Kafka for async work. Kubernetes and autoscaling. Prometheus, Grafana, and ELK. SES and Twilio for messages.

---

## Final design

Read this left to right. The person talks only to the gateway. The gateway talks to the services. The services talk to databases. Email and payment companies sit outside, so a slow email does not hold the seat.

```text
Web / mobile
    |
    v
API Gateway (one door, auth, routing, rate limit)
    |
    v
+--------------------------------------------------+
| Event service --> Seat inventory --> Booking     |
|        |                 |              |        |
| Payment service    Auth service    Notification |
+--------------------------------------------------+
    |                    |                    |
    v                    v                    v
PostgreSQL          MongoDB or           Redis
bookings, pay,      Elasticsearch        seat map, holds
users               events, search
    |
    v
Kafka / RabbitMQ / SQS --> workers (email, SMS, audit)

Booking and payment also call Stripe or Razorpay.
Notification calls the email and SMS provider.
```

## Interview lines

- Design for 2,000 bookings a second, not for 6.
- The conditional update in SQL is what prevents a double sale. Redis only makes the hold expire.
- Payment and email are outside the seat transaction, with idempotency and a timeout.

---

## Interview Q&A

### 1. How do you stop two people booking one seat?

<details>
<summary>Show Answer</summary>

**Answer:**

The winning write is conditional: update the seat only if it is still available. Optimistic locking uses a version. Pessimistic locking holds the row. A Redis key with a five-minute TTL drops abandoned holds. Redis is not enough after a restart, so the database status is the source of truth. The loser gets "seat taken" and refreshes the map.

</details>

### 2. What happens when payment fails?

<details>
<summary>Show Answer</summary>

**Answer:**

The seat stays held, not sold, until payment succeeds. A webhook confirms it. If the gateway times out, retry with the same idempotency key so you do not charge twice. If the hold TTL ends or the payment is refused, set the seat back to available. Confirmation mail is queued. The user is not blocked on Twilio.

</details>

### 3. Why is the average booking rate a trap?

<details>
<summary>Show Answer</summary>

**Answer:**

500,000 bookings a day is about 6 per second. A concert drop is the slide's 2,000 per second, on one event. Cache the event page, partition or isolate hot events, and keep the seat update short. Notifications and audit logs go to Kafka so they do not add to that spike.

</details>

**Q&A recap:** hold, conditional update, pay with an idempotency key, release on failure.

## Interview Q&A — 6 to 8 years

### S1. Two buyers click the last seat in the same millisecond. Walk through the only safe update.

<details>
<summary>Show Answer</summary>

**Answer:**

One conditional write: set the seat to HELD for buyer A only if it is FREE. The second buyer's update matches zero rows. I do not read "free" in the app and then write. The hold has an expiry stored on the row. Payment uses a second conditional update: set SOLD only if it is still HELD by this buyer and not expired. A unique constraint on the sold ticket backs this up.

**Corner cases:** A hold expiry that runs as a sweep while the payment request is in flight. The sweep must not release a hold that has a payment pending, or payment and release must be ordered with the same conditional predicate (status and version). Two seats wanted together: I lock or update them in a fixed id order so I do not deadlock, and I release both if I cannot get both. A browser retry that creates a second hold for the same user. I key the hold by user plus seat. A clock skew that expires a hold early on one app node. The database clock owns expiry, not the laptop.

</details>

### S2. Payment succeeds and the process crashes before the seat is marked SOLD. What does the user see?

<details>
<summary>Show Answer</summary>

**Answer:**

Unknown, not failure. The payment provider has an id. A reconcile worker, or the client's retry with the same idempotency key, looks up that payment and finishes the conditional SOLD update. If the seat was released already, I refund automatically. I never start a second charge because the retry reused the key. The user can poll the order: PENDING, SOLD, or REFUNDED.

**Corner cases:** The provider timed out and actually declined, and I mark SOLD anyway because I guessed. I only mark SOLD on a confirmed provider status. A webhook and a client retry both complete the order. Both paths use the same key so the second is a no-op. A refund that fails after I released the seat. The seat can be sold to someone else while I still owe the first user money. The reconcile queue must keep retrying the refund, and the seat stays blocked until money is settled if I cannot refund reliably. I say which policy I picked.

</details>

### S3. A flash sale opens at noon and every request hits one row. What fails first?

<details>
<summary>Show Answer</summary>

**Answer:**

The hot seat or the inventory counter, plus the connection pool. More app servers make it worse. I serialize the sale of that item in one place on purpose (one partition), I keep the transaction tiny (no email inside it), and I queue or lottery the right to attempt a hold so I do not stampede the row. I precreate seat rows. I rate-limit per user.

**Corner cases:** A cache of "100 seats left" that oversells. The cache can show approximate remaining. The hold path must still be the conditional update. A queue that admits 10,000 people and then times out their holds behind a slow payment provider. I size in-flight holds to the payment capacity. Bots that pass the queue. A single Redis lock for the whole venue, which then blocks an unrelated seat. Lock per seat. A count stored only in Redis that I decrement, while the database is the real stock, and they drift. One source of truth.

</details>

### S4. How do you stop a double spend when the user has two tabs and one expired hold?

<details>
<summary>Show Answer</summary>

**Answer:**

Each hold has an id. Tab A and tab B are two holds only if two seats exist. For one seat, the second hold fails. If tab A's hold expires, its pay button must fail the conditional update. I do not let the client decide it still owns the seat. Show the server status on click. An idempotency key per checkout attempt, not per tab refresh, so a refresh does not look like a new purchase.

**Corner cases:** The client generates a new idempotency key on every click. I generate it when the hold is created and reuse it. A user who pays from tab A, gets SOLD, and tab B still shows HOLD and they pay a friend to "finish" it. Tab B's update matches zero rows because the holder id differs. A transferred ticket that races with entry at the gate. The scan is another conditional update: set USED if SOLD and not USED. Offline scanners need a later reconcile and can double-admit. I state that limitation.

</details>

**Senior recap:** one conditional update owns the seat, payment crash becomes a reconcile, hot rows are not cached as truth, and the client does not mint a new idempotency key per click.

Next: [03_News_Feed.md](03_News_Feed.md)
