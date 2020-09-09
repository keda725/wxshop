package com.github.kb.wxshop.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.kb.wxshop.WxshopApplication;
import com.github.kb.wxshop.entity.Response;
import com.github.kb.wxshop.generate.Goods;
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
        String cookie = loginAndGetCookie();
        Goods goods = new Goods();
        goods.setName("肥皂");
        goods.setDescription("纯天然无污染肥皂");
        goods.setDetails("好肥皂");
        goods.setImgUrl("http://url");
        goods.setPrice(1000L);
        goods.setStock(10);
        goods.setShopId(1L);

        HttpResponse response = doHttpRequest(
                "/api/v1/goods", false, goods, cookie);
        Response<Goods> responseData = objectMapper.readValue(response.body, new TypeReference<Response<Goods>>() {});
        Assertions.assertEquals(SC_CREATED, response.code);
        Assertions.assertEquals("肥皂", responseData.getData().getName());
    }

    @Test
    public void testDeleteGoods() {

    }
}
