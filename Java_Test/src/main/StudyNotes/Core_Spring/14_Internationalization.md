Good—**i18n** is a favorite “practical + configuration” topic. If you understand **MessageSource + how Spring resolves locale**, you’re solid.

---

# 🌍 Internationalization (i18n) in Spring

## 🔹 What is i18n?

> i18n (Internationalization) is the process of designing an application to support **multiple languages and regions** without changing code.

✔ Same code → different languages
✔ Based on **Locale (language + country)**

---

# 🔹 1. MessageSource

## 👉 What is it?

> `MessageSource` is a Spring interface used to **resolve messages from property files** based on locale.

---

## 📌 Why needed?

Instead of hardcoding:

```java
"Welcome User"
```

👉 Use:

```text
welcome.message=Welcome User
```

✔ Then translate for different languages

---

## 📌 Example Files

### messages.properties (default)

```properties
welcome.message=Welcome
```

---

### messages_fr.properties (French)

```properties
welcome.message=Bienvenue
```

---

### messages_hi.properties (Hindi)

```properties
welcome.message=स्वागत है
```

---

## 📌 Configuration (Spring Boot)

```properties
spring.messages.basename=messages
```

✔ Spring automatically loads files

---

---

# 🔹 2. Using MessageSource in Code

## 📌 Example

```java
import org.springframework.context.MessageSource;
import java.util.Locale;

@Autowired
private MessageSource messageSource;

public void printMessage() {
    String msg = messageSource.getMessage(
        "welcome.message",
        null,
        Locale.ENGLISH
    );

    System.out.println(msg);
}
```

---

## 🧠 Parameters

```java
getMessage(String code, Object[] args, Locale locale)
```

* `code` → key
* `args` → dynamic values
* `locale` → language

---

---

# 🔹 3. Multi-language Support

---

## 📌 Locale

👉 Represents:

* Language → `en`, `fr`, `hi`
* Country → `US`, `IN`

```java
Locale locale = new Locale("fr");
```

---

## 📌 Switching Language

```java
Locale locale = new Locale("hi");
```

👉 Output:

```text
स्वागत है
```

---

---

# 🔹 4. Using in Controller (Spring Boot)

```java
@RestController
public class DemoController {

    @Autowired
    private MessageSource messageSource;

    @GetMapping("/msg")
    public String getMessage(Locale locale) {
        return messageSource.getMessage(
            "welcome.message",
            null,
            locale
        );
    }
}
```

✔ Locale auto-detected from request

---

---

# 🔹 5. Passing Dynamic Values

### messages.properties

```properties
welcome.user=Welcome {0}
```

---

### Code

```java
String msg = messageSource.getMessage(
    "welcome.user",
    new Object[]{"John"},
    Locale.ENGLISH
);
```

👉 Output:

```text
Welcome John
```

---

---

# 🔹 6. Locale Resolver (Important)

👉 Determines user language

---

## ✔ Types

* `SessionLocaleResolver`
* `CookieLocaleResolver`
* `AcceptHeaderLocaleResolver` (default in Spring Boot)

---

## 📌 Example

```java
@Bean
public LocaleResolver localeResolver() {
    return new SessionLocaleResolver();
}
```

---

---

# 🔹 7. Changing Language via URL

```java
@Bean
public LocaleChangeInterceptor interceptor() {
    LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
    interceptor.setParamName("lang");
    return interceptor;
}
```

👉 URL:

```text
http://localhost:8080/msg?lang=fr
```

---

---

# ⚡ Important Interview Points

* `MessageSource` → loads messages
* Uses `.properties` files
* Locale decides language
* Supports placeholders `{0}`
* Default locale from request

---

# ❗ SpEL vs i18n (Common Confusion)

* SpEL → dynamic expressions
* i18n → language translation

---

# 🎯 One-Line Answer

> “Spring i18n uses MessageSource to resolve messages from property files based on locale, enabling multi-language support.”

---

# 🚀 Real Use Cases

* Multi-language websites
* Error messages
* UI labels
* Notifications

---
