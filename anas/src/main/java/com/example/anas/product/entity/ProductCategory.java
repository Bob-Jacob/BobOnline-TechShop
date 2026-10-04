package com.example.anas.product.entity;

public enum ProductCategory {
    GAME_LICENSE("Лицензии игр"),
    SOFTWARE_LICENSE("Лицензии ПО"),
    SUBSCRIPTION("Подписки"),
    VPN_SERVICE("VPN-доступ"),
    HOSTING_SERVICE("Хостинг и домены"),
    CLOUD_STORAGE("Облачное хранилище"),
    EBOOK("Электронные книги"),
    ONLINE_COURSE("Онлайн-курсы"),
    TEMPLATE("Шаблоны, темы, плагины");

    private final String displayName;

    ProductCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
