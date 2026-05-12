# Docker Interview Questions (MCQ + Long Questions)

---

# 1. What is Docker?

<details>
<summary>Answer</summary>

Docker is a containerization platform used to package applications along with dependencies, libraries, and runtime into isolated containers.

Benefits:

* Portability
* Faster deployment
* Lightweight virtualization
* Consistent environments

</details>

---

# 2. Docker uses which type of virtualization?

## MCQ

A. Hardware virtualization
B. Hypervisor virtualization
C. OS-level virtualization
D. BIOS virtualization

<details>
<summary>Answer</summary>

✅ Correct Answer: C. OS-level virtualization

Docker containers share the host OS kernel.

</details>

---

# 3. Difference Between Docker Image and Container

<details>
<summary>Answer</summary>

| Docker Image        | Docker Container          |
|---------------------|---------------------------|
| Read-only template  | Running instance of image |
| Blueprint           | Executable process        |
| Static              | Dynamic                   |
| Cannot run directly | Runs application          |

Example:

* nginx image
* nginx container

</details>

---

# 4. What problem does Docker solve?

<details>
<summary>Answer</summary>

Docker solves:

```text
"It works on my machine"
```

problem by packaging:

* Application
* Runtime
* Dependencies
* Configurations

inside containers.

</details>

---

# 5. Which command lists running containers?

## MCQ

A. docker images
B. docker ps
C. docker run
D. docker inspect

<details>
<summary>Answer</summary>

✅ Correct Answer: B. docker ps

</details>

---

# 6. Difference Between VM and Container

<details>
<summary>Answer</summary>

| VM               | Container          |
|------------------|--------------------|
| Includes full OS | Shares host kernel |
| Heavyweight      | Lightweight        |
| Slow startup     | Fast startup       |
| Large size       | Small size         |

Containers are more resource-efficient.

</details>

---

# 7. What is Docker Daemon?

<details>
<summary>Answer</summary>

Docker Daemon:

```text
dockerd
```

is background service responsible for:

* Running containers
* Building images
* Managing networks
* Managing volumes

</details>

---

# 8. Which Docker component actually runs containers?

## MCQ

A. Docker CLI
B. Docker Hub
C. containerd/runc
D. Dockerfile

<details>
<summary>Answer</summary>

✅ Correct Answer: C. containerd/runc

</details>

---

# 9. Explain Docker Architecture

<details>
<summary>Answer</summary>

Architecture:

```text
Docker Client
      ↓
Docker Daemon
      ↓
Container Runtime
      ↓
Containers
```

Components:

* Docker Client → CLI commands
* Docker Daemon → Background engine
* Runtime → Runs containers

</details>

---

# 10. What is Docker Hub?

<details>
<summary>Answer</summary>

[Docker Hub](https://hub.docker.com?utm_source=chatgpt.com) is a public Docker registry used to:

* Store images
* Share images
* Pull official images

Examples:

* nginx
* mysql
* redis

</details>

---

# 11. Which command downloads image from Docker Hub?

## MCQ

A. docker images
B. docker pull
C. docker build
D. docker run

<details>
<summary>Answer</summary>

✅ Correct Answer: B. docker pull

Example:

```bash
docker pull nginx
```

</details>

---

# 12. Explain Docker Layers

<details>
<summary>Answer</summary>

Each Dockerfile instruction creates a layer.

Example:

* FROM
* COPY
* RUN

Benefits:

* Faster builds
* Caching
* Reusability

</details>

---

# 13. What is a Dockerfile?

<details>
<summary>Answer</summary>

Dockerfile is a script containing instructions to build Docker images.

Example:

```dockerfile
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/app.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
```

</details>

---

# 14. Which instruction specifies base image?

## MCQ

A. RUN
B. COPY
C. FROM
D. CMD

<details>
<summary>Answer</summary>

✅ Correct Answer: C. FROM

Example:

```dockerfile
FROM ubuntu
```

</details>

---

# 15. Difference Between CMD and ENTRYPOINT

<details>
<summary>Answer</summary>

| CMD                      | ENTRYPOINT         |
|--------------------------|--------------------|
| Default command          | Main executable    |
| Can be overridden easily | Harder to override |
| Flexible                 | Fixed execution    |

Example:

```dockerfile
ENTRYPOINT ["java"]
CMD ["-version"]
```

</details>

---

# 16. What is Detached Mode?

<details>
<summary>Answer</summary>

Detached mode runs container in background.

Command:

```bash
docker run -d nginx
```

</details>

---

# 17. What does `-it` mean in Docker?

## MCQ

A. Internet terminal
B. Interactive terminal
C. Internal transfer
D. Image tag

<details>
<summary>Answer</summary>

✅ Correct Answer: B. Interactive terminal

* `-i` → interactive
* `-t` → terminal

</details>

---

# 18. Explain Port Mapping

<details>
<summary>Answer</summary>

Port mapping exposes container ports to host.

Example:

```bash
docker run -p 8080:80 nginx
```

Meaning:

```text
HostPort:ContainerPort
```

Browser:

```text
localhost:8080
```

→ nginx container port 80

</details>

---

# 19. Which command enters running container shell?

## MCQ

A. docker ps
B. docker shell
C. docker exec -it
D. docker attach container

<details>
<summary>Answer</summary>

✅ Correct Answer: C. docker exec -it

Example:

```bash
docker exec -it container-id bash
```

</details>

---

# 20. Explain Docker Networking

<details>
<summary>Answer</summary>

Docker networking enables communication between:

* Containers
* Host and containers

Network types:

* bridge
* host
* none
* overlay

Default:

```text
bridge
```

</details>

---

# 21. What is Bridge Network?

<details>
<summary>Answer</summary>

Bridge network is default Docker network.

Features:

* Isolated network
* Container communication
* Internal DNS resolution

</details>

---

# 22. Which command creates Docker network?

## MCQ

A. docker create network
B. docker network create
C. docker bridge create
D. docker net create

<details>
<summary>Answer</summary>

✅ Correct Answer: B. docker network create

Example:

```bash
docker network create mynet
```

</details>

---

# 23. What are Docker Volumes?

<details>
<summary>Answer</summary>

Volumes provide:

```text
Persistent storage
```

for containers.

Without volumes:

* Data lost after container removal

Used heavily for:

* Databases
* Logs
* Persistent files

</details>

---

# 24. Difference Between Bind Mount and Volume

<details>
<summary>Answer</summary>

| Volume                  | Bind Mount                |
|-------------------------|---------------------------|
| Managed by Docker       | Managed by host           |
| Better portability      | Direct host access        |
| Preferred for databases | Preferred for development |

</details>

---

# 25. Which command creates volume?

## MCQ

A. docker volume create
B. docker create volume
C. docker storage create
D. docker mount create

<details>
<summary>Answer</summary>

✅ Correct Answer: A. docker volume create

</details>

---

# 26. What is Docker Compose?

<details>
<summary>Answer</summary>

Docker Compose is used for:

```text
Multi-container orchestration
```

using single YAML file.

Example:

* Spring Boot
* MySQL
* Redis

inside:

```text
compose.yaml
```

</details>

---

# 27. Which command starts compose services?

## MCQ

A. docker compose start
B. docker-compose run
C. docker compose up
D. docker compose build

<details>
<summary>Answer</summary>

✅ Correct Answer: C. docker compose up

</details>

---

# 28. Explain Inter-Container Communication

<details>
<summary>Answer</summary>

Containers communicate using:

```text
Container/service names
```

Example:

```properties
spring.datasource.url=jdbc:mysql://mysql:3306/mydb
```

`mysql` is container/service name.

</details>

---

# 29. What is Multi-Stage Build?

<details>
<summary>Answer</summary>

Multi-stage builds reduce image size.

Example:

* Stage 1 → Build JAR
* Stage 2 → Run JAR

Only final artifacts copied to runtime image.

Benefits:

* Smaller images
* Better security
* Faster deployment

</details>

---

# 30. Which command shows container logs?

## MCQ

A. docker inspect
B. docker ps
C. docker logs
D. docker history

<details>
<summary>Answer</summary>

✅ Correct Answer: C. docker logs

</details>

---

# 31. What is Docker Registry?

<details>
<summary>Answer</summary>

Registry stores Docker images.

Examples:

* Docker Hub
* AWS ECR
* Azure ACR

</details>

---

# 32. Explain Docker Image Optimization Techniques

<details>
<summary>Answer</summary>

Techniques:

* Use slim/alpine images
* Multi-stage builds
* Minimize layers
* Combine RUN commands
* Remove unnecessary files

Example:

```dockerfile
RUN apt update && apt install -y curl
```

instead of multiple RUN instructions.

</details>

---

# 33. What Happens Internally When `docker run` Executes?

<details>
<summary>Answer</summary>

Steps:

1. Pull image if not present
2. Create writable container layer
3. Configure networking
4. Configure filesystem
5. Start container process
6. Attach terminal (if interactive)

</details>

---

# 34. Explain Docker Container Lifecycle

<details>
<summary>Answer</summary>

Lifecycle:

```text
Created
   ↓
Running
   ↓
Paused
   ↓
Stopped
   ↓
Removed
```

</details>

---

# 35. Which command shows resource usage?

## MCQ

A. docker inspect
B. docker logs
C. docker stats
D. docker usage

<details>
<summary>Answer</summary>

✅ Correct Answer: C. docker stats

Shows:

* CPU usage
* Memory usage
* Network usage

</details>

---

# 36. Explain Namespaces and Cgroups

<details>
<summary>Answer</summary>

Docker uses Linux kernel features.

Namespaces provide:

* Process isolation
* Network isolation
* Filesystem isolation

Cgroups provide:

* CPU limits
* Memory limits
* Resource control

</details>

---

# 37. What is OOMKilled in Containers?

<details>
<summary>Answer</summary>

OOMKilled means:

```text
Out Of Memory Kill
```

Container exceeded memory limit.

Linux kernel terminates container process.

</details>

---

# 38. Explain Container Restart Policies

<details>
<summary>Answer</summary>

Policies:

* no
* always
* on-failure
* unless-stopped

Example:

```bash
docker run --restart always nginx
```

</details>

---

# 39. Difference Between COPY and ADD

## MCQ

A. No difference
B. ADD supports URL/tar extraction
C. COPY supports URLs
D. ADD only works for folders

<details>
<summary>Answer</summary>

✅ Correct Answer: B. ADD supports URL/tar extraction

Best practice:

```text
Prefer COPY unless ADD features required
```

</details>

---

# 40. Explain Real Production Docker Best Practices

<details>
<summary>Answer</summary>

Best practices:

* Use minimal base images
* Avoid running as root
* Use multi-stage builds
* Keep containers stateless
* Externalize configs
* Use health checks
* Limit resources
* Scan images for vulnerabilities

</details>

---

---

# Product-Based & Company-Oriented Docker Interview Questions

---

# 1. Your Spring Boot container works locally but fails in production. How will you debug?

<details>
<summary>Answer</summary>

Step-by-step debugging:

1. Check container status

```bash id="h0r9qs"
docker ps -a
```

2. Check logs

```bash id="8j7wma"
docker logs <container-id>
```

3. Enter container

```bash id="8cylso"
docker exec -it <container-id> bash
```

4. Verify:

* Environment variables
* DB connectivity
* Application ports
* JAR existence
* Memory issues

5. Check port mapping:

```bash id="pb11ee"
docker inspect <container-id>
```

Common real-world causes:

* Wrong ENV variables
* DB inaccessible
* Port conflicts
* Insufficient memory
* Wrong Java version

</details>

---

# 2. Which Docker command helps most in debugging startup failures?

## MCQ

A. docker pull
B. docker rm
C. docker logs
D. docker build

<details>
<summary>Answer</summary>

✅ Correct Answer: C. docker logs

Most startup failures are diagnosed using logs.

</details>

---

# 3. Your MySQL data disappears after container restart. Why?

<details>
<summary>Answer</summary>

Cause:

```text id="zyjg8g"
Container filesystem is ephemeral
```

When container is deleted:

* Internal filesystem disappears

Solution:
Use Docker volumes.

Example:

```bash id="yj4r91"
docker run -v mysql-data:/var/lib/mysql mysql
```

Volumes persist data outside container lifecycle.

</details>

---

# 4. In production, why are containers preferred over Virtual Machines?

<details>
<summary>Answer</summary>

Containers are preferred because:

* Faster startup
* Lightweight
* Better scalability
* Lower infrastructure cost
* Efficient resource usage

Example:

* VM startup → minutes
* Container startup → seconds

Containers improve:

* CI/CD speed
* Auto-scaling
* Deployment efficiency

</details>

---

# 5. Your application works locally but container cannot connect to MySQL container. What could be wrong?

<details>
<summary>Answer</summary>

Possible causes:

* Containers not in same network
* Wrong hostname
* MySQL not ready
* Incorrect credentials

Correct approach:

```properties id="yjlwmd"
spring.datasource.url=jdbc:mysql://mysql:3306/mydb
```

Where:

```text id="2ht3ai"
mysql
```

is container/service name.

NOT localhost.

</details>

---

# 6. Which network type is most commonly used in Docker Compose?

## MCQ

A. host
B. none
C. bridge
D. overlay

<details>
<summary>Answer</summary>

✅ Correct Answer: C. bridge

Docker Compose automatically creates bridge networks.

</details>

---

# 7. How would you reduce Docker image size in a real project?

<details>
<summary>Answer</summary>

Techniques:

* Use alpine/slim images
* Multi-stage builds
* Minimize dependencies
* Remove temp files
* Combine RUN commands

Example:

```dockerfile id="r3nh1m"
FROM maven:3.9-eclipse-temurin-21 AS build
COPY . .
RUN mvn clean package

FROM eclipse-temurin:21-jre-alpine
COPY --from=build target/app.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
```

Benefits:

* Faster deployments
* Reduced storage
* Improved security

</details>

---

# 8. Why is multi-stage build important in enterprise projects?

<details>
<summary>Answer</summary>

Without multi-stage build:

* Maven
* Source code
* Build tools

remain inside final image.

Problems:

* Large image size
* Security risk
* Slower deployment

Multi-stage build keeps:

```text id="m7a8nd"
Only runtime artifacts
```

inside final image.

</details>

---

# 9. Your container exits immediately after startup. What will you check first?

## MCQ

A. docker network ls
B. docker logs
C. docker images
D. docker volume ls

<details>
<summary>Answer</summary>

✅ Correct Answer: B. docker logs

Container exits usually due to:

* Startup exceptions
* Missing files
* Wrong commands

</details>

---

# 10. Explain real-world use of Docker Compose.

<details>
<summary>Answer</summary>

Docker Compose is used for:

```text id="pz4j8w"
Running complete local development environments
```

Example stack:

* Spring Boot
* MySQL
* Redis
* Kafka

Benefits:

* Single command startup
* Easy onboarding
* Consistent environments

Command:

```bash id="q9r1xb"
docker compose up
```

</details>

---

# 11. In microservices architecture, why is Docker very important?

<details>
<summary>Answer</summary>

Microservices require:

* Independent deployments
* Environment consistency
* Fast scaling

Docker helps by:

* Packaging each service independently
* Simplifying deployments
* Improving portability

Example:

* User service container
* Payment service container
* Notification service container

</details>

---

# 12. Which command shows real-time container resource usage?

## MCQ

A. docker logs
B. docker inspect
C. docker stats
D. docker history

<details>
<summary>Answer</summary>

✅ Correct Answer: C. docker stats

Displays:

* CPU
* Memory
* Network usage

</details>

---

# 13. A production container consumes too much memory and crashes. How will you troubleshoot?

<details>
<summary>Answer</summary>

Steps:

1. Check memory usage

```bash id="jovwvn"
docker stats
```

2. Check logs

```bash id="x7r6xa"
docker logs <container-id>
```

3. Check OOMKilled status

```bash id="33j1m8"
docker inspect <container-id>
```

4. Increase memory limits

5. Analyze:

* Memory leaks
* JVM heap size
* Infinite loops

For Java:
Tune JVM:

```bash id="m80rbv"
-Xms256m -Xmx512m
```

</details>

---

# 14. What is the biggest mistake developers make in Dockerfiles?

<details>
<summary>Answer</summary>

Common mistakes:

* Using large base images
* Running as root
* Hardcoding secrets
* Too many layers
* Copying unnecessary files

Bad example:

```dockerfile id="t4sr3g"
COPY . .
```

This copies:

* target/
* .git/
* logs/
* unnecessary files

Use:

```text id="aqnl0j"
.dockerignore
```

</details>

---

# 15. Why should secrets never be hardcoded in Docker images?

<details>
<summary>Answer</summary>

Hardcoded secrets:

* Become part of image layers
* Can be extracted easily
* Create major security risk

Never hardcode:

* DB passwords
* API keys
* Tokens

Use:

* Environment variables
* Docker secrets
* Vault systems

</details>

---

# 16. Which file prevents unnecessary files from entering Docker image?

## MCQ

A. .dockerignore
B. .gitignore
C. Dockerfile.ignore
D. docker.exclude

<details>
<summary>Answer</summary>

✅ Correct Answer: A. .dockerignore

</details>

---

# 17. Your frontend can access backend locally but not inside Docker. What could be wrong?

<details>
<summary>Answer</summary>

Possible causes:

* Port mapping issue
* Wrong hostname
* CORS issue
* Containers isolated

Common mistake:
Using:

```text id="e3n1dy"
localhost
```

inside containers.

Correct:
Use service/container names.

Example:

```text id="45xl9n"
http://backend:8080
```

</details>

---

# 18. Explain Docker layer caching with real-world example.

<details>
<summary>Answer</summary>

Docker caches layers for faster builds.

Bad Dockerfile:

```dockerfile id="8e1yzl"
COPY . .
RUN mvn clean package
```

Any file change invalidates cache.

Optimized:

```dockerfile id="vjlwmk"
COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn package
```

Dependencies cached separately.

Benefits:

* Faster CI/CD
* Reduced build times

</details>

---

# 19. In enterprise production, why are immutable Docker images important?

<details>
<summary>Answer</summary>

Immutable image means:

```text id="12w7mx"
Image never changes after build
```

Benefits:

* Consistency
* Easier rollback
* Reliable deployments
* Better debugging

Production systems always deploy:

```text id="u95q8w"
Versioned immutable images
```

</details>

---

# 20. Explain Blue-Green Deployment using Docker.

<details>
<summary>Answer</summary>

Blue-Green deployment:

* Blue → current production
* Green → new version

Flow:

1. Deploy Green environment
2. Test Green
3. Switch traffic
4. Remove Blue later

Benefits:

* Zero downtime
* Easy rollback
* Safer deployments

Widely used in:

* Banking
* E-commerce
* Enterprise systems

</details>

---

# 21. Which is best practice for production Docker containers?

## MCQ

A. Run as root
B. Use latest tag everywhere
C. Use slim images and fixed versions
D. Store DB inside container filesystem

<details>
<summary>Answer</summary>

✅ Correct Answer: C. Use slim images and fixed versions

Avoid:

```text id="7e1vax"
latest
```

in production.

</details>

---

# 22. Why is `latest` tag dangerous in production?

<details>
<summary>Answer</summary>

`latest` is unpredictable.

Problems:

* Different deployments may use different images
* Hard rollback
* Non-repeatable builds

Preferred:

```dockerfile id="e4o8j7"
mysql:8.0.36
```

instead of:

```dockerfile id="3d4k2m"
mysql:latest
```

</details>

---

# 23. Explain Docker container security best practices.

<details>
<summary>Answer</summary>

Best practices:

* Run as non-root user
* Use minimal images
* Scan vulnerabilities
* Remove unnecessary packages
* Avoid hardcoded secrets
* Limit container privileges

Use:

* Distroless images
* Read-only filesystem
* Seccomp/AppArmor

</details>

---

# 24. Your CI/CD pipeline suddenly becomes slow after Docker adoption. Possible reasons?

<details>
<summary>Answer</summary>

Possible reasons:

* Large images
* No layer caching
* Huge build context
* Pulling dependencies repeatedly
* No multi-stage builds

Optimization:

* Use cache efficiently
* Reduce image size
* Use .dockerignore
* Separate dependency layers

</details>

---

# 25. Real-world interview question:

## "How would you Dockerize a Spring Boot application for production?"

<details>
<summary>Answer</summary>

Production-ready approach:

1. Multi-stage build

2. Minimal runtime image

3. Non-root user

4. Externalized configs

5. Health checks

6. Proper JVM tuning

Example:

```dockerfile id="0ylvjlwm"
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /app/target/app.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]
```

Additional production improvements:

* Readiness checks
* Monitoring
* Logging
* Resource limits

</details>
