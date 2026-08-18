# 2nd Technical Round — Java Backend (5–8 Years)

This file is **only for Round 2** (deep technical / design round).

Round 1 (Core Java, collections, Spring basics, SQL, DSA) is in a separate file. Manager and HR are not here.

At 5–8 years, Round 2 is not “what is a microservice?”. They want **trade-offs, failure modes, and something you actually ran in production**.

**What Round 2 usually is (60–90 minutes)**

1. Draw / explain your current project (ESM) — 10–15 minutes
2. Hibernate/JPA deep dive (locking, fetch, persistence context) if Round 1 was light
3. Spring Security (filter chain, JWT resource server, method security) + service-to-service auth
4. Microservices + distributed data (saga, outbox, idempotency)
5. Kafka internals
6. AWS + CI/CD
7. One system design on the whiteboard (15–20 minutes)
8. Sometimes one production scenario: “p99 went from 200ms to 3s — what do you do?”

**How to use this file**

- Theory and system design answers are **open**. Speak them.
- Small design-code problems are **hidden**. Try first, then open.
- Start with the short answer. Add internals only if they probe.
- If you have not used a tool, say so, then compare it to what you **have** used.

**Full study time: about 10–11 hours.** For fast revision, prioritize ESM architecture, saga/outbox/idempotency, Kafka, resilience, one system design, security, migrations, and the night checklist.

| Time | What to finish |
|---|---|
| 1.0h | ESM architecture (you must draw this) |
| 1.0h | Spring Security + Hibernate/JPA deep |
| 1.5h | Microservices + consistency |
| 1.5h | Kafka |
| 1.0h | AWS |
| 0.75h | CI/CD, Docker, deploy |
| 1.5h | System design (speak 4 designs out loud) |
| 1.0h | Testing, API/data migrations, datastore choices |
| 0.75h | Production debugging + hidden coding |
| 0.75h | One LLD/machine-coding exercise |

---

## Table of contents

1. [ESM project — Round 2 depth](#1-esm-project--round-2-depth)
2. [Microservices](#2-microservices)
3. [Distributed data and consistency](#3-distributed-data-and-consistency)
4. [Resilience](#4-resilience)
5. [Spring Security](#5-spring-security)
6. [Hibernate and JPA (deep)](#6-hibernate-and-jpa-deep)
7. [Kafka](#7-kafka)
8. [AWS](#8-aws)
9. [CI/CD, Docker, Kubernetes](#9-cicd-docker-kubernetes)
10. [Observability and production](#10-observability-and-production)
11. [System design](#11-system-design)
12. [Design / coding problems](#12-design--coding-problems-try-first)
13. [Senior testing strategy](#13-senior-testing-strategy-and-release-confidence)
14. [API, data, migrations, datastores](#14-api-data-migrations-and-datastore-decisions)
15. [Low-level design / machine coding](#15-low-level-design--machine-coding)
16. [Night-before checklist](#night-before-checklist-round-2)
17. [How to talk in Round 2](#how-to-talk-in-round-2)

**Topics covered:** Spring Security (filter chain, JWT resource server, OAuth2, method security, CSRF), Hibernate/JPA (session, flush, locking, fetch, L2 cache), monolith vs split, API gateway, saga/2PC/outbox/CDC, CAP, idempotency, circuit breaker, Kafka, AWS, CI/CD, 7 system designs.

---

## 1. ESM project — Round 2 depth

Round 1 was 90 seconds. Round 2 they say: **“Draw it. What happens when deploy is clicked?”**

Do **not** invent `/api/v1/esm` or Strategy classes. Use real names.

---

### Q. Draw ESM and walk a request.

**What you should say (and sketch)**

```text
Operator UI
    |
    v
esm-ws  (Spring MVC @Controller, servlet /oms1350/...)
    |     /esmService  (writes)
    |     /esmBrowser, /data/esmBrowser  (reads)
    |     /esmSync, /esmDiscovery, /routingDisplay, /esmPm
    v
esm-server-service
    Command / CommandManager
    Task chain  (Service_TaskChainLocator, ErpTaskChainLocator, ...)
    |
    |  persist Oracle (esm-server-dao, c3p0 pool)
    |
    v
AdapterProxyManager.getAdapterService(snaId, "Element*Service-Http")
    Kafka proxy (Kafka*ProxyFactoryBean, KafkaESMUtil)
    ReplyingKafkaTemplate
    |
    v
topic  ESM_ASYNCIF_REQUEST_{snaId}
    |
    v
snaadapter-a  ---- protocol ---->  Network Element
    |
    v
reply topic  -->  ESM maps result to DB state
```

**Modules (build order):** `esm-common` (DTOs + Kafka contracts) → `esm-server-dao` (JPA) → `esm-server-service` (`EsmApplication`, all business logic) → `esm-ws` (WAR: **all REST**, XML security, swagger) → `esm-nma` → docker/playbook. `esm-map` is an orphan; do not sell it as core.

**Pattern:** Command + task chain. Not Strategy.

---

### Q. Why Kafka southbound instead of REST to the NE?

**Answer**

Network elements are slow, protocol-diverse (CLI / NETCONF / vendor APIs), and often isolated. ESM should not embed every NE protocol.

The adapter (`snaadapter-a`) is the southbound boundary. Kafka gives:

- a **timeout** on `ReplyingKafkaTemplate` so HTTP threads do not hang forever
- buffering if the adapter is briefly down
- one pattern for many NE types
- a correlation id for request-reply

Cost: eventual view of NE state, duplicate-request risk, harder debug (app + Kafka + adapter + NE). That is the honest trade-off.

---

### Q. What if the Kafka reply never comes?

**Answer**

The template times out. I surface “NE not responding” to the operator. I do **not** mark the service Active just because we wrote intent in Oracle.

Then I check: broker up? adapter consumer lag on `ESM_ASYNCIF_REQUEST_{snaId}`? NE reachable? correlation id in adapter logs?

Retry: only if the southbound operation is **idempotent** (same service id / operation). Tight retry loops on timeout can DDoS the adapter. Bounded retry + alarm.

Multi-instance ESM needs both **correlation** and correct **reply routing**. `ReplyingKafkaTemplate` normally keeps the pending future in the sending JVM, so the reply must reach that instance (for example, an instance-specific reply topic/partition or appropriate assignment). A correlation id matches the response to the future; by itself it does not route the Kafka record to the originating JVM.

---

### Q. Service lifecycle / deploy / discovery / sync?

**Answer**

Create/deploy is not one method. `Service_TaskChainLocator` picks the chain for the current state and action. EPL config entry: `N_EPLServiceConfigLCImpl`. Deploy orchestration: `DeployService`. Each task: validate, persist, or southbound call.

ERP (G.8032): `ErpTaskChainLocator`. MPLS-TP: `N_MPLSTP_TunnelReqImpl`. OAM: `oam/impl`.

**Discovery** (`DiscoverNetwork`): learn what is on the network.

**Sync** (`PerformSyncJob`): reconcile NMS model vs NE. This is the safety net when southbound failed after we stored intent, or someone changed the NE out of band.

I pick **one flow I touched** and go deep. Listing every class without a story sounds fake.

---

### Q. Persistence, locking, list-page hotspot?

**Answer**

Oracle + JPA. Pool is **c3p0**, not Hikari. **No `@Version`** on entities — concurrent editors can overwrite. I would not invent optimistic locking in the interview; I can say it is a gap.

List pages join alarm severity (`ESM_ALR_SEVERITY_VIEW`). That join is the hotspot. I do not add more joins to `getAll`. I paginate, kill N+1, keep predicates sargable, read the plan.

No MapStruct. Mapping is explicit.

---

### Q. Security in ESM (do not recite a Boot 3 tutorial)?

**Answer**

Spring Security **Java config is commented out**. Enforcement is XML + `@Authorizer` RBAC + NAD/VPD data visibility.

Authn is at the WS-NOC edge. ESM still checks whether **this user** may write **this service** / see **this node**. “Internal network is trusted” is not the model.

---

### Q. How do you debug a failed create in production?

**Answer**

1. Request / service id in logs (MDC).
2. Did REST hit `esm-ws`?
3. Did the chain persist intent?
4. Was a Kafka request sent (`snaId`, correlation id)?
5. Adapter logs + NE response.
6. DB state vs NE (sync).

This is distributed debugging. Grepping only the WAR is not enough.

---

### Q. Scale / multi-instance / what you would improve?

**Answer**

Multiple ESM instances: stateless HTTP + DB as source of truth + Kafka for southbound. Watch connection pool vs Oracle sessions, Kafka reply timeouts, and list-query CPU.

Improvements I would actually propose (pick one): readiness that includes DB+Kafka; DLT for poison southbound payloads; optimistic lock on concurrent edit; stop growing the list query. I do not say “rewrite in Boot 3 + Kafka Streams next quarter” unless there is a funded plan.

---

## 2. Microservices

---

### Q. When do you split a monolith? When do you not?

**Answer**

Split when **team and release** boundaries match a **domain** boundary (orders vs billing), and independent deploy is worth the network and ops cost.

Do **not** split because “Netflix does it”, or because two classes feel large. A **modular monolith** (packages, modules, one DB, one deployable) is often the right step. Distributed transactions, dual writes, and on-call load are real.

**Interview line.** “I split for independent deploy and team ownership. I keep a module in-process when the data is one transaction.”

---

### Q. How do you decide service boundaries?

**Answer**

Bounded context: a service owns a **business capability** and the data for it. Look at change together / talk together. If every feature needs a join across two services, you split in the wrong place.

Red flags: shared database, chatty sync calls in one user click, circular dependencies, “common util service” that everyone writes into.

---

### Q. Database per service vs shared DB?

**Answer**

**Database per service** is the rule: each service owns its schema. Others go through API or events. That allows independent deploy and different storage.

**Shared DB** is faster to start and a nightmare to change (one migration breaks four teams). I allow a shared DB only as a stepping stone with a clear owner, not as the target.

**Reporting:** do not join five service DBs. Use events, CDC, or a read replica / warehouse.

---

### Q. API Gateway vs BFF vs service mesh?

**Answer**

**API Gateway:** one front door — TLS, auth, rate limit, routing, maybe WAF. Clients do not know 40 hostnames.

**BFF (Backend for Frontend):** a gateway **per client type** (web vs mobile) that aggregates. Stops mobile from making 15 calls.

**Service mesh (Istio/Linkerd):** sidecar handles mTLS, retries, telemetry **between** services. Heavy. I mention it if the company already has Kubernetes at scale. I do not add a mesh to a 4-service system.

**Discovery:** Eureka/Consul in old Spring Cloud. On Kubernetes, **DNS + Service + Ingress** often replaces Eureka. I say what the platform is, not a 2016 blog.

---

### Q. REST vs gRPC vs async messaging?

**Answer**

| | REST/JSON | gRPC | Kafka/events |
|---|---|---|---|
| Contract | OpenAPI | Protobuf | schema (Avro/JSON) |
| Style | request-response | request-response, streaming | fire-and-forget / log |
| Browser | easy | needs grpc-web | no |
| Coupling | time (caller waits) | time | time-decoupled |
| Use | public APIs, CRUD | internal low latency, typed | fan-out, buffering, audit |

ESM northbound is REST (UI). Southbound is Kafka because the NE is slow and isolated.

---

### Q. Sync vs async? When 202 Accepted?

**Answer**

Sync HTTP when the user needs the **answer now** and the work is short (validate, save, return id).

Async when work is long (NE config, video, report). Return **202** + resource id + status API (or websocket). Holding a Tomcat thread for 30s is how you melt a pool.

Cost of async: UI complexity, eventual consistency, duplicate messages. I do not make everything async to look modern.

---

### Q. API versioning? Backward compatible events?

**Answer**

URI `/v1/orders` is the most visible. Header versioning is cleaner, harder for clients. I version on **breaking** change, not every sprint.

Events: **add** fields; consumers ignore unknown fields (forward compatible). Do not remove or rename a field in the same version. A poison new schema must not block a partition — DLT.

Expand/contract for APIs and DB: add nullable column → deploy readers → deploy writers → drop old later.

---

### Q. CORS — and why it is not a 403 fix between services?

**Answer**

CORS is a **browser** rule. The server sends `Access-Control-Allow-Origin`. Postman and service-to-service calls are not blocked by CORS.

A 403 between two Java services is **authz**, not CORS. I do not “enable CORS” to fix Feign 403.

---

### Q. Spring Cloud today vs Kubernetes?

**Answer**

Old stack: Eureka, Ribbon, Zuul, Hystrix, Config Server.

Now: K8s DNS instead of Eureka, Ingress/Gateway instead of Zuul, Resilience4j instead of Hystrix, ConfigMaps/Secrets or a real config service.

I can still speak Spring Cloud Gateway + Resilience4j on K8s. I do not start a new project on Ribbon/Hystrix.

---

### Q. CQRS and event sourcing — do you use them?

**Answer**

**CQRS:** split write model and read model. Useful when reads are heavy and denormalized (list pages with alarm severity). The read model can be a table/view updated from events. Cost: two models, lag.

**Event sourcing:** store every event as the source of truth, rebuild state. Powerful for audit. Hard: versioning events, queries, team skill. I do not event-source a CRUD admin app.

**Interview line.** “I have used CQRS-lite: write to Oracle, project a list table. I have not run a full event-sourced system unless that is what they run.”

---

### Q. Strangler fig? How do you migrate a monolith?

**Answer**

Put a facade in front. New features go to a new service. Old routes stay on the monolith. Move one bounded context at a time. Sync data with events or dual write (dual write is temporary and dangerous — prefer outbox). Never “big bang rewrite”.

---

## 3. Distributed data and consistency

---

### Q. CAP theorem in one minute? ACID vs BASE?

**Answer**

In a partition, you choose **Consistency** or **Availability** (CP vs AP). You always want Partition tolerance on a real network.

- CP example: majority-quorum DB (ZooKeeper, etcd). Refuse writes if you cannot get quorum.
- AP example: DNS, some Dynamo-style stores. Serve stale data rather than error.

This is not “Cassandra is AP, Oracle is CA”. Oracle in one AZ is not a distributed CAP story. I use CAP when we have **multi-node** systems and a **network split**.

**ACID:** one database transaction. **BASE:** basically available, soft state, eventual consistency — typical across microservices.

---

### Q. Why not 2PC / XA across microservices?

**Answer**

2PC: prepare then commit. Locks resources, coordinator can crash **in-doubt**, availability suffers, ops pain. Almost nobody wants XA between order and payment services.

I keep **one DB transaction per service** and a **saga** or outbox for the rest.

---

### Q. Saga: orchestration vs choreography?

**Answer**

A saga is a sequence of **local transactions** plus **compensating** actions if a later step fails.

**Orchestration:** one coordinator calls step 1, 2, 3. Easy to see in logs. Coordinator can become a god class.

**Choreography:** each service listens to events and emits the next. Loose coupling. Harder to see the whole flow; risk of cycles.

Example: create order → reserve inventory → charge. If charge fails: release inventory, mark order failed.

Compensations are **business undo**, not DB rollback. You cannot un-send an email; you send “ignore previous”.

---

### Q. Dual-write problem and transactional outbox? CDC?

**Answer**

**Dual write:** `INSERT order` then `kafka.send`. Process can die between them. DB committed, event lost — or event sent, DB rolled back.

**Outbox:** in the **same** DB transaction, write the business row **and** an `outbox` row. A publisher polls it, or Debezium reads the committed outbox row from the database log and publishes it. At-least-once to Kafka; consumer must be idempotent.

**Direct CDC:** capture changes from business tables without an outbox row, but that couples events to table shape and still needs a stable event contract. **Outbox + Debezium still requires the application to write the outbox row.**

**Interview line.** “I never ‘fire event after commit’ without an outbox or CDC. That is how we lose messages.”

---

### Q. Idempotency — how do you implement it?

**Answer**

Networks retry. Users double-click. Kafka at-least-once **will** redeliver.

For HTTP POST: client sends `Idempotency-Key` (UUID). Server stores `key → response` (status + body hash) with a TTL. Same key + same body → replay response. Same key + different body → 409.

For Kafka: unique `eventId` or `(topic, partition, offset)` processed table, or unique business key (`service_id` + `operation`). Unique constraint in DB is the real exactly-once **effect**.

---

### Q. Distributed lock? When not to use one?

**Answer**

Use a lock when only one instance may run a job (scheduler) or you must avoid double-provisioning **and** you cannot make the operation idempotent.

Implementations: Redis `SET key NX PX ttl` (add fencing token / Redlock debate — I prefer DB unique constraint or Redis with TTL + token), ZooKeeper/etcd for strong leadership.

**Danger:** lock expires while work still runs → two workers. **Fencing token:** every write carries a lock version; storage rejects old tokens.

**Better than a lock:** unique constraint + idempotent write. Locks are for “I cannot make it idempotent”.

---

### Q. Distributed cache? Cache-aside vs write-through? Stampede?

**Answer**

**Cache-aside:** read Redis; miss → DB → set Redis. Write: DB then **delete** cache (or update). TTL as a safety net.

**Write-through:** write cache and DB together — more consistent, more latency.

**Stampede:** many pods miss together. Fix: lock per key, or probabilistic early refresh, or serve stale.

Sessions: Spring Session Redis. If Redis dies, users re-login. I say that out loud.

Invalidation is the hard part. I cache **read-mostly** data with a clear key design (`user:123:profile`), not “cache the whole world”.

---

### Q. Unique IDs in a distributed system?

**Answer**

DB sequence: simple, one DB as bottleneck, not global across shards.

UUID v4: no coordination, large, not sortable (index fragmentation). UUID v7 / ULID: time-ordered.

**Snowflake:** 64-bit: timestamp + worker id + sequence. Sortable, compact. Need unique worker ids (need care on K8s). Clock skew is the follow-up — don’t go backwards.

I pick UUID for public ids, Snowflake/sequence for internal high-volume numeric ids.

---

## 4. Resilience

---

### Q. Timeout, retry, circuit breaker, bulkhead, rate limit — how they fit?

**Answer**

- **Timeout:** every remote call. Without it, threads wait forever. Timeout must be **less** than the caller’s budget (gateway 3s → service 2s → DB 1s).
- **Retry:** only **idempotent** calls, exponential backoff + **jitter**. Retrying POST create without a key creates duplicates. Retry storm makes an outage worse.
- **Circuit breaker:** after N failures, **open** — fail fast, do not call. After a sleep, **half-open** — probe. Resilience4j is the Spring standard. Hystrix is retired.
- **Bulkhead:** separate thread pool / connection pool per dependency. One slow client cannot take all Tomcat threads.
- **Rate limit:** protect **you** from too many incoming calls (and protect others when you are the client).

I wrap **remote** calls. I do not put a circuit breaker on an in-memory map.

---

### Q. How does a circuit breaker work internally?

**Answer**

States: **Closed** (calls pass, failures counted) → **Open** (calls fail immediately, timer) → **Half-open** (allow a few calls). Success → closed. Failure → open again.

Count failures in a sliding window (count or time). I configure fallback: cached value, default, or fail with 503.

**Follow-up.** Fallback that calls the same down service is useless. Fallback that returns stale cache is a product decision.

---

### Q. Backpressure? What is a retry storm / thundering herd?

**Answer**

**Backpressure:** producer slows when consumer cannot keep up. Kafka: consumer lag + pause consumption. HTTP: 429. Thread pool: bounded queue + CallerRuns / reject.

**Retry storm:** all clients retry at once when a service comes back. Jittered backoff + circuit breaker.

**Thundering herd:** many waiters wake together (cache expire, lock release). Randomize TTL, single-flight the refresh.

---

### Q. Graceful shutdown? Liveness vs readiness?

**Answer**

**Liveness:** process is deadlocked / wedged → K8s **restarts** it. Should be cheap (ping). False fail = restart loop.

**Readiness:** can I take traffic? DB up, Kafka up, caches warmed. Fail readiness → removed from load balancer, **not** necessarily restarted.

**Graceful shutdown:** stop taking new requests (fail readiness), finish in-flight, commit Kafka offsets, close pool. `server.shutdown=graceful` in Boot. SIGTERM handling. If we kill before offset commit, **duplicate processing** — consumers must be idempotent.

---

## 5. Spring Security

Round 1 covered the filter chain, 401/403, CSRF, BCrypt, `@PreAuthorize`. Round 2 they want **JWT resource server, OAuth2 flows, object-level security, and how ESM actually differs**.

---

### Q. Draw the Spring Security filter chain (Security 6).

**Answer**

Servlet container → `DelegatingFilterProxy` (`springSecurityFilterChain`) → `FilterChainProxy` picks a `SecurityFilterChain` by matcher.

Important filters in order (names you should say):

1. `DisableEncodeUrlFilter` / `WebAsyncManagerIntegrationFilter`
2. `SecurityContextHolderFilter` (loads context; old: `SecurityContextPersistenceFilter`)
3. `HeaderWriterFilter`
4. `CorsFilter` (if enabled)
5. `CsrfFilter` (if enabled)
6. `LogoutFilter`
7. Authentication: `UsernamePasswordAuthenticationFilter` or **Bearer token / JWT** (`BearerTokenAuthenticationFilter`)
8. `RequestCacheAwareFilter`, `SecurityContextHolderAwareRequestFilter`
9. `SessionManagementFilter`
10. `AnonymousAuthenticationFilter`
11. `ExceptionTranslationFilter` — 401 vs 403
12. `AuthorizationFilter` (old: `FilterSecurityInterceptor`)

I do not memorize every class. I **do** remember: context → authenticate → exception translation → authorize.

Boot 3: `SecurityFilterChain` bean. No `WebSecurityConfigurerAdapter`.

---

### Q. How do you configure a JWT resource server in Spring Boot 3?

**Answer**

```java
@Bean
SecurityFilterChain api(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .sessionManagement(s -> s.sessionCreationPolicy(STATELESS))
        .authorizeHttpRequests(a -> a
            .requestMatchers("/actuator/health").permitAll()
            .requestMatchers("/esmService/**").hasAuthority("ESM_WRITE")
            .anyRequest().authenticated())
        .oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults()));
    return http.build();
}
```

`spring.security.oauth2.resourceserver.jwt.issuer-uri=...` → Boot fetches JWKS, validates signature, `iss`, `exp`.

I map claims to authorities with a `JwtAuthenticationConverter` (`realm_access.roles` in Keycloak, `scope` in others). `hasRole` vs `hasAuthority` still bites here if I forget `ROLE_` prefix.

---

### Q. OAuth2 / OIDC flows you must name?

**Answer**

- **Authorization Code + PKCE:** browser / SPA / mobile login. User redirects to IdP, comes back with code, app exchanges code for tokens. PKCE stops the code intercept attack.
- **Client credentials:** service-to-service. No user. `client_id` + secret (or JWT assertion).
- **Refresh token:** get new access token without login. Store refresh token carefully (httpOnly cookie or auth server). Rotate refresh tokens.
- **Implicit / password grant:** **deprecated**. I do not propose them.

**OIDC** is OAuth2 + identity (`id_token` with user claims).

Gateway: validate user JWT, optionally mint an **internal** token with `aud` = downstream service. Each service still checks **authorization**.

---

### Q. `@PreAuthorize` vs URL matchers vs a custom `AccessDecision` / `AuthorizationManager`?

**Answer**

URL matchers: coarse (path + HTTP method).

`@PreAuthorize`: per method, SpEL, can use parameters (`#serviceId`).

Object-level (IDOR): `hasPermission(#id, 'Service', 'WRITE')` + `PermissionEvaluator`, or a service that loads the entity and checks tenant/NAD.

ESM: `@Authorizer` + VPD/NAD is **data visibility**, not just a role string. That is more than `hasRole('ADMIN')`.

Method security is AOP. Self-call skips it. Same as transactions.

---

### Q. Multiple `SecurityFilterChain`s? Actuator vs API vs UI?

**Answer**

Yes. `@Order(1)` chain for `/actuator/**` (maybe permit health, protect heapdump). `@Order(2)` for `/oms1350/**`. First matching chain wins. I do not put `anyRequest()` on the first chain unless that chain should own the whole app.

---

### Q. JWT revoke, clock skew, algorithm confusion?

**Answer**

Access tokens are hard to revoke. Patterns: short TTL (5–15 min), refresh rotation, denylist of `jti` in Redis until expiry, session store at IdP.

**Clock skew:** allow 30–60s leeway on `exp`/`nbf`.

**Algorithm:** never trust header `alg=none`. Use RS256/ES256 with JWKS. Do not mix HMAC (shared secret) with a public key by accident.

---

### Q. CSRF with cookies vs SPA? SameSite?

**Answer**

If access token is in **localStorage**, XSS steals it; CSRF is weaker (no cookie). If token is in a **cookie**, CSRF is real — use CSRF token or SameSite=Strict/Lax + custom header.

I prefer: BFF + httpOnly SameSite cookie, or Authorization header from a SPA that got the token via PKCE.

---

### Q. mTLS, API keys, and ESM (do not mix stories)

**Answer**

**mTLS:** mesh or API-to-API. Cert rotation is the cost.

**API keys:** partners, with rotation and rate limit.

**ESM:** Security Java config **commented out**. XML + `@Authorizer` RBAC + NAD/VPD. I never say “we use oauth2ResourceServer in ESM” unless that is actually true.

---

### Q. OWASP for APIs (Round 2 version)?

**Answer**

IDOR (check object + tenant, not only JWT `sub`). Injection (parameterized SQL — Hibernate named params still fail if we concatenate JPQL). Broken auth (weak JWT validation). Mass assignment (`@JsonIgnore` on role fields). SSRF. Excessive data in JWT. Log PII. Dependency CVEs.

---

## 6. Hibernate and JPA (deep)

Round 1: states, persist/merge, L1, N+1, OSIV, owning side, generators, `@Version`. Round 2: flush, locking collisions, batch, cache concurrency, Criteria, Hibernate internals they probe.

---

### Q. Persistence context vs JDBC connection? Transaction boundary?

**Answer**

One Spring `@Transactional` on the service binds a persistence context to the thread and defines the transaction boundary. Hibernate may acquire the JDBC connection **lazily**, when SQL is first needed. It flushes before commit, commits, then closes/unbinds the context.

Propagation `REQUIRES_NEW`: new connection, new context. The outer persistent entities are **not** in the inner context (detached from inner’s point of view).

`readOnly=true`: Hibernate can skip dirty checks/flush. I still do not mutate entities in read-only TX.

---

### Q. `OptimisticLockException` in production — what do you do?

**Answer**

User A and B edited the same row. B’s commit loses. API: 409 Conflict, “reload and retry”. Do not retry blindly in a loop without the user.

For automatic retries (inventory decrement): retry TX a few times on `OptimisticLockException` / SQL deadlock (`ORA-00060`). Idempotent operation required.

Without `@Version`, last write wins — ESM today. I say that.

---

### Q. Pessimistic lock timeout? Deadlock with JPA?

**Answer**

`PESSIMISTIC_WRITE` + timeout (`jakarta.persistence.lock.timeout`). Oracle: `SELECT FOR UPDATE`. Always lock rows in **the same order** (same as Java deadlock). Keep TX short. Never wait on HTTP/Kafka **inside** a lock.

---

### Q. Batch inserts, `order_inserts`, `jdbc.batch_size`, IDENTITY problem?

**Answer**

`hibernate.jdbc.batch_size=50`, `order_inserts=true`, `order_updates=true`. Flush/clear every N entities so L1 does not explode.

IDENTITY: no batch (needs id from DB). SEQUENCE + `allocationSize` aligned with DB: batch works.

`@GeneratedValue` on UUID in app: no DB id fetch, batch works.

---

### Q. Second-level cache concurrency strategies?

**Answer**

`READ_ONLY` — never changes. Best.

`NONSTRICT_READ_WRITE` — rare writes, can be slightly stale.

`READ_WRITE` — transactional, uses soft locks.

`TRANSACTIONAL` — JTA, rare.

If I cannot explain invalidation, I do not turn on L2 for that entity. Query cache + L2 without careful regions is a footgun.

---

### Q. Hibernate proxy: `Hibernate.unproxy`, `instanceof` trap?

**Answer**

Lazy `@ManyToOne` is commonly a proxy subclass. It normally passes `instanceof` for its declared entity class, but exact `getClass()` equality and polymorphic subtype checks can surprise you. Use `Hibernate.unproxy(entity)` or `Hibernate.getClass(entity)` for proxy-safe type handling. Do not base HashMap keys on unstable proxy/runtime-class behavior.

---

### Q. `@Modifying` JPQL vs dirty persistence context?

**Answer**

Bulk `UPDATE/DELETE` JPQL goes to DB and **skips** L1. Memory entities are stale. Use `clearAutomatically = true` / `flushAutomatically = true` or `entityManager.clear()`.

This is a classic “I updated status in bulk but the API still returns old status in the same request” bug.

---

### Q. Criteria API / Specification vs Querydsl?

**Answer**

Dynamic ESM-style filters: Spring Data `JpaSpecificationExecutor` + Specifications. Criteria is verbose but in the JPA spec. Querydsl/jOOQ if the team already has it. I do not string-concatenate JPQL with user input.

---

### Q. Multi-bag fetch exception? How do you load two collections?

**Answer**

Hibernate cannot `join fetch` two bags (`List`) in one query (`MultipleBagFetchException`). Fix: `Set` instead of `List` (careful with extra-lazy), or two queries, or `@Fetch(SUBSELECT)` / `@BatchSize`.

---

### Q. Hibernate `StatelessSession`? When JDBC?

**Answer**

StatelessSession: no L1, no dirty checking — close to JDBC for ETL. JDBC/`JdbcTemplate` for reporting SQL and bulk. Keep Hibernate for transactional domain graphs.

---

### Q. Connection provider: Hikari vs c3p0 (ESM)?

**Answer**

Boot default **Hikari**. ESM uses **c3p0**. Same interview points: `maxPoolSize`, `maxLifetime` below DB idle timeout, leak detection. Exhaustion: slow queries + too many pods × pool > Oracle sessions.

---

## 7. Kafka

---

### Q. Topic, partition, offset, replica, ISR, controller?

**Answer**

A **topic** is a named append-only log, split into **partitions** for parallelism and scale.

Each message in a partition has a monotonic **offset**. Consumers store committed offsets.

**Replication:** each partition has a leader and followers. **ISR** (in-sync replicas) are sufficiently caught up. With `acks=all`, the leader waits according to the topic/broker durability policy; `min.insync.replicas` determines how many in-sync replicas must be available for the write to succeed. A slow follower can drop out of ISR.

**Controller:** in modern **KRaft**, a Raft quorum of controller nodes manages metadata and leader elections, with one active controller at a time. Older Kafka used ZooKeeper plus a broker controller.

**Ordering:** **per partition only**, not per topic.

---

### Q. Why does the message key matter?

**Answer**

The key is hashed to a partition. Same key → same partition → **order for that entity**.

I key by `serviceId` / `orderId`. Random key = max spread, **no** entity order. Two updates for the same service can be processed out of order.

Hot key (one celebrity id) overloads one partition. Then you need better key design or local fan-out.

---

### Q. Producer: acks, retries, idempotence, batching?

**Answer**

- `acks=0` fire and forget — can lose.
- `acks=1` leader only — lose if leader dies before replicate.
- `acks=all` (`-1`) plus an appropriate `min.insync.replicas` is the safest common setting; it does not mean every replica must acknowledge.

**Idempotent producer** (`enable.idempotence=true`): broker stores producer id + sequence. Retries do not create **duplicates in the log**. This is not end-to-end exactly-once.

**Retries + `max.in.flight`:** without idempotence, retries can reorder. Idempotence allows more in-flight safely (up to 5).

**Batching:** `batch.size` + `linger.ms` — throughput vs latency. Compression (`lz4`/`zstd`) for large JSON.

I always set **delivery timeout** and I do not retry forever in app code on top of producer retries without thinking.

---

### Q. Consumer group, commit, rebalance — the 5–8 YOE version?

**Answer**

A **group** shares the topic. Each partition is assigned to **at most one** consumer in the group. More consumers than partitions = idle members.

**Commit offset:** “I have processed up to here.”

- Auto commit: easy, can **lose** (commit before process) or **duplicate** (process before commit, then crash).
- Manual commit **after** successful process: at-least-once. Duplicates on crash. **Idempotent handler.**

**Rebalance:** member join/leave/fail → partitions revoked and reassigned. Processing **pauses**. If processing takes longer than `max.poll.interval.ms`, the member is kicked → more rebalances (a loop). Fix: process less per poll, faster handler, cooperative sticky assignor, static membership (`group.instance.id`) to avoid unnecessary revoke.

`session.timeout.ms` / heartbeat: “I am alive”. Different from poll interval (I am still processing).

---

### Q. at-most-once vs at-least-once vs exactly-once?

**Answer**

- **at-most-once:** commit before process (or fire-and-forget produce). Can lose.
- **at-least-once:** process then commit. Can duplicate. **This is what I design for.**
- **exactly-once (EOS):** idempotent producer + transactional produce + `read_committed` consumers. “Effectively once” still needs a **unique business constraint** in the DB. Kafka EOS does not magically make your HTTP call to Twilio exactly-once.

**Interview line.** “I enable idempotent producer. Consumers are idempotent. I do not promise Kafka EOS unless we actually turned on transactions and tested them.”

---

### Q. Consumer lag? How do you scale consumers?

**Answer**

Lag = end offset − committed. High lag = too slow or stuck partition (poison message).

Scale: **more partitions** then more consumers in the group. You cannot have more active consumers than partitions.

Speed up: batch writes, skip work, faster DB, don’t do HTTP inside the listener without a timeout. Separate slow path to another topic. Watch GC and connection pools.

**Poison pill:** bad JSON blocks a partition if you retry forever. Bounded retry + **DLT** + alert on DLT depth.

---

### Q. Spring Kafka error handling: `DefaultErrorHandler`, retry topic, DLT, ack mode?

**Answer**

Use `DefaultErrorHandler` with bounded backoff for short transient failures and a `DeadLetterPublishingRecoverer` for exhausted/non-retryable records. For long delays, retry topics avoid blocking the original partition.

Manual/record acknowledgements should happen only after the business effect commits. With transactions, coordinate the container transaction semantics carefully; never acknowledge first and then update the DB. DLT records need original topic/partition/offset, exception, event id, monitoring, and a controlled replay process.

---

### Q. Compacted topic vs delete retention?

**Answer**

**Delete:** keep data for time/size (`retention.ms`). Event stream, audit.

**Compact:** for the same **key**, keep the **latest** value. Changelog / KTable / snapshot of entity state. Tombstone (`null` value) deletes the key.

I do not compact an event stream I need history for. I do not use a compacted topic as the only source of truth for operators who need ad-hoc SQL — that is still a DB.

---

### Q. Kafka vs SQS vs Rabbit vs Pulsar (short)?

**Answer**

**Kafka:** replayable log, consumer groups, high throughput, ordering per partition, you operate retention.

**SQS:** consume and delete, easy on AWS, no long replay, competing consumers. SNS+SQS for fan-out.

**RabbitMQ:** smart broker, exchanges, routing keys, not a long log.

I pick Kafka when multiple independent consumers need the **same** events and replay. I pick SQS when the team has no Kafka ops and the job is simple.

---

### Q. Schema registry? Compatibility?

**Answer**

Producers write Avro/JSON Schema/Protobuf with an id. Consumers decode with the registry.

Compatibility: **BACKWARD** (new reader, old data) is the usual produce default. **FORWARD** (old reader, new data). **FULL** both.

I add optional fields. I do not reuse field ids for a new meaning.

---

### Q. Kafka request-reply (ESM)? Kafka transactions consume-process-produce?

**Answer**

Request carries `correlationId` + reply topic. `ReplyingKafkaTemplate` waits with timeout. Adapter replies with the same id.

**Transactional consume-process-produce:** Kafka can atomically publish output records and commit consumed offsets in one **Kafka** transaction. A normal Oracle/PostgreSQL write is **not** part of that Kafka transaction. For DB + Kafka consistency, use an outbox/CDC (or XA only when the organization deliberately accepts that complexity).

---

### Q. How many partitions? Can I change later?

**Answer**

Start from target throughput and consumer parallelism. Too few: cannot scale consumers. Too many: more files, slower rebalance, more memory on brokers.

**You can increase partitions. You cannot decrease.** Adding partitions **breaks** key mapping for old keys (hash % n changes). Plan keys and partition count early, or use a partitioner that stays compatible.

---

## 8. AWS

---

### Q. How would you run a Spring Boot service on AWS?

**Answer**

Docker image → **ECR**. Run **ECS Fargate** (or EKS if the org is k8s). **ALB** in front, health check on `/actuator/health` (readiness). Private subnets. Config from **SSM / Secrets Manager**, not baked in. Logs **CloudWatch**. Autoscaling on CPU or ALB RPS. **RDS** in private subnet, Multi-AZ. Outbound via NAT if needed.

That is the default story. If they are EC2+Ansible, I map the same ideas onto VMs.

---

### Q. VPC, subnet, security group vs NACL?

**Answer**

**VPC:** your isolated network. Public subnet: NAT/ALB/bastion. Private: apps + RDS. **No public IP on the database.**

**Security group:** stateful firewall on ENI (instance/task). Allow 8080 from ALB SG only.

**NACL:** stateless, subnet level, less used for app rules.

I do not open `0.0.0.0/0` to RDS.

---

### Q. ALB vs NLB vs API Gateway?

**Answer**

**ALB:** L7 HTTP, path routing, host-based, WAF, TLS terminate. Default for Spring Boot.

**NLB:** L4 TCP/UDP, very high performance, static IP. gRPC sometimes.

**API Gateway:** managed API, auth, throttling, usage plans, good with Lambda. Cost per million requests. I do not put API Gateway in front of one private ECS service unless we need those features.

---

### Q. EC2 vs ECS vs EKS vs Lambda?

**Answer**

- **EC2:** you patch VMs. Control, more ops.
- **ECS Fargate:** containers, no nodes. Best default for Spring Boot microservices on AWS.
- **EKS:** Kubernetes. Choose when k8s is the org standard.
- **Lambda:** short event work, pay per invoke, cold starts. Awkward for large Boot unless SnapStart/native. Good for S3 events, cron, glue.

---

### Q. S3 important facts?

**Answer**

Object store, 11 nines durability, not a POSIX disk. S3 provides strong consistency for successful writes, overwrites, deletes, GET/HEAD, and LIST operations. Versioning + lifecycle (hot → IA → Glacier). Block public access. Pre-signed URLs for downloads. SSE-S3 or SSE-KMS. Do not use S3 as a low-latency key-value store — that is Redis/Dynamo.

---

### Q. RDS vs Aurora vs DynamoDB vs ElastiCache?

**Answer**

**RDS:** managed Postgres/MySQL/Oracle. Joins, transactions, your current model. **Multi-AZ:** sync standby, HA in one region. **Read replica:** async, lag, scale reads.

**Aurora:** RDS-compatible, faster failover, storage-separated. Cost vs need.

**DynamoDB:** key-value, huge scale, **design access patterns first**. PK + SK. GSI/LSI. Eventually consistent reads by default (can request strong). Not for ad-hoc joins.

**ElastiCache Redis:** cache, session, rate limit, distributed lock. Persistence optional. Not the system of record for inventory.

---

### Q. SQS vs SNS vs EventBridge vs MSK?

**Answer**

**SNS:** pub/sub fan-out (one message, many subscribers).

**SQS:** queue, competing consumers, visibility timeout, **DLQ**. SNS→SQS is classic.

**EventBridge:** routing by event pattern, SaaS integrations, bus.

**MSK:** Kafka on AWS when you need log replay, partitions, many consumer groups.

SQS cannot replay after delete. Kafka can. Visibility timeout too short → duplicate delivery (idempotency again).

---

### Q. IAM least privilege? KMS?

**Answer**

ECS **task role** for the app (S3 get this prefix, SQS consume this queue). Humans: SSO, not long-lived keys. No `s3:*` on `*` in prod.

**KMS:** encrypt secrets, S3, RDS. Separate keys per env. Rotation. App decrypts via IAM, not by embedding a key.

---

### Q. CloudWatch vs X-Ray? Auto Scaling?

**Answer**

CloudWatch: metrics, logs, alarms. Alarm on **5xx, p99, lag**, not only CPU.

X-Ray / OTel: traces across ALB → service → Dynamo.

Autoscaling: CPU is a weak proxy. Better: ALB request count, SQS age, Kafka lag custom metric. Scale **in** slowly so you do not flap.

---

### Q. Multi-AZ vs multi-region?

**Answer**

Multi-AZ: HA in one region (RDS failover, ALB across AZs). **This is the default I ship.**

Multi-region: DR or latency. RPO/RTO. Active-active needs conflict resolution. I do not promise active-active unless they already have a story.

---

## 9. CI/CD, Docker, Kubernetes

---

### Q. Draw your pipeline.

**Answer**

1. PR: compile, unit tests, linters
2. Merge: full tests, **Sonar** quality gate, **JaCoCo** on new code
3. Build image tagged with **git SHA** (never only `latest`)
4. Push ECR/Artifactory
5. Deploy dev → QA → prod (approval on prod)
6. Smoke / health
7. Rollback = redeploy previous SHA

Jenkins (`Jenkinsfile`), GitLab CI, GitHub Actions — I care about **pipeline as code**, secrets, and promotion, not the logo. Older TCS-style: Jenkins + OpenShift + UDeploy. Same stages.

---

### Q. Rolling vs blue-green vs canary?

**Answer**

- **Rolling:** replace pods in batches. Two versions run together. Schema must work with **both**.
- **Blue-green:** two environments, flip the balancer. Fast rollback. 2× cost during cutover.
- **Canary:** 5% traffic, watch errors, then 25%, then 100%. Best for user-facing risk. Needs good metrics.

**Rollback fails** if you dropped a column the old app still reads. **Expand/contract:** add column → deploy app → later drop unused column.

---

### Q. Docker image for Spring Boot?

**Answer**

Multi-stage build. Final image: JRE not JDK, non-root user, `java -jar` or exploded layers (Buildpacks/Paketo). No secrets in `ENV` in the Dockerfile. Orchestrator probe: `/actuator/health/readiness` vs liveness.

Layer dependencies separately so code changes do not rebuild all jars — or use Buildpacks and don’t fuss.

---

### Q. Kubernetes objects you should name?

**Answer**

**Pod** (one or more containers), **Deployment** (replica, rolling), **Service** (stable DNS), **Ingress** (HTTP entry), **ConfigMap** / **Secret**, **HPA** (autoscaling), **PDB** (don’t kill all pods at once).

Probes: liveness, readiness, startup (slow Boot). Resources: request + limit. Without limits, one leak kills the node.

I do not claim I am a full-time SRE. I can read a Deployment spec and explain why a pod is not Ready.

---

### Q. Feature flags vs long-lived git branches?

**Answer**

Trunk-based + short PRs + flags for unfinished work. Flags need an **expiry**. Long-lived branches are merge hell.

---

### Q. 12-factor (the ones they care about)?

**Answer**

Config in the environment, not baked in. Backing services as attached resources (URL for DB/Kafka). Logs to stdout (platform ships them). Disposability (fast start, graceful stop). Dev/prod parity. One codebase, many deploys.

---

## 10. Observability and production

---

### Q. Logs, metrics, traces — how do you use them together?

**Answer**

**Metrics** first: RED — Rate, Errors, Duration (p50/p99). Kafka **lag**. Pool wait. If p99 jumps, I still don’t know **which** dependency.

**Trace** (OpenTelemetry / Micrometer tracing): one `traceId` across HTTP and Kafka. Find the slow span.

**Logs:** JSON + `traceId` + `serviceId`. Not 10 GB of debug in prod. No passwords.

I never debug prod with only logs if I have metrics and traces. I also never “add more logs” as the only fix — I add a **metric and an alarm**.

---

### Q. SLO vs SLA vs SLI? Error budget?

**Answer**

**SLI:** the number (availability, p99). **SLO:** our target (99.9%). **SLA:** contract with the customer (legal).

Error budget: if we burn it, we slow features and fix reliability. I mention this in manager-technical hybrids; in Round 2 I at least know the words.

---

### Q. p99 latency jumped. How do you debug?

**Answer** (speak in this order)

1. Is it one instance or all? Deploy related?
2. Metrics: CPU, GC pause, heap, **DB pool wait**, Kafka lag, error rate.
3. Trace a slow request: which span?
4. If SQL: EXPLAIN, lock waits, sudden seq scan.
5. If downstream: their p99, circuit open, retries amplifying.
6. Thread dump if threads are stuck (`jstack`).
7. Mitigate: rollback, flag off, scale, shed load (429). Then RCA.

---

### Q. Connection pool exhaustion? Thread pool exhaustion?

**Answer**

Symptoms: requests hang, then timeout. Threads in `waiting for connection`. Cause: slow queries, leak (connection not closed), pool too small, or too many pods × pool > DB `max_sessions`.

Fix: try-with-resources, timeout on queries, size pool from **DB capacity / number of tasks**, leak detection. ESM: c3p0; Boot default: Hikari.

Tomcat threads all waiting on Kafka reply without timeout = same class of bug.

---

### Q. Thread dump vs heap dump vs CPU/allocation profiler?

**Answer**

- **Thread dump (`jstack`, `jcmd Thread.print`):** deadlock, blocked/waiting threads, hot repeated stacks, pool starvation.
- **Heap dump:** what objects retain memory; analyze dominator tree in MAT.
- **GC logs/JFR:** pause, allocation rate, promotion, safepoints.
- **async-profiler/JFR CPU profile:** methods consuming CPU; allocation profile shows object churn.

Do not take a huge live heap dump casually on a memory-stressed production JVM; it can pause the process and needs disk.

---

### Q. Virtual threads in a Spring backend—when do they help and what do they not solve?

**Answer**

They help high-concurrency **blocking I/O** code use far fewer platform threads. They do not increase DB connections, Kafka partitions, CPU, or downstream capacity. Keep timeouts and bulkheads; otherwise 100,000 cheap callers can overload a 50-connection DB.

Avoid thread-affinity assumptions and excessive ThreadLocal state. On Java 21, long blocking work in certain `synchronized`/native regions can pin carriers; behavior improves in newer JDKs, so state the version and measure with JFR.

---

### Q. Prometheus/Micrometer cardinality trap?

**Answer**

Never use `userId`, `serviceId`, URL with raw ids, or exception message as a metric label—cardinality explodes memory/cost. Use bounded labels (`method`, normalized route, status class, dependency). Put individual ids in logs/traces, not metrics.

---

## 11. System design

Speak 12–15 minutes: **requirements → API → data → scale numbers → bottlenecks → failure**. Ask QPS, read/write ratio, and latency target if they did not give them.

---

### Design 1. URL shortener

**Requirements.** Write long URL → short code. Read: 302/301. Optional expiry, custom alias, analytics. ~100M writes/month, 1000:1 reads.

**API.** `POST /urls` `{longUrl}` → `{code}`. `GET /{code}` redirect.

**Data.** `code` PK (7+ char base62), `long_url`, `created_at`, `expires_at`. Redis `code → url`. Unique on code.

**IDs.** Random 7 char + retry on unique violation, or Snowflake encoded base62, or Redis `INCR`. Hash collisions: retry.

**Scale.** Stateless redirect nodes + ALB. Cache hot codes. Do not put analytics on the redirect path — enqueue Kafka. 301 caches in browsers (less analytics); 302 if you need every click.

**Bottleneck.** Viral link stampede: lock or serve stale. DB unique. Do not start with Cassandra unless they push huge scale.

---

### Design 2. Rate limiter (API Gateway)

**Requirements.** 100 req/min per API key, works across N gateway pods.

**Algorithms.** Token bucket (burst + refill) — usual choice. Sliding window log — accurate, more memory. Fixed window — cheap, burst at edges.

**Distributed.** Redis + Lua: atomic get-and-decrement tokens. Fail **closed** for auth; fail **open** for a best-effort public page — confirm with interviewer.

Return **429** + `Retry-After`. Different limits per plan. Local memory limiter is wrong with 10 pods.

---

### Design 3. Notification system

**Requirements.** Other services publish `NotificationRequested`. Email, SMS, push. At-least-once. Peak 50k/min.

**Design.** Kafka topic `notifications` keyed by `userId`. **Separate consumer groups** per channel (email / sms / push) so Twilio down does not stop email. Workers call SES/Twilio/FCM with timeout + retry. Table unique on `notification_id`. DLT. Template + user prefs (DND) in another service.

PII: do not log full phone numbers. Outbox from the source service so “order created” always emits the event.

---

### Design 4. Distributed cache / session

**Requirements.** 20 app nodes, no sticky session required.

**Session:** Redis `session:{id}` + TTL. Spring Session.

**Data cache:** cache-aside, TTL, delete on write. Stampede control. `allkeys-lru` for cache; **not** for sessions (TTL only).

Failure: Redis down → re-login vs slower DB. Separate the two so a cache flush does not log everyone out if you used different DBs/prefixes.

---

### Design 5. Unique ID generator

**Requirements.** 10k ids/sec, roughly time-ordered, fit in a long or 64-bit.

**Options.** DB sequence (simple). UUID (easy, not compact). Snowflake: worker id from config/IP, handle clock rollback by refusing or waiting. Range allocator: each app gets a block of 1000 ids from a central service (one extra hop, simple).

---

### Design 6. Service provisioning / order orchestration (maps to ESM)

**Requirements.** User submits “create connectivity” with two endpoints. Validate inventory, reserve, configure devices, lifecycle Designed → Deployed → Active. Devices slow/unreliable. Multi-instance app.

```text
Client → API (authz) → 202 + id
              → DB state machine (optimistic lock)
              → Outbox / Kafka request to Device Adapter
              → Adapter → devices
              → Reply → update state
```

- HTTP does **not** wait for the NE.
- State in DB; allowed transitions explicit.
- Idempotency key on create.
- Reservation in the **same TX** as the order row (do not overbook ports).
- Timeout → failed/retry last step; compensate (release ports).
- List screens: denormalized status + alarms; that join is the hotspot.

If they ask about **your** project, switch to the real ESM names in section 1. Do not invent REST paths.

---

### Design 7. Tiny “search / autocomplete” (optional extra)

**Prefix search.** Trie in memory for small dictionaries. For large: Elasticsearch / OpenSearch, prefix fields, cache top queries in Redis. Write path: Kafka → indexer (async, lag ok). Do not join autocomplete to the primary OLTP on every keystroke.

---

## 12. Design / coding problems (try first)

Round 2 sometimes asks you to **sketch code** for a limiter, idempotency filter, or in-memory cache. Solutions hidden.

---

### D1. Token bucket rate limiter (single process)

**Hint.** Tokens refill with time. On each request: add refill, cap at burst, consume 1 if tokens ≥ 1.

<details>
<summary>Click to see solution</summary>

```java
public final class TokenBucket {
    private final double ratePerSec;
    private final double burst;
    private double tokens;
    private long lastNanos;

    public TokenBucket(double ratePerSec, double burst) {
        this.ratePerSec = ratePerSec;
        this.burst = burst;
        this.tokens = burst;
        this.lastNanos = System.nanoTime();
    }

    public synchronized boolean allow() {
        long now = System.nanoTime();
        double elapsed = (now - lastNanos) / 1_000_000_000.0;
        lastNanos = now;
        tokens = Math.min(burst, tokens + elapsed * ratePerSec);
        if (tokens < 1.0) return false;
        tokens -= 1.0;
        return true;
    }
}
```

For **many pods**, this is not enough — use Redis + Lua. Time O(1).

</details>

---

### D2. Idempotency store for POST

**Hint.** ConcurrentHashMap or DB unique on key. Store status + body. Same key same body → replay.

<details>
<summary>Click to see solution</summary>

```java
public final class IdempotencyStore {
    private final ConcurrentHashMap<String, Saved> map = new ConcurrentHashMap<>();

    public record Saved(String requestHash, Integer status, String responseBody) {
        boolean complete() { return status != null; }
    }

    public Optional<Saved> existing(String key) {
        return Optional.ofNullable(map.get(key));
    }

    public boolean reserve(String key, String requestHash) {
        return map.putIfAbsent(key, new Saved(requestHash, null, null)) == null;
    }

    public void complete(String key, String requestHash, int status, String body) {
        map.replace(key, new Saved(requestHash, null, null),
                new Saved(requestHash, status, body));
    }
}

// In filter:
// hash = sha256(body)
// if existing and hash matches -> return saved response
// if existing and hash differs -> 409
// if reserve wins -> execute once, then complete
// if key is reserved/in-progress -> wait briefly or return 409/202 by API policy
```

Production: Redis/DB with TTL, not a local map (many pods). Atomically insert/reserve under a unique constraint **before** executing the business action.

</details>

---

### D3. In-memory LRU (they tie this to cache design)

**Hint.** LinkedHashMap access-order. Same as Round 1 C11 — they ask it again in design context.

<details>
<summary>Click to see solution</summary>

```java
public class LRUCache<K, V> extends LinkedHashMap<K, V> {
    private final int cap;
    public LRUCache(int cap) {
        super(cap, 0.75f, true);
        this.cap = cap;
    }
    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> e) {
        return size() > cap;
    }
}
```

Then say: “In production I use Redis LRU / Caffeine, not this, because this is one JVM.”

</details>

---

### D4. Circuit breaker sketch (states)

**Hint.** Closed count failures. Open skip until cooldown. Half-open allow one.

<details>
<summary>Click to see solution</summary>

```java
public final class TinyBreaker {
    enum State { CLOSED, OPEN, HALF }
    private State state = State.CLOSED;
    private int failures;
    private long openUntil;
    private final int threshold = 5;
    private final long openMs = 10_000;

    public synchronized boolean allow() {
        long now = System.currentTimeMillis();
        if (state == State.HALF) return false; // one probe already in flight
        if (state == State.OPEN) {
            if (now < openUntil) return false;
            state = State.HALF;
            return true;
        }
        return true;
    }

    public synchronized void success() {
        failures = 0;
        state = State.CLOSED;
    }

    public synchronized void failure() {
        failures++;
        if (state == State.HALF || failures >= threshold) {
            state = State.OPEN;
            openUntil = System.currentTimeMillis() + openMs;
        }
    }
}
```

Use Resilience4j in real Spring apps. This is to show you understand states.

</details>

---

### D5. Snowflake-style id (64-bit)

**Hint.** timestamp | worker | sequence. Mask bits. Guard clock moving backwards.

<details>
<summary>Click to see solution</summary>

```java
public final class Snowflake {
    private static final long EPOCH = 1_700_000_000_000L; // custom epoch
    private final long workerId;
    private long lastTs = -1;
    private long seq = 0;
    // 41 bits time, 10 worker, 12 sequence — example layout
    public Snowflake(long workerId) {
        if (workerId < 0 || workerId > 1023) throw new IllegalArgumentException("workerId");
        this.workerId = workerId;
    }

    public synchronized long nextId() {
        long ts = System.currentTimeMillis() - EPOCH;
        if (ts < lastTs) throw new IllegalStateException("clock moved backwards");
        if (ts == lastTs) {
            seq = (seq + 1) & 4095;
            if (seq == 0) {
                while ((ts = System.currentTimeMillis() - EPOCH) == lastTs) { /* wait */ }
            }
        } else {
            seq = 0;
        }
        lastTs = ts;
        return (ts << 22) | (workerId << 12) | seq;
    }
}
```

Worker id must be unique per instance (K8s: StatefulSet ordinal or an allocator).

</details>

---

## 13. Senior testing strategy and release confidence

---

### Q. How would you test a microservice end to end without making CI take one hour?

**Answer**

Layer it:

1. Fast unit tests for domain rules.
2. MVC/security slice tests for API contracts.
3. Repository integration tests with Testcontainers.
4. Consumer/producer contract tests for HTTP and Kafka schemas.
5. A small number of component tests (service + DB + Kafka, external HTTP stubbed).
6. Only critical user paths in deployed E2E.

Parallelize independent integration suites and reuse containers where safe. Do not put every permutation in E2E.

---

### Q. Consumer-driven contract testing?

**Answer**

The consumer publishes examples of requests/responses it depends on. The provider verifies them in CI (Pact/Spring Cloud Contract). This catches a provider removing a field before deployment.

It does **not** replace integration tests: both services can satisfy a contract and still have wrong business behavior or auth/config.

For Kafka, validate schema compatibility plus event semantics (required fields, key choice, idempotency).

---

### Q. How do you test resilience?

**Answer**

Test timeout, 500, connection reset, slow response, duplicate Kafka event, poison payload, broker/DB temporarily unavailable, and recovery after the circuit half-opens.

Use WireMock/Toxiproxy or controlled stubs—not random production chaos first. Assert bounded retries, backoff, no duplicate business row, correct 503/202, and DLT behavior.

---

### Q. What is chaos testing? When would you use it?

**Answer**

Deliberately terminate pods, inject latency, or block a dependency to verify graceful behavior. Start in a non-production environment with a hypothesis (“one adapter pod dies; requests recover within 30s”). Mature teams may do controlled production game days with rollback and blast-radius limits. Chaos without observability is just an outage.

---

### Q. Performance testing: load vs stress vs spike vs soak?

**Answer**

- **Load:** expected traffic and SLO.
- **Stress:** increase until it breaks; find capacity.
- **Spike:** sudden burst; test queues/autoscaling.
- **Soak:** hours/days; find leaks, pool starvation, log/disk growth.

Measure p50/p95/p99, error rate, throughput, CPU, GC, DB pool wait, Kafka lag. Average latency hides tail pain.

---

### Q. Test data and flaky-test management?

**Answer**

Each test creates and owns its data, uses unique ids, and cleans up/rolls back. Never depend on test order or a shared mutable QA record. Quarantine is temporary: every flaky test gets an owner and expiry. Retrying a flaky test forever hides a real race.

---

## 14. API, data, migrations, and datastore decisions

---

### Q. REST API design checklist at senior level?

**Answer**

Clear resource names, correct methods/statuses, validation, idempotency for creates, pagination/filter limits, stable error schema, OpenAPI, authentication **and object authorization**, request/trace id, backward-compatible changes, timeout budget, and rate limit.

Never expose a JPA entity directly. DTOs protect the wire contract from persistence changes.

---

### Q. How do you evolve a database with zero downtime?

**Answer**

Use **expand/contract**:

1. Add nullable column/table/index (backward compatible).
2. Deploy code that reads old and new as needed.
3. Backfill in bounded batches.
4. Switch writers/readers.
5. Verify no old version uses it.
6. Drop old column in a later release.

Use Flyway/Liquibase with reviewed, versioned migrations. Never rename/drop a column in the same deployment where old pods still run.

---

### Q. Flyway vs Liquibase? How do you handle a failed migration?

**Answer**

Flyway is simple ordered SQL/version scripts. Liquibase supports XML/YAML/SQL and richer change metadata. Pick one team standard.

Migrations should be forward-fixable and repeatable in staging. Large index/backfill operations may need online DB features or a separate job. Do not manually edit the schema and then mark CI green. A failed prod migration requires a tested recovery plan; rollback may be impossible after destructive data changes.

---

### Q. Read replica, sharding, partitioning—when?

**Answer**

- **Read replica:** scale read-only queries; accept replication lag. Never read “just wrote” state from a lagging replica if the user needs consistency.
- **Table partitioning:** one DB, split a large table by date/key for pruning and maintenance.
- **Sharding:** split data across DBs by tenant/customer key. Last resort: cross-shard joins, rebalancing, global uniqueness, and transactions become harder.

First fix queries/indexes/caching before sharding.

---

### Q. SQL vs MongoDB/document database?

**Answer**

SQL for relationships, joins, constraints, transactions, and ad-hoc operational queries (inventory/lifecycle). Document DB when aggregate-shaped JSON is read/written together and schema evolves, with limited cross-document joins.

Do not choose Mongo because the payload is JSON; PostgreSQL JSONB may be enough. Model access patterns first.

---

### Q. Elasticsearch/OpenSearch—when and why not as source of truth?

**Answer**

Use for full-text, fuzzy search, aggregations, autocomplete. Feed it asynchronously from the source DB using outbox/CDC. It is eventually consistent and mappings need care. The DB remains source of truth; if the index is lost, rebuild it.

---

### Q. Redis data types and use cases?

**Answer**

String (cache/counter), Hash (small object), Set (membership), Sorted Set (leaderboard/ranking), List/Stream (queue/log), HyperLogLog (approximate unique), bitmap (flags).

Always define TTL, memory policy, key namespace, and failure behavior. Redis is single-threaded for command execution (with background/I/O improvements in modern versions), so one expensive command can hurt. Avoid `KEYS` in production; use `SCAN`.

---

### Q. Database connection-pool sizing in microservices?

**Answer**

Total possible connections = replicas × pool max across all services. It must fit DB session capacity with headroom. Bigger pools can make the DB slower. Measure connection acquisition time, query latency, and active/idle. Set acquisition timeout and detect leaks. Scale app pods and pool size together—not independently.

---

### Q. Optimistic concurrency at the HTTP layer?

**Answer**

Return an `ETag`/version. Client sends `If-Match`. Update only if version matches; otherwise 412 Precondition Failed (or domain 409). This carries JPA `@Version` semantics through the API and prevents silent lost updates.

---

### Q. How do you prevent duplicate API requests across retries?

**Answer**

Require an idempotency key, persist it with request hash and final response under a unique constraint, and handle concurrent inserts. “Check then insert” without a constraint races. Decide what happens for an in-progress key, failed attempt, and TTL expiry.

---

### Q. Webhooks: delivery and security?

**Answer**

Sign payload with HMAC + timestamp, verify within a replay window, use event id for idempotency, retry with backoff, and expose delivery status/DLT. Return 2xx only after durable acceptance. Rotate webhook secrets.

---

### Q. File upload/download API?

**Answer**

Do not stream a 5-GB file through the Java service unless necessary. Issue a pre-signed S3 URL; after upload, process asynchronously. Validate size/type, scan malware, randomize storage key, authorize object access, and never trust the filename/path.

---

### Q. WebSocket vs Server-Sent Events vs polling?

**Answer**

Polling is simplest and often enough for job status. SSE is server-to-client over HTTP (notifications/progress) with automatic reconnect. WebSocket is bidirectional real-time (chat/collaboration), but needs connection state, scaling, heartbeats, and load-balancer support.

---

## 15. Low-level design / machine coding

At 5–8 years, some product companies ask one 45–90 minute LLD. They assess requirements, object boundaries, extensibility, concurrency, and tests—not whether you memorized UML.

---

### LLD exercise: Design a parking lot

**Clarify first**

- Vehicle types: motorcycle, car, truck?
- Spot types and compatibility?
- Multiple floors/entrances?
- Ticket at entry, payment at exit?
- Pricing by duration and vehicle?
- Must allocation be concurrent across gates?

**Core model**

```text
ParkingLot
  List<ParkingFloor>
  AllocationService
  PricingStrategy
  TicketRepository

ParkingFloor
  Map<SpotType, NavigableSet<ParkingSpot>>

ParkingSpot
  id, floor, type, status, version

Vehicle
  plate, VehicleType

ParkingTicket
  id, vehicle, spotId, entryTime, exitTime, status
```

**Interfaces / patterns**

```java
interface SpotAllocationStrategy {
    Optional<ParkingSpot> allocate(Vehicle vehicle);
    void release(String spotId);
}

interface PricingStrategy {
    Money calculate(ParkingTicket ticket, Instant exitTime);
}
```

Strategy supports nearest-spot vs lowest-floor and hourly vs weekend pricing. Repository abstracts persistence. Avoid singleton/global mutable maps.

**Entry flow**

1. Validate vehicle has no open ticket.
2. Select a compatible available spot.
3. Atomically reserve it (`UPDATE spot SET status='OCCUPIED' ... WHERE status='FREE'` or optimistic version).
4. Create ticket in the same transaction.
5. Return ticket.

**Exit flow**

1. Load open ticket.
2. Calculate fee using injected `Clock` and pricing strategy.
3. Record payment idempotently.
4. Close ticket and release spot in one transaction.

**Concurrency**

Two gates may choose the same spot. A local `synchronized` block is insufficient across pods. Use a conditional DB update/row lock/unique open-ticket constraint and retry allocation. Keep the transaction short; never wait for payment HTTP while holding the spot row lock.

**Important tests**

- Vehicle receives compatible spot.
- Full lot returns no availability.
- Two concurrent allocations cannot receive one spot.
- Duplicate entry returns the existing ticket or conflict.
- Pricing boundary (59/60 minutes).
- Failed payment does not release the spot.
- Exit retry is idempotent.

**What the interviewer wants to hear**

Start simple and state extension points. Do not add Kafka, five microservices, and Kubernetes to an in-memory LLD unless scale requirements demand them.

---

## Night-before checklist (Round 2)

1. Draw ESM: `esm-ws` → task chain → Kafka `ESM_ASYNCIF_REQUEST_{snaId}` → `snaadapter-a` → NE. Oracle + c3p0. No fake `/api/v1`.
2. Timeout on reply. Idempotent southbound. Sync = NMS vs NE.
3. Split services on bounded context, not class count. DB per service.
4. Dual write is a bug. Outbox or CDC.
5. Saga = local TX + compensate. Not XA.
6. Idempotency-Key + unique constraint = practical exactly-once.
7. Timeout every remote call. Retry only if idempotent + jitter. Circuit breaker states. Bulkhead pools.
8. Kafka order = partition = key. Lag, rebalance, DLT. `acks=all` + idempotent producer.
9. Liveness vs readiness. Graceful shutdown + offset commit.
10. ECS/ALB/RDS private + Secrets Manager. SQS vs Kafka. IAM task role.
11. Rolling needs dual-version schema. Expand/contract. Image tag = git SHA.
12. p99 debug: metrics → trace → SQL/pool/GC. Then rollback/flag.
13. One design: URL shortener **or** rate limiter **or** notifications — speak 15 minutes.
14. JWT resource server: issuer JWKS, stateless, CSRF off. Auth code + PKCE for users, client credentials for services. `hasRole` vs `hasAuthority`. ESM is XML + `@Authorizer`, not Boot JWT.
15. Hibernate: persistence context, persist vs merge, SEQUENCE batches, IDENTITY does not, `@Version` / 409 retry, no two bag join fetches, `@Modifying` clears L1.

---

## How to talk in Round 2

1. Ask one clarifying question (QPS, consistency, who the client is).
2. Draw boxes. Talk while drawing.
3. Name **failure** for every arrow (timeout, duplicate, poison).
4. Compare two options and pick one with a reason.
5. If you have not used EKS, say “I have used ECS / OpenShift; EKS is the same ideas with Pods.”
6. Stop when the design is good enough. Do not add Kubernetes Operators for a URL shortener.

Use Round 3 for manager questions and Round 4 for HR.
