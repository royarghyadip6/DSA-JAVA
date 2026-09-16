# 55. Spring Configuration and Annotations

[← 054_2 Lifecycle and Scopes](054_2_Bean_Lifecycle_and_Scopes.md) | [Course map](00_COURSE_MAP.md) | **Next:** [056 AOP →](056_Spring_AOP.md)

Teaching is simple first. **Interview Q&A at the end is 5–8 year standard.**

---

## Simple first

You have to **tell Spring which objects exist**. There are three common ways:

| Way | You write | Everyday meaning |
|-----|-----------|------------------|
| **Scan** | `@Service` on *your* class | “Spring, find my classes in this package.” |
| **Factory method** | `@Bean` method that `return new RestTemplate()` | “I will build this object; you manage it.” Used for library classes you cannot sticker. |
| **Import** | `@Import(MailConfig.class)` | “Also load that other recipe book.” |

**Wiring** = picking *which* bean to plug in when there are two of the same type.

```text
Spring looks at the type (PaymentGateway)
  → if two exist, look at @Qualifier (a name tag)
  → if no tag, look at @Primary (the default)
  → if still confused, the app **fails at startup** (this is good)
```

**`${...}` vs `#{...}`**

- `${app.port}` = read a **property** (from a file / env var)
- `#{...}` = a small **formula** (SpEL — next-but-one chapter)

**Full vs lite `@Bean` (the beginner trap):** if the class is `@Configuration`, calling another `@Bean` method still goes through Spring (one object). If the class is only `@Component`, a normal Java call runs — you can accidentally create a **second** object that Spring does not know about.

---

## When you interview (5–8 years)

Annotations are not a cheat sheet. The real topic is **how Spring learns about beans** and **how it chooses among them**.

They will ask: `@Bean` inside `@Component` creating two `RestTemplate`s; `@Import` vs scan; `@Qualifier` vs `@Primary`; `@Conditional` vs `@Profile`; `@Value` vs `Environment`.

---

## 1. Three configuration styles

| Style | How | Use now |
|-------|-----|---------|
| XML | `<bean class="..."/>` | Legacy, some namespaces |
| Annotation scan | `@ComponentScan` + `@Service` | Your application classes |
| Java config | `@Configuration` + `@Bean` | Third-party objects, explicit wiring, imports |

They compose. A `@SpringBootApplication` (later course) is `@Configuration` + `@ComponentScan` + `@EnableAutoConfiguration`. In Core you write:

```java
@Configuration
@ComponentScan("com.app")
@PropertySource("classpath:app.properties")
@Import(MailConfig.class)
public class AppConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
```

```java
ApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class);
```

### What `@ComponentScan` actually does

- Walks the base package
- Picks up `@Component` / `@Service` / `@Repository` / `@Controller` / `@Configuration` / custom meta-annotations
- Filters: `includeFilters`, `excludeFilters` (`ASSIGNABLE_TYPE`, `ANNOTATION`, `REGEX`, `ASPECTJ`, `CUSTOM`)
- Spring 5.3+ indexed scan (`spring.components` file) speeds large apps

Default base package if you pass a `@Configuration` class to `AnnotationConfigApplicationContext`: **that class’s package**, not the whole classpath.

Scanning the whole `com` or a library package is how you accidentally create duplicate beans.

---

## 2. `@Configuration` full mode vs lite `@Bean`

This is the highest-value annotation question. **In simple words first:**

- `@Configuration` = Spring **subclasses** your config class. When one `@Bean` method calls another, that call is redirected to the container → **one** object.
- Only `@Component` + `@Bean` = a normal Java call → **two** objects possible.

You can avoid the trap entirely: do not call `@Bean` methods on `this`. Inject the other bean as a **method parameter**.

### Full mode

Class annotated `@Configuration`. Spring CGLIB-subclasses it. Calls to `@Bean` methods are intercepted → `getBean` → **one singleton**.

```java
@Configuration
public class HttpConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate(requestFactory());
    }

    @Bean
    public ClientHttpRequestFactory requestFactory() {
        return new HttpComponentsClientHttpRequestFactory();
    }
}
```

`restTemplate()` calling `requestFactory()` hits the **container**, not a raw Java call. One factory bean.

`@Configuration(proxyBeanMethods = true)` is the default (Spring 5.2 named the flag). `proxyBeanMethods = false` = lite-like, faster, no CGLIB config subclass — you must not call `@Bean` methods directly; inject parameters instead:

```java
@Bean
public RestTemplate restTemplate(ClientHttpRequestFactory factory) {
    return new RestTemplate(factory);
}
```

That parameter style is the modern recommendation even in full mode (clearer, AOT-friendly).

### Lite mode

`@Bean` methods on a class that is **only** `@Component` (or a non-annotated class registered somehow) — **not** `@Configuration`.

```java
@Component
public class HttpConfigLite {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate(requestFactory()); // RAW CALL
    }

    @Bean
    public ClientHttpRequestFactory requestFactory() {
        return new HttpComponentsClientHttpRequestFactory();
    }
}
```

`requestFactory()` is invoked twice: once as `@Bean`, once from `restTemplate()`. Two factories. The `RestTemplate`’s factory is **not** the singleton in the container.

**Memory trick:** `@Configuration` = container intercepts `@Bean` calls. `@Component` + `@Bean` = plain Java.

`@Configuration` is itself a `@Component`, so it is scanned. You do not also put `@Component` on it.

Final classes cannot be `@Configuration` in full mode (cannot subclass). `proxyBeanMethods = false` or inject-by-parameter.

---

## 3. `@Import`, `ImportSelector`, `ImportBeanDefinitionRegistrar`

| Mechanism | What you import |
|-----------|-----------------|
| `@Import(MailConfig.class)` | Another `@Configuration` / `@Component` |
| `@Import(SomeSelector.class)` | `ImportSelector` — returns class names to import (can be conditional) |
| `@Import(SomeRegistrar.class)` | `ImportBeanDefinitionRegistrar` — programmatic `BeanDefinition` registration |

```java
public class MonitoringSelector implements ImportSelector {
    @Override
    public String[] selectImports(AnnotationMetadata metadata) {
        return new String[] { MetricsConfig.class.getName() };
    }
}

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(MonitoringSelector.class)
public @interface EnableMonitoring { }
```

This is how `@Enable*` works in Core: `@EnableTransactionManagement`, `@EnableAsync`, `@EnableWebMvc`, `@EnableCaching` are `@Import` of selectors/registrars.

`DeferredImportSelector` is what Boot auto-config uses (run after user configs). Know the name; details are Boot.

`ImportBeanDefinitionRegistrar` is how `@MapperScan` and similar libraries register beans you did not annotate.

---

## 4. Wiring: `@Autowired`, `@Qualifier`, `@Primary`, `@Resource`

### `@Autowired`

- Inject **by type**
- Places: constructor, setter, field, method parameters, `@Bean` method parameters
- `required = true` by default → missing bean fails refresh
- Processed by `AutowiredAnnotationBeanPostProcessor`

Single constructor: annotation optional (Spring 4.3+).

### Resolution order when several beans match a type

```text
1. Collect candidates of that type (autowireCandidate = true)
2. If @Qualifier (or @Qualifier-composing annotation) on injection point
      → keep names / qualifier values that match
3. Else if parameter/field name matches a bean name
      (and compilation retained names, or @Qualifier implicit in some versions)
      → may match by name  — do not rely on this in production
4. Else if one @Primary among remaining
      → use it
5. Else if exactly one candidate
      → use it
6. Else
      → NoUniqueBeanDefinitionException
```

`@Qualifier` **beats** `@Primary`.

```java
@Service
@Primary
public class StripeGateway implements PaymentGateway { }

@Service
@Qualifier("paypal")
public class PayPalGateway implements PaymentGateway { }

@Service
public class CheckoutService {
    public CheckoutService(
            @Qualifier("paypal") PaymentGateway gateway) {
        // PayPal, not Stripe
    }
}
```

Custom annotation:

```java
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface Paypal { }

@Service
@Paypal
public class PayPalGateway implements PaymentGateway { }

public CheckoutService(@Paypal PaymentGateway gateway) { }
```

### `@Resource` (Jakarta)

- JSR-250 / Jakarta annotation
- Default: inject **by name** (field name or `@Resource(name = "beanName")`), then by type
- No `required` flag like `@Autowired`
- Useful when you think in names; Spring teams usually stay on `@Autowired` + `@Qualifier` for consistency

### `@Inject` (Jakarta Inject)

Almost `@Autowired` (JSR-330). `Optional` handling differs slightly. In Spring shops, `@Autowired` is the common dialect.

### Injecting all implementations

```java
public ReportService(List<Notifier> notifiers) { }           // all, including @Primary
public ReportService(Map<String, Notifier> notifiers) { }    // name → bean
public ReportService(ObjectProvider<Notifier> notifiers) { } // lazy stream
```

`@Order` / `Ordered` on the beans controls `List` order.

`@Autowired(required = false) List<Notifier>` → empty list if none, not fail.

---

## 5. `@Value`, Environment, `@PropertySource`

```java
@Value("${app.mail.host}")
String host;

@Value("${app.mail.port:587}")
int port;

@Value("#{systemProperties['user.name']}")
String osUser;
```

| Syntax | Engine |
|--------|--------|
| `${...}` | Placeholder → `Environment` / property sources |
| `#{...}` | SpEL ([056_1](056_1_Events_SpEL_Resources.md)) |

`PropertySourcesPlaceholderConfigurer` / `EmbeddedValueResolver` must be registered. `AnnotationConfigApplicationContext` and Boot do this. A bare `DefaultListableBeanFactory` does not resolve `${}` unless you add the BFPP.

**`Environment`** is the typed API:

```java
@Service
public class MailProps {
    public MailProps(Environment env) {
        String host = env.getProperty("app.mail.host");
        int port = env.getProperty("app.mail.port", Integer.class, 587);
    }
}
```

`@PropertySource("classpath:mail.properties")` on a `@Configuration` adds a file. It does **not** replace `application.properties` (that is Boot). You can use `${}` inside the location: `@PropertySource("classpath:mail-${spring.profiles.active}.properties")` — fragile if multiple profiles.

**`@Value` vs grouped config:** `@Value` is fine for a few keys. A bag of related keys belongs in a dedicated `@Configuration` bean you construct yourself, or later Boot `@ConfigurationProperties` (type-safe, validated). At 5–8 YOE, say: “`@Value` does not bind hierarchical YAML to an object graph well; that’s why Boot added `@ConfigurationProperties`.”

Property source **priority** (Framework): programmatic `Environment` > system properties > env vars > `@PropertySource` (order of declaration). Boot adds its own longer list (command line, `application-{profile}.yml`, …). For Core interviews, “Environment is an ordered list of PropertySources; first match wins.”

---

## 6. `@Profile`

A bean or `@Configuration` is registered **only** if the profile is active.

```java
@Configuration
@Profile("dev")
public class DevMailConfig {
    @Bean
    JavaMailSender sender() { return new MockMailSender(); }
}

@Configuration
@Profile("prod")
public class ProdMailConfig {
    @Bean
    JavaMailSender sender() { return realSender(); }
}
```

Activate: `ctx.getEnvironment().setActiveProfiles("dev")` before refresh, or `spring.profiles.active=dev`.

Expressions (Spring 5.1+ / 4.x `Profile`): `@Profile("dev | local")`, `!prod`.

`@Profile` is implemented as a **`@Conditional`** (`ProfileCondition`). It is a special case of the general engine below.

---

## 7. `@Conditional` (Framework, not Boot)

```java
public class OnLinuxCondition implements Condition {
    @Override
    public boolean matches(ConditionContext ctx, AnnotatedTypeMetadata md) {
        return ctx.getEnvironment().getProperty("os.name", "")
                .toLowerCase().contains("linux");
    }
}

@Bean
@Conditional(OnLinuxCondition.class)
public WatchService linuxWatcher() { ... }
```

`ConditionContext` gives `Environment`, `BeanDefinitionRegistry`, `ClassLoader`.

**Do not** call `getBean()` inside `matches` — beans may not exist; you can only look at **definitions** and the Environment.

Boot annotations (`@ConditionalOnClass`, `@ConditionalOnMissingBean`, `@ConditionalOnProperty`) are conditions on this same SPI. If asked “how auto-config decides,” the Core answer is: **`@Conditional` + `DeferredImportSelector`**.

`@Conditional` on a `@Configuration` class skips **all** its `@Bean` methods.

---

## 8. `@Lazy`, `@DependsOn`, `@Scope`, `@Primary` recap

| Annotation | Role |
|------------|------|
| `@Lazy` on class/`@Bean` | Singleton created on first use |
| `@Lazy` on injection point | Inject a proxy; real getBean on first method |
| `@DependsOn` | Init **order** only, no injection |
| `@Scope` | singleton / prototype / web / custom |
| `@Primary` | Default candidate among many of one type |

`@Lazy` on a class that is the *only* dependency of an eager singleton still starts at startup — unless the injection point is also lazy.

`@DependsOn` is for side-effect beans (static init, register JDBC driver). If you can inject, inject.

---

## 9. Meta-annotations and composed annotations

Spring annotations are often **meta-annotated**. `@Service` includes `@Component`. Your own:

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Service
@Transactional
public @interface ApplicationService { }
```

Scan sees `@Component` through the meta-annotation. This is how you keep stereotypes consistent.

`@AliasFor` (Spring 4.2+) maps attributes of composed annotations to the inner ones. `@SpringBootApplication` uses this heavily.

---

## 10. XML leftovers you should still recognize

```xml
<beans>
  <context:component-scan base-package="com.app"/>
  <context:property-placeholder location="classpath:app.properties"/>
  <bean id="clock" class="java.time.Clock" factory-method="systemUTC"/>
</beans>
```

`ClassPathXmlApplicationContext` vs `AnnotationConfigApplicationContext`. Mixing: `@ImportResource("classpath:legacy.xml")` on a Java config class.

---

## Production pitfalls

1. **Lite `@Bean` + method calls** — extra instances, not in the graph.
2. **Scan too wide** — duplicate beans from test configs or libraries.
3. **Relying on parameter names** without `-parameters` compiler flag — breaks in CI.
4. **`@Autowired` on two beans of same type with no qualifier** — works on your machine because of `@Primary` you forgot you added.
5. **`@Value` for 15 related keys** — no validation, typos at runtime.
6. **`@Profile("dev")` on a class and forgetting prod counterpart** — empty `JavaMailSender` in prod.
7. **Condition that calls `getBean`** — random startup failures.
8. **`@Resource` by field name** after a rename — silent wrong bean if types still match poorly.

---

## Interview Ready Q&A (5–8 year standard)

The notes explained stickers and recipe books. **Here, talk resolution order, full vs lite mode, ImportSelector, and `@Conditional`.**

### Q1. `@Component` vs `@Bean`?

**Answer:** `@Component` (and stereotypes) mark **your** class for scanning; Spring calls the constructor. `@Bean` is a **factory method** that returns an object, used for third-party types or when creation needs code (builders, conditionals).

**Counter:** Can you `@Bean` a class that is also `@Component`?

**Counter-answer:** Yes, and you get **two** beans unless you exclude one. That is a common duplicate-`DataSource` story. Pick one registration path.

---

### Q2. Why is constructor injection preferred, and do you need `@Autowired`?

**Answer:** Required deps, `final` fields, tests, fail-fast. No `@Autowired` if there is a single constructor (4.3+).

**Counter:** Two constructors, one for JPA (`protected` no-arg) and one for Spring?

**Counter-answer:** Put `@Autowired` on the injecting constructor. Keep the no-arg for the persistence provider. Do not make the no-arg `public` if you can avoid it.

---

### Q3. Full `@Configuration` vs lite `@Bean`?

**Answer:** `@Configuration` CGLIB proxy intercepts `@Bean` methods so internal calls go to the container (one singleton). Lite (`@Bean` on `@Component`) is a raw call — extra instances. `proxyBeanMethods = false` is explicit lite-like; inject `@Bean` products as method parameters instead of calling methods.

**Counter:** Why does Boot use `proxyBeanMethods = false` on many auto-configs?

**Counter-answer:** Startup cost (no CGLIB subclass per config) and AOT. Auto-config authors inject parameters and never call `@Bean` methods on `this`. User application configs often still use full mode. Either is correct if you do not call `@Bean` methods directly.

---

### Q4. How does Spring resolve multiple beans of one type?

**Answer:** Qualifier on the injection point → (optionally) parameter name → `@Primary` → exactly one left → else `NoUniqueBeanDefinitionException`. You can also inject `List`/`Map`/`ObjectProvider`.

**Counter:** `@Primary` on two beans?

**Counter-answer:** Still ambiguous. `@Primary` must be unique among the remaining candidates.

---

### Q5. `@Qualifier` vs `@Primary`?

**Answer:** `@Primary` = default when the injection point says nothing. `@Qualifier` = explicit choice; wins over primary.

**Counter:** Should libraries mark a bean `@Primary`?

**Counter-answer:** Dangerous — they steal the default from the application. Prefer unique types or qualifiers. `@Primary` belongs to **application** config (“this is *our* DataSource”).

---

### Q6. `@Autowired` vs `@Resource` vs `@Inject`?

**Answer:** `@Autowired` = Spring, by type, `required` flag, supports `List`. `@Resource` = by name first. `@Inject` = JSR-330, similar to `@Autowired`. Stay consistent; Spring codebases use `@Autowired`.

**Counter:** Will `@Resource(name = "x")` use `@Primary` if `x` is missing?

**Counter-answer:** Name lookup fails first. It does not fall back to `@Primary` the way unmatched `@Autowired` might. Missing name → exception.

---

### Q7. What is `@Import` for?

**Answer:** Pull in other configuration classes without scanning them. Also the implementation of `@Enable*` via `ImportSelector` / `ImportBeanDefinitionRegistrar`.

**Counter:** Why not always `@ComponentScan` the library package?

**Counter-answer:** You would pick up internal `@Component`s you do not want, or nothing if the library used `@Configuration` in a package you do not scan. `@EnableFoo` + `@Import` is the public switch.

---

### Q8. `ImportSelector` vs `ImportBeanDefinitionRegistrar`?

**Answer:** Selector returns class names to import as configurations/components. Registrar gets a `BeanDefinitionRegistry` and can register arbitrary definitions (dynamic names, scanned mappers).

**Counter:** When is `DeferredImportSelector` needed?

**Counter-answer:** When imports must run **after** user beans are registered so conditions like `@ConditionalOnMissingBean` see them. That is Boot auto-config. Core `@EnableAsync` is a normal import, not deferred.

---

### Q9. `@Value` vs `Environment`?

**Answer:** `@Value` injects one placeholder or SpEL into a field/param. `Environment` is the API to query properties, profiles, and typed conversion. `@Value` is implemented **using** the Environment (and SpEL).

**Counter:** `@Value("${missing}")` with no default?

**Counter-answer:** Context fails (placeholder resolution exception) unless you configured ignore-unresolvable. Prefer defaults or `env.getProperty` with a default for optional keys.

---

### Q10. `@Profile` vs `@Conditional`?

**Answer:** `@Profile` is a condition on active profiles. `@Conditional` is the general SPI (any `Condition` implementation). Profiles are conditions; not all conditions are profiles.

**Counter:** Can you replace `@Profile("dev")` with a condition?

**Counter-answer:** Yes — `ProfileCondition` already does. Custom conditions can mix profiles, class presence, and properties. Prefer `@Profile` when the only axis is environment name — it is obvious in ops.

---

### Q11. How do you activate profiles in Core (no Boot)?

**Answer:** `environment.setActiveProfiles("dev")` before `refresh()`, or system property / env `spring.profiles.active`. `ConfigurableEnvironment`.

**Counter:** Two active profiles `dev,mysql`. What beans start?

**Counter-answer:** Any bean whose `@Profile` matches **at least one** (OR), unless you used a more specific expression. A bean with no `@Profile` always starts. Conflicting `@Bean` methods of the same name in two matching configs → last-wins or override rules depending on `allowBeanDefinitionOverriding`.

---

### Q12. What is `allowBeanDefinitionOverriding`?

**Answer:** If two definitions share a name, later registration overrides the earlier when this flag is true (historical default). Boot 2.1+ defaulted it to **false** — duplicate names fail fast.

**Counter:** Is overriding a good way to replace a library bean?

**Counter-answer:** Better: `@ConditionalOnMissingBean` (Boot) or a different name + `@Primary`, or exclude the auto-config. Name override is easy to do by accident with scan.

---

### Q13. `@Lazy` on class vs on constructor parameter?

**Answer:** Class/`@Bean`: that singleton is not created in `preInstantiateSingletons`. Parameter: inject a proxy; the target is created on first use (and this can break constructor cycles).

**Counter:** Lazy bean injected into eager bean without `@Lazy` on the param?

**Counter-answer:** Eager creation pulls the lazy bean immediately. Lazy is skipped.

---

### Q14. `@DependsOn` vs injection?

**Answer:** `@DependsOn` only orders initialization. Injection creates a real dependency and also orders. Prefer injection.

**Counter:** Example where `@DependsOn` is justified?

**Counter-answer:** Bean A must run static registration (license, JDBC driver, logging) before bean B constructs, but B does not need a reference to A. Rare.

---

### Q15. How does `@ComponentScan` decide the base package?

**Answer:** Explicit `basePackages` / `basePackageClasses`, or the package of the class that declares `@ComponentScan`.

**Counter:** Why did my `com.app.api` test pick up `com.app.batch` jobs?

**Counter-answer:** Scan from `com.app` includes both. Narrow the scan or use filters. In tests, import a slice config instead of the whole app ([056_4](056_4_Spring_Testing.md)).

---

### Q16. `@Autowired` on a `List<Foo>` — order and empty list?

**Answer:** All `Foo` beans, ordered by `@Order`/`Ordered`/`@Priority`. If required and none exist → fail. `required = false` → empty list (or use `ObjectProvider.orderedStream()`).

**Counter:** Does the list include the `@Primary` only?

**Counter-answer:** No. Collections get **all** candidates. `@Primary` is for injecting a **single** `Foo`.

---

### Q17. What does `@Qualifier` on a `@Bean` method mean?

**Answer:** It attaches a qualifier to the produced bean (in addition to its name). Injection points with the same qualifier match.

**Counter:** Bean name vs qualifier — are they the same?

**Counter-answer:** Default qualifier is often the bean name, but `@Qualifier("special")` can differ from the method name. Do not assume. Prefer explicit `@Qualifier` on both sides.

---

### Q18. How do `@EnableTransactionManagement` and friends work without Boot?

**Answer:** They are meta-annotated with `@Import` of a selector/registrar that registers advisors, `TransactionInterceptor`, proxy creators. You still must provide a `PlatformTransactionManager` `@Bean`. See [056_3](056_3_Jdbc_and_Transaction_Abstraction.md).

**Counter:** If I forget `@EnableTransactionManagement`, does `@Transactional` still work?

**Counter-answer:** No in plain Framework. Boot’s auto-config enables it for you. Core interview: name the `@Enable*` switch.

---

### Q19. `@Value` with a list?

**Answer:** Comma-separated `${app.roles}` into `String[]` or `List` works with conversion. Nested maps/YAML trees are painful. That limitation is why Boot `@ConfigurationProperties` exists.

**Counter:** SpEL `#{'${app.roles}'.split(',')}` — good idea?

**Counter-answer:** It works; it is noisy and error-prone. Prefer conversion or a dedicated properties object.

---

### Q20. `proxyBeanMethods = false` and calling `this.otherBean()`?

**Answer:** You get a plain Java instance, not the container singleton. Possible double `@PreDestroy`, missing AOP, broken identity.

**Counter:** How should those configs be written?

**Counter-answer:** `@Bean B b(A a)` — parameter injection. Never `this.a()`.

---

### Q21. Can `@Autowired` inject `Optional<Foo>`?

**Answer:** Yes (Spring 4.1+). Empty if no bean. Cleaner than `required = false` + null.

**Counter:** `Optional` of a collection?

**Counter-answer:** Prefer `List<Foo>` with `required = false` or `ObjectProvider`. `Optional<List<Foo>>` is awkward and uncommon.

---

### Q22. What is a composed annotation and why write one?

**Answer:** An annotation that meta-annotates Spring annotations (`@Service` + `@Transactional`). One import in app code, consistent defaults (rollback, timeout).

**Counter:** Does custom `@ApplicationService` get picked up by scan?

**Counter-answer:** Yes if it is meta-annotated with `@Component`/`@Service`. Spring searches meta-annotations.

---

### Interview one-liner

> Scan your classes; `@Bean` third-party types; `@Import` for `@Enable*`. `@Configuration` intercepts `@Bean` methods; lite mode does not. Injection is by type, then `@Qualifier`, then `@Primary`. `@Profile` is one `@Conditional`. `@Value` reads Environment; do not use it for large config objects.
