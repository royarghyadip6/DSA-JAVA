# Day 4 — [Kubernetes Fundamentals](https://www.geeksforgeeks.org/devops/introduction-to-kubernetes-k8s/)

# 1. Introduction

Kubernetes (K8s) is:

```text id="m4q8vx"
Container orchestration platform
```

used to:

* Deploy containers
* Manage containers
* Scale applications
* Self-heal failures

Originally developed by:
Google

Now maintained by:
Cloud Native Computing Foundation

---

# 2. Why Docker Alone is Insufficient

Docker is excellent for:

* Running containers
* Packaging applications

BUT in production, problems arise.

---

# 2.1 Problems in Production

Suppose:

```text id="u7m2qx"
100 containers running
```

Questions:

* What if a container crashes?
* How to scale traffic?
* How to load balance?
* How to deploy updates?
* How to manage networking?

Docker alone cannot handle these efficiently.

---

# 3. What is Container Orchestration?

Container orchestration means:

```text id="x1q6vz"
Automated management of containers
```

Kubernetes automates:

* Deployment
* Scaling
* Networking
* Recovery
* Monitoring

---

# 4. Core Kubernetes Features

---

# 4.1 Auto-Healing

If container crashes:

```text id="m8v3qw"
Kubernetes automatically recreates it
```

---

# 4.2 Scaling

Increase/decrease containers dynamically.

Example:

```text id="p5q9vx"
3 pods → 10 pods
```

during high traffic.

---

# 4.3 Load Balancing

Traffic distributed automatically across pods.

---

# 4.4 Rolling Updates

Deploy new version without downtime.

---

# 4.5 Service Discovery

Containers communicate using:

```text id="t2m7qz"
DNS names
```

instead of IP addresses.

---

# 5. Kubernetes Architecture

Kubernetes [cluster](https://www.geeksforgeeks.org/devops/kubernetes-cluster/) has:

* Control Plane (Master Node)
* Worker [Nodes](https://www.geeksforgeeks.org/devops/kubernetes-node/)

Architecture:

```text id="w6q1vx"
                +----------------------+
                |     kubectl CLI      |
                +----------------------+
                           |
                           ↓
                +----------------------+
                |      API Server      |
                +----------------------+
                           |
        ------------------------------------------------
        |                     |                       |
        ↓                     ↓                       ↓
+---------------+   +----------------+   +------------------+
|   Scheduler   |   | Controller Mgr |   |      etcd        |
+---------------+   +----------------+   +------------------+
                                                   |
                                                   |
================================ CONTROL PLANE ================================

                           |
                           ↓

============================= WORKER NODES ===================================

+-------------------------------------------------------------------+
|                         Worker Node 1                             |
|                                                                   |
|  +------------+     +----------------+     +------------------+   |
|  |  kubelet   |     |   kube-proxy   |     | Container Runtime|   |
|  +------------+     +----------------+     +------------------+   |
|                                                          |        |
|                                                          ↓        |
|                                                   +-----------+   |
|                                                   |   Pods    |   |
|                                                   +-----------+   |
+-------------------------------------------------------------------+
```

Simplified Version:

```text id="p9m4qz"
kubectl → API Server → etcd → Controller → Scheduler → kubelet → Container Runtime → Pods
```

---

# 6. Control Plane Components

Control plane manages cluster.

---

# 6.1 API Server

Most important component.

Acts as:

```text id="x9m4qw"
Entry point of Kubernetes cluster
```

All commands go through API server.

Example:

```bash id="u3q8vz"
kubectl get pods
```

communicates with:

```text id="m1v5qx"
API Server
```

---

# 6.2 Scheduler

Responsible for:

```text id="p7m2qw"
Assigning pods to nodes
```

Scheduler decides:

* Which node has enough CPU
* Which node has enough memory

---

# 6.3 Controller Manager

Ensures:

```text id="t4q9vx"
Desired state = Actual state
```

Example:

* Desired pods = 3
* Running pods = 2

Controller creates missing pod.

---

# 6.4 etcd

Distributed key-value database.

Stores:

* Cluster configuration
* Secrets
* Pod information
* State data

Critical component.

---

# 7. Worker Node Components

Worker nodes run actual applications.

---

# 7.1 Kubelet

Agent running on each node.

Responsibilities:

* Communicate with API server
* Start containers
* Monitor pods

---

# 7.2 kube-proxy

Handles:

* Networking
* Service communication
* Load balancing

---

# 8. Kubernetes Workflow

Flow:

```text id="u8m3qx"
kubectl
   ↓
API Server
   ↓
Scheduler
   ↓
Worker Node
   ↓
Kubelet
   ↓
Container Runtime
   ↓
Pod Running
```

---

# 9. Install Kubernetes

For learning Kubernetes locally:

Use:

* Minikube
* Kind

---

# 10. Minikube

Minikube creates:

```text id="p2q7vw"
Single-node Kubernetes cluster
```

on local machine.

Official website:

[Minikube Official Website](https://minikube.sigs.k8s.io/docs/?utm_source=chatgpt.com)

---

# 11. Kind

Kind:

```text id="x5m1qz"
Kubernetes IN Docker
```

Runs Kubernetes clusters using Docker containers.

Official website:

[Kind Official Website](https://kind.sigs.k8s.io/?utm_source=chatgpt.com)

---

# 12. kubectl

`kubectl` is:

```text id="m9q4vx"
Kubernetes command-line tool
```

used to interact with cluster.

---

# 13. Verify Cluster

---

# 13.1 Get Nodes

```bash id="u1m8qw"
kubectl get nodes
```

Example:

```text id="p6q2vx"
NAME       STATUS   ROLES
minikube   Ready    control-plane
```

---

# 13.2 Cluster Info

```bash id="t7m5qz"
kubectl cluster-info
```

Shows:

* API server
* DNS
* Core services

---

# 14. [What is a Pod?](https://www.geeksforgeeks.org/devops/kubernetes-pods/)

Pod is:

```text id="x3q9vw"
Smallest deployable unit in Kubernetes
```

A pod contains:

* One or more containers
* Shared network
* Shared storage

---

# 15. Why Pods Exist

Kubernetes does NOT directly run containers.

It runs:

```text id="m4v1qx"
Pods
```

Pods wrap containers.

---

# 16. Single vs Multi-Container Pod

---

# 16.1 Single Container Pod

Most common.

Example:

```text id="u6q7vz"
Spring Boot container
```

inside one pod.

---

# 16.2 Multi-Container Pod

Multiple tightly coupled containers.

Example:

* Main app container
* Logging sidecar container

Shared:

* Network
* Volumes

---

# 17. Pod Lifecycle

Lifecycle:

```text id="p8m2qw"
Pending
   ↓
Running
   ↓
Succeeded / Failed
```

---

# 18. Pod YAML

Pods created using YAML manifests.

Example:

```yaml id="x1v5qz"
apiVersion: v1
kind: Pod
metadata:
  name: nginx-pod

spec:
  containers:
    - name: nginx
      image: nginx
```

---

# 19. Important YAML Fields

| Field      | Purpose                |
|------------|------------------------|
| apiVersion | Kubernetes API version |
| kind       | Resource type          |
| metadata   | Resource info          |
| spec       | Desired configuration  |

---

# 20. Create Pod

Save as:

```text id="m7q3vx"
pod.yaml
```

Run:

```bash id="u4m9qw"
kubectl apply -f pod.yaml
```

---

# 21. View Pods

```bash id="p2q6vz"
kubectl get pods
```

Example:

```text id="x8m1qx"
NAME        READY   STATUS
nginx-pod   1/1     Running
```

---

# 22. Describe Pod

Detailed information:

```bash id="t5q7vw"
kubectl describe pod nginx-pod
```

Shows:

* Events
* IP
* Node
* Container details

---

# 23. View Logs

```bash id="w9m2qx"
kubectl logs nginx-pod
```

Useful for:

* Errors
* Debugging
* Application startup issues

---

# 24. Enter Pod Shell

```bash id="u3q8vx"
kubectl exec -it nginx-pod -- bash
```

Used for:

* Debugging
* Running Linux commands
* Checking files

---

# 25. Delete Pod

```bash id="m1v4qz"
kubectl delete pod nginx-pod
```

---

# 26. Auto-Healing Demonstration

If pod managed by Deployment:

```text id="p7q9vw"
Kubernetes recreates deleted pod automatically
```

This is:

```text id="x5m2qx"
Self-healing
```

---

# 27. kubectl Basics

---

# 27.1 Get All Resources

```bash id="t8m6qw"
kubectl get all
```

---

# 27.2 Delete Pod

```bash id="u2q1vx"
kubectl delete pod nginx-pod
```

---

# 27.3 Execute Commands Inside Pod

```bash id="m4v7qz"
kubectl exec -it nginx-pod -- bash
```

---

# 27.4 View Pod Logs

```bash id="p9m3qw"
kubectl logs nginx-pod
```

---

# 27.5 Describe Resource

```bash id="x6q8vz"
kubectl describe pod nginx-pod
```

---

# 28. Common Problems

---

# 28.1 Pod Stuck in Pending

Possible causes:

* Insufficient resources
* Node not ready

Check:

```bash id="u5m1qx"
kubectl describe pod
```

---

# 28.2 ImagePullBackOff

Cause:

* Wrong image name
* Registry issue

---

# 28.3 CrashLoopBackOff

Container repeatedly crashing.

Check logs:

```bash id="t3q7vw"
kubectl logs <pod-name>
```

---

# 29. Hands-On Tasks

---

# Task 1 — Deploy nginx Pod

Create:

```text id="m8v2qz"
pod.yaml
```

Deploy using:

```bash id="x1q5vw"
kubectl apply -f pod.yaml
```

---

# Task 2 — View Logs

```bash id="u9m4qx"
kubectl logs nginx-pod
```

---

# Task 3 — Enter Pod Shell

```bash id="p6q8vz"
kubectl exec -it nginx-pod -- bash
```

---

# Task 4 — Delete Pod

```bash id="w2m7qw"
kubectl delete pod nginx-pod
```

Observe recreation if managed by controller.

---

# 30. Best Practices

---

# 30.1 Use YAML Files

Avoid manual commands for production.

---

# 30.2 Use Deployments Instead of Standalone Pods

Pods alone are temporary.

Deployments provide:

* Scaling
* Auto-healing
* Rolling updates

---

# 30.3 Keep Containers Stateless

Persistent data should use:

```text id="u7q3vx"
Volumes
```

---

# 31. Summary

Today you learned:

✅ What Kubernetes is
✅ Why Docker alone is insufficient
✅ Container orchestration
✅ Kubernetes architecture
✅ API Server
✅ Scheduler
✅ Controller Manager
✅ etcd
✅ Kubelet
✅ kube-proxy
✅ Minikube/Kind
✅ kubectl basics
✅ Pods and pod lifecycle
✅ YAML manifests
✅ Logs and debugging
✅ Auto-healing

These concepts are the foundation for:

* Deployments
* Services
* Ingress
* Helm
* Production Kubernetes clusters
