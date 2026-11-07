# 1. URL Shortener (TinyURL)

## Simple idea

Turn a long URL into a short one, then send people to the original when they open the short one.

```text
A person gives you a long address
        |
        v
You give back a short one, such as tinyurl.com/abc123
        |
        v
Later they open the short one
        |
        v
You send their browser to the long address
```

Creating a link is rare. Opening it is the whole business. In the notes this is called **read-heavy**: many more reads than writes.

`POST /api/shorten` means "here is a long URL, please make a short one." `GET /abc123` means "open this short one." The answer **302** tells the browser "go to this other address."

---

## Requirements

**Do**

- Accept a valid long URL and return a short one.
- Redirect the short key to the original.
- If the same long URL is submitted again, return the same short key, unless the user asked for a custom alias.
- Optional accounts: register, log in, see analytics, set expiry, delete a link you own.

**Qualities:** uptime above 99.9%, redirect in milliseconds, millions to billions of keys, no lost mappings (durable store and backups).

**Out of scope unless asked:** a full analytics warehouse, custom domains for every company.

---

## Numbers from the slides

| | Figure |
|--|--------|
| DAU / MAU | 10 million / 300 million |
| New URLs | 100,000 per day (about **1 per second**) |
| Redirects | 50 million per day (about **600 per second** average) |
| Hot cache | Top 1 million keys × 500 bytes ≈ **500 MB** |
| Bandwidth | 50M × 700 bytes ≈ 35 GB/day ≈ 0.4 MB/s average, about 5 MB/s at peak |
| Disk | 100k × 500 bytes ≈ 50 MB/day ≈ 18 GB/year raw. Plan **~50 GB/year** with indexes and backups |

Writes are easy. The redirect path and a few very popular keys are the bottleneck. Burst traffic wants autoscaling and a cache, not a bigger primary key generator.

---

## How to make the key

| Method | Result |
|--------|--------|
| Random string | Unpredictable. You must check for collisions |
| UUID | Unique, and too long to type |
| Hash of the URL | Stable for duplicates, often long, still can collide |
| **Counter, then Base62** | Short and unique if the counter is unique. **This is the slide's choice** |

Base62 uses `0-9`, `a-z`, `A-Z`. A growing integer becomes `abc123`. Seven characters is a huge space.

**Collisions.** A single database sequence cannot hand out the same id twice. You do **not** need a collision check for that path. Random keys do.

The slides use **ZooKeeper** as a global counter so many app servers do not reuse an id. That works, and it is a single coordination point. At **1 write per second** it is unnecessary. If the interviewer raises writes a lot, do not lock ZooKeeper on every id. Give each server a **block of ids** ("you own 1,000,000–1,999,999") or use a **Snowflake-style** id (time + server id + sequence). Then Base62-encode it. No central lock per request.

---

## Design

```text
Client → API gateway (auth, rate limit)
            → Shortener service   create key, reject bad URLs, save the row
            → Redirect service    cache, then database, then 302
         Redis                     hot keys
         PostgreSQL                key, long URL, owner, expiry
         Auth service              JWT for manage/delete
```

| API | Behavior |
|-----|----------|
| `POST /api/shorten` | Body is the long URL. Response is the short URL |
| `GET /{short_key}` | **302** to the long URL |
| `DELETE /api/url/{short_key}` | Owner only, `Authorization: Bearer` |
| `POST /api/auth/register` and `/login` | Accounts |

Use **302**, not 301. Browsers cache a 301 and the next click may never hit you, so analytics and expiry break. A 302 comes back each time. Put a short cache header only if you accept stale redirects.

Lookup order: Redis, then the database, then fill Redis. Expiry is a timestamp on the row. Miss or expired means 404.

Durable storage plus backups. A cache miss must still redirect. Replication for reads. The mapping itself is the source of truth.

At these numbers, **PostgreSQL plus Redis** is enough. DynamoDB is the answer when the interviewer says billions of keys and you do not want to shard SQL yourself. Do not start there for 50 GB a year.

---

## Diagrams from the slides

The first picture is one API call. The second picture is the whole product: create a link on the top path, open a link on the bottom path.

**Create a short URL**

```text
POST /api/shorten
{ "long_url": "https://www.example.com/article?id=123" }
        |
        v
   TinyURL API
        |
        v
{ "short_url": "https://tinyurl.com/my-alias" }
```

**Final design**

```text
Users (web, mobile)
   |  1 submit long URL          4 click short URL
   v
API Gateway  (auth, validate, rate limit)
   |  2 create                         |  5 redirect lookup
   v                                   v
URL Generation Service          Redirection Service
   | get next id                       | 1 check Redis
   v                                   | 2a hit  -> 302
ZooKeeper counter                      | 2b miss -> database
   unique number                       v
   | Base62                     Database (primary + replicas)
   | store mapping                    short key -> long URL
   v
Redis (hot mappings)  <---- also filled on a miss
User service: signup, login, who owns a link
```

The numbered arrows on the slide are: create goes gateway → generator → ZooKeeper → database and Redis. Open goes gateway → redirect service → Redis, then the database only on a miss, then HTTP 302.

## Interview lines

- Read path is the product. Cache the hot keys. 302 so you still see the click.
- Base62 of a unique id. A global lock per id does not match 1 write per second.
- Same long URL can map to one short key. Custom aliases are the exception and must be reserved in a unique column.

---

## Interview Q&A

### 1. How do you generate the short key?

<details>
<summary>Show Answer</summary>

**Answer:**

Take a unique integer and encode it in Base62 so it is short. One database sequence is enough at the slide's 100,000 creates per day. Across many servers, hand each server a range of ids, or build a Snowflake id. ZooKeeper as a counter on every request is the slide's coordination example, and it becomes a bottleneck if writes grow. Random strings need a collision check. UUIDs are unique and too long. Hashing the URL helps dedupe and is a poor way to get a short key.

</details>

### 2. Why is the redirect path special?

<details>
<summary>Show Answer</summary>

**Answer:**

About 600 redirects per second versus about 1 create. Keep a few hundred megabytes of hot keys in Redis. The service is stateless behind a load balancer. Return 302. The database is the durable copy. Replicas and backups cover loss. A CDN can cache public redirects only if you still meet analytics and expiry rules.

</details>

### 3. How do you stop two users creating the same key?

<details>
<summary>Show Answer</summary>

**Answer:**

The key comes from a unique id, so the encoding does not collide. A custom alias is a separate unique column. Insert fails if it is taken. Deduping the long URL is a lookup before insert, and it races. Use a unique constraint on the long URL if one short link per URL is a hard rule, or allow many short links and skip that feature.

</details>

**Q&A recap:** unique id, Base62, cache the read, 302.

## Interview Q&A — 6 to 8 years

### S1. Two app servers create links at the same time. How do ids collide, and how do they not?

<details>
<summary>Show Answer</summary>

**Answer:**

A single database sequence does not collide and becomes a bottleneck only at a write rate the interview may push up. If I hand each server a range, a crashed server can lose a range (holes are fine) or, if I reissue the same range, I get duplicates. Snowflake-style ids collide if clocks go backwards or two nodes share a worker id. I persist the node id and I refuse to issue until the clock catches the last timestamp I gave out.

**Corner cases:** A custom alias inserted without a unique constraint, checked with a read-then-write. Both succeed. Hashing the long URL into a short key collides and also leaks whether two users shortened the same private link if I dedupe globally. I dedupe per user, or I do not dedupe. Base62 of a guessable sequence lets someone walk every link. If links can be private, I add unguessable randomness and I accept the collision check.

</details>

### S2. The redirect cache is wrong. What does the user hit, and for how long?

<details>
<summary>Show Answer</summary>

**Answer:**

A stale Redis entry sends people to the old long URL until TTL or until I delete the key. An update or a disable must delete the cache key in every region, not only the database row. I use a short TTL for links that can change and a long TTL for immutable public ones. If Redis is down, I read the database and I cap concurrency so a cache outage does not become a database outage. I do not fail the whole redirect product if I can still serve from the primary.

**Corner cases:** A negative cache ("this key does not exist") that sticks after the user just created the link, so their own click 404s. I do not negative-cache a key this user just wrote, or I delete the negative entry on create. A CDN in front of the 302 that caches a redirect to a now-banned URL. I mark personal or banned links uncacheable at the CDN. Cache key without normalization: `Abc` and `abc` are different entries and one of them bypasses the ban.

</details>

### S3. How do you count clicks without slowing the redirect?

<details>
<summary>Show Answer</summary>

**Answer:**

The redirect path stays a cache lookup and a 302. I append a click event asynchronously (a local buffer, then a stream). Counts are eventually right. I do not update a row on the primary for every click. If the product needs exact billing per click, I say so and I accept a slower path or a log that is the source of truth, with late aggregation.

**Corner cases:** A retry of the redirect double-counts unless I key the event by request id, and browsers will retry. A bot flood inflates counts. I separate human and bot only if the product requires it, and I say the count is approximate. Losing the buffer on a crash loses a few seconds of clicks. I state that RPO. A popular link's counter is a hot key if I increment one row. I shard the counter by link id plus a bucket, then sum.

</details>

### S4. What do you do about abuse: spam links, open redirects, and a taken-down target?

<details>
<summary>Show Answer</summary>

**Answer:**

I store the long URL and a status. Redirect checks status before it 302s. I allow-list schemes (https only, no `javascript:`). I rate-limit create per user and per IP. I can disable a key without reusing it, so an old shared link stays dead. Taken-down targets return a simple error page, not the original URL in a way that auto-loads it.

**Corner cases:** An open redirect if I ever 302 to a user-supplied URL that is not the stored one (a `?next=` parameter). A blocklist that lags, so the first minutes of a spam campaign work. I still have the disable path. Reusing a deleted short key later hands the old audience to a new owner. I tombstone keys. A preview bot that follows the link and triggers a side effect on the destination. My 302 will be followed. I do not put destructive actions on the destination's GET, and I do not count preview fetches as user clicks if I can tell them apart. Homograph URLs that look like a bank. I show the real host on an interstitial for suspicious links if the product needs it.

</details>

**Senior recap:** unique ids with a real owner, cache invalidation including negative cache, async click counts, and tombstones so a short key is never silently reused.

Next: [02_Ticketing.md](02_Ticketing.md)
