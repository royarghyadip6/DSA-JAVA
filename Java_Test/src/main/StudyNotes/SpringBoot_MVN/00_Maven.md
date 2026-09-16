# 00. Maven for Spring Boot

## Start here (simple English)

**In one sentence:** Maven is the **build tool**. It downloads libraries, compiles your code, runs tests, and packs everything into a file you can run.

**Everyday picture:** You are cooking.

- `pom.xml` = the **recipe** (what dish, which ingredients, which oven steps)
- **Dependencies** = **ingredients** (Spring Web, JUnit, Postgres driver)
- **Repository** (`~/.m2` or Maven Central) = the **grocery store**
- **Lifecycle** = the **steps in order** (chop → cook → plate → pack)
- **Plugin** = a **kitchen gadget** that does one job (compiler, test runner, JAR packer)

Maven does **not** run your website in production. After the meal is packed (`app.jar`), the JVM runs it: `java -jar app.jar`.

**Words:**

| Word | Meaning |
|------|---------|
| `pom.xml` | Project Object Model — Maven’s config file |
| GAV | Group + Artifact + Version — the unique name of a library |
| Transitive | An ingredient of an ingredient (Web needs Tomcat, you did not list Tomcat) |
| BOM | A price list of versions that are known to work together |
| Fat JAR | One JAR that already contains your code **and** all libraries |

Read the rest of this chapter for how companies actually use Maven. The **Interview Q&A at the bottom is 5–8 year standard** — practise those even if the story above felt easy.

---

Maven is the **contract** for which versions you run, which modules you ship, how CI builds, and how the executable JAR is assembled.

---

## 1. What Maven is, and what it is not

**Apache Maven** is a build tool that:

- Declares a project in `pom.xml` (Project Object Model)
- Resolves dependencies from repositories
- Runs a **lifecycle** (compile → test → package → install → deploy)
- Applies **plugins** to each phase

It is **not**:

- A runtime (the JVM runs your app)
- A package manager in the npm sense (Maven is declarative + lifecycle, not a lockfile-first installer — though you can pin with Enforcer / BOMs)
- Spring Boot itself (Boot is a framework; Maven just builds it)

**Gradle vs Maven (interview one-liner):** Maven is convention + XML POM + strong lifecycle. Gradle is a DSL + incremental build. Most Spring Boot samples and enterprise Java still speak Maven fluently.

---

## 2. Coordinates — how the world names an artifact

Every artifact is uniquely identified by **GAV**:

```xml
<groupId>com.acme</groupId>
<artifactId>payments-api</artifactId>
<version>1.4.2</version>
```

| Part | Meaning |
|------|---------|
| `groupId` | Organization / namespace (`com.company.team`) |
| `artifactId` | Project / module name |
| `version` | Release or SNAPSHOT |
| `packaging` | `jar` (default), `war`, `pom`, `maven-plugin` |
| `classifier` | Extra variant: `sources`, `javadoc`, `exec` |

Local path:

```text
~/.m2/repository/com/acme/payments-api/1.4.2/payments-api-1.4.2.jar
```

### SNAPSHOT vs release

| | Release `1.4.2` | SNAPSHOT `1.4.2-SNAPSHOT` |
|--|-----------------|---------------------------|
| Mutability | Immutable. Never overwrite. | Mutable. Maven may re-download. |
| CI | What you ship. | What you iterate on. |
| Unique builds | Version *is* the identity. | Timestamped files in remote repo (`1.4.2-20260916.153012-17`) |

**Production rule:** never depend on someone else’s SNAPSHOT in a release. Pin a release version.

---

## 3. `pom.xml` is not one file — inheritance stack

When you run Maven, it does **not** use only your POM. It builds an **effective POM**:

```text
Super POM  (Maven built-in defaults)
  → Parent POM  (e.g. spring-boot-starter-parent)
    → Your POM
      → Profiles (active ones)
        → Command line / settings.xml overlays
```

See it:

```bash
mvn help:effective-pom
mvn help:effective-settings
```

### Super POM

Every POM inherits Maven’s Super POM. That is why `src/main/java` and `target/` exist without you declaring them. Compiler plugin version, default lifecycle bindings, and resource filtering defaults come from here (and from the parent).

### Parent POM vs aggregator POM

| | Parent | Aggregator |
|--|--------|------------|
| Purpose | Share versions, plugins, properties | Build many modules with one command |
| `packaging` | `pom` | `pom` |
| Has `<modules>`? | Optional | Yes |
| Children declare `<parent>`? | Yes | Often the same POM is both |

A typical Spring Boot multi-module repo **is both**: the root is parent + aggregator.

```xml
<packaging>pom</packaging>
<modules>
  <module>payments-api</module>
  <module>payments-domain</module>
  <module>payments-app</module>
</modules>
```

Child:

```xml
<parent>
  <groupId>com.acme</groupId>
  <artifactId>payments-parent</artifactId>
  <version>1.4.2</version>
  <relativePath>../pom.xml</relativePath>
</parent>
```

`relativePath` default is `../pom.xml`. Set it empty (`<relativePath/>`) if the parent is **only** in a repository, not on disk.

---

## 4. Repositories — where JARs come from

```text
1. Local  ~/.m2/repository
2. Mirrors / repositories in settings.xml  (company Nexus / Artifactory)
3. Maven Central  (repo1.maven.org)
4. Extra <repositories> in POM (avoid this in companies — use the mirror)
```

`settings.xml` (`~/.m2/settings.xml` or `$MAVEN_HOME/conf/settings.xml`) holds:

- `<localRepository>`
- `<servers>` (credentials for deploy)
- `<mirrors>` (force all traffic through Nexus)
- `<proxies>`
- `<profiles>` (JDK paths, repo URLs)

**Company pattern:** one mirror of `*` → Nexus. Developers never hit Central directly.

```xml
<mirror>
  <id>nexus</id>
  <mirrorOf>*</mirrorOf>
  <url>https://nexus.company.com/repository/maven-public/</url>
</mirror>
```

---

## 5. Dependencies — the part that breaks builds

### 5.1 Declaring a dependency

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-web</artifactId>
  <!-- version omitted: parent / BOM manages it -->
</dependency>
```

Maven then pulls **transitive** dependencies (Tomcat, Jackson, Spring MVC, …).

### 5.2 Scopes (you will be asked)

| Scope | Compile | Test | Runtime | Packaged in fat JAR? |
|-------|---------|------|---------|----------------------|
| `compile` (default) | yes | yes | yes | yes |
| `provided` | yes | yes | **no** (container supplies it) | **no** |
| `runtime` | no | yes | yes | yes |
| `test` | no | yes | no | no |
| `system` | like provided, local path | — | — | no — **do not use** |
| `import` | **only** in `dependencyManagement` on a POM | — | — | n/a |

**`provided` example:** servlet API when you build a WAR for external Tomcat. In a Boot **fat JAR**, Tomcat is embedded, so servlet API is **not** `provided`.

**`runtime` example:** JDBC driver if you compile only against APIs — in practice Boot apps usually use `compile` (default) for drivers.

**`import`:** this is how a BOM is consumed. It does not add JARs to the classpath.

### 5.3 Optional and exclusions

```xml
<dependency>
  <groupId>com.acme</groupId>
  <artifactId>legacy-client</artifactId>
  <exclusions>
    <exclusion>
      <groupId>commons-logging</groupId>
      <artifactId>commons-logging</artifactId>
    </exclusion>
  </exclusions>
</dependency>
```

- **Exclusion:** “do not bring this transitive JAR”.
- **Optional** (declared by the *producer*): transitives are **not** inherited. Consumers who need it add it themselves.

Classic Boot example — swap Tomcat for Jetty:

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
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-jetty</artifactId>
</dependency>
```

### 5.4 Dependency mediation (nearest-wins)

**Simple version:** Library A wants Guava 31. Library B wants Guava 32. Maven must pick **one**. It does **not** pick “the newest”. It picks the one **closer to your project** in the tree (fewer hops). That surprises everyone once.

If two paths pull different versions of the same GAV, Maven does **not** pick the newest.

**Rule: nearest definition wins.** Shortest path in the tree wins. If two paths have the **same depth**, the **first declared** in the POM wins.

```text
You
 ├─ A 1.0 → Guava 31.0     (depth 2)
 └─ B 2.0 → C 1.0 → Guava 32.0   (depth 3)

Winner: Guava 31.0  (nearer)
```

That is why “I declared 32 on a transitive I don’t even see” still loses.

**How seniors debug:**

```bash
mvn dependency:tree
mvn dependency:tree -Dincludes=com.google.guava:guava
mvn dependency:analyze
```

Then either:

1. Add a **direct** dependency with the version you want (depth 1 always wins), or
2. Put the version in `dependencyManagement`, or
3. Exclude the bad path

### 5.5 `dependencies` vs `dependencyManagement`

| | `<dependencies>` | `<dependencyManagement>` |
|--|------------------|--------------------------|
| Adds JAR to classpath? | **Yes** | **No** |
| Sets version for children / transitives? | Can | **Yes — this is the point** |
| Child must still declare the dependency? | n/a | **Yes**, unless parent also lists it in `<dependencies>` |

Parent:

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>org.postgresql</groupId>
      <artifactId>postgresql</artifactId>
      <version>42.7.4</version>
    </dependency>
  </dependencies>
</dependencyManagement>
```

Child only:

```xml
<dependency>
  <groupId>org.postgresql</groupId>
  <artifactId>postgresql</artifactId>
</dependency>
```

No version in the child. That is how you keep 20 modules on one driver version.

---

## 6. BOM — Bill of Materials

A **BOM** is a POM with `packaging=pom` whose only job is `dependencyManagement` for a stack of compatible versions.

### Spring Boot parent (simple apps)

```xml
<parent>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-parent</artifactId>
  <version>3.3.4</version>
  <relativePath/>
</parent>
```

Parent gives you:

- Dependency versions (via `spring-boot-dependencies`)
- Plugin versions (`spring-boot-maven-plugin`, compiler, surefire, failsafe, resources, jar)
- Sensible defaults (`java.version`, resource filtering, UTF-8)
- `repackage` goal so `mvn package` builds an executable JAR

### Spring Boot BOM import (when you already have a parent)

You can only have **one** `<parent>`. If the company parent is already set, **import the Boot BOM**:

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-dependencies</artifactId>
      <version>3.3.4</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

You then add `spring-boot-maven-plugin` yourself (parent would have done that for you).

**Interview line:** Parent = versions **plus** plugin defaults. Import BOM = versions only. Same dependency set; different build convenience.

Other BOMs you will see: `spring-cloud-dependencies`, Jackson BOM, Testcontainers BOM.

---

## 7. Lifecycles, phases, goals

**Simple version:** Maven has a **fixed to-do list**. If you say `mvn package`, it still does compile and test first. A **phase** is a step on that list. A **goal** is a gadget (`compiler:compile`) plugged into a step.

Maven has **three** independent lifecycles:

| Lifecycle | Typical phases |
|-----------|----------------|
| **clean** | `pre-clean` → `clean` → `post-clean` |
| **default** | `validate` → `compile` → `test` → `package` → `verify` → `install` → `deploy` |
| **site** | `pre-site` → `site` → `post-site` → `site-deploy` |

A **phase** is a step. A **goal** is a plugin method (`compiler:compile`). Phases **bind** goals.

Invoking a phase runs **all previous phases in that lifecycle**.

```bash
mvn clean install
```

`clean` lifecycle + default lifecycle through `install`.

### Default lifecycle (what you actually use)

| Phase | What happens in a Boot app |
|-------|----------------------------|
| `validate` | POM is sane |
| `compile` | `src/main/java` → `target/classes` |
| `test` | Surefire runs unit tests (`*Test`) |
| `package` | JAR + **spring-boot-maven-plugin repackage** (fat JAR) |
| `verify` | Failsafe integration tests (`*IT`) if bound |
| `install` | Copy artifact to `~/.m2` |
| `deploy` | Upload to Nexus/Artifactory |

**`install` vs `deploy`:**

- `install` = **your machine** (or CI agent’s local repo)
- `deploy` = **shared remote** so other teams/modules can depend on it

### Plugin vs pluginManagement

Same story as dependencies:

- `<plugins>` — this plugin **runs**
- `<pluginManagement>` — versions and config **available** for children; child must still declare the plugin to activate it (unless inherited from parent’s `<plugins>`)

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-compiler-plugin</artifactId>
  <configuration>
    <release>21</release>
  </configuration>
</plugin>
```

Spring Boot parent already sets compiler release from `${java.version}`.

---

## 8. Standard layout

```text
module/
  pom.xml
  src/main/java          application code
  src/main/resources     application.yml, logback-spring.xml
  src/main/webapp        only for WAR
  src/test/java
  src/test/resources
  target/                build output (never commit)
```

Boot extra: `src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` for custom auto-config (chapter 05).

---

## 9. Spring Boot Maven plugin — fat JAR vs layered JAR

```xml
<plugin>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-maven-plugin</artifactId>
</plugin>
```

What it does:

1. After `jar` is built, **repackage** it: your classes + nested `BOOT-INF/lib/*` + loader
2. Sets `Main-Class` to `org.springframework.boot.loader.launch.JarLauncher` (Boot 3.2+)
3. `mvn spring-boot:run` starts the app from the build output
4. Optional: **layers** for Docker cache

Fat / uber JAR:

```text
app.jar
  META-INF/MANIFEST.MF
  BOOT-INF/classes/     your code + resources
  BOOT-INF/lib/         every compile/runtime dependency
  org/springframework/boot/loader/   nested JAR launcher
```

Run:

```bash
java -jar target/payments-app-1.4.2.jar
```

**Thin JAR** (the original `*.jar.original` after repackage) has **no** dependencies. It is what you `install` if another module depends on your code as a library. For a library module, **skip repackage** or set `<skip>true</skip>` on the plugin so you publish a normal JAR.

Layered JAR (Docker):

```xml
<configuration>
  <layers>
    <enabled>true</enabled>
  </layers>
</configuration>
```

Layers typically: dependencies → snapshot-dependencies → resources → application. Docker then caches the fat dependency layer when only your code changed.

### JAR vs WAR in Boot

| | Executable JAR | WAR |
|--|----------------|-----|
| Server | Embedded | External Tomcat/JBoss |
| Run | `java -jar` | Drop into `webapps` |
| `spring-boot-starter-tomcat` | compile | `provided` |
| Main class | `SpringApplication.run` | `SpringBootServletInitializer` |

New services: **JAR**. WAR is for shops that still mandate a shared app server.

---

## 10. Profiles

Maven profiles are **build-time** (not the same as Spring `spring.profiles.active`).

```xml
<profiles>
  <profile>
    <id>integration</id>
    <build>
      <plugins>
        <plugin>
          <artifactId>maven-failsafe-plugin</artifactId>
        </plugin>
      </plugins>
    </build>
  </profile>
</profiles>
```

```bash
mvn verify -Pintegration
```

Activation can be JDK version, OS, file presence, or property.

**Do not** put production passwords in Maven profiles. Maven profiles select **how you build**. Spring profiles select **how you run**.

---

## 11. Wrapper — `mvnw`

Commit `mvnw`, `mvnw.cmd`, and `.mvn/wrapper/maven-wrapper.properties`.

Why:

- CI and laptops use the **same Maven version**
- No “install Maven 3.9 first”
- Reproducible builds

```bash
./mvnw -q -DskipTests package
```

---

## 12. Commands seniors actually use

```bash
mvn clean                         # delete target
mvn compile
mvn test
mvn package                       # fat JAR for Boot app modules
mvn verify                        # package + integration tests
mvn install                       # local .m2
mvn deploy                        # remote repo (CI only)

mvn -pl payments-app -am package  # this module + dependencies
mvn -T 1C package                 # parallel, 1 thread per core
mvn -o package                    # offline, local repo only
mvn -U package                    # force update SNAPSHOTs / releases metadata

mvn -DskipTests package           # compile tests, do not run them
mvn -Dmaven.test.skip=true package  # skip compiling tests too

mvn dependency:tree
mvn versions:display-dependency-updates
mvn help:effective-pom
mvn spring-boot:run
mvn spring-boot:build-image       # optional Cloud Native Buildpacks
```

**`-DskipTests` vs `-Dmaven.test.skip`:** skipTests = Surefire/Failsafe skip execution. `maven.test.skip=true` = do not even compile tests. Prefer `-DskipTests` when you still want test code to compile.

---

## 13. Conflict, CI, and quality plugins

### Enforcer (pin rules)

```xml
<plugin>
  <artifactId>maven-enforcer-plugin</artifactId>
  <executions>
    <execution>
      <goals><goal>enforce</goal></goals>
      <configuration>
        <rules>
          <requireMavenVersion><version>[3.9,)</version></requireMavenVersion>
          <requireJavaVersion><version>21</version></requireJavaVersion>
          <banDuplicatePomDependencyVersions/>
        </rules>
      </configuration>
    </execution>
  </executions>
</plugin>
```

### Flatten / flatten-maven-plugin

Library authors flatten the POM before deploy so consumers do not inherit your parent’s whole build.

### Reproducible builds

Set `project.build.outputTimestamp` (Boot parent supports this) so JAR timestamps do not change every build. Helps Docker and supply-chain diffs.

### CI pattern

```text
./mvnw -B -U verify
```

`-B` batch mode (no color/prompts). Fail the pipeline on test or Enforcer failure. `deploy` only from `main` with a release version.

---

## 14. Production pitfalls

1. **Version in a child overrides BOM accidentally** — you “fixed” one CVE and broke Boot’s tested set. Prefer BOM upgrade over random version tags.
2. **Nearest-wins surprise** — a close transitive pins an old Jackson. Always `dependency:tree` after adding a library.
3. **Two logging implementations** — `commons-logging` + Log4j + Logback. Boot uses `spring-boot-starter-logging` (Logback). Exclude `commons-logging` / `log4j-to-slf4j` conflicts.
4. **Repackaging a library module** — other modules then cannot compile against your API cleanly. Skip the Boot plugin on non-app modules.
5. **Using `maven.test.skip` in CI “to go faster”** — you ship untested bytecode. Use a dedicated fast job only for compile, a required job for `verify`.
6. **Checking in `target/` or `.m2`** — never.
7. **`<scope>system</scope>` and `libs/foo.jar`** — unreproducible. Install the JAR to Nexus or use a proper coordinate.

---

## 15. How Maven and Spring Boot share the work

```text
Maven (build time)
  resolve BOM versions
  compile
  test
  package / repackage fat JAR

Spring Boot (runtime)
  read nested JARs via JarLauncher
  create ApplicationContext
  auto-configure from classpath (what Maven just put on it)
  start embedded server
```

Maven decides **what is on the classpath**. Boot decides **which auto-config classes fire**. Wrong classpath → wrong auto-config (chapter 05).

---

# Interview Q&A (5–8 year bar)

Learn the story above in simple words. **Answer these as someone who has broken a build in CI.**

### Q1. What is Maven?

**Answer:** A build and dependency-management tool. The POM declares GAV, dependencies, plugins, and modules. Maven runs lifecycles and downloads artifacts into `~/.m2`.

**Counter:** Is Maven required to run a Boot app in production?  
**Answer:** No. Production is `java -jar`. Maven is for build/CI. The JAR already contains dependencies.

**Trap:** “Maven is a framework” / “Maven starts Tomcat”.

---

### Q2. What is a POM? What is the effective POM?

**Answer:** POM = project metadata. Effective POM = Super POM + parent + your POM + active profiles — the real model Maven uses.

**Counter:** How do you inspect it?  
**Answer:** `mvn help:effective-pom`.

---

### Q3. Explain the default lifecycle.

**Answer:** `validate → compile → test → package → verify → install → deploy`. A phase runs all previous phases. Plugins bind goals to phases.

**Counter:** Does `mvn package` run tests?  
**Answer:** Yes. `test` is before `package`. Skip with `-DskipTests`.

**Counter:** `install` vs `package`?  
**Answer:** `package` produces `target/*.jar`. `install` also copies it to the local repository so other local modules can depend on it.

---

### Q4. `install` vs `deploy`?

**Answer:** `install` → local `.m2`. `deploy` → remote repo (Nexus). CI deploys; developers install.

**Counter:** Can another developer use my `install`?  
**Answer:** Only on my machine. Sharing requires `deploy` or a committed multi-module build.

---

### Q5. What is a transitive dependency? How are version conflicts resolved?

**Answer:** Dependencies of your dependencies. Conflict → **nearest-wins** (shortest path). Same depth → first declared.

**Counter:** How do you force a version?  
**Answer:** Direct dependency, or `dependencyManagement` / BOM. Then confirm with `mvn dependency:tree`.

**Trap:** “Maven always picks the newest version.” That is Gradle’s default conflict resolution, not Maven’s.

---

### Q6. List dependency scopes and one real use for each.

**Answer:**

- `compile` — Spring Web
- `provided` — servlet API for a WAR
- `runtime` — JDBC driver if you compiled against SPI only
- `test` — JUnit, Mockito, Testcontainers
- `import` — Boot BOM inside `dependencyManagement`

**Counter:** Is Tomcat `provided` in a Boot fat JAR?  
**Answer:** No. Embedded Tomcat must be packaged. `provided` would break `java -jar`.

---

### Q7. `dependencyManagement` vs `dependencies`?

**Answer:** Management = version catalog, nothing on classpath. Dependencies = actually add the JAR. Children inherit management and still declare the dependency without a version.

**Counter:** If the parent puts `junit` in `<dependencies>` with `test` scope, do children get it?  
**Answer:** Yes — parent `<dependencies>` are inherited. That is why Boot parent can feel “magic”. Prefer management + explicit child deps for company parents.

---

### Q8. What is a BOM? Parent vs `import` of `spring-boot-dependencies`?

**Answer:** BOM = POM that only manages versions. Parent (`spring-boot-starter-parent`) = BOM + plugin management + resource filtering. If you already have a company parent, `import` the Boot BOM with `scope=import` `type=pom`.

**Counter:** Can a POM have two parents?  
**Answer:** No. That is exactly why `import` exists.

---

### Q9. Why don’t Boot dependencies declare versions?

**Answer:** `spring-boot-starter-parent` or the imported `spring-boot-dependencies` BOM pins compatible versions. You override only when you accept the risk.

**Counter:** How do you bump Jackson only?  
**Answer:** Property if Boot exposes it (`jackson-bom.version` / similar in that Boot generation), or a `dependencyManagement` entry. Prefer upgrading Boot.

---

### Q10. What does `spring-boot-maven-plugin` do?

**Answer:** Repackages the module JAR into an executable fat JAR with nested libs and a loader main class. Also provides `spring-boot:run`.

**Counter:** Why is there a `*.jar.original`?  
**Answer:** The plain JAR before repackage. That is what Maven installs as the main artifact’s “original”. The fat JAR is the one you run.

**Counter:** Should a domain module use this plugin?  
**Answer:** No. Only the runnable application module. Libraries should be normal JARs.

---

### Q11. Fat JAR vs layered JAR vs WAR?

**Answer:** Fat JAR = all deps nested, `java -jar`. Layered JAR = same, but split for Docker layer cache. WAR = deploy to external servlet container, Tomcat usually `provided`.

**Counter:** How do you change Tomcat to Jetty?  
**Answer:** Exclude `spring-boot-starter-tomcat` from `starter-web`, add `spring-boot-starter-jetty`.

---

### Q12. `mvn clean install` on a Boot project — walk through it.

**Answer:** `clean` deletes `target`. Default lifecycle: compile → test → package (jar + **repackage fat JAR**) → install to `.m2`.

**Counter:** Tests failed. Does `install` still happen?  
**Answer:** No. The reactor stops on test failure unless you skip tests or use `-fn` (never use `-fn` in CI).

---

### Q13. Build fails with `NoSuchMethodError` at runtime. What do you do?

**Answer:** That is almost always **binary incompatibility from mediation**. `mvn dependency:tree`, find two versions of the same library, pin via BOM or direct dep, add Enforcer `dependencyConvergence` if the team wants fail-on-conflict.

**Trap:** “Just restart” / “it’s a Spring bug” without looking at the tree.

---

### Q14. Difference between `-DskipTests` and `-Dmaven.test.skip`?

**Answer:** `skipTests` skips **running** tests; test sources still compile. `maven.test.skip=true` skips compilation of tests too.

**Counter:** Which should CI use for a “compile only” job?  
**Answer:** Either is fine for compile-only. The merge gate must run tests (`verify`).

---

### Q15. How do you speed up Maven without lying about quality?

**Answer:** Wrapper + local repo cache in CI, `-T 1C`, `-B`, avoid `clean` when not needed, parallel module build, keep SNAPSHOT updates bounded (`-U` only when required). Do not skip tests on the required pipeline.

**Counter:** Why is the first CI run slow?  
**Answer:** Empty `~/.m2`. Cache the local repo between pipelines (keyed on `pom.xml` hash).

---

### Q16. Maven profile vs Spring profile?

**Answer:** Maven profile = **build** (which modules, which plugins). Spring profile = **runtime** (`application-prod.yml`). They are not interchangeable.

**Counter:** Can Maven filter `application.yml` with resource filtering?  
**Answer:** Yes (`@...@` placeholders with Boot parent). Use sparingly; runtime env vars are clearer for secrets.

---

### Q17. What is the Maven Wrapper?

**Answer:** `mvnw` + pinned Maven distribution URL in `.mvn/wrapper`. Guarantees the same Maven on every machine.

---

### Q18. Multi-module: `-pl` and `-am`?

**Answer:** `-pl payments-app` builds that module. `-am` also builds **upstream** dependencies in the reactor. `-amd` builds downstream dependents.

**Counter:** Why not always build the whole reactor?  
**Answer:** You can. `-pl -am` is for large repos when you changed one service.

---

### Q19. What is Super POM?

**Answer:** Maven’s implicit parent. Default directory layout, default plugin bindings. You never write it; you inherit it.

---

### Q20. How does Maven help a Spring Boot project, in one breath?

**Answer:** Maven resolves the Boot BOM, compiles, tests, and the Boot plugin packages an executable JAR. Boot then uses that classpath for auto-configuration and starts an embedded server.

**Counter:** If Maven versions and Boot auto-config disagree, who wins?  
**Answer:** Maven already chose the JARs. Auto-config can only condition on **what is there**. Wrong JAR versions → runtime errors, not a second version resolver.
