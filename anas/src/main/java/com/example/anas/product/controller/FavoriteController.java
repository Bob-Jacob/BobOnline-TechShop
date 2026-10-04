package com.example.anas.product.controller;

import com.example.anas.auth.entity.User;
import com.example.anas.auth.repository.UserRepository;
import com.example.anas.product.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {
    private final FavoriteService favoriteService;
    private final UserRepository userRepository;

    @PostMapping("/{productId}")
    public ResponseEntity<Map<String, Object>> addToFavorites(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails,
            @PathVariable Long productId) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = getCurrentUser(userDetails);
            favoriteService.addToFavorites(user, productId);
            response.put("success", true);
            response.put("message", "Товар добавлен в избранное");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Map<String, Object>> removeFromFavorites(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails,
            @PathVariable Long productId) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = getCurrentUser(userDetails);
            favoriteService.removeFromFavorites(user, productId);
            response.put("success", true);
            response.put("message", "Товар удален из избранного");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/{productId}")
    public ResponseEntity<Map<String, Object>> isFavorite(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails,
            @PathVariable Long productId) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = getCurrentUser(userDetails);
            boolean isFav = favoriteService.isFavorite(user, productId);
            response.put("isFavorite", isFav);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("isFavorite", false);
            return ResponseEntity.ok(response);
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
