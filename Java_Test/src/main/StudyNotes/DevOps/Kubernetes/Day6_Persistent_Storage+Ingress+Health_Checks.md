# Day 6 — Persistent Storage + Ingress + Health Checks

# 1. Introduction

Day 6 focuses on:

* Persistent storage
* Stateful applications
* Health checks
* Ingress
* Resource limits

Goal:

```text id="m4q8vx"
Run production-ready Kubernetes applications
```

---

# 2. Why Persistent Storage is Needed

Containers and pods are:

```text id="u7m2qx"
Ephemeral
```

Meaning:

```text id="x1q6vz"
Data is lost when pod is deleted
```

Problem for:

* MySQL
* PostgreSQL
* MongoDB

Databases require:

```text id="p5q9vx"
Persistent storage
```

---

# 3. Kubernetes Storage Architecture

Kubernetes storage flow:

```text id="t2m7qz"
Pod
 ↓
PVC
 ↓
PV
 ↓
Physical Storage
```

---

# 4. Persistent Volume (PV)

PV is:

```text id="w6q1vx"
Cluster storage resource
```

managed by Kubernetes.

Represents actual storage:

* Disk
* SSD
* Cloud volume
* NFS

---

# 5. Persistent Volume Claim (PVC)

PVC is:

```text id="x9m4qw"
Request for storage
```

by applications.

Pod uses PVC instead of directly using PV.

---

# 6. StorageClass

StorageClass enables:

```text id="u3q8vz"
Dynamic storage provisioning
```

Automatically creates PVs.

Used heavily in cloud environments:

* AWS EBS
* Azure Disk
* GCP Persistent Disk

---

# 7. Persistent Volume YAML

```yaml id="m1v5qx"
apiVersion: v1
kind: PersistentVolume

metadata:
  name: mysql-pv

spec:
  capacity:
    storage: 1Gi

  accessModes:
    - ReadWriteOnce

  hostPath:
    path: /data/mysql
```

---

# 8. Important PV Fields

| Field       | Purpose            |
|-------------|--------------------|
| capacity    | Storage size       |
| accessModes | Access permissions |
| hostPath    | Storage location   |

---

# 9. Access Modes

| Mode          | Meaning             |
|---------------|---------------------|
| ReadWriteOnce | Mounted by one node |
| ReadOnlyMany  | Multiple read-only  |
| ReadWriteMany | Multiple read-write |

Most common:

```text id="p7m2qw"
ReadWriteOnce
```

---

# 10. Persistent Volume Claim YAML

```yaml id="t4q9vx"
apiVersion: v1
kind: PersistentVolumeClaim

metadata:
  name: mysql-pvc

spec:
  accessModes:
    - ReadWriteOnce

  resources:
    requests:
      storage: 1Gi
```

---

# 11. Create PV & PVC

```bash id="u8m3qx"
kubectl apply -f pv.yaml
kubectl apply -f pvc.yaml
```

---

# 12. Verify PV & PVC

---

# 12.1 View PV

```bash id="p2q7vw"
kubectl get pv
```

---

# 12.2 View PVC

```bash id="x5m1qz"
kubectl get pvc
```

---

# 13. Using PVC in Pod

Example:

```yaml id="m9q4vx"
volumes:
  - name: mysql-storage
    persistentVolumeClaim:
      claimName: mysql-pvc
```

Mount inside container:

```yaml id="u1m8qw"
volumeMounts:
  - mountPath: /var/lib/mysql
    name: mysql-storage
```

---

# 14. Stateful Applications

Stateful applications store:

```text id="p6q2vx"
Persistent data
```

Examples:

* MySQL
* PostgreSQL
* MongoDB
* Kafka

---

# 15. Why Stateful Apps are Special

Stateful apps require:

* Persistent storage
* Stable identity
* Ordered startup/shutdown

Unlike stateless apps:

```text id="t7m5qz"
Data cannot be lost
```

---

# 16. MySQL Deployment Example

```yaml id="x3q9vw"
apiVersion: apps/v1
kind: Deployment

metadata:
  name: mysql

spec:
  replicas: 1

  selector:
    matchLabels:
      app: mysql

  template:
    metadata:
      labels:
        app: mysql

    spec:
      containers:
        - name: mysql
          image: mysql:8

          env:
            - name: MYSQL_ROOT_PASSWORD
              value: root

          volumeMounts:
            - mountPath: /var/lib/mysql
              name: mysql-storage

      volumes:
        - name: mysql-storage
          persistentVolumeClaim:
            claimName: mysql-pvc
```

---

# 17. Health Checks

Kubernetes monitors application health using:

* Liveness probe
* Readiness probe

---

# 18. Why Health Checks Matter

Without health checks:

```text id="m4v1qx"
Kubernetes cannot detect broken applications
```

Applications may:

* Hang
* Stop responding
* Fail partially

---

# 19. Liveness Probe

Liveness probe checks:

```text id="u6q7vz"
Is application alive?
```

If failed:

```text id="p8m2qw"
Kubernetes restarts container
```

---

# 20. Readiness Probe

Readiness probe checks:

```text id="x1v5qz"
Is application ready to serve traffic?
```

If failed:

```text id="m7q3vx"
Traffic is NOT sent to pod
```

Container still runs.

---

# 21. Spring Boot Health Endpoint

Using Spring Boot Actuator:

```text id="u4m9qw"
/actuator/health
```

Dependency:

```xml id="p2q6vz"
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

---

# 22. Liveness Probe Example

```yaml id="x8m1qx"
livenessProbe:
  httpGet:
    path: /actuator/health
    port: 8080

  initialDelaySeconds: 30
  periodSeconds: 10
```

---

# 23. Readiness Probe Example

```yaml id="t5q7vw"
readinessProbe:
  httpGet:
    path: /actuator/health
    port: 8080

  initialDelaySeconds: 20
  periodSeconds: 5
```

---

# 24. Important Probe Fields

| Field               | Purpose                 |
|---------------------|-------------------------|
| initialDelaySeconds | Wait before first check |
| periodSeconds       | Check interval          |
| timeoutSeconds      | Probe timeout           |

---

# 25. Common Health Check Problems

---

# 25.1 App Starts Slowly

Problem:

```text id="w9m2qx"
Probe fails before app starts
```

Fix:

```text id="u3q8vx"
Increase initialDelaySeconds
```

---

# 25.2 Wrong Endpoint

Incorrect path causes:

```text id="m1v4qz"
Continuous restarts
```

---

# 26. Ingress

Ingress provides:

```text id="p7q9vw"
HTTP/HTTPS routing into cluster
```

Acts like:

```text id="x5m2qx"
Reverse proxy
```

---

# 27. Why Ingress is Needed

Without ingress:

* Each service needs NodePort/LoadBalancer
* Hard to manage routing

Ingress provides:

* Centralized routing
* TLS termination
* Domain-based access

---

# 28. Ingress Architecture

```text id="t8m6qw"
Internet
   ↓
Ingress Controller
   ↓
Ingress Rules
   ↓
Services
   ↓
Pods
```

---

# 29. What is Reverse Proxy?

Reverse proxy:

```text id="u2q1vx"
Receives client requests and forwards internally
```

Examples:

* NGINX
* HAProxy
* Traefik

---

# 30. NGINX Ingress Controller

Most popular Kubernetes ingress controller.

Official website:

[NGINX Ingress Controller Documentation](https://kubernetes.github.io/ingress-nginx/?utm_source=chatgpt.com)

---

# 31. Install NGINX Ingress Controller

Example:

```bash id="m4v7qz"
kubectl apply -f \
https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/cloud/deploy.yaml
```

---

# 32. Ingress YAML Example

```yaml id="p9m3qw"
apiVersion: networking.k8s.io/v1
kind: Ingress

metadata:
  name: app-ingress

spec:
  rules:
    - host: myapp.local

      http:
        paths:
          - path: /
            pathType: Prefix

            backend:
              service:
                name: springboot-service

                port:
                  number: 8080
```

---

# 33. Routing Example

```text id="x6q8vz"
myapp.local
      ↓
Ingress
      ↓
springboot-service
      ↓
Pods
```

---

# 34. TLS in Ingress

Ingress supports:

```text id="u5m1qx"
HTTPS/TLS termination
```

Example:

```yaml id="t3q7vw"
tls:
  - hosts:
      - myapp.local
```

---

# 35. Resource Limits

Kubernetes controls resource usage using:

* Requests
* Limits

---

# 36. CPU Requests

Minimum CPU guaranteed.

Example:

```yaml id="m8v2qz"
resources:
  requests:
    cpu: "200m"
```

`200m` means:

```text id="x1q5vw"
0.2 CPU core
```

---

# 37. Memory Limits

Maximum memory allowed.

Example:

```yaml id="u9m4qx"
resources:
  limits:
    memory: "512Mi"
```

---

# 38. Why Resource Limits Matter

Without limits:

```text id="p6q8vz"
One pod can consume entire node resources
```

Causing cluster instability.

---

# 39. OOMKilled

OOMKilled means:

```text id="w2m7qw"
Out Of Memory kill
```

Occurs when pod exceeds memory limit.

Check using:

```bash id="u7q3vx"
kubectl describe pod
```

---

# 40. Resource Example

```yaml id="m5q2vz"
resources:
  requests:
    cpu: "200m"
    memory: "256Mi"

  limits:
    cpu: "500m"
    memory: "512Mi"
```

---

# 41. Common Problems

---

# 41.1 PVC Pending

Possible causes:

* No matching PV
* StorageClass issue

---

# 41.2 Probe Failures

Check:

```bash id="u8m7qx"
kubectl describe pod
```

---

# 41.3 OOMKilled

Increase:

```text id="p2q4vw"
memory limits
```

---

# 41.4 Ingress Not Working

Check:

* Ingress controller installed
* DNS mapping
* Service ports

---

# 42. Hands-On Tasks

---

# Task 1 — Persist MySQL Data

Create:

* PV
* PVC

Mount into MySQL pod.

---

# Task 2 — Add Readiness Probe

Use:

```text id="x5m1qz"
/actuator/health
```

---

# Task 3 — Add Liveness Probe

Verify auto-restart behavior.

---

# Task 4 — Install Ingress Controller

Deploy:

```text id="m7v4qx"
NGINX Ingress Controller
```

---

# Task 5 — Create Ingress Routing

Route:

```text id="t2q7vw"
myapp.local → springboot-service
```

---

# 43. Best Practices

---

# 43.1 Always Use Health Checks

Critical for:

* Production stability
* Auto-healing

---

# 43.2 Set Resource Limits

Prevents:

* Node exhaustion
* Unstable clusters

---

# 43.3 Use PVC for Databases

Never store DB data inside container filesystem.

---

# 43.4 Use Ingress Instead of Multiple NodePorts

Better:

* Scalability
* Routing
* TLS support

---

# 44. Summary

Today you learned:

✅ Persistent Volumes
✅ Persistent Volume Claims
✅ StorageClass
✅ Stateful applications
✅ MySQL persistent storage
✅ Liveness probes
✅ Readiness probes
✅ Spring Boot Actuator health checks
✅ Ingress
✅ Reverse proxy
✅ TLS routing
✅ NGINX Ingress Controller
✅ CPU requests
✅ Memory limits
✅ OOMKilled debugging

These concepts are foundational for:

* Production Kubernetes
* Cloud-native deployments
* High availability systems
* Enterprise microservices
