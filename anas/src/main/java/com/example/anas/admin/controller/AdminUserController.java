package com.example.anas.admin.controller;

import com.example.anas.auth.entity.Role;
import com.example.anas.auth.entity.User;
import com.example.anas.auth.repository.RoleRepository;
import com.example.anas.auth.repository.UserRepository;
import com.example.anas.order.repository.OrderRepository;
import com.example.anas.product.repository.LicenseKeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OrderRepository orderRepository;
    private final LicenseKeyRepository licenseKeyRepository;

    @GetMapping
    public String users(Model model) {
        var users = userRepository.findAll();
        
        // Создаем карту с дополнительной информацией о каждом пользователе
        Map<Long, Map<String, Object>> userStats = new HashMap<>();
        for (User user : users) {
            Map<String, Object> stats = new HashMap<>();
            long ordersCount = orderRepository.findByUserOrderByCreatedAtDesc(user).size();
            long licenseKeysCount = licenseKeyRepository.findAll().stream()
                    .filter(key -> key.getOrder() != null && 
                            key.getOrder().getUser().getId().equals(user.getId()))
                    .count();
            stats.put("ordersCount", ordersCount);
            stats.put("licenseKeysCount", licenseKeysCount);
            userStats.put(user.getId(), stats);
        }
        
        model.addAttribute("users", users);
        model.addAttribute("userStats", userStats);
        model.addAttribute("title", "Управление пользователями");
        return "admin/users";
    }

    @PostMapping("/{id}/toggle-enabled")
    public String toggleUserEnabled(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        user.setEnabled(!user.getEnabled());
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("successMessage", "Статус пользователя изменен");
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/add-role")
    public String addRole(@PathVariable Long id,
                         @RequestParam String roleName,
                         RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        Role role = roleRepository.findByName(Role.RoleName.valueOf(roleName))
                .orElseThrow(() -> new IllegalArgumentException("Роль не найдена"));
        
        if (!user.hasRole(role.getName())) {
            user.addRole(role);
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("successMessage", "Роль добавлена");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Роль уже назначена");
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/remove-role")
    public String removeRole(@PathVariable Long id,
                            @RequestParam String roleName,
                            RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        Role role = roleRepository.findByName(Role.RoleName.valueOf(roleName))
                .orElseThrow(() -> new IllegalArgumentException("Роль не найдена"));
        
        user.getRoles().remove(role);
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("successMessage", "Роль удалена");
        return "redirect:/admin/users";
    }
}
