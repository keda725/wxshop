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

    public User createUserIfNotExists(String tel) {
        User user = new User();
        user.setTel(tel);
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());
        try {
            userDao.insertUser(user);
        } catch (PersistenceException e) {
            e.printStackTrace();
            return userDao.getUserByTel(tel);
        }
        return user;

    }

    /**
     * 根据电话返回用户 如果客户不存在 返回null
     * @param tel 电话
     * @return 返回用户信息
     */
    public User getUserByTel(String tel) {
        return userDao.getUserByTel(tel);
    }
}
