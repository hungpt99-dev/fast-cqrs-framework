# CQRS Features

## Command/Query Annotations

### Caching

```java
@CacheableQuery(ttl = "5m", key = "#query.id")
@Query
@GetMapping("/{id}")
OrderDto getOrder(@PathVariable String id);
```

| Attribute | Description |
|-----------|-------------|
| `ttl` | Cache duration: "30s", "5m", "1h", "1d" |
| `key` | Cache key expression |
| `cacheName` | Cache region name |

---

### Retry

```java
@RetryCommand(maxAttempts = 3, backoff = "100ms")
@Command
@PostMapping
void processPayment(@RequestBody PaymentCmd cmd);
```

| Attribute | Description |
|-----------|-------------|
| `maxAttempts` | Max retry attempts (default: 3) |
| `backoff` | Delay between retries |
| `multiplier` | Exponential backoff multiplier |
| `retryOn` | Exception types to retry |

---

### Async

```java
@AsyncCommand
@Command
@PostMapping("/notify")
void sendNotification(@RequestBody NotifyCmd cmd);
```

Fire-and-forget execution in background thread.

---

### Idempotent

```java
@IdempotentCommand(key = "#cmd.orderId", ttl = "24h")
@Command
@PostMapping
void createOrder(@RequestBody CreateOrderCmd cmd);
```

Prevents duplicate command execution.

---

### Timeout

```java
@Timeout("5s")
@Query
@GetMapping("/report")
ReportDto generateReport();
```

---

### Metrics

```java
@Metrics(name = "orders.create")
@Command
@PostMapping
void createOrder(@RequestBody CreateOrderCmd cmd);
```

Collects execution count, time, error rate.

---

### Tracing

```java
@Traced(spanName = "create-order", logArgs = true)
@Command
@PostMapping
void createOrder(@RequestBody CreateOrderCmd cmd);
```

---

## Domain Events

```java
// Define event
public class OrderCreatedEvent extends DomainEvent {
    private final String orderId;
    public OrderCreatedEvent(String orderId) {
        this.orderId = orderId;
    }
}

// Publish event
@PublishEvent(OrderCreatedEvent.class)
@Command
void createOrder(@RequestBody CreateOrderCmd cmd);

// Handle event
@Component
public class OrderCreatedHandler implements EventHandler<OrderCreatedEvent> {
    @Override
    public void handle(OrderCreatedEvent event) {
        // Send notification, update read model, etc.
    }
}
```

---

## Interceptors

Custom interceptor chain for command/query processing:

```java
@Component
public class LoggingInterceptor implements CommandInterceptor {
    @Override
    public Object intercept(InterceptorContext ctx, InterceptorChain chain) throws Exception {
        log.info("Executing: {}", ctx.getMethod().getName());
        return chain.proceed(ctx);
    }
    
    @Override
    public int getOrder() { return 100; }
}
```

Built-in interceptors:
- `CacheInterceptor` - Caching
- `RetryInterceptor` - Retry logic
- `MetricsInterceptor` - Metrics collection
