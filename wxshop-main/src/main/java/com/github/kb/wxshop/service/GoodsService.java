package com.github.kb.wxshop.service;

import com.github.kb.api.data.DataStatus;
import com.github.kb.api.HttpException;
import com.github.kb.wxshop.entity.PageResponse;
import com.github.kb.wxshop.generate.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.toMap;

@Service
public class GoodsService {

    private final GoodsMapper goodsMapper;
    private final ShopMapper shopMapper;

    @Autowired
    public GoodsService(GoodsMapper goodsMapper, ShopMapper shopMapper) {
        this.goodsMapper = goodsMapper;
        this.shopMapper = shopMapper;
    }

    public Goods createGoods(Goods goods) {
        Shop shop = shopMapper.selectByPrimaryKey(goods.getShopId());
        if (Objects.equals(shop.getOwnerUserId(), UserContext.getCurrentUser().getId())) {
            goods.setStatus(DataStatus.OK.getName());
            long id = goodsMapper.insert(goods);
            goods.setId(id);
            return goods;
        } else {
            throw HttpException.forbidden("无权访问!");
        }
    }

    public Goods deleteGoodsById(Long goodsId) {
        Shop shop = shopMapper.selectByPrimaryKey(goodsId);
        if (shop == null || Objects.equals(shop.getOwnerUserId(), UserContext.getCurrentUser().getId())) {
            Goods goods = goodsMapper.selectByPrimaryKey(goodsId);
            if (goods == null) {
                throw HttpException.notFound("商品未找到！");
            }
            goods.setStatus(DataStatus.DELETED.getName());
            goodsMapper.updateByPrimaryKey(goods);
            return goods;
        } else {
            throw HttpException.forbidden("无权访问！");
        }
    }


    public PageResponse<Goods> getGoods(Integer pageNum, Integer pageSize, Integer shopId) {
        // 知道有多少个元素
        // 然后才知道有多少页
        // 然后正确的分页
        int totalNum = countGoods(shopId);
        int totalPage = totalNum % pageSize == 0 ? totalNum / pageSize : totalNum / pageSize + 1;
        GoodsExample page = new GoodsExample();
        page.setLimit(pageSize);
        page.setOffset((pageNum - 1) * pageSize);
        List<Goods> pageGoods = goodsMapper.selectByExample(page);
        return PageResponse.pageData(pageNum, pageSize, totalPage, pageGoods);
    }

    private int countGoods(Integer shopId) {
        GoodsExample goodsExample = new GoodsExample();
        if (shopId == null) {
            goodsExample.createCriteria().andStatusEqualTo(DataStatus.OK.getName());
        } else {
            goodsExample.createCriteria()
                    .andStatusEqualTo(DataStatus.OK.getName())
                    .andShopIdEqualTo(shopId.longValue());
        }
        return (int) goodsMapper.countByExample(goodsExample);
    }


    public Goods updateGoods(Goods goods) {
        Shop shop = shopMapper.selectByPrimaryKey(goods.getShopId());
        if (Objects.equals(shop.getOwnerUserId(), UserContext.getCurrentUser().getId())) {
            GoodsExample byId = new GoodsExample();
            byId.createCriteria().andShopIdEqualTo(goods.getId());
            int affectedRows = goodsMapper.updateByExample(goods, byId);
            if (affectedRows == 0) {
                throw HttpException.notFound("未找到！");
            }
            return goods;
        } else {
            throw HttpException.forbidden("无权访问!");
        }

    }

    public Map<Long, Goods> getIdToGoodsMap(List<Long> goodsId) {
        GoodsExample example = new GoodsExample();
        example.createCriteria().andIdIn(goodsId);
        List<Goods> goods = goodsMapper.selectByExample(example);

        return goods.stream().collect(toMap(Goods::getId, x -> x));
    }

}
