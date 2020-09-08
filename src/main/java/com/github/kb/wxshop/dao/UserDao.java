package com.github.kb.wxshop.dao;

import com.github.kb.wxshop.generate.User;
import com.github.kb.wxshop.generate.UserExample;
import com.github.kb.wxshop.generate.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * @author zuojiabin
 */
@Service
public class UserDao {
    private final UserMapper userMapper;

    @Autowired
    public UserDao(UserMapper userMapper) {
        this.userMapper = userMapper;
    }


    public void insertUser(User user) {
        try {
            userMapper.insert(user);
        } catch (Exception e) {
            if (e.getCause() instanceof SQLIntegrityConstraintViolationException) {
                System.out.println("用户已注册");
            }
        }
    }

    public User getUserByTel(String tel) {
            UserExample example = new UserExample();
            example.createCriteria().andTelEqualTo(tel);
            return userMapper.selectByExample(example).get(0);
        }
    }
