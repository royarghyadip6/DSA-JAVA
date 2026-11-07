# 05 — Web Concepts in System Design

This folder matches the course section **Web Concepts in System Design**.

Notes are based on the Section 5 slides and the three interview PDFs. Each topic file has **theory first**, then **interview Q&A**.

---

## Why this section exists

Browsers and APIs look simple. Under them, three choices decide whether a product is safe and scalable:

```text
Who is this user?          →  Sessions / tokens
How do we send the data?   →  Serialization (JSON, Protobuf, …)
May this website call that API?  →  CORS
```

A wrong choice shows up as logged-out users, huge payloads, or a browser that blocks the API.

**Words in this folder**

| Word | Meaning |
|------|---------|
| **Stateless** | The server forgets you after the response. The next request must carry proof of who you are |
| **Cookie** | A small note the browser stores and sends back automatically |
| **Session** | The server's memory of a logged-in person, usually found with an id from the cookie |
| **JWT** | A signed pass the client carries. The server can check it without looking up a session |
| **JSON** | Text that looks like `{ "name": "Ada" }`. The usual way APIs send data |
| **CORS** | The rule a browser uses before it lets a web page call an API on another site |

---

## Read in this order

| # | File | What is inside |
|---|------|----------------|
| 1 | [01_Web_Sessions.md](01_Web_Sessions.md) | HTTP statelessness, cookies, sessions, JWT, CSRF, scaling + Q&A |
| 2 | [02_Serialization.md](02_Serialization.md) | JSON, XML, Protobuf, Avro, BSON, performance, security + Q&A |
| 3 | [03_CORS.md](03_CORS.md) | Same-origin policy, preflight, headers, proxies + Q&A |

---

## Section cheat sheet

| Idea | One line |
|------|----------|
| HTTP is stateless | The server forgets you after each response |
| Cookie + session | Browser keeps an ID; server keeps the real data |
| JWT | The token itself carries the claims |
| JSON | Easy to read; default for public REST |
| Protobuf / Avro | Smaller and faster; need a schema |
| Same-origin policy | Browser blocks a page from freely reading another site's data |
| CORS | The **API server** says which websites may call it |

---

## What is next

**Scalability in System Design**: [06_Scalability](../06_Scalability/README.md)

---

## Source PDFs used

- `System Design - updated - Section 5.pdf`
- `Web Sessions: Important Interview Questions.pdf`
- `Serialization: Detailed Interview Answers.pdf`
- `CORS & Web Security – Interview Questions & Answers.pdf`
