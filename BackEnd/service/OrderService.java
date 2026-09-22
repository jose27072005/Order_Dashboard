package com.hackforge.orderprocessing.service;

import com.hackforge.orderprocessing.dto.CreateOrderRequest;
import com.hackforge.orderprocessing.entity.Order;
import com.hackforge.orderprocessing.entity.OrderStatus;
import com.hackforge.orderprocessing.entity.Product;
import com.hackforge.orderprocessing.repository.OrderRepository;
import com.hackforge.orderprocessing.repository.ProductRepository;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

import java.util.List;
import java.util.concurrent.ExecutorService;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ExecutorService orderExecutor;
    private final OrderProcessor orderProcessor;

    public OrderService(
            OrderRepository orderRepository,
            ProductRepository productRepository,
            ExecutorService orderExecutor,
            OrderProcessor orderProcessor) {

        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.orderExecutor = orderExecutor;
        this.orderProcessor = orderProcessor;
    }

    public Order createOrder(CreateOrderRequest request) {

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Product not found: " + request.getProductId()
                        ));

        Order order = new Order(
                product,
                request.getQuantity()
        );

        order.setStatus(OrderStatus.PENDING);

        Order savedOrder = orderRepository.save(order);

        /*
         * IMPORTANT:
         *
         * The HTTP request does NOT wait for processing.
         *
         * The order is handed to our thread pool.
         */
        orderExecutor.submit(() ->
                orderProcessor.processWithRetry(savedOrder.getId())
        );

        return savedOrder;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }
    public Map<String, Object> stressTest(
            Long productId,
            int numberOfOrders,
            int quantityPerOrder) {

        if (numberOfOrders < 1 || numberOfOrders > 1000) {
            throw new IllegalArgumentException(
                    "Number of orders must be between 1 and 1000"
            );
        }

        if (quantityPerOrder < 1) {
            throw new IllegalArgumentException(
                    "Quantity must be at least 1"
            );
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Product not found: " + productId
                        ));

        int initialStock = product.getStock();

        for (int i = 0; i < numberOfOrders; i++) {

            Order order = new Order(
                    product,
                    quantityPerOrder
            );

            order.setStatus(OrderStatus.PENDING);

            Order savedOrder = orderRepository.save(order);

            orderExecutor.submit(() ->
                    orderProcessor.processWithRetry(savedOrder.getId())
            );
        }

        Map<String, Object> response = new HashMap<>();

        response.put("message", "Stress test started");
        response.put("productId", productId);
        response.put("initialStock", initialStock);
        response.put("ordersSubmitted", numberOfOrders);
        response.put("quantityPerOrder", quantityPerOrder);
        response.put("workers", 10);

        return response;
    }

    public Order getOrder(Long id) {

        return orderRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Order not found: " + id
                        ));
    }
}