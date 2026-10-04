package com.example.anas.payment.controller;

import com.example.anas.auth.entity.User;
import com.example.anas.auth.repository.UserRepository;
import com.example.anas.order.entity.Order;
import com.example.anas.order.service.OrderService;
import com.example.anas.payment.entity.Payment;
import com.example.anas.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    private final OrderService orderService;
    private final UserRepository userRepository;

    @GetMapping("/order/{orderId}/payment")
    public String paymentPage(@AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails,
                             @PathVariable Long orderId,
                             Model model) {
        User user = getCurrentUser(userDetails);
        Order order = orderService.getOrderById(orderId);

        if (!order.getUser().getId().equals(user.getId())) {
            return "redirect:/orders";
        }

        Payment payment = paymentService.createPayment(order);
        model.addAttribute("payment", payment);
        model.addAttribute("order", order);
        model.addAttribute("title", "Оплата заказа");
        return "payment";
    }

    @PostMapping("/payment/{paymentId}/process")
    public String processPayment(@AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails,
                                @PathVariable Long paymentId,
                                RedirectAttributes redirectAttributes) {
        try {
            User user = getCurrentUser(userDetails);
            Payment payment = paymentService.processPayment(paymentId);
            
            if (!payment.getOrder().getUser().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Недоступная операция");
                return "redirect:/orders";
            }

            redirectAttributes.addFlashAttribute("successMessage", "Оплата успешно выполнена! Товары выданы.");
            return "redirect:/order/" + payment.getOrder().getId();
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/orders";
        }
    }

    private User getCurrentUser(org.springframework.security.core.userdetails.UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
    }
}
