# 2. Caching

## Simple idea

A **cache** keeps a copy of something you need often, in a place that is faster than the source.

```text
First request   →  slow path (database, another service)  →  save a copy
Later requests  →  read the copy
```

You pay with memory and with the risk of a **stale** answer. You gain lower latency and a quieter database.

---

## Where the copy lives

| Layer | What it stores | Example |
|-------|----------------|---------|
| **Browser** | Static files, and sometimes API responses | HTTP cache, service worker |
| **CDN** | Images, JS, CSS, public pages, close to the user | Cloudflare, Akamai |
| **App / server** | Hot objects and computed answers | Redis, Memcached, a local map |
| **Database** | A saved query result | Result cache, materialized view |

The slides list `localStorage` as a client cache. Treat that carefully. `localStorage` is a small key-value box your JavaScript writes on purpose. It is **not** the browser's HTTP cache, and it is a bad place for secrets or tokens (see [web sessions](../05_Web_Concepts/01_Web_Sessions.md)). For static files, rely on HTTP cache headers and a CDN.

---

## How writes meet the cache

| Strategy | What happens | Good | Risk |
|----------|----------------|------|------|
| **Cache-aside** (lazy load) | On a miss, the app reads the DB and then fills the cache | Only hot keys use memory | You must invalidate on updates |
| **Write-through** | Write cache and database before you say success | Cache matches the DB | Writes are slower |
| **Write-back** (write-behind) | Write the cache now. Flush to the DB later | Very fast writes | A cache crash can lose data |
| **Manual** | Your code decides put and delete | Full control | Easy to forget a path |

```text
Cache-aside read:

  app → cache?  hit  → return
              miss → database → put in cache → return
```

Use cache-aside when most rows are cold and only some products or users are hot. Use write-through when a stale read is expensive and you can afford a slower write. Use write-back only when losing the newest writes is acceptable, or when the cache itself is durable.

---

## Eviction

The cache is smaller than the database. Something has to leave.

| Policy | Drop this | Use when |
|--------|-----------|----------|
| **LRU** | Least recently used | Recent items tend to be used again (the usual default) |
| **LFU** | Least frequently used | A few items are popular all day |
| **FIFO** | Oldest insert | Simple, ignores how often it is read |
| **TTL** | Anything past its expiry | You want data to refresh even if nobody updates the cache |

TTL is not only an eviction trick. It is also how you bound staleness: "this price is allowed to be at most 60 seconds old."

Add a little random jitter to TTL so a million keys do not expire on the same second.

---

## Redis

Redis is an in-memory store. A get is a memory read, not a disk query.

People use it for cache, sessions, rate limits, leaderboards, and short queues. It supports TTL, pub/sub, and optional persistence (so a restart need not wipe everything).

In a Java service the pattern is:

```text
key = "product:123"
GET key
  miss → load from PostgreSQL
       → SET key value EX 300
  hit  → return the JSON
```

Spring can hide this behind a cache annotation. You still own the TTL and the invalidation rules.

**Distributed cache tools**

| Tool | Role |
|------|------|
| Redis, often as Redis Cluster | Shared cache, TTL, and more data types |
| Memcached | Simple memory cache, fewer features |
| Hazelcast, Apache Ignite | Cache that can live inside the JVM process |

One app instance with a local `HashMap` is not a shared cache. The next server still hits the database. Put shared data in Redis (or similar) and spread keys with consistent hashing, the same idea as in [sharding](../07_Storage_Database/03_Replication_Sharding_Polyglot.md).

---

## Product page

```text
Images          →  CDN
Product JSON    →  Redis, cache-aside, TTL
Stock count     →  database, or a very short TTL
On edit         →  delete product:123  (or publish an event that deletes it)
```

Do not cache "items left: 1" for ten minutes if you can oversell.

---

## Staying in sync

- **Write-through** if the next read must see the write.
- **Delete the key** when the row changes (in the same request, or from a message).
- **TTL** so a missed delete still heals.
- **Change data capture** or a domain event when many services cache the same row.

Prefer **delete** over "update the cached JSON in every place". A missing key is refilled from the database. A half-updated cache object lies.

---

## Cache stampede

A hot key expires. A thousand requests miss at once and all hit the database.

Ways to stop that:

- Only **one** request reloads the key (a lock, or singleflight). The others wait or get the old value.
- **Stale-while-revalidate**: serve the expired copy once, and refresh in the background.
- **Jitter** the TTL so keys do not die together.

---

## Downsides

| Risk | What you see |
|------|----------------|
| Stale data | User sees an old price or an old name |
| Stampede | The database gets the traffic you tried to avoid |
| Memory waste | You cached cold keys, hot ones got evicted |
| Complexity | Bugs in invalidation are worse than a slow query |

Cache data that is read a lot, expensive to compute, and allowed to be slightly old. Do not cache as a reflex.

---

## Interview lines

- A cache is a fast copy. It cuts latency and database load, and it can be stale.
- Browser and CDN for files. Redis for hot application data.
- Cache-aside fills on a miss. Write-through writes both. Write-back writes the cache first.
- LRU is the usual eviction. TTL bounds how old a value may be.
- On update, delete the key. Stop stampedes with a single refill, stale-while-revalidate, or jittered TTL.

---

## Interview Q&A

### 1. What is caching, and why use it?

<details>
<summary>Show Answer</summary>

**Answer:**

Store a frequently used result in a faster layer, usually memory. Reads get cheaper, the database does less work, and the site stays responsive under load. The cost is memory and possible stale data.

</details>

### 2. Types of caching?

<details>
<summary>Show Answer</summary>

**Answer:**

- **Client** — browser HTTP cache and service workers for static assets
- **CDN** — the same assets, close to the user
- **Server** — Redis or Memcached for API payloads, sessions, computed results
- **Database** — cached query results or a materialized view

`localStorage` is app storage, not the HTTP cache.

</details>

### 3. Write-through vs write-back?

<details>
<summary>Show Answer</summary>

**Answer:**

Write-through updates the cache and the database before success. Reads stay fresh. Writes are slower.

Write-back updates the cache and flushes the database later. Writes are fast. If the cache dies before the flush, those writes are gone.

</details>

### 4. What is cache-aside (lazy loading)?

<details>
<summary>Show Answer</summary>

**Answer:**

The application checks the cache. On a miss it reads the database, stores the value, and returns it. Use it when only some keys are hot and memory is limited. You must invalidate or set a TTL, or updates never show up.

</details>

### 5. How would you cache a product details page?

<details>
<summary>Show Answer</summary>

**Answer:**

CDN for images. Redis for the product document with a TTL, filled on demand (cache-aside). Delete or refresh `product:{id}` when the product changes. Keep fast-changing fields such as stock on a short TTL or read them from the database.

</details>

### 6. Which eviction policy?

<details>
<summary>Show Answer</summary>

**Answer:**

LRU when recent use predicts the next use. LFU when a small set is popular all day. FIFO is simpler and ignores popularity. TTL expires data by age so it cannot stay stale forever. Match the policy to the access pattern. A random TTL jitter avoids a mass expiry.

</details>

### 7. How do you keep cache and database in sync?

<details>
<summary>Show Answer</summary>

**Answer:**

Write-through when they must move together. Otherwise delete the key on update (in the request, from a queue, or from change data capture) and let the next read refill it. A TTL is the safety net when a delete is missed.

</details>

### 8. Risks of caching too much?

<details>
<summary>Show Answer</summary>

**Answer:**

Stale reads, a stampede when a hot key expires, memory filled with cold keys, and invalidation logic that is harder than the original query.

</details>

### 9. How do you add Redis in a web app?

<details>
<summary>Show Answer</summary>

**Answer:**

Use a key such as `product:123`. On a miss, load from the database and `SET` with a TTL. On a hit, return Redis. In Java, a Redis client or Spring Cache does the get-and-put. You still choose the key, the TTL, and when to delete.

</details>

### 10. How do you prevent a cache stampede?

<details>
<summary>Show Answer</summary>

**Answer:**

Let one caller rebuild the key and hold a short lock. Others wait, or you serve a stale value while one refresh runs (stale-while-revalidate). Spread expiry with jittered TTLs so keys do not all vanish together.

</details>

### 11. Tools for a distributed cache?

<details>
<summary>Show Answer</summary>

**Answer:**

Redis Cluster for a shared, highly available cache. Memcached for a simple one. Hazelcast or Apache Ignite when the cache sits in the application JVMs. Spread keys with consistent hashing, and replicate if a node death must not drop the only copy.

</details>

**Q&A recap:** cache hot, expensive, slightly-stale-ok data. Delete on write. Do not let one expiry stampede the database.

## Interview Q&A — 6 to 8 years

### S1. A cache stampede takes the database down at exactly the TTL. How do you stop the next one?

<details>
<summary>Show Answer</summary>

**Answer:**

One request rebuilds the key. The others wait or get a slightly stale value. I add jitter so a million keys do not expire on the same second. I serve stale-while-revalidate. I do not set one global TTL from a deploy that just filled the cache together. Hot keys get request collapsing (a lock or a single-flight per key) so 5,000 misses become one query.

**Corner cases:** The lock holder dies and the lock TTL is long, so the key stays missing. I set a short lock and I let a second request rebuild. The lock is on one app node only, and 40 nodes still stampede. The lock must be shared (Redis) or each node needs a local single-flight plus a shared one. A cache failure that fails all reads, not only misses. I decide fail-open (go to the database with a limit) or fail-closed (error the page) per route. Caching a personalized page under a shared key.

</details>

### S2. You delete the cache key on write and users still see the old value. Where else is it?

<details>
<summary>Show Answer</summary>

**Answer:**

A replica built the cache from stale data after my delete (the race: delete, then a slow read fills it again). A second layer (CDN, browser, another region's cache) still has it. I version the key (`product:42:v9`) so old entries die unused. Or I delay the refilling read until the replica catches up. Browser and CDN need their own short TTL or a purge I can name.

**Corner cases:** Two writes race and the older one deletes the newer cache entry last, then a reader repopulates from the older replica. Versioned writes (compare-and-set on a version) fix that. A delete that fails and I still return 200 on the write. The next read resurrects the old object for the whole TTL. I treat cache delete failure as "retry the delete," not as success. A key that includes a timestamp rounded to the minute, so I cannot find it to delete it.

</details>

### S3. When is a cache the wrong tool even if the page is slow?

<details>
<summary>Show Answer</summary>

**Answer:**

When the working set is bigger than memory, so the hit rate cannot be high. When the data must be correct (a payment balance, a permission you just revoked) and I cannot purge reliably. When each key is read once (no reuse). I fix the query or I precompute in the background instead. A cache that misses most of the time adds a network hop and a new outage mode.

**Corner cases:** A hot key that does not fit the "just add Redis" story because every app node still waits on one Redis slot. I split the value or I replicate the read. An item bigger than the cache's max value (a 100 MB JSON). I cache an id list, not the blob. Serialization CPU that costs more than the query. A cache used as a lock or a queue. It will evict the "lock." I use a real lock with a lease.

</details>

### S4. How do you size and evict without guessing on stage?

<details>
<summary>Show Answer</summary>

**Answer:**

I measure hit rate, eviction rate, and memory. LRU is a good default when recent keys repeat. TTL is how I bound staleness even if the key stays hot. I watch a few huge values that push out many small useful ones (I cap value size). I load-test with a realistic key distribution, including one hot key, not with uniform random ids.

**Corner cases:** LFU that pins a key which was hot yesterday and is now a 404. TTL of zero effectively disables the cache and I think the database got slower. A flush of the whole cache during a deploy, which is a stampede I scheduled. Replication lag between cache primary and cache replica serving a stale session right after logout. Eviction is not a security boundary. Logout must delete the session key, not wait for LRU.

</details>

**Senior recap:** single-flight plus jitter, version keys so a stale refill loses, do not cache what you cannot safely reuse, and hit rate beats a bigger cluster you did not measure.

Next: [03_Messaging_and_Queues.md](03_Messaging_and_Queues.md)
