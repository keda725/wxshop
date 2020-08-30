package com.github.kb.wxshop.service;

import com.github.kb.wxshop.generate.User;

public class UserContext {
    private static ThreadLocal<User> currentUser = new ThreadLocal<>();

    public static User getCurrentUser() {
        return currentUser.get();
    }

    public static void setCurrentUser(User user) {
        currentUser.set(user);
    }
}
