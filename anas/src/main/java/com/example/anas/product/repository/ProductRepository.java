package com.example.anas.product.repository;

import com.example.anas.product.entity.Product;
import com.example.anas.product.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategory(ProductCategory category);
    List<Product> findByStockGreaterThan(Integer stock);
    List<Product> findByNameContainingIgnoreCase(String name);
    
    // Фильтрация по цене
    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
    List<Product> findByCategoryAndPriceBetween(ProductCategory category, BigDecimal minPrice, BigDecimal maxPrice);
    List<Product> findByNameContainingIgnoreCaseAndPriceBetween(String name, BigDecimal minPrice, BigDecimal maxPrice);
    List<Product> findByStockGreaterThanAndPriceBetween(Integer stock, BigDecimal minPrice, BigDecimal maxPrice);
    
    // Сортировка
    List<Product> findAllByOrderByPriceAsc();
    List<Product> findAllByOrderByPriceDesc();
    List<Product> findAllByOrderByCreatedAtDesc();
    List<Product> findAllByOrderByNameAsc();
    
    @Query("SELECT p FROM Product p WHERE p.averageRating IS NOT NULL ORDER BY p.averageRating DESC")
    List<Product> findAllByOrderByAverageRatingDesc();
}
