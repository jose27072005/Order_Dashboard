package com.hackforge.orderprocessing.repository;

import com.hackforge.orderprocessing.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Modifying
    @Query("""
        UPDATE Product p
        SET p.stock = p.stock - :quantity
        WHERE p.id = :productId
        AND p.stock >= :quantity
    """)
    int decreaseStock(
            @Param("productId") Long productId,
            @Param("quantity") int quantity
    );
}