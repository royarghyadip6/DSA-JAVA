# 10.1 — OAuth2 & OpenID Connect Fundamentals

> **Module 10 · Topic 1** · OAuth2 & OpenID Connect
> Baseline: Spring Security 6.x on Boot 3.x
> New to this? Read **In Plain English** below first, then come back to the Version Matrix.

---

## Version Matrix

| Concern | Spring Security 5.x | **Spring Security 6.x (baseline)** | Spring Security 7.x |
|---|---|---|---|
| Baseline | `javax.*`, Java 8 | **`jakarta.*`, Java 17** | `jakarta.*`, Java 17+ |
| Implicit grant | `AuthorizationGrantType.IMPLICIT` exists, deprecated from 5.5; `oauth2Login` never supported it | **not in the supported grant set; nothing in the client stack initiates it** | absent; OAuth 2.1 removes it |
| Password grant (ROPC) | `AuthorizationGrantType.PASSWORD`, `OAuth2PasswordGrantRequest`, `PasswordOAuth2AuthorizedClientProvider` | **present but deprecated** | **removed — the classes are deleted** |
| PKCE | supported from 5.2; automatic when the client authentication method is `none` | **automatic for public clients; opt-in for confidential via `OAuth2AuthorizationRequestCustomizers.withPkce()`** | PKCE is the default posture; OAuth 2.1 requires it for all clients |
| Device Authorization Grant | not available in the client stack | **`AuthorizationGrantType.DEVICE_CODE` + `DeviceCodeOAuth2AuthorizedClientProvider` (6.1+)** | same |
| Token Exchange (RFC 8693) | not available | **added in 6.3 (`TokenExchangeOAuth2AuthorizedClientProvider`)** | same |
| Issuer discovery | `ClientRegistrations.fromIssuerLocation`, `JwtDecoders.fromIssuerLocation` | **same, plus `NimbusJwtDecoder.withIssuerLocation()` (6.1+)** | same |

The consequential row is the **removal of the password grant in 7.0**. If your code exchanges a
username and password at the token endpoint, that is a compile failure on upgrade, not a warning.

---

## Why This Exists

Before OAuth2 there was the **password anti-pattern**: to let a printing service reach your
photos, you gave it your photo-host password. That granted unlimited scope, unlimited duration,
no accountability — every action looked like yours in the audit log — and it trained users to
type credentials into arbitrary third-party forms, which is exactly what phishing relies on.

OAuth2 replaces "here is my password" with **"here is a narrow, expiring, revocable token that
lets you do one thing on my behalf"**. Its complexity is the cost of performing that delegation
safely through a browser, an environment full of redirects, history, referrers and hostile script.

> **OAuth2 is a delegated *authorization* protocol. It was never an authentication protocol.**
> OpenID Connect is a thin standardised layer on top that adds authentication — and it exists
> precisely because everyone kept misusing OAuth2 for login and getting it wrong.

Every other statement in this file follows from that one.

---

## In Plain English

**The one-line version:** OAuth2 is a way to let one application do a specific, limited thing in
another application on your behalf without ever learning your password, and OpenID Connect is a
small addition on top that also tells the first application, reliably, who you are.

**An analogy.** Imagine you rent a storage unit, and you have hired a removals firm to collect a
box from it. The dangerous way to arrange this is to hand the firm your own key and your own
identity documents. They can now open your unit whenever they like, forever, and everything they
do inside is indistinguishable from something you did.

What actually happens at a well-run facility is different. You walk into the facility's own office
— not the removals firm's office — and the staff there check who you are. You say "please issue a
pass that opens unit 14, valid today, for this firm." The office prints a pass and the firm carries
it. The guard at the gate examines the pass, not the firm's honesty; the pass itself says what it
opens and when it stops working. If you change your mind, you call the office and cancel the pass,
in one place, without changing your own key.

That pass is an **access token**. The office is the **authorization server**, the guard at unit 14
is the **resource server**, the removals firm is the **client**, and you are the **resource owner**.
The whole reason for the ceremony is that your key and your identity documents never leave your
hands, and the pass is narrow enough that handing it to a stranger is acceptable.

Now add one more thing. The removals firm also wants to know that it really was you who ordered the
collection, not somebody pretending to be you. So the office additionally hands them a signed,
sealed letter, addressed to that firm by name, saying "Alice attended in person at 10:42, we
checked her photo identity document and she also used her phone to confirm." That letter is the
**ID token**. It is a receipt about an identity check, not a pass — you cannot open any door with
it, and it is addressed to one specific firm. Mixing the two up is the single most common and most
serious mistake in this whole area, which is why the file keeps coming back to it.

**How it actually works, step by step.**

Nothing sensitive is exchanged in the first step. Your application simply sends the browser off to
the authorization server's own website, with a handful of parameters in the URL: which client is
asking (`client_id`), where to come back to afterwards (`redirect_uri`), what is being requested
(`scope`, for example `openid profile email`), and two random values called `state` and `nonce`
whose purpose is explained below. Because this is an ordinary redirect visible in the address bar,
the user can see whose login page they are on. That matters: it is the reason phishing a password
into a random third-party form is no longer necessary.

The user then logs in and, if the provider asks for it, approves the request. All of this happens
on the authorization server's pages, which is why things like multi-factor authentication,
fingerprint login, and "unusual location detected" checks are possible at all. Your application is
not involved and does not see the password.

The authorization server then sends the browser back to your callback URL carrying a short-lived
one-use **authorization code** — something like `code=SplxlOBeZQQYbYS6WxSbIA`, typically valid for
thirty to sixty seconds. A code is not a token. On its own it is useless, because redeeming it
requires a second, separate request that the browser is not part of.

Your server makes that second request directly to the authorization server, machine to machine,
over TLS. It sends the code and proves it is entitled to redeem it, and it gets back the actual
tokens: an access token, usually a refresh token, and for OpenID Connect an ID token. The point of
this two-step structure is that the token never travels through a URL, so it never lands in browser
history, never appears in a `Referer` header, and is never readable by JavaScript on the page.

Two of the random values deserve their own explanation, because they are constantly confused. The
`state` value is generated by your application and stored against the user's browser session; when
the callback arrives you check that the returned `state` matches what you stored. That proves this
callback belongs to a flow *this browser* started, and it is what stops an attacker completing
their own login and then handing the resulting callback URL to you so that you end up logged into
their account. The `nonce` value is different: the authorization server writes it *inside* the ID
token, and you check it there. That proves the ID token was minted for this specific login attempt
and not replayed from somewhere else. Short version: `state` protects the redirect, `nonce`
protects the token.

The third protective mechanism is **PKCE**, pronounced "pixie". Before redirecting, your
application invents a long random string called the `code_verifier` and keeps it private. It sends
only the SHA-256 hash of it, the `code_challenge`, in the visible redirect. The authorization
server remembers the hash alongside the code. When the code is redeemed, your application presents
the original verifier, and the server hashes it and compares. The effect is that somebody who
steals the code cannot use it, because they would have to reverse a SHA-256 hash to produce the
matching verifier. This started as a defence for mobile applications, where another app can
register the same callback and intercept the code, and it is now recommended for every kind of
client.

Finally, a distinction that decides much of the configuration: is your client **confidential** or
**public**? The test is simply whether it can keep a secret from its own user. Code running on your
server can, so it gets a `client_secret`. Code shipped to a phone or a browser cannot — anything in
the bundle is extractable — so it gets no secret at all and relies on PKCE plus exact callback
matching instead. Obfuscating a secret into a mobile app does not make it confidential; it makes
it a published secret.

**Why should a beginner care?** The two most damaging mistakes in this area are both easy to make
and both invisible in testing. The first is treating the access token as proof of identity: your
login code takes whatever token it has, asks the provider "who is this?", and trusts the answer —
which means anybody who can inject a valid token for another user into your flow becomes that user.
The second is sending the ID token to your own API as a credential, which works if the API only
checks the signature, and hands an impersonation tool to anybody holding an ID token for that user
from any other application. Beyond security, this is simply the protocol behind every "sign in
with Google" button and every service integration you will be asked to build, and its error
messages (`redirect_uri_mismatch`, `invalid_grant`, `invalid_client`) are unreadable until you know
which step each one belongs to.

**Words you will meet in this file**

| Term | In plain words |
|---|---|
| Resource owner | The person whose data it is. Usually the human sitting at the browser. |
| Client | The application asking for permission to act on that person's behalf. |
| Authorization server | The system that checks who the user is and issues tokens. It holds the passwords and the signing keys. |
| Resource server | The API that holds the protected data and checks incoming tokens. It holds no passwords. |
| Access token | The pass that lets its bearer perform certain operations on an API. Meant for the API, not for the client to read. |
| ID token | A signed receipt telling the client who logged in, when, and how. Always a JWT, addressed to one client, never used as a pass. |
| Refresh token | A longer-lived token used to obtain new access tokens without asking the user to log in again. |
| Scope | The specific, named permission being requested, such as `orders:read`. Drives the consent screen. |
| Claim | One named field inside a token, such as `sub` (the user identifier) or `email`. |
| Authorization code | A short-lived, single-use value handed back through the browser, which the client exchanges for tokens on a private connection. |
| Grant type | The named procedure used to obtain a token. `authorization_code` is the one you should use when a user is involved. |
| `state` | A random value the client stores against the browser session and checks on the callback, to prove the callback belongs to a flow it started. |
| `nonce` | A random value the client sends and then finds inside the ID token, proving the token was minted for this login attempt. |
| PKCE | Sending only a hash of a private random value up front, and the value itself when redeeming the code, so a stolen code cannot be used. |
| Confidential client | A client that runs somewhere the user cannot inspect, so it can hold a real secret. |
| Public client | A client that runs on the user's device, so it cannot hold a secret and uses PKCE instead. |
| Front channel / back channel | Through the user's browser (visible, less trusted) versus a direct server-to-server call (private, trusted). |
| JWT | A token format made of three base64 pieces: a header, the claims, and a signature. Signed, not encrypted, so the contents are readable by anyone. |
| JWKS | The public keys the authorization server publishes so that anybody can verify its signatures. |
| Discovery / `issuer-uri` | A single well-known URL that lists all the provider's endpoints, so you configure one value rather than six. |
| `aud` (audience) | The claim naming who a token is for. Checking it is what stops a token for one service being replayed against another. |
| `iss` (issuer) | The claim naming which authorization server minted the token. |
| `sub` (subject) | The provider's stable identifier for the user. Unique within one issuer, so the real key is the pair of issuer and subject. |

**If you remember only one thing:** the access token is a pass for an API and the ID token is a
receipt for your own application, and using either one in the other's role is a security
vulnerability rather than a style choice.

---

## Core Concepts

### 1. The Four Roles

**In simple terms:** Four separate parties are involved — the person, the application asking, the
system that checks identity and issues tokens, and the API holding the data — and keeping them
separate is what stops any one of them needing your password.

| Role | Who | Holds | Spring module |
|---|---|---|---|
| **Resource Owner** | the human who owns the data | the actual credentials — never shared with the client | not modelled; it is a person |
| **Client** | the app acting on the owner's behalf | `client_id`, optionally `client_secret`, plus issued tokens | `spring-security-oauth2-client` |
| **Authorization Server (AS)** | authenticates the owner, issues tokens | user database, signing keys, consent records | `spring-security-oauth2-authorization-server` |
| **Resource Server (RS)** | the API holding protected data | validation material only (JWKS or introspection credentials) | `spring-security-oauth2-resource-server` |

Why the separation matters, concretely. **The client never sees the password** — the user
authenticates on the AS's own domain, in the browser's address bar, so a malicious or careless
client cannot capture a credential that never passes through it. **The resource server needs no
user database**, so twenty microservices verify identity without twenty copies of your identity
store, and credential-handling code lives in exactly one place. **Trust is asymmetric**: the RS
trusts the AS's signature, and trusts the client only through what the AS asserted about it
(`azp`, `scope`, `aud`). **Revocation has one owner** — you revoke at the AS rather than chasing
credentials across integrations.

These are **roles, not deployments**. One Spring Boot application can be a resource server for
its own API, an OAuth2 client of a downstream API, and an `oauth2Login` consumer of an external
identity provider at the same time. That is why "should I use `oauth2Login` or
`oauth2ResourceServer`?" is frequently answered with "both, on two filter chains".

### 2. Delegated Authorization, Not Authentication

**In simple terms:** An access token says what its holder may do, not who its holder is, so
building a login feature on one means anybody who can slip you somebody else's token becomes that
person.

An **access token** answers *"may the bearer perform this operation on this resource?"*. RFC 6749
does not require it to contain a subject, does not define its format, and gives the client no
sanctioned way to inspect it. Formally **the access token is not for the client at all** — the
client is a courier; the token is for the resource server.

```java
// BROKEN "login with OAuth2"
String accessToken = exchangeCodeForToken(code);
Map<String,Object> me = restClient.get()
        .uri("https://provider.example.com/api/me")
        .header("Authorization", "Bearer " + accessToken)
        .retrieve().body(Map.class);
loginAs(me.get("id"));   // unsound
```

The reasoning error: "this token works against `/api/me`, and `/api/me` says user 42, therefore
the person at this browser is user 42". But a bearer token binds to nothing — not this browser,
not this application, not this login attempt. Three consequences:

- **Token substitution.** Any valid access token for victim 42 — issued to a *different* client,
  scraped from a log, harvested by a malicious app the victim also used — logs the attacker in
  as the victim if it can be injected into your flow.
- **No freshness.** The token says nothing about when or how the user authenticated. It may have
  been minted from a refresh token days later, so "re-authenticate before this action" is
  impossible to build on it.
- **No standard shape.** Every provider's `/me` differs, so login becomes N bespoke integrations.

Cross-reference: [`02_M1_T2_Authentication_Authorization.md`](02_M1_T2_Authentication_Authorization.md)
establishes that a *scope* is what the **client** was delegated while a *role* is what the
**user** may do, and that the effective permission is their intersection. The delegation model
here is why: a scope is the machine-readable record of the consent this specific client received.
It is also why client-credentials tokens have no meaningful roles — no resource owner, no
delegation, only a service account.

### 3. OpenID Connect — the Authentication Layer

**In simple terms:** This is the standard addition that makes "log in with Google" safe, because
it gives your application a signed statement addressed to it saying who logged in, when, and by
what method.

OIDC is a profile of the authorization code flow adding four things: the reserved **`openid`
scope** (requesting it is what makes the request OIDC), the **ID token**, the **`nonce`**
parameter, and standardised endpoints and claims.

| | Access token | ID token |
|---|---|---|
| `aud` | the resource server / API | **the client** (`aud` = `client_id`) |
| Consumed by | the resource server | the client, once, at login |
| Format | unspecified — opaque or JWT | **always a signed JWT** |
| Answers | "may the bearer do this?" | "who authenticated, when, and how?" |
| Lifetime | minutes to an hour | seconds to minutes; a receipt, not a credential |
| Sent to APIs | yes, `Authorization: Bearer` | **never** |
| Validated by | the RS, against the AS's keys | the client, against the AS's keys *and* its own `nonce` |
| Spring type | `OAuth2AccessToken` / `Jwt` | `OidcIdToken` |

> **An ID token must never be sent to an API as a bearer credential.**

Its `aud` is your `client_id`, a **public** value appearing in every authorization URL. A resource
server that accepts ID tokens accepts a credential whose audience it is not and whose audience
value anyone can discover — and an ID token for the same user issued to *any other client* then
becomes a full impersonation primitive. Frontend teams do this because the ID token is the one
they can decode and read, so it "looks like the useful one".

```json
{
  "iss": "https://accounts.example.com", "sub": "248289761001",
  "aud": "my-web-app-client-id", "exp": 1735689600, "iat": 1735686000,
  "auth_time": 1735685940, "nonce": "n-0S6_WzA2Mj",
  "acr": "urn:mace:incommon:iap:silver", "amr": ["pwd", "otp"],
  "azp": "my-web-app-client-id",
  "email": "alice@example.com", "email_verified": true, "name": "Alice Example"
}
```

`auth_time`, `acr` (authentication context class) and `amr` (authentication methods) are what make
**step-up authentication** possible — demand `amr` containing `otp`, or `auth_time` inside the
last five minutes, before a destructive operation. None of that is expressible with a bare access
token.

| Scope | Claims released |
|---|---|
| `openid` | `sub` — the stable, provider-unique user identifier |
| `profile` | `name`, `family_name`, `given_name`, `preferred_username`, `picture`, `locale`, `updated_at`, and others |
| `email` | `email`, `email_verified` |
| `address` / `phone` | `address`; `phone_number`, `phone_number_verified` |
| `offline_access` | no claims — requests a **refresh token** |

`sub` is unique **within an issuer**, not globally, so the real key for a local user record is the
pair **(`iss`, `sub`)**. Keying on `email` is a security bug, covered in
[`30_M10_T2_OAuth2_Client.md`](30_M10_T2_OAuth2_Client.md).

### 4. Public vs Confidential Clients

**In simple terms:** If your application runs somewhere the user can inspect, such as a phone or a
browser, it cannot hold a password of its own, and that single fact changes how it must be
configured.

The single question is: **can this client keep a secret from its own user?**

| | Confidential | Public |
|---|---|---|
| Examples | server-rendered web app, backend-for-frontend, daemon | SPA, native mobile, desktop, CLI |
| Holds a `client_secret` | **yes**, on a server the user cannot read | **no** — anything shipped to a device is extractable |
| Token endpoint auth | `client_secret_basic`, `client_secret_post`, `private_key_jwt`, `tls_client_auth` | `none` |
| PKCE | strongly recommended; required by OAuth 2.1 | **mandatory** |
| Refresh tokens | long-lived, protected by client authentication | only with **rotation** and reuse detection |
| Spring enum | `ClientAuthenticationMethod.CLIENT_SECRET_BASIC` | `ClientAuthenticationMethod.NONE` |

Minifying a secret into a JavaScript bundle or an Android package does not create a confidential
client. If the secret sits on a device the user controls, **it is public information**; register
the client as public and rely on PKCE plus exact redirect URI matching.

`private_key_jwt` is worth knowing: instead of a shared secret the client signs a short-lived
assertion with its own private key and sends it as `client_assertion`. The AS stores only a public
key, so there is no shared secret to leak from either side.

### 5. The Grant Types, With a Verdict

**In simple terms:** There are several named procedures for obtaining a token, but in practice one
of them is correct whenever a person is involved and another whenever only machines are, and the
rest are either niche or actively removed from the standard.

| Grant | `grant_type` | Verdict | Reason |
|---|---|---|---|
| Authorization Code | `authorization_code` | **the default for anything with a user** | the token never travels through a URL |
| Authorization Code + PKCE | same, plus `code_verifier` | **mandatory for public clients, now recommended for confidential ones** | binds the code to the instance that requested it |
| Client Credentials | `client_credentials` | **machine-to-machine only** | no user, no delegation; the client is the resource owner |
| Refresh Token | `refresh_token` | **yes, rotated for public clients** | keeps access tokens short without re-prompting |
| Device Authorization | `urn:ietf:params:oauth:grant-type:device_code` | **input-constrained devices** | TVs, CLIs, IoT: authenticate on a phone |
| Implicit | `token` response type | **dead — removed in OAuth 2.1** | token in the URL fragment leaks everywhere |
| Password (ROPC) | `password` | **dead — removed in 2.1, deleted in Spring Security 7.0** | reinstates the anti-pattern OAuth2 was invented to kill |

**Implicit** returned the access token in the redirect's URL fragment. Fragments are not sent to
servers, which was the original idea, but the token then lands in browser history, is readable by
every script on the page including analytics and tag managers, is readable by extensions, and can
escape through a `Referer`. There is no code and no client authentication, so nothing binds the
token to the requesting instance and injection is trivial; and refresh tokens could not be issued
safely, which pushed implementations toward long-lived access tokens. Universal CORS support on
token endpoints removed the only justification.

**ROPC** has the client collect the password and post it to the token endpoint. It was a
migration path with a warning attached. It teaches users to type credentials into non-identity-
provider interfaces, makes multi-factor authentication, CAPTCHA, risk-based step-up, passwordless
login, consent and federation impossible because there is no browser and no AS-controlled screen,
puts the raw password in client memory and crash dumps, and hands attackers a clean
credential-stuffing endpoint. Spring Security **deletes** the supporting classes in 7.0; there is
no replacement API because the replacement is a different flow.

**Client credentials** has no resource owner, no browser, no consent:

```http
POST /oauth2/token HTTP/1.1
Host: auth.example.com
Authorization: Basic base64(client_id:client_secret)
Content-Type: application/x-www-form-urlencoded

grant_type=client_credentials&scope=orders%3Aread%20orders%3Awrite
```

**Refresh tokens** are a mechanism, not a user-facing grant. For confidential clients they are
protected by client authentication. For public clients they must be **rotated**: the AS issues a
new one on every use and invalidates the old, so replay of an old token reveals a compromised
family and the whole chain is revoked. That is refresh token reuse detection, and it is mandatory
in the browser-based-apps best current practice.

### 6. The Authorization Code Flow, Step by Step

**In simple terms:** This is the actual sequence of requests: send the browser to the provider,
get a short-lived code back, and swap that code for tokens on a private server-to-server call so
the token never travels through a URL.

**Step 1 — redirect the browser to the authorization endpoint.** A plain `302`; nothing secret.

```
GET https://accounts.example.com/oauth2/authorize
    ?response_type=code
    &client_id=my-web-app
    &redirect_uri=https%3A%2F%2Fapp.example.com%2Flogin%2Foauth2%2Fcode%2Fexample
    &scope=openid%20profile%20email
    &state=xQ7v2mLpR0s9TbKd
    &nonce=n-0S6_WzA2Mj
    &code_challenge=E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM
    &code_challenge_method=S256
```

| Parameter | Purpose | Protects against |
|---|---|---|
| `response_type=code` | asks for a code, not a token | using implicit by accident |
| `client_id` | identifies the client; **public** | nothing — it is an identifier |
| `redirect_uri` | where the code goes | **open redirect and code theft**, but only with *exact string matching* at the AS |
| `scope` | the delegation requested | over-broad grants; drives the consent screen |
| `state` | opaque, unguessable, bound to the session | **CSRF on the callback** and forced-login session fixation |
| `nonce` | opaque, echoed into the ID token | **ID token replay and injection** |
| `code_challenge` / `_method` | PKCE | **authorization code interception** |

**Step 2 — the user authenticates and consents at the AS**, entirely on the AS's domain. This is
where MFA, device checks, risk scoring and consent live, and why ROPC is a dead end.

**Step 3 — the AS redirects back with a code**, short-lived (30–60 seconds) and single-use, in the
**query string**:

```
HTTP/1.1 302 Found
Location: https://app.example.com/login/oauth2/code/example
          ?code=SplxlOBeZQQYbYS6WxSbIA&state=xQ7v2mLpR0s9TbKd
          &iss=https%3A%2F%2Faccounts.example.com
```

The client must compare `state` against the value stored for this browser session. The `iss`
parameter is RFC 9207 issuer identification, the standard mix-up mitigation.

**Step 4 — exchange the code at the token endpoint**, server to server, so nothing can leak
through history, referrers or script:

```http
POST /oauth2/token HTTP/1.1
Host: accounts.example.com
Authorization: Basic bXktd2ViLWFwcDpzM2NyM3Q=
Content-Type: application/x-www-form-urlencoded

grant_type=authorization_code&code=SplxlOBeZQQYbYS6WxSbIA
&redirect_uri=https%3A%2F%2Fapp.example.com%2Flogin%2Foauth2%2Fcode%2Fexample
&code_verifier=dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk
```

The `redirect_uri` is repeated so the AS can confirm it matches step 1 — that check stops a stolen
code being redeemed against a different callback.

**Step 5 — tokens come back**, then **step 6** the client validates the ID token (signature via
JWKS, `iss`, `aud` equal to its own `client_id`, `exp`/`iat`, and `nonce`), establishes its own
session, and optionally calls UserInfo with the **access** token.

```mermaid
sequenceDiagram
    autonumber
    participant UA as Browser
    participant C as Client (your Spring app)
    participant AS as Authorization Server
    participant RS as Resource Server

    UA->>C: GET /dashboard (no session)
    Note over C: code_verifier (43-128 chars, CSPRNG)<br/>code_challenge = BASE64URL(SHA256(verifier))<br/>state + nonce; all stored in the HTTP session
    C-->>UA: 302 /oauth2/authorize?response_type=code&client_id=...<br/>&redirect_uri&scope=openid profile&state&nonce<br/>&code_challenge&code_challenge_method=S256
    UA->>AS: GET /oauth2/authorize?...
    Note over AS: exact-match redirect_uri against the registration<br/>persist code_challenge with the session
    AS-->>UA: login page on the AS domain (AS controls MFA)
    UA->>AS: credentials + second factor + consent
    Note over AS: mint code bound to<br/>(client_id, redirect_uri, code_challenge, nonce, sub)
    AS-->>UA: 302 redirect_uri?code=...&state=...&iss=...
    UA->>C: GET /login/oauth2/code/example?code=...&state=...
    Note over C: compare state with the stored value<br/>mismatch or absent => abort, do not exchange
    C->>AS: POST /oauth2/token (back channel, TLS)<br/>code, redirect_uri, code_verifier + client auth
    Note over AS: SHA256(code_verifier) == stored challenge?<br/>redirect_uri identical to step 1?<br/>code unused and unexpired?
    AS-->>C: 200 {access_token, id_token, refresh_token, expires_in}
    Note over C: validate id_token: signature, iss,<br/>aud == client_id, exp/iat, nonce == stored nonce
    C-->>UA: Set-Cookie: JSESSIONID (rotated) + 302 /dashboard
    C->>RS: GET /api/orders  Authorization: Bearer <access_token>
    Note over RS: validate signature, iss, aud, exp, scope<br/>NEVER accept an id_token here
    RS-->>C: 200 [...]
```

### 7. PKCE

**In simple terms:** The application keeps a private random value and publishes only its hash, so
that whoever ends up holding the stolen code cannot redeem it without the value they never saw.

PKCE ("pixie", RFC 7636) is **Proof Key for Code Exchange**, and it solves **authorization code
interception**. The canonical case is mobile: a native app registers a custom URI scheme, a
malicious app registers the same scheme, and the operating system hands the redirect — with the
code — to the wrong app. A native app is a public client with no secret, so nothing else stands
between a stolen code and a token.

```
code_verifier  = 43-128 chars from [A-Z] [a-z] [0-9] "-" "." "_" "~", from a CSPRNG
                 (32 random bytes base64url-encoded gives exactly 43 chars)
code_challenge = BASE64URL( SHA-256( ASCII(code_verifier) ) )   when method = S256
code_challenge = code_verifier                                  when method = plain
```

The challenge travels the **front channel** (attacker-visible); the verifier travels the **back
channel** at token exchange (invisible). The AS stores the challenge with the code, recomputes the
hash at exchange time, and compares. A thief holding only the code cannot redeem it.

**`plain` must never be used.** If the challenge equals the verifier, anyone who saw the
authorization request already has the verifier. Worse, `plain` is the specification's *default*
when `code_challenge_method` is omitted, so a client that sends a challenge without the method
silently gets the useless variant. Providers advertise support through
`code_challenge_methods_supported`.

**Why confidential clients want it too.** Client authentication proves *who is calling the token
endpoint*; PKCE proves *the caller is the instance that started this flow*. The gap between them
is **authorization code injection**: the attacker runs a legitimate flow with their own account,
obtains a code, and injects it into a victim's session at your callback. Your client authenticates
flawlessly and exchanges the attacker's code, so the victim's browser is now bound to the
attacker's account and everything the victim uploads or types lands there. PKCE stops it, because
the injected code's challenge will not match the verifier in the victim's session. Add the routine
leakage of client secrets into source control and CI logs, and OAuth 2.1 makes PKCE mandatory for
everyone.

```java
DefaultOAuth2AuthorizationRequestResolver resolver =
        new DefaultOAuth2AuthorizationRequestResolver(repo, "/oauth2/authorization");
resolver.setAuthorizationRequestCustomizer(OAuth2AuthorizationRequestCustomizers.withPkce());
```

### 8. Discovery, OAuth 2.1, and the Attack Catalogue

**In simple terms:** Providers publish one URL that describes all their endpoints so you configure
a single line, and the accumulated lessons about what goes wrong have now been folded into a
tightened version of the standard.

```
GET https://accounts.example.com/.well-known/openid-configuration
```

```json
{
  "issuer": "https://accounts.example.com",
  "authorization_endpoint": "https://accounts.example.com/oauth2/authorize",
  "token_endpoint": "https://accounts.example.com/oauth2/token",
  "userinfo_endpoint": "https://accounts.example.com/userinfo",
  "jwks_uri": "https://accounts.example.com/oauth2/jwks",
  "end_session_endpoint": "https://accounts.example.com/oauth2/logout",
  "introspection_endpoint": "https://accounts.example.com/oauth2/introspect",
  "revocation_endpoint": "https://accounts.example.com/oauth2/revoke",
  "device_authorization_endpoint": "https://accounts.example.com/oauth2/device_authorization",
  "response_types_supported": ["code"],
  "id_token_signing_alg_values_supported": ["RS256"],
  "code_challenge_methods_supported": ["S256"],
  "token_endpoint_auth_methods_supported": ["client_secret_basic", "private_key_jwt"]
}
```

Plain OAuth2 has an equivalent at `/.well-known/oauth-authorization-server` (RFC 8414), and
`ClientRegistrations.fromIssuerLocation` tries the OIDC location first. **The `issuer` value in
the document must equal the issuer you asked for** — Spring asserts it, and the assertion is a real
control preventing a metadata document being served from one origin while claiming another. It is
also the commonest cause of "my Keycloak integration will not start": the container knows itself as
`http://keycloak:8080/realms/x` while the browser uses `http://localhost:8080/realms/x`.

**OAuth 2.1** is RFC 6749 plus RFC 6750 plus the accumulated best current practice, with the
dangerous parts deleted: implicit removed, ROPC removed, PKCE required for all clients, redirect
URIs compared by **exact string matching** with no wildcards, bearer tokens in query strings
removed, and refresh tokens sender-constrained or one-time-use. All of that is already the correct
configuration on Spring Security 6.x today; 2.1 describes the present, not a future migration.

| Attack | Mechanism | Mitigation |
|---|---|---|
| **Open redirect** | prefix-matched `redirect_uri`, or a redirector on the client's own origin | exact string matching at the AS; no open redirectors; PKCE so a stolen code is not redeemable |
| **CSRF on the callback** | attacker completes their own flow and feeds the callback URL to the victim, logging the victim into the *attacker's* account | random `state` stored against the session, compared and consumed once |
| **Code injection** | attacker's code injected into the victim's session at the callback | PKCE plus `nonce` validation |
| **Mix-up** | multi-provider client tricked into sending provider A's code to provider B | a **distinct `redirect_uri` per provider**, RFC 9207 `iss`, provider bound into `state` |
| **Token substitution** | a bare access token used as proof of identity | use OIDC; validate `aud` and `nonce` on the ID token |
| **Audience confusion** | a token for service A replayed against service B, which checks only signature and issuer | every resource server validates `aud` — **not on by default**, see [`31_M10_T3_Resource_Server.md`](31_M10_T3_Resource_Server.md) |
| **Scope escalation on refresh** | extra scope requested at refresh time | RFC 6749 requires a subset; verify your AS enforces it |
| **Referrer leakage** | the code sits in the callback URL when an external resource loads | consume the code and redirect immediately; set `Referrer-Policy` |

---

## Working Code

Protocol primitives by hand, to make the mechanics concrete. You would not hand-roll this in
production — [`30_M10_T2_OAuth2_Client.md`](30_M10_T2_OAuth2_Client.md) has the framework
equivalents — but you should be able to.

```java
package com.example.oauth2.fundamentals;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/** RFC 7636 PKCE primitives, mirroring OAuth2AuthorizationRequestCustomizers.withPkce(). */
public final class Pkce {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();

    private Pkce() {
    }

    /** 32 random bytes base64url-encode to 43 characters, the RFC 7636 minimum. */
    public static String createCodeVerifier() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return URL_ENCODER.encodeToString(bytes);
    }

    /** code_challenge = BASE64URL(SHA-256(ASCII(code_verifier))), method S256. */
    public static String createS256Challenge(String codeVerifier) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return URL_ENCODER.encodeToString(
                    digest.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII)));
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 unavailable", ex);   // never in practice
        }
    }

    /** What the authorization server does at token-exchange time. */
    public static boolean verify(String codeVerifier, String storedChallenge) {
        // Constant time: the verifier is attacker-influenced input.
        return MessageDigest.isEqual(
                createS256Challenge(codeVerifier).getBytes(StandardCharsets.US_ASCII),
                storedChallenge.getBytes(StandardCharsets.US_ASCII));
    }

    /** state and nonce need unguessability and nothing else. */
    public static String createOpaqueValue() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return URL_ENCODER.encodeToString(bytes);
    }
}
```

```java
package com.example.oauth2.fundamentals;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController
public class ManualAuthorizationCodeFlowController {

    private static final String AUTHORIZATION_ENDPOINT = "https://accounts.example.com/oauth2/authorize";
    private static final String TOKEN_ENDPOINT = "https://accounts.example.com/oauth2/token";
    private static final String CLIENT_ID = "my-web-app";
    private static final String CLIENT_SECRET = "s3cr3t";
    private static final String REDIRECT_URI = "https://app.example.com/manual/callback";

    private static final String SESSION_STATE = "oauth2.state";
    private static final String SESSION_NONCE = "oauth2.nonce";
    private static final String SESSION_VERIFIER = "oauth2.code_verifier";

    private final RestClient restClient = RestClient.create();

    @GetMapping("/manual/authorize")
    public void startFlow(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String state = Pkce.createOpaqueValue();
        String nonce = Pkce.createOpaqueValue();
        String codeVerifier = Pkce.createCodeVerifier();

        // Binding these three to THIS browser session is the whole security value.
        HttpSession session = request.getSession(true);
        session.setAttribute(SESSION_STATE, state);
        session.setAttribute(SESSION_NONCE, nonce);
        session.setAttribute(SESSION_VERIFIER, codeVerifier);

        response.sendRedirect(AUTHORIZATION_ENDPOINT
                + "?response_type=code"
                + "&client_id=" + enc(CLIENT_ID)
                + "&redirect_uri=" + enc(REDIRECT_URI)
                + "&scope=" + enc("openid profile email")
                + "&state=" + enc(state)
                + "&nonce=" + enc(nonce)
                + "&code_challenge=" + enc(Pkce.createS256Challenge(codeVerifier))
                + "&code_challenge_method=S256");
    }

    @GetMapping("/manual/callback")
    public Map<String, Object> callback(@RequestParam(required = false) String code,
                                        @RequestParam(required = false) String state,
                                        @RequestParam(required = false) String error,
                                        HttpServletRequest request) {
        if (error != null) {
            // RFC 6749 4.1.2.1: access_denied, invalid_scope, server_error, ...
            throw new IllegalStateException("Authorization endpoint returned error: " + error);
        }
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new IllegalStateException("No session; this flow was never started here");
        }

        String expectedState = (String) session.getAttribute(SESSION_STATE);
        String codeVerifier = (String) session.getAttribute(SESSION_VERIFIER);
        // Single use: remove before validating so a replay cannot succeed twice.
        session.removeAttribute(SESSION_STATE);
        session.removeAttribute(SESSION_VERIFIER);

        if (expectedState == null || state == null || !constantTimeEquals(expectedState, state)) {
            throw new IllegalStateException("state mismatch - possible CSRF on the callback");
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("code", code);
        form.add("redirect_uri", REDIRECT_URI);      // byte-identical to step 1
        form.add("code_verifier", codeVerifier);

        @SuppressWarnings("unchecked")
        Map<String, Object> tokenResponse = this.restClient.post()
                .uri(TOKEN_ENDPOINT)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .headers(headers -> headers.setBasicAuth(CLIENT_ID, CLIENT_SECRET))
                .body(form)
                .retrieve()
                .body(Map.class);

        // The id_token must now be validated: signature via JWKS, iss, aud == CLIENT_ID,
        // exp/iat, and nonce == the SESSION_NONCE attribute.
        return tokenResponse;
    }

    private static boolean constantTimeEquals(String a, String b) {
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8),
                                     b.getBytes(StandardCharsets.UTF_8));
    }

    private static String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
```

ID token validation, reusing the framework's decoder rather than reimplementing JWKS handling:

```java
package com.example.oauth2.fundamentals;

import java.util.Objects;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

public class IdTokenVerifier {

    private final JwtDecoder decoder;
    private final String clientId;

    public IdTokenVerifier(String jwkSetUri, String expectedIssuer, String clientId) {
        NimbusJwtDecoder nimbus = NimbusJwtDecoder.withJwkSetUri(jwkSetUri)
                .jwsAlgorithm(SignatureAlgorithm.RS256)
                .build();
        // exp/nbf with the 60s default clock skew, plus iss equality.
        nimbus.setJwtValidator(JwtValidators.createDefaultWithIssuer(expectedIssuer));
        this.decoder = nimbus;
        this.clientId = clientId;
    }

    public Jwt verify(String idTokenValue, String expectedNonce) {
        Jwt jwt = this.decoder.decode(idTokenValue);      // signature + exp/nbf + iss

        if (!jwt.getAudience().contains(this.clientId)) {
            throw new JwtException("id_token aud does not contain this client_id");
        }
        if (jwt.getAudience().size() > 1 && !this.clientId.equals(jwt.getClaimAsString("azp"))) {
            throw new JwtException("multiple audiences require azp == client_id");
        }
        if (!Objects.equals(expectedNonce, jwt.getClaimAsString("nonce"))) {
            throw new JwtException("nonce mismatch - id_token replay or injection");
        }
        return jwt;
    }
}
```

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          example:
            client-id: my-web-app
            client-secret: ${OAUTH_CLIENT_SECRET}
            client-authentication-method: client_secret_basic
            authorization-grant-type: authorization_code
            scope: [openid, profile, email]
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
        provider:
          example:
            # One line replaces authorization-uri, token-uri, jwk-set-uri and user-info-uri.
            issuer-uri: https://accounts.example.com

server:
  # Behind a TLS-terminating proxy the generated redirect_uri would otherwise be http://
  forward-headers-strategy: framework

logging:
  level:
    org.springframework.security.oauth2: DEBUG
```

```java
package com.example.oauth2.fundamentals;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class PkceTests {

    @Test
    void codeVerifierMeetsRfc7636LengthAndAlphabet() {
        String verifier = Pkce.createCodeVerifier();
        assertThat(verifier.length()).isBetween(43, 128);
        assertThat(verifier).matches("[A-Za-z0-9\\-._~]+");
    }

    @Test
    void s256ChallengeMatchesTheRfcTestVector() {
        // RFC 7636 Appendix B.
        assertThat(Pkce.createS256Challenge("dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk"))
                .isEqualTo("E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM");
    }

    @Test
    void challengeIsNotSilentlyThePlainVariant() {
        String verifier = Pkce.createCodeVerifier();
        assertThat(Pkce.createS256Challenge(verifier)).isNotEqualTo(verifier);
    }

    @Test
    void verificationAcceptsOnlyTheMatchingVerifier() {
        String verifier = Pkce.createCodeVerifier();
        String challenge = Pkce.createS256Challenge(verifier);
        assertThat(Pkce.verify(verifier, challenge)).isTrue();
        assertThat(Pkce.verify(Pkce.createCodeVerifier(), challenge)).isFalse();
    }

    @Test
    void opaqueValuesDoNotRepeat() {
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 10_000; i++) {
            assertThat(seen.add(Pkce.createOpaqueValue())).isTrue();
        }
    }
}
```

---

## Internals

| Protocol concept | Spring Security type |
|---|---|
| Client registration | `org.springframework.security.oauth2.client.registration.ClientRegistration` |
| Grant type | `org.springframework.security.oauth2.core.AuthorizationGrantType` |
| Client authentication method | `org.springframework.security.oauth2.core.ClientAuthenticationMethod` |
| Outbound authorization request | `org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest` |
| Callback parameters | `OAuth2AuthorizationResponse` inside `OAuth2AuthorizationExchange` |
| Access / refresh token | `OAuth2AccessToken`, `OAuth2RefreshToken` |
| ID token | `org.springframework.security.oauth2.core.oidc.OidcIdToken` |
| Protocol error | `OAuth2Error` + `OAuth2AuthenticationException` |

`AuthorizationGrantType` is a value object rather than an enum, which is how new grants were
absorbed without a breaking change:

```java
// org.springframework.security.oauth2.core.AuthorizationGrantType (simplified)
public final class AuthorizationGrantType implements Serializable {

    public static final AuthorizationGrantType AUTHORIZATION_CODE =
            new AuthorizationGrantType("authorization_code");
    public static final AuthorizationGrantType CLIENT_CREDENTIALS =
            new AuthorizationGrantType("client_credentials");
    public static final AuthorizationGrantType REFRESH_TOKEN =
            new AuthorizationGrantType("refresh_token");
    public static final AuthorizationGrantType JWT_BEARER =
            new AuthorizationGrantType("urn:ietf:params:oauth:grant-type:jwt-bearer");
    public static final AuthorizationGrantType DEVICE_CODE =
            new AuthorizationGrantType("urn:ietf:params:oauth:grant-type:device_code");
    public static final AuthorizationGrantType TOKEN_EXCHANGE =
            new AuthorizationGrantType("urn:ietf:params:oauth:grant-type:token-exchange");

    private final String value;   // equals/hashCode on value; that is the whole class
}
```

`PASSWORD` sits in that list on 6.x, deprecated, and disappears in 7.0 together with
`OAuth2PasswordGrantRequest` and `PasswordOAuth2AuthorizedClientProvider`.

How PKCE is attached:

```java
// OAuth2AuthorizationRequestCustomizers (simplified)
public static Consumer<OAuth2AuthorizationRequest.Builder> withPkce() {
    return (builder) -> builder.attributes((attrs) ->
            builder.additionalParameters((params) -> applyPkce(params, attrs)));
}

private static void applyPkce(Map<String, Object> parameters, Map<String, Object> attributes) {
    String codeVerifier = DEFAULT_SECURE_KEY_GENERATOR.generateKey();   // Base64StringKeyGenerator(32)
    attributes.put(PkceParameterNames.CODE_VERIFIER, codeVerifier);     // stays client-side
    try {
        parameters.put(PkceParameterNames.CODE_CHALLENGE, createHash(codeVerifier));
        parameters.put(PkceParameterNames.CODE_CHALLENGE_METHOD, "S256");
    }
    catch (NoSuchAlgorithmException ex) {
        parameters.put(PkceParameterNames.CODE_CHALLENGE, codeVerifier);   // "plain" fallback
    }
}
```

The verifier goes into the request's **attributes**, which travel in the
`AuthorizationRequestRepository` — that is, the session. The challenge goes into
**additionalParameters**, which are appended to the browser redirect. Automatic application for
public clients happens because the resolver checks for `ClientAuthenticationMethod.NONE`.

Discovery, and the issuer assertion:

```java
// ClientRegistrations (simplified)
public static ClientRegistration.Builder fromIssuerLocation(String issuer) {
    URI uri = URI.create(issuer);
    Map<String, Object> configuration = getConfiguration(issuer,
            oidc(uri),           // {issuer}/.well-known/openid-configuration
            oidcRfc8414(uri),    // {host}/.well-known/openid-configuration/{path}
            oauth(uri));         // {host}/.well-known/oauth-authorization-server/{path}
    String metadataIssuer = (String) configuration.get("issuer");
    Assert.state(issuer.equals(metadataIssuer),
        () -> "The Issuer \"" + metadataIssuer + "\" provided in the configuration "
            + "did not match the requested issuer \"" + issuer + "\"");
    // ...
}
```

---

## Configuration Reference

| Parameter / option | Effect | Default |
|---|---|---|
| `response_type` | `code` selects the authorization code flow | none — required |
| `client_id` | identifies the client; a public value | none — required |
| `redirect_uri` | callback location; must exactly match a registered value | optional if exactly one is registered — **always send it** |
| `scope` | requested delegation; `openid` makes the flow OIDC | provider-specific |
| `state` | CSRF and session binding on the callback | optional in RFC 6749 — **mandatory in practice**; Spring always sends it |
| `nonce` | ID token replay binding | required for implicit/hybrid, strongly recommended for code |
| `code_challenge` | PKCE challenge | required for public clients, recommended for all |
| `code_challenge_method` | `S256` or `plain` | **`plain` per the RFC — always send `S256`** |
| `prompt` | `none`, `login`, `consent`, `select_account` | provider default |
| `max_age` | forces re-authentication older than N seconds; drives `auth_time` | unset |
| `login_hint` | pre-fills the username at the AS | unset |
| `spring.security.oauth2.client.provider.<id>.issuer-uri` | discovery; populates every endpoint | unset |
| `...registration.<id>.redirect-uri` | callback template | `{baseUrl}/login/oauth2/code/{registrationId}` |
| `...registration.<id>.client-authentication-method` | token endpoint authentication | `client_secret_basic` with a secret, `none` without |
| `server.forward-headers-strategy` | makes the generated `redirect_uri` use the external scheme and host | `none` |

---

## Production Concerns & Anti-Patterns

**Using the access token as proof of identity.** If your login code reads an access token to
decide *who* the user is, it is wrong. Use the ID token, validate `aud` and `nonce`, and treat the
access token as write-only from the client's point of view.

**Sending the ID token to your own API.** The frontend decodes it, sees a nice `sub` and `email`,
and puts it in `Authorization: Bearer`. An API configured with only an issuer check accepts it,
and any party holding an ID token for that user — issued to any client of the same issuer — can
now call your API as them.

**Wildcard or prefix redirect URI registration.** Development convenience that survives into
production. Every open redirect anywhere under the pattern becomes a code-theft primitive.
Register exact URIs, one per environment and provider.

**Long-lived access tokens to "reduce load on the identity provider".** A thirty-day access token
is a thirty-day window in which a leaked token is fully usable, and a self-contained JWT cannot be
revoked inside it. Keep access tokens at five to fifteen minutes and let refresh tokens carry the
longevity, where revocation actually works.

**Storing tokens in `localStorage`.** Identical reasoning to
[`01_M1_T1_HTTP_Web_Basics.md`](01_M1_T1_HTTP_Web_Basics.md): an XSS flaw becomes silent, remote,
persistent account takeover rather than a bounded in-page compromise. The sanctioned pattern for
browser applications is a **backend-for-frontend** holding tokens server-side behind a hardened
`HttpOnly` cookie session.

**Sensitive data in the token payload.** A JWS is signed, not encrypted. Everything in it is
world-readable and ends up in proxy logs, traces and devtools. No national identifiers, no
internal hostnames.

**Skipping `state` because "we use PKCE now".** They defend different things. PKCE binds the
*code* to the client instance; `state` binds the *callback* to the user's browser session and is
what stops an attacker's completed flow being replayed into a victim's browser. Send both.

**Accepting `alg: none`, or letting the token dictate the algorithm.** Pin the expected algorithm
set when building the decoder. A verifier that trusts the header's `alg` can be downgraded, and
the symmetric-versus-asymmetric confusion attack has burned real systems.

---

## Debugging Playbook

| Symptom | Likely root cause | Fix |
|---|---|---|
| `error=redirect_uri_mismatch` | The generated `redirect_uri` differs by scheme, port, host, trailing slash or case | Log the outgoing URL; register the exact value; behind a proxy set `server.forward-headers-strategy=framework` |
| `invalid_grant` at the token endpoint | Code already used, code expired (30–60s), `redirect_uri` not repeated identically, or a wrong/missing `code_verifier` | Confirm single-use handling; resend the identical `redirect_uri`; confirm the session holding the verifier survived |
| `invalid_client` at the token endpoint | Wrong secret, or the wrong client authentication method | Check `token_endpoint_auth_methods_supported` and align the registration |
| Startup fails with "did not match the requested issuer" | The issuer in the metadata differs from the configured `issuer-uri` | Make them byte-identical; for containerised Keycloak set the frontend URL |
| No `id_token` in the token response | The `openid` scope was not requested | Add `openid` to the registration's scopes |
| ID token rejected on the `nonce` | The session was lost between redirect and callback, or a load balancer moved the request | Sticky sessions or a shared store; check `SameSite` on the session cookie |
| `authorization_request_not_found` | The session holding the request is gone — new session, `SameSite=Strict`, or a stateless chain | Allow session creation for the login flow; use `SameSite=Lax`, never `Strict` |
| Token valid at service A, rejected at service B | Correct if B validates `aud`; a vulnerability if it does not | Request the right audience and validate `aud` everywhere |
| `insufficient_scope` from the API | The user consented to less than requested, or the AS trimmed the scope | Read the `scope` value in the token response rather than assuming |

---

## Interview Q&A

### Q1. Someone says "we use OAuth2 for login". What is wrong with that sentence, and what would you ask next?

<details>
<summary>Show answer</summary>

It conflates two protocols. OAuth2 is a **delegated authorization** framework answering "may this
client perform this operation on this resource?", producing an access token meaningful only to a
resource server. It carries no standard statement about who authenticated, when, or how, and
RFC 6749 gives the client no sanctioned way to inspect it. **OpenID Connect** is the
authentication layer, standardised precisely because everyone was building login on top of OAuth2
by hand and getting it wrong.

So my next question is: "do you request the `openid` scope and validate an ID token, or do you
take the access token and call a `/me` endpoint?" If it is the latter, there are three defects.
No audience binding, so an access token issued to any other client for the same user can be
injected and logs the attacker in as that user. No freshness signal, so step-up authentication is
impossible — the token may have been minted from a refresh token days after the user last proved
anything. And no standard shape, so each provider is a bespoke integration with its own bugs.

**Counter-question: describe the token injection attack concretely enough that I believe it is exploitable.**

Take an application that logs users in by calling the provider's user-info endpoint with whatever
access token it holds, and that has any surface where a token can be supplied — a mobile variant
accepting an existing token, a legacy hybrid callback, an implicit-style handler reading the
fragment. The attacker registers their own perfectly legitimate client with the same provider and
persuades the victim to authorise it; that is just a normal consent screen for a plausible app. The
attacker now holds a valid access token whose subject is the victim.

They feed it into the vulnerable login path. The provider answers "this is the victim", and the
application creates a session for the victim. Every check passed, because the token genuinely is
valid and genuinely does belong to the victim — nothing in it says "issued to the attacker's
client". OIDC closes it because the ID token's `aud` must equal *your* `client_id` and its `nonce`
must match the value you generated for this browser session.

**Counter-question: GitHub does not implement OIDC for normal OAuth apps. How do you build login on it safely?**

You accept you are outside the standard and compensate by never letting a token enter the flow
from outside. Run only authorization code with PKCE, generate and verify `state` bound to the
session, exchange the code yourself on the back channel, and use the token you received *in that
exchange* and nothing else. Never accept a client-supplied token as a login credential on any
path.

That gives you a token you know came from a flow you started for this browser, which is the
property the ID token would otherwise supply. Where the provider offers a token-info endpoint that
reports the issuing client, check it — that is the missing audience check. And write the
constraint down, because the next developer will copy "we log in with an access token" somewhere
it is not safe.

**Counter-question: if OIDC is strictly better for login, why does anyone still use plain OAuth2?**

Because most real integrations are not login. Connecting a user's Dropbox so your app can write
files there does not require knowing who they are — it requires a token that can write files. That
is delegated authorization, exactly as designed, and `openid` adds nothing. Plain OAuth2 also
remains correct for machine-to-machine traffic, where client-credentials tokens are the point and
there is no user. And several large providers simply predate OIDC. The rule is not "always OIDC",
it is **"establishing who the user is means OIDC; obtaining permission to act means OAuth2"**.
</details>

### Q2. Walk me through PKCE. Why does it exist, and why is it now recommended even for confidential clients?

<details>
<summary>Show answer</summary>

Before the redirect the client generates a `code_verifier` — 43 to 128 characters from the
unreserved alphabet, from a cryptographic random source — and keeps it in session. It sends
`code_challenge = BASE64URL(SHA-256(code_verifier))` with `code_challenge_method=S256` on the
front channel. The authorization server stores the challenge with the issued code. At token
exchange the client sends the verifier on the back channel; the server recomputes the hash and
compares, and a mismatch is `invalid_grant`.

It prevents **authorization code interception**. The canonical case is mobile: a native app
registers a custom URI scheme, a malicious app registers the same scheme, and the operating system
hands the redirect — with the code — to the wrong app. A native app is a public client with no
secret, so nothing else stands between a stolen code and a token. PKCE means the thief holds a code
they cannot redeem, since that would require inverting SHA-256.

Confidential clients want it because client authentication defends a different thing: it proves
*who is calling the token endpoint*, while PKCE proves *the caller is the instance that started
this flow*. The gap is **authorization code injection** — the attacker runs a legitimate flow with
their own account, obtains a code, and injects it into a victim's session at your callback. Your
client authenticates flawlessly and exchanges the attacker's code, so the victim's browser is now
bound to the attacker's account and everything the victim uploads goes there. PKCE stops it because
the injected code's challenge will not match the victim's verifier. There is also the plain
defence-in-depth point that client secrets leak into source control and CI logs far more often than
teams admit.

**Counter-question: why is `code_challenge_method=plain` in the specification, and what happens if a server accepts it?**

It was for genuinely constrained hardware that could not compute SHA-256. With `plain` the
challenge *is* the verifier, travelling on the front channel, so anyone positioned to intercept the
code has by definition seen the verifier and PKCE contributes nothing.

Worse, `plain` is the specification's **default** when `code_challenge_method` is omitted, so a
client that sends a challenge without the method silently gets the useless variant. Servers should
reject it outright and clients should always send `S256` explicitly; you can confirm support via
`code_challenge_methods_supported` in the metadata. Spring's customizer falls back to `plain` only
if SHA-256 is missing from the JRE, which never happens.

**Counter-question: does PKCE replace `state`?**

No, and the simplification is dangerous. `state` is generated by the client, stored against the
*browser session*, and compared when the callback arrives — its job is to guarantee this callback
belongs to a flow *this browser* started. That is what stops an attacker completing their own
authorization and feeding the resulting callback URL to a victim, logging the victim into the
attacker's account with no code theft involved at all.

PKCE is verified by the *authorization server* at the token endpoint and guarantees that whoever
redeems the code requested it. There is partial overlap, but `state` catches cases where the
attacker never needs the code to be secret. OAuth 2.1 keeps both, and Spring always sends `state`
regardless of your PKCE configuration.

**Counter-question: where does Spring store the `code_verifier` between the redirect and the callback, and what breaks that?**

In the `OAuth2AuthorizationRequest`'s **attributes** map, and the whole request is persisted by the
`AuthorizationRequestRepository` — by default `HttpSessionOAuth2AuthorizationRequestRepository`,
meaning the HTTP session. The challenge, by contrast, goes into `additionalParameters` and is
appended to the redirect.

What breaks it is anything that loses or moves the session: `SessionCreationPolicy.STATELESS` on
the login chain so no session exists; a load balancer with neither sticky sessions nor a shared
store, so the callback lands elsewhere; `SameSite=Strict` on the session cookie, because the
callback is a cross-site top-level navigation and `Strict` suppresses the cookie — `Lax` is
required; and a session timeout shorter than a multi-factor journey. Each surfaces as
`authorization_request_not_found` or a state/nonce mismatch, and each is configuration rather than
protocol.
</details>

### Q3. Explain `state` and `nonce`. They are both random strings on the authorization request — what is the difference?

<details>
<summary>Show answer</summary>

Different lifecycles, different verifiers, different threat models.

`state` is an **OAuth2** parameter. The client generates it, associates it with the browser
session, and sends it. The authorization server treats it as opaque and echoes it back on the
redirect; the **client** compares. It never appears in a token. It binds the callback to a flow
this browser started, defeating CSRF on the callback and forced-login session fixation.

`nonce` is an **OIDC** parameter. The client generates it and sends it; the authorization server
embeds it **inside the ID token** as a claim, and the client compares the claim against what it
generated. It binds the *ID token* to this specific authorization request, defeating ID token
replay and injection.

Compactly: **`state` protects the redirect; `nonce` protects the token.** The authorization server
never inspects `state` and always signs `nonce` into the ID token.

**Counter-question: what makes a good `state` value, and what bad ones would I actually find in production?**

Good means high-entropy, unguessable, single-use, stored server-side against the session — thirty-
two bytes from a `SecureRandom`, base64url-encoded, removed as soon as it is compared.

Bad ones I have genuinely seen: a timestamp, which is predictable; a base64-encoded return URL,
which turns `state` into an open-redirect parameter *and* makes it fully guessable so it provides
no CSRF protection at all; a fixed string like `"login"`, equivalent to having none; and a value
stored in a cookie the attacker's site can influence, which breaks the session binding. If you must
round-trip application data, make `state` a random key pointing at server-side data rather than the
data itself.

**Counter-question: with the authorization code flow, is `nonce` optional? The specification only requires it for implicit and hybrid.**

Strictly yes — OIDC Core mandates it where the ID token comes through the front channel, and makes
it optional for pure code flow because the back-channel exchange already provides a binding. But
"optional" is not "useless". Validating it closes ID token injection in the residual cases:
multi-provider clients where a token from provider A might surface in a flow for provider B, hybrid
or mixed configurations, and any path where a token could arrive other than from your own exchange.
It costs one random value and one comparison, Spring Security does it automatically for OIDC
registrations, so you get it free and should not turn it off.

**Counter-question: the callback arrives with a valid `state` but the flow still fails. What next?**

That combination is informative: browser and session binding worked, so the failure is downstream.
I check the token exchange first. Is `redirect_uri` resent byte-identically — behind a proxy the
app may generate an internal host on the exchange while the browser used the public one? Is the
client authentication method right, which shows as `invalid_client` rather than `invalid_grant`?
Has the code already been consumed by a double-submitted callback, a browser prefetch, or an HTTP
client retry?

If the exchange succeeds and ID token validation fails, I check `nonce` — usually the session moved
or was recreated — then `aud` against the configured client ID, then the issuer string for the
internal-versus-external hostname mismatch, then clock skew, which is why `JwtTimestampValidator`
ships with a sixty-second default.
</details>

### Q4. Compare the ID token and the access token. What is the consequence of mixing them up?

<details>
<summary>Show answer</summary>

The **access token** is a credential for the *resource server*. Its audience is the API, its format
is unspecified by RFC 6749 — opaque handle or JWT, the authorization server's choice — and it is
sent as `Authorization: Bearer`. From the client's point of view it is write-only: hold it, attach
it, do not interpret it.

The **ID token** is a statement to the *client* about an authentication event. It is always a signed
JWT, its `aud` is the client's own `client_id`, and its claims describe who authenticated (`sub`),
when (`auth_time`, `iat`), how (`amr`, `acr`), and under which authorization request (`nonce`). The
client validates it once at the end of login and then normally discards it in favour of its own
session. It is a receipt, not a credential.

Mixing them fails both ways. **Sending the ID token to an API** is the dangerous direction: a
correctly configured resource server rejects it on audience, but one configured with only
`issuer-uri` accepts it happily — and since client IDs are public values appearing in every
authorization URL, no secrecy protects the audience either. **Using the access token as identity**
is the Q1 failure: no audience binding, no freshness, open to injection.

**Counter-question: the resource server's job is to validate tokens. Why is it the client's problem that ID tokens exist?**

Because both artefacts pass through the same frontend code, and the ID token is the one a developer
can decode and see useful fields in, so it looks like "the good token" while the access token looks
like noise. Without a stated rule, someone attaches the readable one.

It is also structurally the client's problem, because only the client can validate an ID token
correctly: only it knows the `nonce` it generated and its own `client_id` to compare against `aud`.
A resource server cannot perform those checks on another party's behalf. So the division is fixed —
the client validates the ID token and must not forward it; the resource server validates the access
token and must reject anything whose audience is not itself.

**Counter-question: how would you make it impossible, rather than merely forbidden, for an ID token to work against your API?**

Validate `aud` on the resource server and assert it **equals the API's own resource identifier**,
not merely that it is present. On Spring Security 6.x that is a `JwtClaimValidator` for `aud`
through a `DelegatingOAuth2TokenValidator`, or the `audiences` property. An ID token's `aud` is a
client ID, which will never equal the API identifier, so it is rejected structurally.

Two reinforcements: ask the authorization server for RFC 9068 access tokens carrying `typ: at+jwt`
and reject anything that is not an access token by type; and architecturally, never let the ID token
out of the login handler — a backend-for-frontend means the browser sees neither token, removing the
whole class of mistake.

**Counter-question: your ID token is 4 KB because the provider stuffs group memberships into it. What do you do?**

First recognise it as a design smell, not a size problem. Groups in a token are a snapshot taken at
authentication time, so every membership change is invisible until reissue — the "permission change
has no effect until re-login" problem from
[`02_M1_T2_Authentication_Authorization.md`](02_M1_T2_Authentication_Authorization.md).

Practically, if the token is validated at login and discarded in favour of a server-side session,
4 KB crosses the wire once and is harmless. The pain comes when a large token rides every request
and hits proxy header limits — nginx defaults near 8 KB — producing intermittent 400s that look like
network faults. The fixes, in order: request fewer scopes; move group data behind the UserInfo
endpoint and fetch it once; map provider groups to a small set of application authorities at login
and store those instead of the raw list; and where supported, use claim-mapping rules so only the
groups relevant to this client are emitted.
</details>

### Q5. Why were implicit and the password grant removed from OAuth 2.1, and what do you tell a team still using them?

<details>
<summary>Show answer</summary>

**Implicit** returned the access token in the redirect's URL fragment, chosen because fragments are
not sent to servers and browsers could not then cross-origin `POST` to a token endpoint. The
problems are structural. The token lands in browser history; every script on the callback page —
analytics, tag managers, a compromised dependency — can read `window.location.hash`; extensions can
read it; there is no code and no client authentication, so nothing binds the token to the requesting
instance and injection is trivial; and refresh tokens could not be issued safely, pushing
implementations toward long-lived access tokens. Universal CORS support on token endpoints removed
the justification: a browser application can run authorization code with PKCE and receive the token
in a `POST` body that never touches a URL.

**ROPC** has the client collect the password and post it to the token endpoint. It reintroduces
exactly the anti-pattern OAuth2 was invented to eliminate. It makes MFA, CAPTCHA, risk-based
step-up, passwordless login, consent and federation impossible, because all of those need a browser
and a screen the authorization server controls. It puts the raw password in client memory, logs and
crash dumps. And it is a clean credential-stuffing endpoint with a machine-readable success signal.
Spring Security **removes it in 7.0** — the classes are deleted, and there is no replacement API
because the replacement is a different flow.

**Counter-question: the team says "we are first-party, we own both the app and the identity provider, so the password never leaves our trust boundary". Is that valid?**

It is the strongest form of the argument and still wrong, for reasons about capability rather than
trust. Even granting the premise, ROPC caps what you can ever build. When security mandates
multi-factor authentication there is nowhere to put the challenge, so everyone invents a private
out-of-band protocol. When the company acquires a business whose employees sign in through their own
identity provider, you cannot federate, because federation is a redirect and ROPC has none. When you
want passkeys, there is no password to post. When fraud wants risk-based step-up, there is no
AS-controlled screen to show it on.

The trust premise also degrades quietly — "first-party" holds until a partner reuses the flow, a QA
tool logs request bodies, or a crash reporter captures the heap. The migration is authorization code
with PKCE in a system browser on mobile (the AppAuth pattern with in-app browser tabs) or a
backend-for-frontend on web. The version argument helps: 7.0 deletes the classes, so this becomes a
blocking upgrade issue with a date attached.

**Counter-question: a legacy machine client uses ROPC with a service account. Same answer?**

No — this one is easy. No human means no delegation, so the correct grant is **client credentials**.
Turn the service account into a registered client with its own credential, map what it could do into
scopes, change the grant type. No browser, no redirect, no interface.

While doing it I would upgrade from a shared secret to `private_key_jwt`, so the authorization server
stores only a public key. And I would give machine tokens a distinct audience so they can be routed
to their own `SecurityFilterChain`, for the reason in
[`02_M1_T2_Authentication_Authorization.md`](02_M1_T2_Authentication_Authorization.md): machine
traffic should never be evaluated by rules written for humans.

**Counter-question: if implicit is dead, what about the hybrid flow (`response_type=code id_token`)?**

Hybrid returns an ID token on the front channel alongside the code, so a client learns who the user
is before completing the exchange. It is not dead in the way implicit is — it remains in OIDC Core
and appears in financial-grade profiles — but it carries the same front-channel exposure for the ID
token, which lands in the URL fragment with all the history and script-access problems, and it must
be validated very carefully including the `c_hash` claim binding it to the code. For a normal
application there is no reason to take that on. If someone proposes hybrid, I ask what property they
need that plain `response_type=code` with PKCE does not give, and the honest answer is almost always
none.
</details>

### Q6. Design question — authentication for a platform with a React SPA, an iOS app, a partner API, and forty internal microservices.

<details>
<summary>Show answer</summary>

I would refuse a single answer, because these are four client populations with different threat
models, and the mistake to guard against is forcing one pattern onto all of them.

**The shared foundation.** One authorization server is the identity authority. Every client is
registered separately with its own `client_id`, exact redirect URIs, and allowed scopes and grants —
a shared registration destroys every audience and consent guarantee. Every API gets a **resource
identifier** that becomes the `aud` of tokens minted for it, and every resource server validates
that `aud`. That is the control stopping a token for the low-value service being replayed against
the high-value one.

**React SPA: backend-for-frontend, not tokens in the browser.** Its backend is a confidential client
running authorization code with PKCE, holding tokens server-side and giving the browser a hardened
session cookie — `HttpOnly`, `Secure`, `SameSite=Lax`, `__Host-` prefix, CSRF protection on because
the credential is now ambient. An XSS flaw becomes a bounded in-page problem rather than silent
remote exfiltration. If the organisation insists on a pure SPA, it is a public client with PKCE,
tokens in memory only, refresh tokens rotated with reuse detection, and access tokens in single-digit
minutes — recorded as an accepted risk rather than passing silently.

**iOS: public client, authorization code with PKCE, system browser.**
`ASWebAuthenticationSession` rather than an embedded web view, because an embedded view can read the
credential and defeats the point, and because the system browser shares the single sign-on session.
Universal links for the redirect rather than a custom URI scheme, since custom schemes are exactly the
hijackable surface PKCE was invented for.

**Partner API: client credentials with `private_key_jwt`.** No user, no delegation. Each partner is a
separate client with its own key pair, so no shared secret can leak from either side and rotation is a
public key swap. Partner traffic gets its own `SecurityFilterChain`, never touching rules written for
humans, with per-partner rate limits keyed on the client ID.

**Forty microservices: short-lived JWTs, local validation.** Each validates signature, `iss`, `exp`
and its own `aud` against a cached JWKS. Introspection on every call would make the authorization
server a synchronous dependency of everything. For service-to-service calls I want the caller's
identity preserved, which means token exchange (RFC 8693, Spring Security 6.3) minting a downstream
token that retains the original subject. Blindly forwarding the incoming token is a confused-deputy
generator, since the downstream cannot tell user intent from a compromised intermediate.

**Counter-question: the mobile team wants a native username and password form posting to your API. What do you say?**

Yes to the requirement, no to the mechanism. The requirement is legitimate — a redirect that looks
unlike the app is genuinely worse — but the mechanism is ROPC, which I am not adding in 2026,
especially when 7.0 deletes the supporting classes.

`ASWebAuthenticationSession` and Custom Tabs are not the old full-browser redirect: they render
in-app, can be themed close to native, and share the system single sign-on session, so a user already
signed in often sees no prompt. Most of the perceived ugliness is an unstyled authorization server
login page, which is a CSS problem on a page we control. Then I make the trade-off concrete: with
ROPC, the day security mandates MFA we cannot ship it without inventing a private protocol; with the
browser flow, MFA, passkeys, device trust and risk-based step-up are configuration at the
authorization server that the app inherits free. The app also skips the prompt on later launches
using a refresh token, so the redirect is a first-launch cost, not a per-login cost.

**Counter-question: security demands that disabling an employee revokes access everywhere within thirty seconds, but your forty services validate locally. Now what?**

I say plainly that local validation and instant revocation are in tension, then give options. This is
the same trade as [`01_M1_T1_HTTP_Web_Basics.md`](01_M1_T1_HTTP_Web_Basics.md): stateless buys scale
and costs revocation, and every scheme to buy revocation back reintroduces state somewhere.

**Shorten access tokens to sixty seconds** and make refresh cheap. Revocation then takes effect at the
next refresh, inside the window, and killing the refresh token is a single stateful operation in a
place that is already stateful. This alone usually satisfies "thirty seconds" and is what I would lead
with. If not, **push revocation rather than pull it**: the authorization server publishes revoked
subject or token identifiers to a topic, each service keeps a small in-memory denylist, and entries
expire when the token would have expired anyway — bounded state, not a session store, and it degrades
to the sixty-second expiry if the topic lags. **Introspection on every request** is third, and I would
present it honestly: exact revocation, at the price of a network round trip on every call and the
authorization server becoming a synchronous dependency of forty services — at which point we should
ask whether we wanted stateless tokens at all. The hybrid worth naming is introspecting only for
genuinely high-value operations, which targets the cost where the thirty-second requirement actually
matters.
</details>

---

## Quick Recall

```
THE ONE-LINE FRAMING
  OAuth2 = DELEGATED AUTHORIZATION   ("may this client do this?")
  OIDC   = the authentication layer ON TOP ("who authenticated, when, how?")
  using an access token to decide identity = token injection vulnerability

FOUR ROLES
  resource owner        the human who owns the data
  client                wants to act on their behalf (client_id [+ secret])
  authorization server  authenticates + issues tokens (holds passwords + keys)
  resource server       validates tokens (holds NO passwords)
  roles != processes; one app can play several at once

CLIENT TYPES
  confidential = can keep a secret server-side -> client_secret / private_key_jwt
  public       = cannot (SPA, mobile, CLI)     -> ClientAuthenticationMethod.NONE + PKCE
  an obfuscated secret in a shipped bundle is NOT a secret

GRANTS - VERDICT
  authorization_code          DEFAULT for anything with a user
  code + PKCE                 REQUIRED public, RECOMMENDED confidential, REQUIRED by 2.1
  client_credentials          machine-to-machine, no user, scopes only, no roles
  refresh_token               rotate + reuse detection for public clients
  device_code                 TV / CLI / IoT (RFC 8628)
  implicit                    DEAD - token in the URL fragment -> history, scripts, extensions
  password (ROPC)             DEAD - out of 2.1, classes DELETED in Spring Security 7.0

AUTH CODE PARAMS -> THREAT
  response_type=code      not implicit
  redirect_uri            open redirect / code theft (EXACT match, no wildcards)
  state                   CSRF on the callback, forced login   (the CLIENT verifies)
  nonce                   ID token replay / injection          (lands INSIDE the id_token)
  code_challenge + S256   code interception + code injection   (the AS verifies)
  the code is single-use, ~30-60s, delivered in the QUERY string

PKCE
  verifier  43-128 chars, CSPRNG, unreserved alphabet, stays in the SESSION
  challenge BASE64URL(SHA256(ASCII(verifier))), S256, travels the front channel
  plain is the SPEC DEFAULT and is useless - always send S256 explicitly
  does NOT replace state

ID TOKEN vs ACCESS TOKEN
  id_token  aud = client_id  for the CLIENT  always a JWT  validated once at login
  access    aud = the API    for the RS      opaque OR JWT never interpreted by the client
  NEVER send an id_token to an API as a bearer credential
  openid -> sub ; profile -> name/picture ; email -> email + email_verified
  offline_access -> refresh token ; auth_time/acr/amr -> step-up
  local user key = (iss, sub)  -- never email, never sub alone

DISCOVERY
  /.well-known/openid-configuration        (OIDC)
  /.well-known/oauth-authorization-server  (RFC 8414)
  metadata "issuer" MUST equal the requested issuer - Spring asserts it

OAUTH 2.1 = 6749 + 6750 + BCP, minus the dangerous parts
  no implicit, no password, PKCE everywhere, exact redirect_uri matching,
  no bearer tokens in query strings, refresh tokens constrained or one-time-use

ATTACKS
  open redirect      -> exact redirect_uri match + PKCE
  CSRF on callback   -> state bound to the session, consumed once
  code injection     -> PKCE + nonce
  mix-up             -> one redirect_uri PER PROVIDER + RFC 9207 iss
  token substitution -> validate id_token aud + nonce
  audience confusion -> EVERY resource server validates aud (NOT on by default)
```

---

**Previous:** [`28_M9_T1_Security_Exception_Handling.md`](28_M9_T1_Security_Exception_Handling.md) ·
**Next:** [`30_M10_T2_OAuth2_Client.md`](30_M10_T2_OAuth2_Client.md)
