package com.winnguyen1905.promotion.common;

public enum ApplyDiscountType {
    ALL("all"),
    SPECIFIC("specific"),
    CATEGORY("category");

    private final String applyDiscountType;

    ApplyDiscountType(String applyDiscountType) {
        this.applyDiscountType = applyDiscountType;
    }

    public String getApplyDiscountType() {
        return applyDiscountType;
    }
}
