# 56.4 Spring Testing

[← 056_3 JDBC and Transactions](056_3_Jdbc_and_Transaction_Abstraction.md) | [Course map](00_COURSE_MAP.md) | **Next:** [056_5 Async, Scheduling, Cache →](056_5_Async_Scheduling_Cache.md)

This is **`spring-test`**. Boot slices (`@WebMvcTest`) are named only for comparison.

Teaching is simple first. **Interview Q&A at the end is 5–8 year standard.**

---

## Simple first

**Not every test should start Spring.** That is the whole chapter.

| What you want to check | What to do |
|------------------------|------------|
| Math / if-else in a service | `new DiscountService()` + maybe Mockito. **Zero Spring.** |
| Did I map `GET /orders/1` correctly? | **MockMvc** — fake HTTP, no real port |
| Does SQL + `@Transactional` work? | Small Spring context + H2 or Testcontainers |
| Whole app with Tomcat | Rare. Slow. Last resort. |

**Why constructor injection helps tests:** you can `new OrderService(mockRepo)` in one line. If you used field `@Autowired`, you need Spring or reflection just to test a calculator.

**Context cache (simple):** starting Spring is slow. JUnit reuses the same started app for many test classes **if** the config looks the same. If you change profiles, properties, or (in Boot) `@MockBean`, Spring starts **again**. That is why a “small extra mock” can make CI 3× slower.

**Test `@Transactional`:** Spring **undoes** your DB writes after the test. That is usually what you want. Trap: a method with `REQUIRES_NEW` **really commits** and will **not** be undone.

---

## When you interview (5–8 years)

A senior does **not** `@SpringBootTest` the world to test `OrderService.total()`. They explain cache keys, `@DirtiesContext`, MockMvc standalone vs web context, and `@Mock` vs `@MockBean`.

---

## 1. The test you should write first (no Spring)

```java
class DiscountServiceTest {
    @Test
    void seniorGetsTenPercent() {
        DiscountService s = new DiscountService();
        assertEquals(90, s.apply(100, CustomerType.SENIOR));
    }
}
```

Constructor DI exists so this is possible. If you cannot do this, the production design is wrong — not the test framework.

Mockito without Spring:

```java
OrderRepository repo = mock(OrderRepository.class);
when(repo.find(1L)).thenReturn(new Order(1L));
OrderService service = new OrderService(repo);
```

Use Spring in tests when you need: real AOP (`@Transactional`), real MVC mapping, real SQL, or real wiring of `@Configuration`.

---

## 2. `@SpringJUnitConfig` / `@ContextConfiguration`

JUnit 5:

```java
@ExtendWith(SpringExtension.class) // included in @SpringJUnitConfig
@SpringJUnitConfig(classes = OrderConfig.class)
class OrderServiceSpringTest {

    @Autowired
    OrderService orders;

    @Test
    void wires() {
        assertNotNull(orders);
    }
}
```

Equivalent older style: `@ContextConfiguration(classes = OrderConfig.class)`.

XML: `@ContextConfiguration("classpath:test-context.xml")`.

`@ContextHierarchy` — parent/child contexts (mirrors production root + servlet). Rare in new tests; useful when you must.

---

## 3. Context cache

Spring caches contexts in the JVM by a **MergedContextConfiguration** key:

- config classes / XML locations
- active profiles
- `ContextCustomizer`s
- property sources / `TestPropertySource`
- web vs non-web
- context initializer classes

Same key → **reuse**. Different key → **new context** (expensive).

```text
12 test classes with identical @SpringJUnitConfig(AppConfig.class)
    → 1 refresh

12 classes each with a different @MockBean / different profile
    → 12 refreshes
```

`@DirtiesContext` tells Spring: **this test mutated the singleton state; drop the cache**.

| Mode | Meaning |
|------|---------|
| `AFTER_CLASS` (common) | Evict when the class finishes |
| `AFTER_EACH_METHOD` | Evict every method — very slow |
| `BEFORE_CLASS` / `BEFORE_METHOD` | Start from a fresh context |

**Senior rule:** prefer isolated data (tx rollback, unique IDs) over `@DirtiesContext`. Use dirties when you change a singleton, stop a `SmartLifecycle`, or mutate statics.

`@DisabledInAotMode` / AOT tests are a Spring 6 topic: native/AOT uses a different test context story. Mention if asked; do not design all tests around it.

---

## 4. Profiles and properties in tests

```java
@SpringJUnitConfig(AppConfig.class)
@ActiveProfiles("test")
@TestPropertySource(properties = "app.mail.host=localhost")
class MailTest { }
```

`@TestPropertySource` sits high in the Environment (wins over `@PropertySource` on the config class).

`@DynamicPropertySource` (Spring 5.2.5+ / Boot Testcontainers pattern):

```java
@DynamicPropertySource
static void db(DynamicPropertyRegistry r) {
    r.add("jdbc.url", container::getJdbcUrl);
}
```

Core Framework supports it on the TestContext. You do not need Boot for the annotation.

---

## 5. `@Transactional` tests

```java
@SpringJUnitConfig(TxConfig.class)
@Transactional
class OrderRepoTest {

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void insertIsRolledBack() {
        jdbc.update("insert into orders(id) values (1)");
    }
}
```

Default: **rollback after each test method**. The next method does not see the insert.

That is why a test that asserts “row exists in DB after the app commits” **fails** unless you `@Commit` / `@Rollback(false)`.

Nested service `@Transactional(REQUIRES_NEW)` **commits for real** (different tx) — not rolled back by the test tx. Classic trap.

`@Sql("/schema.sql")` / `@Sql("/data.sql")` run scripts; combine with rollback or `@Sql(executionPhase = AFTER_TEST_METHOD)` to clean.

---

## 6. MockMvc

**Simple:** MockMvc is “fake HTTP inside the JVM.” No port 8080, no real browser. You still go through DispatcherServlet (mapping, JSON, status codes).

Two flavors: **standalone** (you pass the controller yourself — fast) vs **web context** (load real MVC config — heavier, closer to production mapping).

### Standalone (fast, Core-friendly)

```java
class OrderControllerTest {
    MockMvc mvc = MockMvcBuilders.standaloneSetup(new OrderController(new OrderService(...)))
            .setControllerAdvice(new RestExceptionHandler())
            .build();

    @Test
    void getOrder() throws Exception {
        mvc.perform(get("/orders/{id}", 1).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }
}
```

You **wire the controller yourself**. Filters/interceptors from production config are **not** there unless you add them. Perfect for controller unit tests.

### Web application context

```java
@SpringJUnitConfig(classes = {WebConfig.class, OrderConfig.class})
@WebAppConfiguration
class OrderMvcTest {

    @Autowired
    WebApplicationContext wac;

    MockMvc mvc;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }
}
```

`@WebAppConfiguration` makes a `MockServletContext` web context. You get real `HandlerMapping`, converters, advice **from that config**. Still no real socket.

Boot: `@AutoConfigureMockMvc` + `@SpringBootTest` or `@WebMvcTest`. `@WebMvcTest` is a **slice**: MVC infrastructure + one controller, collaborators mocked. Internally still MockMvc. Name it in interviews as “Boot’s opinionated standalone+slice.”

### What MockMvc does not prove

- Real HTTP client behavior (chunking, TLS)
- Servlet container quirks
- Filters not registered in the builder
- Security filter chain unless you `addFilters` / `springSecurity()`

Use `MockMvc` for mapping, status, JSON path, validation errors. Use a server test for CORS+TLS+filters together.

---

## 7. Mockito vs Spring — `@MockBean` / `@MockitoBean`

**Core:** there is no `@MockBean`. You `@Bean` a mock in a **nested test config**:

```java
@TestConfiguration
static class Mocks {
    @Bean
    PaymentGateway paymentGateway() {
        return Mockito.mock(PaymentGateway.class);
    }
}
```

If `PaymentGateway` is also `@Component`-scanned, you get **two beans** unless you exclude scan or `@Primary` the mock. This is why Boot invented `@MockBean`: replace by type in the context.

**Boot (contrast):**

| Annotation | Role |
|------------|------|
| `@MockBean` | Add/replace a mock in the **Spring context** (deprecated toward `@MockitoBean` in Boot 3.4) |
| `@MockitoBean` | Replacement API |
| `@SpyBean` / `@MockitoSpyBean` | Spy on a real bean |
| `@Mock` + `@InjectMocks` | **No Spring** — Mockito only |

`@MockBean` **changes the context cache key** → extra refreshes. Ten test classes, ten mocks of different types = pain. Prefer constructor unit tests for service logic; reserve context mocks for MVC/security slices.

---

## 8. What a senior loads (and does not)

| Need | Load |
|------|------|
| Pure logic | Nothing |
| `@Transactional` + JdbcTemplate | Config with DataSource + tx manager + repo |
| JSON mapping of one controller | MockMvc standalone + Jackson |
| Full DispatcherServlet config | `@WebAppConfiguration` + MVC config |
| Listeners, scheduling | Only if that is the subject; otherwise disable scheduling in tests |

Do **not** load: embedded servlet + security + JPA + Kafka to assert a string concat.

`@TestConfiguration` (Boot) / inner `@Configuration` — additional beans for tests. Inner configs in the test class are picked up by Boot; with Core `@SpringJUnitConfig`, **list the class** in `classes = {Prod.class, TestDoubles.class}`.

---

## Production (suite) pitfalls

1. **One giant context for all tests** mutated with statics — flakes.
2. **`@DirtiesContext` on every class** — 10-minute builds.
3. **`@MockBean` explosion** — cache misses.
4. **Test `@Transactional` hiding `REQUIRES_NEW` commits** you wanted to assert.
5. **MockMvc standalone** missing `@ControllerAdvice` — tests 500, prod returns 400 JSON.
6. **Asserting on mocks never called** because the test used a real bean from scan.
7. **Time zones / locale** — `MessageSource` tests depending on JVM default ([056_1](056_1_Events_SpEL_Resources.md)).

---

## Interview Ready Q&A (5–8 year standard)

The notes said “not every test starts Spring.” **Here, name cache keys, `@MockBean` cost, MockMvc limits, and AFTER_COMMIT tests that never fire.**

### Q1. When do you *not* use Spring in a test?

**Answer:** When the unit is a POJO with injected deps — `new` + mocks. That is the majority of service tests.

**Counter:** Then why does everyone on your team use `@SpringBootTest`?

**Counter-answer:** Habit and copy-paste. It proves wiring, not logic, and is slow. A senior splits: many unit tests, few context tests, very few full-server tests.

---

### Q2. What does `SpringExtension` do?

**Answer:** JUnit 5 extension that drives `TestContextManager`: load/cache context, inject the test instance, run before/after callbacks (tx, sql).

**Counter:** JUnit 4?

**Counter-answer:** `SpringRunner` / `SpringJUnit4ClassRunner`. Same TestContext underneath. New code: Jupiter.

---

### Q3. How does the context cache work?

**Answer:** Keyed by merged configuration (classes, profiles, properties, web flag, customizers). Same key reused across classes in the same JVM.

**Counter:** Why did adding `@MockBean` make CI 3× slower?

**Counter-answer:** `@MockBean` customizes the context → new cache key → extra `refresh()`. Many keys = many full startups.

---

### Q4. What is `@DirtiesContext` for?

**Answer:** Evict the cached context because the test changed singleton state that would leak.

**Counter:** Is it a substitute for rolling back DB changes?

**Counter-answer:** No. Use `@Transactional` rollback or truncate. Dirties is for the **container**, not the database (unless you mutated an in-memory singleton cache of DB data).

---

### Q5. MockMvc standalone vs `webAppContextSetup`?

**Answer:** Standalone: you pass controller instances; fastest; you must add advice/converters. Web context: real MVC beans from a Spring config; heavier; closer to mapping/CORS/interceptor tests.

**Counter:** Does standalone use `DispatcherServlet`?

**Counter-answer:** Yes, a minimal one. It still runs handler mapping/adapter. It does not start Tomcat.

---

### Q6. `@WebMvcTest` vs `@SpringJUnitConfig` + MockMvc?

**Answer:** `@WebMvcTest` is Boot: auto-configures MVC slice, mocks the rest, one controller. Core equivalent is a small `@Configuration` with `@EnableWebMvc` + the controller bean + MockMvc web setup — more manual.

**Counter:** Does `@WebMvcTest` load `@Service`?

**Counter-answer:** No (generally). Collaborators are `@MockBean`. That is the point of a slice.

---

### Q7. `@Mock` vs `@MockBean`?

**Answer:** `@Mock` = Mockito, not in the container. `@MockBean` = mock **is** a Spring bean replacing the real one. `@InjectMocks` never runs AOP.

**Counter:** Can you `@Autowired` a `@Mock`?

**Counter-answer:** Not unless you also put it in the context. `@Autowired` looks at Spring, `@Mock` looks at Mockito.

---

### Q8. Why did my `@Transactional` test not see data the app “committed”?

**Answer:** The test rolled back. Or the app used `REQUIRES_NEW` / a different manager. Or you queried a different DataSource.

**Counter:** How do you test that a listener runs `AFTER_COMMIT`?

**Counter-answer:** Do **not** wrap the test in a transaction that never commits. `@Commit` the test tx, or publish from a non-test tx and use a fake listener. After-commit hooks do not run on rollback — including test rollback.

---

### Q9. `@TestPropertySource` vs `application-test.properties`?

**Answer:** `@TestPropertySource` is Framework TestContext, high precedence. `application-test.properties` is Boot profile files. In Core without Boot, `@TestPropertySource` or programmatic Environment is how you override.

**Counter:** Same property in both?

**Counter-answer:** TestPropertySource typically wins. Do not fight — pick one source per test.

---

### Q10. `@Sql` vs Flyway in tests?

**Answer:** `@Sql` is ad-hoc scripts for a test class. Flyway/Liquibase is how schemas should evolve. Use Flyway for the schema, `@Sql` for fixture rows if needed.

**Counter:** `@Sql` and `@Transactional` rollback?

**Counter-answer:** Scripts participate in the test transaction if they use the same DataSource and the script runner joins the tx (default). Rollback clears inserts. DDL may or may not roll back (PostgreSQL DDL transactional; some DBs not).

---

### Q11. How do you test an `@Aspect`?

**Answer:** Small context: `@EnableAspectJAutoProxy` + aspect bean + dummy `@Service`. Call the **bean** (proxy), assert the aspect’s side effect. Unit-test advice with a mock `ProceedingJoinPoint` for branching logic.

**Counter:** Test used `new DummyService()`?

**Counter-answer:** No proxy, aspect silent, false confidence.

---

### Q12. `@WebAppConfiguration` — why?

**Answer:** Loads a `WebApplicationContext` with a `MockServletContext` so request/session scopes and MVC work.

**Counter:** Forgetting it with MockMvc web setup?

**Counter-answer:** You may get a non-web context and fail to create `DispatcherServlet` / request scope.

---

### Q13. Context hierarchy in tests?

**Answer:** `@ContextHierarchy({@ContextConfiguration(...), @ContextConfiguration(...)})` parent then child. Mirrors root + servlet. Heavy; skip unless you must test that split.

**Counter:** Bean override in child?

**Counter-answer:** Child can define the same name; lookups from child see the child bean. Same as production.

---

### Q14. How do you test `HandlerInterceptor`?

**Answer:** MockMvc with `addInterceptors` (standalone) or full web config. `perform` and assert headers/status. Unit-test the interceptor class with mock request/response without MVC for pure logic.

**Counter:** Filter vs interceptor test?

**Counter-answer:** Filters need `addFilters` on MockMvc or a servlet container. Easy to think an interceptor test proved Security — it did not.

---

### Q15. Parallel test execution (JUnit) and Spring?

**Answer:** Context cache is static JVM-wide. Parallel classes sharing a mutable singleton context race. Prefer not to parallelize dirty Spring tests; or use isolated configs.

**Counter:** Is the cache thread-safe?

**Counter-answer:** The cache itself is concurrent, but **beans inside** a shared context are not magically isolated. Two tests mutating one singleton service = flakes.

---

### Q16. `ApplicationContextInitializer` in tests?

**Answer:** Hook to mutate Environment before refresh (add PropertySources, activate profiles programmatically). Used by Testcontainers property mapping historically.

**Counter:** vs `@DynamicPropertySource`?

**Counter-answer:** `@DynamicPropertySource` is the readable version for adding properties. Initializers are more general (register scopes, etc.).

---

### Q17. Why inject `JdbcTemplate` in a test instead of mocking it?

**Answer:** SQL, transactions, and mapping bugs only show against a database (H2 or Testcontainers). Mocking JdbcTemplate proves your mock, not SQL.

**Counter:** Always Testcontainers?

**Counter-answer:** H2 is faster but not Postgres. For dialect-specific SQL, Testcontainers is worth it. For JdbcTemplate basics, H2 is fine.

---

### Q18. `@RecordApplicationEvents` / `ApplicationEvents`?

**Answer:** Spring 5.3.3+ test support to capture events published during a test and assert them.

**Counter:** Async listeners?

**Counter-answer:** May not have run when the test asserts. Await or test the listener in isolation.

---

### Q19. What does a 5–8 YOE “test strategy” answer sound like?

**Answer:** Pyramid: unit (no Spring) → a few `@SpringJUnitConfig` for AOP/SQL → MockMvc for HTTP contracts → one or two system tests. Context cache kept stable; mocks in the container used sparingly.

**Counter:** Coverage 90% with only `@SpringBootTest`?

**Counter-answer:** Coverage of lines, not of failures. Slow feedback. Interviewers want intent, not a number.

---

### Q20. `@Nested` JUnit classes and Spring?

**Answer:** Nested tests can inherit the parent’s `SpringExtension` and context — or declare their own (new cache key). Know that inner `@Configuration` in Boot tests is picked up automatically; in Core you must register it.

**Counter:** Nested class with extra `@MockBean`?

**Counter-answer:** Different customizer → different cache → extra refresh for that nested class.

---

### Interview one-liner

> `new` + mocks for logic. `SpringExtension` + cached `ApplicationContext` for wiring, AOP, and SQL. MockMvc for DispatcherServlet without a port. `@Transactional` tests roll back — `REQUIRES_NEW` does not. `@DirtiesContext` and `@MockBean` bust the cache; use them as little as you can.
