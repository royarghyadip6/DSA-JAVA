# 🔐 3.3 JDBC Authentication (Deep + Practical)

## 🧠 Core idea

```text
Login → UsernamePasswordAuthenticationToken
 → AuthenticationManager
 → DaoAuthenticationProvider
 → JdbcUserDetailsManager (DB)
 → PasswordEncoder
 → Authenticated user
```

👉 Only difference from in-memory: **user comes from DB via JDBC**

---

## 🔹 Database Schema (Default expected by Spring)

Spring Security expects **2 tables** by default.

### users table

```sql
CREATE TABLE users (
    username VARCHAR(50) PRIMARY KEY,
    password VARCHAR(100) NOT NULL,
    enabled BOOLEAN NOT NULL
);
```

---

### authorities table

```sql
CREATE TABLE authorities (
    username VARCHAR(50),
    authority VARCHAR(50),
    CONSTRAINT fk_authorities_users 
        FOREIGN KEY(username) REFERENCES users(username)
);
```

---

### Example data

```sql
INSERT INTO users VALUES ('admin', '$2a$10$hash...', true);

INSERT INTO authorities VALUES ('admin', 'ROLE_ADMIN');
```

---

## 🔥 Key Insight

* `username` must match in both tables
* `authority` = role/permission
* Password must be **encoded (BCrypt recommended)**

---

## 🔹 DataSource Configuration

Spring needs DB connection.

### Example (application.properties)

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/security_db
spring.datasource.username=root
spring.datasource.password=pass
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

---

### Bean (optional if auto-config not used)

```java
@Bean
public DataSource dataSource() {
    return new DriverManagerDataSource(
        "jdbc:mysql://localhost:3306/security_db",
        "root",
        "pass"
    );
}
```

---

## 🔹 JdbcUserDetailsManager (Core Component)

This is the JDBC version of UserDetailsService.

---

### Basic configuration

```java
@Bean
public UserDetailsService userDetailsService(DataSource dataSource) {
    return new JdbcUserDetailsManager(dataSource);
}
```

---

👉 That’s it—Spring will:

* Query users table
* Query authorities table
* Build UserDetails

---

## 🔹 Queries Customization (VERY IMPORTANT)

If your DB schema is different, you MUST override queries.

---

### Default queries (important to know)

```sql
SELECT username, password, enabled 
FROM users 
WHERE username = ?
```

```sql
SELECT username, authority 
FROM authorities 
WHERE username = ?
```

---

### Custom queries example

```java
@Bean
public UserDetailsService userDetailsService(DataSource dataSource) {
    JdbcUserDetailsManager manager = new JdbcUserDetailsManager(dataSource);

    manager.setUsersByUsernameQuery(
        "SELECT email, pwd, active FROM app_users WHERE email = ?"
    );

    manager.setAuthoritiesByUsernameQuery(
        "SELECT email, role FROM user_roles WHERE email = ?"
    );

    return manager;
}
```

---

## ⚠️ Critical Rules for Custom Queries

* Must return:

    * username
    * password
    * enabled

* Authorities query must return:

    * username
    * authority

👉 Column names can differ, but **order matters**

---

## 🔹 Full Security Configuration

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/admin/**").hasRole("ADMIN")
            .anyRequest().authenticated()
        )
        .formLogin();

    return http.build();
}
```

---

## 🔥 Internal Flow (DB Version)

```text
Login Request
 → Filter creates token
 → AuthenticationManager
 → DaoAuthenticationProvider
 → JdbcUserDetailsManager queries DB
 → PasswordEncoder validates
 → Authentication success
```

---

## ⚠️ Common Real-World Mistakes

### 1. ❌ Password not encoded

* DB stores plain text
* BCrypt used in app → mismatch

---

### 2. ❌ Missing ROLE_ prefix

* Stored: ADMIN
* Expected: ROLE_ADMIN

---

### 3. ❌ Wrong query structure

* Missing columns
* Wrong order

---

### 4. ❌ User not enabled

```sql
enabled = false
```

👉 Login fails silently

---

### 5. ❌ Authorities table empty

👉 Authentication succeeds, authorization fails (403)

---

## 🔥 Interview Traps

### 🧩 Why does login succeed but access fails?

<details>
<summary>Show Answer</summary>

Authorities missing or incorrect.

👉 Authentication ≠ Authorization

</details>

---

### 🧩 Why is UserDetailsService not called?

<details>
<summary>Show Answer</summary>

Wrong AuthenticationProvider or config.

</details>

---

### 🧩 What happens if user not found?

<details>
<summary>Show Answer</summary>

Throws:
`UsernameNotFoundException`

</details>

---

## 🧠 Interview Gold Lines

* “JdbcUserDetailsManager implements UserDetailsService using DB”
* “Spring expects specific schema unless queries are customized”
* “Authorities drive authorization decisions”
* “Password must match encoder format”

---

## 🧠 Final Mental Model

* DB replaces in-memory store
* Queries fetch user + roles
* DaoAuthenticationProvider handles validation
* Everything else remains same

---

## ✅ What You Should Now Know

* Default schema
* How queries work
* How to customize
* How DB integrates into auth flow

---

---

# 🔥 Extreme MCQs (JDBC Authentication Traps)

---

## 1. What happens if password in DB is plain text but app uses BCrypt?

1. Login succeeds
2. Login fails
3. Password auto-encoded
4. Ignored

<details>
<summary>Show Answer</summary>

**2. Login fails**

👉 Encoder mismatch → comparison fails

</details>

---

## 2. What happens if `enabled = false` in users table?

1. Login succeeds
2. Login fails
3. User becomes anonymous
4. Ignored

<details>
<summary>Show Answer</summary>

**2. Login fails**

👉 Account is disabled

</details>

---

## 3. If authorities table has no entries for a user?

1. Login fails
2. Login succeeds but access fails
3. Full access
4. Exception

<details>
<summary>Show Answer</summary>

**2. Login succeeds but access fails**

👉 No authorities → 403

</details>

---

## 4. What is the correct column order in default user query?

1. password, username, enabled
2. username, password, enabled
3. username, enabled, password
4. enabled, username, password

<details>
<summary>Show Answer</summary>

**2. username, password, enabled**

👉 Order matters, not names

</details>

---

## 5. What happens if custom query returns wrong column order?

1. Compile error
2. Runtime crash
3. Wrong data mapping
4. Ignored

<details>
<summary>Show Answer</summary>

**3. Wrong data mapping**

👉 Leads to authentication failure

</details>

---

## 6. What happens if username column mismatches between tables?

1. Login fails
2. Authorities not fetched
3. System crashes
4. No effect

<details>
<summary>Show Answer</summary>

**2. Authorities not fetched**

👉 Results in 403 later

</details>

---

## 7. What happens if authorities are stored as `ADMIN` instead of `ROLE_ADMIN`?

1. Works with hasRole
2. Works with hasAuthority
3. Always fails
4. Ignored

<details>
<summary>Show Answer</summary>

**2. Works with hasAuthority**

👉 But fails with hasRole

</details>

---

## 8. What happens if DataSource is not configured?

1. Login fails silently
2. App won’t start
3. DB ignored
4. Defaults used

<details>
<summary>Show Answer</summary>

**2. App won’t start**

👉 DataSource is mandatory

</details>

---

## 9. Which component executes DB queries?

1. AuthenticationManager
2. DaoAuthenticationProvider
3. JdbcUserDetailsManager
4. FilterChainProxy

<details>
<summary>Show Answer</summary>

**3. JdbcUserDetailsManager**

</details>

---

## 10. What happens if custom query returns null?

1. Login succeeds
2. Exception thrown
3. User ignored
4. Anonymous user

<details>
<summary>Show Answer</summary>

**2. Exception thrown**

</details>

---

## 11. What if multiple rows returned for same username?

1. First used
2. Exception
3. All merged
4. Ignored

<details>
<summary>Show Answer</summary>

**2. Exception**

👉 Username must be unique

</details>

---

## 12. What happens if password column is missing in query?

1. Login succeeds
2. Login fails
3. Ignored
4. Exception

<details>
<summary>Show Answer</summary>

**4. Exception**

</details>

---

## 13. Which encoding must match?

1. DB encoding
2. App PasswordEncoder
3. Both
4. None

<details>
<summary>Show Answer</summary>

**3. Both**

</details>

---

## 14. What happens if authorities query returns empty result?

1. Full access
2. Authentication fails
3. Authorization fails
4. System crashes

<details>
<summary>Show Answer</summary>

**3. Authorization fails**

</details>

---

## 15. Which is TRUE about JDBC authentication?

1. It bypasses AuthenticationProvider
2. It uses DaoAuthenticationProvider
3. It skips password validation
4. It doesn’t use UserDetails

<details>
<summary>Show Answer</summary>

**2. It uses DaoAuthenticationProvider**

</details>

---

# 🔥 Ultra Trap Questions (Interview Killers)

---

## 🧩 Q1: Login works but every API returns 403

<details>
<summary>Show Answer</summary>

Authorities missing or incorrect.

👉 Most common JDBC bug

</details>

---

## 🧩 Q2: Same query works in DB but fails in app

<details>
<summary>Show Answer</summary>

Column order mismatch.

👉 Spring maps by position, not name

</details>

---

## 🧩 Q3: User exists but login fails

<details>
<summary>Show Answer</summary>

Possible reasons:

* Password encoding mismatch
* enabled = false
* wrong query

</details>

---

## 🧩 Q4: Why does hasRole fail but hasAuthority works?

<details>
<summary>Show Answer</summary>

Missing `ROLE_` prefix in DB.

</details>

---

## 🧩 Q5: Biggest hidden JDBC security bug?

<details>
<summary>Show Answer</summary>

Storing plain passwords.

👉 Security risk + login failure with encoder

</details>

---

# 🧠 Final Mental Model

* DB → user + authorities
* Queries → must match structure
* Password → must match encoder
* Authorities → drive authorization

---

# 🔥 Interview Killer Line

👉 *“In JDBC authentication, most failures are due to mismatched schema, incorrect query mapping, or password encoding issues—not the authentication flow itself.”*

---

---

# 🔥 JDBC Authentication – Hard Interview Q&A (Hidden Answers)

---

## 1. Explain end-to-end flow of JDBC authentication in Spring Security

<details>
<summary>Show Answer</summary>

Flow:

1. Login request hits filter
2. UsernamePasswordAuthenticationToken created (unauthenticated)
3. Passed to AuthenticationManager
4. ProviderManager delegates to DaoAuthenticationProvider
5. DaoAuthenticationProvider calls JdbcUserDetailsManager
6. DB queried for user + authorities
7. PasswordEncoder validates password
8. Authenticated token created
9. Stored in SecurityContext
10. Authorization uses authorities

👉 DB is only used at UserDetailsService layer

</details>

---

## 2. Why does login succeed but authorization fails in JDBC auth?

<details>
<summary>Show Answer</summary>

Because authentication and authorization are separate.

Possible causes:

* Authorities table empty
* Wrong role format (missing ROLE_)
* Incorrect mapping in query

👉 Authentication uses users table, authorization uses authorities

</details>

---

## 3. What happens if custom query returns columns in wrong order?

<details>
<summary>Show Answer</summary>

Spring maps by **position, not column name**.

So:

* username might map to password
* password might map to enabled

👉 Result: authentication failure or unexpected behavior

</details>

---

## 4. Why is PasswordEncoder mandatory in JDBC authentication?

<details>
<summary>Show Answer</summary>

Because:

* Passwords must be stored securely (hashed)
* Spring compares raw input with encoded DB value

Without encoder:

* Either mismatch → login fails
* Or insecure plain text → vulnerability

</details>

---

## 5. Difference between JdbcUserDetailsManager and DaoAuthenticationProvider?

<details>
<summary>Show Answer</summary>

* JdbcUserDetailsManager → fetches user from DB
* DaoAuthenticationProvider → validates credentials

👉 Separation of concerns:
Data access vs authentication logic

</details>

---

## 6. What happens if multiple authorities exist for a user?

<details>
<summary>Show Answer</summary>

All are loaded into:

```text
List<GrantedAuthority>
```

👉 User can have multiple roles/permissions
👉 Authorization checks against all

</details>

---

## 7. Why is username required to be unique?

<details>
<summary>Show Answer</summary>

Because:

* Authentication expects single user
* Multiple rows → ambiguity
* Spring throws exception

👉 Primary key or unique constraint required

</details>

---

## 8. What is the biggest risk in custom query configuration?

<details>
<summary>Show Answer</summary>

Incorrect mapping:

* Missing columns
* Wrong order
* Wrong joins

👉 Leads to silent failures or security bypass

</details>

---

## 9. How does Spring Security map DB data to UserDetails?

<details>
<summary>Show Answer</summary>

JdbcUserDetailsManager:

* Executes queries
* Maps result set → UserDetails object
* Converts authorities → GrantedAuthority

</details>

---

## 10. What happens if authorities query is slow?

<details>
<summary>Show Answer</summary>

Performance impact:

* Every login triggers DB query
* Slower authentication

👉 Solution:

* Index DB
* Cache users
* Use custom UserDetailsService

</details>

---

## 11. Can JDBC authentication work without authorities table?

<details>
<summary>Show Answer</summary>

Technically yes, but:

* User will have no roles
* Authorization will fail

👉 Practically useless

</details>

---

## 12. What is the role of enabled column?

<details>
<summary>Show Answer</summary>

Controls account status.

If:

```text
enabled = false
```

👉 Authentication fails even if password is correct

</details>

---

## 13. Why does hasRole fail but hasAuthority works in JDBC?

<details>
<summary>Show Answer</summary>

Because:

* hasRole("ADMIN") → expects ROLE_ADMIN
* DB stores ADMIN

👉 Prefix mismatch

</details>

---

## 14. How would you design a scalable JDBC authentication system?

<details>
<summary>Show Answer</summary>

* Use proper indexing on username
* Normalize roles/permissions
* Cache UserDetails
* Use connection pooling
* Possibly move to JWT for stateless systems

</details>

---

## 15. How do you debug JDBC authentication failure?

<details>
<summary>Show Answer</summary>

Step-by-step:

1. Check DB data (username, password, enabled)
2. Verify password encoding
3. Check queries (correct columns/order)
4. Check authorities mapping
5. Enable Spring Security logs

👉 Debug layer-by-layer

</details>

---

# 🔥 Ultra Hard Scenario Questions

---

## 🧩 Q1: User exists, password correct, enabled true → still login fails

<details>
<summary>Show Answer</summary>

Likely causes:

* Password encoding mismatch
* Query returning wrong columns
* Wrong AuthenticationProvider config

</details>

---

## 🧩 Q2: Login works, but after restart fails

<details>
<summary>Show Answer</summary>

Possible reasons:

* Data not persisted
* Encoding changed
* Schema mismatch

</details>

---

## 🧩 Q3: Same user works in one service but not another

<details>
<summary>Show Answer</summary>

Configuration mismatch:

* Different queries
* Different encoders
* Different role mappings

</details>

---

## 🧩 Q4: Authorities present but still 403

<details>
<summary>Show Answer</summary>

Mismatch in:

* ROLE_ prefix
* hasRole vs hasAuthority
* Method-level security

</details>

---

# 🧠 Final Interview Insight

Most candidates fail because they:

* Focus only on flow
* Ignore DB mapping
* Ignore encoding
* Ignore roles

👉 Real expertise = **flow + DB + config + debugging**

---

# 🔥 Interview Killer Statement

👉 *“JDBC authentication issues are rarely in the authentication flow itself; they usually arise from incorrect schema mapping, query configuration, or password encoding mismatches.”*

---
