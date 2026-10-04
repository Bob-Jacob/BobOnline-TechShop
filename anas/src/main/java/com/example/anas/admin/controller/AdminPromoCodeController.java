package com.example.anas.admin.controller;

import com.example.anas.promotion.entity.PromoCode;
import com.example.anas.promotion.service.PromoCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin/promo-codes")
@RequiredArgsConstructor
public class AdminPromoCodeController {
    private final PromoCodeService promoCodeService;

    @GetMapping
    public String promoCodes(Model model) {
        model.addAttribute("promoCodes", promoCodeService.getAllPromoCodes());
        model.addAttribute("title", "Управление промокодами");
        return "admin/promo-codes";
    }

    @GetMapping("/new")
    public String newPromoCodeForm(Model model) {
        model.addAttribute("promoCode", new PromoCode());
        model.addAttribute("title", "Создать промокод");
        return "admin/promo-code-form";
    }

    @PostMapping("/save")
    public String savePromoCode(@ModelAttribute PromoCode promoCode,
                               RedirectAttributes redirectAttributes) {
        try {
            promoCodeService.savePromoCode(promoCode);
            redirectAttributes.addFlashAttribute("successMessage", "Промокод сохранен");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка: " + e.getMessage());
        }
        return "redirect:/admin/promo-codes";
    }

    @PostMapping("/{id}/delete")
    public String deletePromoCode(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            promoCodeService.deletePromoCode(id);
            redirectAttributes.addFlashAttribute("successMessage", "Промокод удален");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка: " + e.getMessage());
        }
        return "redirect:/admin/promo-codes";
    }

    @PostMapping("/{id}/toggle")
    public String togglePromoCode(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            promoCodeService.togglePromoCode(id);
            redirectAttributes.addFlashAttribute("successMessage", "Статус промокода изменен");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка: " + e.getMessage());
        }
        return "redirect:/admin/promo-codes";
    }
}
