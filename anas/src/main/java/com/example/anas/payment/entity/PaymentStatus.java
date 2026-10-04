package com.example.anas.payment.entity;

public enum PaymentStatus {
    PENDING("Ожидает оплаты"),
    SUCCESS("Успешно"),
    FAILED("Неудачно");

    private final String displayName;

    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
