# 🌐 REST API – In-Depth Guide (Interview Preparation)

# 📑 Table of Contents

* [📌 Overview](#-overview)
* [🧠 What is REST?](#-what-is-rest)
* [⚙️ REST Principles (Constraints)](#-rest-principles-constraints)
* [🌍 HTTP Methods (Deep Dive)](#-http-methods-deep-dive)
* [📦 HTTP Status Codes (Detailed)](#-http-status-codes-detailed)
* [🔗 URI Design Best Practices](#-uri-design-best-practices)
* [📥 Request & Response Structure](#-request--response-structure)
* [🔄 Idempotency (Very Important)](#-idempotency-very-important)
* [🔐 Authentication & Authorization](#-authentication--authorization)
* [📊 Versioning Strategies](#-versioning-strategies)
* [⚡ Caching (Performance Optimization)](#-caching-performance-optimization)
* [🚨 Error Handling (Best Practices)](#-error-handling-best-practices)
* [🔍 REST vs SOAP](#-rest-vs-soap)
* [🧠 HATEOAS (Advanced)](#-hateoas-advanced)
* [⚙️ REST in Spring Boot](#-rest-in-spring-boot)
* [🎯 Interview Questions (With Answers)](#-interview-questions-with-answers)
* [🚀 Summary](#-summary)

---

# 📌 Overview

REST API (Representational State Transfer) is an **architectural style** used to design scalable web services.

👉 In simple terms:
* A REST API allows a **client (frontend/mobile)** to communicate with a **server (backend)** using HTTP.
* Uses HTTP methods like GET, POST, PUT, PATCH, and DELETE.
* Client sends requests to server endpoints (URLs).
* Server returns responses such as JSON, XML, HTML, or images.
* Maps HTTP methods to CRUD operations (Create, Read, Update, Delete).
 
![RestAPI_WorkFlow.png](Images/RestAPI_WorkFlow.png)

> **Note**: REST is an architectural style that defines how APIs should be designed, whereas HTTP is the protocol used to transfer data between client and server. They work together, but they are not the same.


---

# 🧠 What is REST?

REST is NOT a protocol like HTTP.
It is a **set of design rules (constraints)**.

👉 Core idea:

* Everything is treated as a **resource**
* Each resource has a **unique URI**

Example:

```text id="e9k3lm"
/users        → collection
/users/1      → specific resource
```

👉 Representation:

* Data is usually sent in **JSON format**

---

# ⚙️ REST Principles (Constraints)

## 1. Stateless (Most Important)

👉 Server does NOT remember previous requests.

Example:

* Request 1: Login
* Request 2: Get user data

👉 Server should not rely on request 1.

✔️ Why important?

* Easier scaling
* No session management overhead

---

## 2. Client-Server Separation

👉 Frontend and backend are independent.

✔️ Benefits:

* Frontend can change without backend changes
* Better scalability

---

## 3. Cacheable

👉 Responses should indicate if they can be cached.

✔️ Benefits:

* Faster performance
* Reduced server load

---

## 4. Uniform Interface

👉 Standard way of interacting:

* HTTP methods
* URIs
* Status codes

---

## 5. Layered System

👉 Multiple layers can exist:

* API Gateway
* Load Balancer

Client doesn’t know actual backend.

---

# 🌍 HTTP Methods (Deep Dive)

## 🔹 GET

👉 Retrieve data

✔️ Characteristics:

* Safe (no data change)
* Idempotent

Example:

```http id="e1k8sd"
GET /users/1
```

---

## 🔹 POST

👉 Create new resource

✔️ Not idempotent
Multiple calls → multiple records

---

## 🔹 PUT

👉 Replace entire resource

Example:

```http id="f9k3ls"
PUT /users/1
```

✔️ Idempotent
Same request → same result

---

## 🔹 PATCH

👉 Partial update

Example:

```http id="v7k2mz"
PATCH /users/1
{
  "name": "New Name"
}
```

---

## 🔹 DELETE

👉 Remove resource

✔️ Idempotent
Deleting again won’t change result

---

## Differences Between PUT and PATCH

> Both PATCH and PUT are used to update resources on the server, but they differ in how they handle the update process:

| PUT	                                       | PATCH                                 |
|--------------------------------------------|---------------------------------------|
| Replaces the entire resource	              | Updates only specified fields         |
| Must send full data	                       | Only sends changes                    |
| Idempotent	                                | Not always idempotent                 |
| Example: Updating a user’s entire profile	 | Example: Changing just a user’s email |

---

# 📦 HTTP Status Codes (Detailed)

> When you visit a website or make a request online, your browser communicates with a server using HTTP (Hypertext Transfer Protocol). The server responds with a three-digit HTTP status code that indicates whether the request was successful or if something went wrong.

## What are HTTP Status Codes?

The HTTP status code is a response made by the server to the client's request. These are three-digit codes. [There are over 60 status codes](https://www.geeksforgeeks.org/computer-networks/what-are-http-status-codes/), but they fall into five main categories:

* 1xx - Informational Response (These status codes are all about the information received by the server when a request is made).
* 2xx - Success (This status code depicts that the request made has been fulfilled by the server and the expected response has been achieved).
* 3xx - Redirection (The requested URL is redirected elsewhere).
* 4xx - Client Errors (This indicates that the page is not found).
* 5xx - Server Errors (A request made by the client but the server fails to complete the request).


## ✅ 2xx Success

* 200 OK → Success
* 201 Created → Resource created
* 204 No Content → Success but no body

---

## ⚠️ 4xx Client Errors

* 400 → Bad request (invalid input)
* 401 → Not authenticated
* 403 → Forbidden (no permission)
* 404 → Resource not found

---

## ❌ 5xx Server Errors

* 500 → Internal error
* 503 → Service unavailable

---

# 🔗 URI Design Best Practices

## ✅ Good Design

```text id="r8k2pw"
/users
/users/1
/users/1/orders
```

✔️ Use nouns
✔️ Use hierarchy

---

## ❌ Bad Design

```text id="t7m3kl"
/getUser
/createOrder
```

👉 REST is about **resources, not actions**

---

# 📥 Request & Response Structure

## Request Example

```http id="z9k3lm"
POST /users
Content-Type: application/json

{
  "name": "John",
  "email": "john@mail.com"
}
```

---

## Response Example

```json id="y8k2pl"
{
  "id": 1,
  "name": "John",
  "email": "john@mail.com"
}
```

---

## Headers (Important)

* Content-Type
* Authorization
* Accept

---

# 🔄 Idempotency (Very Important)

👉 Definition:
Making the same request multiple times produces the same result.

## Examples:

* GET → Always same result
* PUT → Replace same data
* DELETE → Already deleted → no change

## Not Idempotent:

* POST → Creates new resource each time

---

# 🔐 Authentication & Authorization

## Authentication (Who are you?)

* Username/password
* Token

## Authorization (What can you do?)

* Role-based access

---

## Common Methods:

### 🔹 JWT

* Stateless
* Token-based

### 🔹 OAuth2

* Used for third-party login (Google, Facebook)

---

# 📊 Versioning Strategies

## URI Versioning

```text id="q3k8ls"
/v1/users
/v2/users
```

✔️ Easy to understand

---

## Header Versioning

```http id="x8m2pz"
Accept: application/vnd.v1+json
```

✔️ Cleaner but harder to manage

---

# ⚡ Caching (Performance Optimization)

## Why caching?

* Reduce DB calls
* Improve speed

## Headers:

```http id="n3k9dl"
Cache-Control: max-age=3600
ETag: "abc123"
```

👉 ETag helps detect changes

---

# 🚨 Error Handling (Best Practices)

## Standard Error Response

```json id="p9k3ls"
{
  "timestamp": "2026-04-11",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid input"
}
```

✔️ Always consistent format
✔️ Never expose internal errors

---

# 🔍 REST vs SOAP

| Feature     | REST          | SOAP     |
|-------------|---------------|----------|
| Style       | Architectural | Protocol |
| Data        | JSON          | XML      |
| Speed       | Fast          | Slow     |
| Flexibility | High          | Low      |

---

# 🧠 HATEOAS (Advanced)

👉 API provides navigation links

```json id="z2k8lm"
{
  "id": 1,
  "_links": {
    "self": "/users/1",
    "orders": "/users/1/orders"
  }
}
```

👉 Helps dynamic API navigation

---

# ⚙️ REST in Spring Boot

```java id="p8k3ls"
@RestController
@RequestMapping("/users")
public class UserController {

    @GetMapping("/{id}")
    public String getUser(@PathVariable int id) {
        return "User " + id;
    }

    @PostMapping
    public String createUser() {
        return "User created";
    }
}
```

👉 Maps HTTP requests to Java methods

---

# 🎯 Interview Questions (With Answers)

## Q1: What is REST?

👉 Architectural style using HTTP and stateless communication.

---

## Q2: PUT vs PATCH?

👉 PUT replaces full resource
👉 PATCH updates partial fields

---

## Q3: What is idempotency?

👉 Same request → same result

---

## Q4: How to secure REST API?

👉 JWT, OAuth2, HTTPS

---

## Q5: How to design scalable REST API?

👉

* Use caching
* Use load balancer
* Use stateless design

---