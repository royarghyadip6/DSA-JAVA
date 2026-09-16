# 5. Forward Proxy vs Reverse Proxy

## Simple idea

A **proxy** is a **middle person**.

It sits between someone who asks and someone who answers.

```text
A  →  Proxy  →  B
A  ←  Proxy  ←  B
```

Why bother?

- Hide who A or B really is
- Cache (answer faster the second time)
- Control traffic
- Filter bad sites / bad requests
- Compress data

Two main types: **forward** (helps the **client**) and **reverse** (helps the **server**).

---

## Memory trick

```text
Forward proxy  =  faces the internet WITH the user
Reverse proxy  =  faces the users IN FRONT of your servers
```

If the company laptop uses it → forward.

If Nginx / Cloudflare sits in front of your Spring Boot apps → reverse.

---

## Forward proxy (client side)

```text
Your browser  →  Forward proxy  →  websites on the internet
```

The website sees the **proxy IP**, not your IP.

Used for:

- Hide user identity (privacy)
- Bypass geo-blocks
- Company: block Facebook during work
- Cache pages so the office WAN is faster
- VPN / Tor style browsing

Tools: **Squid**, **Shadowsocks**, **VPNs**, **Tor**.

---

## Reverse proxy (server side)

```text
Users  →  Reverse proxy  →  App server 1
                         →  App server 2
                         →  App server 3
```

Users think they talk to `www.shop.com`. They never see `10.0.1.21`.

Used for:

- **Load balancing**
- **Caching** hot pages
- **SSL termination** (HTTPS decrypted here)
- **DDoS / WAF** protection
- Hide backend IPs

Tools: **Nginx**, **HAProxy**, **Cloudflare**, **AWS ELB** (acts as reverse proxy / LB).

API Gateway (next topic) is a **smart reverse proxy** for APIs.

---

## Side-by-side

| Feature | Forward proxy | Reverse proxy |
|---------|---------------|---------------|
| Position | Between **client** and internet | Between **users** and **your backends** |
| Who it protects / helps | The client / company users | The servers |
| Typical user | Employee, VPN user | Hosting team, SRE |
| Anonymity | Hides **client** IP | Hides **server** IP |
| Load balancing | No (not the main job) | Yes |
| Content filter | Yes (block sites) | Not for "block Facebook" |
| Caching | Yes | Yes |
| Examples | VPN, Squid, Tor | Nginx, HAProxy, Cloudflare |

---

## When to use which

| Need | Forward | Reverse |
|------|---------|---------|
| Hide user identity | Yes | No |
| Load balancing | No | Yes |
| Protect backend | No | Yes |
| Cache | Yes | Yes |
| Block websites for staff | Yes | No |

**Forward** = clients need safe / controlled access **out**.

**Reverse** = your servers need protection, cache, and traffic split **in**.

---

## SSL termination (reverse proxy)

HTTPS is encrypted. Decrypting burns CPU.

**SSL termination** = reverse proxy decrypts TLS, then talks HTTP (or re-encrypts) to backends.

```text
Browser  ==HTTPS==>  Reverse proxy  ==HTTP inside VPC==>  Spring Boot
                       (certificate lives here)
```

Benefits:

- Backends skip heavy crypto
- One place to renew certificates
- Faster responses

Cloudflare often does this at the edge.

---

## How reverse proxy stops DDoS (simple)

- Filter bad IPs / bots **before** they reach Spring Boot
- **Rate limit** (max N requests per IP)
- Detect weird patterns
- Spread leftover traffic across many servers

---

## Transparent proxy (extra, often asked)

A **transparent proxy** intercepts traffic **without the client configuring a proxy**.

The user does not set "proxy host". The network (ISP, office gateway) silently redirects HTTP.

It is still a kind of forward proxy, but **invisible** to the app. Used for filtering and caching. Harder for the user to notice; privacy-sensitive.

---

## Interview lines

- Proxy = middle box. Forward helps **clients**. Reverse helps **servers**.
- Reverse proxy = load balance + cache + SSL + hide origin.
- Tools: Squid/VPN vs Nginx/HAProxy/Cloudflare.
- SSL termination offloads TLS from app servers.

---

## Interview Q&A

Read the question. Answer out loud. Then open the answer.

### 1. What is a proxy server, and why is it used?

<details>
<summary>Show Answer</summary>

**Answer:**

A **proxy** is a middle server. Client talks to the proxy; proxy talks to the real destination; proxy returns the answer.

Used for: **privacy / security**, **cache**, **traffic control / load balancing**, **content filtering**, **compression**.

</details>

### 2. Explain the key differences between a forward proxy and a reverse proxy.

<details>
<summary>Show Answer</summary>

**Answer:**

| Feature | Forward | Reverse |
|---------|---------|---------|
| Position | Client ↔ internet | Users ↔ **your** backends |
| Purpose | Client anonymity, access control | Protect servers, LB, cache, SSL |
| Who uses it | End users, companies | Hosting / SRE |
| Examples | VPN, Squid, Tor | Nginx, HAProxy, Cloudflare, AWS ELB |

**Forward** = "help me go out." **Reverse** = "help my servers take traffic in."

</details>

### 3. How does a forward proxy improve security and privacy?

<details>
<summary>Show Answer</summary>

**Answer:**

- Hides **client IP** — site sees proxy IP
- VPN-style proxies can **encrypt** traffic from ISP snooping
- Company proxy **blocks** malware / phishing sites
- **Access control** — only finance team can hit certain URLs

</details>

### 4. How does a reverse proxy help in load balancing and caching?

<details>
<summary>Show Answer</summary>

**Answer:**

**Load balancing:** spread requests (round robin, least connections, IP hash) so no app server dies.

**Cache:** store hot HTML/images; next request never hits Spring Boot.

**Bonus:** block junk / DDoS at the proxy (Cloudflare style) before it reaches origin.

</details>

### 5. What are some real-world examples of forward and reverse proxies?

<details>
<summary>Show Answer</summary>

**Answer:**

**Forward:** NordVPN / ExpressVPN, office web filter, Tor.

**Reverse:** Cloudflare CDN, Nginx in front of apps, AWS Elastic Load Balancer.

</details>

### 6. When should you use a forward proxy vs. a reverse proxy?

<details>
<summary>Show Answer</summary>

**Answer:**

| Need | Forward | Reverse |
|------|---------|---------|
| Hide user identity | Yes | No |
| Load balancing | No | Yes |
| Protect backends | No | Yes |
| Cache | Yes | Yes |
| Block staff websites | Yes | No |

Forward when **clients** need controlled/safe outbound access (VPN). Reverse when **servers** need a shield and a traffic splitter.

</details>

### 7. What are some common tools and technologies used for each type of proxy?

<details>
<summary>Show Answer</summary>

**Answer:**

| Type | Tools |
|------|--------|
| Forward | Squid, Shadowsocks, Tor, VPNs |
| Reverse | Nginx, HAProxy, Cloudflare, AWS ELB |

</details>

### 8. How does a reverse proxy protect backend servers from DDoS attacks?

<details>
<summary>Show Answer</summary>

**Answer:**

- **Filter** bots and bad IPs first
- **Rate limit** per IP
- **Anomaly detection** (unusual patterns)
- **Spread** remaining traffic across many servers so one box does not melt

Origin IPs stay hidden, so attackers hit the proxy fabric, not `10.0.1.21`.

</details>

### 9. How does SSL termination work in a reverse proxy?

<details>
<summary>Show Answer</summary>

**Answer:**

The proxy **decrypts HTTPS**. Backends can speak HTTP on the private network (or the proxy re-encrypts).

Benefits: less CPU on app servers, faster responses, certificates managed **in one place**.

Example: Cloudflare terminates TLS at the edge.

</details>

### 10. What are the advantages of using Cloudflare, Nginx, or HAProxy as a reverse proxy?

<details>
<summary>Show Answer</summary>

**Answer:**

| Tool | Why people pick it |
|------|-------------------|
| **Cloudflare** | DDoS, CDN cache, global LB |
| **Nginx** | Fast web server, easy config, static cache |
| **HAProxy** | Serious LB, health checks, high availability |

</details>

### 11. How does a transparent proxy differ from a forward or reverse proxy?

<details>
<summary>Show Answer</summary>

**Answer:**

A **transparent proxy** intercepts traffic **without the client setting a proxy host**. The network redirects packets.

It behaves like a **forward** proxy but is **invisible** to the app. Used by ISPs/offices for filter and cache.

Forward/reverse are usually **explicit**. Transparent is **silent intercept**.

</details>

**Q&A recap:** Forward = client helper. Reverse = server front door. SSL termination, DDoS, tool names, transparent intercept.

Next: [06_Load_Balancing.md](06_Load_Balancing.md)
