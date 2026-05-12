# Day 1 — Docker Fundamentals + Installation

# Topics

## 1. What is Docker?

Learn:

* Problem before Docker
* Virtual Machine vs Container
* Why containers are lightweight
* Docker architecture

Understand:

```text id="4dng2o"
Client → Docker Daemon → Container Runtime
```

---

## 2. Install Docker

### Windows

* Docker Desktop
* WSL2
* Ubuntu integration

Verify:

```bash id="68k24g"
docker version
docker info
```

---

## 3. Core Docker Concepts

Learn:

* Images
* Containers
* Layers
* Registry
* Docker Hub

Commands:

```bash id="mxm3an"
docker pull nginx
docker images
docker ps
docker ps -a
docker stop
docker rm
```

---

## 4. Run Your First Containers

Practice:

```bash id="14gr14"
docker run hello-world
docker run -d nginx
docker run -it ubuntu bash
```

Learn:

* Detached mode
* Interactive mode
* Port mapping

---

## 5. Port Mapping

```bash id="jlwmn6"
docker run -p 8080:80 nginx
```

Understand:

```text id="z8qupv"
HostPort:ContainerPort
```

---

# Hands-On Task

✅ Run:

* nginx
* redis
* mysql

✅ Access nginx in browser

✅ Enter inside container:

```bash id="a7w0gr"
docker exec -it <container-id> bash
```

---

# Day 2 — Dockerfile + Spring Boot Containerization

# Topics

## 1. Dockerfile Deep Dive

Learn:

* FROM
* WORKDIR
* COPY
* RUN
* CMD
* ENTRYPOINT
* EXPOSE

---

## 2. Build Custom Images

Example:

```dockerfile id="jdb85o"
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/app.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
```

Build:

```bash id="hm3tdk"
docker build -t springboot-app .
```

Run:

```bash id="c6ivh0"
docker run -p 8080:8080 springboot-app
```

---

## 3. Docker Layers

Learn:

* Layer caching
* Image optimization

---

## 4. Environment Variables

```bash id="44mdo5"
docker run -e DB_HOST=mysql springboot-app
```

---

## 5. Logs & Debugging

Commands:

```bash id="i6m6vg"
docker logs
docker inspect
docker stats
```

---

# Hands-On Task

✅ Containerize a Spring Boot application

✅ Pass DB configs using ENV variables

✅ Debug container startup issues

---

# Day 3 — Docker Compose + Networking + Volumes

# Topics

## 1. Docker Networking

Learn:

* Bridge network
* Container DNS
* Inter-container communication

Commands:

```bash id="4lazga"
docker network ls
docker network create mynet
```

---

## 2. Docker Volumes

Learn:

* Named volumes
* Bind mounts

Commands:

```bash id="t9gch0"
docker volume create mysql-data
```

---

## 3. Docker Compose

Learn:

* Multi-container orchestration

Example:

```yaml id="7iihfj"
services:
  app:
  mysql:
```

Commands:

```bash id="xnl5oj"
docker compose up
docker compose down
```

---

## 4. Full Stack Setup

Create:

* Spring Boot
* MySQL
* Redis

inside compose.yaml

---

# Hands-On Task

✅ Run Spring Boot + MySQL together

✅ Persist MySQL data

✅ Verify container communication

---

# Day 4 — Kubernetes Fundamentals

# Topics

## 1. What is Kubernetes?

Learn:

* Why Docker alone is insufficient
* Container orchestration
* Auto-healing
* Scaling

---

## 2. Kubernetes Architecture

Learn:

* API Server
* Scheduler
* Controller Manager
* etcd
* Kubelet
* kube-proxy

---

## 3. Install Kubernetes

Use:

* Minikube OR Kind

Commands:

```bash id="nq2x1k"
kubectl get nodes
kubectl cluster-info
```

---

## 4. Pods

Learn:

* Pod lifecycle
* Single vs multi-container pod

Example YAML:

```yaml id="jlg9jt"
apiVersion: v1
kind: Pod
```

Commands:

```bash id="99nv70"
kubectl apply -f pod.yaml
kubectl get pods
kubectl describe pod
kubectl logs
```

---

## 5. kubectl Basics

Commands:

```bash id="q0srns"
kubectl get all
kubectl delete pod
kubectl exec -it
```

---

# Hands-On Task

✅ Deploy nginx pod

✅ Access logs

✅ Enter pod shell

✅ Delete pod and watch recreation

---

# Day 5 — Deployments + Services + ConfigMaps

# Topics

## 1. Deployments

Learn:

* ReplicaSets
* Scaling
* Rolling updates
* Rollbacks

Commands:

```bash id="xf6wq3"
kubectl scale
kubectl rollout history
kubectl rollout undo
```

---

## 2. Services

Learn:

* ClusterIP
* NodePort
* LoadBalancer

Understand:

```text id="tylmpk"
Pod IP changes → Service gives stable access
```

---

## 3. ConfigMaps

Learn:

* Externalized configs

---

## 4. Secrets

Learn:

* Base64 encoding
* Secret injection

---

## 5. Environment Variables in Kubernetes

Use:

```yaml id="j3wv4r"
env:
```

---

# Hands-On Task

✅ Deploy Spring Boot app in Kubernetes

✅ Expose using NodePort

✅ Configure DB URL using ConfigMap

---

# Day 6 — Persistent Storage + Ingress + Health Checks

# Topics

## 1. Persistent Volumes

Learn:

* PV
* PVC
* StorageClass

---

## 2. Stateful Applications

Deploy:

* MySQL in Kubernetes

---

## 3. Health Checks

Learn:

* Liveness probe
* Readiness probe

Spring Boot:

```text id="4jcck6"
/actuator/health
```

---

## 4. Ingress

Learn:

* Reverse proxy
* Routing
* TLS

Install:

* NGINX Ingress Controller

---

## 5. Resource Limits

Learn:

* CPU requests
* Memory limits
* OOMKilled

---

# Hands-On Task

✅ Add readiness/liveness probes

✅ Add ingress routing

✅ Persist MySQL data

---

# Day 7 — Production Basics + Mini Project

# Topics

## 1. Helm Basics

Learn:

* Helm charts
* values.yaml
* Templating

Commands:

```bash id="yjqhq4"
helm install
helm upgrade
```

---

## 2. Monitoring Basics

Learn:

* Prometheus
* Grafana

Understand:

* Metrics
* Alerts

---

## 3. Logging Basics

Learn:

* Centralized logging
* ELK/Loki overview

---

## 4. CI/CD Overview

Understand:

```text id="kl0dy7"
GitHub → Build → Docker → Push → Deploy
```

---

## 5. Kubernetes Debugging

Commands:

```bash id="2n18sj"
kubectl describe
kubectl logs
kubectl top
kubectl events
```

---

# Final Mini Project

Deploy complete stack:

## Components

* Spring Boot app
* MySQL
* Redis

## Features

* Dockerized
* Kubernetes deployment
* ConfigMap
* Secret
* Ingress
* Persistent storage

---

# Daily Time Split

## Theory

1.5–2 hrs

## Hands-On

3–4 hrs

---

# End-of-Week Skills

You should now know:

✅ Docker basics
✅ Dockerfile creation
✅ Docker Compose
✅ Kubernetes Pods
✅ Deployments
✅ Services
✅ ConfigMaps & Secrets
✅ Persistent storage
✅ Ingress
✅ Health checks
✅ Basic production deployment

---

# After This Week — Next Topics

## Week 2

Intermediate Kubernetes:

* Helm
* Autoscaling
* RBAC
* Network Policies

## Week 3

Production Kubernetes:

* Monitoring
* Logging
* GitOps
* ArgoCD

## Week 4

Advanced:

* Service Mesh
* Operators
* Kubernetes Internals
* Security Hardening
