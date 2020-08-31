package com.github.kb.wxshop.dao;

import com.github.kb.wxshop.generate.Goods;
import com.github.kb.wxshop.generate.GoodsMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GoodsDao {
    private final SqlSessionFactory sqlSessionFactory;

    @Autowired
    public GoodsDao(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    public Goods insertGoods(Goods goods) {
        try (SqlSession sqlSession = sqlSessionFactory.openSession(true)) {
            GoodsMapper mapper = sqlSession.getMapper(GoodsMapper.class);
            long id = mapper.insert(goods);
            goods.setId(id);
            return goods;
        }
    }
}
