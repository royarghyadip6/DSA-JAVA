# 9. Video Sharing (YouTube)

## Simple idea

A creator uploads once. The system makes several sizes of the video. Viewers play the size their network can handle, from a CDN close to them. Likes and recommendations must not sit on that play path.

The slides assume **short, on-demand** video, not live TV. Up to about 15 minutes, qualities from 240p to 4K.

---

## Requirements

Accounts, upload, transcode, stream with adaptive quality, title and tags, likes, comments, views, subscriptions, search, a home feed.

**Qualities:** low start time, videos stay available, billions of videos as a direction, storage cost under control, global CDN, abuse and copyright checks.

---

## Numbers from the slides

| | Figure |
|--|--------|
| Users | 100 million |
| Uploads | **10 million videos per day** |
| Views | 500 million a day |
| Likes, comments, shares | 100 million events a day |
| Length | about 10 minutes |
| Raw size | about 5 MB per minute → **50 MB** per video → **500 TB/day** raw |
| Encoded | about 4 renditions, roughly **3×** → about **1.5 PB/day** |
| A month of encoded video | on the order of **tens of PB** |
| Watch time | about 300 million hours a day |
| Egress | about **135 PB/day** if you assume ~0.45 GB per hour |
| Peak | 10 million concurrent streams at 1 Mbps → about **10 Tbps** |

These are "YouTube-sized" assumptions. Say them, then design the pipeline. A smaller product uses the same shape and a smaller CDN bill.

The slide line "500B per action" for engagement does not fit the rest of the table (100 million like and comment events a day). Treat one event as a **few hundred bytes**, not 500 billion of anything.

Metadata is tiny next to video (about 1 KB per video). The bill is **storage and egress**.

---

## The path

```text
Upload (chunked, presigned URL)
  → object storage, status = processing
  → queue
  → workers transcode 240p / 480p / 720p / 1080p
  → HLS or DASH segments + a manifest + a thumbnail
  → CDN
Player reads the manifest and switches quality as bandwidth changes
```

The upload API returns when the bytes are stored, not when every rendition is done. The watch page shows "processing" until the playable set exists.

**Views.** Do not `UPDATE videos SET views = views + 1` on the hot row for every play. Log a view event. Aggregate in the background. The counter can be seconds late.

**Search and home feed** read metadata and a recommendation model. Recommendations are offline or near-line. Playback does not call the model.

**Abuse and copyright** are async scans. They can pull a video later. Say that a public CDN URL should be hard to guess or tied to a check if the video is private.

---

## Design

```text
API gateway
  Upload / ingestion
  Encoding workers          autoscale, GPUs if you need them
  Metadata                  title, tags, owner, status, CDN path
  Engagement                likes, comments, view events
  Search                    Elasticsearch or similar
  User                      OAuth or JWT, subscriptions
  Recommendation            separate from the play API

PostgreSQL     video and user rows
Object storage the segments
CDN            the play
Redis          hot metadata
Kafka          encode jobs and engagement events
```

| Call | Job |
|------|-----|
| `POST /upload` | Start upload, get a target |
| `GET /videos/{id}` | Metadata and the manifest URL |
| `POST /like` | Event, not a heavy transaction |

Sync for metadata and search. Async for encode and counters.

Hot videos stay on a warm tier. Old videos move to colder storage. Egress, not disk, is often the expensive line. Cache at the edge so a hit does not leave the region.

---

## Diagrams from the slides

The columns are the small data. The picture is the heavy path: upload, then a queue, then workers that make smaller copies, then the CDN. Watching a video should come from the CDN, not from those workers.

**Metadata tables (the slide lists these columns)**

```text
USERS     user_id PK, email, username, password_hash, join_date, profile_picture
VIDEOS    video_id PK, user_id FK, title, description, upload_date, status, thumbnail_url
LIKES     like_id PK, user_id FK, video_id FK, timestamp
COMMENTS  comment_id PK, video_id FK, user_id FK, comment_text, timestamp
HISTORY   history_id PK, user_id FK, video_id FK, watch_timestamp
ANALYTICS video_id FK, views, likes, shares, dislikes, comments_count
```

`status` on VIDEOS is what flips from processing to playable. ANALYTICS is the aggregated row, not one write per view.

**Final picture**

```text
Web / phone / TV
      |
      v
API Gateway
      |
      +--> Upload service --> encode queue --> Encoding workers --> S3
      |                                              |
      |                                              +--> CDN --> viewers
      +--> Metadata service --> cache --> metadata DB
      +--> User service --> user DB
      +--> Engagement service --> engagement DB --> Recommendation --> cache
      +--> Search service --> search cache --> search DB
```

Play requests should hit the CDN, not the encode queue.

## Interview lines

- Chunk upload, queue, transcode to HLS/DASH, CDN.
- View counts are aggregated events.
- Metadata is small and in a database. Bytes are objects.
- Adaptive bitrate is the player's job, using the manifest you produced.

---

## Interview Q&A

### 1. What happens after upload?

<details>
<summary>Show Answer</summary>

**Answer:**

The client uploads chunks to object storage. The API records a video id and a processing status and enqueues a job. Workers write several resolutions plus a manifest and thumbnails. When the playable set is ready, status flips and the CDN can serve it. The client is not blocked on transcoding.

</details>

### 2. How do you serve 10 Tbps without melting the origin?

<details>
<summary>Show Answer</summary>

**Answer:**

Viewers hit a CDN, which caches segments near them. The origin objects are the backup. The player picks a bitrate from the manifest. You still pay for egress, so hot content must be cached and cold content must leave the expensive tier. Ten million concurrent streams is an edge problem, not a database problem.

</details>

### 3. Where do likes and recommendations go?

<details>
<summary>Show Answer</summary>

**Answer:**

Likes and views are events on a queue, aggregated off the play path, so a popular video does not have one burning row. Search indexes titles and tags. Recommendations use watch history and run beside the play API. A late like count is acceptable. A video that 404s because the counter locked the row is not.

</details>

**Q&A recap:** transcode asynchronously, play from the CDN, count views in the background.

## Interview Q&A — 6 to 8 years

### S1. Upload returned success and the video still will not play. What states do you show?

<details>
<summary>Show Answer</summary>

**Answer:**

Uploaded, processing, ready, failed. The upload completes the original blob and writes a row in UPLOADED. A queue transcodes renditions. The player only sees READY renditions. If transcoding fails I set FAILED and I can retry the job. I do not block the upload HTTP call on ffmpeg. A page refresh reads the row. It does not guess.

**Corner cases:** A worker that acks the queue message before the rendition is durable, so a crash loses the job and the row stays UPLOADED forever. I ack after the file is stored and the row moves to READY, and a sweeper re-queues stuck UPLOADED rows. Two workers transcoding the same video. A lease on the video id. A partial rendition published (audio without video) that the player treats as READY. I publish an atomic manifest. A title edit that waits on transcoding. Metadata updates are independent of the blob pipeline. A takedown during processing. The worker checks status before it publishes the CDN object.

</details>

### S2. How do you count a view when players retry, prefetch, and seek?

<details>
<summary>Show Answer</summary>

**Answer:**

I do not increment on every byte request. I count one view per user or per device token per video per window, after a threshold of real playback (for example several seconds), via a beacon. The counter is async and sharded. The number on the page can lag and can be approximate. If I need an exact bill to a creator, I define the rule and I dedupe in the log before I pay.

**Corner cases:** A CDN log line per segment, so a 10-minute video looks like hundreds of views. I aggregate. A replay within the window double-counts unless the key is (video, user, window). Prefetch of the next video that never plays. The threshold saves me. Bots. I will not stop all of them. I filter obvious ones and I do not pretend the public counter is an audit grade. A viral video's single Redis INCR key. I shard the counter. A creator payment based on the public counter while I also drop bot traffic later. I pay from the reconciled log, not from the live badge.

</details>

### S3. A live event and a normal upload share a CDN. What must not be cached the same way?

<details>
<summary>Show Answer</summary>

**Answer:**

Recorded segments are immutable and can be cached for a long time. Live segments and the live manifest change every few seconds and need a short TTL. The player follows the manifest. I do not put a one-day cache on the live playlist or viewers stall on an old segment list. Origin is protected by the CDN for hot live segments, with request collapse so a million viewers do not all miss at once.

**Corner cases:** A manifest cached by a middle box that ignores TTL. I name the cache headers and I accept some players will be behind. A live DVR window that grows forever and fills storage. I trim. A region whose CDN node is cold at the start of the event, so origin melts in minute one. I pre-warm or I use an origin shield. Authentication for a paid live stream. Public CDN URLs leak the stream. I use short signed URLs and I rotate the signing key on a schedule, knowing a leaked URL works until expiry. Separate the control API (who may watch) from the byte path.

</details>

### S4. How do you recommend the next video without putting that query on the play path?

<details>
<summary>Show Answer</summary>

**Answer:**

Play is "get manifest, get segments." Recommendations are a separate call, cached per user or per video, computed from a batch or a nearline job. If that service is down, play still works and the sidebar is empty or generic. I do not join watch-history across a huge graph inside the play request.

**Corner cases:** A recommendation cache shared across users because I forgot the user in the key. A model update that must not stop playback. I deploy it independently. Watch history written synchronously on every heartbeat, which becomes the bottleneck. I sample or I batch heartbeats. A kid's profile and an adult profile on the same account sharing recommendations. The cache key includes the profile. Regional restrictions: the recommended video 404s in this country. I filter by the viewer's region before I show it, and the play service enforces the rule even if the sidebar is wrong.

</details>

**Senior recap:** a visible processing state, views counted by a deduped beacon not by segments, short TTL for live manifests, and recommendations off the play path.

Next: [10_Search_Engine.md](10_Search_Engine.md)
