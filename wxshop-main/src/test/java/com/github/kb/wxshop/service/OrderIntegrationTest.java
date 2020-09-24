package com.github.kb.wxshop.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.kb.api.DataStatus;
import com.github.kb.api.data.GoodsInfo;
import com.github.kb.api.data.OrderInfo;
import com.github.kb.api.generate.Order;
import com.github.kb.wxshop.WxshopApplication;
import com.github.kb.wxshop.entity.GoodsWithNumber;
import com.github.kb.wxshop.entity.OrderResponse;
import com.github.kb.wxshop.entity.Response;
import com.github.kb.wxshop.generate.Goods;
import com.github.kb.wxshop.mock.MockRpcOrderService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;

import static java.util.stream.Collectors.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = WxshopApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"spring.config.location=classpath:test-application.yml"})
public class OrderIntegrationTest extends AbstractIntegrationTest {
    @Autowired
    MockRpcOrderService mockRpcOrderService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(mockRpcOrderService);
        when(mockRpcOrderService.orderRpcService.createOrder(any(), any())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Order order = invocationOnMock.getArgument(1);
                order.setId(1234L);
                return order;
            }
        });
    }

    @Test
    public void canCreateOrder() throws JsonProcessingException {
        UserLoginResponse loginResponse = loginAndGetCookie();

        OrderInfo orderInfo = new OrderInfo();
        GoodsInfo goodsInfo1 = new GoodsInfo();
        GoodsInfo goodsInfo2 = new GoodsInfo();

        goodsInfo1.setId(4);
        goodsInfo1.setNumber(3);

        goodsInfo2.setId(5);
        goodsInfo2.setNumber(5);

        orderInfo.setGoods(Arrays.asList(goodsInfo1, goodsInfo2));


        Response<OrderResponse> response = doHttpRequest(
                "/api/v1/order", "POST", orderInfo, loginResponse.cookie)
                .asJsonObject(new TypeReference<Response<OrderResponse>>() {
                });

        Assertions.assertEquals(1234L, response.getData().getId());
        Assertions.assertEquals(2L, response.getData().getShop().getId());
        Assertions.assertEquals("shop2", response.getData().getShop().getName());
        Assertions.assertEquals(DataStatus.PENDING.getName(), response.getData().getStatus());
        Assertions.assertEquals("上海", response.getData().getAddress());
        Assertions.assertEquals(Arrays.asList(4L, 5L),
                response.getData().getGoods().stream().map(Goods::getId).collect(toList()));
        Assertions.assertEquals(Arrays.asList(3, 5),
                response.getData().getGoods().stream().map(GoodsWithNumber::getNumber).collect(toList()));

    }

    @Test
    public void canRollBackIfDeductStockFailed() {


    }
}
