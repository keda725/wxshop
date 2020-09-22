package com.github.kb.api.rpc;

import com.github.kb.api.data.OrderInfo;
import com.github.kb.api.generate.Order;

public interface OrderRpcService {
    String sayHi(String name);

    Order createOrder(OrderInfo orderInfo, Order order);
}
