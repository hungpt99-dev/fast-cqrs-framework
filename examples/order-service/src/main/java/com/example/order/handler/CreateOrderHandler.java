package com.example.order.handler;

import com.example.order.dto.CreateOrderCmd;
import com.example.order.entity.Order;
import com.example.order.event.OrderCreatedEvent;
import com.example.order.repository.OrderRepository;
import com.fast.cqrs.event.EventBus;
import com.fast.cqrs.handler.CommandHandler;
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
    private final EventBus eventBus;

    public CreateOrderHandler(OrderRepository orderRepository, EventBus eventBus) {
        this.orderRepository = orderRepository;
        this.eventBus = eventBus;
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

        // Publish event
        eventBus.publish(new OrderCreatedEvent(orderId, cmd.customerId(), cmd.total()));
    }
}
