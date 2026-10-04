package com.example.anas.admin.controller;

import com.example.anas.common.service.FileStorageService;
import com.example.anas.product.entity.Product;
import com.example.anas.product.entity.ProductCategory;
import com.example.anas.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class AdminProductController {
    private final ProductService productService;
    private final FileStorageService fileStorageService;

    @GetMapping
    public String products(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("categories", ProductCategory.values());
        model.addAttribute("title", "Управление товарами");
        return "admin/products";
    }

    @GetMapping("/new")
    public String newProductForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", ProductCategory.values());
        model.addAttribute("title", "Новый товар");
        return "admin/product-form";
    }

    @GetMapping("/{id}/edit")
    public String editProductForm(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new IllegalArgumentException("Товар не найден"));
        model.addAttribute("product", product);
        model.addAttribute("categories", ProductCategory.values());
        model.addAttribute("title", "Редактирование товара");
        return "admin/product-form";
    }

    @PostMapping("/save")
    public String saveProduct(@ModelAttribute Product product,
                             @RequestParam(required = false) MultipartFile file,
                             RedirectAttributes redirectAttributes) {
        try {
            // Валидация
            if (product.getName() == null || product.getName().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Название товара обязательно");
                return product.getId() != null ? "redirect:/admin/products/" + product.getId() + "/edit" : "redirect:/admin/products/new";
            }
            if (product.getPrice() == null || product.getPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                redirectAttributes.addFlashAttribute("errorMessage", "Цена должна быть больше нуля");
                return product.getId() != null ? "redirect:/admin/products/" + product.getId() + "/edit" : "redirect:/admin/products/new";
            }
            if (product.getStock() == null || product.getStock() < 0) {
                product.setStock(0);
            }
            
            // Автоматический расчет цены со скидкой
            if (product.getDiscountPercent() != null && product.getDiscountPercent() > 0 
                    && product.getDiscountPercent() <= 100 && product.getPrice() != null) {
                java.math.BigDecimal discount = product.getPrice()
                        .multiply(java.math.BigDecimal.valueOf(product.getDiscountPercent()))
                        .divide(java.math.BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
                product.setDiscountPrice(product.getPrice().subtract(discount));
            } else if (product.getDiscountPrice() != null && product.getDiscountPrice().compareTo(java.math.BigDecimal.ZERO) > 0) {
                // Если указана фиксированная цена со скидкой, но нет процента, рассчитываем процент
                if (product.getPrice() != null && product.getPrice().compareTo(java.math.BigDecimal.ZERO) > 0) {
                    java.math.BigDecimal discountAmount = product.getPrice().subtract(product.getDiscountPrice());
                    java.math.BigDecimal percent = discountAmount
                            .divide(product.getPrice(), 4, java.math.RoundingMode.HALF_UP)
                            .multiply(java.math.BigDecimal.valueOf(100));
                    product.setDiscountPercent(percent.intValue());
                }
            } else {
                // Если нет скидки, очищаем поля
                product.setDiscountPercent(null);
                product.setDiscountPrice(null);
                product.setIsOnSale(false);
            }
            
            // Загрузка файла для цифровых товаров
            if (file != null && !file.isEmpty()) {
                // Удаляем старый файл, если он был
                if (product.getId() != null) {
                    Product existingProduct = productService.getProductById(product.getId())
                            .orElse(null);
                    if (existingProduct != null && existingProduct.getFileUrl() != null) {
                        fileStorageService.deleteFile(existingProduct.getFileUrl());
                    }
                }
                
                // Сохраняем новый файл
                String fileUrl = fileStorageService.storeFile(file, "products");
                product.setFileUrl(fileUrl);
            }
            
            productService.saveProduct(product);
            redirectAttributes.addFlashAttribute("successMessage", "Товар сохранен");
            return "redirect:/admin/products";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при сохранении товара: " + e.getMessage());
            return product.getId() != null ? "redirect:/admin/products/" + product.getId() + "/edit" : "redirect:/admin/products/new";
        }
    }

    @GetMapping("/{id}/delete")
    public String deleteProductConfirmation(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new IllegalArgumentException("Товар не найден"));
        
        ProductService.ProductDeletionInfo deletionInfo = productService.getDeletionInfo(id);
        
        model.addAttribute("product", product);
        model.addAttribute("deletionInfo", deletionInfo);
        model.addAttribute("title", "Подтверждение удаления товара");
        return "admin/product-delete-confirm";
    }

    @PostMapping("/{id}/delete")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            ProductService.ProductDeletionInfo deletionInfo = productService.getDeletionInfo(id);
            
            if (deletionInfo.hasRelatedData()) {
                // Показываем информацию о том, что будет удалено
                StringBuilder message = new StringBuilder("Товар удален. Также удалено: ");
                boolean hasItems = false;
                
                if (deletionInfo.getLicenseKeysCount() > 0) {
                    message.append(deletionInfo.getLicenseKeysCount()).append(" лицензионных ключей");
                    hasItems = true;
                }
                if (deletionInfo.getCartItemsCount() > 0) {
                    if (hasItems) message.append(", ");
                    message.append(deletionInfo.getCartItemsCount()).append(" позиций из корзин");
                    hasItems = true;
                }
                if (deletionInfo.getFavoritesCount() > 0) {
                    if (hasItems) message.append(", ");
                    message.append(deletionInfo.getFavoritesCount()).append(" записей из избранного");
                    hasItems = true;
                }
                if (deletionInfo.getOrderItemsCount() > 0) {
                    if (hasItems) message.append(". ");
                    message.append("Внимание: товар присутствует в ").append(deletionInfo.getOrderItemsCount())
                           .append(" заказах (заказы не изменены, только ссылка на товар)");
                }
                
                productService.deleteProduct(id);
                redirectAttributes.addFlashAttribute("successMessage", message.toString());
            } else {
                productService.deleteProduct(id);
                redirectAttributes.addFlashAttribute("successMessage", "Товар удален");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при удалении товара: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }
}
