# 12. Testing Spring Boot Applications

## Start here (simple English)

**In one sentence:** A test is a small program that **proves** a piece of behaviour. Prefer the **cheapest** test that still catches the bug.

**Everyday picture:** Checking a car.

| Check | Like this test | Cost |
|-------|----------------|------|
| Horn works on the bench | **Unit** — `new OrderService(mockRepo)` + JUnit | Cheap |
| Dashboard + steering only | **Slice** — `@WebMvcTest` (controller, not the whole car) | Medium |
| Drive on a real road | **Integration** — `@SpringBootTest` + real Postgres (Testcontainers) | Expensive |

**Do not** put `@SpringBootTest` on every class. Your CI will crawl.

**Words:**

| Annotation | Meaning |
|------------|---------|
| `@Test` | This method is a test (JUnit 5) |
| `@Mock` | Fake object, **no Spring** |
| `@MockBean` / `@MockitoBean` | Fake object **inside** the Spring test context |
| `@WebMvcTest` | Only web layer |
| `@DataJpaTest` | Only JPA + a test database |
| `@SpringBootTest` | Almost the real app |

Interview Q&A is **5–8 year standard**.

---

Testing is not “I have `@SpringBootTest` on everything”. It is **the smallest context that still proves the risk**, plus a few real integration tests against a database.

---

## 1. Test pyramid for a Boot service

| Layer | Tool | Spring context? | Speed |
|-------|------|-----------------|-------|
| Unit | JUnit 5 + Mockito | No | Fast |
| Slice | `@WebMvcTest`, `@DataJpaTest`, `@JsonTest` | Partial | Medium |
| Integration | `@SpringBootTest` + Testcontainers | Full | Slow |
| Contract / e2e | REST Assured, Playwright, consumer contracts | Full system | Slowest |

**Unit-test services** with constructor injection and mocks. You do not need Spring to test `transfer()` math.

Use Spring when the **wiring, MVC, JPA, or Security** is the thing under test.

---

## 2. JUnit 5 basics

```java
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock OrderRepository repo;
    @InjectMocks OrderService service; // constructor injection preferred over InjectMocks

    @Test
    void rejectsNegativeQuantity() {
        assertThatThrownBy(() -> service.create(new CreateOrderRequest("sku", -1)))
            .isInstanceOf(ValidationException.class);
    }
}
```

`@BeforeEach` / `@AfterEach`. `@ParameterizedTest`. AssertJ `assertThat`.

---

## 3. Slice tests

### `@WebMvcTest(OrderController.class)`

Loads **MVC** infrastructure + that controller. Does **not** load services, JPA, Security auto-config may still load if on classpath (Boot 2.4+ often auto-configures Security — add `@AutoConfigureMockMvc` and mock users, or `@Import` security test config).

```java
@WebMvcTest(OrderController.class)
class OrderControllerTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean OrderService orders;  // Boot 3.4+; older: @MockBean

    @Test
    void getOrder() throws Exception {
        when(orders.get(1L)).thenReturn(new OrderResponse(1L, "ok"));
        mockMvc.perform(get("/v1/orders/1").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }
}
```

`@MockBean` (legacy name) **replaces** the bean in the **context**. `@Mock` does not.

### `@DataJpaTest`

Loads JPA + DataSource (usually H2 if on test classpath, or your testcontainers via properties). `@Transactional` **rollback after each test** by default.

```java
@DataJpaTest
class OrderRepositoryTest {
    @Autowired OrderRepository repo;
    @Autowired TestEntityManager em;

    @Test
    void findBySku() {
        em.persistAndFlush(new Order("ABC"));
        assertThat(repo.findBySku("ABC")).isPresent();
    }
}
```

### `@JsonTest`

`JacksonTester<OrderResponse>` — serialization contracts.

### `@JdbcTest` / `@DataMongoTest` / `@WebFluxTest`

Same idea: one slice.

---

## 4. `@SpringBootTest`

```java
@SpringBootTest
@AutoConfigureMockMvc
class OrderIT {
    @Autowired MockMvc mockMvc;
}
```

| `webEnvironment` | Meaning |
|------------------|---------|
| `MOCK` (default) | Mock servlet, `MockMvc` |
| `RANDOM_PORT` | Real server, `TestRestTemplate` / `WebTestClient` |
| `DEFINED_PORT` | Uses `server.port` — avoid in CI |
| `NONE` | No web |

`RANDOM_PORT` is what you want to test filters + MVC + JSON together.

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderApiIT {
    @Autowired TestRestTemplate http;

    @Test
    void create() {
        ResponseEntity<String> res = http.postForEntity("/v1/orders", body, String.class);
        assertThat(res.getStatusCode().is2xxSuccessful()).isTrue();
    }
}
```

---

## 5. `@MockBean` vs `@MockitoBean` vs `@Mock`

| | Spring context | Use |
|--|----------------|-----|
| `@Mock` | No | Pure unit |
| `@MockBean` | Yes, replaces bean | Slice / IT when you must stub a collaborator |
| `@MockitoBean` | Yes (Boot 3.4) | Replacement name for `@MockBean` |
| `@SpyBean` / `@MockitoSpyBean` | Yes | Partial real bean |

**Pitfall:** `@MockBean` on a heavily used type forces **context cache miss**. Every unique mock setup = new Spring context = slow CI. Prefer test slices or constructor unit tests.

`@SpringBootTest` + `@MockBean DataSource` is usually a smell — use Testcontainers.

---

## 6. Testcontainers (the senior default for DB tests)

```java
@Testcontainers
@SpringBootTest
class OrderRepositoryIT {
    @Container
    static PostgreSQLContainer<?> pg = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", pg::getJdbcUrl);
        r.add("spring.datasource.username", pg::getUsername);
        r.add("spring.datasource.password", pg::getPassword);
    }
}
```

H2 **lies** (types, locks, SQL dialect). For anything beyond trivial queries, Postgres in a container is the contract.

Service connection (Boot 3.1+): `@ServiceConnection` on the container reduces property glue.

---

## 7. `@Transactional` tests — the trap

`@DataJpaTest` / `@SpringBootTest` + `@Transactional` on the test class **rolls back**. You never see commit-time constraints, listeners after commit, or “visible to another TX”.

A test that opens a **second** thread/TX (`REQUIRES_NEW`, async) may **not see** uncommitted data from the test TX → flaky “works in prod”.

When you need real commits: don’t annotate the test with `@Transactional`; clean tables with `@Sql` / `JdbcTemplate` / Testcontainers fresh schema.

---

## 8. `@Sql`, profiles, properties

```java
@ActiveProfiles("test")
@TestPropertySource(properties = "app.feature.x=true")
@Sql("/sql/seed.sql")
```

`src/test/resources/application-test.yml`:

```yaml
spring:
  jpa:
    open-in-view: false
  flyway:
    enabled: true
```

`@DynamicPropertySource` beats yaml (chapter 04).

---

## 9. Security tests

```java
mockMvc.perform(get("/v1/orders/1")
        .with(jwt().jwt(j -> j.claim("sub", "u1"))))
    .andExpect(status().isOk());
```

or `.with(user("ana").roles("ADMIN"))`.

`@WithMockUser(roles = "ADMIN")` on the method.

Don’t disable security in `application-test.yml` if you need to prove 401/403.

---

## 10. Test context cache

Spring caches contexts by configuration key. Logs: `Started Application in …` many times → cache broken (different `@MockBean`, different properties).

Keep IT configs uniform. Use `@DirtiesContext` **rarely** (it is a sledgehammer).

---

## 11. Production pitfalls

1. Only `@SpringBootTest` — 10 minute CI.
2. H2 in tests, Postgres in prod — production-only SQL bugs.
3. `@MockBean` on `OrderService` in an IT that was supposed to test JPA.
4. Asserting nothing except `contextLoads()`.
5. Tests depending on method order / leftover rows.
6. Using real external APIs in unit tests — WireMock / MockWebServer.

---

# Interview Q&A (5–8 year bar)

A fresher writes `contextLoads()`. A 5–8 year answer chooses slice vs full, Testcontainers vs H2, and explains test `@Transactional` rollback traps.

### Q1. `@SpringBootTest` vs `@WebMvcTest`?

**Answer:** Full context vs MVC slice. Slice is faster and forces you to mock the service. Full test proves wiring.

**Counter:** When is slice not enough?  
**Answer:** Filters + security + advice + conversion together, or JPA queries.

---

### Q2. Why `@MockBean` instead of `@Mock` in a controller test?

**Answer:** The controller is created by Spring and needs a **bean** of `OrderService`. `@Mock` lives only in the test class.

---

### Q3. Why are `@DataJpaTest`s fast?

**Answer:** Only JPA slice + embedded/test DB, not the web layer. Rollback keeps data isolated.

**Counter:** Why still use Testcontainers?  
**Answer:** Dialect and lock behavior. H2 is not Postgres.

---

### Q4. `MockMvc` vs `TestRestTemplate` / `RANDOM_PORT`?

**Answer:** `MockMvc` = mocked servlet, no real TCP. `RANDOM_PORT` = real embedded server, tests serialization, filters, error controller more faithfully.

---

### Q5. Why did my test pass but prod failed on unique constraint?

**Answer:** Test TX rolled back before commit-time constraints / indexes. Or H2 didn’t enforce the same constraint.

---

### Q6. How do you inject Testcontainers URLs?

**Answer:** `@DynamicPropertySource` or `@ServiceConnection`.

---

### Q7. Does `@Transactional` on a test join the service TX?

**Answer:** Same thread, default `REQUIRED` → yes, one TX, then test rollback undoes service `save`. Good for isolation, bad for testing commit.

---

### Q8. How do you test `@ControllerAdvice`?

**Answer:** `@WebMvcTest` + perform request that throws / fails validation; assert status and JSON.

---

### Q9. Context cache exploded. Why?

**Answer:** Unique `@MockBean` / properties / `@Import` per test class. Standardize.

---

### Q10. How do you test Security?

**Answer:** `SecurityMockMvcRequestPostProcessors`, `@WithMockUser`, JWT post-processor. Assert 401/403/200.

---

### Q11. `@InjectMocks` vs constructor in unit tests?

**Answer:** Prefer `new OrderService(mockRepo)`. `@InjectMocks` hides constructor changes.

---

### Q12. What is a slice annotation’s `@AutoConfigure*`?

**Answer:** Boot test auto-config that loads only that layer’s beans (`@AutoConfigureDataJpa`, `@AutoConfigureWebMvc`).

---

### Q13. Should you test private methods?

**Answer:** No. Test through the public API of the class.

---

### Q14. Flyway in tests?

**Answer:** Yes, same migrations as prod. Schema is the product. `ddl-auto=create-drop` hides migration bugs.

---

### Q15. `@DirtiesContext`?

**Answer:** Rebuilds the context after the test. Use when a test mutates a singleton irrecoverably. If you need it often, tests are coupled to shared mutable state.
