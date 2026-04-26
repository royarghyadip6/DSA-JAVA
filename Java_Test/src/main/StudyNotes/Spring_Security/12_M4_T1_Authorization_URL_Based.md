Perfect—this is **core authorization in Spring Security**. Many bugs in real systems come from **URL config + role mismatch**, so we’ll go **deep but clean**.

---

# 🔐 4.1 URL-Based Authorization (Deep Dive)

## 🧠 Core Idea

👉 Authorization = **Who can access which URL**

Spring Security enforces this using:

```java
http.authorizeHttpRequests(...)
```

---

## 🔹 requestMatchers()

## What it does

Matches incoming HTTP requests (URL patterns)

---

### Example

```java
.requestMatchers("/admin/**").hasRole("ADMIN")
```

👉 Meaning:

* Any URL starting with `/admin/`
* Only accessible by ADMIN

---

### Types of matchers

```java
.requestMatchers("/api/**")     // all API endpoints
.requestMatchers("/login")      // specific endpoint
.requestMatchers(HttpMethod.GET, "/users/**") // method-based
```

---

## 🔥 Matching Order (CRITICAL)

👉 Rules are evaluated **top to bottom**

```java
.requestMatchers("/**").permitAll()
.requestMatchers("/admin/**").hasRole("ADMIN") // ❌ never reached
```

👉 First match wins

---

## 🔹 hasRole() vs hasAuthority()

---

### hasRole("ADMIN")

Internally checks:

```text
ROLE_ADMIN
```

---

### hasAuthority("ADMIN")

Checks exactly:

```text
ADMIN
```

---

## 🔥 Key Difference

| Method                | Checks     |
| --------------------- | ---------- |
| hasRole("ADMIN")      | ROLE_ADMIN |
| hasAuthority("ADMIN") | ADMIN      |

---

## ⚠️ Most Common Trap

```java
.roles("ADMIN")
.hasAuthority("ADMIN") ❌
```

👉 Fails because:

* Stored → ROLE_ADMIN
* Checked → ADMIN

---

## ✅ Correct Usage

```java
.hasRole("ADMIN") ✅
```

OR

```java
.authorities("ADMIN")
.hasAuthority("ADMIN") ✅
```

---

## 🔹 permitAll()

## What it does

👉 Allows access **without authentication**

---

### Example

```java
.requestMatchers("/login", "/signup").permitAll()
```

👉 Public endpoints

---

## ⚠️ Important

* User not authenticated
* No SecurityContext required

---

## 🔹 denyAll()

## What it does

👉 Blocks access completely

---

### Example

```java
.requestMatchers("/admin/secret").denyAll()
```

👉 Nobody can access (even ADMIN)

---

## 🔥 Real Configuration Example

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/public/**").permitAll()
            .requestMatchers("/admin/**").hasRole("ADMIN")
            .requestMatchers("/user/**").hasAnyRole("USER", "ADMIN")
            .anyRequest().authenticated()
        )
        .formLogin();

    return http.build();
}
```

---

# 🔥 Internal Flow (Important)

```text
Request
 → Filter Chain
 → Authorization Filter
 → Match requestMatchers
 → Check roles/authorities
 → Allow / Deny
```

---

# ⚠️ Common Real-World Mistakes

---

## ❌ Wrong order of matchers

```java
.requestMatchers("/**").permitAll() ❌
```

---

## ❌ ROLE_ prefix mismatch

```java
.hasAuthority("ADMIN") ❌
```

---

## ❌ Forgetting `.anyRequest().authenticated()`

👉 Leaves endpoints unprotected

---

## ❌ Overlapping patterns

```java
/api/** and /api/admin/**
```

👉 Order matters

---

# 🔥 Advanced Concepts

---

## hasAnyRole()

```java
.hasAnyRole("USER", "ADMIN")
```

👉 Multiple roles allowed

---

## hasAnyAuthority()

```java
.hasAnyAuthority("READ", "WRITE")
```

---

## Method-specific security

```java
.requestMatchers(HttpMethod.GET, "/api/**").permitAll()
.requestMatchers(HttpMethod.POST, "/api/**").hasRole("ADMIN")
```

---

# 🧠 Debugging Perspective

---

## If always getting 403:

* Role mismatch
* Wrong matcher order
* Authority not present

---

## If endpoint open unexpectedly:

* permitAll applied early
* Pattern too broad

---

# 🧠 Final Mental Model

* requestMatchers → select URL
* hasRole/Authority → check permission
* permitAll → open access
* denyAll → block completely
* Order → defines behavior

---

# 🔥 Interview Killer Statement

👉 *“Spring Security evaluates URL authorization rules in order, and roles are internally mapped to authorities with a ROLE_ prefix, making correct matcher order and role mapping critical.”*

---

# ✅ What You Should Now Know

* How URL-based authorization works
* Role vs authority checks
* Matcher order importance
* Common pitfalls

---

---

# 🔥 Extreme MCQs (URL Authorization Traps)

---

## 1. What happens if this config is used?

```java
.requestMatchers("/**").permitAll()
.requestMatchers("/admin/**").hasRole("ADMIN")
```

1. Admin endpoints secured
2. All endpoints open
3. Only admin secured
4. Exception

<details>
<summary>Show Answer</summary>

**2. All endpoints open**

👉 First matcher catches everything

</details>

---

## 2. Which is TRUE about matcher evaluation?

1. Random order
2. Bottom to top
3. Top to bottom
4. Alphabetical

<details>
<summary>Show Answer</summary>

**3. Top to bottom**

👉 First match wins

</details>

---

## 3. What does `hasRole("ADMIN")` internally check?

1. ADMIN
2. ROLE_ADMIN
3. admin
4. Any role

<details>
<summary>Show Answer</summary>

**2. ROLE_ADMIN**

</details>

---

## 4. If DB stores `ROLE_ADMIN`, which check works?

1. hasAuthority("ADMIN")
2. hasAuthority("ROLE_ADMIN")
3. hasRole("ROLE_ADMIN")
4. hasAnyAuthority("ADMIN")

<details>
<summary>Show Answer</summary>

**2. hasAuthority("ROLE_ADMIN")**

👉 OR hasRole("ADMIN") also works

</details>

---

## 5. What happens if role prefix mismatches?

1. Login fails
2. Access denied (403)
3. App crashes
4. Ignored

<details>
<summary>Show Answer</summary>

**2. Access denied (403)**

</details>

---

## 6. What happens if `.anyRequest().authenticated()` is missing?

1. All endpoints secured
2. Some endpoints unsecured
3. App fails
4. No effect

<details>
<summary>Show Answer</summary>

**2. Some endpoints unsecured**

</details>

---

## 7. What happens if `denyAll()` is applied?

1. Only admins blocked
2. Everyone blocked
3. Anonymous allowed
4. Ignored

<details>
<summary>Show Answer</summary>

**2. Everyone blocked**

</details>

---

## 8. What happens if user is not authenticated and hits protected endpoint?

1. 403
2. 401
3. 404
4. 500

<details>
<summary>Show Answer</summary>

**2. 401**

👉 Not authenticated

</details>

---

## 9. What happens if user is authenticated but lacks role?

1. 401
2. 403
3. 404
4. 200

<details>
<summary>Show Answer</summary>

**2. 403**

👉 Authorization failure

</details>

---

## 10. Which matcher is more specific?

1. /**
2. /api/**
3. /api/admin/**
4. All same

<details>
<summary>Show Answer</summary>

**3. /api/admin/****

👉 More specific pattern

</details>

---

## 11. What happens if specific matcher comes after generic?

```java
.requestMatchers("/api/**").permitAll()
.requestMatchers("/api/admin/**").hasRole("ADMIN")
```

1. Admin secured
2. Admin open
3. Exception
4. Random

<details>
<summary>Show Answer</summary>

**2. Admin open**

👉 Generic matcher captures first

</details>

---

## 12. What does `permitAll()` bypass?

1. Authentication only
2. Authorization only
3. Both
4. Neither

<details>
<summary>Show Answer</summary>

**1. Authentication only**

👉 Still passes through filters

</details>

---

## 13. What happens if authorities list is empty?

1. Login fails
2. Full access
3. 403 on protected endpoints
4. Ignored

<details>
<summary>Show Answer</summary>

**3. 403 on protected endpoints**

</details>

---

## 14. Which is correct for multiple roles?

1. hasRole("USER,ADMIN")
2. hasAnyRole("USER","ADMIN")
3. hasAuthority("USER","ADMIN")
4. hasRole("USER","ADMIN")

<details>
<summary>Show Answer</summary>

**2. hasAnyRole("USER","ADMIN")**

</details>

---

## 15. What happens if no matcher matches request?

1. Allowed
2. Denied
3. Depends on config
4. Exception

<details>
<summary>Show Answer</summary>

**3. Depends on config**

👉 If `.anyRequest()` exists → applies
👉 Otherwise may be open

</details>

---

# 🔥 Ultra Trap Questions

---

## 🧩 Q1: Endpoint unexpectedly open

<details>
<summary>Show Answer</summary>

Cause:

* permitAll applied too early
* Pattern too broad

👉 Order issue

</details>

---

## 🧩 Q2: Always getting 403 even with correct role

<details>
<summary>Show Answer</summary>

* ROLE_ prefix mismatch
* Wrong authority mapping

</details>

---

## 🧩 Q3: Getting 401 instead of 403

<details>
<summary>Show Answer</summary>

User is not authenticated.

👉 401 = authentication issue
👉 403 = authorization issue

</details>

---

## 🧩 Q4: Why `/admin/**` not secured?

<details>
<summary>Show Answer</summary>

* Covered by earlier matcher
* Wrong order

</details>

---

## 🧩 Q5: Public endpoint requires login

<details>
<summary>Show Answer</summary>

* permitAll not applied
* Matcher not matching correctly

</details>

---

# 🧠 Final Mental Model

* Order → first match wins
* hasRole → adds ROLE_
* hasAuthority → exact match
* 401 → not authenticated
* 403 → not authorized

---

# 🔥 Interview Killer Statement

👉 *“In Spring Security, authorization bugs are usually caused by matcher order or role prefix mismatches rather than incorrect business logic.”*

---

---

# 🔥 401 vs 403 — First Principle (Never Forget)

* **401 (Unauthorized)** → ❌ *User NOT authenticated*
* **403 (Forbidden)** → ❌ *User authenticated BUT not allowed*

👉 This distinction drives everything.

---

# 🔥 Real Debugging Scenarios (Deep Dive)

---

## 🧩 Scenario 1: You expect 403 but getting 401

👉 API requires ADMIN role
👉 You hit it without login

Result: **401**

<details>
<summary>Why?</summary>

User is not authenticated yet.

👉 Authorization doesn’t even run
👉 Authentication fails first

</details>

---

## 🧩 Scenario 2: You expect 401 but getting 403

👉 You are logged in
👉 But role is missing

Result: **403**

<details>
<summary>Why?</summary>

Authentication successful
Authorization failed

👉 Access denied

</details>

---

## 🧩 Scenario 3: Always getting 401 even after login

👉 Very common in JWT setups

<details>
<summary>Root Cause</summary>

Authentication not being set in SecurityContext:

```java
SecurityContextHolder.getContext().setAuthentication(auth);
```

Missing or incorrect

👉 Spring thinks user is unauthenticated

</details>

---

## 🧩 Scenario 4: Always getting 403 even with correct role

👉 You verified role is correct

<details>
<summary>Root Cause</summary>

ROLE prefix mismatch:

* Stored → ROLE_ADMIN
* Checked → hasAuthority("ADMIN")

👉 Authorization fails

</details>

---

## 🧩 Scenario 5: Public API returning 401

```java
.requestMatchers("/public/**").permitAll()
```

👉 Still asks for authentication

<details>
<summary>Root Cause</summary>

Possible reasons:

* Wrong URL pattern
* Filter blocking before authorization
* Security config not applied

👉 permitAll affects authorization, not filters

</details>

---

## 🧩 Scenario 6: JWT present but still 401

👉 Token is sent correctly

<details>
<summary>Root Cause</summary>

* Token not parsed correctly
* Authentication not set in context
* Filter not registered properly

👉 Authentication step failed

</details>

---

## 🧩 Scenario 7: Login works, but next request gives 401

👉 Stateful vs Stateless issue

<details>
<summary>Root Cause</summary>

* No session stored
* JWT not sent in next request
* Stateless config without token

👉 Authentication lost between requests

</details>

---

## 🧩 Scenario 8: Correct role, but still 403

👉 Debug shows correct authority

<details>
<summary>Root Cause</summary>

* Method-level security mismatch
* URL vs method security conflict
* Case sensitivity issues

</details>

---

## 🧩 Scenario 9: 403 returned before hitting controller

👉 Controller not even executed

<details>
<summary>Root Cause</summary>

Authorization filter blocked request.

👉 Happens in filter chain, not controller

</details>

---

## 🧩 Scenario 10: 401 returned for some APIs but not others

<details>
<summary>Root Cause</summary>

* Different security rules
* Different matchers
* Filter conditions

👉 Config inconsistency

</details>

---

# 🔥 Multi-Layer Failure Scenario (Very Important)

---

## 🧩 Scenario 11: Full confusion (401 + 403 mix)

Symptoms:

* Some APIs → 401
* Some → 403
* Same user

<details>
<summary>Root Cause</summary>

Combination of:

1. Missing authentication in some flows
2. Role mismatch in others
3. Incorrect matcher order

👉 Multi-layer issue:
Filter + Auth + Authorization

</details>

---

# 🔥 Internal Flow (Key to Debugging)

```text
Request
 ↓
Authentication Filter
   → If fails → 401
 ↓
Authorization Filter
   → If fails → 403
 ↓
Controller
```

---

# 🔥 Debugging Strategy (Step-by-Step)

---

## Step 1: Check Status Code

* 401 → authentication issue
* 403 → authorization issue

---

## Step 2: Check Authentication

* Is token/session present?
* Is SecurityContext populated?

---

## Step 3: Check Authorization

* Correct role?
* ROLE_ prefix?
* Correct matcher?

---

## Step 4: Check Filters

* JWT filter executed?
* Authentication set?

---

## Step 5: Enable Debug Logs

```properties
logging.level.org.springframework.security=DEBUG
```

👉 You’ll see:

* Authentication success/failure
* Authorization decisions

---

# 🔥 Real Logs Example (What to Look For)

---

## 401 case

```text
Failed to authenticate request
```

👉 Authentication failure

---

## 403 case

```text
Access is denied
```

👉 Authorization failure

---

# 🧠 Final Mental Model

* Authentication happens first
* Authorization happens next
* 401 = identity missing
* 403 = permission missing

---

# 🔥 Interview Killer Statement

👉 *“401 indicates failure in authentication (identity not established), while 403 indicates failure in authorization (identity established but insufficient permissions), and the distinction directly maps to different stages in the Spring Security filter chain.”*

---
