# 2-Day Apache Kafka Roadmap (Basic → Practical → Interview Ready)

Apache Kafka

Goal of this roadmap:

* Understand Kafka architecture clearly
* Build producer/consumer apps
* Learn important interview concepts
* Understand real-world flow
* Become confident enough to use Kafka in microservices

---

# DAY 1 — Kafka Fundamentals + Core Architecture

## 1. Why Kafka Exists

### Learn

* Problems with synchronous REST calls
* Why messaging systems are needed
* Queue vs pub-sub
* Event-driven architecture

### Understand

* Producer
* Consumer
* Message broker
* Topic
* Event

---

# 2. Kafka Core Architecture (MOST IMPORTANT)

## Topics

* Broker
* Cluster
* Topic
* Partition
* Offset
* Producer
* Consumer
* Consumer Group
* Replication
* Leader & Follower

## Focus Deeply On

### Partition

* Why partitions exist
* Parallel processing
* Ordering

### Offset

* How Kafka tracks messages

### Consumer Group

* Load balancing
* Scaling consumers

![Image](https://images.openai.com/static-rsc-4/lCtS2pnnD1r5XjUQI8ZjoOvk_DyoxbYgB9d8Y6pt_lLVN8X-6nTXY1bDcwOImQolQex_3xBQwPeyt0wWnxEPypMh3ps2arfAqgMZk9LgX15cTffFKwUC4HdbUgSOrehbLDDdNJN9XMQ0G-KJ91-rg75OGLLlepkIWP_KcxsF7_fE_sh_QlWMg5Lu1NrMtm9K?purpose=fullsize)

![Image](https://images.openai.com/static-rsc-4/UcuEJaFJ9KSW68rx2pBNL5sSp_7aP3nw4qIC0ChNyTxxDVIjcgfNOkA-99gmAo1ZINMaXYTPmXZKZpPJef99z40xis8vA1HE8cXTsljry_OnyJcRYCk9BMjasdgIZJi-gAya6ulovmyYBc5OOr4gsxBn74zvJeh1u3qLRBGcJv3oDahatoSQkxmd6srenHih?purpose=fullsize)

![Image](https://images.openai.com/static-rsc-4/V9it1XTQNqjC67sFHkrm-glbKJEINO3ln2kN0bYEETx_ORDsAOE9lWvUgxyfzKTuE0OSNl1p541bykMJENAVaJkdfGXq2HfNLnZiQz3-AyotOzq47nemmRkLTZj9E-n9gvYTtxOLAT5i_8Z8LYhbZC3W-5Scl-vlY_e2zlmCPHgu0KAqrD7WfLKjV_cMYlzW?purpose=fullsize)

![Image](https://images.openai.com/static-rsc-4/QAF7MFrBfJin_wRNa4IvXqOGiPfvyKecQ-FlWHzBN9CH6qqFaXiqH6Hs79deqUqpbyK--R_hv43F4Gibj1sULz5jakpg6UqxGxlBjS629359SossSU6dSZoQ7bGAHCDxY-pxHhYK7z0wN50XJgcKnF7l_KtvanPvzg2OZx0aDJh1hEt9BKwXziuleHag7_RH?purpose=fullsize)

![Image](https://images.openai.com/static-rsc-4/mmK-Upu9x2FH-UutIxT6mJp1zEKjZp2EPc4ftueY-qbf313OGBKti30zIA4BiuIpsMuEHr2peYnbiW3jz6B9trz41k2fH8jUtzjta8NMi1N_2GqOOExjWTuDGLAdfjB8JqimHGQ7A7CzMiAf6eV6iQ-_sQ3J4wHxCEuE6ozMDeaAumaRkF41TLSV6LrjY05R?purpose=fullsize)

![Image](https://images.openai.com/static-rsc-4/uAqgxI7tccwAEHp58canYdoBEFrRmQQdqBxTCnJr_Td2SZgITYwBo0NxVsEQsapbeWLWu_57L7uKaQFjnQqxN5iDwAU4BGY4YZBhmt1Jo3smH3CZrDBhslknOvuKET6El6DSvqBDFshBRlq7_WSQzdfCIPviFN-tRJyse80h7WqkvgEZG8640dStyOzeKaJN?purpose=fullsize)

---

# 3. Kafka Internal Flow (Very Important)

## Learn Message Flow

```text id="sy9u38"
Producer → Topic Partition → Broker → Consumer Group → Consumer
```

## Understand

* How producer sends messages
* How broker stores messages
* How consumer reads messages
* How offset moves

---

# 4. Install Kafka Locally

## Learn

* Kafka setup using Docker

## Practice

Run:

```bash id="n6wgvf"
docker compose up
```

## Important Commands

```bash id="e0vpj5"
kafka-topics.sh
kafka-console-producer.sh
kafka-console-consumer.sh
```

---

# 5. Producers Deep Dive

## Learn

* Sending messages
* Key & value
* Serialization
* Partitioning

## Important Concepts

* ACKS
* Retries
* Idempotent producer

## Delivery Semantics

* At-most-once
* At-least-once
* Exactly-once

---

# 6. Consumers Deep Dive

## Learn

* Poll mechanism
* Offset commit
* Auto vs manual commit
* Consumer groups
* Rebalancing

## Important

* Duplicate messages
* Message loss
* Consumer lag

---

# 7. Hands-On Practice

## Build

### Project 1

Order Producer Service

* Sends order events

### Project 2

Order Consumer Service

* Consumes order events

Use:

* Java
* Spring Boot
* Spring Kafka

---

# DAY 2 — Spring Kafka + Advanced Basics + Real-World Concepts

---

# 8. Spring Kafka Basics

## Learn

* KafkaTemplate
* @KafkaListener
* ProducerFactory
* ConsumerFactory

## Build

* Producer API
* Consumer Listener

---

# 9. Error Handling & Reliability

## Learn

* Retry
* Dead Letter Topic (DLT)
* Error handler
* Failed message recovery

---

# 10. Kafka Important Configurations

## Producer Configs

* acks
* retries
* linger.ms
* batch.size

## Consumer Configs

* group.id
* auto.offset.reset
* enable.auto.commit

---

# 11. Kafka Interview Important Topics

## Must Know

* Partition vs consumer group
* Offset management
* Rebalancing
* Replication
* ISR
* Exactly once semantics

---

# 12. Real-World Kafka Patterns

## Learn Basics

* Event-driven microservices
* Saga pattern
* Outbox pattern

---

# 13. Monitoring Basics

## Learn

* Consumer lag
* Broker health
* Under replicated partitions

## Tools

* Grafana
* Prometheus

---

# 14. Final Mini Project

## Build Simple E-Commerce Flow

### Services

* Order Service
* Payment Service
* Inventory Service
* Notification Service

## Flow

```text id="m1ivn0"
Order Created Event
        ↓
Payment Service
        ↓
Inventory Service
        ↓
Notification Service
```

---

# What You Should Be Able To Explain After 2 Days

## Architecture

* How Kafka works internally
* Partition & offset
* Consumer groups
* Replication

## Coding

* Create producer
* Create consumer
* Use Spring Kafka

## Reliability

* ACKS
* Retries
* Delivery guarantees

## Real-World Usage

* Event-driven architecture
* Async communication
* Microservices integration

---

# Best Learning Resources

## Official

[Apache Kafka Documentation](https://kafka.apache.org/documentation/?utm_source=chatgpt.com)

## Spring Kafka

[Spring for Apache Kafka](https://spring.io/projects/spring-kafka?utm_source=chatgpt.com)

## Docker

[Docker Official Website](https://www.docker.com/?utm_source=chatgpt.com)

---

# Recommended Learning Order (Very Important)

```text id="d52v2y"
Messaging Basics
    ↓
Kafka Architecture
    ↓
Topics & Partitions
    ↓
Offsets & Consumer Groups
    ↓
Producer
    ↓
Consumer
    ↓
Spring Kafka
    ↓
Reliability & Retries
    ↓
Real-World Projects
```
