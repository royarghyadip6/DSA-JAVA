# 🔐 Servlet Basics (Quick but Important)

## Core idea

A **Servlet** is a Java class that handles HTTP requests inside a **Servlet container (Tomcat)**.

Flow:

```text
Client → Servlet Container → Servlet → Response
```

---

## Lifecycle (must know)

```text
init() → called once  
service() → per request  
destroy() → cleanup
```

👉 `service()` internally routes to:

* doGet()
* doPost()
* doPut()
* doDelete()

---

## Threading Model (very important)

* One servlet instance
* Multiple threads handle requests

👉 Avoid shared mutable state

---

# 🔥 Now the REAL part (Security-related Servlet concepts)

This is where Spring Security lives.

---

# 🔹 1. Filters (MOST IMPORTANT)

## What is a Filter?

A Filter intercepts request **before it reaches Servlet**.

```text
Request → Filter → Servlet → Response
```

---

## Why Filters are used in security?

* Authentication
* Authorization
* Logging
* Input validation

👉 Spring Security = **chain of filters**

---

## How Filter works internally

```java
public void doFilter(request, response, chain) {
    // pre-processing (auth check)

    chain.doFilter(request, response); // pass to next filter

    // post-processing
}
```

---

## Key Concept: Filter Chain

```text
Filter1 → Filter2 → Filter3 → Servlet
```

👉 Order matters (very important)

---

## Types of Filters

* GenericFilterBean (Spring base)
* OncePerRequestFilter (most used in Spring Security)

---

## Example (JWT Filter)

```java
public class JwtFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
                                    throws IOException, ServletException {

        String token = request.getHeader("Authorization");

        if (token != null) {
            // validate token
            Authentication auth = ...;

            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        chain.doFilter(request, response);
    }
}
```

---

## 🔴 Critical Insights (Interview)

* Filters run **before DispatcherServlet**
* They can **block request completely**
* They are **stateless per request**
* Order defines behavior

---

# 🔹 2. Interceptors (Spring MVC concept)

## What is an Interceptor?

Interceptor works **after DispatcherServlet**, not before.

```text
Request → Filter → DispatcherServlet → Interceptor → Controller
```

---

## Methods

```java
preHandle()   // before controller  
postHandle()  // after controller  
afterCompletion() // after response
```

---

## Example

```java
public class MyInterceptor implements HandlerInterceptor {

    public boolean preHandle(...) {
        // logic before controller
        return true;
    }
}
```

---

## Key Difference vs Filters

| Feature                       | Filter  | Interceptor |
| ----------------------------- | ------- | ----------- |
| Level                         | Servlet | Spring MVC  |
| Runs before DispatcherServlet | Yes     | No          |
| Can block request             | Yes     | Yes         |
| Access to controller          | No      | Yes         |

---

## 🔴 Important Insight

👉 Spring Security uses **Filters, NOT Interceptors**

---

# 🔥 Filters vs Interceptors (Interview Gold)

| Aspect    | Filter                   | Interceptor             |
| --------- | ------------------------ | ----------------------- |
| Scope     | Global (all requests)    | MVC only                |
| Position  | Before DispatcherServlet | After DispatcherServlet |
| Use case  | Security, auth           | Logging, business logic |
| Framework | Servlet API              | Spring MVC              |

---

# 🔹 3. Listener (brief but important)

Used to track lifecycle events.

Examples:

* ContextListener → app startup
* SessionListener → session create/destroy

👉 Less used in security, but used for auditing

---

# 🔥 How Spring Security Uses Servlet Concepts

---

## Real Flow

```text
Client Request
 ↓
Servlet Container
 ↓
Filter Chain (Spring Security filters)
 ↓
DispatcherServlet
 ↓
Controller
```

---

## Mapping to Spring Security

* Filters → Authentication + Authorization
* SecurityContext → stored per request
* FilterChain → execution engine

---

# 🔴 Critical Security Flow (Must Know)

```text
Request
 → Security Filter Chain
 → Authentication Filter
 → SecurityContext set
 → Authorization Filter
 → Controller
```

---

# ⚠️ Common Interview Traps

---

## Trap 1: “Can we use Interceptor for authentication?”

👉 No (not recommended)

Reason:

* Runs too late (after DispatcherServlet)
* Security should happen before

---

## Trap 2: “Where does Spring Security run?”

👉 Before controller, in **filter layer**

---

## Trap 3: “Why not use Servlet directly?”

👉 Because Spring provides:

* Abstraction
* Security features
* Cleaner architecture

---

## Trap 4: “Which is better: Filter or Interceptor?”

👉 Depends:

* Security → Filter
* Business logic → Interceptor

---

# 🧠 Final Mental Model

* Servlet = request handler
* Filter = security gate (before everything)
* Interceptor = controller-level hook
* Spring Security = filter-based system

---

# 🔥 Interview Killer Line

👉 *“Spring Security is built entirely on the Servlet Filter mechanism, allowing it to intercept and secure requests before they reach the application logic.”*

---

# ✅ What You Should Now Know

* Servlet lifecycle
* Filter chain working
* Interceptor vs Filter
* Where security happens
* Why filters are used

---

---

# 🔐 DispatcherServlet Deep Dive

## 🧠 Core Idea

👉 `DispatcherServlet` is the **front controller** of Spring MVC.

It receives **every HTTP request** and decides:

* which controller to call
* how to process the request
* how to return the response

---

## 🔹 High-Level Flow

```text id="v3x6p9"
Client → Filter Chain → DispatcherServlet → Controller → Response
```

👉 Important:

* Filters (Spring Security) run **before**
* DispatcherServlet runs **after filters**

---

## 🔹 Why DispatcherServlet exists

Without it:

* Every servlet handles its own logic ❌

With it:

* Centralized request handling ✅
* Clean architecture ✅

---

## 🔹 Internal Workflow (Step-by-Step)

```text id="b7r4k2"
1. Request received
2. HandlerMapping finds controller
3. HandlerAdapter executes controller
4. Controller returns ModelAndView / Response
5. ViewResolver resolves view (if needed)
6. Response sent to client
```

---

## 🔥 Step-by-Step Deep Explanation

---

### 1. Request enters DispatcherServlet

* After passing filters
* DispatcherServlet takes control

---

### 2. HandlerMapping (find controller)

👉 Finds which controller method should handle request

Example:

```text id="q9d3fm"
/users → UserController.getUsers()
```

---

### 3. HandlerAdapter (execute controller)

* Invokes controller method
* Handles method arguments (request params, body, etc.)

👉 This is how Spring calls your method dynamically

---

### 4. Controller execution

Example:

```java id="1g9b3k"
@GetMapping("/users")
public List<User> getUsers() {
    return service.getUsers();
}
```

---

### 5. Return handling

Controller can return:

* JSON (REST API)
* ModelAndView (MVC)
* String view name

---

### 6. ViewResolver (for MVC apps)

```text id="7d3n0k"
"home" → /templates/home.html
```

👉 Not used in REST APIs

---

### 7. Response sent back

* Converted to JSON/XML if needed
* Sent to client

---

# 🔥 Key Components Inside DispatcherServlet

---

## HandlerMapping

* Maps URL → Controller
* Example:

```text id="k8q1yz"
/api/users → UserController
```

---

## HandlerAdapter

* Executes controller methods
* Handles parameter binding

---

## HandlerExceptionResolver

* Handles exceptions globally
* Converts exceptions → HTTP responses

---

## ViewResolver

* Resolves view names to actual UI pages

---

## ModelAndView

* Combines data + view (MVC apps)

---

# 🔴 Important for Spring Security

---

## Full Request Flow with Security

```text id="1r6g0o"
Client
 ↓
Filter Chain (Spring Security)
 ↓
DispatcherServlet
 ↓
Controller
```

👉 Security happens **before DispatcherServlet**

---

## 🔥 Key Insight

👉 DispatcherServlet **never handles authentication/authorization**

👉 That is already done by filters

---

# 🔹 DispatcherServlet vs Servlet

| Feature             | Servlet   | DispatcherServlet |
| ------------------- | --------- | ----------------- |
| Type                | Low-level | Spring MVC        |
| Mapping             | Manual    | Automatic         |
| Controller handling | Manual    | Automatic         |
| Extensibility       | Limited   | High              |

---

# 🔹 DispatcherServlet vs Filters vs Interceptors

```text id="9x8l2a"
Request
 ↓
Filters (Security here)
 ↓
DispatcherServlet
 ↓
Interceptors
 ↓
Controller
```

---

# ⚠️ Common Interview Traps

---

## Trap 1: “Does DispatcherServlet handle security?”

👉 ❌ No
👉 Security handled in filter layer

---

## Trap 2: “Can DispatcherServlet block request?”

👉 Not for security
👉 Filters should do that

---

## Trap 3: “Is DispatcherServlet multi-threaded?”

👉 Yes
👉 Like servlets, it handles multiple requests via threads

---

## Trap 4: “What happens if no handler found?”

👉 404 error returned

---

# 🔥 Debugging Perspective

---

## If controller not hit:

Check:

* URL mapping
* HandlerMapping
* Filters blocking request

---

## If request fails before controller:

👉 Issue in filter layer (Spring Security)

---

## If exception happens:

👉 Handled by:

```text id="2p1g6v"
HandlerExceptionResolver
```

---

# 🧠 Final Mental Model

* DispatcherServlet = **central router**
* HandlerMapping = **find controller**
* HandlerAdapter = **execute controller**
* Filters = **security layer before it**
* Interceptors = **logic after it**

---

# 🔥 Interview Killer Line

👉 *“DispatcherServlet is the front controller of Spring MVC that coordinates request handling using HandlerMapping and HandlerAdapter, while security concerns are handled earlier in the filter chain.”*

---

# ✅ What You Should Now Know

* Full request lifecycle
* Internal components
* Where security fits
* How controllers are invoked

---
