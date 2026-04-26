# 🚀 Spring Boot + Security Basics (Deep Dive)

---

## 🔹 1. Starter Dependencies (What actually happens)

When you add:

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

👉 You are NOT just adding a library.
You are triggering **auto security configuration**.

---

### 🔥 What Spring Boot does automatically

* Adds **default SecurityFilterChain**
* Enables **basic authentication**
* Secures **all endpoints**
* Generates a **default password (console)**

---

### 🔴 Default Behavior (VERY IMPORTANT)

If you don’t configure anything:

* All APIs → 🔒 secured
* Login → Basic Auth popup
* Username → `user`
* Password → generated at startup

---

### 💡 Interview Insight

👉 “Why is my API secured even without config?”

Because:

* Spring Boot auto-configures Spring Security

---

## 🔹 2. Auto-Configuration (Magic behind the scenes)

Spring Boot uses:
👉 **`@EnableAutoConfiguration`**

---

### 🔥 What it does

* Detects dependencies
* Automatically configures beans

---

### 🔹 For Spring Security

It loads:

* `SecurityAutoConfiguration`
* `UserDetailsServiceAutoConfiguration`

---

### 🔥 Internally

Spring checks:

```
Classpath → Security dependency present → Apply default config
```

---

### 🔴 Conditional Loading

Uses:

* `@ConditionalOnClass`
* `@ConditionalOnMissingBean`

👉 Meaning:

* If you don’t define your own config → default is applied
* If you define → your config overrides default

---

### 💡 Interview Insight

👉 “How do you disable default security?”

* Provide your own `SecurityFilterChain` bean
* OR exclude auto-configuration

---

## 🔹 3. Bean Lifecycle (VERY IMPORTANT for understanding security)

Spring manages objects as **beans**.

---

### 🔹 Lifecycle Steps

```id="zv5e7n"
Create → Initialize → Use → Destroy
```

---

### 🔥 Detailed Flow

1. Bean Instantiation
2. Dependency Injection
3. Initialization (`@PostConstruct`)
4. Ready to use
5. Destruction (`@PreDestroy`)

---

### 🔹 Why this matters in Security?

Security components are beans:

* SecurityFilterChain
* AuthenticationManager
* PasswordEncoder

👉 Spring creates and wires them automatically

---

### 🔴 Important Insight

If bean is not created → security won’t work

---

### 💡 Interview Trap

👉 “Why is my custom filter not working?”

Answer:

* It’s not registered as a bean
  OR
* Not added to filter chain

---

## 🔹 4. Filters vs Interceptors (VERY IMPORTANT)

This is one of the **most asked questions**.

---

### 🔹 Filters (Servlet Level)

* Part of **Servlet API**
* Run **before DispatcherServlet**
* Used by Spring Security

---

### 🔥 Flow

```id="f4h9ld"
Request → Filter → DispatcherServlet → Controller
```

---

### 🔹 Key Points

* Works on **raw HTTP request**
* Can block request completely
* Used for:

    * Authentication
    * Logging
    * CORS

---

---

### 🔹 Interceptors (Spring MVC Level)

* Part of **Spring MVC**
* Run **after DispatcherServlet**

---

### 🔥 Flow

```id="9b4a6z"
Request → DispatcherServlet → Interceptor → Controller
```

---

### 🔹 Key Points

* Works on **handler level**
* Cannot fully replace filters
* Used for:

    * Logging
    * Modifying model/view

---

---

### 🔥 Final Difference (CRITICAL)

| Feature           | Filter            | Interceptor |
|-------------------|-------------------|-------------|
| Level             | Servlet           | Spring MVC  |
| Runs Before       | DispatcherServlet | Controller  |
| Used by Security  | ✅ YES             | ❌ NO        |
| Can block request | ✅ Yes             | Limited     |

---

### 💡 Interview Insight

👉 “Why does Spring Security use filters, not interceptors?”

Because:

* Needs to intercept **before Spring MVC**
* Must handle authentication at **lowest level**

---

## 🔹 5. Flow: Where and How Security Works (MOST IMPORTANT)

This is the **core mental model**.

---

### 🔥 Full Flow

```id="x6d7pl"
Client Request
   ↓
Security Filter Chain (Spring Security)
   ↓
DispatcherServlet
   ↓
Controller
   ↓
Response
```

---

### 🔴 Step-by-Step

---

### 1. Request comes in

* From browser/Postman

---

### 2. Hits Security Filter Chain

👉 This is where:

* Authentication happens
* Authorization happens

---

### 3. If authentication fails

👉 Request stops here
👉 Returns **401**

---

### 4. If authentication passes

👉 Goes to authorization

* If fails → **403**
* If passes → continue

---

### 5. Request reaches Controller

👉 Business logic executes

---

### 🔥 Key Insight

👉 Controller is NEVER reached if security fails

---

### 💡 Interview Answer (Golden Line)

👉 “Spring Security works at the **filter layer before the request reaches the controller**.”

---

## 🧠 Final Mental Model

* Dependency → triggers auto security
* Auto-config → creates default security
* Beans → manage security components
* Filters → enforce security
* Flow → security happens BEFORE controller

---

## ⚠️ Common Mistakes

* Thinking interceptors handle security ❌
* Ignoring auto-configuration ❌
* Not defining beans properly ❌
* Not understanding filter chain ❌

---

## ✅ What You Should Now Know

* Why security is enabled automatically
* How Spring Boot configures security
* Difference between filter and interceptor
* Where security sits in request flow

---

---

# 🚀 Spring Boot Security – Interview MCQs (Real Level)

---

## 1. What happens when you add `spring-boot-starter-security` without any configuration?

1. Only login endpoint is secured
2. All endpoints are secured with default authentication
3. Security is disabled by default
4. Only POST endpoints are secured

<details>
<summary>Show Answer</summary>

**2. All endpoints are secured with default authentication**

👉 Spring Boot auto-configures security

</details>

---

## 2. What is the default username when Spring Security is auto-configured?

1. admin
2. root
3. user
4. spring

<details>
<summary>Show Answer</summary>

**3. user**

</details>

---

## 3. What is the role of `DelegatingFilterProxy`?

1. Performs authentication
2. Connects servlet container with Spring Security
3. Stores user session
4. Handles JWT parsing

<details>
<summary>Show Answer</summary>

**2. Connects servlet container with Spring Security**

</details>

---

## 4. Which component actually executes the security filters?

1. DispatcherServlet
2. DelegatingFilterProxy
3. FilterChainProxy
4. AuthenticationManager

<details>
<summary>Show Answer</summary>

**3. FilterChainProxy**

</details>

---

## 5. Where does Spring Security operate in request lifecycle?

1. After controller
2. Before DispatcherServlet
3. Inside service layer
4. Inside database layer

<details>
<summary>Show Answer</summary>

**2. Before DispatcherServlet**

</details>

---

## 6. What happens if no `SecurityFilterChain` bean is defined?

1. Security is disabled
2. Application fails to start
3. Default security configuration is applied
4. Only login is disabled

<details>
<summary>Show Answer</summary>

**3. Default security configuration is applied**

</details>

---

## 7. Which annotation enables auto-configuration in Spring Boot?

1. @Configuration
2. @EnableSecurity
3. @EnableAutoConfiguration
4. @Component

<details>
<summary>Show Answer</summary>

**3. @EnableAutoConfiguration**

</details>

---

## 8. Which is TRUE about Filters?

1. They run after controller
2. They are part of Spring MVC
3. They operate at servlet level
4. They cannot block requests

<details>
<summary>Show Answer</summary>

**3. They operate at servlet level**

</details>

---

## 9. Which is TRUE about Interceptors?

1. They run before filters
2. They are part of Servlet API
3. They work after DispatcherServlet
4. They replace filters

<details>
<summary>Show Answer</summary>

**3. They work after DispatcherServlet**

</details>

---

## 10. Why does Spring Security use filters instead of interceptors?

1. Filters are faster
2. Filters work before Spring MVC
3. Interceptors are deprecated
4. Filters are easier to configure

<details>
<summary>Show Answer</summary>

**2. Filters work before Spring MVC**

</details>

---

## 11. What happens if authentication fails in filter chain?

1. Controller is still called
2. Service layer handles it
3. Request is blocked and response returned
4. Database is queried

<details>
<summary>Show Answer</summary>

**3. Request is blocked and response returned**

</details>

---

## 12. Which component stores authentication information?

1. AuthenticationManager
2. SecurityContext
3. UserDetailsService
4. FilterChainProxy

<details>
<summary>Show Answer</summary>

**2. SecurityContext**

</details>

---

## 13. What is stored inside `SecurityContext`?

1. HTTP request
2. Database connection
3. Authentication object
4. Controller data

<details>
<summary>Show Answer</summary>

**3. Authentication object**

</details>

---

## 14. Which method is used to add custom filter before another filter?

1. addFilter()
2. addFilterBefore()
3. addFilterAfter()
4. registerFilter()

<details>
<summary>Show Answer</summary>

**2. addFilterBefore()**

</details>

---

## 15. What is the purpose of `AuthenticationManager`?

1. Stores user data
2. Performs authentication logic
3. Handles HTTP requests
4. Manages sessions

<details>
<summary>Show Answer</summary>

**2. Performs authentication logic**

</details>

---

## 16. Which filter typically handles login requests?

1. AuthorizationFilter
2. UsernamePasswordAuthenticationFilter
3. SecurityContextFilter
4. JWTFilter

<details>
<summary>Show Answer</summary>

**2. UsernamePasswordAuthenticationFilter**

</details>

---

## 17. What happens if filter order is incorrect?

1. No effect
2. Security may break
3. Application crashes
4. Only performance is affected

<details>
<summary>Show Answer</summary>

**2. Security may break**

</details>

---

## 18. Which component selects appropriate filter chain?

1. DispatcherServlet
2. FilterChainProxy
3. AuthenticationManager
4. Controller

<details>
<summary>Show Answer</summary>

**2. FilterChainProxy**

</details>

---

## 19. What is the purpose of `ExceptionTranslationFilter`?

1. Logs errors
2. Converts exceptions to HTTP responses
3. Authenticates user
4. Stores session

<details>
<summary>Show Answer</summary>

**2. Converts exceptions to HTTP responses**

</details>

---

## 20. What happens if you define your own `SecurityFilterChain` bean?

1. Default security is disabled/overridden
2. Nothing changes
3. Application fails
4. Only filters change

<details>
<summary>Show Answer</summary>

**1. Default security is disabled/overridden**

</details>

---

# 🧠 What You Just Practiced

* Auto-configuration traps
* Filter chain internals
* Bean behavior
* Core security flow

---

# 🔥 Interview Insight

If you can answer these:
👉 You’re already ahead of **80–90% candidates**

---

---

# 🔥 Spring Boot Security – Interview Questions (Hidden Answers)

---

## 1. Why are all endpoints secured when you add Spring Security dependency without configuration?

<details>
<summary>Show Answer</summary>

Because of **auto-configuration**.

Spring Boot detects `spring-boot-starter-security` and automatically:

* Configures a default `SecurityFilterChain`
* Secures all endpoints
* Enables basic authentication

👉 This is done via `SecurityAutoConfiguration`.

</details>

---

## 2. Explain the complete flow of a request in Spring Security.

<details>
<summary>Show Answer</summary>

Flow:

Client
→ Servlet Container (Tomcat)
→ DelegatingFilterProxy
→ FilterChainProxy
→ Security Filters (authentication + authorization)
→ DispatcherServlet
→ Controller
→ Response

👉 If authentication/authorization fails → request never reaches controller.

</details>

---

## 3. Why does Spring Security use filters instead of interceptors?

<details>
<summary>Show Answer</summary>

Because filters:

* Work at **Servlet level**
* Execute **before DispatcherServlet**
* Can block request completely

Interceptors:

* Work inside Spring MVC
* Too late for authentication

👉 Security must happen before MVC layer.

</details>

---

## 4. What is the role of `FilterChainProxy`?

<details>
<summary>Show Answer</summary>

It is the **core engine of Spring Security filter chain**.

Responsibilities:

* Holds all security filters
* Selects appropriate filter chain based on request
* Executes filters in correct order

</details>

---

## 5. What is `DelegatingFilterProxy` and why is it needed?

<details>
<summary>Show Answer</summary>

It is a **bridge between Servlet container and Spring context**.

Why needed:

* Servlet container manages filters
* Spring manages beans

👉 DelegatingFilterProxy delegates request to Spring-managed filters.

</details>

---

## 6. What happens if authentication fails in filter chain?

<details>
<summary>Show Answer</summary>

* Request is stopped immediately
* Controller is NOT called
* Response returned (usually 401)

👉 Failure happens before reaching business logic.

</details>

---

## 7. What is stored inside `SecurityContext`?

<details>
<summary>Show Answer</summary>

It stores:

* `Authentication` object

Which contains:

* Principal (user)
* Credentials
* Authorities (roles)
* Authentication status

</details>

---

## 8. Why is filter order important in Spring Security?

<details>
<summary>Show Answer</summary>

Because filters depend on each other.

Example:

* Authentication must happen before authorization

Wrong order:

* Authorization runs without authentication → incorrect behavior

</details>

---

## 9. How do you override default Spring Security configuration?

<details>
<summary>Show Answer</summary>

By defining your own `SecurityFilterChain` bean.

Spring Boot:

* Uses default config if none provided
* Uses custom config if defined

👉 Your bean overrides auto-configuration.

</details>

---

## 10. What happens if you forget to register a custom filter?

<details>
<summary>Show Answer</summary>

Filter will NOT execute.

Reasons:

* Not added to filter chain
* Not declared as bean

👉 Common real-world bug

</details>

---

## 11. Explain difference between Filter and Interceptor.

<details>
<summary>Show Answer</summary>

Filter:

* Servlet level
* Runs before DispatcherServlet
* Used for security

Interceptor:

* Spring MVC level
* Runs after DispatcherServlet
* Used for request handling logic

</details>

---

## 12. How does Spring decide which SecurityFilterChain to use?

<details>
<summary>Show Answer</summary>

Based on request matching.

* Each chain has URL pattern
* Spring selects first matching chain

👉 Matching order matters

</details>

---

## 13. What is the role of `ExceptionTranslationFilter`?

<details>
<summary>Show Answer</summary>

It handles security exceptions and converts them to HTTP responses.

* Authentication failure → 401
* Authorization failure → 403

</details>

---

## 14. Why does Spring Security use ThreadLocal in SecurityContext?

<details>
<summary>Show Answer</summary>

To store authentication per request thread.

* Each request runs in separate thread
* ThreadLocal ensures isolation

👉 Prevents data leakage between requests

</details>

---

## 15. What happens if SecurityContext is not set?

<details>
<summary>Show Answer</summary>

User is treated as **anonymous**.

Result:

* Authentication considered failed
* Authorization likely fails

</details>

---

## 16. What is the difference between `AuthenticationManager` and `AuthenticationProvider`?

<details>
<summary>Show Answer</summary>

AuthenticationManager:

* Entry point
* Delegates authentication

AuthenticationProvider:

* Contains actual authentication logic

👉 Manager → delegates → Provider → validates

</details>

---

## 17. Why is Spring Security considered filter-based?

<details>
<summary>Show Answer</summary>

Because:

* It intercepts requests using filters
* All security logic is implemented via filter chain

👉 No controller-level security by default

</details>

---

## 18. What is the biggest mistake developers make with Spring Security?

<details>
<summary>Show Answer</summary>

Not understanding the filter chain.

Common issues:

* Wrong filter order
* Missing SecurityContext
* Misconfigured authorization

</details>

---

## 19. What happens if you use `.permitAll()` incorrectly?

<details>
<summary>Show Answer</summary>

All endpoints become accessible without authentication.

👉 Silent security failure (very dangerous)

</details>

---

## 20. How does Spring Security handle multiple authentication methods?

<details>
<summary>Show Answer</summary>

Using multiple filters and AuthenticationProviders.

Example:

* JWT filter
* Basic auth filter

AuthenticationManager delegates to appropriate provider.

</details>

---

# 🧠 What You Just Practiced

* Core architecture explanation
* Internal flow clarity
* Real-world reasoning
* Interview-level articulation

---
