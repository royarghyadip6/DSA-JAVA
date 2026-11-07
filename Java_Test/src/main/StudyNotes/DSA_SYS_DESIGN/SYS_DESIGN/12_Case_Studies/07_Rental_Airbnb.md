# 7. Online Rental (Airbnb)

## Simple idea

Hosts list homes. Guests search, pick dates, and pay. The hard part is **those dates are taken exactly once**, even though search is a fast, slightly stale index.

---

## Requirements

**Guest:** search with filters, read reviews, book, pay.

**Host:** create a listing, upload photos, set price and calendar.

**Admin:** moderate. **Also:** sync an external calendar (Google Calendar or iCal), and email or push on booking.

**Qualities:** up in holiday season, millions of users, payments and personal data protected, search under about 300 ms, no double booking, many currencies and time zones.

**Assumptions from the slides:** a third party takes cards. Email is verified before listing or booking. Media is object storage. Clients are web and mobile. Search can be a moment behind. The booking itself cannot be "eventually" correct. The slide line that says availability may be eventually consistent fights the no-double-booking rule. Split them: **search index eventual, booking transaction strong**.

---

## Numbers

| | Figure |
|--|--------|
| DAU / concurrent | 5 million / about 100,000 |
| Listings | 50 million and growing |
| Searches | 40–50 million per day |
| Bookings and payments | about 1 million per day (about **12 per second** average) |
| Media | about 10 images × 1 MB × 50 million listings ≈ **500 TB** |
| Listing rows | 50 million × 5 KB ≈ 250 GB |
| Bookings | about 1 billion rows a year × 1 KB ≈ **1 TB+** a year |

Hot paths: search, calendar updates, and the booking that locks dates plus payment. Cold paths: old reviews, profile edits, moderation.

---

## Booking

```text
1. Guest asks to book listing + dates
2. Availability service checks the calendar (source of truth, not Elasticsearch)
3. Booking service locks those dates and writes a pending reservation
4. Payment service charges
5. Success → confirm. Failure or timeout → release the dates
6. Queue the email, calendar sync, and review reminder
```

Lock with a transaction or a unique constraint on `(listing, night)`. Two guests cannot both insert the same night. External calendars are **hints**. They sync in the background. If iCal is late, the platform calendar still wins, and the host must resolve a clash. Do not let a stale iCal overwrite a paid booking.

Search is Elasticsearch: text, price, and **geo**. It can be seconds behind a new listing. The book button re-reads the database.

Photos: S3 (or Azure Blob, as the infra slide says) and a CDN. Cards: Stripe or PayPal. You do not store the card number (PCI).

---

## Design

```text
API gateway (JWT, roles: guest, host, admin)
  User, Listing, Search, Availability, Booking
  Payment, Notification, Review
  Media, Calendar sync, Analytics

PostgreSQL     users, listings, bookings, payments
Elasticsearch  search
Redis          popular listings and recent searches
Kafka or RabbitMQ    booking_created, payment webhooks, mail
CDN            photos
```

Sync calls for search and "show this listing". Async for mail, calendar push, and payment webhooks.

---

## Diagrams from the slides

The tables are the notebook. A line from one table to another means "this row points at that row." The big picture is the programs: search can be slightly late, the booking database cannot.

**Tables and how they connect**

```text
USERS (user_id PK, name, email)
  | 1                          | 1
  | many                       | many
  v                            v
LISTINGS                     NOTIFICATIONS
  listing_id PK                notification_id PK
  user_id, title               user_id, message, timestamp
  description, price
  | 1
  | many
  +------------------> BOOKINGS (booking_id PK, user_id,
  |                      listing_id, check_in, check_out)
  |                         | 1
  |                         | many
  |                         v
  |                      PAYMENTS (payment_id PK, booking_id,
  |                                amount, status)
  | 1
  | many
  +--> AVAILABILITY (listing_id PK, start_date, end_date, is_available)
  +--> MEDIA (media_id PK, listing_id, media_url, type)
```

One user, many listings. One listing, many nights, photos, and bookings. One booking, many payment attempts if a retry happens. The slide draws payment as many rows per booking.

**Final picture**

```text
Web / mobile --> API Gateway --> User, Listing, Search (OpenSearch)
                      |              Availability, Booking, Payment
                      |              Review service
                      |              Media service --> S3 --> CDN
                      |
                      +--> Redis (hot listings)
                      |
                      +--> event bus --> Notification
                                      --> Calendar sync --> host iCal
                                      --> Analytics
                      |
                      +--> external card gateway
```

Blue arrows on the slide are the HTTP call. Orange arrows are events after the booking exists.

## Interview lines

- Geo search is an index. The calendar in SQL is the lock.
- Unique nights, then pay, then release on failure.
- iCal is eventual and must not clobber a confirmed stay.
- Hundreds of terabytes of photos, not of booking rows.

---

## Interview Q&A

### 1. How do you search and still avoid a double booking?

<details>
<summary>Show Answer</summary>

**Answer:**

Elasticsearch answers "homes near here under this price" and can be slightly stale. When the guest books, the availability service reads and locks those nights in PostgreSQL, with a unique constraint per listing and night. Payment runs against that hold. If it fails, the nights go back. The search index updates from the booking event and may lag. That lag must not be what you charge against.

</details>

### 2. What are the heavy parts?

<details>
<summary>Show Answer</summary>

**Answer:**

Tens of millions of searches, a calendar that changes on every booking and on external sync, and about 500 TB of images. Search wants a geo index and a cache of hot listings. Images want object storage and a CDN. The booking path wants a short transaction, not a join across search. Time zones belong on the calendar, not as a guess in the client.

</details>

### 3. How do external calendars fit?

<details>
<summary>Show Answer</summary>

**Answer:**

A sync worker pulls iCal or Google Calendar and blocks nights on the platform calendar. It is asynchronous and can be late. The platform reservation is the source of truth. A paid booking is not deleted because a sync is slow. Conflicts are shown to the host. Notifications go out on a queue after the booking commits.

</details>

**Q&A recap:** index for search, SQL for the nights, pay inside the hold.

## Interview Q&A — 6 to 8 years

### S1. Search shows a listing as free. Checkout says it is taken. Is that a bug?

<details>
<summary>Show Answer</summary>

**Answer:**

Not always. Search is allowed to be stale. The book path is not. I re-check the nights in SQL with a constraint that rejects overlap, under a transaction or an exclusion constraint on (listing, night range). If the row conflicts, the user gets a clear "just booked" and search catches up from the outbox. I do not book by decrementing a counter in the search engine.

**Corner cases:** Overlapping ranges that a naive `start >= and end <=` check misses (a stay that starts during another stay). I use a range overlap predicate and a constraint, not application arithmetic alone. Time zones: a "night" is the listing's zone, not the traveler's phone. Two listings for the same physical place that I accidentally allow. A hold that does not block search, so many users try and all fail at pay. I can hide held nights in search with a short delay, knowing the hold may expire. A long hold that denies the owner a real booking. I keep holds short and I expire them on the database clock.

</details>

### S2. Payment is captured and the calendar insert fails. What is the recovery?

<details>
<summary>Show Answer</summary>

**Answer:**

I prefer: insert a PENDING stay that already blocks the calendar, then capture money, then mark PAID. If capture fails, I release the nights. If I captured first and the insert fails, I must refund or retry the insert. Retries use the booking id so I do not create two stays. The user sees PENDING until both are true. I do not send the confirmation email inside the transaction.

**Corner cases:** A capture that succeeds, a refund that fails, and the nights released, so I sold the same nights twice and I owe money. Until the refund is confirmed I keep the nights blocked. A webhook and a synchronous response that both mark PAID. Same booking id, second write is a no-op. A price change between search and pay. I snapshot the price on the hold and I charge that, or I fail the checkout if the price moved. Currency and tax included in that snapshot. An owner calendar edit that races the hold. The constraint decides. The owner edit fails or the hold fails. I say which priority (guest hold wins until expiry).

</details>

### S3. How do you model a listing that is available except three weekends, with a minimum stay?

<details>
<summary>Show Answer</summary>

**Answer:**

Availability is either a set of blocked nights or a set of open seasons plus exceptions. Rules (minimum nights, check-in weekday) are checked on the book path in the listing's time zone. Search can filter coarsely (city, guests, rough dates) and miss some rules. The book path enforces every rule. I do not pretend the search index is the policy engine.

**Corner cases:** A minimum stay of 3 that search ignores, so results look available and checkout always fails. I either index the rule or I accept the miss and say so. A block for maintenance that overlaps a paid stay. The paid stay wins. The owner cannot double-book via a second tool. One calendar is the source of truth. External calendar sync (iCal) that is eventual and double-books. I treat the internal constraint as authority and I surface the conflict. A guest count above the listing max enforced only in the UI.

</details>

### S4. Reviews and ranking are on the listing. What must not be on the booking transaction?

<details>
<summary>Show Answer</summary>

**Answer:**

Updating a rating average, fanning out to search, and sending email. The booking transaction writes the stay and the payment state. A worker updates search and the host's aggregate. Ranking can lag. A booking must not wait on it. I recompute a host score from reviews with an idempotent job so a retried event does not add the rating twice.

**Corner cases:** A review for a stay that was refunded. I decide if it counts and I filter it in the aggregate job. A host who edits the listing mid-stay (address, max guests). The stay snapshots the terms the guest accepted. Search shows the new terms. A popular city's search cache that ignores a new listing for too long. TTL plus an explicit update from the outbox. A fraud guest who books every weekend to block a host. Rate-limit holds per guest and per payment instrument. I mention the limit without designing a fraud-research system.

</details>

**Senior recap:** search can be stale, SQL overlap constraint cannot, block nights before capture, and keep ranking off the booking transaction.

Next: [08_Cloud_Storage.md](08_Cloud_Storage.md)
