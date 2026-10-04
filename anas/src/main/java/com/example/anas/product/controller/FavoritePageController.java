package com.example.anas.product.controller;

import com.example.anas.auth.entity.User;
import com.example.anas.auth.repository.UserRepository;
import com.example.anas.product.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class FavoritePageController {
    private final FavoriteService favoriteService;
    private final UserRepository userRepository;

    @GetMapping("/favorites")
    public String favorites(@AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails,
                           Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        User user = getCurrentUser(userDetails);
        model.addAttribute("products", favoriteService.getUserFavorites(user));
        model.addAttribute("title", "Избранное");
        return "favorites";
    }

    private User getCurrentUser(org.springframework.security.core.userdetails.UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
    }
}
