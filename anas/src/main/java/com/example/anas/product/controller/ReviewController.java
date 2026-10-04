package com.example.anas.product.controller;

import com.example.anas.auth.entity.User;
import com.example.anas.auth.repository.UserRepository;
import com.example.anas.product.entity.Product;
import com.example.anas.product.entity.Review;
import com.example.anas.product.service.ProductService;
import com.example.anas.product.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;
    private final ProductService productService;
    private final UserRepository userRepository;

    @PostMapping("/product/{productId}/review")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addReview(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails,
            @PathVariable Long productId,
            @RequestParam Integer rating,
            @RequestParam(required = false) String comment) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = getCurrentUser(userDetails);
            Review review = reviewService.addReview(user, productId, rating, comment);
            response.put("success", true);
            response.put("message", "Отзыв добавлен и ожидает модерации");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/product/{productId}/reviews")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getReviews(@PathVariable Long productId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Review> reviews = reviewService.getProductReviews(productId, true);
            response.put("success", true);
            response.put("reviews", reviews);
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
