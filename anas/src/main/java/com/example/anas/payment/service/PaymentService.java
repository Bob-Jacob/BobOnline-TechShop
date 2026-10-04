package com.example.anas.payment.service;

import com.example.anas.common.service.EmailService;
import com.example.anas.order.entity.Order;
import com.example.anas.order.entity.OrderStatus;
import com.example.anas.order.service.OrderService;
import com.example.anas.payment.entity.Payment;
import com.example.anas.payment.entity.PaymentStatus;
import com.example.anas.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderService orderService;
    private final EmailService emailService;

    @Transactional
    public Payment createPayment(Order order) {
        Payment existingPayment = paymentRepository.findByOrder(order)
                .orElse(null);

        if (existingPayment != null) {
            return existingPayment;
        }

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(order.getTotalPrice());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaymentMethod("MOCK");
        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment processPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Платеж не найден"));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalArgumentException("Платеж уже обработан");
        }

        // Мок-обработка: всегда успешно
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setTransactionId("MOCK-" + UUID.randomUUID().toString());
        payment = paymentRepository.save(payment);

        // Обновление статуса заказа
        orderService.updateOrderStatus(payment.getOrder().getId(), OrderStatus.PAID);

        // Выполнение заказа (выдача товаров)
        orderService.completeOrder(payment.getOrder().getId());

        // Отправка email с подтверждением
        Order order = payment.getOrder();
        emailService.sendOrderConfirmation(order.getUser(), order, payment);

        return payment;
    }

    public Payment getPaymentByOrder(Order order) {
        return paymentRepository.findByOrder(order)
                .orElseThrow(() -> new IllegalArgumentException("Платеж не найден"));
    }
}
