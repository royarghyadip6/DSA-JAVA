
---

# 📝 Maven – Notes (Quick Revision)

---

## 🔹 What is Maven?

**Apache Maven** is a **build automation and dependency management tool** used primarily for Java projects.

👉 It helps to:

* Build projects
* Manage dependencies
* Standardize project structure

---

## 🔹 Why Maven is Used?

* Automatically downloads dependencies
* Handles project build lifecycle
* Reduces manual jar management
* Ensures consistent builds across environments

---

## 🔹 Key Concepts

---

### ✅ 1. `pom.xml` (VERY IMPORTANT 🔥)

👉 Project Object Model (POM)

Contains:

* Project info (name, version)
* Dependencies
* Plugins
* Build configuration

Example:

```xml
<project>
    <groupId>com.example</groupId>
    <artifactId>demo</artifactId>
    <version>1.0</version>
</project>
```

---

### ✅ 2. Dependencies

👉 External libraries required by project

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

👉 Maven downloads from central repository automatically

---

### ✅ 3. Maven Repository

Types:

* Local (on your system)
* Central (online default repo)
* Remote (custom repo)

---

### ✅ 4. Maven Build Lifecycle (VERY IMPORTANT 🔥)

```
validate → compile → test → package → install → deploy
```

👉 Common commands:

```bash
mvn compile
mvn test
mvn package
mvn install
```

---

### ✅ 5. Plugins

👉 Used to perform tasks like:

* Compile code
* Run tests
* Create JAR/WAR

Example:

```xml
<plugin>
    <artifactId>maven-compiler-plugin</artifactId>
</plugin>
```

---

### ✅ 6. Standard Directory Structure

```
src/main/java       → Application code
src/main/resources  → Config files
src/test/java       → Test code
target/             → Compiled output
```

---

## 🔹 Maven Coordinates (IMPORTANT)

```xml
<groupId>com.example</groupId>
<artifactId>demo</artifactId>
<version>1.0</version>
```

👉 These uniquely identify a project.

---

## 🔹 Advantages

* Easy dependency management
* Standard project structure
* Build automation
* Integration with CI/CD tools

---

## 🔹 Common Commands

```bash
mvn clean       # Deletes target folder
mvn compile     # Compiles code
mvn test        # Runs tests
mvn package     # Creates JAR/WAR
mvn install     # Installs in local repo
```

---

## 🔹 Interview Questions

---

# 🧠 Maven – Answer Set

---

## 🟢 Basic

---

### ✅ What is Maven?

👉 **Apache Maven** is a tool used for:

* Building Java projects
* Managing dependencies
* Standardizing project structure

---

### ✅ What is `pom.xml`?

👉 `pom.xml` (Project Object Model) is the **core configuration file** in Maven.

It contains:

* Project details (name, version)
* Dependencies
* Plugins
* Build configuration

---

## 🟡 Moderate

---

### ✅ What is Maven lifecycle?

👉 Maven lifecycle is a **sequence of build phases**:

```text
validate → compile → test → package → install → deploy
```

👉 Each phase executes the previous ones automatically.

---

### ✅ What are dependencies?

👉 Dependencies are **external libraries (JARs)** required by your project.

Example:

* Spring Boot libraries
* Logging libraries

👉 Maven downloads them automatically from repositories.

---

## 🔴 Tricky

---

### ✅ Difference between `install` and `deploy`?

| Feature  | install                  | deploy            |
| -------- | ------------------------ | ----------------- |
| Location | Local repository (`.m2`) | Remote repository |
| Usage    | Local development        | Sharing with team |
| Scope    | Personal                 | Organization-wide |

👉

* `install` → stores artifact locally
* `deploy` → uploads to remote repo

---

### ✅ What is transitive dependency?

👉 Dependencies required by your dependencies are **automatically included**.

Example:

```text
A → B → C
```

👉 If you add A → B and C also get added automatically.

---

### ✅ What is dependency scope?

👉 Defines **when a dependency is available**.

| Scope    | Description                       |
| -------- | --------------------------------- |
| compile  | Default, available everywhere     |
| test     | Only for testing                  |
| provided | Provided by server (not packaged) |
| runtime  | Needed only at runtime            |

---

## 🔥 Final Interview Tip

👉 If interviewer asks multiple together:

> Maven is a build and dependency management tool that uses pom.xml to manage dependencies and executes a lifecycle of phases to build and deploy applications.

---

# 🧠 Maven + Spring Boot – Interview Q&A

---

## 🟢 Basic Level

---

### 1. How does Maven help in a Spring Boot project?

👉 Answer:

> **Apache Maven** manages dependencies, builds the project, and packages the Spring Boot application into a runnable JAR/WAR.

---

### 2. What is the role of `pom.xml` in Spring Boot?

👉 Answer:

> It defines dependencies, plugins, and build configuration required to run the **Spring Boot application**.

---

### 3. What is Spring Boot Starter Parent?

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
</parent>
```

👉 Answer:

> It provides default configurations, dependency versions, and plugin management.

---

## 🟡 Moderate Level

---

### 4. What are Spring Boot starter dependencies?

👉 Answer:

> Predefined Maven dependencies that include all required libraries for a feature (e.g., web, JPA).

---

### 5. What is Spring Boot Maven Plugin?

```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
</plugin>
```

👉 Answer:

> It allows packaging the application as an **executable JAR** with embedded server.

---

### 6. How do you run a Spring Boot app using Maven?

```bash
mvn spring-boot:run
```

---

### 7. How does Maven manage Spring Boot dependency versions?

👉 Answer:

> Using `spring-boot-starter-parent` or dependency management, which ensures compatible versions.

---

## 🔴 Advanced / Real Interview

---

### 8. What happens when you run `mvn clean install` in a Spring Boot project?

👉 Answer:

```
clean → deletes target
compile → compiles code
test → runs tests
package → creates JAR
install → stores in local repo
```

👉 Spring Boot plugin creates **fat JAR (with dependencies)**

---

### 9. What is a fat JAR (Uber JAR)?

👉 Answer:

> A JAR file that contains:

* Application code
* All dependencies
* Embedded server (like **Apache Tomcat**)

👉 So it can run independently:

```bash
java -jar app.jar
```

---

### 10. How does Spring Boot work with Maven internally?

👉 Answer:

> Maven builds the project and resolves dependencies, while Spring Boot uses those dependencies to auto-configure the application at runtime.

---

### 11. How do you exclude a dependency in Spring Boot Maven?

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <exclusions>
        <exclusion>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-tomcat</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

---

### 12. What is dependency management in Spring Boot?

👉 Answer:

> It allows defining dependency versions centrally so child dependencies don’t need versions.

---

### 13. Why do we not specify versions in Spring Boot dependencies?

👉 Answer:

> Because `spring-boot-starter-parent` manages versions internally.

---

### 14. What is difference between JAR and WAR in Spring Boot?

| JAR             | WAR                   |
|-----------------|-----------------------|
| Standalone      | Needs external server |
| Embedded server | Deployed to server    |
| Recommended     | Less used             |

---

### 15. How do you change server (Tomcat → Jetty)?

👉 Answer:

* Exclude Tomcat
* Add Jetty dependency

---

## 🔥 Real Scenario Questions

---

### 16. Build fails due to dependency conflict. What will you do?

👉 Answer:

* Use:

```bash
mvn dependency:tree
```

* Identify conflict
* Override version or exclude dependency

---

### 17. How do you skip tests in Spring Boot Maven build?

```bash
mvn clean install -DskipTests
```

---

### 18. How do you speed up Maven build?

👉 Answer:

* Skip tests
* Use local repo
* Use parallel build

---

## 🔥 Ultimate Interview Tip

👉 If interviewer asks:

**“How does Maven and Spring Boot work together?”**

Answer:

> Maven handles build and dependency management, while Spring Boot uses those dependencies for auto-configuration and runs the application with an embedded server.

---

## 🔹 Important Concepts (Must Know 🔥)

### 👉 Transitive Dependency

* Dependencies of dependencies are automatically included

---

### 👉 Dependency Scope

| Scope    | Meaning            |
| -------- | ------------------ |
| compile  | Default            |
| test     | Only for testing   |
| provided | Provided by server |
| runtime  | Needed at runtime  |

---

## 🔹 Interview One-Liner

👉

> Maven is a build and dependency management tool that automates project build, dependency handling, and lifecycle management.

---