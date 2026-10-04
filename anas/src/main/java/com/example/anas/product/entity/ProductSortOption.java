package com.example.anas.product.entity;

public enum ProductSortOption {
    PRICE_ASC("По цене (возрастание)"),
    PRICE_DESC("По цене (убывание)"),
    NAME_ASC("По названию (А-Я)"),
    DATE_DESC("По дате добавления (новые первые)"),
    RATING_DESC("По рейтингу (высокий рейтинг)");

    private final String displayName;

    ProductSortOption(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
