package com.example.anas.order.entity;

public enum OrderStatus {
    PENDING("Ожидает оплаты"),
    PAID("Оплачен"),
    COMPLETED("Выполнен"),
    CANCELLED("Отменен");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
