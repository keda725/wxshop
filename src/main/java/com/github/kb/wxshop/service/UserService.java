package com.github.kb.wxshop.service;

import com.github.kb.wxshop.dao.UserDao;
import com.github.kb.wxshop.generate.User;

/**
 * @author zuojiabin
 */
public class UserService {
    private final UserDao userDao;

    public UserService(final UserDao userDao) {
        this.userDao = userDao;
    }

    public User createUserIfNotExists(final String tel) {
        final User user = new User();
        user.setTel(tel);
        this.userDao.insertUser(user);
        return user;

    }
}
