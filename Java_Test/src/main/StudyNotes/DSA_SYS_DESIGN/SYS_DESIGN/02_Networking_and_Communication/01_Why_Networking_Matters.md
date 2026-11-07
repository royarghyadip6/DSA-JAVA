# 1. Why Networking Matters in System Design

## Simple idea

A system is not one Java program. It is **many parts exchanging data**.

Networking is the **road** those parts use.

```text
Browser / Mobile app
        |
        |  network (internet, LAN, Wi-Fi)
        v
Your Spring Boot servers, databases, caches, other APIs
```

If the road is slow, blocked, or unsafe, the whole product feels broken — even if the Java code is perfect.

---

## Four jobs networking does

| Job | In simple words | Example |
|-----|-----------------|---------|
| **Communication** | Move data between client, server, and database | Place-order API call |
| **Load balancing** | Do not dump all traffic on one machine | Festival sale on a shopping app |
| **Security** | Stop strangers from reading or attacking data | HTTPS, firewalls, hiding internal IPs |
| **Efficiency** | Less waiting, less wasted bandwidth | CDN for images, DNS cache |

---

## Why large systems cannot skip this

- Millions of users at the same time need many machines, not one laptop.
- Fast data exchange keeps the app feeling instant.
- Lower latency + backup paths make the system **resilient** (it survives a failure).
- Cloud and distributed systems (AWS, Kubernetes, microservices) are **networks of computers**.

```text
1 user on localhost     =  almost no network pain
1 crore users worldwide =  networking is the design
```

---

## Real example

You open Zomato.

1. Your phone must **find** the right server (DNS + IP).
2. Your request must **reach** an API (client-server).
3. Traffic must be **shared** across many servers (load balancer / API gateway).
4. Pictures of food should load from a **nearby** server (CDN).

All of that is this section.

---

## Interview lines

- Networking is how clients, servers, and data stores **talk**.
- We care because it decides **scale, speed, safety, and uptime**.
- Load balancing, DNS, proxies, gateways, and CDNs are networking building blocks — not optional extras.
- A distributed system is a **networked** system.

---

## Interview Q&A — 6 to 8 years

These questions are for someone who has debugged real traffic. Try them, then open the answer.

### S1. Users say the app is slow, and CPU on the API is low. Where do you look first?

<details>
<summary>Show Answer</summary>

**Answer:**

The time is probably on the network path, not in Java. I check DNS time, TLS handshake, distance to the server, a slow dependency, and packet loss that makes TCP retry. A trace of one request (gateway, service, database) shows which hop owns the milliseconds.

**Corner cases:** The client is on a mobile network with a large round trip. Moving the server to a bigger CPU does nothing. A load balancer health check is fine while the path to the database drops packets. Compression helps large JSON and can hurt tiny responses on a busy CPU. IPv6 and IPv4 paths can differ. One of them may be broken while the other looks healthy.

</details>

### S2. Why is "the network is reliable" a dangerous assumption in a design?

<details>
<summary>Show Answer</summary>

**Answer:**

A call can be lost, delayed, duplicated, or reordered. A timeout means "I do not know," not "it failed." The design needs a timeout, a retry budget, and a way to make the same request safe twice. If I cannot tell whether the other side did the work, I must reconcile (a unique key, a query of the result).

**Corner cases:** Retrying a non-idempotent POST creates a second order. Retrying forever during an outage is a self-inflicted denial of service. A slow network looks like a dead server, so I may fail over while the first server is still writing. Partitions heal, and then both sides have accepted different writes.

</details>

### S3. How do you decide what sits close to the user and what stays in one region?

<details>
<summary>Show Answer</summary>

**Answer:**

Static and cacheable bytes (images, video segments, public pages) move to a nearby edge. The source of truth for money, inventory, and identity usually stays in one place so two copies cannot disagree. I say the latency I am buying and the freshness I am giving up.

**Corner cases:** Personal data at the edge needs a short life and a purge story, or I leak one user's page to another. A "nearby" write replica that accepts orders will split brain when the link to the primary dies. Chat presence wants to be close. Chat history can live farther away if the send path is honest about delivery.

</details>

### S4. A single load balancer or DNS name is now the single point of failure. What do you say in the interview?

<details>
<summary>Show Answer</summary>

**Answer:**

I name it. Then I put at least two load balancers behind one stable address (anycast or a cloud load balancer that is itself redundant). Health checks remove a dead backend. I still admit the data store can be the real single point. Two app servers and one database is not high availability.

**Corner cases:** Both load balancers use the same availability zone. DNS TTL is an hour, so a failover looks "done" in the console and users still hit the old address. Health checks mark a node up because the process listens, while the node cannot reach its database. Clients cache a dead IP.

</details>

**Senior recap:** slowness is often the path, timeouts are not failures, and a redundant front door does not save a single database.

## Check yourself

Can you say this?

> System design starts with how data travels. If that path is slow or fragile, adding more Java code will not save you.

Next: [02_IP_Addresses.md](02_IP_Addresses.md)
