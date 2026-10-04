package com.example.anas.admin.controller;

import com.example.anas.admin.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDashboardController {
    private final AdminService adminService;

    @GetMapping
    public String dashboard(Model model) {
        Map<String, Object> stats = adminService.getDashboardStats();
        model.addAllAttributes(stats);
        model.addAttribute("title", "Админ-панель");
        return "admin/dashboard";
    }
}
