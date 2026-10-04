package com.example.anas.order.repository;

import com.example.anas.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    Long countByProduct(com.example.anas.product.entity.Product product);
}
