package com.github.kb.wxshop.entity;

public class Response<T> {
    private String message;
    private T data;

    public static <T> Response<T> of(T data) {
        return new Response<T>(null, data);
    }

    public static <T> Response<T> of(String message, T data) {
        return new Response<T>(message, data);
    }

    public Response(String message, T data) {
        this.message = message;
        this.data = data;
    }

    public Response() {
    }

    public T getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }
}
