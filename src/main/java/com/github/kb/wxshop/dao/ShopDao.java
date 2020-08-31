package com.github.kb.wxshop.dao;

import com.github.kb.wxshop.generate.Shop;
import com.github.kb.wxshop.generate.ShopMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ShopDao {
    private final ShopMapper shopMapper;

    @Autowired
    public ShopDao(ShopMapper shopMapper) {
        this.shopMapper = shopMapper;
    }

    public Shop findShopById(Long shopId) {
            return shopMapper.selectByPrimaryKey(shopId);
        }
    }

