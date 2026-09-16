# Spring Boot + Maven — Notes

These notes teach **Spring Boot and Maven from zero**, in simple English.  
At the **end of every chapter** you will find **interview Q&A written for a 5–8 year Java backend round** (including counter questions). Learn simply. Answer like a senior.

---

## How to use this folder

1. Read **Start here** at the top of each chapter. That part is for a fresher.
2. Then read the rest of the chapter. Same ideas, more detail.
3. At the end, **say the Q&A out loud**. Counter questions are what interviewers ask after your first answer.
4. Read in order. Later chapters assume earlier ones (especially **bean**, **proxy**, **auto-config**).

This folder is **self-contained**. You do not need the other `StudyNotes/Spring` files to finish it.

---

## Two speeds in every file

| Part of the chapter | Who it is for |
|---------------------|----------------|
| **Start here** + everyday picture | First-time learner |
| Middle sections (how it works, pitfalls) | After you know the words |
| **Interview Q&A** | 5–8 year interview bar — do not water these down |

You can understand the concept on day 1. You still practise the Q&A until the answers are production-grade.

---

## Learning order

| # | File | Simple idea | You also learn (for interviews) |
|---|------|-------------|----------------------------------|
| 00 | [00_Maven.md](00_Maven.md) | Maven builds the project and downloads libraries | BOM, nearest-wins, fat JAR |
| 01 | [01_SpringBoot_Architecture.md](01_SpringBoot_Architecture.md) | Boot starts a full app with little config | `SpringApplication.run()`, JAR layout |
| 02 | [02_IoC_DI_BeanLifecycle.md](02_IoC_DI_BeanLifecycle.md) | Spring creates objects for you | Lifecycle, circular deps, proxies |
| 03 | [03_Annotations.md](03_Annotations.md) | Annotations are labels Spring reads | Which one to pick, and why |
| 04 | [04_Configuration.md](04_Configuration.md) | Settings live outside code | Property order, profiles, secrets |
| 05 | [05_AutoConfiguration.md](05_AutoConfiguration.md) | Boot wires beans if the library is present | Conditions, custom starter |
| 06 | [06_SpringMVC_REST.md](06_SpringMVC_REST.md) | HTTP in → Java method → JSON out | DispatcherServlet, converters |
| 07 | [07_ExceptionHandling_Validation.md](07_ExceptionHandling_Validation.md) | Bad input and crashes become clean API errors | `@Valid`, Problem Details |
| 08 | [08_AOP_Proxies.md](08_AOP_Proxies.md) | Extra behaviour around a method without changing it | Self-invocation, JDK vs CGLIB |
| 09 | [09_SpringDataJPA.md](09_SpringDataJPA.md) | Java objects ↔ database rows | N+1, OSIV, Flyway |
| 10 | [10_Transactions.md](10_Transactions.md) | All DB steps succeed together, or none do | Propagation, rollback rules |
| 11 | [11_SpringSecurity.md](11_SpringSecurity.md) | Who are you, and what may you do? | Filter chain, JWT vs CSRF |
| 12 | [12_Testing.md](12_Testing.md) | Prove the app works, as cheaply as possible | Slices, Testcontainers |
| 13 | [13_Actuator_Observability.md](13_Actuator_Observability.md) | Is the app healthy? Is it slow? | Liveness vs readiness |
| 14 | [14_Caching_Async_Scheduling.md](14_Caching_Async_Scheduling.md) | Remember, do later, run on a clock | Executors, clustered jobs |
| 15 | [15_Production_Ready.md](15_Production_Ready.md) | Ship it without 2 a.m. surprises | Shutdown, Hikari, Docker layers |

---

## Picture of one HTTP request (keep this)

A user clicks in a browser. The request walks this path:

```text
Browser / mobile app
  → Tomcat (the HTTP server inside your JAR)
    → Security filters (login / JWT)
      → DispatcherServlet (Spring MVC traffic cop)
        → Controller (maps URL to a Java method)
          → Service (business rules; often wrapped in a proxy)
            → Repository (talks to the database)
              → HikariCP (connection pool)
                → Database
```

**Interview hint:** if `@Transactional` or `@Async` “did nothing”, ask: did the call go through the **proxy** Spring created, or through `this` (the raw object)?

---

## Words you will see a lot

| Word | Simple meaning |
|------|----------------|
| **JAR** | A zip file of Java classes |
| **Classpath** | The list of JARs the JVM can see |
| **Bean** | An object Spring created and manages |
| **Container / ApplicationContext** | Spring’s “factory + warehouse” of beans |
| **Proxy** | A wrapper around a bean that can run extra code (transaction, cache, security) |
| **Auto-configuration** | Boot creating common beans for you because a library is on the classpath |
| **Profile** | A named set of settings (`dev`, `prod`) |
