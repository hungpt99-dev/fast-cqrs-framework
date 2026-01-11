package com.fast.cqrs.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Publishes a domain event after successful command execution.
 * <p>
 * Example:
 * <pre>{@code
 * @PublishEvent(OrderCreatedEvent.class)
 * @Command
 * void createOrder(@RequestBody CreateOrderCmd cmd);
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PublishEvent {
    
    /**
     * Event class to publish.
     */
    Class<?> value();
    
    /**
     * Whether to publish before or after the command.
     */
    When when() default When.AFTER;
    
    /**
     * When to publish the event.
     */
    enum When {
        BEFORE, AFTER
    }
}
