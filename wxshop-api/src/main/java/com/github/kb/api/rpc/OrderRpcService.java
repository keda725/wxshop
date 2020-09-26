package com.github.kb.api.rpc;

import com.github.kb.api.data.PageResponse;
import com.github.kb.api.data.DataStatus;
import com.github.kb.api.data.OrderInfo;
import com.github.kb.api.data.RpcOrderGoods;
import com.github.kb.api.generate.Order;

public interface OrderRpcService {

    Order createOrder(OrderInfo orderInfo, Order order);

    Order getOrderById(long orderId);

    RpcOrderGoods deleteOrder(long orderId, long userId);

    PageResponse<RpcOrderGoods> getOrder(long userId, Integer pageNum, Integer pageSize, DataStatus status);

    RpcOrderGoods updateOrder(Order order);
}
