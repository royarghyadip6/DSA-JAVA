# 11. E-Commerce (Amazon-style MVP)

## Simple idea

Buyers search a catalog, put items in a cart, and pay. Sellers list stock. The hard part is **the last unit is sold once**, on a system small enough that a tiny team can run it.

The slides are an **MVP**, not the whole of Amazon. No warehouses. Sellers ship. Fraud rules are simple, not a big model. About a 3–4 month build.

---

## Requirements

Register and log in (buyer, seller, admin). Catalog. Cart and checkout. Inventory that stays true. Payments. Basic fraud alerts. A seller view of orders and payouts.

**Qualities:** search and browse under about 300 ms, about 10,000 concurrent users at the start, **99.9%** uptime, OAuth or JWT, card handling that stays inside a payment provider (PCI), **strong consistency on stock**.

**Constraints:** cloud, not a private data center. Little SRE time, so managed databases. Keyword search, not a recommendation empire. GDPR for personal data.

---

## Numbers

| | Figure |
|--|--------|
| Registered / DAU | about 1 million / about 10,000 |
| Peak | about **100 requests per second** |
| Catalog | about 500,000 products |
| Orders | about 1,000 per day |
| Sellers | about 5,000 |

This is a modest write rate. The dangerous moment is a **flash sale on one popular SKU**, not the daily average. Search latency and that stock row are the bottlenecks. Checkout must survive a slow gateway. Payment errors need a human-visible state, not a silent retry that charges twice.

---

## Stock and checkout

```text
Cart                 can live in Redis. It is not a sale.
Reserve              UPDATE stock SET qty = qty - n
                     WHERE product_id = ? AND qty >= n
Order                same transaction as a successful reserve, status = pending payment
Pay                  Stripe or Razorpay, idempotency key
Success              order = paid, tell the seller
Fail or timeout      put the units back
```

If zero rows update, you are out of stock. Do not decrement Redis and hope the database catches up. You may **cache** the displayed quantity. The reserve hits PostgreSQL.

Search is Elasticsearch, fed from the catalog. It can lag. The product page can show a cached price. Checkout re-reads stock.

Events after the money decision: email, seller notification, fraud review. Kafka, SQS, or RabbitMQ. The buyer already has an order id.

---

## Services and APIs

User, catalog, inventory, orders, payments, notifications, admin.

| API | Job |
|-----|-----|
| `POST /register`, `POST /login`, `GET /profile` | Identity |
| `GET /products`, `GET /products/{id}`, `POST /products` | Catalog |
| `GET /inventory/{productId}`, `POST /reserve-stock` | Stock |
| `POST /cart/add`, `POST /checkout`, `GET /order/{id}` | Buy |
| `POST /payments/initiate`, `POST /payments/verify` | Pay |

REST, stateless, versioned (`/v1`). Sync for catalog, reserve, and pay. Async for mail and "tell the seller".

PostgreSQL for users, stock, orders, and payment records (no raw card numbers). Elasticsearch for search. Redis for hot products and sessions. S3 for logs and images. OAuth2 or OpenID Connect, JWT for the session.

---

## Final design

Each colored box is one job with its own database. The person only sees the gateway. A fast copy (Redis) sits in front of each database for browsing. The real stock change is still the PostgreSQL update. Mail leaves through the event bus at the bottom.

```text
Web / mobile
    |
    v
API Gateway (route, auth, rate limit)
    |
    +--> User ----> Redis + PostgreSQL
    +--> Catalog -> Redis + PostgreSQL --> Elasticsearch
    +--> Inventory > Redis + PostgreSQL
    +--> Orders ---> Redis + PostgreSQL
    +--> Payments -> Redis + PostgreSQL --> Stripe / Razorpay
    |
    | events: user registered, product updated, stock changed,
    |         order created, payment completed
    v
Kafka / RabbitMQ / SQS --> Notification workers --> email and SMS
```

Each box has its own cache in front of its own tables. The sale still commits in PostgreSQL. Redis is the fast read.

## Interview lines

- This MVP is 100 requests per second and 1,000 orders a day. Say so, then show the flash-sale path anyway.
- Conditional stock update in SQL. Cache is for the browsing page.
- Payments are an external gateway plus an idempotency key.
- Search index is not the inventory.

---

## Interview Q&A

### 1. How do you prevent overselling?

<details>
<summary>Show Answer</summary>

**Answer:**

Reserve with a conditional update: subtract only if `qty` is still at least the requested amount. Do it in the same transaction as creating a pending order. If payment fails, add the units back. A retry uses the same idempotency key. Redis may show an approximate count on the product page. It does not make the sale.

</details>

### 2. Why so many services if the traffic is small?

<details>
<summary>Show Answer</summary>

**Answer:**

The slide's launch numbers fit a modular monolith. Split on purpose where the work differs: catalog search, the stock transaction, and the payment gateway. Keep one PostgreSQL for orders and stock so the reserve is a real transaction. Add Elasticsearch for text search. Do not add a service per table. The constraint is a small team and a few months.

</details>

### 3. What must be strongly consistent?

<details>
<summary>Show Answer</summary>

**Answer:**

Stock and the payment outcome. A search document, a "customers also bought" list, and the confirmation email can lag. 99.9% uptime is about 8.8 hours a year, so a managed database with a standby matches the goal better than a multi-region active-active ledger you cannot operate.

</details>

**Q&A recap:** conditional stock update, pay outside with an idempotency key, search on the side.

## Interview Q&A — 6 to 8 years

### S1. The cart reserved stock, the user paid, and a second tab paid too. Where is the invariant?

<details>
<summary>Show Answer</summary>

**Answer:**

Stock is a conditional update: decrement where sku = ? and qty >= requested. Payment is outside that transaction, with an idempotency key per checkout, not per tab click. The order row goes PENDING, stock is reserved or decremented, then payment runs, then the order is PAID. A second pay with the same key returns the same order. A second pay with a new key finds no stock or finds the existing open order for that cart and refuses.

**Corner cases:** Decrement in Redis and payment in SQL, and they disagree after a crash. One source of truth for "can I sell this," which should be the same place as the order commit, or a reservation row in SQL. A cart that holds stock for a day and empties the warehouse. Short reservations. A payment success webhook after I released the reservation for timeout. I fulfill from remaining stock or I refund. I do not do both. Quantity 2 where one item fails a per-item constraint (one is a preorder). I define all-or-nothing in one transaction. Partial carts need a stated policy. Oversell because I checked stock in the search index. Search is not the stock check.

</details>

### S2. A flash sale has one SKU. Sharding the catalog does not save you. What does?

<details>
<summary>Show Answer</summary>

**Answer:**

That SKU is one row. I keep its update single-threaded and tiny. I queue the right to attempt the purchase, or I sell a limited batch of reservation tokens created ahead of time (one row per token, spread if I must). App servers scale the waiting room, not the stock row. The page can show "almost gone" from a cache. The button uses the conditional update.

**Corner cases:** A waiting room that lets 50,000 through and then times out payment, returning stock in a thundering herd of releases. I cap in-flight checkouts. Bots with many accounts. Per-user and per-payment limits. A cache that says in stock and a checkout that always fails, which is correct and feels broken. I refresh the cache when the conditional update hits zero. Releasing stock twice on a double timeout. The release is conditional (increment only if this reservation is still HELD). A global lock on the whole catalog because of one SKU. I lock the SKU key only.

</details>

### S3. How do prices, taxes, and a coupon stay consistent with what the user saw?

<details>
<summary>Show Answer</summary>

**Answer:**

I snapshot the price, currency, and coupon onto the order at checkout start, and I re-validate the coupon once. If the catalog price changes after, the snapshot still stands for a short hold, or I fail and ask the user to confirm. I compute tax from the snapshot and the ship-to address in the server, not from a hidden form field. The payment amount must equal the snapshot total.

**Corner cases:** A client-supplied price. I ignore it. A coupon that was used twice because two checkouts read "unused" before either wrote. Conditional update on the coupon. A currency mix-up that charges the numeric amount in the wrong currency. The snapshot stores the currency code and the charge uses it. Rounding differences of one cent between preview and charge. I charge the previewed total, not a recomputed one. A promotion engine call inside the stock transaction that holds the row while a remote service is slow. I price first, then the short stock transaction.

</details>

### S4. Search, cart, and warehouse are different stores. What do you rebuild after a bad deploy?

<details>
<summary>Show Answer</summary>

**Answer:**

The order and stock tables are the source of truth. Search and the warehouse view are projections. After a bad deploy I restore SQL to a point before the bad write if the data was corrupted, then I rebuild search from SQL, and I replay or cancel warehouse jobs using the order id. I do not restore search and hope SQL matches. Payment provider state may be ahead of my restore. I reconcile by payment id and I refund or fulfill. I do not blindly replay "charge" messages from an old queue.

**Corner cases:** A queue full of charge messages that becomes live after restore and charges again. I dedupe by idempotency key and I drain the queue before I open the register. An inventory adjustment in the warehouse system that was not in SQL. I need one owner. A partial refund restored as a full order. The restore point and the payment provider report must be compared before I email customers. A search document that still shows a banned product. The query path also checks a deny list or the SQL status for sensitive cases, not only the index.

</details>

**Senior recap:** conditional stock plus a checkout idempotency key, a waiting room for one hot SKU, a server-side price snapshot, and SQL as the truth you restore first.

Next: [12_Taxi_Uber.md](12_Taxi_Uber.md)
