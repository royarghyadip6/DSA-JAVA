# 03 — Protocols

This folder matches the course section **Protocols (System Design Fundamentals)**.

Notes are built from the Section 3 slides plus the interview PDFs. Each topic file has **theory first**, then **interview Q&A** at the bottom.

---

## Why this section exists

Section 2 taught **where** traffic goes (IP, DNS, proxy, load balancer, CDN).

This section teaches **the rules** machines use when they talk.

```text
Phone  --TCP/UDP-->  Server
App    --HTTP-->     API
API    --REST / gRPC / GraphQL-->  Service
Chat   --WebSocket--> Server
```

If you pick the wrong rule, the product is either **slow** or **wrong**.

---

## Read in this order (one file per topic)

| # | File | What is inside |
|---|------|----------------|
| 1 | [01_TCP_and_UDP.md](01_TCP_and_UDP.md) | Reliable vs fast transport + Q&A |
| 2 | [02_HTTP.md](02_HTTP.md) | Web request/response, methods, status, HTTPS + Q&A |
| 3 | [03_REST.md](03_REST.md) | RESTful API design + Q&A |
| 4 | [04_Real_Time_Communication.md](04_Real_Time_Communication.md) | WebSockets, long polling + Q&A |
| 5 | [05_Beyond_REST.md](05_Beyond_REST.md) | gRPC and GraphQL + Q&A |

---

## How to study one file

1. Read the theory (simple idea + diagram + example).
2. Watch the matching course video.
3. Scroll to **Interview Q&A**. Answer out loud **before** opening the answer.
4. Move to the next file.

---

## Section cheat sheet

| Protocol | One line | Pick it when |
|----------|----------|----------------|
| **TCP** | Reliable, ordered, slower | Web pages, files, email, DB, payments |
| **UDP** | Fast, no delivery promise | Live voice, games, DNS lookups |
| **HTTP** | Request then response, stateless | Browsers and most APIs |
| **HTTPS** | HTTP + TLS | Login, banking, anything secret |
| **REST** | Resources + HTTP verbs | Public CRUD APIs |
| **WebSocket** | Stay open, both sides talk | Chat, live prices, games |
| **Long polling** | HTTP that waits for news | Notifications when WebSocket is too heavy |
| **gRPC** | Fast binary calls (HTTP/2 + Protobuf) | Microservice-to-microservice |
| **GraphQL** | Client asks for exact fields | Mobile/web screens that need flexible data |

**Speak-order in an interview:** What must not be lost? → TCP or UDP. Is it request/response or live? → HTTP or WebSocket. Who is the client? → REST for the public, gRPC inside, GraphQL if the UI needs flexible queries.

---

## What is next

**Architectural Patterns**: [04_Architectural_Patterns](../04_Architectural_Patterns/README.md)

---

## Source PDFs used

- `System Design - updated - Section 3.pdf` (main theory)
- TCP and UDP, HTTP, REST, Real-time communication, Beyond REST interview PDFs
