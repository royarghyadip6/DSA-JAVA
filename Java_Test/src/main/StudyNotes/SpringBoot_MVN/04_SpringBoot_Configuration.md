
---

# 🚀 Spring Boot Configurations – Complete Guide

# 📑 Table of Contents

* [🚀 Overview](#-overview)
* [🧠 1. Configuration Files](#-1-configuration-files)
    * [application.properties](#-applicationproperties)
    * [application.yml](#-applicationyml-recommended)
* [⚙️ 2. Configuration Priority (Order)](#-2-configuration-priority-order)
* [🌍 3. Profiles (Environment-based Config)](#-3-profiles-environment-based-config)
* [🔑 4. @Value Annotation](#-4-value-annotation)
* [📦 5. @ConfigurationProperties](#-5-configurationproperties-best-practice)
* [🧩 6. @Configuration Annotation](#-6-configuration-annotation)
* [🔄 7. @Bean Annotation](#-7-bean-annotation)
* [⚡ 8. Auto Configuration](#-8-auto-configuration)
* [🧠 9. @EnableAutoConfiguration](#-9-enableautoconfiguration)
* [🔍 10. @ComponentScan](#-10-componentscan)
* [📌 11. External Configuration Sources](#-11-external-configuration-sources)
* [☁️ 12. Spring Cloud Config (Advanced)](#-12-spring-cloud-config-advanced)
* [🔐 13. Secure Configuration](#-13-secure-configuration)
* [📊 14. Common Properties](#-14-common-properties)
* [🧪 15. Configuration for Testing](#-15-configuration-for-testing)
* [⚙️ 16. Custom Configuration Example](#-16-custom-configuration-example)
* [🚨 17. Common Mistakes](#-17-common-mistakes)
* [🧠 Best Practices](#-best-practices)


## 📌 Overview

Spring Boot configuration allows you to **externalize application settings** (like ports, DB configs, API keys) so you don’t hardcode them. It helps in managing **different environments (dev, test, prod)** easily.

---

# 🧠 1. Configuration Files

## ✅ application.properties

Simple key-value format, easy for small configs.

```properties
server.port=8081
spring.application.name=my-app
```

## ✅ application.yml (Recommended)

Structured, readable, and widely used.

```yaml
server:
  port: 8081

spring:
  application:
    name: my-app
```

👉 YAML is preferred for complex configs due to hierarchy.

---

# ⚙️ 2. Configuration Priority (Order)

Spring Boot loads configurations in priority order:

1. Command-line arguments
2. Environment variables
3. application.properties / application.yml
4. Default values

👉 Higher priority overrides lower ones.

---

# 🌍 3. Profiles (Environment-based Config)

Used to manage multiple environments.

## 📌 Profile Files

```bash
application-dev.yml
application-prod.yml
```

## 📌 Activate Profile

```properties
spring.profiles.active=dev
```

OR

```bash
-Dspring.profiles.active=prod
```

👉 Helps separate configs for dev, test, production.

---

# 🔑 4. @Value Annotation

Injects single property value into variables.

```java
@Value("${server.port}")
private int port;
```

👉 Simple but not ideal for large configs.

---

# 📦 5. @ConfigurationProperties (Best Practice)

Binds multiple properties to a POJO.

```java
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppConfig {
    private String name;
    private String version;

    // getters & setters
}
```

```yaml
app:
  name: MyApp
  version: 1.0
```

👉 Better for structured and scalable configuration.

---

# 🧩 6. @Configuration Annotation

Marks a class as a configuration class.

```java
@Configuration
public class AppConfig {
}
```

👉 Used to define beans manually.

---

# 🔄 7. @Bean Annotation

Creates and manages objects in Spring context.

```java
@Bean
public RestTemplate restTemplate() {
    return new RestTemplate();
}
```

👉 Used when you want manual control over object creation.

---

# ⚡ 8. Auto Configuration

Spring Boot automatically configures components based on dependencies.

Example:

```xml
<dependency>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

👉 Automatically configures:

* Embedded server (Tomcat)
* DispatcherServlet

---

# 🧠 9. @EnableAutoConfiguration

Enables Spring Boot auto-configuration.

```java
@SpringBootApplication
```

👉 Internally includes:

* @Configuration
* @EnableAutoConfiguration
* @ComponentScan

---

# 🔍 10. @ComponentScan

Scans packages for Spring components.

```java
@ComponentScan(basePackages = "com.example")
```

👉 Automatically detects:

* @Component
* @Service
* @Repository
* @Controller

---

# 📌 11. External Configuration Sources

Spring Boot supports multiple config sources:

* Properties files
* YAML files
* Environment variables
* Command-line arguments
* External config servers

👉 Makes apps flexible and environment-independent.

---

# ☁️ 12. Spring Cloud Config (Advanced)

Centralized configuration for microservices.

👉 Benefits:

* Manage configs in one place
* Dynamic updates
* Environment-specific configs

---

# 🔐 13. Secure Configuration

Avoid storing sensitive data in code.

## ❌ Bad Practice

```properties
db.password=123456
```

## ✅ Good Practice

* Use environment variables
* Use secret managers (Vault, AWS Secrets Manager)

---

# 📊 14. Common Properties

```properties
server.port=8080

spring.datasource.url=jdbc:mysql://localhost:3306/db
spring.datasource.username=root
spring.datasource.password=pass

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

👉 These configure server, database, and JPA behavior.

---

# 🧪 15. Configuration for Testing

Use separate config file:

```bash
application-test.yml
```

Activate in test:

```java
@ActiveProfiles("test")
```

👉 Keeps test config isolated from production.

---

# ⚙️ 16. Custom Configuration Example

```java
@Component
public class AppRunner implements CommandLineRunner {

    @Override
    public void run(String... args) {
        System.out.println("Application started!");
    }
}
```

👉 Runs code automatically after app startup.

---

# 🚨 17. Common Mistakes

* ❌ Mixing YAML & properties unnecessarily
* ❌ Hardcoding secrets
* ❌ Not using profiles
* ❌ Overusing @Value
* ❌ Ignoring config structure

---

# 🧠 Best Practices

* ✅ Use YAML for complex configs
* ✅ Use profiles for environments
* ✅ Prefer @ConfigurationProperties
* ✅ Keep secrets external
* ✅ Use centralized config in microservices

---
