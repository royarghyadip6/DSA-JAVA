# Phase 4 — Date & Time API (`java.time`)

**Duration:** 1–2 weeks | **Goal:** Replace legacy Date/Calendar with modern, immutable date-time API

[← Phase 3](03-stream-api-advanced.md) | [Index](README.md) | **Next:** [Phase 5 — Concurrency & Async →](05-concurrency-async.md)

---

## Theory (Easy to Remember)

### 1. Why `java.time` Was Created

Old `Date` and `Calendar` problems:
- **Mutable** — change by accident
- **Not thread-safe**
- **Confusing API** — months 0-based, `Date` has date AND time
- **Poor timezone** support

New API rules:
- **Immutable** — every change returns new object
- **Thread-safe** by design
- **Clear names** — `LocalDate`, `LocalTime`, `ZonedDateTime`
- **ISO-8601** standard by default

**Memory trick:** *"Old Date = messy notebook. java.time = printed calendar page — can't erase, only get new page."*

---

### 2. Pick the Right Type

| Type | Has Date? | Has Time? | Has Zone? | Use When |
|------|-----------|-----------|-----------|----------|
| `LocalDate` | ✅ | ❌ | ❌ | Birthday, due date |
| `LocalTime` | ❌ | ✅ | ❌ | Store opening hours |
| `LocalDateTime` | ✅ | ✅ | ❌ | Meeting without zone |
| `ZonedDateTime` | ✅ | ✅ | ✅ | Global events, audits |
| `Instant` | Timeline point (UTC) | | | Logs, DB timestamps |
| `Duration` | Time amount (hours, seconds) | | | "2 hours 30 min" |
| `Period` | Date amount (years, months, days) | | | "1 year 2 months" |

**Golden rule:** Use `Local*` for business logic; add `ZoneId` only at the boundary (UI, API, DB).

---

### 3. Common Operations Pattern

All follow similar naming:
- **`now()`** — current date/time
- **`of(...)`** — build from parts
- **`parse(...)`** — from string
- **`plus` / `minus`** — add/subtract
- **`with`** — replace one field (returns new object)
- **`isBefore` / `isAfter`** — compare
- **`format(DateTimeFormatter)`** — to string

---

### 4. Time Zones

- **`ZoneId`** — rules for a region (`"Asia/Kolkata"`, `"Europe/Paris"`)
- **`ZoneOffset`** — fixed offset (`+05:30`)
- **`ZonedDateTime`** — handles DST gaps/overlaps
- Store in DB as **UTC `Instant`** or **offset datetime** — not ambiguous local time without zone

---

### 5. Formatting & Parsing

- **`DateTimeFormatter.ofPattern("dd-MM-yyyy")`** — custom pattern
- **`DateTimeFormatter.ISO_LOCAL_DATE`** — standard formats
- **Immutable formatters** — thread-safe, reusable
- **`withLocale`**, **`withZone`** for localization

---

### 6. Sub-packages (Know the Map)

| Package | Purpose |
|---------|---------|
| `java.time` | Core types |
| `java.time.format` | Format/parse |
| `java.time.temporal` | Low-level fields, adjusters (`TemporalAdjusters.next(MONDAY)`) |
| `java.time.chrono` | Non-ISO calendars (Japanese, Thai Buddhist) |
| `java.time.zone` | Zone rules, DST transitions |

---

## Examples

### Basic date arithmetic

```java
LocalDate today = LocalDate.now();
LocalDate nextWeek = today.plusWeeks(1);
LocalDate firstOfMonth = today.with(TemporalAdjusters.firstDayOfMonth());
boolean isWeekend = today.getDayOfWeek() == DayOfWeek.SATURDAY
                 || today.getDayOfWeek() == DayOfWeek.SUNDAY;
```

### Duration vs Period

```java
LocalTime start = LocalTime.of(9, 0);
LocalTime end = LocalTime.of(17, 30);
Duration workHours = Duration.between(start, end); // PT8H30M

LocalDate birth = LocalDate.of(1990, 5, 15);
Period age = Period.between(birth, LocalDate.now()); // P35Y...
```

### ZonedDateTime — global meeting

```java
ZonedDateTime meetingUtc = ZonedDateTime.of(
    LocalDateTime.of(2026, 7, 22, 10, 0),
    ZoneId.of("UTC"));

ZonedDateTime meetingIndia = meetingUtc.withZoneSameInstant(ZoneId.of("Asia/Kolkata"));
// 15:30 in India
```

### Format and parse

```java
DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
String text = LocalDateTime.now().format(formatter);
LocalDateTime parsed = LocalDateTime.parse("22/07/2026 14:30", formatter);
```

### Convert legacy Date

```java
Date oldDate = new Date();
Instant instant = oldDate.toInstant();
LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());

// Back to Date
Date converted = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
```

### Injectable Clock (testing)

```java
Clock fixed = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneId.of("UTC"));
LocalDate testDate = LocalDate.now(fixed); // always 2026-01-01
```

---

## Practice Exercises

| # | Exercise | Difficulty |
|---|----------|------------|
| 1 | Calculate days until next birthday | Easy |
| 2 | Find all Mondays in a given month | Medium |
| 3 | Parse multiple date formats with fallback | Medium |
| 4 | Build scheduler: add 5 business days (skip weekends) | Hard |
| 5 | Convert meeting time NY → London → Tokyo | Medium |
| 6 | Calculate age in years, months, days using `Period` | Easy |
| 7 | Write unit test with fixed `Clock` | Medium |
| 8 | Find overlap between two time ranges (`LocalTime`) | Hard |
| 9 | Format date in Hindi/English locale | Medium |
| 10 | Migrate method using `Calendar` to `java.time` | Hard |

### Exercise 1 — Solution Sketch

```java
LocalDate today = LocalDate.now();
LocalDate birthday = LocalDate.of(today.getYear(), Month.MARCH, 15);
if (!birthday.isAfter(today)) {
    birthday = birthday.plusYears(1);
}
long days = ChronoUnit.DAYS.between(today, birthday);
```

---

## Interview Q&A (5–8 Years Experience)

### Q1. Why is `LocalDateTime` not enough for storing timestamps in a global app?

**Answer:** `LocalDateTime` has **no timezone/offset** — it's ambiguous. "2026-07-22T10:00" could mean different instants in NY vs Tokyo. For global systems, store **`Instant` (UTC)** or **`ZonedDateTime`/`OffsetDateTime`** with zone info. Use `LocalDateTime` only when timezone is implicit (e.g., "store closes at 9 PM local" for one location).

---

### Q2. Difference between `Duration` and `Period`?

**Answer:**
- **`Duration`:** Time-based (seconds, nanos). Based on 24-hour days. Use for time-of-day differences, timeouts.
- **`Period`:** Date-based (years, months, days). Calendar-aware (variable month lengths). Use for ages, billing cycles.

Don't use `Duration.between` on `LocalDate` — use `Period.between`.

---

### Q3. How does `java.time` handle DST gaps and overlaps?

**Answer:** `ZonedDateTime` uses `ZoneRules`:
- **Gap** (spring forward): non-existent local time → `DateTimeException` or adjusted per resolver
- **Overlap** (fall back): same local time twice → two different offsets; `ZonedDateTime` disambiguates

Always convert to **`Instant`** for unambiguous storage.

---

### Q4. Are `DateTimeFormatter` instances thread-safe?

**Answer:** **Yes.** Formatters are immutable. Create once, reuse. Unlike old `SimpleDateFormat` which was **not** thread-safe.

---

### Q5. How do you test time-dependent code?

**Answer:** Inject **`Clock`**:
```java
public class OrderService {
    private final Clock clock;
    public OrderService(Clock clock) { this.clock = clock; }
    public boolean isExpired(Instant expiry) {
        return Instant.now(clock).isAfter(expiry);
    }
}
// Test: Clock.fixed(...)
```

---

### Q6. `Instant` vs `ZonedDateTime` vs `OffsetDateTime`?

**Answer:**
- **`Instant`:** Absolute point on UTC timeline. Best for machine timestamps.
- **`ZonedDateTime`:** Local + full zone rules (DST). Best for human scheduling in a region.
- **`OffsetDateTime`:** Local + fixed offset. Common in APIs/XML. No DST rules.

---

### Q7. What is `TemporalAdjuster`? Example use case.

**Answer:** Strategy for date adjustment. Built-ins in `TemporalAdjusters`:
- `firstDayOfMonth()`, `lastDayOfYear()`
- `next(DayOfWeek.MONDAY)` — next Monday
- Custom: `(temporal) -> temporal.with(DAY_OF_MONTH, 1).plus(1, MONTHS).minus(1, DAYS)` for last day of month

Cleaner than manual field manipulation.

---

### Q8. How to migrate `SimpleDateFormat` thread-safety issues?

**Answer:** Replace with `DateTimeFormatter`. If stuck on legacy `Date`, convert at boundary:
`date.toInstant().atZone(zone).toLocalDate()`. Never share `SimpleDateFormat` across threads; if unavoidable, use `ThreadLocal<SimpleDateFormat>` until migrated.

---

### Q9. What is `ChronoUnit.between` vs `Period.between`?

**Answer:** `ChronoUnit.DAYS.between(d1, d2)` = total day count between dates. `Period.between` = years/months/days components (human-readable age). Different purposes.

---

### Q10. When would you use `java.time.chrono`?

**Answer:** Non-ISO calendars for **localized display/input** (Japanese era, Thai Buddhist year). Store internally as ISO/`Instant`; convert at UI layer. `Chronology.of("Japanese")` for Japan locale users.

---

## Self-Check

- [ ] I never use `new Date()` in new code
- [ ] I know when to use Duration vs Period
- [ ] I can convert Date ↔ Instant ↔ LocalDateTime
- [ ] I use Clock in testable services

**Next:** [Phase 5 — Concurrency & Async →](05-concurrency-async.md)
