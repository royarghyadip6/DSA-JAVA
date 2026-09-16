# 10.2 — Spring Security OAuth2 Client & Social Login

> **Module 10 · Topic 2** · OAuth2 & OpenID Connect
> Baseline: Spring Security 6.x on Boot 3.x
> New to this? Read **In Plain English** below first, then come back to the Version Matrix.

---

## Version Matrix

| Concern | Spring Security 5.x | **Spring Security 6.x (baseline)** | Spring Security 7.x |
|---|---|---|---|
| DSL style | `http.oauth2Login().userInfoEndpoint()...and()` | **lambda only — `http.oauth2Login(oauth2 -> ...)`; `and()` removed** | lambda only |
| Password grant client provider | `PasswordOAuth2AuthorizedClientProvider`, `OAuth2PasswordGrantRequest` | **present but deprecated** | **removed — the classes are deleted** |
| PKCE on the client | opt-in from 5.2; automatic for public clients | **automatic for public clients; `OAuth2AuthorizationRequestCustomizers.withPkce()` for confidential ones** | applied by default more broadly |
| Token response client | `DefaultAuthorizationCodeTokenResponseClient` on `RestTemplate` | **same, plus `RestClientAuthorizationCodeTokenResponseClient` from 6.4, which deprecates the `Default*` variants** | `RestClient`-based clients are the norm |
| Calling downstream APIs | `ServletOAuth2AuthorizedClientExchangeFilterFunction` with `WebClient` | **same, plus `OAuth2ClientHttpRequestInterceptor` for `RestClient` from 6.4** | both supported |
| OIDC logout | `OidcClientInitiatedLogoutSuccessHandler` (RP-initiated) | **same, plus OIDC Back-Channel Logout via `http.oidcLogout(...)` from 6.2** | same |
| Device code grant | not available | **`DeviceCodeOAuth2AuthorizedClientProvider` from 6.1** | same |
| Token exchange grant | not available | **`TokenExchangeOAuth2AuthorizedClientProvider` from 6.3** | same |
| Authorities after login | `ROLE_USER` plus `SCOPE_*` | **same defaults; `GrantedAuthoritiesMapper` is the supported extension point** | same |

The upgrade hazard is the same as in
[`29_M10_T1_OAuth2_OIDC_Fundamentals.md`](29_M10_T1_OAuth2_OIDC_Fundamentals.md): the password
grant is **deleted** in 7.0. Any `ClientRegistration` declaring
`authorization-grant-type: password` stops resolving.

---

## Why This Exists

Implementing the authorization code flow by hand means getting a dozen small things right, each a
vulnerability when wrong: generating and comparing `state` against the session, generating and
verifying `nonce` against the ID token, keeping a PKCE verifier alive across a redirect, building
`redirect_uri` correctly behind a proxy, exchanging the code exactly once, validating the ID
token's signature against a rotating JWKS, and turning claims into your application's authorities.

`spring-security-oauth2-client` does all of it. Its value is not saved typing — it is that the
*secure* path becomes the *default* path. What remains is the part the framework cannot know:
which provider, how provider identity maps to your user records, and what happens when two
providers claim the same person.

Two distinct jobs live in one module, and conflating them is the commonest mistake here:

> **`oauth2Login` makes the provider your authentication source.**
> **`oauth2Client` just manages tokens so you can call somebody else's API.**

---

## In Plain English

**The one-line version:** This is the part of Spring Security that adds a working "Sign in with
Google" button to your application, and that keeps hold of the tokens your application needs in
order to call other people's APIs on a user's behalf.

**An analogy.** Think of the concierge desk in a hotel. It has two jobs that people often assume
are one job. The first is deciding whether you belong here at all: you present something the hotel
recognises, and the desk decides you are a guest in room 412. The second is completely separate —
the desk keeps an envelope for you containing the theatre tickets and museum passes it collected
on your behalf, and hands over the right one when you head out for the evening. The concierge
never becomes the theatre and never becomes the museum; it holds passes issued by them.

Spring Security's OAuth2 client module does both jobs, and they are two different switches in your
configuration. `oauth2Login` is the first job: it makes an external provider the thing that decides
who is logged into your application. `oauth2Client` is the second job: it obtains, stores, and
refreshes tokens so your code can call somebody else's API, and it does not log anybody in at all.
The reason this matters is that beginners reach for the wrong one, wire up a login feature when
they wanted an API integration, and end up with a configuration that half works. The rule is
simple: if the question is "who is this person?", you want `oauth2Login`; if the question is "what
credential do I attach to this outgoing call?", you want `oauth2Client`.

**How it actually works, step by step.**

Everything starts from a **client registration**, which is just the set of facts about one
provider: the identifier your application was given, the secret if it has one, the permissions to
request, and the provider's web addresses. In Spring Boot you usually write this as a few lines of
YAML under `spring.security.oauth2.client.registration`, and each block has a name you choose —
`google`, `github`, `corporate-idp`. That name is called the `registrationId`, and it is the key
that ties everything together: it appears in the URL that starts the login, in the URL the provider
comes back to, and in the annotation you use to fetch a stored token later. Spring knows the
addresses for a handful of well-known providers by name, which is why a registration called
`google` with just a client identifier and secret is complete working Google login.

When a user clicks the sign-in link, they are sent to `/oauth2/authorization/google`. A filter
picks that up, builds the outbound request, and stores three pieces of state in the user's HTTP
session: the `state` value it will compare when the provider returns, the `nonce` it will look for
inside the ID token, and the PKCE `code_verifier` it will need for the token exchange. Then it
redirects the browser to the provider. This is the reason a chain that uses `oauth2Login` must be
allowed to create a session — the login is two separate requests with a human typing a password in
between, and those three values have to survive that gap. Configuring such a chain as stateless
makes every login fail.

The provider sends the browser back to `/login/oauth2/code/google` with a code. A second filter
handles that callback, and the way it verifies `state` is worth knowing because it is elegant: the
stored requests sit in a map keyed by `state`, so it simply looks up the returned value. A hit
means this browser started the flow; a miss means it did not, and you get the error
`authorization_request_not_found`. The filter then hands off to an authentication provider, which
exchanges the code for tokens over a direct server-to-server call, validates the ID token, and
loads the user's details.

Loading the user's details is the step you will most often want to customise, and Spring gives you
two places to do it. An `OAuth2UserService` (or `OidcUserService` for OpenID Connect providers) is
where you would look up or create a matching row in your own user table and attach that user's
roles. A `GrantedAuthoritiesMapper` is a lighter option for when you only need to rewrite
permissions, for example turning a provider's group list into your own roles. The important
practice in both cases is to call the built-in implementation first and then add to its result,
rather than re-implementing the HTTP call yourself.

Once login succeeds, the result is an `OAuth2AuthenticationToken` in the security context, whose
principal is an `OidcUser` or an `OAuth2User`. At the same time Spring stores an
`OAuth2AuthorizedClient`, which is simply the bundle of "this user, at this provider, with these
tokens". Later, when a controller needs to call that provider's API, it asks for that bundle —
typically with the `@RegisteredOAuth2AuthorizedClient` annotation on a method parameter — and
Spring hands it over, having quietly refreshed the access token first if it was about to expire.
Where those bundles are kept matters in production: the default keeps them in one server's memory,
which loses every user's tokens on restart, so a real deployment stores them in a database.

The last strand is **account linking**, and it is the one genuine security trap in this file rather
than a configuration inconvenience. When a user logs in with Google, you have to decide which row
in your own user table they are. The tempting answer is to match on the email address the provider
sent. That is a vulnerability, because an email address inside a token is something the provider
*claims*, not something you verified, and several providers will happily assert an address the user
never proved they own. Somebody who gets a provider to assert your user's address can then log in
as your user without ever knowing a password. The correct key is the pair of values (issuer,
subject), which is the provider's own permanent identifier for the account, and section 8 covers
the full set of rules.

**Why should a beginner care?** Two of the three things you will actually hit are here. The first
is that most first attempts at social login fail on plumbing rather than protocol — a session that
was not allowed to exist, a cookie setting of `SameSite=Strict` that suppresses the callback
cookie, or a reverse proxy that makes your application generate the wrong callback URL and earns a
`redirect_uri_mismatch` from the provider. Knowing what the framework needs makes these five-minute
fixes rather than afternoon-long mysteries. The second is account linking, where the obvious
implementation is an account-takeover bug that looks completely reasonable in code review and that
no framework warning will catch.

**Words you will meet in this file**

| Term | In plain words |
|---|---|
| `oauth2Login` | The switch that makes an external provider the thing that logs users into your application. |
| `oauth2Client` | The switch that obtains and stores tokens so your application can call other APIs. It logs nobody in. |
| `ClientRegistration` | The set of facts describing one provider: your identifier, your secret, the permissions to ask for, and the provider's addresses. |
| `registrationId` | The short name you give a registration, such as `google`. It appears in the login URL, the callback URL, and in code. |
| `ClientRegistrationRepository` | The component that looks a registration up by that name. |
| `CommonOAuth2Provider` | A built-in set of addresses for well-known providers, matched by the registration name, so Google login needs almost no configuration. |
| `issuer-uri` | One configuration line pointing at the provider's description document, which fills in all its other addresses automatically. |
| `redirect-uri` | The address the provider sends the browser back to. It must match what you registered with the provider exactly, character for character. |
| `{baseUrl}` | A placeholder in the redirect address that Spring fills in from the incoming request, which is why a reverse proxy can break it. |
| `AuthorizationRequestRepository` | Where the in-flight login state (`state`, `nonce`, PKCE verifier) is kept between the redirect out and the callback back. The session, by default. |
| `OAuth2AuthenticationToken` | The logged-in identity that results from `oauth2Login`. |
| `OAuth2User` / `OidcUser` | The principal object holding the provider's information about the user. The second adds the ID token and its claims. |
| `getName()` | The user's identifier as far as Spring is concerned. It is only unique within one provider, so it is not a safe key for your own user table. |
| `OAuth2UserService` / `OidcUserService` | The component that loads user details after the token exchange. The place to create a local user record and attach roles. |
| `GrantedAuthoritiesMapper` | A lighter hook for rewriting a user's permissions without touching how they were loaded. |
| Authority | One permission label on an identity, such as `ROLE_ADMIN` or `SCOPE_read`. |
| `OAuth2AuthorizedClient` | The stored bundle of "this user, at this provider, with this access token and refresh token". |
| `OAuth2AuthorizedClientService` / `...Repository` | Where those bundles are stored. The service version works without a web request, which is what background jobs need. |
| `OAuth2AuthorizedClientManager` | The component that obtains a bundle when needed and refreshes an expiring access token first. |
| `@RegisteredOAuth2AuthorizedClient` | An annotation on a controller method parameter that hands you the stored bundle for the current user. |
| Just-in-time provisioning | Creating the local user record on the user's first successful login rather than in advance. |
| Account linking | Deciding which existing local user an incoming provider identity belongs to. Doing this by email address is a security bug. |
| RP-initiated logout | Logging the user out of the provider as well as your own application, so that "log out" really logs them out. |

**If you remember only one thing:** identify local users by the pair of provider issuer and
subject, never by the email address in the token, because an email address in a token is a claim
rather than a proof.

---

## Core Concepts

### 1. `oauth2Login` versus `oauth2Client`

**In simple terms:** One of these logs users into your application using an external provider, and
the other only collects tokens so you can call somebody else's API; picking the wrong one is the
most common starting mistake.

| | `http.oauth2Login(...)` | `http.oauth2Client(...)` |
|---|---|---|
| Purpose | authenticate users **into your application** | obtain and store tokens for **outbound** calls |
| Produces an `Authentication`? | **yes** — an `OAuth2AuthenticationToken` in the `SecurityContext` | **no** — the user is authenticated some other way, or not at all |
| Principal type | `OidcUser` or `OAuth2User` | unchanged by this feature |
| Filters registered | `OAuth2AuthorizationRequestRedirectFilter` + `OAuth2LoginAuthenticationFilter` | `OAuth2AuthorizationRequestRedirectFilter` + `OAuth2AuthorizationCodeGrantFilter` |
| Callback URI | `/login/oauth2/code/*` | `/authorize/oauth2/code/*` |
| Typical grant | `authorization_code` with the `openid` scope | any — `authorization_code`, `client_credentials`, `refresh_token` |
| Typical use | "Sign in with Google" | "post to the user's Slack", "call the partner API as a service" |

They compose. One application can use `oauth2Login` against the corporate identity provider *and*
`oauth2Client` with a `client_credentials` registration for a partner API. `oauth2Login` also
implicitly enables the client machinery, so social login plus downstream calls on the logged-in
user's behalf needs no separate `oauth2Client` declaration.

The decision rule: **"who is the user?" is `oauth2Login`; "what token do I attach to this outbound
request?" is `oauth2Client`.**

### 2. `ClientRegistration` and the Repository

**In simple terms:** A registration is the small bundle of facts about one provider, and the short
name you give it becomes the key that appears in your URLs, your configuration, and your code.

```java
package org.springframework.security.oauth2.client.registration;

public final class ClientRegistration implements Serializable {

    private String registrationId;                        // "google", "corporate-idp"
    private String clientId;
    private String clientSecret;
    private ClientAuthenticationMethod clientAuthenticationMethod;  // basic / post / none / private_key_jwt
    private AuthorizationGrantType authorizationGrantType;
    private String redirectUri;                           // may contain {baseUrl}, {registrationId}
    private Set<String> scopes;
    private ProviderDetails providerDetails;
    private String clientName;                            // label on the default login page

    public static final class ProviderDetails implements Serializable {
        private String authorizationUri;
        private String tokenUri;
        private UserInfoEndpoint userInfoEndpoint;        // uri + authenticationMethod + userNameAttributeName
        private String jwkSetUri;
        private String issuerUri;
        private Map<String, Object> configurationMetadata;   // the whole discovery document
    }
}

public interface ClientRegistrationRepository {
    ClientRegistration findByRegistrationId(String registrationId);
}
```

`InMemoryClientRegistrationRepository` is what Boot auto-configures, and it additionally
implements `Iterable<ClientRegistration>` — which is how the default login page enumerates
providers. A custom repository that does **not** implement `Iterable`, such as one loading
registrations from a database for multi-tenant deployments, silently produces an empty default
login page.

**The `registrationId` is the key to everything.** It appears in the authorization URL
(`/oauth2/authorization/{registrationId}`), the callback URL
(`/login/oauth2/code/{registrationId}`), `@RegisteredOAuth2AuthorizedClient("...")`, and
`OAuth2AuthenticationToken.getAuthorizedClientRegistrationId()`. It is neither the `client_id`
nor the provider name, though Boot uses it as a fallback for both.

### 3. `CommonOAuth2Provider` and the Properties

**In simple terms:** Spring already knows the web addresses for a few popular providers and finds
them by matching your registration's name, which is why Google login works from two lines of
configuration and stops working the moment you rename the block.

```java
// org.springframework.security.oauth2.client.CommonOAuth2Provider (simplified)
public enum CommonOAuth2Provider {

    GOOGLE {
        public ClientRegistration.Builder getBuilder(String registrationId) {
            ClientRegistration.Builder builder = getBuilder(registrationId,
                    ClientAuthenticationMethod.CLIENT_SECRET_BASIC, DEFAULT_REDIRECT_URL);
            builder.scope("openid", "profile", "email");
            builder.authorizationUri("https://accounts.google.com/o/oauth2/v2/auth");
            builder.tokenUri("https://www.googleapis.com/oauth2/v4/token");
            builder.jwkSetUri("https://www.googleapis.com/oauth2/v3/certs");
            builder.issuerUri("https://accounts.google.com");
            builder.userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo");
            builder.userNameAttributeName(IdTokenClaimNames.SUB);     // "sub"
            builder.clientName("Google");
            return builder;
        }
    },

    GITHUB {
        public ClientRegistration.Builder getBuilder(String registrationId) {
            // ... authorizationUri https://github.com/login/oauth/authorize
            //     tokenUri        https://github.com/login/oauth/access_token
            //     userInfoUri     https://api.github.com/user
            builder.userNameAttributeName("id");    // GitHub has no OIDC, so no "sub"
            builder.clientName("GitHub");
            return builder;
        }
    },

    FACEBOOK { /* userNameAttributeName("id"), userInfoUri with explicit fields */ },
    OKTA     { /* endpoints come from the tenant, so only the shape is predefined */ };

    private static final String DEFAULT_REDIRECT_URL = "{baseUrl}/{action}/oauth2/code/{registrationId}";
}
```

Boot matches a registration to this enum **by `registrationId`**, case-insensitively, when no
explicit provider is set — which is why a `client-id` and `client-secret` under the key `google`
is complete, working Google login. Rename the registration to `corporate-google` and it breaks,
because no such enum constant exists; you then need `provider: google` to point back at it.

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          <registrationId>:
            provider: <providerId>                   # defaults to <registrationId>
            client-id: ...
            client-secret: ...
            client-authentication-method: client_secret_basic | client_secret_post | none | private_key_jwt
            authorization-grant-type: authorization_code | client_credentials | refresh_token
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
            scope: [openid, profile, email]
            client-name: "Corporate SSO"             # label on the default login page
        provider:
          <providerId>:
            issuer-uri: https://idp.example.com      # discovery; replaces the four below
            authorization-uri: ...
            token-uri: ...
            user-info-uri: ...
            user-info-authentication-method: header  # header | form | query
            jwk-set-uri: ...
            user-name-attribute: sub
```

**The `redirect-uri` template** defaults to `{baseUrl}/login/oauth2/code/{registrationId}`. The
supported placeholders are `{baseUrl}`, `{baseScheme}`, `{baseHost}`, `{basePort}`, `{basePath}`,
`{registrationId}` and `{action}`. `{baseUrl}` is expanded **per request** from the incoming
`HttpServletRequest`, which is exactly why an unaccounted-for reverse proxy produces
`http://internal-host:8080/...` and a `redirect_uri_mismatch`.

The per-registration callback path is a **security control**, not cosmetics. A distinct
`redirect_uri` per provider is the standard mitigation for the **mix-up attack** in
[`29_M10_T1_OAuth2_OIDC_Fundamentals.md`](29_M10_T1_OAuth2_OIDC_Fundamentals.md): the callback
path identifies the provider unambiguously, so the application cannot be tricked into sending
provider A's code to provider B's token endpoint.

### 4. The Filter-Level Flow

**In simple terms:** This is which piece of Spring does what during a login, from the click on the
sign-in link to an identity existing, and it also explains why this kind of login always needs a
session.

```mermaid
sequenceDiagram
    autonumber
    participant B as Browser
    participant RF as OAuth2AuthorizationRequestRedirectFilter
    participant ARR as HttpSessionOAuth2AuthorizationRequestRepository
    participant LF as OAuth2LoginAuthenticationFilter
    participant AM as ProviderManager
    participant P as OidcAuthorizationCodeAuthenticationProvider
    participant US as OidcUserService
    participant AS as Authorization Server

    B->>RF: GET /oauth2/authorization/corporate-idp
    Note over RF: DefaultOAuth2AuthorizationRequestResolver builds<br/>OAuth2AuthorizationRequest: state + nonce +<br/>PKCE if public; expands {baseUrl} from the request
    RF->>ARR: saveAuthorizationRequest(...)
    Note over ARR: stored in the HTTP SESSION, keyed by state
    RF-->>B: 302 to the authorization endpoint
    B->>AS: authenticate + consent (AS domain)
    AS-->>B: 302 /login/oauth2/code/corporate-idp?code=...&state=...
    B->>LF: GET /login/oauth2/code/corporate-idp
    LF->>ARR: removeAuthorizationRequest(...)
    Note over LF: the map lookup by state IS the state check<br/>miss => authorization_request_not_found
    LF->>AM: authenticate(OAuth2LoginAuthenticationToken)
    AM->>P: authenticate(...)
    P->>AS: POST /token (code, redirect_uri, code_verifier, client auth)
    AS-->>P: access_token + id_token + refresh_token
    Note over P: validate id_token: signature via JWKS, iss,<br/>aud == client_id, exp/iat, nonce
    P->>US: loadUser(OidcUserRequest)
    US->>AS: GET /userinfo (Bearer access_token)
    AS-->>US: claims
    US-->>P: DefaultOidcUser(authorities, idToken, userInfo, "sub")
    P-->>LF: authenticated OAuth2LoginAuthenticationToken
    Note over LF: convert to OAuth2AuthenticationToken,<br/>save the OAuth2AuthorizedClient,<br/>rotate the session id, save the SecurityContext
    LF-->>B: 302 to the saved request or "/"
```

| Component | Default | Responsibility |
|---|---|---|
| `OAuth2AuthorizationRequestRedirectFilter` | base URI `/oauth2/authorization` | builds and stores the authorization request, redirects the browser |
| `DefaultOAuth2AuthorizationRequestResolver` | matches `/oauth2/authorization/{registrationId}` | constructs the request, expands the redirect template, applies PKCE |
| `AuthorizationRequestRepository` | `HttpSessionOAuth2AuthorizationRequestRepository` | persists the request — and so the `nonce` and `code_verifier` — across the redirect |
| `OAuth2LoginAuthenticationFilter` | processes `/login/oauth2/code/*` | handles the callback, verifies `state`, drives authentication, stores the authorized client |
| `OAuth2LoginAuthenticationProvider` | — | plain OAuth2: exchanges the code, calls `OAuth2UserService` |
| `OidcAuthorizationCodeAuthenticationProvider` | — | OIDC: exchanges the code, **validates the ID token**, calls `OidcUserService` |
| `OAuth2AuthorizationCodeAuthenticationProvider` | — | used by `oauth2Client`: exchange only, no user loading |
| `DefaultLoginPageGeneratingFilter` | `/login` | renders one link per registration when no custom login page is set |

**Why `oauth2Login` needs a session even in an otherwise stateless application.** The flow is two
request/response pairs separated by however long the user takes to type a password and complete
multi-factor authentication. Three pieces of state must survive that gap — the `state` to compare
on return, the `nonce` to compare against the ID token, and the PKCE `code_verifier` for the token
exchange. All three are attributes of the `OAuth2AuthorizationRequest`, and the default repository
puts the whole object in the `HttpSession`.

The concrete constraint: a chain carrying `oauth2Login` **cannot** use
`SessionCreationPolicy.STATELESS`. The standard architecture for an otherwise stateless
application is two chains — a session-bearing chain for the login and callback paths, and a
stateless resource-server chain for the API. The callback also arrives as a cross-site top-level
navigation, so the session cookie must be `SameSite=Lax`; `Strict` suppresses it and every login
fails. If you genuinely cannot have a session, `AuthorizationRequestRepository` is an interface and
a cookie-based implementation is legitimate — but it must be signed or encrypted, because an
attacker who can write the stored `state` defeats the protection it exists to provide.

### 5. Principal Types

**In simple terms:** The object representing the logged-in user differs depending on the provider
and the mechanism, and the identifier it reports is only unique within that one provider.

```java
public interface OAuth2User extends OAuth2AuthenticatedPrincipal {
    Map<String, Object> getAttributes();
    Collection<? extends GrantedAuthority> getAuthorities();
    String getName();               // the value of userNameAttributeName
}

public interface OidcUser extends OAuth2User, IdTokenClaimAccessor {
    Map<String, Object> getClaims();
    OidcUserInfo getUserInfo();
    OidcIdToken getIdToken();
}

public class OAuth2AuthenticationToken extends AbstractAuthenticationToken {
    private final OAuth2User principal;
    private final String authorizedClientRegistrationId;   // which provider logged this user in
}
```

| Flow | Principal implementation | `getName()` returns |
|---|---|---|
| `oauth2Login` with `openid` | `DefaultOidcUser` | the `userNameAttributeName` claim, `sub` by default |
| `oauth2Login` without `openid` | `DefaultOAuth2User` | the configured attribute — GitHub and Facebook use `id` |
| `oauth2ResourceServer().jwt()` | `Jwt` | the `sub` claim |

**The `getName()` trap.** The named attribute is **provider-scoped**, not global. GitHub's `id` is
a number and Google's `sub` is a long digit string; both are unique only within their own issuer.
Using `authentication.getName()` as your application's user key means identically-numbered
subjects from two providers collapse into one account. The correct key is the pair **(issuer or
registration id, subject)**.

Principal type varies by mechanism, which is the `ClassCastException` hazard catalogued in
[`02_M1_T2_Authentication_Authorization.md`](02_M1_T2_Authentication_Authorization.md). Pattern
match:

```java
String subject = switch (authentication.getPrincipal()) {
    case OidcUser oidc -> oidc.getIdToken().getSubject();
    case OAuth2User oauth2 -> String.valueOf(oauth2.getAttribute("id"));
    case Jwt jwt -> jwt.getSubject();
    default -> authentication.getName();
};
```

### 6. Customising the User

**In simple terms:** This is where you turn "a person who logged in at Google" into "a row in my
own user table with my own roles attached", and the rule is to let the framework do its work first
and then add yours on top.

```java
@FunctionalInterface
public interface OAuth2UserService<R extends OAuth2UserRequest, U extends OAuth2User> {
    U loadUser(R userRequest) throws OAuth2AuthenticationException;
}
```

`DefaultOAuth2UserService` calls the `user-info-uri` with the access token and builds a
`DefaultOAuth2User` with `ROLE_USER` plus `SCOPE_*` authorities. `OidcUserService` delegates to it
only when the requested scopes intersect its `accessibleScopes` (`profile`, `email`, `address`,
`phone`), merges ID token claims with UserInfo claims, and builds a `DefaultOidcUser`.

This is where you **just-in-time provision** a local record and map claims onto authorities. The
implementation rule is **delegate first, decorate second** — never reimplement the HTTP call,
which handles content negotiation, the `user-info-authentication-method` modes, and error
translation:

```java
OidcUserService delegate = new OidcUserService();
OidcUser oidcUser = delegate.loadUser(userRequest);
// merge authorities, provision the local record, then:
return new DefaultOidcUser(mergedAuthorities, oidcUser.getIdToken(), oidcUser.getUserInfo(), "sub");
```

The third constructor argument is the `nameAttributeKey`. Passing an attribute that does not exist
throws `IllegalArgumentException` at login — the usual first-run failure when a Google example is
copied onto GitHub.

`GrantedAuthoritiesMapper` is the lighter alternative when you only need to rewrite authorities:

```java
@FunctionalInterface
public interface GrantedAuthoritiesMapper {
    Collection<? extends GrantedAuthority> mapAuthorities(Collection<? extends GrantedAuthority> authorities);
}
```

It runs **after** the `OAuth2UserService`, inside the authentication provider, and it also applies
to LDAP and SAML — so it is the natural home for a cross-cutting rule such as "every `SCOPE_x`
also implies `ROLE_X`". Use an `OAuth2UserService` when you need the claims themselves or a
database.

### 7. `OAuth2AuthorizedClient` — Calling Downstream APIs

**In simple terms:** Spring keeps a bundle of "this user, this provider, these tokens" so that
when your code calls that provider's API it can attach the right token and quietly renew it first
if it is about to expire.

An `OAuth2AuthorizedClient` is the tuple **(registration, principal name, access token, refresh
token)**: "this user authorised this client at this provider, and here are the tokens".

| | `OAuth2AuthorizedClientService` | `OAuth2AuthorizedClientRepository` |
|---|---|---|
| Signature | `loadAuthorizedClient(registrationId, principalName)` | `loadAuthorizedClient(registrationId, principal, request)` |
| Bound to a request | **no** | **yes** — takes `HttpServletRequest`/`HttpServletResponse` |
| Use from | background jobs, schedulers, message listeners | web request handling |
| Implementations | `InMemoryOAuth2AuthorizedClientService`, `JdbcOAuth2AuthorizedClientService` | `AuthenticatedPrincipalOAuth2AuthorizedClientRepository`, `HttpSessionOAuth2AuthorizedClientRepository` |

The default web repository is `AuthenticatedPrincipalOAuth2AuthorizedClientRepository`, which
**delegates to the service for authenticated principals and to the session repository for
anonymous ones**. That branch explains a classic incident: with the default in-memory service,
tokens live in one instance's heap, so a restart or a reroute produces "authorized client not
found" and an unexplained re-consent prompt. `JdbcOAuth2AuthorizedClientService` (schema in
`oauth2-client-schema.sql` inside the jar) is the fix, and the stored refresh tokens are then
secrets at rest.

`OAuth2AuthorizedClientManager` acquires and refreshes tokens.
`DefaultOAuth2AuthorizedClientManager` is for web request contexts;
`AuthorizedClientServiceOAuth2AuthorizedClientManager` is for background contexts with no request.
Both take an `OAuth2AuthorizedClientProvider` built by `OAuth2AuthorizedClientProviderBuilder`,
which is where you declare which grants this application performs.

`RefreshTokenOAuth2AuthorizedClientProvider` treats a token as expired when `expiresAt` falls
within a **60-second default clock skew**, and refreshes eagerly. With no refresh token it returns
`null` and the manager falls through — for an `authorization_code` client that means the user is
redirected to re-authorise; for `client_credentials` a fresh token is minted silently.

### 8. Account Linking — A Real Vulnerability Class

**In simple terms:** Deciding which existing local account an incoming provider identity belongs
to by matching the email address lets an attacker take over that account without a password, so
match on the provider's own permanent identifier instead.

```java
// VULNERABLE
String email = oidcUser.getEmail();
User user = userRepository.findByEmail(email)
        .orElseGet(() -> userRepository.save(new User(email)));
login(user);
```

The victim has an account under `alice@corp.com` created with a password. The attacker signs up at
a provider that does **not** verify email ownership — or runs their own OIDC provider that you
accept for enterprise customers, or exploits one that lets a user set an arbitrary address — and
sets their email to `alice@corp.com`. They click "sign in with that provider", your code looks up
by email, finds Alice's account, and logs the attacker in as Alice. No password was needed.

The root cause is always the same: **treating an email address as proof of identity when it is only
a claim**. The rules:

1. **The key is (`iss`, `sub`), never the email.** A `user_identities` table with `(issuer,
   subject)` unique and a foreign key to the local user.
2. **If you match on email, require `email_verified` to be true** — and know the provider well
   enough to be sure the flag means what you think. GitHub's `/user` has no such flag; verification
   status lives on `/user/emails` behind the `user:email` scope.
3. **Auto-link only providers on an explicit allowlist**, not by a general rule.
4. **Prefer explicit linking**: the user is *already authenticated*, then initiates "connect
   another provider" from account settings, and the new identity attaches to the session's current
   user — never the reverse.
5. **Re-authenticate before linking.** It is an account-takeover primitive, so treat it like a
   password change and require `fullyAuthenticated()` or a fresh `auth_time` — the step-up
   reasoning from
   [`02_M1_T2_Authentication_Authorization.md`](02_M1_T2_Authentication_Authorization.md).
6. **Never unlink the last identity** without confirming a usable credential remains, or you have
   built a permanent lockout.

The mirror-image hazard: some enterprise providers reissue `sub` on directory migrations. If your
mapping is purely `(iss, sub)`, every user becomes a new user. Keep an audited manual re-linking
path rather than a silent email fallback.

---

## Working Code

```java
package com.example.oauth2client.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.OAuth2AuthorizationRequestCustomizers;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class OAuth2ClientConfig {

    /**
     * The browser-facing chain. It carries oauth2Login and therefore MUST allow a session:
     * state, nonce and the PKCE verifier have to survive the provider redirect.
     */
    @Bean
    @Order(1)
    SecurityFilterChain webChain(HttpSecurity http,
                                 ClientRegistrationRepository registrations,
                                 CustomOidcUserService oidcUserService,
                                 AppGrantedAuthoritiesMapper authoritiesMapper) throws Exception {
        http
            .securityMatcher("/", "/login/**", "/oauth2/**", "/app/**", "/logout")
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login/**", "/oauth2/**").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .authorizationEndpoint(endpoint -> endpoint
                    .baseUri("/oauth2/authorization")
                    .authorizationRequestResolver(pkceResolver(registrations)))
                .redirectionEndpoint(endpoint -> endpoint
                    .baseUri("/login/oauth2/code/*"))
                .userInfoEndpoint(userInfo -> userInfo
                    .oidcUserService(oidcUserService)
                    .userAuthoritiesMapper(authoritiesMapper))
                .defaultSuccessUrl("/app/home", false)
                .failureUrl("/login?error")
            )
            .logout(logout -> logout
                .logoutSuccessHandler(oidcLogoutSuccessHandler(registrations)))
            .oauth2Client(Customizer.withDefaults());

        return http.build();
    }

    /**
     * PKCE is automatic for public clients. This enables it for CONFIDENTIAL clients too,
     * closing the authorization-code-injection hole that client authentication does not cover.
     */
    private OAuth2AuthorizationRequestResolver pkceResolver(ClientRegistrationRepository repo) {
        DefaultOAuth2AuthorizationRequestResolver resolver =
                new DefaultOAuth2AuthorizationRequestResolver(repo, "/oauth2/authorization");
        resolver.setAuthorizationRequestCustomizer(OAuth2AuthorizationRequestCustomizers.withPkce());
        return resolver;
    }

    /** RP-initiated logout: end the local session AND the session at the provider. */
    private OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler(
            ClientRegistrationRepository repo) {
        OidcClientInitiatedLogoutSuccessHandler handler =
                new OidcClientInitiatedLogoutSuccessHandler(repo);
        // Must be registered at the provider as a post_logout_redirect_uri.
        handler.setPostLogoutRedirectUri("{baseUrl}/login?logout");
        return handler;
    }

    /** Explicit manager so refresh_token and client_credentials are both available. */
    @Bean
    OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository registrations,
            OAuth2AuthorizedClientRepository authorizedClients) {

        OAuth2AuthorizedClientProvider provider = OAuth2AuthorizedClientProviderBuilder.builder()
                .authorizationCode()
                .refreshToken()
                .clientCredentials()
                .build();

        DefaultOAuth2AuthorizedClientManager manager =
                new DefaultOAuth2AuthorizedClientManager(registrations, authorizedClients);
        manager.setAuthorizedClientProvider(provider);
        return manager;
    }
}
```

```java
package com.example.oauth2client.config;

import java.util.HashSet;
import java.util.Set;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
public class CustomOidcUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private final OidcUserService delegate = new OidcUserService();
    private final LocalUserRepository users;

    public CustomOidcUserService(LocalUserRepository users) {
        this.users = users;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        // Let the framework do the UserInfo call and the claim merge.
        OidcUser oidcUser = this.delegate.loadUser(userRequest);

        String issuer = oidcUser.getIdToken().getIssuer().toString();
        String subject = oidcUser.getSubject();

        // (iss, sub) is the identity key. Email is a CLAIM, never a key.
        LocalUser localUser = this.users.findByIssuerAndSubject(issuer, subject)
                .orElseGet(() -> provision(userRequest, oidcUser, issuer, subject));

        if (!localUser.isEnabled()) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("account_disabled", "This account has been disabled", null));
        }

        Set<GrantedAuthority> authorities = new HashSet<>(oidcUser.getAuthorities());
        localUser.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .forEach(authorities::add);

        // Keep the SCOPE_* authorities so role-and-scope intersection checks stay possible.
        return new DefaultOidcUser(authorities, oidcUser.getIdToken(), oidcUser.getUserInfo(), "sub");
    }

    private LocalUser provision(OidcUserRequest request, OidcUser oidcUser,
                                String issuer, String subject) {
        String email = oidcUser.getEmail();
        String registrationId = request.getClientRegistration().getRegistrationId();

        // Auto-linking by email is acceptable ONLY for explicitly trusted providers AND
        // only when the provider asserts the address is verified.
        boolean trusted = TrustedProviders.AUTO_LINK_BY_EMAIL.contains(registrationId);
        if (trusted && Boolean.TRUE.equals(oidcUser.getEmailVerified()) && email != null) {
            return this.users.findByEmail(email)
                    .map(existing -> this.users.attachIdentity(existing, issuer, subject))
                    .orElseGet(() -> this.users.createFromOidc(issuer, subject, email));
        }
        // Otherwise a new, unlinked account. Linking is an explicit, re-authenticated action
        // from account settings, never a silent side effect of logging in.
        return this.users.createFromOidc(issuer, subject, null);
    }
}
```

```java
package com.example.oauth2client.config;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.stereotype.Component;

/** Cross-cutting rule: provider groups become application roles. Runs after the user service. */
@Component
public class AppGrantedAuthoritiesMapper implements GrantedAuthoritiesMapper {

    @Override
    public Collection<? extends GrantedAuthority> mapAuthorities(
            Collection<? extends GrantedAuthority> authorities) {

        Set<GrantedAuthority> mapped = new HashSet<>(authorities);
        for (GrantedAuthority authority : authorities) {
            if (authority instanceof OidcUserAuthority oidcAuthority) {
                addRoles(mapped, oidcAuthority.getIdToken().getClaimAsStringList("groups"));
            }
            else if (authority instanceof OAuth2UserAuthority oauth2Authority
                    && oauth2Authority.getAttributes().get("groups") instanceof Collection<?> groups) {
                groups.forEach(group -> mapped.add(
                        new SimpleGrantedAuthority("ROLE_" + String.valueOf(group).toUpperCase())));
            }
        }
        return mapped;
    }

    private void addRoles(Set<GrantedAuthority> target, Collection<String> groups) {
        if (groups != null) {
            groups.stream()
                  .map(group -> new SimpleGrantedAuthority("ROLE_" + group.toUpperCase()))
                  .forEach(target::add);
        }
    }
}
```

Calling downstream APIs with the stored token:

```java
package com.example.oauth2client.web;

import static org.springframework.security.oauth2.client.web.reactive.function.client
        .ServletOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;
import static org.springframework.security.oauth2.client.web.reactive.function.client
        .ServletOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient;

import java.util.List;
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

@RestController
public class DownstreamController {

    private final WebClient webClient;

    public DownstreamController(WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Resolves the stored client for the CURRENT principal, refreshing the access token first
     * if it falls within the 60-second expiry skew.
     */
    @GetMapping("/app/repos")
    public List<Map<String, Object>> repos(
            @RegisteredOAuth2AuthorizedClient("github") OAuth2AuthorizedClient authorizedClient) {
        return this.webClient.get()
                .uri("https://api.github.com/user/repos")
                .attributes(oauth2AuthorizedClient(authorizedClient))
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<Map<String, Object>>() {})
                .collectList()
                .block();
    }

    /** No annotation: the filter function resolves the client from the registration id. */
    @GetMapping("/app/partner-data")
    public Map<String, Object> partnerData() {
        return this.webClient.get()
                .uri("https://partner.example.com/api/data")
                .attributes(clientRegistrationId("partner-service"))   // client_credentials
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }
}
```

```java
package com.example.oauth2client.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.reactive.function.client
        .ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    WebClient webClient(OAuth2AuthorizedClientManager manager) {
        ServletOAuth2AuthorizedClientExchangeFilterFunction filter =
                new ServletOAuth2AuthorizedClientExchangeFilterFunction(manager);
        // Fall back to the registration of the currently logged-in OAuth2AuthenticationToken.
        filter.setDefaultOAuth2AuthorizedClient(true);
        return WebClient.builder().apply(filter.oauth2Configuration()).build();
    }
}
```

A background job has no request, so it needs the service-based manager:

```java
package com.example.oauth2client.job;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class NightlySyncJob {

    /** Must be an AuthorizedClientServiceOAuth2AuthorizedClientManager: no HttpServletRequest here. */
    private final OAuth2AuthorizedClientManager manager;
    private final RestClient restClient = RestClient.create();

    public NightlySyncJob(OAuth2AuthorizedClientManager manager) {
        this.manager = manager;
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void sync() {
        OAuth2AuthorizeRequest request = OAuth2AuthorizeRequest
                .withClientRegistrationId("partner-service")   // client_credentials registration
                .principal("nightly-sync")                     // no user; just a storage label
                .build();

        OAuth2AuthorizedClient client = this.manager.authorize(request);

        this.restClient.get()
                .uri("https://partner.example.com/api/export")
                .header("Authorization", "Bearer " + client.getAccessToken().getTokenValue())
                .retrieve()
                .body(String.class);
    }
}
```

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope: [openid, profile, email]
            client-name: Google
          github:
            client-id: ${GITHUB_CLIENT_ID}
            client-secret: ${GITHUB_CLIENT_SECRET}
            scope: [read:user, user:email]
            client-name: GitHub
          corporate-idp:
            provider: corporate-idp
            client-id: ${CORP_CLIENT_ID}
            client-secret: ${CORP_CLIENT_SECRET}
            authorization-grant-type: authorization_code
            client-authentication-method: client_secret_basic
            scope: [openid, profile, email, groups]
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
            client-name: Corporate SSO
          partner-service:
            provider: corporate-idp
            client-id: ${PARTNER_CLIENT_ID}
            client-secret: ${PARTNER_CLIENT_SECRET}
            authorization-grant-type: client_credentials
            scope: [partner:export]
        provider:
          corporate-idp:
            issuer-uri: https://idp.example.com/realms/corporate

server:
  forward-headers-strategy: framework     # {baseUrl} must resolve to the PUBLIC URL
  servlet:
    session:
      cookie:
        http-only: true
        secure: true
        same-site: lax                    # STRICT breaks the provider callback

logging:
  level:
    org.springframework.security.oauth2.client: DEBUG
```

```java
package com.example.oauth2client;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class OAuth2LoginTests {

    @Autowired MockMvc mvc;

    @Test
    void anonymousRequestToAProtectedPageGoesToTheLoginPage() throws Exception {
        mvc.perform(get("/app/home"))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void authorizationEndpointRedirectsWithStateAndPkce() throws Exception {
        mvc.perform(get("/oauth2/authorization/corporate-idp"))
           .andExpect(status().is3xxRedirection())
           .andExpect(header().string("Location", allOf(
                   containsString("response_type=code"),
                   containsString("state="),
                   containsString("code_challenge="),
                   containsString("code_challenge_method=S256"))));
    }

    @Test
    void oidcLoginPopulatesAnOidcUserPrincipal() throws Exception {
        mvc.perform(get("/app/home").with(oidcLogin()
                .idToken(token -> token
                        .subject("248289761001")
                        .claim("iss", "https://idp.example.com/realms/corporate")
                        .claim("email", "alice@example.com")
                        .claim("email_verified", true))
                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
           .andExpect(status().isOk());
    }

    @Test
    void plainOAuth2LoginPopulatesAnOAuth2UserPrincipal() throws Exception {
        mvc.perform(get("/app/home").with(oauth2Login()
                .attributes(attrs -> attrs.put("id", 12345))
                .clientRegistration(client -> client.registrationId("github"))))
           .andExpect(status().isOk());
    }

    @Test
    void logoutRedirectsToTheProviderEndSessionEndpoint() throws Exception {
        mvc.perform(post("/logout")
                        .with(oidcLogin().idToken(token -> token
                                .subject("248289761001")
                                .claim("iss", "https://idp.example.com/realms/corporate")))
                        .with(csrf()))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrlPattern("https://idp.example.com/**"));
    }
}
```

---

## Internals

```java
// OAuth2AuthorizationRequestRedirectFilter (simplified)
public static final String DEFAULT_AUTHORIZATION_REQUEST_BASE_URI = "/oauth2/authorization";

protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {
    try {
        OAuth2AuthorizationRequest authorizationRequest =
                this.authorizationRequestResolver.resolve(request);
        if (authorizationRequest != null) {
            sendRedirectForAuthorization(request, response, authorizationRequest);
            return;                     // the chain STOPS here
        }
    }
    catch (Exception failed) {
        this.authenticationFailureHandler.onAuthenticationFailure(request, response,
                new OAuth2AuthenticationException(new OAuth2Error("invalid_request"), failed));
        return;
    }
    filterChain.doFilter(request, response);
}

private void sendRedirectForAuthorization(HttpServletRequest request, HttpServletResponse response,
        OAuth2AuthorizationRequest authorizationRequest) throws IOException {
    if (AuthorizationGrantType.AUTHORIZATION_CODE.equals(authorizationRequest.getGrantType())) {
        this.authorizationRequestRepository.saveAuthorizationRequest(
                authorizationRequest, request, response);      // state, nonce, code_verifier
    }
    this.authorizationRedirectStrategy.sendRedirect(request, response,
            authorizationRequest.getAuthorizationRequestUri());
}
```

```java
// HttpSessionOAuth2AuthorizationRequestRepository (simplified)
public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
    String stateParameter = getStateParameter(request);       // request.getParameter("state")
    if (stateParameter == null) {
        return null;
    }
    return getAuthorizationRequests(request).get(stateParameter);
}
```

**The `state` comparison is the map lookup.** There is no explicit `equals` on a `state` string
anywhere — the returned parameter is the key into a session-scoped map, and a miss means this
browser never started the request. That makes the check impossible to forget, and it supports
concurrent logins from multiple tabs because the map holds one entry per in-flight `state`.

```java
// OAuth2LoginAuthenticationFilter (simplified)
public static final String DEFAULT_FILTER_PROCESSES_URI = "/login/oauth2/code/*";

public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
    MultiValueMap<String, String> params =
            OAuth2AuthorizationResponseUtils.toMultiMap(request.getParameterMap());
    if (!OAuth2AuthorizationResponseUtils.isAuthorizationResponse(params)) {
        throw new OAuth2AuthenticationException(new OAuth2Error("invalid_request"));
    }
    OAuth2AuthorizationRequest authorizationRequest =
            this.authorizationRequestRepository.removeAuthorizationRequest(request, response);
    if (authorizationRequest == null) {
        throw new OAuth2AuthenticationException(new OAuth2Error("authorization_request_not_found"));
    }
    String registrationId = authorizationRequest.getAttribute(OAuth2ParameterNames.REGISTRATION_ID);
    ClientRegistration clientRegistration =
            this.clientRegistrationRepository.findByRegistrationId(registrationId);
    // ... delegate to the AuthenticationManager, then:
    OAuth2AuthorizedClient authorizedClient = new OAuth2AuthorizedClient(
            clientRegistration, oauth2Authentication.getName(), accessToken, refreshToken);
    this.authorizedClientRepository.saveAuthorizedClient(
            authorizedClient, oauth2Authentication, request, response);
    return oauth2Authentication;
}
```

The `registrationId` is read **from the stored authorization request**, not from the callback
URL's path variable. That is a deliberate mix-up mitigation: even if an attacker reaches a
different callback path, the provider used for the exchange is the one the application chose.

```java
// OidcAuthorizationCodeAuthenticationProvider (simplified)
public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    OAuth2LoginAuthenticationToken token = (OAuth2LoginAuthenticationToken) authentication;

    // 1. Only handle OIDC; otherwise let the plain OAuth2 provider run.
    if (!token.getAuthorizationExchange().getAuthorizationRequest()
              .getScopes().contains(OidcScopes.OPENID)) {
        return null;
    }
    OAuth2AccessTokenResponse accessTokenResponse = getResponse(token);
    Map<String, Object> additionalParameters = accessTokenResponse.getAdditionalParameters();
    if (!additionalParameters.containsKey(OidcParameterNames.ID_TOKEN)) {
        throw new OAuth2AuthenticationException(new OAuth2Error(INVALID_ID_TOKEN_ERROR_CODE,
                "Missing (required) ID Token in Token Response", null));
    }
    // 2. Decode and validate the ID token: signature, iss, aud, exp, azp.
    OidcIdToken idToken = createOidcToken(clientRegistration, accessTokenResponse);
    // 3. Compare the nonce claim against the hash stored in the request attributes.
    validateNonce(token, idToken);

    OidcUser oidcUser = this.userService.loadUser(new OidcUserRequest(
            clientRegistration, accessTokenResponse.getAccessToken(), idToken, additionalParameters));
    // ...
}
```

The nonce is handled indirectly: `OAuth2AuthorizationRequest` stores the raw `nonce` as an
**attribute** and sends a *hash* of it as the `nonce` **parameter**, so the value on the wire
cannot address session state. `validateNonce` recomputes the hash and compares.

```java
// OidcClientInitiatedLogoutSuccessHandler (simplified)
protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response,
        Authentication authentication) {
    String targetUrl = null;
    if (authentication instanceof OAuth2AuthenticationToken
            && authentication.getPrincipal() instanceof OidcUser) {
        ClientRegistration clientRegistration = this.clientRegistrationRepository
                .findByRegistrationId(((OAuth2AuthenticationToken) authentication)
                        .getAuthorizedClientRegistrationId());
        URI endSessionEndpoint = endSessionEndpoint(clientRegistration);   // discovery metadata
        if (endSessionEndpoint != null) {
            String idToken = ((OidcUser) authentication.getPrincipal()).getIdToken().getTokenValue();
            targetUrl = endpointUri(endSessionEndpoint, idToken,
                    postLogoutRedirectUri(request, clientRegistration));
            // ...id_token_hint=<jwt>&post_logout_redirect_uri=<url>
        }
    }
    return (targetUrl != null) ? targetUrl : super.determineTargetUrl(request, response);
}
```

`endSessionEndpoint` comes from `configurationMetadata.get("end_session_endpoint")`, so
**RP-initiated logout only works when the registration was built from an `issuer-uri`** and
discovery populated the metadata. A registration assembled from individual endpoint properties has
no `end_session_endpoint`, and the handler silently falls back to the local logout success URL —
which looks like "logout does not log me out of the provider" and is hard to trace.

---

## Configuration Reference

| Option | Effect | Default |
|---|---|---|
| `oauth2Login().loginPage(...)` | custom login page; disables the generated one | generated page at `/login` |
| `oauth2Login().authorizationEndpoint().baseUri(...)` | where the flow starts | `/oauth2/authorization` |
| `...authorizationEndpoint().authorizationRequestResolver(...)` | customise the outbound request (PKCE, extra parameters) | `DefaultOAuth2AuthorizationRequestResolver` |
| `...authorizationEndpoint().authorizationRequestRepository(...)` | where `state`/`nonce`/`code_verifier` live | `HttpSessionOAuth2AuthorizationRequestRepository` |
| `oauth2Login().redirectionEndpoint().baseUri(...)` | callback path pattern | `/login/oauth2/code/*` |
| `oauth2Login().tokenEndpoint().accessTokenResponseClient(...)` | how the code is exchanged | `DefaultAuthorizationCodeTokenResponseClient`; `RestClient`-based from 6.4 |
| `oauth2Login().userInfoEndpoint().userService(...)` | plain OAuth2 user loading | `DefaultOAuth2UserService` |
| `oauth2Login().userInfoEndpoint().oidcUserService(...)` | OIDC user loading | `OidcUserService` |
| `oauth2Login().userInfoEndpoint().userAuthoritiesMapper(...)` | rewrite authorities after loading | none |
| `oauth2Login().defaultSuccessUrl(url, alwaysUse)` | landing page after login | saved request, else `/` |
| `oauth2Client()` | enables `OAuth2AuthorizationCodeGrantFilter` for outbound tokens | callback `/authorize/oauth2/code/*` |
| `spring.security.oauth2.client.registration.<id>.redirect-uri` | callback template | `{baseUrl}/login/oauth2/code/{registrationId}` |
| `spring.security.oauth2.client.provider.<id>.issuer-uri` | discovery; the only way to obtain `end_session_endpoint` | unset |
| `spring.security.oauth2.client.provider.<id>.user-name-attribute` | which claim becomes `getName()` | `sub` for OIDC, `id` for GitHub/Facebook |
| `server.forward-headers-strategy` | makes `{baseUrl}` resolve to the public URL | `none` |
| `OidcClientInitiatedLogoutSuccessHandler.setPostLogoutRedirectUri(...)` | where the provider returns after logout | none — the provider decides |

---

## Production Concerns & Anti-Patterns

**Auto-linking accounts by email address.** The most damaging mistake here. An email in a token is
a claim, not a proof. Key on `(iss, sub)`, require `email_verified` from an explicit allowlist of
providers, and make linking an explicit, re-authenticated action from account settings.

**`SessionCreationPolicy.STATELESS` on a chain carrying `oauth2Login`.** Every login fails with
`authorization_request_not_found`, often intermittently at first because some sessions survive.
Split into two `SecurityFilterChain` beans, and remember the ordering rule from
[`01_M1_T1_HTTP_Web_Basics.md`](01_M1_T1_HTTP_Web_Basics.md): the first matching chain wins, so the
catch-all goes last.

**`SameSite=Strict` on the session cookie.** The provider callback is a cross-site top-level
navigation, so `Strict` suppresses the cookie and the session appears empty on return. Use `Lax`.

**Ignoring the reverse proxy.** `{baseUrl}` is expanded from the incoming request, which behind a
TLS-terminating load balancer means an internal host and the wrong scheme. Set
`server.forward-headers-strategy=framework`, and ensure the proxy strips client-supplied
`X-Forwarded-*` headers — otherwise an attacker influences your generated `redirect_uri`, the exact
hazard flagged in [`01_M1_T1_HTTP_Web_Basics.md`](01_M1_T1_HTTP_Web_Basics.md).

**`InMemoryOAuth2AuthorizedClientService` in a multi-instance deployment.** Tokens live in one
instance's heap, so a restart or a reroute produces "authorized client not found" and an
unexplained re-consent. Use `JdbcOAuth2AuthorizedClientService` and treat the refresh token column
as a secret at rest.

**Requesting every scope at first login.** Extra scopes make the consent screen scarier and widen
the blast radius of a leaked token. Request the minimum at sign-in and use **incremental
authorisation** — a second authorization request when the feature is actually used.

**Assuming you received the scopes you asked for.** Some providers let the user deselect scopes on
the consent screen, and some silently trim. Read the `scope` value from the token response and
degrade gracefully rather than failing deep inside a downstream call.

**Forgetting provider logout.** Clearing the local session while the provider session survives
means "log out" then "log in" silently re-authenticates — on a shared machine that is an account
exposure. Use `OidcClientInitiatedLogoutSuccessHandler`, and note it needs discovery metadata. For
multi-application single sign-on, OIDC Back-Channel Logout (`http.oidcLogout(...)`, 6.2+) is the
complete answer.

**Treating a client secret as safe because it is in an environment variable.** It is still a shared
secret, still visible in process listings and orchestrator manifests, still replayable. For
high-value clients prefer `private_key_jwt`, where the provider holds only a public key.

**Logging the token response.** `org.springframework.security.oauth2` at `TRACE` in production
writes access, refresh and ID tokens into log aggregation, searchable by anyone with log access
and retained for months.

---

## Debugging Playbook

| Symptom | Likely root cause | Fix |
|---|---|---|
| `redirect_uri_mismatch` at the provider | `{baseUrl}` expanded to the internal host or `http` scheme behind a proxy | `server.forward-headers-strategy=framework`; log the generated `Location`; register the exact public URL |
| `authorization_request_not_found` on the callback | Session lost: stateless chain, `SameSite=Strict`, no sticky sessions, or a short timeout | Allow sessions on the login chain; `same-site: lax`; shared session store; timeout longer than the MFA journey |
| Login works locally, fails in the cluster | No shared session store and no sticky sessions, so the callback lands elsewhere | Spring Session with Redis, or sticky sessions |
| `IllegalArgumentException: Missing attribute 'sub' in attributes` | `userNameAttributeName` is `sub` but the provider is not OIDC | `user-name-attribute: id` for GitHub/Facebook, or pass the right key to `DefaultOAuth2User` |
| Default login page lists no providers | A custom `ClientRegistrationRepository` that does not implement `Iterable<ClientRegistration>` | Implement `Iterable`, or supply a custom login page |
| Logout clears the local session but the provider logs the user straight back in | RP-initiated logout not configured, or no `end_session_endpoint` because the registration was built from individual URIs | `OidcClientInitiatedLogoutSuccessHandler` plus `issuer-uri` |
| Logout redirects to the provider and then errors | `post_logout_redirect_uri` not registered at the provider | Register the exact URI; set it via `setPostLogoutRedirectUri` |
| `ClientAuthorizationRequiredException` on a downstream call | No stored `OAuth2AuthorizedClient` for this principal and registration | Confirm the user completed that flow; check the store survives restarts |
| Downstream calls fail after an hour | Access token expired and no refresh token was issued (missing `offline_access`, or the provider needs `access_type=offline`/`prompt=consent`) | Add the scope or the provider-specific parameter via an `OAuth2AuthorizationRequestCustomizer` |
| Authorities contain only `ROLE_USER` and `SCOPE_*` | Provider roles live in a claim the default mapping ignores | Add a `GrantedAuthoritiesMapper` or a custom `OidcUserService` |
| PKCE parameters missing from the authorization URL | Confidential client and the customizer was not applied | `resolver.setAuthorizationRequestCustomizer(OAuth2AuthorizationRequestCustomizers.withPkce())` |

---

## Interview Q&A

### Q1. What is the difference between `http.oauth2Login()` and `http.oauth2Client()`, and when do you use each?

<details>
<summary>Show answer</summary>

`oauth2Login` makes the external provider your **authentication source**. It registers
`OAuth2AuthorizationRequestRedirectFilter` and `OAuth2LoginAuthenticationFilter`, runs the
authorization code flow, and on success places an `OAuth2AuthenticationToken` into the
`SecurityContext` with an `OidcUser` or `OAuth2User` principal. The user is now logged into your
application, so `authorizeHttpRequests`, `@PreAuthorize` and `authentication.getName()` all work
against that principal. Its callback path is `/login/oauth2/code/*`.

`oauth2Client` authenticates nobody. It registers `OAuth2AuthorizationRequestRedirectFilter` and
`OAuth2AuthorizationCodeGrantFilter`, whose job is to obtain and store an `OAuth2AuthorizedClient`
so you can attach a token to outbound calls. The user may be authenticated by form login, by a
resource-server JWT, or not at all when the registration uses `client_credentials`. Its callback
is `/authorize/oauth2/code/*`, deliberately different so the two never collide.

The decision rule is the question being asked. "Who is the user?" is `oauth2Login`. "What token do
I attach to this outbound request?" is `oauth2Client`. An application that lets users sign in with
the corporate identity provider *and* connect their GitHub to import repositories uses both.

**Counter-question: I have `oauth2Login` configured and want to call the provider's API with the user's token. Do I also need `oauth2Client`?**

Not strictly. `oauth2Login` enables the client infrastructure as part of its configuration — the
`OAuth2AuthorizedClientRepository`, the manager, and the storage of the authorized client at the
end of `OAuth2LoginAuthenticationFilter.attemptAuthentication` — so
`@RegisteredOAuth2AuthorizedClient` resolves without declaring `oauth2Client`.

You add it explicitly when you need a flow login does not provide: tokens for a *different*
registration than the one that logged the user in, which needs the extra authorization endpoint and
callback filter to run a second consent flow; or grants such as `client_credentials` for a service
registration with no user. I declare it anyway when both are in play, so the configuration states
the intent rather than relying on a side effect.

**Counter-question: what actually persists the `OAuth2AuthorizedClient`, and when?**

`OAuth2LoginAuthenticationFilter.attemptAuthentication` constructs it from the authentication
result — registration, principal name, access token, refresh token — and calls
`OAuth2AuthorizedClientRepository.saveAuthorizedClient(...)` before returning. So it happens at the
end of a successful login, inside the callback request, before the success handler redirects.

The default repository is `AuthenticatedPrincipalOAuth2AuthorizedClientRepository`, which delegates
to the `OAuth2AuthorizedClientService` for authenticated principals and to
`HttpSessionOAuth2AuthorizedClientRepository` for anonymous ones. That branch is why "it works in
development and loses tokens in production" happens: the default service is
`InMemoryOAuth2AuthorizedClientService`, so tokens sit in one instance's heap and do not survive a
restart or a reroute.

**Counter-question: for a pure `client_credentials` registration with no user, where does the token get stored and under what principal?**

Under whatever principal name you supply on the `OAuth2AuthorizeRequest`. With no user there is no
natural key, so you pass a label —
`OAuth2AuthorizeRequest.withClientRegistrationId("x").principal("nightly-sync")` — and the manager
stores and looks up the authorized client under that name.

In a background context you must use `AuthorizedClientServiceOAuth2AuthorizedClientManager`, not
`DefaultOAuth2AuthorizedClientManager`, because the latter reaches for the current
`HttpServletRequest` and a scheduler thread has none. Getting that wrong produces an
`IllegalArgumentException` about a missing request attribute that looks nothing like an OAuth2
problem.
</details>

### Q2. Trace what happens from clicking "Sign in with Google" to a `SecurityContext` existing.

<details>
<summary>Show answer</summary>

The link points at `/oauth2/authorization/google`.
`OAuth2AuthorizationRequestRedirectFilter` matches it and hands the request to
`DefaultOAuth2AuthorizationRequestResolver`, which extracts `google` as the `registrationId`, loads
the `ClientRegistration`, and builds an `OAuth2AuthorizationRequest`: `response_type=code`, the
client ID, the scopes, a random `state`, a `nonce` (stored raw as an attribute, sent hashed as a
parameter), and — for a public client, or a confidential one with the PKCE customizer — a
`code_verifier` attribute plus a `code_challenge` parameter. The redirect template is expanded from
the current request, which is where `{baseUrl}` becomes concrete.

The filter calls `saveAuthorizationRequest`, which puts the object into the `HttpSession` in a map
keyed by `state`, sends a `302` to Google's authorization endpoint, and **stops the chain**.

After authentication and consent, Google redirects to `/login/oauth2/code/google?code=...&state=...`.
`OAuth2LoginAuthenticationFilter` matches and calls `removeAuthorizationRequest`, which uses the
returned `state` as the key into the session map — that lookup *is* the `state` verification, and a
miss throws `authorization_request_not_found`. It reads the `registrationId` from the stored request
rather than the URL, which is the mix-up mitigation, and hands an `OAuth2LoginAuthenticationToken`
to the `AuthenticationManager`.

`OidcAuthorizationCodeAuthenticationProvider` claims it because the scopes contain `openid`. It
posts to the token endpoint with the code, the same `redirect_uri`, the `code_verifier` and client
authentication; requires an `id_token` in the response; decodes it against Google's JWKS; and
validates issuer, audience, expiry and `nonce`. It then calls `OidcUserService.loadUser`, which
fetches the UserInfo claims, merges them with the ID token claims, and produces a `DefaultOidcUser`
with `ROLE_USER` and `SCOPE_*` authorities, which a `GrantedAuthoritiesMapper` may rewrite.

Back in the filter, the result becomes an `OAuth2AuthenticationToken`, the `OAuth2AuthorizedClient`
is saved, `AbstractAuthenticationProcessingFilter` runs the session authentication strategy —
rotating the session identifier — stores the context, and redirects to the saved request.

**Counter-question: at which exact point is CSRF on the callback prevented, and what if that check were removed?**

At `removeAuthorizationRequest`, when the returned `state` is used as the map key. Without it, an
attacker could complete a flow with *their own* Google account, capture the resulting callback URL
containing a valid unused code, and induce the victim's browser to visit it.

The victim's browser hits your callback, your application exchanges the attacker's code, and the
victim is silently logged in as the attacker. That sounds harmless until you consider what the
victim does next — uploads a document, connects a payment method, types confidential notes — all
into the attacker's account, which the attacker reads at leisure. It is why `state` is effectively
mandatory even though RFC 6749 lists it as optional.

**Counter-question: the `nonce` is stored raw in the session but sent hashed on the wire. Why?**

So the value visible in the URL cannot address session state. The authorization request URL passes
through the browser, the provider's logs, and possibly a referrer; if the wire value were also the
session key, anyone observing it would hold a handle to your server-side state.

Sending `SHA-256` of the stored value keeps the raw nonce private to the server while preserving the
comparison — at validation time the framework hashes the stored value again and compares it with the
claim inside the ID token. The property you need from a nonce survives; the correlatable handle does
not leak.

**Counter-question: where does session fixation protection happen here, and why does it matter more than in form login?**

`OAuth2LoginAuthenticationFilter` extends `AbstractAuthenticationProcessingFilter`, which invokes
the configured `SessionAuthenticationStrategy` — by default `ChangeSessionIdAuthenticationStrategy`
— after a successful authentication and before the success handler.

It matters more here because the flow *requires* a session to exist before authentication, to hold
the authorization request. So there is guaranteed to be a pre-authentication session identifier
created while the user was anonymous. An attacker who can plant a known session identifier in the
victim's browser — a subdomain cookie, a cookie-injection flaw — would otherwise hold a valid handle
on the session that becomes authenticated. Rotating the identifier at authentication invalidates the
planted value, so disabling session fixation protection on an `oauth2Login` chain is a serious
misconfiguration.
</details>

### Q3. How do you map provider claims onto your application's authorities, and where does that logic go?

<details>
<summary>Show answer</summary>

Three extension points, not interchangeable.

**`OAuth2UserService` / `OidcUserService`** is right when you need the claims themselves, a database,
or a decision depending on a local record. It runs inside the authentication provider, after the
token exchange and ID token validation, and returns the principal. Just-in-time provisioning belongs
here: look up `(iss, sub)` in `user_identities`, create the record if absent, load that user's roles,
merge them with the provider-derived authorities.

The rule is **delegate then decorate** — construct a `DefaultOAuth2UserService` or `OidcUserService`,
call `loadUser`, and build a new principal from the result with your merged authority set. Never
reimplement the UserInfo call, which handles content negotiation, the
`user-info-authentication-method` modes, and error translation into `OAuth2AuthenticationException`.

**`GrantedAuthoritiesMapper`** is right for a cross-cutting rewrite needing no database. It runs
after the user service and, because it also applies to LDAP and SAML, is the natural home for a rule
such as "every `SCOPE_x` also grants `ROLE_X`". The authorities passed in are `OidcUserAuthority` or
`OAuth2UserAuthority`, which carry the ID token and attribute map, so you can still read claims.

**A custom `AuthenticationSuccessHandler`** is the wrong place, and people try it. By then the
`Authentication` is built and stored; mutating authorities there produces inconsistencies between the
context and what was persisted.

I keep the provider's `SCOPE_*` authorities alongside my `ROLE_*` ones. Discarding the scopes
destroys the role-and-scope intersection checks from
[`02_M1_T2_Authentication_Authorization.md`](02_M1_T2_Authentication_Authorization.md), which are
what stop a narrowly-consented third-party client inheriting an admin's powers.

**Counter-question: your `OidcUserService` hits the database on every login. Any concerns?**

It is once per login, not per request, so throughput is fine. The concerns are failure and
correctness.

If the database is down, login fails *after* a successful token exchange, so the user has consumed a
single-use code and a round trip to the provider. I would fail fast with a short timeout, translate
it into an `OAuth2AuthenticationException` with a clear error code so the user sees a sensible
message, and make sure retries cannot duplicate provisioning.

Then concurrency: two tabs logging in simultaneously both miss the lookup and both insert. The fix is
a unique constraint on `(issuer, subject)` and handling the violation by re-reading, not an
application-level check-then-act, which is a race by construction. Finally, authorities snapshotted
at login are stale until the next login — if revocation timeliness matters, keep sessions short or
re-resolve sensitive authorities per request.

**Counter-question: GitHub returns no email when the user has it private. How do you handle that?**

Accept that `/user` is incomplete and call `/user/emails` with the `user:email` scope, which returns
every address with `primary` and `verified` flags. That goes inside a custom `OAuth2UserService` —
GitHub is not OIDC, so it is a `DefaultOAuth2UserService` delegate plus a second call with the same
access token — selecting the address that is both primary and verified.

The important part is what happens when there is none. **Not** falling back to an unverified address,
because that walks into the linking vulnerability. If no verified primary email exists, create the
account without one and prompt the user to add and verify an address inside your application, keeping
the invariant that every email on a local account was verified by somebody you trust.

**Counter-question: two providers return the same email. Should they be the same user?**

Not automatically. An email address is a claim made by a provider, not proof of identity, and
providers vary enormously in whether and how they verify it. Silently merging means anyone who can
get a provider to assert a victim's address takes over the victim's account without a password.

My rules: the identity key is `(iss, sub)`; a local user may have many identities; auto-link only when
the provider is on an explicit allowlist of "trusted to verify email" *and* the claim carries
`email_verified: true`; otherwise create a separate account and let the user link deliberately from
settings while already authenticated as the existing user, with re-authentication because linking is
an account-takeover primitive.

I would also handle the reverse case without leaking: if someone signs up with an address that already
exists on another identity, do not tell the browser the account exists — send a message to the address
itself, or you have built an account-enumeration oracle.
</details>

### Q4. Your application authenticates users with `oauth2Login` and must also call a partner API as a service. Design the token handling.

<details>
<summary>Show answer</summary>

Two registrations, two grant types, and one rule about not mixing the identities.

The **user-facing** registration uses `authorization_code` with `openid` against the corporate
identity provider. Its tokens are per-user, stored as `OAuth2AuthorizedClient` records keyed by
`(registrationId, principalName)`, representing a delegation from that specific human.

The **partner** registration uses `client_credentials`. No user, no browser, no consent. Its token
represents the application itself and its `sub` is the client ID — and as
[`02_M1_T2_Authentication_Authorization.md`](02_M1_T2_Authentication_Authorization.md) notes, roles
are meaningless for it; only scopes are.

Storage is `JdbcOAuth2AuthorizedClientService` rather than the in-memory default, because the
application runs on several instances and user refresh tokens must survive a restart and a reroute.
The refresh token column is a credential at rest and gets encrypted.

Acquisition goes through an explicit `OAuth2AuthorizedClientManager` built with
`OAuth2AuthorizedClientProviderBuilder.builder().authorizationCode().refreshToken().clientCredentials().build()`.
In request handling that is a `DefaultOAuth2AuthorizedClientManager`; in the nightly batch job, which
has no request, it must be `AuthorizedClientServiceOAuth2AuthorizedClientManager`.

Outbound calls use a `WebClient` with `ServletOAuth2AuthorizedClientExchangeFilterFunction` — or,
from 6.4, a `RestClient` with `OAuth2ClientHttpRequestInterceptor` — so attachment and refresh are
automatic and no code handles a raw token string. The rule I write down: **never forward the user's
access token to the partner API, and never use the partner token to act on a user's behalf.**
Different audiences, different meanings; mixing them is the confused-deputy pattern.

**Counter-question: the partner API needs to know which user the request is for. How do you pass that without forwarding the user's token?**

The cleanest standard mechanism is **token exchange**, RFC 8693, supported in Spring Security 6.3
through `TokenExchangeOAuth2AuthorizedClientProvider`. The application presents the user's token to
the authorization server and receives a *new* token audienced for the partner that retains the user's
subject and may carry an `act` claim naming the acting service. The partner then sees who the request
is for and who is making it, asserted by the authorization server rather than by my application.

If the partner does not support that, a client-credentials token plus an explicit user identifier in
the body or a dedicated header works, but the partner is now trusting *my* assertion rather than the
authorization server's, so it is only acceptable with a real trust relationship and should be
documented as such.

Forwarding the user's access token is the option to avoid. Its audience is not the partner, so a
correctly configured partner rejects it; and if they accept it, I have handed a third party a
credential that works against my *other* APIs too — the audience-confusion attack from
[`29_M10_T1_OAuth2_OIDC_Fundamentals.md`](29_M10_T1_OAuth2_OIDC_Fundamentals.md).

**Counter-question: your nightly job wakes up and the stored client-credentials token has expired. What happens?**

Nothing bad. `ClientCredentialsOAuth2AuthorizedClientProvider` compares the stored `expiresAt`
against the current time plus a **60-second default clock skew**, and if the token is expired or
within that skew it simply requests a new one. There is no refresh token for client credentials and
none is needed, because the client can always re-authenticate with its own credential.

Two notes: the skew is configurable via `setClockSkew`, and it exists because a token passing your
expiry check can still be rejected by a resource server whose clock runs ahead. And if the
authorization server is unreachable at that moment the manager throws rather than returning a stale
token, so the job needs its own retry with backoff.

**Counter-question: a user's refresh token has been revoked at the provider. What does the application see, and what should it do?**

`RefreshTokenOAuth2AuthorizedClientProvider` attempts the refresh, the provider answers
`invalid_grant`, and Spring raises an `OAuth2AuthorizationException`. With the exchange filter
function that surfaces during the outbound call, which is a confusing place to discover an
authentication problem.

The correct handling is to **remove the stored authorized client and re-run the authorization flow**.
There is a purpose-built mechanism — `OAuth2AuthorizationFailureHandler`, specifically
`RemoveAuthorizedClientOAuth2AuthorizationFailureHandler`, which deletes the record on
`invalid_grant` so nothing keeps retrying a dead token. Wire it into the
`DefaultOAuth2AuthorizedClientManager`. The user is then redirected into the flow and re-consents,
which is the right outcome: the provider revoked the delegation and only the user can restore it.
What you must not do is swallow the failure or retry in a loop, which turns a clear signal into an
outage that looks like a partner problem.
</details>

### Q5. A colleague implements "sign in with Google or GitHub" and links accounts by email. Explain precisely why that is a vulnerability.

<details>
<summary>Show answer</summary>

Because the code treats an email address as proof of identity when it is only a **claim made by a
provider**, and the trustworthiness of that claim varies by provider, by configuration, and
sometimes by the user's own settings.

Concretely: Alice has an account under `alice@corp.com` created with a password. The attacker needs
a provider you accept that will assert `alice@corp.com` for an account they control. The family of
options is large — a provider that lets a user set an arbitrary unverified email; a provider where
the attacker controls the domain, which matters if you support enterprise customers bringing their
own identity provider; a provider exposing an unconfirmed address; or simply a provider whose
response you read without checking the verification flag. The attacker signs in, your code looks up
by email, finds Alice's account, and creates a session for her. No password required.

It is a *class* of vulnerability rather than one bug because the vulnerable code looks completely
reasonable. `findByEmail(...).orElseGet(create)` is idiomatic and reads as obviously correct,
nothing in the framework warns you, and it usually ships because linking is treated as a
user-experience problem rather than a security decision.

The correct model: the identity key is `(iss, sub)` in a `user_identities` table with a unique
constraint, and a local user may have several identities. Auto-linking by email is permitted only
when the provider is on an explicit allowlist *and* the claim carries `email_verified: true`.
Everything else is an explicit linking action from account settings, by a user already authenticated
as the existing account, with a re-authentication step — because linking a new identity is an
account-takeover primitive deserving the same treatment as a password change.

**Counter-question: I check `email_verified`. Am I safe now?**

Better, not finished, because the flag means different things to different issuers. It tells you the
*provider* believes the address is verified. It does not tell you the provider verified it in a way
you would accept, nor that the address belongs to a domain you should trust from that provider.

A concrete case that has burned real products: a provider supporting custom domains will assert
`email_verified: true` for `alice@corp.com` if the attacker proved control of `corp.com` **in their
own tenant**. From your side the claim looks perfect. So the check needs two more parts. Bind
acceptable domains to acceptable issuers — only `iss = https://corp-tenant.idp.example.com` may
assert addresses at `corp.com`. And handle providers with no verification concept: GitHub's `/user`
has no flag, so you must call `/user/emails` and require both `primary` and `verified`, treating
their absence as "no email". The principle is that the allowlist is per-provider and deliberate,
never a general "if `email_verified` then link" rule.

**Counter-question: your design creates duplicate accounts when a user signs in with Google one day and GitHub the next. Users will complain. Fix the experience without reintroducing the flaw.**

Make linking explicit but well-signposted, and move the confirmation to a channel the attacker does
not control. When a new identity arrives with an email matching an existing account, do not link and
do not tell the browser the account exists — that is an enumeration oracle. Send a message **to the
email address**: "someone signed in with GitHub using this address; if that was you, click here to
link it to your existing account". The link requires authenticating with the existing account before
the link is created. The attacker never receives that email, so the flow is safe, and the genuine
user experiences one extra click once.

The other half is prevention: on the sign-in page remember which provider the user used last, in a
long-lived cookie that is a hint rather than a security decision, and surface it prominently. Most
duplicate-account complaints come from users who forgot which button they pressed, which is a
user-interface fix rather than a security trade-off.

**Counter-question: an enterprise customer wants their employees auto-provisioned from their own identity provider. Does that change your answer?**

It changes the trust boundary in a way that makes auto-linking acceptable, provided the boundary is
explicit. In that arrangement the customer's identity provider is authoritative for their domain by
contract. I would register it as its own `ClientRegistration` with its own `issuer-uri` and record a
mapping from that issuer to the domains it may assert. An identity from that issuer claiming
`alice@corp.com` is auto-provisioned into the `corp.com` tenant, because the issuer is authorised for
that domain and only that domain; the same issuer asserting `gmail.com` is rejected.

Two additions. Domain ownership is verified out of band at onboarding through a DNS record, not
asserted by whoever configured the integration. And I keep the enterprise path separate from the
consumer social-login path, including at the `SecurityFilterChain` level where practical, so a change
to consumer linking rules can never widen the enterprise one by accident. Mixing the two populations
under one rule set is how a careful design erodes six months later.
</details>

### Q6. Design question — single sign-on for twelve internal Spring Boot applications, server-rendered and single-page, plus contractors on a separate identity provider.

<details>
<summary>Show answer</summary>

Three decisions: one identity authority, one client pattern per application shape, and an explicit
story for the two populations.

**One authorization server as the identity authority** — Keycloak or the corporate tenant. Employees
authenticate against the corporate directory. Contractors authenticate against their own identity
provider **federated behind the same authorization server**, not as a second authorization server
every application must know about. That choice is what keeps the twelve applications simple: each
trusts exactly one issuer, and the population distinction arrives as a claim rather than as
configuration in twelve places.

**Each application is its own client registration** — separate `client_id`, separate exact redirect
URIs per environment, separate allowed scopes. One shared registration would mean a leaked secret
compromises all twelve and no audience separation is possible.

**Server-rendered applications** use `oauth2Login` with authorization code and PKCE even though they
are confidential clients, and a session cookie that is `HttpOnly`, `Secure`, `SameSite=Lax`, with a
`__Host-` prefix. Sessions go into Spring Session on Redis, because the authorization request state
must survive a load-balancer reroute and because otherwise every deployment logs everyone out. CSRF
protection stays on, since the credential is now ambient.

**Single-page applications** get a backend-for-frontend rather than tokens in the browser. The backend
is the confidential client, holds the tokens, and gives the browser the same hardened cookie session.
The reasoning is the one in [`01_M1_T1_HTTP_Web_Basics.md`](01_M1_T1_HTTP_Web_Basics.md): with an
`HttpOnly` cookie an XSS flaw is a bounded in-page compromise, while with a token in `localStorage`
the same flaw is silent remote exfiltration.

**Service-to-service traffic** uses `client_credentials`, or token exchange where user context must be
preserved. Each application's API is a resource server validating its own `aud`, which is what stops a
token for the low-value wiki being replayed against the high-value finance application.

**Logout must be real single logout.** Local logout alone is misleading — sign out, sign in, and the
user is back with no prompt. So `OidcClientInitiatedLogoutSuccessHandler` for RP-initiated logout, and
OIDC Back-Channel Logout (`http.oidcLogout(...)`, 6.2+) so signing out of one application terminates
the other eleven. Back-channel logout needs an addressable session registry, which Spring Session
already provides.

**Counter-question: twelve registrations, twelve secrets. How do you manage them, and what breaks during rotation?**

Secrets live in a secret manager, injected at startup, never in source control or an image. For the
higher-value applications I move to `private_key_jwt`, so the authorization server stores only a
public key and rotation is a public key swap.

Rotation is what people get wrong. A naive rotation is a hard cutover: the new secret is written, the
application still holds the old one in memory, and every token exchange fails with `invalid_client`
until a restart. The fix has two halves. The authorization server must support **two active secrets**
during an overlap window, which most do. And the application must pick up the new value without a
redeploy, which means the registration is not a frozen `@ConfigurationProperties` snapshot but
something refreshable — a custom `ClientRegistrationRepository` reading from the secret manager, or a
refresh-scoped bean. I would also make rotation routine and scheduled, because a path exercised once
a year during an incident is a path that does not work, and I would alert on `invalid_client` rates,
since that is the otherwise-invisible signal that a rotation went wrong.

**Counter-question: a contractor's engagement ends. What must happen for access to actually stop?**

Disabling the account at the contractor's own identity provider is only the first step and on its own
stops nothing immediately, because existing sessions and tokens outlive it.

The federated identity at our authorization server must be disabled, so no new authentication can
succeed even if the upstream provider is slow. Refresh tokens issued to every client for that subject
must be revoked, or the contractor keeps minting access tokens without ever authenticating again —
the step most often missed. Access tokens already issued remain valid until expiry, the bounded
revocation window from [`01_M1_T1_HTTP_Web_Basics.md`](01_M1_T1_HTTP_Web_Basics.md), which is the
practical reason to keep them at five to fifteen minutes. Sessions in all twelve applications must be
terminated, which is what back-channel logout and a shared session registry provide; without them a
contractor with an open browser stays logged in until timeout. Stored `OAuth2AuthorizedClient` records
for that principal must be deleted, or a background job keeps acting on their behalf with a stored
refresh token. And local application records must be marked disabled, so a stale session or cached
authority set is rejected at the next check.

I would automate that as a single revocation workflow triggered by an offboarding event, and test it
periodically, because a revocation path never exercised is an assumption rather than a control.

**Counter-question: security wants multi-factor authentication only for the finance application. How?**

This is step-up authentication, and the mechanism is `acr` and `auth_time`, not anything
application-local. The finance application sends `acr_values` requesting a higher authentication
context — and optionally `max_age` to bound staleness — on its authorization request, which in Spring
is an `OAuth2AuthorizationRequestCustomizer` or a custom resolver. The authorization server sees the
existing session does not satisfy the requested context, prompts for the second factor even though the
user is signed in, and issues an ID token whose `acr` reflects the stronger context and whose `amr`
lists the methods used.

The finance application must then **verify** the claim rather than assume the request was honoured —
an authorization server may return a weaker `acr` than requested, and a client that does not check has
implemented nothing. I would do that in the `OidcUserService`, rejecting the authentication if `acr`
is insufficient, and additionally gate the most sensitive operations on a recent `auth_time`.

The architectural point is that the other eleven applications need no changes at all. The policy lives
at the authorization server and in one client's request parameters, not scattered across twelve
codebases — which is the whole reason for a single identity authority.
</details>

---

## Quick Recall

```
TWO FEATURES, ONE MODULE
  oauth2Login  -> the provider AUTHENTICATES users into my app
                  OAuth2AuthorizationRequestRedirectFilter + OAuth2LoginAuthenticationFilter
                  callback /login/oauth2/code/*    principal OidcUser / OAuth2User
  oauth2Client -> only MANAGES TOKENS for outbound calls; authenticates nobody
                  ...RedirectFilter + OAuth2AuthorizationCodeGrantFilter
                  callback /authorize/oauth2/code/*
  oauth2Login implicitly enables the client infrastructure

URLS (defaults)
  start     /oauth2/authorization/{registrationId}
  callback  /login/oauth2/code/{registrationId}
  template  {baseUrl}/login/oauth2/code/{registrationId}
  one callback PER PROVIDER = the mix-up attack mitigation

REGISTRATION
  ClientRegistration -> id, secret, grant, authMethod, redirectUri, scopes, ProviderDetails
  ClientRegistrationRepository.findByRegistrationId(id)
  InMemoryClientRegistrationRepository ALSO implements Iterable
     -> a non-Iterable custom repo = empty default login page
  CommonOAuth2Provider GOOGLE GITHUB FACEBOOK OKTA, matched by registrationId
  provider: <id> points a renamed registration back at the shared block / enum

SESSION IS MANDATORY FOR oauth2Login
  state + nonce + code_verifier live in the OAuth2AuthorizationRequest
  HttpSessionOAuth2AuthorizationRequestRepository, keyed by state
  THE STATE CHECK IS THE MAP LOOKUP -> miss = authorization_request_not_found
  STATELESS chain  -> every login fails
  SameSite=Strict  -> callback loses the cookie -> use Lax
  no shared session store -> fails behind a load balancer

PRINCIPALS
  OIDC         DefaultOidcUser (getIdToken/getUserInfo/getClaims)  name = sub
  plain OAuth2 DefaultOAuth2User                                   name = "id" on GitHub/Facebook
  wrapper      OAuth2AuthenticationToken.getAuthorizedClientRegistrationId()
  local key    (iss, sub) -- NEVER getName() alone, NEVER email

CUSTOMISING
  OAuth2UserService / OidcUserService -> claims + DB + JIT provisioning; DELEGATE then DECORATE
  GrantedAuthoritiesMapper            -> cross-cutting rewrite, runs after the user service
  KEEP the SCOPE_* authorities so role AND scope intersection stays possible
  DefaultOidcUser(authorities, idToken, userInfo, nameAttributeKey)

DOWNSTREAM CALLS
  OAuth2AuthorizedClient = (registration, principalName, accessToken, refreshToken)
  OAuth2AuthorizedClientService     no request  -> jobs  (InMemory | Jdbc)
  OAuth2AuthorizedClientRepository  has request -> web   (AuthenticatedPrincipal | HttpSession)
  OAuth2AuthorizedClientManager     acquires + refreshes, 60s clock skew
     Default...Manager                  needs an HttpServletRequest
     AuthorizedClientService...Manager  for schedulers / listeners
  @RegisteredOAuth2AuthorizedClient("github") on a controller parameter
  ServletOAuth2AuthorizedClientExchangeFilterFunction (WebClient)
  OAuth2ClientHttpRequestInterceptor (RestClient, 6.4+)
  InMemory service in a cluster = lost tokens on restart/reroute -> use Jdbc

ACCOUNT LINKING (a real vulnerability class)
  findByEmail(...).orElseGet(create) = ACCOUNT TAKEOVER
  email is a CLAIM, not a proof
  key on (iss, sub); require email_verified AND a per-provider allowlist
  bind acceptable domains to acceptable issuers
  explicit linking only, already authenticated, with re-authentication

LOGOUT
  OidcClientInitiatedLogoutSuccessHandler -> end_session_endpoint + id_token_hint
                                             + post_logout_redirect_uri (must be registered)
  needs issuer-uri discovery, else end_session_endpoint is absent and it silently no-ops
  http.oidcLogout(...) = OIDC Back-Channel Logout (6.2+) for true single logout

PROXY
  {baseUrl} is expanded from the incoming request
  server.forward-headers-strategy=framework, and the proxy MUST strip client X-Forwarded-*
```

---

**Previous:** [`29_M10_T1_OAuth2_OIDC_Fundamentals.md`](29_M10_T1_OAuth2_OIDC_Fundamentals.md) ·
**Next:** [`31_M10_T3_Resource_Server.md`](31_M10_T3_Resource_Server.md)
