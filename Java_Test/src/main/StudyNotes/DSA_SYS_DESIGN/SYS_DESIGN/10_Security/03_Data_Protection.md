# 3. Data Protection and Secure Communication

## Simple idea

Data is in two places: sitting on a disk, or moving on a network. Attackers want both.

```text
On disk     →  encryption at rest
On the wire →  encryption in transit (TLS)
Passwords   →  a slow hash, not encryption
```

Laws such as GDPR and HIPAA, and user trust, are why this is not optional. A backup of plaintext is still plaintext. Encrypt the backup too ([backups](../09_Reliability_Availability_DR/03_Backup_and_Recovery.md)).

---

## Encryption

**Encryption** turns plaintext into ciphertext with a **key**. The same key, or the matching private key, turns it back.

| | Symmetric | Asymmetric |
|--|-----------|------------|
| Keys | One shared secret | Public key and private key |
| Speed | Fast. AES is the usual choice, and CPUs accelerate it | Slow. RSA and elliptic curves use heavy math and large keys |
| Use | The actual data: disks, files, the TLS session | Exchanging a secret, signing, proving identity |
| Example | AES-256 | RSA, ECDSA |

You do not encrypt a video with RSA. In TLS the slow algorithm runs only long enough to agree on a symmetric session key. The bytes of the page then use AES.

```text
Client                          Server
  │  "hello, here are my ciphers"  │
  │  ← certificate (public key)    │
  │  agree on a session key        │
  │  AES from here on              │
```

---

## At rest and in transit

| | At rest | In transit (in motion) |
|--|---------|------------------------|
| Where | Disks, databases, buckets, backups, logs | Browser to API, service to service |
| Threat | Stolen disk, open bucket, insider query | Eavesdropper, MITM |
| Tools | Disk encryption (LUKS, BitLocker), database TDE, S3 with KMS | TLS, a VPN or IPsec on a private link, mTLS between services |

**Envelope encryption** is the cloud pattern. A data key encrypts the file. KMS encrypts that data key. You can rotate the master key without rewriting every file yourself. AWS KMS, GCP KMS, and Azure Key Vault do this job.

Encryption does not replace access control. If every app role can ask KMS to decrypt, the lock is decorative.

A token does not encrypt the HTTP body. TLS does. A JWT signature only protects the token.

---

## Hashing and passwords

A **hash** is one-way. The same input always gives the same fixed-length output. A tiny change in the input changes the hash completely. You cannot turn the hash back into the password.

| | Encryption | Hash |
|--|------------|------|
| Reversible | Yes, with the key | No |
| Purpose | Hide the data | Detect change, or store a password |
| Key | Required | Not for a plain hash |
| Output | Grows with the message | Fixed size |

Use a hash to see if a file changed. **Do not store passwords with a fast hash.** SHA-256 and MD5 are built to be fast, so an attacker tries billions of guesses per second. A salt stops precomputed rainbow tables, and it does not make SHA-256 a good password hash.

Store passwords with a **slow** password hash: **bcrypt, scrypt, or Argon2**, each with a unique salt. Spring's `BCryptPasswordEncoder` is the usual Java choice. You never decrypt a password. On login you hash the attempt the same way and compare.

---

## PKI and HTTPS

**HTTPS** is HTTP over TLS. It gives you:

- **confidentiality** — the body is encrypted
- **integrity** — tampering breaks the MAC
- **authenticity** — the certificate says which name you reached

**PKI** is the system of certificates and authorities that makes the name check possible.

```text
Your browser trusts a small set of root CAs
    → a root signs an intermediate CA
        → the intermediate signs example.com
Browser trusts example.com because it trusts the root
```

The certificate binds a public key to a name. It does not mean the company is honest. It means you are talking to whoever the CA checked for that domain.

If a key leaks, the certificate must be **revoked** (a revocation list, or an OCSP check) or left to expire quickly. Short-lived certificates from Let's Encrypt, renewed automatically, are the usual scale answer. The handshake details are in the foundations note: turn off old TLS, renew before expiry, and do not pin certificates unless you can ship an update when they rotate.

**Mutual TLS (mTLS)** means the server also checks a client certificate. That is how services prove identity to each other, not only how a browser proves the server.

---

## APIs

- HTTPS only
- OAuth 2.0 or a signed JWT, checked on every call
- Rate limits
- mTLS for service-to-service calls that must not rely on the network
- An allowlist only for a known partner. IP lists break on mobile networks and NAT. They are not a user login

---

## Interview lines

- Encrypt data on disk and on the wire. Hash passwords with bcrypt or Argon2 and a salt.
- Symmetric is fast and is what carries bulk data. Asymmetric is for the handshake and for signatures.
- PKI is a chain from a root CA to the site certificate. Trust is "the CA signed this name", plus revocation.
- TLS protects the channel. A JWT does not encrypt the request body.

---

## Interview Q&A

### 1. Hashing vs encryption?

<details>
<summary>Show Answer</summary>

**Answer:**

Encryption is reversible with a key. It hides data. Symmetric (AES) uses one key. Asymmetric (RSA) uses a public and a private key. Hashing is one-way and fixed length. It checks integrity or stores a password. Same input, same hash. A one-bit change produces a different hash. For passwords, a fast hash such as raw SHA-256 is the wrong tool even with a salt. Use bcrypt, scrypt, or Argon2.

</details>

### 2. Why is asymmetric encryption slower?

<details>
<summary>Show Answer</summary>

**Answer:**

It uses large keys and expensive math. AES uses a short key and bitwise operations, and hardware accelerates it, so it can encrypt bulk data. TLS uses the slow algorithm only to agree a session key, then switches to AES. Use asymmetric crypto for exchange and signatures, not for a database dump.

</details>

### 3. How does PKI create trust?

<details>
<summary>Show Answer</summary>

**Answer:**

A certificate authority signs a certificate that binds a public key to a name. Browsers trust a few roots. Those roots sign intermediates, which sign the site. If the chain is valid, unexpired, and for the name you asked for, the browser treats the server as that name. Revocation lists or OCSP mark leaked certificates. That proves identity of the endpoint. It does not by itself encrypt anything until TLS uses the key. Signed documents also give integrity and non-repudiation: the signer cannot credibly deny the signature.

</details>

### 4. Data at rest vs data in motion?

<details>
<summary>Show Answer</summary>

**Answer:**

At rest means stored: disks, databases, buckets, backups. Threats are theft and unauthorized reads. Use disk or database encryption, KMS, and IAM. In motion means on the network. Threats are eavesdropping and MITM. Use TLS, and mTLS between services. Encrypt both, and still restrict who can decrypt. A bearer token does not protect the body. TLS does.

</details>

**Q&A recap:** hide data with encryption, store passwords with a slow salted hash, and let certificates prove the name on the other end of TLS.

## Interview Q&A — 6 to 8 years

### S1. TLS is on. Where is the data still readable?

<details>
<summary>Show Answer</summary>

**Answer:**

On the server after decryption, in the database, in logs, in a replica, in a backup, in a queue payload, and in a crash dump. TLS protects the path. I encrypt specific fields at rest when a database leak should not reveal them (national id, some tokens), and I keep the key in a KMS the database host does not store next to the disk. I do not log bodies of login or payment calls.

**Corner cases:** Encryption at rest with the key on the same disk, which does nothing for a stolen snapshot that includes the key. A CDN or a service mesh that terminates TLS and logs the URL, including a secret in the query. Backups of the database taken before I turned encryption on, still sitting in the old bucket. A replica in another account I forgot. Application-level encryption that I cannot search or index, so someone copies the plaintext into a "search" table and undoes it. Key administrators who can decrypt and also deploy. I split those roles for the most sensitive keys.

</details>

### S2. How do you store a password, and what do you refuse to build?

<details>
<summary>Show Answer</summary>

**Answer:**

A slow salted hash made for passwords (Argon2, bcrypt, or scrypt), unique salt per user, parameters that I can raise over time. I never store the password, and I never use a fast hash (plain SHA-256) as if it were a password hash. I do not encrypt passwords so I can decrypt them. Reset tokens are random, single-use, and short-lived. I can tell the user the password was wrong without saying whether the email exists, if product agrees, to avoid account enumeration.

**Corner cases:** A salt that is global, so identical passwords share a hash. A pepper in the code repo. Truncating long passwords in a way that surprises users, or hashing without a length limit so one request can burn CPU. Logging the reset link. A migration that leaves old fast hashes in place with no rehash on next login. Comparing hashes with a short-circuit that leaks timing. I use a constant-time compare. MFA backup codes stored in plaintext.

</details>

### S3. How do you rotate a key or a certificate without an outage?

<details>
<summary>Show Answer</summary>

**Answer:**

I introduce the new key while the old one still verifies or decrypts. I write new data with the new key. I keep the old key for read until the data is rewritten or the oldest token has expired. Then I remove the old key. Certificates overlap: the new cert is served, clients that pinned the old one need a planned pin update, and I do not pin so tightly that I cannot rotate. I rehearse on a staging account.

**Corner cases:** A hard cutover at midnight that invalidates every session and every mobile app that baked in the old pin. A KMS key scheduled for deletion faster than the data encrypted with it. Two regions that rotate on different days and cannot read each other's rows. A load balancer that has the new cert and an old app node that does not, so some users fail. A secret in an environment variable that a rolling deploy only half-updates. I version the secret. Rotation that forgets the backup encryption key, so last month's backup is unreadable. I rotate that key with the same overlap rule.

</details>

### S4. What is the corner case between hashing, encryption, and signing?

<details>
<summary>Show Answer</summary>

**Answer:**

Hashing is one-way. I use it for passwords and for a checksum. Encryption is reversible if you have the key. I use it so I can read the data later. Signing shows who produced a token and that it was not edited. A JWT signature is not encryption. Anyone can read the payload. I do not put secrets inside a JWT. I do not "hash the database so it is encrypted." I do not use encryption when I only needed a fingerprint.

**Corner cases:** A JWT in a log that contains an email and a national id, readable without the key. An encrypted field I cannot rotate because I lost the key version. A checksum with no signature, so an attacker who can edit the file edits the checksum too. Signing with a key the client also has, which means anyone can sign. The private key stays on the server or in the KMS. Using the same key for encryption and for signing. I split purposes.

</details>

**Senior recap:** TLS is the path, field encryption and log hygiene cover the rest, password hashes are slow and salted, and key rotation needs an overlap.

Next: [04_Network_and_Infrastructure_Security.md](04_Network_and_Infrastructure_Security.md)
