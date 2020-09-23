package com.github.kb.wxshop.service;

import com.github.kb.api.data.GoodsInfo;
import com.github.kb.api.data.OrderInfo;
import com.github.kb.api.generate.Order;
import com.github.kb.api.rpc.OrderRpcService;
import com.github.kb.wxshop.dao.GoodsStockMapper;
import com.github.kb.wxshop.entity.GoodsWithNumber;
import com.github.kb.wxshop.entity.HttpException;
import com.github.kb.wxshop.entity.OrderResponse;
import com.github.kb.wxshop.generate.Goods;
import com.github.kb.wxshop.generate.ShopMapper;
import com.github.kb.wxshop.generate.UserMapper;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.*;

@Service
public class OrderService {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderService.class);
    @Reference(version = "${wxshop.orderservice.version}")
    private OrderRpcService orderRpcService;

    private UserMapper userMapper;
    private GoodsService goodsService;
    private ShopMapper shopMapper;
    private SqlSessionFactory sqlSessionFactory;
    private GoodsStockMapper goodsStockMapper;

    @Autowired
    public OrderService(UserMapper userMapper,
                        GoodsService goodsService,
                        ShopMapper shopMapper,
                        SqlSessionFactory sqlSessionFactory,
                        GoodsStockMapper goodsStockMapper) {
        this.userMapper = userMapper;
        this.goodsService = goodsService;
        this.shopMapper = shopMapper;
        this.sqlSessionFactory = sqlSessionFactory;
        this.goodsStockMapper = goodsStockMapper;
    }

    public OrderResponse createOrder(OrderInfo orderInfo, Long userId) {
        // 判断扣减库存是否成功
        if (!deductStock(orderInfo)) {
            throw HttpException.gone("扣减库存失败!");
        }

        Map<Long, Goods> idToGoodsMap = getIdToGoodsMap(orderInfo);

        Order order = createOrderViaRpc(orderInfo, userId, idToGoodsMap);

        return generateResponse(orderInfo, idToGoodsMap, order);
    }


    private OrderResponse generateResponse(OrderInfo orderInfo, Map<Long, Goods> idToGoodsMap, Order order) {
        Order createdOrder = orderRpcService.createOrder(orderInfo, order);
        OrderResponse response = new OrderResponse(createdOrder);
        Long shopId = new ArrayList<>(idToGoodsMap.values()).get(0).getShopId();
        response.setShop(shopMapper.selectByPrimaryKey(shopId));
        response.setGoods(orderInfo.getGoods()
                .stream().map(goods -> toGoodsWithNumber(goods, idToGoodsMap))
                .collect(toList()));

        return response;
    }

    /**
     * 通过RPC创建订单
     * @param orderInfo
     * @param userId
     * @param idToGoodsMap
     * @return
     */
    private Order createOrderViaRpc(OrderInfo orderInfo, Long userId, Map<Long, Goods> idToGoodsMap) {
        Order order = new Order();
        order.setId(userId);
        order.setAddress(userMapper.selectByPrimaryKey(userId).getAddress());
        order.setTotalPrice(calculateTotalPrice(orderInfo, idToGoodsMap));
        return order;
    }

    /**
     * 获取ID到商品的映射
     * @param orderInfo
     * @return
     */
    private Map<Long, Goods> getIdToGoodsMap(OrderInfo orderInfo) {
        List<Long> goodsId = orderInfo.getGoods()
                .stream()
                .map(GoodsInfo::getId)
                .collect(toList());

        return goodsService.getIdToGoodsMap(goodsId);
    }

    /**
     * 扣减库存
     * @param orderInfo
     * @return 若全部扣减成功 返回true 否则返回false
     */
    private boolean deductStock(OrderInfo orderInfo) {
        try (SqlSession sqlSession = sqlSessionFactory.openSession(false)) {
            for (GoodsInfo goodsInfo : orderInfo.getGoods()) {
                if (goodsStockMapper.deductStock(goodsInfo) <= 0) {
                    LOGGER.error("扣减库存失败，商品id:" + goodsInfo.getId() + ",数量:" + goodsInfo.getNumber());
                    sqlSession.rollback();
                    return false;
                }
            }
            sqlSession.commit();
            return true;
        }
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
