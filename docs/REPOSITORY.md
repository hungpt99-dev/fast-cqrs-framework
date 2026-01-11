# FastRepository

Fast CQRS SQL module provides a Spring Data JPA-like repository with automatic CRUD operations.

## Quick Start

```java
@SqlRepository
public interface OrderRepository extends FastRepository<Order, Long> {
    // CRUD methods work automatically - no SQL needed!
    
    // Add custom queries as needed:
    @Select("SELECT * FROM orders WHERE customer_id = :customerId")
    List<Order> findByCustomerId(@Param("customerId") Long customerId);
}
```

## Entity Mapping

```java
@Table("orders")  // Optional: defaults to snake_case of class name
public class Order {
    
    @Id
    private Long id;
    
    @Column("cust_id")  // Optional: defaults to snake_case of field name
    private Long customerId;
    
    private String status;      // → status column
    private LocalDate orderDate; // → order_date column
}
```

## Available Methods

| Method | Description |
|--------|-------------|
| `findById(ID id)` | Find by primary key |
| `findAll()` | Get all entities |
| `save(T entity)` | Insert or update |
| `saveAll(List<T>)` | Batch insert |
| `updateAll(List<T>)` | Batch update |
| `deleteById(ID id)` | Delete by ID |
| `deleteAllById(List<ID>)` | Batch delete |
| `existsById(ID id)` | Check existence |
| `count()` | Count all |
| `deleteAll()` | Delete all |

## Pagination

```java
public interface OrderRepository extends PagingRepository<Order, Long> {
    // Adds pagination support
}

// Usage:
Page<Order> page = orderRepository.findAll(Pageable.of(0, 20));
List<Order> sorted = orderRepository.findAll(Sort.by("createdAt").descending());
```

## Multi-Database Support

Auto-detects: MySQL, PostgreSQL, Oracle, SQL Server, H2

```java
// Manual selection
DatabaseDialect dialect = DialectFactory.forName("postgresql");
```
