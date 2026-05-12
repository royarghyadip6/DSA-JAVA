# Day 5 — Deployments + Services + ConfigMaps

# 1. Introduction

Day 5 focuses on:

* Deployments
* Scaling applications
* Services
* ConfigMaps
* Secrets
* Environment variables

Goal:

```text id="m4q8vx"
Deploy production-style Spring Boot application in Kubernetes
```

---

# 2. Why Deployments are Needed

Pods are:

```text id="u7m2qx"
Temporary and replaceable
```

Problems with standalone pods:

* No auto-healing
* No scaling
* No rolling updates
* Manual management

Kubernetes solves this using:

```text id="x1q6vz"
Deployments
```

---

# 3. What is a Deployment?

Deployment is a Kubernetes resource that manages:

* Pods
* ReplicaSets
* Scaling
* Updates

Deployment ensures:

```text id="p5q9vx"
Desired number of pods always running
```

---

# 4. Deployment Architecture

Flow:

```text id="t2m7qz"
Deployment
    ↓
ReplicaSet
    ↓
Pods
```

---

# 5. What is a ReplicaSet?

ReplicaSet ensures:

```text id="w6q1vx"
Specific number of pod replicas are running
```

Example:

```text id="x9m4qw"
replicas = 3
```

If one pod crashes:

```text id="u3q8vz"
ReplicaSet recreates it automatically
```

---

# 6. Deployment YAML Example

```yaml id="m1v5qx"
apiVersion: apps/v1
kind: Deployment

metadata:
  name: springboot-app

spec:
  replicas: 3

  selector:
    matchLabels:
      app: springboot

  template:
    metadata:
      labels:
        app: springboot

    spec:
      containers:
        - name: springboot
          image: springboot-app:1.0
          ports:
            - containerPort: 8080
```

---

# 7. Important Deployment Fields

| Field      | Purpose                |
|------------|------------------------|
| replicas   | Number of pods         |
| selector   | Pod matching labels    |
| template   | Pod template           |
| containers | Application containers |

---

# 8. Create Deployment

Save YAML as:

```text id="p7m2qw"
deployment.yaml
```

Apply:

```bash id="t4q9vx"
kubectl apply -f deployment.yaml
```

---

# 9. View Deployments

```bash id="u8m3qx"
kubectl get deployments
```

---

# 10. View ReplicaSets

```bash id="p2q7vw"
kubectl get rs
```

`rs` means:

```text id="x5m1qz"
ReplicaSet
```

---

# 11. View Pods

```bash id="m9q4vx"
kubectl get pods
```

You should see:

```text id="u1m8qw"
3 running pods
```

---

# 12. Scaling Deployments

Increase pod count dynamically.

---

# 12.1 Scale Up

```bash id="p6q2vx"
kubectl scale deployment springboot-app --replicas=5
```

---

# 12.2 Scale Down

```bash id="t7m5qz"
kubectl scale deployment springboot-app --replicas=2
```

---

# 13. Rolling Updates

Deployments support:

```text id="x3q9vw"
Zero-downtime updates
```

Kubernetes gradually replaces old pods with new ones.

---

# 14. Update Deployment Image

```bash id="m4v1qx"
kubectl set image deployment/springboot-app \
springboot=springboot-app:2.0
```

---

# 15. Rollout Status

```bash id="u6q7vz"
kubectl rollout status deployment springboot-app
```

---

# 16. Rollout History

```bash id="p8m2qw"
kubectl rollout history deployment springboot-app
```

Shows deployment revisions.

---

# 17. Rollback Deployment

If new version fails:

```bash id="x1v5qz"
kubectl rollout undo deployment springboot-app
```

Kubernetes restores previous version.

---

# 18. What is a Service?

Pods have dynamic IPs.

Problem:

```text id="m7q3vx"
Pod IP changes frequently
```

Solution:

```text id="u4m9qw"
Service
```

Service provides:

```text id="p2q6vz"
Stable networking endpoint
```

---

# 19. Service Architecture

```text id="x8m1qx"
Client
   ↓
Service
   ↓
Pods
```

Service load-balances traffic across pods.

---

# 20. Types of Services

| Type         | Purpose                |
|--------------|------------------------|
| ClusterIP    | Internal communication |
| NodePort     | External access        |
| LoadBalancer | Cloud load balancer    |

---

# 21. ClusterIP Service

Default service type.

Accessible:

```text id="t5q7vw"
Only inside cluster
```

Example:

```yaml id="w9m2qx"
type: ClusterIP
```

Used for:

* Internal microservices
* Database communication

---

# 22. NodePort Service

Exposes application externally using node port.

Example:

```yaml id="u3q8vx"
type: NodePort
```

Access:

```text id="m1v4qz"
<NodeIP>:NodePort
```

---

# 23. LoadBalancer Service

Used in cloud environments.

Cloud provider creates:

```text id="p7q9vw"
External load balancer
```

Used in:

* AWS
* Azure
* GCP

---

# 24. NodePort Service YAML

```yaml id="x5m2qx"
apiVersion: v1
kind: Service

metadata:
  name: springboot-service

spec:
  type: NodePort

  selector:
    app: springboot

  ports:
    - port: 8080
      targetPort: 8080
      nodePort: 30080
```

---

# 25. Important Service Fields

| Field      | Purpose            |
|------------|--------------------|
| port       | Service port       |
| targetPort | Container port     |
| nodePort   | External node port |

---

# 26. Create Service

```bash id="t8m6qw"
kubectl apply -f service.yaml
```

---

# 27. View Services

```bash id="u2q1vx"
kubectl get svc
```

`svc` means:

```text id="m4v7qz"
Service
```

---

# 28. ConfigMaps

ConfigMaps store:

```text id="p9m3qw"
Externalized application configurations
```

Examples:

* DB URL
* API endpoints
* Environment settings

---

# 29. Why ConfigMaps?

Avoid hardcoding configs inside:

* Application
* Docker image

Improves:

* Flexibility
* Reusability

---

# 30. ConfigMap YAML

```yaml id="x6q8vz"
apiVersion: v1
kind: ConfigMap

metadata:
  name: db-config

data:
  DB_HOST: mysql
  DB_PORT: "3306"
```

---

# 31. Create ConfigMap

```bash id="u5m1qx"
kubectl apply -f configmap.yaml
```

---

# 32. View ConfigMaps

```bash id="t3q7vw"
kubectl get configmaps
```

---

# 33. What are Secrets?

Secrets store:

```text id="m8v2qz"
Sensitive data
```

Examples:

* Passwords
* API keys
* Tokens

---

# 34. Secret Encoding

Secrets use:

```text id="x1q5vw"
Base64 encoding
```

Important:

```text id="u9m4qx"
Base64 is NOT encryption
```

It is only encoding.

---

# 35. Generate Base64

Linux:

```bash id="p6q8vz"
echo -n "root" | base64
```

Example output:

```text id="w2m7qw"
cm9vdA==
```

---

# 36. Secret YAML

```yaml id="u7q3vx"
apiVersion: v1
kind: Secret

metadata:
  name: db-secret

type: Opaque

data:
  DB_PASSWORD: cm9vdA==
```

---

# 37. Create Secret

```bash id="m5q2vz"
kubectl apply -f secret.yaml
```

---

# 38. View Secrets

```bash id="u8m7qx"
kubectl get secrets
```

---

# 39. Environment Variables in Kubernetes

Environment variables passed using:

```yaml id="p2q4vw"
env:
```

---

# 40. Inject ConfigMap Values

Example:

```yaml id="x5m1qz"
env:
  - name: DB_HOST
    valueFrom:
      configMapKeyRef:
        name: db-config
        key: DB_HOST
```

---

# 41. Inject Secret Values

Example:

```yaml id="m7v4qx"
env:
  - name: DB_PASSWORD
    valueFrom:
      secretKeyRef:
        name: db-secret
        key: DB_PASSWORD
```

---

# 42. Full Spring Boot Deployment Example

```yaml id="t2q7vw"
apiVersion: apps/v1
kind: Deployment

metadata:
  name: springboot-app

spec:
  replicas: 2

  selector:
    matchLabels:
      app: springboot

  template:
    metadata:
      labels:
        app: springboot

    spec:
      containers:
        - name: app
          image: springboot-app:1.0

          ports:
            - containerPort: 8080

          env:
            - name: DB_HOST
              valueFrom:
                configMapKeyRef:
                  name: db-config
                  key: DB_HOST
```

---

# 43. Common Problems

---

# 43.1 Pods Not Updating

Check rollout:

```bash id="u6m3qx"
kubectl rollout status deployment springboot-app
```

---

# 43.2 Service Not Accessible

Check:

* Labels
* Selectors
* Ports

---

# 43.3 CrashLoopBackOff

Check logs:

```bash id="p8m1qz"
kubectl logs <pod-name>
```

---

# 43.4 Wrong ConfigMap Key

Application may fail if:

```text id="w4q6vx"
Config key name mismatch
```

---

# 44. Hands-On Tasks

---

# Task 1 — Deploy Spring Boot App

Use:

* Deployment YAML
* Service YAML

---

# Task 2 — Expose Using NodePort

Access app using:

```text id="x1m9qw"
<NodeIP>:30080
```

---

# Task 3 — Create ConfigMap

Store:

* DB_HOST
* DB_PORT

---

# Task 4 — Inject Environment Variables

Use:

```yaml id="m5q2vz"
env:
```

---

# Task 5 — Test Scaling

```bash id="u8m7qx"
kubectl scale deployment springboot-app --replicas=5
```

---

# 45. Best Practices

---

# 45.1 Use Deployments Instead of Pods

Provides:

* Auto-healing
* Scaling
* Rolling updates

---

# 45.2 Keep Secrets Outside Code

Never hardcode:

* Passwords
* Tokens

---

# 45.3 Use Labels Properly

Services depend on labels/selectors.

---

# 45.4 Use ConfigMaps for Externalized Configurations

Avoid rebuilding images for config changes.

---

# 46. Summary

Today you learned:

✅ Deployments
✅ ReplicaSets
✅ Scaling
✅ Rolling updates
✅ Rollbacks
✅ Services
✅ ClusterIP
✅ NodePort
✅ LoadBalancer
✅ ConfigMaps
✅ Secrets
✅ Base64 encoding
✅ Environment variables in Kubernetes
✅ Spring Boot deployment in Kubernetes

These concepts are foundational for:

* Production Kubernetes
* Microservices
* CI/CD pipelines
* Cloud-native architecture
