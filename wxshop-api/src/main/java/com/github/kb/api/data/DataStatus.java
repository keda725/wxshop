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
}
