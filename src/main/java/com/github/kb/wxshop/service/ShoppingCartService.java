package com.github.kb.wxshop.service;

import com.github.kb.wxshop.entity.ShoppingCartGoods;
import com.github.kb.wxshop.generate.ShoppingCartQueryMapper;
import com.github.kb.wxshop.entity.PageResponse;
import com.github.kb.wxshop.entity.ShoppingCartData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ShoppingCartService {
    private ShoppingCartQueryMapper shoppingCartQueryMapper;

    @Autowired
    public ShoppingCartService(ShoppingCartQueryMapper shoppingCartQueryMapper) {
        this.shoppingCartQueryMapper = shoppingCartQueryMapper;
    }

    public PageResponse<ShoppingCartData> getShoppingCartOfUser(Long userId, int pageNum, int pageSize) {
        // 需要知道总共有多少条结果
        // 需要按照结果进行分页查询

        int offset = (pageNum - 1) * pageSize;

        int totalNum = shoppingCartQueryMapper.countHowManyShopsInUserShoppingCart(userId);
        List<ShoppingCartData> pagedDate = shoppingCartQueryMapper.selectShoppingCartDataByUserId(userId, pageSize, offset);
        Map<Long, List<ShoppingCartData>> groupByShopId = pagedDate.stream().collect(
                Collectors.groupingBy(shoppingCartData -> shoppingCartData.getShop().getId())
        );
        List<ShoppingCartData> result = groupByShopId.values().stream().map(this::merge).collect(Collectors.toList());


        int totalPage = totalNum % pageSize == 0 ? totalNum / pageSize : totalNum / pageSize + 1;
        return PageResponse.pageData(pageNum,pageSize, totalPage,pagedDate);

    }

    private ShoppingCartData merge(List<ShoppingCartData> goodsOfSameShop) {
        ShoppingCartData result = new ShoppingCartData();
        result.setShop(goodsOfSameShop.get(0).getShop());
        List<ShoppingCartGoods> goods = goodsOfSameShop.stream()
                .map(ShoppingCartData::getGoods)
                .flatMap(List::stream)
                .collect(Collectors.toList());
        result.setGoods(goods);
        return result;
    }
}
