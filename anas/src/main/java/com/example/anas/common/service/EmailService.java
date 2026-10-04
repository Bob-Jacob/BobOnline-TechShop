package com.example.anas.common.service;

import com.example.anas.auth.entity.User;
import com.example.anas.order.entity.Order;
import com.example.anas.order.entity.OrderItem;
import com.example.anas.payment.entity.Payment;
import com.example.anas.product.entity.LicenseKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final JavaMailSender mailSender;

    public void sendOrderConfirmation(User user, Order order, Payment payment) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("cyril.shatkovsky@yandex.ru");
            message.setTo(user.getEmail());
            message.setSubject("Подтверждение заказа #" + order.getId());
            
            StringBuilder body = new StringBuilder();
            body.append("Здравствуйте, ").append(user.getEmail()).append("!\n\n");
            body.append("Ваш заказ #").append(order.getId()).append(" успешно оформлен.\n\n");
            body.append("Состав заказа:\n");
            
            for (OrderItem item : order.getItems()) {
                body.append("- ").append(item.getProduct().getName())
                    .append(" x").append(item.getQuantity())
                    .append(" - ").append(item.getPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity())))
                    .append(" ₽\n");
            }
            
            body.append("\nИтого: ").append(order.getTotalPrice()).append(" ₽\n");
            body.append("Статус платежа: ").append(payment.getStatus().getDisplayName()).append("\n\n");
            
            if (payment.getStatus().name().equals("SUCCESS")) {
                body.append("Спасибо за покупку!\n");
            } else {
                body.append("Для завершения заказа необходимо произвести оплату.\n");
            }
            
            message.setText(body.toString());
            mailSender.send(message);
            log.info("Письмо отправлено пользователю: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Ошибка при отправке письма", e);
        }
    }

    public void sendLicenseKeys(User user, Order order, List<LicenseKey> licenseKeys) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("cyril.shatkovsky@yandex.ru");
            message.setTo(user.getEmail());
            message.setSubject("Лицензионные ключи для заказа #" + order.getId());
            
            StringBuilder body = new StringBuilder();
            body.append("Здравствуйте, ").append(user.getEmail()).append("!\n\n");
            body.append("Ваш заказ #").append(order.getId()).append(" выполнен.\n\n");
            body.append("Лицензионные ключи:\n\n");
            
            for (LicenseKey key : licenseKeys) {
                body.append("Товар: ").append(key.getProduct().getName()).append("\n");
                body.append("Ключ: ").append(key.getKey()).append("\n\n");
            }
            
            body.append("Спасибо за покупку!\n");
            
            message.setText(body.toString());
            mailSender.send(message);
            log.info("Лицензионные ключи отправлены пользователю: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Ошибка при отправке лицензионных ключей", e);
        }
    }

    public void sendDownloadLinks(User user, Order order, List<OrderItem> digitalItems) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("cyril.shatkovsky@yandex.ru");
            message.setTo(user.getEmail());
            message.setSubject("Ссылки на скачивание для заказа #" + order.getId());
            
            StringBuilder body = new StringBuilder();
            body.append("Здравствуйте, ").append(user.getEmail()).append("!\n\n");
            body.append("Ваш заказ #").append(order.getId()).append(" выполнен.\n\n");
            body.append("Ссылки на скачивание:\n\n");
            
            for (OrderItem item : digitalItems) {
                if (item.getProduct().getFileUrl() != null) {
                    body.append("Товар: ").append(item.getProduct().getName()).append("\n");
                    body.append("Ссылка: ").append(item.getProduct().getFileUrl()).append("\n\n");
                }
            }
            
            body.append("Спасибо за покупку!\n");
            
            message.setText(body.toString());
            mailSender.send(message);
            log.info("Ссылки на скачивание отправлены пользователю: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Ошибка при отправке ссылок на скачивание", e);
        }
    }
}
