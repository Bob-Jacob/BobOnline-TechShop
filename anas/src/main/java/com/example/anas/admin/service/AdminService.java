package com.example.anas.admin.service;

import com.example.anas.auth.entity.User;
import com.example.anas.auth.repository.UserRepository;
import com.example.anas.order.entity.Order;
import com.example.anas.order.entity.OrderStatus;
import com.example.anas.order.repository.OrderRepository;
import com.example.anas.payment.entity.Payment;
import com.example.anas.payment.entity.PaymentStatus;
import com.example.anas.payment.repository.PaymentRepository;
import com.example.anas.product.entity.LicenseKey;
import com.example.anas.product.entity.Product;
import com.example.anas.product.entity.ProductCategory;
import com.example.anas.product.repository.LicenseKeyRepository;
import com.example.anas.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final LicenseKeyRepository licenseKeyRepository;

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Базовые счетчики (быстрые запросы)
        long totalProducts = productRepository.count();
        long totalOrders = orderRepository.count();
        long totalUsers = userRepository.count();
        
        // Выручка - используем только успешные платежи
        BigDecimal totalRevenue = paymentRepository.findAll().stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Ожидающие заказы
        long pendingOrders = orderRepository.findAll().stream()
                .filter(o -> o.getStatus() == OrderStatus.PENDING)
                .count();
        
        // Статистика по категориям товаров (оптимизировано)
        Map<ProductCategory, Long> productsByCategory = new HashMap<>();
        for (ProductCategory category : ProductCategory.values()) {
            long count = productRepository.findByCategory(category).size();
            if (count > 0) {
                productsByCategory.put(category, count);
            }
        }
        
        // Статистика заказов по месяцам (последние 6 месяцев) - оптимизировано
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        List<Order> allOrders = orderRepository.findAll();
        Map<String, Long> ordersByMonth = new LinkedHashMap<>();
        String[] monthNames = {"Янв", "Фев", "Мар", "Апр", "Май", "Июн", "Июл", "Авг", "Сен", "Окт", "Ноя", "Дек"};
        
        for (int i = 5; i >= 0; i--) {
            LocalDateTime monthStart = LocalDateTime.now().minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0);
            LocalDateTime monthEnd = monthStart.plusMonths(1);
            long count = allOrders.stream()
                    .filter(o -> o.getCreatedAt() != null && 
                            o.getCreatedAt().isAfter(monthStart) && 
                            o.getCreatedAt().isBefore(monthEnd))
                    .count();
            int monthIndex = monthStart.getMonthValue() - 1;
            ordersByMonth.put(monthNames[monthIndex], count);
        }
        
        // Статистика по статусам заказов
        Map<OrderStatus, Long> ordersByStatus = new EnumMap<>(OrderStatus.class);
        for (OrderStatus status : OrderStatus.values()) {
            long count = allOrders.stream()
                    .filter(o -> o.getStatus() == status)
                    .count();
            if (count > 0) {
                ordersByStatus.put(status, count);
            }
        }
        
        // Статистика по лицензионным ключам (оптимизировано)
        long totalLicenseKeys = licenseKeyRepository.count();
        List<LicenseKey> allKeys = licenseKeyRepository.findAll();
        long usedLicenseKeys = allKeys.stream()
                .filter(LicenseKey::getIsUsed)
                .count();
        long availableLicenseKeys = totalLicenseKeys - usedLicenseKeys;
        
        // Купленные ключи (ключи с заказами)
        long purchasedKeys = allKeys.stream()
                .filter(key -> key.getOrder() != null)
                .count();
        
        // Топ пользователей по количеству заказов (ограничено 5)
        Map<User, Long> userOrdersCount = allOrders.stream()
                .collect(Collectors.groupingBy(Order::getUser, Collectors.counting()));
        
        List<Map<String, Object>> topUsers = userOrdersCount.entrySet().stream()
                .sorted(Map.Entry.<User, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> {
                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put("email", entry.getKey().getEmail());
                    userInfo.put("ordersCount", entry.getValue());
                    userInfo.put("id", entry.getKey().getId());
                    return userInfo;
                })
                .collect(Collectors.toList());
        
        // Топ товаров по продажам (ограничено 5)
        Map<Product, Long> productSales = allOrders.stream()
                .flatMap(order -> order.getItems().stream())
                .collect(Collectors.groupingBy(
                    item -> item.getProduct(),
                    Collectors.summingLong(item -> item.getQuantity().longValue())
                ));
        
        List<Map<String, Object>> topProducts = productSales.entrySet().stream()
                .sorted(Map.Entry.<Product, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> {
                    Map<String, Object> productInfo = new HashMap<>();
                    productInfo.put("name", entry.getKey().getName());
                    productInfo.put("salesCount", entry.getValue());
                    productInfo.put("id", entry.getKey().getId());
                    return productInfo;
                })
                .collect(Collectors.toList());
        
        // Последние купленные ключи (для информационного блока)
        List<Map<String, Object>> recentPurchasedKeys = allKeys.stream()
                .filter(key -> key.getOrder() != null && key.getIsUsed())
                .sorted((k1, k2) -> {
                    if (k1.getUsedAt() != null && k2.getUsedAt() != null) {
                        return k2.getUsedAt().compareTo(k1.getUsedAt());
                    }
                    return 0;
                })
                .limit(10)
                .map(key -> {
                    Map<String, Object> keyInfo = new HashMap<>();
                    keyInfo.put("key", key.getKey());
                    keyInfo.put("productName", key.getProduct().getName());
                    keyInfo.put("userEmail", key.getOrder().getUser().getEmail());
                    keyInfo.put("orderId", key.getOrder().getId());
                    keyInfo.put("usedAt", key.getUsedAt());
                    return keyInfo;
                })
                .collect(Collectors.toList());
        
        stats.put("totalProducts", totalProducts);
        stats.put("totalOrders", totalOrders);
        stats.put("totalUsers", totalUsers);
        stats.put("totalRevenue", totalRevenue);
        stats.put("pendingOrders", pendingOrders);
        stats.put("productsByCategory", productsByCategory);
        stats.put("ordersByMonth", ordersByMonth);
        stats.put("ordersByStatus", ordersByStatus);
        stats.put("totalLicenseKeys", totalLicenseKeys);
        stats.put("usedLicenseKeys", usedLicenseKeys);
        stats.put("availableLicenseKeys", availableLicenseKeys);
        stats.put("purchasedKeys", purchasedKeys);
        stats.put("topUsers", topUsers);
        stats.put("topProducts", topProducts);
        stats.put("recentPurchasedKeys", recentPurchasedKeys);
        
        return stats;
    }
}
