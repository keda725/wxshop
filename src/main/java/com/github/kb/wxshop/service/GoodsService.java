package com.github.kb.wxshop.service;

import com.github.kb.wxshop.dao.GoodsDao;
import com.github.kb.wxshop.dao.ShopDao;
import com.github.kb.wxshop.generate.Goods;
import com.github.kb.wxshop.generate.Shop;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class GoodsService {

    private final GoodsDao goodsDao;
    private final ShopDao shopDao;

    @Autowired
    public GoodsService(GoodsDao goodsDao, ShopDao shopDao) {
        this.goodsDao = goodsDao;
        this.shopDao = shopDao;
    }

    public Goods createGoods(Goods goods) {
        Shop shop = shopDao.findShopById(goods.getShopId());
        if (Objects.equals(shop.getOwnerUserId(), UserContext.getCurrentUser().getId())) {
            return goodsDao.insertGoods(goods);
        } else {
            throw new NoAuthorizedForShopException("无权访问!");
        }
    }

    public Goods deleteGoodsById(Long goodsId) {
        Shop shop = shopDao.findShopById(goodsId);
        if (Objects.equals(shop.getOwnerUserId(), UserContext.getCurrentUser().getId())) {
            return goodsDao.deleteGoodsById(goodsId);
        } else {
            throw new NoAuthorizedForShopException("无权访问！");
        }
    }

    public static class NoAuthorizedForShopException extends RuntimeException {
        public NoAuthorizedForShopException(String message) {
            super(message);
        }
    }

}
