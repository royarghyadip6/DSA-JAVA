# 🔐 MODULE 2: Authentication vs Authorization (Deep Dive)

---

## 🔹 Core Idea (Don’t confuse this)

* **Authentication → Who are you?**
* **Authorization → What can you do?**

👉 Order ALWAYS:

```
Authentication → Authorization
```

If authentication fails → authorization never happens.

---

## 🔹 Authentication (Identity Verification)

Authentication is the process of **verifying the identity of a user/system**.

---

### 🔥 How Authentication Works (Conceptually)

```
User → sends credentials → Server verifies → User becomes authenticated
```

---

### 🔹 What are Credentials?

* Username + Password
* JWT Token
* API Key
* OAuth Token

👉 Anything that proves identity.

---

### 🔹 Types of Authentication

---

### 1. Basic Authentication

* Credentials sent in header:

```
Authorization: Basic base64(username:password)
```

❌ Not secure without HTTPS
👉 Rarely used in production APIs

---

### 2. Form-Based Authentication

* Login form (username/password)
* Server creates session

👉 Default in Spring Security

---

### 3. Token-Based Authentication (JWT)

* User logs in once
* Server gives token
* Token sent in every request

👉 Most common in modern apps

---

### 4. OAuth2 Authentication

* Login via Google, GitHub, et3.
* Uses access tokens

---

### 🔹 Authentication in Spring Security (IMPORTANT)

Core classes:

* **Authentication**

    * Represents user identity
    * Contains:

        * Principal (user)
        * Credentials
        * Authorities
        * isAuthenticated flag

---

* **AuthenticationManager**

    * Main entry point
    * Decides how to authenticate

---

* **AuthenticationProvider**

    * Actual logic of authentication
    * Example:

        * DB check
        * LDAP check

---

* **UserDetailsService**

    * Loads user from DB

---

### 🔥 Flow in Spring Security

```
Request → Filter → AuthenticationManager → AuthenticationProvider
        → UserDetailsService → Validate → Authentication object created
        → Stored in SecurityContext
```

👉 If success → user is authenticated
👉 If fail → 401

---

## 🔹 Authorization (Access Control)

Once user is authenticated, next step:

👉 “Are you allowed to access this resource?”

---

### 🔹 How Authorization Works

```
Authenticated User → Check Roles/Permissions → Allow / Deny
```

---

### 🔹 Authorization Types

---

### 1. Role-Based Access Control (RBAC)

Example:

* ADMIN → full access
* USER → limited access

---

### 2. Authority-Based Access

More granular than roles.

Example:

* READ_PRIVILEGE
* WRITE_PRIVILEGE

---

### 🔥 Role vs Authority (VERY IMPORTANT)

| Feature | Role       | Authority      |
| ------- | ---------- | -------------- |
| Prefix  | ROLE_      | No prefix      |
| Level   | High-level | Fine-grained   |
| Example | ROLE_ADMIN | READ_PRIVILEGE |

👉 Internally both are **GrantedAuthority**

---

### 🔹 Authorization in Spring Security

---

### URL-Level Authorization

```java
http.authorizeHttpRequests(auth -> auth
    .requestMatchers("/admin/**").hasRole("ADMIN")
    .requestMatchers("/user/**").hasAnyRole("USER", "ADMIN")
    .anyRequest().authenticated()
);
```

---

### Method-Level Authorization

```java
@PreAuthorize("hasRole('ADMIN')")
public void deleteUser() {}
```

---

### 🔹 Expression-Based Access (SpEL)

```java
@PreAuthorize("hasAuthority('READ_PRIVILEGE')")
```

👉 You can create custom logic too.

---

## 🔥 Authentication vs Authorization (Final Clarity)

| Feature        | Authentication  | Authorization        |
|----------------|-----------------|----------------------|
| Purpose        | Verify identity | Check permissions    |
| Happens First? | Yes             | After authentication |
| Failure Code   | 401             | 403                  |
| Example        | Login           | Access admin API     |

---

## 🔥 Real Request Flow (Combine Both)

```
Request → Authentication (Who are you?)
        → Authorization (Can you access?)
        → Controller
```

---

## 🔴 Real-World Scenario

User tries:

```
DELETE /admin/deleteUser
```

### Case 1:

* No token
  👉 ❌ 401 (Not authenticated)

### Case 2:

* Valid token but USER role
  👉 ❌ 403 (Not authorized)

### Case 3:

* Valid ADMIN token
  👉 ✅ 200 OK

---

## ⚠️ Common Mistakes

* Thinking authentication = authorization ❌
* Returning wrong status codes ❌
* Misusing roles vs authorities ❌
* Skipping authorization checks ❌

---

## 🧠 Interview-Level Insights

* Authentication creates **Authentication object**
* Authorization uses **GrantedAuthority**
* Everything stored in **SecurityContext**
* Filters trigger authentication
* Access decision manager handles authorization

---

## ✅ What You Should Now Be Able To Answer

* Difference between authentication and authorization
* Where each happens in Spring Security
* Role vs authority difference
* Why 401 vs 403 happens
* Complete authentication flow

---

---

# 🔥 EXTREME TRAP MCQs (Top Product Companies – Part 2)

---

Good catch—that’s exactly the kind of attention interviewers like 👍
Here are the **answers + explanations for Q1–Q5**.

---

# 🔥 EXTREME TRAP MCQs (Q1–Q5 Answers)

---

## 1. A request has a valid JWT but no `Authorization` header (token is sent in body). What happens?

1. Authentication succeeds
2. Authorization fails
3. Authentication fails
4. Depends on role configuration

<details>
<summary>Show Answer</summary>

**3. Authentication fails**

Reason:

* Spring Security (by default) reads JWT from **Authorization header**
* Token in body is ignored unless custom logic is written

👉 No header → no authentication → 401

</details>

---

## 2. You configure:

```java
.anyRequest().authenticated()
```

But no authentication mechanism is configure4.

What happens?

1. All requests succeed
2. All requests fail with 403
3. All requests fail with 401
4. Application won’t start

<details>
<summary>Show Answer</summary>

**3. All requests fail with 401**

Reason:

* You’re requiring authentication
* But no way to authenticate → every request is unauthenticated

👉 Result → 401 for all requests

</details>

---

## 3. Which statement is MOST accurate?

1. Authentication always requires a database
2. Authorization can happen without authentication
3. Authentication always happens before authorization
4. Authorization modifies authentication

<details>
<summary>Show Answer</summary>

**3. Authentication always happens before authorization**

Reason:

* System must know “who you are” before deciding “what you can do”

👉 Order is fixed:
Authentication → Authorization

</details>

---

## 4. A request contains:

```http
Authorization: Bearer invalid_token
```

What is the expected response?

1. 200
2. 403
3. 401
4. 500

<details>
<summary>Show Answer</summary>

**3. 401**

Reason:

* Invalid token → authentication fails

👉 Not an authorization problem → authentication problem

</details>

---

## 5. Which scenario can lead to **both 401 and 403 for same API**?

1. Server restart
2. Same user with same role
3. Different users with different authentication states
4. Same token reused

<details>
<summary>Show Answer</summary>

**3. Different users with different authentication states**

Example:

* User 1 → no token → 401
* User 2 → valid token but wrong role → 403

👉 Same API, different outcomes

</details>

---

## 6. You configure:

```java
.requestMatchers("/admin/**").hasAuthority("ADMIN")
```

But user has:

```id="yznc4k"
ROLE_ADMIN
```

👉 What happens?

1. Access granted
2. Access denied
3. 401 returned
4. Depends on JWT

<details>
<summary>Show Answer</summary>

**2. Access denied**

Reason:

* `hasAuthority("ADMIN")` checks for exact match → `ADMIN`
* User has `ROLE_ADMIN`

👉 Mismatch → authorization fails → 403

</details>

---

## 7. You configure:

```java
.requestMatchers("/admin/**").hasRole("ADMIN")
```

User has authority:

```id="c8ydzn"
ADMIN
```

👉 What happens?

1. Access granted
2. Access denied
3. 401
4. Runtime error

<details>
<summary>Show Answer</summary>

**2. Access denied**

Reason:

* `hasRole("ADMIN")` → internally checks `ROLE_ADMIN`
* User has only `ADMIN`

👉 Missing prefix → fails

</details>

---

## 8. Which statement is MOST correct?

1. JWT removes need for authentication
2. JWT is a replacement for authorization
3. JWT is just a way to carry authentication information
4. JWT guarantees security

<details>
<summary>Show Answer</summary>

**3. JWT is just a way to carry authentication information**

👉 It does NOT:

* Replace auth logic
* Guarantee security

</details>

---

## 9. If a request passes authentication but fails authorization, which component decides rejection?

1. AuthenticationManager
2. UserDetailsService
3. AccessDecisionManager / Authorization layer
4. FilterChainProxy

<details>
<summary>Show Answer</summary>

**3. AccessDecisionManager / Authorization layer**

👉 Authentication already done
👉 Authorization layer checks permissions

</details>

---

## 10. You added a JWT filter but forgot to disable CSRF.

👉 What is MOST likely to happen?

1. Everything works fine
2. Only GET works, POST fails
3. JWT stops working
4. Server crashes

<details>
<summary>Show Answer</summary>

**2. Only GET works, POST fails**

Reason:

* CSRF blocks state-changing requests (POST, PUT, DELETE)

👉 Common real-world bug

</details>

---

## 11. A user sends valid JWT but still gets 401.

👉 MOST likely reason?

1. Wrong role
2. Token not parsed in filter
3. Authorization failure
4. Controller issue

<details>
<summary>Show Answer</summary>

**2. Token not parsed in filter**

👉 Authentication didn’t happen at all
→ SecurityContext not set

</details>

---

## 12. Which is TRUE about SecurityContext?

1. Stored in database
2. Stored in ThreadLocal
3. Stored in JWT
4. Stored in headers

<details>
<summary>Show Answer</summary>

**2. Stored in ThreadLocal**

👉 Per-request, per-thread storage

</details>

---

## 13. Which scenario leads to **silent security failure (dangerous)**?

1. Missing JWT
2. Invalid JWT
3. Using `.permitAll()` unintentionally
4. Role mismatch

<details>
<summary>Show Answer</summary>

**3. Using `.permitAll()` unintentionally**

👉 Everything becomes public → no errors → dangerous

</details>

---

## 14. If you don’t set authentication in SecurityContext, what happens?

1. Authorization still works
2. User is treated as authenticated
3. User is treated as anonymous
4. Server crashes

<details>
<summary>Show Answer</summary>

**3. User is treated as anonymous**

👉 Leads to 401 or limited access

</details>

---

## 15. What is the MOST accurate statement?

1. Authentication creates roles
2. Authorization verifies identity
3. Authentication builds Authentication object
4. Authorization creates JWT

<details>
<summary>Show Answer</summary>

**3. Authentication builds Authentication object**

👉 Core internal concept

</details>

---

## 16. Which is TRUE about filter execution?

1. Controller runs before filter
2. Filters run after response
3. Filters run before controller
4. Filters are optional

<details>
<summary>Show Answer</summary>

**3. Filters run before controller**

👉 Spring Security lives here

</details>

---

## 17. A user has valid session but no roles assigne4.

👉 What happens when accessing secured endpoint?

1. 200
2. 401
3. 403
4. 500

<details>
<summary>Show Answer</summary>

**3. 403**

👉 Authenticated but no permissions

</details>

---

## 18. Which is TRUE about stateless authentication?

1. Server remembers user
2. Each request must carry identity
3. Uses session by default
4. Requires cookies

<details>
<summary>Show Answer</summary>

**2. Each request must carry identity**

👉 Core principle of JWT

</details>

---

## 19. If JWT signature is invalid, what happens?

1. Authorization fails
2. Authentication fails
3. Only role check fails
4. Token still works

<details>
<summary>Show Answer</summary>

**2. Authentication fails**

👉 Token is rejected entirely → 401

</details>

---

## 20. Which is the MOST dangerous assumption?

1. JWT is stateless
2. GET is safe
3. Roles are enough for security
4. Authentication equals authorization

<details>
<summary>Show Answer</summary>

**4. Authentication equals authorization**

👉 Biggest real-world mistake

</details>

---

# 🧠 What You Just Mastered

* Role vs Authority traps
* JWT misconceptions
* Filter + SecurityContext pitfalls
* CSRF edge cases
* Silent security bugs

---

# 🔥 If You Can Solve These Easily

👉 You’re already at **top product company level for this module**

---

---

# 🔥 Hard-Level Interview Scenarios (Auth vs Authorization)

---

## 🧩 Scenario 1: 401 vs 403 Confusion

You secured an endpoint:

```
GET /admin/dashboard
```

Config:

```java
.requestMatchers("/admin/**").hasRole("ADMIN")
```

A request comes with a **valid JWT**, but role = `USER`.

👉 What should be returned?

<details>
<summary>Show Answer</summary>

**403 Forbidden**

Reason:

* User is authenticated (valid JWT)
* But lacks required role (ADMIN)

👉 Authentication passed, authorization faile4.

</details>

---

## 🧩 Scenario 2: Missing Token

Same API:

```
GET /admin/dashboard
```

Request:

* No Authorization header

👉 What happens?

<details>
<summary>Show Answer</summary>

**401 Unauthorized**

Reason:

* User is not authenticated at all

👉 Authentication failed → authorization not even checked

</details>

---

## 🧩 Scenario 3: Expired JWT

Request:

```
Authorization: Bearer <expired_token>
```

👉 What should be returned?

<details>
<summary>Show Answer</summary>

**401 Unauthorized**

Reason:

* Token is invalid → authentication fails

👉 Expired token = unauthenticated user

</details>

---

## 🧩 Scenario 4: Wrong Role Prefix Trap

You stored role in DB as:

```
ADMIN
```

But config:

```java
.hasRole("ADMIN")
```

👉 Why might authorization fail?

<details>
<summary>Show Answer</summary>

Spring internally expects:

```
ROLE_ADMIN
```

👉 `hasRole("ADMIN")` → checks for `ROLE_ADMIN`

Fix:

* Store as `ROLE_ADMIN`
  OR
* Use `hasAuthority("ADMIN")`

</details>

---

## 🧩 Scenario 5: Authentication Happens Twice?

You added a custom JWT filter but also enabled:

```java
http.formLogin();
```

👉 What issue might occur?

<details>
<summary>Show Answer</summary>

Multiple authentication mechanisms may conflict:

* Form login tries session-based auth
* JWT filter tries token-based auth

👉 Can cause:

* Unexpected redirects
* Authentication overridden

Fix:

* Disable formLogin for JWT APIs

</details>

---

## 🧩 Scenario 6: SecurityContext Empty

User sends valid JWT, but inside controller:

```java
SecurityContextHolder.getContext().getAuthentication()
```

returns null.

👉 What went wrong?

<details>
<summary>Show Answer</summary>

JWT filter did NOT set authentication into SecurityContext.

👉 Missing step:

```java
SecurityContextHolder.getContext().setAuthentication(auth);
```

</details>

---

## 🧩 Scenario 7: Access Granted Without Authentication

You configured:

```java
.anyRequest().permitAll()
```

But also added JWT filter.

👉 What happens?

<details>
<summary>Show Answer</summary>

All requests will be allowed WITHOUT authentication.

👉 Authorization rule overrides everything.

Even if JWT is present → not require4.

</details>

---

## 🧩 Scenario 8: Method-Level Security Not Working

You added:

```java
@PreAuthorize("hasRole('ADMIN')")
```

But it is ignore4.

👉 Why?

<details>
<summary>Show Answer</summary>

You forgot:

```java
@EnableMethodSecurity
```

👉 Without it, method-level security is disable4.

</details>

---

## 🧩 Scenario 9: Multiple Roles but Access Denied

User has roles:

```
ROLE_USER, ROLE_ADMIN
```

Config:

```java
.hasRole("ADMIN")
```

But still getting 403.

👉 Possible reason?

<details>
<summary>Show Answer</summary>

Authorities not correctly set in Authentication object.

👉 Common issues:

* Roles not loaded from DB
* Missing mapping in UserDetailsService

</details>

---

## 🧩 Scenario 10: JWT Tampering Attack

User modifies payload:

```json
"role": "ADMIN"
```

👉 Will server accept it?

<details>
<summary>Show Answer</summary>

❌ No (if properly implemented)

Reason:

* JWT is signed
* Any change breaks signature

👉 Server will reject token

</details>

---

## 🧩 Scenario 11: API Works Without Login (Security Bug)

You expected authentication, but API is open.

👉 Most likely cause?

<details>
<summary>Show Answer</summary>

Misconfiguration:

* `.permitAll()` used accidentally
* Filter not registered
* SecurityFilterChain not applied

</details>

---

## 🧩 Scenario 12: Authorization Works but Authentication Fails

User gets 401 even with correct credentials.

👉 Possible issue?

<details>
<summary>Show Answer</summary>

AuthenticationProvider issue:

* Password encoder mismatch
* Wrong DB query
* UserDetailsService returning null

</details>

---

## 🧩 Scenario 13: Same User Different Behavior

Same JWT:

* Works for one API
* Fails for another

👉 Why?

<details>
<summary>Show Answer</summary>

Different authorization rules:

* Endpoint requires different roles
* Method-level security applied

👉 Not authentication issue, authorization issue

</details>

---

## 🧩 Scenario 14: Stateless App Using Session

You configured JWT but still see session create4.

👉 Why?

<details>
<summary>Show Answer</summary>

Default session policy not disable4.

Fix:

```java
.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
```

</details>

---

## 🧩 Scenario 15: SecurityContext Lost in Async Call

Inside async method:

```java
SecurityContextHolder.getContext()
```

returns empty.

👉 Why?

<details>
<summary>Show Answer</summary>

SecurityContext uses ThreadLocal.

Async runs in different thread → context lost.

Fix:

* Use DelegatingSecurityContextExecutor
* Or manually propagate context

</details>

---

# 🧠 What You Just Practiced

* Real debugging scenarios
* Filter vs config mistakes
* Role/authority traps
* JWT edge cases
* Spring Security internals

---

# 🔥 If You Can Solve These…

👉 You are already at **top 10% interview level**

---
