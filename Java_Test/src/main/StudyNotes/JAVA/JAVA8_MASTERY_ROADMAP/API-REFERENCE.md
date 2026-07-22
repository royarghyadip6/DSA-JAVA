# Java 8 New API Reference — All Classes, Interfaces & Enums

[← Back to Index](README.md)

> **Scope:** Types **introduced in Java 8** (new packages or new types in existing packages).  
> **Source:** Java SE 8 API Specification (JSR 337) and official Javadoc.

---

## `java.lang`

| Kind | Name | Purpose |
|------|------|---------|
| Annotation | `FunctionalInterface` | Marks SAM interfaces for lambdas |

---

## `java.lang.annotation`

| Kind | Name | Purpose |
|------|------|---------|
| Annotation | `Native` | Marks constants used in JNI headers |
| Annotation | `Repeatable` | Marks repeatable annotation types |

---

## `java.lang.reflect`

| Kind | Name | Purpose |
|------|------|---------|
| Class | `Parameter` | Reflect method/constructor parameter (name, modifiers) |
| Class | `Executable` | Abstract base for `Method` and `Constructor` |

---

## `java.lang.invoke`

| Kind | Name | Purpose |
|------|------|---------|
| Interface | `MethodHandleInfo` | Public metadata about a direct method handle |

---

## `java.util`

| Kind | Name | Purpose |
|------|------|---------|
| Interface | `Spliterator` | Traversable, splittable element source for streams |
| Interface | `Spliterator.OfPrimitive` | Base for primitive spliterators |
| Interface | `Spliterator.OfInt` | `int` spliterator |
| Interface | `Spliterator.OfLong` | `long` spliterator |
| Interface | `Spliterator.OfDouble` | `double` spliterator |
| Interface | `PrimitiveIterator` | Iterator + `forEachRemaining` |
| Interface | `PrimitiveIterator.OfInt` | `int` iterator |
| Interface | `PrimitiveIterator.OfLong` | `long` iterator |
| Interface | `PrimitiveIterator.OfDouble` | `double` iterator |
| Class | `Spliterators` | Factory methods for spliterators |
| Class | `Spliterators.AbstractSpliterator` | Partial spliterator impl |
| Class | `Spliterators.AbstractIntSpliterator` | Partial int spliterator |
| Class | `Spliterators.AbstractLongSpliterator` | Partial long spliterator |
| Class | `Spliterators.AbstractDoubleSpliterator` | Partial double spliterator |
| Class | `Optional` | Optional non-null object container |
| Class | `OptionalInt` | Optional `int` primitive |
| Class | `OptionalLong` | Optional `long` primitive |
| Class | `OptionalDouble` | Optional `double` primitive |
| Class | `Base64` | Base64 encoder/decoder factory |
| Class | `Base64.Encoder` | Base64 encoding |
| Class | `Base64.Decoder` | Base64 decoding |
| Class | `StringJoiner` | Delimited string builder |
| Class | `Objects` | Null-safe utilities (`requireNonNull`, etc.) |
| Class | `DoubleSummaryStatistics` | Stream stats for `double` |
| Class | `IntSummaryStatistics` | Stream stats for `int` |
| Class | `LongSummaryStatistics` | Stream stats for `long` |
| Class | `SplittableRandom` | Splittable pseudorandom generator |
| Enum | `Locale.Category` | Locale categories (FORMAT, DISPLAY) |
| Enum | `Locale.FilteringMode` | Locale matching mode |

---

## `java.util.function` (Entire Package — New in Java 8)

| Kind | Name | Purpose |
|------|------|---------|
| Interface | `BiConsumer<T,U>` | Accept two args, no result |
| Interface | `BiFunction<T,U,R>` | Two args → result |
| Interface | `BinaryOperator<T>` | Binary same-type operator |
| Interface | `BiPredicate<T,U>` | Two-arg predicate |
| Interface | `BooleanSupplier` | Supply `boolean` |
| Interface | `Consumer<T>` | Accept one arg, no result |
| Interface | `DoubleBinaryOperator` | `double` binary operator |
| Interface | `DoubleConsumer` | Accept `double` |
| Interface | `DoubleFunction<R>` | `double` → R |
| Interface | `DoublePredicate` | Predicate on `double` |
| Interface | `DoubleSupplier` | Supply `double` |
| Interface | `DoubleToIntFunction` | `double` → `int` |
| Interface | `DoubleToLongFunction` | `double` → `long` |
| Interface | `DoubleUnaryOperator` | `double` → `double` |
| Interface | `Function<T,R>` | One arg → result |
| Interface | `IntBinaryOperator` | `int` binary operator |
| Interface | `IntConsumer` | Accept `int` |
| Interface | `IntFunction<R>` | `int` → R |
| Interface | `IntPredicate` | Predicate on `int` |
| Interface | `IntSupplier` | Supply `int` |
| Interface | `IntToDoubleFunction` | `int` → `double` |
| Interface | `IntToLongFunction` | `int` → `long` |
| Interface | `IntUnaryOperator` | `int` → `int` |
| Interface | `LongBinaryOperator` | `long` binary operator |
| Interface | `LongConsumer` | Accept `long` |
| Interface | `LongFunction<R>` | `long` → R |
| Interface | `LongPredicate` | Predicate on `long` |
| Interface | `LongSupplier` | Supply `long` |
| Interface | `LongToDoubleFunction` | `long` → `double` |
| Interface | `LongToIntFunction` | `long` → `int` |
| Interface | `LongUnaryOperator` | `long` → `long` |
| Interface | `ObjDoubleConsumer<T>` | Object + `double` consumer |
| Interface | `ObjIntConsumer<T>` | Object + `int` consumer |
| Interface | `ObjLongConsumer<T>` | Object + `long` consumer |
| Interface | `Predicate<T>` | One-arg boolean test |
| Interface | `Supplier<T>` | Supply a result |
| Interface | `ToDoubleBiFunction<T,U>` | Two args → `double` |
| Interface | `ToDoubleFunction<T>` | T → `double` |
| Interface | `ToIntBiFunction<T,U>` | Two args → `int` |
| Interface | `ToIntFunction<T>` | T → `int` |
| Interface | `ToLongBiFunction<T,U>` | Two args → `long` |
| Interface | `ToLongFunction<T>` | T → `long` |
| Interface | `UnaryOperator<T>` | Unary same-type operator |

---

## `java.util.stream` (Entire Package — New in Java 8)

| Kind | Name | Purpose |
|------|------|---------|
| Interface | `BaseStream<T,S>` | Base stream operations |
| Interface | `Collector<T,A,R>` | Mutable reduction |
| Interface | `Stream<T>` | Object stream |
| Interface | `Stream.Builder<T>` | Build a `Stream` |
| Interface | `IntStream` | `int` primitive stream |
| Interface | `IntStream.Builder` | Build an `IntStream` |
| Interface | `LongStream` | `long` primitive stream |
| Interface | `LongStream.Builder` | Build a `LongStream` |
| Interface | `DoubleStream` | `double` primitive stream |
| Interface | `DoubleStream.Builder` | Build a `DoubleStream` |
| Class | `Collectors` | Prebuilt `Collector` factories |
| Class | `StreamSupport` | Low-level stream utilities |
| Enum | `Collector.Characteristics` | `CONCURRENT`, `UNORDERED`, `IDENTITY_FINISH` |

---

## `java.time` (Entire Package — New in Java 8)

| Kind | Name | Purpose |
|------|------|---------|
| Class | `Clock` | Pluggable clock |
| Class | `Duration` | Time-based amount |
| Class | `Instant` | UTC instant on timeline |
| Class | `LocalDate` | ISO date without zone |
| Class | `LocalDateTime` | ISO date-time without zone |
| Class | `LocalTime` | ISO time without zone |
| Class | `MonthDay` | Month + day without year |
| Class | `OffsetDateTime` | Date-time with UTC offset |
| Class | `OffsetTime` | Time with UTC offset |
| Class | `Period` | Date-based amount |
| Class | `Year` | Year only |
| Class | `YearMonth` | Year + month |
| Class | `ZonedDateTime` | Date-time with time zone |
| Class | `ZoneId` | Time-zone ID |
| Class | `ZoneOffset` | Fixed UTC offset |
| Enum | `DayOfWeek` | Day of week |
| Enum | `Month` | Month of year |
| Exception | `DateTimeException` | Date-time calculation error |

---

## `java.time.chrono` (Entire Package — New in Java 8)

| Kind | Name | Purpose |
|------|------|---------|
| Interface | `ChronoLocalDate` | Date in any chronology |
| Interface | `ChronoLocalDateTime<D>` | Date-time in any chronology |
| Interface | `Chronology` | Calendar system |
| Interface | `ChronoPeriod` | Period in any chronology |
| Interface | `ChronoZonedDateTime<D>` | Zoned date-time in any chronology |
| Interface | `Era` | Era abstraction |
| Class | `AbstractChronology` | Base chronology implementation |
| Class | `HijrahChronology` | Islamic calendar |
| Class | `HijrahDate` | Islamic date |
| Class | `IsoChronology` | ISO calendar system |
| Class | `JapaneseChronology` | Japanese Imperial calendar |
| Class | `JapaneseDate` | Japanese date |
| Class | `JapaneseEra` | Japanese era |
| Class | `MinguoChronology` | Minguo calendar |
| Class | `MinguoDate` | Minguo date |
| Class | `ThaiBuddhistChronology` | Thai Buddhist calendar |
| Class | `ThaiBuddhistDate` | Thai Buddhist date |
| Enum | `HijrahEra` | Islamic era |
| Enum | `IsoEra` | ISO era (BCE/CE) |
| Enum | `MinguoEra` | Minguo era |
| Enum | `ThaiBuddhistEra` | Thai Buddhist era |

---

## `java.time.format` (Entire Package — New in Java 8)

| Kind | Name | Purpose |
|------|------|---------|
| Class | `DateTimeFormatter` | Format/parse date-time |
| Class | `DateTimeFormatterBuilder` | Build formatters |
| Class | `DecimalStyle` | Localized decimal symbols |
| Enum | `FormatStyle` | Format style |
| Enum | `ResolverStyle` | Parsing resolver strictness |
| Enum | `SignStyle` | Sign formatting style |
| Enum | `TextStyle` | Text width style |
| Exception | `DateTimeParseException` | Parse failure |

---

## `java.time.temporal` (Entire Package — New in Java 8)

| Kind | Name | Purpose |
|------|------|---------|
| Interface | `Temporal` | Read-write temporal object |
| Interface | `TemporalAccessor` | Read-only temporal |
| Interface | `TemporalAdjuster` | Adjustment strategy |
| Interface | `TemporalAmount` | Amount of time |
| Interface | `TemporalField` | Date-time field |
| Interface | `TemporalQuery<R>` | Query strategy |
| Interface | `TemporalUnit` | Date-time unit |
| Class | `IsoFields` | ISO-specific fields |
| Class | `JulianFields` | Julian day fields |
| Class | `TemporalAdjusters` | Common adjusters |
| Class | `TemporalQueries` | Common queries |
| Class | `ValueRange` | Valid field range |
| Class | `WeekFields` | Locale week fields |
| Enum | `ChronoField` | Standard fields |
| Enum | `ChronoUnit` | Standard units |
| Exception | `UnsupportedTemporalTypeException` | Unsupported field/unit |

---

## `java.time.zone` (Entire Package — New in Java 8)

| Kind | Name | Purpose |
|------|------|---------|
| Class | `ZoneRules` | Zone offset rules |
| Class | `ZoneRulesProvider` | Zone rules provider SPI |
| Class | `ZoneOffsetTransition` | Offset transition |
| Class | `ZoneOffsetTransitionRule` | Recurring transition rule |
| Enum | `ZoneOffsetTransitionRule.TimeDefinition` | Local time definition |
| Exception | `ZoneRulesException` | Zone configuration error |

---

## `java.util.concurrent`

| Kind | Name | Purpose |
|------|------|---------|
| Interface | `CompletionStage<T>` | Stage of async computation |
| Interface | `CompletableFuture.AsynchronousCompletionTask` | Marker for async tasks |
| Class | `CompletableFuture<T>` | Composable future |
| Class | `CountedCompleter<T>` | FJP task with completion trigger |
| Exception | `CompletionException` | Async completion failure |

---

## `java.util.concurrent.atomic`

| Kind | Name | Purpose |
|------|------|---------|
| Class | `DoubleAccumulator` | Scalable `double` accumulator |
| Class | `DoubleAdder` | Scalable `double` sum |
| Class | `LongAccumulator` | Scalable `long` accumulator |
| Class | `LongAdder` | Scalable `long` sum |

---

## `java.util.concurrent.locks`

| Kind | Name | Purpose |
|------|------|---------|
| Class | `StampedLock` | Optimistic read / write lock |

---

## `javax.lang.model.type`

| Kind | Name | Purpose |
|------|------|---------|
| Interface | `IntersectionType` | Intersection type in annotation processing |

---

## `jdk.nashorn.api.scripting` (New in Java 8 — Removed JDK 15+)

| Kind | Name | Purpose |
|------|------|---------|
| Class | `NashornScriptEngineFactory` | Nashorn script engine factory |
| Interface | `ClassFilter` | Control Java class access from scripts |
| Class | `JSObject` | JS object wrapper |
| Class | `ScriptObjectMirror` | Mirror for script objects |
| Class | `AbstractJSObject` | Base JSObject implementation |

---

## `jdk.nashorn.api.linker` (New in Java 8)

| Kind | Name | Purpose |
|------|------|---------|
| Class | `NashornBeansLinker` | JavaBeans linker for Nashorn |

---

## Security & Networking (Selected New Types)

| Package | Kind | Name | Purpose |
|---------|------|------|---------|
| `java.security` | Class | `DomainLoadStoreParameter` | Domain keystore grouping |
| `java.security.cert` | Class | `PKIXRevocationChecker` | PKIX revocation checking |
| `java.security.spec` | Class | `DSAGenParameterSpec` | FIPS 186-3 DSA parameters |
| `javax.net.ssl` | Class | `SNIServerName` | SNI server name *(Java 8)* |
| `javax.net.ssl` | Class | `SNIHostName` | SNI hostname |
| `javax.net.ssl` | Class | `SNIMatcher` | SNI matcher |

---

## Quick Topic → Class Mapping

| You Want To… | Start With These Classes |
|--------------|--------------------------|
| Write less boilerplate | Lambda + `Consumer`, `Function`, `Predicate`, `Supplier` |
| Process collections declaratively | `Stream`, `Collectors`, `Optional` |
| Handle nulls safely | `Optional`, `OptionalInt/Long/Double`, `Objects.requireNonNull` |
| Work with dates properly | `LocalDate`, `LocalDateTime`, `ZonedDateTime`, `Instant`, `DateTimeFormatter` |
| Run async tasks | `CompletableFuture`, `CompletionStage` |
| High-throughput counters | `LongAdder`, `DoubleAdder` |
| Fast read-heavy locking | `StampedLock` |
| Encode binary data | `Base64` |
| Read files as streams | `Files.lines` |
| Reflect parameter names | `Parameter`, `Executable` |
| Repeat annotations | `@Repeatable` |
| Embed JavaScript (legacy) | `NashornScriptEngineFactory` |

---

## Suggested Weekly Study Plan (12 Weeks)

| Week | Focus | Deliverable Project |
|------|-------|---------------------|
| 1 | Lambdas, method refs, functional interfaces | CLI data filter with custom FI |
| 2 | Default/static interface methods | Mini library with evolving interface |
| 3 | Stream intermediate ops + Optional | Refactor file parser to streams |
| 4 | Collectors + grouping | Sales report aggregator |
| 5 | Parallel streams + Spliterator | Benchmark tool |
| 6 | Custom Collector + stats | Immutable collection collector |
| 7 | `java.time` core | Appointment scheduler |
| 8 | `java.time` zones + formatting | Multi-timezone event system |
| 9 | CompletableFuture | Async web scraper / API aggregator |
| 10 | StampedLock + LongAdder | Metrics counter service |
| 11 | Reflection + annotations | Mini validation framework |
| 12 | Full integration + review | End-to-end app using all Java 8 APIs |

---

## References

| Resource | URL |
|----------|-----|
| Java SE 8 Documentation | https://docs.oracle.com/javase/8/docs/ |
| What's New in JDK 8 | https://www.oracle.com/java/technologies/javase/8-whats-new.html |
| Lambda/Stream API Guide | https://docs.oracle.com/javase/8/docs/technotes/guides/language/enhancements.htm |
| JSR 337 (Java SE 8 Spec) | https://jcp.org/en/jsr/detail?id=337 |
| `java.util.function` Javadoc | https://docs.oracle.com/javase/8/docs/api/java/util/function/package-summary.html |
| `java.util.stream` Javadoc | https://docs.oracle.com/javase/8/docs/api/java/util/stream/package-summary.html |
| `java.time` Javadoc | https://docs.oracle.com/javase/8/docs/api/java/time/package-summary.html |

---

*Last updated: July 2026 — Java SE 8 (1.8)*
