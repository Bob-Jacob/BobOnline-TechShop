package com.example.anas.order.service;

import com.example.anas.auth.entity.User;
import com.example.anas.order.entity.Cart;
import com.example.anas.order.entity.CartItem;
import com.example.anas.order.repository.CartItemRepository;
import com.example.anas.order.repository.CartRepository;
import com.example.anas.product.entity.Product;
import com.example.anas.product.repository.LicenseKeyRepository;
import com.example.anas.product.repository.ProductRepository;
import com.example.anas.promotion.service.PromoCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final LicenseKeyRepository licenseKeyRepository;
    private final PromoCodeService promoCodeService;

    @Transactional
    public Cart getOrCreateCart(User user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setUser(user);
                    return cartRepository.save(cart);
                });
    }

    @Transactional
    public void addToCart(User user, Long productId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Количество должно быть больше нуля");
        }
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Товар не найден"));

        // Для товаров с лицензионными ключами проверяем доступные ключи
        int availableStock = product.getStock();
        if (product.getRequiresLicenseKey() != null && product.getRequiresLicenseKey()) {
            // Для товаров с ключами остаток должен быть равен количеству доступных ключей
            Long availableKeys = licenseKeyRepository.countByProductAndIsUsedFalse(product);
            availableStock = availableKeys.intValue();
            // Синхронизируем остаток с количеством ключей
            if (product.getStock() != availableStock) {
                product.setStock(availableStock);
                productRepository.save(product);
            }
        }

        if (availableStock < quantity) {
            throw new IllegalArgumentException("Недостаточно товара на складе. Доступно: " + availableStock);
        }

        Cart cart = getOrCreateCart(user);
        Optional<CartItem> existingItem = cartItemRepository.findByCartAndProduct(cart, product);

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + quantity;
            if (availableStock < newQuantity) {
                throw new IllegalArgumentException("Недостаточно товара на складе. Доступно: " + availableStock);
            }
            item.setQuantity(newQuantity);
            cartItemRepository.save(item);
        } else {
            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
        }
    }

    @Transactional
    public void updateCartItemQuantity(User user, Long cartItemId, Integer quantity) {
        Cart cart = getOrCreateCart(user);
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Позиция корзины не найдена"));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new IllegalArgumentException("Недоступная операция");
        }

        if (quantity <= 0) {
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }
    }

    @Transactional
    public void removeFromCart(User user, Long cartItemId) {
        Cart cart = getOrCreateCart(user);
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Позиция корзины не найдена"));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new IllegalArgumentException("Недоступная операция");
        }

        cartItemRepository.delete(item);
    }

    @Transactional
    public void clearCart(User user) {
        Cart cart = getOrCreateCart(user);
        cartItemRepository.deleteByCart(cart);
    }

    public BigDecimal calculateTotal(Cart cart) {
        return cart.getItems().stream()
                .map(item -> item.getProduct().getFinalPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calculateTotalWithDiscount(Cart cart, String promoCode) {
        BigDecimal subtotal = calculateTotal(cart);
        if (promoCode != null && !promoCode.isEmpty()) {
            if (promoCodeService.validatePromoCode(promoCode, subtotal)) {
                BigDecimal discount = promoCodeService.calculateDiscount(promoCode, subtotal);
                BigDecimal total = subtotal.subtract(discount);
                return total.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : total;
            }
        }
        return subtotal;
    }

    public boolean validatePromoCode(String code, Cart cart) {
        BigDecimal subtotal = calculateTotal(cart);
        return promoCodeService.validatePromoCode(code, subtotal);
    }
}
