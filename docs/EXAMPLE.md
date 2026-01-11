# Complete Example

A full example showing all framework features working together.

## Entity

```java
@Table("orders")
public class Order {
    @Id
    private String id;

    @Column("customer_id")
    private String customerId;

    private BigDecimal total;
    private String status;

    @Column("created_at")
    private LocalDateTime createdAt;
}
```

## DTOs / Commands / Queries

```java
public record OrderDto(
    String id,
    String customerId,
    BigDecimal total,
    String status,
    String createdAt
) {}

public record CreateOrderCmd(
    @NotBlank String customerId,
    @NotNull @Min(1) BigDecimal total
) {}

public record GetOrderQuery(String id) {}
```

## Repository

```java
@SqlRepository
public interface OrderRepository extends FastRepository<Order, String> {
    // CRUD methods work automatically!
    // - findById(id)
    // - findAll()
    // - save(entity)
    // - saveAll(entities)
    // - deleteById(id)

    // Custom queries:
    @Select("SELECT * FROM orders WHERE customer_id = :customerId")
    List<Order> findByCustomerId(@Param("customerId") String customerId);
}
```

## Controller

```java
@HttpController
@RequestMapping("/api/orders")
public interface OrderController {

    @CacheableQuery(ttl = "5m")
    @Metrics(name = "orders.get")
    @Query
    @PostMapping("/get")
    OrderDto getOrder(@RequestBody GetOrderQuery query);

    @RetryCommand(maxAttempts = 3)
    @Metrics(name = "orders.create")
    @Command
    @PostMapping
    void createOrder(@Valid @RequestBody CreateOrderCmd cmd);
}
```

## Handlers

```java
@Component
public class GetOrderHandler implements QueryHandler<GetOrderQuery, OrderDto> {

    private final OrderRepository repository;

    @Override
    public OrderDto handle(GetOrderQuery query) {
        return repository.findById(query.id())
            .map(this::toDto)
            .orElse(null);
    }
}

@Component
public class CreateOrderHandler implements CommandHandler<CreateOrderCmd> {

    private final OrderRepository repository;
    private final EventBus eventBus;

    @Override
    public void handle(CreateOrderCmd cmd) {
        Order order = new Order();
        order.setId(UUID.randomUUID().toString());
        order.setCustomerId(cmd.customerId());
        order.setTotal(cmd.total());
        order.setStatus("PENDING");
        
        repository.save(order);
        eventBus.publish(new OrderCreatedEvent(order.getId()));
    }
}
```

## Event Handler

```java
@Component
public class OrderCreatedHandler implements EventHandler<OrderCreatedEvent> {

    @Override
    public void handle(OrderCreatedEvent event) {
        log.info("Order created: {}", event.getOrderId());
        // Send notification, update analytics, etc.
    }
}
```

## Application

```java
@SpringBootApplication
@EnableCqrs(basePackages = "com.example.controller")
@EnableSqlRepositories(basePackages = "com.example.repository")
public class OrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }
}
```

## API Requests

```bash
# Create order
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"customerId": "C001", "total": 99.99}'

# Get order
curl -X POST http://localhost:8080/api/orders/get \
  -H "Content-Type: application/json" \
  -d '{"id": "ORD-001"}'
```
