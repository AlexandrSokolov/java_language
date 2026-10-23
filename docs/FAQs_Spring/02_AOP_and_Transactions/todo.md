# Proxies and AOP

How Spring adds behaviour around a bean's methods.

- JDK dynamic proxy (interfaces) vs CGLIB (subclass); which one Spring Boot uses by default.
- What a proxy can't intercept — `final` classes and methods, private methods.
- Self-invocation — a call from inside the same class skips the proxy.
- What runs through proxies — `@Transactional`, `@Async`, `@Cacheable`, `@PreAuthorize`.
- AOP terms — aspect, advice, pointcut, join point.
- Spring AOP vs AspectJ weaving.
- How proxies work in the JVM — [Reflection and Proxies](../../FAQs_JVM/1_Internals/1.3_Reflection_and_Proxies.md).

### Which beans does Spring proxy?
<details><summary>Show answer</summary>

```java
@Service
class PriceService { }                                    // nothing to intercept: injected as itself

@Service
class OrderService {
  @Transactional void place(Order order) { /* ... */ }    // proxied: other beans get the wrapper
}
```

A bean with nothing to intercept is injected as the real object. A proxy appears only when Spring must put code
between the caller and the method:

- **To add behaviour around calls:** `@Transactional`, `@Async`, `@Cacheable`, `@PreAuthorize`, `@Validated`, or
  your own `@Aspect`. The bean is wrapped, and every other bean gets the wrapper.
- **To decide later which object to call:** the injected reference is a stand-in that forwards to the real bean.
    - A request- or session-scoped bean injected into a singleton: each call goes to the current request's object.
    - A `@Lazy` injection point: the real bean is created on the first call.
- **To catch calls between `@Bean` methods:** a full-mode `@Configuration` class is replaced by a CGLIB subclass.

Spring proxies a bean only to add behaviour, to pick the object later, or to catch `@Bean` calls.

</details>


### Own proxied methods — how to call them?
<details><summary><strong>Show details</strong></summary>

<details><summary>Show question</summary>

A service calls some of its own `@Async` or `@Transactional` methods. A call on `this` skips the proxy, so the
annotation does nothing. How do you design it so these calls get the proxy?

</details>

<details><summary>Show answer</summary>

**Draft answer — inject the bean into itself.** It works, but it is not the design to keep.

```java
@Service
class ReportService {
  @Autowired @Lazy ReportService self;   // the proxy; @Lazy needed under Boot 2.6+

  public void runAll(List<Long> ids) { ids.forEach(self::runOne); }

  @Async
  public void runOne(long id) { /* ... */ }
}
```

**Why this draft is not the final design:**
- It is a [cycle of one](CYCLES_FILE.md#self-injection-under-boot-26): Boot 2.6+ refuses it, and `@Lazy` only hides it.
- Every reader must know why `self.runOne()` and `runOne()` behave differently.

**Correct design (the method decides):** move the method into its own bean. Every caller is outside, so every call
goes through the proxy.

```java
@Service
class ReportRunner {
  @Async
  public void runOne(long id) { /* ... */ }
}

@Service
class ReportService {
  @Autowired ReportRunner runner;

  public void runAll(List<Long> ids) { ids.forEach(runner::runOne); }
}
```

**Alternative design (the caller decides):** one wrapper bean that runs any lambda through its own proxy.

```java
@Component
class AsyncRunner {
  @Async
  public void run(Runnable task) { task.run(); }

  @Async
  public <T> CompletableFuture<T> supply(Supplier<T> task) {
    return CompletableFuture.completedFuture(task.get());
  }
}
// in ReportService: runner.run(() -> runOne(id));
```

- **Buys:** no cycle; you choose per call; the method stays where it is.
- **Costs:** the method no longer says how it runs, so a direct call quietly runs without it. `Runnable` and
  `Supplier` can't throw checked exceptions. One wrapper usually means one thread pool. The lambda calls `this`, so
  [other annotations on the target are skipped](#describe-a-code-snippet-nn).

Spring has this design built in, with no proxy: `TransactionTemplate` for transactions, and an injected executor
(`CompletableFuture.supplyAsync(task, executor)`) for async.

**Alternative design (no proxy):** AspectJ weaving changes the class itself, so calls on `this` are covered too.
- **Buys:** no change to the code structure.
- **Costs:** build or agent setup; rare in Spring projects, so few readers expect it.

Every call needs the behavior → it belongs to the method: own bean. A choice per call → wrapper or the built-in
programmatic versions.

</details>

</details>

### Describe a code snippet #NN
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
@Component
class AsyncRunner {
  @Async
  public void run(Runnable task) { task.run(); }
}

@Service
class ReportService {
  @Autowired AsyncRunner runner;

  public void saveAllLater(List<Report> reports) {
    reports.forEach(r -> runner.run(() -> save(r)));
  }

  @Transactional
  public void save(Report r) { /* two inserts that must commit together */ }
}
```

</details>

<details><summary>Show answer</summary>

`save()` runs async, but with no transaction. The lambda calls `this.save(r)`: the real object, not its proxy, so
`@Transactional` is skipped. If the second insert fails, the first one stays.

The only proxy on the call path is `AsyncRunner`'s, and it adds only `@Async`.

Fix: start the transaction inside the task.

```java
@Autowired TransactionTemplate tx;

reports.forEach(r -> runner.run(() -> tx.executeWithoutResult(status -> save(r))));
```

Or move `save()` into its own bean and call that bean from the lambda.

The wrapper brings its own proxy, not the target's.

</details>

</details>