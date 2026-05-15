```bash id="z1m8qx"
kubectl get pods
# List all running pods in current namespace

kubectl get pods -w
# Watch pod status continuously in real-time

kubectl create deployment nginx --image=nginx
# Create a deployment named nginx using nginx image

kubectl expose deployment nginx --type=NodePort --port=80
# Expose nginx deployment externally using NodePort service on port 80

kubectl get svc
# List all services

kubectl get rs
# List all ReplicaSets

kubectl describe rs nginx
# Show detailed information about ReplicaSet named nginx

kubectl scale deployment nginx --replicas=5
# Scale nginx deployment to 5 pod replicas

kubectl set image deployment/nginx nginx=nginx:1.19
# Update nginx container image version to nginx:1.19

kubectl rollout status deployment nginx
# Check rollout/update status of nginx deployment

kubectl rollout history deployment nginx
# View rollout revision history of deployment

kubectl rollout undo deployment nginx
# Rollback deployment to previous version

kubectl exec -it nginx-c89577dd5-qjjkg -- sh
# Enter inside running pod shell interactively

kubectl logs nginx-c89577dd5-qjjkg
# View logs of the pod

kubectl get deployments nginx -o yaml
# Export deployment YAML configuration

kubectl get svc nginx -o yaml
# Export service YAML configuration

kubectl apply -f app.yaml
# Create/update resources defined in app.yaml

kubectl apply -f service.yaml
# Create/update resources defined in service.yaml

kubectl apply -f config.yaml
# Create/update ConfigMap resources

kubectl apply -f secret.yaml
# Create/update Secret resources

kubectl apply -f config.yaml -f secret.yaml -f app.yaml -f service.yaml
# Apply multiple YAML files together

printenv | grep DB_PASSWORD
# Check environment variable containing DB_PASSWORD

kubectl delete -f config.yaml -f secret.yaml -f app.yaml -f service.yaml
# Delete all resources created from YAML files

kubectl delete deployment nginx
# Delete nginx deployment

kubectl delete svc nginx
# Delete nginx service

```


# Kubernetes Commands Cheat Sheet (Section-Wise)

---

# 1. Cluster Information Commands

## Check Cluster Info

```bash id="v8m1qx"
kubectl cluster-info
```

Shows:

* Kubernetes control plane URL
* Core services

---

## Check Nodes

```bash id="u2q7vw"
kubectl get nodes
```

Lists all worker/master nodes.

Example output:

```text id="t4m9qx"
NAME       STATUS   ROLES
minikube   Ready    control-plane
```

---

## Detailed Node Information

```bash id="x1q5vz"
kubectl describe node <node-name>
```

Shows:

* CPU
* Memory
* Conditions
* Allocated resources

---

## Kubernetes Version

```bash id="m7q2vw"
kubectl version
```

Displays:

* Client version
* Server version

---

# 2. Namespace Commands

## List Namespaces

```bash id="u6m8qw"
kubectl get namespaces
```

OR

```bash id="p2q4vx"
kubectl get ns
```

---

## Create Namespace

```bash id="x8m1qz"
kubectl create namespace dev
```

---

## Delete Namespace

```bash id="q7m5vw"
kubectl delete namespace dev
```

---

## Run Commands in Specific Namespace

```bash id="u3q9vx"
kubectl get pods -n dev
```

---

# 3. Pod Commands

## List Pods

```bash id="m1q7vw"
kubectl get pods
```

---

## List Pods Wide Output

```bash id="x2m8qx"
kubectl get pods -o wide
```

Shows:

* Node
* Pod IP
* Internal details

---

## Describe Pod

```bash id="v9q3wx"
kubectl describe pod <pod-name>
```

Shows:

* Events
* Restart count
* Volumes
* Probes

---

## Create Pod from YAML

```bash id="p6m1qv"
kubectl apply -f pod.yaml
```

---

## Delete Pod

```bash id="u4q8vz"
kubectl delete pod <pod-name>
```

---

## Watch Pod Status Continuously

```bash id="x7m2qw"
kubectl get pods -w
```

---

## Execute Inside Pod

```bash id="t5q1vx"
kubectl exec -it <pod-name> -- sh
```

For Ubuntu:

```bash id="m9q4vw"
kubectl exec -it <pod-name> -- bash
```

---

## View Pod Logs

```bash id="u8m7qx"
kubectl logs <pod-name>
```

---

## Stream Logs Live

```bash id="p1q6vz"
kubectl logs -f <pod-name>
```

---

## Logs of Specific Container

```bash id="x5m9qw"
kubectl logs <pod-name> -c <container-name>
```

---

# 4. Deployment Commands

## List Deployments

```bash id="m4q8vx"
kubectl get deployments
```

OR

```bash id="u7m1qw"
kubectl get deploy
```

---

## Create Deployment

```bash id="x9q2vz"
kubectl apply -f deployment.yaml
```

---

## Scale Deployment

```bash id="p3m7qx"
kubectl scale deployment myapp --replicas=5
```

---

## Restart Deployment

```bash id="u2q5vw"
kubectl rollout restart deployment myapp
```

---

## Check Rollout Status

```bash id="x8m4qz"
kubectl rollout status deployment myapp
```

---

## Rollout History

```bash id="m1q9vx"
kubectl rollout history deployment myapp
```

---

## Rollback Deployment

```bash id="u6m2qw"
kubectl rollout undo deployment myapp
```

---

## Delete Deployment

```bash id="p5q8vz"
kubectl delete deployment myapp
```

---

# 5. Service Commands

## List Services

```bash id="x3m7qw"
kubectl get svc
```

---

## Create Service

```bash id="u9q1vx"
kubectl apply -f service.yaml
```

---

## Describe Service

```bash id="m8q4vw"
kubectl describe svc myservice
```

---

## Expose Deployment as Service

```bash id="p2m6qx"
kubectl expose deployment myapp --type=NodePort --port=80
```

---

## Delete Service

```bash id="x7q3vz"
kubectl delete svc myservice
```

---

# 6. ConfigMap Commands

## Create ConfigMap

```bash id="u1m9qw"
kubectl create configmap app-config --from-literal=DB_HOST=mysql
```

---

## List ConfigMaps

```bash id="m5q2vx"
kubectl get configmaps
```

---

## Describe ConfigMap

```bash id="x4m8qz"
kubectl describe configmap app-config
```

---

## Delete ConfigMap

```bash id="u7q1vw"
kubectl delete configmap app-config
```

---

# 7. Secret Commands

## Create Secret

```bash id="p9m4qx"
kubectl create secret generic db-secret --from-literal=password=admin123
```

---

## List Secrets

```bash id="x2q7vw"
kubectl get secrets
```

---

## Describe Secret

```bash id="u8m5qz"
kubectl describe secret db-secret
```

---

## Decode Secret

```bash id="m3q1vx"
echo <base64-value> | base64 --decode
```

---

# 8. Namespace Resource Commands

## Get All Resources

```bash id="x6m2qw"
kubectl get all
```

Shows:

* Pods
* Services
* Deployments
* ReplicaSets

---

## Delete All Pods

```bash id="u1q8vz"
kubectl delete pods --all
```

---

## Delete Everything in Namespace

```bash id="m7q5vw"
kubectl delete all --all
```

---

# 9. Resource Usage Commands

## Pod Resource Usage

```bash id="x9m3qx"
kubectl top pod
```

---

## Node Resource Usage

```bash id="u2q6vw"
kubectl top node
```

---

# 10. YAML & Apply Commands

## Apply YAML

```bash id="m4q9vz"
kubectl apply -f app.yaml
```

---

## Delete YAML Resources

```bash id="x5m1qw"
kubectl delete -f app.yaml
```

---

## Dry Run

```bash id="u8q4vx"
kubectl apply -f app.yaml --dry-run=client
```

---

## Generate YAML Without Creating

```bash id="p7m2qz"
kubectl create deployment nginx --image=nginx --dry-run=client -o yaml
```

---

# 11. Debugging Commands

## Describe Resource

```bash id="x3q8vw"
kubectl describe pod mypod
```

Most important debugging command.

---

## Cluster Events

```bash id="u9m1qx"
kubectl get events
```

---

## Sort Events

```bash id="m2q7vz"
kubectl get events --sort-by=.metadata.creationTimestamp
```

---

## Check Crash Reasons

```bash id="x8m5qw"
kubectl describe pod <pod-name>
```

Look for:

* OOMKilled
* CrashLoopBackOff
* FailedMount

---

# 12. Persistent Volume Commands

## List PV

```bash id="u4q2vx"
kubectl get pv
```

---

## List PVC

```bash id="m6q9vw"
kubectl get pvc
```

---

## Describe PVC

```bash id="x1m7qz"
kubectl describe pvc myclaim
```

---

# 13. Ingress Commands

## List Ingress

```bash id="u5q8vw"
kubectl get ingress
```

---

## Describe Ingress

```bash id="m9q3vx"
kubectl describe ingress myingress
```

---

# 14. Context & Config Commands

## Current Context

```bash id="x2m5qw"
kubectl config current-context
```

---

## List Contexts

```bash id="u7q4vz"
kubectl config get-contexts
```

---

## Switch Context

```bash id="m1q8vw"
kubectl config use-context minikube
```

---

# 15. Advanced Useful Commands

## Port Forwarding

```bash id="x6q2vx"
kubectl port-forward pod/mypod 8080:80
```

Access pod locally.

---

## Copy Files Into Pod

```bash id="u3m7qw"
kubectl cp file.txt mypod:/tmp
```

---

## Copy Files From Pod

```bash id="m8q1vz"
kubectl cp mypod:/tmp/file.txt .
```

---

## API Resources

```bash id="x4q9vw"
kubectl api-resources
```

---

## Explain YAML Fields

```bash id="u9m5qx"
kubectl explain deployment
```

Deep inspection:

```bash id="p1q7vz"
kubectl explain deployment.spec.template
```

---

# 16. Most Important Interview Commands

| Command              | Purpose             |
|----------------------|---------------------|
| kubectl get pods     | List pods           |
| kubectl describe pod | Debug pod           |
| kubectl logs         | View logs           |
| kubectl exec -it     | Enter pod           |
| kubectl apply -f     | Create resources    |
| kubectl delete -f    | Delete resources    |
| kubectl rollout undo | Rollback            |
| kubectl scale        | Scale deployment    |
| kubectl top pod      | Resource monitoring |
| kubectl get events   | Troubleshooting     |
