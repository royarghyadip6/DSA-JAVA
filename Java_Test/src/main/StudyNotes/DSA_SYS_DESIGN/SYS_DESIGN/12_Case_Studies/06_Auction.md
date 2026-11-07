# 6. Auction (eBay)

## Simple idea

A seller lists an item with a start and an end. People bid. The **highest valid bid when the clock stops** wins, then that person pays.

The hard part is the last second: many bids, one price, and everyone watching must see the new price quickly.

---

## Requirements

Accounts. List an item (start, end, reserve price). Bid in real time. States: scheduled → active → ended. Then payment (Stripe or PayPal). Notify people who were outbid, the winner, and the seller.

**Qualities:** sub-second bid and fan-out, thousands of concurrent auctions, secure payments, anti-bot measures, up during the close, logs of bids and payments.

**Actors:** seller, bidder, the clock (the system), admin for fraud and disputes.

---

## Numbers

| | Figure |
|--|--------|
| Users | 5 million registered, 500,000 DAU |
| Live auctions | about 1 million |
| Bids | about 10 per auction → **10 million bids a day** (about 100 per second average) |
| One hot auction | **10,000** people watching or bidding |
| Payments | 100,000 completed auctions a day |

Reads dominate (browse, watch). Writes are fewer and must be **correct**, especially at the close.

---

## The bid

```text
accept bid only if amount > current price
and now < end time
and this bid id was not already applied
```

Do that as one conditional update on the auction row (or a short transaction). Two bids of the same amount: define a rule, usually "first committed write wins". The loser gets a fresh price and can bid again.

**One hot auction is one row.** Do not split that row across servers. Scale by having many auctions on many servers, and by pushing **views** out over WebSockets. The price update itself stays a single conditional write.

**Sniping.** The slides want fairness against last-millisecond bots. A common rule is a **soft close**: a bid in the last two minutes extends the end. Say that rule out loud. Simply "blocking bots" is rate limits, auth, and not a guarantee.

**The clock.** A scheduler (delayed job, or a Redis expiry you trust and then confirm) moves scheduled → active → ended. Ending must be **idempotent**: if the job runs twice, you still have one winner and one payment. Then publish `auction.ended`. A worker notifies and starts payment. Payment failure retries on `payment.failed`. Do not take the card on every bid.

```text
Client subscribes to auction:{id}
Bid service commits → event → WebSocket fan-out to watchers
```

---

## Design

```text
API gateway
  User service
  Listing service       item, photos, category
  Auction service       lifecycle
  Bid service           the conditional write
  Scheduler             start and end
  Payment service
  Notification service
  WebSocket tier        channel per auction

PostgreSQL    users, auction, current price, bids, payments
Redis         hot auction page, pub/sub for the socket tier
Kafka         bid.placed, auction.ended, payment.failed
```

| API | Job |
|-----|-----|
| `POST /auctions` | Create |
| `GET /auctions/{id}` | View |
| `POST /auctions/{id}/bids` | Bid |
| `GET /auctions/{id}/bids` | History |
| `POST /payments/initiate` | After you win |

Internal events: `auction.ended`, `bid.placed`, `payment.failed`.

Auth and listing reads can be synchronous. Notifications and payment are asynchronous.

---

## Diagrams from the slides

The tables show who owns an item, who bid, and who pays. The big picture shows the same idea as a set of programs: the bid service is the only one that changes the price, and watchers hear about it on a live connection.

**Tables**

```text
USER (id PK, name, email, role, created_at)
   | creates                         | places
   v                                 v
LISTING (id PK, title,            BID (id PK, auction_id FK, user_id FK,
         description, media_url,       amount, timestamp)
         seller_id FK)
   | has auctions
   v
AUCTION (id PK, listing_id FK, start_time, end_time,
         reserve_price, status)
   | has one payment
   v
PAYMENT (id PK, auction_id FK, buyer_id FK, amount, status)
```

**Final picture**

```text
Web / mobile
   |                         ^ live prices
   v                         |
API Gateway            WebSocket servers ----> watchers
   |
   +--> User service --------> PostgreSQL (+ read replica)
   +--> Listing service -----> object storage (photos)
   +--> Auction service
   +--> Bid service ---------> Redis (hot auction page)
   +--> Payment service -----> Stripe / PayPal
   +--> Scheduler -----------> message queue --> Notification service
```

Bids that change the price go to the WebSocket tier. Email and SMS leave through the queue.

## Interview lines

- Conditional update: higher than current, before the end, once per bid id.
- Hot auctions are a hot row plus a WebSocket fan-out, not a sharded price.
- End the auction with an idempotent job, then pay.
- Soft-close if the product forbids sniping.

---

## Interview Q&A

### 1. How do you accept a bid safely?

<details>
<summary>Show Answer</summary>

**Answer:**

One conditional write: the new amount must beat the stored price, the auction must still be active, and the client bid id must be new. The first commit wins a tie. The bidder is told the new price if they lost. Watchers get the update on the auction's WebSocket channel. The bid history is an insert. The current price is that one row.

</details>

### 2. How does the auction end?

<details>
<summary>Show Answer</summary>

**Answer:**

A scheduler fires at `end_time`. The close job is idempotent: it sets the state to ended, records the current high bidder, and emits `auction.ended` once. Notification and payment run from that event. Retries must not charge twice. Missed timers alarm. A soft close pushes `end_time` forward if a bid lands in the last minutes, and you say that rule up front.

</details>

### 3. What breaks on a famous item?

<details>
<summary>Show Answer</summary>

**Answer:**

Ten thousand people subscribe and bid in the last seconds. The price row is the write bottleneck, so keep that transaction tiny. Fan-out is the other bottleneck: publish one event and let the WebSocket tier spread it. Cache the item page. Do not do payment or email in the bid request. Rate-limit anonymous bidders.

</details>

**Q&A recap:** one conditional price update, a real clock, idempotent close, then pay.

## Interview Q&A — 6 to 8 years

### S1. Two bids arrive with the same amount, and a third is lower but reaches the server first. Who wins?

<details>
<summary>Show Answer</summary>

**Answer:**

The server decides using its own clock and one conditional update: set price and leader only if the new bid is strictly higher than the stored price and the auction is OPEN. Equal bids do not replace the leader (first accepted stays), or I break ties by bid id. I do not trust the client's timestamp. A lower bid that was "sent earlier" on a skewed phone loses if the higher bid is already committed.

**Corner cases:** A bid in flight when the auction closes. The close and the bid are one conditional update on the same row (version or status). One of them matches zero rows. I define a small grace only if I extend the close on a last-second bid, and that extension is also a conditional update so two extensions cannot fork. A proxy bid (max amount) that must be applied without two bidders' proxies looping forever. I cap iterations and I store the max, not only the current price. A bid that arrives twice. Idempotency key so I do not apply two increments for one click.

</details>

### S2. The close job runs twice and two users are charged. What should the close have done?

<details>
<summary>Show Answer</summary>

**Answer:**

Close is idempotent. One update: set CLOSED and winner id where status is OPEN. The second run sees CLOSED and does nothing. Payment is a later step with its own idempotency key derived from the auction id, not from the job attempt. If payment fails I retry payment. I do not reopen the auction and pick a new winner because a job retried. The winner row is the source of truth.

**Corner cases:** Two closers in two regions both see OPEN because of replication lag. I need one writer or a compare-and-set with a generation. A close that charges inside the same transaction as the row lock, holding the lock during an HTTP call to the bank. I commit CLOSED first, then charge. A winner with an expired card. I notify the next bidder only with an explicit policy and a new conditional update, otherwise I leave the item unsold and visible as PAYMENT_FAILED. A scheduled job on every app node. The job takes a lease.

</details>

### S3. How do you push "you are outbid" without making the bid path slow or wrong?

<details>
<summary>Show Answer</summary>

**Answer:**

The bid path commits the price and returns. A message carries auction id, new price, and the previous leader. The notifier tells the previous leader. If the message is delivered twice, the user may see two notifications. I dedupe by (auction, price, user). If the message is late and a newer price exists, I send the latest or I drop the stale notice. I do not block the HTTP bid on the push.

**Corner cases:** Notifying every watcher of a hot auction from the bid thread, so p99 follows fan-out. Queue it. A stale client UI that bids from an old price and then shows an error. The API returns the current price on conflict (409) so the client can refresh. A spectator who should not see the bidder's identity. I define what the payload contains. Shill patterns are a product/policy problem. Technically I still have one conditional update. I can flag a user bidding against themselves if the interviewer asks, using account links, without claiming I stop all fraud.

</details>

### S4. After you shard auctions, which queries become dangerous?

<details>
<summary>Show Answer</summary>

**Answer:**

The hot path is "bid on auction id," so the shard key is auction id. Loading one auction is one shard. "All open auctions ending soon" and "all bids by this user" are scatter queries. I serve those from a secondary index or a search list that can be slightly stale, and I send the user to the owning shard to bid. I never bid against the stale copy.

**Corner cases:** A famous auction that is still one hot row after sharding. Sharding does not split a single row. I accept a single writer for that auction. A user page that must be strongly consistent ("did my bid land?") reads the primary shard, not the search index. Clock skew across shards if I ever compared end times locally. The end time is a column on the auction row, and the closer runs per shard or from a scheduler that writes back to the owning shard. A reshard in the last second of the auction. I do not move an OPEN hot auction casually. I drain or I wait until CLOSED.

</details>

**Senior recap:** one conditional price update on the server clock, idempotent close then pay, notify after commit, and shard by auction id without bidding on a stale index.

Next: [07_Rental_Airbnb.md](07_Rental_Airbnb.md)
