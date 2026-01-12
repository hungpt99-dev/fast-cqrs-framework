# Concurrent Module

Unified concurrency framework for Java 21+ with Virtual Threads, parallel execution, and resilience patterns.

## Installation

```groovy
implementation 'com.fast:fast-cqrs-concurrent:1.0.0-SNAPSHOT'
```

## Quick Start

```java
import com.fast.cqrs.concurrent.task.*;
import com.fast.cqrs.concurrent.flow.*;
```

---

## 1. Task API

Unified async API with timeout, retry, and fallback:

```java
User user = Tasks.supply("load-user", () -> userService.load(id))
    .timeout(2, TimeUnit.SECONDS)
    .retry(3)
    .fallback(() -> User.EMPTY)
    .trace()
    .execute();
```

### Execution Strategies

| Strategy | Use Case |
|----------|----------|
| `VIRTUAL_THREAD` | I/O-bound (default) |
| `FORK_JOIN` | CPU-bound |
| `CALLER` | Synchronous |

---

## 2. Parallel Flow

Fan-out/fan-in with declarative syntax:

```java
FlowResult result = ParallelFlow.of()
    .task("user", () -> loadUser())
    .task("orders", () -> loadOrders())
    .task("balance", () -> loadBalance())
    .timeout(3, TimeUnit.SECONDS)
    .failFast()
    .execute();

User user = result.get("user");
if (result.hasErrors()) {
    result.errors().forEach((k, e) -> log.error("{} failed", k, e));
}
```

---

## 3. Task Graph (Dependencies)

Execute tasks with dependencies:

```java
FlowResult result = TaskGraph.of()
    .task("user", () -> loadUser())
    .task("orders", () -> loadOrders())
    .task("profile", g -> buildProfile(g.get("user"), g.get("orders")))
        .dependsOn("user", "orders")
    .execute();
```

---

## 4. Structured Concurrency

Parent-child task ownership with auto-cancellation:

```java
try (TaskScope scope = TaskScope.open("load-profile")) {
    Future<User> user = scope.fork(() -> loadUser());
    Future<List<Order>> orders = scope.fork(() -> loadOrders());
    
    scope.join();
    return new Profile(user.get(), orders.get());
}
```

---

## 5. Parallel Stream

Bounded parallel collections:

```java
List<Enriched> results = ParallelStream.from(users)
    .parallel(10)
    .map(this::enrich)
    .timeoutPerItem(200, TimeUnit.MILLISECONDS)
    .retryPerItem(2)
    .skipOnError()
    .collect();
```

---

## 6. Circuit Breaker

Resilient execution:

```java
CircuitBreaker breaker = CircuitBreaker.of("payment")
    .failureThreshold(5)
    .resetTimeout(30)
    .build();

Result result = breaker.execute(() -> paymentService.call());
```

---

## 7. Executor Registry

Named executors with CPU/IO separation:

```java
ExecutorRegistry.register("db", ExecutorType.IO, 50);
ExecutorRegistry.register("compute", ExecutorType.CPU);

ExecutorService exec = ExecutorRegistry.get("db");
```

---

## 8. Context Propagation

Automatic MDC/SecurityContext propagation:

```java
ContextSnapshot snapshot = ContextSnapshot.capture();
executor.submit(snapshot.wrap(() -> {
    // MDC and SecurityContext available
}));
```

---

## 9. Observability

### Metrics

```java
Tasks.supply("my-task", () -> doWork())
    .listener(TaskMetrics.listener())
    .execute();

TaskMetrics.Stats stats = TaskMetrics.stats("my-task");
```

### OpenTelemetry

```java
OpenTelemetryTracing.configure(otel);
task.listener(OpenTelemetryTracing.listener()).execute();
```

---

## 10. Spring Boot Config

```yaml
fast.concurrent:
  default-mode: virtual-thread
  executors:
    db:
      type: io
      max-threads: 50
    compute:
      type: cpu
```

---

## API Reference

| Class | Description |
|-------|-------------|
| `Tasks` | Task factory |
| `ParallelFlow` | Fan-out/fan-in |
| `TaskGraph` | DAG execution |
| `TaskScope` | Structured concurrency |
| `ParallelStream` | Bounded collections |
| `CircuitBreaker` | Resilience |
| `ExecutorRegistry` | Executor management |
| `ContextSnapshot` | Context propagation |
| `TaskMetrics` | Observability |
