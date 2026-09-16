# 3. How DNS Works

## Simple idea

People remember **names**. Machines use **IP numbers**.

**DNS** (Domain Name System) is the **phonebook of the internet**.

```text
You type:     google.com
DNS returns:  142.250.x.x   (an IP)
Browser then talks to that IP
```

Without DNS, you would bookmark `142.250.190.78` instead of `google.com`.

---

## Why DNS matters in system design

- Users and apps reach services by **stable names**, even when IPs change
- You can move servers, add regions, or fail over — **change DNS**, not every client
- Large systems use DNS for **routing, load balancing, and CDN**

---

## Types of DNS servers

Remember four roles:

| Server | Job in one line |
|--------|-----------------|
| **Recursive resolver** | Does the lookup **for you** (ISP, `8.8.8.8`, Cloudflare `1.1.1.1`) |
| **Root name server** | First hint: "`.com` lives over there" (there are 13 root **identities**) |
| **TLD name server** | Knows who owns `example.com` inside `.com` / `.org` / `.in` |
| **Authoritative name server** | The **final truth** for that domain (A, CNAME, MX records) |

Simple memory:

```text
Recursive  =  the helper who asks around
Authoritative  =  the owner who knows the real IP
```

---

## Resolution process (learn this by heart)

User types `https://example.com`.

```text
1. Browser cache   —  did I look this up recently?
2. OS cache        —  Windows/Linux DNS cache, /etc/hosts
3. Recursive resolver (ISP / 8.8.8.8)
       │
       ├─ if not cached → Root
       ├─ Root says → go to .com TLD
       ├─ TLD says → go to example.com's authoritative server
       └─ Authoritative returns the IP (A / AAAA record)
4. Resolver caches the answer (TTL)
5. Browser connects to that IP
```

Full chain:

```text
Browser → OS → Resolver → Root → TLD → Authoritative → IP back
```

Most of the time **cache** stops the chain early. That is why the second visit feels faster.

---

## Caching and TTL

**Why cache?** Less waiting. Less load on root / TLD / authoritative servers.

**Where cache lives**

| Place | Example |
|-------|---------|
| Browser | Chrome DNS cache |
| OS | `ipconfig /displaydns`, `/etc/hosts` |
| Recursive resolver | ISP / Google / Cloudflare cache (helps **many** users) |

**TTL** (Time-To-Live) = how many seconds a record may be reused.

```text
TTL = 60 seconds   →  changes spread fast, more DNS queries
TTL = 24 hours     →  fewer queries, slow to change IP / failover
```

Interview trick: before a big cut-over, **lower TTL** a day earlier so caches expire quickly.

---

## DNS in large systems

**High availability**

- Several DNS servers (primary + secondary)
- **Anycast DNS** — same IP advertised from many cities; user hits the nearest

**DNS load balancing**

- One name, **several IPs**
- **Round-robin** — rotate IPs
- **Geo routing** — India users get Mumbai IP, US users get Virginia IP
- **Failover DNS** — unhealthy IP is removed

**CDN + DNS**

- DNS (or Anycast) sends the user to the **nearest edge** (PoP), not always to origin

**Security risks** (names only here; answers in Q&A)

- Cache poisoning / spoofing
- DDoS on DNS
- MITM on DNS queries
- NXDOMAIN floods

Mitigations you will say: **DNSSEC**, **DoH / DoT**, rate limiting, Anycast.

---

## Real example

```text
api.shop.com  →  DNS  →  13.234.x.x  (load balancer in Mumbai)
                   or   52.x.x.x    (failover in Singapore)
```

Your Spring Boot pods never appear in public DNS. Only the **front door** IP does.

---

## Interview lines

- DNS translates names to IPs. Recursive asks; authoritative answers.
- Always mention the cache layers and **TTL**.
- Large systems use DNS for geo-routing, failover, and CDN steering.
- Security: DNSSEC + encrypted DNS + Anycast against DDoS.

---

## Interview Q&A

Read the question. Answer out loud. Then open the answer.

### 1. Explain the DNS resolution process step by step.

<details>
<summary>Show Answer</summary>

**Answer:**

DNS turns `www.example.com` into an IP.

1. User types the name in the browser
2. **Browser cache**
3. **OS cache** (`/etc/hosts`, OS DNS cache)
4. **Recursive resolver** (ISP, `8.8.8.8`, `1.1.1.1`)
5. If still unknown → **root** server (13 root identities)
6. Root points to **TLD** (`.com`, `.org`, `.in`)
7. TLD points to **authoritative** server for that domain
8. Authoritative returns the **IP** (A / AAAA)
9. Resolver **caches** it (TTL)
10. Browser **connects** to the web server

```text
Browser → OS → Resolver → Root → TLD → Authoritative → IP
```

Most visits stop at a cache step. Mention that — it shows you know production.

</details>

### 2. What is the difference between authoritative and recursive DNS servers?

<details>
<summary>Show Answer</summary>

**Answer:**

| | Recursive resolver | Authoritative server |
|--|--------------------|----------------------|
| Job | Looks up **on behalf of** the client | Holds the **real records** for a domain |
| Has the final truth? | No — it collects answers | Yes — A, CNAME, MX, etc. |
| Who runs it | ISP, Google Public DNS, Cloudflare | Domain owner / Route53 / registrar |

**One line:** recursive **fetches**; authoritative **owns the answer**.

</details>

### 3. How does DNS caching improve performance? Where does it occur?

<details>
<summary>Show Answer</summary>

**Answer:**

Caching avoids repeating the full root→TLD→authoritative walk.

Benefits: lower **latency**, less load on DNS, faster repeat visits.

| Place | Helps |
|-------|--------|
| Browser | That user, that browser |
| OS | All apps on that laptop |
| Recursive resolver | **Many users** of the same ISP / 8.8.8.8 |

</details>

### 4. What is TTL in DNS, and why is it important?

<details>
<summary>Show Answer</summary>

**Answer:**

**TTL** = how many seconds a cached record is allowed to be reused.

| TTL | Effect |
|-----|--------|
| Short (60s) | Failover / IP change is fast; more DNS queries |
| Long (24h) | Cheap and fast lookups; slow to move traffic |

**Interview tip:** Lower TTL **before** a migration, wait for old TTL to expire, then switch IPs.

</details>

### 5. How does DNS-based load balancing work in large-scale systems?

<details>
<summary>Show Answer</summary>

**Answer:**

One name, **several possible IPs**. DNS chooses which IP to return.

- **Round-robin** — rotate IPs
- **Geo routing** — nearest region
- **Failover** — drop unhealthy IPs
- **Anycast** — same IP announced from many sites; internet routes to nearest

This improves availability and latency. It is **not** as precise as an L7 load balancer (TTL delay, caches). Often used **together** with a real LB.

</details>

### 6. What are common DNS-related security threats, and how can they be mitigated?

<details>
<summary>Show Answer</summary>

**Answer:**

| Threat | What happens | Mitigation |
|--------|----------------|------------|
| **Spoofing / cache poisoning** | Fake IP, user goes to a fake site | **DNSSEC** |
| **DDoS on DNS** | Domain becomes unresolvable | Rate limit, Anycast, extra capacity |
| **MITM on DNS** | Queries altered in transit | **DoH** or **DoT** (encrypted DNS) |
| **NXDOMAIN flood** | Queries for junk names exhaust resolver | Response rate limiting, DNS firewall |

**Say this:** DNS is a control plane. If it lies or dies, the site is gone even if app servers are healthy.

</details>

**Q&A recap:** Walk the 10 steps, recursive vs authoritative, caches + TTL, DNS LB, DNSSEC/DoH/Anycast.

Next: [04_Client_Server_Model.md](04_Client_Server_Model.md)
