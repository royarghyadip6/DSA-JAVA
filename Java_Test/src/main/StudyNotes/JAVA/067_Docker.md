# 67. Docker Questions

## 67. Docker Questions

Containerization for Java backend — build, ship, run consistently from dev to production.

---

## Basics

---

# 1. What is Docker?

<details>
<summary>Show Answer</summary>

**Answer:**

**Docker** is a platform to **package applications with all dependencies** (JRE, libs, config) into a **portable container** that runs identically on any host with Docker Engine installed.

```text
Your Spring Boot JAR + JRE 17 + OS libs → Docker Image → runs same on laptop, CI, prod
```

| Concept | Meaning |
|---------|---------|
| Image | Read-only blueprint (layers) |
| Container | Running instance of an image |
| Dockerfile | Recipe to build image |
| Registry | Store/share images (Docker Hub, ECR, ACR) |

```dockerfile
FROM eclipse-temurin:17-jre-alpine
COPY target/app.jar /app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

**Interview Point:**

> Docker = OS-level virtualization via containers. Not a VM — shares host kernel. "Build once, run anywhere" for microservices.

</details>

---

# 2. Why Docker?

<details>
<summary>Show Answer</summary>

**Answer:**

| Problem (without Docker) | Docker fix |
|--------------------------|------------|
| "Works on my machine" | Same image everywhere |
| Manual JDK/Tomcat setup | Base image bundles runtime |
| Slow VM provisioning | Seconds to start container |
| Dependency conflicts | Isolated filesystem per container |
| Inconsistent deploys | Immutable image version tag |

**Production benefits for Java teams:**

```text
✅ CI builds image → scan → push to registry → K8s pulls exact tag
✅ Rollback = redeploy previous image tag (v1.2.3 → v1.2.2)
✅ Horizontal scale = more container replicas, not more VMs
✅ Resource limits: --cpus, --memory per container
```

```bash
docker run -d --name order-service \
  -e SPRING_PROFILES_ACTIVE=prod \
  -p 8080:8080 \
  myregistry/order-service:1.4.0
```

**Interview Point:**

> Docker solves environment parity, fast deploys, and immutable releases. Senior answer: tie to CI/CD pipeline and image tagging strategy.

</details>

---

# 3. VM vs Docker?

<details>
<summary>Show Answer</summary>

**Answer:**

| | Virtual Machine | Docker Container |
|---|-----------------|------------------|
| Isolation | Full OS per VM | Process + namespaces/cgroups |
| Boot time | Minutes | Seconds |
| Size | GBs (full OS) | MBs (app + libs only) |
| Kernel | Own guest kernel | **Shares host kernel** |
| Density | Low | High (many containers per host) |
| Use case | Legacy, strong isolation | Microservices, cloud-native |

```text
Host OS
├── Hypervisor
│   ├── VM1 (Guest OS + App)     ← heavy
│   └── VM2 (Guest OS + App)
└── Docker Engine
    ├── Container A (App only)   ← light
    └── Container B (App only)
```

**Security note:** Containers are **not** as isolated as VMs — kernel escape is a risk; use namespaces, seccomp, non-root user, read-only root FS in prod.

**Interview Point:**

> VM = hardware virtualization + full OS. Container = OS virtualization — same kernel, isolated process. Containers win on speed and density; VMs win on hard multi-tenant isolation.

</details>

---

## Frequently Asked

---

# 4. Image?

<details>
<summary>Show Answer</summary>

**Answer:**

A **Docker image** is an **immutable, layered, read-only** template used to create containers.

```text
Layer 1: base OS (alpine)
Layer 2: JRE 17
Layer 3: app.jar
Layer 4: config (optional)
→ Each instruction in Dockerfile = new layer (cached on rebuild)
```

| Property | Detail |
|----------|--------|
| Immutable | Change = new image tag, not edit running container |
| Layer cache | `COPY pom.xml` before `COPY src` = faster rebuilds |
| Tag | `app:1.0.0`, `app:latest`, `app:sha-abc123` |
| Digest | Content-addressable `sha256:...` for supply-chain pinning |

```bash
docker images
docker pull eclipse-temurin:17-jre-alpine
docker tag myapp:1.0 myregistry/myapp:1.0
docker push myregistry/myapp:1.0
```

**Interview Point:**

> Image = blueprint (layers). Never patch prod container — rebuild image, redeploy. Use semantic tags + never rely on `latest` in production.

</details>

---

# 5. Container?

<details>
<summary>Show Answer</summary>

**Answer:**

A **container** is a **running instance** of an image — writable layer on top of read-only image layers.

```text
Image (read-only layers)
    +
Container writable layer (logs, temp files, runtime state)
    =
Running process(es) with isolated network, PID, mount namespaces
```

| State | Command |
|-------|---------|
| Created | `docker create` |
| Running | `docker run` / `docker start` |
| Stopped | `docker stop` (SIGTERM → grace → SIGKILL) |
| Removed | `docker rm` (data in writable layer gone unless volumes) |

```bash
docker ps              # running
docker ps -a           # all
docker inspect <id>    # IP, mounts, env, health
docker stats           # CPU/memory live
```

**Production:** Containers are **ephemeral** — design for restart anytime. State goes to DB, Redis, or volumes.

**Interview Point:**

> Container = runnable instance. Ephemeral by design. Persist data via volumes, not container filesystem.

</details>

---

# 6. Dockerfile?

<details>
<summary>Show Answer</summary>

**Answer:**

**Dockerfile** = text recipe to build an image step by step.

```dockerfile
# Multi-stage friendly Spring Boot example
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN ./mvnw dependency:go-offline -B
COPY src ./src
RUN ./mvnw package -DskipTests -B

FROM eclipse-temurin:17-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
USER app
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
```

| Instruction | Purpose |
|-------------|---------|
| `FROM` | Base image |
| `WORKDIR` | Set working directory |
| `COPY` / `ADD` | Copy files (prefer COPY) |
| `RUN` | Execute at build time |
| `ENV` | Environment variable |
| `EXPOSE` | Document port (does not publish) |
| `USER` | Non-root for security |
| `ENTRYPOINT` / `CMD` | Start command |

**Interview Point:**

> Dockerfile = build recipe. Senior tips: non-root user, HEALTHCHECK, JVM container flags (`UseContainerSupport`, `MaxRAMPercentage`), `.dockerignore` to shrink context.

</details>

---

## Commands

---

# 7. build

<details>
<summary>Show Answer</summary>

**Answer:**

**`docker build`** creates an image from a Dockerfile.

```bash
# Basic build
docker build -t order-service:1.2.0 .

# Build with build-args (version, profile)
docker build \
  --build-arg APP_VERSION=1.2.0 \
  -t myregistry/order-service:1.2.0 \
  -f Dockerfile.prod .

# No cache (force fresh layers)
docker build --no-cache -t order-service:1.2.0 .
```

| Flag | Use |
|------|-----|
| `-t` | Tag name:version |
| `-f` | Alternate Dockerfile path |
| `--build-arg` | Pass variables to Dockerfile |
| `.` | Build context (sent to daemon) |

**CI/CD pattern:**

```text
git commit → Jenkins/GitHub Actions → docker build → trivy scan → push ECR → deploy K8s
```

**Interview Point:**

> Build context = everything Docker daemon receives. Keep small with `.dockerignore`. Tag with git SHA for traceability.

</details>

---

# 8. run

<details>
<summary>Show Answer</summary>

**Answer:**

**`docker run`** creates and starts a container from an image.

```bash
docker run -d \
  --name payment-api \
  --restart unless-stopped \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/pay \
  -e JAVA_OPTS="-Xms512m -Xmx512m" \
  -v app-logs:/var/log \
  --memory=1g --cpus=1.5 \
  --network backend-net \
  myregistry/payment-api:2.1.0
```

| Flag | Purpose |
|------|---------|
| `-d` | Detached (background) |
| `-p host:container` | Port mapping |
| `-e` | Environment variable |
| `-v` | Volume mount |
| `--name` | Container name |
| `--restart` | Auto-restart policy |
| `--memory` / `--cpus` | Resource limits |

**Interview Point:**

> `run` = create + start. Prod: always set memory/CPU limits, restart policy, explicit image tag, env from secrets manager not hardcoded.

</details>

---

# 9. ps

<details>
<summary>Show Answer</summary>

**Answer:**

**`docker ps`** lists containers.

```bash
docker ps              # running only
docker ps -a           # including stopped
docker ps -q           # IDs only
docker ps --filter "name=order" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
```

| Column | Meaning |
|--------|---------|
| CONTAINER ID | Short ID |
| IMAGE | Source image |
| STATUS | Up 2 hours / Exited (137) |
| PORTS | 0.0.0.0:8080→8080/tcp |
| NAMES | Assigned name |

**Exit code 137** = SIGKILL (often OOM killed by kernel).

```bash
docker inspect --format='{{.State.OOMKilled}}' <container>
```

**Interview Point:**

> `ps -a` for crash-loop debugging. Exit 137 = OOM. Check `docker events` for restart storms.

</details>

---

# 10. logs

<details>
<summary>Show Answer</summary>

**Answer:**

**`docker logs`** streams stdout/stderr from container process.

```bash
docker logs payment-api
docker logs -f payment-api          # follow (tail -f)
docker logs --since 30m payment-api # last 30 min
docker logs --tail 200 payment-api  # last 200 lines
docker logs -t payment-api          # timestamps
```

**Production logging architecture:**

```text
App → stdout/stderr → Docker logging driver → Fluentd/Fluent Bit → ELK/Datadog
```

| Driver | Use |
|--------|-----|
| `json-file` | Default (rotate with log-opt) |
| `awslogs` | CloudWatch on ECS |
| `fluentd` | Centralized aggregation |

```json
// logback-spring.xml — log to console in containers
<appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
```

**Interview Point:**

> Containers should log to **stdout**, not files inside container. Use centralized log stack; `docker logs` is for quick debug only.

</details>

---

# 11. exec

<details>
<summary>Show Answer</summary>

**Answer:**

**`docker exec`** runs a command **inside a running container**.

```bash
# Interactive shell (if sh/bash exists in image)
docker exec -it payment-api sh

# One-off command
docker exec payment-api java -version
docker exec payment-api curl -s localhost:8080/actuator/health

# Thread dump from running Java container
docker exec payment-api jstack 1
```

| Flag | Meaning |
|------|---------|
| `-it` | Interactive + TTY |
| `-u app` | Run as specific user |
| `-e VAR=val` | Env for this command |

**vs `docker attach`:** attach connects to PID 1 stdout; exec starts new process.

**Production caution:** Don't install debug tools in prod image — use **debug sidecar** or `kubectl debug` equivalent. Prefer actuator endpoints over shell in prod.

**Interview Point:**

> `exec` = debug live container. For Java: jstack/jcmd via exec if JDK tools in image, or use actuator/threaddump endpoint.

</details>

---

## Advanced

---

# 12. Multi-stage build?

<details>
<summary>Show Answer</summary>

**Answer:**

**Multi-stage build** uses multiple `FROM` stages — compile in one stage, copy only artifact to final slim runtime image.

```dockerfile
# Stage 1: build (heavy — Maven, JDK, source)
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /src
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests

# Stage 2: runtime (light — JRE only + jar)
FROM eclipse-temurin:17-jre-alpine
COPY --from=builder /src/target/app.jar /app.jar
USER nobody
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

| Benefit | Impact |
|---------|--------|
| Smaller image | 800MB build → 150MB runtime |
| Faster deploys | Less to pull over network |
| Security | No source, Maven, or compiler in prod image |
| Separation | Build tools never in final layer |

```bash
docker history myapp:1.0   # verify final image has no src/
```

**Interview Point:**

> Multi-stage = build fat, ship thin. Standard for Java: Maven stage + JRE-alpine stage. Reduces attack surface and pull time.

</details>

---

# 13. Docker networking?

<details>
<summary>Show Answer</summary>

**Answer:**

Docker provides **virtual networks** so containers communicate by name without hardcoded IPs.

| Network driver | Use case |
|----------------|----------|
| `bridge` | Default single-host container comms |
| `host` | Container uses host network stack (no NAT) |
| `overlay` | Multi-host (Swarm/K8s CNI uses own) |
| `none` | No networking |
| `macvlan` | Container gets real MAC/IP on LAN |

```bash
docker network create backend-net
docker run -d --name api --network backend-net api:1.0
docker run -d --name db --network backend-net postgres:15
# api connects to jdbc:postgresql://db:5432/mydb
```

```text
bridge network "backend-net"
  ├── api (172.18.0.2) ──DNS──► db (172.18.0.3)
  └── host port 8080:8080 published for external access
```

**Compose example:**

```yaml
services:
  api:
    networks: [backend]
  db:
    networks: [backend]
networks:
  backend:
```

**Interview Point:**

> Containers on same user-defined bridge resolve each other by **service name**. `-p` publishes to host; internal traffic stays on overlay/bridge without exposing DB ports.

</details>

---

# 14. Docker volumes?

<details>
<summary>Show Answer</summary>

**Answer:**

**Volumes** persist data **outside** the container writable layer — survive container delete/restart.

| Type | Description |
|------|-------------|
| **Named volume** | Docker-managed (`docker volume create`) |
| **Bind mount** | Host path → container path |
| **tmpfs** | RAM-only (secrets, temp) |

```bash
# Named volume (preferred for DB data)
docker volume create pgdata
docker run -d -v pgdata:/var/lib/postgresql/data postgres:15

# Bind mount (dev only — config hot reload)
docker run -v $(pwd)/application.yml:/app/config/application.yml api:dev

# List / inspect
docker volume ls
docker volume inspect pgdata
```

| Data type | Where to store |
|-----------|----------------|
| DB files | Named volume or managed DB service |
| App logs | stdout → log aggregator (not volume) |
| Uploads | S3/MinIO, not container FS |
| Config/secrets | ConfigMap/Secret (K8s) or env/secrets manager |

```dockerfile
VOLUME ["/data"]   # declare mount point — data not in image layer
```

**Interview Point:**

> Volumes = persistent state. Stateless app containers + external DB/cache. Bind mounts for local dev only; prod uses named volumes or cloud storage.

</details>

---

# 5–8 Year Interview Rapid Fire

### Q: Image vs container?

<details>
<summary>Show Answer</summary>

**Answer:**

**Image** = read-only template (layers). **Container** = running instance with writable layer on top. Many containers from one image.

</details>

---

### Q: Why multi-stage Dockerfile for Java?

<details>
<summary>Show Answer</summary>

**Answer:**

Build with JDK+Maven, ship only JRE+jar. Smaller image, faster deploy, no build tools in production attack surface.

</details>

---

### Q: Container OOM — how do you know?

<details>
<summary>Show Answer</summary>

**Answer:**

Exit code **137**, `OOMKilled: true` in inspect, kernel logs `Killed process`. Fix: set `--memory`, tune `-XX:MaxRAMPercentage`, right-size heap.

</details>

---

### Q: Where should Java apps log in Docker?

<details>
<summary>Show Answer</summary>

**Answer:**

**Stdout/stderr** — collected by logging driver → ELK/Datadog. Never rely on log files inside ephemeral container FS.

</details>

---

### Q: docker run -p 8080:8080 meaning?

<details>
<summary>Show Answer</summary>

**Answer:**

Map **host port 8080** → **container port 8080**. External traffic hits host:8080, Docker NAT forwards to container.

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> Docker packages app+deps into immutable layered images; containers are ephemeral runtime instances sharing host kernel. Multi-stage builds ship slim JRE images. Persist data with volumes; log to stdout; set memory limits; tag images for immutable prod deploys.

</details>
