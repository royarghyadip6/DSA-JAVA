# Core Spring — Course Map

**Spring version in these notes:** Spring Framework 6 / Jakarta EE (`jakarta.*`)  
**Spring 5 / `javax.*`:** mentioned only where interviewers still ask

---

## How these notes are written

Each chapter has **two layers on purpose**:

| Layer | Who it is for | What it feels like |
|-------|----------------|-------------------|
| Teaching sections (the notes) | A **new learner** can follow | Short sentences, pictures, kitchen/restaurant analogies, words explained when they first appear |
| **Interview Ready Q&A** at the end | **5–8 years** interview | Full answers + **counter-questions** (what the interviewer asks next) |

**How to study**

1. Read the teaching part slowly. If a word is new, the note explains it in the same section.
2. Ignore the interview section on the first pass.
3. After you understand the picture, read **Interview Ready Q&A**. Those answers are meant to be *spoken in an interview*, not learned as the first explanation.

You do **not** need 5 years of experience to understand the notes. You **do** need that depth to answer the Q&A well.

---

## What “Core Spring” means here (simple)

Think of a car:

- **Core Spring** = the **engine** (how objects are created, connected, wrapped, and how a web request is handled)
- **Spring Boot** = the **factory that ships a ready car** (auto-config, starters, embedded server)
- **Spring Data / Hibernate / Security** = extra systems (database mapping, login)

This course is only the engine.

```text
Core Spring (this course)
  IoC + beans + config + AOP + MVC + JDBC/tx + test + async/cache

Later (do not mix while studying Core)
  057 Spring Boot
  058 REST API design
  059 Exception handling in Boot
  060 Spring Data JPA
  061 Hibernate internals
  062 Transactions (Hibernate / locking / Saga)
  066 Spring Security
```

**Memory trick:** Core Spring = “how the engine works.” Boot/Data/Security = “how products are assembled on top of the engine.”

---

## Words you will see everywhere

| Word | Simple meaning |
|------|----------------|
| **Bean** | An object Spring creates and manages for you |
| **Container / ApplicationContext** | The “manager” that creates beans and connects them |
| **Dependency** | Another object your class needs to do its job |
| **Inject** | Spring *gives* that object to your class (you do not `new` it) |
| **Proxy** | A wrapper around your bean so extra work can run (transaction, cache, …) |

---

## Reading order

| # | File | Simple takeaway |
|---|------|-----------------|
| 1 | [054_Spring_Framework_Fundamentals.md](054_Spring_Framework_Fundamentals.md) | Spring creates objects and plugs them together |
| 2 | [054_1_IoC_Container_Internals.md](054_1_IoC_Container_Internals.md) | Recipe first, then cooking; how two beans can wait for each other |
| 3 | [054_2_Bean_Lifecycle_and_Scopes.md](054_2_Bean_Lifecycle_and_Scopes.md) | Birth → work → death of a bean; how many copies exist |
| 4 | [055_Spring_Annotations.md](055_Spring_Annotations.md) | How you *tell* Spring which objects exist and which one to pick |
| 5 | [056_Spring_AOP.md](056_Spring_AOP.md) | Extra behavior around methods using a wrapper (proxy) |
| 6 | [056_1_Events_SpEL_Resources.md](056_1_Events_SpEL_Resources.md) | Messages inside the app, expressions, files, converting types, i18n |
| 7 | [056_2_Spring_MVC_Internals.md](056_2_Spring_MVC_Internals.md) | What happens when a browser hits your URL |
| 8 | [056_3_Jdbc_and_Transaction_Abstraction.md](056_3_Jdbc_and_Transaction_Abstraction.md) | SQL helper + “all or nothing” database work |
| 9 | [056_4_Spring_Testing.md](056_4_Spring_Testing.md) | When to use `new`, when to start Spring, how MockMvc works |
| 10 | [056_5_Async_Scheduling_Cache.md](056_5_Async_Scheduling_Cache.md) | Background work, timers, and method-level cache (same proxy idea) |

```text
054 Fundamentals
    → 054_1 Container internals
        → 054_2 Lifecycle and scopes
            → 055 Config and annotations
                → 056 AOP
                    → 056_1 Events / SpEL / Resources
                    → 056_5 Async / Scheduling / Cache
                → 056_2 MVC internals
                    → 056_3 JDBC + tx abstraction
                        → 056_4 Testing
```

After AOP, `056_5` and `056_1` can be read in either order — both use ideas from AOP.

---

## Interview section contract (do this *after* the notes)

Each chapter ends with about **15–25** questions. Every question has:

- a full answer (definition + how Spring does it + what breaks)
- a **counter-question** (the follow-up)
- a counter-answer

A good 5–8 year answer usually has three layers:

```text
1. Definition in one sentence
2. How Spring implements it
3. What breaks in production / what you would not do
```

If you cannot answer the counter-question, the first answer was still junior.

---

## One-liner for the whole course

> Core Spring is a manager that creates objects (beans), plugs them together (DI), can wrap them (proxy / AOP), and in a web app sends HTTP requests to the right method (DispatcherServlet). Boot and the rest of the Spring family sit on top of that.
