package com.example.anas.order.repository;

import com.example.anas.order.entity.Cart;
import com.example.anas.order.entity.CartItem;
import com.example.anas.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
    void deleteByCart(Cart cart);
    Long countByProduct(Product product);
}
