package com.example.order.dto;

import com.fast.cqrs.sql.repository.Id;
import com.fast.cqrs.sql.repository.Table;
import com.fast.cqrs.sql.repository.Column;

import java.math.BigDecimal;

/**
 * Order entity for database operations.
 * 
 * Uses FastRepository annotations for auto-mapping.
 */
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

    // Default constructor for ORM
    public OrderDto() {}

    public OrderDto(String id, String customerId, BigDecimal total, String status, String createdAt) {
        this.id = id;
        this.customerId = customerId;
        this.total = total;
        this.status = status;
        this.createdAt = createdAt;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
