package com.example.order;

import com.fast.cqrs.autoconfigure.EnableFast;

import com.fast.cqrs.event.AsyncEventBus;
import com.fast.cqrs.event.EventBus;
import com.fast.cqrs.eventsourcing.EventStore;
import com.fast.cqrs.eventsourcing.InMemoryEventStore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Example application demonstrating Fast Framework with DX features.
 * 
 * Uses @EnableFast for zero-config setup.
 */
@SpringBootApplication
@EnableFast
public class OrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }

    @Bean
    public EventBus eventBus() {
        return new AsyncEventBus();
    }

    @Bean
    public EventStore eventStore() {
        return new InMemoryEventStore();
    }
}
