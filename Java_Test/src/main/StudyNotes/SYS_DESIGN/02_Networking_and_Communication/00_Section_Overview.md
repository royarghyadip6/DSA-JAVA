# 02 — Networking & Communication

This folder matches the course section **Networking & Communication (System Design Fundamentals)**.

Notes are built from the Section 2 slides plus the interview PDFs. Each topic file has **theory first**, then **interview Q&A** at the bottom.

---

## Why this section exists

Every system you design is many machines talking to each other.

```text
User phone  →  network  →  your servers  →  database
```

If you do not understand that path, later topics (scalability, security, YouTube, Uber) will feel like random tool names.

---

## Read in this order (one file per topic)

| # | File | What is inside |
|---|------|----------------|
| 1 | [01_Why_Networking_Matters.md](01_Why_Networking_Matters.md) | Why networking is the foundation |
| 2 | [02_IP_Addresses.md](02_IP_Addresses.md) | IPv4/IPv6, public/private, NAT + Q&A |
| 3 | [03_How_DNS_Works.md](03_How_DNS_Works.md) | DNS lookup, cache, TTL + Q&A |
| 4 | [04_Client_Server_Model.md](04_Client_Server_Model.md) | Client-server, HTTP, state + Q&A |
| 5 | [05_Forward_vs_Reverse_Proxy.md](05_Forward_vs_Reverse_Proxy.md) | Proxies + Q&A |
| 6 | [06_Load_Balancing.md](06_Load_Balancing.md) | Why LB exists + Q&A |
| 7 | [07_API_Gateway.md](07_API_Gateway.md) | API front door + Q&A |
| 8 | [08_CDN.md](08_CDN.md) | Edge caches + Q&A |

---

## How to study one file

1. Read the theory (simple idea + diagram + example).
2. Watch the matching course video.
3. Scroll to **Interview Q&A**. Answer out loud **before** opening the answer.
4. Move to the next file.

Do **not** jump to Protocols until this folder feels easy.

---

## Section cheat sheet

```text
User types shop.com
        |
        v
      DNS  (name → IP of the front door)
        |
        v
  CDN / Reverse proxy / Load balancer / API Gateway
        |
        v
  Many app servers on private IPs  (NAT, VPC)
        |
        v
  Database / other services
```

| Topic | One line |
|-------|----------|
| Networking | How parts exchange data — scale, speed, safety |
| IP | Machine address. IPv4 scarce, IPv6 huge. Public vs private + NAT |
| DNS | Phonebook. Recursive asks, authoritative answers. Cache + TTL |
| Client-server | Client asks, server answers. Sync/async, state/stateless |
| Forward proxy | Helps **clients** go out (privacy, filter) |
| Reverse proxy | Helps **servers** take traffic (LB, SSL, hide origin) |
| Load balancer | One door, many workers, skip dead boxes |
| API Gateway | One **API** door: route, auth, quota, transform |
| CDN | Copies content near users (edge / PoP) |

Do not mix the boxes:

```text
Forward proxy     →  beside the USER
Reverse proxy     →  in front of YOUR APPS
Load balancer     →  spreads clones of the SAME service
API Gateway       →  understands API paths, tokens, limits
CDN               →  global CACHES for files (and some APIs)
```

**Interview speak-order:** Client → DNS → IP → CDN for static → LB or API Gateway → stateless apps on private IPs → cache + database.

---

## What is next

**Protocols** (HTTP, TCP, and friends): [03_Protocols](../03_Protocols/README.md)

---

## Source PDFs used

- `System Design - Updated - Section 2.pdf` (main theory)
- IP, DNS, Client-Server, Proxy, Load Balancing, API Gateway, CDN interview PDFs
