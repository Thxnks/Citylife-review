package com.citylife.enums;

public enum VoucherOrderStatus {

    PROCESSING(0, "PROCESSING"),
    SUCCESS(1, "SUCCESS"),
    FAILED(7, "FAILED");

    private final int value;
    private final String description;

    VoucherOrderStatus(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public int getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public static String describe(Integer value) {
        if (value == null) {
            return "UNKNOWN";
        }
        for (VoucherOrderStatus status : values()) {
            if (status.value == value) {
                return status.description;
            }
        }
        return "UNKNOWN";
    }
}
