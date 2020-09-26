package com.github.kb.wxshop.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.kb.api.data.*;
import com.github.kb.api.generate.Order;
import com.github.kb.wxshop.WxshopApplication;
import com.github.kb.wxshop.entity.GoodsWithNumber;
import com.github.kb.wxshop.entity.LoginResponse;
import com.github.kb.wxshop.entity.OrderResponse;
import com.github.kb.wxshop.entity.Response;
import com.github.kb.wxshop.generate.Goods;
import com.github.kb.wxshop.generate.Shop;
import com.github.kb.wxshop.mock.MockOrderRpcService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.matchers.Or;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.*;
import static java.util.stream.Collectors.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = WxshopApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"spring.config.location=classpath:test-application.yml"})
public class OrderIntegrationTest extends AbstractIntegrationTest {
    @Autowired
    MockOrderRpcService mockOrderRpcService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(mockOrderRpcService);
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

        orderInfo.setGoods(asList(goodsInfo1, goodsInfo2));

        when(mockOrderRpcService.orderRpcService.createOrder(any(), any())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Order order = invocationOnMock.getArgument(1);
                order.setId(123L);
                return order;
            }
        });

        Response<OrderResponse> response = doHttpRequest(
                "/api/v1/order", "POST", orderInfo, loginResponse.cookie)
                .asJsonObject(new TypeReference<Response<OrderResponse>>() {
                });

        //Assertions.assertEquals(123L, response.getData().getId());
        Assertions.assertEquals(2L, response.getData().getShop().getId());
        Assertions.assertEquals("shop2", response.getData().getShop().getName());
        Assertions.assertEquals(DataStatus.PENDING.getName(), response.getData().getStatus());
        Assertions.assertEquals("上海", response.getData().getAddress());
        Assertions.assertEquals(asList(4L, 5L),
                response.getData().getGoods().stream().map(Goods::getId).collect(toList()));
        Assertions.assertEquals(asList(3, 5),
                response.getData().getGoods().stream().map(GoodsWithNumber::getNumber).collect(toList()));
    }

    @Test
    public void canRollBackIfDeductStockFailed() throws JsonProcessingException {
        UserLoginResponse loginResponse = loginAndGetCookie();

        OrderInfo orderInfo = new OrderInfo();
        GoodsInfo goodsInfo1 = new GoodsInfo();
        GoodsInfo goodsInfo2 = new GoodsInfo();

        goodsInfo1.setId(4);
        goodsInfo1.setNumber(3);

        goodsInfo2.setId(5);
        goodsInfo2.setNumber(6);
        orderInfo.setGoods(asList(goodsInfo1, goodsInfo2));

        HttpResponse response = doHttpRequest("/api/v1/order", "POST", orderInfo, loginResponse.cookie);
        Assertions.assertEquals(HttpStatus.GONE.value(), response.code);
        //确保扣库存成功的回滚了
        canCreateOrder();
    }

    @Test
    public void canDeleteOrder() throws Exception {
        // 用户登陆
        UserLoginResponse loginResponse = loginAndGetCookie();

        when(mockOrderRpcService.orderRpcService.getOrder(anyLong(), anyInt(), anyInt(), any())).thenReturn(mockResponse());

        // 获取当前订单
        PageResponse<OrderResponse> orders = doHttpRequest("/api/v1/order?pageSize=2&pageNum=3", "GET", null, loginResponse.cookie)
                .asJsonObject(new TypeReference<PageResponse<OrderResponse>>() {
                });

        Assertions.assertEquals(3, orders.getPageNum());
        Assertions.assertEquals(2, orders.getPageSize());
        Assertions.assertEquals(10, orders.getTotalPage());
        Assertions.assertEquals(asList("shop2", "shop2"),
                orders.getData()
                        .stream()
                        .map(OrderResponse::getShop)
                        .map(Shop::getName)
                        .collect(toList()));

        Assertions.assertEquals(asList("goods3", "goods4"),
                orders.getData()
                        .stream()
                        .map(OrderResponse::getGoods)
                        .flatMap(List::stream)
                        .map(Goods::getName)
                        .collect(toList()));

        Assertions.assertEquals(asList(5, 3),
                orders.getData()
                        .stream()
                        .map(OrderResponse::getGoods)
                        .flatMap(List::stream)
                        .map(GoodsWithNumber::getNumber)
                        .collect(toList()));

        when(mockOrderRpcService.orderRpcService.deleteOrder(100L, 1L))
                .thenReturn(mockRpcOrderGoods(100, 1, 3, 2, 5, DataStatus.DELETED));

        // 删除某个订单
        Response<OrderResponse> deleteOrder = doHttpRequest("/api/v1/order/100", "DELETE", null, loginResponse.cookie)
                .assertOkStatusCode()
                .asJsonObject(new TypeReference<Response<OrderResponse>>() {
                });

        Assertions.assertEquals(DataStatus.DELETED.getName(), deleteOrder.getData().getStatus());
        Assertions.assertEquals(100L, deleteOrder.getData().getId());
        Assertions.assertEquals(1, deleteOrder.getData().getGoods().size());
        Assertions.assertEquals(3L, deleteOrder.getData().getGoods().get(0).getId());
        Assertions.assertEquals(5, deleteOrder.getData().getGoods().get(0).getNumber());


    }

    @Test
    public void return404IfOrderNotFound() throws Exception {
        UserLoginResponse loginResponse = loginAndGetCookie();
        Order order = new Order();
        order.setId(12345L);

        Assertions.assertEquals(HttpURLConnection.HTTP_NOT_FOUND,
                doHttpRequest("/api/v1/order/12345678", "PATCH", order, loginResponse.cookie).code);
    }

    private PageResponse<RpcOrderGoods> mockResponse() {
        RpcOrderGoods order1 = mockRpcOrderGoods(100, 1, 3, 2, 5, DataStatus.DELIVERED);
        RpcOrderGoods order2 = mockRpcOrderGoods(101, 1, 4, 3, 3, DataStatus.RECEIVED);

        return PageResponse.pagedData(3, 2, 10, asList(order1, order2));

    }

    private RpcOrderGoods mockRpcOrderGoods(long orderId,
                                            long userId,
                                            long goodsId,
                                            long shopId,
                                            int number,
                                            DataStatus status) {
        RpcOrderGoods orderGoods = new RpcOrderGoods();
        Order order = new Order();
        GoodsInfo goodsInfo = new GoodsInfo();

        goodsInfo.setId(goodsId);
        goodsInfo.setNumber(number);

        order.setId(orderId);
        order.setShopId(shopId);
        order.setStatus(status.getName());

        orderGoods.setGoods(asList(goodsInfo));
        orderGoods.setOrder(order);

        return orderGoods;

    }
}
