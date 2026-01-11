# Domain Events

## Overview

Domain events enable loose coupling between components. When something important happens (e.g., order created), publish an event and let interested parties react.

## Quick Start

### 1. Define Event

```java
public class OrderCreatedEvent extends DomainEvent {
    private final String orderId;
    private final String customerId;
    
    public OrderCreatedEvent(String orderId, String customerId) {
        super(orderId); // aggregateId
        this.orderId = orderId;
        this.customerId = customerId;
    }
    
    public String getOrderId() { return orderId; }
    public String getCustomerId() { return customerId; }
}
```

### 2. Publish Event

```java
@Service
public class OrderService {
    
    @Autowired
    private EventBus eventBus;
    
    public void createOrder(CreateOrderCmd cmd) {
        Order order = orderRepository.save(toOrder(cmd));
        eventBus.publish(new OrderCreatedEvent(order.getId(), cmd.customerId()));
    }
}
```

Or use annotation:

```java
@PublishEvent(OrderCreatedEvent.class)
@Command
void createOrder(@RequestBody CreateOrderCmd cmd);
```

### 3. Handle Event

```java
@Component
public class NotificationHandler implements EventHandler<OrderCreatedEvent> {
    
    @Override
    public void handle(OrderCreatedEvent event) {
        emailService.sendOrderConfirmation(event.getCustomerId(), event.getOrderId());
    }
}
```

## Event Properties

| Property | Description |
|----------|-------------|
| `eventId` | Unique ID (auto-generated UUID) |
| `occurredAt` | Timestamp |
| `aggregateId` | Related entity ID |
| `eventType` | Class simple name |
