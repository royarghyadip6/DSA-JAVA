# ⚡ Validation in Spring

---

# 🔹 1. What is Validation?

👉

> Validation ensures that input data is **correct, safe, and follows business rules** before processing.

---

## 🧠 Why needed?

* Prevent invalid data
* Avoid errors in DB
* Improve security

---

## 📌 Example

```java
age = -5 ❌
email = "abc" ❌
```

👉 Should be rejected ✔

---

# 🔹 2. Validator Interface

---

## 👉 What is it?

> `Validator` is a Spring interface used to **implement custom validation logic manually**.

---

## 📌 Methods

```java
boolean supports(Class<?> clazz);
void validate(Object target, Errors errors);
```

---

## 🔍 Meaning

* `supports()` → checks which class it supports
* `validate()` → contains validation logic

---

---

## 📌 Example

### Model

```java
class User {
    private String name;
    private int age;

    // getters/setters
}
```

---

### Validator

```java
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class UserValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return User.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {

        User user = (User) target;

        if (user.getName() == null || user.getName().isEmpty()) {
            errors.rejectValue("name", "name.empty", "Name is required");
        }

        if (user.getAge() < 18) {
            errors.rejectValue("age", "age.invalid", "Age must be >= 18");
        }
    }
}
```

---

## 📌 Usage

```java
User user = new User();
user.setAge(15);

UserValidator validator = new UserValidator();

Errors errors = new BeanPropertyBindingResult(user, "user");

validator.validate(user, errors);

if (errors.hasErrors()) {
    errors.getAllErrors().forEach(System.out::println);
}
```

---

## 🧠 Key Point

✔ Full control over validation
❌ More manual work

---

# 🔹 3. Basic Validation Concept (JSR-303 / Bean Validation)

👉 Modern approach (most used in Spring Boot)

---

## 📌 Use Annotations

```java
import jakarta.validation.constraints.*;

class User {

    @NotNull
    private String name;

    @Min(18)
    private int age;

    @Email
    private String email;
}
```

---

## 📌 Controller Example

```java
@RestController
class UserController {

    @PostMapping("/user")
    public String createUser(@Valid @RequestBody User user) {
        return "User created";
    }
}
```

---

## 🧠 How it works

* `@Valid` triggers validation
* Spring checks annotations
* If invalid → error returned

---

# 🔹 Common Annotations

| Annotation      | Meaning           |
| --------------- | ----------------- |
| `@NotNull`      | Cannot be null    |
| `@NotEmpty`     | Not empty         |
| `@Size`         | Length constraint |
| `@Min` / `@Max` | Number range      |
| `@Email`        | Valid email       |

---

# 🔥 4. Handling Validation Errors

```java
@PostMapping("/user")
public String createUser(
    @Valid @RequestBody User user,
    BindingResult result) {

    if (result.hasErrors()) {
        return result.getAllErrors().toString();
    }

    return "Success";
}
```

---

# ⚡ Validator vs Annotation-Based Validation

| Feature     | Validator Interface | Annotation Validation |
| ----------- | ------------------- | --------------------- |
| Style       | Manual              | Declarative           |
| Flexibility | High                | Moderate              |
| Usage       | Rare                | Very common           |

---

# ⚡ Important Interview Points

* `Validator` = manual validation
* `@Valid` = automatic validation
* Uses **Hibernate Validator (JSR-303)**
* `BindingResult` stores errors

---

# ❗ Common Mistakes

* Forgetting `@Valid`
* Not handling `BindingResult`
* Using wrong annotations

---

# 🎯 One-Line Answer

> “Spring validation can be done using the Validator interface for custom logic or using annotation-based JSR-303 validation with @Valid for automatic validation.”

---

# 🚀 Real Use Cases

* Form validation
* API request validation
* Input sanitization

---