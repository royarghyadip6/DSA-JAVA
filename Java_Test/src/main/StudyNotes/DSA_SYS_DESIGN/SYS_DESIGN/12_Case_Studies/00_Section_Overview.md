# 12 — Case Studies

This folder matches the course **case studies**. Each note follows the [4-step blueprint](../11_System_Design_Blueprint/01_Four_Step_Approach.md): requirements, numbers, boxes and APIs, then tools and the tradeoff.

The slides share one shell: an API gateway, a login token, a load balancer, and logs. The notes keep that shell short and spend the page on the part that makes **this** system different.

Read [How to read these notes](../00_HOW_TO_READ.md) and the [4-step blueprint](../11_System_Design_Blueprint/01_Four_Step_Approach.md) if the pictures feel crowded.

**How to read a case-study picture**

```text
Left side     the person and their app
Middle        your programs (one box = one job)
Right side    databases, files, and outside companies (payments, email)
Arrow         a request or a later background job
```

Follow one user action only. For a ticket, follow "buy this seat" from the phone to the database. Then follow the email, which should leave on a queue, not inside the buy button.

**Words that repeat in this folder**

| Word | Meaning |
|------|---------|
| **DAU** | Daily active users. How many different people use it on a normal day |
| **QPS** | Queries per second. How many requests arrive each second |
| **302** | "Go to this other address." A short link answers 302 so the browser opens the long URL |
| **Idempotent** | Doing the same job twice is safe. A retry must not charge the card twice |
| **CDN** | Nearby copies of heavy files, so video and photos do not all come from one building |

---

## Read in this order

| # | File | The hard part |
|---|------|----------------|
| 1 | [01_URL_Shortener.md](01_URL_Shortener.md) | Short unique keys, and a redirect that is almost only a cache read |
| 2 | [02_Ticketing.md](02_Ticketing.md) | Two people must not get the same seat |
| 3 | [03_News_Feed.md](03_News_Feed.md) | Fan-out: push the post, or build the feed on read |
| 4 | [04_Notification_System.md](04_Notification_System.md) | Deliver at least once, without spamming twice |
| 5 | [05_Chat.md](05_Chat.md) | Millions of live connections, and order inside one chat |
| 6 | [06_Auction.md](06_Auction.md) | One winning bid, even in the last second |
| 7 | [07_Rental_Airbnb.md](07_Rental_Airbnb.md) | Search can be a little stale. A booking cannot double-book |
| 8 | [08_Cloud_Storage.md](08_Cloud_Storage.md) | File bytes in object storage. Metadata and sync beside them |
| 9 | [09_Video_YouTube.md](09_Video_YouTube.md) | Upload, transcode, then play from a CDN |
| 10 | [10_Search_Engine.md](10_Search_Engine.md) | Crawl, then an inverted index, then rank |
| 11 | [11_Ecommerce.md](11_Ecommerce.md) | Do not sell the last item twice |
| 12 | [12_Taxi_Uber.md](12_Taxi_Uber.md) | Nearby drivers, and one of them gets the ride |
| 13 | [13_Collaborative_Docs.md](13_Collaborative_Docs.md) | Two people type at once and the document still agrees |
| 14 | [14_Interview_Mindset.md](14_Interview_Mindset.md) | How to speak the design out loud |

---

## What is next

The course notes are complete. Practice one case with a timer. Change one constraint ("now it is multi-region", "now payments can fail") and say what moves.

---

## Source PDFs used

Course sections 12 through 25 (TinyURL through the closing lecture).
