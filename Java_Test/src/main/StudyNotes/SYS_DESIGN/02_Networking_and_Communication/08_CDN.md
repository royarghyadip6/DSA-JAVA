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

Back to [00_Section_Overview.md](00_Section_Overview.md) for the section cheat sheet. Next course folder: [03_Protocols](../03_Protocols/README.md).
