package com.example.anas.product.controller;

import com.example.anas.auth.entity.User;
import com.example.anas.auth.repository.UserRepository;
import com.example.anas.product.entity.Product;
import com.example.anas.product.entity.ProductCategory;
import com.example.anas.product.entity.ProductSortOption;
import com.example.anas.product.service.ProductService;
import com.example.anas.product.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final ReviewService reviewService;
    private final UserRepository userRepository;

    @GetMapping("/")
    public String index(Model model) {
        // Сначала пытаемся получить товары с остатком > 0
        List<Product> products = productService.getAvailableProducts();
        // Если таких нет, показываем все товары (чтобы секция "Популярные товары" не была пустой)
        if (products == null || products.isEmpty()) {
            products = productService.getAllProducts();
        }
        model.addAttribute("products", products);
        model.addAttribute("categories", ProductCategory.values());
        model.addAttribute("title", "Главная");
        return "index";
    }

    @GetMapping("/products")
    public String products(@RequestParam(required = false) String category,
                          @RequestParam(required = false) String search,
                          @RequestParam(required = false) String minPrice,
                          @RequestParam(required = false) String maxPrice,
                          @RequestParam(required = false) Boolean inStock,
                          @RequestParam(required = false) String sortBy,
                          Model model) {
        ProductCategory productCategory = null;
        if (category != null && !category.isEmpty()) {
            try {
                productCategory = ProductCategory.valueOf(category);
            } catch (IllegalArgumentException e) {
                // Игнорируем неверную категорию
            }
        }

        java.math.BigDecimal minPriceDecimal = null;
        java.math.BigDecimal maxPriceDecimal = null;
        try {
            if (minPrice != null && !minPrice.isEmpty()) {
                minPriceDecimal = new java.math.BigDecimal(minPrice);
            }
            if (maxPrice != null && !maxPrice.isEmpty()) {
                maxPriceDecimal = new java.math.BigDecimal(maxPrice);
            }
        } catch (NumberFormatException e) {
            // Игнорируем неверные значения цен
        }

        ProductSortOption sortOption = null;
        if (sortBy != null && !sortBy.isEmpty()) {
            try {
                sortOption = ProductSortOption.valueOf(sortBy);
            } catch (IllegalArgumentException e) {
                // Игнорируем неверный вариант сортировки
            }
        }

        List<Product> products = productService.getFilteredProducts(
                productCategory, search, minPriceDecimal, maxPriceDecimal, inStock, sortOption);

        model.addAttribute("products", products);
        model.addAttribute("categories", ProductCategory.values());
        model.addAttribute("selectedCategory", category);
        model.addAttribute("searchQuery", search);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("inStock", inStock);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortOptions", ProductSortOption.values());
        model.addAttribute("title", "Каталог товаров");
        return "products";
    }

    @GetMapping("/product/{id}")
    public String productDetails(@PathVariable Long id, 
                                @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails,
                                Model model) {
        return productService.getProductById(id)
                .map(product -> {
                    model.addAttribute("product", product);
                    if (product.getRequiresLicenseKey()) {
                        Long availableKeys = productService.getAvailableLicenseKeysCount(product);
                        model.addAttribute("availableKeys", availableKeys);
                    }
                    
                    // Получаем одобренные отзывы
                    List<com.example.anas.product.entity.Review> reviews = reviewService.getProductReviews(id, true);
                    model.addAttribute("reviews", reviews);
                    
                    // Проверяем, может ли пользователь оставить отзыв
                    boolean canReview = false;
                    boolean hasReviewed = false;
                    if (userDetails != null) {
                        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
                        if (user != null) {
                            canReview = reviewService.hasUserPurchasedProduct(user, product);
                            hasReviewed = reviewService.getUserReview(user, product).isPresent();
                        }
                    }
                    model.addAttribute("canReview", canReview);
                    model.addAttribute("hasReviewed", hasReviewed);
                    
                    model.addAttribute("title", product.getName());
                    return "product-details";
                })
                .orElseGet(() -> {
                    model.addAttribute("errorMessage", "Товар не найден");
                    return "redirect:/products";
                });
    }
}
