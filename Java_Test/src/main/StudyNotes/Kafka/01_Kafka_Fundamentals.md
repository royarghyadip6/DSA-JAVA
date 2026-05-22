# 1. Why [Kafka](https://www.geeksforgeeks.org/apache-kafka/what-is-apache-kafka-and-how-does-it-work/) Exists

Apache Kafka was created to solve problems in large-scale distributed systems where services need to communicate reliably, asynchronously, and at very high speed.

Before Kafka, systems mostly used direct synchronous communication like REST APIs.

As systems grew:

* Traffic increased
* Services became dependent on each other
* Failures spread across systems
* Scaling became difficult

Kafka solves these problems using event streaming and asynchronous communication.

---

# Problems with Synchronous REST Calls

## What is Synchronous Communication?

In synchronous communication:

* Service A sends request to Service B
* Service A waits for response
* Until response comes, request is blocked

Example:

```text id="k1"
Order Service → Payment Service → Response Wait
```

---

# Real Example

Suppose an E-commerce Application has:

* Order Service
* Payment Service
* Inventory Service
* Notification Service

When order is placed:

```text id="k2"
Order Service
   ↓
Payment Service
   ↓
Inventory Service
   ↓
Notification Service
```

Each service waits for the next service.

---

# Problems in REST-Based Communication

---

## 1. Tight Coupling

Services become dependent on each other.

If Payment Service is down:

* Order Service also fails

### Problem

One service failure affects entire system.

---

## 2. Increased Response Time

Every API call adds delay.

Example:

```text id="k3"
Order API → 200ms
Payment API → 300ms
Inventory API → 150ms
Notification API → 250ms
```

Total response becomes slow.

---

## 3. Cascading Failure

If one service becomes slow:

* Other services also become slow
* Threads become blocked
* Entire system may crash

This is called:

## Cascading Failure

---

## 4. Poor Scalability

During high traffic:

* Too many REST requests
* Server overload
* Thread exhaustion

---

## 5. No Retry Mechanism

If request fails:

* Data may be lost
* Order may fail permanently

---

# Why Messaging Systems Are Needed

Messaging systems solve these issues using asynchronous communication.

Instead of direct service-to-service calls:

```text id="k4"
Producer → Message Broker → Consumer
```

Services communicate through messages.

---

# Advantages of Messaging Systems

---

## 1. Loose Coupling

Producer does NOT know:

* Who consumes message
* When message is consumed

Services become independent.

---

## 2. Asynchronous Processing

Producer sends message and continues work.

No waiting.

---

## 3. Reliability

Messages are stored safely in broker.

Even if consumer crashes:

* Message remains محفوظ (stored safely)

---

## 4. Scalability

Consumers can be scaled horizontally.

Example:

```text id="k5"
Topic
  ↓
Consumer 1
Consumer 2
Consumer 3
```

---

## 5. Retry Capability

Failed messages can be retried later.

---

# Why Kafka Became Popular

Kafka provides:

* High throughput
* Distributed architecture
* Fault tolerance
* Persistent storage
* Real-time streaming
* Scalability

Kafka can process:

* Millions of messages per second

---

# Queue vs Publish-Subscribe (Pub-Sub)

These are two messaging models.

---

# 1. Queue Model

In Queue:

* One message is consumed by ONLY ONE consumer.

Example:

```text id="k6"
Queue
 ↓
Worker 1
Worker 2
Worker 3
```

If Worker 1 consumes message:

* Other workers cannot consume same message.

---

## Queue Characteristics

### Used For

* Task processing
* Background jobs

### Example

* Email sending
* Image processing

---

# 2. Publish-Subscribe Model (Pub-Sub)

In Pub-Sub:

* Multiple consumers receive SAME message.

Example:

```text id="k7"
Topic
 ├── Notification Service
 ├── Analytics Service
 └── Inventory Service
```

One event can be consumed by many services.

---

# Pub-Sub Characteristics

### Used For

* Event-driven systems
* Real-time streaming

### Example

Order Created Event:

* Notification service sends email
* Analytics service updates dashboard
* Inventory service updates stock

---

# Kafka Uses Both Queue + Pub-Sub

Kafka combines:

* Pub-sub model
* Consumer group model

Example:

```text id="k8"
Order Topic
   ↓
Consumer Group A
   ↓
One consumer processes message

Consumer Group B
   ↓
Another independent service processes same message
```

This is one of Kafka’s biggest strengths.

---

# Event-Driven Architecture (EDA)

In Event-Driven Architecture:

* Services communicate using events.

Instead of:

```text id="k9"
Service A directly calls Service B
```

We use:

```text id="k10"
Service A publishes event
Other services react to event
```

---

# What is an Event?

An event means:

> Something important happened in the system.

Examples:

* Order Created
* Payment Completed
* User Registered
* Product Shipped

---

# Example Flow

```text id="k11"
Order Created Event
        ↓
Kafka Topic
        ↓
Payment Service
Inventory Service
Notification Service
Analytics Service
```

Each service works independently.

---

# Benefits of Event-Driven Architecture

---

## 1. Loose Coupling

Services are independent.

---

## 2. Better Scalability

Services scale independently.

---

## 3. Better Fault Tolerance

If Notification Service fails:

* Payment still works

---

## 4. Real-Time Processing

Events processed instantly.

---

## 5. Easier Microservices Communication

Kafka becomes central communication hub.

![Image](https://images.openai.com/static-rsc-4/0cfwMdS3DWOCLzJwG3aH52zVsYU-KzlUehIGch_GzX_dqf6jloUbVO7LQmPuNDAAO8uyTlTaeg3ltxYmZtoYNtW6ZMVGp5_5twWRsLFKHIolPHMelCwo3q7r0vtZUl6errUyt1Y2bS3T-URgC3U_M2FwVZgtk37KU2SJEXMLPH7gUyvXIwFqbdOGg3LgFCkU?purpose=fullsize)

![Image](https://images.openai.com/static-rsc-4/G_k7M5KFK1j7NxEUhrDpiQiKE1qYU-FA1AxOiYhPq1t9gRh-YuXgDAXH1TpcJXGtL75x9FpTslhAlzilTONajoVBJpAYXupFXMa_gB2VPKpYLTkgR2wkCoslGHNjGN_ubO6JlTB-TacYqExE3M8fDQF2LdZxjz6o4nTiqxuomIgSfsJDBCEK-QGTTzBozO0b?purpose=fullsize)

![Image](https://images.openai.com/static-rsc-4/TyPwxzppnT37EOFaR6-uHlVPbYuOpaiSIlUPXBsyMzCHuK4C30ZBS5Imm02lSc6LCr89NLL8qMEYFClXJQqjn8MKgx7xdeCs4Bw4_fDmwMBVfvfgBovAvlOURlt8oN5oN3zdREJL4hIRaf0tL9StsGCro3t4e-c9xaf4cPsu_2C35uVcM1i0WAFnsIFe9zMw?purpose=fullsize)

![Image](https://images.openai.com/static-rsc-4/wC9bwNL3NHUxbwtmV97QLt9ZRULx3gShcsVDrhvaxCWR1Bzn7hAqI1FvpKOvxq_bAG5lS2LEbmviyE66jmIUnbdIyZAT544qRHqpOVr5_dokUlUiYtTZ_fAkk0u4ro7nUaQjQJtH75i48MC0ajxOZRxitIZTcivxMb2D0a79v7dTYInM0KlKYQynQ83Y66mF?purpose=fullsize)

![Image](https://images.openai.com/static-rsc-4/xqVbh97UxK5l3ynzuh9dqN2D2urvjDABIZ_8sug9FiMULXrnPFYdfA3tormNtOo2FBTb9OQRtRcK9yF_VGx87wVRKqTdBzhTc1oGhjn86oqnxTtdnCXZrTDzzpeUNJ1fEMKtxe3nA0EoO0P_nb9bM_gB7tClLt6vCHsL1imX1ZBE5Aii4GsoU6HXH8tDo7si?purpose=fullsize)

![Image](https://images.openai.com/static-rsc-4/ZYw0o88MLQQRCNqG1oadLIlDzo1rinTh_W3ErvxdVYPc7kgJun9DkaFv_ajTQ8JVcodUfJVbzK3LmIFhMsQ0-x6pPKA6N0iJReiF2WXWvrcBvRsUsT0yjmTxltnYvhS2rjJTdI9LUtcIYN2pDDohIkVHp6nvZZRPvJCNMGhCwLZnfbilwtveaKuFtnxhsP5q?purpose=fullsize)

![Image](https://images.openai.com/static-rsc-4/E9rAhljmy8P4qS508_kPRnIV_dYkXrnAMI0LmoiWlLacLZ6ocr-8qd_1FsMnbU66pB2BFhZHVd-QC1QYHG3m06XidAGewy_hR3069BCAR4jC78eULPjObo3uhMqatQaCZv_7-1HXIkgDBX6rpE4S5fr2a6Ulk9aX9w_jvhJYxxllbzhdbhsfCwOHcT___dUh?purpose=fullsize)

---

# Core Kafka Terminologies

---

# 1. Producer

A Producer is:

> Application that sends messages to Kafka.

Example:

* Order Service
* Payment Service

---

## Producer Responsibilities

* Create message
* Choose topic
* Send event to Kafka

---

## Example

```text id="k12"
Order Service
    ↓
Produces:
"Order Created"
```

---

# 2. Consumer

A Consumer is:

> Application that reads messages from Kafka.

Example:

* Notification Service
* Analytics Service

---

## Consumer Responsibilities

* Read message
* Process message
* Commit offset

---

## Example

```text id="k13"
Notification Service
    ↓
Consumes:
"Order Created"
```

---

# 3. Message Broker

A Message Broker is:

> System that stores and transfers messages between producer and consumer.

Kafka itself is the broker.

---

## Broker Responsibilities

* Store messages
* Deliver messages
* Manage partitions
* Handle replication

---

# Real Flow

```text id="k14"
Producer
   ↓
Kafka Broker
   ↓
Consumer
```

---

# 4. Topic

A Topic is:

> Logical category/channel where messages are stored.

Example Topics:

* order-topic
* payment-topic
* user-topic

---

# Topic Example

```text id="k15"
Topic: order-topic

Messages:
Order1
Order2
Order3
```

---

# Important Point

Topics are divided into:

## Partitions

Partitions provide:

* Scalability
* Parallel processing

---

# 5. Event

An Event is:

> Record describing something that happened.

---

# Event Structure

Usually contains:

* Key
* Value
* Timestamp
* Metadata

---

# Example Event

```json id="k16"
{
  "orderId": 101,
  "status": "CREATED",
  "amount": 500
}
```

This event means:

* An order was created.

---

# End-to-End Kafka Flow

```text id="k17"
Order Service
   ↓
Producer
   ↓
Kafka Topic
   ↓
Kafka Broker
   ↓
Consumer Group
   ↓
Notification Service
Analytics Service
Inventory Service
```

---

# Real-World Example

## User Places Order

### Step 1

Order Service publishes:

```json id="k18"
{
  "event": "ORDER_CREATED"
}
```

### Step 2

Kafka stores event in topic.

### Step 3

Consumers read event.

### Step 4

Different services react independently.

---

# Key Takeaways

## Kafka Exists Because:

* REST communication has scalability limitations
* Distributed systems need async communication
* Systems require fault tolerance
* Services should be loosely coupled

---

# Important Concepts Summary

| Concept  | Meaning                        |
|----------|--------------------------------|
| Producer | Sends messages                 |
| Consumer | Reads messages                 |
| Broker   | Stores/transfers messages      |
| Topic    | Message category               |
| Event    | Something happened             |
| Queue    | One consumer gets message      |
| Pub-Sub  | Multiple consumers get message |
| EDA      | Communication using events     |
