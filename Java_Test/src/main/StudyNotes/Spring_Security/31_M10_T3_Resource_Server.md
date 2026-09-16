# 10.3 — OAuth2 Resource Server

> **Module 10 · Topic 3** · OAuth2 & OpenID Connect
> Baseline: Spring Security 6.x on Boot 3.x
> New to this? Read **In Plain English** below first, then come back to the Version Matrix.

---

## Version Matrix

| Concern | Spring Security 5.x | **Spring Security 6.x (baseline)** | Spring Security 7.x |
|---|---|---|---|
| DSL style | `http.oauth2ResourceServer().jwt().and()` | **lambda only — `http.oauth2ResourceServer(o -> o.jwt(Customizer.withDefaults()))`** | lambda only |
| Opaque token introspector | `NimbusOpaqueTokenIntrospector` | **`SpringOpaqueTokenIntrospector`; the `Nimbus*` name is deprecated** | `SpringOpaqueTokenIntrospector`, `RestClient`-based |
| Audience validation | no property; hand-written `OAuth2TokenValidator` | **`spring.security.oauth2.resourceserver.jwt.audiences` (Boot 3.1+), or `JwtClaimValidator` in a `DelegatingOAuth2TokenValidator`** | same, plus `JwtValidators.createAtJwtValidator()` |
| Decoder from an issuer | `JwtDecoders.fromIssuerLocation(issuer)` — discovery runs immediately | **same, plus `NimbusJwtDecoder.withIssuerLocation(issuer)` (6.1+), which derives the JWK Set URI inside `build()`** | same |
| Startup coupling to the identity provider | Boot's auto-configured decoder became lazy in Boot 2.6 | **auto-configured decoder is lazy via `SupplierJwtDecoder`; declaring your own decoder bean makes it eager again** | same |
| `typ` header validation (RFC 9068) | not available | **`JwtTypeValidator`, controlled by the builder's `validateTypes(...)`, applied by default in later 6.x** | applied by default; `JwtValidators.createAtJwtValidator()` for full RFC 9068 |
| Multi-tenancy | `JwtIssuerAuthenticationManagerResolver` (5.3+) | **same, plus the `fromTrustedIssuers(...)` factories** | same |
| Password grant on the client side | present | deprecated | **removed** — relevant because resource servers often sit beside a client |
| Test support | `@MockBean JwtDecoder` | **`@MockBean` still works; Boot 3.4 introduces `@MockitoBean` and deprecates `@MockBean`** | `@MockitoBean` |

---

## Why This Exists

A resource server is the API at the end of the chain described in
[`29_M10_T1_OAuth2_OIDC_Fundamentals.md`](29_M10_T1_OAuth2_OIDC_Fundamentals.md). It holds the
protected data, has no user database, never sees a password, and does one thing:

> Take the bearer token off the request, decide whether it is genuine **and applicable**, turn it
> into an `Authentication`, and let the normal authorization machinery take over.

Small job, unusually high blast radius — and the *default* configuration is not the *correct*
configuration for a multi-service estate. Configure `issuer-uri` and nothing else and you get a
resource server that checks the signature and the issuer, which means it accepts **any** token that
authority ever minted, for any client, for any sibling service. Across forty services behind one
identity provider, that turns every low-value service into a pivot into every high-value one. The
missing check is `aud`, and it is the single most important thing in this file.

The second reason is a design fork you will be asked to defend: **self-contained JWTs validated
locally, or opaque tokens introspected over the network** — the scale-versus-revocation trade-off
from [`01_M1_T1_HTTP_Web_Basics.md`](01_M1_T1_HTTP_Web_Basics.md) in a new costume.

---

## In Plain English

**The one-line version:** A resource server is your API configured to accept requests that carry a
token instead of a login, and its entire job is to decide whether that token is genuine, whether it
was meant for this particular API, and what its holder is allowed to do.

**An analogy.** Picture the usher at a multiplex cinema. Someone hands over a ticket, and the usher
has to make several separate checks, each of which can go wrong independently. Is the ticket
genuine, or printed by somebody else? Is it for today, or for last Tuesday? And — the check that is
easy to skip — is it for *this screen*, or for the one down the corridor?

A careless usher checks only the first two. The ticket is real, the cinema printed it, the date is
right, so in you go — even though the ticket was sold for Screen 1 and this is Screen 6. That is
exactly the mistake this file is built around. A resource server configured the obvious way checks
that the token has a valid signature and came from the expected authority, and stops there. The
claim that says which API the token was meant for is called `aud`, short for audience, and Spring
does not check it unless you tell it to. In an organisation with forty services behind one identity
provider, that means a token collected by the least important service works against the most
important one.

The analogy extends in one more useful direction. The usher can verify a ticket in two very
different ways. Either the ticket carries all its details printed on it with a seal that proves the
cinema issued it, so the usher can check it on the spot with no phone call — but a ticket cancelled
five minutes ago still looks perfectly valid. Or the ticket is just a reference number, and the
usher phones the box office to ask about it — which is always accurate and always current, but adds
a phone call to every single admission and stops working entirely when the box office line is busy.
Those are the two token strategies, JWT validation and introspection, and the whole first section
is about choosing between them.

**How it actually works, step by step.**

The caller sends an ordinary HTTP request with one extra header:
`Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR...`. The word "bearer" is literal — whoever holds
the token can use it, exactly like a cinema ticket, which is why the rest of this file cares so much
about tokens not leaking into places like URLs and log files.

A filter called `BearerTokenAuthenticationFilter` looks for that header and pulls the token out.
Worth knowing immediately: if there is no token at all, this filter does not reject anything. It
simply lets the request carry on, and the later authorization step decides whether an anonymous
request was acceptable for this URL. That is deliberate, and it is what keeps public endpoints and
health checks working.

If there is a token, the next question is whether it is genuine. In the common case the token is a
**JWT**, which is a block of JSON carrying claims such as "subject 248289761001", "expires at
14:05", "audience https://orders-api.example.com", together with a cryptographic signature. Your
API does not need a password database to check that signature, because the identity provider
publishes its public keys at a URL known as the **JWKS** endpoint. Spring fetches those keys once,
caches them, and from then on verification is pure local computation with no network call at all.
This is why token validation is fast enough to do on every request.

Verifying the signature only tells you the token was not forged. The claims still have to be
checked, and that is done by a chain of small **validators**. Spring gives you two for free: one
checks the expiry time (with sixty seconds of tolerance, because server clocks drift), and one
checks the issuer if you configured the provider by its issuer URL. The audience validator is the
one you must add, either by setting the `audiences` property or by writing a small claim validator.
The mental model to carry away is that the default configuration is a *good start* rather than a
*complete* one.

Once the token is accepted, Spring turns it into an identity. The permissions attached to that
identity come from the token's `scope` claim, and Spring prefixes each one with `SCOPE_`, so a token
with `"scope": "orders:read orders:write"` produces two authorities named `SCOPE_orders:read` and
`SCOPE_orders:write`. It deliberately does not produce roles, because a scope and a role are
different things: a scope records what the *calling application* was permitted to do on the user's
behalf, while a role records what the *user* is. If your identity provider puts roles somewhere
else in the token, which most do, you supply a small converter to pick them up — and you keep the
scopes rather than replacing them, because checking both is what stops a narrowly-permitted
application inheriting an administrator's powers.

From there, everything is ordinary Spring Security. Your URL rules and `@PreAuthorize` annotations
compare the required permission against the authorities derived from the token. A bad token produces
a `401` response with a header explaining that the token was invalid, which tells the client to get
a fresh one. A good token without the necessary permission produces a `403` with a header saying the
scope was insufficient, which tells the client that retrying is pointless.

Two operational details round it out. First, a resource server is **stateless**: there is no session
and no login, because the token carries everything. That is also why cross-site request forgery
protection is switched off on this kind of chain — the protection exists for credentials the browser
attaches automatically, and a header the client sets explicitly is not one of them. Second, because
validation is local, a token cannot be cancelled early; it stays usable until it expires. The
practical answer is to keep token lifetimes short, typically five to fifteen minutes, so the window
in which a stolen or revoked token still works is small.

**Why should a beginner care?** The audience problem alone is worth the time: it is a genuine,
common, high-impact vulnerability that the default configuration does not protect you from, and
nothing in your logs will tell you it exists. The second reason is that this is the most common way
to secure a modern API, so you will configure it repeatedly, and a small number of recurring
failures account for almost every problem — a token that is valid but produces no permissions
because the roles are in a claim the default converter never reads, a `500` response that really
means "we could not reach the key endpoint" rather than "your token is bad", and an application that
refuses to start because you wrote a decoder bean that contacts the identity provider during
startup.

**Words you will meet in this file**

| Term | In plain words |
|---|---|
| Resource server | Your API, configured to accept a token instead of a login. |
| Bearer token | A credential where holding it is sufficient to use it, sent in the `Authorization` header. |
| JWT | A token whose contents are readable JSON with a signature attached, so it can be checked without contacting anyone. |
| Opaque token | A token that is just a reference number, meaningless until you ask the issuer about it. |
| Introspection | Asking the identity provider, over the network, whether a token is currently valid. Accurate, but a call per request. |
| `JwtDecoder` | The component that parses a token, verifies its signature, and runs the claim checks. |
| JWKS | The identity provider's published set of public keys, used to verify signatures without any shared secret. |
| `kid` (key id) | A label in the token header saying which published key signed it. It selects a key; it never grants trust. |
| Key rotation | Replacing the signing key, which must be done in stages so that no service is left holding only the old key. |
| Claim | One named piece of information inside a token, such as `sub`, `exp`, or `aud`. |
| `aud` (audience) | The claim naming which API the token was issued for. Not checked unless you configure it, which is the central warning of this file. |
| `iss` (issuer) | The claim naming which identity provider minted the token. |
| `exp` / `nbf` | When the token stops being valid, and when it starts. Checked with sixty seconds of clock tolerance by default. |
| `OAuth2TokenValidator` | One small check on a token's claims. Several are combined into a chain, and all of them run. |
| `issuer-uri` | One configuration line that tells Spring where the provider is, from which it finds the key endpoint and checks the issuer claim. |
| Scope | What the calling application was permitted to do on the user's behalf. Becomes an authority named `SCOPE_something`. |
| Authority | Any permission label on an identity, whether derived from a scope or a role. |
| `JwtAuthenticationConverter` | The component that turns a validated token into an identity with permissions attached. The place to read your provider's roles claim. |
| `BearerTokenResolver` | The component that finds the token on the request. It reads only the `Authorization` header by default, and that is the safe setting. |
| Stateless | No session and no cookie. The token is the entire state, so nothing is stored on the server between requests. |
| `insufficient_scope` | The error code returned with a `403` meaning "your token is fine but does not permit this", so the client knows not to retry. |
| `invalid_token` | The error code returned with a `401` meaning "this token is not usable", so the client knows to obtain a new one. |
| Multi-tenancy | Accepting tokens from more than one identity provider, chosen from a fixed trusted list rather than from whatever the token claims. |

**If you remember only one thing:** signature and issuer checks alone let any token from your
identity provider into any of your services, so validate the audience claim on every resource
server.

---

## Core Concepts

### 1. The Two Token Strategies

**In simple terms:** Either the token proves itself and you can check it on the spot, or it is a
reference number and you have to ask the identity provider about it on every single request.

```java
http.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));        // local validation
http.oauth2ResourceServer(oauth2 -> oauth2.opaqueToken(Customizer.withDefaults())); // introspection
```

| | `jwt(...)` — local validation | `opaqueToken(...)` — introspection |
|---|---|---|
| What the token is | a signed JWS carrying claims | an opaque handle; a key at the authorization server |
| Validation | signature against a cached JWKS, plus claim checks | `POST` to the introspection endpoint with client credentials |
| Network calls per request | **zero**, once the JWKS is cached | **one**, unless cached |
| Latency added | microseconds — an RSA verification | a full round trip, typically 5–50 ms |
| Revocation | **bounded by `exp`** — a revoked token works until it expires | **immediate**; the AS is the source of truth |
| Failure mode | keeps working if the AS is down, until an unknown key is needed | **hard dependency** — AS down means every request fails |
| Scaling | each service is independent | the introspection endpoint is a shared bottleneck carrying every service's traffic |
| Token size on the wire | 500 bytes to several kilobytes, every request | ~40 bytes |
| Leakage | claims readable by anything that sees the token | nothing readable without the endpoint |
| Resulting `Authentication` | `JwtAuthenticationToken`, principal `Jwt` | `BearerTokenAuthentication`, principal `OAuth2IntrospectionAuthenticatedPrincipal` |

**The honest comparison.** JWT validation is the right default for internal microservice estates: it
removes a synchronous dependency from every hot path, and the revocation window becomes a parameter
you control by shortening `exp`. Introspection is right when revocation must be exact — financial
operations, administrative interfaces, regulated environments — and the volume is low enough that a
round trip does not dominate. What you should not do is treat introspection as free: forty services
introspecting per request makes the authorization server a synchronous dependency of everything,
with total availability bounded by one component.

**Caching introspection** buys back most of the performance and part of the revocation guarantee:

- Key on the **token value** — never on the subject, or two tokens for the same user with different
  scopes collide. Store the key as a **hash**, so a cache dump is not a credential dump.
- Cache **only successes**, with a time-to-live that is the smaller of a short fixed window (30 to
  60 seconds) and the token's remaining lifetime. The short window is what keeps revocation
  meaningful — a five-minute cache quietly throws away the property you chose introspection for.
- **Never cache negative results indefinitely**, or a token that becomes valid after a clock
  correction stays rejected.
- The cache is now a second place where an authorization decision lives, so revocation or logout
  events must invalidate it.

There is also a hybrid worth naming: validate locally on every request and introspect **only** for
genuinely high-value operations, targeting the network cost where exact revocation matters.

### 2. Configuration Properties

**In simple terms:** A handful of configuration lines cover the whole setup, and the important
thing to notice is exactly which checks each line does and does not give you.

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          # Pick ONE source of validation material:
          issuer-uri: https://idp.example.com          # discovery -> jwk-set-uri, and validates iss
          jwk-set-uri: https://idp.example.com/jwks    # skip discovery, go straight to the keys
          public-key-location: classpath:public.pem    # one static RSA public key
          # And ALWAYS set this:
          audiences: https://orders-api.example.com    # validates aud (Boot 3.1+)
          jws-algorithms: [RS256]                      # pin the accepted algorithms
        opaquetoken:
          introspection-uri: https://idp.example.com/oauth2/introspect
          client-id: orders-api
          client-secret: ${INTROSPECTION_SECRET}
```

| Property | Gives you | Does **not** give you |
|---|---|---|
| `issuer-uri` | the JWKS location through discovery **and** a `JwtIssuerValidator` | audience validation |
| `jwk-set-uri` | the keys only | **no issuer validation** — add `JwtIssuerValidator` yourself |
| `public-key-location` | a single static key, for local testing | rotation, `kid` selection, issuer validation |
| `audiences` | a validator asserting `aud` contains one of the listed values | anything else |

`jwk-set-uri` alone is weaker than it looks: signature verification with no issuer check, so if two
authorization servers ever share a key set there is nothing to catch it. `public-key-location`
cannot rotate, which makes every key change a synchronised redeploy of every service.

### 3. `NimbusJwtDecoder` and the Startup Question

**In simple terms:** The component that checks tokens can be built in ways that contact the
identity provider while your application is still starting, which means a provider outage stops
your application from coming up at all.

```java
public interface JwtDecoder {
    Jwt decode(String token) throws JwtException;
}
```

```java
// 1. Keys directly. No discovery call; Nimbus fetches the JWK set lazily on first use.
NimbusJwtDecoder.withJwkSetUri("https://idp.example.com/jwks")
        .jwsAlgorithm(SignatureAlgorithm.RS256)
        .build();

// 2. From the issuer. Discovery happens inside build(), so a @Bean method makes it EAGER.
NimbusJwtDecoder.withIssuerLocation("https://idp.example.com").build();   // 6.1+

// 3. From the issuer, with the default validators pre-wired. Also EAGER.
JwtDecoders.fromIssuerLocation("https://idp.example.com");

// 4. A static public key. No network at all.
NimbusJwtDecoder.withPublicKey(rsaPublicKey).build();
```

**The startup consequence, stated precisely, because it is widely half-understood.** Spring Boot's
*auto-configured* resource server is **lazy**: with only `issuer-uri` set and no `JwtDecoder` bean of
your own, Boot wraps the decoder in `SupplierJwtDecoder`, and the discovery request plus the JWKS
fetch happen on the **first request carrying a JWT**, not at context refresh. The application starts
even with the identity provider unreachable.

The moment you declare your own `JwtDecoder` bean — which you must as soon as you want a hand-built
audience validator, a non-default algorithm, or a cache — that laziness disappears, because both
`JwtDecoders.fromIssuerLocation(...)` and `NimbusJwtDecoder.withIssuerLocation(...).build()` perform
the discovery call while the bean is being created. The identity provider becomes a **startup
dependency**, and an outage during a rolling deployment produces pods that will not come up.

Three ways to avoid it:

```java
// A. Preserve laziness by wrapping your construction in a Supplier.
@Bean
JwtDecoder jwtDecoder(@Value("${app.issuer-uri}") String issuer) {
    return new SupplierJwtDecoder(() -> {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withIssuerLocation(issuer).build();
        decoder.setJwtValidator(validators(issuer));
        return decoder;
    });
}
// B. Skip discovery: configure jwk-set-uri directly and add JwtIssuerValidator yourself.
//    Nimbus fetches keys lazily, so there is no startup call at all.
// C. Contribute only an OAuth2TokenValidator<Jwt> bean and let Boot build the decoder.
```

Option C is the one to reach for when all you need is an extra claim check: Boot composes an
`OAuth2TokenValidator<Jwt>` bean alongside its own defaults, so you keep the lazy behaviour.

### 4. JWKS, `kid` Lookup, and Rotation Without Downtime

**In simple terms:** Your API verifies signatures using public keys the provider publishes, and
replacing those keys has to be done in a specific order or every service stops accepting tokens at
once.

```json
{
  "keys": [
    { "kty": "RSA", "kid": "2026-03-key", "use": "sig", "alg": "RS256", "n": "0vx7...", "e": "AQAB" },
    { "kty": "RSA", "kid": "2026-01-key", "use": "sig", "alg": "RS256", "n": "qLd9...", "e": "AQAB" }
  ]
}
```

Every JWT carries the matching `kid` in its header: `{ "alg": "RS256", "typ": "JWT", "kid":
"2026-03-key" }`. Verification reads `kid`, selects the matching key from the cached set, and
verifies. **The `kid` is a selector, never a trust decision** — an attacker controls the header, so
the decoder must verify against a key from the trusted set and reject any `alg` outside its
allowlist. `NimbusJwtDecoder` trusts only `RS256` unless configured otherwise, which makes algorithm
confusion structurally impossible.

Spring delegates key retrieval to Nimbus, which caches the JWK set with a bounded lifespan (five
minutes is typical) and refreshes ahead of expiry so requests rarely block. When a token arrives with
an unknown `kid`, Nimbus re-fetches — otherwise every rotation would cause a five-minute outage. That
refresh is **rate limited**, which matters: without the limit, a stream of tokens with random `kid`
values would turn your resource server into a denial-of-service amplifier aimed at your own identity
provider.

**Rotation without downtime** follows from that mechanism:

1. The AS publishes the new public key in the JWK set **alongside** the old one, still signing with
   the old key.
2. Wait at least one full cache lifespan so every resource server has both keys.
3. The AS switches to signing with the new key; resource servers already have it, or fetch it on the
   first unknown `kid`.
4. Wait at least the maximum access token lifetime, so nothing signed with the old key is in flight.
5. Remove the old public key.

Skipping step 2 or step 4 is what turns a routine rotation into an outage. `public-key-location`
removes the ability to do any of this.

### 5. Validators — and the Audience Gap

**In simple terms:** Spring runs a short list of checks on a token's contents, and the check for
"was this token actually meant for me" is not on that list until you add it.

```java
@FunctionalInterface
public interface OAuth2TokenValidator<T extends OAuth2Token> {
    OAuth2TokenValidatorResult validate(T token);
}
```

| Validator | Checks | Applied when |
|---|---|---|
| `JwtTimestampValidator` | `exp` and `nbf`, with a **60-second default clock skew** | always |
| `JwtIssuerValidator` | `iss` equals the configured issuer | only when built from an issuer |
| `JwtTypeValidator` | the `typ` header is a JWT type (RFC 9068 `at+jwt`) | later 6.x, via the builder's `validateTypes` |
| **audience** | — | **not by default in older setups** |

`JwtValidators.createDefaultWithIssuer(issuer)` composes the timestamp and issuer validators;
`JwtValidators.createDefault()` composes only the timestamp validator.

**The audience gap.** Two services, `wiki-api` and `payments-api`, both configured with the same
`issuer-uri` and nothing else. A token minted for `wiki-api` carries
`aud: ["https://wiki-api.example.com"]`. Present it to `payments-api`: the signature is valid
because both trust the same authority, the issuer matches because both trust the same issuer, and
`exp` is in the future. **It is accepted.** The only thing distinguishing the two services was `aud`,
and nobody checked it.

This is the **audience confusion** attack from
[`29_M10_T1_OAuth2_OIDC_Fundamentals.md`](29_M10_T1_OAuth2_OIDC_Fundamentals.md). Any service that
can obtain a token — a compromised low-value service, a third-party integration that legitimately
receives tokens — replays it everywhere. An **ID token** becomes an API credential, since its `aud`
is a public client ID that nobody is checking. And a token from a different tenant of a shared
identity provider may be accepted if the issuer is shared.

```yaml
spring.security.oauth2.resourceserver.jwt.audiences: https://payments-api.example.com
```

or, when you need more than equality:

```java
OAuth2TokenValidator<Jwt> audience =
        new JwtClaimValidator<List<String>>(JwtClaimNames.AUD,
                aud -> aud != null && aud.contains("https://payments-api.example.com"));
OAuth2TokenValidator<Jwt> all = new DelegatingOAuth2TokenValidator<>(
        JwtValidators.createDefaultWithIssuer(issuerUri), audience);
decoder.setJwtValidator(all);
```

`DelegatingOAuth2TokenValidator` runs every delegate and **collects all errors** rather than
short-circuiting, which is why a rejection can name both a bad audience and an expired token.
`JwtClaimValidator` — a claim name plus a predicate — is also the right tool for tenant checks
(`tid`), authorized-party checks (`azp`), and any provider-specific claim your policy needs.

### 6. From `Jwt` to Authorities

**In simple terms:** This is where a validated token becomes a set of permissions, and by default
it only picks up scopes, so roles stored anywhere else in the token are silently ignored.

```java
package org.springframework.security.oauth2.server.resource.authentication;

public final class JwtGrantedAuthoritiesConverter
        implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final String DEFAULT_AUTHORITY_PREFIX = "SCOPE_";
    private static final Collection<String> WELL_KNOWN_AUTHORITIES_CLAIM_NAMES =
            Arrays.asList("scope", "scp");

    private String authorityPrefix = DEFAULT_AUTHORITY_PREFIX;
    private String authoritiesClaimName;          // null -> use the well-known names

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        for (String authority : getAuthorities(jwt)) {
            grantedAuthorities.add(new SimpleGrantedAuthority(this.authorityPrefix + authority));
        }
        return grantedAuthorities;
    }

    private Collection<String> getAuthorities(Jwt jwt) {
        String claimName = getAuthoritiesClaimName(jwt);    // first of scope, scp that is present
        if (claimName == null) {
            return Collections.emptyList();
        }
        Object authorities = jwt.getClaim(claimName);
        if (authorities instanceof String s) {
            return StringUtils.hasText(s) ? Arrays.asList(s.split(" ")) : Collections.emptyList();
        }
        if (authorities instanceof Collection<?>) {
            return castAuthoritiesToCollection(jwt, claimName);
        }
        return Collections.emptyList();
    }
}
```

A token with `"scope": "orders:read orders:write"` therefore produces exactly two authorities,
`SCOPE_orders:read` and `SCOPE_orders:write`. **No roles, ever** — which is correct, because, as
[`02_M1_T2_Authentication_Authorization.md`](02_M1_T2_Authentication_Authorization.md) establishes, a
scope is what the *client* was delegated, not what the *user* is, and the framework refuses to
pretend otherwise.

| Provider | Roles claim | Shape |
|---|---|---|
| Keycloak | `realm_access.roles`, `resource_access.<client>.roles` | nested JSON, array of strings |
| Auth0 | `permissions`, or a namespaced custom claim | array of strings |
| Entra ID | `roles`, `groups` (group object ids, not names) | array of strings |
| Cognito | `cognito:groups` | array of strings |
| Okta | `groups`, with a claim mapping configured | array of strings |

**Keep the scopes.** Replacing them with roles destroys the role-and-scope intersection rules that
stop a narrowly-consented client inheriting a privileged user's powers.

### 7. The Filter Path

**In simple terms:** This traces what happens to a request between the token arriving in a header
and your controller method running, including why a request with no token is allowed to continue.

```mermaid
sequenceDiagram
    autonumber
    participant C as Caller
    participant BTF as BearerTokenAuthenticationFilter
    participant BTR as DefaultBearerTokenResolver
    participant P as JwtAuthenticationProvider
    participant D as NimbusJwtDecoder
    participant JWKS as JWK Set endpoint
    participant V as DelegatingOAuth2TokenValidator
    participant CONV as JwtAuthenticationConverter
    participant AZ as AuthorizationFilter
    participant ET as ExceptionTranslationFilter

    C->>BTF: GET /api/orders  Authorization: Bearer eyJ...
    BTF->>BTR: resolve(request)
    Note over BTR: Authorization header ONLY by default;<br/>form body and query parameter are OFF
    BTR-->>BTF: token value (null -> the chain continues anonymously)
    BTF->>P: authenticate(BearerTokenAuthenticationToken)
    P->>D: decode(tokenValue)
    Note over D: parse header, read kid,<br/>reject any alg outside the allowlist
    D->>JWKS: GET /jwks (cache miss or unknown kid only, rate limited)
    JWKS-->>D: JWK Set
    D->>V: validate(jwt)
    Note over V: JwtTimestampValidator (exp/nbf, 60s skew)<br/>JwtIssuerValidator (iss)<br/>JwtTypeValidator (typ)<br/>audience validator - ADD THIS
    V-->>D: OAuth2TokenValidatorResult
    D-->>P: Jwt, or JwtValidationException
    P->>CONV: convert(jwt)
    Note over CONV: JwtGrantedAuthoritiesConverter -> SCOPE_*<br/>plus your role mapping
    CONV-->>P: JwtAuthenticationToken
    Note over BTF: SecurityContext saved via<br/>RequestAttributeSecurityContextRepository (stateless)
    BTF->>AZ: chain.doFilter
    alt invalid token
        BTF->>ET: OAuth2AuthenticationException
        ET-->>C: 401 + WWW-Authenticate: Bearer error="invalid_token"
    else denied by rule
        AZ->>ET: AccessDeniedException
        ET-->>C: 403 + WWW-Authenticate: Bearer error="insufficient_scope"
    else granted
        AZ-->>C: 200
    end
```

### 8. `BearerTokenResolver` — Why Query Parameters Stay Off

**In simple terms:** Spring will only look for the token in the request header, because a token in
the web address ends up in server logs, browser history, and the referrer sent to other sites.

```java
// org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver (simplified)
private static final Pattern authorizationPattern =
        Pattern.compile("^Bearer (?<token>[a-zA-Z0-9-._~+/]+=*)$", Pattern.CASE_INSENSITIVE);

private boolean allowFormEncodedBodyParameter = false;   // OFF
private boolean allowUriQueryParameter = false;          // OFF
private String bearerTokenHeaderName = HttpHeaders.AUTHORIZATION;

public String resolve(final HttpServletRequest request) {
    final String authorizationHeaderToken = resolveFromAuthorizationHeader(request);
    final String parameterToken = isParameterTokenSupportedForRequest(request)
            ? resolveFromRequestParameters(request) : null;
    if (authorizationHeaderToken != null) {
        if (parameterToken != null) {
            // Two tokens in one request is always an error - RFC 6750 forbids it.
            throw new OAuth2AuthenticationException(BearerTokenErrors.invalidRequest(
                    "Found multiple bearer tokens in the request"));
        }
        return authorizationHeaderToken;
    }
    return parameterToken;
}
```

RFC 6750 defines three ways to send a bearer token — the `Authorization` header, a form-encoded body
parameter, and a URI query parameter. **Spring enables only the header**, and the other two should
stay disabled. The query parameter is the dangerous one, for the same reasons listed in
[`01_M1_T1_HTTP_Web_Basics.md`](01_M1_T1_HTTP_Web_Basics.md): the full URL lands in web server,
load balancer and proxy access logs and in APM traces; it lands in browser history; it leaks through
`Referer`; and it ends up in bookmarks and shared links. OAuth 2.1 removes it entirely.

A custom `BearerTokenResolver` is legitimate when the token arrives somewhere non-standard — a
cookie in a backend-for-frontend, or a gateway-injected header — but a token carried in a cookie is
an ambient credential, so CSRF protection is back on the table.

### 9. Error Responses — RFC 6750

**In simple terms:** The refusal your API sends back is structured so the client can tell the
difference between "fetch a new token and try again" and "a new token will not help you".

```
HTTP/1.1 401 Unauthorized
WWW-Authenticate: Bearer realm="api", error="invalid_token",
                  error_description="An error occurred while attempting to decode the Jwt:
                                     Signed JWT rejected: Invalid signature",
                  error_uri="https://tools.ietf.org/html/rfc6750#section-3.1"

HTTP/1.1 403 Forbidden
WWW-Authenticate: Bearer error="insufficient_scope",
                  error_description="The request requires higher privileges than provided by
                                     the access token.",
                  error_uri="https://tools.ietf.org/html/rfc6750#section-3.1", scope="orders:write"
```

| Condition | Status | `error` code |
|---|---|---|
| No token at all | `401` | none — a bare `WWW-Authenticate: Bearer` |
| Malformed header, or two tokens in one request | `400` | `invalid_request` |
| Bad signature, expired, wrong issuer, wrong audience | `401` | `invalid_token` |
| Valid token, insufficient authorities | `403` | `insufficient_scope` |

This is genuinely useful to clients: `invalid_token` means "refresh and retry", while
`insufficient_scope` means "retrying is pointless, request a broader delegation". Note that a
**missing** token does not fail in the filter — `BearerTokenAuthenticationFilter` continues the
chain, the anonymous authentication is installed, and `AuthorizationFilter` eventually denies, at
which point `ExceptionTranslationFilter` upgrades the anonymous denial into an authentication
challenge exactly as described in
[`01_M1_T1_HTTP_Web_Basics.md`](01_M1_T1_HTTP_Web_Basics.md).

### 10. Multi-Tenancy, and Two Chains in One Application

**In simple terms:** One API can accept tokens from several identity providers, provided it picks
the provider from a list you decided in advance rather than from whatever the token claims to be.

```java
AuthenticationManagerResolver<HttpServletRequest> resolver =
        JwtIssuerAuthenticationManagerResolver.fromTrustedIssuers(
                "https://tenant-a.idp.example.com", "https://tenant-b.idp.example.com");
http.oauth2ResourceServer(oauth2 -> oauth2.authenticationManagerResolver(resolver));
```

`JwtIssuerAuthenticationManagerResolver` reads `iss` from the **unverified** token body, looks up the
corresponding `AuthenticationManager`, and delegates. That is safe for one specific reason: the issuer
only *selects* a validator from a **fixed, trusted list**, and the selected validator then verifies
the signature and re-checks the issuer. An unknown issuer is rejected before any key material is
touched. A resolver that turned an arbitrary issuer string into a live discovery call would be a
server-side request forgery primitive, which is why the trusted-issuer factories exist.

Where tenants map onto distinct URL spaces, **separate `SecurityFilterChain` beans with distinct
`securityMatcher`s** are usually better, because the policies stay visibly separate. The same shape
applies to an application serving both an administrative interface for humans and a JSON API:

```java
@Bean
@Order(1)
SecurityFilterChain apiChain(HttpSecurity http) throws Exception {
    http.securityMatcher("/api/**")                 // ONLY the API
        .authorizeHttpRequests(auth -> auth.anyRequest().hasAuthority("SCOPE_api"))
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .csrf(CsrfConfigurer::disable)              // no ambient credentials on this chain
        .oauth2ResourceServer(o -> o.jwt(Customizer.withDefaults()));
    return http.build();
}

@Bean
@Order(2)
SecurityFilterChain webChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
        .oauth2Login(Customizer.withDefaults());    // needs a session; see topic 10.2
    return http.build();
}
```

Three things people get wrong. The API chain must come **first**, because the first matching chain
wins and a chain with no `securityMatcher` swallows everything. Disabling CSRF protection is correct
**only** on the bearer-token chain, because a header token is not ambient — the rule from
[`01_M1_T1_HTTP_Web_Basics.md`](01_M1_T1_HTTP_Web_Basics.md) is "no cookie-based credentials means no
CSRF", not "stateless means no CSRF". And `oauth2Login` cannot be stateless, for the reasons in
[`30_M10_T2_OAuth2_Client.md`](30_M10_T2_OAuth2_Client.md).

---

## Working Code

```java
package com.example.resourceserver.config;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorityAuthorizationManager;
import org.springframework.security.authorization.AuthorizationManagers;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.SupplierJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class ResourceServerConfig {

    private final String issuerUri;
    private final String audience;

    public ResourceServerConfig(@Value("${app.oauth2.issuer-uri}") String issuerUri,
                                @Value("${app.oauth2.audience}") String audience) {
        this.issuerUri = issuerUri;
        this.audience = audience;
    }

    /** The API chain. Most specific matcher, so it is ordered first. */
    @Bean
    @Order(1)
    SecurityFilterChain apiSecurityFilterChain(HttpSecurity http,
                                               JwtAuthenticationConverter converter) throws Exception {
        http
            .securityMatcher("/api/**")
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/api/orders/**").hasAuthority("SCOPE_orders:read")
                .requestMatchers(HttpMethod.POST, "/api/orders/**").hasAuthority("SCOPE_orders:write")
                // Role AND delegated scope: the intersection, never the union.
                .requestMatchers("/api/admin/**").access(AuthorizationManagers.allOf(
                        AuthorityAuthorizationManager.hasRole("ADMIN"),
                        AuthorityAuthorizationManager.hasAuthority("SCOPE_admin:write")))
                .anyRequest().authenticated()
            )
            // No session: the token is the whole state.
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // Safe ONLY because the credential is a header, never a cookie.
            .csrf(CsrfConfigurer::disable)
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(converter)));
        return http.build();
    }

    /**
     * Wrapped in a SupplierJwtDecoder so OIDC discovery happens on the FIRST REQUEST rather than
     * at context refresh. Without the wrapper, declaring a decoder bean makes the identity
     * provider a hard startup dependency.
     */
    @Bean
    JwtDecoder jwtDecoder() {
        return new SupplierJwtDecoder(() -> {
            NimbusJwtDecoder decoder = NimbusJwtDecoder.withIssuerLocation(this.issuerUri).build();
            decoder.setJwtValidator(tokenValidator());
            return decoder;
        });
    }

    private OAuth2TokenValidator<Jwt> tokenValidator() {
        OAuth2TokenValidator<Jwt> defaults = new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(Duration.ofSeconds(60)),
                new JwtIssuerValidator(this.issuerUri));

        // THE CHECK MISSING BY DEFAULT: this token was minted for THIS api.
        OAuth2TokenValidator<Jwt> audienceValidator =
                new JwtClaimValidator<List<String>>(JwtClaimNames.AUD,
                        aud -> aud != null && aud.contains(this.audience));

        // Reject an ID token structurally: an access token carries no nonce.
        OAuth2TokenValidator<Jwt> notAnIdToken =
                new JwtClaimValidator<String>("nonce", nonce -> nonce == null);

        return new DelegatingOAuth2TokenValidator<>(defaults, audienceValidator, notAnIdToken);
    }

    /** Scopes stay as SCOPE_*; provider roles are added as ROLE_*. Both are needed. */
    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter scopes = new JwtGrantedAuthoritiesConverter();
        scopes.setAuthorityPrefix("SCOPE_");
        scopes.setAuthoritiesClaimName("scope");

        Converter<Jwt, Collection<GrantedAuthority>> combined = jwt -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>(scopes.convert(jwt));
            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");   // Keycloak
            if (realmAccess != null && realmAccess.get("roles") instanceof Collection<?> roles) {
                roles.stream()
                     .map(String::valueOf)
                     .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                     .forEach(authorities::add);
            }
            List<String> permissions = jwt.getClaimAsStringList("permissions");    // Auth0
            if (permissions != null) {
                permissions.stream().map(SimpleGrantedAuthority::new).forEach(authorities::add);
            }
            return authorities;
        };

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(combined);
        converter.setPrincipalClaimName("sub");
        return converter;
    }
}
```

The opaque-token alternative, with caching:

```java
package com.example.resourceserver.config;

import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.introspection.SpringOpaqueTokenIntrospector;
import org.springframework.util.DigestUtils;

@Configuration
public class OpaqueTokenConfig {

    @Bean
    SpringOpaqueTokenIntrospector delegateIntrospector(
            @Value("${app.oauth2.introspection-uri}") String uri,
            @Value("${app.oauth2.introspection-client-id}") String clientId,
            @Value("${app.oauth2.introspection-secret}") String secret) {
        return new SpringOpaqueTokenIntrospector(uri, clientId, secret);
    }

    /**
     * Introspection is a network call per request. Cache successes for a SHORT window so
     * revocation stays meaningful, and key on a HASH so a cache dump is not a credential dump.
     */
    @Bean
    OpaqueTokenIntrospector cachingIntrospector(CacheManager cacheManager,
                                                SpringOpaqueTokenIntrospector delegate) {
        Cache cache = cacheManager.getCache("introspection");   // configure ttl <= 60s
        return token -> {
            String key = DigestUtils.md5DigestAsHex(token.getBytes(StandardCharsets.UTF_8));
            OAuth2AuthenticatedPrincipal cached = cache.get(key, OAuth2AuthenticatedPrincipal.class);
            if (cached != null) {
                return cached;
            }
            OAuth2AuthenticatedPrincipal principal = delegate.introspect(token);
            cache.put(key, principal);      // successes only; failures are never cached
            return principal;
        };
    }
}
```

```java
package com.example.resourceserver.web;

import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderRepository orders;

    public OrderController(OrderRepository orders) {
        this.orders = orders;
    }

    @GetMapping
    public Map<String, Object> list(@AuthenticationPrincipal Jwt jwt) {
        return Map.of("subject", jwt.getSubject(),
                      "issuer", jwt.getIssuer().toString(),
                      "audience", jwt.getAudience(),
                      "scopes", jwt.getClaimAsStringList("scope"));
    }

    /**
     * Scope cannot express ownership. This is the BOLA / confused-deputy fix from topic 1.2:
     * the subject from the token is pushed into the query.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_orders:read')")
    public Order get(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return this.orders.findByIdAndOwnerSubject(id, jwt.getSubject())
                          .orElseThrow(OrderNotFoundException::new);   // 404, never 403
    }
}
```

```java
package com.example.resourceserver.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerAuthenticationManagerResolver;

@Configuration
public class MultiTenantConfig {

    @Bean
    AuthenticationManagerResolver<HttpServletRequest> tenantResolver() {
        // A FIXED trust list. The unverified iss claim only SELECTS from this list; the
        // selected manager then verifies the signature and re-checks the issuer.
        return JwtIssuerAuthenticationManagerResolver.fromTrustedIssuers(
                "https://tenant-a.idp.example.com",
                "https://tenant-b.idp.example.com");
    }
}
```

```yaml
app:
  oauth2:
    issuer-uri: https://idp.example.com/realms/corporate
    audience: https://orders-api.example.com

spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${app.oauth2.issuer-uri}
          audiences: ${app.oauth2.audience}      # the check that is missing by default
          jws-algorithms: [RS256]                # pin it; never trust the token's alg header

server:
  forward-headers-strategy: framework

logging:
  level:
    org.springframework.security.oauth2.server.resource: DEBUG
```

Tests. The key idea is that authorization rules should not need a live identity provider:
`SecurityMockMvcRequestPostProcessors.jwt()` builds a `JwtAuthenticationToken` directly and bypasses
the decoder.

```java
package com.example.resourceserver;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;   // @MockitoBean from Boot 3.4
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ResourceServerTests {

    @Autowired MockMvc mvc;

    /** Stops the context reaching the identity provider; also lets us drive decoder failures. */
    @MockBean JwtDecoder jwtDecoder;

    @Test
    void noTokenIsUnauthorizedWithABearerChallenge() throws Exception {
        mvc.perform(get("/api/orders"))
           .andExpect(status().isUnauthorized())
           .andExpect(header().string("WWW-Authenticate", startsWith("Bearer")));
    }

    @Test
    void correctScopeIsAllowed() throws Exception {
        mvc.perform(get("/api/orders")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_orders:read"))))
           .andExpect(status().isOk());
    }

    @Test
    void wrongScopeIsForbiddenWithInsufficientScope() throws Exception {
        mvc.perform(post("/api/orders")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_orders:read"))))
           .andExpect(status().isForbidden())
           .andExpect(header().string("WWW-Authenticate", containsString("insufficient_scope")));
    }

    @Test
    void adminNeedsBothTheRoleAndTheDelegatedScope() throws Exception {
        // Role without scope: the client was never delegated this.
        mvc.perform(get("/api/admin/users")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
           .andExpect(status().isForbidden());
        // Scope without role: the human is not an admin.
        mvc.perform(get("/api/admin/users")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_admin:write"))))
           .andExpect(status().isForbidden());
        // Both.
        mvc.perform(get("/api/admin/users")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"),
                                                new SimpleGrantedAuthority("SCOPE_admin:write"))))
           .andExpect(status().isOk());
    }

    @Test
    void aTokenTheDecoderRejectsProducesInvalidToken() throws Exception {
        BDDMockito.given(this.jwtDecoder.decode("bad-token"))
                  .willThrow(new JwtException("Invalid signature"));

        mvc.perform(get("/api/orders").header("Authorization", "Bearer bad-token"))
           .andExpect(status().isUnauthorized())
           .andExpect(header().string("WWW-Authenticate", containsString("invalid_token")));
    }
}
```

The validator deserves its own test, with no web layer at all:

```java
package com.example.resourceserver;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;

class AudienceValidatorTests {

    private final OAuth2TokenValidator<Jwt> validator =
            new JwtClaimValidator<List<String>>(JwtClaimNames.AUD,
                    aud -> aud != null && aud.contains("https://orders-api.example.com"));

    @Test
    void acceptsATokenMintedForThisApi() {
        assertThat(this.validator.validate(jwtFor("https://orders-api.example.com"))
                                 .hasErrors()).isFalse();
    }

    @Test
    void rejectsATokenMintedForAnotherService() {
        assertThat(this.validator.validate(jwtFor("https://wiki-api.example.com"))
                                 .hasErrors()).isTrue();
    }

    @Test
    void rejectsAnIdTokenWhoseAudienceIsAClientId() {
        assertThat(this.validator.validate(jwtFor("my-web-app-client-id"))
                                 .hasErrors()).isTrue();
    }

    private Jwt jwtFor(String audience) {
        return Jwt.withTokenValue("token")
                  .header("alg", "RS256")
                  .subject("248289761001")
                  .audience(List.of(audience))
                  .issuedAt(Instant.now())
                  .expiresAt(Instant.now().plusSeconds(300))
                  .build();
    }
}
```

---

## Internals

```java
// BearerTokenAuthenticationFilter (simplified)
protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {
    String token;
    try {
        token = this.bearerTokenResolver.resolve(request);
    }
    catch (OAuth2AuthenticationException invalid) {
        this.authenticationEntryPoint.commence(request, response, invalid);
        return;
    }
    if (token == null) {
        // No token is NOT a failure here. AuthorizationFilter decides later.
        filterChain.doFilter(request, response);
        return;
    }
    BearerTokenAuthenticationToken authenticationRequest = new BearerTokenAuthenticationToken(token);
    authenticationRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
    try {
        AuthenticationManager authenticationManager =
                this.authenticationManagerResolver.resolve(request);
        Authentication result = authenticationManager.authenticate(authenticationRequest);

        SecurityContext context = this.securityContextHolderStrategy.createEmptyContext();
        context.setAuthentication(result);
        this.securityContextHolderStrategy.setContext(context);
        this.securityContextRepository.saveContext(context, request, response);

        filterChain.doFilter(request, response);
    }
    catch (AuthenticationException failed) {
        this.securityContextHolderStrategy.clearContext();
        this.authenticationFailureHandler.onAuthenticationFailure(request, response, failed);
    }
}
```

The filter uses an `AuthenticationManagerResolver`, not a plain `AuthenticationManager`, which is what
makes multi-tenancy first-class. And the default repository on a stateless chain is
`RequestAttributeSecurityContextRepository`, which holds the context for the request and writes
nothing to a session.

```java
// JwtAuthenticationProvider (simplified)
private Jwt getJwt(BearerTokenAuthenticationToken bearer) {
    try {
        return this.jwtDecoder.decode(bearer.getToken());
    }
    catch (BadJwtException failed) {
        // Decoding or validation failed -> invalid_token, 401
        throw new InvalidBearerTokenException(failed.getMessage(), failed);
    }
    catch (JwtException failed) {
        // Infrastructure failure (JWKS unreachable) -> 500, NOT 401
        throw new AuthenticationServiceException(failed.getMessage(), failed);
    }
}
```

**That split matters.** A `BadJwtException` means the *token* is bad and the caller can act on it, so
it becomes `401` with `invalid_token`. A generic `JwtException` means *we* could not do our job — the
JWK Set endpoint timed out — and a `401` there would be a lie telling the client to refresh a
perfectly good token. A burst of `500`s from a resource server means JWKS connectivity, not bad
tokens.

```java
// NimbusJwtDecoder (simplified)
@Override
public Jwt decode(String token) throws JwtException {
    JWT jwt = parse(token);                          // structural parse only
    if (jwt instanceof PlainJWT) {
        // alg: none is rejected outright, before key selection happens.
        throw new BadJwtException("Unsupported algorithm of none");
    }
    Jwt createdJwt = createJwt(token, jwt);          // JWSVerificationKeySelector -> kid lookup
    return validateJwt(createdJwt);                  // the OAuth2TokenValidator chain
}

// DelegatingOAuth2TokenValidator (simplified) - note: it does NOT short-circuit
@Override
public OAuth2TokenValidatorResult validate(T token) {
    Collection<OAuth2Error> errors = new ArrayList<>();
    for (OAuth2TokenValidator<T> validator : this.tokenValidators) {
        errors.addAll(validator.validate(token).getErrors());
    }
    return OAuth2TokenValidatorResult.failure(errors);
}
```

Because every delegate runs, a single rejection message can name both a bad audience and an expired
token — and a validator must be cheap, since it executes even when an earlier check already failed.

```java
// SpringOpaqueTokenIntrospector (simplified)
public OAuth2AuthenticatedPrincipal introspect(String token) {
    ResponseEntity<Map<String, Object>> responseEntity = makeRequest(token);   // POST token=...
    Map<String, Object> claims = adaptToNimbusResponse(responseEntity);
    if (!(boolean) claims.get(OAuth2TokenIntrospectionClaimNames.ACTIVE)) {
        throw new BadOpaqueTokenException("Provided token is not active");
    }
    return convertClaimsSet(claims);   // -> OAuth2IntrospectionAuthenticatedPrincipal
}
```

The `active` field is the whole protocol: RFC 7662 says the response for an invalid, expired or
revoked token is simply `{"active": false}` with a `200`, so introspection cannot be used as an
oracle. The resulting `Authentication` is a `BearerTokenAuthentication`, so code written against
`@AuthenticationPrincipal Jwt` breaks if you switch strategies — program against
`OAuth2AuthenticatedPrincipal` when both are possible.

---

## Configuration Reference

| Option | Effect | Default |
|---|---|---|
| `spring.security.oauth2.resourceserver.jwt.issuer-uri` | discovery for the JWKS **and** a `JwtIssuerValidator` | unset |
| `...jwt.jwk-set-uri` | keys only — **no issuer validation** | unset |
| `...jwt.public-key-location` | one static RSA public key; no rotation | unset |
| `...jwt.audiences` | validates that `aud` contains one of the listed values | **unset — set it** |
| `...jwt.jws-algorithms` | accepted signature algorithms | `RS256` |
| `...opaquetoken.introspection-uri` | RFC 7662 endpoint | unset |
| `...opaquetoken.client-id` / `client-secret` | credentials for introspection | unset |
| `JwtTimestampValidator` clock skew | tolerance on `exp` and `nbf` | **60 seconds** |
| `JwtGrantedAuthoritiesConverter.setAuthorityPrefix` | prefix on scope-derived authorities | `SCOPE_` |
| `JwtGrantedAuthoritiesConverter.setAuthoritiesClaimName` | which claim holds the authorities | `scope`, then `scp` |
| `JwtAuthenticationConverter.setPrincipalClaimName` | which claim becomes `getName()` | `sub` |
| `DefaultBearerTokenResolver.setAllowFormEncodedBodyParameter` | accept the token in a form body | `false` — **leave it** |
| `DefaultBearerTokenResolver.setAllowUriQueryParameter` | accept the token in a query parameter | `false` — **leave it** |
| `DefaultBearerTokenResolver.setBearerTokenHeaderName` | header carrying the token | `Authorization` |
| `oauth2ResourceServer().authenticationEntryPoint(...)` | 401 response shape | `BearerTokenAuthenticationEntryPoint` |
| `oauth2ResourceServer().accessDeniedHandler(...)` | 403 response shape | `BearerTokenAccessDeniedHandler` |
| `oauth2ResourceServer().authenticationManagerResolver(...)` | multi-tenant dispatch | a single manager |
| `NimbusJwtDecoder.JwkSetUriJwtDecoderBuilder.cache(Cache)` | Spring `Cache` for the JWK set | Nimbus in-memory cache |

---

## Production Concerns & Anti-Patterns

**Not validating the audience.** The headline item. Configuring only `issuer-uri` means the service
accepts any token that authority ever minted, for any client and any sibling service — including ID
tokens, whose audience is a public client ID. Set `audiences` or add a `JwtClaimValidator` for `aud`
on **every** resource server, and treat it as a deployment checklist item rather than a per-team
decision.

**Declaring a `JwtDecoder` bean without thinking about startup.** Boot's auto-configuration is lazy
through `SupplierJwtDecoder`; your own bean built with `JwtDecoders.fromIssuerLocation` or
`NimbusJwtDecoder.withIssuerLocation(...).build()` performs discovery during context refresh and
makes the identity provider a startup dependency.

**Enabling the query-parameter bearer token resolver.** It puts credentials into access logs, proxy
logs, browser history and `Referer` headers, and OAuth 2.1 removes the option entirely. If a client
cannot set a header, fix the client.

**Treating `alg` in the token header as an instruction.** Pin the algorithms. `NimbusJwtDecoder`
rejects `alg: none` before anything else, but an unpinned decoder accepting both symmetric and
asymmetric algorithms is open to the confusion attack where a public key is used as an HMAC secret.

**Very long access token lifetimes.** With local validation, `exp` *is* your revocation window. A
one-hour token means a stolen token works for up to an hour after you disable the account. Five to
fifteen minutes is the normal range.

**Introspecting on every request without caching.** Each call adds a round trip and makes the
authorization server a synchronous dependency of every service. Cache successes briefly on a hashed
key, or reconsider whether you wanted opaque tokens.

**Relying on scope for object-level authorization.** `SCOPE_orders:read` says the client may read
orders; it says nothing about *which* orders. This is the confused deputy from
[`02_M1_T2_Authentication_Authorization.md`](02_M1_T2_Authentication_Authorization.md), and the fix
is the same: push the subject into the query, and return `404` rather than `403` when existence is
itself sensitive.

**Assuming `sub` is globally unique.** It is unique within an issuer. In a multi-tenant or
multi-provider deployment the key is `(iss, sub)`, as in
[`30_M10_T2_OAuth2_Client.md`](30_M10_T2_OAuth2_Client.md).

**Logging the token.** `DEBUG` or `TRACE` on the resource-server packages writes bearer tokens into
log aggregation, searchable and retained for months. Log `jti` and `sub`, never the token value.

**Leaving CSRF protection disabled on a chain that also accepts cookies.** Disabling it is correct
for a pure bearer-token chain. The moment a gateway or backend-for-frontend introduces a cookie onto
the same chain, the exemption is wrong.

**Putting a large authority list in the token.** Kilobyte tokens on every request hit proxy header
limits — nginx defaults near 8 KB — producing intermittent `400`s that look like network faults.

---

## Debugging Playbook

| Symptom | Likely root cause | Fix |
|---|---|---|
| `401` `invalid_token`, "Signed JWT rejected: Invalid signature" | Wrong JWKS, wrong issuer, or the signing key rotated out before the cache refreshed | Verify `jwk-set-uri` resolves; check the token's `kid` is present in the JWK set |
| `401` "Jwt expired at ..." | Genuinely expired, or clock drift greater than the 60-second skew | Fix NTP on the hosts; raise the skew only as a last resort |
| `401` "The iss claim is not valid" | Internal versus external hostname mismatch between the container and the browser | Configure the identity provider's frontend URL so the issuer string is identical everywhere |
| `403` `insufficient_scope` | Token is valid; the required authority is absent | Print `authentication.getAuthorities()`; check the prefix and claim name in the converter |
| `403` on everything despite a valid token | Authorities not extracted — roles sit in `realm_access.roles` and the default converter reads only `scope`/`scp` | Supply a `JwtAuthenticationConverter` with a combined authorities converter |
| Startup fails, "Unable to resolve Configuration with the provided Issuer" | A `JwtDecoder` bean performs discovery at context refresh and the provider is unreachable | Wrap in `SupplierJwtDecoder`, use `jwk-set-uri`, or contribute only a validator bean |
| `500` instead of `401` on some requests | A `JwtException` that is not a `BadJwtException` — usually the JWK Set endpoint timing out | Check connectivity and timeouts to `jwk-set-uri`; alert on this separately from `401` |
| Everything fails for minutes right after a key rotation | The new key was published and used for signing at the same moment | Publish, wait one cache lifespan, then switch signing; remove the old key after the maximum token lifetime |
| A burst of JWKS fetches under attack traffic | Tokens with random `kid` values forcing refreshes | Confirm the rate limiter is active; consider a Spring `Cache` on the decoder builder |
| Token works against service A and also against service B | No audience validation on B | Add `audiences`; this is a vulnerability, not a convenience |
| An ID token is accepted by the API | No audience validation, and the API trusts the same issuer | Validate `aud` against the resource identifier; reject tokens carrying a `nonce` claim |
| `400` "Found multiple bearer tokens in the request" | A token in both the header and a parameter, usually a gateway adding one | Send exactly one; keep the parameter resolvers disabled |
| `@AuthenticationPrincipal Jwt` is null after switching to opaque tokens | The principal is now `OAuth2IntrospectionAuthenticatedPrincipal` inside a `BearerTokenAuthentication` | Program against `OAuth2AuthenticatedPrincipal` |

---

## Interview Q&A

### Q1. Compare JWT validation and token introspection for a resource server. How do you choose?

<details>
<summary>Show answer</summary>

With `jwt(...)` the token is a self-contained JWS. The resource server verifies the signature against
public keys cached from the JWK Set endpoint, then checks `exp`, `nbf`, `iss` and — if you configured
it — `aud`. There is **no network call on the request path** once the keys are cached; validation is
an RSA verification, measured in microseconds.

With `opaqueToken(...)` the token is a meaningless handle and the resource server posts it to the
introspection endpoint (RFC 7662) with its own client credentials, receiving the claim set plus an
`active` flag. That is a full round trip on **every** request.

The trade-off comes down to two axes. **Revocation**: with a JWT a revoked token remains valid until
`exp`, so the revocation window is a parameter you set by choosing the token lifetime; with
introspection it is immediate and exact. **Coupling and cost**: with a JWT each resource server is
independent and survives an authorization server outage; with introspection the authorization server
becomes a synchronous dependency of every request in the estate, carrying the sum of all inbound
traffic, and its availability bounds everything else.

My default for an internal microservice estate is JWT with short lifetimes, because I do not want
forty services unable to serve traffic during a ten-minute identity provider incident. For a small
number of high-value, low-volume operations — payments, administrative actions — introspection, or a
hybrid where most requests validate locally and those specific operations introspect.

**Counter-question: describe the introspection cache precisely, including what you would get wrong if careless.**

Key on the **token value**, or a hash of it. The careless version keys on the subject, which is wrong
because two tokens for the same user can carry different scopes and audiences, so you would serve one
token's permissions for another token's request.

The time-to-live is the minimum of a short fixed window — thirty to sixty seconds — and the token's
remaining lifetime from the `exp` in the response. That short window is what keeps the revocation
guarantee meaningful: the worst case becomes "revoked access persists for up to one window" rather
than "until expiry", which is the property you chose introspection for. A five-minute cache quietly
throws it away.

Cache **successes only** — caching a negative result means a token that becomes valid after a clock
correction stays rejected for the window. Store the key as a hash, so a Redis dump or a monitoring
export is not a credential dump. And recognise the cache is a second place where an authorization
decision lives, so revocation or logout events must invalidate it.

**Counter-question: the introspection endpoint goes down. Fail open or fail closed?**

Fail closed, without hesitation. Failing open turns an availability incident into an authentication
bypass across the estate, which is never the right trade.

The nuance is the *status code*. Failing to reach the endpoint is not "your token is bad" — it is "we
cannot answer right now". Returning `401` with `invalid_token` tells the client to discard a good
token and fetch a new one, sending a stampede at the authorization server that is already
struggling. It should surface as a `503` or `500` so clients back off. Spring already models this:
`BadOpaqueTokenException` and `BadJwtException` mean the token is bad and produce `401`, while other
failures become `AuthenticationServiceException` and produce a server error. Around it I would put a
circuit breaker with a short timeout so requests fail fast rather than piling up threads, and alert on
the breaker rather than the resulting error rate, which is a lagging indicator.

**Counter-question: can you get both local validation and exact revocation?**

Not exactly both, and the honest answer names which property you give up. The approach I would take is
**short tokens plus pushed revocation**: access tokens live sixty seconds and are validated locally,
so the hot path stays free of network calls, while the authorization server publishes revoked token or
subject identifiers onto a topic that every resource server consumes into a small in-memory denylist.
Entries expire when the token would have expired anyway, so the structure stays tiny — bounded state,
not a session store — and if the topic lags the failure mode is graceful, falling back to the
sixty-second natural expiry.

What you do not get is a guarantee that revocation has taken effect at a particular instant, because a
service that has not yet received the message still accepts the token. If the requirement is genuinely
"provably revoked at time T", introspection is the only answer, and I would say so rather than dress up
an eventually-consistent design as a strongly-consistent one.
</details>

### Q2. A token minted for service A is accepted by service B. How does that happen and how do you prevent it?

<details>
<summary>Show answer</summary>

Because both services validate the same things and none of those things distinguish them. A resource
server configured with only `issuer-uri` gets two validators: `JwtTimestampValidator` for `exp` and
`nbf`, and `JwtIssuerValidator` for `iss`. A token minted for service A by the shared authorization
server has a valid signature, because both trust the same JWK set; a matching issuer, because both
trust the same authority; and an unexpired timestamp. Every check passes. The only claim that ever
said "this is for service A" was `aud`, and **audience is not validated by default**.

The consequences are worse than one service accepting another's token. Any component that legitimately
receives tokens becomes a pivot — a compromised low-value service, or a third-party integration you
deliberately issue tokens to, can replay them against payments. An **ID token** becomes an API
credential, since its `aud` is a client ID nobody compares to anything. And with a shared identity
provider, a token from a different tenant may be accepted.

The fix has two halves. The authorization server must mint tokens with a meaningful `aud`, usually by
the client requesting a `resource` or `audience` parameter naming the target API. And every resource
server must validate it — `spring.security.oauth2.resourceserver.jwt.audiences`, or a
`JwtClaimValidator<List<String>>` on `AUD` composed with the defaults through a
`DelegatingOAuth2TokenValidator`.

**Counter-question: why wasn't audience validation on by default? That seems like an obvious gap.**

Because the framework cannot know the value. `iss` and `exp` are self-describing — the decoder knows
the configured issuer and the current time — but the audience is a name for *this* service that only
the deployment knows, and there is no safe default. "Require `aud` to be present" would break every
provider that omits it; "accept any `aud`" is what we have; and guessing from the application name
would be worse than nothing because it would appear to work.

There is also history: RFC 6749 left the access token format entirely unspecified, so `aud` semantics
were not standardised until RFC 9068, which is recent, and providers genuinely differ in what they
put there. What has changed is the ergonomics — the `audiences` property makes it one line instead of
a custom bean, which removes the last excuse. I treat it as a deployment checklist item enforced by a
shared configuration template, because one team forgetting re-opens the hole for everyone.

**Counter-question: how do you actually get the right `aud` into the token in the first place?**

It depends on the provider, and this is where the detail lives. The standard mechanism is RFC 8707
**resource indicators**: the client adds a `resource` parameter naming the target API, and the
authorization server mints a token audienced for it. Auth0 uses an `audience` parameter for the same
purpose, Entra ID encodes it through scopes namespaced by the target application's identifier URI, and
Keycloak uses an audience mapper on a client scope. On the Spring client side, adding a non-standard
parameter to the authorization request means an `OAuth2AuthorizationRequestCustomizer` or a custom
resolver; adding one to a token request means customising the request entity converter.

The implication people miss is that **one token cannot serve two APIs** once you do this properly. A
client calling payments and orders needs two tokens, or a token exchange to swap one for the other.
That feels like friction, and it is precisely the friction that makes the audience boundary real.

**Counter-question: how would you make it structurally impossible for an ID token to be accepted by your API?**

Audience validation already does most of it: an ID token's `aud` is a client ID, which never equals
the resource identifier.

Two reinforcements on top. RFC 9068 defines a `typ` header of `at+jwt` for JWT access tokens; if the
provider emits it, validate it — Spring's `JwtTypeValidator`, controlled by the decoder builder's
`validateTypes`, does exactly that and later versions apply it by default, so a token with `typ: JWT`
rather than `at+jwt` is rejected structurally. The cheap belt-and-braces check is the `nonce` claim: an
ID token from an authorization code flow carries one and an access token does not, so a
`JwtClaimValidator` asserting `nonce` is absent costs nothing and covers providers that omit `typ`.

Architecturally, the best prevention is that the ID token never leaves the client that received it —
the rule in [`29_M10_T1_OAuth2_OIDC_Fundamentals.md`](29_M10_T1_OAuth2_OIDC_Fundamentals.md), which a
backend-for-frontend gives you free because the browser holds neither token.
</details>

### Q3. Your resource server will not start because the identity provider is down. Explain exactly why, and how you avoid it.

<details>
<summary>Show answer</summary>

The precise cause matters, because "Spring resource servers always fail to start when the identity
provider is down" has not been true for several versions.

Boot's **auto-configured** decoder is lazy. With only `issuer-uri` set and no `JwtDecoder` bean of
your own, Boot wraps the construction in a `SupplierJwtDecoder`, so the discovery request to
`/.well-known/openid-configuration` and the JWK Set fetch happen on the **first request carrying a
JWT**, not at context refresh. The application starts fine with the provider down; the first
authenticated request is what fails or blocks.

What reintroduces the coupling is **declaring your own `JwtDecoder` bean**, which almost every real
application ends up doing for an audience validator, a non-default algorithm, or a cache. Both
`JwtDecoders.fromIssuerLocation(issuer)` and `NimbusJwtDecoder.withIssuerLocation(issuer).build()`
perform the discovery call while the bean is being created, because that call is how the JWK Set URI
is derived. Now the provider is a startup dependency and a rolling deployment during a provider
incident produces pods that will not come up.

Three ways to avoid it, in the order I would consider them. **Contribute only an
`OAuth2TokenValidator<Jwt>` bean** and let Boot build the decoder — you get your audience check and
keep laziness, with the least configuration. **Wrap your construction in a `SupplierJwtDecoder`**,
which is the documented way to preserve deferral when you must build the decoder yourself. Or **skip
discovery** by configuring `jwk-set-uri` directly, since Nimbus fetches keys lazily — but note you
lose the automatic `JwtIssuerValidator` and must add it yourself.

**Counter-question: is failing at startup actually worse than failing at the first request? At least it fails loudly.**

It is worse, and the reason is blast radius rather than loudness. A resource server that starts and
fails the first authenticated request still serves its health endpoint, still serves public endpoints,
still participates in service discovery, and — the important part — **recovers by itself** when the
provider returns, with no human involved. One that will not start is an outage requiring someone to
notice, diagnose and redeploy, and during a rolling deployment you can end up with zero healthy
instances of a service that was running perfectly a minute earlier.

There is a second-order effect that is worse still: it couples your deployment pipeline to a third
party's availability, so a provider incident becomes "we cannot deploy anything" at exactly the moment
you most want to. The fail-fast instinct is right for genuine misconfiguration — a typo in the issuer
URL should not wait for production traffic — but I would get that from a startup health check that
reports degraded without preventing the refresh, and from a smoke test in the pipeline, rather than
from bean construction.

**Counter-question: with the lazy decoder, what does the first request look like if the provider is slow?**

It blocks on the discovery call and then the JWKS fetch, serially, on the request thread. With default
timeouts that can be a long block, and under load several requests arriving together each wait — you
can exhaust the thread pool before anything times out.

Two mitigations. Set explicit, short connect and read timeouts on the `RestOperations` used for
discovery and JWKS, so failure is fast rather than indefinite; the decoder builder accepts a
customised `RestOperations` for exactly this. And warm it deliberately: an `ApplicationReadyEvent`
listener that decodes a dummy token on a background thread, catching and ignoring the failure,
triggers the fetch without blocking startup or a real request. That gives the benefit of eager
initialisation without the cost of eager failure.

**Counter-question: the JWK Set endpoint becomes unreachable an hour after startup. What happens?**

Nothing, until a key you do not have is needed. The set is cached in memory and Nimbus refreshes ahead
of expiry rather than on expiry, so a brief outage is usually invisible and tokens signed with cached
keys validate normally.

It breaks on an unknown `kid`. If the provider rotates during the outage, tokens arrive signed with a
key the cache lacks, the refresh fails, and those requests fail — as a `JwtException` that is not a
`BadJwtException`, so the caller sees a `500` rather than a `401`. That is correct, because the token
is not bad, we are. It is also the operational argument for the rotation procedure: publish the new
public key well before signing with it, so every cache picks it up while the endpoint is healthy. And
it is why I monitor JWKS fetch failures as a first-class signal rather than inferring them from an
error rate.
</details>

### Q4. Walk me through everything between `Authorization: Bearer eyJ...` arriving and your controller method running.

<details>
<summary>Show answer</summary>

`BearerTokenAuthenticationFilter` calls `BearerTokenResolver.resolve(request)`. The default matches the
`Authorization` header against `^Bearer (?<token>[a-zA-Z0-9-._~+/]+=*)$`, case-insensitively;
form-body and query-parameter resolution are off, and a token in more than one location throws
`invalid_request` per RFC 6750. With **no** token the filter simply continues the chain — a missing
token is not a failure here.

With a token, the filter builds a `BearerTokenAuthenticationToken` and resolves an
`AuthenticationManager` through an `AuthenticationManagerResolver`, the hook that makes multi-tenancy
first-class.

`JwtAuthenticationProvider` calls `JwtDecoder.decode`. `NimbusJwtDecoder` parses structurally, rejects
a `PlainJWT` outright so `alg: none` never reaches key selection, reads the `kid`, and selects the
matching key from the cached JWK set — fetching on an unknown `kid`, subject to a rate limiter. It
verifies using only the algorithms in its allowlist, `RS256` by default.

The validator chain then runs through `DelegatingOAuth2TokenValidator`, which executes **every**
delegate and collects all errors: `JwtTimestampValidator` with a sixty-second skew, `JwtIssuerValidator`,
`JwtTypeValidator` in later versions, and whatever audience or claim validators you added. Any error
becomes a `JwtValidationException`, a subclass of `BadJwtException`.

`JwtAuthenticationConverter` turns the `Jwt` into a `JwtAuthenticationToken`. Its
`JwtGrantedAuthoritiesConverter` reads `scope` or `scp`, splits on spaces and prefixes with `SCOPE_`;
your custom converter adds `ROLE_*` from the provider's roles claim. The principal name comes from
`principalClaimName`, `sub` by default.

The context is stored — on a stateless chain that is `RequestAttributeSecurityContextRepository`, so
nothing touches a session — and the chain continues. `AuthorizationFilter`, the last filter, evaluates
the rules; a denial throws `AccessDeniedException`, caught by `ExceptionTranslationFilter`, which
invokes `BearerTokenAccessDeniedHandler`. Then `DispatcherServlet` runs, `@PreAuthorize` is evaluated
by the method security interceptor, and the controller body executes.

**Counter-question: a valid token arrives for an endpoint the caller lacks scope for. Trace the response precisely.**

Authentication succeeds, so the context holds a fully authenticated `JwtAuthenticationToken`.
`AuthorizationFilter` evaluates the rule, the authority is absent, and it throws
`AuthorizationDeniedException`, which extends `AccessDeniedException`.

`ExceptionTranslationFilter` catches it and asks `AuthenticationTrustResolver` whether the
authentication is anonymous or remember-me. It is neither, so it does **not** start authentication and
instead calls the `AccessDeniedHandler` — `BearerTokenAccessDeniedHandler` on a resource server chain
— writing `403` with `WWW-Authenticate: Bearer error="insufficient_scope"`, an `error_description`, an
`error_uri`, and a `scope` parameter where the required scope is known.

The contrast with a **missing** token is the part worth stating: there the context holds an
`AnonymousAuthenticationToken`, the filter sees an anonymous denial, and it upgrades to an
authentication challenge — `401` with a bare `WWW-Authenticate: Bearer`. Same filter, same exception
type, two responses, decided entirely by the trust level of the current authentication. That is the
mechanism in [`01_M1_T1_HTTP_Web_Basics.md`](01_M1_T1_HTTP_Web_Basics.md), and "my API returns the
wrong status code" almost always resolves here.

**Counter-question: why is a `403` from `AuthorizationFilter` not catchable by `@RestControllerAdvice`, but one from `@PreAuthorize` is?**

Different layers. `AuthorizationFilter` is a servlet filter running **before** `DispatcherServlet`
exists in the call stack, and `@ControllerAdvice` is Spring MVC machinery that only sees exceptions
thrown inside the dispatch. A filter-layer exception never reaches it, so filter-level denials must be
shaped by `AccessDeniedHandler` and `AuthenticationEntryPoint`. `@PreAuthorize` is evaluated by an AOP
interceptor around the controller method, well inside the dispatch, so its exception propagates through
`DispatcherServlet` to the `HandlerExceptionResolver` chain including your advice.

The practical consequence is that one logical "access denied" can produce two different response bodies
in one application. On a resource server that matters more than usual, because clients parse the RFC
6750 `WWW-Authenticate` header to decide whether to refresh. I either keep denials at the filter layer
for anything a client must react to, or make the advice emit the identical body and header shape.

**Counter-question: where would you add a filter that needs the authenticated token, and what breaks if you get the position wrong?**

After `BearerTokenAuthenticationFilter` and before `AuthorizationFilter` — in practice
`http.addFilterAfter(myFilter, BearerTokenAuthenticationFilter.class)`. That window is where the
context is populated and still guaranteed to be cleaned up.

Too early, and `getAuthentication()` is anonymous or null, so any token-dependent logic silently does
the wrong thing — "silently" being the problem, since a rate limiter keyed on the subject would bucket
everything under anonymous. After `AuthorizationFilter`, and it never runs for denied requests, which
is usually the opposite of what an audit or rate-limiting filter wants.

The one thing I would not do is put authorization logic in a custom filter when `authorizeHttpRequests`
or an `AuthorizationManager` can express it — a bespoke filter is invisible to anyone auditing the
security configuration, which is the maintainability argument from
[`02_M1_T2_Authentication_Authorization.md`](02_M1_T2_Authentication_Authorization.md).
</details>

### Q5. How do you test a resource server without a live identity provider, and what does each technique actually cover?

<details>
<summary>Show answer</summary>

Three layers, each covering something different; the mistake is using one and believing it covers all
three.

**`SecurityMockMvcRequestPostProcessors.jwt()`** builds a `JwtAuthenticationToken` directly and
**bypasses the decoder entirely** — no signature, no claims validation, no converter. It tests your
*authorization rules*: does this endpoint require `SCOPE_orders:write`, does the admin endpoint demand
both role and scope, does ownership filtering work. That is the bulk of what you want covered, in
milliseconds with no network. Note that `.with(jwt().authorities(...))` sets authorities directly, so
it does **not** exercise your `JwtAuthenticationConverter`.

**A mocked `JwtDecoder` bean** — `@MockBean`, or `@MockitoBean` from Boot 3.4 — serves two purposes: it
stops the context reaching the identity provider at startup, which is necessary even when every test
bypasses the decoder because the bean is still required; and it lets you drive a real token *string*
through the filter and control the outcome, which is how you assert the `401` `invalid_token` shape and
the `WWW-Authenticate` header.

**Unit tests on the validator and the converter.** `Jwt.withTokenValue(...).claim(...).build()`
constructs a `Jwt` with no cryptography, so you can assert directly that your audience validator rejects
a token for another service, rejects an ID token whose audience is a client ID, and that your converter
maps `realm_access.roles` correctly. These would have caught the audience gap, and they are the ones
people skip.

**Counter-question: none of that proves a real token from your real identity provider is accepted. How do you cover it?**

With a small number of integration tests using real cryptography but no real provider. Generate an RSA
key pair in the test, sign a JWT with it using Nimbus, and serve a JWK set containing the matching
public key from a `MockWebServer` or WireMock stub pointed at by `jwk-set-uri`. That exercises the
genuine path — `kid` selection, signature verification, the validator chain, the converter — with full
control over claims, so you can assert that a wrong audience, a bad issuer, an expired token and an
absent `kid` each fail as expected.

At the top of the pyramid, Testcontainers with a real Keycloak checks that your issuer configuration and
claim mapping match what the provider actually emits, which is what breaks when someone changes a client
scope in the provider's console. That is slow, so I keep it to a handful of cases outside the inner loop.
What I avoid is an integration test against the shared development identity provider: it fails for
reasons unrelated to the change and makes the build depend on a system nobody owns.

**Counter-question: you mock the `JwtDecoder` to stop the context calling the provider. Can you avoid needing that?**

Yes, and for most test classes it is cleaner. Point `jwk-set-uri` at a local stub in the test profile, or
use `public-key-location` with a test key on the classpath — both avoid discovery entirely, so the
context builds with no network access and without a mock in every class.

There is also the earlier point: Boot's auto-configured decoder is lazy, so with only `issuer-uri` set
and no decoder bean of your own, a context that never decodes a token never calls the provider. The mock
becomes necessary mainly when the application declares its own eager decoder bean — another small
argument for the `SupplierJwtDecoder` wrapper, which makes the test story easier too. Where I still want
the mock is when asserting decoder *failure* behaviour, since stubbing `decode` to throw is far more
direct than constructing a token that fails in exactly the way I want.

**Counter-question: your tests all pass and production returns 403 on every request. What did they miss?**

Almost certainly the **authorities mapping**, because `.with(jwt().authorities(...))` supplies
authorities directly and therefore asserts nothing about how they are derived from a real token. The
tests proved that an endpoint requiring `SCOPE_orders:read` admits a principal holding
`SCOPE_orders:read`; they never proved a real token produces that authority.

In production the provider puts roles in `realm_access.roles`, the default
`JwtGrantedAuthoritiesConverter` reads only `scope` and `scp`, every request arrives with an empty or
scope-only authority set, and every rule fails — as `403` rather than `401`, because the token is
perfectly valid.

The diagnostic is to log `authentication.getAuthorities()` once for a real request, which immediately
shows either an empty collection or a prefix mismatch, the two failure modes catalogued in
[`02_M1_T2_Authentication_Authorization.md`](02_M1_T2_Authentication_Authorization.md). The durable fix
is a unit test on the converter built from a `Jwt` carrying a claim set copied from a real production
token with values redacted, asserting the exact authority strings that come out.
</details>

### Q6. Design question — forty microservices, one identity provider, tokens flowing between services. Design the resource server layer.

<details>
<summary>Show answer</summary>

Three things must be true everywhere, and then the parts that vary.

**Every service validates the same five things**: signature against the cached JWK set, `iss` against
the single issuer, `exp` and `nbf` with the default sixty-second skew, and — the one not on by default
— `aud` against that service's own resource identifier. I would not leave the audience check to each
team's judgement; it goes into a shared starter or configuration template that every service inherits,
with a build-time check failing if `audiences` is unset, because one team forgetting re-opens the hole
for all forty.

**Local JWT validation, not introspection.** Forty services introspecting per request makes the identity
provider a synchronous dependency of the entire estate. Access tokens live five to fifteen minutes,
bounding the revocation window, and revocation is pushed as a short-lived denylist rather than pulled.

**Identity is preserved across hops, not forwarded.** This is the decision people get wrong. The lazy
option is forwarding the incoming token to every downstream call, which is a confused-deputy generator:
the downstream cannot tell whether the request reflects the user's intent or a compromised intermediate
service, and the audience is wrong for the downstream anyway. The right mechanism is **token exchange**,
RFC 8693, supported in Spring Security 6.3 — the calling service presents the incoming token and
receives a new one, audienced for the downstream, retaining the original subject and carrying an `act`
claim naming the acting service.

**Key rotation is a documented, automated procedure**: publish the new public key, wait at least one
JWKS cache lifespan, switch signing, wait at least the maximum token lifetime, remove the old key. The
failure mode of skipping a step is a fleet-wide authentication outage.

**Observability**: every service logs `sub`, `jti`, `iss`, `aud` and authorities on authentication
failures, never the token value. JWKS fetch failures are a first-class alert, because they are the
leading indicator of the `500`-instead-of-`401` failure mode. A correlation identifier propagates across
all forty hops.

**Counter-question: a service calls downstream on behalf of a user but also has background jobs. How many token types, and how do you keep them apart?**

Three, and keeping them apart is a design obligation rather than a runtime detection problem. There is
the **user token** arriving from the client, audienced for this service; the **exchanged token** minted
downstream-audienced with the user's subject preserved and an `act` claim naming this service; and the
**client-credentials token** for background work, whose subject is the service's own client ID and which
represents no user.

The instinct is to inspect a token and decide what kind it is. I would not. As
[`02_M1_T2_Authentication_Authorization.md`](02_M1_T2_Authentication_Authorization.md) argues, the clean
architecture *separates* rather than *detects*: machine traffic gets a distinct audience and its own
`SecurityFilterChain` with its own `securityMatcher`, so the two populations never share a rule set and
there is nothing to confuse. A `@PreAuthorize` written as `#username == authentication.name`, which is
meaningless for a machine token, then cannot be reached by one.

The background job holds tokens through `AuthorizedClientServiceOAuth2AuthorizedClientManager`, because a
scheduler thread has no `HttpServletRequest`, as covered in
[`30_M10_T2_OAuth2_Client.md`](30_M10_T2_OAuth2_Client.md). And I would make it structurally impossible
for a job to pick up a user token by never putting one where a scheduler can reach it.

**Counter-question: an engineer proposes that services trust each other on the internal network and skip token validation for internal calls. Respond.**

I would push back, and the argument is about what the perimeter actually buys rather than purity. Network
position is not identity: "inside the cluster" is a property a compromised pod, a misconfigured ingress,
a leaked service-account token, or a server-side request forgery in any one of forty services also has.
Once one service is compromised, an unauthenticated internal API is a free lateral movement path to the
other thirty-nine, with no audit trail because there was never an identity to log. It also destroys
accountability — when the auditor asks who deleted the record, "a call from inside the cluster" is not an
answer.

The engineer's real concern is usually latency or complexity, and both are answerable. Local JWT
validation is an RSA verification with no network call, so there is essentially nothing to save, and the
complexity is one shared starter every service already inherits. What I *would* concede is that mutual
TLS between services is a genuine additional control worth having — but it authenticates the *service*,
not the *user*, so it complements token validation rather than replacing it. And a small number of
genuinely internal endpoints such as health and metrics can reasonably be network-gated, provided they
expose nothing sensitive.

**Counter-question: one of the forty is a legacy application that cannot be changed. How do you bring it in?**

With a sidecar or gateway in front of it, and a hard guarantee it cannot be reached directly. The pattern
is an authenticating proxy — a service mesh sidecar, or a small Spring Cloud Gateway instance — that
terminates the bearer token, performs the full validation including the audience check, and forwards to
the legacy application with the identity in a header it already understands. The legacy code changes not
at all.

The critical part is the guarantee. If the legacy service is still reachable on its own port from
anywhere in the cluster, the proxy is decoration and an attacker simply skips it, so it binds to
localhost in a sidecar deployment, or network policy restricts ingress to the gateway's identity — and I
would test that constraint rather than assume it.

The residual risk to name explicitly is **header injection**: if the legacy application trusts an
`X-User-Id` header and any path exists by which a client-supplied header reaches it, that is a complete
authentication bypass. The proxy must strip and overwrite that header on every request, never merely add
it — the `X-Forwarded-For` hazard from [`01_M1_T1_HTTP_Web_Basics.md`](01_M1_T1_HTTP_Web_Basics.md) in a
different costume. I would also give the legacy service a deadline and a named owner, because a permanent
exception becomes the template everyone copies.
</details>

---

## Quick Recall

```
TWO STRATEGIES
  jwt(...)         local validation, ZERO network calls, revocation bounded by exp
                   -> JwtAuthenticationToken, principal = Jwt
  opaqueToken(...) RFC 7662 introspection, ONE network call per request, exact revocation
                   -> BearerTokenAuthentication, principal = OAuth2IntrospectionAuthenticatedPrincipal
  introspection makes the IdP a synchronous dependency of EVERY request
  cache: key on a HASH of the token, successes only, ttl = min(30-60s, remaining life)

PROPERTIES
  jwt.issuer-uri          discovery + JwtIssuerValidator
  jwt.jwk-set-uri         keys ONLY, NO issuer validation
  jwt.public-key-location one static key, no rotation, tests only
  jwt.audiences           THE aud CHECK. SET IT. (Boot 3.1+)
  jwt.jws-algorithms      pin them; default RS256
  opaquetoken.introspection-uri / client-id / client-secret

DEFAULT VALIDATORS (the gap)
  JwtTimestampValidator  exp + nbf, 60s CLOCK SKEW
  JwtIssuerValidator     iss              (only when built from an issuer)
  JwtTypeValidator       typ header       (later 6.x, builder validateTypes)
  AUDIENCE = NOT VALIDATED BY DEFAULT
     -> a token for service A replays against service B
     -> an ID TOKEN becomes an API credential (its aud is a public client id)
  fix: audiences property, or JwtClaimValidator(AUD, ...) in DelegatingOAuth2TokenValidator
  DelegatingOAuth2TokenValidator runs ALL delegates and collects ALL errors

STARTUP
  Boot auto-config = LAZY via SupplierJwtDecoder, discovery on the FIRST JWT request
  YOUR OWN decoder bean = EAGER:
     JwtDecoders.fromIssuerLocation(issuer)          discovery now
     NimbusJwtDecoder.withIssuerLocation(i).build()  discovery inside build()
  stay lazy: wrap in SupplierJwtDecoder
           | use jwk-set-uri (+ add JwtIssuerValidator yourself)
           | contribute only an OAuth2TokenValidator<Jwt> bean

JWKS
  header kid SELECTS a key from the cached set (a selector, NEVER a trust decision)
  cache with refresh-ahead; unknown kid triggers a RATE-LIMITED re-fetch
  rotation: publish new key -> wait 1 cache lifespan -> switch signing
            -> wait max token lifetime -> remove the old key
  alg: none rejected before key selection; pin jws-algorithms

AUTHORITIES
  JwtGrantedAuthoritiesConverter: claim "scope" then "scp", split on space, prefix SCOPE_
  NO ROLES, EVER, by default -- correct: scope != role
  Keycloak realm_access.roles | Auth0 permissions | Entra roles/groups | Cognito cognito:groups
  add them in JwtAuthenticationConverter and KEEP the SCOPE_* ones
  JwtAuthenticationConverter.setPrincipalClaimName -> default "sub"

BEARER TOKEN RESOLUTION
  DefaultBearerTokenResolver: Authorization header ONLY
  allowFormEncodedBodyParameter=false  allowUriQueryParameter=false  KEEP THEM OFF
  query parameters leak into access logs, proxies, history, Referer; removed in OAuth 2.1
  two tokens in one request -> 400 invalid_request

ERRORS (RFC 6750)
  no token                        401, bare WWW-Authenticate: Bearer
  malformed / duplicate           400 invalid_request
  bad sig, expired, wrong iss/aud 401 invalid_token
  valid token, missing authority  403 insufficient_scope
  BadJwtException    -> 401 (the TOKEN is bad)
  other JwtException -> 500 (WE are broken: JWKS unreachable)

MULTI-TENANCY
  JwtIssuerAuthenticationManagerResolver.fromTrustedIssuers(a, b)
  the unverified iss only SELECTS from a FIXED list; the selected manager then verifies
  an arbitrary issuer -> discovery = an SSRF primitive
  or: a separate SecurityFilterChain per tenant URL space

TWO CHAINS IN ONE APP
  @Order(1) securityMatcher("/api/**") STATELESS + csrf disabled + oauth2ResourceServer
  @Order(2) catch-all, session allowed, oauth2Login   (login CANNOT be stateless)
  first matching chain wins -> catch-all LAST
  csrf off is safe because a HEADER token is not ambient, not because it is stateless

TESTING
  .with(jwt().authorities(...))      tests RULES; bypasses the decoder AND your converter
  @MockBean/@MockitoBean JwtDecoder  stops startup discovery; asserts invalid_token shape
  Jwt.withTokenValue(...).build()    unit-test validators and converters, no crypto
  real crypto: sign with a test key + stub the JWK set endpoint
```

---

**Previous:** [`30_M10_T2_OAuth2_Client.md`](30_M10_T2_OAuth2_Client.md) ·
**Next:** [`32_M11_T1_SecurityContext_Internals.md`](32_M11_T1_SecurityContext_Internals.md)
