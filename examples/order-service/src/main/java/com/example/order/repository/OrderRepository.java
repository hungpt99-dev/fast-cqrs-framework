package com.example.order.repository;

import com.example.order.dto.OrderDto;
import com.fast.cqrs.sql.annotation.Param;
import com.fast.cqrs.sql.annotation.Select;
import com.fast.cqrs.sql.annotation.SqlRepository;
import com.fast.cqrs.sql.repository.FastRepository;

import java.util.List;

/**
 * SQL Repository for Order operations.
 * 
 * Extends FastRepository for automatic CRUD operations.
 * No implementation needed - the framework creates a dynamic proxy at runtime.
 */
@SqlRepository
public interface OrderRepository extends FastRepository<OrderDto, String> {

    // CRUD methods from FastRepository:
    // - findById(String id) 
    // - findAll()
    // - save(OrderDto entity)
    // - saveAll(List<OrderDto> entities)
    // - updateAll(List<OrderDto> entities)
    // - deleteById(String id)
    // - deleteAllById(List<String> ids)
    // - existsById(String id)
    // - count()
    // - deleteAll()

    // Custom queries:
    @Select("SELECT * FROM orders WHERE customer_id = :customerId")
    List<OrderDto> findByCustomerId(@Param("customerId") String customerId);

    @Select("SELECT * FROM orders WHERE status = :status")
    List<OrderDto> findByStatus(@Param("status") String status);
}
