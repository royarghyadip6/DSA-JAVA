# 4. Network and Infrastructure Security

## Simple idea

The network is not a safe room. People inside it make mistakes, and attackers who pass one door move sideways to the next.

```text
Edge        →  firewall, proxy, rate limit
Middle      →  separate zones, private subnets
Every call  →  prove identity again (zero trust)
```

A reverse proxy was defined in [forward vs reverse proxy](../02_Networking_and_Communication/05_Forward_vs_Reverse_Proxy.md). Here it is a security control, not only a traffic director.

---

## Firewall vs reverse proxy

| | Firewall | Reverse proxy |
|--|----------|----------------|
| Question | May this packet pass? | Which service should handle this HTTP call? |
| Looks at | IP, port, protocol. Some look at the application | Host, path, headers, TLS |
| Also does | Drops scans and unexpected ports | Hides servers, terminates TLS, can cache and limit |
| Examples | Security groups, NACLs, a hardware firewall | Nginx, AWS ALB, an API gateway |

You want both. The firewall stops random ports. The proxy is the only public door for HTTP. A **WAF** is the proxy's stricter cousin: it blocks injection-shaped requests. It is not a substitute for safe code.

Host firewalls sit on the machine. Network firewalls sit in front of a zone. Cloud firewalls are the same idea as rules on a VPC.

---

## Rate limits, throttling, IP lists

| Control | Behaviour |
|---------|-----------|
| **Rate limit** | A cap per user or per IP. Past the cap, reject or delay |
| **Throttle** | Slow down so the system degrades instead of dying |
| **IP allow / deny** | Drop or admit whole addresses |

Server-side algorithms you can name:

- **Fixed window** — 100 requests per minute. Simple. A burst at the window boundary can double the rate.
- **Token bucket** — tokens refill at a steady rate. A small burst is allowed, then it tightens.
- **Leaky bucket** — requests leave at a steady rate. Extra requests wait or get dropped.

**Exponential backoff** is what the **client** should do after a `429`. It is not the server's limiter.

Rate limits cut abuse, credential stuffing, and a slice of DDoS. They also protect a downstream database from one noisy client. Put them at the gateway, and again on expensive operations such as login.

An IP list is a poor login. Carriers and offices share addresses. Use it for a partner system with a stable address, plus real authentication.

---

## Segmentation

```text
Internet → DMZ (proxy, public API) → app subnet → database subnet
                 nothing talks to the database except the app
```

If one web server is compromised, it should not stroll to every database. In the cloud that is VPCs, subnets, security groups, and network ACLs. Private VLANs are the on-premises version.

---

## Zero trust

**Never trust, always verify.** Being "on the VPN" or "in the VPC" is not permission.

- Every request has an identity (user or service).
- Least privilege: that identity can do one job, not administer the account.
- The network is cut into small segments.
- You log and watch, because a stolen token should look odd.

This matters more in cloud and hybrid networks, where there is no single office firewall around everything. You still use firewalls. Zero trust means the firewall is not the only control.

---

## Cloud shared responsibility

The provider secures the building: hardware, the hypervisor, and the managed service's platform. **You** secure what you put in it.

| You run | You still own |
|---------|----------------|
| Virtual machines | OS patches, firewall, IAM, your data |
| Kubernetes | Workload identity, policies, images, data |
| Serverless | The function's IAM role, timeouts, input validation, data |
| S3, RDS | Bucket policy, encryption, who can query |

**IAM** is how that ownership is expressed.

- **Authentication** — this user or this role is who they claim.
- **Authorization** — this role may read one bucket, not all of them.
- **Audit** — CloudTrail (AWS) or the equivalent records the call.

Give services roles, not long-lived access keys copied into config. A person should not use the root account. Turn on MFA for humans.

**CSPM** tools scan the account for mistakes: a public bucket, an open security group, a disk without encryption. They find misconfiguration. They do not write your app.

---

## Containers and Kubernetes

| Layer | What to do |
|-------|------------|
| Image | Minimal base. Scan (Trivy, Clair). Sign images. Rebuild when a library is vulnerable |
| Identity | RBAC. A workload gets a service account that can do its job only |
| Pod | Do not run as root. No extra Linux capabilities. The old **PodSecurityPolicy** API is gone (removed in Kubernetes 1.25). Use **Pod Security Admission** (restricted profile) or a policy engine (Gatekeeper, Kyverno) |
| Network | NetworkPolicy so pods cannot talk to every other pod |
| Mesh | Istio or Linkerd can add mTLS and path rules. Useful, and it is more moving parts |
| Secrets | Kubernetes Secrets are Base64 in etcd, not encrypted until you turn encryption on. Prefer a vault, and mount a short-lived secret |

Serverless: one IAM role per function, a short timeout, and the API gateway in front. A function with the admin role is a stolen key waiting to happen.

---

## Service mesh

A mesh (Istio, Linkerd) sits beside the pods and handles service-to-service calls:

- **mTLS** so traffic is encrypted and both sides show a certificate
- policy so only the payment service may call the ledger
- metrics and logs for those calls
- retries, limits, and circuit breaking in one place

You can do mTLS and JWT without a mesh. The mesh stops every team from inventing it. The cost is operational complexity and a little latency. Say that in interviews. "We run Istio" is not a security design.

---

## OWASP risks

The Top 10 list is revised every few years. Learn the risks, not a frozen numbering. The slides and the interview PDF point at these:

| Risk | Mitigation |
|------|------------|
| **Injection** | Parameterized queries. Do not concatenate SQL or shell commands |
| **Broken authentication** | MFA, rate-limited login, slow salted password hashes |
| **Sensitive data exposure** | TLS, encryption at rest, no secrets in logs or git |
| **Broken access control** | Check the user against the specific object on the server. IDs in the URL are guesses |
| **Security misconfiguration** | Private by default, patch, review security groups. CSPM helps |
| **XSS** | Encode output. Set a Content-Security-Policy. HttpOnly cookies |
| **CSRF** | SameSite cookies, anti-CSRF tokens. See [CORS](../05_Web_Concepts/03_CORS.md) |
| **SSRF** | Do not let the server fetch a URL the user typed, unless it is allowlisted |
| **Insecure deserialization** | Do not turn untrusted bytes into objects |
| **Vulnerable components** | Update dependencies. Scan the build |
| **Weak logging** | Log auth failures and admin actions. Protect the logs |
| **XXE** | Disable external entities in XML parsers |

XSS and CSRF are browser bugs. Injection and broken access control are server bugs. Scanning dependencies is a pipeline job, not a one-time install.

---

## Interview lines

- A firewall allows packets. A reverse proxy routes and hides HTTP services. Use both.
- Rate-limit with a token or leaky bucket. Backoff is the client's job after a 429.
- Zero trust: the network location is not identity. Authenticate every call. Grant least privilege.
- In the cloud, IAM and your data are yours. The provider runs the hardware.
- Pod Security Policies are retired. Say Pod Security Admission or an admission policy. Do not treat Kubernetes Secrets as a vault until etcd encryption is on.
- OWASP: injection, broken auth, broken access control, misconfiguration, XSS. Fix them in code, not only in a WAF.

---

## Interview Q&A

### 1. Firewall vs reverse proxy?

<details>
<summary>Show Answer</summary>

**Answer:**

A firewall allows or blocks traffic by IP, port, and protocol (and sometimes by application). It is the gate on the network. A reverse proxy sits in front of HTTP services, hides them, terminates TLS, and can balance, cache, and rate-limit. Nginx and an AWS ALB are proxies. A security group is a firewall. Production uses both. A WAF on the proxy blocks abusive HTTP. It does not replace parameterized queries.

</details>

### 2. How does rate limiting protect the backend?

<details>
<summary>Show Answer</summary>

**Answer:**

It caps how many calls one user or IP can make in a period, so one client cannot exhaust the database or log in forever. It blunts abuse and part of a DDoS, and it keeps usage fair. Fixed windows are simple and can be gamed at the boundary. A token bucket allows a small burst, then a steady rate. A leaky bucket smooths the outflow. When the client is limited, it should back off. Backoff is not the server's algorithm. Also throttle so overload degrades instead of crashing.

</details>

### 3. What is zero trust?

<details>
<summary>Show Answer</summary>

**Answer:**

Never trust a request only because it came from inside the network. Authenticate and authorize every call, grant least privilege, segment the network, and monitor. That limits how far an attacker walks after one foothold. It matters in cloud and hybrid systems where a single perimeter does not exist. Firewalls stay. They are no longer the whole design.

</details>

### 4. How do you secure a workload on Kubernetes?

<details>
<summary>Show Answer</summary>

**Answer:**

Scan and sign minimal images. Use RBAC and a narrow service account. Do not run containers as root. Enforce that with Pod Security Admission or Gatekeeper. PodSecurityPolicy was removed in Kubernetes 1.25, so do not offer it as the current control. Add NetworkPolicies so pods cannot talk to everything. Use mTLS for service calls, via a mesh if you already operate one. Keep real secrets in a vault. Kubernetes Secrets are only Base64 unless etcd encryption is enabled.

</details>

### 5. OWASP risks and fixes?

<details>
<summary>Show Answer</summary>

**Answer:**

Injection: parameterized queries. Broken authentication: MFA, rate limits, bcrypt or Argon2. Sensitive data: TLS and encryption at rest. Broken access control: check permission on the object, every time. Misconfiguration: least privilege and reviews. XSS: encode output and set Content-Security-Policy. CSRF: SameSite cookies and CSRF tokens. SSRF: do not fetch arbitrary URLs. Deserialization: do not trust pickled or Java-serialized input. Dependencies: patch and scan. Logging: record security events and protect the log. XXE: disable external XML entities. The published Top 10 order changes. The habits do not.

</details>

### 6. How does a service mesh help?

<details>
<summary>Show Answer</summary>

**Answer:**

It moves service-to-service security out of application code. Istio or Linkerd can require mTLS, restrict which service may call which, and log the calls. They also offer rate limits and circuit breakers. You can build the same controls without a mesh. The mesh pays off when many services would otherwise each invent TLS and policy. It adds complexity and some latency.

</details>

### 7. What is IAM for?

<details>
<summary>Show Answer</summary>

**Answer:**

IAM is identity and access in the cloud. It authenticates users and services, then authorizes each action with a policy. Least privilege means a service role can touch only its bucket or its queue. Audit logs show who called what. Prefer roles over access keys pasted into config. Humans use MFA. The provider secures the platform. IAM is how you secure your slice of it.

</details>

**Q&A recap:** filter the network, prove identity on every call, and keep each service in the smallest permission you can.

## Interview Q&A — 6 to 8 years

### S1. The VPC is private, so the team skips service authentication. What do you object to?

<details>
<summary>Show Answer</summary>

**Answer:**

A network boundary is not a user identity. Anyone who can run a pod, a script on a build agent, or a SSRF bug inside the network can call the service. I still authenticate service-to-service (mTLS or a signed identity) and I still authorize the action. Private subnets and security groups reduce who can open a socket. They do not replace the check.

**Corner cases:** A security group that is `allow all` inside the VPC, so the database is reachable from every test tool. An admin port published on a public load balancer by a bad manifest. Metadata endpoints and internal admin panels that assume "only we can route here." A partner VPN that drops the partner into the same flat network as production. Egress that allows every destination, so a compromised app can send data anywhere. I allow-list egress for the few hosts we call.

</details>

### S2. Zero trust sounds like "no VPN." What do you actually change in the design?

<details>
<summary>Show Answer</summary>

**Answer:**

Every request carries a provable identity, and the service checks it, whether the caller is a human, a job, or another service. I segment networks so a breach of one app does not mean a route to every database. I log the identity on the allow and on the deny. I do not trust a source IP as a person. The VPN can stay as one way to reach an admin host. It is not the authorization system.

**Corner cases:** A mesh that encrypts traffic and then every service account has the same broad role. I still scope roles. Health checks and metrics scrapes that bypass auth and leak data. A break-glass admin path with a shared password and no audit. Device posture that I cannot enforce on a CI job, so I give CI its own narrow role instead of a human's role. "Internal" DNS names that are guessable and routed from the office without a second factor.

</details>

### S3. How do you keep a Kubernetes workload from being the shortest path to the data?

<details>
<summary>Show Answer</summary>

**Answer:**

The pod gets a service account that can touch only its bucket prefix and its database role, not the cloud admin. The container does not run as root, the filesystem is read-only where it can be, and the image is from our registry with a known build. Secrets come from a secret store at start, not from the image. The database is not on a public address. Admin tools are a separate, smaller path with MFA.

**Corner cases:** A default service account in the namespace that can list secrets. A sidecar with more permissions than the app. A debug container left in the manifest. Pulling `:latest` so I cannot say what ran during the incident. A network policy I meant to write and never applied, so every pod can dial every other pod. Host path mounts that expose the node's credentials. Resource limits missing, so one tenant's pod can starve the node and the readiness of everyone else.

</details>

### S4. What do you log for security, and what do you refuse to log?

<details>
<summary>Show Answer</summary>

**Answer:**

I log who, what action, which resource, when, and the decision (allow or deny), with a request id. I log admin changes and login failures with a rate limit so the log itself is not the outage. I do not log passwords, full card numbers, raw tokens, or the contents of private files. Logs go to a store the app cannot delete, with a retention I can explain. I alert on a spike of denies and on a new admin grant.

**Corner cases:** An authorization deny that returns a body explaining exactly which rule matched, which helps a scanner. I keep the detail in the server log and a generic error for the client. Logs so verbose they contain the next request's Authorization header because I logged all headers. A retention of forever that violates the privacy promise, or a retention of one day that makes an incident uninvestigable. Two teams with two clocks and no request id, so I cannot reconstruct a path. A WAF in front and no application log, so I see blocked noise and not the successful abuse that looked like a normal call.

</details>

**Senior recap:** private network plus identity, least privilege on the workload's own credentials, and audit logs that record the decision without storing the secret.

Next course section: **The System Design Blueprint** — [11_System_Design_Blueprint](../11_System_Design_Blueprint/00_Section_Overview.md)
