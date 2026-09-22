package com.hackforge.orderprocessing.service;

import org.springframework.stereotype.Service;

@Service
public class OrderProcessor {

    private static final int MAX_RETRIES = 3;

    private final OrderTransactionService transactionService;

    public OrderProcessor(
            OrderTransactionService transactionService) {

        this.transactionService = transactionService;
    }

    public void processWithRetry(Long orderId) {

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {

            System.out.println(
                    "Order #" + orderId +
                            " | Attempt " + attempt +
                            "/" + MAX_RETRIES
            );

            try {

                OrderTransactionService.ProcessResult result =
                        transactionService.processOnce(orderId);

                if (result == OrderTransactionService.ProcessResult.DONE) {
                    return;
                }

                if (result == OrderTransactionService.ProcessResult.OUT_OF_STOCK) {
                    /*
                     * Deterministic terminal outcome.
                     * Retrying will never succeed, and this is NOT a
                     * transient system failure — so it must not go to the DLQ.
                     */
                    return;
                }

                // result == RETRY -> fall through to backoff + next attempt

            } catch (Exception e) {

                System.out.println(
                        "Order #" + orderId +
                                " | Attempt " + attempt +
                                " failed: " +
                                e.getMessage()
                );
            }

            /*
             * Small bounded backoff.
             */
            try {

                Thread.sleep(100L * attempt);

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();
                return;
            }
        }

        transactionService.moveToDlq(
                orderId,
                "Maximum retry attempts exceeded"
        );
    }
}