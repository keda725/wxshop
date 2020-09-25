package com.github.kb.api.rpc;

import com.github.kb.api.data.OrderInfo;
import com.github.kb.api.data.RpcOrderGoods;
import com.github.kb.api.generate.Order;

public interface OrderRpcService {

    Order createOrder(OrderInfo orderInfo, Order order);

    RpcOrderGoods getOrderById(long orderId);
}
