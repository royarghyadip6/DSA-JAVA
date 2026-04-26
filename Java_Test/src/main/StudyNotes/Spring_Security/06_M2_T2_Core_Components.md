Perfect—this is the **heart of Spring Security internals**.
If you truly understand these 6 components, everything (JWT, OAuth, filters) becomes crystal clear.

I’ll keep it **compact, deep, and connected** so you can build a strong mental model.

---

# 🔐 MODULE 2.2: Core Components (Deep Dive)

## 🧠 First Understand the Flow (Big Picture)

```text
Request → Authentication → SecurityContext → Authorization
```

👉 These components work **together**, not in isolation.

---

## 🔹 1. SecurityContext

### What it is

A container that holds **security information of the current user**.

### What it stores

* `Authentication` object (main thing)

### Why it exists

👉 So Spring Security can access **current logged-in user anywhere**

---

### Example (concept)

```java
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
```

---

### Key Points

* Created per request
* Cleared after request
* Central place for user security data

---

## 🔹 2. SecurityContextHolder

### What it is

A **utility class** to access `SecurityContext`.

---

### How it works

Stores SecurityContext using:
👉 **ThreadLocal**

---

### Why ThreadLocal?

* Each request runs in separate thread
* Keeps user data isolated per request

---

### Modes (Advanced but important)

```text
MODE_THREADLOCAL (default)
MODE_INHERITABLETHREADLOCAL
MODE_GLOBAL
```

👉 Most used: **ThreadLocal**

---

### Key Insight

👉 You never create SecurityContext manually
👉 You always access it via SecurityContextHolder

---

## 🔹 3. Authentication

### What it is

Represents **current user identity + status**

---

### It contains:

* Principal → User (who you are)
* Credentials → Password/token
* Authorities → Roles/permissions
* isAuthenticated → true/false

---

### Example structure

```text
Authentication
 ├── Principal (UserDetails)
 ├── Credentials (password)
 ├── Authorities (ROLE_USER)
 └── Authenticated (true/false)
```

---

### Lifecycle

1. Created (unauthenticated)
2. Validated by AuthenticationManager
3. Marked as authenticated
4. Stored in SecurityContext

---

### Key Insight

👉 This is the **core object used everywhere**

---

## 🔹 4. GrantedAuthority

### What it is

Represents a **permission or role**

---

### Example

```text
ROLE_USER
ROLE_ADMIN
READ_PRIVILEGE
```

---

### Why needed

👉 Authorization decisions depend on this

---

### Important Difference

* Role = high-level (ROLE_ADMIN)
* Authority = fine-grained permission

---

### Key Insight

👉 Spring Security internally works with **authorities**, not roles

---

## 🔹 5. UserDetails

### What it is

A **representation of user data** used by Spring Security

---

### It contains:

* Username
* Password
* Authorities
* Account status flags:

    * accountNonExpired
    * accountNonLocked
    * credentialsNonExpired
    * enabled

---

### Example (concept)

```text
UserDetails
 ├── username
 ├── password (hashed)
 ├── authorities
 └── account status flags
```

---

### Why needed

👉 Standard format for user information

---

### Key Insight

👉 Spring Security does NOT use your DB entity directly
👉 It uses **UserDetails abstraction**

---

## 🔹 6. UserDetailsService

### What it is

A **service to load user data**

---

### Main method

```java
loadUserByUsername(String username)
```

---

### What it does

* Fetch user from DB
* Convert to UserDetails
* Return to Spring Security

---

### Flow

```text
Login request
→ UserDetailsService
→ Fetch user from DB
→ Return UserDetails
```

---

### Key Insight

👉 This is where **DB interaction happens**

---

## 🔥 How Everything Connects (MOST IMPORTANT)

---

### Full Flow

```text
1. User sends login request
2. UserDetailsService loads user
3. AuthenticationManager validates credentials
4. Authentication object created
5. Stored in SecurityContext
6. Authorization uses authorities
```

---

### Visual Mapping

```text
UserDetailsService → UserDetails
                        ↓
                 Authentication
                        ↓
               SecurityContext
                        ↓
                Authorization (using GrantedAuthority)
```

---

## 🧠 Final Mental Model

* `UserDetailsService` → loads user
* `UserDetails` → user data
* `Authentication` → current login state
* `SecurityContext` → holds authentication
* `SecurityContextHolder` → access point
* `GrantedAuthority` → permissions

---

## ⚠️ Common Mistakes

* Confusing UserDetails with entity ❌
* Not setting Authentication in context ❌
* Ignoring authorities ❌
* Misunderstanding ThreadLocal ❌

---

## 🔥 Interview Gold Lines

* “SecurityContext stores Authentication object per request”
* “Authentication represents the currently logged-in user”
* “UserDetailsService loads user data from DB”
* “Spring Security uses authorities internally for authorization”

---

## ✅ What You Should Now Be Able to Explain

* How user data flows through system
* Where authentication is stored
* How roles/permissions are checked
* How Spring fetches users

---

---

# 🔥 PART 1: Real Interview Scenarios (Hard Level)

---

## 🧩 Scenario 1: `SecurityContext` is null

You call:

```java
SecurityContextHolder.getContext().getAuthentication()
```

and it returns `null`.

👉 Why?

<details>
<summary>Show Answer</summary>

Most likely reasons:

* Authentication not set in filter
* Filter not executed
* Request is unauthenticated
* SecurityContext not initialized

👉 Common bug:
Forgot:

```java
SecurityContextHolder.getContext().setAuthentication(auth);
```

</details>

---

## 🧩 Scenario 2: User authenticated but always gets 403

User logs in successfully, but every API returns **403**.

👉 What’s wrong?

<details>
<summary>Show Answer</summary>

Authorization issue:

* No authorities assigned
* Role mismatch (`ROLE_` prefix issue)
* Wrong configuration (`hasRole` vs `hasAuthority`)

👉 Authentication works, authorization fails

</details>

---

## 🧩 Scenario 3: Custom User Entity not working

You passed your entity directly instead of `UserDetails`.

👉 What happens?

<details>
<summary>Show Answer</summary>

Spring Security expects `UserDetails`.

👉 If not:

* Authorities not recognized
* Authentication may fail
* Authorization will definitely fail

Fix:

* Implement `UserDetails`

</details>

---

## 🧩 Scenario 4: Same user works in one API but fails in another

Same JWT/token, different endpoints behave differently.

👉 Why?

<details>
<summary>Show Answer</summary>

Authorization rules differ:

* Different roles required
* Method-level security applied
* Endpoint-specific config

👉 Not authentication issue

</details>

---

## 🧩 Scenario 5: Async call loses authentication

Inside async method:

```java
SecurityContextHolder.getContext()
```

is empty.

👉 Why?

<details>
<summary>Show Answer</summary>

SecurityContext uses **ThreadLocal**.

Async → new thread → context not available.

Fix:

* Use DelegatingSecurityContextExecutor
* Or manually pass context

</details>

---

## 🧩 Scenario 6: UserDetailsService not called

Login fails, and breakpoint in `loadUserByUsername` never hits.

👉 Why?

<details>
<summary>Show Answer</summary>

Possible reasons:

* Wrong authentication configuration
* Custom AuthenticationProvider bypassing it
* Filter not triggering authentication

</details>

---

## 🧩 Scenario 7: Password matches but login fails

You stored password correctly, but login still fails.

👉 What could be wrong?

<details>
<summary>Show Answer</summary>

PasswordEncoder mismatch.

Example:

* Stored with BCrypt
* Compared with NoOp

👉 Always use same encoder

</details>

---

## 🧩 Scenario 8: Authorities present but still access denied

Authentication object shows:

```text
ROLE_ADMIN
```

But API returns 403.

👉 Why?

<details>
<summary>Show Answer</summary>

Possible issues:

* Using `hasAuthority("ADMIN")` instead of `ROLE_ADMIN`
* Method-level security mismatch
* Incorrect mapping of authorities

</details>

---

## 🧩 Scenario 9: Authentication always anonymous

Even after login, user appears as anonymous.

👉 Why?

<details>
<summary>Show Answer</summary>

Authentication not stored in SecurityContext.

Or:

* Stateless app without JWT processing
* Filter missing

</details>

---

## 🧩 Scenario 10: Multiple users share same context (critical bug)

User A sees User B’s data.

👉 What’s wrong?

<details>
<summary>Show Answer</summary>

SecurityContext not isolated properly.

Possible causes:

* Using global mode instead of ThreadLocal
* Manual misconfiguration

👉 Extremely dangerous bug

</details>

---

# 🔥 PART 2: EXTREME TRAP MCQs

---

## 1. What does `SecurityContextHolder` actually store?

1. Authentication directly
2. SecurityContext
3. UserDetails
4. JWT token

<details>
<summary>Show Answer</summary>

**2. SecurityContext**

</details>

---

## 2. What is the MOST important object for authorization?

1. UserDetails
2. Authentication
3. SecurityContextHolder
4. PasswordEncoder

<details>
<summary>Show Answer</summary>

**2. Authentication**

👉 It contains authorities

</details>

---

## 3. What happens if `Authentication.isAuthenticated()` is false?

1. Authorization passes
2. User treated as anonymous
3. Request succeeds
4. Controller decides

<details>
<summary>Show Answer</summary>

**2. User treated as anonymous**

</details>

---

## 4. Which component loads user from database?

1. AuthenticationManager
2. AuthenticationProvider
3. UserDetailsService
4. SecurityContext

<details>
<summary>Show Answer</summary>

**3. UserDetailsService**

</details>

---

## 5. Which is TRUE about `GrantedAuthority`?

1. Stores password
2. Represents roles/permissions
3. Stores session
4. Handles authentication

<details>
<summary>Show Answer</summary>

**2. Represents roles/permissions**

</details>

---

## 6. If `SecurityContext` is empty, what happens?

1. Authorization still works
2. User is authenticated
3. User is anonymous
4. System crashes

<details>
<summary>Show Answer</summary>

**3. User is anonymous**

</details>

---

## 7. What is the MOST common mistake with roles?

1. Missing password
2. Wrong encoding
3. Missing `ROLE_` prefix
4. Wrong username

<details>
<summary>Show Answer</summary>

**3. Missing `ROLE_` prefix**

</details>

---

## 8. What does `UserDetailsService` return?

1. Authentication
2. UserDetails
3. JWT
4. SecurityContext

<details>
<summary>Show Answer</summary>

**2. UserDetails**

</details>

---

## 9. What is stored inside `Authentication.getPrincipal()`?

1. Password
2. UserDetails
3. JWT token
4. Authorities

<details>
<summary>Show Answer</summary>

**2. UserDetails**

</details>

---

## 10. Why is `SecurityContext` cleared after request?

1. Save memory
2. Avoid data leak between requests
3. Improve performance
4. Required by JVM

<details>
<summary>Show Answer</summary>

**2. Avoid data leak between requests**

</details>

---

## 11. What happens if authorities list is empty?

1. User gets full access
2. Authentication fails
3. Authorization fails
4. Nothing happens

<details>
<summary>Show Answer</summary>

**3. Authorization fails**

</details>

---

## 12. Which is TRUE about `UserDetails`?

1. It is a DB entity
2. It is a Spring Security abstraction
3. It stores JWT
4. It performs authentication

<details>
<summary>Show Answer</summary>

**2. It is a Spring Security abstraction**

</details>

---

## 13. What happens if `UserDetailsService` returns null?

1. Authentication succeeds
2. Exception occurs
3. Authorization succeeds
4. Request ignored

<details>
<summary>Show Answer</summary>

**2. Exception occurs**

</details>

---

## 14. What is the role of `AuthenticationManager`?

1. Load user
2. Validate credentials
3. Store context
4. Handle HTTP

<details>
<summary>Show Answer</summary>

**2. Validate credentials**

</details>

---

## 15. Which is the MOST dangerous mistake?

1. Wrong role
2. Empty authorities
3. Not clearing SecurityContext
4. Using BCrypt

<details>
<summary>Show Answer</summary>

**3. Not clearing SecurityContext**

👉 Leads to user data leakage

</details>

---

# 🧠 What You Just Mastered

* Internal object relationships
* Real debugging mindset
* Authority/role traps
* ThreadLocal behavior
* Production-level issues

---

# 🔥 Interview Insight

These are exactly the questions where candidates fail:

* “Why SecurityContext is empty?”
* “Why 403 after login?”
* “Why async breaks security?”

👉 You can now answer all of them.

---
