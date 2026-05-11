
# Docker

```bash
./mvnw spring-boot:build-image -Dspring-boot.build-image.imageName=<image-name>
```

```bash
./mvnw spring-boot:build-image "-Dspring-boot.build-image.builder=argroy/webshopper:first"
```

# Docker Important Commands Cheat Sheet

# 1. Docker Version & Info

## Check Docker Version

```bash id="00f1u1"
docker version
```

## Detailed Docker Information

```bash id="sbr5n0"
docker info
```

---

# 2. Docker Images

## List Images

```bash id="h6egbc"
docker images
```

OR

```bash id="m3s6mn"
docker image ls
```

---

## Pull Image From Docker Hub

```bash id="c5js73"
docker pull nginx
```

Specific version:

```bash id="uc9q5k"
docker pull mysql:8.0
```

---

## Remove Image

```bash id="jlwmi1"
docker rmi image-name
```

Force remove:

```bash id="jlwmi2"
docker rmi -f image-name
```

---

## Remove Unused Images

```bash id="jlwmi3"
docker image prune
```

Remove ALL unused:

```bash id="jlwmi4"
docker image prune -a
```

---

# 3. Docker Containers

## Run Container

```bash id="jlwmi5"
docker run nginx
```

---

## Run Container in Background

```bash id="jlwmi6"
docker run -d nginx
```

---

## Run With Port Mapping

```bash id="jlwmi7"
docker run -p 8080:80 nginx
```

Format:

```text id="jlwmi8"
HOST_PORT:CONTAINER_PORT
```

---

## Run With Container Name

```bash id="jlwmi9"
docker run --name my-nginx nginx
```

---

## Interactive Terminal

```bash id="jlwmia"
docker run -it ubuntu bash
```

---

## List Running Containers

```bash id="jlwmib"
docker ps
```

---

## List All Containers

```bash id="jlwmic"
docker ps -a
```

---

## Stop Container

```bash id="jlwmid"
docker stop container-id
```

---

## Start Container

```bash id="jlwmie"
docker start container-id
```

---

## Restart Container

```bash id="jlwmif"
docker restart container-id
```

---

## Remove Container

```bash id="jlwmig"
docker rm container-id
```

Force remove:

```bash id="jlwmih"
docker rm -f container-id
```

---

# 4. Logs & Debugging

## View Logs

```bash id="jlwmii"
docker logs container-id
```

---

## Follow Logs Live

```bash id="jlwmij"
docker logs -f container-id
```

---

## Last 100 Lines

```bash id="jlwmik"
docker logs --tail 100 container-id
```

---

# 5. Execute Commands Inside Container

## Open Shell

```bash id="jlwmil"
docker exec -it container-id bash
```

Alpine:

```bash id="jlwmim"
docker exec -it container-id sh
```

---

## Run Command Inside Container

```bash id="jlwmin"
docker exec container-id ls
```

---

# 6. Build Docker Images

## Build Image

```bash id="jlwmio"
docker build -t myapp .
```

---

## Build Without Cache

```bash id="jlwmip"
docker build --no-cache -t myapp .
```

---

## Build Using Specific Dockerfile

```bash id="jlwmiq"
docker build -f Dockerfile.dev -t myapp .
```

---

# 7. Docker Hub / Registry

## Login

```bash id="jlwmir"
docker login
```

---

## Tag Image

```bash id="jlwmis"
docker tag myapp username/myapp:v1
```

---

## Push Image

```bash id="jlwmit"
docker push username/myapp:v1
```

---

## Logout

```bash id="jlwmiu"
docker logout
```

---

# 8. Volumes (Persistent Storage)

## Create Volume

```bash id="jlwmiv"
docker volume create myvolume
```

---

## List Volumes

```bash id="jlwmiw"
docker volume ls
```

---

## Inspect Volume

```bash id="jlwmix"
docker volume inspect myvolume
```

---

## Mount Volume

```bash id="కీయjy"
docker run -v myvolume:/data nginx
```

---

## Bind Mount

```bash id="jlwmiz"
docker run -v $(pwd):/app nginx
```

Windows CMD:

```cmd id="jlwmj0"
docker run -v %cd%:/app nginx
```

---

# 9. Docker Networks

## List Networks

```bash id="jlwmj1"
docker network ls
```

---

## Create Network

```bash id="jlwmj2"
docker network create mynetwork
```

---

## Run Container in Network

```bash id="jlwmj3"
docker run --network=mynetwork nginx
```

---

## Inspect Network

```bash id="jlwmj4"
docker network inspect mynetwork
```

---

# 10. Docker Compose

## Start Services

```bash id="jlwmj5"
docker compose up
```

---

## Background Mode

```bash id="jlwmj6"
docker compose up -d
```

---

## Stop Services

```bash id="jlwmj7"
docker compose down
```

---

## Rebuild Services

```bash id="jlwmj8"
docker compose up --build
```

---

## View Logs

```bash id="jlwmj9"
docker compose logs
```

---

# 11. Inspect & Stats

## Inspect Container

```bash id="’winijja"
docker inspect container-id
```

---

## Resource Usage

```bash id="jlwmjb"
docker stats
```

---

## Processes Inside Container

```bash id="’winijjc"
docker top container-id
```

---

# 12. Cleanup Commands

## Remove Stopped Containers

```bash id="quotelevjjd"
docker container prune
```

---

## Remove Unused Volumes

```bash id="ಳೆದjje"
docker volume prune
```

---

## Remove Unused Networks

```bash id="mergedjjf"
docker network prune
```

---

## Remove Everything Unused

```bash id="thoroughjjg"
docker system prune
```

---

## Remove EVERYTHING

```bash id="fulljjh"
docker system prune -a
```

---

# 13. Save & Load Images

## Save Image

```bash id="savejji"
docker save -o myapp.tar myapp
```

---

## Load Image

```bash id="loadjjj"
docker load -i myapp.tar
```

---

# 14. Copy Files

## Copy Host → Container

```bash id="copyjjk"
docker cp file.txt container-id:/app
```

---

## Copy Container → Host

```bash id="copyjjl"
docker cp container-id:/app/file.txt .
```

---

# 15. Health & Events

## Docker Events

```bash id="eventjjm"
docker events
```

---

## Check Health Status

```bash id="healthjjn"
docker inspect --format='{{.State.Health.Status}}' container-id
```

---

# 16. Useful Real-World Commands

## Remove All Containers

```bash id="realjjo"
docker rm -f $(docker ps -aq)
```

Windows PowerShell:

```powershell id="realjjp"
docker ps -aq | % { docker rm -f $_ }
```

---

## Remove All Images

```bash id="realjjq"
docker rmi -f $(docker images -aq)
```

---

## Enter Running Java Container

```bash id="realjjr"
docker exec -it container-id sh
```

---

## Check Container IP

```bash id="realjjs"
docker inspect -f '{{range.NetworkSettings.Networks}}{{.IPAddress}}{{end}}' container-id
```

---

# 17. Multi-Architecture

## Build Multi-Arch Image

```bash id="multijjt"
docker buildx build --platform linux/amd64,linux/arm64 .
```

---

# 18. Docker Context

## List Contexts

```bash id="contextjju"
docker context ls
```

---

## Switch Context

```bash id="contextjjv"
docker context use default
```

---

# 19. Debugging Commands

## Inspect Layers

```bash id="debugjjw"
docker history image-name
```

---

## Detailed Container Info

```bash id="debugjjx"
docker inspect container-id
```

---

## Monitor Resource Usage

```bash id="debugjjy"
docker stats
```

---

# 20. Most Important Commands For Daily Use

```bash id="dailyjjz"
docker ps
docker images
docker build -t app .
docker run -p 8080:8080 app
docker logs -f container
docker exec -it container sh
docker stop container
docker rm container
docker rmi image
docker compose up -d
docker compose down
docker system prune -a
```

---

# Real Production Skills To Practice

Focus heavily on:

* logs
* networking
* volumes
* image optimization
* debugging
* health checks
* docker compose
* multi-stage builds
* container security
* resource limits
