package com.example.anas.order.controller;

import com.example.anas.auth.entity.User;
import com.example.anas.auth.repository.UserRepository;
import com.example.anas.order.entity.Cart;
import com.example.anas.order.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartApiController {
    private final CartService cartService;
    private final UserRepository userRepository;

    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getCartCount(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails) {
        try {
            User user = getCurrentUser(userDetails);
            Cart cart = cartService.getOrCreateCart(user);
            int count = cart.getItems().stream()
                    .mapToInt(item -> item.getQuantity())
                    .sum();
            
            Map<String, Object> response = new HashMap<>();
            response.put("count", count);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("count", 0);
            response.put("success", false);
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addToCart(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails,
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") Integer quantity) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = getCurrentUser(userDetails);
            cartService.addToCart(user, productId, quantity);
            response.put("success", true);
            response.put("message", "Товар добавлен в корзину");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    private User getCurrentUser(org.springframework.security.core.userdetails.UserDetails userDetails) {
        if (userDetails == null) {
            throw new IllegalArgumentException("Пользователь не авторизован");
        }
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
    }
}
