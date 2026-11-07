# 10 — Security in System Design

This folder matches the course section **Security — Principles, Practices & Protocols**.

Notes use the Section 10 slides and the interview PDFs. Each file is **theory first**, then **interview Q&A**.

---

## Why this section exists

A system can be fast and still be unsafe. Attackers want data, money, or downtime.

```text
Who are you?          →  authentication
What may you do?      →  authorization
Can others read it?   →  confidentiality
Can others change it? →  integrity
Can users still get in? →  availability
```

Security is a design constraint, like latency. You add it while you design, not after the breach.

**Words in this folder**

| Word | Meaning |
|------|---------|
| **Authentication** | Proving who you are. Login |
| **Authorization** | What that person is allowed to do. An admin can delete. A viewer cannot |
| **Encryption** | Scrambling data so only someone with the key can read it. It can be unlocked |
| **Hash** | A one-way fingerprint. You cannot turn a password hash back into the password |
| **TLS / HTTPS** | Encryption for data while it travels on the network |
| **Firewall** | A filter that drops network traffic you did not invite |

---

## Read in this order

| # | File | What is inside |
|---|------|----------------|
| 1 | [01_Security_Foundations.md](01_Security_Foundations.md) | CIA, STRIDE, common attacks, SDLC + Q&A |
| 2 | [02_Authentication_and_Authorization.md](02_Authentication_and_Authorization.md) | Sessions, JWT, OAuth, OIDC, RBAC, SSO + Q&A |
| 3 | [03_Data_Protection.md](03_Data_Protection.md) | Encryption, hashing, TLS, PKI + Q&A |
| 4 | [04_Network_and_Infrastructure_Security.md](04_Network_and_Infrastructure_Security.md) | Firewalls, zero trust, cloud, OWASP + Q&A |

Sessions, cookies, and CSRF were introduced in [web concepts](../05_Web_Concepts/00_Section_Overview.md). This section is the security view of those ideas.

---

## One-page picker

| Risk | First control |
|------|----------------|
| Someone pretends to be the user | MFA, short-lived tokens, HTTPS |
| Someone reads the database | Encryption at rest, least privilege, KMS |
| Someone reads the network | TLS. Inside the cluster, mTLS |
| A flood of traffic | Rate limit, WAF, scrubbing. Do not only autoscale |
| A bad SQL string | Parameterized queries |
| A stolen password file | Slow password hash (bcrypt or Argon2) plus a salt |
| One hacked service walks to the database | Segment the network. Zero trust. No shared admin key |

---

## What is next

**The System Design Blueprint**: [11_System_Design_Blueprint](../11_System_Design_Blueprint/00_Section_Overview.md)

---

## Source PDFs used

- `System Design - updated - Section 10.pdf`
- Security Focused Q&A
- Authentication & Authorization Q&A
- Data Protection and Secure Communication Q&A
- Network & Infrastructure Security Q&A
