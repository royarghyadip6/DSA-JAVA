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

## Interview Q&A — 6 to 8 years

### S1. You terminate TLS at the reverse proxy. What did you just change about security and debugging?

<details>
<summary>Show Answer</summary>

**Answer:**

The proxy can see the URL, route by path, and cache. The hop from proxy to the app is a new network. If that hop is plain HTTP, anyone on that network sees the traffic. I still use TLS or a private network there. Logs at the proxy become the place I look for status codes. The app sees the proxy's IP unless I forward the client address in a header I trust only from the proxy.

**Corner cases:** I log the full URL and capture tokens in query strings. A second proxy in front appends another X-Forwarded-For, and I ban the wrong address. HTTP/2 to the proxy and HTTP/1.1 to the app changes connection counts. A client certificate dies at the proxy unless I forward it on purpose. Renewing the certificate on only one proxy node black-holes some users.

</details>

### S2. When is a forward proxy part of the system design, not only a company IT box?

<details>
<summary>Show Answer</summary>

**Answer:**

When my servers must call the internet through a controlled exit: a fixed IP for a partner allow-list, a filter so services cannot reach arbitrary hosts, or a cache for a heavy third-party download. I put that proxy in the design and I give it a timeout and a spare.

**Corner cases:** The proxy becomes a single point of failure for every outbound call. It caches a personalized response. Server-side request forgery: an attacker passes a URL and my server fetches it via the proxy, reaching internal addresses (169.254.169.254, localhost). I allow-list destinations. A transparent proxy intercepts TLS and breaks certificate pinning in a mobile app.

</details>

### S3. The reverse proxy returns 502 and the app logs are empty. How do you narrow it?

<details>
<summary>Show Answer</summary>

**Answer:**

502 means the proxy could not get a valid response. The app may be down, too slow for the proxy timeout, crashing before logs, or speaking the wrong protocol. I compare proxy timeout with app timeout. I check health checks, connection reuse to a dead upstream, and whether only one path (only POST, only large bodies) fails.

**Corner cases:** The app timeout is 30 seconds and the proxy cuts at 15, so the app keeps working after the user already got an error, and the retry runs the work twice. Idle keep-alive: the proxy reuses a connection the app already closed. Upload size limits differ, so small calls work and video upload returns 413 from the proxy. IPv6 upstream only, and the proxy is IPv4.

</details>

### S4. How do you roll a proxy config change without taking the site down?

<details>
<summary>Show Answer</summary>

**Answer:**

Two proxy instances, a syntax check before reload, and a reload that keeps old connections. I canary one node, watch 5xx, then move the rest. I keep the last known good config. Routing rules and TLS certificates are versioned with the app, not edited live by hand.

**Corner cases:** A regex in a path rule matches too much and sends /api/admin to the wrong service. A buffer size change breaks gRPC streaming. Both proxies reload at the same second and drop health checks, so the load balancer marks them all dead. A rollback of the app without a rollback of the route leaves the proxy calling a removed path.

</details>

**Senior recap:** terminating TLS creates a new trusted hop, 502 is a timeout or protocol mismatch, and proxy config is a production change.

Next: [06_Load_Balancing.md](06_Load_Balancing.md)
