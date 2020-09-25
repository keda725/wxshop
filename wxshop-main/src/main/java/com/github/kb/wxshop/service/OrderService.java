package com.github.kb.wxshop.service;

import com.github.kb.api.data.DataStatus;
import com.github.kb.api.data.GoodsInfo;
import com.github.kb.api.data.OrderInfo;
import com.github.kb.api.data.RpcOrderGoods;
import com.github.kb.api.generate.Order;
import com.github.kb.api.rpc.OrderRpcService;
import com.github.kb.wxshop.dao.GoodsStockMapper;
import com.github.kb.wxshop.entity.GoodsWithNumber;
import com.github.kb.api.HttpException;
import com.github.kb.wxshop.entity.OrderResponse;
import com.github.kb.api.data.PageResponse;
import com.github.kb.wxshop.generate.Goods;
import com.github.kb.wxshop.generate.Shop;
import com.github.kb.wxshop.generate.ShopMapper;
import com.github.kb.wxshop.generate.UserMapper;
import org.apache.dubbo.config.annotation.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
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
    private GoodsStockMapper goodsStockMapper;

    @Autowired
    public OrderService(UserMapper userMapper,
                        GoodsService goodsService,
                        ShopMapper shopMapper,
                        GoodsStockMapper goodsStockMapper) {
        this.userMapper = userMapper;
        this.goodsService = goodsService;
        this.shopMapper = shopMapper;
        this.goodsStockMapper = goodsStockMapper;
    }

    public OrderResponse createOrder(OrderInfo orderInfo, Long userId) {

        Map<Long, Goods> idToGoodsMap = getIdToGoodsMap(orderInfo.getGoods());

        Order order = createOrderViaRpc(orderInfo, userId, idToGoodsMap);

        return generateResponse(order, idToGoodsMap, orderInfo.getGoods());
    }


    /**
     * 获取ID到商品的映射
     *
     * @param goodsInfo
     * @return
     */
    private Map<Long, Goods> getIdToGoodsMap(List<GoodsInfo> goodsInfo) {
        if (goodsInfo.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> goodsId = goodsInfo
                .stream()
                .map(GoodsInfo::getId)
                .collect(toList());

        return goodsService.getIdToGoodsMap(goodsId);
    }


    /**
     * 通过RPC创建订单
     *
     * @param orderInfo
     * @param userId
     * @param idToGoodsMap
     * @return
     */
    private Order createOrderViaRpc(OrderInfo orderInfo, Long userId, Map<Long, Goods> idToGoodsMap) {
        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(DataStatus.PENDING.getName());
        order.setAddress(userMapper.selectByPrimaryKey(userId).getAddress());
        order.setTotalPrice(calculateTotalPrice(orderInfo, idToGoodsMap));
        return order;
    }

    private OrderResponse generateResponse(Order createdOrder, Map<Long, Goods> idToGoodsMap, List<GoodsInfo> goodsInfo) {
        OrderResponse response = new OrderResponse(createdOrder);
        Long shopId = new ArrayList<>(idToGoodsMap.values()).get(0).getShopId();
        response.setShop(shopMapper.selectByPrimaryKey(shopId));
        response.setGoods(goodsInfo.stream().map(goods -> toGoodsWithNumber(goods, idToGoodsMap)).collect(toList()));

        return response;
    }

    // 使用mybatis-spring的事务管理
    @Transactional
    public void deductStock(OrderInfo orderInfo) {
        for (GoodsInfo goodsInfo : orderInfo.getGoods()) {
            if (goodsStockMapper.deductStock(goodsInfo) < 0) {
                LOGGER.error("扣减库存失败，商品id:" + goodsInfo.getId() + ",数量:" + goodsInfo.getNumber());
                throw HttpException.gone("扣减库存失败!");
            }
        }
    }


    private GoodsWithNumber toGoodsWithNumber(GoodsInfo goodsInfo, Map<Long, Goods> idToGoodsMap) {
        GoodsWithNumber ret = new GoodsWithNumber(idToGoodsMap.get(goodsInfo.getId()));
        ret.setNumber(goodsInfo.getNumber());
        return ret;
    }


    private Long calculateTotalPrice(OrderInfo orderInfo, Map<Long, Goods> idToGoodsMap) {
        // 订单价格总和
        long result = 0L;

        for (GoodsInfo goodsInfo : orderInfo.getGoods()) {
            Goods goods = idToGoodsMap.get(goodsInfo.getId());
            if (goods == null) {
                throw HttpException.badRequest("goods id 非法" + goodsInfo.getId());
            }

            if (goodsInfo.getNumber() <= 0) {
                throw HttpException.badRequest("number 非法" + goodsInfo.getNumber());
            }

            result = result + goods.getPrice() * goodsInfo.getNumber();
        }
        return result;
    }

    public RpcOrderGoods doGetOrderById(long userId, long orderId) {
        RpcOrderGoods orderInDatabase = orderRpcService.getOrderById(orderId);
        if (orderInDatabase == null) {
            throw HttpException.notFound("订单未找到: " + orderId);
        }
        Shop shop = shopMapper.selectByPrimaryKey(orderInDatabase.getOrder().getShopId());
        if (shop == null) {
            throw HttpException.notFound("店铺未找到: " + orderInDatabase.getOrder().getShopId());
        }

        if (shop.getOwnerUserId() != userId && orderInDatabase.getOrder().getUserId() != userId) {
            throw HttpException.forbidden("无权访问！");
        }
        return orderInDatabase;
    }

    public OrderResponse getOrderById(long userId, long orderId) {
        return toOrderResponse(doGetOrderById(userId, orderId));
    }

    private OrderResponse toOrderResponse(RpcOrderGoods rpcOrderGoods) {
        Map<Long, Goods> idToGoodsMap = getIdToGoodsMap(rpcOrderGoods.getGoods());
        return generateResponse(rpcOrderGoods.getOrder(), idToGoodsMap, rpcOrderGoods.getGoods());
    }


    public OrderResponse deleteOrder(long orderId, long userId) {
        RpcOrderGoods rpcOrderGoods = orderRpcService.deleteOrder(orderId, userId);
        Map<Long, Goods> idToGoodsMap = getIdToGoodsMap(rpcOrderGoods.getGoods());
        return generateResponse(rpcOrderGoods.getOrder(), idToGoodsMap, rpcOrderGoods.getGoods());
    }

    public PageResponse<OrderResponse> getOrder(long userId,Integer pageNum, Integer pageSize, DataStatus status) {
        PageResponse<RpcOrderGoods> rpcOrderGoods = orderRpcService.getOrder(userId,pageNum, pageSize, status);
        List<GoodsInfo> goodIds = rpcOrderGoods
                .getData()
                .stream()
                .map(RpcOrderGoods::getGoods)
                .flatMap(List::stream)
                .collect(toList());
        Map<Long, Goods> idToGoodsMap = getIdToGoodsMap(goodIds);

        List<OrderResponse> orders = rpcOrderGoods.getData()
                .stream()
                .map(order -> generateResponse(order.getOrder(), idToGoodsMap, order.getGoods()))
                .collect(toList());

        return PageResponse.pagedata(rpcOrderGoods.getPageNum(),
                rpcOrderGoods.getPageSize(),
                rpcOrderGoods.getTotalPage(),
                orders);
    }
}
