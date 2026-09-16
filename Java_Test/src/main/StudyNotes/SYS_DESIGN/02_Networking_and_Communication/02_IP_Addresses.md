# 2. IP Addresses

## Simple idea

An **IP address** is a unique number given to a machine on a network.

It is like a **home address**. Without it, packets do not know where to go.

```text
"Talk to the order server"  →  actually "Talk to 52.95.12.10"
```

Two versions exist: **IPv4** (old, still everywhere) and **IPv6** (new, huge address space).

Two categories exist: **public** (internet) and **private** (inside your office / VPC).

---

## IPv4

- **IPv4** = Internet Protocol version 4
- **32-bit** address
- Written as four numbers 0–255: `192.168.1.1`
- About **4.3 billion** addresses — not enough for every phone, IoT device, and server
- Still used by most websites and office networks

Problems: address shortage, extra security must be added (IPsec is not built-in by default), fragmentation of the old internet.

---

## IPv6

- **IPv6** = next-generation addressing
- **128-bit** address
- Written in hex with colons: `2001:0db8:85a3:0000:0000:8a2e:0370:7334`
- About **340 undecillion** addresses (practically unlimited)
- Built for IoT, mobile, and future growth
- Better routing, larger space, **IPsec can be built in**

You do not need to memorise a full IPv6 address. Remember: **much longer, hex, solves IPv4 running out**.

---

## IPv4 vs IPv6

| Point | IPv4 | IPv6 |
|-------|------|------|
| Size | 32-bit | 128-bit |
| Look | `192.168.1.1` | `2001:0db8:...:7334` |
| How many | ~4.3 billion | Huge (undecillion) |
| Shortage | Yes | No |
| Security | Extra config (IPsec optional) | IPsec designed in |
| Auto-config | Limited | Better (SLAAC) |

---

## Public vs private IPs

**Public IP**

- Given by an **ISP**
- Unique on the whole internet
- Used to talk to the outside world
- Example style: `203.0.113.45` (the slide used `192.203.23.45` as a sample public-looking address)

**Private IP**

- Used only **inside** a home, office, data centre, or cloud VPC
- **Not** reachable directly from the internet
- Same private range can be reused in every company

Private IPv4 ranges you must remember:

```text
10.0.0.0      –  10.255.255.255
172.16.0.0    –  172.31.255.255
192.168.0.0   –  192.168.255.255
```

Your laptop `192.168.1.5` is private. Your office Spring Boot box `10.2.4.20` is private. The world cannot hit those unless you **publish** them through NAT, a load balancer, or a VPN.

---

## Why private IPs exist

1. **Save public IPv4** — billions of homes share a few public IPs
2. **Security** — internal machines are not sitting on the open internet
3. **NAT** — many devices hide behind one public IP
4. Normal in companies, data centres, and cloud (AWS VPC, Azure VNet)

---

## NAT (Network Address Translation) — very important

**NAT** lets many private devices share **one public IP**.

```text
Phone  192.168.1.10  ─┐
Laptop 192.168.1.11  ─┼─→  NAT router  ─→  public IP 49.x.x.x  ─→  internet
TV     192.168.1.12  ─┘
```

The outside world sees only the public IP. Replies come back to the router, which maps them to the right private device.

This is how IPv4 lasted so long.

---

## Role of IPs in system design

| Area | How IPs help |
|------|----------------|
| **Scalability** | Many machines, many regions, each with an address |
| **Security** | Firewall rules, VPNs, private subnets |
| **Load balancing** | Traffic sent to a VIP (virtual IP) or Anycast IP |
| **Cloud** | Public vs private vs hybrid IP plans (AWS / GCP / Azure) |
| **Microservices** | Pods and containers talk on **internal private IPs** |

You almost never give a user a raw app-server IP. You give them a **name** (DNS) that points to a **load balancer IP**.

---

## Real example (Java / Spring)

```text
User  →  https://api.shop.com     (DNS → public IP of load balancer)
          ↓
     Load balancer (public IP)
          ↓
     App pods on 10.0.x.x         (private IPs, never exposed)
          ↓
     RDS / MySQL on 10.0.y.y      (private IP)
```

---

## Interview lines

- IP = unique network address. IPv4 is 32-bit and scarce. IPv6 is 128-bit and huge.
- Private IPs + NAT save addresses and hide internal machines.
- Load balancers and DNS sit in front so clients do not talk to one server IP.
- In cloud, public subnet vs private subnet is an IP design choice.

---

## Interview Q&A

Read the question. Answer out loud. Then open the answer.

**How to speak:** define → why it matters → one real example (AWS VPC, home NAT, Netflix DNS).

### 1. How do IPv4 and IPv6 addresses differ?

<details>
<summary>Show Answer</summary>

**Answer:**

Interviewers want the **size, look, scale, and security** difference — not a history lesson.

| Point | IPv4 | IPv6 |
|-------|------|------|
| Length | 32-bit | 128-bit |
| Example | `192.168.1.1` | `2001:0db8:85a3::8a2e:0370:7334` |
| Format | Dotted decimal, each part 0–255 | Hex groups with colons |
| How many | ~4.3 billion (not enough) | Practically unlimited |
| Extra | Shortage, NAT everywhere | Auto-config, IPsec designed in |

**Say this:**

> IPv4 is 32-bit and we ran out of addresses, so the world uses NAT. IPv6 is 128-bit, so every device can have a real address, with better routing and built-in security features.

**Example:** Your home Wi-Fi is still IPv4 + NAT. Mobile / IoT / new cloud VPCs increasingly add IPv6.

</details>

### 2. Why do we need private IPs in system design?

<details>
<summary>Show Answer</summary>

**Answer:**

Three reasons:

1. **Save public IPv4** — many companies reuse `10.x` / `192.168.x` inside.
2. **Security** — app servers and databases are not sitting on the open internet.
3. **Cost** — one (or a few) public IPs at the load balancer; thousands of private IPs inside.

Private ranges:

```text
10.0.0.0/8
172.16.0.0/12
192.168.0.0/16
```

**Say this:**

> Private IPs let us build a large internal network without burning public addresses. Users hit a public load balancer; Spring Boot and MySQL stay on private IPs in a VPC.

</details>

### 3. How does NAT help in addressing the IPv4 shortage?

<details>
<summary>Show Answer</summary>

**Answer:**

**NAT** (Network Address Translation) maps many **private** IPs to **one public** IP.

```text
10.0.1.10  ─┐
10.0.1.11  ─┼─ NAT ─→  203.0.113.8  ─→  internet
10.0.1.12  ─┘
```

Why it matters:

- **Reuse** — billions of homes share a small pool of public IPs
- **Security** — inbound internet cannot freely address `10.0.1.10`
- **Scale for ISPs and offices** — fewer public IPs to buy

**Say this:**

> NAT is why IPv4 still works. Many devices share one public IP. Cloud equivalent: NAT gateway so private subnets can call the internet outbound, but nothing inbound.

</details>

### 4. Explain how a load balancer distributes traffic using IPs.

<details>
<summary>Show Answer</summary>

**Answer:**

Clients do not pick `10.0.1.21`. They hit a **front-door IP** (or a DNS name that resolves to it). The LB then chooses a backend.

Three layers you can name:

1. **DNS load balancing** — one domain, several IPs; DNS returns different IPs by location or health.
2. **Layer 4** — decide using **IP + port** (TCP/UDP). Fast, dumb, very scalable.
3. **Layer 7** — peek at **HTTP** (URL, header, cookie) and route `/images` vs `/api`.

**Say this:**

> A virtual IP (or Anycast IP) is the only address clients know. The load balancer fans traffic out to private backend IPs using L4, L7, or DNS, so no single server IP is the bottleneck.

</details>

### 5. How does DNS resolve IP addresses in a large-scale system?

<details>
<summary>Show Answer</summary>

**Answer:**

1. Client asks resolver: "IP of `api.shop.com`?"
2. Resolver returns from **cache**, or asks **root → TLD → authoritative**
3. Authoritative returns **A/AAAA** (maybe several IPs)
4. Client connects and **caches** using TTL
5. Big systems add: **geo DNS**, **failover**, **CDN/Anycast**, so the IP you get is the *nearest healthy front door*, not a random app box

**Say this:**

> DNS is a multi-hop lookup with heavy caching. At scale we do not expose app IPs; we expose load balancer or CDN IPs and use short TTL when we need fast failover.

</details>

**Q&A recap:** IPv4 vs IPv6 = size + shortage vs room. Private IP + NAT = save addresses + hide internals. Users see LB/CDN IPs.

Next: [03_How_DNS_Works.md](03_How_DNS_Works.md)
