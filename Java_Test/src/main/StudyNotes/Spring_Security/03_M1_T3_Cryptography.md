# 1.3 — Cryptography for Backend Engineers

> **Module 1 · Topic 3** · Prerequisites
> Baseline: Spring Security 6.x on Boot 3.x, Java 17+
> New to this? Read **In Plain English** below first, then come back to the Version Matrix.

---

## Version Matrix

| Concern | Spring Security 5.x | **Spring Security 6.x (baseline)** | Spring Security 7.x |
|---|---|---|---|
| Default `PasswordEncoder` | `DelegatingPasswordEncoder` with `bcrypt` | **same**, `bcrypt` strength 10 | same |
| Argon2 / SCrypt encoders | require BouncyCastle on the classpath | **same** (`spring-security-crypto` + BC) | same |
| `NoOpPasswordEncoder` | deprecated | **deprecated** — never use outside tests | deprecated |
| Key derivation for encryptors | `Encryptors.standard` = AES-256-CBC + PKCS5 | **`Encryptors.stronger` = AES-256-GCM** (preferred) | same |
| JWT signing support | `spring-security-oauth2-jose` (Nimbus) | **same** | same |

---

## Why This Exists

You will not implement a cipher. You *will* be asked, repeatedly, to explain why a password
is hashed and a session token is not, why bcrypt is deliberately slow, why a JWT you can read
on jwt.io is still secure, and what a salt actually accomplishes. Getting any of these wrong
in an interview is disqualifying, because they map directly to breach post-mortems.

Three primitives cover almost everything a backend engineer touches:

| Primitive | Reversible? | Needs a key? | Gives you |
|---|---|---|---|
| **Encoding** | Yes, by anyone | No | Transport compatibility. **Zero security.** |
| **Encryption** | Yes, with the key | Yes | Confidentiality |
| **Hashing** | No | No (MAC variants do) | Integrity, password storage |

---

## In Plain English

**The one-line version:** There are only three things you can do to a piece of data — rewrite it so
it travels safely, lock it so only a key-holder can read it, or crush it one-way so it can be
checked but never recovered — and almost every security mistake comes from confusing the three.

**An analogy.** Think of three different things you might do with a letter before posting it.

Writing the address in block capitals so the sorting machine can read it is **encoding**. You have
changed the appearance of the information for the benefit of the machinery, and anybody who glances
at the envelope can still read it perfectly. It hides nothing, and it was never meant to. Base64 is
exactly this, which is why HTTP Basic authentication provides no protection on its own.

Putting the letter in a locked strongbox is **encryption**. Whoever holds the key can open it and
read the original letter. This is genuinely private, but the privacy depends entirely on the key
staying safe — and a key that exists can be stolen.

Burning the letter and keeping only the peculiar shape of the ash is **hashing**. You cannot
reconstruct the letter from the ash. But if somebody hands you a letter claiming it is the same
one, you can burn theirs and compare the ash. That is precisely how password checking works: the
server never stores your password, only the ash, and at login it burns what you typed and compares.

The analogy also explains why speed is bad here. If burning a letter took a millionth of a second,
somebody with a stolen pile of ash could burn every word in the dictionary and find the match.
Password hashing therefore uses a deliberately slow fire.

**How it actually works, step by step.**

When a user registers, you never save what they typed. You pass it to a `PasswordEncoder`, which
runs it through a *key derivation function* — a hash that has been made intentionally slow and
memory-hungry. Before hashing, it mixes in a *salt*: a random value generated fresh for this one
password. The salt is not a secret and is stored right next to the hash. Its job is to make two
users who both chose `password123` end up with completely different stored values, which destroys
the attacker's ability to precompute answers in advance.

bcrypt packs all of this into one string. The value
`$2a$10$N9qo8uLOickgx2ZMRZoMye.IjZAgcfl7p92ldGxad68LJZdL17lhWy` reads as: algorithm `2a`, cost
factor `10` (meaning 2 to the power of 10, so 1024 rounds of work), then a 22-character salt, then
the digest itself. Because the salt and the cost are inside the stored string, the check method
`matches(rawPassword, storedString)` needs nothing else to do its job.

Spring wraps all of the available algorithms in a `DelegatingPasswordEncoder`. It writes a prefix
such as `{bcrypt}` or `{argon2}` in front of every hash, so the stored value announces which
algorithm produced it. That one detail is what makes it possible to change algorithms later without
asking two million users to reset their passwords: old rows still verify with the old algorithm, and
each row gets quietly rewritten in the new format the next time its owner logs in.

Tokens are a different problem, and here the mistake to avoid is randomness that is not really
random. `java.util.Random` produces a predictable sequence — see two of its outputs and you can
compute the rest — so any password-reset link or session identifier built with it can be forged.
`SecureRandom` is the correct tool, and thirty-two random bytes encoded as text gives you a token
nobody can guess.

Finally there is signing. A *signature* or a *message authentication code* proves that a piece of
data has not been altered and came from someone holding the right key. This is what protects a JWT.
Note carefully what it does not do: the contents of a normal JWT are plain readable text that
anybody can decode. The signature stops tampering; it does not hide anything. When you compare
such a signature, or an API key, do not use `equals`, because it stops at the first character that
differs, and the time it takes therefore reveals how much of the guess was correct.

**Why should a beginner care?** If you encrypt passwords rather than hashing them, one stolen key
exposes every password your users own — including the ones they reuse for their bank. If you use a
fast hash such as SHA-256, a stolen database is cracked over a weekend on a gaming graphics card.
If you generate password-reset tokens with `Math.random()`, an attacker can predict the next one
and take over accounts without guessing anything. And if you put a user's email address into a JWT
believing it is encrypted, you have published it to anyone who sees the token.

**Words you will meet in this file**

| Term | In plain words |
|---|---|
| Encoding | Rewriting data into another shape so it travels safely. Reversible by anyone; no security at all. |
| Base64 | The most common encoding. Turns arbitrary bytes into plain text. Decoded in one line of code. |
| Encryption | Locking data so only someone with the key can read it. Reversible with the key. |
| Hashing | A one-way crush. You can check a guess against the result, but never recover the original. |
| `PasswordEncoder` | Spring's interface for turning a password into a stored hash and later checking a guess. |
| Key derivation function (KDF) | A hash made deliberately slow and memory-hungry, which is what passwords need. |
| bcrypt / scrypt / Argon2 | The three respectable password algorithms, oldest to newest. Argon2 is the current best. |
| Cost factor / strength | The dial that decides how slow the hashing is. Higher is safer and more expensive. |
| Salt | A random value unique to each password, stored openly, so identical passwords hash differently. |
| Pepper | A single secret value kept outside the database, so stealing the database alone is not enough. |
| Symmetric | One shared secret does both the locking and the unlocking. Fast, but everyone must hold it. |
| Asymmetric | A pair of keys: one private key creates, one public key verifies. Nobody needs the private one to check. |
| HMAC | A tamper-check built from a shared secret. Anyone who can verify it can also create it. |
| Digital signature | A tamper-check made with a private key. Only the holder can create it; anyone can verify it. |
| JWS / JWT | The ordinary signed token. Signed means tamper-proof; it does **not** mean hidden. |
| JWE | The much rarer encrypted token, used when the contents genuinely must be unreadable. |
| `SecureRandom` | The random number generator whose output cannot be predicted. Use it for anything secret. |
| Constant-time comparison | Checking two secrets in a way that always takes the same time, so the timing leaks nothing. |
| `DelegatingPasswordEncoder` | Spring's wrapper that tags each hash with its algorithm so you can migrate later. |
| AES-GCM | The recommended encryption mode, because it detects tampering as well as hiding the contents. |

**If you remember only one thing:** Passwords get hashed with a deliberately slow algorithm and a
random salt, never encrypted — because encryption implies a key, and a key can be stolen.

---

## Core Concepts

### 1. Encoding Is Not Security

**In simple terms:** Base64 and its relatives only change how data looks so it can travel safely.
Anyone can undo them instantly, so they hide nothing whatsoever.

Base64, URL-encoding, hex, HTML entities — all reversible by anyone with a browser console.

```
Authorization: Basic YWRtaW46cDRzc3cwcmQ=
                     └─ base64 decode → admin:p4ssw0rd
```

The only purpose of the Base64 in HTTP Basic is to make arbitrary bytes safe inside an ASCII
header. Any answer that treats it as obfuscation is wrong.

The same applies to a JWT: `eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhbGljZSJ9.<sig>` — the first two
segments are `base64url` **plaintext**. Paste any JWT into jwt.io and read it. The signature
protects *integrity*, not *confidentiality*.

### 2. Hashing vs Encryption

**In simple terms:** Encryption can be undone by whoever holds the key, hashing cannot be undone at
all, and that is exactly why passwords must be hashed rather than encrypted.

| | Hashing | Encryption |
|---|---|---|
| Direction | One-way | Two-way |
| Output size | Fixed | Proportional to input |
| Key | None (except HMAC) | Required |
| Use for passwords | **Yes** | **Never** |
| Use for data at rest | No | Yes |
| Examples | SHA-256, bcrypt, Argon2 | AES-GCM, RSA, ChaCha20 |

**Passwords must be hashed, never encrypted.** Encryption is reversible, which means a key
exists, which means an attacker who gets the database *and* the key gets every plaintext
password — and users reuse passwords across sites, so you have breached other companies too.
Hashing means there is no key to steal and no plaintext to recover.

This has a corollary interviewers probe: **you must never be able to email a user their
password.** If a system can, it is storing it reversibly, and that is a finding.

### 3. Symmetric vs Asymmetric

**In simple terms:** Either everyone shares one secret, which means everyone who can check a token
can also fake one, or you split it into a private key that creates and a public key that only checks.

| | Symmetric | Asymmetric |
|---|---|---|
| Keys | One shared secret | Public/private pair |
| Speed | Fast (AES-NI hardware accelerated) | 100–1000× slower |
| Key distribution | **Hard** — every party needs the secret | Easy — publish the public key |
| Typical size | AES-256 | RSA-2048/4096, EC P-256 |
| Spring usage | `Encryptors.stronger`, JWT `HS256` | TLS, JWT `RS256`/`ES256`, mTLS |

Real systems use both: TLS uses asymmetric crypto to agree on a symmetric session key, then
uses the fast symmetric cipher for the actual traffic. This is called a hybrid cryptosystem.

**Applied to JWTs**, the choice is architectural, not cryptographic:

- **`HS256` (HMAC, symmetric)** — signer and verifier share one secret. Fine when one service
  both issues and validates. In a microservices estate it means every service holds the
  secret, so **any** service can forge a token for any other. That is usually disqualifying.
- **`RS256` / `ES256` (asymmetric)** — the authorization server holds the private key; every
  resource server fetches the public key from the JWKS endpoint. Resource servers can verify
  but not forge. This is the correct default for distributed systems.

`ES256` over `RS256` when token size matters: an ECDSA P-256 signature is 64 bytes versus
256 bytes for RSA-2048, and key generation and signing are faster.

### 4. Why Fast Hashes Are Wrong for Passwords

**In simple terms:** A fast hash lets an attacker with your stolen database try billions of guesses
a second, so password algorithms are built to be slow on purpose.

SHA-256 is an excellent hash — and a terrible password hash, precisely *because* it is
excellent. It is designed to be fast. A modern GPU computes billions of SHA-256 hashes per
second, so an attacker with your database brute-forces short passwords in minutes.

Password storage needs a **Key Derivation Function** — a hash deliberately made slow and
memory-hungry:

| KDF | Year | Hard on | Tunable | Verdict |
|---|---|---|---|---|
| MD5, SHA-1, SHA-256 | — | nothing | no | **Never for passwords** |
| PBKDF2 | 2000 | CPU (iterations) | iterations | Acceptable; FIPS-approved; weak against GPUs |
| **bcrypt** | 1999 | CPU + small memory (4 KB) | cost factor | **Good default.** Spring's default |
| scrypt | 2009 | CPU + configurable memory | N, r, p | Good; GPU/ASIC resistant |
| **Argon2id** | 2015 | CPU + memory + parallelism | m, t, p | **Best available.** Password Hashing Competition winner |

The cost factor is logarithmic: bcrypt strength 10 means 2¹⁰ = 1024 rounds; strength 12 is
four times slower than 10. Target roughly **250–1000 ms** on your production hardware, then
re-tune every couple of years as hardware improves.

```java
// Spring Security default is strength 10.
new BCryptPasswordEncoder();      // ~50-100ms on modern hardware
new BCryptPasswordEncoder(12);    // ~250-400ms  <- usually the right production value
```

**bcrypt's 72-byte input limit** is a real gotcha. Input beyond 72 bytes is silently
truncated, so a 200-character passphrase is only as strong as its first 72 bytes. Worse, if
you "pre-hash" with SHA-256 and hex-encode to work around it, you get 64 hex chars — safe —
but if you base64 a SHA-512 you get 88 chars and are truncated again. Argon2 has no such
limit.

### 5. Salt — And Why It Is Not a Secret

**In simple terms:** A salt is a random extra ingredient per password, so two people who chose the
same password get different stored values and an attacker cannot prepare answers ahead of time.

A **salt** is a unique random value per password, stored alongside the hash.

```
Without salt:  hash("password123") is identical for every user who chose it
               -> one rainbow table cracks all of them at once
               -> equal hashes in the dump reveal which users share a password

With salt:     hash("password123" + "a8f3d9...")  unique per user
               -> rainbow tables are useless
               -> the attacker must brute-force each password independently
```

The salt is **not secret**. Its job is uniqueness, not confidentiality. bcrypt stores it
inside the output string:

```
$2a$10$N9qo8uLOickgx2ZMRZoMye.IjZAgcfl7p92ldGxad68LJZdL17lhWy
│  │  │ └─────────────────────┴──────────────────────────────┘
│  │  │              22-char salt          31-char digest
│  │  └─ cost factor = 10 (2^10 rounds)
│  └──── bcrypt version identifier
└─────── always $ delimited
```

This is why `matches(raw, encoded)` needs no extra arguments — everything required to
recompute the hash is in the stored string.

A **pepper** is different: a single secret value, shared across all passwords, kept *outside*
the database (in an HSM, a KMS, or an environment variable). It means a database dump alone
is insufficient to mount an offline attack. Spring has no built-in pepper support; you
implement it by HMAC-ing the password with the pepper before passing it to the encoder — and
you must then plan for pepper rotation, which is why many teams skip it.

### 6. HMAC vs Digital Signature

**In simple terms:** Both prove a message was not tampered with, but with a shared secret either
side could have written it, whereas with a signature only the private key holder could have.

Both prove integrity and authenticity. They differ in who can produce and who can verify.

| | HMAC | Digital signature |
|---|---|---|
| Key | One shared secret | Private key signs, public key verifies |
| Who can create | Anyone with the secret | Only the private key holder |
| Who can verify | Anyone with the secret (so also anyone who can forge) | Anyone with the public key |
| **Non-repudiation** | **No** | **Yes** |
| Speed | Very fast | Slower |
| JWT alg | `HS256` | `RS256`, `ES256`, `PS256` |

Non-repudiation is the discriminator. With HMAC, both parties can produce a valid tag, so
neither can prove the other created a given message. With a signature, only the private key
holder could have produced it.

### 7. Constant-Time Comparison

**In simple terms:** Comparing secrets with `equals` stops at the first wrong character, so the time
it takes tells the attacker how much of their guess was right, letting them work it out one character
at a time.

```java
// VULNERABLE — returns at the first differing byte.
if (providedApiKey.equals(storedApiKey)) { ... }
```

`String.equals` short-circuits. The time it takes leaks how many leading characters matched.
Over enough requests an attacker recovers the secret byte by byte. This is practical over a
LAN and, with statistical averaging, over the internet.

```java
// SAFE — examines every byte regardless of where the first difference is.
if (MessageDigest.isEqual(provided.getBytes(UTF_8), stored.getBytes(UTF_8))) { ... }
```

Apply it to any secret **the attacker supplies and can vary**: API keys, HMAC tags, CSRF
tokens, password-reset tokens, webhook signatures. Spring does this internally —
`CsrfFilter` uses `MessageDigest.isEqual`, and `BCryptPasswordEncoder.matches` uses a
constant-time check on the digest.

Password hash comparison is less critical (the attacker cannot vary the stored hash), but
using the safe API costs nothing.

### 8. Randomness

**In simple terms:** The ordinary random number generators produce a guessable sequence, so any
token you build with them can be predicted; secrets need `SecureRandom`.

```java
new Random()                    // WRONG - predictable, seeded from the clock
Math.random()                   // WRONG - same underlying generator
ThreadLocalRandom.current()     // WRONG for secrets - fast, not cryptographic
new SecureRandom()              // CORRECT
UUID.randomUUID()               // acceptable - backed by SecureRandom, 122 bits of entropy
```

`java.util.Random` is a 48-bit linear congruential generator. Observing two outputs lets you
reconstruct the seed and predict every future value. Any token generated with it — password
reset, session ID, CSRF token, OTP — is forgeable.

```java
// Generating a URL-safe token with 256 bits of entropy.
byte[] bytes = new byte[32];
new SecureRandom().nextBytes(bytes);
String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
```

Spring provides `org.springframework.security.crypto.keygen.KeyGenerators.string()` and
`secureRandom()` for exactly this.

---

## Working Code

```java
package com.example.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CryptoConfig {

    /**
     * The default: bcrypt for new passwords, with the ability to VERIFY legacy formats.
     * Stored values are prefixed: {bcrypt}$2a$10$..., {noop}plain, {pbkdf2}...
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        String idForEncode = "argon2";

        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put("argon2", Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8());
        encoders.put("bcrypt", new BCryptPasswordEncoder(12));
        encoders.put("scrypt", SCryptPasswordEncoder.defaultsForSpringSecurity_v5_8());
        encoders.put("pbkdf2", Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8());

        DelegatingPasswordEncoder delegating =
            new DelegatingPasswordEncoder(idForEncode, encoders);

        // Legacy rows written before we adopted the {id} prefix convention were bcrypt.
        delegating.setDefaultPasswordEncoderForMatches(new BCryptPasswordEncoder());

        return delegating;
    }
}
```

Transparent upgrade-on-login — how you migrate hashes without asking users to reset:

```java
package com.example.security;

import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JdbcUserDetailsPasswordService implements UserDetailsPasswordService {

    private final UserRepository users;

    public JdbcUserDetailsPasswordService(UserRepository users) {
        this.users = users;
    }

    /**
     * Spring calls this automatically after a SUCCESSFUL authentication when
     * PasswordEncoder.upgradeEncoding(storedHash) returns true - e.g. the row is
     * still {bcrypt} but idForEncode is now {argon2}, or the cost factor is too low.
     * The raw password is available here precisely because the user just typed it.
     */
    @Override
    public UserDetails updatePassword(UserDetails user, String newEncodedPassword) {
        users.updatePasswordHash(user.getUsername(), newEncodedPassword);
        return org.springframework.security.core.userdetails.User
                 .withUserDetails(user)
                 .password(newEncodedPassword)
                 .build();
    }
}
```

Symmetric encryption for data at rest (not for passwords):

```java
package com.example.security;

import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;

public class FieldEncryption {

    private final TextEncryptor encryptor;

    /**
     * @param password  the encryption key material, sourced from a KMS / vault - NEVER hard-coded
     * @param salt      a hex-encoded salt, generated once with KeyGenerators.string()
     */
    public FieldEncryption(String password, String salt) {
        // "stronger" = AES-256-GCM (authenticated encryption).
        // "standard" = AES-256-CBC, which gives confidentiality but NOT integrity.
        this.encryptor = Encryptors.delux(password, salt);
    }

    public String encrypt(String plaintext) {
        return encryptor.encrypt(plaintext);
    }

    public String decrypt(String ciphertext) {
        return encryptor.decrypt(ciphertext);
    }

    public static String newSalt() {
        return KeyGenerators.string().generateKey();   // 8-byte hex, SecureRandom backed
    }
}
```

Secure token generation and constant-time verification:

```java
package com.example.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public final class SecureTokens {

    private static final SecureRandom RANDOM = new SecureRandom();

    private SecureTokens() {}

    /** 256 bits of entropy, URL-safe, no padding. */
    public static String generate() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /** Constant-time comparison: never use String.equals for attacker-supplied secrets. */
    public static boolean matches(String provided, String expected) {
        if (provided == null || expected == null) {
            return false;
        }
        return MessageDigest.isEqual(
            provided.getBytes(StandardCharsets.UTF_8),
            expected.getBytes(StandardCharsets.UTF_8));
    }
}
```

Tests worth writing:

```java
class CryptoConfigTests {

    private final PasswordEncoder encoder = new CryptoConfig().passwordEncoder();

    @Test
    void sameInputProducesDifferentHashesBecauseOfSalt() {
        String a = encoder.encode("hunter2");
        String b = encoder.encode("hunter2");
        assertThat(a).isNotEqualTo(b);       // random salt per call
        assertThat(encoder.matches("hunter2", a)).isTrue();
        assertThat(encoder.matches("hunter2", b)).isTrue();
    }

    @Test
    void newHashesUseArgon2ButLegacyBcryptStillVerifies() {
        assertThat(encoder.encode("hunter2")).startsWith("{argon2}");
        String legacy = "{bcrypt}" + new BCryptPasswordEncoder().encode("hunter2");
        assertThat(encoder.matches("hunter2", legacy)).isTrue();
    }

    @Test
    void legacyHashesAreFlaggedForUpgrade() {
        String legacy = "{bcrypt}" + new BCryptPasswordEncoder().encode("hunter2");
        assertThat(encoder.upgradeEncoding(legacy)).isTrue();   // triggers UserDetailsPasswordService
    }

    @Test
    void bcryptSilentlyTruncatesBeyond72Bytes() {
        BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();
        String seventyTwo = "a".repeat(72);
        String hash = bcrypt.encode(seventyTwo);
        // The 73rd character is ignored entirely.
        assertThat(bcrypt.matches(seventyTwo + "DIFFERENT", hash)).isTrue();
    }
}
```

---

## Internals

### What `DelegatingPasswordEncoder` actually does

```java
// encode(): prefix the chosen algorithm id
public String encode(CharSequence rawPassword) {
    return this.idPrefix + this.idForEncode + this.idSuffix
         + this.passwordEncoderForEncode.encode(rawPassword);
}

// matches(): parse the prefix, route to the right encoder
public boolean matches(CharSequence rawPassword, String prefixEncodedPassword) {
    String id = extractId(prefixEncodedPassword);                 // text between { and }
    PasswordEncoder delegate = this.idToPasswordEncoder.get(id);
    if (delegate == null) {
        return this.defaultPasswordEncoderForMatches
                   .matches(rawPassword, prefixEncodedPassword);  // unprefixed legacy rows
    }
    String encoded = extractEncodedPassword(prefixEncodedPassword);
    return delegate.matches(rawPassword, encoded);
}

// upgradeEncoding(): true when the stored id is not the current idForEncode
public boolean upgradeEncoding(String prefixEncodedPassword) {
    String id = extractId(prefixEncodedPassword);
    if (!this.idForEncode.equalsIgnoreCase(id)) {
        return true;
    }
    return this.idToPasswordEncoder.get(id)
               .upgradeEncoding(extractEncodedPassword(prefixEncodedPassword));
}
```

This design is what makes algorithm migration possible without a mass password reset. Your
database holds a mix of `{bcrypt}`, `{pbkdf2}`, and `{argon2}` rows simultaneously, each
verifiable, and each upgraded to the current algorithm the next time its owner logs in.

### The upgrade-on-login flow

```mermaid
sequenceDiagram
    participant U as User
    participant P as DaoAuthenticationProvider
    participant E as DelegatingPasswordEncoder
    participant S as UserDetailsPasswordService
    participant DB as Database

    U->>P: username + raw password
    P->>DB: loadUserByUsername -> "{bcrypt}$2a$10$..."
    P->>E: matches(raw, "{bcrypt}$2a$10$...")
    E-->>P: true
    P->>E: upgradeEncoding("{bcrypt}$2a$10$...")
    E-->>P: true  (idForEncode is now argon2)
    P->>E: encode(raw) -> "{argon2}$argon2id$v=19$..."
    P->>S: updatePassword(user, newHash)
    S->>DB: UPDATE users SET password = ?
    P-->>U: authenticated
```

The key insight: this is the **only** moment the server legitimately holds the raw password,
so it is the only moment a re-hash is possible. Hence "upgrade on login" rather than a batch
migration.

### Why `BCryptPasswordEncoder.matches` is safe against timing attacks

```java
// BCrypt.checkpw (simplified)
byte[] hashed_bytes = hashed.getBytes(UTF_8);
byte[] try_bytes = try_hashed.getBytes(UTF_8);
if (hashed_bytes.length != try_bytes.length) {
    return false;
}
byte ret = 0;
for (int i = 0; i < try_bytes.length; i++) {
    ret |= hashed_bytes[i] ^ try_bytes[i];    // accumulate, never short-circuit
}
return ret == 0;
```

The XOR-accumulate loop touches every byte regardless of where the first mismatch occurs.
This is the canonical constant-time pattern.

---

## Configuration Reference

| Component | Option | Effect | Default |
|---|---|---|---|
| `BCryptPasswordEncoder` | `strength` | 2^strength rounds | `10` |
| `BCryptPasswordEncoder` | `version` | `$2a`, `$2b`, `$2y` | `$2a` |
| `Argon2PasswordEncoder` | `saltLength` | bytes | `16` |
| `Argon2PasswordEncoder` | `hashLength` | bytes | `32` |
| `Argon2PasswordEncoder` | `parallelism` | lanes | `1` |
| `Argon2PasswordEncoder` | `memory` | KB | `16384` (16 MB) |
| `Argon2PasswordEncoder` | `iterations` | time cost | `2` |
| `SCryptPasswordEncoder` | `cpuCost` (N) | must be a power of 2 | `65536` |
| `Pbkdf2PasswordEncoder` | `iterations` | | `310000` (v5_8 defaults) |
| `Encryptors.standard` | — | AES-256-**CBC**, no integrity | — |
| `Encryptors.stronger` / `delux` | — | AES-256-**GCM**, authenticated | — |
| `KeyGenerators.string()` | — | 8-byte hex salt from `SecureRandom` | — |

`Argon2PasswordEncoder` and `SCryptPasswordEncoder` require BouncyCastle:

```xml
<dependency>
  <groupId>org.bouncycastle</groupId>
  <artifactId>bcprov-jdk18on</artifactId>
</dependency>
```

---

## Production Concerns & Anti-Patterns

**Rolling your own crypto.** Custom XOR "encryption", home-made token formats, hand-written
signature verification. Use vetted primitives; every custom scheme has been broken.

**`NoOpPasswordEncoder` reaching production.** It exists only for tests and is deprecated with
a loud warning. A misconfigured profile is all it takes.

**Hard-coded keys.** `private static final String SECRET = "mySecretKey123"` ends up in Git
forever, in every developer's laptop, and in the container image. Keys belong in a KMS,
Vault, or at minimum an injected environment variable, with a documented rotation procedure.

**A single global salt.** Defeats the entire purpose. If every hash uses the same salt, one
rainbow table built for that salt cracks the whole database, and identical passwords still
produce identical hashes.

**bcrypt strength left at the framework default forever.** Strength 10 was calibrated years
ago. Re-benchmark on production hardware and raise it, relying on `upgradeEncoding` to
migrate existing rows.

**Using `Encryptors.standard` (CBC) and assuming integrity.** CBC gives confidentiality only.
Without a MAC, an attacker can flip ciphertext bits and cause controlled plaintext changes —
the padding-oracle family of attacks. Use `Encryptors.stronger`/`delux` (GCM), which is
authenticated.

**Logging secrets.** Logging the full `Authorization` header, the request body of a login
call, or a `UserDetails` whose `toString()` includes the hash. Scrub at the logging layer and
verify with a test.

**Accepting `alg: none` or letting the token dictate the algorithm.** The classic JWT attack:
the attacker changes the header to `{"alg":"none"}` or switches `RS256` to `HS256` and signs
with the public key as the HMAC secret. Always pin the expected algorithm server-side. Spring
Security's `NimbusJwtDecoder` does this correctly by default — do not hand-roll it.

**Reusing a GCM nonce.** Catastrophic for AES-GCM: two messages under the same key and nonce
leak the XOR of the plaintexts and can compromise the authentication key. Let the library
generate nonces; never supply a counter or a constant.

---

## Debugging Playbook

| Symptom | Likely root cause | Fix |
|---|---|---|
| `IllegalArgumentException: There is no PasswordEncoder mapped for the id "null"` | Stored hash has no `{id}` prefix and `DelegatingPasswordEncoder` is in use | Set `setDefaultPasswordEncoderForMatches(...)`, or backfill prefixes |
| Login fails though the password is definitely right | Hash written with one encoder, verified with another | Log the stored prefix; confirm one `PasswordEncoder` bean exists |
| Passwords stored in plaintext in the DB | `NoOpPasswordEncoder` active, or the service saved the raw value without encoding | Encode in the registration path; add a test asserting the stored value starts with `{` |
| Login is slow under load, CPU pinned | bcrypt cost too high for the request rate, or Basic auth re-hashing per request | Lower the cost, or authenticate once and issue a session/token |
| Long passphrases behave oddly | bcrypt's 72-byte truncation | Switch to Argon2, or pre-hash to a fixed short digest deliberately |
| Intermittent `AEADBadTagException` on decrypt | Ciphertext or key mismatch, or a nonce/salt changed between encrypt and decrypt | Ensure the exact same salt and key material; store them with the ciphertext |
| JWT verification fails after a key rotation | Cached JWKS still holds the old key | Verify the decoder refreshes JWKS on unknown `kid`; check cache TTL |
| Tokens are guessable / users report collisions | `java.util.Random` used instead of `SecureRandom` | Replace with `SecureRandom`; audit all token generation |

---

## Interview Q&A

### Q1. Why do we hash passwords instead of encrypting them?

<details>
<summary>Show answer</summary>

Because encryption is reversible, and reversibility is the liability.

Encryption requires a key. That key has to exist somewhere your application can reach it at
runtime — so an attacker who gets your database is likely to be able to get the key too
(same host, same config server, same compromised service account). The moment they have
both, they have every user's plaintext password.

That is far worse than it sounds, because people reuse passwords. A breach of your
encrypted-password database is effectively a breach of those users' email, banking, and
employer accounts.

Hashing removes the key from the equation entirely. There is nothing to steal that turns the
stored value back into a password. The attacker's only option is to guess, and a
deliberately-slow KDF makes guessing expensive.

The practical test of whether a system got this right: **can it email you your password?**
If yes, it is storing it reversibly, and that is a finding.

**Counter-question: fine, but why not just SHA-256? It's a hash.**

Because SHA-256 is designed to be *fast*, and speed is exactly what you do not want here.
SHA-256 is optimised so you can checksum a gigabyte file quickly; a consumer GPU does billions
of SHA-256 operations per second. Given a stolen hash, every password up to about eight
characters falls in hours.

Password storage needs the opposite property: deliberate slowness. bcrypt, scrypt, and Argon2
are *key derivation functions* with a tunable work factor. You pick a cost that makes one
verification take a few hundred milliseconds — imperceptible to a user logging in once, but it
turns a billion-guesses-per-second attack into a few thousand per second.

Argon2 goes further and is *memory*-hard: it requires a configurable amount of RAM per hash,
which destroys the GPU and ASIC advantage, because those get their parallelism from having
many weak cores with little memory each.

**Counter-question: so how do you choose the cost factor?**

Empirically, on production-representative hardware, not by copying a number from a blog post.
Benchmark `encode()` and target roughly 250–1000 ms.

Then sanity-check it against your throughput. If you do 100 logins per second and each burns
500 ms of CPU, you need 50 cores just for hashing. That constraint usually pushes you to
authenticate once and issue a session or token, rather than verifying a password on every
request — which is, incidentally, the strongest technical argument against HTTP Basic for
user traffic.

And write down the date you chose it, because hardware improves and you should re-tune every
couple of years. `upgradeEncoding` plus `UserDetailsPasswordService` makes that a
zero-downtime change.

**Counter-question: doesn't an expensive hash create a denial-of-service vector?**

Yes, and it is a real one that people miss. An unauthenticated attacker sends login attempts
with garbage credentials; each one costs you 500 ms of CPU and costs them nothing. A few
hundred concurrent requests saturate the server.

Mitigations: rate-limit unauthenticated login attempts at the edge before they reach the
application; cap the accepted password length (Argon2 on a 10 MB string is far worse than on
a 20-character one); and short-circuit obviously-invalid requests before hashing. What you
must *not* do is skip the hash when the username does not exist — that creates a timing
oracle for username enumeration. Spring's `DaoAuthenticationProvider` handles this by hashing
a dummy password when the user is not found, specifically to keep the timing uniform.
</details>

### Q2. What is a salt, and why is it stored in plaintext next to the hash?

<details>
<summary>Show answer</summary>

A salt is a unique random value generated per password and mixed in before hashing. Its
purpose is **uniqueness, not secrecy**, which is why storing it in the clear is correct.

Two attacks it defeats:

1. **Precomputation.** Without a salt, an attacker builds (or downloads) a rainbow table
   mapping common passwords to their hashes once, and then cracks every database instantly. A
   per-password salt means the table would have to be rebuilt for each salt value, which
   makes precomputation worthless.
2. **Correlation.** Without a salt, two users who chose the same password have identical
   hashes. An attacker looking at a dump immediately sees which accounts share a password,
   and cracking one cracks them all. It also reveals the most common passwords in your user
   base, which is a great place to start guessing.

bcrypt embeds the salt in its output string — `$2a$10$<22-char-salt><31-char-digest>` — which
is why `matches(raw, encoded)` needs no extra parameters. Everything needed to recompute is
in the stored value.

**Counter-question: if the salt is not secret, what is a pepper, and would you use one?**

A pepper is a *single secret value* shared across all passwords, stored **outside** the
database — in an HSM, a KMS, or at minimum an environment variable on a different trust
boundary. You HMAC the password with the pepper before handing it to the KDF.

The threat it addresses is narrow but real: a database-only compromise. SQL injection, a
leaked backup, a misconfigured snapshot — the attacker gets the hashes but not the
application's secrets. Without a pepper they can start cracking offline immediately. With
one, the hashes are useless without a second, separate compromise.

Would I use one? Only if the infrastructure genuinely separates the two, and only if I have
a rotation story. That is the catch: rotating a pepper means re-hashing every password, which
you can only do at login, so you must support the old and new pepper simultaneously for a
long transition. Many teams add a pepper, never plan rotation, and end up with a secret they
can never change. I would rather spend that effort on Argon2 with a well-tuned cost and solid
detection of database exfiltration.

**Counter-question: how long should a salt be?**

Long enough that collisions are negligible across your entire user base and across time.
16 bytes (128 bits) is the modern standard and is what Argon2 uses by default. bcrypt uses
16 bytes encoded as 22 base64-ish characters.

The failure mode to avoid is a salt derived from something non-random and low-entropy — the
username, the user ID, the registration timestamp. Those are predictable and often reused
across systems, so an attacker targeting a specific user can still precompute. The salt must
come from `SecureRandom`.
</details>

### Q3. Is a JWT encrypted? Walk me through what its signature actually guarantees.

<details>
<summary>Show answer</summary>

No. A standard JWT is a **JWS** — JSON Web Signature — which means signed, not encrypted. The
header and payload are `base64url`-encoded plaintext. Anyone who intercepts the token, or any
JavaScript on the page, can decode and read every claim.

The signature guarantees two things and no more:

1. **Integrity** — the token has not been modified since it was signed. Change one character
   of the payload and verification fails.
2. **Authenticity** — it was produced by a party holding the signing key.

It guarantees **nothing** about confidentiality. It also guarantees nothing about whether the
token is still *valid* — revocation and expiry are separate checks the verifier must perform
(`exp`, `nbf`, and your own denylist if you have one).

The practical rule: **never put anything in a JWT payload you would not put in a log file.**
An identifier, roles, expiry — fine. Email addresses, national IDs, internal hostnames,
feature flags that reveal your architecture — not fine.

If you genuinely need confidentiality there is **JWE** (JSON Web Encryption), a five-part
structure rather than three. It is rare, because the usual answer is to keep the sensitive
data on the server and put only a reference in the token.

**Counter-question: someone changes `"alg"` in the header to `"none"`. What happens?**

In a correct implementation, verification fails, because the server decides which algorithms
it accepts — the token does not get a vote.

In a naive hand-rolled implementation that reads `alg` from the header and dispatches on it,
`none` means "no signature required" and the attacker forges any token they like. This was a
widespread real vulnerability around 2015 and is still found in custom code today.

There is a subtler variant: **algorithm confusion**. The server is configured for `RS256`. The
attacker takes the *public* key — which is published at the JWKS endpoint, so they have it —
changes the header to `HS256`, and signs the token using that public key as the HMAC secret.
A server that reads `alg` from the token and passes "the key" to a generic verify function
will use the public key as an HMAC key, and the signature checks out.

The defence is the same in both cases: pin the accepted algorithm server-side.
`NimbusJwtDecoder.withJwkSetUri(...).jwsAlgorithm(SignatureAlgorithm.RS256)` does this, and it
is one of the strongest reasons to use `spring-security-oauth2-resource-server` instead of a
hand-written filter.

**Counter-question: HS256 versus RS256 — how do you choose?**

It is an architecture decision, not a cryptography one.

`HS256` is HMAC with a shared secret. Whoever can verify can also forge. That is acceptable
when exactly one service both issues and validates tokens — a monolith. In a microservices
estate it means every resource server holds the signing secret, so a compromise of your
least-important service lets the attacker mint admin tokens for your most-important one.
That is usually disqualifying.

`RS256` or `ES256` is asymmetric. The authorization server holds the private key; resource
servers fetch the public key from the JWKS endpoint and can verify but never forge. Key
rotation is handled by the `kid` header and JWKS refresh, with no coordinated deployment.
This is the correct default for anything distributed.

Between `RS256` and `ES256` I would pick `ES256` when token size matters — an ECDSA P-256
signature is 64 bytes versus 256 for RSA-2048, which is meaningful when the token rides on
every request — and `RS256` when I need maximum compatibility with older clients and
libraries.
</details>

### Q4. Why is `String.equals` unsafe for comparing an API key, and how bad is it really?

<details>
<summary>Show answer</summary>

`String.equals` compares character by character and returns as soon as it finds a difference.
That makes its execution time proportional to the length of the matching prefix, which leaks
information to anyone who can measure it.

The attack: send `aaaa...`, `baaa...`, `caaa...` and measure response times. The first
character that produces a measurably slower response is correct. Fix it, move to the second
character, repeat. A 32-character key that would take 2^160 guesses to brute-force falls in
roughly 32 × 256 requests.

How bad in practice? Over a LAN or from a co-located container, very practical — the timing
difference is microseconds and the network noise is low. Over the public internet it is
harder but not theoretical; with enough samples and statistical filtering it has been
demonstrated. And the "attacker" is increasingly *inside* your network perimeter in a
containerised environment.

The fix is trivial, so there is no reason to accept the risk:

```java
MessageDigest.isEqual(provided.getBytes(UTF_8), expected.getBytes(UTF_8));
```

It XOR-accumulates over every byte and only checks the result at the end.

**Counter-question: does that apply to comparing password hashes too?**

Less so, and it is worth explaining why rather than just saying "use it everywhere".

The attack needs the attacker to **control and vary** one side of the comparison while the
other stays fixed. With an API key, they control the submitted key and the stored one is
fixed — perfect conditions. With a password hash, the comparison is between two *derived*
values; the attacker controls the password, not the resulting hash, and a one-character
change to the password changes the entire digest unpredictably. They cannot walk it byte by
byte.

So it is not the same class of vulnerability. That said, `BCryptPasswordEncoder` uses a
constant-time check anyway, because it costs nothing and removes the need for this argument.

Where it *does* matter beyond API keys: HMAC signature verification on webhooks (Stripe,
GitHub — the attacker controls the signature they send), CSRF tokens, password-reset tokens,
and email-verification tokens. All of those are attacker-supplied and directly compared.

**Counter-question: `MessageDigest.isEqual` returns early if the lengths differ. Isn't that a leak too?**

Yes, it leaks the length — and that is considered acceptable, because the length of a token
is not secret. Your tokens are all the same length by construction, and an attacker can read
that from the documentation or from any token they legitimately hold.

If length genuinely were sensitive, you would hash both sides to a fixed size first and
compare the digests. But for API keys and signatures this is a non-issue.

The more interesting related leak is *lookup* timing: if you look the API key up in a database
and the query is faster for a non-existent key than for an existing one, you have leaked
existence regardless of how you compare. That is the same class of problem as username
enumeration, and it is solved the same way — do the same amount of work in both branches.
</details>

### Q5. Your database has 2 million bcrypt-10 password hashes. Security says move to Argon2. How do you do it with zero user disruption?

<details>
<summary>Show answer</summary>

You cannot batch-convert, and that is the crux: you do not have the plaintext passwords, and
re-hashing a bcrypt hash with Argon2 would produce `argon2(bcrypt(password))`, which is a
different scheme you would then have to support forever.

The only moment you legitimately hold the raw password is **during a successful login**. So
you migrate opportunistically, one user at a time, as they log in. Spring Security has
first-class support for exactly this.

**Step 1 — make both algorithms verifiable.** Use `DelegatingPasswordEncoder` with both
encoders registered, and set `idForEncode` to `argon2`. Existing rows must carry the
`{bcrypt}` prefix; if they do not, set `setDefaultPasswordEncoderForMatches(new BCryptPasswordEncoder())`
so unprefixed legacy rows still verify, and backfill the prefix with a simple `UPDATE`.

**Step 2 — implement `UserDetailsPasswordService`.** After a successful authentication,
`DaoAuthenticationProvider` calls `passwordEncoder.upgradeEncoding(storedHash)`. Since the
stored id is `bcrypt` and `idForEncode` is `argon2`, that returns `true`, so Spring re-encodes
the raw password with Argon2 and calls your `updatePassword` to persist it. The user notices
nothing.

**Step 3 — deploy and watch.** Add a metric on the `updatePassword` call so you can see the
migration curve. Typically 80–90% of active users convert within a few weeks.

**Step 4 — handle the tail.** After a defined window, the remaining `{bcrypt}` rows belong to
dormant accounts. Options: force a password reset on next login, or simply leave them — they
are still bcrypt-10, which is not broken, just not ideal. I would force a reset only if there
is a compliance requirement, because forced resets generate support load and push users toward
weaker, more memorable passwords.

**Step 5 — remove the bcrypt encoder** from the map only once the count of `{bcrypt}` rows is
zero, and verify with a query rather than an assumption.

**Counter-question: what is the risk window during this migration?**

The main one is operational, not cryptographic. Argon2's default in Spring is 16 MB of memory
per hash. If you have a login spike and 200 concurrent authentications, that is 3.2 GB of
transient allocation. On a container with a 2 GB limit you will OOM — and an OOM in the auth
service is an outage.

So before deploying I would load-test the login path at peak concurrency with the real Argon2
parameters, and size memory and connection pool limits accordingly. If the numbers are
uncomfortable, tune Argon2 down (lower memory, more iterations) rather than abandoning the
migration.

The second risk is the classic one: deploying the new encoder *without* registering the old
one, which locks out every existing user instantly. That is why step 1 exists and why I would
have an integration test that loads a real legacy hash from a fixture and asserts it still
verifies.

**Counter-question: a user's row gets upgraded, then you have to roll back the deployment. What happens?**

Their row now says `{argon2}` and the rolled-back application has no Argon2 encoder
registered, so `DelegatingPasswordEncoder` throws
`IllegalArgumentException: There is no PasswordEncoder mapped for the id "argon2"` — that user
cannot log in, and they get a 500 rather than a clean failure.

This is why the rollout has to be two deployments, not one:

1. **Release A** adds the Argon2 encoder to the map but leaves `idForEncode = "bcrypt"`.
   Nothing changes behaviourally; every instance can now *verify* Argon2. Let this bake and
   confirm it is fully rolled out.
2. **Release B** flips `idForEncode` to `"argon2"` and adds `UserDetailsPasswordService`. Now
   upgrades begin, and any rollback target already understands the new format.

The same discipline applies during a rolling deploy even within one release: for a period,
old and new pods run simultaneously. If an old pod cannot verify what a new pod wrote, you get
intermittent login failures that are miserable to diagnose. Verify-before-write is the general
rule for any format migration.
</details>

### Q6. Design question — you need to store users' third-party API credentials (they connect their Salesforce account to your product). Walk me through it.

<details>
<summary>Show answer</summary>

First I would challenge the requirement, because the best storage is none. If Salesforce
supports OAuth2 — it does — I should be storing a refresh token issued to my application, not
the user's credentials. That is a revocable, scoped, auditable artefact, and the user can
disconnect from their side. Storing a username and password for a third party is something I
would only accept if there is genuinely no OAuth path.

Assuming I must store something reversible (a refresh token or an API key), the design:

**Encryption, not hashing.** This is the one case where reversibility is the requirement — I
need the actual value to call Salesforce. So: authenticated symmetric encryption, AES-256-GCM.
In Spring, `Encryptors.delux(password, salt)`. Never CBC, because without integrity an
attacker with database write access can tamper with ciphertext.

**Envelope encryption, not a single key.** Generate a unique data encryption key (DEK) per
credential, encrypt the credential with it, then encrypt the DEK with a key encryption key
(KEK) held in a KMS (AWS KMS, GCP KMS, Vault Transit). Store the ciphertext and the wrapped
DEK together; the KEK never leaves the KMS. This gives three things a single key cannot:
rotating the KEK is cheap (re-wrap the DEKs, no need to re-encrypt the data), the blast radius
of any one DEK is one credential, and the KMS gives you an audit log of every decrypt.

**Key material never in the application.** Not in `application.yml`, not in an environment
variable if I can avoid it, not in the image. The application authenticates to the KMS with a
workload identity and asks for a decrypt; it never holds the KEK.

**Decrypt as late and as briefly as possible.** Decrypt at the point of the outbound call, use
it, drop the reference. Do not cache plaintext credentials in a field, a static map, or a
Spring bean. For the truly paranoid, hold it in a `char[]` and zero it after use — though with
an immutable `String` and a copying GC this is more ritual than guarantee, and I would say so
rather than over-claim.

**Never in logs, never in responses.** A `toString()` on the entity, a debug log of the HTTP
request, or an accidental inclusion in an API response are how these leak in practice. I would
annotate the field to exclude it from serialisation and add a test that asserts the credential
never appears in a serialised representation.

**Audit every access.** Who decrypted which credential, when, and for what operation.

**Support rotation and revocation.** The user must be able to disconnect, which deletes the
ciphertext. And I need a documented procedure for "the KEK is compromised" that does not
require downtime.

**Counter-question: where does the encryption key come from at runtime, concretely?**

From the KMS, via workload identity — the pod's service account is granted `kms:Decrypt` on
the specific key, so there is no credential to steal at all; the cloud provider's metadata
service issues short-lived tokens.

If I am not on a cloud with a KMS, HashiCorp Vault's Transit engine gives the same shape:
the application authenticates with a Kubernetes service account token, and Vault performs the
encrypt/decrypt so the key never reaches the application.

The worst acceptable option is an environment variable injected from a secrets manager at pod
start. It is better than a config file because it is not in Git and not in the image, but the
key is then in process memory and in `/proc/<pid>/environ` for the process lifetime, and a
heap dump exposes it. I would document that as a known gap with a plan to move to KMS.

What I would reject outright is a key in `application.yml`, a key derived from something
predictable like the application name, or a key checked into Git "temporarily".

**Counter-question: the KMS is down. Your users cannot connect to Salesforce. How do you handle that?**

I would treat it as a deliberate availability trade-off and design for it explicitly rather
than discovering it during an incident.

Mitigations, in order: cache the *unwrapped DEK* in memory with a short TTL (minutes) so a
brief KMS blip does not break in-flight work — this caches a per-credential key, not the KEK,
so the blast radius stays small. Use the KMS's regional redundancy and a multi-region key if
the provider supports it. Make the failure mode explicit and graceful — a clear "integration
temporarily unavailable, retry shortly" rather than a 500 — and make sure the retry does not
hammer the KMS and slow its recovery.

What I would not do is keep a plaintext fallback copy "for emergencies". That defeats the
entire design, and it will be discovered by an auditor or an attacker before it is ever used
in an emergency.

I would also raise this at design time as a dependency: adding a KMS to the critical path
means your availability is now bounded by theirs, and that should be an explicit, accepted
decision rather than an accident.
</details>

---

## Quick Recall

```
THREE PRIMITIVES
  encoding   reversible by anyone, no key   -> ZERO security (Base64, URL, hex)
  encryption reversible with key            -> confidentiality (AES-GCM)
  hashing    one-way                        -> integrity, passwords

PASSWORDS
  hash, never encrypt   (no key to steal, no plaintext to recover)
  "can the system email me my password?" -> if yes, it is broken
  SHA-256 is WRONG here: too fast, GPUs do billions/sec
  use a KDF: Argon2id > scrypt > bcrypt > PBKDF2 >>> SHA/MD5
  target 250-1000 ms per verification on prod hardware
  bcrypt silently TRUNCATES input beyond 72 bytes

SALT vs PEPPER
  salt   per-password, random, stored in the clear, defeats rainbow tables + correlation
  pepper global secret, stored OUTSIDE the DB, defeats DB-only compromise
  bcrypt embeds the salt: $2a$10$<22 salt><31 digest>

SYMMETRIC vs ASYMMETRIC (the JWT decision)
  HS256 shared secret  -> anyone who can verify can FORGE -> monolith only
  RS256/ES256 keypair  -> resource servers verify, cannot forge -> microservices
  ES256 = 64-byte sig vs RS256 = 256-byte sig

JWT
  JWS = signed, NOT encrypted. Payload is world-readable on jwt.io.
  signature proves integrity + authenticity, NOT confidentiality, NOT validity
  never put in a payload what you would not put in a log
  attacks: alg:none, and RS256->HS256 confusion using the public key as the HMAC secret
  defence: PIN the algorithm server-side

TIMING
  String.equals leaks the matching prefix length
  use MessageDigest.isEqual for ANY attacker-supplied secret
  (API keys, HMAC tags, CSRF tokens, reset tokens)

RANDOMNESS
  new Random() / Math.random() -> predictable, forgeable tokens
  SecureRandom (or UUID.randomUUID) -> correct

MIGRATION (bcrypt -> argon2, zero downtime)
  DelegatingPasswordEncoder: {bcrypt} and {argon2} both verifiable
  idForEncode = argon2, upgradeEncoding() == true
  UserDetailsPasswordService re-hashes ON LOGIN (only moment the raw password exists)
  TWO releases: add-verify first, flip-encode second (so rollback still works)
  watch Argon2 memory: 16 MB x concurrency can OOM the container
```

---

**Previous:** [`02_M1_T2_Authentication_Authorization.md`](02_M1_T2_Authentication_Authorization.md) ·
**Next:** [`04_M1_T4_Servlet_Basics.md`](04_M1_T4_Servlet_Basics.md)
