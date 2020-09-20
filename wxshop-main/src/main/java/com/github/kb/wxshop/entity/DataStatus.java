package com.github.kb.wxshop.entity;

public enum  DataStatus {
    OK(),
    DELETED();

    public String getName() {
        return name().toLowerCase();
    }
}
