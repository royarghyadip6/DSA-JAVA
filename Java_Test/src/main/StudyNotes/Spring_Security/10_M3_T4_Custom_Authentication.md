# 🔐 3.4 Custom Authentication (Deep Dive)

## 🧠 Why “custom” at all?

Default (in-memory/JDBC) is limited. Real systems need:

* custom DB schema
* login by **email/phone** (not username)
* extra checks (status, OTP, tenant, etc.)
* external systems (LDAP, APIs)

👉 You customize at two layers:

* **UserDetailsService** → how user is loaded
* **AuthenticationProvider** → how credentials are validated

---

# 🔹 Custom UserDetailsService

## What it does

Loads user data from your source and converts it to **UserDetails**

---

## Core method

```java
UserDetails loadUserByUsername(String username)
```

👉 “username” can actually be email/phone/etc.

---

## Example (DB + email login)

```java id="uds1"
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository repo;

    public CustomUserDetailsService(UserRepository repo) {
        this.repo = repo;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        AppUser user = repo.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new org.springframework.security.core.userdetails.User(
            user.getEmail(),
            user.getPassword(), // already encoded
            user.isEnabled(),
            true, true, true,
            user.getRoles().stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                .toList()
        );
    }
}
```

---

## Key responsibilities

* Fetch user from DB
* Map to **UserDetails**
* Convert roles → authorities
* Throw exception if not found

---

## ⚠️ Common traps

* Returning null instead of exception ❌
* Not encoding password ❌
* Wrong authority mapping ❌

---

# 🔹 Custom AuthenticationProvider

## What it does

Performs **actual authentication logic**

👉 Use when default (password check) is not enough

---

## When you need it

* OTP / 2FA
* External API validation
* Custom password logic
* Multi-factor authentication
* Multi-tenant checks

---

## Core methods

```java
Authentication authenticate(Authentication authentication);

boolean supports(Class<?> authentication);
```

---

## Example (Email + Password + extra check)

```java id="ap1"
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public CustomAuthenticationProvider(CustomUserDetailsService uds,
                                        PasswordEncoder encoder) {
        this.userDetailsService = uds;
        this.passwordEncoder = encoder;
    }

    @Override
    public Authentication authenticate(Authentication auth) {

        String email = auth.getName();
        String rawPassword = auth.getCredentials().toString();

        UserDetails user = userDetailsService.loadUserByUsername(email);

        // password validation
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new BadCredentialsException("Invalid password");
        }

        // extra custom check (example)
        if (!user.isEnabled()) {
            throw new DisabledException("User disabled");
        }

        // return authenticated token
        return new UsernamePasswordAuthenticationToken(
            user,
            null,
            user.getAuthorities()
        );
    }

    @Override
    public boolean supports(Class<?> authType) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authType);
    }
}
```

---

## 🔥 Critical Insight

* Input token → unauthenticated
* Output token → authenticated
* Credentials usually cleared (set to null)

---

# 🔹 Wiring Everything Together

```java id="cfg1"
@Bean
public AuthenticationManager authenticationManager(
        AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
}
```

Spring will automatically pick your custom provider if it’s a bean.

---

# 🔥 Full Flow (Custom Auth)

```text id="flow1"
Request
 → Filter creates UsernamePasswordAuthenticationToken
 → AuthenticationManager
 → CustomAuthenticationProvider
 → CustomUserDetailsService (DB)
 → PasswordEncoder
 → Authenticated token
 → SecurityContext
```

---

# 🔴 Custom UserDetailsService vs AuthenticationProvider

| Aspect       | UserDetailsService | AuthenticationProvider      |
|--------------|--------------------|-----------------------------|
| Role         | Load user          | Validate credentials        |
| Logic        | Data fetch         | Auth logic                  |
| Replace when | Custom DB schema   | Custom authentication rules |

---

# ⚠️ Real-World Mistakes

* ❌ Putting validation logic inside UserDetailsService
* ❌ supports() returning true for wrong types
* ❌ Returning null incorrectly
* ❌ Not clearing credentials
* ❌ Not handling exceptions properly

---

# 🔥 Debugging Checklist

* Is UserDetailsService called?
* Is provider selected (`supports()`)?
* Is passwordEncoder correct?
* Is Authentication stored in context?
* Are authorities mapped correctly?

---

# 🧠 Interview Gold Lines

* “UserDetailsService only loads data, it does not authenticate”
* “AuthenticationProvider performs actual validation”
* “supports() decides which provider handles the token”
* “Authentication object flows from unauthenticated to authenticated state”

---

# 🧠 Final Mental Model

* UserDetailsService → who the user is
* AuthenticationProvider → whether user is valid
* AuthenticationManager → delegates
* SecurityContext → stores result

---

---

# 🔥 Extreme MCQs (Custom Authentication Traps)

---

## 1. What happens if `UserDetailsService` returns null?

1. Authentication succeeds
2. Authentication fails silently
3. Exception thrown
4. User becomes anonymous

<details>
<summary>Show Answer</summary>

**3. Exception thrown**

👉 Must throw `UsernameNotFoundException`, not return null

</details>

---

## 2. What is the responsibility of `UserDetailsService`?

1. Validate password
2. Load user data
3. Create token
4. Store session

<details>
<summary>Show Answer</summary>

**2. Load user data**

👉 No authentication logic here

</details>

---

## 3. What happens if `supports()` returns false?

1. Provider still used
2. Next provider is tried
3. Authentication succeeds
4. Exception thrown

<details>
<summary>Show Answer</summary>

**2. Next provider is tried**

</details>

---

## 4. What happens if all providers return null?

1. Authentication succeeds
2. Anonymous user assigned
3. Exception thrown
4. Ignored

<details>
<summary>Show Answer</summary>

**3. Exception thrown**

</details>

---

## 5. What is returned after successful authentication?

1. Same token
2. New authenticated token
3. UserDetails only
4. Boolean

<details>
<summary>Show Answer</summary>

**2. New authenticated token**

</details>

---

## 6. What happens if credentials are not cleared?

1. No issue
2. Performance issue
3. Security risk
4. Exception

<details>
<summary>Show Answer</summary>

**3. Security risk**

👉 Password remains in memory

</details>

---

## 7. Where should password validation happen?

1. Controller
2. UserDetailsService
3. AuthenticationProvider
4. Filter

<details>
<summary>Show Answer</summary>

**3. AuthenticationProvider**

</details>

---

## 8. What happens if `supports()` returns true for all types?

1. Works fine
2. Performance boost
3. Wrong provider may handle token
4. Ignored

<details>
<summary>Show Answer</summary>

**3. Wrong provider may handle token**

👉 Very dangerous bug

</details>

---

## 9. What happens if `AuthenticationProvider` returns null incorrectly?

1. Authentication succeeds
2. Next provider is tried
3. Exception thrown
4. System crashes

<details>
<summary>Show Answer</summary>

**2. Next provider is tried**

👉 Can lead to unexpected failures

</details>

---

## 10. What is principal in authenticated token?

1. Password
2. Username string
3. UserDetails object
4. JWT

<details>
<summary>Show Answer</summary>

**3. UserDetails object**

</details>

---

## 11. What happens if `UserDetailsService` is not wired?

1. Login succeeds
2. Login fails
3. Default used
4. Ignored

<details>
<summary>Show Answer</summary>

**2. Login fails**

</details>

---

## 12. Which exception is correct for wrong password?

1. RuntimeException
2. IOException
3. BadCredentialsException
4. NullPointerException

<details>
<summary>Show Answer</summary>

**3. BadCredentialsException**

</details>

---

## 13. What happens if authorities are empty?

1. Login fails
2. Login succeeds but access fails
3. Full access
4. Exception

<details>
<summary>Show Answer</summary>

**2. Login succeeds but access fails**

</details>

---

## 14. What happens if you validate password in UserDetailsService?

1. Correct design
2. No effect
3. Breaks separation of concerns
4. Faster

<details>
<summary>Show Answer</summary>

**3. Breaks separation of concerns**

</details>

---

## 15. What does AuthenticationManager do?

1. Validate password
2. Load user
3. Delegate authentication
4. Store session

<details>
<summary>Show Answer</summary>

**3. Delegate authentication**

</details>

---

# 🔥 Ultra Trap Interview Questions

---

## 🧩 Q1: Login fails but UserDetailsService works fine

<details>
<summary>Show Answer</summary>

Likely issue:

* PasswordEncoder mismatch
* AuthenticationProvider logic incorrect

👉 Loading user ≠ validating credentials

</details>

---

## 🧩 Q2: Custom provider never called

<details>
<summary>Show Answer</summary>

Possible reasons:

* supports() returns false
* Wrong Authentication type
* Provider not registered

</details>

---

## 🧩 Q3: Authentication succeeds but user has no access

<details>
<summary>Show Answer</summary>

Authorities missing or incorrect mapping.

👉 Authorization failure (403)

</details>

---

## 🧩 Q4: Multiple providers but wrong one used

<details>
<summary>Show Answer</summary>

Provider order issue.

👉 First matching provider wins

</details>

---

## 🧩 Q5: What is the biggest mistake in custom auth?

<details>
<summary>Show Answer</summary>

* Mixing responsibilities
* Incorrect supports()
* Returning null incorrectly

</details>

---

# 🧠 Final Mental Model

* UserDetailsService → fetch user
* AuthenticationProvider → validate
* supports() → routing logic
* AuthenticationManager → delegation

---

# 🔥 Interview Killer Statement

👉 *“In custom authentication, most bugs come from incorrect provider selection via supports() and improper separation between user loading and credential validation.”*

---

---

# 🔥 Custom Authentication – Interview Level Q&A (Hidden Answers)

---

## 1. Explain the difference between UserDetailsService and AuthenticationProvider

<details>
<summary>Show Answer</summary>

* **UserDetailsService**

    * Loads user data from source (DB/API)
    * Returns UserDetails
    * Does NOT validate password

* **AuthenticationProvider**

    * Validates credentials (password, OTP, etc.)
    * Uses UserDetailsService internally
    * Returns authenticated token

👉 Separation:

* Who the user is vs whether the user is valid

</details>

---

## 2. Explain full flow of custom authentication

<details>
<summary>Show Answer</summary>

1. Filter creates UsernamePasswordAuthenticationToken
2. Sent to AuthenticationManager
3. ProviderManager selects AuthenticationProvider
4. CustomAuthenticationProvider runs
5. Calls UserDetailsService → fetch user
6. PasswordEncoder validates password
7. Returns authenticated token
8. Stored in SecurityContext

</details>

---

## 3. Why is supports() method critical?

<details>
<summary>Show Answer</summary>

* Determines which provider handles request
* If returns false → provider skipped
* If incorrectly returns true → wrong provider handles request

👉 It’s routing logic for authentication

</details>

---

## 4. What happens if AuthenticationProvider returns null?

<details>
<summary>Show Answer</summary>

* ProviderManager tries next provider
* If none handle → exception thrown

👉 Returning null ≠ success

</details>

---

## 5. Why should credentials be cleared after authentication?

<details>
<summary>Show Answer</summary>

* Prevent password leakage in memory
* Reduce security risk

👉 Authenticated token should not hold raw password

</details>

---

## 6. Where should password validation logic reside?

<details>
<summary>Show Answer</summary>

Inside AuthenticationProvider.

👉 Not in:

* Controller ❌
* UserDetailsService ❌

</details>

---

## 7. What is the biggest mistake developers make in custom authentication?

<details>
<summary>Show Answer</summary>

Mixing responsibilities:

* Doing validation in UserDetailsService
* Incorrect supports() implementation
* Returning null incorrectly

</details>

---

## 8. How would you implement login using email instead of username?

<details>
<summary>Show Answer</summary>

* Override UserDetailsService:

```java
loadUserByUsername(String email)
```

* Query DB using email
* Return UserDetails

👉 No need to change AuthenticationProvider

</details>

---

## 9. How does Spring Security know which AuthenticationProvider to use?

<details>
<summary>Show Answer</summary>

Using:

```java
supports(Class<?> authentication)
```

👉 ProviderManager iterates providers → picks first matching

</details>

---

## 10. Why might UserDetailsService not be called?

<details>
<summary>Show Answer</summary>

* Wrong AuthenticationProvider selected
* supports() returning false
* Filter not creating correct token

</details>

---

## 11. What happens if user exists but password is wrong?

<details>
<summary>Show Answer</summary>

AuthenticationProvider throws:

```text
BadCredentialsException
```

</details>

---

## 12. How would you implement OTP-based authentication?

<details>
<summary>Show Answer</summary>

* Create custom Authentication token (OtpAuthenticationToken)
* Create custom AuthenticationProvider
* Validate OTP instead of password
* Return authenticated token

</details>

---

## 13. How do you support multiple authentication mechanisms?

<details>
<summary>Show Answer</summary>

* Define multiple AuthenticationProviders
* Each handles different token types
* ProviderManager delegates based on supports()

</details>

---

## 14. What is the role of AuthenticationManager?

<details>
<summary>Show Answer</summary>

* Delegates authentication to providers
* Does NOT validate credentials itself

</details>

---

## 15. What is inside authenticated Authentication object?

<details>
<summary>Show Answer</summary>

* principal → UserDetails
* credentials → null
* authorities → roles/permissions
* authenticated = true

</details>

---

# 🔥 Ultra Hard Scenario Questions

---

## 🧩 Q1: Login fails but UserDetailsService works perfectly

<details>
<summary>Show Answer</summary>

Likely issue:

* PasswordEncoder mismatch
* AuthenticationProvider logic incorrect

👉 Loading user ≠ authentication

</details>

---

## 🧩 Q2: Custom provider never triggered

<details>
<summary>Show Answer</summary>

* supports() returns false
* Wrong token type
* Provider not registered

</details>

---

## 🧩 Q3: Authentication succeeds but access denied

<details>
<summary>Show Answer</summary>

* Authorities missing
* Role mismatch (ROLE_ prefix)

👉 Authorization failure

</details>

---

## 🧩 Q4: Multiple providers but wrong one used

<details>
<summary>Show Answer</summary>

* supports() too generic
* Provider order issue

👉 First match wins

</details>

---

## 🧩 Q5: Authentication works in one API but fails in another

<details>
<summary>Show Answer</summary>

* Different filters
* Different security config
* Context not propagated

</details>

---

# 🧠 Final Mental Model

* Token created → AuthenticationManager
* Provider selected → AuthenticationProvider
* User loaded → UserDetailsService
* Password validated → PasswordEncoder
* Result stored → SecurityContext

---

# 🔥 Interview Killer Statement

👉 *“Custom authentication in Spring Security revolves around correctly separating user loading and credential validation, and ensuring proper provider selection via supports().”*

---

