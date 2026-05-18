# Tell Me About Yourself

I am a Java backend developer with around 5+ years of experience, primarily working in Java, Spring Boot, microservices, Kafka, and backend system development.

Currently, I am working at Tata Consultancy Services for the client Nokia in the telecom domain. I am part of the WSNOC project, where I work on the Ethernet Service Manager module, which handles Layer 2 network services like ERP, LAG, MC-LAG, and tunnel management.

In my current role, I am mainly responsible for:

* backend feature development using Java and Spring Boot
* Kafka-based microservice communication
* requirement analysis and solution implementation
* database interactions and API development
* debugging production and post-production issues
* deployment support and issue resolution

Technically, I have experience working with:

* Java
* Spring Boot
* Microservices
* Kafka
* REST APIs
* SQL
* Docker
* Git
* Linux

Over the years, I have developed strong interest in:

* distributed systems
* backend architecture
* JVM internals
* performance optimization
* scalable system design

One thing I particularly enjoy is debugging complex production issues and understanding systems internally rather than just implementing features.

I would describe myself as someone who is:

* technically curious
* calm under pressure
* responsible toward production systems
* continuously eager to learn and improve

Currently, I am looking for opportunities where I can work on larger-scale backend systems, contribute more to architecture-level decisions, and continue growing as a backend engineer and future technical leader.

---

# Tell Me About Your Current Project

Currently, I am working at Tata Consultancy Services for the client Nokia in the telecom domain. The project name is WSNOC, which stands for Web Suite Network Operations Center. It is mainly used for monitoring and managing telecom network services.

I am primarily working on a module called ESM — Ethernet Service Manager. This module is responsible for handling and monitoring Layer 2 network services and features such as:

* ERP (Ethernet Ring Protection)
* LAG (Link Aggregation Group)
* MC-LAG (Multi-Chassis Link Aggregation)
* Tunnel services

The overall architecture follows a microservices-based approach. The main services involved are:

* ESM (business service)
* SNA adapter service
* ANI layer for device communication

The request flow works like this:

When a user performs any operation from the UI, the request first comes to the ESM service. ESM contains the core business logic and validation layer. Based on the operation, ESM processes the request and stores the required data into the database.

After that, ESM publishes messages through Kafka to another microservice called SNA. The responsibility of SNA is to convert the business-level or human-readable requests into SNMP commands that can be understood by network devices.

Once the SNMP commands are generated, they are sent to ANI, which communicates with the actual telecom network devices. The device response again comes back through ANI to SNA. Then SNA converts the SNMP-level response back into a human-readable format and sends it back to ESM through Kafka.

Finally:

* ESM updates the database with the latest device status or response
* the processed response is returned back to the UI/client

So overall, the architecture is event-driven and highly asynchronous using Kafka for inter-service communication.

From the deployment perspective, all the microservices are containerized using Docker, and the deployment artifacts are packaged and deployed as Docker containers.

My major responsibilities in this project include:

* developing backend features in Java and Spring Boot
* implementing L2 service functionalities
* working on Kafka-based communication between microservices
* debugging production and post-production issues
* requirement analysis and solution design
* database interactions and API development
* handling critical issue fixes and deployment support

This project gave me strong exposure to:

* microservices architecture
* Kafka messaging
* telecom domain workflows
* distributed systems concepts
* production support and debugging
* containerization using Docker

Most of my work is focused on backend development, system integration, and ensuring reliable communication between services and network devices.

---

# What Are Your Strengths?

One of my biggest strengths is problem-solving and debugging capability, especially in production environments. I stay calm during critical issues and try to analyze problems systematically instead of rushing into fixes.

Another strength is my ownership mindset. In my current telecom project with Nokia through Tata Consultancy Services, I have handled backend development, Kafka integration, production issue debugging, and deployment support responsibilities. I try to understand the complete flow end-to-end rather than focusing only on my module.

I also have a strong learning mindset. Recently, I have been spending time improving my understanding of:

* distributed systems
* JVM internals
* Kafka internals
* Kubernetes
* system design

because I want to grow into a stronger backend engineer and technical leader.

---

# What Is Your Weakness?

Earlier, I used to spend too much time trying to make solutions technically perfect. Sometimes I focused heavily on optimization or cleaner implementation even when a simpler solution was sufficient for the business timeline.

Over time, I learned the importance of balancing:

* engineering quality
* delivery timelines
* business priorities

Now I focus more on delivering practical and maintainable solutions incrementally while continuously improving them later if needed.
