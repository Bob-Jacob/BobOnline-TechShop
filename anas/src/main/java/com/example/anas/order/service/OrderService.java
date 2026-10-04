package com.example.anas.order.service;

import com.example.anas.auth.entity.User;
import com.example.anas.common.service.EmailService;
import com.example.anas.order.entity.Cart;
import com.example.anas.order.entity.CartItem;
import com.example.anas.order.entity.Order;
import com.example.anas.order.entity.OrderItem;
import com.example.anas.order.entity.OrderStatus;
import com.example.anas.order.repository.OrderRepository;
import com.example.anas.product.entity.LicenseKey;
import com.example.anas.product.entity.Product;
import com.example.anas.product.repository.LicenseKeyRepository;
import com.example.anas.product.repository.ProductRepository;
import com.example.anas.product.service.ProductService;
import com.example.anas.promotion.service.PromoCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final ProductService productService;
    private final LicenseKeyRepository licenseKeyRepository;
    private final ProductRepository productRepository;
    private final EmailService emailService;
    private final PromoCodeService promoCodeService;

    @Transactional
    public Order createOrderFromCart(User user, String promoCode) {
        Cart cart = cartService.getOrCreateCart(user);
        
        if (cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Корзина пуста");
        }

        // Проверка наличия товаров
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            if (product.getStock() < cartItem.getQuantity()) {
                throw new IllegalArgumentException("Недостаточно товара: " + product.getName());
            }
        }

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            BigDecimal itemPrice = product.getFinalPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            subtotal = subtotal.add(itemPrice);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(product.getFinalPrice());
            orderItems.add(orderItem);
        }

        order.setItems(orderItems);
        order.setSubtotal(subtotal);

        // Применение промокода
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (promoCode != null && !promoCode.isEmpty()) {
            if (promoCodeService.validatePromoCode(promoCode, subtotal)) {
                discountAmount = promoCodeService.calculateDiscount(promoCode, subtotal);
                order.setPromoCode(promoCode);
                order.setDiscountAmount(discountAmount);
                promoCodeService.incrementUsage(promoCode);
            } else {
                throw new IllegalArgumentException("Недействительный промокод");
            }
        }

        BigDecimal totalPrice = subtotal.subtract(discountAmount);
        if (totalPrice.compareTo(BigDecimal.ZERO) < 0) {
            totalPrice = BigDecimal.ZERO;
        }
        order.setTotalPrice(totalPrice);
        order = orderRepository.save(order);

        // Очистка корзины
        cartService.clearCart(user);

        return order;
    }

    public List<Order> getUserOrders(User user) {
        return orderRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Заказ не найден"));
    }

    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = getOrderById(orderId);
        order.setStatus(status);
        orderRepository.save(order);
    }

    @Transactional
    public void completeOrder(Long orderId) {
        Order order = getOrderById(orderId);
        
        if (order.getStatus() != OrderStatus.PAID) {
            throw new IllegalArgumentException("Заказ должен быть оплачен");
        }

        List<LicenseKey> assignedKeys = new ArrayList<>();
        List<OrderItem> digitalItems = new ArrayList<>();

        // Выдача лицензионных ключей и уменьшение остатков
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            
            // Уменьшаем остаток товара
            int newStock = product.getStock() - item.getQuantity();
            if (newStock < 0) {
                throw new IllegalArgumentException("Недостаточно товара на складе: " + product.getName());
            }
            product.setStock(newStock);
            productRepository.save(product);
            
            if (product.getRequiresLicenseKey() != null && product.getRequiresLicenseKey()) {
                for (int i = 0; i < item.getQuantity(); i++) {
                    productService.assignLicenseKey(product)
                            .ifPresent(licenseKey -> {
                                licenseKey.setOrder(order);
                                licenseKeyRepository.save(licenseKey);
                                assignedKeys.add(licenseKey);
                            });
                }
                
                // Обновляем остаток на основе доступных ключей
                Long availableKeys = productService.getAvailableLicenseKeysCount(product);
                product.setStock(availableKeys.intValue());
                productRepository.save(product);
            }
            
            // Собираем цифровые товары для отправки ссылок
            if (product.getFileUrl() != null && !product.getFileUrl().isEmpty()) {
                digitalItems.add(item);
            }
        }

        order.setStatus(OrderStatus.COMPLETED);
        orderRepository.save(order);

        // Отправка email с лицензионными ключами
        if (!assignedKeys.isEmpty()) {
            emailService.sendLicenseKeys(order.getUser(), order, assignedKeys);
        }

        // Отправка email со ссылками на скачивание
        if (!digitalItems.isEmpty()) {
            emailService.sendDownloadLinks(order.getUser(), order, digitalItems);
        }
    }

    public List<LicenseKey> getOrderLicenseKeys(Order order) {
        return licenseKeyRepository.findByOrderId(order.getId());
    }
}
