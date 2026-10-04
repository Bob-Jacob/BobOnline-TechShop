package com.example.anas.order.controller;

import com.example.anas.auth.entity.User;
import com.example.anas.auth.repository.UserRepository;
import com.example.anas.order.entity.Cart;
import com.example.anas.order.entity.Order;
import com.example.anas.order.service.CartService;
import com.example.anas.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class OrderController {
    private final CartService cartService;
    private final OrderService orderService;
    private final UserRepository userRepository;

    @GetMapping("/cart")
    public String cart(@AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails,
                      Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        User user = getCurrentUser(userDetails);
        Cart cart = cartService.getOrCreateCart(user);
        BigDecimal total = cartService.calculateTotal(cart);
        
        model.addAttribute("cart", cart);
        model.addAttribute("total", total);
        model.addAttribute("title", "Корзина");
        return "cart";
    }

    @PostMapping(value = "/cart/add", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> addToCartAjax(@AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails,
                           @RequestParam Long productId,
                           @RequestParam(defaultValue = "1") Integer quantity) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = getCurrentUser(userDetails);
            cartService.addToCart(user, productId, quantity);
            response.put("success", true);
            response.put("message", "Товар добавлен в корзину");
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @PostMapping(value = "/cart/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> updateCartItemAjax(@AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails,
                                @RequestParam Long cartItemId,
                                @RequestParam Integer quantity) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = getCurrentUser(userDetails);
            cartService.updateCartItemQuantity(user, cartItemId, quantity);
            response.put("success", true);
            response.put("message", "Корзина обновлена");
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @PostMapping(value = "/cart/remove", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> removeFromCartAjax(@AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails,
                                @RequestParam Long cartItemId) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = getCurrentUser(userDetails);
            cartService.removeFromCart(user, cartItemId);
            response.put("success", true);
            response.put("message", "Товар удален из корзины");
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @GetMapping("/order/create")
    public String createOrderForm(@AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails,
                                 Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        User user = getCurrentUser(userDetails);
        Cart cart = cartService.getOrCreateCart(user);
        
        if (cart.getItems().isEmpty()) {
            return "redirect:/cart";
        }

        BigDecimal total = cartService.calculateTotal(cart);
        model.addAttribute("cart", cart);
        model.addAttribute("total", total);
        model.addAttribute("title", "Оформление заказа");
        return "order-form";
    }

    @PostMapping("/order/create")
    public String createOrder(@AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails,
                            @RequestParam(required = false) String promoCode,
                            RedirectAttributes redirectAttributes) {
        try {
            User user = getCurrentUser(userDetails);
            Order order = orderService.createOrderFromCart(user, promoCode);
            redirectAttributes.addFlashAttribute("successMessage", "Заказ создан успешно");
            return "redirect:/order/" + order.getId() + "/payment";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/cart";
        }
    }

    @PostMapping(value = "/cart/validate-promo", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> validatePromoCode(@AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails,
                                                   @RequestParam String promoCode) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = getCurrentUser(userDetails);
            Cart cart = cartService.getOrCreateCart(user);
            BigDecimal subtotal = cartService.calculateTotal(cart);
            
            boolean isValid = cartService.validatePromoCode(promoCode, cart);
            if (isValid) {
                BigDecimal discount = cartService.calculateTotalWithDiscount(cart, promoCode);
                BigDecimal discountAmount = subtotal.subtract(discount);
                response.put("success", true);
                response.put("valid", true);
                response.put("discount", discountAmount);
                response.put("total", discount);
            } else {
                response.put("success", true);
                response.put("valid", false);
                response.put("message", "Недействительный промокод");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @GetMapping("/orders")
    public String orders(@AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails,
                        Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        User user = getCurrentUser(userDetails);
        model.addAttribute("orders", orderService.getUserOrders(user));
        model.addAttribute("title", "Мои заказы");
        return "orders";
    }

    @GetMapping("/order/{id}")
    public String orderDetails(@AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails,
                              @PathVariable Long id,
                              Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        User user = getCurrentUser(userDetails);
        Order order = orderService.getOrderById(id);
        
        if (!order.getUser().getId().equals(user.getId())) {
            return "redirect:/orders";
        }

        model.addAttribute("order", order);
        model.addAttribute("title", "Заказ #" + order.getId());
        return "order-details";
    }

    private User getCurrentUser(org.springframework.security.core.userdetails.UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
    }
}
