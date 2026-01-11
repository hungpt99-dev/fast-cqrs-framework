package com.example.order.controller;

import com.example.order.dto.CreateOrderCmd;
import com.example.order.dto.GetOrderQuery;
import com.example.order.dto.GetOrdersByCustomerQuery;
import com.example.order.dto.OrderDto;
import com.example.order.handler.CreateOrderHandler;
import com.example.order.handler.GetOrderHandler;
import com.example.order.handler.GetOrdersByCustomerHandler;
import com.fast.cqrs.annotation.CacheableQuery;
import com.fast.cqrs.annotation.Command;
import com.fast.cqrs.annotation.HttpController;
import com.fast.cqrs.annotation.Metrics;
import com.fast.cqrs.annotation.Query;
import com.fast.cqrs.annotation.RetryCommand;

import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;

/**
 * Order API Controller demonstrating CQRS features.
 * 
 * REST conventions:
 * - @Query + @GetMapping for reads
 * - @Command + @PostMapping for writes
 */
@HttpController
@RequestMapping("/api/orders")
public interface OrderController {

    /**
     * Get order by ID.
     */
    @CacheableQuery(ttl = "5m")
    @Metrics(name = "orders.get")
    @Query(handler = GetOrderHandler.class)
    @GetMapping("/{id}")
    OrderDto getOrder(@PathVariable String id);

    /**
     * Get orders by customer.
     */
    @CacheableQuery(ttl = "1m")
    @Query(handler = GetOrdersByCustomerHandler.class)
    @GetMapping
    List<OrderDto> getOrdersByCustomer(@RequestParam String customerId);

    /**
     * Create a new order.
     */
    @RetryCommand(maxAttempts = 3, backoff = "100ms")
    @Metrics(name = "orders.create")
    @Command(handler = CreateOrderHandler.class)
    @PostMapping
    void createOrder(@Valid @RequestBody CreateOrderCmd cmd);
}
