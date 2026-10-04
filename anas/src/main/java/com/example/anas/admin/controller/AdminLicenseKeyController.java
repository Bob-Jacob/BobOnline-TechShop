package com.example.anas.admin.controller;

import com.example.anas.product.entity.LicenseKey;
import com.example.anas.product.entity.Product;
import com.example.anas.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
@RequestMapping("/admin/license-keys")
@RequiredArgsConstructor
public class AdminLicenseKeyController {
    private final ProductService productService;

    @GetMapping
    public String licenseKeys(@RequestParam(required = false) Long productId, Model model) {
        if (productId != null) {
            Product product = productService.getProductById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Товар не найден"));
            model.addAttribute("product", product);
            List<LicenseKey> keys = productService.getAllLicenseKeysForProduct(product);
            model.addAttribute("licenseKeys", keys);
        }
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("title", "Управление лицензионными ключами");
        return "admin/license-keys";
    }

    @PostMapping("/add")
    public String addLicenseKey(@RequestParam Long productId,
                               @RequestParam String key,
                               RedirectAttributes redirectAttributes) {
        try {
            Product product = productService.getProductById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Товар не найден"));
            productService.addLicenseKey(product, key);
            redirectAttributes.addFlashAttribute("successMessage", "Ключ добавлен");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при добавлении ключа");
        }
        return "redirect:/admin/license-keys?productId=" + productId;
    }

    @PostMapping("/upload")
    public String uploadLicenseKeys(@RequestParam Long productId,
                                   @RequestParam MultipartFile file,
                                   RedirectAttributes redirectAttributes) {
        try {
            Product product = productService.getProductById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Товар не найден"));
            
            int count = 0;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty()) {
                        productService.addLicenseKey(product, line);
                        count++;
                    }
                }
            }
            
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Добавлено ключей: " + count);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Ошибка при загрузке файла: " + e.getMessage());
        }
        return "redirect:/admin/license-keys?productId=" + productId;
    }

    @PostMapping("/{keyId}/delete")
    public String deleteLicenseKey(@PathVariable Long keyId,
                                   @RequestParam Long productId,
                                   RedirectAttributes redirectAttributes) {
        try {
            productService.deleteLicenseKey(keyId);
            redirectAttributes.addFlashAttribute("successMessage", "Ключ удален");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при удалении ключа: " + e.getMessage());
        }
        return "redirect:/admin/license-keys?productId=" + productId;
    }
}
