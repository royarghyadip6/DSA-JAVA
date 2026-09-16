# 4.3 - Expression-Based Access Control (SpEL)

> **Module 4 - Topic 3** - Authorization
> Baseline: Spring Security 6.x on Boot 3.x
> New to this? Read **In Plain English** below first, then come back to the Version Matrix.

## Version Matrix

| Concern | Spring Security 5.x | **Spring Security 6.x (baseline)** | Spring Security 7.x |
| --- | --- | --- | --- |
| Who evaluates a method expression | `PreInvocationAuthorizationAdviceVoter` inside an `AccessDecisionManager`. | `PreAuthorizeAuthorizationManager` and `PostAuthorizeAuthorizationManager`, plain `AuthorizationManager` beans. | Same manager model; the voter classes are gone. |
| Who evaluates a URL expression | `WebExpressionVoter` with `DefaultWebSecurityExpressionHandler`, reached through `access("...")`. | `WebExpressionAuthorizationManager`, passed to `access(...)`. The string-taking `access("...")` overload is gone. | Same. |
| Method expression root | `MethodSecurityExpressionRoot`, built by `DefaultMethodSecurityExpressionHandler`. | Same class, now built from a `Supplier<Authentication>` so the principal is resolved lazily. | Same. |
| Web expression root | `WebSecurityExpressionRoot`, exposing `request` and `hasIpAddress`. | Same, via `DefaultHttpSecurityExpressionHandler`, with URI template variables bound as SpEL variables. | Same. |
| Parameter references (`#id`) | `DefaultSecurityParameterNameDiscoverer`, which could fall back to reading the local-variable table from debug symbols. | Debug-symbol fallback is deprecated and unreliable. You need the `-parameters` compiler flag or `@P("id")`. | `-parameters` or `@P` only. |
| Meta-annotations | Composed annotations carrying `@PreAuthorize` work. | Same, plus templated placeholders such as `@PreAuthorize("hasRole('{value}')")` from 6.3, opt-in via `AnnotationTemplateExpressionDefaults`. | Templating is standard. |
| Denial signal | `AccessDeniedException`. | `AuthorizationDeniedException`, a subclass of `AccessDeniedException`, so existing handlers still catch it. | Same. |
| Expression compilation | Interpreted. | Still interpreted. Spring Security does not enable `SpelCompilerMode`. | Still interpreted. |

## Why This Exists

Every authorization rule in Spring Security eventually reduces to a boolean. SpEL exists because most of those booleans are trivial and writing a Java class for each one would be absurd, while a small minority are not trivial at all and writing them as strings is how teams end up with authorization logic that no compiler, no IDE refactoring, and no static analysis can see.

The reason this topic deserves its own study is that the string in `@PreAuthorize("...")` is not evaluated in a vacuum. There is a root object that supplies `hasRole` and `authentication`, a variable map that supplies `#id`, a bean resolver that supplies `@invoiceSecurity`, and a handler that assembles all three. Each of those is configurable, each has a default that is sometimes wrong, and when one of them is not wired the expression does not fail loudly. It evaluates to `null`, `null` compares false, and the user gets 403 with no stack trace and no log line naming the cause. Diagnosing that requires knowing which component was supposed to supply the missing piece.

The second reason is a judgement call that separates mid-level from senior engineers: knowing when to stop. SpEL is a scripting language embedded in your security policy. It is the right tool for `hasRole('ADMIN')` and the wrong tool for a five-clause ownership rule with null handling. The dividing line, and the two escape hatches (a named bean reference and a typed `AuthorizationManager`), are the practical content of this topic. `14_M4_T2_Method_Level_Security.md` covers where the annotations are intercepted; this file covers what happens to the string once interception has begun.

## In Plain English

**The one-line version:** The text inside the quotes of `@PreAuthorize("...")` is a miniature program
in its own little language, and this topic is about which words you are allowed to use in there, where
each of those words comes from, and when you should stop writing them and write Java instead.

**An analogy.** Imagine a sticky note taped to a door, and a member of the door staff whose job is to
read the note and do what it says. The note might say "let in anyone whose badge says MANAGER". That is
a perfectly reasonable instruction and the system works.

Notice what the note is not. It is not a form with fixed options; it is free handwriting. Nobody
proofreads it. If you misspell MANAGER as MANGER, the note is still a completely valid-looking
instruction and the door staff will follow it faithfully — which means it will never let anybody in,
and nobody will know until somebody complains. There is no spell-check, no red underline, and no error
at the moment the note goes up.

The door staff does carry a small reference card listing the words they understand: "badge", "holder",
"has-temporary-pass". That card is what Spring calls the *root object*, and it is the reason `hasRole`
and `authentication` mean something inside an expression. Any word not on the card is not an error to
them; they simply treat it as a blank. That is the single most confusing behaviour in this topic,
because a blank compared against a real value is never equal, so a typo becomes a refusal rather than
a complaint. The staff also has a list of internal phone numbers they can ring for a second opinion,
which is what `@beanName` gives you: a way for the note to say "ring the invoice office and ask them".

And the last property of a handwritten note is the one to take seriously. The staff will do literally
anything written on it. That is fine as long as you are the only person who ever writes notes, which
is true for every expression Spring Security itself evaluates, because they all come from your source
code. It becomes catastrophic the moment your application starts reading notes that somebody else
wrote, such as an expression loaded from a database or typed into an admin screen, because the language
is powerful enough to run arbitrary code on your server.

**How it actually works, step by step.**

There are two places expressions get evaluated, and they have overlapping but different vocabularies.
Method annotations such as `@PreAuthorize` are one site, and URL rules written as
`access(new WebExpressionAuthorizationManager("..."))` are the other. Both understand `hasRole` and
`authentication`. Only the method site understands `returnObject` and `#parameterName`, and only the
URL site understands `request` and `hasIpAddress`. They are configured separately, which is why it is
possible to set something up correctly for one and have it silently missing for the other.

Each time an expression is evaluated, Spring builds a fresh *root object*. Being the root means its
public methods are callable without any prefix, which is exactly why you write `hasRole('ADMIN')` and
not `something.hasRole('ADMIN')`. It is also where several settings live that the surrounding handler
has to remember to fill in: the role hierarchy, the role prefix, the permission evaluator, and the
helper that distinguishes a genuine login from a "remember me" cookie. If a custom setup forgets to
fill one in, the matching expression quietly stops working properly rather than failing.

The difference between `hasRole` and `hasAuthority` is exactly one line of framework code.
`hasRole('ADMIN')` sticks `ROLE_` on the front of what you wrote and looks for `ROLE_ADMIN`.
`hasAuthority('ADMIN')` looks for the literal string `ADMIN` and adds nothing. Both are checking the
same list of permission strings on the user.

Writing `#userId` refers to a method parameter by name, and this is where the famous silent failure
lives. Java does not normally keep parameter names in the compiled class file; it only does so if the
code was compiled with a flag called `-parameters`. Spring Boot's standard build sets that flag, so
most applications never notice the requirement — until a module is built outside that setup. When the
name is not available, the expression sees a blank, the comparison is false, the caller gets a 403, and
there is no exception and no log line naming the missing parameter. The robust fix is to write
`@P("userId")` on the parameter, which puts the name in your source where no build setting can remove
it.

Two more names appear only after a method has run. `returnObject` is whatever the method returned, and
it exists for `@PostAuthorize` and `@PostFilter` only, so referring to it from `@PreAuthorize` gets you
a blank or an error. `filterObject` is the single item currently under consideration when a collection
is being filtered, rebound over and over as the filter walks the collection.

The recommended escape hatch, and the thing to reach for as soon as a rule is more than trivial, is
`@beanName.method(...)`. Spring gives the expression access to every bean in your application, so you
can put the real decision in an ordinary Java class with real parameters, real null handling, and real
unit tests, and leave the expression as one short readable line. The costs are that the bean's name and
its method signature are both just text inside a string, so renaming either breaks the rule at runtime
rather than at compile time. Pin the name with `@Component("invoiceSecurity")` so the class can be
renamed freely.

All of that assembly is done by an object called the expression handler, and there is one detail about
registering a custom one that is worth knowing in advance because the failure is so quiet. The bean
method that declares it must be `static`. Declared without `static`, the application starts perfectly
and simply uses the default handler instead, so every customisation you made is absent and nothing says
so.

Two more built-ins are worth naming. `hasPermission(...)` hands the decision to a separate component
you have to supply, and the one registered by default refuses everything, so `hasPermission` denies
every caller until you wire a real one in. And a meta-annotation — your own annotation that carries a
`@PreAuthorize` on it — lets you name a rule once and refer to it everywhere by a symbol the compiler
understands. That last technique is the highest-value, lowest-risk thing in this whole file.

**Why should a beginner care?** Because nothing checks these strings, and every kind of failure looks
the same from outside. Rename a parameter and you get 403 for everyone. Misspell a role name inside the
quotes and you get 403 for everyone. Rename the bean the expression calls and you get a 500 on the
first call after deployment. None of these are caught by the compiler, by startup, or by your IDE's
rename refactoring, and the one test most people write — checking that an unauthorised user is refused
— passes happily in every single one of those broken states, because a broken expression refuses
everybody.

**Words you will meet in this file**

| Term | In plain words |
|---|---|
| SpEL | Spring Expression Language: the small language you write inside the quotes of a security annotation. |
| Expression | One such string, for example `hasRole('ADMIN') or #id == authentication.name`. |
| Root object | The object whose methods are callable without a prefix, which is why `hasRole(...)` works on its own. |
| `SecurityExpressionRoot` | The shared base root that supplies `hasRole`, `hasAuthority`, `authentication`, and `principal`. |
| Expression handler | The component that builds the root, gathers the variables, and hands everything to the expression. |
| Evaluation context | The bundle of root object, variables, and bean access against which one expression is evaluated. |
| `#paramName` | A reference to one of the method's own arguments by name. |
| `-parameters` | The compiler flag that records real parameter names, without which `#paramName` silently resolves to nothing. |
| `@P("name")` | An annotation that fixes the name a parameter is known by inside an expression, independent of the build. |
| `@beanName.method(...)` | A call out to any Spring bean from inside an expression. The recommended way to express a non-trivial rule. |
| `hasRole` versus `hasAuthority` | The first adds `ROLE_` to what you wrote; the second uses your text exactly as written. |
| `returnObject` | What the method returned. Available only after the method has run, so only in `@PostAuthorize` and `@PostFilter`. |
| `filterObject` | The one collection item currently being judged while a filter runs. |
| `filterTarget` | An annotation setting naming which argument to filter, required once the method has more than one argument. |
| `PermissionEvaluator` | The pluggable component behind `hasPermission(...)`. The default one refuses everything. |
| `RoleHierarchy` | A configured statement that one role implies others, so `ADMIN` can automatically satisfy a rule written for `USER`. |
| Meta-annotation | Your own annotation carrying a `@PreAuthorize`, so a rule gets a name and is defined in exactly one place. |
| Templated placeholder | A `{value}` slot inside a meta-annotation's expression, filled in from the annotation's own attribute. |
| `StandardEvaluationContext` | The permissive setup Spring Security uses. It can reach any class and any bean, which is why expressions must only ever come from source code. |
| `SimpleEvaluationContext` | The restricted alternative, with class and bean access switched off, for the rare case of evaluating an expression you did not write. |
| Abstain | Having no opinion, which is what happens when a method carries no annotation, and which results in the call being allowed. |
| `static @Bean` | A bean declared on a static method. Required for a custom expression handler, or it is silently ignored. |

**If you remember only one thing:** nothing checks these strings, so keep each expression short enough
to be obviously correct, push any real logic into a named bean you can unit-test, and always write a
test proving the *authorised* caller is allowed — because a broken expression refuses everyone and
therefore passes every denial test you write.

## Core Concepts

### 1. Two evaluation sites, two roots, two handlers

**In simple terms:** Expressions written on methods and expressions written on URLs are read by two separate engines that share most of their vocabulary but not all of it, and they are configured separately, so setting something up for one does not set it up for the other.

Expressions are evaluated in two places, and they are wired independently. Conflating them is the most common source of confusion, because the vocabulary overlaps but is not identical.

| | Method security | URL security |
| --- | --- | --- |
| Entry point | `@PreAuthorize`, `@PostAuthorize`, `@PreFilter`, `@PostFilter` | `access(new WebExpressionAuthorizationManager("..."))` |
| Manager | `PreAuthorizeAuthorizationManager` and friends | `WebExpressionAuthorizationManager` |
| Handler | `DefaultMethodSecurityExpressionHandler` | `DefaultHttpSecurityExpressionHandler` |
| Root object | `MethodSecurityExpressionRoot` | `WebSecurityExpressionRoot` |
| Extra vocabulary | `#paramName`, `returnObject`, `filterObject`, `#root.this` | `request`, `hasIpAddress(...)`, URI template variables |
| Configured by | a `static @Bean MethodSecurityExpressionHandler` | passing a handler to the manager, or a `DefaultHttpSecurityExpressionHandler` bean |

Both roots extend `SecurityExpressionRoot`, which is why `hasRole` and `authentication` work in both. Everything beyond that base is site-specific: `hasIpAddress` exists only at the URL site, and `returnObject` exists only at the method site. Wiring a `RoleHierarchy` into one handler and not the other produces the asymmetric failure described in `16_M4_T4_Role_Authority.md` §6.

### 2. `SecurityExpressionRoot` - the shared contract

**In simple terms:** This is the object that supplies the words you are allowed to write inside an expression, which is why `hasRole(...)` and `authentication` work without any prefix in front of them.

```java
package org.springframework.security.access.expression;

public abstract class SecurityExpressionRoot implements SecurityExpressionOperations {

    protected final Authentication authentication;
    private AuthenticationTrustResolver trustResolver;
    private RoleHierarchy roleHierarchy;
    private Set<String> roles;
    private String defaultRolePrefix = "ROLE_";
    private PermissionEvaluator permissionEvaluator;

    /** Bare names usable as hasPermission(returnObject, read) without quotes. */
    public final String read = "read";
    public final String write = "write";
    public final String admin = "administration";

    @Override
    public final boolean hasAnyRole(String... roles) {
        return hasAnyAuthorityName(this.defaultRolePrefix, roles);   // prefix APPLIED
    }

    @Override
    public final boolean hasAuthority(String authority) {
        return hasAnyAuthorityName(null, authority);                 // prefix NOT applied
    }

    private boolean hasAnyAuthorityName(String prefix, String... roles) {
        Set<String> roleSet = getAuthoritySet();
        for (String role : roles) {
            if (roleSet.contains(getRoleWithDefaultPrefix(prefix, role))) {
                return true;
            }
        }
        return false;
    }

    private Set<String> getAuthoritySet() {
        if (this.roles == null) {
            Collection<? extends GrantedAuthority> userAuthorities = this.authentication.getAuthorities();
            if (this.roleHierarchy != null) {
                userAuthorities = this.roleHierarchy.getReachableGrantedAuthorities(userAuthorities);
            }
            this.roles = AuthorityUtils.authorityListToSet(userAuthorities);
        }
        return this.roles;
    }

    @Override
    public final Object getPrincipal() {
        return this.authentication.getPrincipal();
    }

    @Override
    public final boolean isFullyAuthenticated() {
        return !this.trustResolver.isAnonymous(this.authentication)
                && !this.trustResolver.isRememberMe(this.authentication);
    }
}
```

Four details in that snippet explain most real behaviour. `hasRole` passes `defaultRolePrefix` and `hasAuthority` passes `null`, which is the entire difference between them. `getRoleWithDefaultPrefix` skips the prefix when the value already has it, unlike `AuthorityAuthorizationManager`, which concatenates unconditionally. `getAuthoritySet` caches the flattened `Set<String>` on the root instance, and because a fresh root is built per evaluation the cache is per-evaluation, not per-request. And `roleHierarchy` and `trustResolver` are fields that the handler must populate; if a custom handler forgets them, `hasRole` silently ignores inheritance and `isFullyAuthenticated()` throws or misbehaves.

### 3. Every built-in expression

**In simple terms:** This is the complete dictionary of words the expression language already understands, so anything not in this list has to come from a bean of your own or from a custom root object.

| Expression | True when | Notes |
| --- | --- | --- |
| `hasRole('X')` | The principal holds `ROLE_X` | Prefix applied with skip semantics; `RoleHierarchy` applied |
| `hasAnyRole('X','Y')` | Any of them matches | Same prefix and hierarchy handling |
| `hasAuthority('X')` | The principal holds literally `X` | No prefix; hierarchy expansion still runs but rarely matters |
| `hasAnyAuthority('X','Y')` | Any literal match | |
| `permitAll` | Always | A property, not a call; `permitAll()` also parses |
| `denyAll` | Never | Useful to disable an endpoint without deleting it |
| `isAnonymous()` | `AuthenticationTrustResolver.isAnonymous` | Needs `trustResolver` wired |
| `isRememberMe()` | `AuthenticationTrustResolver.isRememberMe` | |
| `isAuthenticated()` | Not anonymous | A remember-me principal passes this |
| `isFullyAuthenticated()` | Neither anonymous nor remember-me | The correct guard before a sensitive write |
| `principal` | The `Authentication.getPrincipal()` object | Often `UserDetails`, but a `String` for JWT unless configured otherwise |
| `authentication` | The `Authentication` itself | `authentication.name` is the usual idiom |
| `hasPermission(obj, 'read')` | Delegated to `PermissionEvaluator` | Denies everything until an evaluator is registered |
| `hasPermission(id, 'Type', 'read')` | Same, identifier form | |

At the URL site you additionally get `request`, exposing the `HttpServletRequest`, and `hasIpAddress('10.0.0.0/8')`. `hasIpAddress` survives **only** inside a web expression; the `authorizeHttpRequests` DSL has no shorthand for it, which is why `13_M4_T1_Authorization_URL_Based.md` §8 shows a custom `AuthorizationManager` as the alternative.

### 4. Referencing method arguments

**In simple terms:** Writing `#userId` pulls the value of the method's own argument into the rule, but the argument's name only survives into the running program under certain build settings, and when it does not survive the rule quietly refuses everybody.

```java
@PreAuthorize("#userId == authentication.name")
public Profile load(String userId) { ... }
```

`#userId` is a SpEL variable, and the handler populates the variable map by asking a `ParameterNameDiscoverer` for the parameter names of the intercepted method. Java does not retain parameter names in bytecode by default, so this only works if one of two things is true.

The first is compiling with the `-parameters` flag, which records names in the class file:

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-compiler-plugin</artifactId>
  <configuration>
    <parameters>true</parameters>
  </configuration>
</plugin>
```

Spring Boot's parent POM enables this, which is why most applications never notice the requirement until they build a module outside that parent or a library consumer compiles without it. The second is annotating explicitly:

```java
@PreAuthorize("#userId == authentication.name")
public Profile load(@P("userId") String userId) { ... }
```

`org.springframework.security.core.parameters.P` makes the name part of the source and immune to both compiler configuration and parameter renaming. Use it on anything security-critical.

The failure mode when neither applies is the single most confusing bug in this topic. The variable is simply absent from the map, so SpEL resolves `#userId` to `null`, `null == "alice"` is `false`, and the caller receives 403. There is no exception, no warning, and no log line naming the variable. Spring Security 5.x had a fallback that read names from the local-variable table when classes were compiled with debug symbols, which is why some applications worked in a development build and failed in a stripped production build; that fallback is deprecated in 6.x.

Note also that the annotation and the parameter must be on the same declaration site that the interceptor reads. If the annotation is on an interface method and `@P` is on the implementation's parameter, the name is not discovered, because the interceptor resolves the attribute from the interface.

### 5. `#root`, `returnObject`, `filterObject`, and `filterTarget`

**In simple terms:** These are the extra names that only exist at particular moments: `returnObject` is what the method just handed back and so only exists after it has run, while `filterObject` is the one item from a collection currently being judged.

The root object itself is reachable as `#root`, and its properties are available unqualified. `MethodSecurityExpressionRoot` implements `MethodSecurityExpressionOperations`:

```java
package org.springframework.security.access.expression.method;

public interface MethodSecurityExpressionOperations extends SecurityExpressionOperations {
    void setFilterObject(Object filterObject);
    Object getFilterObject();
    void setReturnObject(Object returnObject);
    Object getReturnObject();
    Object getThis();
}
```

`returnObject` is bound only for `@PostAuthorize` and `@PostFilter`, because it does not exist before the method runs. Referencing it from `@PreAuthorize` yields `null`, so `@PreAuthorize("returnObject.owner == authentication.name")` throws a property-access failure on `null` or evaluates false, depending on the navigation operator used. This is a real mistake, usually made while converting a `@PostAuthorize` rule to a `@PreAuthorize` one for the transaction reasons described in `14_M4_T2_Method_Level_Security.md` §7.

`filterObject` is bound per element, repeatedly, by the filtering engine. For `@PostFilter` each element of the returned collection is bound in turn and the expression decides whether it survives; for `@PreFilter` the same happens to a collection argument. `#root.this` (via `getThis()`) exposes the target object, which is occasionally useful for calling a protected helper on the bean being invoked.

`filterTarget` is not a SpEL name but an annotation attribute, and it is required whenever a `@PreFilter` method has more than one argument, because otherwise the engine cannot tell which argument to filter:

```java
@PreFilter(filterTarget = "ids", value = "@invoiceSecurity.isOwner(filterObject, authentication)")
public void bulkCancel(List<Long> ids, String reason) { ... }
```

Two behaviours matter here. `@PreFilter` mutates the caller's collection in place, so the argument must be a mutable `List` or `Set`; passing `List.of(...)` throws `UnsupportedOperationException`. And `@PostFilter` requires the whole result to be materialised before anything is discarded, which is why filtering in the query is almost always better.

### 6. Calling a bean: `@beanName.method(...)`

**In simple terms:** Instead of cramming the decision into the string, you put it in a normal Java class that the compiler checks and a test can exercise, and the expression just calls that class by name.

The handler installs a `BeanFactoryResolver` on the evaluation context, so any bean in the application context is addressable from an expression:

```java
@PreAuthorize("hasRole('ADMIN') or @invoiceSecurity.isOwner(#invoiceId, authentication)")
public void cancel(@P("invoiceId") Long invoiceId, String reason) { ... }
```

This is the recommended pattern for anything beyond a single `hasRole`, and the reasons are all about maintainability rather than capability. The logic lives in a Java class the compiler checks, the IDE refactors, and a unit test can exercise directly with a fabricated `Authentication`. Null handling, database access, caching, and logging are ordinary code rather than expression fragments. And the expression string collapses to something a reviewer can read in one pass.

Three costs are worth stating plainly. The bean name is a string, so renaming the class silently breaks every expression referencing it unless you pin the name with `@Component("invoiceSecurity")`, which you should always do. The method signature is also unchecked, so changing a parameter type produces a `SpelEvaluationException` and a 500 on the first call after deployment rather than a compile error. And the method runs outside the caller's transaction for `@PreAuthorize`, because the authorization interceptor sits outside `TransactionInterceptor`, so a bean method that touches the database needs its own `@Transactional(readOnly = true)`.

The bean must also not itself be advised by method security, or an annotated security bean would recurse into the interceptor that is calling it. Marking it as infrastructure, or simply not annotating it, avoids that.

### 7. `MethodSecurityExpressionHandler` and registering a custom one

**In simple terms:** This is the component that assembles everything an expression needs before it runs, and if you replace it you must declare it on a `static` bean method, because a non-static one is ignored without any error and your settings silently vanish.

```java
package org.springframework.security.access.expression.method;

public interface MethodSecurityExpressionHandler extends SecurityExpressionHandler<MethodInvocation> {
    void setReturnObject(Object returnObject, EvaluationContext context);
    Object filter(Object filterTarget, Expression filterExpression, EvaluationContext context);
}
```

`DefaultMethodSecurityExpressionHandler` implements it and exposes five knobs: `setRoleHierarchy`, `setPermissionEvaluator`, `setParameterNameDiscoverer`, `setTrustResolver`, and `setDefaultRolePrefix`. Registering a configured instance is how you make `RoleHierarchy` and `hasPermission` work at the method site:

```java
@Configuration
@EnableMethodSecurity
public class ExpressionConfig {

    /**
     * MUST be static. The advisor beans that consume this handler are themselves
     * registered as static @Bean methods during method-security setup, and a
     * non-static declaration forces early instantiation of this configuration
     * class, which either fails with a circular reference or is resolved too
     * late and silently leaves the default handler in place.
     */
    @Bean
    static MethodSecurityExpressionHandler methodSecurityExpressionHandler(
            RoleHierarchy roleHierarchy, PermissionEvaluator permissionEvaluator) {

        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setRoleHierarchy(roleHierarchy);
        handler.setPermissionEvaluator(permissionEvaluator);
        return handler;
    }
}
```

Exactly one such bean may exist. A second `MethodSecurityExpressionHandler` bean makes the injection ambiguous and startup fails, which is at least loud. The quiet failure is the non-static declaration: the application starts, the default handler is used, and every `hasPermission` call denies while every `hasRole` ignores the hierarchy.

### 8. A custom root for new vocabulary

**In simple terms:** You can teach the expression language new words of your own, such as `isSameTenant(...)`, but you take on the job of copying across every setting the built-in root normally receives, and forgetting one weakens security without any warning.

`MethodSecurityExpressionRoot` is package-private, so you cannot subclass it. To add vocabulary you extend `SecurityExpressionRoot`, implement `MethodSecurityExpressionOperations`, and override the handler's factory method:

```java
public class TenantSecurityExpressionRoot extends SecurityExpressionRoot
        implements MethodSecurityExpressionOperations {

    private Object filterObject;
    private Object returnObject;
    private Object target;
    private final TenantContext tenants;

    public TenantSecurityExpressionRoot(Supplier<Authentication> authentication, TenantContext tenants) {
        super(authentication);
        this.tenants = tenants;
    }

    /** New vocabulary: usable as isSameTenant(#orgId) in any annotation. */
    public boolean isSameTenant(Object organisationId) {
        return organisationId != null
                && organisationId.toString().equals(this.tenants.currentOrganisationId());
    }

    @Override public void setFilterObject(Object o) { this.filterObject = o; }
    @Override public Object getFilterObject() { return this.filterObject; }
    @Override public void setReturnObject(Object o) { this.returnObject = o; }
    @Override public Object getReturnObject() { return this.returnObject; }
    @Override public Object getThis() { return this.target; }

    void setThis(Object target) { this.target = target; }
}
```

```java
public class TenantExpressionHandler extends DefaultMethodSecurityExpressionHandler {

    private final TenantContext tenants;

    public TenantExpressionHandler(TenantContext tenants) { this.tenants = tenants; }

    @Override
    public MethodSecurityExpressionOperations createSecurityExpressionRoot(
            Supplier<Authentication> authentication, MethodInvocation invocation) {

        TenantSecurityExpressionRoot root =
                new TenantSecurityExpressionRoot(authentication, this.tenants);
        root.setThis(invocation.getThis());
        root.setPermissionEvaluator(getPermissionEvaluator());
        root.setTrustResolver(getTrustResolver());
        root.setRoleHierarchy(getRoleHierarchy());
        root.setDefaultRolePrefix(getDefaultRolePrefix());
        return root;
    }
}
```

Those four `set...` calls are not optional boilerplate. Omitting `setRoleHierarchy` disables role inheritance for every method annotation in the application; omitting `setDefaultRolePrefix` makes `hasRole` ignore `GrantedAuthorityDefaults`; omitting `setTrustResolver` breaks `isFullyAuthenticated()` and `isAnonymous()`. This is the most common way a custom handler quietly weakens authorization, and it is why the named-bean pattern of §6 is preferable unless the new vocabulary is genuinely used in dozens of places.

### 9. `PermissionEvaluator` and `hasPermission`

**In simple terms:** `hasPermission(...)` does not decide anything itself; it hands the question to a separate component you are expected to supply, and the one Spring registers by default answers "no" to everything.

```java
package org.springframework.security.access;

public interface PermissionEvaluator extends AopInfrastructureBean {

    boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission);

    boolean hasPermission(Authentication authentication, Serializable targetId,
                          String targetType, Object permission);
}
```

The two overloads map to the two SpEL forms, one for when you hold the object and one for when you hold only its identifier:

```java
@PostAuthorize("hasPermission(returnObject, 'read')")
@PreAuthorize("hasPermission(#invoiceId, 'Invoice', 'write')")
```

The registered default is `DenyAllPermissionEvaluator`, which returns `false` and logs a warning, so `hasPermission(...)` denies everything until you supply a real evaluator, either `AclPermissionEvaluator` from `spring-security-acl` or your own. Note that the interface extends `AopInfrastructureBean`, which excludes the evaluator from method-security proxying and prevents it recursing into itself.

`hasPermission` is the right abstraction when permissions are data rather than code: per-object sharing, delegation, an ownership table that changes at runtime. It is the wrong abstraction for `hasRole('ADMIN') or isOwner(...)`, because three opaque strings hide the entire policy behind an indirection the reader has to go and find. Prefer a named bean when the policy is fixed and an evaluator when the policy is a table.

### 10. Meta-annotations and template variables

**In simple terms:** You write the expression once inside your own annotation, such as `@IsAdmin`, and then use that name everywhere, so the rule lives in exactly one place and the compiler can at least check the name.

A composed annotation carrying `@PreAuthorize` works without configuration, because Spring Security resolves annotations through Spring's merged-annotation machinery, which searches meta-annotations:

```java
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@PreAuthorize("hasRole('ADMIN')")
public @interface IsAdmin {
}
```

This is the highest-value, lowest-risk technique in the topic. It gives the expression a name, so the rule is defined once and referenced by a compiler-checked symbol. Renaming it is a refactor rather than a text search, changing the rule is a one-line edit in one file, and finding every place it applies is "find usages".

From 6.3 the meta-annotation can be parameterised, so one annotation covers a family of rules:

```java
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasRole('{value}')")
public @interface HasRole {
    String value();
}
```

The `{value}` placeholder is resolved from the annotation's attributes and is off by default; you opt in by publishing an `AnnotationTemplateExpressionDefaults` bean. Two cautions apply. Its `ignoreUnknown` flag defaults to `true`, so a misspelled placeholder is left in place rather than failing at startup, producing the literal expression `hasRole('{valu}')`, which denies everyone. And the placeholder is substituted as text, so a templated annotation whose attribute came from anywhere other than source code would be an expression-injection vector. Keep templated attributes to compile-time constants.

### 11. Cost, brittleness, and the injection risk

**In simple terms:** Speed is rarely the problem; the problem is that nothing checks these strings, so ordinary refactoring breaks them at runtime, and because the language can run arbitrary code you must never build an expression out of text that came from outside your source files.

Parsing is cached and evaluation is not. `PreAuthorizeAuthorizationManager` keeps an `ExpressionAttributeRegistry` keyed by method and target class, so each string is parsed into a `SpelExpression` once. Every invocation then walks that tree reflectively, because Spring Security does not enable SpEL's compiler. For `hasRole('ADMIN')` that is a few microseconds and irrelevant beside a database call. The cost that matters is what the expression calls: a bean method that queries the database adds a query to every invocation of every annotated method, running outside the surrounding transaction.

The real problem is not speed, it is that nothing checks the string. This table is the honest inventory:

| Change | Compiler | Startup | Runtime |
| --- | --- | --- | --- |
| Rename a method parameter | fine | fine | `#oldName` resolves to `null`, comparison false, **403** |
| Rename a bean | fine | fine | bean not found, **500** on first call |
| Change a bean method signature | fine | fine | method not found, **500** on first call |
| Typo inside a literal, `hasRole('ADMNI')` | fine | fine | authority never matches, **403** for everyone |
| Typo in a function name, `hasRolle(...)` | fine | fine | `SpelEvaluationException`, **500** |
| Rename a field used by `returnObject.x` | fine | fine | property not found, **500** |

Every row is a production incident that a typed `MethodInterceptor` or `AuthorizationManager` would have turned into a compilation error. Move off SpEL when the rule needs more than one `and` or `or`, when it needs a loop or a null-safe chain or a type check, when it needs isolated testing, when it sits on a hot path where you want to control caching, or when it is important enough that a reviewer should see typed code instead of a string.

The injection risk is separate and more serious. Spring Security evaluates against a `StandardEvaluationContext`, which permits type references, constructor invocation, and bean resolution. That permissiveness is required for `@beanName` and `T(...)` to work, and it means a SpEL expression is arbitrary code. Spring Security itself is never exposed, because every expression it evaluates comes from an annotation or a configuration call, both of which are source. The risk arrives when an application builds "dynamic policies" by parsing a string from a database, a configuration service, or an administrative user interface. An attacker who controls that string writes `T(java.lang.Runtime).getRuntime().exec('...')` and owns the process.

Three rules follow. Never concatenate anything into an expression string, not a tenant identifier and not a role name; pass values as variables or arguments so they are data rather than code. If you must evaluate an externally sourced expression, do not use Spring Security's handler; build a `SimpleEvaluationContext.forReadOnlyDataBinding()`, which disables type references, constructors, and bean resolution, and accept that `@beanName` stops working, because that is the point. Best of all, do not put dynamic policy in SpEL: a policy table interpreted by your own typed evaluator gives the same flexibility without handing out a scripting language.

### 12. The evaluation pipeline

**In simple terms:** This diagram traces one expression from the annotation text through parsing, assembly of the vocabulary, and the final true-or-false answer, including the two ways it can go wrong.

```mermaid
flowchart TD
    A["@PreAuthorize(\"...\") string"] --> B["ExpressionAttributeRegistry<br/>parse ONCE per method + target class, cached"]
    B --> C["PreAuthorizeAuthorizationManager.check"]
    C --> D["DefaultMethodSecurityExpressionHandler<br/>createEvaluationContext"]
    D --> E["createSecurityExpressionRoot<br/>MethodSecurityExpressionRoot"]
    E --> E1["roleHierarchy, defaultRolePrefix,<br/>trustResolver, permissionEvaluator"]
    D --> F["StandardEvaluationContext"]
    F --> F1["rootObject = the expression root<br/>gives hasRole, authentication, principal"]
    F --> F2["beanResolver = BeanFactoryResolver<br/>gives @beanName.method(...)"]
    F --> F3["variables from ParameterNameDiscoverer<br/>gives #paramName, needs -parameters or @P"]
    F --> G["ExpressionUtils.evaluateAsBoolean<br/>INTERPRETED, every invocation"]
    G -->|true| H["mi.proceed()"]
    G -->|false| I["AuthorizationDeniedException"]
    G -->|"unknown variable"| J["null, comparison false, 403 with no log line"]
    G -->|"unknown property or bean"| K["SpelEvaluationException, 500"]
```

## Working Code

```java
package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.annotation.AnnotationTemplateExpressionDefaults;

@Configuration
@EnableMethodSecurity
public class ExpressionConfig {

    @Bean
    static RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.withDefaultRolePrefix()
                .role("ADMIN").implies("MANAGER")
                .role("MANAGER").implies("USER")
                .build();
    }

    /** Static by necessity. Forgetting either setter silently disables that feature globally. */
    @Bean
    static MethodSecurityExpressionHandler methodSecurityExpressionHandler(
            RoleHierarchy roleHierarchy, PermissionEvaluator permissionEvaluator) {

        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setRoleHierarchy(roleHierarchy);
        handler.setPermissionEvaluator(permissionEvaluator);   // default denies everything
        return handler;
    }

    /** Opt in to {placeholder} substitution in templated meta-annotations (6.3+). */
    @Bean
    static AnnotationTemplateExpressionDefaults templateExpressionDefaults() {
        return new AnnotationTemplateExpressionDefaults();
    }
}
```

Named rules, so each expression string exists in exactly one file:

```java
package com.example.security.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.security.access.prepost.PreAuthorize;

public final class Rules {

    @Target({ ElementType.METHOD, ElementType.TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @PreAuthorize("hasRole('ADMIN')")
    public @interface IsAdmin { }

    /** Templated: one annotation, a family of rules. Needs AnnotationTemplateExpressionDefaults. */
    @Target({ ElementType.METHOD, ElementType.TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @PreAuthorize("hasRole('{value}')")
    public @interface HasRole {
        String value();
    }

    /** The ownership rule, defined once, referenced by symbol. */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @PreAuthorize("hasRole('ADMIN') or @invoiceSecurity.isOwner(#invoiceId, authentication)")
    public @interface IsInvoiceOwnerOrAdmin { }

    private Rules() { }
}
```

The bean the expressions call:

```java
package com.example.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.invoice.InvoiceRepository;

// Name it explicitly so a class rename cannot break every expression string.
@Component("invoiceSecurity")
public class InvoiceSecurity {

    private final InvoiceRepository invoices;

    public InvoiceSecurity(InvoiceRepository invoices) {
        this.invoices = invoices;
    }

    /** readOnly transaction of its own, because @PreAuthorize runs outside the caller's. */
    @Transactional(readOnly = true)
    public boolean isOwner(Long invoiceId, Authentication authentication) {
        if (invoiceId == null || authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return this.invoices.findById(invoiceId)
                .map(invoice -> invoice.getOwnerUsername().equals(authentication.getName()))
                .orElse(false);          // unknown invoice denies, and does not leak existence
    }
}
```

The service, using named rules and explicit parameter names:

```java
package com.example.invoice;

import java.util.List;

import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.security.annotations.Rules;

@Service
public class InvoiceService {

    private final InvoiceRepository invoices;

    public InvoiceService(InvoiceRepository invoices) {
        this.invoices = invoices;
    }

    /** @P pins the SpEL variable name against both renaming and a missing -parameters flag. */
    @Rules.IsInvoiceOwnerOrAdmin
    @Transactional
    public void cancel(@P("invoiceId") Long id, String reason) {
        this.invoices.findById(id).orElseThrow().cancel(reason);
    }

    /** returnObject is bound only for @PostAuthorize and @PostFilter, so read paths only. */
    @PostAuthorize("returnObject.ownerUsername == authentication.name or hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public Invoice findById(Long id) {
        return this.invoices.findById(id).orElseThrow();
    }

    /** hasPermission delegates to the registered PermissionEvaluator, not to roles. */
    @PostFilter("hasPermission(filterObject, 'read')")
    @Transactional(readOnly = true)
    public List<Invoice> findShared() {
        return this.invoices.findSharedWithAnyone();   // deliberately a small, bounded set
    }

    @Rules.HasRole("AUDITOR")
    @Transactional(readOnly = true)
    public AuditReport audit() {
        return AuditReport.of(this.invoices.findAll());
    }
}
```

A web rule using URI template variables, which is where `WebExpressionAuthorizationManager` earns its place:

```java
// Inside a @Bean SecurityFilterChain, with
// org.springframework.security.web.access.expression.WebExpressionAuthorizationManager imported.
http
    .httpBasic(basic -> {})
    .authorizeHttpRequests(authz -> authz
        // {username} is bound as a SpEL variable from the matcher's MatchResult,
        // so no parameter-name discovery is involved at this layer.
        .requestMatchers("/users/{username}/**")
            .access(new WebExpressionAuthorizationManager("#username == authentication.name"))
        // hasIpAddress survives ONLY inside a web expression; the DSL has no shorthand.
        .requestMatchers("/actuator/**")
            .access(new WebExpressionAuthorizationManager(
                    "hasRole('OPS') and hasIpAddress('10.0.0.0/8')"))
        .anyRequest().authenticated());
```

Tests, each of which fails for a specific wiring mistake:

```java
package com.example.invoice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;

@SpringBootTest
class ExpressionAccessControlTests {

    @Autowired InvoiceService service;

    @Test
    @WithMockUser(username = "alice", roles = "USER")
    void ownershipExpressionGrantsTheOwner() {
        service.cancel(1L, "duplicate");            // invoice 1 belongs to alice
    }

    @Test
    @WithMockUser(username = "mallory", roles = "USER")
    void ownershipExpressionDeniesEveryoneElse() {
        // Fails if #invoiceId did not resolve, because then isOwner(null, ...) also denies
        // the owner, which the previous test catches. The pair is the real assertion.
        assertThatExceptionOfType(AccessDeniedException.class)
                .isThrownBy(() -> service.cancel(1L, "hijack"));
    }

    @Test
    @WithMockUser(username = "root", roles = "ADMIN")
    void roleHierarchyReachesTheMethodExpressionHandler() {
        // ADMIN implies MANAGER implies USER. Fails if the custom handler
        // omitted setRoleHierarchy, or was declared as a non-static @Bean.
        service.cancel(1L, "admin override");
    }

    @Test
    @WithMockUser(username = "alice", authorities = "ROLE_AUDITOR")
    void templatedMetaAnnotationResolvesThePlaceholder() {
        // Fails if AnnotationTemplateExpressionDefaults is absent, because the
        // expression stays literally hasRole('{value}') and denies.
        assertThat(service.audit()).isNotNull();
    }

    @Test
    @WithMockUser(username = "alice", roles = "USER")
    void hasPermissionUsesTheRegisteredEvaluatorNotRoles() {
        // Empty for everyone if DenyAllPermissionEvaluator is still in place.
        assertThat(service.findShared()).isNotEmpty();
    }
}
```

The first two tests together are the important pattern. A grant test alone passes even when `#invoiceId` fails to resolve, if the rule also has a `hasRole('ADMIN')` branch or if the fixture happens to match. A deny test alone passes when the expression is broken, because a broken expression denies. Only the pair proves the expression actually discriminates.

## Internals

`PreAuthorizeAuthorizationManager` is thin, and everything interesting is in the registry and the handler:

```java
package org.springframework.security.authorization.method;

public final class PreAuthorizeAuthorizationManager
        implements AuthorizationManager<MethodInvocation> {

    private final PreAuthorizeExpressionAttributeRegistry registry =
            new PreAuthorizeExpressionAttributeRegistry();

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication,
            MethodInvocation mi) {

        ExpressionAttribute attribute = this.registry.getAttribute(mi);
        if (attribute == ExpressionAttribute.NULL_ATTRIBUTE) {
            return null;                       // no annotation: abstain
        }
        EvaluationContext ctx = this.registry.getExpressionHandler()
                .createEvaluationContext(authentication, mi);
        boolean granted = ExpressionUtils.evaluateAsBoolean(attribute.getExpression(), ctx);
        return new ExpressionAuthorizationDecision(granted, attribute.getExpression());
    }
}
```

Three things to take from it. Returning `null` when there is no annotation is an abstain, and abstain at the method layer means allow, exactly as it does at the URL layer; an unannotated method is not protected. `getAttribute(mi)` consults a cache keyed by the method and the target class, so the parse happens once but the cache key includes the concrete class, which is why an annotation on an interface and one on the implementation resolve to different attributes. And the returned `ExpressionAuthorizationDecision` carries the expression itself, which is what lets a denial handler or an `AuthorizationDeniedEvent` listener log the rule that rejected the call rather than a bare "denied". Logging that expression is the single highest-value observability change available in this topic.

Context assembly happens in `DefaultMethodSecurityExpressionHandler`:

```java
public EvaluationContext createEvaluationContext(Supplier<Authentication> authentication,
        MethodInvocation mi) {

    MethodSecurityExpressionOperations root = createSecurityExpressionRoot(authentication, mi);
    MethodSecurityEvaluationContext ctx =
            new MethodSecurityEvaluationContext(root, mi, getParameterNameDiscoverer());
    ctx.setBeanResolver(this.beanResolver);
    return ctx;
}
```

`MethodSecurityEvaluationContext` populates the variable map lazily, on first variable lookup, by pairing the discovered parameter names with the invocation's argument array. If the discoverer returns `null` for the method, no variables are added and every `#name` reference resolves to `null`. The `beanResolver` is a `BeanFactoryResolver` set when the handler is initialised with the application context, which is what makes `@beanName` work and, as §11 notes, is also what makes an externally sourced expression dangerous.

Filtering runs through the handler's `filter` method, which obtains an `Iterator` over the target, binds each element with `root.setFilterObject(element)`, re-evaluates the same cached expression, and removes non-matching elements via the iterator. Two consequences follow: the expression is evaluated once per element, so a bean call inside a `@PostFilter` on a thousand-element list is a thousand invocations and potentially a thousand queries; and because removal goes through the iterator, the collection must be mutable.

On the web side, `WebExpressionAuthorizationManager` wraps a parsed expression and a `DefaultHttpSecurityExpressionHandler`, building a `WebSecurityExpressionRoot` from the `RequestAuthorizationContext`. The URI template variables that the matcher captured are copied into the evaluation context as SpEL variables, which is why `#username` works in the `/users/{username}/**` rule without any parameter-name discovery at all.

## Configuration Reference

| Option | Effect | Default |
| --- | --- | --- |
| `static @Bean MethodSecurityExpressionHandler` | Replaces the handler for all method annotations. Exactly one bean permitted. | `DefaultMethodSecurityExpressionHandler` |
| `setRoleHierarchy(RoleHierarchy)` | Makes SpEL `hasRole` honour role inheritance. | Unset, so no inheritance |
| `setPermissionEvaluator(PermissionEvaluator)` | Backs `hasPermission(...)`. | `DenyAllPermissionEvaluator`, denies everything |
| `setDefaultRolePrefix(String)` | Prefix applied by `hasRole` and `hasAnyRole`. | `"ROLE_"`, or `GrantedAuthorityDefaults` when present |
| `setTrustResolver(AuthenticationTrustResolver)` | Backs `isAnonymous()`, `isRememberMe()`, `isFullyAuthenticated()`. | `AuthenticationTrustResolverImpl` |
| `setParameterNameDiscoverer(...)` | How `#paramName` is resolved. | `DefaultSecurityParameterNameDiscoverer` |
| `-parameters` compiler flag | Records parameter names in bytecode so `#paramName` resolves. | Enabled by the Spring Boot parent POM |
| `@P("name")` on a parameter | Pins the SpEL variable name in source. | Absent |
| `@Bean AnnotationTemplateExpressionDefaults` | Enables `{placeholder}` substitution in meta-annotations (6.3+). | Absent, templating off |
| `AnnotationTemplateExpressionDefaults.setIgnoreUnknown(boolean)` | Whether an unmatched placeholder is left as text rather than failing. | `true`, so typos deny silently |
| `@PreFilter(filterTarget = "...")` | Which argument to filter. Required when the method has more than one argument. | Inferred from the single argument |
| `DefaultHttpSecurityExpressionHandler` | Handler for web expressions; also takes a `RoleHierarchy`. | Used by `WebExpressionAuthorizationManager` |

## Production Concerns & Anti-Patterns

**Logic in the string.** An expression that contains more than one `and` or `or`, a ternary, a null-safe navigation chain, or a type check has become a program written in a language with no compiler, no debugger, and no tests. Move it into a named bean the moment it crosses that line. The refactor is mechanical and the result is strictly better: the same rule, now covered by unit tests and visible to the IDE.

**Near-duplicate expressions.** A codebase with a hundred slightly different `@PreAuthorize` strings has no authorization policy, it has a hundred independent decisions that nobody can audit. Two or three distinct strings differing only in a role name are a signal to introduce a meta-annotation or a templated one. The measure of success is being able to answer "what rule protects invoice cancellation" by following a symbol rather than by grepping.

**Forgetting that expressions are not refactored.** Renaming a method parameter, a bean, or a domain field will not touch the strings that reference them, and the resulting failure is a 403 or a 500 in production rather than a red build. Defend with `@P` on every referenced parameter, an explicit `@Component("name")` on every bean referenced from SpEL, and a test per rule that asserts both the grant and the denial. The denial-only test is worthless here, because a broken expression denies.

**The non-static handler bean.** Declaring `MethodSecurityExpressionHandler` as an instance `@Bean` starts the application successfully with the default handler in place. Every `hasPermission` then denies and every `hasRole` ignores the hierarchy, and nothing in the logs says so. Make it `static` and assert the behaviour with an integration test, not by inspecting the bean.

**`@PostAuthorize` and `@PostFilter` on write paths and large collections.** Both run after the method body. `@PostAuthorize` on a mutating method means the mutation already happened, and because the authorization interceptor sits outside `TransactionInterceptor`, the transaction has already committed by the time the denial is raised. `@PostFilter` materialises the entire collection before discarding elements, and evaluates the expression once per element. Push both into the query, as covered in `14_M4_T2_Method_Level_Security.md` §7 and §8.

**Dynamic expressions.** Building an expression string from anything outside source code is remote code execution dressed as configuration, because `StandardEvaluationContext` permits `T(...)` and constructor calls. If a requirement genuinely calls for operator-editable policy, express it as structured data interpreted by your own typed evaluator, or use `SimpleEvaluationContext.forReadOnlyDataBinding()` and accept the loss of bean resolution. Never concatenate a user-supplied value into an expression, even one that looks harmless like a role name.

**Database access inside expressions.** A bean call in a `@PreAuthorize` runs on every invocation, outside the caller's transaction, and before any of the method's own caching. On a hot path that is an extra round trip per call, and inside a `@PostFilter` it is one per element. Give the security bean its own `@Transactional(readOnly = true)`, consider a request-scoped or short-lived cache, and measure before putting a query behind a frequently invoked annotation.

## Debugging Playbook

| Symptom | Likely root cause | Fix |
| --- | --- | --- |
| `#paramName` is always `null`, every call returns 403 | Compiled without `-parameters`, so the discoverer found no names | Enable the flag, or annotate the parameter with `@P("paramName")` |
| `#paramName` works locally, fails in the release build | Local build had debug symbols and relied on the deprecated local-variable-table fallback | Same fix; do not rely on debug symbols |
| `hasPermission(...)` denies everyone | `DenyAllPermissionEvaluator` is still registered | Register a `PermissionEvaluator` on the handler |
| `hasRole` ignores the role hierarchy | Handler has no `RoleHierarchy`, or the handler bean is not `static` and was resolved too late | Make it a `static @Bean` and call `setRoleHierarchy` |
| `SpelEvaluationException: EL1057E` bean not found | A bean referenced as `@name` was renamed or is not a bean | Pin the name with `@Component("name")` |
| 500 with a property-not-found message on first call | A field used by `returnObject.x` or `principal.x` was renamed, or `principal` is a `String` not `UserDetails` | Fix the expression; for JWT principals use `authentication.name` |
| `@PostAuthorize` denies but the row was still changed | Denial happened after the method body and after transaction commit | Use `@PreAuthorize` with an identifier-based ownership check |
| `UnsupportedOperationException` from a `@PreFilter` method | The argument is an immutable collection and filtering mutates in place | Pass a mutable `List` or `Set` |
| `@PreFilter` fails to start with an ambiguity message | More than one argument and no `filterTarget` | Add `filterTarget = "argName"` |
| Templated meta-annotation denies everyone | `AnnotationTemplateExpressionDefaults` bean absent, or the placeholder name is misspelled and `ignoreUnknown` left it as text | Publish the bean and verify the placeholder matches an attribute name |
| Annotation on an interface appears ignored | The attribute was resolved against the concrete class, which carries a different or no annotation | Put the annotation in one place only; duplicates across interface and implementation raise an exception in 6.x |

## Interview Q&A

### Q1. What exactly is the root object when `@PreAuthorize("hasRole('ADMIN')")` is evaluated, and how do `#id`, `@beanName`, and `returnObject` each get into scope?

<details>
<summary>Show answer</summary>

The root object is a `MethodSecurityExpressionRoot`, built per evaluation by `DefaultMethodSecurityExpressionHandler.createSecurityExpressionRoot`. Because it is the SpEL root, its public methods and properties are addressable unqualified, which is where `hasRole`, `hasAuthority`, `isFullyAuthenticated()`, `principal`, and `authentication` come from. Those are inherited from the abstract `SecurityExpressionRoot`; the method-specific subclass adds `returnObject`, `filterObject`, and `getThis()` by implementing `MethodSecurityExpressionOperations`. The root also holds mutable configuration that the handler injects: a `RoleHierarchy`, a `defaultRolePrefix`, an `AuthenticationTrustResolver`, and a `PermissionEvaluator`. If the handler does not set one of those, the corresponding expression silently degrades rather than failing.

The three other name sources come from the `EvaluationContext`, not the root. `#id` is a SpEL variable: `MethodSecurityEvaluationContext` asks a `ParameterNameDiscoverer` for the intercepted method's parameter names and pairs them with the invocation's argument array, so `#id` exists only if the name was discoverable through the `-parameters` flag or a `@P` annotation. `@beanName` works because the handler installs a `BeanFactoryResolver` as the context's bean resolver, making every bean in the context addressable. And `returnObject` is a property on the root that the interceptor populates by calling `setReturnObject` after the method body has run, which is why it exists for `@PostAuthorize` and `@PostFilter` and is `null` everywhere else.

The practical significance of the split is diagnostic. A failure involving `hasRole` or `isFullyAuthenticated` is a handler-configuration problem. A failure involving `#name` is a parameter-name-discovery problem. A failure involving `@bean` is a bean-naming problem. And a failure involving `returnObject` is usually an annotation on the wrong phase.

**Counter-question: a fresh root is built per evaluation, yet `getAuthoritySet` caches. What is the actual scope of that cache, and does it matter?**

The cache lives on the root instance, so it is per evaluation, not per request. Within one expression, `hasRole('A') or hasRole('B')` flattens and expands the authority collection once; across two annotations on the same call, or across two calls in one request, the work is repeated. It matters in exactly one situation: when a `RoleHierarchy` is wired and the principal holds a large authority set, because `getReachableGrantedAuthorities` allocates a fresh set proportional to the held authorities on every expansion. With a handful of authorities this is unmeasurable. With thousands, and with several annotated calls per request, it becomes visible, which is one of the arguments in `16_M4_T4_Role_Authority.md` for expanding the hierarchy at authentication time instead.

**Counter-question: `principal` is on the root, so why do people prefer `authentication.name` over `principal.username`?**

Because `principal` is typed `Object` and its actual type depends entirely on the authentication mechanism. With form login backed by a `UserDetailsService` it is a `UserDetails`, so `principal.username` works. On a resource server with JWT it is, by default, a `String` holding the `sub` claim, so `principal.username` throws a property-not-found `SpelEvaluationException` and returns 500. `authentication.name` is defined for every `Authentication` implementation, because `getName()` is on the interface and each implementation resolves it appropriately. Writing `authentication.name` means the same expression survives a change of authentication mechanism, which is exactly the kind of change that otherwise produces a 500 on first call in production.

**Counter-question: if the root's methods are addressable unqualified, what stops an expression from calling something dangerous on it?**

Nothing about the root specifically, and that is not where the danger is. `SecurityExpressionRoot` marks `hasRole`, `hasAuthority`, `getPrincipal`, and the rest `final` precisely so a subclass cannot change their meaning, but the real exposure is the `StandardEvaluationContext`, which permits `T(...)` type references, constructor invocation, and bean resolution regardless of what the root offers. An expression can reach any bean and any class on the classpath. That is harmless as long as every expression originates in source code, which is true for everything Spring Security evaluates, and it is catastrophic the moment an application parses an expression from a database or an administrative interface.

</details>

### Q2. `@PreAuthorize("#userId == authentication.name")` returns 403 for a user whose identifier clearly matches. Diagnose it.

<details>
<summary>Show answer</summary>

The overwhelmingly likely cause is that `#userId` did not resolve, so SpEL evaluated `null == "alice"`, which is `false`. Nothing logs this. The variable map is populated by `MethodSecurityEvaluationContext` pairing discovered parameter names with the argument array, and if the `ParameterNameDiscoverer` returns no names for the method, the map has no entry for `userId` and SpEL treats an unknown variable as `null` rather than an error.

The reason names go missing is that Java does not retain parameter names in bytecode unless compiled with `-parameters`. Spring Boot's parent POM enables that flag, so applications built inside it usually work, and the failure shows up in a module built outside the parent, in a Gradle build without the equivalent setting, or in a library compiled elsewhere. Spring Security 5.x had a fallback that read names from the local-variable table when classes carried debug symbols, which produced the especially confusing pattern of working in a development build and failing in a stripped release build; that fallback is deprecated in 6.x.

To confirm rather than guess, change the expression temporarily to `#userId != null` and observe. If that also denies, the variable is absent and the diagnosis is confirmed. Then fix it by annotating the parameter with `@P("userId")`, which puts the name in source where no compiler setting can remove it. Enabling the `-parameters` flag is the other fix and is worth doing globally, but `@P` is what I would use on security-critical parameters because it cannot regress.

Two less common causes are worth checking if the variable does resolve. The annotation may be on an interface while `@P` is on the implementation parameter, in which case the attribute is resolved from the interface and the name is not discovered. And the comparison itself may be wrong: if the parameter is a `Long` and `authentication.name` is a `String`, `==` compares incomparable types and is false even when both represent the same identifier, which needs an explicit `#userId.toString() == authentication.name` or, better, a bean method that does the comparison in typed Java.

**Counter-question: why does Spring Security treat an unresolvable variable as `null` rather than throwing? Would throwing not be better?**

Throwing would be better for this class of bug, and the behaviour is inherited from SpEL rather than chosen by Spring Security. SpEL's variable lookup returns `null` for an unknown name because variables are an ordinary part of a dynamic expression language where absence is a legitimate state, and the same context mechanism is used for cases where a null variable is meaningful. The consequence for authorization is that the failure is safe but silent: it denies, which is the right direction, but it denies everyone and reports nothing. That asymmetry, safe but undiagnosable, is the strongest single argument for keeping expressions trivial and pushing anything that references arguments into a named bean where a null argument can be logged and handled explicitly.

**Counter-question: the fix works, but how do you stop it recurring across a large codebase?**

Two mechanisms, one preventive and one detective. Preventively, make `@P` a convention for every parameter referenced from an expression, and prefer named beans so the argument is passed as a method parameter that the compiler checks rather than a string. Detectively, write per-rule tests in grant-and-deny pairs. This is the crucial detail: a deny test passes when the expression is broken, because a broken expression denies everything. Only a test asserting that the legitimate owner is allowed will fail when `#userId` stops resolving. A test suite of denial assertions gives complete false confidence here.

**Counter-question: same expression at the URL layer, `/users/{userId}/**` with a web expression. Does the same failure exist?**

No, and the reason is instructive. At the URL layer `#userId` is not a method parameter, it is a URI template variable that the request matcher captured and that `WebExpressionAuthorizationManager` copies from the `RequestAuthorizationContext` variables into the evaluation context. No parameter-name discovery is involved, so the `-parameters` flag is irrelevant and the variable resolves regardless of compiler settings. The failure that does exist at the URL layer is a mismatch between the template variable name in the matcher and the name in the expression, which again resolves to `null` and denies. Same symptom, different cause, and the check is to confirm the two spellings match.

</details>

### Q3. Why is `@beanName.method(...)` the recommended pattern for non-trivial rules, and what are its real costs?

<details>
<summary>Show answer</summary>

Because it moves the policy from a string into Java without giving up the declarative annotation. The expression stays readable, typically one `hasRole` plus one bean call, while the actual decision lives in a class that the compiler checks, the IDE refactors, a unit test can drive directly with a fabricated `Authentication`, and a reviewer can read. Everything that is awkward inside an expression becomes ordinary code: null handling, database access, caching, structured logging of why a decision went the way it did. It also removes the duplication problem, because twenty methods can reference one rule rather than carrying twenty near-identical strings.

The mechanism is that `DefaultMethodSecurityExpressionHandler` installs a `BeanFactoryResolver` as the evaluation context's bean resolver, so `@invoiceSecurity` is a lookup by bean name in the application context.

The costs are real and specific. The bean name is a string, so renaming the class breaks every expression referencing it with a `SpelEvaluationException` and a 500 on the first call after deployment; always pin the name with `@Component("invoiceSecurity")` so the class name and the expression are decoupled. The method signature is equally unchecked, so changing a parameter type or arity produces the same runtime failure. The method runs outside the caller's transaction for `@PreAuthorize`, because the authorization interceptor is ordered outside `TransactionInterceptor`, so a bean that touches the database needs its own `@Transactional(readOnly = true)` and will open a second connection. And it runs on every invocation, which inside a `@PostFilter` means once per element.

For rules that are important enough, the further step is a typed `AuthorizationManager` or `MethodInterceptor` with no expression at all, which eliminates the string entirely at the cost of the declarative convenience.

**Counter-question: what happens if the security bean is itself annotated with `@PreAuthorize`?**

You get recursion, or at best a confusing failure. The bean is a Spring bean, so if method security is enabled and the bean's method carries an annotation, invoking it goes through the authorization interceptor, which evaluates an expression, which calls the bean again. `PermissionEvaluator` avoids this structurally by extending `AopInfrastructureBean`, which excludes it from proxying, and that is the model to follow: security-decision beans should not be advised by method security. In practice the rule is simply never to annotate them, and if you want it enforced rather than remembered, have them implement `AopInfrastructureBean` too.

**Counter-question: the bean call queries the database on every invocation. When does that stop being acceptable, and what do you do?**

It stops being acceptable when the annotated method is called often enough that the extra round trip is a measurable fraction of the request, or when it is called inside a loop or a `@PostFilter` where one call becomes N. The first thing to do is restructure rather than cache: an ownership check that loads the entity so the method body can load it again is doing the work twice, and pushing the ownership predicate into the method's own query eliminates both the extra call and the race between the two loads. When the check genuinely cannot be folded into the query, cache at the right granularity, keyed by principal and resource identifier with a short time to live, and be explicit that you have accepted a staleness window during which a revoked permission still grants access. Caching authorization decisions without deciding that window deliberately is how revocation quietly stops working.

**Counter-question: is there a way to get compile-time safety on the bean name and method?**

Not on the expression string itself, since SpEL is resolved at runtime by name. What you can do is make the failure loud and early instead of loud and late. A test that exercises every annotated entry point with at least one grant case turns a renamed bean or changed signature into a red build, which is the outcome a compiler would have given you. An architecture test can also parse the annotation values and assert that each `@name` referenced actually exists as a bean and that a method of that name is present, which catches the rename at build time. Beyond that, the honest answer is that if the rule matters enough to need compile-time safety, it should be a typed `AuthorizationManager` and not an expression.

</details>

### Q4. Walk me through writing a custom `MethodSecurityExpressionHandler` with a custom root. What is the most common mistake?

<details>
<summary>Show answer</summary>

`MethodSecurityExpressionRoot` is package-private, so extending it is not possible. The route is to extend the abstract `SecurityExpressionRoot`, implement `MethodSecurityExpressionOperations` to supply `setFilterObject`, `getFilterObject`, `setReturnObject`, `getReturnObject`, and `getThis`, and add whatever public methods constitute the new vocabulary, for example `isSameTenant(Object)`. Then extend `DefaultMethodSecurityExpressionHandler` and override `createSecurityExpressionRoot(Supplier<Authentication>, MethodInvocation)` to return an instance of your root. Finally register the handler as a `static @Bean MethodSecurityExpressionHandler`, of which exactly one may exist.

The most common mistake is inside that override: forgetting to copy the handler's configuration onto the new root. The base class populates `permissionEvaluator`, `trustResolver`, `roleHierarchy`, and `defaultRolePrefix` on the root it builds, and a custom override that only calls the constructor leaves all four unset. The consequences are individually severe and collectively silent. Without `roleHierarchy`, role inheritance stops applying to every method annotation in the application, so an administrator is denied a rule written for a subordinate role. Without `defaultRolePrefix`, `hasRole` ignores `GrantedAuthorityDefaults` and looks for the wrong prefix. Without `permissionEvaluator`, `hasPermission` degrades. Without `trustResolver`, `isFullyAuthenticated()` and `isAnonymous()` misbehave, which is the worst of the four because those are the guards in front of sensitive operations. Nothing warns, and nothing in the test suite notices unless there is a test asserting hierarchy or full-authentication behaviour specifically.

The second most common mistake is the non-static `@Bean`. The handler is consumed while method-security infrastructure is being registered, and an instance `@Bean` method forces early instantiation of the enclosing configuration class. The outcome is either a circular-reference failure at startup or, worse, successful startup with the default handler in place and every customisation silently absent.

For that reason I would only write a custom root when the new vocabulary is genuinely used in dozens of places. For anything less, a named bean referenced as `@tenantSecurity.isSameTenant(#orgId)` gives the same expressiveness with no framework surface to get wrong.

**Counter-question: how do you make the four forgotten setters impossible to forget?**

Call the supertype and then decorate, rather than constructing from scratch, wherever the type allows it, so the base class's configuration work still happens. When the return type forces you to construct your own instance, the next best thing is a test that asserts each of the four behaviours through a real annotated method: one asserting a superior role reaches an inferior rule, one asserting `hasRole` honours a custom prefix if you use one, one asserting `hasPermission` consults your evaluator, and one asserting a remember-me principal fails `isFullyAuthenticated()`. Those four tests are cheap and they fail loudly on exactly the regression that is otherwise undetectable. A code comment listing the setters is not a control; a test is.

**Counter-question: your root adds `isSameTenant(#orgId)`. What are the arguments against adding vocabulary at all?**

Three. The vocabulary is invisible to anyone reading the annotation, because `isSameTenant` looks like a framework built-in and there is no way to navigate to its definition from the string; a reader has to know that a custom root exists. It is untestable in isolation, since exercising it requires the whole method-security stack rather than a unit test on a class. And it is unbounded: once a custom root exists, every new rule becomes a candidate for another method on it, and the root grows into a god object holding the entire authorization policy with no module boundaries. A named bean per policy area, referenced as `@tenantSecurity.isSameTenant(...)`, costs eleven extra characters in the expression and avoids all three problems, which is why it is the better default.

**Counter-question: is there a legitimate case for a custom root over a bean?**

Yes, when the vocabulary needs something only the root has, which in practice means the `MethodInvocation`. The root can be given `getThis()` and the argument array, so a rule that needs to reason about the target object or about positional arguments cannot be expressed as a bean call without passing those in explicitly. The other case is a very large codebase where the vocabulary is genuinely ubiquitous and the eleven characters of `@beanName.` multiplied across a thousand annotations is a real readability cost. Both are real, and both are rarer than the number of custom roots in the wild would suggest.

</details>

### Q5. Is SpEL in `@PreAuthorize` a security risk in itself?

<details>
<summary>Show answer</summary>

As Spring Security uses it, no, and it is important to be precise about why. Every expression the framework evaluates comes from one of two places: an annotation attribute or a configuration method call. Both are source code, compiled into the artifact, and therefore under the same control as the rest of the application. An attacker who can change an annotation value can already change anything.

The risk appears when an application decides to make policy dynamic and parses an expression string that came from outside source code, such as a policy table, a configuration service, or an administrative user interface. Spring Security evaluates against a `StandardEvaluationContext`, which by design permits type references via `T(...)`, constructor invocation, and bean resolution, because those are required for `@beanName` and for the general usefulness of SpEL. That makes an expression arbitrary code running with the application's privileges. An attacker who controls the string writes something along the lines of `T(java.lang.Runtime).getRuntime().exec(...)` and has remote code execution, not a bypassed authorization check. The severity is the maximum: it is not privilege escalation within the application, it is execution on the host.

Three rules follow. Never concatenate anything into an expression string, including values that look harmless such as a role name or a tenant identifier, because concatenation is the mechanism by which data becomes code; pass values as SpEL variables or method arguments instead, where they are compared rather than parsed. If you must evaluate an externally sourced expression, do not use Spring Security's handler; build a `SimpleEvaluationContext.forReadOnlyDataBinding()`, which disables type references, constructors, and bean resolution, and accept that `@beanName` stops working, since that is precisely the capability being removed. Best, do not put dynamic policy in SpEL at all: structured policy data interpreted by your own typed evaluator gives operators the flexibility they asked for without handing them a scripting language.

Worth separating from this is the ordinary brittleness of SpEL, which is not a vulnerability but is a reliability problem: renamed parameters, beans, and fields break strings silently and surface as 403 or 500 in production.

**Counter-question: templated meta-annotations substitute `{value}` into the expression as text. Is that the injection vector you just described?**

It is the same mechanism, and it is safe only because of where the value comes from. An annotation attribute is a compile-time constant in source code, so substituting it into an expression is no different from writing the expression out by hand. It becomes a vector the moment anything other than a literal reaches the attribute. In practice that is hard to arrange, since annotation attributes must be constants, but the reasoning matters for the general principle: text substitution into an expression is safe exactly to the degree that the substituted text is trusted, and you should be able to state where it came from. Related, and more likely in practice, is the `ignoreUnknown` default of `true`, which means a misspelled placeholder is left in place as literal text rather than failing at startup, producing an expression that denies everyone. That is a correctness trap rather than a security hole, but it is the one you will actually hit.

**Counter-question: `SimpleEvaluationContext` disables bean resolution, so how would you implement operator-editable policy that still needs to consult application state?**

By inverting the direction. Instead of letting the expression reach into the application, compute the facts the policy might need before evaluation and pass them in as read-only data: the principal's roles, the resource's owner, the tenant, whatever the policy domain requires. The expression then operates on a plain data structure with no access to types, constructors, or beans, and `SimpleEvaluationContext.forReadOnlyDataBinding()` is sufficient. This also bounds the policy language to something you can document and validate. If the set of facts needed is open-ended enough that this becomes impractical, that is a signal the requirement is really for a policy engine with its own sandboxed language, not for embedded SpEL.

**Counter-question: how would you find out whether an existing codebase has this problem?**

Search for the parsing entry points rather than for the expressions, since the expressions are the untrusted part and will not look distinctive. Every dynamic evaluation must go through `SpelExpressionParser.parseExpression`, `ExpressionParser`, or a `SecurityExpressionHandler` invoked directly, so those are the call sites to audit, and in a healthy codebase all of them are inside the framework. Then trace each application call site back to the origin of its string and require that origin to be a literal or a compile-time constant. Add a build-time check forbidding `SpelExpressionParser` outside an allowlist of classes, so the audit does not have to be repeated. This is a small, bounded audit, which is worth knowing because the severity if you find something is critical.

</details>

### Q6. Design question - you inherit a codebase with 120 distinct `@PreAuthorize` strings, many near-duplicates, several containing real logic. Design the cleanup.

<details>
<summary>Show answer</summary>

I would treat this as a policy-consolidation problem rather than a refactoring problem, because the 120 strings are not an implementation of a policy, they are 120 independent decisions that no longer add up to one. The goal is a small named vocabulary that a reviewer can enumerate, and the constraint is that no step may widen access, because widening is the only failure mode that is not self-announcing.

The first step is inventory before any change. I would extract every annotation value, normalise whitespace and quoting, and group by exact and near match, which typically collapses 120 strings into fifteen or twenty distinct intents plus a handful of genuinely unique ones. That grouping is the actual deliverable of the first week, because it is the first time anyone will be able to see the policy. Alongside it I would record, for every annotated method, whether a test exercises the grant case, since methods with only denial tests or no tests are where a broken expression is currently invisible.

The second step is to establish safety before touching anything. For every distinct intent I would write a grant-and-deny test pair against the existing behaviour, so the current semantics are pinned. The grant half is the important one and the one most likely to be missing, because a broken expression denies, so a suite of denial assertions gives false confidence. I would also enable denial observability at this point, logging the `ExpressionAuthorizationDecision` expression on every denial through an `AuthorizationDeniedEvent` listener, so that during the migration I can see which rule rejected which call in production rather than inferring it.

The third step is naming, which is where most of the value lands. Each distinct intent becomes a meta-annotation carrying the expression, so `@PreAuthorize("hasRole('ADMIN')")` becomes `@IsAdmin`, and families that differ only by a role name become one templated annotation such as `@HasRole("AUDITOR")` with an `AnnotationTemplateExpressionDefaults` bean. The mechanical replacement is safe because the expression text is unchanged, and it converts every subsequent change to the rule into a one-line edit in one file, with "find usages" answering who is affected. I would keep the templated form for genuine families only; using it to unify rules that merely look similar reintroduces the original problem with extra indirection.

The fourth step addresses the strings that contain logic. Anything with more than one boolean operator, a ternary, a null-safe chain, or a type check moves into a named bean, pinned with an explicit `@Component("name")` so the class can be renamed freely, given its own `@Transactional(readOnly = true)` because `@PreAuthorize` runs outside the caller's transaction, and covered by unit tests driven with fabricated `Authentication` objects. Parameters referenced from the remaining expressions get `@P`, and I would enable `-parameters` project-wide as a backstop rather than a primary defence.

The fifth step is the configuration audit, which is separate and easy to miss. I would verify there is exactly one `MethodSecurityExpressionHandler` bean, that it is `static`, and that it has both a `RoleHierarchy` and a real `PermissionEvaluator` if `hasPermission` appears anywhere. The specific thing to test is that a superior role reaches an inferior rule at the method layer and at the URL layer independently, because wiring the hierarchy into one and not the other is the standard version of this bug.

Finally, I would prevent regression with build-time rules rather than review discipline: forbid raw `@PreAuthorize` outside the annotations package so every new rule must be named, forbid `SpelExpressionParser` outside an allowlist so dynamic policy cannot appear, and reject any expression whose length exceeds a threshold, which is a crude but effective proxy for "this contains logic".

What I would flag as needing a decision from the team: whether `@PostAuthorize` and `@PostFilter` occurrences found during the inventory should be converted to query-level filtering in the same effort or scheduled separately, since that is a data-access change rather than a policy change and it is the one part of this plan that touches behaviour rather than expression text.

**Counter-question: 120 strings collapse to twenty intents. How do you decide the twenty are right rather than just a smaller set of accidents?**

By checking them against the resource-and-operation matrix rather than against each other. I would list the resource types and the operations the application actually performs, and for each cell ask which named rule governs it. A good vocabulary covers every cell, has no rule that governs only one cell for no structural reason, and has no cell governed by two rules that could disagree. The rules that survive that test are policy; the ones that do not are historical accidents that happened to be duplicated. The other signal is whether a rule's name can be stated without reference to implementation, so `@IsInvoiceOwnerOrAdmin` is a policy statement while `@CheckInvoiceAccess2` is a leftover.

**Counter-question: during the migration, how do you know you have not widened access anywhere?**

Structurally, by never changing expression text in the same commit as anything else. Steps three and four are text-preserving for step three, and for step four the bean method must be written to be equivalent or stricter, with the grant-and-deny pair from step two proving equivalence on the cases that matter. Where equivalence is genuinely uncertain, I would run the new check in shadow alongside the old one, composing them so the effective decision is the conjunction, and log any disagreement; the new check can then only ever deny, never grant. Once the logs show no disagreement for a representative period, the old check is removed. The general principle is that every intermediate state is at least as strict as the original, so the worst outcome of a mistake is a false denial, which users report immediately.

**Counter-question: the team asks whether to go further and replace the annotations with typed `AuthorizationManager` beans everywhere. What do you say?**

That it is the right answer for a minority of the rules and the wrong answer for most. The declarative annotation is genuinely valuable: the rule sits next to the method it protects, which is how a reviewer discovers that a method is protected at all. Replacing it wholesale moves the policy into configuration that is far away from the code it governs, and the usual result is methods that quietly have no rule. I would keep named annotations as the default, and reserve typed managers for the cases that earn them: rules on hot paths where caching or short-circuiting must be controlled, rules complex enough that they need their own unit tests beyond a grant-and-deny pair, and rules important enough that a reviewer should be reading typed code. That is usually ten or fifteen of the twenty intents staying as annotations over named beans, and a handful becoming typed managers.

</details>

## Quick Recall

```
TWO SITES, TWO ROOTS, TWO HANDLERS
  method: @PreAuthorize etc -> PreAuthorizeAuthorizationManager
          -> DefaultMethodSecurityExpressionHandler -> MethodSecurityExpressionRoot
          extra: #paramName, returnObject, filterObject, #root.this
  web:    access(new WebExpressionAuthorizationManager("..."))
          -> DefaultHttpSecurityExpressionHandler -> WebSecurityExpressionRoot
          extra: request, hasIpAddress(...), URI template variables
  both extend SecurityExpressionRoot -> hasRole, hasAuthority, authentication, principal

WHERE EACH NAME COMES FROM
  hasRole / authentication / principal / returnObject   root object
  #paramName                                           EvaluationContext variables,
                                                        via ParameterNameDiscoverer
  @beanName.method(...)                                 BeanFactoryResolver on the context
  filterObject                                          rebound per element by handler.filter

hasRole vs hasAuthority
  hasRole('X')      -> defaultRolePrefix + X, SKIPS an existing prefix, hierarchy applied
  hasAuthority('X') -> literal X, no prefix
  isAuthenticated() true for remember-me; isFullyAuthenticated() is not

#paramName REQUIREMENTS
  -parameters compiler flag (Boot parent enables it) OR @P("name")
  5.x debug-symbol fallback is deprecated -> worked in dev build, failed in stripped build
  unresolved variable = null -> comparison false -> 403, NO log line, NO exception
  @P must be on the same declaration the interceptor reads (interface vs impl)

BEAN REFERENCE (the recommended pattern)
  @PreAuthorize("hasRole('ADMIN') or @invoiceSecurity.isOwner(#invoiceId, authentication)")
  pin the name: @Component("invoiceSecurity")
  give it @Transactional(readOnly=true): @PreAuthorize runs OUTSIDE the caller's tx
  do not annotate it with @PreAuthorize (recursion); PermissionEvaluator avoids this
  via AopInfrastructureBean

HANDLER
  static @Bean MethodSecurityExpressionHandler, exactly one
  non-static -> starts fine with the DEFAULT handler -> hasPermission denies,
               hierarchy ignored, nothing logged
  knobs: setRoleHierarchy / setPermissionEvaluator / setDefaultRolePrefix
         setTrustResolver / setParameterNameDiscoverer
  custom root: extend SecurityExpressionRoot + implement MethodSecurityExpressionOperations
               (MethodSecurityExpressionRoot is package-private)
               COPY all four config values onto it or they are silently unset

hasPermission
  PermissionEvaluator, two overloads: (obj, perm) and (id, type, perm)
  default DenyAllPermissionEvaluator -> denies everything until registered
  right when permissions are DATA (sharing, delegation); wrong for role-or-owner rules

META-ANNOTATIONS
  @IsAdmin carrying @PreAuthorize: works out of the box, highest value / lowest risk
  templated @PreAuthorize("hasRole('{value}')") needs 6.3+
    and a @Bean AnnotationTemplateExpressionDefaults
    ignoreUnknown defaults TRUE -> typo left as text -> denies everyone, no startup error

COST AND INJECTION
  parse cached per method+target class; evaluation INTERPRETED every invocation
    (SpEL compiler not enabled), and once per ELEMENT inside @PreFilter / @PostFilter
  no compile-time checking: renamed param -> 403; renamed bean/field/signature -> 500
  abstain (no annotation) = ALLOW, same as the URL layer
  StandardEvaluationContext allows T(...), constructors, bean resolution => arbitrary code
    safe only because framework expressions come from SOURCE CODE
    NEVER concatenate into an expression string
    external expression -> SimpleEvaluationContext.forReadOnlyDataBinding() (kills @beanName)

MOVE OFF SpEL WHEN
  more than one and/or, a ternary, a null-safe chain, a type check, needs isolated tests,
  on a hot path needing caching, or critical enough that a reviewer should see typed code

TEST IN GRANT+DENY PAIRS
  a broken expression DENIES, so deny-only tests pass while the rule is broken
```

**Previous:** [`14_M4_T2_Method_Level_Security.md`](14_M4_T2_Method_Level_Security.md) - **Next:** [`16_M4_T4_Role_Authority.md`](16_M4_T4_Role_Authority.md)
