package com.example.anas.product.service;

import com.example.anas.order.repository.CartItemRepository;
import com.example.anas.order.repository.OrderItemRepository;
import com.example.anas.product.entity.LicenseKey;
import com.example.anas.product.entity.Product;
import com.example.anas.product.entity.ProductCategory;
import com.example.anas.product.entity.ProductSortOption;
import com.example.anas.product.repository.FavoriteRepository;
import com.example.anas.product.repository.LicenseKeyRepository;
import com.example.anas.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final LicenseKeyRepository licenseKeyRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderItemRepository orderItemRepository;
    private final FavoriteRepository favoriteRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getProductsByCategory(ProductCategory category) {
        return productRepository.findByCategory(category);
    }

    public List<Product> getAvailableProducts() {
        return productRepository.findByStockGreaterThan(0);
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public List<Product> searchProducts(String query) {
        return productRepository.findByNameContainingIgnoreCase(query);
    }

    public List<Product> getFilteredProducts(ProductCategory category, String search,
                                            BigDecimal minPrice, BigDecimal maxPrice,
                                            Boolean inStock, ProductSortOption sortBy) {
        List<Product> products;

        // Базовый запрос с учетом категории и поиска
        if (search != null && !search.isEmpty()) {
            if (category != null) {
                products = productRepository.findByNameContainingIgnoreCase(search).stream()
                        .filter(p -> p.getCategory() == category)
                        .collect(Collectors.toList());
            } else {
                products = productRepository.findByNameContainingIgnoreCase(search);
            }
        } else if (category != null) {
            products = productRepository.findByCategory(category);
        } else {
            products = productRepository.findAll();
        }

        // Фильтрация по цене
        if (minPrice != null || maxPrice != null) {
            BigDecimal min = minPrice != null ? minPrice : BigDecimal.ZERO;
            BigDecimal max = maxPrice != null ? maxPrice : BigDecimal.valueOf(Long.MAX_VALUE);
            products = products.stream()
                    .filter(p -> {
                        BigDecimal price = p.getFinalPrice();
                        return price.compareTo(min) >= 0 && price.compareTo(max) <= 0;
                    })
                    .collect(Collectors.toList());
        }

        // Фильтрация по наличию
        if (inStock != null && inStock) {
            products = products.stream()
                    .filter(p -> p.getStock() > 0)
                    .collect(Collectors.toList());
        }

        // Сортировка
        if (sortBy != null) {
            switch (sortBy) {
                case PRICE_ASC:
                    products = products.stream()
                            .sorted((p1, p2) -> p1.getFinalPrice().compareTo(p2.getFinalPrice()))
                            .collect(Collectors.toList());
                    break;
                case PRICE_DESC:
                    products = products.stream()
                            .sorted((p1, p2) -> p2.getFinalPrice().compareTo(p1.getFinalPrice()))
                            .collect(Collectors.toList());
                    break;
                case NAME_ASC:
                    products = products.stream()
                            .sorted((p1, p2) -> p1.getName().compareToIgnoreCase(p2.getName()))
                            .collect(Collectors.toList());
                    break;
                case DATE_DESC:
                    products = products.stream()
                            .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()))
                            .collect(Collectors.toList());
                    break;
                case RATING_DESC:
                    products = products.stream()
                            .sorted((p1, p2) -> {
                                BigDecimal r1 = p1.getAverageRating() != null ? p1.getAverageRating() : BigDecimal.ZERO;
                                BigDecimal r2 = p2.getAverageRating() != null ? p2.getAverageRating() : BigDecimal.ZERO;
                                return r2.compareTo(r1);
                            })
                            .collect(Collectors.toList());
                    break;
            }
        }

        return products;
    }

    @Transactional
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Товар не найден"));
        
        // Каскадное удаление связанных данных
        // Удаляем лицензионные ключи
        licenseKeyRepository.deleteAll(licenseKeyRepository.findByProduct(product));
        
        // Удаляем из корзин (CartItem)
        List<com.example.anas.order.entity.CartItem> cartItems = cartItemRepository.findAll().stream()
                .filter(item -> item.getProduct().getId().equals(id))
                .collect(Collectors.toList());
        cartItemRepository.deleteAll(cartItems);
        
        // Удаляем из избранного
        List<com.example.anas.product.entity.Favorite> favorites = favoriteRepository.findAll().stream()
                .filter(fav -> fav.getProduct().getId().equals(id))
                .collect(Collectors.toList());
        favoriteRepository.deleteAll(favorites);
        
        // OrderItem не удаляем, так как они являются частью истории заказов
        
        // Удаляем сам товар
        productRepository.delete(product);
    }
    
    public ProductDeletionInfo getDeletionInfo(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Товар не найден"));
        
        Long licenseKeysCount = licenseKeyRepository.countByProduct(product);
        Long cartItemsCount = cartItemRepository.countByProduct(product);
        Long orderItemsCount = orderItemRepository.countByProduct(product);
        Long favoritesCount = favoriteRepository.countByProduct(product);
        
        return new ProductDeletionInfo(licenseKeysCount, cartItemsCount, orderItemsCount, favoritesCount);
    }
    
    public static class ProductDeletionInfo {
        private final Long licenseKeysCount;
        private final Long cartItemsCount;
        private final Long orderItemsCount;
        private final Long favoritesCount;
        
        public ProductDeletionInfo(Long licenseKeysCount, Long cartItemsCount, 
                                   Long orderItemsCount, Long favoritesCount) {
            this.licenseKeysCount = licenseKeysCount;
            this.cartItemsCount = cartItemsCount;
            this.orderItemsCount = orderItemsCount;
            this.favoritesCount = favoritesCount;
        }
        
        public Long getLicenseKeysCount() { return licenseKeysCount; }
        public Long getCartItemsCount() { return cartItemsCount; }
        public Long getOrderItemsCount() { return orderItemsCount; }
        public Long getFavoritesCount() { return favoritesCount; }
        
        public boolean hasRelatedData() {
            return licenseKeysCount > 0 || cartItemsCount > 0 || orderItemsCount > 0 || favoritesCount > 0;
        }
    }

    @Transactional
    public Optional<LicenseKey> assignLicenseKey(Product product) {
        if (!product.getRequiresLicenseKey()) {
            return Optional.empty();
        }

        Optional<LicenseKey> availableKey = licenseKeyRepository.findFirstByProductAndIsUsedFalse(product);
        if (availableKey.isPresent()) {
            LicenseKey key = availableKey.get();
            key.setIsUsed(true);
            key.setUsedAt(java.time.LocalDateTime.now());
            return Optional.of(licenseKeyRepository.save(key));
        }
        return Optional.empty();
    }

    public Long getAvailableLicenseKeysCount(Product product) {
        return licenseKeyRepository.countByProductAndIsUsedFalse(product);
    }

    @Transactional
    public LicenseKey addLicenseKey(Product product, String keyValue) {
        LicenseKey licenseKey = new LicenseKey();
        licenseKey.setProduct(product);
        licenseKey.setKey(keyValue);
        licenseKey.setIsUsed(false);
        LicenseKey saved = licenseKeyRepository.save(licenseKey);
        
        // Обновляем остаток товара, если требуется лицензионный ключ
        if (product.getRequiresLicenseKey() != null && product.getRequiresLicenseKey()) {
            Long availableKeys = licenseKeyRepository.countByProductAndIsUsedFalse(product);
            product.setStock(availableKeys.intValue());
            productRepository.save(product);
        }
        
        return saved;
    }

    public List<LicenseKey> getAllLicenseKeysForProduct(Product product) {
        return licenseKeyRepository.findByProduct(product);
    }

    @Transactional
    public void deleteLicenseKey(Long keyId) {
        licenseKeyRepository.deleteById(keyId);
    }

    public Optional<LicenseKey> getLicenseKeyById(Long keyId) {
        return licenseKeyRepository.findById(keyId);
    }
}
