package com.example.anas.auth.controller;

import com.example.anas.auth.entity.User;
import com.example.anas.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;

    @GetMapping
    public String profile(@AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails,
                         Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        User user = getCurrentUser(userDetails);
        model.addAttribute("user", user);
        model.addAttribute("title", "Профиль");
        return "profile";
    }

    @PostMapping("/update-email")
    public String updateEmail(@AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails,
                             @RequestParam String newEmail,
                             RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        
        try {
            User user = getCurrentUser(userDetails);
            
            // Проверка, что email не занят другим пользователем
            if (userRepository.findByEmail(newEmail).isPresent() && 
                !user.getEmail().equals(newEmail)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Этот email уже используется");
                return "redirect:/profile";
            }
            
            user.setEmail(newEmail);
            userRepository.save(user);
            
            redirectAttributes.addFlashAttribute("successMessage", "Email успешно изменен");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при изменении email: " + e.getMessage());
        }
        
        return "redirect:/profile";
    }

    @PostMapping("/contact-support")
    public String contactSupport(@AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails,
                                 @RequestParam String subject,
                                 @RequestParam String message,
                                 RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        
        try {
            User user = getCurrentUser(userDetails);
            
            // Отправляем письмо админу от имени пользователя
            String adminEmail = "cyril.shatkovsky@yandex.ru";
            String emailBody = String.format(
                "Новое обращение в поддержку\n\n" +
                "От пользователя: %s\n" +
                "Email пользователя: %s\n" +
                "ID пользователя: %d\n\n" +
                "Тема: %s\n\n" +
                "Сообщение:\n%s\n\n" +
                "---\n" +
                "Это автоматическое сообщение от Digital Store",
                user.getEmail(), user.getEmail(), user.getId(), subject, message
            );
            
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom("cyril.shatkovsky@yandex.ru");
            mailMessage.setTo(adminEmail);
            mailMessage.setReplyTo(user.getEmail()); // Админ сможет ответить напрямую пользователю
            mailMessage.setSubject("[Поддержка] " + subject + " - от " + user.getEmail());
            mailMessage.setText(emailBody);
            mailSender.send(mailMessage);
            
            redirectAttributes.addFlashAttribute("successMessage", "Ваше сообщение отправлено в поддержку");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при отправке сообщения: " + e.getMessage());
        }
        
        return "redirect:/profile";
    }

    private User getCurrentUser(org.springframework.security.core.userdetails.UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
    }
}
