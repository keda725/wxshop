package com.github.kb.order.service;

import com.github.kb.api.HttpException;
import com.github.kb.api.data.DataStatus;
import com.github.kb.api.data.GoodsInfo;
import com.github.kb.api.data.OrderInfo;
import com.github.kb.api.data.RpcOrderGoods;
import com.github.kb.api.generate.Order;
import com.github.kb.api.generate.OrderMapper;
import com.github.kb.api.rpc.OrderRpcService;
import com.github.kb.order.mapper.MyOrderMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.function.BooleanSupplier;

@Service(version = "${wxshop.orderservice.version}")
public class RpcOrderRpcServiceImpl implements OrderRpcService {
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private MyOrderMapper myOrderMapper;


    @Override
    public Order createOrder(OrderInfo orderInfo, Order order) {
        insertOrder(order);
        orderInfo.setOrderId(order.getId());
        myOrderMapper.insertOrders(orderInfo);
        return null;
    }

    @Override
    public RpcOrderGoods deleteOrder(long orderId, long userId) {
        Order order = orderMapper.selectByPrimaryKey(userId);
        if (order == null) {
            throw HttpException.notFound("订单未找到：" + orderId);
        }
        if (order.getUserId() != userId) {
            throw HttpException.forbidden("无权访问!");
        }

        List<GoodsInfo> goodsInfo = myOrderMapper.getGoodsInfoOfOrder(orderId);

        order.setStatus(DataStatus.DELETED.getName());
        order.setUpdatedAt(new Date());
        orderMapper.updateByPrimaryKey(order);
        RpcOrderGoods result = new RpcOrderGoods();
        result.setGoods(goodsInfo);
        result.setOrder(order);
        return result;
    }

    @Override
    public RpcOrderGoods getOrderById(long orderId) {
        return null;
    }

    private void insertOrder(Order order) {
        order.setStatus(DataStatus.PENDING.getName());

        verify(() -> order.getUserId() == null, "userId不能为空");
        verify(() -> order.getTotalPrice() == null || order.getTotalPrice().doubleValue() < 0, "totalPrice非法");
        verify(() -> order.getAddress() == null, "address不能为空");


        order.setAddress(null);
        order.setExpressId(null);
        order.setCreatedAt(new Date());
        order.setUpdatedAt(new Date());

        orderMapper.insert(order);
    }

    private void verify(BooleanSupplier supplier, String message) {
        if (supplier.getAsBoolean()) {
            throw new IllegalArgumentException(message);
        }
    }
}
