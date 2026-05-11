````md id="v9k2xp"
# Day 2 — Dockerfile + Spring Boot Containerization

# 1. Introduction

Day 2 focuses on:
- Creating Docker images
- Writing Dockerfiles
- Running Spring Boot applications inside containers
- Understanding Docker layers
- Passing environment variables
- Debugging containers

Goal:
```text id="q1m7tz"
Containerize a Spring Boot application successfully
````

---

# 2. What is a Dockerfile?

A Dockerfile is:

```text id="n4x8pv"
A text file containing instructions to build Docker images
```

Docker executes instructions line-by-line.

Workflow:

```text id="z2m5qw"
Dockerfile
    ↓
docker build
    ↓
Docker Image
    ↓
docker run
    ↓
Container
```

---

# 3. Dockerfile Deep Dive

---

# 3.1 FROM

## Purpose

Defines:

```text id="u6q1vx"
Base image
```

Every Docker image must start with FROM.

Example:

```dockerfile id="m8t3wy"
FROM eclipse-temurin:21-jre
```

This provides:

* Linux filesystem
* Java 21 runtime

---

## Best Practice

Always use fixed versions.

BAD:

```dockerfile id="p5v7xz"
FROM openjdk:latest
```

GOOD:

```dockerfile id="k2m9tw"
FROM eclipse-temurin:21-jre
```

---

# 3.2 WORKDIR

## Purpose

Sets:

```text id="a7q4vp"
Working directory inside container
```

Example:

```dockerfile id="r9x1mz"
WORKDIR /app
```

All following commands execute from:

```text id="t3v8qy"
/app
```

---

# 3.3 COPY

## Purpose

Copies files:

```text id="y5m2wx"
From host machine → Docker image
```

Example:

```dockerfile id="j7q9tv"
COPY target/app.jar app.jar
```

Meaning:

```text id="k4v1xp"
target/app.jar
→
/app/app.jar
```

inside container.

---

# 3.4 RUN

## Purpose

Executes commands:

```text id="m2q8vz"
During image build
```

Example:

```dockerfile id="u5x7qw"
RUN mkdir logs
```

Used for:

* Installing packages
* Creating folders
* Setting permissions

---

## Best Practice

Combine commands:

BAD:

```dockerfile id="n1v6py"
RUN apt update
RUN apt install curl
```

GOOD:

```dockerfile id="w8q2mz"
RUN apt update && apt install -y curl
```

Reduces image layers.

---

# 3.5 CMD

## Purpose

Defines:

```text id="p3x9tv"
Default startup command
```

Example:

```dockerfile id="k6m1qw"
CMD ["java","-jar","app.jar"]
```

Can be overridden at runtime.

---

# 3.6 ENTRYPOINT

## Purpose

Defines:

```text id="t7v4xp"
Main executable of container
```

Example:

```dockerfile id="m5q8wy"
ENTRYPOINT ["java","-jar","app.jar"]
```

Container always starts application.

---

## ENTRYPOINT vs CMD

| ENTRYPOINT       | CMD               |
| ---------------- | ----------------- |
| Fixed executable | Default arguments |
| Hard to override | Easy to override  |
| Main process     | Optional defaults |

---

# 3.7 EXPOSE

## Purpose

Documents:

```text id="r1m7qz"
Application port inside container
```

Example:

```dockerfile id="x8v3pw"
EXPOSE 8080
```

---

## Important

EXPOSE:

```text id="k2t9vx"
Does NOT publish port externally
```

To expose externally:

```bash id="u4q6mz"
docker run -p 8080:8080 app
```

---

# 4. Complete Spring Boot Dockerfile

```dockerfile id="d9m2wx"
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY target/app.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]
```

---

# 5. Build Custom Docker Image

---

# 5.1 Build Spring Boot JAR

Run:

```bash id="c5q8tv"
mvn clean package
```

Generates:

```text id="m1x7pw"
target/app.jar
```

---

# 5.2 Build Docker Image

Command:

```bash id="v7m3qz"
docker build -t springboot-app .
```

Explanation:

| Part           | Meaning           |
| -------------- | ----------------- |
| docker build   | Build image       |
| -t             | Tag image         |
| springboot-app | Image name        |
| .              | Current directory |

---

# 5.3 Verify Image

```bash id="k8v2wx"
docker images
```

---

# 6. Run Spring Boot Container

Command:

```bash id="t4q9mz"
docker run -p 8080:8080 springboot-app
```

Explanation:

| Part      | Meaning                |
| --------- | ---------------------- |
| -p        | Port mapping           |
| 8080:8080 | HostPort:ContainerPort |

---

# 7. Access Application

Open browser:

```text id="y6m1vx"
http://localhost:8080
```

---

# 8. Docker Layers

---

# 8.1 What are Layers?

Every Dockerfile instruction creates:

```text id="n3q7tw"
Image layer
```

Example:

```dockerfile id="p8v4mz"
FROM ubuntu
RUN apt install java
COPY app.jar .
```

Creates:

```text id="u2x9qw"
Layer 1 → Ubuntu
Layer 2 → Java
Layer 3 → app.jar
```

---

# 8.2 Why Layers Matter

Benefits:

* Faster builds
* Better caching
* Smaller downloads
* Shared storage

---

# 8.3 Layer Caching

If only:

```text id="m5v1qx"
app.jar changes
```

Docker rebuilds only:

```text id="w7t3mz"
COPY layer
```

Previous layers reused.

This improves performance significantly.

---

# 8.4 Image Optimization

---

## Use Smaller Base Images

Example:

```dockerfile id="k1q8vx"
FROM eclipse-temurin:21-jre-alpine
```

Benefits:

* Smaller image size
* Faster pull
* Lower attack surface

---

## Use Multi-Stage Build

Example:

```dockerfile id="t6m4qw"
FROM maven:3.9 AS build
FROM eclipse-temurin:21-jre
```

Benefits:

* Removes unnecessary build tools
* Production-ready image

---

# 9. Environment Variables

---

# 9.1 Why Environment Variables?

Used for:

* DB URL
* Username/password
* API keys
* Profiles

Avoid hardcoding configs.

---

# 9.2 Pass Environment Variables

Example:

```bash id="x3v9mz"
docker run -e DB_HOST=mysql springboot-app
```

---

# 9.3 Multiple Environment Variables

```bash id="m7q1vx"
docker run \
-e DB_HOST=mysql \
-e DB_PORT=3306 \
-e DB_USER=root \
springboot-app
```

---

# 9.4 Access in Spring Boot

Example:

```properties id="w2t8qy"
spring.datasource.url=jdbc:mysql://${DB_HOST}:${DB_PORT}/mydb
```

Docker injects values at runtime.

---

# 10. Logs & Debugging

Debugging is one of the most important Docker skills.

---

# 10.1 View Logs

Command:

```bash id="u9m3qx"
docker logs <container-id>
```

Example:

```bash id="k4v7tw"
docker logs abc123
```

Used for:

* Startup failures
* Exception tracking
* Application debugging

---

# 10.2 Follow Logs

Real-time logs:

```bash id="x1q8mz"
docker logs -f <container-id>
```

---

# 10.3 Inspect Container

Command:

```bash id="m6v2qx"
docker inspect <container-id>
```

Provides:

* Network info
* Mounts
* Environment variables
* Container configuration

---

# 10.4 Resource Usage

Command:

```bash id="w8q5tv"
docker stats
```

Shows:

* CPU usage
* Memory usage
* Network usage

---

# 10.5 Enter Running Container

Command:

```bash id="r3m9vx"
docker exec -it <container-id> bash
```

Useful for:

* Checking files
* Running Linux commands
* Debugging manually

---

# 11. Common Debugging Problems

---

# 11.1 Port Not Accessible

Problem:

```text id="k7q2tw"
Application running but browser cannot access
```

Check:

* Correct port mapping
* Application listening on correct port

---

# 11.2 Container Exits Immediately

Possible causes:

* Application crash
* Wrong ENTRYPOINT
* Missing dependencies

Check logs:

```bash id="u5v8mz"
docker logs <container-id>
```

---

# 11.3 JAR File Missing

Problem:

```text id="x4m1qy"
Unable to access jarfile
```

Cause:

* Incorrect COPY path

---

# 11.4 Out of Memory

Check:

```bash id="t9q6vx"
docker stats
```

---

# 12. Hands-On Task

---

# Task 1 — Containerize Spring Boot App

Steps:

1. Create Dockerfile
2. Build JAR
3. Build Docker image
4. Run container

---

# Task 2 — Pass Environment Variables

Pass:

* DB_HOST
* DB_PORT
* DB_USER

using:

```bash id="m2v7qw"
-e
```

---

# Task 3 — Debug Failures

Practice:

* Wrong port mapping
* Missing JAR
* Application crash

Use:

```bash id="p6q1vx"
docker logs
docker inspect
docker exec
```

---

# 13. Best Practices

---

# 13.1 Use Fixed Base Image Versions

GOOD:

```dockerfile id="w3m8qz"
FROM eclipse-temurin:21-jre
```

BAD:

```dockerfile id="k5v2tw"
FROM latest
```

---

# 13.2 Keep Images Small

Use:

* Alpine
* Multi-stage builds

---

# 13.3 Avoid Hardcoding Configurations

Use:

```text id="r7q4vx"
Environment variables
```

---

# 13.4 One Process Per Container

Each container should run:

```text id="x9m1qw"
Single main application
```

---

# 14. Summary

Today you learned:

✅ Dockerfile fundamentals
✅ Building Docker images
✅ Running Spring Boot containers
✅ Docker layers & caching
✅ Environment variables
✅ Container debugging

These are foundational skills for:

* Docker Compose
* Kubernetes
* CI/CD pipelines
* Cloud-native deployments

```
```
