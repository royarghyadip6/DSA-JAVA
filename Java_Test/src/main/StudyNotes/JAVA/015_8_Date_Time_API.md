# 15.8 Date & Time API (Java 8)

## Date & Time API (Java 8)

### Basic

---

# 1. Problems with Date and Calendar?

<details>
<summary>Show Answer</summary>

**Answer:**

The legacy `java.util.Date` and `java.util.Calendar` classes have **design flaws** that make them error-prone, hard to use, and unsuitable for modern applications.

### Problems with `Date`

| Problem | Detail |
|---------|--------|
| **Mutable** | `setYear()`, `setMonth()` modify object — unsafe in threads |
| **Not thread-safe** | Shared Date objects cause bugs |
| **Confusing API** | Year is `year - 1900`, month is 0-based |
| **Date + Time mixed** | `Date` holds both but many methods confusing |
| **Poor formatting** | No built-in format/parse — need `SimpleDateFormat` |
| **Deprecated methods** | Many methods deprecated since Java 1.1 |

### Example — Confusing Date API

```java
Date date = new Date();
date.setYear(125);   // year 2025? No — 125 means 2025 (1900 + 125)
date.setMonth(0);    // January? Yes — but 0-based months!
```

### Problems with `Calendar`

| Problem | Detail |
|---------|--------|
| **Mutable** | `calendar.set(...)` changes object |
| **Not thread-safe** | Shared Calendar = race conditions |
| **Verbose** | `calendar.set(Calendar.YEAR, 2025)` |
| **0-based months** | January = 0, December = 11 |
| **Weak type safety** | int constants easy to misuse |
| **Timezone complexity** | `GregorianCalendar` behavior confusing |

### Thread Safety Problem

```java
// ❌ SimpleDateFormat NOT thread-safe — shared instance corrupts dates
private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

// Two threads using same sdf → wrong results
```

### What Java 8 `java.time` Fixes

```text
✅ Immutable — all objects unmodifiable after creation
✅ Thread-safe — safe to share across threads
✅ Clear naming — LocalDate, LocalTime, ZonedDateTime
✅ Fluent API — plusDays(), minusMonths()
✅ Built-in formatting — DateTimeFormatter
✅ Better timezone support — ZoneId, ZonedDateTime
✅ Inspired by Joda-Time — proven design
```

### Migration Note

```java
// Old — avoid
Date date = new Date();
Calendar cal = Calendar.getInstance();

// New — use java.time
LocalDate today = LocalDate.now();
LocalDateTime now = LocalDateTime.now();
```

**Interview Point:**

> Old Date/Calendar: **mutable**, **not thread-safe**, **confusing** (0-based months, year offset). Java 8 `java.time` = immutable, thread-safe, clear API.

</details>

---

# 2. LocalDate?

<details>
<summary>Show Answer</summary>

**Answer:**

`LocalDate` represents a **date without time or timezone**—year, month, and day only (e.g., `2024-03-15`).

### Creating LocalDate

```java
LocalDate today = LocalDate.now();
LocalDate specific = LocalDate.of(2024, 3, 15);
LocalDate parsed = LocalDate.parse("2024-03-15");
```

### Common Operations

```java
LocalDate date = LocalDate.of(2024, 3, 15);

date.getYear();        // 2024
date.getMonth();       // MARCH (Month enum)
date.getDayOfMonth();  // 15
date.getDayOfWeek();   // FRIDAY

date.plusDays(10);     // 2024-03-25
date.minusMonths(1);   // 2024-02-15
date.withYear(2025);   // 2025-03-15
```

### Comparison

```java
LocalDate d1 = LocalDate.of(2024, 1, 1);
LocalDate d2 = LocalDate.of(2024, 6, 1);

d1.isBefore(d2);   // true
d1.isAfter(d2);    // false
d1.equals(d2);     // false
```

### Use Cases

```text
Birthdays (no time needed)
Due dates
Contract start/end dates
Holiday calendars
```

### Example — Birthday Check

```java
LocalDate birthday = LocalDate.of(1990, 5, 20);
LocalDate today = LocalDate.now();

if (today.getMonth() == birthday.getMonth()
    && today.getDayOfMonth() == birthday.getDayOfMonth()) {
    sendBirthdayWish();
}
```

### Key Properties

| Property | Detail |
|----------|--------|
| Package | `java.time` |
| Immutable | Yes |
| Timezone | None — "local" date |
| ISO format | `2024-03-15` |

**Interview Point:**

> `LocalDate` = date only, no time, no zone. Use for birthdays, deadlines, dates without time context.

</details>

---

# 3. LocalTime?

<details>
<summary>Show Answer</summary>

**Answer:**

`LocalTime` represents a **time without date or timezone**—hour, minute, second, and nanosecond (e.g., `14:30:00`).

### Creating LocalTime

```java
LocalTime now = LocalTime.now();
LocalTime specific = LocalTime.of(14, 30, 0);
LocalTime parsed = LocalTime.parse("14:30:00");
LocalTime fromString = LocalTime.parse("14:30");  // ISO format
```

### Common Operations

```java
LocalTime time = LocalTime.of(14, 30, 45);

time.getHour();        // 14
time.getMinute();      // 30
time.getSecond();      // 45

time.plusHours(2);     // 16:30:45
time.minusMinutes(15); // 14:15:45
time.withHour(9);      // 09:30:45
```

### Comparison

```java
LocalTime morning = LocalTime.of(9, 0);
LocalTime evening = LocalTime.of(18, 0);

morning.isBefore(evening);  // true
```

### Use Cases

```text
Store opening/closing hours
Meeting start time (same day assumed)
Daily schedule slots
Alarm times
```

### Example — Business Hours Check

```java
LocalTime open = LocalTime.of(9, 0);
LocalTime close = LocalTime.of(18, 0);
LocalTime now = LocalTime.now();

boolean isOpen = !now.isBefore(open) && now.isBefore(close);
```

### Constants

```java
LocalTime.MIN    // 00:00:00
LocalTime.MAX    // 23:59:59.999999999
LocalTime.MIDNIGHT // 00:00
LocalTime.NOON     // 12:00
```

**Interview Point:**

> `LocalTime` = time only, no date, no zone. Use for hours of operation, schedules within a day.

</details>

---

# 4. LocalDateTime?

<details>
<summary>Show Answer</summary>

**Answer:**

`LocalDateTime` combines **date and time** without timezone—local date plus local time (e.g., `2024-03-15T14:30:00`).

### Creating LocalDateTime

```java
LocalDateTime now = LocalDateTime.now();
LocalDateTime specific = LocalDateTime.of(2024, 3, 15, 14, 30, 0);
LocalDateTime fromParts = LocalDateTime.of(
    LocalDate.of(2024, 3, 15),
    LocalTime.of(14, 30)
);
LocalDateTime parsed = LocalDateTime.parse("2024-03-15T14:30:00");
```

### Common Operations

```java
LocalDateTime dt = LocalDateTime.now();

dt.getYear();          // date part
dt.getHour();          // time part
dt.plusDays(1);        // add 1 day
dt.plusHours(3);       // add 3 hours
dt.withMonth(12);      // change month
```

### Convert To/From LocalDate and LocalTime

```java
LocalDateTime dt = LocalDateTime.now();

LocalDate date = dt.toLocalDate();
LocalTime time = dt.toLocalTime();

LocalDateTime combined = LocalDateTime.of(date, time);
```

### Use Cases

```text
Appointment scheduling (local, no zone)
Log timestamps (when zone not needed)
Event start/end (single timezone apps)
Database datetime columns (local context)
```

### Example — Meeting Scheduler

```java
LocalDateTime meetingStart = LocalDateTime.of(2024, 6, 1, 10, 0);
LocalDateTime meetingEnd = meetingStart.plusHours(1);

if (LocalDateTime.now().isAfter(meetingStart)
    && LocalDateTime.now().isBefore(meetingEnd)) {
    System.out.println("Meeting in progress");
}
```

### vs LocalDate / LocalTime

| Class | Contains |
|-------|----------|
| `LocalDate` | Year, month, day |
| `LocalTime` | Hour, minute, second |
| `LocalDateTime` | Date + time (no zone) |

**Interview Point:**

> `LocalDateTime` = date + time, no timezone. ISO format `2024-03-15T14:30:00`. Most common for local event timestamps.

</details>

---

# 5. ZonedDateTime?

<details>
<summary>Show Answer</summary>

**Answer:**

`ZonedDateTime` represents **date, time, and timezone**—a complete timestamp with zone context (e.g., `2024-03-15T14:30:00+05:30[Asia/Kolkata]`).

### Creating ZonedDateTime

```java
ZonedDateTime now = ZonedDateTime.now();
ZonedDateTime india = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
ZonedDateTime specific = ZonedDateTime.of(
    2024, 3, 15, 14, 30, 0, 0,
    ZoneId.of("Asia/Kolkata")
);
```

### Timezone Conversion

```java
ZonedDateTime india = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
ZonedDateTime utc = india.withZoneSameInstant(ZoneId.of("UTC"));
ZonedDateTime ny = india.withZoneSameInstant(ZoneId.of("America/New_York"));

// Same instant, different local representation
```

### Use Cases

```text
Global meeting times
Flight schedules
API timestamps with timezone
User-facing datetime in their zone
Distributed system event times
```

### Example — Global Meeting

```java
ZonedDateTime meetingUTC = ZonedDateTime.of(
    2024, 6, 1, 15, 0, 0, 0, ZoneId.of("UTC"));

ZonedDateTime indiaTime = meetingUTC.withZoneSameInstant(
    ZoneId.of("Asia/Kolkata"));  // 20:30 IST

ZonedDateTime nyTime = meetingUTC.withZoneSameInstant(
    ZoneId.of("America/New_York")); // 11:00 EDT
```

### Hierarchy

```text
LocalDate      → date only
LocalTime      → time only
LocalDateTime  → date + time (no zone)
ZonedDateTime  → date + time + zone
Instant        → UTC instant (no local fields)
```

### ZoneId Examples

```java
ZoneId.of("Asia/Kolkata")
ZoneId.of("UTC")
ZoneId.of("America/New_York")
ZoneId.systemDefault()  // JVM default zone
```

**Interview Point:**

> `ZonedDateTime` = full datetime with timezone. Use `withZoneSameInstant()` to convert between zones. Required for global apps.

</details>

---

### Intermediate

---

# 6. Period vs Duration?

<details>
<summary>Show Answer</summary>

**Answer:**

`Period` measures **date-based** amounts (years, months, days). `Duration` measures **time-based** amounts (hours, minutes, seconds).

### Comparison Table

| | `Period` | `Duration` |
|---|----------|------------|
| Measures | Date units | Time units |
| Units | Years, months, days | Hours, minutes, seconds, nanos |
| Use with | `LocalDate`, `LocalDateTime` | `LocalTime`, `Instant` |
| Example | 2 years, 3 months | 5 hours, 30 minutes |

### Period — Date-Based

```java
LocalDate start = LocalDate.of(2024, 1, 15);
LocalDate end = LocalDate.of(2024, 6, 20);

Period period = Period.between(start, end);
// 5 months, 5 days

Period twoMonths = Period.ofMonths(2);
LocalDate future = start.plus(twoMonths);
```

### Duration — Time-Based

```java
LocalTime t1 = LocalTime.of(9, 0);
LocalTime t2 = LocalTime.of(17, 30);

Duration duration = Duration.between(t1, t2);
// 8 hours, 30 minutes

Duration oneHour = Duration.ofHours(1);
LocalTime later = t1.plus(oneHour);
```

### With Instant (UTC)

```java
Instant start = Instant.now();
// ... some work ...
Instant end = Instant.now();

Duration elapsed = Duration.between(start, end);
long millis = elapsed.toMillis();
```

### Period Trap — Not Fixed Length

```java
// Period months vary in length
Period p = Period.ofMonths(1);
LocalDate d1 = LocalDate.of(2024, 1, 31).plus(p);  // Feb 29 (leap)
LocalDate d2 = LocalDate.of(2024, 3, 31).plus(p);  // Apr 30
// Same Period.ofMonths(1) — different day results
```

### Memory Trick

```text
Period   → P calendar dates (birthday to today)
Duration → D clock time (stopwatch elapsed)
```

### Real Examples

```java
// Age calculation — Period
Period age = Period.between(birthDate, LocalDate.now());

// Request timeout — Duration
Duration timeout = Duration.ofSeconds(30);

// SLA check — Duration between Instants
Duration responseTime = Duration.between(requestStart, Instant.now());
```

**Interview Point:**

> `Period` = years/months/days with `LocalDate`. `Duration` = hours/minutes/seconds with `LocalTime`/`Instant`. Don't mix them.

</details>

---

# 7. Date formatting?

<details>
<summary>Show Answer</summary>

**Answer:**

Use `DateTimeFormatter` to format `java.time` objects into strings with custom or ISO patterns.

### ISO Format (Default)

```java
LocalDate date = LocalDate.of(2024, 3, 15);
String iso = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
// "2024-03-15"

LocalDateTime dt = LocalDateTime.now();
String isoDt = dt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
// "2024-03-15T14:30:00"
```

### Custom Pattern

```java
LocalDate date = LocalDate.of(2024, 3, 15);

DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
String formatted = date.format(formatter);
// "15-03-2024"

DateTimeFormatter usFormat = DateTimeFormatter.ofPattern("MM/dd/yyyy");
// "03/15/2024"

DateTimeFormatter full = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss");
LocalDateTime.now().format(full);
// "15 Mar 2024 14:30:00"
```

### Common Pattern Symbols

| Symbol | Meaning | Example |
|--------|---------|---------|
| `yyyy` | Year | 2024 |
| `MM` | Month (2 digit) | 03 |
| `MMM` | Month (short) | Mar |
| `dd` | Day | 15 |
| `HH` | Hour (24h) | 14 |
| `mm` | Minute | 30 |
| `ss` | Second | 45 |

### Predefined Formatters

```java
DateTimeFormatter.ISO_LOCAL_DATE       // 2024-03-15
DateTimeFormatter.ISO_LOCAL_TIME       // 14:30:00
DateTimeFormatter.ISO_LOCAL_DATE_TIME  // 2024-03-15T14:30:00
DateTimeFormatter.ISO_INSTANT          // 2024-03-15T09:00:00Z
```

### Locale-Aware Formatting

```java
DateTimeFormatter formatter = DateTimeFormatter
    .ofPattern("dd MMMM yyyy", Locale.ENGLISH);
// "15 March 2024"

DateTimeFormatter hindi = DateTimeFormatter
    .ofPattern("dd MMMM yyyy", new Locale("hi", "IN"));
```

### vs Old SimpleDateFormat

```text
DateTimeFormatter — immutable, thread-safe ✅
SimpleDateFormat  — mutable, NOT thread-safe ❌
```

**Interview Point:**

> `DateTimeFormatter.ofPattern("dd-MM-yyyy")` + `date.format(formatter)`. Thread-safe unlike `SimpleDateFormat`.

</details>

---

# 8. Parsing dates?

<details>
<summary>Show Answer</summary>

**Answer:**

Parse strings into `java.time` objects using `parse()` with `DateTimeFormatter`—reverse of formatting.

### ISO Parse (Default)

```java
LocalDate date = LocalDate.parse("2024-03-15");
LocalTime time = LocalTime.parse("14:30:00");
LocalDateTime dt = LocalDateTime.parse("2024-03-15T14:30:00");
```

### Custom Pattern Parse

```java
DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

LocalDate date = LocalDate.parse("15-03-2024", formatter);
// LocalDate 2024-03-15
```

### Parse with Time

```java
DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

LocalDateTime dt = LocalDateTime.parse("15/03/2024 14:30", fmt);
```

### ZonedDateTime Parse

```java
ZonedDateTime zdt = ZonedDateTime.parse("2024-03-15T14:30:00+05:30[Asia/Kolkata]");
```

### Strict vs Lenient

```java
// Lenient — may adjust invalid dates
DateTimeFormatter lenient = DateTimeFormatter
    .ofPattern("dd-MM-yyyy")
    .withResolverStyle(ResolverStyle.LENIENT);

// Strict — throws on invalid
DateTimeFormatter strict = DateTimeFormatter
    .ofPattern("dd-MM-yyyy")
    .withResolverStyle(ResolverStyle.STRICT);
```

### Error Handling

```java
DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy");

try {
    LocalDate date = LocalDate.parse(input, fmt);
} catch (DateTimeParseException e) {
  // invalid format or value
    log.error("Invalid date: " + input);
}
```

### Parse API Request Date

```java
public LocalDate parseRequestDate(String dateStr) {
    return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
}

public LocalDate parseCustom(String dateStr) {
    return LocalDate.parse(dateStr,
        DateTimeFormatter.ofPattern("yyyyMMdd"));
}
```

### Format ↔ Parse Round Trip

```java
LocalDate original = LocalDate.of(2024, 3, 15);
DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy");

String str = original.format(fmt);           // "15-03-2024"
LocalDate parsed = LocalDate.parse(str, fmt); // back to 2024-03-15
```

**Interview Point:**

> `LocalDate.parse(string, formatter)` — same pattern as format. Use `ResolverStyle.STRICT` for validation. Handle `DateTimeParseException`.

</details>

---

### Advanced

---

# 9. Timezone handling?

<details>
<summary>Show Answer</summary>

**Answer:**

Java 8 uses `ZoneId` for timezone rules and `ZonedDateTime` for zoned timestamps—avoid legacy `TimeZone` and `Calendar` for new code.

### ZoneId

```java
ZoneId india = ZoneId.of("Asia/Kolkata");
ZoneId utc = ZoneId.of("UTC");
ZoneId ny = ZoneId.of("America/New_York");
ZoneId system = ZoneId.systemDefault();  // JVM timezone
```

### Create ZonedDateTime

```java
ZonedDateTime indiaNow = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
ZonedDateTime utcNow = ZonedDateTime.now(ZoneId.of("UTC"));
```

### Convert Between Zones (Same Instant)

```java
ZonedDateTime india = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
// 2024-03-15T20:00+05:30[Asia/Kolkata]

ZonedDateTime utc = india.withZoneSameInstant(ZoneId.of("UTC"));
// 2024-03-15T14:30Z — same moment in UTC

ZonedDateTime london = india.withZoneSameInstant(ZoneId.of("Europe/London"));
```

### LocalDateTime + Zone → ZonedDateTime

```java
LocalDateTime local = LocalDateTime.of(2024, 6, 1, 10, 0);
ZonedDateTime zoned = local.atZone(ZoneId.of("Asia/Kolkata"));
```

### ZonedDateTime → LocalDateTime (Strip Zone)

```java
ZonedDateTime zoned = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
LocalDateTime local = zoned.toLocalDateTime();  // drops zone info
```

### User Timezone Pattern

```java
public ZonedDateTime toUserZone(Instant instant, String userTimezone) {
    return instant.atZone(ZoneId.of(userTimezone));
}

// Store Instant in DB, display in user zone
Instant stored = order.getCreatedAt();
ZonedDateTime userView = stored.atZone(ZoneId.of(user.getTimezone()));
```

### DST Handling

```java
// java.time handles DST automatically
ZoneId ny = ZoneId.of("America/New_York");
ZonedDateTime beforeDST = ZonedDateTime.of(2024, 3, 9, 12, 0, 0, 0, ny);
ZonedDateTime afterDST = beforeDST.plusDays(1);
// Correctly accounts for DST transition
```

### Best Practices

```text
✅ Store Instant or UTC in database
✅ Convert to user ZoneId for display
✅ Use ZoneId.of("Asia/Kolkata") not offset +05:30 alone
❌ Don't store local time without zone for global apps
❌ Don't use deprecated TimeZone class
```

**Interview Point:**

> `ZoneId` + `ZonedDateTime`. Store `Instant` in DB, convert with `atZone()` for display. `withZoneSameInstant()` for zone conversion.

</details>

---

# 10. UTC conversion?

<details>
<summary>Show Answer</summary>

**Answer:**

Use `Instant` for UTC timestamps and convert between `Instant`, `ZonedDateTime`, and `LocalDateTime` using `atZone()` and `toInstant()`.

### Instant — UTC Point in Time

```java
Instant now = Instant.now();  // current UTC instant
// 2024-03-15T09:00:00.123Z
```

### Local → UTC (Instant)

```java
LocalDateTime local = LocalDateTime.of(2024, 3, 15, 14, 30);
ZoneId india = ZoneId.of("Asia/Kolkata");

Instant utc = local.atZone(india).toInstant();
// Converts IST 14:30 → UTC 09:00
```

### UTC (Instant) → Local Zone

```java
Instant utc = Instant.parse("2024-03-15T09:00:00Z");

ZonedDateTime india = utc.atZone(ZoneId.of("Asia/Kolkata"));
// 2024-03-15T14:30+05:30

ZonedDateTime ny = utc.atZone(ZoneId.of("America/New_York"));
// 2024-03-15T05:00-04:00
```

### Conversion Flow

```text
LocalDateTime + ZoneId  → atZone()  → ZonedDateTime
ZonedDateTime           → toInstant() → Instant (UTC)
Instant                 → atZone(zone) → ZonedDateTime
```

### Full Example

```java
// User in India creates event
LocalDateTime eventLocal = LocalDateTime.of(2024, 6, 1, 18, 0);
ZonedDateTime eventIST = eventLocal.atZone(ZoneId.of("Asia/Kolkata"));

// Store as UTC Instant in database
Instant stored = eventIST.toInstant();

// Display to US user
ZonedDateTime eventNY = stored.atZone(ZoneId.of("America/New_York"));
```

### Old Date to Instant

```java
Date oldDate = new Date();
Instant instant = oldDate.toInstant();

Instant back = Instant.from(instant);
Date converted = Date.from(instant);
```

### Epoch Milliseconds

```java
Instant instant = Instant.now();
long epochMilli = instant.toEpochMilli();  // UTC millis since 1970

Instant fromEpoch = Instant.ofEpochMilli(epochMilli);
```

### API Timestamp Standard

```java
// REST API — always UTC ISO-8601
@GetMapping("/orders")
public OrderDTO getOrder() {
    Instant created = order.getCreatedAt();  // UTC Instant from DB
    dto.setCreatedAt(created.toString());    // "2024-03-15T09:00:00Z"
    return dto;
}
```

### Rules for Production

```text
1. Store Instant (UTC) in database
2. Never store local time without zone for global data
3. Convert at API boundary (user zone ↔ Instant)
4. Use ISO-8601 with Z for UTC in JSON
5. Instant.now() for server-side timestamps
```

**Interview Point:**

> `Instant` = UTC timeline. `local.atZone(zone).toInstant()` → UTC. `instant.atZone(zone)` → local display. Store Instant, display ZonedDateTime.

</details>

---

# 5–8 Year Interview Rapid Fire

### Q: LocalDateTime vs ZonedDateTime for DB?

<details>
<summary>Show Answer</summary>

**Answer:** Store **`Instant`** (UTC) or timestamp with timezone in DB. `LocalDateTime` loses zone info—dangerous for global apps. Convert at read/write boundaries.

</details>

---

### Q: Is java.time mutable?

<details>
<summary>Show Answer</summary>

**Answer:** **No.** All `java.time` classes are immutable. `plusDays()`, `withYear()` return **new** objects—original unchanged. Thread-safe by design.

</details>

---

### Q: Convert Date to LocalDate?

<details>
<summary>Show Answer</summary>

**Answer:**

```java
Date date = new Date();
LocalDate localDate = date.toInstant()
    .atZone(ZoneId.systemDefault())
    .toLocalDate();
```

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> `java.time` replaces Date/Calendar: immutable, thread-safe. **LocalDate/Time/DateTime** = no zone. **ZonedDateTime** = with zone. **Instant** = UTC. **Period** = dates, **Duration** = time. Store Instant, display with ZoneId.

</details>
