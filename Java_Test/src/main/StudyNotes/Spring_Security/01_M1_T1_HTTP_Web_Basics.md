# 🌐 HTTP & Web Basics (Deep Dive)

---

## 🔹 HTTP Methods (Why they matter in security)

HTTP methods define **what kind of action** the client is trying to perform.

* **GET** → Read data

    * Should be **safe & idempotent** (no data change)
    * Usually allowed publicly (e.g., fetch products)

* **POST** → Create data

    * Changes server state
    * Needs stricter security (authentication + CSRF protection)

* **PUT** → Update entire resource

    * Idempotent (same request → same result)

* **PATCH** → Partial update (important in real APIs)

* **DELETE** → Remove data

    * Highly sensitive → must be restricted

👉 Why this matters in Spring Security:

* You often define rules like:

    * Allow `GET /public/**`
    * Restrict `POST /admin/**`

👉 Common interview trap:

* *“Is GET always safe?”* → No. It **should be**, but backend logic may still modify state.

---

## 🔹 Request–Response Lifecycle (Core Understanding)

This is the **most important concept**.

### Flow:

```
Client → HTTP Request → Server → Processing → HTTP Response → Client
```

### In a Spring Boot app:

```
Client
  ↓
Filter Chain (Spring Security lives here)
  ↓
DispatcherServlet
  ↓
Controller
  ↓
Service
  ↓
Repository (DB)
  ↓
Response
```

👉 Key insight:

* **Spring Security runs BEFORE your controller**
* If authentication fails → request never reaches controller

---

### 🔥 What exactly is in a request?

A request has:

* Method (GET/POST)
* URL (`/api/users`)
* Headers
* Body (optional)

Example:

```
POST /login
Authorization: Bearer eyJhbGci...
Content-Type: application/json

{
  "username": "admin",
  "password": "1234"
}
```

---

### 🔥 What is in a response?

* Status code
* Headers
* Body

Example:

```
200 OK
Content-Type: application/json

{
  "message": "Success"
}
```

---

## 🔹 Headers (Critical for Security)

Headers are where **security data travels**.

### Important headers:

### 1. Authorization (MOST IMPORTANT)

Used to send credentials:

```
Authorization: Bearer <JWT_TOKEN>
Authorization: Basic <base64>
```

👉 Spring Security reads this to authenticate user.

---

### 2. Cookie

Used in **session-based authentication**:

```
Cookie: JSESSIONID=ABC123
```

👉 Server uses this to identify user session.

---

### 3. Content-Type

```
Content-Type: application/json
```

👉 Helps server understand request body format.

---

### 4. Origin (CORS related)

```
Origin: https://example.com
```

👉 Used to control **cross-origin requests**

---

### 5. Accept

```
Accept: application/json
```

👉 Tells server what format client expects

---

### 🔥 Key Insight:

* JWT → travels in **Authorization header**
* Session → travels in **Cookie**

---

## 🔹 HTTP Status Codes (Security Signals)

These are extremely important in interviews.

### ✅ 200 OK

* Request successful

---

### ❌ 401 Unauthorized

* User is **NOT authenticated**
* No/invalid credentials

👉 Example:

* Missing JWT
* Invalid token

---

### ❌ 403 Forbidden

* User **is authenticated BUT not allowed**

👉 Example:

* USER trying to access ADMIN API

---

### ❌ 404 Not Found

* Resource not found
  👉 Sometimes used to **hide sensitive endpoints**

---

### 🔥 Interview Trap:

* 401 → Authentication problem
* 403 → Authorization problem

---

## 🔹 Stateless vs Stateful Communication (VERY IMPORTANT)

This is where most confusion happens.

---

### 🟢 Stateful (Session-Based Authentication)

Server stores user state.

Flow:

```
Login → Server creates session → Sends session ID (cookie)
Next request → Cookie sent → Server verifies session
```

👉 Example:

```
Cookie: JSESSIONID=XYZ123
```

### ✅ Pros:

* Easy to implement
* Built-in support in Spring Security

### ❌ Cons:

* Server must store sessions (memory issue)
* Not scalable for microservices

---

### 🔴 Stateless (Token-Based / JWT)

Server does NOT store user state.

Flow:

```
Login → Server generates JWT → Client stores it
Next request → Sends JWT → Server validates token
```

👉 Example:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### ✅ Pros:

* Scalable
* No server memory
* Perfect for microservices

### ❌ Cons:

* Token revocation is tricky
* Must handle expiration carefully

---

### 🔥 Key Difference (VERY IMPORTANT)

| Feature     | Stateful (Session) | Stateless (JWT) |
|-------------|--------------------|-----------------|
| Storage     | Server             | Client          |
| Scaling     | Hard               | Easy            |
| Performance | Medium             | High            |
| Security    | Depends            | Depends         |

---

### 💡 Golden Rule:

* Monolith → Session is fine
* Microservices → JWT preferred

---

## 🧠 Final Mental Model (IMPORTANT)

* HTTP is **stateless by default**
* Security adds **identity tracking**
* This tracking happens via:

    * **Session (stateful)** OR
    * **Token (stateless)**

---

## ⚠️ Common Mistakes

* Confusing 401 vs 403 ❌
* Thinking JWT is always better ❌
* Ignoring headers ❌
* Not understanding request flow ❌

---

## ✅ What You Should Be Able to Answer Now

* How does a request travel in Spring Boot?
* Where does Spring Security intercept?
* Difference between session and JWT?
* What is stored in headers?
* Difference between 401 and 403?

---

---

# 🧠 Trap-Based MCQs (HTTP & Web Basics)

---

## 1. Which statement is TRUE about HTTP?

1. HTTP is stateful but can behave stateless
2. HTTP is stateless but can be made stateful using sessions
3. HTTP always stores user data between requests
4. HTTP cannot support authentication

---

## 2. A request returns **401 Unauthorized**. What is the MOST accurate reason?

1. User does not have required role
2. User is authenticated but not authorized
3. User is not authenticated or credentials are invalid
4. Resource does not exist

---

## 3. Which header is primarily used for JWT authentication?

1. Cookie
2. Authorization
3. Accept
4. Content-Type

---

## 4. Which of the following is TRUE about GET requests?

1. They always modify server data
2. They are guaranteed to be safe
3. They should not modify server state, but can if poorly designed
4. They cannot have headers

---

## 5. Where does Spring Security intercept a request?

1. Controller layer
2. Service layer
3. Filter chain before DispatcherServlet
4. Database layer

---

## 6. Which scenario will MOST likely result in **403 Forbidden**?

1. No token provided
2. Invalid token
3. Valid token but insufficient role
4. Server is down

---

## 7. In session-based authentication, where is session data stored?

1. Client browser only
2. Server memory
3. Inside JWT token
4. Inside HTTP headers permanently

---

## 8. Which is TRUE about JWT?

1. Server must store session for JWT
2. JWT removes need for sending data in every request
3. JWT must be stored only in cookies
4. JWT is sent with every request

---

## 9. Which of the following is NOT idempotent?

1. GET
2. PUT
3. DELETE
4. POST

---

## 10. What happens if Authorization header is missing in a secured endpoint?

1. 200 OK
2. 403 Forbidden
3. 401 Unauthorized
4. 404 Not Found

---

## 11. Which header is mainly used in session-based authentication?

1. Authorization
2. Cookie
3. Accept
4. Origin

---

## 12. What is the key difference between PUT and PATCH?

1. PUT is faster
2. PATCH replaces entire resource
3. PUT replaces full resource, PATCH updates partially
4. PATCH is always idempotent

---

## 13. Which statement is TRUE about stateless systems?

1. Server stores user session
2. Each request contains all required information
3. Requests depend on previous requests
4. Requires cookies

---

## 14. Which of the following best describes CORS?

1. Authentication mechanism
2. Encryption method
3. Cross-origin request control mechanism
4. Session management system

---

## 15. What does the Origin header indicate?

1. Authentication token
2. Server IP
3. Source of the request (domain)
4. HTTP method

---

## 16. If a user sends a valid JWT but the server rejects it due to expiration, what should be returned?

1. 200 OK
2. 403 Forbidden
3. 401 Unauthorized
4. 500 Internal Server Error

---

## 17. Which layer handles routing to controllers in Spring Boot?

1. Filter
2. DispatcherServlet
3. SecurityContext
4. AuthenticationManager

---

## 18. Which is TRUE about cookies?

1. Stored only on server
2. Used to maintain session state
3. Cannot be modified
4. Used only for caching

---

## 19. Why are DELETE APIs usually restricted?

1. They are slow
2. They consume more memory
3. They permanently remove data
4. They cannot be secured

---

## 20. What is the MOST correct statement?

1. JWT is always more secure than sessions
2. Sessions are always better than JWT
3. Security depends on implementation, not mechanism
4. JWT does not require validation

---

# ✅ ANSWERS

---

1. **2** ✅
2. **3** ✅
3. **2** ✅
4. **3** ✅
5. **3** ✅
6. **3** ✅
7. **2** ✅
8. **4** ✅
9. **4** ✅
10. **3** ✅
11. **2** ✅
12. **3** ✅
13. **2** ✅
14. **3** ✅
15. **3** ✅
16. **3** ✅
17. **2** ✅
18. **2** ✅
19. **3** ✅
20. **3** ✅

---

---

# 🔥 HTTP & Web Basics – Interview Questions (Hidden Answers)

---

## 1. What is the difference between HTTP being stateless and authentication being stateful?

<details>
<summary>Show Answer</summary>

HTTP is inherently **stateless**, meaning each request is independent and the server does not remember previous requests.

Authentication can be:

* **Stateful** → Server stores session (e.g., JSESSIONID)
* **Stateless** → Client sends token (JWT) in every request

👉 So stateless protocol + stateful auth = possible via sessions.

</details>

---

## 2. What happens internally when a request hits a Spring Boot application?

<details>
<summary>Show Answer</summary>

Flow:

1. Request hits **Servlet container**
2. Goes through **Filter Chain (Spring Security here)**
3. Reaches **DispatcherServlet**
4. Routed to **Controller**
5. Business logic executes
6. Response returned

👉 If authentication fails → blocked in filter stage.

</details>

---

## 3. What is the difference between 401 and 403?

<details>
<summary>Show Answer</summary>

* **401 Unauthorized** → User is NOT authenticated
* **403 Forbidden** → User is authenticated BUT lacks permission

👉 Key:
401 = Who are you?
403 = You are not allowed

</details>

---

## 4. Where is JWT stored and how is it sent in requests?

<details>
<summary>Show Answer</summary>

JWT is typically stored on client side:

* LocalStorage / SessionStorage / Memory

Sent in header:

```
Authorization: Bearer <token>
```

👉 Server validates token on each request.

</details>

---

## 5. What is the role of the Authorization header?

<details>
<summary>Show Answer</summary>

It carries **credentials or tokens** used for authentication.

Examples:

* Basic Auth → base64 encoded credentials
* Bearer → JWT token

👉 Spring Security reads this header to authenticate the user.

</details>

---

## 6. What is the difference between PUT and PATCH?

<details>
<summary>Show Answer</summary>

* **PUT** → Replaces entire resource
* **PATCH** → Updates partial resource

👉 PUT is idempotent; PATCH may not be.

</details>

---

## 7. Why is GET considered safe? Is it always safe?

<details>
<summary>Show Answer</summary>

GET is *intended* to be safe (no state change).

BUT:

* Backend can still modify data
* So it is not guaranteed safe

👉 Safety depends on implementation, not method.

</details>

---

## 8. How does session-based authentication work?

<details>
<summary>Show Answer</summary>

1. User logs in
2. Server creates session
3. Sends session ID in cookie
4. Client sends cookie in each request
5. Server validates session

👉 Session stored on server side.

</details>

---

## 9. How does JWT authentication differ from session-based authentication?

<details>
<summary>Show Answer</summary>

Session:

* Server stores session
* Uses cookies

JWT:

* No server storage
* Token contains user info
* Sent in Authorization header

👉 JWT is stateless.

</details>

---

## 10. What is stored inside an HTTP request?

<details>
<summary>Show Answer</summary>

* Method (GET/POST)
* URL
* Headers
* Body (optional)

</details>

---

## 11. What is the purpose of the Cookie header?

<details>
<summary>Show Answer</summary>

Stores session-related data (e.g., JSESSIONID)

Used by server to identify user session.

</details>

---

## 12. Can we use both cookies and Authorization headers together?

<details>
<summary>Show Answer</summary>

Yes.

Example:

* Cookie → session tracking
* Authorization → API token

👉 But usually:

* Session → cookies
* JWT → Authorization header

</details>

---

## 13. What is CORS and why is Origin header important?

<details>
<summary>Show Answer</summary>

CORS controls cross-origin requests.

Origin header tells server:
→ where request is coming from

Server decides whether to allow it.

</details>

---

## 14. What happens if Authorization header is missing in a secured API?

<details>
<summary>Show Answer</summary>

Spring Security:
→ Treats request as unauthenticated
→ Returns **401 Unauthorized**

</details>

---

## 15. Why is DELETE considered dangerous?

<details>
<summary>Show Answer</summary>

Because it permanently removes data.

👉 Must be:

* Authenticated
* Authorized
* Sometimes logged/audited

</details>

---

## 16. What is idempotency? Which HTTP methods are idempotent?

<details>
<summary>Show Answer</summary>

Idempotent → Same request multiple times = same result

Idempotent methods:

* GET
* PUT
* DELETE

Non-idempotent:

* POST

</details>

---

## 17. Where does Spring Security sit in request flow?

<details>
<summary>Show Answer</summary>

Inside **Filter Chain**, before DispatcherServlet.

👉 It intercepts request before controller.

</details>

---

## 18. What is the difference between header and body?

<details>
<summary>Show Answer</summary>

Headers:

* Metadata (auth, content type)

Body:

* Actual data (JSON, XML)

</details>

---

## 19. Why is stateless architecture preferred in microservices?

<details>
<summary>Show Answer</summary>

* No session storage
* Easy scaling
* Each request is independent

👉 JWT fits perfectly here.

</details>

---

## 20. Can HTTP be stateful?

<details>
<summary>Show Answer</summary>

HTTP itself is stateless.

But we make it stateful using:

* Sessions (cookies)

</details>

---

# 🧠 Pro Tip

If you can confidently answer:

* Request flow
* 401 vs 403
* JWT vs Session
* Headers usage

👉 You already beat **70% of candidates** in interviews.

---
