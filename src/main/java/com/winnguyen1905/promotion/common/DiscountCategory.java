package com.winnguyen1905.promotion.common;

public enum DiscountCategory {
    PRODUCT("PRODUCT"),
    SHIPPING("SHPPING");

    private final String description;

    DiscountCategory(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}