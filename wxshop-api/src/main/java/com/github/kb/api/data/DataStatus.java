package com.github.kb.api.data;

public enum  DataStatus {
    OK(),
    DELETED(),
    // only for order
    PENDING(),
    PAID(),
    DELIVERED(),
    RECEIVED();

    public String getName() {
        return name().toLowerCase();
    }

    public static DataStatus formStatus(String name) {
        try {
            return DataStatus.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
