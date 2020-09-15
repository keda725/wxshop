package com.github.kb.wxshop.mock;

import com.github.kb.wxshop.api.OrderService;
import org.apache.dubbo.config.annotation.Service;

@Service(version = "${wxshop.orderService.version}")
public class MockOrderService implements OrderService {
    @Override
    public void placeOrder(int goodsId, int number) {
        System.out.println("mock");
    }
}
