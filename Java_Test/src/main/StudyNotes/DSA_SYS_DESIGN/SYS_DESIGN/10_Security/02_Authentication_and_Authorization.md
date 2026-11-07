# 2. Authentication and Authorization

## Simple idea

**Authentication** answers "who is this?". **Authorization** answers "what may they do?".

```text
Login proves identity     →  authentication
"Admins can delete"       →  authorization
```

A valid login is not permission to do everything. Check the action on every request, on the server. Hiding a button is not authorization.

Sessions and cookies are in [web sessions](../05_Web_Concepts/01_Web_Sessions.md). This file is the security angle.

---

## Ways to prove identity

| Method | What it is | Use it when |
|--------|------------|-------------|
| **Basic auth** | Username and password on every call, only encoded (Base64), not encrypted | Almost never, and only over HTTPS. A poor default |
| **Session** | Server remembers a random id. Browser sends it in a cookie | Classic websites |
| **JWT** | A signed token that carries the user id and expiry | APIs and services, if you accept hard revocation |
| **OAuth 2.0** | A way to give an app **limited access** without giving it your password | "This app may read my calendar" |
| **OpenID Connect** | Login **on top of** OAuth 2.0. Adds an ID token | "Sign in with Google" |

**OAuth 2.0 is not login.** It is delegated authorization. The access token means "this app may call that API". It does not, by itself, prove who the human is. **OpenID Connect** adds the ID token (usually a JWT) with the user's identity. That is the piece people mean by social login.

| | OAuth 2.0 | OpenID Connect |
|--|-----------|----------------|
| Job | Limited access to a resource | Also proves who logged in |
| Tokens | Access token (and often a refresh token) | Those, plus an ID token |
| Identity claims | Not the point | Inside the ID token |
| Example | Posting on your behalf | SSO, "Sign in with Google" |

---

## Session vs token

| | Session | JWT |
|--|---------|-----|
| Server state | A row in a store, often Redis | None. The token is the proof |
| Logout | Delete the row. It works now | The token stays valid until it expires, unless you add a blocklist |
| Scale | Fine if every node shares the store. Sticky sessions are the fragile version | Any node can check the signature |
| Browser | HttpOnly cookie | Cookie, or a header. The header is easy to steal from JavaScript |

The slides say sessions do not scale. They scale when the session lives in a shared store. They do not scale when the session lives in one server's memory.

A JWT is three parts: header, payload, signature. The payload is readable Base64, **not encrypted**, unless you add encryption. Anyone who has the token can read the user id. The signature stops them from changing it.

```text
header.payload.signature
check signature → check expiry, issuer, audience → then trust the claims
```

Reject the `none` algorithm. An attacker will ask you to trust an unsigned token.

**Where to keep it.** `localStorage` is readable by any script on the page, so an XSS bug steals it. An **HttpOnly** cookie is not readable by script. CSRF then matters, because the browser will attach the cookie. [CORS and CSRF](../05_Web_Concepts/03_CORS.md) cover that. Short access tokens (minutes) plus a refresh token limit the damage of theft.

Refresh tokens are credentials. Store them in a place you can delete (a database or Redis), and rotate them. Do not put a 30-day access token in the browser.

---

## Access control

| Model | Rule | Shape |
|-------|------|--------|
| **RBAC** | Permissions hang off roles. Users get roles | Admin, editor, viewer. Simple. Awkward when the rule is "only your region, only on weekdays" |
| **ABAC** | The decision uses attributes of the user, the resource, and the context | `department = HR` and `hour is in business hours`. Flexible. Harder to audit |
| **DAC** | The owner of the object grants access | Unix file owner. Fine for documents. Easy to get inconsistent |
| **MAC** | A central policy the owner cannot override | Military classifications. Strong and rigid |

ABAC fits a multi-tenant product whose rules change by customer, time, and data label. RBAC fits most apps until the role list becomes "admin-but-not-on-Fridays". You can start with roles and add attributes for the few rules that need them.

Check authorization in the service that owns the data. A gateway check is useful and not enough, because internal callers can skip the gateway.

---

## SSO and federation

**Single sign-on:** one login opens many apps. The user stops keeping twenty passwords. You can require MFA in one place, and the helpdesk resets one credential. Logs sit in one identity system. Protocols: SAML, OpenID Connect, sometimes OAuth 2.0 used carefully.

**Federation:** you trust another organization's identity provider (Google, a partner company). Federation is how SSO crosses a company boundary. SSO inside one company does not require a public identity provider.

One identity system is also one big door. Protect it more than any single app: MFA, short sessions, alerts on new devices.

---

## Token risks

| Risk | Why | Mitigation |
|------|-----|------------|
| Theft | XSS and `localStorage`, or a leaked log | HttpOnly cookie, no tokens in logs |
| No revoke | JWT is valid until `exp` | Short life, refresh-token store you can delete |
| Replay | A stolen token works until expiry | HTTPS, short life, bind sensitive actions to a fresh check |
| Bad signature config | `alg: none`, or a public key used as an HMAC secret | Fix the library defaults. Pin the algorithm |
| Long life | A stolen token is a stolen account for days | Minutes for access, rotatable refresh |

Spring Security can validate JWTs and enforce roles. The design choices above are still yours.

---

## Interview lines

- Authentication is who. Authorization is what they may do. Check both on the server.
- OAuth 2.0 delegates access. OpenID Connect is the login layer on top, with an ID token.
- JWT scales because it is self-contained. It is not secret, and it is hard to cancel.
- RBAC is roles. ABAC is attributes. ABAC wins when the rules keep changing.
- SSO is one login for many apps. Put MFA on that one login.

---

## Interview Q&A

### 1. Why JWT for stateless auth?

<details>
<summary>Show Answer</summary>

**Answer:**

The token carries the user id, roles, and expiry, and it is signed. Any service can check it without calling a session database. It is compact and sent as a Bearer header or a cookie. That fits a fleet of services. You still need short expiry, a refresh flow, safe storage, and a plan to revoke. The payload is not encrypted. Never accept the `none` algorithm.

</details>

### 2. OAuth 2.0 vs OpenID Connect?

<details>
<summary>Show Answer</summary>

**Answer:**

OAuth 2.0 lets an app act on a user's behalf without seeing the password. The result is an access token. It is authorization. OpenID Connect adds authentication: an ID token, usually a JWT, that says who logged in. "Sign in with Google" is OpenID Connect. "Let this app post for me" is OAuth 2.0.

</details>

### 3. RBAC vs ABAC?

<details>
<summary>Show Answer</summary>

**Answer:**

RBAC grants permissions through roles such as admin or viewer. It is easy to explain and coarse. ABAC grants from attributes: who the user is, what the resource is, and context such as time or tenant. ABAC fits dynamic, multi-tenant rules. It is harder to test and to explain in an audit. Many systems use roles first and attributes for the exceptions.

</details>

### 4. Why SSO?

<details>
<summary>Show Answer</summary>

**Answer:**

Users sign in once for many applications. Fewer passwords, less fatigue, one place for MFA and password policy, fewer reset tickets, and one audit log. Enterprises usually do this with SAML or OpenID Connect. The identity provider becomes critical. Protect it accordingly. Federation is the extra step of trusting another organization's identities.

</details>

### 5. What goes wrong with tokens?

<details>
<summary>Show Answer</summary>

**Answer:**

Theft via XSS if the token is in `localStorage`. No easy revoke until expiry. Replay if the token is copied. Long lifetimes make theft worse. A mis-set algorithm lets someone forge a token. Mitigate with HTTPS, HttpOnly cookies, short access tokens, refresh tokens you can delete, and checks of signature, issuer, audience, and expiry. Force logout by killing the refresh token and keeping access tokens short.

</details>

**Q&A recap:** prove identity, then authorize the action. Prefer short-lived proof you can cancel over a token that lives for a month.

## Interview Q&A — 6 to 8 years

### S1. A user is logged in. How do they still read another user's order?

<details>
<summary>Show Answer</summary>

**Answer:**

Login answered who they are. It did not answer whether this order is theirs. I load the order and check the owner (or a role that is allowed) on the server. I do not trust an id in the URL, a hidden form field, or a role stuffed inside a token the client can edit. If the check is only in the UI, any other client skips it.

**Corner cases:** A list endpoint that filters in the UI and returns every row to the browser. A guessable id (`/orders/1001`) with no owner check, called an insecure direct reference. A token that says `role=admin` and is not signed by me, or is signed and I forgot to check the signature. An admin impersonation feature with no audit and no expiry. A batch API that checks the first id and writes the rest. A service account that is allowed to do everything because the internal call "was already authenticated" at the edge.

</details>

### S2. How do you revoke access in under a minute?

<details>
<summary>Show Answer</summary>

**Answer:**

Short-lived access tokens (a few minutes) and a refresh token I can delete. For a session id, I delete the server-side session. For a long-lived JWT I cannot delete, I keep a denylist of the token id until it would have expired anyway, and I accept that this store must be up. Password change and logout revoke the refresh family, not only the current tab.

**Corner cases:** A mobile refresh token copied to another device. I detect reuse of a rotated token and revoke the whole family. A gateway that caches a "this token is valid" answer for too long. Workers still holding a token after the user was disabled. I check the user is active on sensitive actions, not only at login. Clock skew that keeps a token alive past what I think `exp` means. An SSO session that I killed in my app and not at the identity provider, so the next click signs them in again.

</details>

### S3. OAuth, OIDC, and a homegrown login. What do you pick for a new product, and what do you still own?

<details>
<summary>Show Answer</summary>

**Answer:**

I use an identity provider for passwords, MFA, and federation. OIDC tells me who the user is. OAuth scopes tell a partner what they may do. I still own authorization inside my app (this user, this order). I do not build a password store to "save time" unless I am ready to own reset, breach response, and MFA. Service-to-service calls use their own credentials (mTLS or a signed service identity), not a user's session pasted into a backend.

**Corner cases:** A redirect URL allow-list that is too wide, so a login code can be sent to an attacker's site. I mention I would lock the allow-list. A token in the query string that lands in logs. I use the body or a header. Scopes that are "all" for every partner. A confused deputy: my service calls another with its powerful credential on behalf of a user who should not have that power. I pass the user context and the other service checks it. Storing the provider's long-lived refresh token in a place the browser can read.

</details>

### S4. RBAC was fine until one customer asked for "only these projects." What do you change?

<details>
<summary>Show Answer</summary>

**Answer:**

Roles stay coarse (admin, member, viewer). The new rule is a relation: this user, this project, this action. That is an authorization check on the resource, sometimes called ABAC or ReBAC. I keep the decision in one service so every API uses it. I cache decisions briefly and I purge on membership change. I test "removed from the project" as a first-class case.

**Corner cases:** A role checked at login and cached all day, so a demotion does nothing. A list of projects in the token that exceeds header size for a big customer. I look up membership server-side. An admin role that bypasses the resource check and is copied into shared test accounts. Two systems (the API and the export job) that apply different rules, so the export leaks. Delegation ("act as this team") without an expiry. A default of allow when the authorization service times out. Sensitive actions should fail closed.

</details>

**Senior recap:** authorize the resource on every request, make revoke real with short tokens and a server-side delete, let an identity provider own passwords, and check membership when roles are not enough.

Next: [03_Data_Protection.md](03_Data_Protection.md)
