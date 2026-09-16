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

## Check yourself

Can you say this?

> System design starts with how data travels. If that path is slow or fragile, adding more Java code will not save you.

Next: [02_IP_Addresses.md](02_IP_Addresses.md)
