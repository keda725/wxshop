package com.github.kb.wxshop.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.kb.wxshop.WxshopApplication;
import com.github.kb.wxshop.entity.Response;
import com.github.kb.wxshop.generate.Goods;
import com.github.kb.wxshop.generate.Shop;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static javax.servlet.http.HttpServletResponse.SC_CREATED;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = WxshopApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class GoodsIntegrationTest extends AbstractIntegrationTest {

    @Test
    public void testCreateGoods() throws JsonProcessingException {
        UserLoginResponse loginResponse = loginAndGetCookie();

        Shop shop = new Shop();
        shop.setName("我的微信店铺");
        shop.setDescription("我的小店开张了");
        shop.setImgUrl("http://url");
        HttpResponse shopResponse = doHttpRequest(
                "/api/v1/shop", false, shop, loginResponse.cookie);
        Response<Shop> shopInResponse = objectMapper.readValue(shopResponse.body, new TypeReference<Response<Shop>>() {});

        Assertions.assertEquals(SC_CREATED, shopResponse.code);
        Assertions.assertEquals("我的微信店铺", shopInResponse.getData().getName());
        Assertions.assertEquals("我的小店开张了", shopInResponse.getData().getDescription());
        Assertions.assertEquals("ok", shopInResponse.getData().getStatus());
        Assertions.assertEquals(shopInResponse.getData().getOwnerUserId(), loginResponse.user.getId());

        Goods goods = new Goods();
        goods.setName("肥皂");
        goods.setDescription("纯天然无污染肥皂");
        goods.setDetails("好肥皂");
        goods.setImgUrl("http://url");
        goods.setPrice(1000L);
        goods.setStock(10);
        goods.setShopId(shopInResponse.getData().getId());

        HttpResponse response = doHttpRequest(
                "/api/v1/goods", false, goods, loginResponse.cookie);
        Response<Goods> goodsInResponse = objectMapper.readValue(response.body, new TypeReference<Response<Goods>>() {});
        Assertions.assertEquals(SC_CREATED, response.code);
        Assertions.assertEquals("肥皂", goodsInResponse.getData().getName());
        Assertions.assertEquals(shopInResponse.getData().getId(), goodsInResponse.getData().getShopId());
        Assertions.assertEquals("ok", goodsInResponse.getData().getStatus());
    }

    @Test
    public void testDeleteGoods() {

    }
}
