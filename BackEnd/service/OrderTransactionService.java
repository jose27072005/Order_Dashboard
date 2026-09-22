package com.hackforge.orderprocessing.service;

import com.hackforge.orderprocessing.entity.FailedOrder;
import com.hackforge.orderprocessing.entity.Order;
import com.hackforge.orderprocessing.entity.OrderStatus;
import com.hackforge.orderprocessing.repository.FailedOrderRepository;
import com.hackforge.orderprocessing.repository.OrderRepository;
import com.hackforge.orderprocessing.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderTransactionService {

    /**
     * Outcome of a single processing attempt.
     *
     * DONE        -> order finished successfully (or was already SUCCESS)
     * OUT_OF_STOCK-> deterministic terminal failure; do NOT retry, do NOT DLQ
     * RETRY       -> transient failure; safe to retry
     */
    public enum ProcessResult {
        DONE,
        OUT_OF_STOCK,
        RETRY
    }

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final FailedOrderRepository failedOrderRepository;

    public OrderTransactionService(
            OrderRepository orderRepository,
            ProductRepository productRepository,
            FailedOrderRepository failedOrderRepository) {

        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.failedOrderRepository = failedOrderRepository;
    }

    @Transactional
    public ProcessResult processOnce(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Order not found: " + orderId
                        ));

        if (order.getStatus() == OrderStatus.SUCCESS) {
            return ProcessResult.DONE;
        }

        if (order.getStatus() == OrderStatus.OUT_OF_STOCK) {
            return ProcessResult.OUT_OF_STOCK;
        }

        order.setStatus(OrderStatus.PROCESSING);
        orderRepository.save(order);

        Long productId = order.getProduct().getId();
        int quantity = order.getQuantity();

        System.out.println(
                "Worker: " +
                        Thread.currentThread().getName() +
                        " | Order #" +
                        orderId +
                        " | Product #" +
                        productId +
                        " | Quantity=" +
                        quantity
        );

        /*
         * ATOMIC INVENTORY OPERATION
         *
         * PostgreSQL performs:
         *
         * UPDATE products
         * SET stock = stock - quantity
         * WHERE id = productId
         * AND stock >= quantity
         *
         * 1 = inventory successfully reserved
         * 0 = insufficient inventory
         */
        int updatedRows =
                productRepository.decreaseStock(
                        productId,
                        quantity
                );

        if (updatedRows == 0) {

            order.setStatus(OrderStatus.OUT_OF_STOCK);
            order.setRetryCount(
                    order.getRetryCount() + 1
            );

            orderRepository.save(order);

            System.out.println(
                    "Order #" +
                            orderId +
                            " -> OUT_OF_STOCK"
            );

            return ProcessResult.OUT_OF_STOCK;
        }

        order.setStatus(OrderStatus.SUCCESS);
        orderRepository.save(order);

        System.out.println(
                "Order #" +
                        orderId +
                        " -> SUCCESS"
        );

        return ProcessResult.DONE;
    }

    /**
     * Moves an order to the DLQ. Idempotent: if a DLQ row already exists
     * for this orderId, it is not duplicated. If the order already
     * succeeded or is OUT_OF_STOCK, it is not moved.
     */
    @Transactional
    public void moveToDlq(Long orderId, String reason) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Order not found: " + orderId
                        ));

        if (order.getStatus() == OrderStatus.SUCCESS
                || order.getStatus() == OrderStatus.OUT_OF_STOCK) {
            return;
        }

        // Idempotency: only insert if we haven't already recorded a DLQ row.
        if (failedOrderRepository.existsByOrderId(orderId)) {
            System.out.println(
                    "Order #" + orderId + " already in DLQ; skipping."
            );
            return;
        }

        order.setStatus(OrderStatus.FAILED);

        FailedOrder failedOrder = new FailedOrder(
                order.getId(),
                reason,
                order.getRetryCount()
        );

        failedOrderRepository.save(failedOrder);
        orderRepository.save(order);

        System.out.println(
                "Order #" +
                        orderId +
                        " -> DEAD LETTER QUEUE"
        );
    }
}