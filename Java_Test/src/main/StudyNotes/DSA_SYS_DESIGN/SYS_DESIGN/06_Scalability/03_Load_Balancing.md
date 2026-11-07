# 3. Load Balancing

## Simple idea

Horizontal scaling creates many servers. Users must not pick one.

A **load balancer** is the front door. It spreads requests, skips dead servers, and lets you add capacity.

```text
Users → Load balancer → Server A
                      → Server B
                      → Server C
```

Section 2 explained **why** this box exists. Here you choose **what kind** and **which algorithm**.

---

## Why it matters

| Job | What the user feels |
|-----|---------------------|
| Spread traffic | One server is not melted while others sit idle |
| High availability | A dead server is removed; the site stays up |
| Lower latency | Work goes to a server that can answer |
| Scale out | New servers join the pool |
| Failures | Checkout still works if one node dies |

A busy shop uses this so the sale-hour rush is shared.

---

## Layer 4 vs Layer 7

| | Layer 4 | Layer 7 |
|--|---------|---------|
| Looks at | IP and port (TCP/UDP) | HTTP: URL, headers, cookies |
| Speed | Faster, less work per packet | Smarter, a bit more overhead |
| Can do | Simple spread | Path routing, SSL offload, stickiness |
| Examples | AWS NLB, HAProxy in L4 mode | AWS ALB, Nginx, Traefik |

```text
L4:  "Send this TCP connection to a healthy backend."
L7:  "/images → static servers,  /checkout → checkout servers"
```

Use **L4** when you only need fast fan-out (or raw TCP, such as some database front doors).

Use **L7** when the route depends on the URL or you want TLS termination and HTTP features.

---

## Where it runs

| Kind | Examples | Fit |
|------|----------|-----|
| Hardware | F5, Citrix NetScaler | Very high throughput in a private data centre |
| Software | Nginx, HAProxy, Envoy | Cloud and normal web apps. Cheap and flexible |
| Cloud | AWS ELB, Google Cloud Load Balancing | Managed, ties into autoscaling |

**Software** runs on normal machines or containers. You can change it quickly. It uses some of the server's CPU.

**Hardware** is a dedicated appliance. It is fast and often has DDoS features. It is expensive and slow to grow.

Prefer software or cloud balancers for new cloud apps. Hardware still shows up in large on-prem estates.

---

## How it picks a server

The slides group some of these as "static" and some as "dynamic." In practice, anything that looks at **live** connections or response time is dynamic. Round robin does not.

| Strategy | Rule | Use when |
|----------|------|----------|
| **Round robin** | A, then B, then C, then A | Servers are equal and requests are similar |
| **Least connections** | Fewest open connections | Some requests are much slower than others |
| **IP hash** | Same client IP → same server | You want a simple sticky behaviour |
| **Least response time** | Fastest server right now | You can measure latency cheaply |
| **Weighted** | Stronger servers get more share | Machines are not the same size |
| **Adaptive** | Watch live metrics and adjust | Traffic shape changes through the day |

Round robin is simple and unfair if one box is weaker or one request holds a connection for a long time.

Least connections reacts to load, but the balancer must count connections.

Weighted round robin or weighted least connections is how you mix a large VM and a small VM in one pool.

---

## Health, failover, and sessions

- **Health checks** — TCP connect, HTTP `/health`, or a ping. Fail the check, leave the pool.
- **Failover** — traffic moves to the servers that still pass.
- **Redundancy of the balancer itself** — active-active (both serve) or active-passive (standby takes over). One balancer is also a single point of failure.
- **Stickiness** — if session data is on one app server, keep the user there. Better long term: session in Redis, or no server session.
- **Global** — GeoDNS or Anycast sends users to a nearby region, then a local balancer.

---

## Security the balancer can take on

- **TLS termination** — decrypt HTTPS once, apps do less crypto
- **DDoS filtering** — drop floods before they hit app servers (stronger on some cloud and hardware products)
- **Allow / deny** — IP rules
- **WAF** — block common injection and XSS patterns
- **Rate limits** — cap requests per client

The balancer is not a full security program. You still authenticate and authorize in the app.

---

## A large shop, said simply

```text
DNS (which region)
  →  CDN for images and JS
  →  redundant L7 balancers for the site and APIs
  →  autoscaling app servers
  →  health checks remove a bad node
```

- L7 for pages and APIs (routes, cookies, TLS)
- L4 when you only need fast TCP fan-out
- round robin for similar static work
- least connections when request time varies
- weights if instance sizes differ
- cache so the app tier is smaller

---

## How you choose

| Question | Lean toward |
|----------|-------------|
| Requests are similar, servers match | Round robin |
| Some calls are slow | Least connections |
| Servers differ in size | Weights |
| User must hit the same node | Stickiness or IP hash |
| Need URL routing or TLS offload | Layer 7 |
| Need raw speed, no HTTP logic | Layer 4 |
| Running in AWS/GCP/Azure | Their managed balancer |
| Fixed enterprise data centre | Hardware or HAProxy/Nginx on your machines |

---

## Interview lines

- The balancer shares load, drops dead nodes, and makes scale-out possible.
- L4 is IP and port. L7 understands HTTP.
- Round robin is fair only when servers and requests are similar.
- Least connections and weights follow real capacity.
- TLS offload, WAF, and rate limits are security side effects, not a replacement for app auth.

---

## Interview Q&A

### 1. What is load balancing, and why is it important?

<details>
<summary>Show Answer</summary>

**Answer:**

It spreads incoming traffic across backend servers so one machine is not overloaded.

That improves availability (skip a dead node), performance, fault tolerance, and the ability to add servers as demand grows.

</details>

### 2. Layer 4 vs Layer 7?

<details>
<summary>Show Answer</summary>

**Answer:**

**Layer 4** uses IP and port. It does not read the HTTP body or URL. It is fast. AWS NLB is the usual example.

**Layer 7** uses the HTTP request: path, headers, cookies. It can terminate TLS and route `/api` differently from `/images`. AWS ALB, Nginx, and Traefik are examples.

L4 is quicker and simpler. L7 is flexible and costs a little more CPU.

</details>

### 3. How does a load balancer handle availability and failover?

<details>
<summary>Show Answer</summary>

**Answer:**

Health checks (HTTP, TCP, or ping) mark a server down. New traffic goes only to healthy servers. Run two balancers (active-active or active-passive) so the door itself is not a single failure. Sticky sessions matter only if state sits on one app server. GeoDNS or Anycast can fail over between regions.

</details>

### 4. Round robin vs least connections?

<details>
<summary>Show Answer</summary>

**Answer:**

**Round robin** rotates servers in order. It is fine when every server is equal and every request is similar. It overloads a weak server or a server stuck on long calls.

**Least connections** sends the next request to the quietest server. It fits uneven work, such as mixed fast and slow queries, but the balancer must track connections.

</details>

### 5. What are the advantages of weighted load balancing?

<details>
<summary>Show Answer</summary>

**Answer:**

Stronger servers get a larger share. You can mix instance sizes in one pool (weighted round robin or weighted least connections) instead of pretending every machine is identical.

</details>

### 6. Software balancer or hardware balancer?

<details>
<summary>Show Answer</summary>

**Answer:**

**Software** (Nginx, HAProxy, Envoy) is cheaper, runs in VMs or containers, and changes quickly. It spends CPU on the host.

**Hardware** (F5, NetScaler) is a dedicated box: high throughput and often extra DDoS features, but costly and rigid.

Use software or a cloud balancer for cloud apps. Hardware is an enterprise data-centre choice.

</details>

### 7. How would you load-balance a large e-commerce site?

<details>
<summary>Show Answer</summary>

**Answer:**

Global DNS to a region, redundant balancers, L7 for the site and APIs, autoscaling groups, health checks, CDN and cache in front, least connections or weights for uneven work, and round robin only where servers and requests match.

</details>

### 8. What factors pick the strategy?

<details>
<summary>Show Answer</summary>

**Answer:**

Traffic shape, whether servers match, whether sessions stick, L4 speed vs L7 routing, and whether you are in the cloud or on premises.

</details>

### 9. How does a load balancer improve security?

<details>
<summary>Show Answer</summary>

**Answer:**

It can terminate TLS, rate-limit, integrate with a WAF, apply IP allow and deny rules, and absorb or filter some DDoS before the app. The application still has to authenticate the user.

</details>

**Q&A recap:** L4 vs L7, round robin vs least connections vs weights, health checks, software vs hardware vs cloud.

## Interview Q&A — 6 to 8 years

### S1. When do you need Layer 7 balancing, and what did you just take on?

<details>
<summary>Show Answer</summary>

**Answer:**

When the route depends on the URL, header, or cookie: `/video` to a different pool, a canary by header, WebSocket upgrade, or a per-user stickiness that Layer 4 cannot see. I then terminate or inspect TLS, I own certificate renewal, and I can hurt latency. If I only need to spread TCP connections and survive a dead IP, Layer 4 is simpler and fails less often.

**Corner cases:** Layer 7 inspection breaks mutual TLS unless I forward client certs. A path rule sends `/payments` and `/payments-old` to the wrong place because of a prefix match. HTTP/2 multiplexing hides many requests behind one connection, so Layer 4 balance looks even and Layer 7 would have split them. A WAF rule on the balancer false-positives a legitimate large POST. I log the rule id.

</details>

### S2. Health checks say the node is up. Users still get errors. What is a better check?

<details>
<summary>Show Answer</summary>

**Answer:**

Liveness means "the process should be restarted." Readiness means "do not send traffic." Readiness must touch the thing the request needs, with a tight timeout: the database ping or a local "I have finished startup" flag. It must not be so heavy that the check is the outage. I fail the node out after a few misses, and I require a few successes before it returns, so a flap does not oscillate.

**Corner cases:** A check that always returns 200 inside the process while the thread pool is exhausted. Users time out. The balancer keeps sending more. A check that opens a new database connection can fill the database. A deep check of a downstream two hops away removes every node when that downstream blips. I then have zero capacity. I prefer to serve a degraded response. Startup probes that are too short kill a slow JVM before it can listen.

</details>

### S3. How do you canary 5% of traffic safely?

<details>
<summary>Show Answer</summary>

**Answer:**

Sticky by user or by a header, so one person does not bounce between versions and see a split brain of their own write. I watch error rate and p99 on the canary compared to the baseline, not only CPU. I pick a metric that canaries can fail (checkout success), and I auto-roll back. Writes are the dangerous 5%. I either canary reads first or I make writes compatible both ways.

**Corner cases:** 5% of requests is not 5% of users if one account polls. 5% of a rare endpoint is too few to see the bug. A canary that runs database migrations for everyone. Session created on v2 and the next request on v1 cannot read it. Cache objects written in a new shape. I version the cache key. Load balancer weights that do not sum to 100 and accidentally send 0% to the old pool.

</details>

### S4. A cloud load balancer versus software you run yourself. What do you say at this level?

<details>
<summary>Show Answer</summary>

**Answer:**

The cloud balancer gives me anycast, TLS, and health checks without a box to patch, and it hides its own redundancy. I accept provider limits (rules, connections, idle timeout) and I design inside them. Self-managed (Nginx, Envoy) is right when I need a custom protocol or I am not on that cloud. Then I must run at least two, with a tested config reload, or the balancer is my outage.

**Corner cases:** Idle timeout shorter than my heartbeats, so sockets drop. A connection limit that scales slower than my pods. Cross-zone balancing that doubles the bill. An internal software balancer with a single VIP and no health of its own. Preserving client IP: if I forget the proxy protocol, every user is the balancer's address and my rate limit and audit logs are wrong.

</details>

**Senior recap:** L7 when the route needs the HTTP meaning, readiness is not liveness, canary by user with a rollback metric, and know the balancer's idle timeout and connection cap.

Next: [04_Autoscaling.md](04_Autoscaling.md)
