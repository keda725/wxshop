package com.github.kb.wxshop.service;

import com.github.kb.api.data.GoodsInfo;
import com.github.kb.api.data.OrderInfo;
import com.github.kb.api.generate.Order;
import com.github.kb.api.rpc.OrderRpcService;
import com.github.kb.wxshop.entity.GoodsWithNumber;
import com.github.kb.wxshop.entity.HttpException;
import com.github.kb.wxshop.entity.OrderResponse;
import com.github.kb.wxshop.generate.Goods;
import com.github.kb.wxshop.generate.GoodsMapper;
import com.github.kb.wxshop.generate.ShopMapper;
import com.github.kb.wxshop.generate.UserMapper;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.*;

@Service
public class OrderService {
    @Reference(version = "${wxshop.orderservice.version}")
    private OrderRpcService orderRpcService;

    private UserMapper userMapper;
    private GoodsMapper goodsMapper;
    private GoodsService goodsService;
    private ShopMapper shopMapper;

    @Autowired
    public OrderService(UserMapper userMapper,
                        GoodsMapper goodsMapper,
                        GoodsService goodsService, ShopMapper shopMapper) {
        this.userMapper = userMapper;
        this.goodsMapper = goodsMapper;
        this.goodsService = goodsService;
        this.shopMapper = shopMapper;
    }

    public OrderResponse createOrder(OrderInfo orderInfo, Long userId) {
        List<Long> goodsId = orderInfo.getGoods()
                .stream()
                .map(GoodsInfo::getId)
                .collect(toList());

        Map<Long, Goods> idToGoodsMap = goodsService.getIdToGoodsMap(goodsId);
        Order order = new Order();
        order.setId(userId);
        order.setAddress(userMapper.selectByPrimaryKey(userId).getAddress());
        order.setTotalPrice(calculateTotalPrice(orderInfo, idToGoodsMap));

        Order createdOrder = orderRpcService.createOrder(orderInfo, order);
        OrderResponse response = new OrderResponse(createdOrder);
        Long shopId = new ArrayList<>(idToGoodsMap.values()).get(0).getShopId();
        response.setShop(shopMapper.selectByPrimaryKey(shopId));
        response.setGoods(orderInfo.getGoods()
                .stream().map(goods -> toGoodsWithNumber(goods, idToGoodsMap))
                .collect(toList()));

        return response;
    }

    private GoodsWithNumber toGoodsWithNumber(GoodsInfo goodsInfo, Map<Long, Goods> idToGoodsMap) {
        GoodsWithNumber ret = new GoodsWithNumber(idToGoodsMap.get(goodsInfo.getId()));
        ret.setNumber(goodsInfo.getNumber());
        return ret;
    }


    private BigDecimal calculateTotalPrice(OrderInfo orderInfo, Map<Long, Goods> idToGoodsMap) {
        // 订单价格总和
        BigDecimal result = BigDecimal.ZERO;

        for (GoodsInfo goodsInfo : orderInfo.getGoods()) {
            Goods goods = idToGoodsMap.get(goodsInfo.getId());
            if (goods == null) {
                throw HttpException.badRequest("goods id 非法" + goodsInfo.getId());
            }

            if (goodsInfo.getNumber() <= 0) {
                throw HttpException.badRequest("number 非法" + goodsInfo.getNumber());
            }

            result = result.add(goods.getPrice().multiply(new BigDecimal(goodsInfo.getNumber())));
        }
        return result;
    }
}
