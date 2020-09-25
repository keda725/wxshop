package com.github.kb.wxshop.mock;

import com.github.kb.api.data.OrderInfo;
import com.github.kb.api.data.RpcOrderGoods;
import com.github.kb.api.generate.Order;
import com.github.kb.api.rpc.OrderRpcService;
import org.apache.dubbo.config.annotation.Service;
import org.mockito.Mock;

@Service(version = "${wxshop.orderservice.version}")
public class MockOrderRpcService implements OrderRpcService {
    @Mock
    public OrderRpcService orderRpcService;

    @Override
    public Order createOrder(OrderInfo orderInfo, Order order) {
        return orderRpcService.createOrder(orderInfo, order);
    }

    @Override
    public RpcOrderGoods getOrderById(long orderId) {
        return null;
    }
}
