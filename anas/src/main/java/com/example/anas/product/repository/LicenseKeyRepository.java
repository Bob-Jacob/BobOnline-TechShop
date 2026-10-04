package com.example.anas.product.repository;

import com.example.anas.product.entity.LicenseKey;
import com.example.anas.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LicenseKeyRepository extends JpaRepository<LicenseKey, Long> {
    List<LicenseKey> findByProductAndIsUsedFalse(Product product);
    List<LicenseKey> findByProduct(Product product);
    Optional<LicenseKey> findFirstByProductAndIsUsedFalse(Product product);
    Long countByProductAndIsUsedFalse(Product product);
    List<LicenseKey> findByOrderId(Long orderId);
    Long countByProduct(Product product);
}
