package com.github.kb.wxshop.service;

import com.github.kb.wxshop.controller.ShoppingCartController;
import com.github.kb.wxshop.entity.PageResponse;
import org.springframework.stereotype.Service;

@Service
public class ShoppingCartService {

    public PageResponse<ShoppingCartController.ShoppingCartData> getShoppingCartOfUser(Long id, int pageNum, int pageSize) {
        // 需要知道总共有多少条结果
        // 需要按照结果进行分页查询
        return null;
    }
}
