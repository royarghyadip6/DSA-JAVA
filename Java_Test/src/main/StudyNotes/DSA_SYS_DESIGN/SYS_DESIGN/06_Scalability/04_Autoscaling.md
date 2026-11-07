# 4. Autoscaling and Cloud Practice

## Simple idea

**Autoscaling** adds or removes compute when load changes. Nobody SSHes in at midnight to start ten servers.

```text
Traffic rises  →  add instances   →  users stay fast
Traffic falls  →  remove them     →  you stop paying for idle boxes
```

It is normal for web apps, microservices, and event consumers.

You still set the **rules**. The cloud only follows them.

---

## What can trigger it

| Signal | Meaning |
|--------|---------|
| CPU | The process is busy |
| Memory | The process is near its RAM cap |
| Request rate | Each instance is taking too many calls |
| Queue length | Kafka or SQS is backing up |
| Latency or errors | Users are already hurting |
| Custom metric | Orders per second, active viewers |

CPU alone is a weak signal for an I/O-bound or queue-based app. Scale the thing that is actually scarce.

---

## Horizontal vs vertical, again, in the cloud

- **Horizontal (scale out / in)** — more or fewer instances or pods. Best for stateless apps. This is what people mean by autoscaling most of the time.
- **Vertical (scale up / down)** — a bigger or smaller VM. Often needs a restart. Hardware still has a maximum size.

Prefer horizontal for the app tier. Use vertical when the workload cannot be split (some databases) or you are still early.

---

## Reactive vs predictive vs scheduled

| Policy | When it acts |
|--------|----------------|
| **Reactive** | After a metric crosses a line (CPU > 70% for 3 minutes) |
| **Predictive** | Before the rush, using history or a forecast (AWS Predictive Scaling is the course example) |
| **Scheduled** | A known clock, such as 9 a.m. login or Friday sale |

Reactive is late by design: instances take time to boot. Predictive and scheduled scaling cover the gap when you know the pattern.

---

## AWS, Azure, and GCP (course map)

| Cloud | What you name |
|-------|----------------|
| **AWS** | Auto Scaling Groups on EC2, driven by CloudWatch. Also ECS, EKS, Lambda |
| **Azure** | VM Scale Sets, App Service plans, AKS, rules from Azure Monitor |
| **GCP** | Managed Instance Groups, Cloud Run, Cloud Functions, GKE Horizontal Pod Autoscaler, Cloud Monitoring |

You do not need every screen click. Say: **a group of instances, a metric, a policy, a min and a max.**

---

## Containers

```text
Load balancer
    ↓
Kubernetes pods (or ECS tasks)
    ↓
Horizontal Pod Autoscaler on CPU or a custom metric
```

The orchestrator starts and stops pods. The load balancer must include new pods and drop ones that fail health checks. Alert if the scaler is stuck at max or flapping.

---

## What to watch

- CPU and memory
- requests per instance
- queue depth
- latency and error rate
- a business number (checkout rate, viewers)

Tools from the course: CloudWatch, Prometheus + Grafana, Azure Monitor, GCP operations.

If you scale only on CPU, a full queue can grow while CPU still looks calm.

---

## Cost

Autoscaling saves money only if it also **scales in**.

- set thresholds so one small spike does not launch a fleet
- **min and max** instance counts
- spot / preemptible instances for batch and jobs that can be interrupted
- scale to zero for idle HTTP or functions (Cloud Run, Lambda)
- resize instance types from real usage (rightsizing)
- budgets and alerts so a bug cannot spend all month

Over-provisioning is waste. Under-provisioning is an outage. The policy is the balance.

---

## Real-time and serverless pain

| Problem | What the user sees |
|---------|-------------------|
| **Scale latency** | New VMs are not instant. The spike hits before they are ready |
| **Cold start** | Lambda or Cloud Functions pay a startup delay on the first calls |
| **Scale-in too fast** | You kill instances that were about to handle the next burst |
| **Threshold too high** | You add capacity only after errors already started |

For live traffic, keep a small warm floor (minimum instances), scale on queue or request rate early, and do not depend on a cold start for the first user of a sale.

---

## Interview lines

- Autoscale out for the peak and in for the quiet hours, between a min and a max.
- Horizontal for stateless apps. Vertical when one process must get bigger.
- Reactive is late. Predictive or scheduled covers known rushes.
- Name the cloud primitive (ASG, VMSS, MIG, HPA) and the metric.
- Cold starts and boot time are why a pure reactive policy fails a sudden TV spike.

---

## Interview Q&A

### 1. What is autoscaling, and why does it matter?

<details>
<summary>Show Answer</summary>

**Answer:**

It changes capacity automatically with demand. You stay available at peaks, you stop paying for idle machines later, and users avoid a overloaded site. In a distributed system this is how the fleet tracks load without an operator.

</details>

### 2. Horizontal vs vertical scaling?

<details>
<summary>Show Answer</summary>

**Answer:**

**Horizontal** adds or removes instances or pods. It fits stateless services and survives a single death.

**Vertical** resizes one machine's CPU or RAM. It is simpler, hits a hardware limit, and often restarts the machine.

</details>

### 3. How does predictive autoscaling work?

<details>
<summary>Show Answer</summary>

**Answer:**

It looks at past traffic (sometimes with a learning model) and adds capacity **before** the peak. AWS Predictive Scaling on EC2 is the course example. Scheduled scaling is the manual version: you already know the Friday sale starts at 8 a.m.

</details>

### 4. How does autoscaling work on AWS, Azure, and GCP?

<details>
<summary>Show Answer</summary>

**Answer:**

- **AWS** — Auto Scaling Groups plus CloudWatch, for EC2, and scaling for ECS, EKS, and Lambda
- **Azure** — VM Scale Sets, App Service, AKS, with Azure Monitor
- **GCP** — Managed Instance Groups, Cloud Run, Functions, and the GKE Horizontal Pod Autoscaler

Same idea everywhere: metric, policy, min, max.

</details>

### 5. How would you autoscale a container app?

<details>
<summary>Show Answer</summary>

**Answer:**

Run it on Kubernetes or ECS/EKS/AKS. Add a Horizontal Pod Autoscaler on CPU or a custom metric such as requests or queue depth. Put a load balancer in front. Alert when you sit at max replicas or when scale events do not reduce latency.

</details>

### 6. Which metrics should you monitor?

<details>
<summary>Show Answer</summary>

**Answer:**

CPU, memory, request rate, queue depth, latency, error rate, and a business metric such as orders per second. Pick the one that matches the bottleneck, not CPU by habit.

</details>

### 7. How do you keep autoscaling from wasting money?

<details>
<summary>Show Answer</summary>

**Answer:**

Wise thresholds, a maximum size, spot instances for interruptible work, scale-to-zero for idle functions, rightsizing, and budget alerts. Scaling out without scaling in is just a slow way to overpay.

</details>

### 8. What is hard about autoscaling real-time systems?

<details>
<summary>Show Answer</summary>

**Answer:**

New instances are slow to boot, so reactive scale lags the spike. Serverless cold starts add the same delay. Too aggressive and you overspend. Too slow or too high a threshold and users see errors before capacity arrives. Keep a warm minimum for live traffic.

</details>

**Q&A recap:** metric plus min/max, scale out and in, predict known peaks, watch boot time and the bill.

## Interview Q&A — 6 to 8 years

### S1. You scale on CPU and the site is still down. Which metric lied?

<details>
<summary>Show Answer</summary>

**Answer:**

CPU can be low while threads wait on a database, a lock, or a full pool. I scale on the symptom that matches the bottleneck: queue depth, pool wait, p99, or requests in flight. I also set a minimum number of warm instances so I am not at zero when the first spike arrives. A maximum stops a bug from scaling to infinity and taking the database with it.

**Corner cases:** Scaling out on a hot key adds callers to the same row and makes it worse. A metric that lags 2 minutes scales up after the spike and scales down just in time for the next one (oscillation). I add cooldown. A scale-in that kills nodes with in-flight long requests. I drain first. CPU of a sidecar counted as app load, so I scale for the proxy's TLS, or I ignore the app. I pick the right container metric.

</details>

### S2. New instances come up and immediately fail. What did the scale event forget?

<details>
<summary>Show Answer</summary>

**Answer:**

Boot time, cache warmth, and connection storms. A JVM or a big cache may need minutes. Readiness must stay false until then. If 50 nodes start together, they must not each open 100 database connections and run a cold miss against the same keys. I stagger start, cap connections, and warm the cache. The load balancer only sees them after readiness.

**Corner cases:** An image pull that is slow or rate-limited, so scale-up times out and the autoscaler tries again. A config or secret the new node cannot read, so it crash-loops and the autoscaler keeps adding copies. A singleton job that now runs on every new node. A quota (vCPU, IP addresses) that makes the scale-up succeed in the API and fail in reality. I alert on "desired != ready."

</details>

### S3. How do you autoscale for a known sale without waiting for the pain?

<details>
<summary>Show Answer</summary>

**Answer:**

Scheduled scale-up before the sale, a higher minimum during the window, and a load test that looks like the sale's mix (not only health checks). I pre-warm caches. I raise database and queue capacity first, then app servers, because the app is usually the easy part. After the sale I scale in slowly so I do not drop stragglers.

**Corner cases:** The sale moves time zone and the schedule is UTC-wrong. Predictive scaling trained on last week misses a TV mention. A scheduled scale-up of apps against a database that was not scaled, which fails faster. Scale-to-zero on the path that must send the "sale is live" notification. I keep that path warm. Licenses or connection caps that do not schedule themselves.

</details>

### S4. What do you hard-cap, even when the autoscaler wants more?

<details>
<summary>Show Answer</summary>

**Answer:**

Database connections, downstream calls, and spend. The app tier's max instances times pool size must be under the database max connections, with room for admin and migrations. I cap queue consumers so a backlog cannot stampede a partner API. I would rather return 429 than double the bill and still fail. The cap is a product decision I can say out loud.

**Corner cases:** Two autoscalers (HPA and a cluster autoscaler, or two regions) that each think they have the whole connection budget. A max that is so low the site cannot meet the SLO on a normal day, so people raise it in the incident and never put it back. Scale-in so aggressive that a daily wave pays cold-start latency every time. I set a floor. Spot or preemptible nodes that all vanish together need a non-spot floor for the core path.

</details>

**Senior recap:** scale on saturation not vanity CPU, warm and stagger new nodes, schedule the sale, and cap connections so scale-out cannot kill the database.

Back to [00_Section_Overview.md](00_Section_Overview.md). Next course folder: [07_Storage_Database](../07_Storage_Database/00_Section_Overview.md).
