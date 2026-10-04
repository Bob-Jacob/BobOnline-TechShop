package com.example.anas.product.service;

import com.example.anas.auth.entity.User;
import com.example.anas.order.repository.OrderRepository;
import com.example.anas.product.entity.Product;
import com.example.anas.product.entity.Review;
import com.example.anas.product.repository.ProductRepository;
import com.example.anas.product.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public Review addReview(User user, Long productId, Integer rating, String comment) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Рейтинг должен быть от 1 до 5");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Товар не найден"));

        // Проверка, что пользователь уже оставлял отзыв
        Optional<Review> existingReview = reviewRepository.findByUserAndProduct(user, product);
        if (existingReview.isPresent()) {
            throw new IllegalArgumentException("Вы уже оставили отзыв на этот товар");
        }

        // Проверка, что пользователь купил товар
        boolean hasPurchased = orderRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .flatMap(order -> order.getItems().stream())
                .anyMatch(item -> item.getProduct().getId().equals(productId));

        if (!hasPurchased) {
            throw new IllegalArgumentException("Вы можете оставить отзыв только после покупки товара");
        }

        Review review = new Review();
        review.setProduct(product);
        review.setUser(user);
        review.setRating(rating);
        review.setComment(comment);
        review.setIsApproved(false); // Требует модерации

        review = reviewRepository.save(review);
        calculateAverageRating(product);
        return review;
    }

    public List<Review> getProductReviews(Long productId, boolean approvedOnly) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Товар не найден"));

        if (approvedOnly) {
            return reviewRepository.findByProductAndIsApprovedTrue(product);
        }
        return reviewRepository.findByProduct(product);
    }

    @Transactional
    public void approveReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Отзыв не найден"));
        review.setIsApproved(true);
        reviewRepository.save(review);
        calculateAverageRating(review.getProduct());
    }

    @Transactional
    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Отзыв не найден"));
        Product product = review.getProduct();
        reviewRepository.delete(review);
        calculateAverageRating(product);
    }

    @Transactional
    public void calculateAverageRating(Product product) {
        Double avgRating = reviewRepository.averageRatingByProduct(product);
        Long reviewsCount = reviewRepository.countByProductAndIsApprovedTrue(product);

        if (avgRating != null) {
            product.setAverageRating(BigDecimal.valueOf(avgRating)
                    .setScale(2, RoundingMode.HALF_UP));
        } else {
            product.setAverageRating(null);
        }
        product.setReviewsCount(reviewsCount);
        productRepository.save(product);
    }

    public boolean hasUserPurchasedProduct(User user, Product product) {
        return orderRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .flatMap(order -> order.getItems().stream())
                .anyMatch(item -> item.getProduct().getId().equals(product.getId()));
    }

    public Optional<Review> getUserReview(User user, Product product) {
        return reviewRepository.findByUserAndProduct(user, product);
    }

    public List<Review> getAllReviews(Boolean approvedOnly) {
        if (approvedOnly != null && approvedOnly) {
            return reviewRepository.findAll().stream()
                    .filter(Review::getIsApproved)
                    .toList();
        }
        return reviewRepository.findAll();
    }
}
