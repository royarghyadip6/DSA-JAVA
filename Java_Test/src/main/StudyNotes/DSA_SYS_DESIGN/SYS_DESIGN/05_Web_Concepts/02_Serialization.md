# 2. Serialization — Data Exchange and Storage

## Simple idea

A Java `Order` object lives in memory. A network, a Redis cache, or a file cannot store that live object.

**Serialization** turns the object into bytes or text.

**Deserialization** turns those bytes back into an object.

```text
Order object  →  serialize  →  JSON / Protobuf bytes  →  network or Redis
JSON / bytes  →  deserialize  →  Order object in another service
```

You need it for APIs, caches, queues, databases, and any two processes that do not share memory.

---

## What the format changes

| Effect | Meaning |
|--------|---------|
| **Size** | Fat text uses more bandwidth and disk |
| **Speed** | Parsing text costs more CPU than reading a compact binary layout |
| **Compatibility** | Can an old consumer read a new message? |
| **Readability** | Can a human debug it in logs? |
| **Type safety** | Does a schema stop a string landing in an `int` field? |

A sloppy format can also lose meaning (dates, big integers, or binary blobs) if the format cannot represent them.

---

## The formats you must compare

| | JSON | XML | Protobuf | Avro |
|--|------|-----|----------|------|
| Human readable | Yes | Yes | No (binary) | No (binary) |
| Schema required | No | No (XSD optional) | Yes (`.proto`) | Yes |
| Size | Medium | Large | Small | Small |
| Speed | Moderate | Slower | Fast | Fast |
| Typical use | REST APIs, web | SOAP, old configs | gRPC, internal services | Kafka, Hadoop, data pipelines |

**BSON** is binary JSON, used by MongoDB. It is not the same as Protobuf.

---

## JSON

```json
{ "id": 42, "status": "PLACED" }
```

- easy to read and to use from any language
- no schema, so a typo can ship silently
- text is larger and slower to parse than binary
- default choice for public REST

---

## XML

Tag-based and verbose. Still seen in SOAP, some bank/government APIs, and config files.

It can describe rich structure and optional schemas (XSD). You pay with size and parsing cost. Do not pick it for a new public JSON-style API.

---

## Protocol Buffers

Google's binary format. A `.proto` file is the contract. Generated code reads and writes the bytes.

Pick Protobuf over JSON when:

- payload size and CPU matter (many internal calls)
- you want a strict schema
- you use **gRPC**

Skip it when partners must debug with curl, or the API is a public browser API. JSON wins there.

---

## Why gRPC uses Protobuf, not JSON

- smaller messages, less bandwidth
- faster encode/decode
- schema supports adding fields without breaking old clients (if you follow the compatibility rules)
- RPC needs a clear request and response type

---

## Avro

Avro is another binary format with a schema. Big-data systems like it because:

- the schema can travel with the data (or live in a registry)
- you can evolve fields without breaking old files
- Hadoop and Kafka ecosystems support it well
- files stay compact

The course line: **JSON for APIs, Protobuf for fast RPC, Avro for big data.**

---

## Where serialization shows up

| Place | Usual format |
|-------|----------------|
| REST | JSON |
| gRPC | Protobuf |
| SOAP | XML |
| Redis / Memcached | JSON, or Protobuf/Avro if you care about size. Values are bytes either way |
| MongoDB | BSON |
| Kafka / data lake | Avro, Protobuf, or JSON |

Redis does not force JSON. JSON is common because it is easy. A smaller format means less memory and a faster network read. You can also compress the bytes. The cache key stays a string. The value is the serialized payload.

---

## BSON vs JSON

MongoDB stores **BSON** because:

- it is binary, so scans and storage are tighter than raw JSON text
- it has types JSON text does not: date, 32/64-bit int, binary
- those types make indexes and queries more precise

You still think in JSON when you query. The database stores BSON.

---

## CPU, memory, and API speed

- JSON/XML: more bytes on the wire, more CPU to parse quotes and braces
- Protobuf/Avro: fewer bytes, less CPU, but you must manage schemas
- a huge object graph serialized naively can blow memory on both sides

For a public API, readability often beats a few milliseconds. For 10,000 internal calls per order, binary formats pay off.

---

## Schema evolution (compatibility)

- **JSON:** you can add a field, but nothing checks it. Old clients ignore unknown fields only if you wrote them that way
- **XML:** schemas exist, but documents are bulky
- **Protobuf / Avro:** schemas are the point. Add optional fields. Do not reuse field numbers or change a field's meaning

Interview line: binary formats are efficient **and** they force you to version the contract.

---

## Security

Deserialization is a trust boundary. Bytes from the network are not "just data" if the decoder can build arbitrary objects.

| Risk | What happens |
|------|----------------|
| **Unsafe deserialization** | A malicious payload becomes an object that runs code (classic Java gadget / RCE) |
| **DoS** | A tiny payload expands into a huge object and exhausts memory |
| **Tampering** | An attacker edits JSON or bytes on the wire if there is no TLS or signature |
| **Injection** | Deserialized strings are concatenated into SQL or commands |

Safer habits:

- do not deserialize untrusted Java `ObjectInputStream` data
- validate against a schema before use
- authenticate and use TLS
- reject unexpected types and huge payloads
- prefer plain JSON/Protobuf data objects over "any object" decoders

---

## Interview lines

- Serialize to store or send. Deserialize to rebuild the object.
- JSON = readable REST. Protobuf = small, typed, gRPC. Avro = schema evolution in data pipelines. BSON = MongoDB's binary JSON.
- Smaller payloads save bandwidth and CPU. Schemas save you from silent breakage.
- Never deserialize untrusted language-native object streams.

---

## Interview Q&A

### 1. What is serialization, and why is it needed?

<details>
<summary>Show Answer</summary>

**Answer:**

It converts an in-memory object into a form you can store or send. The other side deserializes it.

You need it so APIs, caches, queues, files, and different languages can exchange structured data.

</details>

### 2. How does it affect exchange and storage?

<details>
<summary>Show Answer</summary>

**Answer:**

The format decides size (bandwidth and disk), encode/decode speed, whether humans can read it, and whether types and schemas stay compatible. A poor mapping can drop precision or types.

</details>

### 3. JSON vs XML vs Protobuf vs Avro?

<details>
<summary>Show Answer</summary>

**Answer:**

| | JSON | XML | Protobuf | Avro |
|--|------|-----|----------|------|
| Readable | Yes | Yes | No | No |
| Schema | Optional | Optional | Required | Required |
| Size | Medium | Large | Small | Small |
| Use | Web APIs | Legacy / SOAP / config | gRPC | Kafka, Hadoop, big data |

</details>

### 4. When choose Protobuf over JSON?

<details>
<summary>Show Answer</summary>

**Answer:**

When messages are frequent or large, bandwidth and CPU matter, you want a strict schema, or the call is gRPC between services.

Keep JSON when humans debug the API or browsers and partners expect it.

</details>

### 5. How does serialization affect API performance?

<details>
<summary>Show Answer</summary>

**Answer:**

Bigger text means more network time and more CPU to parse. Binary formats shrink payloads and parsing cost, so the API spends less time on encoding and less money on bandwidth.

</details>

### 6. Why does gRPC use Protobuf instead of JSON?

<details>
<summary>Show Answer</summary>

**Answer:**

Protobuf is compact, fast, and schema-backed, with rules for compatible changes. gRPC needs a clear typed request and response. JSON is larger and loosely typed.

</details>

### 7. How does serialization affect Redis caching?

<details>
<summary>Show Answer</summary>

**Answer:**

Redis stores bytes. JSON is easy to inspect. Protobuf or Avro uses less memory and can be faster to read back. Compression shrinks values further. The format does not change the key design. It changes value size and CPU.

</details>

### 8. Readability vs efficiency vs compatibility?

<details>
<summary>Show Answer</summary>

**Answer:**

JSON and XML are readable and flexible, but larger and slower. Protobuf and Avro are compact and fast, and their schemas make evolution explicit. Pick readable at the public edge and efficient on internal hot paths.

</details>

### 9. Impact on CPU and memory?

<details>
<summary>Show Answer</summary>

**Answer:**

Text parsers burn more CPU. Binary layouts use less CPU and RAM for the same data. Serializing a giant object graph can still spike memory on both sides.

</details>

### 10. Why is Avro useful in big data?

<details>
<summary>Show Answer</summary>

**Answer:**

It is compact, supports schema evolution, and fits Kafka and Hadoop. You can change the schema in a compatible way and still read old data.

</details>

### 11. Why does MongoDB use BSON instead of JSON?

<details>
<summary>Show Answer</summary>

**Answer:**

BSON is binary, so storage and scans are tighter. It also has types JSON text lacks, such as date, sized integers, and raw bytes, which helps indexing and queries.

</details>

### 12. What security risks come with serialization?

<details>
<summary>Show Answer</summary>

**Answer:**

Unsafe deserialization can run attacker-controlled objects. Huge payloads can exhaust memory. Unencrypted or unsigned data can be changed in transit. Unvalidated fields can become injection.

</details>

### 13. How can bad deserialization become a vulnerability?

<details>
<summary>Show Answer</summary>

**Answer:**

- remote code execution from malicious objects (especially native Java serialization)
- denial of service from expansion bombs
- data tampering

Mitigate by refusing untrusted native object streams, validating a strict schema, capping size, and using TLS plus authentication.

</details>

**Q&A recap:** JSON at the edge, Protobuf for gRPC, Avro for pipelines, BSON inside MongoDB, and never trust hostile bytes.

## Interview Q&A — 6 to 8 years

### S1. A "simple JSON change" took the mobile app down. What rules do you follow?

<details>
<summary>Show Answer</summary>

**Answer:**

Old clients ignore new fields, so I may add a field. I may not change a type, rename a field, or change units (`seconds` versus `milliseconds`) in place. I send both old and new for a while. Dates are an explicit format (ISO-8601 or epoch), not a locale string. I test the oldest supported app against the new response.

**Corner cases:** Integers above 2^53 lose precision in JavaScript. I send large ids as strings. A missing field and a null field mean different things, and one language's decoder treats them the same. Enum defaulting turns an unknown status into `0` and looks like a real "pending." Duplicate JSON keys: parsers disagree on which value wins. I reject duplicates. HTML in a string field is stored and later rendered as script. I encode on output.

</details>

### S2. Why do you refuse to deserialize a type the client names?

<details>
<summary>Show Answer</summary>

**Answer:**

If the payload says the class name, an attacker can point it at a class that runs code on load. I allow a fixed list of types I wrote, or I use a format without polymorphic class names (plain JSON fields, protobuf). I cap bytes and nesting depth so a small request cannot expand into gigabytes of objects.

**Corner cases:** A library default that allows any Java type on the classpath. Nested lists that blow the stack. A gzip bomb: tiny body, huge inflated size. I limit both sizes. Avro or protobuf with a schema registry avoids "whatever class," and it still needs a compatibility rule. Logging the raw body to debug a parse error can store passwords and card numbers.

</details>

### S3. Protobuf is smaller and still the wrong default at the public edge. Why?

<details>
<summary>Show Answer</summary>

**Answer:**

Browsers, curl, and third-party developers speak JSON without a generated stub. Protobuf shines between my own services, where both sides share a schema and I care about CPU and bytes. Public APIs change slowly and many clients are not in my repo. I can still use protobuf inside and JSON at the door. The translation is one place, with a deadline.

**Corner cases:** JSON mapping of `int64` and `bytes` surprises clients. A proto3 default of `0` hides "field was absent," so I cannot tell a real zero from a missing balance. Unknown fields must be preserved if I proxy the message onward, or a rolling deploy drops them. Schema evolution that reserves field numbers is easy to violate under time pressure. I treat the registry as production, not as documentation.

</details>

### S4. How do you design a pipeline schema (Avro or similar) so a replay still works next year?

<details>
<summary>Show Answer</summary>

**Answer:**

The schema id travels with each record. Old files stay readable. New consumers use the registry to decode old ids. I add optional fields with defaults. I do not rewrite history in place. A replay uses the same idempotency keys as live traffic so side effects (emails, charges) do not repeat.

**Corner cases:** A consumer compiled against a newer required field fails the whole backlog. A change of logical type (date as int versus string) passes the registry and fails the job at midnight. Two teams publish the same event name with different schemas. I namespace names. Compaction or a column format that cannot handle a schema jump needs a new table, not an in-place break.

</details>

**Senior recap:** add fields, never retcon types, do not deserialize a client-chosen class, JSON at the edge, schema ids on the log so replay still decodes.

Next: [03_CORS.md](03_CORS.md)
