# Complete Example

A full example showing all framework features working together.

## Project Structure

```
order-service/

├── controller/
│   └── OrderController.java     # CQRS controller
├── dto/
│   ├── CreateOrderCmd.java      # Command
│   ├── GetOrderQuery.java       # Query
│   └── OrderDto.java            # Entity with @Table

├── handler/
│   ├── CreateOrderHandler.java  # Command handler
│   └── GetOrderHandler.java     # Query handler
└── repository/
    └── OrderRepository.java     # FastRepository
```

## Entity

```java
@Table("orders")
public class OrderDto {
    @Id
    private String id;

    @Column("customer_id")
    private String customerId;

    private BigDecimal total;
    private String status;

    @Column("created_at")
    private String createdAt;
}
```

## Repository (CRUD + Custom)

```java
@SqlRepository
public interface OrderRepository extends FastRepository<OrderDto, String> {
    // Auto: findById, findAll, save, saveAll, updateAll, deleteById, etc.

    @Select("SELECT * FROM orders WHERE customer_id = :customerId")
    List<OrderDto> findByCustomerId(@Param("customerId") String customerId);
}
```

## Controller with Annotations

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



## Configuration

```java
@SpringBootApplication
@EnableFast
public class OrderApplication {
    


    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }
}
```

## Features Used

| Feature | Annotation/Class |
|---------|------------------|
| CQRS | `@Query`, `@Command` |
| Repository | `FastRepository`, `@Table`, `@Id` |
| Batch | `saveAll()`, `updateAll()` |
| Caching | `@CacheableQuery` |
| Retry | `@RetryCommand` |
| Metrics | `@Metrics` |
| Validation | `@Valid` |

