# 1. Security Foundations

## Simple idea

**Security** is keeping the system trustworthy: the right people see the data, the data is not silently changed, and the service stays up.

More machines mean more doors. Every API, queue, and admin port is an entry point.

---

## The CIA triad

| Letter | Meaning | Attack that breaks it | Usual defense |
|--------|---------|----------------------|----------------|
| **Confidentiality** | Strangers cannot read it | Leak, stolen backup, open bucket | TLS, encryption at rest, IAM |
| **Integrity** | Strangers cannot change it unnoticed | SQL injection, a tampered message | Signatures, parameterized queries, checksums |
| **Availability** | The real users can still get in | DDoS, ransomware, deleting the only copy | Rate limits, redundancy, backups |

This availability is the same word as in [reliability](../09_Reliability_Availability_DR/01_System_Reliability.md), seen from an attacker. A system that is fast to fail over can still be knocked down by a flood, or locked by ransomware.

CIA pulls apart. An open endpoint is highly available and not confidential. A lock with one person who knows the key is confidential and fragile.

---

## Threat modeling

Before you pick tools, name:

```text
Assets     →  passwords, payments, personal data
Actors     →  criminal, curious user, insider, broken script
Entry      →  API, login, admin port, dependency
Abuse      →  what they do if they get in
Control    →  what stops or limits that
```

**STRIDE** is a checklist so you do not forget a class of abuse:

| | Threat | You need |
|--|--------|----------|
| **S** | Spoofing (fake identity) | Strong auth, MFA |
| **T** | Tampering | Integrity checks, signed tokens |
| **R** | Repudiation ("I never did that") | Audit logs that the actor cannot edit |
| **I** | Information disclosure | Encryption, least privilege |
| **D** | Denial of service | Limits, redundancy |
| **E** | Elevation of privilege (become admin) | Authorization checks on every action |

Do this at design time, and again when a feature adds a door. The interview notes also name **DREAD** (damage, reproducibility, exploitability, affected users, discoverability) as a way to score a threat. A simple high / medium / low on impact and likelihood is enough. Write the result down. Revisit it.

---

## How attackers get in

- APIs with no auth, or auth that trusts a user id from the request body
- Servers left on default passwords, open security groups, public buckets
- Logins with no MFA and no limit on guesses
- Ports and admin tools exposed to the internet

The attack surface is every place a packet or a user can touch. Shrink it.

---

## Attacks you must be able to name

| Attack | Hurts | What it is | What actually helps |
|--------|-------|------------|---------------------|
| **DDoS** | Availability | Many machines flood you | Rate limits, WAF, a scrubbing service (Cloudflare, AWS Shield). Detect a spike in traffic or failed logins |
| **MITM** | Confidentiality and integrity | Someone sits on the path and reads or edits traffic | TLS. A VPN on untrusted networks |
| **Injection** (SQL and friends) | Confidentiality and integrity | Your code treats user text as a command | **Parameterized queries**. A WAF is a backup, not the fix |
| **Spoofing** | Identity | Someone pretends to be a user, a host, or a site | MFA and signed tokens for users. Do not trust IP or a header as identity |

**DDoS and autoscaling.** The slides list autoscaling as a mitigation. It keeps real users up during a genuine burst. An attacker can also force you to scale and send you the bill. Limit and scrub first. Scale so legitimate traffic survives. Do not treat "add more pods" as the defense.

**Certificate pinning** (the app accepts only one certificate) makes a MITM with a rogue certificate harder. It also breaks the app when you rotate certificates. Use it for a few high-risk mobile clients, not as the default for every website.

**Spoofing is several problems.** MFA stops a stolen password. It does not stop someone lying about a source IP. Do not use the client IP as the user id. Email spoofing is a mail-server problem (SPF, DKIM, DMARC). DNS spoofing is reduced by DNSSEC and by checking TLS certificates.

**SQL injection in Java:** pass values through `PreparedStatement` or JPA parameters. Never build SQL by gluing strings.

---

## Security in the lifecycle

**Shift left** means you think about abuse before the code is finished.

| Phase | Security work |
|-------|----------------|
| Requirements | What are we protecting? Threat model |
| Design | Trust boundaries, auth, encryption |
| Coding | Validation, parameterized queries, no secrets in git |
| Test | Security tests, fuzzing (throw junk at parsers) |
| Deploy | Secrets in a vault, least privilege |
| Run | Patch, watch logs, rotate keys |

A penetration test the week before launch is late. It can still find bugs. It cannot redesign a public bucket.

---

## Habits

- Encrypt on the wire and on disk (later file).
- Put services in private networks. Open only the front door.
- Validate input. Encode output so a browser cannot treat it as script.
- Log who did what. Alert on login storms and denied admin calls.

---

## Interview lines

- CIA: cannot read, cannot silently change, real users can still get in.
- STRIDE is the threat checklist. Do it when you design, and when you add a feature.
- Injection is fixed in code with parameters. A WAF is extra.
- DDoS is fought at the edge. Autoscaling alone grows the bill.
- TLS gives confidentiality, integrity, and the server's identity. Turn off TLS 1.0 and 1.1.

---

## Interview Q&A

### 1. How would you design authentication for a distributed app?

<details>
<summary>Show Answer</summary>

**Answer:**

Use an identity provider with OpenID Connect so apps do not collect passwords themselves. Issue a short-lived access token (about 15 minutes) and a refresh token. Send them only over HTTPS. Validate the signature (HMAC or RSA) on every service, and check issuer, audience, and expiry. Prefer an HttpOnly cookie for browsers. Add MFA, rate-limit login, and rotate signing keys. Details of JWT and OAuth are in the next file.

</details>

### 2. How does CIA show up in a design?

<details>
<summary>Show Answer</summary>

**Answer:**

Confidentiality: TLS, encryption at rest, IAM so most services cannot read the data. Integrity: signatures, hashes, and validated input so a change is detected. Availability: load balancing, autoscaling, and DDoS controls so real users still get in. They trade off. A wide-open endpoint is available and not confidential.

</details>

### 3. Threats between microservices?

<details>
<summary>Show Answer</summary>

**Answer:**

A service calls another with no identity. Internal APIs leak data. Nobody has one audit trail. Someone forges the gateway. Mitigate with mTLS or a signed identity on every call (zero trust), TLS inside the network, a gateway that authenticates and rate-limits, and central logs. "We are inside the VPC" is not authentication.

</details>

### 4. How do you survive a DDoS?

<details>
<summary>Show Answer</summary>

**Answer:**

Rate-limit at the CDN or API gateway. Block bad patterns with a WAF. Use a scrubbing service (Cloudflare, AWS Shield) so junk never hits your origin. Spread real traffic with a load balancer. Autoscale so a real spike has capacity, and put a ceiling on it so an attack cannot scale your bill without limit. Alert on a sudden jump in requests, latency, or failed logins.

</details>

### 5. What does TLS do, and how do you run certificates at scale?

<details>
<summary>Show Answer</summary>

**Answer:**

TLS encrypts the bytes, detects tampering, and proves the server name via a certificate. HTTPS is HTTP inside TLS. At scale, terminate TLS on the load balancer or gateway with automated certificates (Let's Encrypt, or AWS Certificate Manager). Renew before expiry. Send HSTS so browsers refuse plain HTTP. Disable SSL and TLS 1.0 and 1.1. Prefer TLS 1.3. If the inside network is not trusted, encrypt again to the service. Do not stop at the load balancer and speak cleartext the rest of the way.

</details>

### 6. Secure data storage in the cloud?

<details>
<summary>Show Answer</summary>

**Answer:**

Encrypt at rest with keys in KMS. Give each service a role with only the permissions it needs. Keep passwords and API keys in a vault (Secrets Manager, HashiCorp Vault), not in git. Log access. A public bucket or a database password in a config file undoes the encryption.

</details>

### 7. What is threat modeling, and when do you do it?

<details>
<summary>Show Answer</summary>

**Answer:**

Name the assets, the attackers, the doors, and the controls. Use STRIDE so spoofing, tampering, repudiation, leaks, denial of service, and privilege jumps all get a look. Do it in design, write it down, and repeat when the system changes. Scoring with impact and likelihood (DREAD is one old checklist) tells you what to fix first.

</details>

**Q&A recap:** name the asset and the door, then close it with identity, encryption, and limits.

## Interview Q&A — 6 to 8 years

These answers stay at design level: what to protect and which tradeoff to name. They are not attack steps.

### S1. How do you start a threat model when the interviewer says "secure this design"?

<details>
<summary>Show Answer</summary>

**Answer:**

I name the asset (money, personal data, the admin action) and the boundary it crosses (browser, partner, queue, admin tool). Then I ask what must stay true: only the owner reads it, a payment is not replayed, an admin action is auditable. I map those to controls I already have in the diagram (login, TLS, authorization on the service, immutable logs). I do not recite a framework name and stop.

**Corner cases:** A threat model that lists every buzzword and never points at a box in my drawing. An internal "trusted network" that includes every contractor laptop. A queue message that is a command ("refund this") with no check that the sender may refund. Logs that contain the secret I just protected in transit. A third-party script on the page that can read the DOM. I mention supply chain and the admin path, not only the public API.

</details>

### S2. STRIDE or a checklist produced 40 items. How do you prioritize in the interview?

<details>
<summary>Show Answer</summary>

**Answer:**

I rank by what a failure costs and how exposed the door is. Unauthenticated access to payment or to other users' data comes first. A missing audit log on a rare admin tool is serious and not equal to a public write. I pick three and say the control. The rest I would track. A senior answer is a priority, not a taxonomy recital.

**Corner cases:** Treating "we use HTTPS" as done while object storage is world-readable. Two controls that assume the other exists (the gateway authenticates, the service trusts every caller from the VPC, and the gateway can be bypassed). A denial-of-service concern that I answer only with "add a firewall" while a single unpaged query can still exhaust the database. Insider paths: the same person can deploy and approve their own payment change. I split duties for that action.

</details>

### S3. Where do you put security so a later feature does not quietly drop it?

<details>
<summary>Show Answer</summary>

**Answer:**

In the path that cannot be skipped: authorization in the service that owns the data, not only in the UI. Defaults that are closed (new endpoints are authenticated). Secrets in a store, not in the repo. A migration and a template that include the check. CI that fails on a known unsafe pattern for my stack. I still review the payment and privacy changes by hand.

**Corner cases:** A new mobile client that calls a service directly and skips the gateway check I was proud of. A "temporary" debug route left in production. Feature flags that turn off auth for a test and ship that way. An internal admin API with the same deployment and no second factor. Security tests that only hit the happy path with an admin token. I test a normal user touching someone else's id.

</details>

### S4. What do you say about DDoS without designing a research paper?

<details>
<summary>Show Answer</summary>

**Answer:**

I separate volumetric junk (a CDN and a provider scrub it) from application abuse (a few expensive queries, a login storm). For the second I use rate limits, auth, pagination caps, and a timeout so one request cannot hold a connection forever. I shed load with 429 before the database falls over. I do not claim I can absorb any size of attack.

**Corner cases:** A limit that blocks a whole country because of one NAT. A WAF rule that blocks the real client and the health check. Caching as a shield that accidentally caches private responses. Auto-scaling into an attack so the bill is the outage. A dependency (DNS, identity provider) that is the single door. I name who absorbs the big flood (the cloud or CDN) and what I must still limit myself (expensive authenticated calls).

</details>

**Senior recap:** point at the asset and the door in your own diagram, rank three risks, enforce the rule in the service that owns the data, and separate a big flood from an expensive query.

Next: [02_Authentication_and_Authorization.md](02_Authentication_and_Authorization.md)
