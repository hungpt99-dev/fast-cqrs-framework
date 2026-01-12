package com.example.order.handler;

import com.example.order.dto.CreateOrderCmd;
import com.example.order.entity.Order;

import com.fast.cqrs.cqrs.CommandHandler;
import com.fast.cqrs.logging.annotation.Loggable;
import com.fast.cqrs.util.IdGenerator;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Handler for CreateOrderCmd.
 */
@Component
public class CreateOrderHandler implements CommandHandler<CreateOrderCmd> {

    private final OrderRepository orderRepository;

    public CreateOrderHandler(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Loggable("Creating new order")
    @Override
    public void handle(CreateOrderCmd cmd) {
        String orderId = cmd.orderId() != null ? cmd.orderId() : IdGenerator.prefixedId("ORD");

        Order order = new Order();
        order.setId(orderId);
        order.setCustomerId(cmd.customerId());
        order.setTotal(cmd.total());
        order.setStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now().toString());

        orderRepository.save(order);

    }
}
