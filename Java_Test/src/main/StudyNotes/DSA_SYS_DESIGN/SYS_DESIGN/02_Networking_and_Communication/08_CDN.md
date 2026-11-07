# 8. Content Delivery Networks (CDN)

## Simple idea

A **CDN** is a **worldwide photocopy network** for your content.

Instead of every user in India hitting a server in the US, they hit a copy in **Mumbai**.

```text
User in Delhi
      |
      v
  Nearby edge (PoP)  --cache hit-->  image/video/js instantly
      |
      cache miss
      v
  Origin server (your main data centre)
```

Goals: **less wait**, **less load on origin**, **better uptime**, **some security**.

---

## The problem without a CDN

- User is far from origin → high **latency**
- Origin serves every image for the whole world → **overload**
- Fat videos eat **bandwidth** and money
- Origin IP is easy to attack

---

## Three parts

| Part | Role | Everyday picture |
|------|------|------------------|
| **Origin** | Holds the original files / APIs | Main warehouse |
| **Edge / PoP** | Cache close to users | Local kirana / distribution centre |
| **Request routing** | Pick the best PoP | GPS for the delivery van |

**PoP** = Point of Presence = a CDN mini data centre in a city.

More good PoPs → more users are "nearby".

---

## What happens on one request

1. User asks for a photo / video / page / sometimes API data
2. CDN picks the best edge (location, latency, load)
3. **Cache hit** — edge already has it → send immediately
4. **Cache miss** — fetch from origin, **store on edge**, then send
5. Next user in that city is fast

---

## Cache hit vs miss

```text
Hit  =  "I already have this file"
Miss =  "I must ask the warehouse, then keep a copy"
```

**TTL** = how long the copy is allowed to live.

After TTL, edge re-checks origin (or serves stale while refreshing).

**Invalidation** (force a new copy):

| Strategy | Meaning |
|----------|---------|
| Manual purge | "Delete this file from all edges now" |
| Versioning | `logo_v2.png` — new name, no stale cache |
| Stale-while-revalidate | Show old file, refresh in background |

Stale content is the classic CDN bug ("I deployed but users still see yesterday's JS"). Versioning is the simple fix.

---

## How CDNs improve three things

**Performance**

- Cache popular bytes worldwide
- Gzip / Brotli compress text
- Minify CSS/JS
- Images as WebP / AVIF
- Adaptive bitrate for video

**Reliability**

- Many PoPs share load
- Routing: **geo**, **latency**, **load-aware**
- If one PoP dies, send users to the next

**Security**

- DDoS absorbed at many edges (Anycast)
- Rate limit + filter
- SSL/TLS at the edge (**offload** origin)
- Origin stays hidden

---

## Routing strategies

| Strategy | Pick the PoP that is… |
|----------|------------------------|
| Geo-based | Physically closest |
| Latency-based | Fastest network path (not always same as km) |
| Load-aware | Not already overloaded |
| Anycast | Same IP, internet routes to nearest announcement |

---

## Static vs dynamic

- **Static** — images, video files, HTML/CSS/JS — CDNs love this
- **Dynamic** — personalised pages, "your cart" — harder to cache
- **API acceleration** — cache **public GETs** (product list), not private POSTs (pay now)
- **Edge computing** — run a little logic on the PoP (auth check, A/B, personalisation) so origin is not hit

---

## Multi-CDN

Big companies (Netflix-scale thinking) use **more than one CDN vendor**.

If Cloudflare has a bad day, traffic can shift to another provider. Extra cost, extra control.

---

## Video streaming sketch (interview favourite)

```text
Video split into small chunks (segments)
Chunks cached at many PoPs
Player picks quality from network speed (adaptive bitrate)
If a PoP is busy, another PoP serves the next chunk
```

---

## Real-time apps (gaming / stock ticks)

CDN helps **static assets** and sometimes **edge PoPs close to users**.

Pure live ticks still need **WebSockets / UDP** and regional servers. Do not claim "CDN caches live stock prices for 24 hours" — that would be wrong.

---

## Challenges

- Cache inconsistency after deploys → version + purge
- Dynamic / personalised data → cache keys, short TTL, or no cache
- Cost of multi-CDN and egress
- Origin shield / collapse so a miss storm does not kill origin

---

## Interview lines

- CDN = global caches (edges) + origin + smart routing.
- Hit vs miss + TTL + invalidation.
- Cuts latency, origin load, and some DDoS.
- Static first; APIs only when data is cacheable.

---

## Interview Q&A

Read the question. Answer out loud. Then open the answer.

### 1. What is a CDN, and how does it work?

<details>
<summary>Show Answer</summary>

**Answer:**

A **CDN** is a global set of servers that deliver content with less wait.

1. User requests a file (image, video, page)
2. CDN sends them to a nearby **edge / PoP**
3. **Cache hit** → send immediately
4. **Cache miss** → fetch **origin**, store on edge, then send

</details>

### 2. Why do we need CDNs in system design?

<details>
<summary>Show Answer</summary>

**Answer:**

Without CDN, everyone hits origin: high **latency**, **overloaded** origin, high **bandwidth** cost, origin easier to **attack**.

CDN spreads load, caches hot files, and adds an edge shield.

</details>

### 3. What are the key benefits of using a CDN?

<details>
<summary>Show Answer</summary>

**Answer:**

Lower **latency**, lower **bandwidth** bill, better **availability** (many PoPs), better **security** (DDoS, TLS at edge).

</details>

### 4. Explain the difference between an origin server and an edge server in a CDN.

<details>
<summary>Show Answer</summary>

**Answer:**

- **Origin** — the real copy (your S3 / data centre). Used on **miss**.
- **Edge (PoP)** — local cache near users.

**Picture:** origin = main warehouse. Edges = neighbourhood stores.

</details>

### 5. What is a PoP (Point of Presence) in a CDN?

<details>
<summary>Show Answer</summary>

**Answer:**

A **PoP** is a CDN mini data centre full of edge servers. More (and better placed) PoPs → more users are close to a cache.

</details>

### 6. How does request routing work in a CDN?

<details>
<summary>Show Answer</summary>

**Answer:**

Pick the best edge using **geo** (closest city), **latency** (fastest path), **load-aware** (skip a busy PoP). DNS and **Anycast** are common steering tools.

</details>

### 7. What is a cache hit vs. cache miss, and how does a CDN handle them?

<details>
<summary>Show Answer</summary>

**Answer:**

- **Hit** — edge has the object → serve now
- **Miss** — fetch origin → **save on edge** → serve → next user hits

</details>

### 8. Explain cache expiration and TTL (Time-To-Live) in a CDN.

<details>
<summary>Show Answer</summary>

**Answer:**

**TTL** = how long the edge may keep a copy. When TTL ends, edge revalidates or refetches.

Short TTL = fresher, more origin load. Long TTL = faster, risk of staleness.

</details>

### 9. What are cache invalidation strategies, and why are they important?

<details>
<summary>Show Answer</summary>

**Answer:**

Deploys fail when users still have yesterday's `app.js`.

| Strategy | Idea |
|----------|------|
| **Manual purge** | Delete this path from edges now |
| **Versioning** | `app.v3.js` — new URL, no stale |
| **Stale-while-revalidate** | Show old, refresh in background |

Versioning is the simplest reliable trick.

</details>

### 10. How do CDNs use load balancing to improve reliability?

<details>
<summary>Show Answer</summary>

**Answer:**

Traffic is spread across PoPs: round-robin, latency-based, geo-based. One busy or dead location does not take the whole site down.

</details>

### 11. Explain different request routing strategies (geo, latency, load-aware).

<details>
<summary>Show Answer</summary>

**Answer:**

| Strategy | Choose the PoP that is… |
|----------|-------------------------|
| Geo | Physically nearest |
| Latency | Lowest RTT / best path |
| Load-aware | Has spare capacity |

Latency ≠ distance (a "closer" PoP on a bad link can be slower).

</details>

### 12. What happens if a CDN PoP fails? How does failover handling work?

<details>
<summary>Show Answer</summary>

**Answer:**

Health checks notice the dead PoP. Routing (DNS/Anycast) sends users to the **next best** PoP. Service continues; maybe slightly higher latency.

</details>

### 13. What compression and minification techniques do CDNs use?

<details>
<summary>Show Answer</summary>

**Answer:**

**Gzip / Brotli**, minify CSS/JS, images as **WebP / AVIF**. Smaller files = faster downloads and cheaper bandwidth.

</details>

### 14. How does a CDN optimize image and video delivery?

<details>
<summary>Show Answer</summary>

**Answer:**

**Images:** modern formats, resize at edge.

**Video:** split into **chunks**, cache chunks at PoPs, **adaptive bitrate** (360p on 3G, 1080p on fibre).

</details>

### 15. How does API acceleration work in a CDN?

<details>
<summary>Show Answer</summary>

**Answer:**

Cache **safe, repeatable GETs** (public product JSON) at the edge. Skip cache for private or POST (checkout).

</details>

### 16. How does a CDN protect against DDoS attacks?

<details>
<summary>Show Answer</summary>

**Answer:**

Rate limit, drop junk, **Anycast** (attack spreads across many PoPs). Origin IP stays hidden.

</details>

### 17. What is SSL/TLS offloading, and why is it useful?

<details>
<summary>Show Answer</summary>

**Answer:**

Edge **terminates HTTPS**. Origin does less crypto work. Same idea as reverse-proxy SSL termination, but global.

</details>

### 18. What are some challenges in CDN implementation, and how can they be mitigated?

<details>
<summary>Show Answer</summary>

**Answer:**

| Challenge | Mitigation |
|-----------|------------|
| Stale content after deploy | Versioned URLs + purge |
| Personalised / dynamic pages | Cache keys, short TTL, or bypass |
| Miss storms hitting origin | Origin shield, request collapsing |
| Cost | Cache more static, compress |
| "Works in Mumbai, not in London" | Check PoP cache and headers |

</details>

### 19. How does a multi-CDN architecture work?

<details>
<summary>Show Answer</summary>

**Answer:**

Use **two or more CDN vendors**. If one has an outage, the other still serves. More cost; used when one vendor is too big a risk.

</details>

### 20. What is edge computing, and how does it relate to CDNs?

<details>
<summary>Show Answer</summary>

**Answer:**

**Edge computing** = run a **little logic** on the PoP (A/B test, auth check, image resize), not only store files. CDN already has machines near users.

</details>

### 21. How would you design a CDN for a large-scale video streaming platform?

<details>
<summary>Show Answer</summary>

**Answer:**

Split video into **segments**, cache hot segments at many PoPs, **adaptive bitrate**, load-balance viewers, hide origin, multi-CDN if needed.

</details>

### 22. How do CDNs help in real-time applications like online gaming or stock trading?

<details>
<summary>Show Answer</summary>

**Answer:**

CDN helps **assets** (maps, UI) and can put **PoPs close** to players.

Live ticks / game state still need **WebSocket / UDP / regional servers**. Do **not** cache live prices for hours. CDN is **supporting**, not the whole real-time design.

</details>

**Q&A recap:** Origin vs edge, hit/miss, TTL, purge/versioning, routing, DDoS/TLS, video chunks, multi-CDN, edge compute. Always talk **latency, redundancy, scale**.

## Interview Q&A — 6 to 8 years

### S1. A product page is cached at the CDN and a user sees another user's name. What did the design get wrong?

<details>
<summary>Show Answer</summary>

**Answer:**

The cache key ignored something that changes the body: cookie, auth header, or country. A shared object was stored as if it were public. I mark private responses `Cache-Control: private` or I do not send them to the CDN at all. If only a small part is personal, I cache the public shell and fill the name from a separate uncached call.

**Corner cases:** The `Vary` header is missing, so a German page is served to a French user. A query string with a token is cached and then reused by someone else. An error page (500) or a login redirect (302) is cached and sticks. A purge API fails partially, so some edges keep the bad object. I version the URL (`app.3f2a.js`) so I rarely need a global purge.

</details>

### S2. You set a one-day TTL and now you cannot ship a fix. What is the senior pattern?

<details>
<summary>Show Answer</summary>

**Answer:**

Immutable file names for assets, long TTL. HTML or the API response that points at those names stays short, or is not cached. A fix is a new file name, not a race to purge every city. For API data I use a short TTL plus an explicit purge on write for the keys I know.

**Corner cases:** HTML cached for a day still points at old JavaScript after an emergency rollback, and the mix is broken. Purge is rate-limited and a viral object cannot be cleared in time. Stale-while-revalidate helps availability and can serve the broken body for a while. I set a cap. Two CDNs do not purge together, so some countries stay wrong.

</details>

### S3. Origin falls over at the top of the hour. Why, if the CDN is in front?

<details>
<summary>Show Answer</summary>

**Answer:**

Many objects expire together, or a deploy changed every URL, so every edge misses at once. That is a stampede. I jitter TTLs, use stale-if-error so a dead origin still serves the last good copy, and I protect origin with a request collapse (one fetch for a key, everyone else waits) and a rate limit.

**Corner cases:** The first request after expiry is a huge file and ties up origin threads. Query strings are all unique (cache busters, session ids), so the hit ratio is zero and the CDN is an expensive proxy. A POST is not cacheable. If the app uses POST for reads, the CDN never helps. Origin shield (one extra cache in front of origin) cuts duplicate misses from many edges.

</details>

### S4. When would you not put a CDN on the path?

<details>
<summary>Show Answer</summary>

**Answer:**

Highly personal data, strict freshness (a stock price the user will trade on), or requests that must hit one region for legal reasons. Also uploads, and any response whose cache key I cannot define safely. I can still use the CDN for the static shell and the video bytes.

**Corner cases:** A "private" CDN URL that is only unguessable is not access control. Anyone with the URL can fetch it until it expires. Signed URLs need a short life and a clock that is not wildly skewed, or valid users get 403. Edge compute that talks to the primary database on every request puts the database back on the hot path and adds a new place to leak credentials. Multi-CDN failover can split caches and double origin load on the day one CDN dies.

</details>

**Senior recap:** never cache a personal body on a shared key, version files instead of praying for purge, jitter expiry, and do not CDN what must be fresh or private.

Back to [00_Section_Overview.md](00_Section_Overview.md) for the section cheat sheet. Next course folder: [03_Protocols](../03_Protocols/README.md).
