# 🔐 MODULE: Cryptography Basics (Deep Dive for Spring Security)

---

## 🔹 Core Idea

Cryptography in security is about:

* Protecting data
* Verifying identity
* Ensuring integrity

👉 In Spring Security, it is mainly used for:

* Password storage
* Token signing (JWT)
* Secure communication

---

## 🔹 1. Hashing vs Encryption (MOST IMPORTANT)

This is asked in almost every interview.

---

### 🔹 Hashing (One-Way Function)

* Converts data → fixed-length hash
* **Cannot be reversed**

Example:

```
password → 5f4dcc3b5aa765d61d8327deb882cf99
```

👉 Used for:

* Password storage

---

### 🔥 Key Properties

* One-way (irreversible)
* Same input → same output
* Fast computation
* Fixed length output

---

### 🔴 Problem (Important)

Same password → same hash
👉 Vulnerable to attacks

---

---

### 🔹 Encryption (Two-Way Function)

* Converts data → encrypted form
* Can be **decrypted back**

Example:

```
Hello → (Encrypted) → Hello (after decryption)
```

👉 Used for:

* Data transmission (HTTPS)

---

### 🔥 Key Difference

| Feature      | Hashing          | Encryption           |
| ------------ | ---------------- | -------------------- |
| Reversible   | ❌ No             | ✅ Yes                |
| Use case     | Password storage | Secure communication |
| Key required | ❌ No             | ✅ Yes                |

---

## 🔹 2. Salting (VERY IMPORTANT)

---

### 🔹 Problem Without Salt

If two users have same password:

```
password123 → same hash
```

👉 Hacker can detect patterns

---

### 🔹 What is Salt?

* Random value added to password before hashing

```
password + randomSalt → hash
```

---

### 🔥 Benefits

* Same password → different hash
* Protects against rainbow table attacks

---

### 🔥 In Spring Security

BCrypt automatically:

* Generates salt
* Stores it inside hash

👉 You don’t manage salt manually

---

## 🔹 3. Password Hashing Algorithms

---

### ❌ Weak Algorithms (Never use)

* MD5
* SHA-1

👉 Fast → easy to crack via brute force

---

### ✅ Strong Algorithms

---

### 🔹 BCrypt (MOST IMPORTANT)

* Adaptive hashing (slow by design)
* Includes salt
* Work factor (strength)

Example:

```
$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36
```

👉 Used in Spring:

```java
new BCryptPasswordEncoder()
```

---

### 🔹 Why Slow is Good?

👉 Slows down brute-force attacks

---

### 🔹 SCrypt / Argon2 (Advanced)

* More secure than BCrypt
* Memory-intensive

👉 Used in high-security systems

---

## 🔹 4. Password Encoding in Spring Security

---

### 🔹 PasswordEncoder Interface

Core interface:

```java
encode()
matches()
```

---

### 🔹 Example

```java
PasswordEncoder encoder = new BCryptPasswordEncoder();

String hash = encoder.encode("password");

boolean match = encoder.matches("password", hash);
```

---

### 🔥 Important Rule

👉 NEVER compare passwords manually
👉 ALWAYS use `matches()`

---

## 🔹 5. DelegatingPasswordEncoder (Advanced but Important)

---

### 🔹 Why Needed?

Different users may have passwords encoded with different algorithms.

---

### 🔹 Format

```
{bcrypt}$2a$10$...
{noop}password
```

---

### 🔹 How it works

* Prefix tells which algorithm to use
* Spring picks encoder automatically

---

## 🔹 6. Symmetric vs Asymmetric Encryption

---

### 🔹 Symmetric Encryption

* Same key for encryption & decryption

Example:

```
AES
```

👉 Fast, used in:

* HTTPS (partially)

---

---

### 🔹 Asymmetric Encryption

* Two keys:

    * Public key
    * Private key

Example:

```
RSA
```

---

### 🔥 Use Case

* Secure key exchange
* JWT signing

---

### 🔥 Simple Flow

```
Public key → encrypt  
Private key → decrypt  
```

---

## 🔹 7. Digital Signature (VERY IMPORTANT for JWT)

---

### 🔹 What is it?

Ensures:

* Data integrity
* Authenticity

---

### 🔹 How it works

```
Data → Sign with private key → Signature
Receiver → Verify using public key
```

---

### 🔥 In JWT

* JWT is **signed**
* If modified → signature breaks

👉 That’s why tampering fails

---

## 🔹 8. Brute Force & Security Concepts

---

### 🔹 Brute Force Attack

* Trying all possible passwords

👉 Defense:

* Strong hashing (BCrypt)
* Rate limiting
* Account lock

---

### 🔹 Rainbow Table Attack

* Precomputed hashes

👉 Defense:

* Salting

---

## 🔹 9. HTTPS (Where Encryption is Used)

---

### 🔹 What HTTPS does

* Encrypts communication between client & server

---

### 🔹 Uses Both:

* Asymmetric → key exchange
* Symmetric → fast data transfer

---

## 🧠 Final Mental Model

* Password → **Hash (store safely)**
* Data → **Encrypt (protect transmission)**
* JWT → **Sign (verify integrity)**

---

## ⚠️ Common Mistakes

* Using MD5/SHA ❌
* Storing plain passwords ❌
* Comparing raw passwords ❌
* Thinking encryption = hashing ❌

---

## ✅ What You Should Be Able to Answer

* Hashing vs Encryption
* Why BCrypt is used
* What is salting
* How JWT is secured
* Why hashing must be slow

---

---

# 🔐 EXTREME TRAP MCQs (Cryptography)

---

## 1. Why is hashing preferred over encryption for storing passwords?

1. Hashing is faster
2. Hashing is reversible
3. Hashing is one-way and prevents password recovery
4. Encryption uses more memory

<details>
<summary>Show Answer</summary>

**3. Hashing is one-way and prevents password recovery**

Reason:

* Even if DB is compromised → attacker cannot reverse password
* Encryption can be decrypted → unsafe for passwords

</details>

---

## 2. Two users have same password, but stored hashes are different. Why?

1. Different hashing algorithm
2. Salting applied
3. Encoding error
4. Password mismatch

<details>
<summary>Show Answer</summary>

**2. Salting applied**

Reason:

* Random salt added → same password → different hash

</details>

---

## 3. Why is MD5 considered insecure?

1. It is too slow
2. It is reversible
3. It is fast and vulnerable to brute force
4. It uses too much memory

<details>
<summary>Show Answer</summary>

**3. It is fast and vulnerable to brute force**

Reason:

* Fast hashing = easy to try millions of guesses per second

</details>

---

## 4. What is the main purpose of salt?

1. Encrypt password
2. Reduce hash size
3. Prevent rainbow table attacks
4. Speed up hashing

<details>
<summary>Show Answer</summary>

**3. Prevent rainbow table attacks**

</details>

---

## 5. If salt is NOT used, what is the biggest risk?

1. Password becomes reversible
2. Same passwords produce identical hashes
3. Encryption fails
4. JWT breaks

<details>
<summary>Show Answer</summary>

**2. Same passwords produce identical hashes**

</details>

---

## 6. Which statement is MOST correct about BCrypt?

1. It is reversible
2. It is fast for performance
3. It includes salt internally
4. It does not support password matching

<details>
<summary>Show Answer</summary>

**3. It includes salt internally**

</details>

---

## 7. Why is slow hashing desirable?

1. Saves memory
2. Prevents decryption
3. Slows brute-force attacks
4. Reduces DB load

<details>
<summary>Show Answer</summary>

**3. Slows brute-force attacks**

</details>

---

## 8. What happens if attacker modifies JWT payload?

1. Server accepts it
2. Only authorization fails
3. Signature validation fails
4. Token becomes encrypted

<details>
<summary>Show Answer</summary>

**3. Signature validation fails**

</details>

---

## 9. Which algorithm is used for encryption (not hashing)?

1. BCrypt
2. SHA-256
3. AES
4. MD5

<details>
<summary>Show Answer</summary>

**3. AES**

</details>

---

## 10. Which is TRUE about encryption?

1. It is always one-way
2. It cannot be reversed
3. It requires a key
4. It replaces hashing

<details>
<summary>Show Answer</summary>

**3. It requires a key**

</details>

---

## 11. In JWT, what ensures token integrity?

1. Payload
2. Header
3. Signature
4. Expiry

<details>
<summary>Show Answer</summary>

**3. Signature**

</details>

---

## 12. If two hashes are identical, what can you conclude?

1. Passwords are definitely same
2. Hash algorithm failed
3. Possibly same input and same salt
4. Encryption used

<details>
<summary>Show Answer</summary>

**3. Possibly same input and same salt**

👉 Without salt → definitely same
👉 With salt → not guaranteed

</details>

---

## 13. Why should passwords NEVER be decrypted?

1. It is slow
2. It is not supported
3. It indicates encryption was used instead of hashing
4. It breaks JWT

<details>
<summary>Show Answer</summary>

**3. It indicates encryption was used instead of hashing**

</details>

---

## 14. What is the biggest advantage of asymmetric encryption?

1. Faster than symmetric
2. No key required
3. Secure key exchange
4. Smaller output

<details>
<summary>Show Answer</summary>

**3. Secure key exchange**

</details>

---

## 15. Which scenario is MOST insecure?

1. BCrypt with salt
2. SHA-256 without salt
3. Argon2 hashing
4. Encrypted password

<details>
<summary>Show Answer</summary>

**2. SHA-256 without salt**

</details>

---

## 16. What does PasswordEncoder.matches() do internally?

1. Decrypts password
2. Compares raw strings
3. Hashes input and compares
4. Calls database

<details>
<summary>Show Answer</summary>

**3. Hashes input and compares**

</details>

---

## 17. Which is TRUE about symmetric encryption?

1. Uses two keys
2. Uses same key for encryption & decryption
3. Cannot be decrypted
4. Used for hashing

<details>
<summary>Show Answer</summary>

**2. Uses same key for encryption & decryption**

</details>

---

## 18. Why is JWT not encrypted by default?

1. Encryption is not supported
2. Payload must be readable
3. Signature is enough for integrity
4. It reduces performance

<details>
<summary>Show Answer</summary>

**3. Signature is enough for integrity**

👉 JWT is signed, not encrypted

</details>

---

## 19. What is the purpose of digital signature?

1. Encrypt data
2. Compress data
3. Verify integrity and authenticity
4. Store password

<details>
<summary>Show Answer</summary>

**3. Verify integrity and authenticity**

</details>

---

## 20. Which is the MOST dangerous practice?

1. Using BCrypt
2. Using JWT
3. Storing plain text passwords
4. Using HTTPS

<details>
<summary>Show Answer</summary>

**3. Storing plain text passwords**

</details>

---

# 🧠 What You Just Mastered

* Hashing vs encryption traps
* Salt importance
* JWT signature logic
* BCrypt internals
* Real attack scenarios

---

# 🔥 If You Can Solve These

👉 You’re at **top product-company level for cryptography basics**

---

---

# 🔐 Real Interview Scenarios (Cryptography – Hard Level)

---

## 🧩 Scenario 1: Password Leak Incident

Your DB is leaked. Passwords are stored using:

```id="q1p9ka"
SHA-256(password)
```

👉 What is the risk?

<details>
<summary>Show Answer</summary>

**High risk (critical vulnerability)**

Reason:

* SHA-256 is fast → easy brute force
* No salt → same passwords → same hashes
* Rainbow tables can crack many passwords quickly

👉 Correct fix:

* Use BCrypt/Argon2 with salt

</details>

---

## 🧩 Scenario 2: Same Password, Same Hash

You notice:

```id="r5m8zn"
User1 → abc123 → hash1  
User2 → abc123 → hash1
```

👉 What does this indicate?

<details>
<summary>Show Answer</summary>

**No salting applied**

Reason:

* Same password should NOT produce same hash

👉 Security flaw:

* Attacker can detect common passwords easily

</details>

---

## 🧩 Scenario 3: Developer Stores Encrypted Password

Developer uses:

```id="2c2jjx"
AES.encrypt(password)
```

👉 What’s wrong?

<details>
<summary>Show Answer</summary>

**Wrong approach**

Reason:

* Encryption is reversible
* If key is compromised → all passwords exposed

👉 Passwords must be hashed, not encrypted

</details>

---

## 🧩 Scenario 4: BCrypt Strength Misconfigured

Config:

```java id="fc0ycv"
new BCryptPasswordEncoder(4)
```

👉 What is the issue?

<details>
<summary>Show Answer</summary>

**Too weak (low work factor)**

Reason:

* Lower strength → faster hashing
* Easier brute-force attack

👉 Recommended: 10–12+

</details>

---

## 🧩 Scenario 5: Password Comparison Bug

Developer writes:

```java id="y8d6bd"
if(rawPassword.equals(storedHash)) {
   login();
}
```

👉 What’s wrong?

<details>
<summary>Show Answer</summary>

**Incorrect comparison**

Reason:

* Stored value is hashed
* Raw password must be hashed before comparison

👉 Correct:

```java
passwordEncoder.matches(rawPassword, storedHash);
```

</details>

---

## 🧩 Scenario 6: JWT Tampering Attack

Attacker modifies:

```json id="q0q1xt"
"role": "ADMIN"
```

👉 Why does server reject it?

<details>
<summary>Show Answer</summary>

**Signature mismatch**

Reason:

* JWT signature is based on payload
* Any change breaks signature

👉 Server verifies signature → rejects token

</details>

---

## 🧩 Scenario 7: JWT Not Expiring

System never checks token expiry.

👉 What is the risk?

<details>
<summary>Show Answer</summary>

**Severe security risk**

Reason:

* Stolen token can be used forever

👉 Fix:

* Validate expiration (`exp`)
* Use refresh tokens

</details>

---

## 🧩 Scenario 8: Token Stored in LocalStorage

Frontend stores JWT in LocalStorage.

👉 What is the risk?

<details>
<summary>Show Answer</summary>

**XSS attack vulnerability**

Reason:

* Malicious JS can read LocalStorage
* Token can be stolen

👉 Better:

* HttpOnly cookies (trade-offs apply)

</details>

---

## 🧩 Scenario 9: Same Salt Used for All Users

System uses:

```id="zv1v5j"
password + "STATIC_SALT"
```

👉 What is the issue?

<details>
<summary>Show Answer</summary>

**Weak security**

Reason:

* Salt must be unique per user
* Static salt still allows pattern detection

👉 Use random salt (BCrypt does this automatically)

</details>

---

## 🧩 Scenario 10: JWT Signed with Weak Secret

Secret:

```id="w8kk7f"
secret = "123"
```

👉 What is the risk?

<details>
<summary>Show Answer</summary>

**Token can be forged**

Reason:

* Weak secret → attacker can guess
* Can generate valid JWT with admin role

👉 Use strong, long secret key

</details>

---

## 🧩 Scenario 11: Password Reset Feature Bug

Reset API accepts:

```id="l6r2ot"
POST /reset?email=user@gmail.com
```

No verification.

👉 What is the issue?

<details>
<summary>Show Answer</summary>

**Account takeover vulnerability**

Reason:

* Anyone can reset password

👉 Fix:

* Use secure token (one-time, expiring)
* Email verification link

</details>

---

## 🧩 Scenario 12: Logging Passwords

Logs contain:

```id="zyv4a9"
User login: password=admin123
```

👉 Why dangerous?

<details>
<summary>Show Answer</summary>

**Sensitive data exposure**

Reason:

* Logs can be accessed
* Leads to credential leakage

👉 Never log passwords

</details>

---

## 🧩 Scenario 13: HTTPS Not Used

Login API uses HTTP.

👉 What happens?

<details>
<summary>Show Answer</summary>

**Credentials exposed**

Reason:

* Data sent in plain text
* Can be intercepted (MITM attack)

👉 Always use HTTPS

</details>

---

## 🧩 Scenario 14: JWT Without Signature

System uses unsigned JWT.

👉 What is the risk?

<details>
<summary>Show Answer</summary>

**Anyone can modify token**

Reason:

* No integrity check
* Attacker can change role, user, etc.

</details>

---

## 🧩 Scenario 15: Brute Force Login Attack

Attacker tries 1M passwords.

👉 What is missing?

<details>
<summary>Show Answer</summary>

**Protection mechanisms missing**

Fix:

* Rate limiting
* Account lock
* CAPTCHA

</details>

---

# 🧠 What You Just Practiced

* Real production vulnerabilities
* Password storage mistakes
* JWT attack vectors
* Secure design thinking

---

# 🔥 Interview Insight

Interviewers LOVE questions like:

* “What if DB is leaked?”
* “What if token is stolen?”
* “What if user modifies JWT?”

👉 You just covered all of them.

---
