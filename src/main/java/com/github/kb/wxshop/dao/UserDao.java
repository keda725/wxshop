package com.github.kb.wxshop.dao;

import com.github.kb.wxshop.generate.User;
import com.github.kb.wxshop.generate.UserExample;
import com.github.kb.wxshop.generate.UserMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;

/**
 * @author zuojiabin
 */
@Service
public class UserDao {
    private final SqlSessionFactory sqlSessionFactory;

    public UserDao(final SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    public void insertUser(final User user) {
        try (final SqlSession sqlSession = this.sqlSessionFactory.openSession(true)) {
            final UserMapper mapper = sqlSession.getMapper(UserMapper.class);
            mapper.insert(user);
        }
    }

    public User getUserByTel(final String tel) {
        try (final SqlSession sqlSession = this.sqlSessionFactory.openSession(true)) {
            final UserMapper mapper = sqlSession.getMapper(UserMapper.class);
            final UserExample example = new UserExample();
            example.createCriteria().andTelEqualTo(tel);
            return mapper.selectByExample(example).get(0);
        }
    }
}
