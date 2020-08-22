package com.github.kb.wxshop.service;

import com.github.kb.wxshop.dao.UserDao;
import com.github.kb.wxshop.generate.User;
import org.apache.ibatis.exceptions.PersistenceException;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author zuojiabin
 */
@Service
public class UserService {
    private final UserDao userDao;

    public UserService(final UserDao userDao) {
        this.userDao = userDao;
    }

    public User createUserIfNotExists(final String tel) {
        final User user = new User();
        user.setTel(tel);
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());
        try {
            this.userDao.insertUser(user);
        } catch (final PersistenceException e) {
            e.printStackTrace();
            return this.userDao.getUserByTel(tel);
        }
        return user;

    }
}
