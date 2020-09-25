package com.github.kb.order.service;

import com.github.kb.api.data.DataStatus;
import com.github.kb.api.data.OrderInfo;
import com.github.kb.api.generate.Order;
import com.github.kb.api.generate.OrderMapper;
import com.github.kb.api.rpc.OrderRpcService;
import com.github.kb.order.mapper.MyOrderMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.function.BooleanSupplier;

@Service(version = "${wxshop.orderservice.version}")
public class RpcOrderRpcServiceImpl implements OrderRpcService {
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private MyOrderMapper myOrderMapper;

    @Override
    public String sayHi(String name) {
        return null;

    }

    @Override
    public Order createOrder(OrderInfo orderInfo, Order order) {
        insertOrder(order);
        myOrderMapper.insertOrders(orderInfo);
        return null;
    }

    private void insertOrder(Order order) {
        order.setStatus(DataStatus.PENDING.getName());
//        if (order.getUserId() == null) {
//            throw new IllegalArgumentException("userId不能为空");
//        }
//
//        if (order.getTotalPrice() == null || order.getTotalPrice().doubleValue() < 0) {
//            throw new IllegalArgumentException("totalPrice非法");
//        }
//
//        if (order.getAddress() == null) {
//            throw new IllegalArgumentException("address不能为空");
//        }

        verify(() -> order.getUserId() == null, "userId不能为空");
        verify(()-> order.getTotalPrice() == null || order.getTotalPrice().doubleValue() < 0, "totalPrice非法");
        verify(()-> order.getAddress() == null, "address不能为空");


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
