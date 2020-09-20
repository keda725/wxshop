package com.github.kb.order.service;

import com.github.kb.api.rpc.OrderService;
import org.apache.dubbo.config.annotation.Service;

@Service(version = "${wxshop.orderservice.version}")
public class RpcOrderServiceImpl implements OrderService {
    @Override
    public String sayHi(String name) {
        return null;
    }
}
