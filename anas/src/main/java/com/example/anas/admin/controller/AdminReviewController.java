package com.example.anas.admin.controller;

import com.example.anas.product.service.ProductService;
import com.example.anas.product.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/reviews")
@RequiredArgsConstructor
public class AdminReviewController {
    private final ReviewService reviewService;
    private final ProductService productService;

    @GetMapping
    public String reviews(@RequestParam(required = false) Long productId,
                         @RequestParam(required = false) Boolean approved,
                         Model model) {
        List<com.example.anas.product.entity.Review> reviews;
        
        if (productId != null) {
            reviews = reviewService.getProductReviews(productId, false);
        } else if (approved != null) {
            reviews = reviewService.getAllReviews(approved);
        } else {
            reviews = reviewService.getAllReviews(null);
        }
        
        model.addAttribute("reviews", reviews);
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("selectedProductId", productId);
        model.addAttribute("selectedApproved", approved);
        model.addAttribute("title", "Управление отзывами");
        return "admin/reviews";
    }

    @PostMapping("/{id}/approve")
    public String approveReview(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            reviewService.approveReview(id);
            redirectAttributes.addFlashAttribute("successMessage", "Отзыв одобрен");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка: " + e.getMessage());
        }
        return "redirect:/admin/reviews";
    }

    @PostMapping("/{id}/delete")
    public String deleteReview(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            reviewService.deleteReview(id);
            redirectAttributes.addFlashAttribute("successMessage", "Отзыв удален");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка: " + e.getMessage());
        }
        return "redirect:/admin/reviews";
    }
}
