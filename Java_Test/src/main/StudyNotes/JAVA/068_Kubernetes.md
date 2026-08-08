# 68. Kubernetes Questions

## 68. Kubernetes Questions

Container orchestration for production Java microservices — deploy, scale, heal, roll out safely.

---

## Basics

---

# 1. What is Kubernetes?

<details>
<summary>Show Answer</summary>

**Answer:**

**Kubernetes (K8s)** is an **open-source container orchestration platform** that automates deploying, scaling, and managing containerized applications across a cluster of nodes.

```text
You declare desired state (YAML) → K8s control plane reconciles actual state
  "3 replicas of order-service:2.1.0, port 8080, 512Mi RAM"
```

| Layer | Components |
|-------|------------|
| Control plane | API Server, etcd, Scheduler, Controller Manager |
| Worker nodes | kubelet, kube-proxy, container runtime (containerd) |
| Workloads | Pod, Deployment, Service, Ingress |

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: order-service
spec:
  replicas: 3
  template:
    spec:
      containers:
        - name: app
          image: myregistry/order-service:2.1.0
          ports:
            - containerPort: 8080
```

**Interview Point:**

> K8s = declarative orchestration. You describe desired state; controllers loop until reality matches. Industry standard for Java microservices at scale.

</details>

---

# 2. Why Kubernetes?

<details>
<summary>Show Answer</summary>

**Answer:**

| Without K8s | With K8s |
|-------------|----------|
| Manual deploy to N servers | Rolling update across cluster |
| Script-based scaling | HPA scales on CPU/custom metrics |
| Single host failure = outage | Pods rescheduled to healthy nodes |
| Config baked in image | ConfigMap/Secret injection |
| No standard service discovery | DNS-based service names |

**Why Java teams adopt K8s:**

```text
✅ 20 Spring Boot services — one platform, same deploy model
✅ Self-healing: crash → restart; failed node → reschedule
✅ Resource quotas: prevent one service starving others
✅ Blue-green / canary via Ingress + multiple Deployments
✅ Integrates with Prometheus, Istio, ArgoCD
```

**Trade-offs (senior answer):**

```text
Complexity cost — need platform team or managed EKS/AKS/GKE
Not for simple monolith on one VM — Docker Compose may suffice
Stateful workloads (DB) often stay managed (RDS) not in-cluster
```

**Interview Point:**

> K8s solves deploy at scale, self-healing, service discovery, and rollout strategies. Senior: mention operational cost and when NOT to use it.

</details>

---

# 3. Container orchestration?

<details>
<summary>Show Answer</summary>

**Answer:**

**Container orchestration** = automating the **lifecycle** of containers across many hosts: scheduling, networking, scaling, health checks, updates, secrets.

```text
Orchestrator responsibilities:
  Schedule   — which node runs which pod
  Scale      — add/remove replicas based on load
  Heal       — restart failed containers, replace bad nodes
  Network    — stable IPs/DNS between services
  Update     — rolling, blue-green, canary
  Config     — inject env/config without rebuild
```

| Tool | Notes |
|------|-------|
| Kubernetes | De facto standard |
| Docker Swarm | Simpler, declining |
| Nomad | HashiCorp alternative |
| ECS/Fargate | AWS-managed orchestration |

```text
Manual:  docker run × 50 services × 5 servers = nightmare
K8s:     Deployment replicas=5 → Scheduler places pods → Service routes traffic
```

**Interview Point:**

> Orchestration = automate what ops did manually with scripts. K8s is the control loop: observe → compare → act.

</details>

---

## Components

---

# 4. Pod

<details>
<summary>Show Answer</summary>

**Answer:**

A **Pod** is the **smallest deployable unit** in K8s — one or more containers sharing network namespace and volumes.

```text
Pod = wrapper
  ├── Container: order-service (Spring Boot)
  ├── Optional sidecar: fluent-bit (log shipping)
  └── Shared: localhost networking, volumes
```

| Property | Detail |
|----------|--------|
| Ephemeral | New IP on restart — don't rely on pod IP |
| Single IP | Containers in pod share `localhost` |
| Lifecycle | Pending → Running → Succeeded/Failed |
| Restart policy | Always / OnFailure / Never |

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: order-pod
spec:
  containers:
    - name: app
      image: order-service:2.1.0
      resources:
        requests: { memory: "512Mi", cpu: "250m" }
        limits:   { memory: "1Gi", cpu: "500m" }
      livenessProbe:
        httpGet: { path: /actuator/health/liveness, port: 8080 }
      readinessProbe:
        httpGet: { path: /actuator/health/readiness, port: 8080 }
```

**Interview Point:**

> Pod = one or more tightly coupled containers. Production: rarely create bare Pods — use Deployment. Always set probes + resource requests/limits for Java apps.

</details>

---

# 5. Deployment

<details>
<summary>Show Answer</summary>

**Answer:**

A **Deployment** manages **ReplicaSets** to run desired number of **Pod replicas** and handles **rolling updates** and rollbacks.

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: payment-service
spec:
  replicas: 3
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
  selector:
    matchLabels: { app: payment }
  template:
    metadata:
      labels: { app: payment }
    spec:
      containers:
        - name: payment
          image: myregistry/payment:3.0.1
```

| Command | Action |
|---------|--------|
| `kubectl apply -f deploy.yaml` | Create/update |
| `kubectl rollout status deployment/payment-service` | Watch rollout |
| `kubectl rollout undo deployment/payment-service` | Rollback |
| `kubectl scale deployment/payment-service --replicas=5` | Scale |

**Interview Point:**

> Deployment = desired state for stateless apps. Controls replica count + rolling updates. Java microservice = one Deployment per service.

</details>

---

# 6. ReplicaSet

<details>
<summary>Show Answer</summary>

**Answer:**

A **ReplicaSet** ensures a **specified number of Pod replicas** are running at all times — created and managed by Deployment.

```text
Deployment
    └── ReplicaSet (payment-service-7d4f8b9c6)
            ├── Pod payment-abc
            ├── Pod payment-def
            └── Pod payment-ghi
```

| Event | ReplicaSet action |
|-------|-------------------|
| Pod crash | Create replacement |
| Scale up replicas: 3→5 | Start 2 new pods |
| Node drain | Reschedule pods elsewhere |
| New Deployment revision | New ReplicaSet; old scaled down |

```bash
kubectl get rs
kubectl describe rs payment-service-7d4f8b9c6
```

**Interview Point:**

> ReplicaSet = pod counter/keeper. You usually interact with Deployment, not ReplicaSet directly. Old ReplicaSets kept for rollback history.

</details>

---

# 7. Service

<details>
<summary>Show Answer</summary>

**Answer:**

A **Service** provides a **stable network endpoint** (ClusterIP/DNS) to reach a dynamic set of Pods.

```text
payment-service.default.svc.cluster.local:8080
        │
        ▼ load balance across healthy pods
   [Pod1] [Pod2] [Pod3]
```

| Service type | Use |
|--------------|-----|
| `ClusterIP` | Internal only (default) |
| `NodePort` | Expose on each node IP:port |
| `LoadBalancer` | Cloud LB provisions external IP |
| `Headless` | DNS returns pod IPs (StatefulSet) |

```yaml
apiVersion: v1
kind: Service
metadata:
  name: payment-service
spec:
  selector:
    app: payment
  ports:
    - port: 80
      targetPort: 8080
  type: ClusterIP
```

**Java client:** `http://payment-service/api/pay` — kube-dns resolves name inside cluster.

**Interview Point:**

> Service = stable VIP + load balancing + service discovery. Pods come and go; Service DNS stays constant.

</details>

---

# 8. ConfigMap

<details>
<summary>Show Answer</summary>

**Answer:**

**ConfigMap** stores **non-sensitive configuration** as key-value pairs — injected as env vars or mounted files.

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: app-config
data:
  application.yml: |
    server:
      port: 8080
    spring:
      profiles:
        active: prod
  LOG_LEVEL: INFO
---
# Mount as file
volumeMounts:
  - name: config
    mountPath: /config
volumes:
  - name: config
    configMap:
      name: app-config
```

| Method | When |
|--------|------|
| Env var | Simple key-value |
| Volume mount | Full `application.yml` |
| Spring Cloud K8s | Auto-reload on ConfigMap change |

**Interview Point:**

> ConfigMap = externalized non-secret config. Change config without rebuilding image. Don't put passwords here — use Secret.

</details>

---

# 9. Secret

<details>
<summary>Show Answer</summary>

**Answer:**

**Secret** stores **sensitive data** (passwords, API keys, TLS certs) — base64 encoded at rest; RBAC controls access.

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: db-credentials
type: Opaque
stringData:
  username: app_user
  password: s3cr3tP@ss
---
env:
  - name: SPRING_DATASOURCE_PASSWORD
    valueFrom:
      secretKeyRef:
        name: db-credentials
        key: password
```

| Best practice | Why |
|---------------|-----|
| External Secrets Operator | Sync from AWS SM / Vault |
| Never commit secrets to Git | Use sealed-secrets or CI injection |
| RBAC least privilege | Only workload SA can read |
| Rotate regularly | Short-lived credentials |

```bash
kubectl create secret generic db-creds \
  --from-literal=password=xxx
```

**Interview Point:**

> Secret = sensitive config. Base64 ≠ encryption — enable etcd encryption at rest. Prefer external secret managers in enterprise prod.

</details>

---

## Advanced

---

# 10. Ingress?

<details>
<summary>Show Answer</summary>

**Answer:**

**Ingress** exposes HTTP/HTTPS routes from outside cluster to internal Services — host/path routing, TLS termination.

```text
Internet → Ingress Controller (nginx/ALB) → Ingress rules → Service → Pods
```

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: api-ingress
  annotations:
    nginx.ingress.kubernetes.io/rate-limit: "100"
spec:
  tls:
    - hosts: [api.mycompany.com]
      secretName: api-tls
  rules:
    - host: api.mycompany.com
      http:
        paths:
          - path: /orders
            pathType: Prefix
            backend:
              service:
                name: order-service
                port: { number: 80 }
          - path: /payments
            pathType: Prefix
            backend:
              service:
                name: payment-service
                port: { number: 80 }
```

| vs LoadBalancer Service | Ingress |
|-------------------------|---------|
| One LB per service | One entry, many routes |
| L4 TCP | L7 HTTP routing |
| Cost | Fewer cloud LBs |

**Interview Point:**

> Ingress = L7 routing into cluster. Needs Ingress Controller. Path-based routing for microservices behind one domain.

</details>

---

# 11. Load Balancer?

<details>
<summary>Show Answer</summary>

**Answer:**

In K8s, load balancing happens at **multiple levels**:

```text
External LB (cloud) → Ingress or Service type LoadBalancer
                           ↓
                    kube-proxy (iptables/IPVS)
                           ↓
                    Pod endpoints (ready only)
```

| Layer | Mechanism |
|-------|-----------|
| Service | kube-proxy distributes to pod IPs |
| Ingress | Controller balances across service backends |
| Cloud LB | AWS NLB/ALB, GCP LB for external traffic |

```yaml
apiVersion: v1
kind: Service
metadata:
  name: api-external
spec:
  type: LoadBalancer
  selector: { app: api }
  ports:
    - port: 443
      targetPort: 8080
```

**Session affinity:** `sessionAffinity: ClientIP` for sticky sessions (avoid if possible — prefer JWT/stateless).

**Interview Point:**

> K8s Service provides internal LB + DNS. External access via LoadBalancer Service or Ingress. Readiness probe ensures traffic only to healthy pods.

</details>

---

# 12. Horizontal Pod Autoscaler?

<details>
<summary>Show Answer</summary>

**Answer:**

**HPA** automatically scales Pod replicas based on **metrics** (CPU, memory, custom).

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: order-service-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: order-service
  minReplicas: 2
  maxReplicas: 20
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 70
    - type: Pods
      pods:
        metric:
          name: http_requests_per_second
        target:
          type: AverageValue
          averageValue: "100"
```

```text
CPU > 70% → HPA adds pods (if resources available on nodes)
CPU < 70% → scale down (respect cooldown)
```

| Requirement | Detail |
|-------------|--------|
| metrics-server | For CPU/memory |
| Prometheus adapter | Custom metrics (RPS, queue depth) |
| requests set | HPA needs resource requests defined |

**Interview Point:**

> HPA = horizontal scale on metrics. Java: set CPU requests realistically; consider custom metrics (Kafka lag, thread pool queue) over CPU alone.

</details>

---

## Production

---

# 13. Rolling update?

<details>
<summary>Show Answer</summary>

**Answer:**

**Rolling update** replaces pods **gradually** — new version pods start, pass readiness, old pods terminate — zero-downtime if configured correctly.

```yaml
strategy:
  type: RollingUpdate
  rollingUpdate:
    maxSurge: 25%        # extra pods during rollout
    maxUnavailable: 0    # never drop below desired count
```

```text
v1: [P1][P2][P3]
      ↓ deploy v2
v2: [P1][P2][P3][P4-new]  ← surge
v2: [P2][P3][P4][P5-new]
v2: [P4][P5][P6]          ← all v2
```

```bash
kubectl set image deployment/order-service app=order:2.2.0
kubectl rollout status deployment/order-service
kubectl rollout history deployment/order-service
kubectl rollout undo deployment/order-service --to-revision=3
```

**Java prod tips:**

```text
readinessProbe waits for DB connection pool warm-up
preStop hook: sleep 10s for connection drain
graceful shutdown: server.shutdown=graceful in Spring Boot
```

**Interview Point:**

> Rolling update = default safe deploy. maxUnavailable=0 + readiness probe = no traffic to unready pods. Rollback = undo to previous ReplicaSet.

</details>

---

# 14. Blue-Green deployment?

<details>
<summary>Show Answer</summary>

**Answer:**

**Blue-Green** runs **two identical environments** — Blue (live) and Green (new) — switch traffic instantly at cutover.

```text
                    ┌── Blue  (v1.0) ← 100% traffic (live)
Ingress/Switch ─────┤
                    └── Green (v2.0) ← 0% traffic (standby, fully tested)
                    
Cutover: flip Service selector or Ingress weight → Green 100%
Rollback: flip back to Blue (seconds)
```

```yaml
# Two Deployments, one active Service selector
# Blue: labels app=api, version=blue
# Green: labels app=api, version=green
# Service selector: version: blue  → change to green at cutover
```

| Pros | Cons |
|------|------|
| Instant rollback | 2× resources during deploy |
| Full test green before switch | DB schema migration complexity |
| No mixed versions | All-or-nothing risk at flip |

**Interview Point:**

> Blue-green = two full stacks, traffic switch. Fast rollback. Needs double capacity briefly. Schema changes need backward-compatible migrations.

</details>

---

# 15. Canary deployment?

<details>
<summary>Show Answer</summary>

**Answer:**

**Canary** routes a **small % of traffic** to new version, monitors errors/latency, then gradually increases.

```text
v1 (stable): 95% traffic
v2 (canary):  5% traffic  → monitor 15 min
v2:          25% traffic  → monitor
v2:         100% traffic  → promote, retire v1
```

```yaml
# Istio VirtualService example
http:
  - route:
      - destination: { host: api, subset: v1 }
        weight: 90
      - destination: { host: api, subset: v2 }
        weight: 10
```

| Tool | Approach |
|------|----------|
| Istio/Linkerd | Traffic split by weight |
| Argo Rollouts | Automated canary analysis |
| Ingress nginx | `canary-weight` annotation |
| Flagger | Prometheus-based promotion |

**Metrics to watch:**

```text
Error rate, p99 latency, JVM GC pause, 5xx from actuator
Auto-rollback if error rate > threshold
```

**Interview Point:**

> Canary = risk-limited rollout with real prod traffic. Best for high-stakes Java services. Requires observability + automated rollback.

</details>

---

# 5–8 Year Interview Rapid Fire

### Q: Pod vs Deployment?

<details>
<summary>Show Answer</summary>

**Answer:**

**Pod** = single instance (ephemeral). **Deployment** = manages N pod replicas + updates. Always use Deployment for stateless Java services.

</details>

---

### Q: ClusterIP vs LoadBalancer?

<details>
<summary>Show Answer</summary>

**Answer:**

**ClusterIP** = internal cluster access only. **LoadBalancer** = provisions external cloud LB. External HTTP routing usually via **Ingress**.

</details>

---

### Q: Liveness vs readiness probe?

<details>
<summary>Show Answer</summary>

**Answer:**

**Liveness** = is process alive? Fail → restart pod. **Readiness** = ready for traffic? Fail → remove from Service endpoints. Java: readiness waits for DB/Kafka connection.

</details>

---

### Q: How does service discovery work?

<details>
<summary>Show Answer</summary>

**Answer:**

CoreDNS resolves `service-name.namespace.svc.cluster.local` to ClusterIP; kube-proxy load-balances to ready pod endpoints.

</details>

---

### Q: Rolling vs canary?

<details>
<summary>Show Answer</summary>

**Answer:**

**Rolling** replaces pods batch by batch (all users may hit mixed versions briefly). **Canary** sends small traffic % to new version first — safer for risky releases.

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> K8s orchestrates containers declaratively: Deployment manages ReplicaSets/Pods; Service gives stable DNS/LB; ConfigMap/Secret externalize config; Ingress routes HTTP; HPA scales; rolling/canary/blue-green control release risk for Java microservices.

</details>
