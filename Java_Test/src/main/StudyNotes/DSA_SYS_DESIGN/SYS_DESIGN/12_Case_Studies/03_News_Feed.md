# 3. News Feed (Twitter / Instagram)

## Simple idea

Alice follows Bob. Bob posts. Alice's home screen shows that post. The design question is **when** you build Alice's list: when Bob posts, or when Alice opens the app.

---

## Requirements

Post, follow and unfollow, home timeline, likes, replies, reposts, images and video.

The feed should load fast, feel fresh, and not drop or duplicate posts. High availability, low latency, huge scale.

---

## Numbers from the slides

| | Figure |
|--|--------|
| Users / DAU | 500 million / 200 million |
| Posts | about 1 billion per day |
| Media uploads | about 300 million per day, **100–500 TB/day** |
| Engagement | about 1 billion likes, 500 million replies, 250 million reposts per day |
| Feed opens | 2 billion per day |

About **80% of traffic is reads**. Media goes to object storage (S3 or GCS) and out through a CDN. The post stores the media id, not the bytes.

---

## Fan-out

```text
Fan-out on write:  Bob posts → push the id onto each follower's timeline
Fan-out on read:   Alice opens the app → merge recent posts from people she follows
Hybrid:            push for normal people, merge at read time for celebrities
```

| | Write | Read |
|--|-------|------|
| **On write** | Heavy. A star with 1 million followers does 1 million timeline inserts | Cheap. The timeline is already there |
| **On read** | Cheap. Store the post once | Heavy. You read many followees and sort |
| **Hybrid** | Push to ordinary followers. Skip or sample the huge accounts | Those huge accounts are merged when the feed is opened |

The slides call the celebrity case a **write storm**. That is the bottleneck. Likes and reposts also multiply writes (counters and notifications). A media upload is its own pipeline: chunk, store, attach the id.

Do the push in a **background worker**, not inside the "Post" HTTP call. The user gets "posted" once the tweet row exists. Followers catch up from the queue.

---

## Design

```text
App → API gateway (auth, rate limit)
        User service          follow graph
        Tweet service         create and fetch a post
        Timeline service      the home list
        Fan-out workers       consume "tweet created"
        Engagement service    like, reply, repost
        Media service         upload, S3, CDN URL
        Notification service  alerts

Cassandra or DynamoDB    posts and precomputed timelines (huge write volume)
PostgreSQL or Redis      counters and small engagement sets, depending on consistency
Redis                     hot timelines and posts
Kafka                     fan-out, media, notification events
```

| API | Job |
|-----|-----|
| `POST /tweet` | Text plus media ids |
| `GET /timeline` | Latest posts for the caller |
| `POST /follow` and unfollow | Change the graph |
| `POST /like`, `/retweet`, `/reply` | Engagement events |
| `POST /media/upload` | Returns a CDN URL |

APIs are stateless. The token is in the header.

**Sync:** open timeline, read one tweet, submit a like. **Async:** fan-out, media processing, notification.

A like counter can be slightly late. A follow graph used for the next fan-out should not lose the edge. Say that split.

---

## Final design

The person posts through the gateway. Saving the tweet is the straight path. Filling every follower's home screen happens later, through the queue in the middle. Photos go to file storage and the CDN, not into the tweet database.

```text
Phone / web / desktop
        |
        v
API Gateway (auth, route, rate limit)
        |
        +----------+----------+----------+----------+
        v          v          v          v          v
      User       Tweet     Timeline    Media    Engagement
        |          |          |          |          |
        |          +---- publish "tweet created" ---+
        |                         |
        |                         v
        |               Kafka or Pulsar
        |                    |            |
        |                    v            v
        |            Fan-out workers   Notification workers
        |            (timelines)       (push, email)
        |
        v
Redis cache on the hot timelines and tweets

Stores, off to the side:
  users        PostgreSQL
  tweets       Cassandra
  timelines    Redis or a key-value store
  media        S3 / GCS + CDN back to the phone
  engagement   Cassandra
```

Solid arrows are the request. Dashed arrows are the event after the post is saved.

## Interview lines

- Name fan-out on write, on read, and the hybrid before you name Cassandra.
- Celebrities are not pushed to every follower.
- Media is object storage plus a CDN. The feed stores ids.
- The post request does not wait for a million timeline inserts.

---

## Interview Q&A

### 1. Fan-out on write or on read?

<details>
<summary>Show Answer</summary>

**Answer:**

On write, you precompute each follower's timeline when the post is saved. Reads are a single fetch. Writes explode for a celebrity. On read, you store the post once and merge followees when the app opens. Writes stay small. Reads get slower as you follow more people. The practical design is hybrid: push to normal followers, and pull celebrities at read time.

</details>

### 2. What is the bottleneck?

<details>
<summary>Show Answer</summary>

**Answer:**

One post to a million followers, plus likes and reposts that touch many counters, plus hundreds of terabytes of media a day. Put fan-out and media on Kafka. Cache hot timelines in Redis. Store posts in a store that shards by user or tweet id. Serve bytes from a CDN. Protect the request path from the fan-out work.

</details>

### 3. How does Alice see Bob's post quickly?

<details>
<summary>Show Answer</summary>

**Answer:**

Bob's post is saved. An event tells workers to append that id to follower timelines, except for accounts so large that the merge happens when Alice calls `GET /timeline`. Her app reads her precomputed list, hydrates the tweets, and fills media from the CDN. A brand-new follow may be slightly late. That is the tradeoff of doing the work asynchronously.

</details>

**Q&A recap:** hybrid fan-out, queue the push, CDN for media.

## Interview Q&A — 6 to 8 years

### S1. A user with 40 million followers posts. Pure fan-out on write melts. What do you switch, and what do you keep?

<details>
<summary>Show Answer</summary>

**Answer:**

I keep fan-out on write for normal users so their followers' feeds are precomputed. For a whale, I store the post once and merge it at read time for the viewers. The read path is: precomputed feed plus a small set of whale posts, sorted by time, then truncated. I do not copy 40 million rows synchronously on the POST. The POST returns when the post row is durable. Fan-out happens on a queue.

**Corner cases:** The whale set changes. Someone crosses the threshold mid-fan-out. I finish the old way or I cut over at a version, and I do not do both forever. A read that merges too many whales and misses the latency SLO. I cap the merge and cache the merged page per user for a few seconds. Ranking that cannot be precomputed (every view is personal) forces more work onto the read. I precompute candidates and rank a small list. A delete of the whale post that is still cached in millions of merged pages. Short TTL plus a tombstone on read.

</details>

### S2. The fan-out worker crashes at follower 500,000 of 2 million. What happens on restart?

<details>
<summary>Show Answer</summary>

**Answer:**

The job is idempotent. Each (post id, follower id) insert ignores duplicates. Progress is a cursor, not "I hope the process finishes." Restart continues after the cursor. Followers who already got the post do not get a second copy. If I cannot guarantee the cursor, a full retry is still safe because of the unique key.

**Corner cases:** A follower who follows after the cursor passed them misses the post. I define the rule: fan-out uses the follower list at start, and later follows see the post only on the pull path (read from the author), or I accept a missed push. Unfollow during fan-out leaves a post in a feed. The read path filters blocks and unfollows so I do not have to delete millions of rows synchronously. A poison post (bad media) that retries forever and blocks the partition. Dead-letter that post and keep the feed moving. Ordering: a later post appears above an earlier one because its smaller fan-out finished first. Sort by post time, not by arrival.

</details>

### S3. How do you paginate a feed that is still receiving inserts?

<details>
<summary>Show Answer</summary>

**Answer:**

A cursor of (time, post id), not an offset. New posts arrive at the top and do not shift page two into duplicates. I do not use `OFFSET` on a growing table. Pull-to-refresh loads newer than the top cursor. The next page loads older than the bottom cursor. Deleted items may leave a hole. That is acceptable. A duplicate from at-least-once fan-out is removed by the unique key.

**Corner cases:** Two posts with the same timestamp. The second key (post id) makes the cursor stable. A user with an empty precomputed feed and a whale merge that changes between page one and page two, causing a skip. I merge inside a single cached snapshot for the session's first page and then page inside that snapshot, or I accept a rare duplicate and dedupe by id on the client. A celebrity's post inserted into page one while the client shows a stale first page. Refresh is the fix, not a perfect live view. Cache per user that ignores a privacy change (a private account) for the TTL. I check the author's visibility on read for private posts, or I purge.

</details>

### S4. Media on the post is huge and the feed API becomes slow. What moves out?

<details>
<summary>Show Answer</summary>

**Answer:**

The feed stores post ids and a small text payload. Images and video are URLs on a CDN, uploaded separately, transcoded async. The feed API does not proxy the bytes. A post can be published before transcoding finishes, with a placeholder. Rank and fan-out do not wait for the largest rendition.

**Corner cases:** A CDN URL that is public and guessable for a private account's photo. I use an auth check or a short-lived signed URL for private media. A post row written and the upload failing, so the feed shows a broken image forever. The client retries the upload against the same post id. A takedown that deletes the database row and leaves the CDN object. Purge and a tombstone. Feed ranking that fetches user features N+1 on every post. I batch those lookups or I denormalize a small feature vector onto the feed item.

</details>

**Senior recap:** hybrid fan-out with an idempotent cursor, page by (time, id), and keep media off the feed request path.

Next: [04_Notification_System.md](04_Notification_System.md)
