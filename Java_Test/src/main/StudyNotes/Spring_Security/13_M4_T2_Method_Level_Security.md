# 🔐 4.2 Method-Level Security (Deep Dive)

## 🧠 Core Idea

👉 Instead of securing URLs, you secure **methods**:

* Service layer
* Controller layer
* Even repository layer

👉 More precise than URL-based security

---

# 🔹 @EnableMethodSecurity

## What it does

👉 Enables method-level annotations

---

### Example

```java
@EnableMethodSecurity
@Configuration
public class SecurityConfig {
}
```

---

## What it activates

* `@PreAuthorize`
* `@PostAuthorize`
* `@Secured`
* `@RolesAllowed` (optional)

---

## ⚠️ Important

👉 Without this, annotations won’t work at all

---

# 🔹 @PreAuthorize (MOST IMPORTANT)

## What it does

👉 Checks **before method execution**

---

### Example

```java
@PreAuthorize("hasRole('ADMIN')")
public void deleteUser() {
    // only ADMIN can call
}
```

---

## Expression-based security (VERY IMPORTANT)

You can write powerful conditions:

```java
@PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
```

👉 Meaning:

* Admin OR owner can access

---

## Common expressions

* `hasRole('ADMIN')`
* `hasAuthority('READ')`
* `isAuthenticated()`
* `isAnonymous()`

---

## 🔥 Real Example

```java
@PreAuthorize("#username == authentication.name")
public User getProfile(String username) {
    return service.getUser(username);
}
```

👉 User can access only their own data

---

# 🔹 @PostAuthorize

## What it does

👉 Checks **after method execution**

---

### Example

```java
@PostAuthorize("returnObject.owner == authentication.name")
public Document getDocument(Long id) {
    return repo.findById(id);
}
```

---

## 🔥 Key Use Case

👉 When decision depends on returned data

---

## ⚠️ Important

* Method executes first
* Then authorization happens

👉 Can be expensive

---

# 🔹 @Secured

## What it does

👉 Simple role-based check

---

### Example

```java
@Secured("ROLE_ADMIN")
public void deleteUser() {
}
```

---

## Limitations

* No expressions
* Only role-based
* Less flexible

---

# 🔥 @PreAuthorize vs @Secured

| Feature     | @PreAuthorize | @Secured          |
|-------------|---------------|-------------------|
| Expressions | ✅ Yes         | ❌ No              |
| Flexibility | High          | Low               |
| Use case    | Complex logic | Simple role check |

---

# 🔥 Execution Flow

```text
Request
 ↓
Filter Chain (Authentication + URL Authorization)
 ↓
DispatcherServlet
 ↓
Method Security Interceptor
 ↓
@PreAuthorize / @PostAuthorize
 ↓
Method Execution
```

---

# 🔴 Critical Insight

👉 Method security runs **AFTER URL security**

👉 If URL blocks → method never runs

---

# ⚠️ Common Real-World Mistakes

---

## ❌ Forgot @EnableMethodSecurity

👉 Annotations silently ignored

---

## ❌ Wrong role format

```java
@Secured("ADMIN") ❌
```

👉 Should be:

```java
@Secured("ROLE_ADMIN")
```

---

## ❌ Using @PostAuthorize unnecessarily

👉 Slower (method already executed)

---

## ❌ Not understanding evaluation context

* `authentication` object
* method parameters (`#id`)

---

## ❌ Mixing URL and method rules incorrectly

👉 Leads to confusion/debugging nightmare

---

# 🔥 Advanced Concepts

---

## Access method parameters

```java
@PreAuthorize("#id == authentication.principal.id")
```

---

## Access return object

```java
@PostAuthorize("returnObject.owner == authentication.name")
```

---

## Custom expressions (advanced)

👉 You can define your own security logic

---

# 🧠 Debugging Perspective

---

## If annotation not working:

* Missing @EnableMethodSecurity
* Proxy not created
* Method not public

---

## If always 403:

* Role mismatch
* Expression incorrect
* Principal not mapped correctly

---

## If method not even called:

👉 Blocked at URL level

---

# 🧠 Final Mental Model

* URL security → coarse filtering
* Method security → fine-grained control
* @PreAuthorize → before execution
* @PostAuthorize → after execution

---

# 🔥 Interview Killer Statement

👉 *“Method-level security in Spring Security provides fine-grained authorization using expression-based annotations like @PreAuthorize, which are enforced after URL-level security via AOP proxies.”*

---

# ✅ What You Should Now Know

* How method security works internally
* Difference between annotations
* Expression usage
* Execution order

---

---

# 🔥 Extreme MCQs (Method Security Traps)

---

## 1. What happens if `@EnableMethodSecurity` is missing?

1. Methods secured by default
2. Annotations ignored
3. Compile error
4. Runtime exception

<details>
<summary>Show Answer</summary>

**2. Annotations ignored**

👉 No activation → no method security

</details>

---

## 2. What happens if method is `private` with `@PreAuthorize`?

1. Works normally
2. Ignored
3. Exception
4. Secured partially

<details>
<summary>Show Answer</summary>

**2. Ignored**

👉 Proxies work only on public methods

</details>

---

## 3. What happens if a method calls another secured method inside same class?

1. Security applied
2. Ignored
3. Exception
4. Random

<details>
<summary>Show Answer</summary>

**2. Ignored**

👉 Self-invocation bypasses proxy

</details>

---

## 4. What happens if `@PreAuthorize` fails?

1. Method executes
2. 401
3. 403
4. 404

<details>
<summary>Show Answer</summary>

**3. 403**

👉 Authorization failure

</details>

---

## 5. What happens if `@PostAuthorize` fails?

1. Method not executed
2. Method executes, then 403
3. 401
4. Ignored

<details>
<summary>Show Answer</summary>

**2. Method executes, then 403**

👉 Check happens after execution

</details>

---

## 6. Which is more powerful?

1. @Secured
2. @PreAuthorize
3. @RolesAllowed
4. All same

<details>
<summary>Show Answer</summary>

**2. @PreAuthorize**

👉 Supports expressions

</details>

---

## 7. What does `hasRole("ADMIN")` check?

1. ADMIN
2. ROLE_ADMIN
3. admin
4. Any role

<details>
<summary>Show Answer</summary>

**2. ROLE_ADMIN**

</details>

---

## 8. What happens if role prefix mismatch?

1. Login fails
2. Method executes
3. 403
4. Ignored

<details>
<summary>Show Answer</summary>

**3. 403**

</details>

---

## 9. What happens if URL security blocks request?

1. Method security still runs
2. Method never reached
3. Both run
4. Exception

<details>
<summary>Show Answer</summary>

**2. Method never reached**

👉 URL security runs first

</details>

---

## 10. What happens if SpEL expression is incorrect?

1. Ignored
2. Compile error
3. Runtime exception
4. Always true

<details>
<summary>Show Answer</summary>

**3. Runtime exception**

</details>

---

## 11. What is `authentication` in SpEL?

1. String
2. UserDetails
3. Authentication object
4. JWT

<details>
<summary>Show Answer</summary>

**3. Authentication object**

</details>

---

## 12. What happens if `returnObject` is null in @PostAuthorize?

1. Works fine
2. Exception
3. Always false
4. Ignored

<details>
<summary>Show Answer</summary>

**2. Exception**

👉 Null pointer risk

</details>

---

## 13. Which annotation checks before execution?

1. @PostAuthorize
2. @PreAuthorize
3. @Secured
4. @RolesAllowed

<details>
<summary>Show Answer</summary>

**2. @PreAuthorize**

</details>

---

## 14. What happens if principal mapping is wrong?

1. No effect
2. Wrong authorization
3. Exception
4. Compile error

<details>
<summary>Show Answer</summary>

**2. Wrong authorization**

👉 Expression evaluates incorrectly

</details>

---

## 15. What happens if method is not public in proxy-based security?

1. Works fine
2. Ignored
3. Exception
4. Partial execution

<details>
<summary>Show Answer</summary>

**2. Ignored**

</details>

---

# 🔥 Ultra Trap Questions

---

## 🧩 Q1: @PreAuthorize not working at all

<details>
<summary>Show Answer</summary>

* Missing @EnableMethodSecurity
* Method not public
* Proxy not created

</details>

---

## 🧩 Q2: Method security works for external calls but not internal

<details>
<summary>Show Answer</summary>

Self-invocation bypasses proxy.

👉 Internal calls skip security

</details>

---

## 🧩 Q3: Getting 403 even with correct role

<details>
<summary>Show Answer</summary>

* ROLE_ prefix mismatch
* Expression error
* Wrong authority mapping

</details>

---

## 🧩 Q4: Method executed but still 403 returned

<details>
<summary>Show Answer</summary>

@PostAuthorize failed.

👉 Post-execution check

</details>

---

## 🧩 Q5: Expression using parameter not working

<details>
<summary>Show Answer</summary>

* Parameter name mismatch
* Missing `#`
* Debug info not available

</details>

---

# 🧠 Final Mental Model

* @PreAuthorize → before execution
* @PostAuthorize → after execution
* Proxy → required
* Self-invocation → bypasses security
* ROLE_ prefix → critical

---

# 🔥 Interview Killer Statement

👉 *“Method-level security in Spring relies on proxy-based AOP, which means self-invocation and non-public methods can silently bypass security checks.”*

---

---

# 🔥 Real Debugging Scenarios (Annotation NOT Working + Proxy Issues)

---

## 🧩 Scenario 1: `@PreAuthorize` not working at all

👉 No error, method executes freely

<details>
<summary>Root Cause</summary>

Missing:

```java
@EnableMethodSecurity
```

👉 Without it:

* No proxy created
* Annotations ignored silently

</details>

---

## 🧩 Scenario 2: Annotation works in some classes, not others

👉 Very confusing behavior

<details>
<summary>Root Cause</summary>

Bean not managed by Spring:

* Created using `new`
* Not annotated with `@Service`, `@Component`

👉 No proxy → no security

</details>

---

## 🧩 Scenario 3: Works from controller, fails inside same class

```java
public void methodA() {
    methodB(); // methodB has @PreAuthorize
}
```

👉 methodB security not applied

<details>
<summary>Root Cause</summary>

**Self-invocation problem**

* Proxy is bypassed
* Direct method call inside same class

👉 Spring AOP works only via proxy

</details>

---

## 🧩 Scenario 4: Method security not applied on private method

```java
@PreAuthorize("hasRole('ADMIN')")
private void deleteUser() {}
```

👉 No security applied

<details>
<summary>Root Cause</summary>

Spring AOP proxies only intercept:

* public methods (default)

👉 Private methods bypass proxy

</details>

---

## 🧩 Scenario 5: @PostAuthorize throws exception after method execution

👉 Method executes, then fails

<details>
<summary>Root Cause</summary>

* returnObject is null
* SpEL expression fails

👉 Happens AFTER execution

</details>

---

## 🧩 Scenario 6: Expression using parameter not working

```java
@PreAuthorize("#id == authentication.principal.id")
```

👉 Always false

<details>
<summary>Root Cause</summary>

* Parameter name mismatch
* Missing debug symbols
* Method param not visible

👉 Use:

```java
@PreAuthorize("#p0 == authentication.principal.id")
```

</details>

---

## 🧩 Scenario 7: Correct role, still getting 403

👉 Everything looks correct

<details>
<summary>Root Cause</summary>

* ROLE_ prefix mismatch
* Using hasAuthority instead of hasRole

👉 Example:

```java
hasRole("ADMIN") → ROLE_ADMIN
```

</details>

---

## 🧩 Scenario 8: Method security never triggered

👉 Logs show nothing

<details>
<summary>Root Cause</summary>

URL-level security blocked request earlier.

👉 Flow:

* Filter blocks → method never reached

</details>

---

## 🧩 Scenario 9: Works in dev, fails in production

<details>
<summary>Root Cause</summary>

* Different proxy mode (JDK vs CGLIB)
* Missing interface-based proxy
* Different bean configuration

👉 Proxy behavior differs

</details>

---

## 🧩 Scenario 10: Annotation works only on interface, not implementation

<details>
<summary>Root Cause</summary>

Using JDK dynamic proxies:

* Proxy created for interface
* Implementation annotations ignored

👉 Fix:

* Move annotation to interface
  OR
* Use CGLIB proxy

</details>

---

## 🧩 Scenario 11: Multiple annotations conflict

```java
@PreAuthorize("hasRole('ADMIN')")
@Secured("ROLE_USER")
```

👉 Unexpected behavior

<details>
<summary>Root Cause</summary>

* Multiple interceptors
* Conflicting rules

👉 Avoid mixing

</details>

---

## 🧩 Scenario 12: Bean method works but injected method fails

👉 Same method behaves differently

<details>
<summary>Root Cause</summary>

* One call via proxy
* One call via direct instance

👉 Only proxy calls apply security

</details>

---

# 🔥 Deep Internal Reason (Must Understand)

---

## Proxy-Based Execution

```text
Client
 ↓
Proxy Object
 ↓
Security Check (@PreAuthorize)
 ↓
Actual Method
```

---

## Problem Case (Self-invocation)

```text
Method A
 ↓ (direct call)
Method B (NO proxy)
```

👉 Security bypassed

---

# 🔥 How to Fix Proxy Issues

---

## Fix 1: Move method to another bean

```java
@Service
class ServiceA {
    private final ServiceB serviceB;

    serviceB.methodB(); // goes through proxy
}
```

---

## Fix 2: Use ApplicationContext (advanced)

```java
applicationContext.getBean(Service.class).methodB();
```

---

## Fix 3: Force CGLIB proxy

```java
@EnableMethodSecurity(proxyTargetClass = true)
```

---

# 🔥 Debugging Strategy

---

## Step 1: Check if method is called

👉 If not:

* Blocked at URL level

---

## Step 2: Check logs

```properties
logging.level.org.springframework.security=DEBUG
```

---

## Step 3: Verify proxy

* Is bean managed by Spring?
* Is method public?

---

## Step 4: Check invocation path

* External call → works
* Internal call → fails

👉 Proxy issue

---

# 🧠 Final Mental Model

* Method security = proxy-based
* Proxy required for interception
* Self-invocation bypasses security
* URL security runs before method security

---

# 🔥 Interview Killer Statement

👉 *“Method-level security in Spring is implemented using proxy-based AOP, so any direct method invocation (self-invocation) bypasses security checks, which is a common cause of silent failures.”*

---
