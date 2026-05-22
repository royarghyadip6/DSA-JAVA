# Month 1 Goal

By the end of Month 1, you should have:

* Production-style Spring Boot microservices foundation
* API Gateway working
* Service Discovery working
* Config Server working
* Auth Service with JWT security
* RBAC (Role-Based Access Control)
* Dockerized setup
* PostgreSQL integration
* Swagger/OpenAPI docs
* Centralized logging basics
* Clean GitHub repositories

You should NOT build business features yet.

The entire first month is about creating a strong platform foundation.

---

# Which Microservice Should You Start First?

Start in this exact order:

| Order | Service          | Why                            |
| ----- | ---------------- | ------------------------------ |
| 1     | Config Server    | All services depend on configs |
| 2     | Eureka Discovery | Service registration           |
| 3     | API Gateway      | Single entry point             |
| 4     | Auth Service     | Security foundation            |
| 5     | User Service     | Used by future services        |

Do NOT start with:

* Product service
* Chat service
* Payment service
* AI service

Those depend on foundational architecture.

---

# Recommended Repository Structure

Option 1 (Recommended for Learning)

```text
petcare-platform/
│
├── config-server/
├── discovery-server/
├── api-gateway/
├── auth-service/
├── user-service/
├── common-library/
├── docker/
└── docs/
```

---

# Recommended Tech Stack for Month 1

| Area              | Technology            |
| ----------------- | --------------------- |
| Java              | Java 21               |
| Framework         | Spring Boot 3.x       |
| Build Tool        | Maven                 |
| DB                | PostgreSQL            |
| Container         | Docker                |
| Service Discovery | Eureka                |
| Gateway           | Spring Cloud Gateway  |
| Security          | Spring Security + JWT |
| API Docs          | Swagger/OpenAPI       |
| Logs              | ELK later             |
| Config            | Spring Cloud Config   |
| IDE               | IntelliJ IDEA         |
| Testing           | Postman               |

---

# Month 1 — Day-by-Day Plan

# WEEK 1 — Core Environment Setup

# Day 1 — Project Planning & Environment Setup

Tasks:

* Install Java 21
* Install IntelliJ
* Install Docker Desktop
* Install PostgreSQL
* Install Postman
* Install Git
* Create GitHub organization/repositories

Learn:

* Spring Boot architecture
* Microservices basics
* Docker basics

Deliverables:

* All tools installed
* GitHub repositories created

---

# Day 2 — Setup Parent Project

Create:

* Parent Maven project

Add:

* Common dependency management
* Shared plugin versions

Structure:

```text
petcare-platform-parent
├── pom.xml
```

Learn:

* Maven parent-child architecture
* Dependency management

Deliverables:

* Parent pom.xml

---

# Day 3 — Setup Config Server

Create:

* config-server

Add:

* Spring Cloud Config Server

Learn:

* Centralized configuration
* Externalized configs

Tasks:

* Enable config server
* Create Git-backed config repository
* Configure application.yml

Test:

* Access config via browser

Deliverables:

* Config server running on port 8888

---

# Day 4 — Setup Eureka Discovery Server

Create:

* discovery-server

Add:

* Eureka Server dependency

Learn:

* Service discovery
* Registration mechanism

Tasks:

* Enable Eureka server
* Configure registry

Test:

* Eureka dashboard accessible

Deliverables:

* Eureka server working

---

# Day 5 — Setup API Gateway

Create:

* api-gateway

Add:

* Spring Cloud Gateway

Learn:

* API Gateway pattern
* Routing
* Filters

Tasks:

* Register with Eureka
* Connect to Config Server
* Configure routes

Deliverables:

* Gateway running
* Route forwarding working

---

# Day 6 — Docker Basics

Learn:

* Docker architecture
* Images vs containers
* Docker networking

Tasks:

* Create Dockerfiles
* Dockerize:

    * config-server
    * discovery-server
    * api-gateway

Commands to practice:

* docker build
* docker run
* docker ps
* docker logs

Deliverables:

* All services running in Docker

---

# Day 7 — Docker Compose

Learn:

* Multi-container orchestration

Tasks:

* Create docker-compose.yml
* Add:

    * PostgreSQL
    * Config Server
    * Eureka
    * Gateway

Deliverables:

* Entire infra starts with one command

Command:

```bash
docker compose up
```

---

# WEEK 2 — Authentication Service

# Day 8 — Create Auth Service

Create:

* auth-service

Add:

* Spring Web
* Spring Security
* Spring Data JPA
* PostgreSQL Driver

Learn:

* Spring Security basics

Tasks:

* Connect to PostgreSQL
* Create user entity

Deliverables:

* Auth service starts successfully

---

# Day 9 — User Registration

Create:

* Register API

Endpoints:

```text
POST /auth/register
```

Features:

* Save user
* Hash password with BCrypt

Learn:

* Password encoding
* DTO pattern

Deliverables:

* User registration working

---

# Day 10 — Login API

Create:

* Login endpoint

Endpoints:

```text
POST /auth/login
```

Features:

* Validate credentials
* Generate JWT

Learn:

* AuthenticationManager
* JWT basics

Deliverables:

* JWT token generation

---

# Day 11 — JWT Security Filter

Create:

* JwtAuthenticationFilter

Learn:

* OncePerRequestFilter
* Security filter chain

Tasks:

* Validate JWT
* Extract claims
* Set authentication context

Deliverables:

* Protected endpoints working

---

# Day 12 — Role-Based Authorization

Roles:

* CUSTOMER
* DOCTOR
* ADMIN

Tasks:

* Implement RBAC
* Secure APIs using roles

Examples:

```java
@PreAuthorize(\"hasRole('ADMIN')\")
```

Learn:

* Method security
* Authorization flow

Deliverables:

* Role-based access working

---

# Day 13 — Refresh Tokens

Tasks:

* Create refresh token mechanism
* Store refresh tokens

Endpoints:

```text
POST /auth/refresh
```

Learn:

* Access token lifecycle

Deliverables:

* Refresh token flow

---

# Day 14 — Swagger/OpenAPI

Setup:

* SpringDoc OpenAPI

Tasks:

* Add Swagger UI
* Document APIs

Deliverables:

* Swagger accessible

Example:

```text
/swagger-ui.html
```

---

# WEEK 3 — Production Engineering Basics

# Day 15 — Centralized Exception Handling

Tasks:

* Create global exception handler
* Standardize error responses

Learn:

* @ControllerAdvice

Deliverables:

* Consistent API errors

---

# Day 16 — Validation

Tasks:

* Add request validations

Examples:

```java
@NotBlank
@Email
@Size
```

Learn:

* Jakarta Validation

Deliverables:

* Input validation working

---

# Day 17 — Logging

Tasks:

* Configure structured logging
* Add request logging

Learn:

* SLF4J
* Logback

Deliverables:

* Clean logs

---

# Day 18 — API Gateway Security

Tasks:

* JWT validation at gateway
* Route protection

Learn:

* Gateway filters

Deliverables:

* Gateway-secured APIs

---

# Day 19 — Config Server Integration

Tasks:

* Move configs to centralized repo
* Refresh configs dynamically

Learn:

* Spring Cloud Config Client

Deliverables:

* Externalized configs working

---

# Day 20 — Eureka Integration

Tasks:

* Register all services

Learn:

* Discovery client

Deliverables:

* All services visible in Eureka

---

# Day 21 — API Communication

Tasks:

* Setup OpenFeign

Learn:

* Inter-service communication

Deliverables:

* Service-to-service calls

---

# WEEK 4 — DevOps & Hardening

# Day 22 — PostgreSQL Optimization

Tasks:

* Connection pooling
* Indexes
* Migrations

Learn:

* HikariCP
* Flyway

Deliverables:

* Stable DB setup

---

# Day 23 — Docker Optimization

Tasks:

* Multi-stage Docker builds
* Reduce image size

Learn:

* Docker layering

Deliverables:

* Optimized Docker images

---

# Day 24 — Docker Compose Full Stack

Add:

* PostgreSQL
* Gateway
* Eureka
* Config Server
* Auth Service

Deliverables:

* Entire system booting together

---

# Day 25 — Health Checks

Tasks:

* Spring Boot Actuator

Endpoints:

```text
/actuator/health
```

Deliverables:

* Health monitoring

---

# Day 26 — Basic Monitoring

Setup:

* Prometheus basics

Learn:

* Metrics collection

Deliverables:

* Application metrics

---

# Day 27 — Security Hardening

Tasks:

* CORS
* Rate limiting basics
* Secure headers

Learn:

* OWASP basics

Deliverables:

* Hardened APIs

---

# Day 28 — Integration Testing

Tasks:

* Test all flows:

    * Register
    * Login
    * JWT validation
    * Gateway routing

Deliverables:

* Stable authentication flow

---

# Day 29 — Documentation

Create:

* README
* Architecture diagrams
* API docs

Deliverables:

* Professional documentation

---

# Day 30 — Final Review & Refactoring

Tasks:

* Clean code
* Refactor packages
* Improve naming
* Remove duplication

Deliverables:

* Production-style foundational system

---

# Final Architecture After Month 1

```text
                        Client Apps
                              |
                        API Gateway
                              |
         ---------------------------------------
         |                |                    |
     Auth Service     User Service      Future Services
                              |
                       PostgreSQL

Infrastructure:
- Eureka Discovery
- Config Server
- Docker Compose
- Swagger
- JWT Security
```

---

# Important Advice

Do NOT rush Kubernetes in Month 1.

First become comfortable with:

* Spring Security
* JWT
* Docker
* PostgreSQL
* Gateway routing
* Service discovery

Kubernetes comes later.

---

# Best Learning Strategy

For every feature:

1. Learn concept
2. Build minimally
3. Dockerize
4. Test via Postman
5. Push to GitHub
6. Refactor

That cycle will make you production-ready fast.
