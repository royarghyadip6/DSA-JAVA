# Day 3 — Docker Compose + Networking + Volumes

# 1. Introduction

Day 3 focuses on:

* Container networking
* Persistent storage
* Multi-container applications
* Docker Compose

Goal:

```text id="p4m8vx"
Run complete Spring Boot + MySQL + Redis stack using Docker Compose
```

---

# 2. Docker Networking

Containers communicate using Docker networks.

Without networking:

```text id="u7q2tw"
Containers cannot communicate properly
```

Docker provides isolated virtual networks.

---

# 3. Types of Docker Networks

| Network Type | Purpose                   |
|--------------|---------------------------|
| bridge       | Default container network |
| host         | Shares host network       |
| none         | No networking             |
| overlay      | Multi-host networking     |

Most commonly used:

```text id="x1m5qz"
Bridge network
```

---

# 4. Bridge Network

Default Docker network.

Containers connected to same bridge network can:

* Communicate
* Resolve each other using DNS

---

# 4.1 View Networks

```bash id="m9q3vx"
docker network ls
```

Example:

```text id="w4m7qy"
NETWORK ID     NAME      DRIVER
abc123         bridge    bridge
```

---

# 4.2 Create Custom Network

```bash id="u2v8qx"
docker network create mynet
```

---

# 4.3 Verify Network

```bash id="x6m1qw"
docker network inspect mynet
```

Shows:

* Connected containers
* IP addresses
* Network details

---

# 5. Connect Containers to Network

Example:

```bash id="p5q9vz"
docker run -d --network mynet --name mysql mysql
```

Another container:

```bash id="t8m2qx"
docker run -it --network mynet ubuntu bash
```

Now containers can communicate.

---

# 6. Container DNS

Docker provides:

```text id="w3v7qy"
Automatic DNS resolution
```

Containers communicate using:

```text id="u9m4qx"
Container names
```

instead of IP addresses.

Example:

```text id="x2q8vw"
mysql
redis
app
```

---

# 7. Inter-Container Communication

Suppose:

* Spring Boot container
* MySQL container

Spring Boot connects using:

```properties id="m6v1qz"
spring.datasource.url=jdbc:mysql://mysql:3306/mydb
```

Here:

```text id="p7m5qx"
mysql
```

is container name.

No hardcoded IP needed.

---

# 8. Why Custom Networks are Important

Benefits:

* Better isolation
* Automatic DNS
* Easier communication
* Cleaner architecture

---

# 9. Docker Volumes

Containers are:

```text id="u1q7vx"
Ephemeral
```

Meaning:

```text id="m4v2qw"
Data disappears when container is removed
```

Volumes provide:

```text id="x8m9qz"
Persistent storage
```

---

# 10. Types of Docker Storage

| Type         | Purpose              |
|--------------|----------------------|
| Named Volume | Managed by Docker    |
| Bind Mount   | Uses host filesystem |
| tmpfs        | Memory-only storage  |

---

# 11. Named Volumes

Docker-managed persistent storage.

---

# 11.1 Create Volume

```bash id="p3q6vw"
docker volume create mysql-data
```

---

# 11.2 List Volumes

```bash id="t7m1qx"
docker volume ls
```

---

# 11.3 Use Volume

Example:

```bash id="w5v8qz"
docker run -d \
-v mysql-data:/var/lib/mysql \
mysql
```

Meaning:

```text id="u2m4qx"
mysql-data
→
/var/lib/mysql
```

inside container.

---

# 12. Why Volumes are Important

Without volume:

```text id="x9q3vw"
MySQL data lost when container removed
```

With volume:

```text id="m1v7qx"
Data persists permanently
```

---

# 13. Bind Mounts

Maps:

```text id="p6m2qz"
Host directory → Container directory
```

Example:

```bash id="t4q8vw"
docker run -v /home/app/logs:/logs nginx
```

---

# 14. Named Volume vs Bind Mount

| Named Volume            | Bind Mount                |
|-------------------------|---------------------------|
| Managed by Docker       | Managed by user           |
| Better portability      | Direct host access        |
| Preferred for databases | Preferred for development |

---

# 15. Docker Compose

Docker Compose is used for:

```text id="w8m5qx"
Running multi-container applications
```

using:

```text id="u3q1vz"
Single YAML file
```

---

# 16. Why Docker Compose?

Without Compose:

Need multiple commands:

* Run MySQL
* Run Redis
* Run Spring Boot
* Create networks
* Create volumes

Complex and error-prone.

Compose simplifies everything.

---

# 17. Compose File

Default filename:

```text id="x7m4qw"
compose.yaml
```

OR

```text id="p2v9qx"
docker-compose.yml
```

---

# 18. Basic Compose Structure

```yaml id="m5q7vw"
services:
  app:
  mysql:
```

---

# 19. Full Stack Compose Example

```yaml id="u8m2qx"
services:

  mysql:
    image: mysql:8
    container_name: mysql
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: mydb
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql

  redis:
    image: redis
    container_name: redis
    ports:
      - "6379:6379"

  app:
    build: .
    container_name: springboot-app
    ports:
      - "8080:8080"
    depends_on:
      - mysql
      - redis
    environment:
      DB_HOST: mysql

volumes:
  mysql-data:
```

---

# 20. Important Compose Concepts

---

# 20.1 services

Defines containers.

Example:

```yaml id="x1q5vz"
services:
  mysql:
```

---

# 20.2 image

Specifies Docker image.

Example:

```yaml id="m9v2qx"
image: mysql:8
```

---

# 20.3 build

Builds image from Dockerfile.

Example:

```yaml id="u4m7qw"
build: .
```

---

# 20.4 ports

Port mapping.

Example:

```yaml id="p8q1vx"
ports:
  - "8080:8080"
```

---

# 20.5 environment

Environment variables.

Example:

```yaml id="t6m3qz"
environment:
  DB_HOST: mysql
```

---

# 20.6 volumes

Persistent storage.

Example:

```yaml id="w2q9vx"
volumes:
  - mysql-data:/var/lib/mysql
```

---

# 20.7 depends_on

Controls startup order.

Example:

```yaml id="x5m1qw"
depends_on:
  - mysql
```

---

# 21. Docker Compose Commands

---

# 21.1 Start Application

```bash id="m7q4vz"
docker compose up
```

---

# 21.2 Start in Background

```bash id="u1m8qx"
docker compose up -d
```

---

# 21.3 Stop Application

```bash id="p5q2vw"
docker compose down
```

---

# 21.4 View Logs

```bash id="t9m6qx"
docker compose logs
```

---

# 21.5 Rebuild Images

```bash id="w3q7vz"
docker compose up --build
```

---

# 22. Compose Networking Internally

Docker Compose automatically creates:

```text id="x8m4qw"
Dedicated bridge network
```

All services communicate using:

```text id="p2q1vx"
Service names
```

Example:

```text id="u6m9qz"
mysql
redis
app
```

---

# 23. Full Stack Setup

Application stack:

| Service     | Purpose     |
|-------------|-------------|
| Spring Boot | Backend API |
| MySQL       | Database    |
| Redis       | Cache       |

---

# 24. Spring Boot Database Connection

application.properties:

```properties id="m4q7vw"
spring.datasource.url=jdbc:mysql://mysql:3306/mydb
spring.datasource.username=root
spring.datasource.password=root
```

`mysql` works because:

```text id="t1m5qx"
Docker DNS resolves service names
```

---

# 25. Verify Container Communication

Enter Spring Boot container:

```bash id="w7q2vz"
docker exec -it springboot-app bash
```

Ping MySQL:

```bash id="u9m6qx"
ping mysql
```

Successful ping confirms networking works.

---

# 26. Verify Persistent Storage

Stop containers:

```bash id="p3q8vw"
docker compose down
```

Restart:

```bash id="x5m1qz"
docker compose up
```

MySQL data should remain.

Because:

```text id="m7v4qx"
Volume persists data
```

---

# 27. Common Problems

---

# 27.1 MySQL Connection Failed

Possible causes:

* Wrong hostname
* Container startup timing
* Incorrect credentials

Check:

```bash id="t2q7vw"
docker compose logs
```

---

# 27.2 Port Already in Use

Change host port:

```yaml id="u6m3qx"
ports:
  - "3307:3306"
```

---

# 27.3 Volume Permission Issues

Sometimes Linux permissions block DB startup.

Fix:

* Remove volume
* Recreate container

---

# 28. Best Practices

---

# 28.1 Use Named Volumes for Databases

Recommended for:

* MySQL
* PostgreSQL
* MongoDB

---

# 28.2 Use Service Names Instead of IPs

GOOD:

```text id="p8m1qz"
mysql
```

BAD:

```text id="w4q6vx"
172.18.0.5
```

---

# 28.3 Keep Secrets Outside Compose File

Avoid hardcoding:

* Passwords
* API keys

Use:

* .env
* Docker secrets

---

# 28.4 Use depends_on Carefully

`depends_on`:

```text id="x1m9qw"
Does NOT guarantee application readiness
```

Only startup order.

---

# 29. Hands-On Tasks

---

# Task 1 — Create Network

```bash id="m5q2vz"
docker network create mynet
```

---

# Task 2 — Create Volume

```bash id="u8m7qx"
docker volume create mysql-data
```

---

# Task 3 — Run MySQL with Volume

Persist database data.

---

# Task 4 — Create compose.yaml

Include:

* Spring Boot
* MySQL
* Redis

---

# Task 5 — Verify Communication

Inside container:

```bash id="p2q4vw"
ping mysql
```

---

# 30. Summary

Today you learned:

✅ Docker networking
✅ Bridge networks
✅ Container DNS
✅ Inter-container communication
✅ Docker volumes
✅ Named volumes
✅ Bind mounts
✅ Docker Compose
✅ Multi-container orchestration
✅ Spring Boot + MySQL setup

These concepts are foundational for:

* Kubernetes networking
* Persistent storage
* Microservices
* Production deployments
