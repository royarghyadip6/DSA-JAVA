# Day 7 — Production Basics + Mini Project

# 1. Introduction

Day 7 focuses on:

* Helm
* Monitoring
* Logging
* CI/CD
* Kubernetes debugging
* Production concepts

Goal:

```text id="m4q8vx"
Understand how real production Kubernetes systems operate
```

---

# 2. What is Helm?

Helm is:

```text id="u7m2qx"
Package manager for Kubernetes
```

Similar to:

* apt for Ubuntu
* npm for Node.js
* Maven for Java

Helm simplifies:

* Kubernetes deployments
* Reusable templates
* Configuration management

Official website:

[Helm Official Website](https://helm.sh/?utm_source=chatgpt.com)

---

# 3. Why Helm is Needed

Without Helm:

* Large YAML files
* Duplicate configurations
* Difficult environment management

Helm solves:

```text id="x1q6vz"
Reusable Kubernetes templates
```

---

# 4. What is a Helm Chart?

Helm chart is:

```text id="p5q9vx"
Collection of Kubernetes YAML templates
```

used to deploy applications.

Example:

* Spring Boot chart
* MySQL chart
* Redis chart

---

# 5. Helm Chart Structure

```text id="t2m7qz"
mychart/
 ├── Chart.yaml
 ├── values.yaml
 ├── templates/
 └── charts/
```

---

# 6. Important Helm Files

| File        | Purpose              |
|-------------|----------------------|
| Chart.yaml  | Chart metadata       |
| values.yaml | Configurable values  |
| templates   | Kubernetes templates |

---

# 7. values.yaml

Stores configurable values.

Example:

```yaml id="w6q1vx"
replicaCount: 3

image:
  repository: springboot-app
  tag: "1.0"
```

Avoid hardcoding values inside templates.

---

# 8. Helm Templating

Helm uses:

```text id="x9m4qw"
Go templating engine
```

Example:

```yaml id="u3q8vz"
replicas: {{ .Values.replicaCount }}
```

Reads value from:

```text id="m1v5qx"
values.yaml
```

---

# 9. Install Helm

Official installation guide:

[Helm Install Guide](https://helm.sh/docs/intro/install/?utm_source=chatgpt.com)

Verify:

```bash id="p7m2qw"
helm version
```

---

# 10. Create Helm Chart

```bash id="t4q9vx"
helm create springboot-chart
```

Generates default chart structure.

---

# 11. Install Helm Chart

```bash id="u8m3qx"
helm install myapp ./springboot-chart
```

Where:

* `myapp` = release name

---

# 12. Upgrade Helm Release

```bash id="p2q7vw"
helm upgrade myapp ./springboot-chart
```

Used after:

* Config changes
* Image updates

---

# 13. Uninstall Helm Release

```bash id="x5m1qz"
helm uninstall myapp
```

---

# 14. Why Helm is Powerful

Benefits:

* Reusable deployments
* Environment-specific configs
* Easier upgrades
* Version management

Widely used in:

* Production Kubernetes
* Cloud-native systems

---

# 15. Monitoring Basics

Monitoring means:

```text id="m9q4vx"
Collecting and analyzing system metrics
```

Production systems must monitor:

* CPU
* Memory
* Requests
* Errors
* Response times

---

# 16. What is Prometheus?

Prometheus is:

```text id="u1m8qw"
Metrics collection and monitoring system
```

Official website:

[Prometheus Official Website](https://prometheus.io/?utm_source=chatgpt.com)

---

# 17. What Metrics Mean

Metrics are:

```text id="p6q2vx"
Numerical measurements over time
```

Examples:

* CPU usage
* Memory usage
* Request count
* Error rate

---

# 18. Prometheus Workflow

```text id="t7m5qz"
Application
   ↓
Exports metrics
   ↓
Prometheus scrapes metrics
   ↓
Stores time-series data
```

---

# 19. Spring Boot Metrics

Spring Boot Actuator exposes metrics.

Endpoint:

```text id="x3q9vw"
/actuator/prometheus
```

Dependency:

```xml id="m4v1qx"
<dependency>
  <groupId>io.micrometer</groupId>
  <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

---

# 20. What is Grafana?

Grafana is:

```text id="u6q7vz"
Visualization dashboard tool
```

Displays:

* Charts
* Dashboards
* Monitoring panels

Official website:

[Grafana Official Website](https://grafana.com/?utm_source=chatgpt.com)

---

# 21. Prometheus + Grafana Architecture

```text id="p8m2qw"
Application
   ↓
Prometheus
   ↓
Grafana
   ↓
Dashboards
```

---

# 22. Alerts

Alerts notify when problems occur.

Examples:

* High CPU usage
* Pod crashes
* Low memory
* High error rate

---

# 23. Logging Basics

Logs help diagnose:

* Errors
* Failures
* Debugging issues

In Kubernetes:

```text id="x1v5qz"
Logs are distributed across pods
```

Centralized logging is important.

---

# 24. Centralized Logging

Instead of checking logs manually:

```text id="m7q3vx"
All logs collected centrally
```

Benefits:

* Easier debugging
* Searchable logs
* Historical analysis

---

# 25. ELK Stack

Popular logging stack:

| Component     | Purpose        |
|---------------|----------------|
| Elasticsearch | Stores logs    |
| Logstash      | Processes logs |
| Kibana        | Visualization  |

Architecture:

```text id="u4m9qw"
Pods
 ↓
Logstash
 ↓
Elasticsearch
 ↓
Kibana
```

---

# 26. Loki

Grafana Loki is lightweight logging system.

Works well with:
Grafana

Advantages:

* Lower storage usage
* Simpler architecture

Official website:

[Grafana Loki Official Website](https://grafana.com/oss/loki/?utm_source=chatgpt.com)

---

# 27. CI/CD Overview

CI/CD means:

| Term | Meaning                        |
|------|--------------------------------|
| CI   | Continuous Integration         |
| CD   | Continuous Deployment/Delivery |

Goal:

```text id="p2q6vz"
Automate build, test, and deployment
```

---

# 28. CI/CD Workflow

```text id="x8m1qx"
GitHub
   ↓
Build Application
   ↓
Run Tests
   ↓
Build Docker Image
   ↓
Push Image
   ↓
Deploy to Kubernetes
```

---

# 29. Common CI/CD Tools

| Tool           | Purpose           |
|----------------|-------------------|
| GitHub Actions | Automation        |
| Jenkins        | CI/CD pipelines   |
| GitLab CI      | DevOps pipelines  |
| ArgoCD         | GitOps deployment |

---

# 30. Example CI/CD Flow

Developer pushes code:

```text id="t5q7vw"
git push
```

Pipeline automatically:

* Builds JAR
* Builds Docker image
* Pushes to registry
* Updates Kubernetes deployment

---

# 31. Docker Registry in CI/CD

Images pushed to:

* Docker Hub
* AWS ECR
* Azure ACR
* GCR

Kubernetes pulls images from registry.

---

# 32. Kubernetes Debugging

Production debugging is critical.

Most common debugging tools:

* describe
* logs
* top
* events

---

# 33. kubectl describe

Detailed resource information.

Example:

```bash id="w9m2qx"
kubectl describe pod <pod-name>
```

Shows:

* Events
* Errors
* Scheduling issues
* Restart counts

---

# 34. kubectl logs

View container logs.

```bash id="u3q8vx"
kubectl logs <pod-name>
```

Multi-container pod:

```bash id="m1v4qz"
kubectl logs <pod-name> -c <container-name>
```

---

# 35. kubectl top

Shows resource usage.

```bash id="p7q9vw"
kubectl top pod
```

Displays:

* CPU usage
* Memory usage

Requires:

```text id="x5m2qx"
Metrics Server
```

---

# 36. kubectl events

View cluster events.

```bash id="t8m6qw"
kubectl get events
```

Useful for:

* Scheduling failures
* Image pull errors
* Node issues

---

# 37. Common Kubernetes Problems

---

# 37.1 CrashLoopBackOff

Container repeatedly crashing.

Debug:

```bash id="u2q1vx"
kubectl logs <pod-name>
```

---

# 37.2 ImagePullBackOff

Image cannot be downloaded.

Possible causes:

* Wrong image name
* Authentication issue
* Registry issue

---

# 37.3 OOMKilled

Container exceeded memory limit.

Check:

```bash id="m4v7qz"
kubectl describe pod
```

---

# 37.4 Pending Pods

Possible causes:

* Insufficient resources
* Storage issues
* Node unavailable

---

# 38. Mini Project Overview

Build complete system:

| Component        | Technology  |
|------------------|-------------|
| Backend          | Spring Boot |
| Database         | MySQL       |
| Cache            | Redis       |
| Containerization | Docker      |
| Orchestration    | Kubernetes  |

---

# 39. Mini Project Flow

```text id="p9m3qw"
Spring Boot
    ↓
Docker Image
    ↓
Docker Registry
    ↓
Kubernetes Deployment
    ↓
Service + Ingress
```

---

# 40. Mini Project Kubernetes Components

Create:

* Deployment
* Service
* ConfigMap
* Secret
* PVC
* Ingress

---

# 41. Production Best Practices

---

# 41.1 Always Use Health Checks

Critical for:

* Auto-healing
* Stable deployments

---

# 41.2 Set Resource Limits

Prevents:

* Node crashes
* Resource starvation

---

# 41.3 Use Centralized Logging

Essential for:

* Debugging
* Production monitoring

---

# 41.4 Monitor Everything

Track:

* CPU
* Memory
* Errors
* Response time
* Traffic

---

# 41.5 Use CI/CD Pipelines

Avoid manual deployments.

Benefits:

* Faster releases
* Fewer human errors
* Automated rollback

---

# 42. Real Production Kubernetes Stack

Typical production setup:

```text id="x6q8vz"
Application
   ↓
Ingress
   ↓
Services
   ↓
Pods
   ↓
PVC
   ↓
Database
```

Monitoring:

```text id="u5m1qx"
Prometheus + Grafana
```

Logging:

```text id="t3q7vw"
ELK / Loki
```

Deployment:

```text id="m8v2qz"
Helm + CI/CD
```

---

# 43. Final Learning Summary

Over 7 days you learned:

✅ Docker fundamentals
✅ Docker networking
✅ Docker volumes
✅ Docker Compose
✅ Kubernetes architecture
✅ Pods and deployments
✅ Services and networking
✅ ConfigMaps and Secrets
✅ Persistent storage
✅ Ingress and routing
✅ Health probes
✅ Resource limits
✅ Helm
✅ Monitoring
✅ Logging
✅ CI/CD basics
✅ Kubernetes debugging

---

# 44. Next Learning Path

After these fundamentals, continue with:

| Advanced Topic   | Importance             |
|------------------|------------------------|
| StatefulSets     | Databases              |
| DaemonSets       | Node agents            |
| RBAC             | Security               |
| Network Policies | Security               |
| HPA              | Auto-scaling           |
| ArgoCD           | GitOps                 |
| Service Mesh     | Advanced networking    |
| EKS/GKE/AKS      | Cloud Kubernetes       |
| Terraform        | Infrastructure as Code |

---

# 45. Recommended Mini Projects

Build:

* E-commerce backend
* Banking microservices
* URL shortener
* Chat application
* Real-time notification system

Deploy using:

* Docker
* Kubernetes
* Helm
* CI/CD pipeline

This completes your:

```text id="x1q5vw"
Docker + Kubernetes Beginner-to-Production Roadmap
```
