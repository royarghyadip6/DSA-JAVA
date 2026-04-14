# 🧠 TOPIC 2: Spring Core (IoC & Dependency Injection)

---

### 1. IoC (Inversion of Control)

**Inversion of Control (IOC)**: 
> The Spring IoC Container is the core of the Spring Framework. It is a design principle where the control of object creation and lifecycle is managed by a framework or container rather than by the developer. Spring IOC Container is responsible for creating, configuring, and managing the lifecycle of objects called beans.
 
The IoC Container retrieves object configuration from:

* XML Configuration Files
* Java Configuration Classes
* Java Annotations

> Since object creation and lifecycle management are handled by the IoC Container, developers do not need to manually instantiate dependencies. This reduces tight coupling in the application.

* Concept: Control is given to the container instead of developer
* Container manages object creation

👉 Example:

* ❌ Without Spring → `new Object()`
* ✅ With Spring → container creates object

---

### 2. DI (Dependency Injection)

**Dependency Injection**: 
> Dependency Injection (DI) is a key feature provided by Spring IoC. It is a design pattern and a part of IOC container. The Spring Core module injects dependencies into objects via different injection methods, ensuring that components are loosely coupled.

* Inject dependencies instead of creating them manually by developer.

Types of DI:
* **Constructor Injection**: Dependencies are provided through the class constructor. (Recommended for mandatory dependencies).
* **Setter Injection**: Dependencies are provided through public setter methods. (Good for optional dependencies).
* **Field Injection**: Dependencies are injected directly into fields using @Autowired. (Common in Spring Boot, though less preferred for unit testing).


1. Setter Dependency Injection (SDI)
   > In Setter Injection, dependencies are injected using setter methods. The property to be injected is declared inside the <property> tag in the XML configuration file.

Example:
```xml
<bean id="myBean" class="com.example.MyClass">
    <property name="dependency" ref="myDependency"/>
</bean>
```

2. Constructor Dependency Injection (CDI)
   > In Constructor Injection, dependencies are passed via the class constructor. The dependency is set using the <constructor-arg> tag in the XML configuration file.

Example:
```xml
<bean id="myBean" class="com.example.MyClass">
    <constructor-arg ref="myDependency"/>
</bean>
```

---

### 3. Spring Container

* Responsible for:

    * Creating objects (beans)
    * Managing lifecycle
    * Injecting dependencies

Types:

* BeanFactory (basic)
* ApplicationContext (advanced, mostly used)

---

### 4. Beans

* Objects managed by Spring container

Annotations:

* `@Component`
* `@Service`
* `@Repository`
* `@Controller`

---

### 5. Bean Scope

* Singleton (default)
* Prototype
* Request
* Session

---

### 6. Spring IOC vs Spring DI

| **Spring IoC (Inversion of Control)**                                                                                                                               | **Spring Dependency Injection (DI)**                                                                                                                               |
|---------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Spring IoC Container is the core of the Spring Framework. It creates the objects, configures and assembles their dependencies, and manages their entire life cycle. | Spring Dependency Injection is a way to inject the dependency of a framework component by the following ways of spring: Constructor Injection and Setter Injection |
| Spring helps in creating objects, managing objects, configurations, etc. because of IoC (Inversion of Control).                                                     | Spring framework helps in the creation of loosely-coupled applications because of Dependency Injection.                                                            |
| Spring IoC is achieved through Dependency Injection.                                                                                                                | Dependency Injection is the method of providing the dependencies, and Inversion of Control is the end result of Dependency Injection.                              |
| IoC is a design principle where the control flow of the program is inverted.                                                                                        | Dependency Injection is one of the subtypes of the IOC principle.                                                                                                  |
| Aspect-Oriented Programming is one way to implement Inversion of Control.                                                                                           | In case of any changes in business requirements, no code change is required.                                                                                       |


## 🎯 Interview Questions

### 🟢 Basic

1. What is IoC?
2. What is Dependency Injection?
3. What is a Spring Bean?
4. What is ApplicationContext?

### 🟡 Moderate

5. Difference between BeanFactory and ApplicationContext?
6. Types of Dependency Injection?
7. What is bean scope?
8. Difference between `@Component`, `@Service`, `@Repository`?

### 🔴 Tricky / Real Interview

9. Why is constructor injection preferred over field injection?
10. What happens if two beans of same type exist?
11. How does Spring resolve dependency injection internally?
