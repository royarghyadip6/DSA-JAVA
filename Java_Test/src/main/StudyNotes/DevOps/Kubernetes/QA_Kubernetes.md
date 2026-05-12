# Kubernetes Interview Questions (MCQ + Long Questions)

---

# 1. What is Kubernetes?

<details>
<summary>Answer</summary>

Kubernetes is a container orchestration platform used to:

* Deploy containers
* Scale applications
* Manage networking
* Perform auto-healing

It automates container lifecycle management.

</details>

---

# 2. Why is Kubernetes needed if Docker already exists?

<details>
<summary>Answer</summary>

Docker handles:

```text id="a1k7qx"
Single container management
```

Kubernetes handles:

* Scaling
* Auto-healing
* Load balancing
* Rolling updates
* Service discovery
* Multi-node orchestration

Docker alone becomes difficult in large-scale production systems.

</details>

---

# 3. Which Kubernetes component stores cluster state?

## MCQ

A. kubelet
B. etcd
C. kube-proxy
D. Scheduler

<details>
<summary>Answer</summary>

✅ Correct Answer: B. etcd

etcd is distributed key-value database storing:

* Cluster configuration
* Desired state
* Metadata

</details>

---

# 4. Explain Kubernetes Architecture

<details>
<summary>Answer</summary>

Architecture:

```text id="n4q8vx"
Master Node(Control Plane)
    ↓
Worker Nodes
```

Control Plane Components:

* API Server
* Scheduler
* Controller Manager
* etcd

Worker Components:

* kubelet
* kube-proxy
* Container runtime

</details>

---

# 5. What is a Pod?

<details>
<summary>Answer</summary>

Pod is:

```text id="u7m2qx"
Smallest deployable unit in Kubernetes
```

A pod contains:

* One or more containers
* Shared network
* Shared storage

Usually:

```text id="x1q6vz"
One application per pod
```

</details>

---

# 6. Which command lists pods?

## MCQ

A. kubectl pods
B. kubectl get pods
C. kubectl show pods
D. kubectl list pods

<details>
<summary>Answer</summary>

✅ Correct Answer: B. kubectl get pods

</details>

---

# 7. Difference Between Pod and Container

<details>
<summary>Answer</summary>

| Container               | Pod                       |
|-------------------------|---------------------------|
| Single runtime instance | Wrapper around containers |
| Docker concept          | Kubernetes concept        |
| Independent             | Managed by Kubernetes     |

Pod provides:

* Networking
* Storage
* Lifecycle management

</details>

---

# 8. What is kubelet?

<details>
<summary>Answer</summary>

kubelet is:

```text id="p5q9vx"
Node agent running on worker node
```

Responsibilities:

* Communicates with API server
* Starts containers
* Monitors pod health

</details>

---

# 9. Which component decides where pods run?

## MCQ

A. kube-proxy
B. etcd
C. Scheduler
D. kubelet

<details>
<summary>Answer</summary>

✅ Correct Answer: C. Scheduler

Scheduler selects best node based on:

* CPU
* Memory
* Constraints
* Policies

</details>

---

# 10. What is a Deployment?

<details>
<summary>Answer</summary>

Deployment manages:

* Pods
* ReplicaSets
* Scaling
* Rolling updates

Ensures:

```text id="t2m7qz"
Desired number of pods always running
```

</details>

---

# 11. What is ReplicaSet?

<details>
<summary>Answer</summary>

ReplicaSet ensures:

```text id="w6q1vx"
Specific number of pod replicas are running
```

If pod crashes:

* ReplicaSet recreates pod automatically

</details>

---

# 12. Which command scales deployment?

## MCQ

A. kubectl expand
B. kubectl replicas
C. kubectl scale
D. kubectl resize

<details>
<summary>Answer</summary>

✅ Correct Answer: C. kubectl scale

Example:

```bash id="n0p3we"
kubectl scale deployment app --replicas=5
```

</details>

---

# 13. Explain Rolling Updates

<details>
<summary>Answer</summary>

Rolling update:

```text id="x9m4qw"
Gradually replaces old pods with new pods
```

Benefits:

* Zero downtime
* Safer deployments
* Automatic rollback capability

</details>

---

# 14. Which command rolls back deployment?

## MCQ

A. kubectl rollback
B. kubectl rollout undo
C. kubectl revert
D. kubectl restore

<details>
<summary>Answer</summary>

✅ Correct Answer: B. kubectl rollout undo

</details>

---

# 15. What is a Service in Kubernetes?

<details>
<summary>Answer</summary>

Service provides:

```text id="u3q8vz"
Stable network endpoint for pods
```

Because:

```text id="m1v5qx"
Pod IPs change frequently
```

Service enables:

* Stable access
* Load balancing
* Service discovery

</details>

---

# 16. Types of Kubernetes Services

<details>
<summary>Answer</summary>

| Type         | Purpose             |
|--------------|---------------------|
| ClusterIP    | Internal access     |
| NodePort     | External access     |
| LoadBalancer | Cloud load balancer |

</details>

---

# 17. Default Kubernetes service type?

## MCQ

A. NodePort
B. LoadBalancer
C. ClusterIP
D. ExternalIP

<details>
<summary>Answer</summary>

✅ Correct Answer: C. ClusterIP

</details>

---

# 18. Explain ConfigMaps

<details>
<summary>Answer</summary>

ConfigMaps store:

```text id="p7m2qw"
Externalized application configuration
```

Examples:

* DB URL
* API endpoints
* Environment configs

Benefits:

* No hardcoding
* Easier configuration changes

</details>

---

# 19. What are Kubernetes Secrets?

<details>
<summary>Answer</summary>

Secrets store:

```text id="t4q9vx"
Sensitive information
```

Examples:

* Passwords
* Tokens
* API keys

Encoded using:

```text id="u8m3qx"
Base64
```

</details>

---

# 20. Which command views logs?

## MCQ

A. kubectl events
B. kubectl logs
C. kubectl inspect
D. kubectl status

<details>
<summary>Answer</summary>

✅ Correct Answer: B. kubectl logs

</details>

---

# 21. Explain Liveness Probe

<details>
<summary>Answer</summary>

Liveness probe checks:

```text id="p2q7vw"
Is application alive?
```

If failed:

* Kubernetes restarts container

Used for:

* Deadlocks
* Hung applications

</details>

---

# 22. Explain Readiness Probe

<details>
<summary>Answer</summary>

Readiness probe checks:

```text id="x5m1qz"
Is application ready for traffic?
```

If failed:

* Pod removed from service endpoints
* Traffic not sent

Container still runs.

</details>

---

# 23. Which Spring Boot endpoint commonly used for probes?

## MCQ

A. /health
B. /ready
C. /actuator/health
D. /status

<details>
<summary>Answer</summary>

✅ Correct Answer: C. /actuator/health

Requires:

```xml id="m9q4vx"
spring-boot-starter-actuator
```

</details>

---

# 24. What is Ingress?

<details>
<summary>Answer</summary>

Ingress provides:

```text id="u1m8qw"
HTTP/HTTPS routing into Kubernetes cluster
```

Features:

* Reverse proxy
* TLS termination
* Path routing
* Domain routing

</details>

---

# 25. Explain Persistent Volume (PV)

<details>
<summary>Answer</summary>

Persistent Volume is:

```text id="p6q2vx"
Cluster storage resource
```

used for persistent data storage.

Examples:

* SSD
* Cloud disk
* NFS storage

</details>

---

# 26. What is PVC?

## MCQ

A. Pod Virtual Config
B. Persistent Volume Claim
C. Persistent Virtual Container
D. Pod Volume Control

<details>
<summary>Answer</summary>

✅ Correct Answer: B. Persistent Volume Claim

PVC requests storage from PV.

</details>

---

# 27. Why Stateful Applications Need Persistent Storage?

<details>
<summary>Answer</summary>

Containers are ephemeral.

Without persistent storage:

```text id="t7m5qz"
Database data lost after pod restart
```

Stateful apps:

* MySQL
* PostgreSQL
* MongoDB

must use PVC.

</details>

---

# 28. Explain Resource Requests and Limits

<details>
<summary>Answer</summary>

Requests:

```text id="x3q9vw"
Minimum guaranteed resources
```

Limits:

```text id="m4v1qx"
Maximum allowed resources
```

Example:

```yaml id="u6q7vz"
resources:
  requests:
    cpu: "200m"

  limits:
    memory: "512Mi"
```

</details>

---

# 29. What is OOMKilled?

<details>
<summary>Answer</summary>

OOMKilled means:

```text id="p8m2qw"
Out Of Memory Kill
```

Container exceeded memory limit.

Linux kernel terminates process.

</details>

---

# 30. Which command shows detailed pod info?

## MCQ

A. kubectl info
B. kubectl inspect
C. kubectl describe
D. kubectl show

<details>
<summary>Answer</summary>

✅ Correct Answer: C. kubectl describe

Example:

```bash id="x1v5qz"
kubectl describe pod mypod
```

</details>

---

# 31. Explain Kubernetes Auto-Healing

<details>
<summary>Answer</summary>

Kubernetes automatically:

* Restarts failed containers
* Recreates failed pods
* Maintains replica count

Using:

* Deployments
* ReplicaSets
* Health probes

</details>

---

# 32. Difference Between Deployment and StatefulSet

<details>
<summary>Answer</summary>

| Deployment       | StatefulSet       |
|------------------|-------------------|
| Stateless apps   | Stateful apps     |
| Random pod names | Stable identities |
| Web apps         | Databases         |

StatefulSet used for:

* MySQL
* Kafka
* MongoDB

</details>

---

# 33. What is Helm?

<details>
<summary>Answer</summary>

Helm is Kubernetes package manager.

Helm uses:

```text id="m7q3vx"
Charts
```

to simplify deployments.

Benefits:

* Reusable templates
* Easier upgrades
* Environment configurations

</details>

---

# 34. Which command installs Helm chart?

## MCQ

A. helm deploy
B. helm install
C. helm run
D. helm apply

<details>
<summary>Answer</summary>

✅ Correct Answer: B. helm install

</details>

---

# 35. Explain Kubernetes Networking

<details>
<summary>Answer</summary>

Kubernetes networking allows:

* Pod-to-pod communication
* Service communication
* External access

Rules:

* Every pod gets unique IP
* Pods communicate directly

</details>

---

# 36. What is kube-proxy?

<details>
<summary>Answer</summary>

kube-proxy handles:

```text id="u4m9qw"
Network routing and service forwarding
```

Responsibilities:

* Load balancing
* iptables rules
* Service networking

</details>

---

# 37. What happens when a Pod crashes?

<details>
<summary>Answer</summary>

Flow:

1. kubelet detects failure
2. ReplicaSet notices missing replica
3. New pod created automatically

This is:

```text id="p2q6vz"
Self-healing
```

</details>

---

# 38. Explain Kubernetes CI/CD Flow

<details>
<summary>Answer</summary>

Typical workflow:

```text id="x8m1qx"
GitHub
   ↓
Build JAR
   ↓
Build Docker Image
   ↓
Push Registry
   ↓
Deploy Kubernetes
```

Automation tools:

* Jenkins
* GitHub Actions
* GitLab CI
* ArgoCD

</details>

---

# 39. Which command shows resource usage?

## MCQ

A. kubectl metrics
B. kubectl top
C. kubectl stats
D. kubectl usage

<details>
<summary>Answer</summary>

✅ Correct Answer: B. kubectl top

Example:

```bash id="t5q7vw"
kubectl top pod
```

</details>

---

# 40. Explain Real Production Kubernetes Best Practices

<details>
<summary>Answer</summary>

Best practices:

* Use readiness/liveness probes
* Set resource limits
* Use ConfigMaps & Secrets
* Centralized logging
* Monitoring with Prometheus/Grafana
* Use Helm
* Use Ingress
* Use CI/CD
* Avoid hardcoded configs
* Enable RBAC

</details>

---

---

# Product-Based & Company-Oriented Kubernetes Interview Questions

---

# 1. Your Spring Boot application works locally but fails in Kubernetes. How will you debug?

<details>
<summary>Answer</summary>

Step-by-step debugging:

1. Check pod status

```bash id="7mdm5k"
kubectl get pods
```

2. Describe pod

```bash id="q3l5pw"
kubectl describe pod <pod-name>
```

3. Check logs

```bash id="r7x1qk"
kubectl logs <pod-name>
```

4. Enter container

```bash id="u2k9vz"
kubectl exec -it <pod-name> -- sh
```

5. Verify:

* Environment variables
* DB connectivity
* Application ports
* Memory usage
* ConfigMaps/Secrets

Common real-world causes:

* Wrong image
* CrashLoopBackOff
* Missing ConfigMap
* DB inaccessible
* Wrong service port

</details>

---

# 2. Which command is most important for Kubernetes debugging?

## MCQ

A. kubectl scale
B. kubectl logs
C. kubectl expose
D. kubectl create

<details>
<summary>Answer</summary>

✅ Correct Answer: B. kubectl logs

Logs are the first place to investigate failures.

</details>

---

# 3. Your application is getting traffic before startup completes. How will you fix it?

<details>
<summary>Answer</summary>

Use:

```text id="n4p2qz"
Readiness Probe
```

Readiness probe ensures:

* Pod receives traffic only after fully ready

Example:

```yaml id="u7m1qx"
readinessProbe:
  httpGet:
    path: /actuator/health
    port: 8080
```

Without readiness probes:

* Users may see failures during startup

</details>

---

# 4. In production, why are Deployments preferred over standalone Pods?

<details>
<summary>Answer</summary>

Deployments provide:

* Auto-healing
* Scaling
* Rolling updates
* Rollbacks

Standalone pods:

* Not self-healing
* Hard to manage
* No scaling support

Production systems always use:

```text id="x5m8vw"
Deployments
```

</details>

---

# 5. Your pod keeps restarting continuously. What could be wrong?

<details>
<summary>Answer</summary>

Possible causes:

* Application crash
* Wrong ENV variables
* Probe failures
* Memory issues
* Missing dependencies

Check:

```bash id="p3q7vz"
kubectl logs <pod-name>
kubectl describe pod <pod-name>
```

Common production issue:

```text id="m7q4vx"
CrashLoopBackOff
```

</details>

---

# 6. Which Kubernetes object provides stable access to pods?

## MCQ

A. Pod
B. Deployment
C. Service
D. Ingress

<details>
<summary>Answer</summary>

✅ Correct Answer: C. Service

Pods are temporary and IPs change frequently.

</details>

---

# 7. Your frontend cannot connect to backend service in Kubernetes. What will you check?

<details>
<summary>Answer</summary>

Check:

* Service name
* Service selector
* Pod labels
* Service port
* DNS resolution

Correct communication:

```text id="u9q2wx"
http://backend-service:8080
```

NOT:

```text id="p6m1qv"
localhost
```

inside pods.

</details>

---

# 8. Why is ConfigMap important in enterprise applications?

<details>
<summary>Answer</summary>

ConfigMaps externalize configuration.

Benefits:

* No image rebuild for config changes
* Easier environment management
* Separate config from application

Examples:

* DB URLs
* Feature flags
* API endpoints

Widely used in:

* Dev/Test/Prod environments

</details>

---

# 9. Your deployment update failed in production. How do you rollback?

## MCQ

A. kubectl revert
B. kubectl rollback
C. kubectl rollout undo
D. kubectl restore

<details>
<summary>Answer</summary>

✅ Correct Answer: C. kubectl rollout undo

Example:

```bash id="q8m4vx"
kubectl rollout undo deployment myapp
```

</details>

---

# 10. Explain real-world use of Ingress.

<details>
<summary>Answer</summary>

Ingress acts as:

```text id="x2q7vw"
Reverse proxy for Kubernetes
```

Features:

* Domain routing
* HTTPS/TLS
* Centralized routing
* Load balancing

Example:

```text id="u5m9qx"
api.company.com → backend-service
```

Without ingress:

* Every service requires NodePort/LoadBalancer

</details>

---

# 11. Why are liveness probes critical in production?

<details>
<summary>Answer</summary>

Applications can:

* Hang
* Deadlock
* Stop responding

Liveness probes detect failures.

If probe fails:

```text id="p7m2vz"
Kubernetes restarts container automatically
```

Provides:

* Self-healing
* Higher availability

</details>

---

# 12. Which issue commonly causes OOMKilled?

## MCQ

A. High CPU usage
B. Network timeout
C. Exceeding memory limit
D. Disk corruption

<details>
<summary>Answer</summary>

✅ Correct Answer: C. Exceeding memory limit

OOMKilled:

```text id="m3q8vx"
Out Of Memory Kill
```

</details>

---

# 13. Your MySQL data disappears after pod recreation. Why?

<details>
<summary>Answer</summary>

Cause:

```text id="u1m5qw"
Pod filesystem is ephemeral
```

When pod recreated:

* Internal data lost

Solution:
Use:

* Persistent Volume
* Persistent Volume Claim

Example:

```yaml id="x7q2vz"
persistentVolumeClaim:
  claimName: mysql-pvc
```

</details>

---

# 14. Why should databases use StatefulSet instead of Deployment?

<details>
<summary>Answer</summary>

StatefulSet provides:

* Stable identities
* Stable storage
* Ordered startup/shutdown

Important for:

* MySQL
* Kafka
* MongoDB

Deployments are better for:

```text id="p4m7qx"
Stateless applications
```

</details>

---

# 15. Your Kubernetes cluster suddenly becomes slow. What will you investigate?

<details>
<summary>Answer</summary>

Investigate:

* CPU usage
* Memory usage
* Pending pods
* Node pressure
* Disk usage
* Network bottlenecks

Commands:

```bash id="t9m1vx"
kubectl top pod
kubectl top node
kubectl get events
```

Common causes:

* Resource exhaustion
* OOMKilled pods
* Infinite loops
* Heavy traffic spikes

</details>

---

# 16. Which command shows resource usage?

## MCQ

A. kubectl metrics
B. kubectl logs
C. kubectl top
D. kubectl monitor

<details>
<summary>Answer</summary>

✅ Correct Answer: C. kubectl top

</details>

---

# 17. Why is Helm heavily used in enterprise Kubernetes?

<details>
<summary>Answer</summary>

Helm simplifies:

* Large deployments
* Environment configurations
* Reusable templates
* Version upgrades

Benefits:

* Reduced YAML duplication
* Easier maintenance
* Faster deployments

Enterprise applications may contain:

* Hundreds of YAML files

Helm manages them efficiently.

</details>

---

# 18. Your deployment works in Dev but fails in Prod. Possible reasons?

<details>
<summary>Answer</summary>

Possible reasons:

* Different ConfigMaps
* Missing Secrets
* Resource limits
* Network policies
* Wrong image versions
* DB connectivity issues

Common enterprise issue:

```text id="u7q3vx"
Environment mismatch
```

</details>

---

# 19. Why are resource limits important in Kubernetes?

<details>
<summary>Answer</summary>

Without limits:

```text id="m2q6vw"
One pod can consume entire node resources
```

Problems:

* Node instability
* Cluster crashes
* OOMKilled issues

Best practice:
Always define:

* CPU requests
* Memory requests
* CPU limits
* Memory limits

</details>

---

# 20. Explain Blue-Green Deployment in Kubernetes.

<details>
<summary>Answer</summary>

Blue-Green deployment:

* Blue = current production
* Green = new version

Flow:

1. Deploy Green environment
2. Test Green
3. Switch traffic
4. Remove Blue later

Benefits:

* Zero downtime
* Safer releases
* Easy rollback

Common in:

* Banking
* E-commerce
* Enterprise SaaS

</details>

---

# 21. Which service type is commonly used with cloud load balancers?

## MCQ

A. ClusterIP
B. NodePort
C. LoadBalancer
D. ExternalName

<details>
<summary>Answer</summary>

✅ Correct Answer: C. LoadBalancer

Cloud providers create external load balancers automatically.

</details>

---

# 22. Why are Secrets preferred over ConfigMaps for passwords?

<details>
<summary>Answer</summary>

Secrets designed for:

```text id="x8m4qw"
Sensitive information
```

Benefits:

* Separate handling
* Better access control
* Safer storage mechanisms

Used for:

* DB passwords
* Tokens
* API keys

</details>

---

# 23. Your pod remains in Pending state. What could be wrong?

<details>
<summary>Answer</summary>

Possible causes:

* Insufficient resources
* PVC issues
* Node unavailable
* Scheduler constraints
* Taints/tolerations mismatch

Debug:

```bash id="t5q8vx"
kubectl describe pod <pod-name>
```

</details>

---

# 24. Why is centralized logging important in Kubernetes?

<details>
<summary>Answer</summary>

Pods are temporary.

Without centralized logging:

* Logs lost after pod deletion
* Hard debugging
* Difficult monitoring

Solutions:

* ELK Stack
* Loki
* Fluentd

Enterprise systems always aggregate logs centrally.

</details>

---

# 25. Real-world interview question:

## "How would you deploy a production-grade Spring Boot application in Kubernetes?"

<details>
<summary>Answer</summary>

Production-grade deployment includes:

1. Dockerized application

2. Kubernetes Deployment

3. Service

4. Ingress

5. ConfigMaps

6. Secrets

7. PVC (if needed)

8. Readiness & liveness probes

9. Resource limits

10. Monitoring & logging

11. CI/CD pipeline

12. Helm chart

Typical architecture:

```text id="n1q7vw"
Ingress
   ↓
Service
   ↓
Spring Boot Pods
   ↓
MySQL + Redis
```

Production best practices:

* Horizontal scaling
* Health checks
* Immutable images
* Auto-healing
* Monitoring
* Centralized logging

</details>
