package com.hackforge.orderprocessing.controller;

import com.hackforge.orderprocessing.entity.FailedOrder;
import com.hackforge.orderprocessing.repository.FailedOrderRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dlq")
@CrossOrigin(origins = "http://localhost:5173")
public class DlqController {

    private final FailedOrderRepository failedOrderRepository;

    public DlqController(FailedOrderRepository failedOrderRepository) {
        this.failedOrderRepository = failedOrderRepository;
    }

    @GetMapping
    public List<FailedOrder> getFailedOrders() {
        return failedOrderRepository.findAllByOrderByFailedAtDesc();
    }
}