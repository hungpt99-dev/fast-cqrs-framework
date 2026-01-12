package com.example.order;

import com.fast.cqrs.autoconfigure.EnableFast;

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

}
