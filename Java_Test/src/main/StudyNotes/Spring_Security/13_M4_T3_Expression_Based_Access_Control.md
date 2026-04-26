Good—this is where Spring Security becomes **very powerful (and very tricky)**. Most developers *use* `@PreAuthorize`, but interviews expect you to understand **SpEL evaluation + custom expressions deeply**.

---

# 🔐 4.3 Expression-Based Access Control (Deep Dive)

## 🧠 Core Idea

👉 Instead of simple role checks, you write **dynamic authorization rules using SpEL (Spring Expression Language)**

Example:

```java
@PreAuthorize("#id == authentication.principal.id")
```

👉 Meaning:

* Only owner can access their data

---

# 🔹 1. SpEL in Spring Security

## What is SpEL?

👉 A powerful expression language used to evaluate conditions at runtime

---

## Where it is used

* `@PreAuthorize`
* `@PostAuthorize`
* URL config (`access()`)

---

## Basic Syntax

```text
#variable → method parameter  
authentication → current user  
principal → logged-in user object  
returnObject → method result (PostAuthorize)
```

---

# 🔥 Common Expressions (Must Know)

---

## Role/Authority checks

```java
@PreAuthorize("hasRole('ADMIN')")
@PreAuthorize("hasAuthority('READ')")
```

---

## Authentication checks

```java
@PreAuthorize("isAuthenticated()")
@PreAuthorize("isAnonymous()")
```

---

## Logical conditions

```java
@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
@PreAuthorize("hasRole('ADMIN') and #id == authentication.principal.id")
```

---

## Parameter-based checks

```java
@PreAuthorize("#userId == authentication.principal.id")
```

---

## Access return object

```java
@PostAuthorize("returnObject.owner == authentication.name")
```

---

# 🔥 Real Examples (Important)

---

## Example 1: Owner or Admin

```java
@PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
public User getUser(Long id) {}
```

---

## Example 2: Check request parameter

```java
@PreAuthorize("#email == authentication.name")
```

---

## Example 3: Role + permission

```java
@PreAuthorize("hasAuthority('WRITE') and hasRole('ADMIN')")
```

---

# 🔴 Key Context Objects

---

## authentication

```text
authentication.name → username  
authentication.principal → UserDetails  
authentication.authorities → roles
```

---

## principal

👉 Your logged-in user object

```java
authentication.principal.getId()
```

---

## method parameters

```java
#id, #username
```

---

## returnObject (PostAuthorize only)

👉 Used after method execution

---

# ⚠️ Common SpEL Traps

---

## ❌ Parameter name not working

```java
@PreAuthorize("#id == ...")
```

👉 Might fail due to missing debug info

✔ Fix:

```java
@PreAuthorize("#p0 == ...")
```

---

## ❌ ROLE_ mismatch

```java
hasAuthority("ADMIN") ❌
```

---

## ❌ Null pointer in PostAuthorize

```java
returnObject.owner
```

👉 If returnObject is null → crash

---

## ❌ Complex expressions become unreadable

👉 Avoid over-complicating

---

# 🔹 2. Custom Expressions (ADVANCED)

---

## Why needed?

Default expressions are limited.

👉 Real systems need:

* Business rules
* DB checks
* External validation

---

## Approach 1: Use Spring Bean in SpEL

---

### Step 1: Create bean

```java
@Component("authService")
public class AuthService {

    public boolean canAccess(Long userId, Authentication auth) {
        return userId.equals(((User) auth.getPrincipal()).getId());
    }
}
```

---

### Step 2: Use in expression

```java
@PreAuthorize("@authService.canAccess(#id, authentication)")
public User getUser(Long id) {}
```

---

## 🔥 Key Insight

👉 `@beanName.method()` allows custom logic

---

## Approach 2: Custom Permission Evaluator

👉 Used for advanced access control

---

### Example

```java
@PreAuthorize("hasPermission(#id, 'USER', 'READ')")
```

👉 Internally calls custom logic

---

## When to use this?

* Fine-grained permissions
* Object-level security
* Complex business rules

---

# 🔥 Internal Flow

```text
Request
 ↓
Authentication
 ↓
Method Security
 ↓
SpEL Expression Evaluated
 ↓
Allow / Deny
```

---

# 🧠 Debugging Perspective

---

## If expression not working:

* Parameter name mismatch
* Principal not mapped correctly
* Bean not found

---

## If always false:

* Wrong role/authority
* Expression logic wrong

---

## If exception thrown:

* Null values
* Invalid SpEL syntax

---

# 🔥 Real-World Example (Full)

```java
@PreAuthorize(
    "hasRole('ADMIN') or " +
    "(hasRole('USER') and #id == authentication.principal.id)"
)
public User getUser(Long id) {}
```

👉 Meaning:

* Admin → full access
* User → only own data

---

# 🧠 Final Mental Model

* SpEL = dynamic authorization engine
* @PreAuthorize = before execution
* @PostAuthorize = after execution
* Custom beans = extend logic

---

# 🔥 Interview Killer Statement

👉 *“Spring Security uses SpEL to evaluate authorization rules dynamically at runtime, allowing fine-grained, context-aware access control based on user identity, roles, and method parameters.”*

---

# ✅ What You Should Now Know

* SpEL syntax
* Built-in expressions
* Custom expression usage
* Common traps

---

---

# 🔥 Extreme MCQs (SpEL Traps in Spring Security)

---

## 1. What happens if you forget `#` before method parameter?

```java
@PreAuthorize("id == authentication.name")
```

1. Works correctly
2. Always true
3. Always false
4. Runtime exception

<details>
<summary>Show Answer</summary>

**4. Runtime exception**

👉 `id` not recognized → SpEL evaluation fails

</details>

---

## 2. What does `#p0` refer to?

1. First parameter
2. Principal
3. Password
4. Property

<details>
<summary>Show Answer</summary>

**1. First parameter**

</details>

---

## 3. What happens if parameter name is not available at runtime?

1. Works fine
2. Compile error
3. Expression fails
4. Ignored

<details>
<summary>Show Answer</summary>

**3. Expression fails**

👉 Use `#p0`, `#p1` instead

</details>

---

## 4. What happens if `returnObject` is null in @PostAuthorize?

1. Returns null
2. Expression skipped
3. Runtime exception
4. Always true

<details>
<summary>Show Answer</summary>

**3. Runtime exception**

</details>

---

## 5. What does `authentication` represent?

1. UserDetails
2. String username
3. Authentication object
4. JWT

<details>
<summary>Show Answer</summary>

**3. Authentication object**

</details>

---

## 6. What happens if you use wrong role in SpEL?

```java
hasRole("ADMIN")
```

But DB has `ADMIN`

1. Works
2. 401
3. 403
4. Ignored

<details>
<summary>Show Answer</summary>

**3. 403**

👉 Missing `ROLE_` prefix

</details>

---

## 7. What happens if SpEL syntax is invalid?

1. Ignored
2. Compile error
3. Runtime exception
4. Always false

<details>
<summary>Show Answer</summary>

**3. Runtime exception**

</details>

---

## 8. What does `principal` represent?

1. Password
2. UserDetails
3. Role
4. Token

<details>
<summary>Show Answer</summary>

**2. UserDetails**

</details>

---

## 9. What happens if you call non-existent method in SpEL?

```java
@PreAuthorize("@authService.check(#id)")
```

Method not present

1. Ignored
2. Runtime exception
3. Always false
4. Compile error

<details>
<summary>Show Answer</summary>

**2. Runtime exception**

</details>

---

## 10. What happens if bean name is incorrect?

```java
@PreAuthorize("@wrongBean.check(#id)")
```

1. Ignored
2. Runtime exception
3. Always false
4. Works

<details>
<summary>Show Answer</summary>

**2. Runtime exception**

</details>

---

## 11. What happens if `authentication.principal` is cast incorrectly?

1. Works
2. Runtime exception
3. Always null
4. Ignored

<details>
<summary>Show Answer</summary>

**2. Runtime exception**

</details>

---

## 12. Which is correct way to access username?

1. authentication
2. authentication.name
3. principal.username
4. user.name

<details>
<summary>Show Answer</summary>

**2. authentication.name**

</details>

---

## 13. What happens if expression always evaluates to false?

1. Method executes
2. 401
3. 403
4. Ignored

<details>
<summary>Show Answer</summary>

**3. 403**

</details>

---

## 14. What happens if SpEL returns null?

1. Treated as true
2. Treated as false
3. Exception
4. Ignored

<details>
<summary>Show Answer</summary>

**2. Treated as false**

</details>

---

## 15. What happens if logical operator is wrong?

```java
hasRole('ADMIN') & hasRole('USER')
```

1. Works
2. Compile error
3. Runtime exception
4. Always false

<details>
<summary>Show Answer</summary>

**3. Runtime exception**

👉 Use `and`, not `&`

</details>

---

# 🔥 Ultra Trap Questions

---

## 🧩 Q1: Expression works locally but fails in production

<details>
<summary>Show Answer</summary>

* Parameter names not available
* Missing debug symbols

👉 Use `#p0`

</details>

---

## 🧩 Q2: Custom bean method not called

<details>
<summary>Show Answer</summary>

* Wrong bean name
* Bean not registered

</details>

---

## 🧩 Q3: Always getting 403 even with correct role

<details>
<summary>Show Answer</summary>

* ROLE_ mismatch
* Expression logic wrong

</details>

---

## 🧩 Q4: PostAuthorize causing crash

<details>
<summary>Show Answer</summary>

* returnObject is null
* Expression accessing property

</details>

---

## 🧩 Q5: Parameter-based expression not working

<details>
<summary>Show Answer</summary>

* Missing `#`
* Wrong parameter name
* Not compiled with debug info

</details>

---

# 🧠 Final Mental Model

* SpEL runs at runtime
* Errors → runtime exceptions
* # → access parameters
* authentication → current user
* principal → user object

---

# 🔥 Interview Killer Statement

👉 *“SpEL expressions in Spring Security are evaluated at runtime using the authentication context, and most failures arise from incorrect parameter binding, role prefix mismatches, or missing bean references.”*

---

---

# 🔥 Real Debugging Scenarios (SpEL Expression Failures)

---

## 🧩 Scenario 1: Expression always returns false

```java
@PreAuthorize("#id == authentication.principal.id")
```

👉 Even for correct user → **403**

<details>
<summary>Root Cause</summary>

Type mismatch:

* `#id` → Long
* `principal.id` → String

👉 Comparison fails

</details>

💡 Fix:

* Align types
* Convert explicitly if needed

---

## 🧩 Scenario 2: Parameter not recognized

```java
@PreAuthorize("#userId == authentication.name")
```

👉 Always fails

<details>
<summary>Root Cause</summary>

Parameter name not available at runtime.

👉 Spring cannot resolve `#userId`

</details>

💡 Fix:

```java
@PreAuthorize("#p0 == authentication.name")
```

---

## 🧩 Scenario 3: Runtime exception in expression

👉 Error: *EL1008E or EL1011E*

<details>
<summary>Root Cause</summary>

Accessing null object:

```java
returnObject.owner
```

👉 returnObject is null

</details>

💡 Fix:

```java
@PostAuthorize("returnObject != null and returnObject.owner == authentication.name")
```

---

## 🧩 Scenario 4: Custom bean not working

```java
@PreAuthorize("@authService.canAccess(#id)")
```

👉 Method never called

<details>
<summary>Root Cause</summary>

* Bean name mismatch
* Bean not registered

👉 SpEL cannot resolve bean

</details>

💡 Fix:

```java
@Component("authService")
```

---

## 🧩 Scenario 5: Correct role, still 403

```java
@PreAuthorize("hasRole('ADMIN')")
```

👉 User has ADMIN role but denied

<details>
<summary>Root Cause</summary>

ROLE prefix mismatch:

* Stored → ADMIN
* Expected → ROLE_ADMIN

</details>

💡 Fix:

* Use `hasAuthority("ADMIN")`
  OR
* Store ROLE_ADMIN

---

## 🧩 Scenario 6: Expression works locally but fails in production

👉 Very common

<details>
<summary>Root Cause</summary>

* Parameter names not retained in compiled code
* Debug symbols missing

</details>

💡 Fix:

* Use `#p0`, `#p1`
  OR
* Compile with `-parameters`

---

## 🧩 Scenario 7: Expression not executed at all

👉 Method executes without restriction

<details>
<summary>Root Cause</summary>

Proxy issue:

* Self-invocation
* Bean not managed

👉 SpEL never evaluated

</details>

💡 Fix:

* Call via Spring bean
* Avoid internal calls

---

## 🧩 Scenario 8: Principal casting failure

```java
@PreAuthorize("#id == ((User)authentication.principal).id")
```

👉 Runtime exception

<details>
<summary>Root Cause</summary>

Principal is not your custom User:

* It’s `UserDetails`
* Or JWT object

</details>

💡 Fix:

* Check actual type
* Use correct casting

---

## 🧩 Scenario 9: Expression works for some users, fails for others

👉 Same API, inconsistent behavior

<details>
<summary>Root Cause</summary>

Data inconsistency:

* Some users → correct authorities
* Some → missing roles

👉 Expression evaluates differently

</details>

---

## 🧩 Scenario 10: PostAuthorize causes performance issue

👉 API slow

<details>
<summary>Root Cause</summary>

Method executes first → then authorization

👉 Heavy DB call already done

</details>

💡 Fix:

* Prefer `@PreAuthorize` where possible

---

## 🧩 Scenario 11: Logical expression behaves unexpectedly

```java
@PreAuthorize("hasRole('ADMIN') or hasRole('USER') and #id == authentication.principal.id")
```

👉 Wrong access control

<details>
<summary>Root Cause</summary>

Operator precedence:

* `and` evaluated before `or`

👉 Logic misunderstood

</details>

💡 Fix:

```java
@PreAuthorize("(hasRole('ADMIN') or hasRole('USER')) and #id == authentication.principal.id")
```

---

## 🧩 Scenario 12: Always getting 403 even when expression looks correct

👉 No obvious issue

<details>
<summary>Root Cause</summary>

Expression evaluates to false silently:

* Wrong field name
* Wrong property path
* Typo

</details>

💡 Fix:

* Log values
* Debug authentication object

---

## 🧩 Scenario 13: Bean method returns wrong result

```java
@PreAuthorize("@authService.check(#id)")
```

👉 Access denied unexpectedly

<details>
<summary>Root Cause</summary>

Custom method logic incorrect:

* Returns false
* Wrong comparison

</details>

💡 Fix:

* Debug custom method

---

## 🧩 Scenario 14: Expression fails only for anonymous users

👉 Logged-in users fine

<details>
<summary>Root Cause</summary>

`authentication.principal` is:

```text
"anonymousUser"
```

👉 Not your User object

</details>

💡 Fix:

```java
@PreAuthorize("isAuthenticated() and #id == authentication.name")
```

---

## 🧩 Scenario 15: Expression throws exception randomly

👉 Hard to reproduce

<details>
<summary>Root Cause</summary>

Null values in data:

* Missing fields
* Partial objects

</details>

💡 Fix:

```java
@PreAuthorize("#obj != null and #obj.owner != null and ...")
```

---

# 🔥 Debugging Strategy (REAL GOLD)

---

## Step 1: Enable logs

```properties
logging.level.org.springframework.security=DEBUG
```

---

## Step 2: Inspect Authentication

* What is `principal`?
* What authorities exist?

---

## Step 3: Validate expression manually

👉 Break into smaller parts

---

## Step 4: Check nulls

👉 Most runtime failures = null access

---

## Step 5: Test with hardcoded values

👉 Isolate expression issue

---

# 🧠 Final Mental Model

* SpEL runs at runtime
* Depends on context (auth + params + return)
* Small mismatch → full failure
* No compile-time safety

---

# 🔥 Interview Killer Statement

👉 *“SpEL-based authorization failures are typically caused by runtime context issues like missing parameter names, incorrect principal types, null values, or expression evaluation order rather than syntax errors.”*

---