package com.hackforge.orderprocessing.repository;

import com.hackforge.orderprocessing.entity.FailedOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FailedOrderRepository extends JpaRepository<FailedOrder, Long> {

    List<FailedOrder> findAllByOrderByFailedAtDesc();

    boolean existsByOrderId(Long orderId);
}