package com.hackforge.orderprocessing.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "failed_orders",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_failed_orders_order_id",
                columnNames = "order_id"
        )
)
public class FailedOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private String reason;

    @Column(nullable = false)
    private Integer retryCount;

    @Column(nullable = false)
    private LocalDateTime failedAt;

    public FailedOrder() {
    }

    public FailedOrder(Long orderId, String reason, Integer retryCount) {
        this.orderId = orderId;
        this.reason = reason;
        this.retryCount = retryCount;
        this.failedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getReason() {
        return reason;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public LocalDateTime getFailedAt() {
        return failedAt;
    }
}