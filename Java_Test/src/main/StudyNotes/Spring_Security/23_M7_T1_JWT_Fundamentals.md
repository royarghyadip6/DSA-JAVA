# 7.1 — JWT Fundamentals

> **Module 7 · Topic 1** · JWT
> Baseline: Spring Security 6.x on Boot 3.x, Java 17+
> New to this? Read **In Plain English** below first, then come back to the Version Matrix.

---

## Version Matrix

| Concern | Spring Security 5.x | **Spring Security 6.x (baseline)** | Spring Security 7.x |
|---|---|---|---|
| JOSE implementation | Nimbus `nimbus-jose-jwt` via `spring-security-oauth2-jose` | **same** — Nimbus 9.x | same, Nimbus 10.x |
| `JwtDecoder` builders | `withJwkSetUri`, `withPublicKey`, `withSecretKey` | **same**, plus `withIssuerLocation(...)` (6.1+) | same |
| Default validators | `JwtTimestampValidator` only (`JwtValidators.createDefault()`) | **same** — `exp`/`nbf` with 60 s skew; `iss` only when built from an issuer | `typ` validation folded into the defaults |
| `typ` header validation | not performed | **`JwtTypeValidator` available in later 6.x**; `JwtValidators.createAtJwtValidator()` for RFC 9068 | on by default for issuer-built decoders |
| `aud` validation | **never automatic** | **still never automatic** — you must add it | still not automatic without configuration |
| Audience via properties | none | `spring.security.oauth2.resourceserver.jwt.audiences` (Boot 3.4+) | same |
| JWK set caching | Nimbus `RemoteJWKSet`, 5-minute cache | **Nimbus `JWKSourceBuilder`** — cache + rate-limited refresh on unknown `kid` | same |
| JOSE namespace | `javax.*`-free already | **`jakarta.*` elsewhere in the stack** | `jakarta.*` |

---

## Why This Exists

A JWT is the most widely deployed and most widely *misimplemented* credential format in
backend engineering. Every one of its published vulnerabilities is an implementation bug, not
a cryptographic weakness — which means the defence is knowledge, not a library upgrade.

File [`03_M1_T3_Cryptography.md`](03_M1_T3_Cryptography.md) established the cryptographic
ground rules: a JWS is signed and not encrypted, the signature proves integrity and
authenticity but nothing about confidentiality or current validity, and `HS256` versus `RS256`
is an architecture decision rather than a cryptography one. File
[`04_M1_T4_Servlet_Basics.md`](04_M1_T4_Servlet_Basics.md) established where a token-reading
filter is allowed to sit in the chain.

This topic goes underneath both. It covers the *format* — what the specifications say, which
fields exist, which are validated by default, how keys are published and rotated, how large a
token may be before your infrastructure silently rejects it, and the full catalogue of attacks
with the defence for each. The recurring interview failure is an engineer who can recite
"header, payload, signature" but cannot say which claims Spring Security validates without
being told to, or why `kid` is an attacker-controlled string.

---

## In Plain English

**The one-line version:** A JWT is a small piece of text carrying a few facts about a user, stamped in a
way that anybody holding the right public key can check for tampering on the spot, without having to
phone the office that issued it.

**An analogy.** Think of a passport. When you reach the boarding gate, the agent does not telephone the
passport office to ask whether you really exist. They look at the hologram, the watermark, and the
microprinting, all of which only the issuing office can produce, and that is enough to trust everything
printed on the page. This is exactly the appeal of a JWT: verification is instant, local, and works even
if the issuing office is closed.

Two consequences follow from that design, and both matter enormously.

The first is that a passport is not a locked box. Everything printed inside it, your name, your date of
birth, your nationality, is plainly readable by anyone who picks it up. The hologram prevents forgery,
not reading. A JWT is the same: the contents are merely encoded, not encrypted, and anyone who obtains
the token can read every claim inside it.

The second is that a passport cannot be un-printed. If yours is reported stolen five minutes after it is
issued, the gate agent who only checks the hologram still sees a perfectly genuine document. The only
real mitigations are printing a short expiry date on it, and maintaining a separate list of cancelled
passports that the gate must consult, which of course reintroduces exactly the phone call the hologram
was supposed to avoid. That tension is the permanent trade-off of token-based authentication.

**How it actually works, step by step.**

A JWT you see in the wild is one long string with two dots in it, like
`eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjMifQ.SflKxwRJSMeKK...`. The dots split it into three parts: a
header, a payload, and a signature.

The header is a tiny piece of JSON saying which algorithm was used to sign the token, for example
`{"alg":"RS256"}`, and often which key was used, under a field named `kid` meaning key identifier. The
payload is another piece of JSON containing the claims, which is the formal word for statements about
the user, such as `"sub":"1234"` for the subject, `"exp":1735689600` for the expiry time, and
`"iss":"https://login.example.com"` for the issuer. The signature is a block of raw bytes proving that
the first two parts have not been altered.

All three parts are written in an encoding called base64url, which is a way of expressing arbitrary
bytes using only characters that are safe inside a URL or an HTTP header. This is worth saying plainly
because it is the most common misunderstanding about JWTs: base64url is an encoding, not encryption.
Pasting a token into any online decoder reveals its full contents in seconds. Never put anything
sensitive in a claim.

Signing works in one of two families. Symmetric signing, written `HS256`, uses one shared secret for
both stamping and checking, which means anyone able to verify a token is also able to forge one.
Asymmetric signing, written `RS256` or `ES256`, uses a private key held only by the issuer for stamping
and a matching public key that anybody may hold for checking. Asymmetric is the right default for
anything larger than a single application, because a compromised verifier cannot mint tokens.

Because verifiers only need the public key, issuers publish their public keys at a well-known web
address as a document called a JWK Set. Each key in it carries a `kid`. When a token arrives, the
verifier reads the `kid` from the header, looks up that key in its cached copy of the set, and checks
the signature. This is what makes key rotation possible without downtime: publish the new key first,
start signing with it only once every verifier has had a chance to cache it, and retire the old key only
after the last token signed with it has expired.

Now the part that costs people the most. Spring Security, by default, checks the expiry time and the
not-before time, and it checks the issuer only when you built the decoder from an issuer address. It
does **not** check the audience claim, `aud`, which says which service the token was meant for. If two of
your services trust the same login server and neither checks `aud`, then a token issued for the
low-value reporting service is a completely valid credential for the high-value payments service. Adding
that check is your job, and the next file in this module shows how.

Finally, there is a whole family of attacks, and every single one comes from trusting something the
token itself told you. The header is written by whoever composed the token, which on a bad day is the
attacker. So the algorithm must be pinned in your configuration rather than read from the token, the
`kid` must only ever be used as a lookup key in a map you control and never as a filename or a piece of
a database query, and header fields such as `jku` that point at a URL to fetch the key from must be
ignored outright.

**Why should a beginner care?** The failure mode here is unusually harsh. Every published JWT
vulnerability is an implementation mistake rather than a flaw in the mathematics, which means a library
upgrade will not save you and reading carefully will. A validation step you simply forgot to add, most
often the audience check, produces a system that works perfectly in every test and hands an attacker a
valid credential in production. And the "it is just encoded, not encrypted" point catches nearly
everyone at least once, usually by putting an email address, a phone number, or an internal customer
identifier into a token that then sits in browser storage and in every proxy log along the way.

**Words you will meet in this file**

| Term | In plain words |
|---|---|
| JWT | JSON Web Token. A small set of facts about a user, packaged so that it can be checked for tampering. |
| Claim | One statement inside the token, such as who the user is or when the token expires. |
| JWS | The signed three-part format everyone actually means when they say JWT. Two dots, three segments. |
| JWE | The encrypted five-part format. Rare in practice, used when claims must not be readable in transit. |
| JOSE | The family of specifications that JWT, JWS, JWE, JWK and the algorithm list all belong to. |
| base64url | A way of writing bytes using only URL-safe characters. It is encoding, not encryption, and is trivially reversible. |
| Header | The first segment, saying which algorithm signed the token and often which key. Written by whoever made the token. |
| Payload | The second segment, holding the claims. Readable by anyone who has the token. |
| Signature | The third segment, proving the first two have not been altered since they were stamped. |
| `alg` | The signing algorithm named in the header. Pin the ones you accept in your own configuration; never trust the token's word for it. |
| `kid` | Key identifier. Tells the verifier which published key to check against. Safe as a lookup key, dangerous anywhere else. |
| `sub` | Subject. Who the token is about. Only unique within one issuer, so the real identity is issuer plus subject. |
| `iss` | Issuer. Which login server minted the token. |
| `aud` | Audience. Which service the token was meant for. Spring Security does not check this for you, and that is the most common real gap. |
| `exp` | Expiry time, in seconds since 1 January 1970. Checked by default. A token with no `exp` at all never expires. |
| `nbf` | Not before. The token is not valid until this instant. Also checked by default. |
| `iat` | Issued at. When the token was created. Parsed but not enforced. |
| `jti` | A unique identifier for this one token. The handle you would use to blacklist it. |
| `HS256` | Symmetric signing with one shared secret. Whoever can verify can also forge, so it suits a single application only. |
| `RS256` and `ES256` | Asymmetric signing. A private key stamps, a public key checks. `ES256` produces much smaller signatures. |
| `alg: none` | A legal but unsigned token format. Any verifier that accepts it can be trivially forged against. |
| JWK and JWKS | A public key written as JSON, and the published set of them that verifiers download and cache. |
| `JwtDecoder` | The Spring Security object that turns the raw string into a verified, trustworthy set of claims. |
| Clock skew | The small time allowance, sixty seconds by default, given because two machines never agree exactly on the time. |
| Bearer token | Any credential where simply holding the string is enough to be treated as the user. |

**If you remember only one thing:** a JWT is signed but not secret, and every claim inside it is
attacker-supplied text until the signature has been verified and every check you care about has passed.

---

## Core Concepts

### 1. The JOSE Family — Five Specifications, One Ecosystem

**In simple terms:** The word JWT is used loosely for a group of related standards, and separating them
makes it clear that a JWT only describes the contents while a separate standard describes the wrapper.

"JWT" is casually used to mean the whole thing. It is actually one member of a family of RFCs
published together in May 2015, collectively called **JOSE** (JavaScript Object Signing and
Encryption).

| Spec | RFC | What it defines | Shape |
|---|---|---|---|
| **JWT** | 7519 | A *claims* format — a JSON object of statements about a subject | Not a wire format on its own |
| **JWS** | 7515 | Signed content, integrity-protected | **3 parts**, two dots |
| **JWE** | 7516 | Encrypted content, confidentiality-protected | **5 parts**, four dots |
| **JWK** | 7517 | A key represented as JSON; **JWKS** is a set of them | `{"keys":[...]}` |
| **JWA** | 7518 | The algorithm registry — the legal values of `alg`, `enc`, `kty` | A table |
| **JWT BCP** | 8725 | Best current practice — the security rules that fix the 2015 mistakes | Guidance |

The relationship people get wrong: **a JWT is not a serialisation format.** RFC 7519 defines a
set of claims. To put those claims on the wire you must wrap them in *either* a JWS *or* a JWE.
So "a JWT" in practice means "a JWS whose payload happens to be a JSON claims set", and the
three-part `xxx.yyy.zzz` string everyone recognises is **JWS Compact Serialisation**. JWS can
carry arbitrary bytes, so a detached-payload JWS signing a file is a JWS but not a JWT; a
**nested JWT** — a JWS wrapped inside a JWE, giving both signing and encryption — is signalled
by `"cty":"JWT"` on the outer header.

The overwhelming default in Spring applications is JWS with an asymmetric signature. JWE
appears rarely, almost always because a regulator demanded that claims not be readable in
transit, and the better answer is usually to keep sensitive data server-side and put only an
opaque reference in the token.

### 2. Compact Serialisation — A Worked Decode

**In simple terms:** Here is a real token taken apart piece by piece, showing that the first two
segments are ordinary readable JSON and only the third is unreadable bytes.

```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
└──────────── header ────────────────┘ └──────────────────────── payload ───────────────────────────────────┘ └──────────────── signature ──────────────┘
```

Decoding part 1 (`base64url` → UTF-8 JSON):

```json
{"alg":"HS256","typ":"JWT"}
```

Decoding part 2:

```json
{"sub":"1234567890","name":"John Doe","iat":1516239022}
```

Part 3 is **not** text. It is 32 raw bytes of HMAC-SHA-256 output, `base64url`-encoded to 43
characters. There is nothing to "decode" into JSON.

The signed input is precisely the first two encoded segments joined by a dot — the **encoded**
forms, not the decoded JSON:

```
signature = HMAC-SHA256( ASCII(base64url(header) + "." + base64url(payload)), key )
```

That definition is deliberate. Because the signature covers the exact encoded bytes, a verifier
must never re-serialise the JSON before checking: JSON key ordering and whitespace are not
canonical, so re-serialising produces different bytes and the signature fails.

Two visual tells worth internalising for log triage. Every JWS starts with `eyJ`, because `{"`
always encodes to `eyJ`. And count the dots: **two dots is a JWS, four dots is a JWE**, while a
JWS with an empty third segment (`header.payload.`) is an **unsecured JWT** — `alg:none`, no
signature at all.

### 3. base64url Is Not base64

**In simple terms:** Tokens use a slightly different alphabet from ordinary base64 so they survive being
put in URLs and headers, and decoding one with the wrong decoder fails only on some tokens.

RFC 7515 mandates `base64url` (RFC 4648 §5) with padding removed. Two differences from
standard base64, and both cause real bugs:

| | Standard base64 | base64url |
|---|---|---|
| Index 62 | `+` | `-` |
| Index 63 | `/` | `_` |
| Padding | `=` to a multiple of 4 | **stripped entirely** |
| Safe in a URL / header | No — `+` becomes a space, `/` splits paths | Yes |

The example signature above, `SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c`, demonstrates both:
it contains `_` (which standard base64 would render as `/`), and it is 43 characters where
standard base64 of 32 bytes would be 44 with a trailing `=`.

In Java:

```java
Base64.getUrlDecoder().decode(segment);        // CORRECT — tolerates missing padding
Base64.getDecoder().decode(segment);           // WRONG — IllegalArgumentException on '-' or '_'
Base64.getMimeDecoder().decode(segment);       // WRONG — silently skips illegal characters
```

`Base64.getUrlDecoder()` accepts unpadded input, so you never need to re-pad manually. The
classic hand-rolled bug is decoding with `getDecoder()` and shipping it, because the happy-path
test token happened to contain no `-` or `_`; it then throws intermittently in production.

The security point, in a new context: base64url is **encoding, not encryption**. The payload is
world-readable by the browser, a proxy, a log aggregator, an APM agent, or a support engineer
with a screenshot.

### 4. JOSE Header Parameters

**In simple terms:** The header says how to check the token, but it was written by whoever created the
token, so taking its instructions at face value is what makes most JWT attacks possible.

The header tells the verifier how to verify. Every field in it is **attacker-controlled**,
because the attacker composes the token. That single sentence is the root of half the attacks
in section 11.

| Parameter | Meaning | Trust posture |
|---|---|---|
| `alg` | Signing algorithm (`RS256`, `ES256`, `HS256`, `none`, …) | **Never dispatch on it.** Pin the accepted set server-side and compare |
| `typ` | Media type of the whole object; `JWT`, or `at+jwt` for RFC 9068 access tokens | Optional; validating it prevents cross-token-type confusion |
| `cty` | Content type of the *payload*; `JWT` signals a nested JWE-over-JWS | Only relevant for nested tokens |
| `kid` | Key identifier — which key from the key set signed this | **Safe to use as a lookup key, never as a path, filename, or SQL fragment** |
| `jku` | URL of a JWK Set to fetch the key from | **Never honour from the token.** Use configured endpoints only |
| `jwk` | The public key, embedded directly in the header | **Never honour.** The attacker supplies their own keypair |
| `x5u` | URL of an X.509 certificate chain | **Never honour from the token** |
| `x5c` | The certificate chain, embedded | Only with a pinned trust anchor and full path validation |
| `x5t` / `x5t#S256` | SHA-1 / SHA-256 thumbprint of the signing certificate | A lookup key, exactly like `kid` |
| `crit` | Names of extension headers the verifier *must* understand | Reject the token if you do not understand a listed name |

**`kid` is the one you actually use.** It is an opaque string chosen by the issuer, and its
purpose is key rotation: the JWK Set publishes several keys at once, each with its own `kid`,
and the verifier picks the matching one. Without `kid` a verifier must trial-verify against
every published key, which works but is slower and makes rotation ambiguous. The security rule
is narrow and absolute: **treat `kid` as a map key into a set you control.** An unrecognised
`kid` means "refresh the key set once, then reject". It must never reach a filesystem call, a
SQL query, or a shell.

**`jku`, `jwk`, and `x5u` are the "let the attacker choose the key" parameters.** They exist
because JOSE was designed for scenarios broader than bearer tokens. For a resource server they
are pure liability. Spring Security's `NimbusJwtDecoder` never reads them — it uses only the
`JWKSource` you configured.

### 5. Registered Claims, and Which Are Actually Validated

**In simple terms:** Seven standard fields exist, but the library only enforces some of them, and the
one it never checks for you is the field saying which service the token was actually meant for.

RFC 7519 §4.1 registers seven claims. All seven are optional in the specification, which is
exactly the trap: **"the library parsed it" is not "the library checked it".**

| Claim | Name | Type | What it is *for* | Validated by Spring Security by default? |
|---|---|---|---|---|
| `iss` | Issuer | `StringOrURI` | Who minted the token — pins which authorization server you trust | **Only** if the decoder was built from an issuer location |
| `sub` | Subject | `StringOrURI` | The principal the claims are about; unique **within the issuer** | No — used as the principal name, not checked |
| `aud` | Audience | `StringOrURI` or array | Which resource server(s) may accept it | **No. This is the real gap.** |
| `exp` | Expiration | `NumericDate` | Hard cut-off; reject at or after | **Yes** — `JwtTimestampValidator` |
| `nbf` | Not Before | `NumericDate` | Token is invalid before this instant | **Yes** — `JwtTimestampValidator` |
| `iat` | Issued At | `NumericDate` | When it was minted; enables "max age" policies | Parsed, **not** enforced |
| `jti` | JWT ID | `String` | Unique token identifier — the handle for replay detection and denylisting | No — you must use it |

`NumericDate` is **seconds** since the Unix epoch, not milliseconds. A millisecond value
produces a token that expires in the year 55,000, and it passes every local test.

Three consequences that come up constantly in review:

**`sub` is only unique within an issuer.** Federate two identity providers and `sub` `1001` from
each is a different human. The primary key for a user is `(iss, sub)`, never `sub` alone.

**`aud` missing means cross-service replay.** If payments and reporting both trust the same
issuer and neither checks `aud`, a token minted for reporting is a valid credential for
payments — a low-value service becomes a credential source for a high-value one. Spring Security
does not add an audience validator for you, which makes this the single most common real gap;
[`24_M7_T2_JWT_Spring_Integration.md`](24_M7_T2_JWT_Spring_Integration.md) closes it.

**`exp` absent means a token valid forever.** `JwtTimestampValidator` only fails a token whose
`exp` is in the *past*; a token with no `exp` at all passes, because there is nothing to
compare. Require the claim to be present.

### 6. Public, Private, and Collision-Resistant Claim Names

**In simple terms:** You can add your own fields to a token, but give them a distinctive prefix, because
a plain name such as `role` will one day collide with one your identity provider starts sending.

Beyond the registered seven, RFC 7519 defines **public claims** — names in the IANA JSON Web
Token Claims registry (`scope`, `client_id`, `groups`, `roles`, and the OIDC set `email`, `name`,
`preferred_username`, `azp`, `nonce`, `auth_time`, `acr`, `amr`), or names collision-resistant by
construction — and **private claims**, anything you and your counterparty agree on privately.

The collision-resistant convention is a namespaced URI, which is what Auth0 enforces for custom
claims: `"https://api.example.com/claims/tenant": "acme"` rather than a bare `"tenant": "acme"`.
Verbose, and worth it the moment two systems federate. A bare `"role": "admin"` looks cleaner
until your identity provider starts emitting its own `role` claim with different semantics and
one silently wins. The practical compromise is a single short organisation-wide prefix —
`acme_tenant`, `acme_plan` — documented in one place. What is not defensible is unprefixed
generic names like `type`, `id`, `user`, or `admin`.

### 7. Signing Algorithms

**In simple terms:** The central choice is between one shared secret, where anyone who can check a token
can also forge one, and a key pair, where only the issuer can create tokens and everyone else can check.

| `alg` | Family | Key | Signature size | Relative speed | Use when |
|---|---|---|---|---|---|
| `HS256` | HMAC-SHA-256 | Shared secret, **≥ 256 bits** | 32 B | Fastest, both sides | One service issues *and* verifies |
| `HS384` / `HS512` | HMAC-SHA-384/512 | ≥ 384 / 512 bits | 48 / 64 B | Fast | Rarely needed over HS256 |
| `RS256` | RSASSA-PKCS1-v1_5 + SHA-256 | RSA ≥ 2048 | **256 B** (RSA-2048) | Slow sign, **fast verify** | Maximum interoperability; the de-facto default |
| `RS384` / `RS512` | Same, larger digest | RSA ≥ 2048 | 256 B | Same | Compliance mandates only |
| `PS256` | RSASSA-**PSS** + SHA-256, MGF1 | RSA ≥ 2048 | 256 B | Slightly slower than RS256 | Preferred over RS256 for new RSA deployments — PSS has a modern security proof |
| `ES256` | ECDSA P-256 + SHA-256 | EC P-256 | **64 B** | Fast sign, verify slower than RSA | Token size matters; modern default |
| `ES384` / `ES512` | ECDSA P-384 / P-521 | EC P-384 / P-521 | 96 / **132** B | Slower | Compliance mandates only |
| `EdDSA` | Ed25519 / Ed448 (RFC 8037) | Ed25519 | 64 B | Fastest asymmetric, deterministic | Greenfield with modern libraries only |
| `none` | — | — | 0 B | — | **Never.** See section 11 |

Reading the table as a decision:

**`HS256` is a monolith algorithm.** Symmetric means anyone who can verify can also forge, so in
a microservices estate every resource server holds the signing secret and compromising the least
important service mints admin tokens for the most important one. It also has no clean rotation
story, because the secret must change on the issuer and every verifier simultaneously.

**`RS256` is the interoperability default.** Every library, gateway, and legacy client supports
it. Its cost is size: an RSA-2048 signature is 256 bytes, roughly 342 characters after
base64url, before any claims.

**`ES256` is the size and performance answer.** A 64-byte signature and much faster key
generation and signing. Verification is somewhat slower than RSA, which is the one workload a
resource server performs on every request, so measure rather than assume. Two cautions: ECDSA
catastrophically leaks the private key if its per-signature nonce ever repeats, and
CVE-2022-21449 ("Psychic Signatures", JDK 15 through 18) let an all-zero ECDSA signature verify
against any key. Both are library concerns, which is another argument against hand-rolling.

**`PS256` over `RS256` when you control both ends** — RSASSA-PSS is randomised and has a
security proof that PKCS#1 v1.5 lacks. A preference, not an urgency. **`EdDSA` when the whole
estate is modern**: deterministic, fast, small, but registered separately in RFC 8037 and less
widely supported.

One JOSE subtlety: JWS uses the **raw `R || S` concatenation** for ECDSA, not the DER-encoded
`SEQUENCE` that `java.security.Signature` produces. A hand-rolled verifier that passes JOSE
signature bytes straight to a JCA `Signature` fails on every token with an unhelpful error.

### 8. Clock Skew and Leeway

**In simple terms:** Two servers never agree on the time to the exact second, so expiry checks allow a
small grace period, and enlarging that grace period to hide a broken clock extends every token's life.

`exp` and `nbf` are absolute instants compared against the verifier's clock. Two machines
never agree exactly, so every serious library accepts a **leeway** (Spring calls it
`clockSkew`).

```java
// org.springframework.security.oauth2.jwt.JwtTimestampValidator
private static final Duration DEFAULT_MAX_CLOCK_SKEW = Duration.ofSeconds(60);

public OAuth2TokenValidatorResult validate(Jwt jwt) {
    Instant expiry = jwt.getExpiresAt();
    if (expiry != null && Instant.now(this.clock).minus(this.clockSkew).isAfter(expiry)) {
        return OAuth2TokenValidatorResult.failure(createOAuth2Error(
            "Jwt expired at " + jwt.getExpiresAt()));
    }
    Instant notBefore = jwt.getNotBefore();
    if (notBefore != null && Instant.now(this.clock).plus(this.clockSkew).isBefore(notBefore)) {
        return OAuth2TokenValidatorResult.failure(createOAuth2Error(
            "Jwt used before " + jwt.getNotBefore()));
    }
    return OAuth2TokenValidatorResult.success();
}
```

Spring Security's default is **60 seconds** in both directions. Skew on `exp` extends a token's
life and skew on `nbf` accepts it early, so leeway is a security cost paid for reliability.

The honest position: leeway is a workaround for infrastructure you should fix. If your fleet
runs NTP and drift is measured in milliseconds, 30 seconds is generous. Raising leeway to five
minutes because "we saw expired-token errors" hides a broken clock and materially extends the
replay window — and a broken clock shows a *consistent* offset, whereas genuine skew is small
and random. Leeway bites hardest on short-lived tokens: a 60-second access token with 60 seconds
of leeway is a 120-second token, so the short lifetime is fiction.

### 9. Token Size and the HTTP Header Limits You Will Hit

**In simple terms:** A token rides along in a header on every single request, so once it grows too large
it is rejected by a proxy before your application ever sees it, leaving nothing in your logs.

A JWT travels in a header on every request, so size is an operational constraint — and it fails
in a way that is hard to diagnose, because the rejection happens in infrastructure and never
reaches your application logs.

Rough budget for an `RS256` token: header 40–70 characters, payload with the registered claims
and a handful of scopes 300–500, RSA-2048 signature 342, plus 22 for `Authorization: Bearer `.
Around 700–900 characters total, which is comfortable. It stops being comfortable the moment
someone adds directory group membership: a user in 200 Active Directory groups produces a
multi-kilobyte token, and it works perfectly for the developer who tested with three groups.

| Component | Limit | Default | When exceeded |
|---|---|---|---|
| Tomcat (Boot 3) | `server.max-http-request-header-size` | **8 KB** | `400 Bad Request` |
| nginx | `large_client_header_buffers` | **4 × 8 KB** | `400`, or `494` internally |
| nginx | `client_header_buffer_size` | 1 KB | Promoted to a large buffer |
| Apache httpd | `LimitRequestFieldSize` | 8190 B | `400 Bad Request` |
| AWS ALB | single header | ~16 KB | `400` from the load balancer |
| AWS ALB | all request headers | ~64 KB | `400` from the load balancer |
| Browser cookie | per cookie | **4096 B** | Cookie silently **dropped** |

Two failure modes matter. **The 400 with no application log**: nginx or the ALB rejects the
request before your service sees it, so the access log is empty, the APM shows nothing, and the
client gets HTML instead of JSON. The tell is that it correlates with *specific users* — the ones
in many groups — rather than with load or endpoint. **The silently dropped cookie**: cross 4096
bytes and the browser simply does not store it, with no error anywhere, so the user appears
logged out immediately after logging in and only on their own account.

Fixes in order: resolve groups and permissions server-side from `sub` instead of carrying them;
switch to `ES256` to reclaim roughly 280 characters of signature; shorten claim names; use a
reference token if the data genuinely must be large. Raising buffer limits is last, because you
must raise them at *every* hop and one missed proxy reproduces the bug.

### 10. JWKS — Publishing and Rotating Keys

**In simple terms:** The issuer publishes its public keys at a web address that verifiers download and
cache, which is what allows a signing key to be replaced without invalidating every token in flight.

A **JWK Set** is a JSON document containing the issuer's public keys. It is what makes
asymmetric JWTs operationally practical, because resource servers fetch keys rather than
having them deployed.

```json
{
  "keys": [
    { "kty": "RSA", "use": "sig", "alg": "RS256", "kid": "2026-03-a",
      "n": "0vx7agoebGcQSuuPiLJXZptN9nndrQmbXEps2aiAFbWhM78LhWx4...", "e": "AQAB" },

    { "kty": "EC",  "use": "sig", "alg": "ES256", "kid": "2026-06-b", "crv": "P-256",
      "x": "f83OJ3D2xF1Bg8vub9tLe1gHMzV76e8Tus9uPHvRVEU",
      "y": "x_FEzRu9m36HLN_tue659LNpXW6pCyStikYjKIWI5a0" }
  ]
}
```

There are **no private key components**. An RSA JWK carries only the modulus `n` and exponent
`e`; the private half would add `d`, `p`, and `q` and must never appear on a public endpoint. A
JWKS endpoint exposing `d` is a total compromise of the issuer, and it has happened.

Discovery normally runs through OIDC: `GET {issuer}/.well-known/openid-configuration` returns a
document whose `jwks_uri` points at the key set. Spring Security's
`JwtDecoders.fromIssuerLocation(issuer)` performs exactly this and additionally verifies that the
`issuer` field in the discovery document matches what you asked for — a small but important check
against a misconfigured or hijacked discovery URL.

**Rotation without downtime** is the whole point of `kid`:

```mermaid
sequenceDiagram
    participant AS as Authorization Server
    participant JW as JWKS endpoint
    participant RS as Resource Server
    participant C as Client

    Note over AS: Phase 1 — publish the new key, keep signing with the old
    AS->>JW: keys = [ kid=A (signing), kid=B (published only) ]
    RS->>JW: GET /.well-known/jwks.json
    JW-->>RS: [A, B]
    Note over RS: cached ~5 min; both keys now known

    Note over AS: Phase 2 — switch signing to B
    C->>AS: authenticate
    AS-->>C: token signed with kid=B
    C->>RS: Authorization: Bearer <kid=B>
    RS->>RS: kid=B already cached, verify locally
    RS-->>C: 200

    Note over RS: A resource server that had NOT refreshed
    C->>RS: Authorization: Bearer <kid=B>
    RS->>RS: kid=B unknown in cache
    RS->>JW: forced refresh (rate limited)
    JW-->>RS: [A, B]
    RS->>RS: verify with B
    RS-->>C: 200

    Note over AS: Phase 3 — only after max token lifetime has elapsed
    AS->>JW: keys = [ kid=B ]
```

The critical ordering: **publish before you sign, and retire only after the last token signed
with the old key has expired.** Removing the old key immediately invalidates every token in
flight. The safe retirement delay is the maximum access token lifetime plus the maximum JWKS
cache time-to-live plus a margin.

**Caching and refresh-on-unknown-`kid`** is what makes this work without a thundering herd.
Nimbus's `JWKSourceBuilder`, which `NimbusJwtDecoder` uses underneath, layers a time-based cache
(five minutes by default) so the common path is local; a forced refresh when a token arrives
with a `kid` not in the cache; and a rate limiter on those forced refreshes, so a flood of
tokens with garbage `kid` values cannot become a denial-of-service against the authorization
server. An outage-tolerant cache that keeps serving stale keys when the JWKS endpoint is
unreachable is worth enabling deliberately, since a JWKS outage otherwise takes down every
resource server that has to refresh during it.

The anti-pattern is fetching the JWK Set per request, which puts a network round trip and a hard
availability dependency on the hot path. It is what you accidentally build by constructing a new
`JwtDecoder` per request instead of registering a singleton bean.

### 11. The Attack Catalogue

**In simple terms:** Every known way of breaking JWT verification, and each one comes down to the same
root cause: the code believed something the token said before proving the token was genuine.

| Attack | Mechanism | Defence |
|---|---|---|
| **`alg: none`** | Header set to `{"alg":"none"}`, signature segment empty. A verifier that dispatches on `alg` accepts anything | Pin the accepted algorithms server-side; never let the token choose. Nimbus rejects `none` unless you explicitly build an unsecured parser |
| **Algorithm confusion (`RS256` → `HS256`)** | Attacker takes the *public* key from your JWKS, sets `alg:HS256`, and HMACs the token with that public key as the secret. A verifier that passes "the key" to a generic verify function accepts it | Pin the algorithm **and** bind key type to algorithm. An RSA public key must never be usable as an HMAC secret |
| **`kid` path traversal** | `"kid":"../../../../dev/null"` makes the verifier read an empty file as the key, so the attacker HMACs with an empty secret. `/proc/sys/kernel/randomize_va_space` (value `2`) is another classic | Never use `kid` as a file path. Look it up in an in-memory map from the JWK Set |
| **`kid` SQL injection** | `"kid":"x' UNION SELECT 'secret"` against `SELECT key FROM keys WHERE kid = '...'` returns an attacker-chosen key | Never interpolate `kid`. Parameterise, or better, do not use a database for this |
| **`kid` command injection** | `kid` passed to a shell to invoke an external key tool | Never. Same rule |
| **`jku` / `x5u` redirection** | Attacker sets `jku` to a URL they control, serving their own public key. Variants use an open redirect on your domain to defeat naive host allowlists | Ignore `jku`/`x5u`/`jwk`/`x5c` from the token entirely. Use only configured key sources |
| **Weak HMAC secret** | `HS256` with `secret`, `changeit`, or the framework sample value. `hashcat -m 16500` cracks a dictionary secret offline in seconds, and the attacker needs only one captured token | Use ≥ 256 bits from `SecureRandom`; prefer asymmetric so there is no shared secret at all |
| **Missing `exp` validation** | Library parsed the token but nothing compared `exp` to now. Common in hand-rolled filters | Use a decoder with `JwtTimestampValidator`; additionally require `exp` to be *present* |
| **Missing `aud` validation** | Token minted for service A is replayed against service B, which trusts the same issuer | Add an audience validator. Not on by default in Spring Security |
| **Missing `iss` validation** | Any issuer's token is accepted, including one from a public provider the attacker can self-register with | Build the decoder from an issuer location, or add `JwtIssuerValidator` |
| **"Sign-then-read" ordering** | Claims parsed and acted upon *before* signature verification — logging `sub`, selecting a tenant datasource, or looking up a key by an unverified claim | Verify first, read second. Treat the token as hostile bytes until verification returns |
| **Replay of a stolen bearer token** | Anyone holding the string is the user; nothing binds it to the client | Short lifetimes, TLS everywhere, and sender-constrained tokens (DPoP / mTLS) — see [`25_M7_T3_Token_Strategies.md`](25_M7_T3_Token_Strategies.md) |
| **Sensitive data in claims** | Payload is plaintext; it lands in browser storage, proxy logs, APM traces, and screenshots | Put identifiers in the token, data on the server |
| **`typ` confusion** | An ID token, a refresh token, or a token from a different flow is presented where an access token is expected | Validate `typ` (RFC 9068 uses `at+jwt`), and always validate `aud` |

The "sign-then-read" mistake deserves emphasis because well-meaning code creates it. Consider a
multi-tenant application that reads a `tenant` claim in a filter, before verification, so the
datasource is ready by the time the controller runs. An attacker forges an unsigned token with
any tenant value; verification later fails and the request is rejected, but a connection to
another tenant's database was already opened and the logging, metrics, and pool state it touched
are attacker-controlled. The rule is mechanical: **no claim influences any decision until
verification has succeeded.**

### 12. What a JWT Is Good At, and What It Is Not

**In simple terms:** Tokens are excellent when a service must decide on its own without calling anyone,
and poor whenever you need to cancel access immediately, because a signature cannot be taken back.

JWTs are excellent when the verifier must decide **without a network call to the issuer** — that
is the entire value proposition. They are poor when you need **immediate revocation**, because a
signature cannot be un-signed: a token is valid until `exp` regardless of what happens to the
account, and every revocation mechanism reintroduces server-side state and therefore erodes the
statelessness that motivated JWTs in the first place. That tension is the subject of
[`25_M7_T3_Token_Strategies.md`](25_M7_T3_Token_Strategies.md). They are also poor as a session
mechanism for a server-rendered application, where an opaque session cookie is smaller, instantly
revocable, and free of claim staleness — "we used JWTs because they are stateless" is frequently
a decision made before anyone asked whether statelessness was needed.

---

## Working Code

Dependencies — the resource-server starter pulls `spring-security-oauth2-jose`, which brings
Nimbus:

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>

<!-- Only if you are issuing tokens yourself and need signing APIs directly. -->
<dependency>
  <groupId>com.nimbusds</groupId>
  <artifactId>nimbus-jose-jwt</artifactId>
</dependency>
```

Verification done correctly with Nimbus directly — what a library does for you, written out so
the pinning is visible. Nothing in this class reads a claim before `process` returns.

```java
package com.example.jwt;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.*;
import com.nimbusds.jose.proc.*;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.*;

import java.net.URL;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class StrictJwtVerifier {

    /** Deliberately well below Tomcat's and nginx's 8 KB header budgets. */
    public static final int MAX_TOKEN_CHARS = 4096;

    private final DefaultJWTProcessor<SecurityContext> processor;

    public StrictJwtVerifier(URL jwkSetUrl, String issuer, String audience) {

        // Cache the key set; force a refresh on an unknown kid; rate-limit those forced
        // refreshes so a flood of bogus kids cannot DoS the authorization server; keep
        // serving stale keys through a JWKS outage.
        JWKSource<SecurityContext> keys = JWKSourceBuilder.create(jwkSetUrl)
                .retrying(true)
                .cache(TimeUnit.MINUTES.toMillis(5), TimeUnit.SECONDS.toMillis(30))
                .rateLimited(TimeUnit.SECONDS.toMillis(30))
                .outageTolerant(TimeUnit.MINUTES.toMillis(30))
                .build();

        this.processor = new DefaultJWTProcessor<>();

        // THE PIN. The token's "alg" is compared against RS256/ES256, never used to select a
        // code path. alg:none and HS256 are structurally rejected, and the selector hands back
        // only RSA/EC keys, so a public key can never be pressed into service as an HMAC secret.
        this.processor.setJWSKeySelector(new JWSVerificationKeySelector<>(
                Set.of(JWSAlgorithm.RS256, JWSAlgorithm.ES256), keys));

        // Required claims are DECLARED, so a token that simply omits exp or jti fails.
        this.processor.setJWTClaimsSetVerifier(new DefaultJWTClaimsVerifier<>(
                audience,
                new JWTClaimsSet.Builder().issuer(issuer).build(),
                Set.of("sub", "exp", "iat", "jti")));
    }

    /** @return the verified claims. No claim may be read before this returns. */
    public JWTClaimsSet verify(String compact) throws Exception {
        if (compact.length() > MAX_TOKEN_CHARS) {
            // Fail loudly here rather than silently at a proxy an hour later.
            throw new IllegalStateException("Token is " + compact.length() + " chars, limit "
                    + MAX_TOKEN_CHARS + "; move group and permission lists out of the token.");
        }
        return this.processor.process(compact, null);
    }
}
```

Tests that assert the attacks actually fail:

```java
package com.example.jwt;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

class JwtAttackTests {

    private static final String ISSUER = "https://idp.example.com";
    private static final String AUDIENCE = "orders-api";

    private JWTClaimsSet claims() {
        return new JWTClaimsSet.Builder().issuer(ISSUER).subject("u-8842").audience(AUDIENCE)
                .jwtID("jti-1").issueTime(Date.from(Instant.now()))
                .expirationTime(Date.from(Instant.now().plusSeconds(300))).build();
    }

    private String sign(RSAKey key, JWTClaimsSet claims) throws Exception {
        SignedJWT jwt = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID("k1").build(), claims);
        jwt.sign(new RSASSASigner(key));
        return jwt.serialize();
    }

    @Test
    void algNoneIsRejected() {
        String compact = new PlainJWT(claims()).serialize();
        assertThat(compact).endsWith(".");                    // empty signature segment

        assertThatThrownBy(() -> verifier().verify(compact))
                .hasMessageContaining("Unsecured");           // no JWS key selector matches
    }

    @Test
    void algorithmConfusionUsingThePublicKeyAsAnHmacSecretIsRejected() throws Exception {
        RSAKey rsa = new RSAKeyGenerator(2048).keyID("k1").generate();

        // The attacker has the PUBLIC key — it is published at the JWKS endpoint.
        byte[] publicKey = rsa.toPublicJWK().toJSONString().getBytes();
        SignedJWT forged = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.HS256).keyID("k1").build(), claims());
        forged.sign(new MACSigner(Arrays.copyOf(publicKey, 32)));

        // Only RS256/ES256 are accepted, so the HS256 header never reaches a verifier at all.
        assertThatThrownBy(() -> verifier().verify(forged.serialize()))
                .hasMessageContaining("algorithm");
    }

    @Test
    void tokensMissingExpOrCarryingAnotherAudienceAreRejected() throws Exception {
        RSAKey rsa = new RSAKeyGenerator(2048).keyID("k1").generate();

        String noExp = sign(rsa, new JWTClaimsSet.Builder()
                .issuer(ISSUER).subject("u-8842").audience(AUDIENCE).jwtID("jti-2").build());
        String wrongAudience = sign(rsa,
                new JWTClaimsSet.Builder(claims()).audience("reporting-api").build());

        assertThatThrownBy(() -> verifier().verify(noExp)).hasMessageContaining("exp");
        assertThatThrownBy(() -> verifier().verify(wrongAudience))
                .hasMessageContaining("audience");
    }

    @Test
    void base64urlDecodingMustUseTheUrlAlphabet() {
        String signature = "SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";   // 43 chars, 32 bytes

        assertThat(Base64.getUrlDecoder().decode(signature)).hasSize(32);
        assertThatThrownBy(() -> Base64.getDecoder().decode(signature))
                .isInstanceOf(IllegalArgumentException.class);              // chokes on '_'
    }

    private StrictJwtVerifier verifier() { /* wired to a local JWKS fixture */ return null; }
}
```

---

## Internals

### How `NimbusJwtDecoder` pins the algorithm

```java
// org.springframework.security.oauth2.jwt.NimbusJwtDecoder.JwkSetUriJwtDecoderBuilder
// (simplified)

private Set<SignatureAlgorithm> signatureAlgorithms = new HashSet<>();

public JwkSetUriJwtDecoderBuilder jwsAlgorithm(SignatureAlgorithm signatureAlgorithm) {
    this.signatureAlgorithms.add(signatureAlgorithm);
    return this;
}

JWSKeySelector<SecurityContext> jwsKeySelector(JWKSource<SecurityContext> jwkSource) {
    if (this.signatureAlgorithms.isEmpty()) {
        // Default: RS256 only.
        return new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, jwkSource);
    }
    Set<JWSAlgorithm> jwsAlgorithms = new HashSet<>();
    for (SignatureAlgorithm signatureAlgorithm : this.signatureAlgorithms) {
        jwsAlgorithms.add(JWSAlgorithm.parse(signatureAlgorithm.getName()));
    }
    return new JWSVerificationKeySelector<>(jwsAlgorithms, jwkSource);
}
```

Two things to read out of this. The default accepted algorithm is **`RS256` alone** — not
"whatever the token says", and not "any RSA algorithm". And the selector resolves keys **from
the `JWKSource`, filtered by the algorithm**, so an HMAC algorithm can never resolve a key from
an RSA JWK source. Algorithm confusion is prevented by the shape of the API, not by a check
bolted onto it.

### How the `kid` lookup actually happens

```java
// com.nimbusds.jose.proc.JWSVerificationKeySelector (simplified)

public List<Key> selectJWSKeys(JWSHeader header, C context) throws KeySourceException {
    if (!this.allowedAlgs.contains(header.getAlgorithm())) {
        return Collections.emptyList();      // algorithm not accepted -> no keys, verification fails
    }
    JWKMatcher matcher = createJWKMatcher(header);   // matches on kid, kty, use, alg, x5t
    List<JWK> matches = getJWKSource().get(new JWKSelector(matcher), context);
    return KeyConverter.toJavaKeys(matches);
}
```

The `kid` becomes one criterion in a `JWKMatcher` against an **in-memory list** of keys from the
configured JWKS endpoint. It is a filter predicate, never a path, a query, or an identifier
passed outside the process — which is why the `kid` injection family is confined to hand-rolled
verifiers.

### `JwtValidators.createDefault()` — what you get for free

```java
// org.springframework.security.oauth2.jwt.JwtValidators (simplified, 6.x)

public static OAuth2TokenValidator<Jwt> createDefault() {
    return new DelegatingOAuth2TokenValidator<>(
        Collections.singletonList(new JwtTimestampValidator()));
}

public static OAuth2TokenValidator<Jwt> createDefaultWithIssuer(String issuer) {
    return new DelegatingOAuth2TokenValidator<>(
        Arrays.asList(new JwtTimestampValidator(), new JwtIssuerValidator(issuer)));
}
```

Read it literally. The default is **timestamps only**. Building the decoder from an issuer
location adds issuer checking. **Nothing adds an audience validator**, and nothing requires
`exp` to be present. Those are yours to add, and
[`24_M7_T2_JWT_Spring_Integration.md`](24_M7_T2_JWT_Spring_Integration.md) shows exactly how.

### The `Jwt` object encodes "verify first, read second"

```java
// org.springframework.security.oauth2.jwt.Jwt
public class Jwt extends AbstractOAuth2Token implements JwtClaimAccessor {
    private final Map<String, Object> headers;
    private final Map<String, Object> claims;
    // from JwtClaimAccessor: getIssuer(), getSubject(), getAudience(), getExpiresAt(),
    // getNotBefore(), getIssuedAt(), getId(), getClaimAsString/StringList/Map(name)
}
```

A `Jwt` instance only ever exists **after** successful verification — `JwtDecoder.decode` either
returns one or throws `JwtException`. That puts the ordering rule into the type system, and it
is why a `JwtAuthenticationToken` holding a `Jwt` principal can be trusted downstream.

---

## Configuration Reference

| Option / API | Effect | Default |
|---|---|---|
| `NimbusJwtDecoder.withJwkSetUri(uri)` | Fetch keys from a JWK Set endpoint | — |
| `.jwsAlgorithm(SignatureAlgorithm.X)` | **Pin** accepted algorithms; repeatable | `RS256` only |
| `.cache(Cache)` | Back the JWK Set with a Spring `Cache` | Nimbus in-memory cache |
| `.restOperations(RestOperations)` | Custom HTTP client for the JWKS fetch (timeouts, proxy, mTLS) | plain `RestTemplate` |
| `NimbusJwtDecoder.withPublicKey(key)` | Single static RSA public key, no JWKS | — |
| `NimbusJwtDecoder.withSecretKey(key)` | HMAC verification | `HS256` |
| `NimbusJwtDecoder.withIssuerLocation(uri)` | OIDC/OAuth2 discovery, lazily resolved (6.1+) | — |
| `JwtDecoders.fromIssuerLocation(uri)` | Discovery at startup; validates the `issuer` field | — |
| `JwtTimestampValidator(Duration)` | Clock skew allowance for `exp`/`nbf` | **60 s** |
| `JwtTimestampValidator.setClock(Clock)` | Injectable clock, for tests | `Clock.systemUTC()` |
| `JwtIssuerValidator(issuer)` | Exact-match `iss` | not registered unless issuer-built |
| `JwtClaimValidator<T>(name, predicate)` | Arbitrary claim rule — how you add `aud` | none |
| `DelegatingOAuth2TokenValidator<>(...)` | Compose validators; collects **all** errors | — |
| `JWKSourceBuilder.cache(ttl, refreshTimeout)` | JWK Set cache lifetime | ~5 min |
| `JWKSourceBuilder.rateLimited(minInterval)` | Throttle forced refreshes on unknown `kid` | enabled |
| `JWKSourceBuilder.outageTolerant(ttl)` | Serve stale keys through a JWKS outage | disabled |
| `server.max-http-request-header-size` | Tomcat header budget | **8 KB** |

---

## Production Concerns & Anti-Patterns

**Reading claims before verifying the signature.** A filter that extracts a tenant, a correlation
identifier, or a user name from the payload "just for logging" before the decoder has run is
acting on attacker-supplied data. Verify first; log from the verified `Jwt`.

**Treating the payload as private.** It is base64url, not encryption, and anything in it reaches
browser storage, proxy access logs, APM traces, crash reports, and support screenshots. File 03's
rule stands: never put in a payload what you would not put in a log.

**Putting group or permission lists in the token.** Works for the developer with three groups,
breaks for the executive with two hundred, at which point a proxy returns a 400 your application
never sees. Resolve authority from `sub` server-side, or use coarse scopes.

**An `HS256` secret shorter than 256 bits, or a human-chosen one.** `hashcat -m 16500` cracks a
dictionary secret offline from a single captured token, and the attacker can then mint anything.
If you must use `HS256`, take 32 bytes from `SecureRandom` — but this is really an argument for
an asymmetric algorithm.

**Long-lived access tokens.** A one-hour token is a one-hour revocation window: disable the
account and the token still works. Minutes, not hours, with a refresh token doing the long-lived
work — the subject of file 25.

**Removing a rotated key from the JWKS immediately**, which fails every in-flight token signed
with it. Retire only after the maximum token lifetime plus the maximum cache time-to-live.

**Fetching the JWK Set per request**, which puts a network call and a hard availability
dependency on the hot path. Register the decoder as a singleton and let the cache work.

**Trusting `jku`, `x5u`, or an embedded `jwk`.** The token would choose its own verification key,
which is not verification.

**Milliseconds in `exp`, `nbf`, or `iat`.** `NumericDate` is seconds; a millisecond value
produces a token that never expires, and every local test passes.

**Using `sub` alone as a user primary key.** It is unique only per issuer; federation needs
`(iss, sub)`.

**Assuming the library validated something.** Parsing is not validation. Write tests that present
an expired token, a wrong-audience token, and a wrong-issuer token and assert each is rejected.
If any passes, you have found a real gap, not a test problem.

---

## Debugging Playbook

| Symptom | Likely root cause | Fix |
|---|---|---|
| `JwtValidationException: Jwt expired at ...` while the token looks fresh | Clock drift between issuer and verifier, or `exp` written in milliseconds | Check NTP on both hosts; print `exp` and compare with `Instant.now().getEpochSecond()` |
| `JwtException: Signed JWT rejected: Another algorithm expected, or no matching key(s) found` | `kid` not in the cached JWK Set, or the issuer rotated and the cache has not refreshed | Fetch the JWKS manually and compare `kid` values; confirm the decoder is a singleton with a live cache |
| `IllegalArgumentException: Illegal base64 character 5f` | Decoding with `Base64.getDecoder()` instead of `getUrlDecoder()` | Use the URL decoder; `5f` is `_` |
| `400 Bad Request` with an HTML body, nothing in the application log | Token exceeded a proxy or container header limit | Measure the token length; shrink claims or raise limits at **every** hop |
| User appears logged out immediately after login, only some accounts | Token in a cookie exceeded 4096 bytes and the browser silently dropped it | Shrink the token or move to a server-side session reference |
| Token from another service is accepted | No audience validator registered | Add a `JwtClaimValidator` on `aud`; it is never automatic |
| Every token fails right after a deployment | `jwk-set-uri` or `issuer-uri` points at the wrong environment | Fetch the discovery document and compare `issuer` and `jwks_uri` |
| Verification fails for exactly the tokens signed since 09:00 | Key rotated, resource server cache still holds only the old `kid` | Confirm refresh-on-unknown-`kid` is in effect; check for a per-request decoder blocking the cache |
| ECDSA tokens fail with an opaque signature error, RSA tokens work | Raw `R‖S` JOSE signature passed to a JCA `Signature` expecting DER | Use the library's ECDSA verifier; do not hand-roll |
| Revoked user still has access for up to an hour | Access token lifetime is the revocation window | Shorten the lifetime; add a denylist or introspection — see file 25 |
| `InvalidBearerTokenException` with no detail in the response body | RFC 6750 responses carry the reason in `WWW-Authenticate`, not the body | Read the response header; see file 24 |

---

## Interview Q&A

### Q1. Walk me through exactly what happens when a resource server verifies a JWT, from the raw string to an authenticated principal.

<details>
<summary>Show answer</summary>

Seven steps, and the ordering of the first four is the part that matters.

**1. Extract.** The bearer token is pulled from the `Authorization` header. Spring's
`DefaultBearerTokenResolver` requires the `Bearer ` prefix and by default refuses tokens in
query parameters or form bodies, because a token in a URL lands in access logs, browser history,
and `Referer` headers.

**2. Split and parse structurally.** The string is split on dots — three segments is a JWS, five
is a JWE, anything else is malformed. The header is base64url-decoded and parsed as JSON. At
this point we know the claimed `alg` and `kid`, and **we trust neither**.

**3. Select a key using our configuration, not the token's instruction.** The claimed `alg` is
compared against the configured accepted set. If it is not in the set, verification fails
immediately with no key lookup at all. If it is, `kid` becomes a match criterion against the
in-memory JWK Set, with a rate-limited refetch on a cache miss.

**4. Verify the signature** over the exact bytes `base64url(header) + "." + base64url(payload)`.
Nothing before this point may influence any decision.

**5. Decode the payload** into a claims map. Only now do the claims exist as trusted data.

**6. Validate claims.** `exp` and `nbf` against the clock with the configured skew, `iss` against
the expected issuer, and whatever else you registered — audience, required claims, token type. In
Spring this is a `DelegatingOAuth2TokenValidator` running an ordered list, reporting *all*
failures rather than only the first.

**7. Convert to an `Authentication`.** `JwtAuthenticationConverter` maps claims to granted
authorities and produces a `JwtAuthenticationToken` for the `SecurityContext`, so that
`authorizeHttpRequests` rules and `@PreAuthorize` can act on it.

In the steady state this involves **no call to the authorization server**, which is the entire
value proposition — and also why revocation is hard.

**Counter-question: step 3 says "compare `alg` against a configured set". Why is dispatching on it so dangerous when the signature still has to verify?**

Because dispatching on `alg` lets the attacker choose which *verification function* runs, and
some verification functions are trivially satisfiable.

The extreme case is `alg:none`, where the verification function is "return true" — there is no
signature to forge because none is required.

The interesting case is algorithm confusion. The server is configured for `RS256` and holds an
RSA public key. The attacker changes the header to `HS256` and computes an HMAC over the token
using that public key — which is not secret, it is published at your JWKS endpoint — as the HMAC
secret. A naive implementation reasons "the algorithm is HS256, so call `verifyHmac(token, key)`
with the key I have", and the HMAC checks out because the attacker used the same key.

The root error is treating "the key" as a shapeless blob. An RSA public key and an HMAC secret
are different kinds of thing and must never be interchangeable. Spring and Nimbus enforce that
structurally: `JWSVerificationKeySelector` filters allowed algorithms *first*, then resolves keys
of the matching type from the JWK source, so the vulnerable code path does not exist.

**Counter-question: step 4 verifies over the encoded bytes rather than the parsed JSON. Why does that distinction matter?**

Because JSON has no canonical form, so re-serialising produces different bytes.
`{"alg":"HS256","typ":"JWT"}` and `{"typ":"JWT", "alg":"HS256"}` are the same object and
different byte sequences; key order, whitespace, Unicode escaping, and number formatting all vary
by serialiser. If the signature covered re-serialised JSON, a token signed by a Go issuer would
routinely fail in a Java resource server.

The security consequence is that the verifier must keep the original segments and must not "clean
up" the token before verifying — no trimming, no re-encoding, no normalising. A gateway that
normalises a token is a gateway that breaks every signature.

The operational consequence is that you cannot modify a token in any way, not even adding a
claim, without re-signing it. Anything that wants to enrich a token must be the issuer.
</details>

### Q2. Which JWT claims does Spring Security validate out of the box, and which one is the dangerous omission?

<details>
<summary>Show answer</summary>

Out of the box, with `spring.security.oauth2.resourceserver.jwt.issuer-uri` set, you get
exactly two things: **`exp` and `nbf`** via `JwtTimestampValidator` with 60 seconds of clock
skew, and **`iss`** via `JwtIssuerValidator` — the latter only because you built the decoder
from an issuer location. Configure with `jwk-set-uri` alone and you do not even get the issuer
check.

The dangerous omission is **`aud`**. Spring Security does not add an audience validator, and
until Boot 3.4's `spring.security.oauth2.resourceserver.jwt.audiences` property there was no
configuration-only way to add one.

Why that matters: audience is the claim that says "this token is for *you*". Without it, every
service trusting the same issuer accepts every token that issuer minted. Consider one Keycloak
realm and thirty microservices. A token minted for the internal wiki — a low-value service a
contractor can get a token for — is a structurally valid token for the payments service. Payments
checks the signature (valid, same realm), `exp` (fine), `iss` (fine), and authorises the request.
The only thing that would have stopped it is the claim nobody checked.

The second, quieter omission is that **`exp` is only validated when present**. A token with no
`exp` passes `JwtTimestampValidator`, because there is nothing to compare. If you consume tokens
you did not mint, add a validator that requires the claim to exist.

**Counter-question: your issuer puts `aud: ["orders-api", "reporting-api"]` on every token so one token works everywhere. Is that fine?**

It is syntactically legal and it defeats the purpose.

A multi-valued `aud` is legitimate when one token genuinely must be presented to several services
in a single logical operation — a batch job calling three APIs. What it means security-wise is
that all listed audiences share a blast radius: compromise any one, capture a token, and you hold
a valid credential for all the others.

Issuing every token to every audience is "no audience validation" wearing a costume. The question
I would ask is what the list is *for*. If the answer is "so we do not have to think about which
service a token targets", the right fix is an access token per resource, which is what the OAuth2
`resource` and `audience` request parameters exist for.

The compromise I would accept is grouping by trust tier: one audience for customer-facing read
APIs, a separate one for anything that moves money or changes entitlements. That keeps the count
manageable while ensuring a leaked read token is not a write credential.

**Counter-question: if two validators fail at once, do you get one error or both?**

Both, and it is a deliberate design detail.

`DelegatingOAuth2TokenValidator` runs every delegate and accumulates the errors rather than
short-circuiting:

```java
public OAuth2TokenValidatorResult validate(T token) {
    Collection<OAuth2Error> allErrors = new ArrayList<>();
    for (OAuth2TokenValidator<T> validator : this.tokenValidators) {
        allErrors.addAll(validator.validate(token).getErrors());
    }
    return OAuth2TokenValidatorResult.failure(allErrors);
}
```

Good for debugging: a token that is both expired and for the wrong audience tells you both facts
in one log line rather than sending you round the loop twice.

One caution — the *response* must not carry that detail. RFC 6750 puts a coarse `error` and
`error_description` in the `WWW-Authenticate` header, and it should stay coarse. Telling an
attacker precisely which of five checks failed is a free oracle for probing your configuration.
Log the detail, return `invalid_token`.
</details>

### Q3. Explain `kid` — what it is for, and why it has its own family of vulnerabilities.

<details>
<summary>Show answer</summary>

`kid` is a key identifier in the JOSE header. It answers "which of the issuer's published keys
signed this token", and its reason for existing is **rotation**.

Without `kid`, a verifier holding several public keys must trial-verify against each until one
succeeds. That works, but it costs a signature verification per candidate key on every request
and makes failure ambiguous — you cannot distinguish "wrong key" from "forged token". With `kid`
the lookup is a map access and failure is precise.

Rotation then becomes a three-phase zero-downtime operation: publish the new key alongside the
old and let caches pick it up, switch signing to the new key, and remove the old one only after
the last token signed with it has expired. Resource servers need no deployment and no
coordination.

The vulnerability family exists because of one fact: **`kid` is a free-form string chosen by
whoever composed the token, which includes an attacker.** RFC 7515 imposes no constraint beyond
"case-sensitive string". Implementations that use it as anything other than a lookup key into a
trusted set hand over a primitive:

- **Path traversal.** Reading the key from `keys/{kid}.pem` accepts
  `"kid": "../../../../dev/null"`, making the key material zero bytes — then set `alg` to
  `HS256` and HMAC with an empty secret. Attackers also target files with known stable contents,
  such as `/proc/sys/kernel/randomize_va_space`, which reliably contains `2`.
- **SQL injection.** `SELECT secret FROM keys WHERE kid = '<kid>'` accepts
  `"kid": "x' UNION SELECT 'attacker-chosen-secret'--"`, and now the attacker knows the secret
  because they supplied it.
- **Command injection**, where `kid` reaches a shell invoking an external key tool.

Every one is a hand-rolled-verifier bug, and the defence is a single rule: `kid` is a key into an
in-memory map obtained from a source you configured. It never reaches a filesystem, a database,
or a process.

**Counter-question: a token arrives with a `kid` you do not recognise. What should happen?**

Refresh the key set **once**, then reject if it is still unknown — and "once" is doing a lot of
work.

The legitimate case is rotation: the issuer started signing with a new key and your cache is five
minutes stale. Rejecting outright would cause a burst of failures on every rotation, which is the
problem `kid` was meant to solve, so a forced refresh is correct.

The illegitimate case is a flood of tokens with random `kid` values. If each triggers a JWKS
fetch you have handed the attacker an amplified denial-of-service against your own authorization
server — and that server going down takes out logins everywhere, not just this service.

Nimbus's `JWKSourceBuilder` handles it with a rate limiter, so a flood collapses into one fetch.
That is precisely why I would use the library's caching rather than writing "if unknown kid,
fetch" by hand. I would also alert on the rate of unknown-`kid` rejections: a trickle around a
rotation is normal, a sustained spike is a misconfigured client or someone probing.

**Counter-question: the token has no `kid` at all. Now what?**

Two acceptable behaviours, chosen by how much control I have over the issuer.

If I control the issuer, I require `kid` and reject tokens without it. It costs nothing to emit
and removes the ambiguity permanently.

If I consume a third-party issuer that does not emit it, trial verification against every key in
the set is the specified fallback and is what Nimbus does — the `JWKMatcher` simply has one fewer
criterion, so several keys match and each is tried. The costs are bounded: a few extra signature
verifications per request, and no way to distinguish "wrong key" from "forgery" in the logs. With
two or three keys that is fine; with twenty it is a performance problem and a sign the issuer is
not retiring old keys.

What I would not do is fall back to "use whichever key is first in the set". That works right up
until a rotation and then fails intermittently, for exactly the tokens signed with the new key,
depending on JWKS ordering.
</details>

### Q4. A colleague proposes storing the user's full permission list in the JWT so services never have to call the user service. Argue both sides.

<details>
<summary>Show answer</summary>

**The case for it** is genuine. It removes a network call from every authorization decision in
every service, removes a hard runtime dependency so a user-service outage is not a total outage,
and makes each resource server independently verifiable — which is the reason to choose JWTs at
all. It also gives a consistent view: every service sees the same permissions for the life of the
token, so you do not get a request authorised in one service and denied in another because a
change landed between two lookups.

**The case against it** comes in four parts, and all four eventually bite.

*Staleness.* Permissions are frozen at issue time. Revoke one and the user keeps it until the
token expires — a fifteen-minute window during which a fired employee retains write access, and
worse the longer the token. This is the strongest argument, because it is a security property
rather than an inconvenience.

*Size.* Permission lists grow without bound and nobody notices until they cross a proxy header
limit, producing a 400 that never appears in the application log. The failure correlates with
seniority, because the people with the most group memberships escalate loudest.

*Exposure.* The payload is readable by anyone holding the token, so shipping the permission model
to the client publishes your authorization structure. The mere existence of
`finance.payroll.export` and `admin.impersonate` tells an attacker what to go looking for.

*Coupling.* Every service now depends on the issuer's permission vocabulary, so renaming a
permission is a coordinated change across the authorization server and every consumer — and it
cannot be done in one deployment, because tokens carrying old names are still in flight.

**Where I land:** put **coarse, stable, slow-changing** claims in the token — roles or scopes, a
handful of values describing what kind of actor this is. Resolve **fine-grained, resource-specific**
permissions server-side from `sub` with a short-TTL cache. "Can this user act as an administrator"
belongs in the token; "can this user edit document 4471" does not, and would not fit anyway.

**Counter-question: fine-grained checks then need the permission service on every request. Haven't you just reintroduced the coupling you were avoiding?**

Partly, and the honest framing is that I moved the coupling somewhere it can be managed rather
than eliminating it.

The mitigations are ordinary and effective. Cache the permission set per user with a thirty- to
sixty-second TTL, which collapses almost all traffic to a cache hit while keeping staleness
bounded and — critically — *tunable at runtime* rather than fixed at issue time. Serve stale on
failure so a permission-service blip degrades rather than fails. Push invalidation events so a
revocation takes effect in seconds instead of waiting out the TTL.

The difference from a token claim is control. With a cache, the staleness window is a
configuration value I can shorten during an incident and an invalidation event can clear
immediately. With a claim, the staleness window *is* the token lifetime, and nothing can shorten
it short of a denylist — which is server-side state, which is what the claim was avoiding.

So the trade is not "coupling versus no coupling". It is "a dependency I can cache, degrade, and
invalidate" versus "a security window I cannot close".

**Counter-question: how would you migrate a system that already has fat tokens?**

Incrementally, with the token as fallback rather than source of truth, because a flag-day switch
makes every in-flight token wrong at once.

First, stand up the permission endpoint and cache, and have resource servers call it *in parallel
with* reading the claim, comparing the two and emitting a metric on disagreement. That runs in
production with no behaviour change and tells you whether the new path is correct, which no
amount of testing will.

Second, once disagreement has been at zero long enough to cover the edge cases, flip the decision
to the service and keep the claim as an emergency fallback behind a feature flag.

Third, stop emitting the claim. Token size drops immediately, and only now do you get the
security benefit, because until the claim is gone an old token still carries it.

Fourth, after the maximum token lifetime has elapsed, delete the fallback code.

The ordering follows the same principle as key rotation: tokens outlive deployments, so any
change to what a token *means* must tolerate the old and new forms circulating simultaneously for
at least the maximum token lifetime.
</details>

### Q5. Your `HS256`-signed tokens have been in production for two years with the secret in `application.yml`, which is in Git. Assess and remediate.

<details>
<summary>Show answer</summary>

**Assessment first: this is a full compromise of the authentication system and should be handled
as an incident, not a backlog item.**

With `HS256`, the verification key and the signing key are the same value. Anyone who has ever
had read access to that repository — current staff, former staff, contractors, any fork, any CI
system that cached it — can mint a token with arbitrary `sub`, arbitrary roles, and an arbitrary
expiry. Not steal a session: *mint* one, for any user, including one that does not exist.

Two aggravating factors. Git history means rotating the file does not help retroactively; the
value is in every clone forever. And because forged tokens are cryptographically
indistinguishable from legitimate ones, **you cannot tell from your logs whether this has been
exploited**. The honest statement to leadership is "we have no evidence of abuse and no mechanism
that would have produced evidence."

**Immediate containment**, in order:

1. Generate a new 256-bit secret from `SecureRandom`, hold it in a KMS or Vault, inject at
   runtime. Never in a file, never in the image.
2. Rotate. Because the old secret is compromised, accepting old tokens during a graceful
   transition means accepting forgeries — so this is a hard cut and every user is logged out.
   That is the correct call and it needs to be communicated, not avoided.
3. Purge from Git history with `git filter-repo` or BFG, force-push, and rotate every other
   secret that shared the file. Treat history rewriting as damage limitation, not a fix; assume
   every clone still has it.
4. Audit for damage you cannot see: administrative actions, permission grants, account creations,
   and data exports over the retained window, correlated with unusual source addresses.

**Structural remediation**, because rotating the secret leaves the design intact:

*Move to `RS256` or `ES256`.* The issuer holds a private key and resource servers hold only
public keys, so a leaked public key is a non-event. That converts "secret in Git" from a total
compromise into an embarrassment, and the change in blast radius is the real fix.

*Publish a JWKS endpoint* so rotation is routine, scheduled, and zero-downtime rather than
incident response. A key you rotate quarterly by habit is a key whose rotation you know works.

*Add secret scanning* to pre-commit hooks and CI, failing the build on high-entropy strings and
known key formats. That is the control that prevents recurrence; everything else cleans up.

*Make tokens short-lived* so any future exposure has a bounded window.

**Counter-question: the business says a forced logout of all users is unacceptable. What do you offer?**

I would state the trade plainly and then offer the least-bad option rather than refuse.

The trade: continuing to accept tokens signed with the compromised secret means continuing to
accept forged tokens. There is no scheme that accepts old signatures from real users but not from
an attacker, because they are the same signatures.

What I can offer is a window that is short and monitored rather than absent. Accept the old
secret for exactly one access-token lifetime — fifteen minutes, not a day — while issuing
everything new under the new secret, and log every verification that succeeds under the old key
with `sub` and source address, with someone watching. Then remove it.

That only works if access tokens are short-lived. If they last twenty-four hours, "one token
lifetime" is a day of accepting forgeries, and the forced logout is plainly cheaper.

I would also make sure the decision is recorded as a business decision with a named owner. My job
is to state the risk accurately; the business may accept it, but knowingly.

**Counter-question: how do you move from `HS256` to `RS256` without a big-bang cutover?**

Verify-before-sign, exactly the pattern from the password-encoder migration in file 03.

*Release A:* resource servers accept **both** `HS256` with the existing secret and `RS256`
against a new JWKS endpoint. The authorization server publishes the RSA public key but keeps
signing with `HS256`. Nothing changes behaviourally. Confirm the rollout across every instance of
every service — genuinely every one, because a single stale instance produces intermittent 401s
that are miserable to diagnose.

*Release B:* the authorization server switches to signing `RS256`. Existing `HS256` tokens keep
working because resource servers still accept them.

*Release C:* after the maximum `HS256` token lifetime has elapsed, remove `HS256` everywhere and
destroy the shared secret.

The subtlety: during release A the `HS256` path is still there and the secret is still shared, so
the attack surface is wider. That is acceptable for a short planned window and not acceptable
permanently, which is exactly what happens when nobody owns release C. Put a date on it and track
it as a deliverable.

This is only safe because both algorithms are explicitly *configured* as accepted and each is
bound to its own key type. It is emphatically not "let the token's `alg` choose", which is the
confusion attack — Nimbus's key selector enforces the binding, so an `HS256` header can never
resolve the RSA key.
</details>

### Q6. Design question — design the token format for a platform with a web SPA, mobile apps, third-party API partners, and forty internal services across three regions.

<details>
<summary>Show answer</summary>

I would start by refusing to design one token format, because those four consumers have genuinely
different requirements and forcing them into one shape produces a lowest-common-denominator token
that is bad for all of them.

**Signing: `ES256`, asymmetric, JWKS per region.** Asymmetric is non-negotiable at forty
services — `HS256` would put the signing secret in all forty, so compromising the least-defended
one mints tokens for the most sensitive. `ES256` over `RS256` because 64 bytes against 256 saves
roughly 280 characters on every request, which at this scale is real bandwidth and real
header-limit headroom. I would confirm the partners' libraries handle `ES256` first; if a
significant partner cannot, they get `RS256` from a separate audience and I accept the size cost
for that segment only.

**Claims: small and stable.**

```json
{
  "iss": "https://auth.example.com",
  "sub": "u_01HQ8X",
  "aud": "orders-api",
  "exp": 1771234567,
  "iat": 1771233667,
  "jti": "01HQ8XK3M",
  "scope": "orders:read orders:write",
  "acme_tenant": "t_4471",
  "acme_tv": 7
}
```

`jti` is mandatory because it is the handle for denylisting and for correlating a token across
service logs. `acme_tv` is a token version counter checked against a cache — the revocation
mechanism from file 25. No email, no name, no group list, no permissions, and namespaced private
claims so a later federation does not collide.

**Audience per service group, not per service.** Forty audiences is unmanageable and one is
meaningless, so I would group by trust tier: `public-read`, `customer-write`, `internal`,
`money-movement`. A read-tier token is not a payment-tier credential, which is the property that
matters.

| Consumer | Access token | Refresh | Storage |
|---|---|---|---|
| Web SPA | 5 min | Rotating, `HttpOnly` `Secure` `SameSite=Lax` cookie | Access token in memory only |
| Mobile | 15 min | Rotating, in Keychain / Keystore | Secure platform storage |
| Third-party partner | 60 min | Client credentials, no refresh token | Server-side, their problem |
| Service to service | 5 min | None — re-request with client credentials | In memory |

The SPA gets the shortest lifetime and strictest storage because it is most exposed to
cross-site scripting. Partners get longer tokens because they are servers, not browsers, and the
round-trip cost matters more than the marginal revocation window. **For the SPA I would use a
backend-for-frontend**: the browser gets an `HttpOnly` session cookie and never sees a token at
all, which removes the "where do I store a JWT in a browser" problem rather than choosing the
least-bad answer to it.

**Regions.** One logical issuer and one `iss` value, regional JWKS endpoints behind the same
hostname via latency routing, and the **same key material replicated** rather than per-region
keys — per-region keys mean a token minted in Frankfurt fails in Virginia during a failover, at
exactly the moment you least want extra failure modes. The private key lives in a KMS with
cross-region replication and never leaves it.

**Rotation:** quarterly, automated, three-phase — publish, switch, retire after the maximum token
lifetime plus the cache TTL. Automated because a rotation procedure you have never run is a
procedure that does not work.

**Validation every service must perform**, enforced by a shared starter rather than
documentation: pinned algorithm, `iss` exact match, `aud` containing this service's tier, `exp`
and `nbf` with 30 seconds of skew, `typ` of `at+jwt`, `jti` present, and `acme_tv` matching the
cached token version for that subject.

**Counter-question: why a shared library rather than each team configuring their own resource server?**

Because the default configuration is insecure in a specific, silent way, and asking forty teams
to remember the same non-default step will fail.

Concretely: Spring Security does not validate `aud` unless you add a validator. A team that sets
`issuer-uri`, sees tokens verifying, and ships has no audience check — and nothing fails. No
error, no warning, no failing test. The gap is visible only if someone deliberately presents a
token from another service, and nobody does that by accident.

A shared auto-configuration makes the correct behaviour the *default* and requires an explicit,
reviewable opt-out to deviate. It also gives one place to add `typ` validation or a new
revocation check when the standard moves, rather than forty tickets.

The cost is real and I would name it: a shared library is a coupling point and a potential single
point of failure for everyone's ability to deploy. I would keep it deliberately thin —
configuration and validators, no business logic — version it properly, and let a service pin an
older version rather than upgrade in lockstep.

I would also back it with detection rather than trusting the library: a periodic conformance job
that presents a deliberately wrong-audience token to every service and alerts on any 200. That
tests the running system, which is the only thing that proves the control is in place.

**Counter-question: a partner reports that some of their requests get 400 from your load balancer and never reach your API. Diagnose.**

An infrastructure-level rejection with nothing in the application log points almost immediately
at a header size limit, and the fact that it affects *some* requests rather than all tells me
the tokens differ in size.

My first question is whether it correlates with specific users rather than with load or endpoint.
If certain partner users always fail and others never do, that is size, not a transient.

I would capture the `Authorization` header length for a failing case — from the partner's side,
since it never reaches us — and compare against the ALB's roughly 16 KB single-header limit and
whatever the intervening proxies enforce. Then find what varies. It is almost always a claim
populated from a directory: group membership, entitlements, or a `roles` array that grew.

The fix is to remove the unbounded claim rather than raise the limit. Raising limits means
changing every hop — load balancer, CDN, reverse proxy, container — and missing one reproduces
the bug somewhere harder to find; and the claim will keep growing, so an increase buys time
rather than solving anything.

The preventive control is a size check at issue time: if a token exceeds a budget well below the
tightest downstream limit, fail loudly at the authorization server where someone will see it
rather than silently at a proxy an hour later. A bounded-size token is a property only the issuer
can guarantee.
</details>

---

## Quick Recall

```
JOSE FAMILY
  JWT 7519 claims only, NOT a wire format   JWS 7515 signed, 3 parts / 2 dots
  JWE 7516 encrypted, 5 parts / 4 dots      JWK 7517 key as JSON, JWKS = {"keys":[...]}
  JWA 7518 the alg registry                 BCP 8725 the rules fixing the 2015 mistakes

COMPACT SERIALISATION
  base64url(header) . base64url(payload) . base64url(signature)
  signed input = the ENCODED first two segments joined by a dot (NEVER re-serialise)
  every JWS starts "eyJ"; 2 dots = JWS, 4 = JWE, empty 3rd segment = alg:none

BASE64URL != BASE64
  62 -> '-' not '+',  63 -> '_' not '/',  padding '=' STRIPPED
  Base64.getUrlDecoder() CORRECT   Base64.getDecoder() throws on '-' / '_'

HEADER PARAMS
  alg  NEVER dispatch on it - pin the accepted set server-side
  kid  map lookup ONLY (never a path, never SQL, never a shell)
  typ  JWT / at+jwt (RFC 9068)      cty  JWT = nested
  jku jwk x5u x5c = "attacker picks the key" -> NEVER honour from the token

REGISTERED CLAIMS   (NumericDate = SECONDS, not millis)
  iss  validated ONLY if the decoder was built from an issuer location
  sub  unique per ISSUER -> key on (iss, sub)
  aud  *** NOT VALIDATED BY DEFAULT - the real gap ***
  exp nbf  JwtTimestampValidator, 60 s skew; an ABSENT exp PASSES
  iat  parsed, not enforced        jti  handle for denylist / replay detection

ALGORITHMS
  HS256 shared secret, 32 B  -> verifier can FORGE -> monolith only, >=256-bit secret
  RS256 RSA, 256 B           -> interop default, big
  PS256 RSA-PSS, 256 B       -> prefer over RS256 for new RSA
  ES256 EC P-256, 64 B       -> size + speed default; JOSE uses raw R||S, not DER
  EdDSA Ed25519, 64 B        -> fastest, deterministic, patchier support
  none                       -> NEVER

SIZE LIMITS  (failure is a 400 your app never logs)
  Tomcat 8 KB   nginx 4 x 8 KB   Apache 8190   ALB 16 KB/header, 64 KB total
  browser cookie 4096 B -> SILENTLY DROPPED
  cause is almost always a group / permission list in the payload

JWKS + ROTATION
  publish -> switch signing -> retire ONLY after max lifetime + cache TTL
  cache ~5 min; forced refresh on UNKNOWN kid, RATE LIMITED (else self-DoS)
  outage-tolerant cache serves stale keys through a JWKS outage
  never fetch JWKS per request

ATTACKS -> DEFENCE
  alg:none                  pin algorithms server-side
  RS256 -> HS256 confusion  pin alg AND bind key type to alg
  kid traversal / SQLi      map lookup only, never a path or query
  jku / x5u / jwk           ignore entirely
  weak HMAC secret          32 bytes SecureRandom, or go asymmetric
  missing exp / aud         add validators; REQUIRE the claims to be present
  sign-then-read            NO claim influences ANYTHING before verification returns
  replay                    short exp + TLS + DPoP / mTLS binding

SPRING DEFAULTS
  JwtValidators.createDefault()   -> timestamps ONLY
  createDefaultWithIssuer(iss)    -> timestamps + issuer
  NimbusJwtDecoder default alg    -> RS256 only
  audience                        -> you add it, always
```

---

**Previous:** [`22_M6_T3_CORS.md`](22_M6_T3_CORS.md) ·
**Next:** [`24_M7_T2_JWT_Spring_Integration.md`](24_M7_T2_JWT_Spring_Integration.md)
