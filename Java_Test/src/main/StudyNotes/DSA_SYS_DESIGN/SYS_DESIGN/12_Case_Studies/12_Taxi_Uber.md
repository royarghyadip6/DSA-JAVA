# 12. Taxi Hailing (Uber)

## Simple idea

A rider asks for a car. The system finds a **nearby driver who is free**, gives the ride to **one** of them, and both apps see the car move. At the end, you charge a fare.

Start in **one city**, as the slides do.

---

## Requirements

**Rider:** sign in, request pickup and drop-off, see the driver and an ETA, pay in the app.

**Driver:** sign in, go online or offline, accept or reject, navigate.

**System:** match, take location updates, move the trip through states (assigned, en route, completed), price the trip.

**Qualities:** match in about **2 seconds**, location updates every **2–3 seconds**, high availability at rush hour, private data, safe payments.

Location shown on the map can be **slightly stale**. The trip state and the charge cannot. Maps and the card gateway are outside. They time out and rate-limit you. Phones lose signal in tunnels. Have a polling fallback when the socket dies.

---

## Numbers, and one slide figure to fix

| | Figure |
|--|--------|
| Registered / DAU | 10 million / 1 million |
| Drivers | 200,000 active per day |
| Peak sessions | about 150,000 |
| Ride requests | 500,000 per day → about **6 per second** |
| Payments | 100,000 per day → about 1 per second |
| Map tiles | about 1 million per hour → about 280 per second |

The slides also say location updates are "3× the ride requests", about **18 per second**. That is not a heartbeat. If 200,000 drivers publish every 3 seconds, that is on the order of **tens of thousands of location writes per second** (200,000 / 3 ≈ 67,000). Design the location path for that. The 18-per-second figure is far too small for "every 2–3 seconds".

Matching and those location writes are the bottleneck. Cards and map APIs are the external risk.

---

## Location and match

```text
Driver app --every few seconds--> Location service
                                      stores a geohash or an H3 cell in Redis
Rider requests
  → cells around the pickup
  → a few free drivers
  → offer the ride to one (or a short list, one after another)
  → first accept wins
```

**Geohash or H3** turns a lat/long into a cell id so "who is near me?" is a key lookup, not a scan of every driver. Geohash is simpler. H3 is more even across the city. Either is a valid answer if you say how you search the neighboring cells.

**One ride, one driver.** The trip row moves from `requested` to `assigned` with a conditional update. A second accept affects zero rows. Timeouts put the trip back if the driver never answers.

Do **not** write every GPS point to PostgreSQL. Redis (or a similar memory store) holds the latest point and the cell index. The trip service stores the route you need for the receipt, sampled, in a durable database.

Fare and payment are PostgreSQL plus the gateway, after the trip completes. Refunds use an idempotency key.

```text
Rider → API gateway → Matching → offer via socket or push
                       Location service streams positions (WebSocket or MQTT)
                       If the socket dies, the app polls
Trip service owns the state machine
Notification service for "driver arrived"
```

Internal calls can be gRPC or Kafka. Phones talk REST or GraphQL through the gateway, plus a socket for the live car.

Users and drivers: PostgreSQL. Trip history: PostgreSQL or a wide-column store if the log gets huge. The live position: Redis.

---

## Final design

The rider and the driver both enter through the gateway. Matching asks "who is near?" from the location box. The live car on the map comes back through the WebSocket, the long-lived connection at the bottom. Paying is a separate box that talks to a card company.

```text
Rider app / driver app / web
        |                         ^ live location and trip status
        v                         |
API Gateway                  WebSocket servers
        |
        +--> User service      (cache + user DB)
        +--> Driver service    (cache + driver DB)
        +--> Matching          (cache + matching DB)
        +--> Location service  (cache + latest points)
        +--> Trip service      (cache + trip DB)
        +--> Payment service   (cache + payment DB) --> card gateway
        |
        +--> notification queue --> push / SMS
        |
CDN serves map tiles and app images, not the GPS stream
```

Matching reads the location cache. The trip row and the charge are the durable path.

## Interview lines

- Heartbeats are tens of thousands per second in this city model, not 18.
- A geo index of cells, then a conditional assign so two drivers cannot take one rider.
- Live location is memory. Money and trip state are durable and consistent.
- Map APIs are slow and billed. Cache routes. Do not call them on every heartbeat.

---

## Interview Q&A

### 1. How do you find a nearby driver in under two seconds?

<details>
<summary>Show Answer</summary>

**Answer:**

Drivers publish a location every few seconds into Redis, keyed by a geohash or H3 cell. A request loads the pickup cell and its neighbors, filters to drivers who are online and free, and ranks a short list by distance and ETA. You offer the trip to one driver. The trip row is assigned only if it is still unassigned. If they decline or time out, you offer the next. You do not scan 200,000 rows.

</details>

### 2. Why is the slide's location QPS misleading?

<details>
<summary>Show Answer</summary>

**Answer:**

Six ride requests per second times three is about 18. Real apps send a point every two or three seconds per online driver. With 200,000 drivers that is on the order of 67,000 writes per second. Those writes belong in an in-memory geo index with a short TTL, not in the orders database. The durable write is the trip, not every meter driven.

</details>

### 3. What is strongly consistent?

<details>
<summary>Show Answer</summary>

**Answer:**

Assignment of the ride and the payment. Two accepts must not both win. The dot on the map can lag by a second. If the map provider fails, show the last point and a retry, and do not block the trip state machine on a tile fetch. If the socket drops, poll. Charge once, with an idempotency key, when the trip completes.

</details>

**Q&A recap:** cell index for "nearby", one conditional assign, money in SQL, GPS in memory.

## Interview Q&A — 6 to 8 years

### S1. Two riders request the same driver at once. Who gets the car?

<details>
<summary>Show Answer</summary>

**Answer:**

One conditional update: set driver to BUSY and trip to ASSIGNED only if the driver is FREE and the trip is still OPEN. The loser gets zero rows and tries the next driver. I do not assign in the app after a casual read of "nearby." Driver location in a geo index is a hint. The assignment row in the database is the decision.

**Corner cases:** A driver who accepts on two phones. The same conditional update, keyed by driver id. A trip offered to ten drivers who all tap yes. Either the first commit wins and the others get 409, or I use a short offer lease so only one offer is live. I state which. A matcher crash after writing ASSIGNED and before notifying the phone. The trip row is already assigned. Notification retries. I do not run a second match. A geo cell so large that "nearby" is 10 km in a dense city, or so small that a driver on the border is invisible. I query neighboring cells. A hot downtown cell. I split popular cells or I accept a hotter shard and isolate it.

</details>

### S2. GPS updates every second for a million drivers. What do you refuse to put on the primary database?

<details>
<summary>Show Answer</summary>

**Answer:**

The raw point stream. I write current location to an in-memory geo index with a TTL, and I sample or batch a coarser trail to durable storage if the product needs replay. Matching reads the geo index. Trip state and money read SQL. If the geo index restarts, drivers republish location within a second or two. I do not block a location ping on a SQL commit.

**Corner cases:** A stale location that still matches because TTL was an hour. I set a short TTL and I ignore points older than N seconds. A driver in a tunnel whose last point keeps them "available" at the wrong place. The TTL handles disappearance. Spoofed GPS. I can sanity-check speed between points and I will not claim I stop every fake. Clock skew that reorders points. Server receive time for "how fresh," device time as a hint. A surge of reconnects after a blip that overwrites a newer point with a buffered older one. I drop points older than the stored one.

</details>

### S3. How do you charge the final fare exactly once when the network drops at the end of the trip?

<details>
<summary>Show Answer</summary>

**Answer:**

The trip completion is a conditional update: COMPLETE where trip = ASSIGNED and driver = this driver. The fare is computed from the server's trip record (distance, time, surge snapshot), not from the phone's last screen alone. Payment uses an idempotency key of the trip id. If the phone dies, the server still completes from the last known state or from a driver-side complete that retries safely. A webhook and a retry must not capture twice.

**Corner cases:** Two different fare quotes because surge changed mid-trip. I snapshot surge at request or at assign and I show that. A driver and a rider both tap complete with different routes. The server route wins, or I store both and pick by policy. I do not average silently if money is involved. A tip added later is a second payment with its own key, not a replay of the fare. A refund and a complete racing. The payment state machine allows REFUND only from CAPTURED. Currency and authorization hold: I auth at start for an estimate and capture the final, and I handle an auth that expired on a long trip by re-auth or a failed-payment state. The trip still completes. Money is a separate state.

</details>

### S4. A region partition splits drivers and riders. What do you keep doing?

<details>
<summary>Show Answer</summary>

**Answer:**

Matching for a city should have one owner (one region or one cell authority) so two matchers cannot assign the same driver. If the partition isolates that owner, I would rather stop matching in that city than double-assign. Location updates can keep flowing into the local index and reconcile. In-progress trips keep their SQL row. When the partition heals, a driver with two OFFERs is fenced by the conditional row. I do not run active-active matchers on the same driver pool.

**Corner cases:** A rider whose request landed in region A and whose driver heartbeat landed in region B. Sticky routing by city id. Failover of the matcher with a cold geo index, so the first minute matches nobody. Drivers republish, and I have a replica of the index if I need faster recovery. A scheduled job that pays drivers running in both regions after failover. Lease it. Surge computed differently in two regions for the same cell. One writer for the surge value per cell.

</details>

**Senior recap:** geo index finds candidates, a conditional row assigns, money is idempotent per trip id, and one matcher owns a city's drivers.

Next: [13_Collaborative_Docs.md](13_Collaborative_Docs.md)
