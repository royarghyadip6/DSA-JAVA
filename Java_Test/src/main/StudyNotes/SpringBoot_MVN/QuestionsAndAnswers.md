# 🎯 SpringBoot FULL ROADMAP 

1. [Spring Boot Basics](#1)
2. [Spring Core (IoC, DI)](#2))
3. [Annotations](#3)
4. [Configuration](#4)
5. [REST APIs](#5)
6. [Exception Handling](#6)
7. [Spring Data JPA](#7)
8. [Security](#8)
9. [Testing](#9)
10. [Actuator](#10)

---
# 🧠 TOPIC 1: Introduction to Spring Boot Basics – Answer Set
<a id="1"> </a>
---

## ✅ 1. What is Spring Boot?

> **Spring Boot** is a framework built on top of Spring that simplifies application development by providing auto-configuration, embedded servers, and ready-to-use features.

## ✅ 2. Why do we use Spring Boot instead of Spring?

👉 Because it:

* Removes complex XML configuration
* Provides auto-configuration
* Includes embedded servers
* Speeds up development

## ✅ 3. What are the main features of Spring Boot?

* Auto-configuration
* Starter dependencies
* Embedded server
* Production-ready features (Actuator)
* Minimal configuration

## ✅ 4. What is embedded server in Spring Boot?

> An embedded server (like **Apache Tomcat**) is built into the application, so no external deployment is required.

## ✅ 5. What is auto-configuration? How does it work?

👉 Auto-configuration automatically configures beans based on:

* Classpath (dependencies present)
* Properties file
* Existing beans

👉 Internally uses:

* `@Conditional` annotations
* `@EnableAutoConfiguration`

## ✅ 6. What is `@SpringBootApplication`?

👉 It is a combination of:

* `@Configuration`
* `@EnableAutoConfiguration`
* `@ComponentScan`

👉 Used as the main entry point.

## ✅ 7. What are starter dependencies?

👉 Predefined dependency bundles like:

* `spring-boot-starter-web`
* `spring-boot-starter-data-jpa`

👉 They reduce manual dependency management.

## ✅ 8. How does Spring Boot reduce boilerplate code?

* Auto-configuration
* Starter dependencies
* Annotation-based config
* No XML setup

## ✅ 9. Can we disable auto-configuration? How?

👉 Yes:

### Option 1:

```java
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
```

### Option 2:

```properties
spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
```

## ✅ 10. What happens internally when you run a Spring Boot application?

👉 Flow:

1. `main()` method runs
2. `SpringApplication.run()` is called
3. Spring Boot starts **ApplicationContext**
4. Performs **component scanning**
5. Applies **auto-configuration**
6. Creates and initializes beans
7. Starts embedded server
8. Application becomes ready

---

## 🔥 Final Interview Tip

If interviewer asks this set together:

👉 Give this **one-liner summary**:

> Spring Boot simplifies Spring development using auto-configuration, embedded servers, and starter dependencies, reducing manual setup and boilerplate code.


---
# 🧠 TOPIC 2 : Spring Core (IoC & DI) – Answer Set
<a id="2"> </a>
---

## ✅ 1. What is IoC?

👉 **Inversion of Control (IoC)** means the control of object creation and management is transferred from the developer to the **Spring Framework container**.

## ✅ 2. What is Dependency Injection?

👉 Dependency Injection (DI) is a technique where dependencies are **injected into a class** instead of the class creating them.

## ✅ 3. What is a Spring Bean?

👉 A **Spring Bean** is an object that is **created, managed, and initialized by the Spring container**.

## ✅ 4. What is ApplicationContext?

👉 `ApplicationContext` is the **advanced Spring container** that:

* Creates beans
* Manages lifecycle
* Provides additional features (events, AOP, i18n)

## ✅ 5. Difference between BeanFactory and ApplicationContext?

| Feature        | BeanFactory | ApplicationContext |
| -------------- | ----------- | ------------------ |
| Initialization | Lazy        | Eager (default)    |
| Features       | Basic       | Advanced           |
| Use            | Rare        | Mostly used        |

👉 `ApplicationContext` is preferred in modern apps.

## ✅ 6. Types of Dependency Injection?

* Constructor Injection ✅ (best practice)
* Setter Injection
* Field Injection ❌ (not recommended)

> Constructor injection is preferred because it ensures immutability, easier testing, and mandatory dependency injection.

### Constructor Injection (Best Practice)

```java
@Service
public class OrderService {

    private final PaymentService paymentService;

    public OrderService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
}
```

## ✅ 7. What is bean scope?

👉 Defines lifecycle/visibility of a bean.

Types:

* Singleton (default)
* Prototype
* Request
* Session

## ✅ 8. Difference between `@Component`, `@Service`, `@Repository`?

👉 All are **stereotype annotations**:

* `@Component` → Generic bean
* `@Service` → Business logic layer
* `@Repository` → DAO layer (adds DB exception translation)

## ✅ 9. Why is constructor injection preferred over field injection?

👉 Because:

* Ensures **mandatory dependencies**
* Makes class **immutable**
* Easier for **unit testing**
* No reflection required

## ✅ 10. What happens if two beans of same type exist?

👉 Spring throws:

```
NoUniqueBeanDefinitionException
```

✅ Resolve using:

* `@Qualifier("beanName")`
* `@Primary`

---

## ✅ 11. How does Spring resolve dependency injection internally?

👉 Flow:

1. Spring scans classes (`@ComponentScan`)
2. Creates beans in container
3. Uses **BeanFactory/ApplicationContext**
4. Resolves dependencies:
  * By type
  * By name (if multiple)
5. Injects using constructor/setter

👉 Internally uses:

* Reflection
* BeanPostProcessor
* Dependency resolution algorithm

---

## 🔥 Final Interview Tip

👉 If interviewer asks: **“Explain IoC and DI in one line”**

Answer:

> IoC is the concept of delegating object control to Spring, and DI is the mechanism used to inject dependencies.


---
# 🧠 TOPIC 3: Spring Annotations (Core + Boot)
<a id="3"> </a>
---

## 🔹 What to Study

### 1. Stereotype Annotations (Bean Creation)

These tell Spring to **create and manage beans**:

* `@Component` → Generic bean
* `@Service` → Business logic layer
* `@Repository` → DAO layer (adds DB exception handling)
* `@Controller` → MVC controller

👉 All are detected via **component scanning**

---

### 2. Dependency Injection

* `@Autowired` → Inject dependency automatically
* `@Qualifier` → Resolve multiple beans
* `@Primary` → Default bean when multiple exist

---

### 3. Configuration Annotations

* `@Configuration` → Marks config class
* `@Bean` → Manually define a bean
* `@ComponentScan` → Scan packages for beans

---

### 4. Spring Boot Main Annotation

### 👉 `@SpringBootApplication`

This is a combination of:

```java
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan
```

Used in **Spring Boot** main class.

---

### 5. Web Layer Annotations

* `@RestController` → REST API (JSON response)
* `@Controller` → MVC (HTML views)
* `@RequestMapping` → Base URL mapping
* `@GetMapping`, `@PostMapping`, etc.

---

## 🎯 Interview Questions

### 🟢 Basic

1. What is `@Component`?
2. What is `@Autowired`?
3. Difference between `@Controller` and `@RestController`?

---

### 🟡 Moderate

4. What does `@SpringBootApplication` do internally?
5. Difference between `@Component` and `@Bean`?
6. What is `@Qualifier`?

---

### 🔴 Tricky / Real Interview

7. Can `@Autowired` work without `@Component`?
8. What happens if two beans of same type exist?
9. Difference between `@Controller` and `@RestController` in response handling?

---

## 💡 Expected Answers

### 👉 @Component vs @Bean

> `@Component` is auto-detected via scanning, while `@Bean` is manually defined inside a configuration class.

---

### 👉 @Controller vs @RestController

> `@Controller` returns view (HTML), while `@RestController` returns JSON/XML response.

---

### 👉 @SpringBootApplication

> It combines configuration, auto-configuration, and component scanning.

---

## 🔹 Code Examples

### ✅ Using @Component

```java id="qh9lxe"
@Component
public class PaymentService {
}
```

---

### ✅ Using @Bean

```java id="ekr3km"
@Configuration
public class AppConfig {

    @Bean
    public PaymentService paymentService() {
        return new PaymentService();
    }
}
```

---

### ✅ REST Controller

```java id="vfmflj"
@RestController
@RequestMapping("/api")
public class UserController {

    @GetMapping("/users")
    public String getUsers() {
        return "Users List";
    }
}
```

---

## 🔹 Common Mistakes 🚫

* Thinking `@RestController` = `@Controller` (they are different)
* Not knowing `@SpringBootApplication` is a combination
* Confusion between `@Component` vs `@Bean`

---

## 🔹 Interview Tip 💡

👉 If interviewer asks tricky question:

**Q: Can Spring work without annotations?**
👉 Answer:

> Yes, using XML configuration, but annotations are preferred in modern applications.

---
# 🧠 TOPIC 4: Spring Boot Configuration
<a id="4"> </a>
---

## 🔹 What to Study

### 1. Configuration Files

In **Spring Boot**, configuration is mainly done using:

#### ✅ `application.properties`

```properties
server.port=8081
spring.datasource.url=jdbc:mysql://localhost:3306/test
```

#### ✅ `application.yml` (YAML format)

```yaml
server:
  port: 8081

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/test
```

👉 YAML is more readable for nested configs.

---

### 2. Profiles (VERY IMPORTANT 🔥)

Used for **environment-specific configs**:

* dev
* test
* prod

#### Example:

```properties
spring.profiles.active=dev
```

Files:

* `application-dev.properties`
* `application-prod.properties`

---

### 3. @Value Annotation

Used to inject values from properties:

```java
@Value("${server.port}")
private String port;
```

---

### 4. @ConfigurationProperties (Better Approach)

Used for **grouped configuration**

```java
@ConfigurationProperties(prefix = "app")
@Component
public class AppConfig {
    private String name;
    private String version;
}
```

---

### 5. Environment Object

```java
@Autowired
private Environment env;

String port = env.getProperty("server.port");
```

---

## 🎯 Interview Questions

### 🟢 Basic

1. What is `application.properties`?
2. Difference between `.properties` and `.yml`?
3. What is Spring profile?

---

### 🟡 Moderate

4. How do you activate a profile?
5. What is `@Value`?
6. What is `@ConfigurationProperties`?

---

### 🔴 Tricky / Real Interview

7. Difference between `@Value` and `@ConfigurationProperties`?
8. How does Spring Boot load configuration internally?
9. What is the priority order of configuration sources?

---

## 💡 Expected Answers

### 👉 @Value vs @ConfigurationProperties

> `@Value` is used for single values, while `@ConfigurationProperties` is used for grouped and type-safe configuration.

---

### 👉 Profile Usage

> Profiles allow different configurations for different environments like dev, test, and prod.

---

### 👉 Config Priority (IMPORTANT 🔥)

Highest priority:

1. Command line args
2. Environment variables
3. `application.properties`

---

## 🔹 Code Example

### ✅ Profile-based config

```properties
# application-dev.properties
server.port=8081
```

```properties
# application-prod.properties
server.port=9090
```

---

## 🔹 Common Mistakes 🚫

* Hardcoding values instead of using config
* Not using profiles in real projects
* Overusing `@Value` instead of structured config

---

## 🔹 Interview Tip 💡

👉 If interviewer asks:

**“How do you manage different environments?”**

Answer:

> Using Spring profiles with separate configuration files like application-dev and application-prod.

---

# 🧠 TOPIC 5: REST API Development
<a id="5"> </a>
---

## 🔹 What to Study

### 1. What is REST?

* REST = Representational State Transfer
* Used to build **stateless APIs over HTTP**

---

### 2. Core Annotations

#### ✅ `@RestController`

* Combines `@Controller + @ResponseBody`
* Returns JSON directly

#### ✅ `@RequestMapping`

* Base URL mapping

#### ✅ HTTP Method Annotations

* `@GetMapping` → Fetch data
* `@PostMapping` → Create data
* `@PutMapping` → Update
* `@DeleteMapping` → Delete

---

### 3. Handling Request Data

#### 📌 Path Variable

```java id="8hyy45"
@GetMapping("/users/{id}")
public String getUser(@PathVariable int id) {
    return "User ID: " + id;
}
```

---

#### 📌 Query Parameter

```java id="b6y7b3"
@GetMapping("/users")
public String getUser(@RequestParam String name) {
    return name;
}
```

---

#### 📌 Request Body

```java id="smv0ks"
@PostMapping("/users")
public User createUser(@RequestBody User user) {
    return user;
}
```

---

### 4. Response Handling

#### ✅ Returning Object → JSON automatically

```java id="3w0p41"
@GetMapping("/user")
public User getUser() {
    return new User("John", 25);
}
```

---

#### ✅ Custom Response (IMPORTANT)

```java id="f6daxn"
@GetMapping("/user")
public ResponseEntity<User> getUser() {
    return ResponseEntity.ok(new User("John", 25));
}
```

---

### 5. HTTP Status Codes

* 200 → OK
* 201 → Created
* 400 → Bad Request
* 404 → Not Found
* 500 → Server Error

---

## 🎯 Interview Questions

### 🟢 Basic

1. What is REST API?
2. What is `@RestController`?
3. Difference between GET and POST?

---

### 🟡 Moderate

4. Difference between `@PathVariable` and `@RequestParam`?
5. What is `@RequestBody`?
6. What is `ResponseEntity`?

---

### 🔴 Tricky / Real Interview

7. Difference between PUT and PATCH?
8. How does Spring convert Java object to JSON?
9. What happens if request body is invalid?

---

## 💡 Expected Answers

### 👉 @PathVariable vs @RequestParam

> PathVariable is used for URL path values, while RequestParam is used for query parameters.

---

### 👉 ResponseEntity

> It allows full control over HTTP response including status code, headers, and body.

---

### 👉 Object → JSON Conversion

> Done using Jackson library automatically by Spring Boot.

---

## 🔹 Full Example

```java id="1r0izp"
@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/{id}")
    public ResponseEntity<String> getUser(@PathVariable int id) {
        return ResponseEntity.ok("User " + id);
    }

    @PostMapping
    public ResponseEntity<String> createUser(@RequestBody String user) {
        return ResponseEntity.status(201).body(user);
    }
}
```

---

## 🔹 Common Mistakes 🚫

* Using `@Controller` instead of `@RestController`
* Not handling HTTP status properly
* Confusing PUT vs POST

---

## 🔹 Interview Tip 💡

👉 If interviewer asks:

**“How do you design a REST API?”**

Answer:

> Use proper HTTP methods, meaningful URLs, correct status codes, and stateless communication.

---

# 🧠 TOPIC 6: Exception Handling in Spring Boot
<a id="6"> </a>
---

## 🔹 What to Study

### 1. Why Exception Handling?

* Prevent application crash ❌
* Return meaningful error responses ✅
* Improve API quality

---

### 2. Types of Exception Handling

#### ✅ Local Exception Handling (Controller Level)

```java
@ExceptionHandler(Exception.class)
public ResponseEntity<String> handleException(Exception ex) {
    return ResponseEntity.status(500).body(ex.getMessage());
}
```

👉 Works only within that controller

---

#### ✅ Global Exception Handling (IMPORTANT 🔥)

Using:

```java
@ControllerAdvice
```

---

### 3. @ControllerAdvice (VERY IMPORTANT)

* Applies to all controllers globally
* Centralized exception handling

```java
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntime(RuntimeException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
```

---

### 4. Custom Exceptions

```java
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String msg) {
        super(msg);
    }
}
```

Usage:

```java
throw new UserNotFoundException("User not found");
```

---

### 5. Custom Error Response (Best Practice)

```java
public class ErrorResponse {
    private String message;
    private int status;
}
```

---

## 🎯 Interview Questions

### 🟢 Basic

1. What is exception handling in Spring Boot?
2. What is `@ExceptionHandler`?
3. What is `@ControllerAdvice`?

---

### 🟡 Moderate

4. Difference between `@ControllerAdvice` and `@RestControllerAdvice`?
5. How do you create custom exception?
6. How do you return custom error response?

---

### 🔴 Tricky / Real Interview

7. How does Spring handle exceptions internally?
8. What is default error handling in Spring Boot?
9. Can multiple exception handlers exist?

---

## 💡 Expected Answers

### 👉 @ControllerAdvice vs @RestControllerAdvice

> `@RestControllerAdvice` = `@ControllerAdvice + @ResponseBody` (returns JSON directly)

---

### 👉 Default Handling

> Spring Boot uses **BasicErrorController** to return default error JSON.

---

### 👉 Multiple Handlers

> Yes, Spring picks the most specific exception handler.

---

## 🔹 Real Example

```java
@RestController
@RequestMapping("/users")
public class UserController {

    @GetMapping("/{id}")
    public String getUser(@PathVariable int id) {
        if (id == 0) {
            throw new RuntimeException("Invalid ID");
        }
        return "User " + id;
    }
}
```

---

### Global Handler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleException(RuntimeException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
```

---

## 🔹 Common Mistakes 🚫

* Not using global exception handling
* Returning raw exceptions to users
* Not using proper HTTP status codes

---

## 🔹 Interview Tip 💡

👉 If interviewer asks:

**“How do you handle exceptions globally?”**

Answer:

> Using @ControllerAdvice or @RestControllerAdvice with @ExceptionHandler methods.

---

# 🧠 TOPIC 7: Spring Data JPA
<a id="7"> </a>
---

## 🔹 What to Study

### 1. What is Spring Data JPA?

* Simplifies database operations using JPA
* Reduces boilerplate code

👉 Built on top of:

* **Java Persistence API**
* Implemented by **Hibernate**

---

### 2. Repository Interfaces

#### ✅ `JpaRepository` (Most Used)

```java id="6kyg8j"
public interface UserRepository extends JpaRepository<User, Long> {
}
```

👉 Provides:

* `save()`
* `findById()`
* `findAll()`
* `delete()`

---

### 3. Entity Mapping

```java id="n9gwfs"
@Entity
public class User {

    @Id
    @GeneratedValue
    private Long id;

    private String name;
}
```

---

### 4. Custom Query Methods (VERY IMPORTANT 🔥)

Spring generates query automatically:

```java id="s5h2f9"
List<User> findByName(String name);
```

```java id="ksw3ta"
List<User> findByAgeGreaterThan(int age);
```

---

### 5. @Query Annotation

```java id="lvllp5"
@Query("SELECT u FROM User u WHERE u.name = :name")
List<User> getUsersByName(@Param("name") String name);
```

---

### 6. Pagination & Sorting

```java id="7yi2j1"
Page<User> findAll(Pageable pageable);
```

---

### 7. Relationships (IMPORTANT 🔥)

* `@OneToOne`
* `@OneToMany`
* `@ManyToOne`
* `@ManyToMany`

---

## 🎯 Interview Questions

### 🟢 Basic

1. What is Spring Data JPA?
2. What is `JpaRepository`?
3. What is an Entity?

---

### 🟡 Moderate

4. Difference between JPA and Hibernate?
5. What are derived query methods?
6. What is `@Query`?

---

### 🔴 Tricky / Real Interview

7. What happens inside `save()` method?
8. Difference between `findById()` and `getById()`?
9. What is Lazy vs Eager loading?

---

## 💡 Expected Answers

### 👉 JPA vs Hibernate

> JPA is a specification, Hibernate is an implementation.

---

### 👉 Derived Query

> Spring creates query automatically based on method name.

---

### 👉 Lazy vs Eager

> Lazy loads data when needed, Eager loads immediately.

---

## 🔹 Code Example

### Repository

```java id="e42h2l"
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByName(String name);
}
```

---

### Service

```java id="rjh6h5"
@Service
public class UserService {

    @Autowired
    private UserRepository repo;

    public List<User> getUsers(String name) {
        return repo.findByName(name);
    }
}
```

---

## 🔹 Common Mistakes 🚫

* Not understanding Lazy vs Eager
* Writing wrong method names in query methods
* Not handling Optional from `findById()`

---

## 🔹 Interview Tip 💡

👉 If interviewer asks:

**“How does Spring Data JPA reduce code?”**

Answer:

> By providing repository interfaces and auto-generating queries from method names.

---

# 🧠 TOPIC 8: Spring Boot Security (Basics)
<a id="8"> </a>
---

## 🔹 What to Study

### 1. What is Spring Security?

* Framework used to secure applications
* Handles:

  * Authentication ✅
  * Authorization ✅

👉 Provided by **Spring Security**

---

## 🔹 2. Authentication vs Authorization (VERY IMPORTANT 🔥)

* **Authentication** → *Who are you?* (Login)
* **Authorization** → *What can you access?* (Permissions)

---

## 🔹 3. Default Security Behavior

If you add Spring Security dependency:

* All endpoints are secured 🔒
* Default username → `user`
* Password → generated in console

---

## 🔹 4. Basic Security Configuration

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/public/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin();

        return http.build();
    }
}
```

---

## 🔹 5. Password Encoding (IMPORTANT)

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

👉 Uses:

* **BCrypt**

---

## 🔹 6. In-Memory Authentication

```java
@Bean
public UserDetailsService userDetailsService() {
    UserDetails user = User
        .withUsername("admin")
        .password(passwordEncoder().encode("1234"))
        .roles("ADMIN")
        .build();

    return new InMemoryUserDetailsManager(user);
}
```

---

## 🎯 Interview Questions

### 🟢 Basic

1. What is Spring Security?
2. Difference between Authentication and Authorization?
3. What is default username/password?

---

### 🟡 Moderate

4. What is `SecurityFilterChain`?
5. What is password encoding?
6. What is CSRF?

---

### 🔴 Tricky / Real Interview

7. Why do we disable CSRF in REST APIs?
8. How does Spring Security work internally?
9. What is filter chain in Spring Security?

---

## 💡 Expected Answers

### 👉 CSRF

> Cross-Site Request Forgery attack; disabled in stateless REST APIs.

---

### 👉 Password Encoding

> Passwords should be hashed using BCrypt for security.

---

### 👉 Filter Chain

> Requests pass through multiple security filters before reaching controller.

---

## 🔹 Common Mistakes 🚫

* Storing plain text passwords ❌
* Not understanding authentication vs authorization
* Blindly disabling security

---

## 🔹 Interview Tip 💡

👉 If interviewer asks:

**“How do you secure REST APIs?”**

Answer:

> Using Spring Security with authentication, authorization, and token-based mechanisms (like JWT).

---

# 🧠 TOPIC 9: Testing in Spring Boot
<a id="9"> </a>
---

## 🔹 What to Study

### 1. Types of Testing

#### ✅ Unit Testing

* Tests individual class
* No Spring context

#### ✅ Integration Testing

* Tests full flow with Spring context

---

## 🔹 2. Key Annotations

### ✅ `@SpringBootTest`

* Loads full application context
* Used for integration testing

```java
@SpringBootTest
class AppTest {
}
```

---

### ✅ `@WebMvcTest`

* Tests only controller layer

---

### ✅ `@MockBean`

* Mock dependencies inside Spring context

---

## 🔹 3. Mockito (VERY IMPORTANT 🔥)

Used for mocking dependencies

👉 Provided by **Mockito**

---

### Example:

```java
@Mock
private UserRepository repo;

@InjectMocks
private UserService service;
```

---

## 🔹 4. Unit Test Example

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repo;

    @InjectMocks
    private UserService service;

    @Test
    void testGetUser() {
        when(repo.findById(1L)).thenReturn(Optional.of(new User()));

        User user = service.getUser(1L);

        assertNotNull(user);
    }
}
```

---

## 🔹 5. Controller Test Example

```java
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testAPI() throws Exception {
        mockMvc.perform(get("/users"))
               .andExpect(status().isOk());
    }
}
```

---

## 🎯 Interview Questions

### 🟢 Basic

1. What is unit testing?
2. What is `@SpringBootTest`?
3. What is Mockito?

---

### 🟡 Moderate

4. Difference between unit test and integration test?
5. What is `@MockBean`?
6. What is `MockMvc`?

---

### 🔴 Tricky / Real Interview

7. Why is `@SpringBootTest` slow?
8. When to use `@WebMvcTest` vs `@SpringBootTest`?
9. How do you test service layer?

---

## 💡 Expected Answers

### 👉 Unit vs Integration

> Unit test tests single class, integration test loads Spring context and tests full flow.

---

### 👉 @SpringBootTest

> Loads entire Spring context, so slower but more realistic.

---

### 👉 MockMvc

> Used to test REST APIs without starting server.

---

## 🔹 Common Mistakes 🚫

* Using `@SpringBootTest` everywhere (slow)
* Not mocking dependencies
* Mixing unit & integration testing

---

## 🔹 Interview Tip 💡

👉 If interviewer asks:

**“How do you test your service layer?”**

Answer:

> Using Mockito to mock repository and test business logic independently.

---

# 🧠 TOPIC 10: Spring Boot Actuator & Production Readiness
<a id="10"> </a>
---

## 🔹 What to Study

### 1. What is Spring Boot Actuator?

👉 **Spring Boot Actuator** provides **production-ready features** like:

* Health checks ✅
* Metrics ✅
* Application monitoring ✅

---

## 🔹 2. Adding Actuator

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

---

## 🔹 3. Important Endpoints (VERY IMPORTANT 🔥)

| Endpoint            | Purpose                |
| ------------------- | ---------------------- |
| `/actuator/health`  | App health             |
| `/actuator/info`    | App info               |
| `/actuator/metrics` | Performance metrics    |
| `/actuator/env`     | Environment properties |

---

## 🔹 4. Enable Endpoints

```properties
management.endpoints.web.exposure.include=*
```

👉 Or specific:

```properties
management.endpoints.web.exposure.include=health,info
```

---

## 🔹 5. Health Check Example

```json
{
  "status": "UP"
}
```

👉 If DB fails:

```json
{
  "status": "DOWN"
}
```

---

## 🔹 6. Custom Health Indicator

```java
@Component
public class CustomHealth implements HealthIndicator {

    @Override
    public Health health() {
        return Health.up().withDetail("message", "OK").build();
    }
}
```

---

## 🔹 7. Securing Actuator

* Don’t expose all endpoints in production ❌
* Secure using Spring Security 🔒

---

## 🎯 Interview Questions

### 🟢 Basic

1. What is Spring Boot Actuator?
2. What is `/actuator/health`?

---

### 🟡 Moderate

3. How do you enable actuator endpoints?
4. What is custom health indicator?

---

### 🔴 Tricky / Real Interview

5. How do you monitor application in production?
6. How do you secure actuator endpoints?
7. What metrics does actuator provide?

---

## 💡 Expected Answers

### 👉 Actuator

> Provides production-ready monitoring features like health checks and metrics.

---

### 👉 Health Endpoint

> Used to check if application and dependencies are running.

---

### 👉 Production Monitoring

> Using actuator endpoints, logs, and monitoring tools.

---

## 🔹 Common Mistakes 🚫

* Exposing all endpoints in production
* Not securing actuator endpoints
* Ignoring monitoring

---

## 🔹 Interview Tip 💡

👉 If interviewer asks:

**“How do you check if your app is running in production?”**

Answer:

> Using `/actuator/health` endpoint from Spring Boot Actuator.

---

