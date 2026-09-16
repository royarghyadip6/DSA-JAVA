# 03. Spring and Spring Boot Annotations — Decision Guide

## Start here (simple English)

**In one sentence:** An annotation is a **label** on a class or method. Spring reads labels at startup and decides what to do.

**Everyday picture:** Stickers on lunch boxes in an office fridge.

- `@Service` = “this is business logic”
- `@RestController` = “this talks HTTP and returns JSON”
- `@Autowired` / constructor = “please put the matching lunch box in my bag”

You do **not** need to memorise 200 annotations. You need to **pick the right sticker**.

**First set to learn:**

| You want | Put this |
|----------|----------|
| A normal Spring-managed class | `@Component` |
| Business logic | `@Service` |
| Database access class you wrote | `@Repository` |
| HTTP JSON API | `@RestController` |
| A factory method for an object you build yourself | `@Bean` inside `@Configuration` |
| A transaction around a method | `@Transactional` (on a **public** service method) |

The rest of this chapter is a **decision guide**, not a dictionary. JPA, AOP, Security, Testing have their own chapters. Interview Q&A is **5–8 year standard**.

---

This is **not** a dictionary of every annotation. It is how you **choose**.

---

## 1. How annotations work in Spring

Most Spring annotations are **markers** read at startup by `BeanPostProcessor`s, `BeanFactoryPostProcessor`s, or MVC infrastructure. They are not Java language features beyond metadata.

**Meta-annotations:** `@RestController` = `@Controller` + `@ResponseBody`. `@SpringBootApplication` = three annotations. You can write your own composed annotations.

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Service
@Transactional
public @interface ApplicationService {}
```

Use composed annotations for **team conventions**, not to hide magic.

---

## 2. Stereotype — what you put on a class

| Need | Annotation | Why that one |
|------|------------|--------------|
| Generic Spring-managed class | `@Component` | Lowest level |
| Business logic | `@Service` | Documents layer; same scan as `@Component` |
| Persistence adapter you wrote | `@Repository` | Exception translation to `DataAccessException` |
| MVC controller returning views | `@Controller` | Handler mapping |
| HTTP API returning JSON | `@RestController` | `@Controller` + `@ResponseBody` |
| Bean factory class | `@Configuration` | `@Bean` methods, proxy mode |
| Global MVC advice | `@ControllerAdvice` / `@RestControllerAdvice` | Exception handlers, `@InitBinder` |
| Boot auto-config class | `@AutoConfiguration` (Boot 3) | Loaded via `AutoConfiguration.imports`, not component scan |

**Rule:** one stereotype per class. Do not stack `@Service` and `@Repository`.

**`@Component` vs `@Bean`:**

- `@Component` on **your** class → scan creates one definition.
- `@Bean` on a **method** → you construct objects you do not own (`RestTemplate`, `ObjectMapper` customizer, third-party clients).

If you can add an annotation to the class, prefer stereotype. If the class is in a library, use `@Bean`.

---

## 3. Wiring — how beans find each other

| Situation | Use |
|-----------|-----|
| Required collaborator | Constructor; no annotation if single ctor |
| Optional collaborator | `ObjectProvider<T>` or `Optional<T>` |
| Several implementations, one default | `@Primary` on the default |
| Several implementations, pick by name | `@Qualifier` (or custom qualifier) |
| Inject config value | `@Value` for one-off; `@ConfigurationProperties` for groups |
| Delay a singleton | `@Lazy` on class or injection point |
| Create this bean after another (rare) | `@DependsOn` — implicit deps from injection are enough |
| Prototype per call | `@Lookup` / `ObjectProvider` |
| Scope | `@Scope` + `proxyMode` for web scopes into singletons |

**Do not** use `@Autowired` on **every** constructor in new code. Use it when there are multiple constructors.

`@Qualifier` value defaults to bean name. Bean name defaults to decapitalized class name (`orderService`). `@Bean` method name is the default bean name.

---

## 4. Configuration and Boot

| Annotation | When |
|------------|------|
| `@Configuration` | Java config |
| `@Bean` | Manual instance |
| `@Import` | Pull another config class (or `ImportSelector`) |
| `@ImportResource` | Legacy XML |
| `@PropertySource` | Extra `.properties` (YAML needs a factory; prefer `application.yml` + `spring.config.import`) |
| `@Profile` | Bean only if profile active |
| `@ConditionalOn*` | Boot auto-config (chapter 05) — avoid in app code unless you are writing a starter |
| `@ConfigurationProperties` | Typed config (`app.mail.*`) |
| `@ConfigurationPropertiesScan` / `@EnableConfigurationProperties` | Register the properties class as a bean |
| `@SpringBootApplication` | Application entry |
| `@EnableAutoConfiguration` | Usually via Boot application annotation |
| `@ConditionalOnProperty` | Feature flags in **your** `@Configuration` is OK |

`@Value("${timeout:30}")` — default `30` if missing. No validation, no grouping, no IDE metadata. Fine for two values. Not fine for a mail server block.

---

## 5. Web / REST (details in chapter 06)

| Annotation | Role |
|------------|------|
| `@RequestMapping` | Super-annotation; method + path + consumes/produces |
| `@GetMapping` `@PostMapping` `@PutMapping` `@PatchMapping` `@DeleteMapping` | HTTP method shortcuts |
| `@PathVariable` | URI template `{id}` |
| `@RequestParam` | Query / form |
| `@RequestHeader` `@CookieValue` | Header / cookie |
| `@RequestBody` | JSON → object (converter) |
| `@ResponseBody` | Object → JSON |
| `@ResponseStatus` | Default status (prefer `ResponseEntity` for APIs) |
| `@RequestPart` | Multipart |
| `@ModelAttribute` | Form / query → object (also MVC model) |
| `@CrossOrigin` | CORS on one handler — prefer a global `CorsConfigurationSource` |
| `@ControllerAdvice` | Cross-controller MVC hooks |

**`@PathVariable` vs `@RequestParam`:** `/users/5` vs `/users?id=5`. IDs in the path for resources.

**`required = false`** on a primitive `@PathVariable int id` will still fail if missing — use `Integer`.

---

## 6. Validation (details in chapter 07)

| Annotation | Role |
|------------|------|
| `@Valid` | Cascade Jakarta Bean Validation (on `@RequestBody`, nested objects) |
| `@Validated` | Spring’s variant — supports **validation groups** on classes/methods |
| `@NotNull` `@NotBlank` `@Size` `@Email` `@Min` `@Max` `@Pattern` | Constraints on fields |
| `@Constraint` | Custom constraint |

Controller:

```java
@PostMapping("/orders")
public OrderResponse create(@Valid @RequestBody CreateOrderRequest req) { ... }
```

Service-layer validation: `@Validated` on the class + `@Valid` on parameters. `@Valid` alone on a service method does **nothing** unless there is a method-validation interceptor (`@Validated` on the class).

---

## 7. Scheduling, async, cache (chapter 14)

| Annotation | Needs | Trap |
|------------|-------|------|
| `@EnableAsync` / `@Async` | Proxy; return `void` or `Future`/`CompletableFuture` | Self-invocation; default executor is unbounded |
| `@EnableScheduling` / `@Scheduled` | One node vs many | Every pod runs the job |
| `@EnableCaching` / `@Cacheable` `@CacheEvict` `@CachePut` | Same proxy rules | Unknown keys; stale cache |

These are **AOP**. Same proxy rules as `@Transactional`.

---

## 8. Transactions (chapter 10)

```java
@Transactional
public void transfer(...) { }
```

Put it on **public** methods of **Spring beans** (typically `@Service`). Settings you must be able to explain: `propagation`, `isolation`, `readOnly`, `rollbackFor`, `timeout`.

`@Transactional` on a `@Repository` is usually redundant with Spring Data (already transactional for single repo methods) and **wrong** as the business boundary.

---

## 9. JPA (chapter 09) — recognition only

`@Entity` `@Table` `@Id` `@GeneratedValue` `@Column` `@OneToMany` `@ManyToOne` `@JoinColumn` `@Version` `@Embeddable` `@MappedSuperclass` `@NamedQuery` `@Query` `@Modifying` `@EntityGraph` `@Transactional` (service) `@CreationTimestamp` (Hibernate).

Do not put JPA annotations on REST DTOs.

---

## 10. Testing (chapter 12)

| Annotation | Loads |
|------------|-------|
| `@SpringBootTest` | Full context (heavy) |
| `@WebMvcTest` | MVC slice + MockMvc |
| `@DataJpaTest` | JPA slice + in-memory / test DB |
| `@JsonTest` | Jackson |
| `@MockBean` / `@MockitoBean` | Replace a bean in the context |
| `@ActiveProfiles("test")` | `application-test.yml` |
| `@DynamicPropertySource` | Testcontainers URLs |
| `@Test` `@BeforeEach` | JUnit 5 |

`@MockBean` is Spring Test (adds mock to **context**). `@Mock` is Mockito only (unit test, no Spring).

---

## 11. AOP (chapter 08)

`@Aspect` `@Pointcut` `@Before` `@After` `@AfterReturning` `@AfterThrowing` `@Around` `@Order`. Aspect class must be a Spring bean (`@Component`).

---

## 12. Security (chapter 11)

Boot 3: `SecurityFilterChain` `@Bean`, not `WebSecurityConfigurerAdapter`.

`@EnableWebSecurity` `@EnableMethodSecurity` `@PreAuthorize` `@PostAuthorize` `@Secured` `@RolesAllowed`.

`@CrossOrigin` is **not** authentication.

---

## 13. Decision tree

```text
Is it a class I own that should be a singleton service?
  → @Service + constructor injection

Is it HTTP?
  → @RestController + mapping annotations + @Valid on body

Is it a third-party object?
  → @Bean in @Configuration

Is it a bunch of YAML keys?
  → @ConfigurationProperties

Is it a transaction boundary?
  → @Transactional on the service method (public)

Is it optional infrastructure?
  → @ConditionalOnProperty on a @Configuration, or a profile
```

---

## 14. Production pitfalls

1. `@Autowired` on fields in new services — review comment: use constructor.
2. `@Value` for 15 related keys — use `@ConfigurationProperties`.
3. `@Transactional` + `@RestController` on the same class — mixing layers; long TX around JSON serialization.
4. Custom `@AliasFor` composed annotations that forget `@RequestMapping` aliases — mappings silently missing.
5. `@Async` on a method in the same class that the controller calls via `this`.
6. Putting `@ComponentScan` on a random config with a **narrow** package and accidentally **replacing** Boot’s scan instead of adding to it.

---

# Interview Q&A (5–8 year bar)

A fresher lists annotations. A 5–8 year answer explains **why this one**, **meta-annotations**, and **why `@Valid` on a service did nothing**.

### Q1. What does `@SpringBootApplication` combine?

**Answer:** `@SpringBootConfiguration`, `@EnableAutoConfiguration`, `@ComponentScan`.

**Counter:** Can you use the three separately?  
**Answer:** Yes. The composed form is the convention.

---

### Q2. `@Component` vs `@Service` vs `@Repository` vs `@Controller`?

**Answer:** All are stereotypes scanned as beans. `@Repository` adds persistence exception translation. `@Controller` registers MVC handlers. `@Service` is semantic (business layer).

**Counter:** Will `@Component` on a DAO still translate exceptions?  
**Answer:** Not via `PersistenceExceptionTranslationPostProcessor`, which looks for `@Repository`.

---

### Q3. `@Bean` vs `@Component`?

**Answer:** `@Component` = annotate your class, scanner instantiates. `@Bean` = factory method for objects you construct (or third-party).

**Counter:** Can `@Bean` methods live on a `@Service`?  
**Answer:** They work in lite mode; don’t. Put them on `@Configuration`.

---

### Q4. Why is constructor injection preferred over `@Autowired` fields?

**Answer:** Immutability, testability, mandatory graph. See chapter 02.

**Counter:** Is `@Autowired` on the constructor required?  
**Answer:** Not for a single constructor (Spring 4.3+).

---

### Q5. `@Qualifier` vs `@Primary`?

**Answer:** `@Primary` = default candidate. `@Qualifier` = explicit selection. Use both: primary for the common path, qualifier for the special path.

**Counter:** Two `@Primary` of the same type?  
**Answer:** Still ambiguous.

---

### Q6. `@Value` vs `@ConfigurationProperties`?

**Answer:** `@Value` = one key, SpEL, defaults. `@ConfigurationProperties` = prefix, nested objects, validation (`@Validated` + constraints), IDE metadata.

**Counter:** Can `@Value` reload on Cloud Config refresh?  
**Answer:** Not with `@ConfigurationProperties` either unless the bean is `@RefreshScope` (Spring Cloud). Both are snapshotted unless designed to refresh.

---

### Q7. `@Controller` vs `@RestController`?

**Answer:** `@RestController` = `@Controller` + `@ResponseBody`. Every method’s return value is the response body, not a view name.

**Counter:** Can you mix in one class?  
**Answer:** Use `@Controller` and put `@ResponseBody` only on API methods, or split classes.

---

### Q8. `@GetMapping` vs `@RequestMapping`?

**Answer:** `@GetMapping` is `@RequestMapping(method = GET)` plus composed aliases. Prefer verb mappings for REST.

---

### Q9. `@RequestBody` vs `@ModelAttribute` vs `@RequestParam`?

**Answer:** Body JSON/XML → `@RequestBody`. Query/form fields bound to an object → `@ModelAttribute`. Single query key → `@RequestParam`.

**Counter:** Why is my JSON body empty with `@ModelAttribute`?  
**Answer:** `@ModelAttribute` does not use `HttpMessageConverter` for JSON. Use `@RequestBody`.

---

### Q10. `@Valid` vs `@Validated`?

**Answer:** `@Valid` = Jakarta standard, cascades. `@Validated` = Spring, supports **groups**, and enables method validation when on a class.

**Counter:** `@Valid` on a service method without `@Validated` on the class?  
**Answer:** Constraints are not run.

---

### Q11. Which 10 annotations must you explain in any Boot interview?

**Answer:** `@SpringBootApplication`, `@RestController`, `@Service`, `@Repository`, `@Autowired`/`constructor`, `@Bean`, `@Configuration`, `@Transactional`, `@RequestBody`, `@Entity` — plus `@Qualifier`/`@Primary` if they ask about conflicts.

---

### Q12. What is a meta-annotation?

**Answer:** An annotation that is itself annotated, so it inherits semantics (`@RestController`). Spring’s `MergedAnnotations` API reads them.

**Counter:** Can you replace `@Service` with a custom `@UseCase` that is meta-annotated with `@Service`?  
**Answer:** Yes, if `@Service` is on your annotation with runtime retention and type target. Component scan will pick it up.

---

### Q13. `@Lazy` on an injection point vs on a class?

**Answer:** On class: bean created on first use. On injection point: that collaborator is a lazy proxy; the target bean may still be eager for other injection points.

---

### Q14. `@DependsOn` — when is it actually needed?

**Answer:** When there is a **side-effect** dependency (static init, driver registration) that injection does not express. If you already inject B into A, A already depends on B.

---

### Q15. Why did `@Async` / `@Transactional` / `@Cacheable` not run?

**Answer:** Not a Spring bean, self-invocation, non-public method, or `final` method with CGLIB. Same proxy story (chapters 02 and 08).
