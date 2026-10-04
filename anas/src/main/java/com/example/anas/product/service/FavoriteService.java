package com.example.anas.product.service;

import com.example.anas.auth.entity.User;
import com.example.anas.product.entity.Favorite;
import com.example.anas.product.entity.Product;
import com.example.anas.product.repository.FavoriteRepository;
import com.example.anas.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteService {
    private final FavoriteRepository favoriteRepository;
    private final ProductRepository productRepository;

    @Transactional
    public Favorite addToFavorites(User user, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Товар не найден"));

        if (favoriteRepository.existsByUserAndProduct(user, product)) {
            throw new IllegalArgumentException("Товар уже в избранном");
        }

        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setProduct(product);
        return favoriteRepository.save(favorite);
    }

    @Transactional
    public void removeFromFavorites(User user, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Товар не найден"));
        favoriteRepository.deleteByUserAndProduct(user, product);
    }

    public List<Product> getUserFavorites(User user) {
        return favoriteRepository.findByUser(user).stream()
                .map(Favorite::getProduct)
                .collect(Collectors.toList());
    }

    public boolean isFavorite(User user, Long productId) {
        Product product = productRepository.findById(productId)
                .orElse(null);
        if (product == null) return false;
        return favoriteRepository.existsByUserAndProduct(user, product);
    }
}
