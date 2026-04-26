# 🔐 3.1 Authentication Flow (Deep + Practical)

## 🧠 Big Picture First

```text
Login Request
 → UsernamePasswordAuthenticationFilter
 → UsernamePasswordAuthenticationToken
 → AuthenticationManager
 → AuthenticationProvider
 → (UserDetailsService + Password check)
 → Authenticated Token
 → Stored in SecurityContext
```

👉 Everything revolves around **Authentication object flowing through components**

---

## 🔹 UsernamePasswordAuthenticationToken

### What it is ?

An implementation of `Authentication` used for **username/password login**


### Two states (very important)

1. **Unauthenticated (input token)**

```java
new UsernamePasswordAuthenticationToken(username, password);
```

2. **Authenticated (output token)**

```java
new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
```

### Internal structure

```text
Principal → username / UserDetails
Credentials → password
Authorities → roles
Authenticated → true/false
```

### Key Insight

👉 Same class used for:

* sending credentials
* storing authenticated user


---

## 🔹 AuthenticationManager

### What it is?

Entry point for authentication


### Main method

```java
Authentication authenticate(Authentication authentication);
```

### Responsibility

* Accepts authentication request
* Delegates to AuthenticationProvider

### Important

👉 It does NOT validate credentials itself

👉 It **delegates**


---

## 🔹 AuthenticationProvider

### What it is?

Actual component that **validates credentials**

### Two key methods

```java
Authentication authenticate(Authentication authentication);

boolean supports(Class<?> authentication);
```

### Responsibilities

* Validate username/password
* Load user via UserDetailsService
* Compare password
* Return authenticated token

### Key Insight

👉 This is where **real authentication logic happens**

---

## 🔹 ProviderManager

### What it is?

Default implementation of `AuthenticationManager`

### How it works

* Holds list of AuthenticationProviders
* Iterates through them
* Finds matching provider
* Delegates authentication

### Flow

```text
AuthenticationManager (ProviderManager)
   ↓
Provider1 (supports?)
   ↓
Provider2 (supports?)
   ↓
Authenticate
```

### Key Insight

👉 Enables **multiple authentication mechanisms**

Example:

* DB login
* LDAP
* JWT

---

# 🔥 End-to-End Authentication Flow (Step-by-Step)

```text
1. User sends login request
2. Filter creates UsernamePasswordAuthenticationToken (unauthenticated)
3. Token sent to AuthenticationManager
4. ProviderManager loops through providers
5. Matching AuthenticationProvider validates user
6. UserDetailsService loads user from DB
7. PasswordEncoder compares password
8. If valid → authenticated token created
9. Stored in SecurityContext
10. Request proceeds
```

---

---

# 💻 Coding Example (Modern Spring Boot)

## Step 1: UserDetailsService

```java
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) {
        // Normally fetch from DB
        if ("admin".equals(username)) {
            return new User(
                "admin",
                "$2a$10$Dow1...hashedPassword", // BCrypt
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
            );
        }
        throw new UsernameNotFoundException("User not found");
    }
}
```

---

## Step 2: PasswordEncoder

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

---

## Step 3: AuthenticationProvider

```java
@Bean
public AuthenticationProvider authenticationProvider(
        UserDetailsService userDetailsService,
        PasswordEncoder passwordEncoder) {

    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(userDetailsService);
    provider.setPasswordEncoder(passwordEncoder);

    return provider;
}
```

---

## Step 4: AuthenticationManager

```java
@Bean
public AuthenticationManager authenticationManager(
        AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
}
```

---

## Step 5: Security Config

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            .anyRequest().authenticated()
        )
        .formLogin();

    return http.build();
}
```

---

# 🔥 Internal Flow Mapping (Code ↔ Theory)

* Filter → creates `UsernamePasswordAuthenticationToken`
* AuthenticationManager → receives token
* ProviderManager → finds provider
* DaoAuthenticationProvider → validates
* UserDetailsService → loads user
* PasswordEncoder → checks password
* Returns authenticated token

---

# ⚠️ Common Mistakes (Interview + Real)

* Using raw password without encoder ❌
* Not registering AuthenticationProvider ❌
* Returning null instead of throwing exception ❌
* Confusing AuthenticationManager vs Provider ❌

---

# 🧠 Interview Gold Lines

* “AuthenticationManager delegates to AuthenticationProvider”
* “ProviderManager supports multiple providers”
* “UsernamePasswordAuthenticationToken represents both request and result”
* “Authentication becomes valid only after provider validation”

---

# ✅ What You Should Now Know

* Full authentication flow
* Role of each component
* How they interact
* How to implement in code

---

---

# 🔥 PART 1: Extreme Debugging Scenarios

---

## 🧩 Scenario 1: `AuthenticationManager.authenticate()` always fails

You call authenticate(), but always get exception.

👉 What could be wrong?

<details>
<summary>Show Answer</summary>

Common causes:

* No AuthenticationProvider registered
* `supports()` method returning false
* Wrong Authentication type passed

👉 If no provider supports the token → authentication fails

</details>

---

## 🧩 Scenario 2: `supports()` returns false → login fails

Your provider is configured, but never used.

👉 Why?

<details>
<summary>Show Answer</summary>

Because `supports()` method decides if provider handles the token.

Example mistake:

```java
return false;
```

or wrong class:

```java
return authentication.equals(SomeOtherToken.class);
```

👉 Provider skipped completely

</details>

---

## 🧩 Scenario 3: Password correct but login fails

Everything looks fine, still fails.

👉 Root cause?

<details>
<summary>Show Answer</summary>

PasswordEncoder mismatch.

* Stored password → BCrypt
* Incoming password → raw

👉 Must use same encoder:

```java
passwordEncoder.matches(raw, encoded)
```

</details>

---

## 🧩 Scenario 4: Authentication succeeds but user still anonymous

After login, `SecurityContext` shows anonymous.

👉 Why?

<details>
<summary>Show Answer</summary>

Authentication not stored in SecurityContext.

Missing:

```java
SecurityContextHolder.getContext().setAuthentication(auth);
```

Or filter didn’t complete properly

</details>

---

## 🧩 Scenario 5: Multiple providers but wrong one used

You have 2 providers, wrong one handles request.

👉 Why?

<details>
<summary>Show Answer</summary>

ProviderManager picks first matching provider.

If multiple `supports()` return true → order matters.

👉 First match wins

</details>

---

## 🧩 Scenario 6: `UserDetailsService` never called

Login endpoint hit, but DB not queried.

👉 Why?

<details>
<summary>Show Answer</summary>

* Wrong AuthenticationProvider used
* Custom provider bypassing UserDetailsService
* Incorrect filter triggering

</details>

---

## 🧩 Scenario 7: `BadCredentialsException` even with correct password

👉 What’s hidden issue?

<details>
<summary>Show Answer</summary>

* Password encoded twice
* Stored password incorrect format
* Encoder mismatch

👉 Classic trap in interviews

</details>

---

## 🧩 Scenario 8: Custom AuthenticationProvider returns null

👉 What happens?

<details>
<summary>Show Answer</summary>

ProviderManager tries next provider.

If no provider handles → exception thrown.

👉 Returning null ≠ success

</details>

---

## 🧩 Scenario 9: Authentication works for one user but not others

👉 Possible cause?

<details>
<summary>Show Answer</summary>

* Authorities missing
* UserDetails incomplete
* Account flags (locked/disabled)

Example:

```java
isAccountNonLocked = false
```

</details>

---

## 🧩 Scenario 10: Login API works, but protected APIs fail

👉 Why?

<details>
<summary>Show Answer</summary>

Authentication not persisted.

* Stateless system without JWT
* Session not maintained

👉 Login success ≠ future request authenticated

</details>

---

# 🔥 PART 2: EXTREME MCQs (Product Company Level)

---

## 1. What happens if no AuthenticationProvider supports the token?

1. Authentication succeeds
2. Next filter handles it
3. Exception is thrown
4. Anonymous user assigned

<details>
<summary>Show Answer</summary>

**3. Exception is thrown**

</details>

---

## 2. What is returned after successful authentication?

1. Same input token
2. New authenticated token
3. UserDetails
4. JWT token

<details>
<summary>Show Answer</summary>

**2. New authenticated token**

</details>

---

## 3. What is the purpose of `supports()` method?

1. Validate password
2. Check token type compatibility
3. Load user
4. Store authentication

<details>
<summary>Show Answer</summary>

**2. Check token type compatibility**

</details>

---

## 4. What happens if AuthenticationProvider returns null?

1. Authentication succeeds
2. Authentication fails immediately
3. Next provider is tried
4. System crashes

<details>
<summary>Show Answer</summary>

**3. Next provider is tried**

</details>

---

## 5. Which component actually validates password?

1. AuthenticationManager
2. ProviderManager
3. AuthenticationProvider
4. SecurityContext

<details>
<summary>Show Answer</summary>

**3. AuthenticationProvider**

</details>

---

## 6. What is stored in authenticated token credentials?

1. Password
2. null (usually cleared)
3. JWT
4. Username

<details>
<summary>Show Answer</summary>

**2. null**

👉 Password removed for security

</details>

---

## 7. Which class supports multiple providers?

1. AuthenticationProvider
2. ProviderManager
3. UserDetailsService
4. SecurityContextHolder

<details>
<summary>Show Answer</summary>

**2. ProviderManager**

</details>

---

## 8. What is the biggest mistake in custom AuthenticationProvider?

1. Wrong password
2. Returning null incorrectly
3. Using BCrypt
4. Throwing exception

<details>
<summary>Show Answer</summary>

**2. Returning null incorrectly**

</details>

---

## 9. What happens if passwordEncoder is not set?

1. Authentication succeeds
2. Plain text comparison
3. Exception or mismatch
4. Ignored

<details>
<summary>Show Answer</summary>

**3. Exception or mismatch**

</details>

---

## 10. What does AuthenticationManager primarily do?

1. Validate credentials
2. Delegate authentication
3. Store session
4. Handle HTTP

<details>
<summary>Show Answer</summary>

**2. Delegate authentication**

</details>

---

## 11. What is principal after authentication?

1. String username
2. UserDetails object
3. Password
4. JWT

<details>
<summary>Show Answer</summary>

**2. UserDetails object**

</details>

---

## 12. What happens if credentials are not cleared?

1. Performance issue
2. Security risk
3. Authentication fails
4. No impact

<details>
<summary>Show Answer</summary>

**2. Security risk**

</details>

---

## 13. Which exception is most common on login failure?

1. NullPointerException
2. BadCredentialsException
3. IOException
4. RuntimeException

<details>
<summary>Show Answer</summary>

**2. BadCredentialsException**

</details>

---

## 14. Which layer creates UsernamePasswordAuthenticationToken?

1. Controller
2. Filter
3. Service
4. DAO

<details>
<summary>Show Answer</summary>

**2. Filter**

</details>

---

## 15. What is the correct flow?

1. Provider → Manager → Token
2. Token → Provider → Manager
3. Token → Manager → Provider
4. Manager → Token → Provider

<details>
<summary>Show Answer</summary>

**3. Token → Manager → Provider**

</details>

---

# 🧠 What You Just Mastered

* Provider selection logic
* Token lifecycle
* Password pitfalls
* Multi-provider behavior
* Real debugging approach

---

# 🔥 Interview Reality

These exact questions appear in:

* Product companies
* Backend-heavy roles
* Spring Boot microservices interviews

👉 Most candidates fail in:

* `supports()` logic
* Provider chaining
* Password encoding

---

---

# 🔥 Ultra-Hard Debugging Scenarios (Multi-Layer Failures)

---

## 🧩 Scenario 1: Login succeeds, but next API call is always 401

* Login API returns success
* Credentials correct
* Next request → 401

👉 What’s broken?

<details>
<summary>Show Answer</summary>

Classic **stateless vs stateful mismatch**.

Possible causes:

* Session not maintained (STATELESS mode)
* No JWT issued after login
* SecurityContext not persisted

👉 Flow breaks between requests:
Authentication works once, but not reused

Fix:

* Use session (stateful) OR
* Issue JWT and validate in filter

</details>

---

## 🧩 Scenario 2: JWT filter works, but Authorization always fails (403)

* Token parsed correctly
* Authentication set in SecurityContext
* Still getting 403

👉 Why?

<details>
<summary>Show Answer</summary>

Authorities missing or incorrect.

Common issues:

* Authorities not set in Authentication
* Using `hasRole("ADMIN")` but token has `ADMIN` (missing ROLE_ prefix)
* Empty authorities list

👉 Authentication success ≠ Authorization success

</details>

---

## 🧩 Scenario 3: Custom filter executes, but authentication ignored

* Filter extracts token
* Creates Authentication
* Still user is anonymous

👉 What’s wrong?

<details>
<summary>Show Answer</summary>

Authentication not stored properly.

Missing:

```java
SecurityContextHolder.getContext().setAuthentication(auth);
```

OR

Filter runs AFTER AnonymousAuthenticationFilter → gets overridden

👉 Order issue + context issue

</details>

---

## 🧩 Scenario 4: One endpoint works, another fails with same token

* `/api/user` → works
* `/api/admin` → fails

👉 Why?

<details>
<summary>Show Answer</summary>

Authorization mismatch:

* Different role requirements
* Method-level security (`@PreAuthorize`)
* URL-based config differs

👉 Same authentication, different authorization rules

</details>

---

## 🧩 Scenario 5: AuthenticationProvider never triggered

* Login endpoint hit
* But provider not called

👉 Root cause?

<details>
<summary>Show Answer</summary>

Multi-layer issue:

* Filter not creating correct token
* `supports()` returning false
* Wrong AuthenticationManager wired

👉 Any of these breaks chain before provider

</details>

---

## 🧩 Scenario 6: Works locally, fails in production

* Local → success
* Prod → authentication fails

👉 Hidden issue?

<details>
<summary>Show Answer</summary>

Environment mismatch:

* Different PasswordEncoder
* DB stores different format
* Missing bean config

Example:
Local → NoOp
Prod → BCrypt

👉 Password mismatch at runtime

</details>

---

## 🧩 Scenario 7: Random users getting 403 intermittently

* Same API
* Same roles
* Sometimes works, sometimes fails

👉 What’s happening?

<details>
<summary>Show Answer</summary>

Thread-related issue:

* SecurityContext leakage or overwrite
* Async execution losing context
* Incorrect ThreadLocal handling

👉 Multi-thread bug

Fix:

* Use DelegatingSecurityContextExecutor
* Avoid manual context misuse

</details>

---

## 🧩 Scenario 8: Authentication works, but controller receives null user

* SecurityContext shows authenticated
* Controller sees null principal

👉 Why?

<details>
<summary>Show Answer</summary>

Context lost between layers:

* Async processing
* Wrong thread execution
* SecurityContextHolder not propagated

👉 ThreadLocal scope issue again

</details>

---

## 🧩 Scenario 9: Custom AuthenticationProvider works, but default login breaks

* Custom login works
* Form login fails

👉 Why?

<details>
<summary>Show Answer</summary>

Provider conflict:

* Custom provider overrides default
* supports() incorrectly matches all tokens

👉 Wrong provider handling wrong token type

Fix:

* Fix `supports()` method
* Or order providers correctly

</details>

---

## 🧩 Scenario 10: Token valid, but always treated as anonymous

* JWT parsed
* No exception
* Still anonymous user

👉 Root cause?

<details>
<summary>Show Answer</summary>

Multi-layer issue:

* Authentication not set in SecurityContext
* Filter placed after AnonymousAuthenticationFilter
* Filter not part of chain

👉 Order + registration issue

</details>

---

## 🧩 Scenario 11: Authentication succeeds but password still exposed

👉 What’s wrong?

<details>
<summary>Show Answer</summary>

Credentials not cleared.

Authenticated token should have:

```java
credentials = null
```

If not:
👉 Security vulnerability

</details>

---

## 🧩 Scenario 12: Multiple providers but wrong one always used

👉 Why?

<details>
<summary>Show Answer</summary>

ProviderManager logic:

* First provider with `supports()` = true is used
* Others ignored

👉 Ordering issue

</details>

---

# 🔥 Meta Debugging Strategy (Very Important)

When debugging Spring Security:

---

## Step 1: Check Filter Layer

* Is filter executing?
* Correct order?
* Authentication set?

---

## Step 2: Check Authentication Layer

* Token created?
* Provider called?
* Password valid?

---

## Step 3: Check SecurityContext

* Authentication stored?
* Thread preserved?

---

## Step 4: Check Authorization

* Roles present?
* Correct prefix?
* Config mismatch?

---

## Step 5: Check Flow Type

* Stateful vs Stateless?
* JWT vs Session?

---

# 🧠 Final Insight

Most real bugs are **not single-layer**:

👉 They are combinations of:

* Filter issue
* Provider issue
* Context issue
* Config issue

---

# 🔥 Interview Reality

If you explain like this:

👉 “I would debug from filter → authentication → context → authorization”

You immediately stand out.

---
