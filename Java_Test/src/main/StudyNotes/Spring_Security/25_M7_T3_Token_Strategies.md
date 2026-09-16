# 7.3 — Access and Refresh Token Strategies

> **Module 7 · Topic 3** · JWT
> Baseline: Spring Security 6.x on Boot 3.x, Java 17+
> New to this? Read **In Plain English** below first, then come back to the Version Matrix.

---

## Version Matrix

| Concern | Spring Security 5.x | **Spring Security 6.x (baseline)** | Spring Security 7.x |
|---|---|---|---|
| Token issuance | Deprecated `spring-security-oauth2` project | **Spring Authorization Server 1.x** (`spring-boot-starter-oauth2-authorization-server`) | Authorization Server 1.4+ |
| Refresh rotation | manual | **`TokenSettings.reuseRefreshTokens(false)`** — default is `true`, so rotation is **off** unless you enable it | same default |
| Reuse detection (family revocation) | none | **not built in** — the mechanism is yours | not built in |
| Revocation endpoint | none | **`/oauth2/revoke`** (RFC 7009) | same |
| Introspection | none | **`/oauth2/introspect`** (RFC 7662); client side via `opaqueToken(...)` | same |
| Refresh token storage | none | **`OAuth2AuthorizationService`** — in-memory or `JdbcOAuth2AuthorizationService` | same |
| Sender-constrained tokens | none | mTLS-bound tokens (RFC 8705) in Authorization Server 1.3+; **DPoP (RFC 9449) in later 6.x / AS 1.4+** | DPoP first class |
| Default lifetimes | — | **access 5 min, refresh 60 min, authorization code 5 min** | same |
| Client-side storage guidance | localStorage common | **BFG / BFF pattern recommended; `HttpOnly` cookies for refresh** | same |

---

## Why This Exists

[`23_M7_T1_JWT_Fundamentals.md`](23_M7_T1_JWT_Fundamentals.md) ended on the central tension: a
JWT cannot be un-signed, so a token is valid until `exp` no matter what happens to the account.
[`24_M7_T2_JWT_Spring_Integration.md`](24_M7_T2_JWT_Spring_Integration.md) showed that the
resource server has no revocation mechanism at all — a valid signature and a future `exp` is
acceptance.

This topic is how real systems live with that. The answers are not cryptographic; they are
architectural, and they are all trade-offs between the statelessness that motivated JWTs and the
control that operating a system requires. Every one of them reintroduces some server-side state.
The engineering skill is choosing *how much*, and being able to say out loud what you gave up.

The interview version of this topic is unusually discriminating. Almost everyone can describe
"short access token, long refresh token". Far fewer can explain refresh token rotation, almost
nobody can explain reuse detection and the token family model, and the number who can honestly
describe what "JWT logout" means is smaller still. Those three are the questions that separate a
senior engineer from someone who has used a library.

---

## In Plain English

**The one-line version:** Because a signed token cannot be cancelled once issued, real systems hand out
two different credentials, a short-lived one used everywhere and a long-lived one shown to exactly one
place, so that the widely-shared credential expires quickly and the durable one can be cancelled.

**An analogy.** Think about a hotel. When you check in you are given two things. The first is a plastic
room key card that opens your door, the gym, and the pool, and which stops working automatically at
checkout time. The second is your booking reference, which you show at the front desk when you need a
new key card.

The key card is presented constantly and to lots of different readers, so it is heavily exposed. That is
acceptable precisely because it expires on its own and nobody has to be told to cancel it. The booking
reference goes to exactly one place, the front desk, and almost never leaves your pocket, so it can
safely last the whole week. Notice the pattern: the thing that gets waved around everywhere is made
harmless by being short-lived, and the thing that lasts is made safe by being rarely shown.

Now a genuinely clever refinement. Suppose the front desk cancels your booking reference each time it
issues a new key card and gives you a fresh reference along with the card. A well-behaved guest
therefore never presents the same reference twice. So if an old, already-used reference ever turns up at
the desk, the clerk can conclude something specific: two people are holding copies of this booking, and
one of them is not you. The clerk cannot tell which of you is the impostor, so the only safe action is
to cancel the entire booking and make the real guest come back with their passport. That is refresh
token rotation with reuse detection, and it is the highest-value idea in this file.

**How it actually works, step by step.**

The short-lived credential is the access token. It is typically a JWT, it is sent on every request to
every service, and it usually lives around fifteen minutes. The long-lived credential is the refresh
token. It is sent only to the login server, only when the access token runs out, and it typically lives
days or weeks.

The refresh token should deliberately **not** be a JWT. The whole point of a JWT is that it can be
checked without calling anyone, and here there is exactly one checker, the login server, which is
already looking it up in a database. Making it a JWT would gain nothing and would remove the ability to
cancel it.

The access token's lifetime is the same thing as your revocation window. If you disable somebody's
account, they keep working for as long as their current access token lasts, because no service will
consult anyone about it. Fifteen minutes is the usual compromise. Shortening it to one minute sounds
safer but multiplies traffic against the login server by fifteen, and turns that server into the thing
that fails first during a busy period.

A refresh token is a password in every meaningful sense: a long-lived secret that grants access simply
by being presented. So it is stored hashed rather than in plain text. One deliberate difference from
password storage is the hashing choice: passwords use a slow algorithm such as bcrypt specifically to
frustrate guessing human-chosen words, whereas a refresh token is 256 random bits with nothing to guess,
so a fast SHA-256 is correct and a slow hash would only add latency.

Rotation means each refresh consumes the token and returns a new one. Reuse detection means that if an
already-consumed token is presented, the whole lineage, called a token family, is cancelled at once.
Getting this right depends on one small implementation detail: the database update that marks a token
used must be conditional on it not already being used, so two simultaneous requests cannot both succeed.
Without that condition the family silently splits into two valid lineages and the detection never fires.

Logout deserves a precise answer, because it is a favourite interview question. You cannot invalidate an
access token that has already been issued. A correct logout therefore revokes the refresh token family
on the server, which means the session cannot be extended, and deletes the tokens on the client. Access
genuinely ends a few minutes later when the current access token expires, unless you also maintain a
blocklist of token identifiers. The honest summary is that there is no such thing as stateless logout;
every implementation reintroduces some server-side state, and the design question is how much.

Finally, where a browser keeps these tokens matters more than almost anything else here. Keeping them in
`localStorage` is the most common choice and the wrong default, because any piece of JavaScript on the
page, including one in an advertisement or a dependency, can read and send them elsewhere. The better
arrangement is the access token in a plain JavaScript variable in memory and the refresh token in a
cookie marked `HttpOnly`, which JavaScript cannot read at all. The best arrangement, where you can
afford it, is that the browser never holds a token whatsoever and a small server-side component holds
them on its behalf.

**Why should a beginner care?** This is where token-based authentication stops being a tutorial and
starts being a system you have to operate. The questions that arrive in week two of a real project all
live here: how does a user log out, what happens when we fire someone, why is somebody still able to use
the API ten minutes after we disabled them, and what do we do when a token is stolen. Every answer is a
trade-off rather than a setting, and knowing which trade-off you made is the whole skill.

**Words you will meet in this file**

| Term | In plain words |
|---|---|
| Access token | The short-lived credential presented to every service on every request. Usually a JWT lasting minutes. |
| Refresh token | The long-lived credential presented only to the login server, used to obtain new access tokens. |
| Opaque token | A random string that means nothing on its own and must be looked up on the server. The right shape for a refresh token. |
| Revocation window | How long a cancelled user keeps working. It is exactly the access token lifetime unless you add more machinery. |
| Rotation | Issuing a brand-new refresh token on every refresh and marking the old one used, so no token is ever valid twice. |
| Reuse detection | Noticing that an already-used refresh token has been presented, which proves two parties hold copies. |
| Token family | All refresh tokens descended from one original login. Cancelled as a unit when reuse is detected. |
| `family_id` | The column tying a lineage together so that a single database statement can cancel all of it. |
| Absolute versus sliding expiry | Whether the family's end date is fixed at login or pushed forward on every refresh. You want a fixed upper limit. |
| `jti` | The unique identifier inside a token. The handle you would put on a blocklist. |
| Denylist or blocklist | A short-lived list of cancelled token identifiers, checked on each request. Closes the gap that short expiry leaves open. |
| Introspection | Asking the login server on every request whether a token is still valid. Immediate, but reinvents sessions with added latency. |
| Token version | A counter stored per user and copied into tokens, so incrementing it invalidates everything issued before. |
| `localStorage` | Browser storage readable by any JavaScript on the page. A poor place for a credential. |
| `HttpOnly` cookie | A cookie the browser sends automatically but JavaScript cannot read. Good for a refresh token. |
| BFF | Backend for Frontend. A small server-side component that holds the tokens so the browser never sees one. |
| Sender-constrained token | A token cryptographically tied to the client holding it, so a stolen copy is useless to anyone else. |
| DPoP | One such scheme, where the client signs each request with a private key the browser cannot export. |
| mTLS-bound token | Another such scheme, tying the token to a TLS client certificate. Practical between services, not for browsers. |
| Spring Authorization Server | The Spring project that issues tokens. Note that rotation is switched off by default and you must enable it. |
| `invalid_grant` | The standard error returned when a refresh token is unknown, expired, already used, or part of a cancelled family. |

**If you remember only one thing:** an access token cannot be taken back, so the refresh token is where
all your real control lives, and rotating it with reuse detection is what turns a stolen credential into
a detected incident.

---

## Core Concepts

### 1. Why the Split Exists

**In simple terms:** Two credentials exist because one cannot be both widely used and long-lasting
safely, so the widely-used one expires fast and the long-lasting one is shown to only one place.

A bearer token is a credential that anyone holding it can use. That single property drives the
entire design.

Give a client one long-lived token and you have a credential that is presented on every request,
travels through every proxy, sits in client storage for weeks, and cannot be revoked. Give a
client one short-lived token and the user re-authenticates every fifteen minutes, which no product
will accept.

The split resolves the contradiction by separating two jobs that were never the same job:

| | Access token | Refresh token |
|---|---|---|
| Purpose | Prove identity to resource servers | Obtain new access tokens |
| Lifetime | Minutes | Days to months |
| Presented to | Every resource server, every request | **The authorization server only** |
| Exposure | High — many services, many hops, many logs | Low — one endpoint, one hop |
| Format | Self-contained JWT, verified offline | **Opaque**, checked against server state |
| Revocable | No (until `exp`) | **Yes, immediately** |
| Stored server-side | No | **Yes, hashed** |

Read the table as one idea: **the high-exposure credential is made harmless by being short-lived,
and the long-lived credential is made safe by being rarely exposed and fully revocable.** The
access token accepts the risk of wide distribution because its value decays in minutes. The
refresh token accepts a long life because it touches exactly one endpoint and that endpoint can
refuse it.

This also explains why a refresh token should **not** be a JWT. A JWT's value is offline
verification, and there is exactly one verifier here — the authorization server — which is already
performing a database lookup. A self-contained refresh token gains nothing and loses revocability.

### 2. Choosing the Access Token Lifetime

**In simple terms:** However long the access token lasts is exactly how long a disabled user keeps
working, so this single number is both your security setting and your load on the login server.

The access token lifetime **is** the revocation window. Disable an account and the user retains
access for up to that long, because no resource server will ask anyone whether the token is still
good.

| Lifetime | Revocation window | Refresh calls per user per 8 h day | Fit |
|---|---|---|---|
| 1 min | 1 min | 480 | Only with sender-constrained tokens; refresh traffic dominates |
| **5 min** | 5 min | 96 | High-value APIs; payments, administration |
| **15 min** | 15 min | 32 | **The common default.** Sound balance |
| 60 min | 60 min | 8 | Server-to-server, third-party partners |
| 24 h | 24 h | 0.3 | Effectively unrevocable. Almost never justified |

The load calculation is worth doing out loud, because "just make it one minute" sounds safer than
it is. Ten thousand concurrent users on a five-minute token generate roughly 33 refresh requests
per second against a single authorization server that also performs a database write per rotation.
At one minute that is 166 per second. The authorization server becomes a availability
single point of failure for the entire estate, and it will fail during exactly the traffic spike
that made you worry about security in the first place.

My default is 15 minutes, dropping to 5 for services that move money or change entitlements, and
rising to 60 for machine clients that re-authenticate with client credentials and have no human
in the loop. I would then treat a shorter window as something to buy with a denylist rather than
with refresh traffic, which is section 5.

### 3. Storing Refresh Tokens — A Refresh Token Is a Credential

**In simple terms:** A refresh token grants access simply by being presented, so treat it like a
password and store only a hash of it, never the value itself.

This is the single most commonly skipped control, and the framing that makes it obvious is:
**a refresh token is a password.** It is a long-lived secret that grants access on presentation.
Everything you know about password storage applies.

```sql
CREATE TABLE refresh_token (
    id             UUID PRIMARY KEY,
    token_hash     CHAR(64)    NOT NULL UNIQUE,   -- SHA-256 hex of the raw token
    family_id      UUID        NOT NULL,          -- lineage; see section 4
    user_id        VARCHAR(64) NOT NULL,
    client_id      VARCHAR(64) NOT NULL,
    device_label   VARCHAR(128),                  -- "Chrome on macOS", for the sessions screen
    issued_at      TIMESTAMPTZ NOT NULL,
    expires_at     TIMESTAMPTZ NOT NULL,
    used_at        TIMESTAMPTZ,                   -- non-null once rotated away
    revoked_at     TIMESTAMPTZ,
    revoked_reason VARCHAR(32),                   -- LOGOUT | ROTATED | REUSE_DETECTED | ADMIN
    created_ip     INET,
    user_agent     VARCHAR(256)
);

CREATE INDEX ix_refresh_family ON refresh_token (family_id);
CREATE INDEX ix_refresh_user   ON refresh_token (user_id, revoked_at);
```

Four decisions embedded in that schema:

**Hashed, not encrypted, not plaintext.** The server never needs the raw value back — it only needs
to check a presented value — so hashing is correct and removes the key from the equation, exactly
as [`03_M1_T3_Cryptography.md`](03_M1_T3_Cryptography.md) argues for passwords.

**SHA-256 rather than bcrypt**, which is the one place this deliberately diverges from password
storage. The threat bcrypt defends against is offline brute force of a *low-entropy human-chosen*
secret. A refresh token is 256 bits from `SecureRandom`; there is no dictionary and no feasible
brute force, so the work factor buys nothing and costs a few hundred milliseconds on a hot path.
Being able to say *why* the rule differs here is the interesting part.

**A unique index on the hash**, so lookup is a single indexed read and a duplicate is impossible.

**`used_at` and `revoked_at` as separate columns**, because "rotated away normally" and "revoked
because something was wrong" are different facts and you need to distinguish them during an
incident.

The generation and comparison follow the rules from file 03:

```java
byte[] raw = new byte[32];
new SecureRandom().nextBytes(raw);                                  // 256 bits
String token = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
String hash  = HexFormat.of().formatHex(
        MessageDigest.getInstance("SHA-256").digest(token.getBytes(UTF_8)));
```

Lookup is by hash, so it is an equality match on an indexed column and the timing-attack concern
that applies to comparing secrets in application code does not arise — there is no byte-by-byte
comparison an attacker can walk.

### 4. Refresh Token Rotation and Reuse Detection

**In simple terms:** Because a well-behaved client never reuses a refresh token, seeing one used twice
proves two parties hold copies, and the only safe response is to cancel the whole lineage.

This is the highest-value concept in the topic, and it is where most candidates stop.

**Rotation** means every use of a refresh token consumes it: the client presents refresh token
`RT1`, receives a new access token *and a new refresh token* `RT2`, and `RT1` is marked used and
can never be used again.

Rotation alone is a modest improvement — it bounds how long a stolen token stays useful, provided
the legitimate client refreshes before the attacker does. **Reuse detection** is what makes it
powerful, and it follows from a simple observation:

> Under rotation, a correctly behaving client never presents the same refresh token twice.
> Therefore, if an already-used token is presented, **there are two holders of the lineage**, and
> one of them is an attacker. You cannot tell which — so you must revoke both.

That is the **token family** model. Every refresh token issued from the same original
authentication shares a `family_id`. Presenting an already-used token revokes the entire family,
forcing a full re-authentication.

```mermaid
sequenceDiagram
    participant U as Legitimate client
    participant A as Attacker
    participant AS as Authorization Server
    participant DB as refresh_token store

    Note over U,DB: Normal rotation
    U->>AS: POST /oauth2/token  grant_type=refresh_token  RT1
    AS->>DB: find hash(RT1)  -> active, family F
    AS->>DB: mark RT1 used_at=now; insert RT2 (family F)
    AS-->>U: AT2 + RT2

    Note over A: RT2 is exfiltrated (XSS, stolen backup, malicious proxy)
    A->>AS: POST /oauth2/token  RT2
    AS->>DB: find hash(RT2) -> active
    AS->>DB: mark RT2 used; insert RT3 (family F)
    AS-->>A: AT3 + RT3
    Note over A: attacker now holds a working lineage

    Note over U: the legitimate client still holds RT2 and eventually refreshes
    U->>AS: POST /oauth2/token  RT2
    AS->>DB: find hash(RT2) -> used_at IS NOT NULL  ** REUSE **
    AS->>DB: UPDATE refresh_token SET revoked_at=now,<br/>revoked_reason='REUSE_DETECTED' WHERE family_id=F
    AS->>AS: emit security event: family F compromised, user U
    AS-->>U: 400 invalid_grant
    Note over U: user is forced to re-authenticate

    A->>AS: POST /oauth2/token  RT3
    AS->>DB: RT3 revoked (family F)
    AS-->>A: 400 invalid_grant
    Note over A: attacker locked out; access ends at AT3's exp, minutes away
```

The algorithm, stated precisely:

```
on refresh(presented):
    h = sha256(presented)
    row = SELECT ... FROM refresh_token WHERE token_hash = h
    if row is null:                      -> 400 invalid_grant   (unknown token)
    if row.revoked_at is not null:       -> 400 invalid_grant   (family already burned)
    if row.used_at is not null:          -> REUSE DETECTED
                                            revoke ENTIRE family_id
                                            emit security event
                                            -> 400 invalid_grant
    if row.expires_at <= now:            -> 400 invalid_grant
    if user disabled / password changed: -> revoke family, 400 invalid_grant

    BEGIN TRANSACTION
        UPDATE refresh_token SET used_at = now WHERE id = row.id AND used_at IS NULL
        if rows_updated = 0:             -> concurrent use, treat as REUSE, roll back to detection
        INSERT new refresh_token (same family_id, new hash, expires_at = row.expires_at)
    COMMIT
    issue new access token
```

Three details that separate a working implementation from a plausible-looking one:

**The conditional update is the concurrency control.** `UPDATE ... WHERE id = ? AND used_at IS
NULL` returning zero rows means another transaction consumed the same token first. Reading then
writing without that guard lets two simultaneous refreshes both succeed, which splits the family
into two valid lineages and defeats detection entirely.

**The family's expiry does not extend on rotation.** `expires_at` is copied from the parent, not
recalculated. Otherwise an attacker who keeps refreshing holds the lineage open indefinitely and
"thirty-day refresh token" becomes "permanent access". This is called *absolute* versus *sliding*
expiry, and you want an absolute cap even if you also apply a sliding idle timeout.

**Revoking the family must revoke the descendants too**, which is why `family_id` is a flat
identifier rather than a parent pointer. A single indexed `UPDATE ... WHERE family_id = ?`
terminates the lineage in one statement; walking a chain of parent references is slower and fails
if the chain is broken.

**The false positive you must design for** is a client that legitimately replays a token: a mobile
application killed mid-refresh that retries after the response was lost, or two browser tabs
refreshing at the same moment. Three mitigations, in order of preference: serialise refreshes in
the client behind a single-flight lock, which is the real fix; give the response a short *grace
window* during which the immediately-superseding token is also returned for the parent, at the cost
of some detection sharpness; and make the client's retry idempotent by having it re-read its stored
token after acquiring the lock. What you should not do is disable detection because it produced
support tickets — the tickets are the control working, and the correct response is to fix the
client.

### 5. Revocation Strategies, Compared Honestly

**In simple terms:** There is no way to cancel a token instantly without keeping some record on the
server, so the real choice is how much record-keeping you are willing to pay for.

Every option below reintroduces state. There is no stateless revocation; the question is what you
pay.

| Strategy | Revocation latency | Per-request cost | Operational cost | Honest verdict |
|---|---|---|---|---|
| **Short expiry only** | Up to the token lifetime | Zero | Zero | Genuinely stateless. Correct for most systems. The lifetime *is* your security parameter |
| **`jti` denylist in Redis**, TTL = remaining token life | Immediate | One Redis `GET` (~1 ms) | Redis becomes a dependency of every request | Good for targeted revocation. Only stores *revoked* tokens, so it stays small |
| **Token version / session claim** checked against a cache | Immediate | One cache lookup per request, cacheable per user | A counter per user | Best when you need "log this user out everywhere" |
| **Full introspection** (RFC 7662) on every request | Immediate | A network call per request | The authorization server is on every hot path | You have rebuilt sessions with extra latency. Reserve for very high value operations |

**Short expiry only.** The default, and a defensible one. If the access token lives five minutes,
your maximum exposure after revoking an account is five minutes, and you have no new
infrastructure. Most systems that add a denylist would have been better served by shortening the
lifetime and keeping the architecture simple.

**The `jti` denylist.** Store only revoked identifiers, with a TTL equal to the token's remaining
life, so entries evict themselves exactly when they stop mattering and the set stays proportional
to your revocation rate rather than your user count.

```java
// On revocation
long ttlSeconds = jwt.getExpiresAt().getEpochSecond() - Instant.now().getEpochSecond();
if (ttlSeconds > 0) {
    redis.opsForValue().set("revoked:" + jwt.getId(), "1", Duration.ofSeconds(ttlSeconds));
}
```

The cost that gets underestimated: Redis is now on the critical path of every authenticated
request, so you must decide the failure mode in advance. Fail closed (Redis down means everyone is
rejected) trades availability for security; fail open (Redis down means nothing is denylisted)
trades security for availability and is usually right for a five-minute token, because the exposure
is bounded by the same window you already accepted. Decide it deliberately, write it down, and test
it — do not discover it during an outage.

**Token version.** Put a counter in the token, keep the current value per user, and compare.

```json
{ "sub": "u-8842", "tv": 7 }
```

Incrementing `tv` invalidates every token that user holds, everywhere, instantly. This is the
mechanism for "log out all devices", "password changed", and "roles changed". It is cheaper than a
denylist at scale because the lookup is per *user* and therefore cacheable with a short TTL, and
the stored state is one small integer per user rather than an entry per revoked token. What it
cannot do is revoke one token while leaving the user's other sessions alone — for that you need a
per-session identifier, which is the same mechanism keyed on `sid` instead.

**Introspection.** The resource server calls `/oauth2/introspect` and the authorization server
answers `{"active": true}` or not. Perfectly current and it throws away every benefit of a
self-contained token: latency on every request, a hard availability dependency, and the
authorization server as a bottleneck for the whole estate. If you have arrived here, opaque tokens
with a shared session store are the simpler design — say so rather than building an expensive
imitation of it.

### 6. Logout in a Stateless System

**In simple terms:** You cannot take back an access token that has already been issued, so logging out
really means cancelling the refresh token and accepting a few more minutes of access.

"How do you log out of a JWT?" has a precise and uncomfortable answer: **you cannot invalidate
the access token, so logout is a combination of client-side disposal and server-side state.**

What a correct logout does:

1. **Revoke the refresh token** server-side, and the whole family. This is the part that actually
   matters: it means the session cannot be extended, so access ends at the current access token's
   `exp`, minutes away. RFC 7009 defines `/oauth2/revoke` for exactly this.
2. **Delete the tokens on the client.** Clear memory, clear storage, clear the cookie with an
   expired `Set-Cookie`.
3. **Optionally denylist the outstanding access token by `jti`**, if you have a denylist. This is
   what closes the remaining few minutes.
4. **Increment the token version** if the intent is "log out everywhere" rather than "log out this
   device".

The honest sentence for an interview: *there is no such thing as stateless logout; every "JWT
logout" implementation reintroduces server-side state, and the design question is how much and
where.* Someone who says "you just delete the token on the client" has not thought about the
attacker, who is not going to delete anything.

There is a second consideration in a browser: **`/logout` must be a `POST`**. A `GET` logout can be
triggered by an `<img src="https://app.example.com/logout">` on any site the user visits, which is
a cross-site request forgery that logs them out. Spring Security's `LogoutFilter` requires `POST`
by default when CSRF protection is enabled, and people frequently relax it for convenience.

### 7. Client-Side Storage

**In simple terms:** Where a browser keeps its tokens decides how bad a single JavaScript flaw becomes,
and the most popular choice is the one that lets an attacker walk away with the credential.

Where the browser keeps the token is the decision with the largest real-world impact, and every
option is a trade between cross-site scripting and cross-site request forgery.

| Storage | XSS-readable | Sent automatically | CSRF risk | Survives reload | Verdict |
|---|---|---|---|---|---|
| `localStorage` | **Yes** | No | None | Yes | **Avoid.** Any script on the page reads it |
| `sessionStorage` | **Yes** | No | None | Per tab only | Marginally better; same class of exposure |
| **In-memory** (a JavaScript variable) | Only while running | No | None | **No** | Good for the *access* token |
| **`HttpOnly` cookie** | **No** | Yes | **Yes** — needs `SameSite` and/or a CSRF token | Yes | Good for the *refresh* token |
| **BFF / token-handler** | **No** — the browser never sees a token | Session cookie only | Handled by the BFF | Yes | **Best.** The recommendation |

**Why `localStorage` is the wrong default despite being the most common.** A single cross-site
scripting flaw — in your code, in a dependency, in an analytics tag, in an advertisement — reads
the token and exfiltrates it in one line. The token then works from the attacker's machine for its
full lifetime, and if a refresh token is stored alongside it, indefinitely. The usual counter-argument
is "if you have XSS you are compromised anyway", and it is wrong in an important way: with an
`HttpOnly` cookie the attacker must *act through the user's browser*, which is noisy, rate-limited
by the user's session, and observable. With an exfiltrated token they act from anywhere, silently,
for as long as it lives.

**The practical browser recommendation**, when a BFF is not available: access token **in memory
only**, refresh token in an `HttpOnly`, `Secure`, `SameSite=Strict` (or `Lax`) cookie scoped to the
refresh endpoint's path. Cross-site scripting can then *use* the refresh endpoint but cannot *steal*
the credential, and a page reload triggers one silent refresh to repopulate memory.

**The BFF is the answer when you can afford it.** A server-side component holds the tokens, the
browser gets an ordinary `HttpOnly` session cookie, and the BFF attaches bearer tokens to upstream
calls. This eliminates the entire question rather than choosing the least-bad answer to it, and it
is why the OAuth2 browser-based-apps guidance moved towards it. The costs are real: a stateful
component in front of a stateless architecture, an extra network hop, and session affinity or a
shared session store. It is worth it for anything handling money or regulated data.

### 8. Sender-Constrained Tokens — The Answer to Theft

**In simple terms:** Rather than limiting the damage from a stolen token, these schemes tie the token to
a private key the client never gives away, so a stolen copy is useless to anybody else.

Everything above limits the *damage* from a stolen bearer token. Sender-constrained tokens attack
the premise: make a stolen token useless to anyone but the legitimate client.

**mTLS-bound tokens (RFC 8705).** The client authenticates the TLS connection with a certificate;
the authorization server embeds the certificate's SHA-256 thumbprint in the token's `cnf`
(confirmation) claim; the resource server checks that the presenting connection's client
certificate matches. Stealing the token is useless without the private key, which never leaves the
client. Excellent for service-to-service and for regulated environments where certificates already
exist. Impractical for browsers.

```json
{ "sub": "svc-orders", "cnf": { "x5t#S256": "bwcK0esc3ACC3DB2Y5_lESsXE8o9ltc05O89jdN-dg2" } }
```

**DPoP — Demonstrating Proof of Possession (RFC 9449).** The client generates a key pair, and each
request carries a `DPoP` header containing a short-lived JWT signed with the private key and
covering the HTTP method, the URI, a timestamp, a unique identifier, and a hash of the access
token. The access token's `cnf` claim carries the thumbprint of the public key. A stolen access
token cannot be used without the private key, and a stolen DPoP proof is bound to one method, one
URI, and one moment.

DPoP is designed for exactly the case mTLS cannot serve — public clients and browsers, where the
key can live in non-extractable `CryptoKey` form in IndexedDB, so even cross-site scripting can
*sign with* it but cannot *exfiltrate* it. Support is emerging: Spring Authorization Server 1.4 and
later Spring Security 6.x versions implement it, and adoption requires both the authorization
server and every resource server to participate.

The realistic position for most teams today: sender-constrained tokens are the right long-term
answer and are not yet the default. Know what they solve, use mTLS binding where certificates
already exist, and watch DPoP.

### 9. Clock Skew, Multi-Device Sessions, and Token Contents

**In simple terms:** Three practical matters: the time allowance quietly lengthens short tokens, each
device needs its own cancellable lineage, and only decision-making data belongs inside a token.

**Clock skew.** Restated from file 23 because it interacts with short lifetimes: 60 seconds of
default leeway against a 5-minute access token means up to a 6-minute effective life, and against
a 60-second token it means the short lifetime is fiction. Run NTP, set skew to 30 seconds or less,
and treat a consistent offset as a broken clock rather than something to compensate for.

Refresh tokens have a related problem in reverse: if the authorization server's clock runs ahead,
tokens expire earlier than clients expect and users are logged out for no visible reason.

**Multi-device sessions.** One user, several devices, and each must be independently revocable —
logging out of a phone must not log out the laptop. The model falls out of the schema in section 3:
**one token family per device per login**, with `device_label`, `created_ip`, and `user_agent`
recorded so the user can see a meaningful sessions screen.

| Action | Mechanism |
|---|---|
| Log out this device | Revoke that one family |
| Log out all devices | Revoke every family for the user, **and** increment the token version |
| Password changed | Revoke every family except the current one; increment the token version |
| Reuse detected | Revoke that one family, alert, and consider stepping up authentication |
| Administrative lock | Increment the token version; revoke all families |

Note why "log out all devices" needs both halves: revoking the families stops *future* refreshes,
and incrementing the token version invalidates the access tokens already in circulation. Doing only
the first leaves every device working until its current access token expires, which is not what a
user clicking "sign out everywhere" after a suspected compromise expects.

**Token contents, restated as a rule.** Put in the access token only what a resource server needs
to make an authorization decision without a round trip: `iss`, `sub`, `aud`, `exp`, `nbf`, `iat`,
`jti`, coarse scopes or roles, a tenant identifier, and a token version or session identifier. Keep
out email addresses and names, national identifiers, phone numbers, fine-grained permission lists,
group memberships from a directory, internal hostnames and infrastructure details, and anything
whose exposure in a log would be reportable. The refresh token carries **nothing** — it is an
opaque random string.

### 10. Monitoring — Reuse Detection Is Worthless Without Alerting

**In simple terms:** Detecting a stolen refresh token only helps if somebody is told, so these are the
signals worth watching and what each one usually turns out to mean.

Reuse detection produces the single highest-signal security event most systems will ever generate.
A confirmed reuse means a refresh token existed in two places, which means either a real compromise
or a client bug. Both need someone to look.

| Signal | Why it matters | Suggested response |
|---|---|---|
| **Refresh reuse detected** | Token in two places — compromise or client bug | Page during business hours; investigate every occurrence at low volume |
| Reuse rate stepping up after a release | Almost certainly a client retry bug | Correlate with deployments before assuming an attack |
| Refresh from a new country or ASN within minutes of the previous one | Impossible travel | Step-up authentication or revoke |
| Refresh rate per user far above the token lifetime implies | A client refresh loop, or credential stuffing | Rate-limit the token endpoint per client and per user |
| Spike in `invalid_grant` | Rotation misconfigured, or families being burned | Check the reuse metric alongside it |
| Spike in 401 `invalid_token` across all services | Key rotation or clock problem, not per-user | Check JWKS and NTP |
| Access tokens issued but never used | Token minting without a corresponding client | Investigate the client |

Two implementation notes. Log the reuse event with `user_id`, `family_id`, both source addresses,
both user agents, and both timestamps — during an investigation the *pair* is what tells you which
holder was the attacker. And rate-limit `/oauth2/token` per client and per user regardless, because
the endpoint performs a database write per call and is otherwise a free amplification point.

---

## Working Code

Spring Authorization Server, configured for rotation:

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-oauth2-authorization-server</artifactId>
</dependency>
```

```java
package com.example.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.*;
import org.springframework.security.oauth2.server.authorization.settings.*;

import java.time.Duration;
import java.util.UUID;

@Configuration
public class AuthorizationServerConfig {

    @Bean
    RegisteredClientRepository registeredClientRepository() {
        RegisteredClient spa = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("web-spa")
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)   // public client
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("https://app.example.com/callback")
                .scope("orders:read").scope("orders:write")
                .clientSettings(ClientSettings.builder()
                        .requireProofKey(true)          // PKCE, mandatory for a public client
                        .build())
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofMinutes(15))
                        .refreshTokenTimeToLive(Duration.ofDays(14))
                        // DEFAULT IS TRUE. Without this line there is no rotation at all,
                        // and therefore nothing for reuse detection to detect.
                        .reuseRefreshTokens(false)
                        .build())
                .build();
        return new InMemoryRegisteredClientRepository(spa);
    }
}
```

The rotation-with-reuse-detection service. Spring Authorization Server rotates when
`reuseRefreshTokens(false)` is set and rejects an already-rotated token with `invalid_grant`, but
family-wide revocation on reuse is not provided — this is the part you add:

```java
package com.example.auth.refresh;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service
public class RefreshTokenService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int TOKEN_BYTES = 32;                 // 256 bits of entropy

    private final RefreshTokenRepository repository;
    private final ApplicationEventPublisher events;

    public RefreshTokenService(RefreshTokenRepository repository, ApplicationEventPublisher events) {
        this.repository = repository;
        this.events = events;
    }

    public record IssuedToken(String rawToken, UUID familyId, Instant expiresAt) {}

    /** First issue after a successful authentication: a brand-new family. */
    @Transactional
    public IssuedToken issueNewFamily(String userId, String clientId, DeviceInfo device,
                                      Duration absoluteLifetime) {
        return persist(UUID.randomUUID(), userId, clientId, device,
                Instant.now().plus(absoluteLifetime));
    }

    /**
     * Rotation with reuse detection.
     *
     * @throws RefreshTokenReuseException when an already-consumed token is presented, which
     *         means two holders exist. The whole family is revoked before this throws.
     */
    @Transactional
    public IssuedToken rotate(String presentedToken, DeviceInfo device) {

        String hash = sha256Hex(presentedToken);
        RefreshToken current = this.repository.findByTokenHash(hash)
                .orElseThrow(() -> new InvalidRefreshTokenException("unknown token"));

        if (current.getRevokedAt() != null) {
            // The family was already burned — by a logout, an administrator, or a previous reuse.
            throw new InvalidRefreshTokenException("revoked");
        }
        if (current.getUsedAt() != null) {
            // *** REUSE. Two parties hold this lineage and we cannot tell which is legitimate. ***
            revokeFamily(current.getFamilyId(), RevocationReason.REUSE_DETECTED);
            this.events.publishEvent(new RefreshTokenReuseEvent(
                    current.getUserId(), current.getFamilyId(),
                    current.getCreatedIp(), device.ip(), Instant.now()));
            throw new RefreshTokenReuseException(current.getFamilyId());
        }
        if (current.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidRefreshTokenException("expired");
        }

        // Conditional update IS the concurrency control. Zero rows updated means another
        // transaction consumed this token first, which is indistinguishable from reuse.
        int consumed = this.repository.markUsedIfUnused(current.getId(), Instant.now());
        if (consumed == 0) {
            revokeFamily(current.getFamilyId(), RevocationReason.REUSE_DETECTED);
            throw new RefreshTokenReuseException(current.getFamilyId());
        }

        // Absolute expiry is INHERITED, never extended. Otherwise a lineage never ends.
        return persist(current.getFamilyId(), current.getUserId(), current.getClientId(),
                device, current.getExpiresAt());
    }

    @Transactional
    public void revokeFamily(UUID familyId, RevocationReason reason) {
        this.repository.revokeFamily(familyId, Instant.now(), reason);
    }

    @Transactional
    public void revokeAllForUser(String userId, RevocationReason reason) {
        this.repository.revokeAllForUser(userId, Instant.now(), reason);
    }

    private IssuedToken persist(UUID familyId, String userId, String clientId,
                                DeviceInfo device, Instant expiresAt) {
        byte[] raw = new byte[TOKEN_BYTES];
        RANDOM.nextBytes(raw);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);

        RefreshToken entity = new RefreshToken();
        entity.setId(UUID.randomUUID());
        entity.setTokenHash(sha256Hex(token));      // the raw value is NEVER stored
        entity.setFamilyId(familyId);
        entity.setUserId(userId);
        entity.setClientId(clientId);
        entity.setDeviceLabel(device.label());
        entity.setCreatedIp(device.ip());
        entity.setUserAgent(device.userAgent());
        entity.setIssuedAt(Instant.now());
        entity.setExpiresAt(expiresAt);
        try {
            this.repository.save(entity);
        }
        catch (DataIntegrityViolationException collision) {
            // 256 bits of entropy; this is effectively impossible and must never be swallowed.
            throw new IllegalStateException("Refresh token hash collision", collision);
        }
        return new IssuedToken(token, familyId, expiresAt);
    }

    /**
     * SHA-256, deliberately NOT bcrypt. bcrypt's work factor defends a low-entropy,
     * human-chosen secret against offline brute force. This value is 256 random bits, so
     * there is nothing to brute-force and the work factor would only add latency to a
     * hot path. See 03_M1_T3_Cryptography.md for why the rule differs for passwords.
     */
    private static String sha256Hex(String value) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(value.getBytes(UTF_8)));
        }
        catch (Exception ex) {
            throw new IllegalStateException("SHA-256 unavailable", ex);
        }
    }
}
```

```java
package com.example.auth.refresh;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /** Returns 0 if another transaction consumed it first — that is the reuse signal. */
    @Modifying
    @Query("UPDATE refresh_token SET used_at = :now WHERE id = :id AND used_at IS NULL")
    int markUsedIfUnused(UUID id, Instant now);

    @Modifying
    @Query("""
           UPDATE refresh_token SET revoked_at = :now, revoked_reason = :reason
           WHERE family_id = :familyId AND revoked_at IS NULL
           """)
    int revokeFamily(UUID familyId, Instant now, RevocationReason reason);

    @Modifying
    @Query("""
           UPDATE refresh_token SET revoked_at = :now, revoked_reason = :reason
           WHERE user_id = :userId AND revoked_at IS NULL
           """)
    int revokeAllForUser(String userId, Instant now, RevocationReason reason);
}
```

The `jti` denylist, with an explicit failure mode, and the resource-server validator that consults
it:

```java
package com.example.api.security;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Duration;
import java.time.Instant;

public class DenylistTokenValidator implements OAuth2TokenValidator<Jwt> {

    private static final OAuth2Error REVOKED =
            new OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN, "Token has been revoked", null);

    private final StringRedisTemplate redis;
    private final boolean failClosed;

    public DenylistTokenValidator(StringRedisTemplate redis, boolean failClosed) {
        this.redis = redis;
        this.failClosed = failClosed;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        String jti = jwt.getId();
        if (jti == null) {
            // A token with no jti cannot be denylisted at all. Require jti at issue time.
            return OAuth2TokenValidatorResult.success();
        }
        try {
            boolean revoked = Boolean.TRUE.equals(this.redis.hasKey("revoked:" + jti));
            return revoked ? OAuth2TokenValidatorResult.failure(REVOKED)
                           : OAuth2TokenValidatorResult.success();
        }
        catch (RuntimeException redisDown) {
            // DECIDE THIS DELIBERATELY AND TEST IT.
            // fail closed  -> Redis outage is a total authentication outage
            // fail open    -> exposure bounded by the access token lifetime, which you
            //                 already accepted when you chose that lifetime
            if (this.failClosed) {
                return OAuth2TokenValidatorResult.failure(REVOKED);
            }
            return OAuth2TokenValidatorResult.success();
        }
    }

    /** TTL equals the token's REMAINING life, so entries evict when they stop mattering. */
    public void revoke(String jti, Instant expiresAt) {
        long seconds = expiresAt.getEpochSecond() - Instant.now().getEpochSecond();
        if (seconds > 0) {
            this.redis.opsForValue().set("revoked:" + jti, "1", Duration.ofSeconds(seconds));
        }
    }
}
```

Wiring it into the resource server, composed with the defaults and the audience validator from
[`24_M7_T2_JWT_Spring_Integration.md`](24_M7_T2_JWT_Spring_Integration.md):

```java
@Bean
JwtDecoder jwtDecoder(StringRedisTemplate redis) {
    NimbusJwtDecoder decoder = (NimbusJwtDecoder) JwtDecoders.fromIssuerLocation(issuerUri);
    decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
            JwtValidators.createDefaultWithIssuer(issuerUri),          // exp, nbf, iss
            new JwtClaimValidator<List<String>>(JwtClaimNames.AUD,     // aud — never automatic
                    aud -> aud != null && aud.contains(audience)),
            new DenylistTokenValidator(redis, false)));                // fail open, deliberately
    return decoder;
}
```

Tests for the behaviour that matters:

```java
package com.example.auth.refresh;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class RefreshTokenRotationTests {

    @Autowired RefreshTokenService service;
    @Autowired RefreshTokenRepository repository;

    private static final DeviceInfo DEVICE = new DeviceInfo("Chrome on macOS", "203.0.113.7", "UA");

    @Test
    void rotationIssuesANewTokenInTheSameFamilyAndConsumesTheOld() {
        var first  = service.issueNewFamily("u-8842", "web-spa", DEVICE, Duration.ofDays(14));
        var second = service.rotate(first.rawToken(), DEVICE);

        assertThat(second.rawToken()).isNotEqualTo(first.rawToken());
        assertThat(second.familyId()).isEqualTo(first.familyId());
        assertThat(second.expiresAt()).isEqualTo(first.expiresAt());   // absolute, NOT extended
    }

    @Test
    void presentingAConsumedTokenRevokesTheEntireFamily() {
        var rt1 = service.issueNewFamily("u-8842", "web-spa", DEVICE, Duration.ofDays(14));
        var rt2 = service.rotate(rt1.rawToken(), DEVICE);
        var rt3 = service.rotate(rt2.rawToken(), DEVICE);

        // The attacker's lineage is rt3; the victim still holds rt2 and replays it.
        assertThatThrownBy(() -> service.rotate(rt2.rawToken(), DEVICE))
                .isInstanceOf(RefreshTokenReuseException.class);

        // Every descendant, including the attacker's newest token, is now dead.
        assertThatThrownBy(() -> service.rotate(rt3.rawToken(), DEVICE))
                .isInstanceOf(InvalidRefreshTokenException.class);

        assertThat(repository.findByFamilyId(rt1.familyId()))
                .allMatch(t -> t.getRevokedAt() != null
                            && t.getRevokedReason() == RevocationReason.REUSE_DETECTED);
    }

    @Test
    void theRawTokenIsNeverPersisted() {
        var issued = service.issueNewFamily("u-8842", "web-spa", DEVICE, Duration.ofDays(14));
        assertThat(repository.findAll())
                .noneMatch(t -> t.getTokenHash().equals(issued.rawToken()));
    }

    @Test
    void concurrentRotationOfTheSameTokenLeavesOnlyOneWinnerAndBurnsTheFamily() throws Exception {
        var rt1 = service.issueNewFamily("u-8842", "web-spa", DEVICE, Duration.ofDays(14));

        var pool = Executors.newFixedThreadPool(2);
        var start = new CountDownLatch(1);
        List<Future<?>> results = List.of(
                pool.submit(() -> { start.await(); return service.rotate(rt1.rawToken(), DEVICE); }),
                pool.submit(() -> { start.await(); return service.rotate(rt1.rawToken(), DEVICE); }));
        start.countDown();

        long failures = results.stream().filter(RefreshTokenRotationTests::threw).count();
        assertThat(failures).isEqualTo(1);       // exactly one succeeds; the other is reuse
    }

    @Test
    void revokingAllForAUserKillsEveryDeviceFamily() {
        var phone  = service.issueNewFamily("u-8842", "web-spa", DEVICE, Duration.ofDays(14));
        var laptop = service.issueNewFamily("u-8842", "web-spa", DEVICE, Duration.ofDays(14));

        service.revokeAllForUser("u-8842", RevocationReason.LOGOUT_ALL);

        assertThatThrownBy(() -> service.rotate(phone.rawToken(), DEVICE))
                .isInstanceOf(InvalidRefreshTokenException.class);
        assertThatThrownBy(() -> service.rotate(laptop.rawToken(), DEVICE))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }
}
```

---

## Internals

### `TokenSettings` — the default that disables rotation

```java
// org.springframework.security.oauth2.server.authorization.settings.TokenSettings
public static Builder builder() {
    return new Builder()
            .authorizationCodeTimeToLive(Duration.ofMinutes(5))
            .accessTokenTimeToLive(Duration.ofMinutes(5))
            .accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED)
            .deviceCodeTimeToLive(Duration.ofMinutes(5))
            .reuseRefreshTokens(true)                      // <-- ROTATION IS OFF BY DEFAULT
            .refreshTokenTimeToLive(Duration.ofMinutes(60))
            .idTokenSignatureAlgorithm(SignatureAlgorithm.RS256);
}
```

`reuseRefreshTokens(true)` means the same refresh token is returned every time, so it behaves like
a long-lived credential and there is nothing for reuse detection to detect. This is the single line
most implementations miss, and because everything works perfectly with it enabled, nothing ever
surfaces the omission.

Note also `accessTokenFormat`: `SELF_CONTAINED` produces a JWT, and `REFERENCE` produces an opaque
token that resource servers must introspect. Switching to `REFERENCE` is how you get immediate
revocation at the cost of a round trip per request, which is the fourth row of the table in section
5, available as a configuration change.

### `OAuth2RefreshTokenAuthenticationProvider` — where rotation happens

```java
// simplified
OAuth2Authorization.Token<OAuth2RefreshToken> refreshToken = authorization.getRefreshToken();
if (!refreshToken.isActive()) {
    // isActive() checks invalidated, notBefore, and expiresAt
    throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_GRANT);
}
// ... generate the new access token ...

OAuth2RefreshToken currentRefreshToken = refreshToken.getToken();
if (!registeredClient.getTokenSettings().isReuseRefreshTokens()) {
    tokenContext = tokenContextBuilder.tokenType(OAuth2TokenType.REFRESH_TOKEN).build();
    OAuth2Token generated = this.tokenGenerator.generate(tokenContext);
    currentRefreshToken = (OAuth2RefreshToken) generated;      // a NEW refresh token
    authorizationBuilder.refreshToken(currentRefreshToken);
}
this.authorizationService.save(authorizationBuilder.build());
```

Two things to read out of this. The rotation branch is gated entirely on `isReuseRefreshTokens()`,
confirming that rotation is opt-in. And the previous refresh token is replaced in the stored
`OAuth2Authorization` rather than retained with a used marker — so presenting the old value simply
fails to find an authorization and returns `invalid_grant`. That is a correct rejection but it is
**not** reuse detection: nothing concludes that the lineage is compromised, nothing revokes the
descendants, and nothing raises an event. Family revocation is the layer you add on top, which is
what `RefreshTokenService` above does.

### `OAuth2AuthorizationService` — where the state lives

```java
public interface OAuth2AuthorizationService {
    void save(OAuth2Authorization authorization);
    void remove(OAuth2Authorization authorization);
    OAuth2Authorization findById(String id);
    OAuth2Authorization findByToken(String token, @Nullable OAuth2TokenType tokenType);
}
```

`findByToken` is the proof that refresh tokens are server-side state: every refresh is a lookup.
`InMemoryOAuth2AuthorizationService` is for development only — restart the authorization server and
every user is logged out, and it cannot work across more than one instance.
`JdbcOAuth2AuthorizationService` is the production implementation, and it needs a cleanup job,
because expired authorizations are not removed automatically and the table grows without bound.

### `LogoutFilter` and RFC 7009 revocation

Spring Security's `LogoutFilter` clears the `SecurityContext` and invalidates the HTTP session,
neither of which exists in a stateless bearer-token API. For token-based logout the relevant
endpoint is the authorization server's `/oauth2/revoke`, implemented by
`OAuth2TokenRevocationEndpointFilter`, which accepts a `token` parameter and an optional
`token_type_hint` and marks the token invalidated in the `OAuth2AuthorizationService`.

Revoking a refresh token there ends the ability to extend the session. It does **not** invalidate
access tokens already issued — those remain valid until `exp`, which is precisely why the honest
answer to "how do you log out of a JWT" includes a denylist or a token version if the residual few
minutes are unacceptable.

---

## Configuration Reference

| Option | Effect | Default |
|---|---|---|
| `TokenSettings.accessTokenTimeToLive` | Access token lifetime — **this is your revocation window** | 5 min |
| `TokenSettings.refreshTokenTimeToLive` | Refresh token absolute lifetime | 60 min |
| `TokenSettings.reuseRefreshTokens(false)` | **Enable rotation** | `true` (rotation off) |
| `TokenSettings.accessTokenFormat` | `SELF_CONTAINED` (JWT) or `REFERENCE` (opaque) | `SELF_CONTAINED` |
| `TokenSettings.authorizationCodeTimeToLive` | Code exchange window | 5 min |
| `ClientSettings.requireProofKey(true)` | Require PKCE | `false` |
| `ClientSettings.requireAuthorizationConsent` | Show a consent screen | `false` |
| `JwtTimestampValidator(Duration)` | Clock skew on `exp` / `nbf` | 60 s |
| `JdbcOAuth2AuthorizationService` | Persist authorizations across restarts and instances | in-memory |
| `/oauth2/revoke` | RFC 7009 revocation endpoint | enabled |
| `/oauth2/introspect` | RFC 7662 introspection endpoint | enabled |
| Cookie `HttpOnly` | Blocks JavaScript access | set it |
| Cookie `Secure` | HTTPS only | set it |
| Cookie `SameSite` | `Strict` or `Lax` for CSRF defence | `Lax` in modern browsers |
| Cookie `Path` | Scope the refresh cookie to the token endpoint | `/` |
| Denylist entry TTL | Equal to the token's **remaining** lifetime | — |

---

## Production Concerns & Anti-Patterns

**Leaving `reuseRefreshTokens` at its default.** Rotation is off unless you turn it on, everything
works, and you have a long-lived static credential you believe is rotating. Assert it in a test:
refresh twice and check the values differ.

**Rotation without reuse detection.** Rotation alone bounds how long a stolen token is useful only
if the legitimate client refreshes first. Detection is what turns rotation into a compromise alarm,
and it costs one column and one indexed update.

**Storing refresh tokens in plaintext.** They are credentials. A database dump or a leaked backup
is then a full account takeover for every user, with no expiry in the attacker's way. Hash them.

**Extending absolute expiry on every rotation.** A "thirty-day" refresh token that resets its
expiry each time never expires as long as someone keeps using it — including an attacker. Inherit
`expires_at`; apply a sliding idle timeout separately if you want one.

**Read-then-write rotation without a conditional update.** Two concurrent refreshes both succeed,
the family splits into two valid lineages, and reuse detection can never fire again for that user.

**Detecting reuse and doing nothing with it.** The event is the most valuable security signal the
system produces. If it only increments a counter nobody watches, you have built the mechanism and
skipped the purpose.

**Turning off reuse detection because it generated support tickets.** The tickets are usually a
client that retries a refresh without a single-flight lock. Fix the client; do not remove the
alarm.

**`localStorage` for tokens.** One cross-site scripting flaw anywhere on the page — including in a
dependency or a marketing tag — exfiltrates a credential that then works from anywhere for its full
lifetime.

**A refresh token cookie without `HttpOnly`, `Secure`, and `SameSite`.** Missing `HttpOnly` gives
up the only real advantage over `localStorage`; missing `SameSite` opens cross-site request
forgery against the refresh endpoint.

**A `GET /logout` endpoint.** Any third-party site can trigger it with an image tag. Logout must be
a `POST`.

**Believing a client-side token delete is a logout.** An attacker who has the token does not run
your logout code. Revoke server-side.

**A denylist with no defined failure mode.** Decide fail-open versus fail-closed deliberately,
write down why, and test the Redis-down path. Discovering it during an incident is the worst
possible time.

**No cleanup job on the token table.** `JdbcOAuth2AuthorizationService` and a hand-rolled table
both accumulate expired rows forever. Delete them on a schedule, retaining revoked rows long enough
for forensics.

---

## Debugging Playbook

| Symptom | Likely root cause | Fix |
|---|---|---|
| Refresh returns the same token every time | `reuseRefreshTokens` left at `true` | `TokenSettings.builder().reuseRefreshTokens(false)` |
| Users randomly logged out, `invalid_grant` in bursts | Concurrent refresh from multiple tabs or a retrying client triggering reuse detection | Add a single-flight lock in the client; consider a short grace window |
| Reuse detection never fires even under test | Rotation is off, or read-then-write without the conditional update | Assert two refreshes return different values; check the `UPDATE ... AND used_at IS NULL` row count |
| Refresh tokens never expire | Absolute expiry recalculated on each rotation | Inherit `expires_at` from the parent |
| Revoked user still has access for minutes | Working as designed — the access token lifetime is the window | Shorten the lifetime, or add a denylist / token version |
| Logout does not end access | Only the client-side token was deleted | Revoke the refresh family server-side; denylist the `jti` if needed |
| Authorization server CPU or database saturated | Access token lifetime too short, so refresh traffic dominates | Lengthen the lifetime; rate-limit `/oauth2/token`; index the hash column |
| All users logged out after a deploy | `InMemoryOAuth2AuthorizationService` in production | Switch to `JdbcOAuth2AuthorizationService` |
| Token table growing without bound | No cleanup of expired authorizations | Scheduled delete; keep revoked rows for forensics |
| Cookie-based refresh works in development, not in production | `Secure` on plain HTTP, or `SameSite=Strict` blocking a cross-site redirect return | Serve over HTTPS; use `Lax` if the flow crosses sites |
| Refresh succeeds but resource servers still reject the new token | Clock skew, or the new token carries a stale token version | Compare `iat` with server time; check the `tv` claim against the cache |

---

## Interview Q&A

### Q1. Why have both an access token and a refresh token? Why not one long-lived token, or make the client re-authenticate?

<details>
<summary>Show answer</summary>

Because the two options at the extremes are both unacceptable, and the split exists precisely to
escape the trade-off between them.

**One long-lived token** means a credential that is presented to every resource server on every
request, passes through every proxy and load balancer, sits in client storage for weeks, appears in
logs when someone misconfigures a filter — and cannot be revoked, because a resource server
verifies it offline and never asks anyone whether it is still good. A single leak is a long-lived
account compromise.

**Re-authentication every few minutes** is secure and no product will ship it. It also has a
counter-intuitive security cost: prompting for a password constantly trains users to type it
reflexively, which is exactly the habit phishing exploits.

The split separates two jobs that were conflated. The **access token** is the high-exposure
credential, so it is made harmless by being short-lived: it goes everywhere, and it is worthless in
fifteen minutes. The **refresh token** is the long-lived credential, so it is made safe by being
low-exposure: it is presented to exactly one endpoint, it is stored server-side, and it can be
revoked instantly.

Two design consequences follow that are worth stating, because they show you understand *why*
rather than *what*. A refresh token should be opaque, not a JWT — its only verifier is the
authorization server, which is already doing a lookup, so self-containment gains nothing and loses
revocability. And the access token lifetime is not a tuning parameter for performance; **it is the
revocation window**, and choosing it is a security decision.

**Counter-question: so how do you actually choose the access token lifetime?**

By deciding what an acceptable revocation window is for what that token can do, then checking the
refresh load that implies.

My default is 15 minutes. If someone is fired or an account is compromised, 15 minutes of residual
access is usually tolerable. For a service that moves money or changes entitlements I would drop to
5 and accept the extra refresh traffic. For machine-to-machine clients that re-authenticate with
client credentials and have no human in the loop, 60 minutes is fine because there is no user to
revoke in a hurry.

Then I check the arithmetic, because "shorter is safer" has a limit. Ten thousand concurrent users
on a 5-minute token generate roughly 33 refresh requests per second, each involving a database
write under rotation. At one minute it is 166 per second, and the authorization server becomes a
single point of failure for the entire estate — and it will fail during a traffic spike, which is
the worst time for every user to be unable to refresh.

So below roughly five minutes I would stop buying security with refresh traffic and buy it with a
denylist or a token version claim instead. That gives immediate revocation without turning the
token endpoint into a bottleneck. The general principle: shortening the lifetime trades
availability for security, and past a point the trade stops being good.

**Counter-question: if the refresh token is the thing that can be stolen and used for weeks, have you actually improved anything?**

Yes, and specifically because the refresh token is far harder to steal and far easier to respond to.

Consider where each one goes. The access token is presented to every service, crosses every proxy,
appears in `Authorization` headers that any middlebox can log, and is held by client code that
attaches it to dozens of calls. The refresh token touches exactly one endpoint, one hop, on one
origin, and in a well-built browser client it lives in an `HttpOnly` cookie scoped to that path so
JavaScript cannot read it at all. The attack surface is smaller by an order of magnitude.

Then consider response. A stolen access token cannot be revoked and works until `exp` — but that is
minutes. A stolen refresh token can be revoked instantly, and with rotation and reuse detection the
system *detects* the theft on its own the moment both holders try to use the lineage, without
anyone reporting anything.

That last point is the real answer. The refresh token is not just more protected; it is the only
part of the design that can notice it has been stolen. A single long-lived access token has no
equivalent — theft is invisible until someone notices the consequences.
</details>

### Q2. Explain refresh token rotation with reuse detection. Walk through the attack it defeats.

<details>
<summary>Show answer</summary>

**Rotation** means a refresh token is single-use: presenting `RT1` returns a new access token *and*
a new refresh token `RT2`, and `RT1` is permanently consumed.

**Reuse detection** is the inference rotation makes possible. Under rotation, a correctly behaving
client never presents the same refresh token twice. So if an already-consumed token is presented,
two parties hold that lineage, and one of them is an attacker. You cannot tell which — the
legitimate client might be the one replaying — so the only safe response is to revoke **both**,
which means revoking the entire family and forcing re-authentication.

The **token family** is the implementation: every refresh token descended from one original
authentication shares a `family_id`, and reuse revokes the family in a single indexed update.

**The attack, step by step.**

The user authenticates and receives `AT1` and `RT1`. They refresh normally and hold `AT2`, `RT2`.
An attacker exfiltrates `RT2` — cross-site scripting, a stolen device backup, a malicious browser
extension, a compromised proxy.

*Without rotation:* the attacker holds a credential valid for the full refresh lifetime, weeks. They
mint access tokens at will. Nothing in the system ever notices, because from the server's point of
view a valid refresh token is being used normally.

*With rotation but no detection:* whoever refreshes first wins the lineage and the other is locked
out. If the attacker moves first, the legitimate user is mysteriously logged out and the attacker
continues indefinitely. If the user moves first, the attacker's token is dead. It is a coin flip,
and nobody is alerted either way.

*With rotation and detection:* the attacker refreshes `RT2`, consuming it and receiving `RT3`. The
legitimate client, still holding `RT2`, eventually refreshes. The server sees `RT2` with a non-null
`used_at`, concludes the lineage is compromised, revokes the entire family — including the
attacker's `RT3` — and raises a security event. The user is forced to re-authenticate, which they
can do because they know their credentials. The attacker cannot, and their access ends when the
current access token expires, minutes later.

The order does not matter. If the legitimate client refreshes first and the attacker replays, the
attacker triggers the detection and burns their own access. Either way the compromise is contained
and **someone is told**, which is what turns a silent breach into an incident with a timestamp, two
IP addresses, and two user agents.

**Counter-question: the user has three browser tabs open. All three refresh at once. What happens?**

Two of them trigger reuse detection, the family is revoked, and a legitimate user is logged out.
This is the single biggest practical objection and it must be designed for, not discovered.

The correct fix is in the client: a **single-flight lock**, so that concurrent requests needing a
fresh token await one shared refresh rather than each starting their own. Across tabs that means a
shared lock — the Web Locks API, a `BroadcastChannel` election, or a `localStorage` mutex — because
each tab is a separate JavaScript context. Most OAuth2 client libraries implement this; hand-rolled
interceptors usually do not, which is why this shows up after someone writes their own.

The server-side mitigation, for cases where the client cannot be fixed, is a **grace window**: for
a short period after rotation — say ten seconds — presenting the immediately-superseded token
returns the *same* successor rather than triggering detection. It handles the "response was lost,
client retried" case honestly and it does blunt detection slightly, since an attacker acting within
that window is indistinguishable from a retry. I would keep the window very short and treat it as a
compromise, not a default.

What I would not do is disable detection. The tickets are the control working. The right response
is to find out which client is replaying and fix it.

**Counter-question: your authorization server runs on six instances behind a load balancer. Two refreshes for the same token land on different instances simultaneously. Does detection still work?**

Only if the consume step is atomic in the shared store, which is why the implementation must be a
conditional update rather than a read followed by a write.

```sql
UPDATE refresh_token SET used_at = now() WHERE id = ? AND used_at IS NULL
```

The database guarantees exactly one of the two transactions updates a row. The winner rotates
normally. The loser gets zero rows affected, which is indistinguishable from reuse and must be
treated as such.

A read-then-write implementation breaks here in the worst possible way: both instances read
`used_at IS NULL`, both proceed, both issue successors, and the family now has two valid lineages.
Detection can never fire for that user again, because neither lineage overlaps. The bug is invisible
in single-instance testing and only appears under concurrency in production.

I would prove this with the concurrency test in the Working Code section — two threads racing on the
same token, asserting exactly one succeeds. That test is cheap and it is the only thing that
actually demonstrates the guarantee.

If refresh state lived in Redis rather than a relational database, the equivalent primitive is a
single atomic operation — `GETDEL`, or a Lua script — not a `GET` followed by a `SET`. The principle
is identical: consumption must be one atomic step.
</details>

### Q3. Compare revocation strategies for JWTs. Which would you actually implement?

<details>
<summary>Show answer</summary>

The honest framing first: **there is no stateless revocation.** A signature cannot be un-signed, so
every option below reintroduces server-side state. The question is how much, where, and what it
costs on the hot path.

**1. Short expiry only.** No revocation mechanism; the access token lifetime *is* the window. Zero
per-request cost, zero new infrastructure, and completely stateless. With a five-minute token your
maximum exposure after disabling an account is five minutes.

**2. A `jti` denylist in Redis**, with a TTL equal to the token's remaining lifetime. Revocation is
immediate and targeted at individual tokens. The set stays small because it contains only revoked
tokens and entries evict themselves exactly when the token would have expired anyway. The cost is a
Redis lookup on every authenticated request and a new availability dependency on the hot path.

**3. A token version or session identifier claim**, checked against a cache. The token carries
`"tv": 7`; the server keeps the current version per user; a mismatch rejects. Incrementing the
counter invalidates every token that user holds, everywhere, instantly. Cheaper than a denylist at
scale, because the lookup is per user and therefore highly cacheable, and the stored state is one
integer per user. It cannot revoke a single token without revoking the user's other sessions unless
you key it on a session identifier instead.

**4. Full introspection** on every request. Perfectly current, and it discards every benefit of a
self-contained token: a network call per request, the authorization server on every hot path, and a
new estate-wide bottleneck.

**What I would implement**, and this is a layered answer rather than a single choice:

Start with **short expiry** — fifteen minutes — as the baseline, because it covers the ordinary
case with no infrastructure at all. Add a **token version claim** for user-level events: password
changed, account disabled, roles changed, "sign out everywhere". That covers the operations people
actually need, it is cheap, and the cache makes it nearly free per request. Add a **`jti` denylist**
only if there is a concrete requirement to kill one specific token — a device reported stolen, a
token leaked in a support ticket — and go in knowing Redis is now on the critical path.

I would reach for **introspection** only for a small set of very high value operations, and I would
say plainly that a system needing it everywhere should be using opaque tokens with a shared session
store instead. Building an expensive imitation of sessions is worse than using sessions.

**Counter-question: your denylist is in Redis and Redis goes down. Fail open or fail closed?**

Fail open for a short-lived access token, and I would want that decision documented and tested
rather than discovered.

The reasoning is about the *marginal* risk. Failing open means revoked tokens are accepted for as
long as Redis is down, but only until they expire — and I already accepted that window when I chose
a fifteen-minute lifetime. The denylist was buying me minutes, so losing it costs me minutes.
Failing closed means every authenticated request across the estate is rejected, which is a total
outage caused by a cache being unavailable. Trading a complete outage for a few minutes of residual
exposure on a small number of revoked tokens is a bad trade.

The answer flips if the denylist is load-bearing rather than supplementary. If tokens live twenty-four
hours and the denylist is the only revocation mechanism, failing open means a revoked token works
for a day, and then failing closed is right — but I would treat that as a symptom that the lifetime
is wrong.

Either way, two things are non-negotiable. The behaviour must be explicit in the code with a comment
explaining the reasoning, not an accident of where the `try` block ended. And there must be a test
that runs the path with the cache unavailable, because "what happens when Redis is down" is
otherwise answered for the first time during an incident.

I would also alert on the denylist being unreachable, because failing open silently means the
control is off and nobody knows.

**Counter-question: the compliance team requires that a revoked token stops working within one second, across forty services. Now what?**

I would first test the requirement, because "within one second" is often a number chosen without a
cost attached, and the cost here is substantial.

Genuine sub-second global revocation means one of two things. Either every resource server checks
shared state on every request — introspection or a denylist with sub-second propagation — which
puts a network call on every hot path and makes that store a single point of failure for the whole
estate. Or tokens live less than a second, which is absurd.

So the honest options are: opaque tokens with a shared session store, which is the clean design if
that is truly the requirement and stops pretending to be stateless; or a denylist with an aggressive
push invalidation — Redis pub/sub or a streaming update to a local in-memory set in each service —
which gets propagation into the tens of milliseconds while keeping the per-request check local. The
second is what I would propose, because the per-request cost stays a local hash lookup and only the
*invalidation* crosses the network.

I would then ask what the requirement is actually protecting against, because the answer usually
narrows the scope enormously. If it is "a terminated employee must lose access immediately", that is
a user-level event, so a token version with push invalidation covers it and I do not need per-token
granularity. If it is "a leaked token must be killable", that is rare and manual, and a denylist
with a few seconds of propagation is fine. A blanket one-second rule applied to every token on every
request is usually an over-generalisation of one real scenario, and finding that scenario turns an
expensive architectural change into a targeted one.
</details>

### Q4. "JWTs are stateless, so we don't need a session store." Respond.

<details>
<summary>Show answer</summary>

I would agree with the literal claim and challenge almost every conclusion drawn from it.

It is true that a JWT is self-contained and a resource server can verify it without calling anyone.
That is real and valuable, and for service-to-service traffic and multi-backend mobile clients it is
the right architecture.

What is not true is that the *system* becomes stateless. Count the state a production JWT deployment
actually holds: refresh tokens, stored server-side and hashed, because they must be revocable.
Token families and their used and revoked markers, if you do rotation properly. A denylist or a
token version cache, if you need revocation faster than expiry. A record of active sessions per
device, because users expect to see and manage them. And the signing keys and their rotation
history.

That is a session store. It has a different shape — it is consulted on refresh rather than on every
request — but the statefulness did not disappear, it moved. The honest description is not "stateless
authentication" but "authentication state consulted on a minority of requests instead of all of
them", which is a genuine performance property and a much smaller claim.

I would also push on *why* statelessness is wanted, because the usual reasons do not survive
scrutiny. "We can scale horizontally" — so can sessions, with Redis or a database, which most
systems already run. "No sticky sessions" — true, and solved the same way. "It is modern" — not an
argument.

Where I would firmly agree: if the same identity must be verified by many independent services,
possibly across organisational boundaries, JWTs are clearly right, because the alternative is every
service calling a central authority on every request.

Where I would push back hardest: a server-rendered web application with one backend, where a session
cookie is smaller, instantly revocable, has no claim-staleness problem, and requires no rotation
story. Choosing JWTs there adds every hard problem in this topic and buys nothing.

**Counter-question: fine — what specifically do you lose by moving to server-side sessions?**

Three things, and I would name them rather than pretend the choice is free.

*Offline verification.* Every request now needs a lookup in the session store. With Redis that is
about a millisecond, which is usually irrelevant next to the rest of the request — but it is a hard
dependency, and if the session store is down, nobody is authenticated. A JWT resource server keeps
working through an authorization server outage, and that is a real availability property.

*Cross-domain and cross-organisation use.* Cookies are bound to a domain. If a mobile app, a partner
API, and three of your own domains all need the same identity, a bearer token travels naturally and
a cookie does not. Federation across organisations is effectively impossible with sessions.

*Horizontal scale of the verification path itself.* JWT verification is CPU-local and scales with
your service fleet automatically. A session store is a shared resource that must be sized, sharded,
and made highly available.

In exchange you get instant revocation, no claim staleness, smaller requests, no key rotation
problem, and no refresh token machinery at all. For a single-domain web application that is a clearly
better trade, and I would say so. For a multi-client distributed estate it is not.

The framing I would offer is that this is not "stateless versus stateful" but "where does the state
live and how often is it consulted". Both designs hold state; JWTs consult it rarely, sessions
consult it always.

**Counter-question: you have chosen JWTs. How do you implement "log out from all devices"?**

The mechanism people reach for first is a denylist of every outstanding token for that user, and it
is the wrong shape, because you would have to enumerate tokens you never stored.

The right mechanism is a **token version**. Keep a counter per user, include it in every access
token as a claim, and have resource servers compare it against the current value from a short-TTL
cache. "Log out everywhere" is a single increment; every token in circulation for that user
instantly fails the comparison, without knowing anything about the individual tokens.

Then the second half, which is easy to miss: **revoke every refresh token family for that user** as
well. The version increment kills the access tokens already out there; revoking the families stops
new ones being minted. Doing only the version increment means a client immediately refreshes and
gets a fresh, valid token. Doing only the family revocation leaves every device working until its
current access token expires — which is exactly what a user clicking "sign out everywhere" after a
suspected compromise does not want.

For "log out *this* device", the same machinery keyed differently: revoke that one family, and if
the residual access token minutes matter, denylist its `jti`. That is why the schema carries a
family per device with a label, an IP, and a user agent — it makes a genuine sessions screen
possible, and it means device-level revocation is one indexed update.

And I would note the cost plainly: I have now added a per-user counter, a cache, and a families
table. That is server-side session state. It is the unavoidable price of revocation in a
token-based system, and pretending otherwise is how people end up with a "stateless" design that
cannot log anyone out.
</details>

### Q5. A user reports their account was accessed from another country. Your access tokens live 15 minutes, refresh tokens 30 days with rotation and reuse detection. Walk me through your response.

<details>
<summary>Show answer</summary>

**Contain first, investigate second.** The instinct to understand before acting costs minutes
during which the attacker still has access.

*Immediate containment.* Revoke every refresh token family for that user, which stops any new
access tokens being minted. Increment the user's token version, which kills the access tokens
already in circulation — without this, the attacker keeps working for up to fifteen minutes. Force a
password reset, and if multi-factor authentication is not enabled, enrol it as part of recovery,
because otherwise the reset only helps if the password was the vector.

*Then investigate, and the design gives me unusually good material.* The refresh token table holds
`created_ip`, `user_agent`, `issued_at`, and `family_id` for every token in every lineage. So I can
reconstruct exactly when the second holder appeared and what they looked like. The specific question
I want answered is **whether a reuse event fired**, because it partitions the incident:

*Reuse was detected.* The attacker and the user both used the lineage, the family was burned
automatically, and I have a timestamp and two source addresses. The theft was of a refresh token,
and the access ended when detection fired. I now look for how the token was stolen — cross-site
scripting on the front end, a compromised device, a malicious extension, a leaked backup.

*No reuse was detected.* This is the more concerning branch, because it means the attacker never
collided with the legitimate client. Either they stole an access token only, in which case access
ended within fifteen minutes and there is no refresh lineage to find; or — and this is what I would
actually be worried about — **they authenticated legitimately** with stolen credentials and were
issued their own family. In that case the failure is in authentication, not tokens, and the
remediation is credential hygiene and multi-factor, not anything in this topic.

The way to tell them apart is the families table: a family created from a new IP and device at a
time the user was not logging in is a fresh authentication, not a stolen token.

*Then broaden.* Query for other users with reuse events, refreshes from the same ASN or IP range, or
impossible-travel patterns in the same window. One compromised account is an incident; ten sharing a
source is a campaign, and the response is different.

*Then close the loop.* If it was a token theft, find and fix the exfiltration path. If it was
credential stuffing, check whether the credentials appear in a known breach corpus and consider a
forced reset for the affected cohort.

**Counter-question: reuse was never detected. Does that mean your detection is broken?**

Not necessarily, and working through why is the useful part.

Detection only fires when **both** holders use the lineage. If the attacker stole the refresh token
and the user never refreshed again — they closed the laptop, or the client held a valid access token
for the rest of the window — there is no collision and nothing to detect. Detection is a
collision detector, not a theft detector, and that limitation is worth stating plainly rather than
overselling the control.

It also would not fire if only an access token was stolen, because no refresh token was involved at
all.

That said, I would verify the mechanism rather than assume. The specific ways it silently fails:
`reuseRefreshTokens` left at its default so nothing rotates and every presentation looks legitimate;
a read-then-write rotation that split the family into two independent lineages, so the two holders
never collide; or detection that fires and is swallowed because the event handler is not wired to
anything. I would check the first by refreshing twice and comparing values, the second with the
concurrency test, and the third by looking for any reuse event in the logs, ever — a system with
real users and zero reuse events in six months is more likely broken than perfect.

The complementary control, since detection has this blind spot, is behavioural: alert on a refresh
from a new country or autonomous system within an implausible interval of the previous one. That
catches the case where the attacker has the lineage to themselves.

**Counter-question: the user asks whether the attacker could still get in. What do you tell them, precisely?**

I would give them a specific answer with the reasoning, because a vague reassurance is worse than
useless and they will ask again.

"Access ended within fifteen minutes of us revoking, and here is why. Anything they were holding
that lets them get *new* access — the refresh credential — was revoked at 14:32 and cannot be used
again. Anything they were holding that grants *current* access expires within fifteen minutes by
design, and we additionally invalidated those immediately by incrementing your token version. Your
password has been reset, so they cannot start a new session, and we have enabled multi-factor
authentication so a stolen password alone would not be enough next time."

Then the honest caveats, because they matter: this covers access through our authentication system.
If the attacker also obtained data during their session, that data is already gone and revocation
does not retrieve it — so here is what they could have reached. And if their device or browser is
compromised, resetting the password from that same device hands the new password straight to the
attacker, so the reset should be done from a device we have reason to trust.

The reason to be this specific is that "we have secured your account" invites the follow-up "how do
you know". Being able to name the two mechanisms, the two timestamps, and the residual risk is what
makes the answer credible — and internally, being forced to articulate it is a good test of whether
the revocation design actually does what you think.
</details>

### Q6. Design question — design the complete token strategy for a banking application: web, iOS, Android, and third-party aggregators under open banking rules.

<details>
<summary>Show answer</summary>

Four consumer classes with genuinely different threat models, so four configurations from one
authorization server. Forcing them into one shape would mean securing everything to the weakest
client's constraints.

**Web — a backend-for-frontend, not tokens in the browser.** For a banking application I would not
put a token in the browser at all. The browser gets an `HttpOnly`, `Secure`, `SameSite=Strict`
session cookie; the BFF holds the access and refresh tokens server-side and attaches them to
upstream calls. This removes the entire "where do I store a JWT in a browser" problem rather than
choosing the least-bad answer, and cross-site scripting can then act *through* the session — noisy,
rate-limited, observable — but cannot exfiltrate a credential. Access token 5 minutes, refresh 8
hours absolute with a 15-minute idle timeout, rotation with reuse detection. The cost is a stateful
component and session affinity, which for this domain is clearly worth paying.

**iOS and Android — native, with hardware-backed keys.** Refresh token in the Keychain or the
Android Keystore with biometric unlock, never in shared preferences. Authorization code flow with
PKCE in an `ASWebAuthenticationSession` or Custom Tab, never an embedded web view, because an
embedded view can read the user's credentials. Access token 15 minutes, refresh 30 days absolute
with a 7-day idle timeout — longer than web because the storage is genuinely stronger and because
forcing a re-login on a phone drives users to weaker credentials. **Device binding**: the refresh
token is tied to a device identifier, and a refresh presenting a different device fingerprint is
rejected and alerted. I would also require step-up authentication — biometric or a fresh
authorization — for payment initiation regardless of token state, because token possession should
never by itself authorise moving money.

**Third-party aggregators — sender-constrained, always.** Under open banking these are registered,
certificate-bearing institutions, so mTLS is available and should be mandatory: client
authentication with `tls_client_auth` and certificate-bound access tokens carrying the certificate
thumbprint in the `cnf` claim, per RFC 8705. A stolen token is then useless without the private key.
Access token 10 minutes, refresh tied to the consent's lifetime — 90 days under typical open banking
rules, with mandatory re-consent at the end, which is a regulatory requirement and not a technical
choice. Scopes must be narrow and per-consent: `accounts:read` and `balances:read`, never
`payments:write` unless that specific consent was granted.

**Common to all of them:**

Asymmetric signing with `ES256` and a JWKS endpoint, rotated quarterly on the three-phase schedule
from file 23. Audience per service tier, not per service, with the payment-initiation tier separate
from everything else. Mandatory `jti`. A token version claim per user for instant global revocation.
Refresh tokens hashed with SHA-256 and stored with a family per device, absolute expiry inherited
on rotation.

**Revocation, layered:** short lifetimes as the baseline; token version for user-level events —
password change, account lock, "sign out everywhere"; a `jti` denylist in Redis for targeted
revocation of a specific compromised token, failing open because the lifetimes are short; and
introspection *only* on the payment-initiation endpoints, where the extra round trip is trivially
justified by the transaction value. That last point is the shape of the whole design: buy stronger
guarantees only where the value is highest, rather than paying for them everywhere.

**Monitoring:** reuse detection pages the on-call security engineer, because in this domain a
confirmed reuse is a fraud signal and not merely a bug. Impossible travel, refresh rate anomalies,
and any aggregator refreshing outside its consent scope all alert. Everything is written to an
append-only audit store with its own retention and access control, separate from the application
logs, because in a regulated environment the audit trail is evidence.

**Counter-question: the mobile team says a 30-day refresh token is too short — users complain about re-authenticating monthly. They want a year. What do you say?**

I would separate what they are actually asking for from what they proposed, because the complaint is
about friction and the proposal is about a security parameter.

The problem with a year is not the duration in the abstract; it is that a year-long refresh token in
device storage is a year-long account credential for a banking application, and the recovery story
for a lost or resold device becomes very long. Phones are lost, sold, and restored from backups to
new devices constantly.

What I would offer instead addresses the friction directly. First, an absolute lifetime of 90 days
with a rolling 30-day idle timeout — an active user never sees a prompt, because each use extends
the idle window while the absolute cap still bounds the credential. Second, biometric re-authentication
instead of a password prompt, so the "re-login" is a thumb press and the complaint largely
disappears. Third, device binding plus reuse detection, so a token lifted from a backup and used on
another device is rejected and alerted rather than merely being time-limited.

That combination gives the user experience they want without a year-long credential, and it is the
sort of trade I would want to reach rather than simply refusing.

If they still pushed for a year, I would ask what the actual complaint volume is, because "users
complain" is sometimes one loud ticket. And I would make it a documented risk acceptance with a
named owner in the business, not a quiet configuration change — in a regulated domain that
conversation will happen eventually, and it is far better on our terms than an auditor's.

**Counter-question: an aggregator's certificate is compromised. What is your response, and what does your design let you do that a bearer-token design would not?**

The certificate is what makes this tractable, and the contrast is the point of sender-constrained
tokens.

Immediate response: revoke the certificate at the certificate authority and add it to the revocation
list; reject the certificate at the TLS terminator as well, since certificate revocation checking is
unreliable in practice and I do not want to depend on it. Revoke every refresh token issued to that
client, and — this is the part the design makes possible — **every access token bound to that
certificate's thumbprint immediately stops working**, because the resource server checks the `cnf`
claim against the presenting connection's certificate. There is no residual window at all, even for
tokens already issued and in flight.

With plain bearer tokens I could not do that. Revoking the refresh tokens would stop new access
tokens, but every outstanding access token would keep working until `exp`, and an attacker holding
stolen bearer tokens would have an unstoppable window measured in minutes. With certificate binding
the tokens are inert the moment the certificate is rejected.

Then: issue a new certificate through the registration process, coordinate with the aggregator on
rotation, and audit every call made with the compromised certificate over the retained window,
checking whether any exceeded the consented scope.

And the design lesson worth stating: this is exactly why sender-constrained tokens matter. Every
other control in this design *limits the damage* from a stolen token. Binding makes the stolen token
useless, which turns a compromise into an operational task instead of an incident. Where the client
population makes it possible — registered institutions with certificates, or modern clients that can
do DPoP — it is the strongest control available, and I would push to extend it to the mobile clients
via DPoP as support matures.
</details>

---

## Quick Recall

```
WHY THE SPLIT
  access  short, self-contained JWT, presented EVERYWHERE, NOT revocable
  refresh long, OPAQUE, presented to the AUTH SERVER ONLY, hashed server-side, revocable
  high-exposure credential made harmless by being short-lived
  long-lived credential made safe by being rarely exposed + revocable
  a refresh token must NOT be a JWT (one verifier, already doing a lookup)

ACCESS TOKEN LIFETIME *IS* THE REVOCATION WINDOW
  5 min  high-value / money      15 min  the default      60 min  machine clients
  10k users @ 5 min ~= 33 refresh/s (each a DB write); @ 1 min ~= 166/s
  below ~5 min, buy revocation with a denylist, not with refresh traffic

REFRESH TOKEN STORAGE — IT IS A CREDENTIAL
  32 bytes SecureRandom, base64url
  store SHA-256 hex, UNIQUE index.  NOT bcrypt: 256 bits of entropy, nothing to brute-force
  columns: token_hash, family_id, user_id, used_at, revoked_at, revoked_reason, ip, ua

ROTATION + REUSE DETECTION  (the differentiator)
  rotation      = every use consumes the token; a new one is returned
  reuse         = an already-used token is presented -> TWO holders -> one is an attacker
  response      = revoke the WHOLE family_id + emit a security event + force re-auth
  order does not matter: whoever replays second burns the lineage for both
  UPDATE ... WHERE id=? AND used_at IS NULL   <- 0 rows = concurrent use = treat as reuse
  expires_at is INHERITED, never extended (absolute cap; idle timeout is separate)
  false positive = multi-tab / retry -> fix with a client single-flight lock,
                   or a short server-side grace window. NEVER disable detection.
  Spring Authorization Server: reuseRefreshTokens DEFAULT TRUE -> rotation is OFF
                               family revocation is NOT built in -> you add it

REVOCATION — THERE IS NO STATELESS REVOCATION
  short expiry only   latency = lifetime   cost 0            <- correct for most systems
  jti denylist Redis  immediate            1 lookup/request  <- targeted; TTL = REMAINING life
  token version "tv"  immediate            cacheable/user    <- "log out everywhere"
  introspection       immediate            network/request   <- you have rebuilt sessions
  denylist failure mode: fail OPEN for short tokens (exposure already accepted). TEST IT.

LOGOUT — ALWAYS REINTRODUCES SERVER STATE
  1 revoke the refresh FAMILY (RFC 7009 /oauth2/revoke)   <- the part that matters
  2 delete client-side copies
  3 denylist the outstanding access token jti (closes the residual minutes)
  4 increment tv for "everywhere"
  /logout must be POST (a GET logout is CSRF-able with an <img> tag)

BROWSER STORAGE
  localStorage / sessionStorage  XSS-readable -> AVOID
  in-memory                      good for the ACCESS token, lost on reload
  HttpOnly+Secure+SameSite cookie  good for the REFRESH token, needs CSRF defence
  BFF / token handler            browser never sees a token   <- BEST
  "XSS means you're owned anyway" is WRONG: HttpOnly forces the attacker to act
  THROUGH the browser (noisy, observable) instead of exfiltrating and acting anywhere

SENDER-CONSTRAINED (the real answer to theft)
  mTLS RFC 8705  cnf: {"x5t#S256": ...}  service-to-service, regulated, not browsers
  DPoP RFC 9449  per-request proof JWT over method+URI+time+ath; non-extractable CryptoKey
  makes a STOLEN token USELESS rather than merely short-lived

MULTI-DEVICE
  one FAMILY per device per login + label/ip/ua -> a real sessions screen
  log out one device  = revoke that family
  log out everywhere  = revoke ALL families AND increment tv   (both halves needed)

MONITORING
  refresh REUSE = the highest-signal event the system produces -> ALERT, do not just count
  log user_id, family_id, BOTH ips, BOTH user agents, BOTH timestamps
  also: impossible travel, refresh-rate anomalies, invalid_grant spikes
  rate-limit /oauth2/token per client AND per user (it does a DB write per call)

IN THE TOKEN / NOT IN THE TOKEN
  yes: iss sub aud exp nbf iat jti, coarse scopes/roles, tenant, tv or sid
  no:  email, name, national id, phone, fine-grained permissions, directory groups,
       internal hostnames, anything reportable if it appeared in a log
```

---

**Previous:** [`24_M7_T2_JWT_Spring_Integration.md`](24_M7_T2_JWT_Spring_Integration.md) ·
**Next:** [`26_M8_T1_Default_Filters.md`](26_M8_T1_Default_Filters.md)
