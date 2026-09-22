package com.hackforge.orderprocessing.controller;

import com.hackforge.orderprocessing.dto.CreateOrderRequest;
import com.hackforge.orderprocessing.entity.Order;
import com.hackforge.orderprocessing.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:5173")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public Order createOrder(
            @Valid @RequestBody CreateOrderRequest request) {

        return orderService.createOrder(request);
    }

    @GetMapping
    public List<Order> getOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/{id}")
    public Order getOrder(@PathVariable Long id) {
        return orderService.getOrder(id);
    }
    @PostMapping("/stress-test")
    public Map<String, Object> stressTest(
            @RequestParam Long productId,
            @RequestParam int orders,
            @RequestParam int quantity) {

        return orderService.stressTest(
                productId,
                orders,
                quantity
        );
    }
}