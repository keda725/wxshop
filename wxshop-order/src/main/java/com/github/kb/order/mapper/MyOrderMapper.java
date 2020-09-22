package com.github.kb.order.mapper;

import com.github.kb.api.data.OrderInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MyOrderMapper {
    void insertOrders(OrderInfo orderInfo);
}
