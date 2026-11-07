# 10. Search Engine

## Simple idea

Find pages on the web, remember which words are on which pages, and answer a query in under **200 ms**.

This note is a **web search** design (crawl plus index). Product search inside a shop is the smaller version: you already have the documents, so you skip the crawler and still use an inverted index (often Elasticsearch).

---

## Requirements

Crawl public pages. Build an index. Answer keyword queries with ranked results. Refresh the index on a schedule, not on every edit of the internet.

**Qualities:** under 200 ms, large scale, freshness in hours rather than seconds, no single machine whose death stops search, compressed storage.

**Assumptions:** public HTML, obey `robots.txt`, simple keywords, rank with TF-IDF and PageRank, top 10 or 20 results. No personalization in this pass. Real-time indexing is not required.

The slides contain **two sizes**. Do not mix them up.

| | Whole-web sketch | MVP the rest of the slides design |
|--|------------------|-------------------------------------|
| Pages | 100 billion+ | **100 million** |
| Raw size | about 10 PB (about 2–3 PB compressed) | 100 million × 100 KB ≈ **10 TB** |
| Queries | 5–10 billion a day worldwide | 10 million users × 5 queries ≈ **50 million a day** |

50 million queries a day is about **600 per second** average, and the slide's peak of about **1,000–2,000**. A separate bullet says 50,000 QPS as a scalability target. Design the query path so you can add index shards. Use the MVP numbers unless the interviewer says "Google scale".

---

## The index

An **inverted index** maps a word to the list of documents that contain it (the postings), with positions and counts.

A **forward index** maps a document id to its title, stored text, and metadata, so you can build a snippet after you know the ids.

```text
Query → parse (< 5 ms)
      → look up postings (< 20 ms)
      → score TF-IDF, PageRank, freshness (< 50 ms)
      → format top results (< 10 ms)
      → under 200 ms
```

Popular queries sit in Redis so they never touch the index.

---

## Crawl

```text
URL frontier (a prioritized queue, sharded by domain)
  → workers fetch, slowly, and obey robots.txt
  → save HTML in object storage
  → extract links back into the frontier
  → skip near-duplicates (SimHash fingerprint)
```

The slides' MVP crawl: 100 million pages in 7 days is about **170 pages per second**. A few hundred workers, each well under one page per second per site, can do that if you are polite. One site must not be hammered. Partition the frontier by **domain hash** so one site's politeness is one worker's problem.

Refresh high-change sites sooner. Diff the content so an unchanged page does not rebuild the world.

---

## Index build

Normalize: strip tags, drop stop words, stem. Write the forward index and the postings. Store postings in **shards** (by term or by a hash of the term) with replicas. Compression (gap encoding of doc ids) is how the index stays smaller than the raw HTML. The slide's MVP inverted index is on the order of **500–800 GB**, plus a few TB of forward data.

Crawler → parser → indexer is a queue. Searchers only read a published segment of the index, so a half-built segment is invisible.

---

## Query

```text
Search API → cache?
          → query service fans out to the shards that hold those terms
          → ranking merges the candidates
          → snippets from the forward index
          → top N
```

95% of traffic is read. Writes are crawl and index updates.

---

## Tools

Object storage for raw pages. A document store or a custom service for postings. Elasticsearch is a fair way to **implement** the index if you can still explain postings, shards, and rank. "We use Elasticsearch" without that explanation does not answer the question. Kafka between crawler and indexer. Redis for hot queries. A load balancer in front of the search API.

---

## Diagrams from the slides

Two different machines. The crawler walks the web and builds a word index. The query path only looks that index up. Read the crawler picture first, then the query picture. The last picture puts both on one page.

**Crawler**

```text
Scheduler (politeness, how often)
        |
        v
URL Frontier  <---- new links
        |
        v
Crawler workers -- raw HTML --> Document store (S3)
        |                              ^
        +--> fingerprint (SimHash) ----+ skip duplicates
        |
        v
Parser --> Indexer queue
```

Workers are partitioned by domain hash so one site is not hit by every worker.

**Tables**

```text
UrlQueue
  url PK, status, domain_hash, next_crawl_time, retries

Documents          (forward index)
  doc_id PK, title, url, tokens, metadata

InvertedIndex
  token PK, posting_list     (which doc ids contain that word)
```

A crawl result fills Documents. Tokens from that page fill InvertedIndex.

**Query**

```text
"best laptops"
   --> Search API (auth, rate limit)
   --> parser
   --> cache?  hit --> return
   --> inverted index lookup
   --> rank (TF-IDF, PageRank, freshness)
   --> top results + a snippet
```

**Both pipelines on the final slide**

```text
Internet --> frontier --> crawlers --> parser --> dedup --> indexer queue --> inverted index
                                                                                  |
User --> gateway --> search API --> parser --> cache or index --> ranker --> page
                                              click logs feed the ranker later
```

## Interview lines

- Inverted index for lookup. Forward index for snippets. Rank after you have candidates.
- Polite, domain-sharded crawl. Duplicates dropped with a fingerprint.
- Cache the head queries. Shard the postings. Publish index segments, do not query a half-written one.
- Quote the MVP size unless they asked for the whole web.

---

## Interview Q&A

### 1. How do you answer a query so fast?

<details>
<summary>Show Answer</summary>

**Answer:**

Check the cache. Otherwise look up each term in the inverted index, which is sharded, and intersect or score the postings. Rank the candidates with TF-IDF, a link score such as PageRank, and freshness. Load titles and snippets from the forward index. The slide budget is under 200 ms: a few milliseconds to parse, about 20 ms in the index, about 50 ms to rank, about 10 ms to format.

</details>

### 2. How do you crawl without getting banned or filling disk with duplicates?

<details>
<summary>Show Answer</summary>

**Answer:**

A frontier queues URLs and is partitioned by domain so one site has a rate limit and a `robots.txt` policy. Workers store the page and push new links back. SimHash or a similar fingerprint drops near-duplicates. Re-crawl changing sites first. Raw HTML goes to object storage. For the MVP, about 170 pages per second finishes 100 million pages in a week.

</details>

### 3. Why not only a relational database?

<details>
<summary>Show Answer</summary>

**Answer:**

A `LIKE` scan over 10 TB will not return in 200 ms. The inverted index is the structure that makes "pages containing these words" a lookup. You still store metadata somewhere ordinary. Elasticsearch is one product that stores postings. The design you must be able to draw is the index, the shards, and the ranker, not the logo.

</details>

**Q&A recap:** polite crawl, inverted index, rank the candidates, cache the common queries.

## Interview Q&A — 6 to 8 years

### S1. The index is rebuilt nightly. A site deletes a page at noon. What do users find, and what do you add?

<details>
<summary>Show Answer</summary>

**Answer:**

They find the page until the next build, unless I have an incremental update. I say that staleness. For a fresher index I apply adds and deletes as documents land, and I still run a full rebuild to fix drift. A delete is a tombstone so a later merge does not resurrect the URL from an old segment. Search results that 404 are a signal to recrawl, not a silent success.

**Corner cases:** A tombstone that expires before the old segment is merged, so the page comes back. I keep the tombstone until the merge that drops the URL. An update that changes the words and leaves the old posting list in place, so the page matches a query it no longer contains. Updates are delete plus add. Two crawlers indexing the same URL with different content. I version by fetch time and I keep the latest fetch that passed my rules. A legal removal that cannot wait for nightly. I have a deny list on the query path, not only in the next build.

</details>

### S2. How do you crawl without getting banned or melting a small host?

<details>
<summary>Show Answer</summary>

**Answer:**

Per-host politeness: a delay, a cap on parallel connections, and robots rules. I identify the crawler. I backoff when a host errors or slows. The frontier is a queue partitioned by host so one huge site does not block the rest and so one small site is not hit by every worker. I dedupe URLs (canonical) before I fetch again.

**Corner cases:** A redirect loop or a calendar that generates infinite URLs. I cap depth, cap URLs per host, and I fingerprint near-duplicates. A host that serves different content to the crawler than to users. I may rank a page the user will not see. I state crawl-as-seen. Revisit frequency: news versus a static PDF. I do not recrawl everything at one interval. A poisoned robots or a sitemap that points at internal admin URLs. I still enforce an allow-list of schemes and I do not index pages that require a login. Fetching a URL a user submitted (SSRF). The crawler must not reach link-local or private addresses.

</details>

### S3. Ranking is "score and sort." What breaks at query time?

<details>
<summary>Show Answer</summary>

**Answer:**

I do not score the whole web. I look up the posting lists for the terms, intersect or union a bounded set, score those candidates, and sort the top K. Caching is by normalized query. Personalization is a second stage on a small candidate list, or the cache key includes the user and the hit rate collapses. I budget a timeout. A partial top K is better than a slow perfect score.

**Corner cases:** A common word whose posting list is enormous. I skip or downweight it, or the query never finishes. A hot query cache that ignores a news event until TTL, so results feel dead. Short TTL for newsy queries or an explicit purge. Typos and operators that bypass the cache key (`cat` versus `cat `). I normalize. A score that uses a signal I can game (raw keyword count). I mention diversity and a quality signal without pretending one formula. Pagination into deep results that are unstable between requests. A cursor into a cached result list, or I accept movement.

</details>

### S4. How do you add a new document without stopping queries?

<details>
<summary>Show Answer</summary>

**Answer:**

I write new postings into a small in-memory or new segment, and I search old segments plus the new one. A background merge compacts segments. Queries keep running on the previous snapshot. I never block readers on a full rebuild. The cost is more segments and a merge IO spike I should not schedule at peak.

**Corner cases:** A merge that deletes a document a query is currently walking. Snapshot isolation per query. A replica that is behind, so one user refreshes and the new page vanishes. I route a user's next search to a replica at least as fresh as their last, or I accept replica lag and say so. Index corruption on one shard. I can rebuild that shard from the document store, which means the document store, not the inverted index, is the source of truth. Running out of disk during a merge. I leave headroom or queries fail in a hard way. Schema of the document (new field) that old replicas cannot read. I roll a reader that ignores unknown fields first.

</details>

**Senior recap:** tombstones so deletes stay deleted, politeness and an SSRF-safe fetcher, top-K over posting lists, and searchable new segments while merge runs in the background.

Next: [11_Ecommerce.md](11_Ecommerce.md)
