package com.example.anas.product.repository;

import com.example.anas.product.entity.Product;
import com.example.anas.product.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProduct(Product product);
    List<Review> findByProductAndIsApprovedTrue(Product product);
    List<Review> findByUser(com.example.anas.auth.entity.User user);
    Optional<Review> findByUserAndProduct(com.example.anas.auth.entity.User user, Product product);
    Long countByProduct(Product product);
    Long countByProductAndIsApprovedTrue(Product product);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product = :product AND r.isApproved = true")
    Double averageRatingByProduct(@Param("product") Product product);
}
