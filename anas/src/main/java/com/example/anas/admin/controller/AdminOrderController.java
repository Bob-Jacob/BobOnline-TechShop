package com.example.anas.admin.controller;

import com.example.anas.order.entity.Order;
import com.example.anas.order.entity.OrderStatus;
import com.example.anas.order.repository.OrderRepository;
import com.example.anas.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {
    private final OrderService orderService;
    private final OrderRepository orderRepository;

    @GetMapping
    public String orders(Model model) {
        List<Order> allOrders = orderRepository.findAllByOrderByCreatedAtDesc();
        model.addAttribute("orders", allOrders);
        model.addAttribute("title", "Управление заказами");
        return "admin/orders";
    }

    @GetMapping("/{id}")
    public String orderDetails(@PathVariable Long id, Model model) {
        Order order = orderService.getOrderById(id);
        model.addAttribute("order", order);
        model.addAttribute("title", "Заказ #" + order.getId());
        return "admin/order-details";
    }

    @PostMapping("/{id}/status")
    public String updateOrderStatus(@PathVariable Long id,
                                   @RequestParam OrderStatus status,
                                   RedirectAttributes redirectAttributes) {
        try {
            orderService.updateOrderStatus(id, status);
            redirectAttributes.addFlashAttribute("successMessage", "Статус заказа обновлен");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при обновлении статуса");
        }
        return "redirect:/admin/orders/" + id;
    }
}
