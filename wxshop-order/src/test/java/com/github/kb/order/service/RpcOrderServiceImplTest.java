package com.github.kb.order.service;


import com.github.kb.api.HttpException;
import com.github.kb.api.data.*;
import com.github.kb.api.generate.Order;
import com.github.kb.api.generate.OrderGoodsMapper;
import com.github.kb.api.generate.OrderMapper;
import com.github.kb.order.mapper.MyOrderMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.ClassicConfiguration;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.*;


class RpcOrderServiceImplTest {
    String databaseUrl = "jdbc:mysql://localhost:3307/order?useSSL=false&amp&allowPublicKeyRetrieval=true";
    String username = "root";
    String password = "root";

    RpcOrderServiceImpl rpcOrderService;

    SqlSession sqlSession;

    @BeforeEach
    public void setUp() throws IOException {
        //在每个测试开始前 执行一次flyway:clean flyway:migrate
        ClassicConfiguration conf = new ClassicConfiguration();
        conf.setDataSource(databaseUrl, username, password);
        Flyway flyway = new Flyway(conf);
        flyway.clean();
        flyway.migrate();

        String resources = "db/test-config.xml";
        InputStream inputStream = Resources.getResourceAsStream(resources);
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        sqlSession = sqlSessionFactory.openSession(true);

        rpcOrderService = new RpcOrderServiceImpl(
                sqlSession.getMapper(OrderMapper.class),
                sqlSession.getMapper(MyOrderMapper.class),
                sqlSession.getMapper(OrderGoodsMapper.class)
        );
    }

    @AfterEach
    public void cleanUp() {
        sqlSession.close();
    }

    @Test
    public void createOrderTest() {
        OrderInfo orderInfo = new OrderInfo();
        GoodsInfo goods1 = new GoodsInfo(1, 2);
        GoodsInfo goods2 = new GoodsInfo(2, 10);
        orderInfo.setGoods(Arrays.asList(goods1, goods2));

        Order order = new Order();
        order.setUserId(1L);
        order.setShopId(1L);
        order.setAddress("上海");
        order.setTotalPrice(10000L);

        Order orderWithId = rpcOrderService.createOrder(orderInfo, order);

        Assertions.assertNotNull(orderWithId.getId());

        Order orderInDB = rpcOrderService.getOrderById(orderWithId.getId());

        Assertions.assertEquals(1L, orderInDB.getUserId());
        Assertions.assertEquals(1L, orderInDB.getShopId());
        Assertions.assertEquals("上海", orderInDB.getAddress());
        Assertions.assertEquals(10000L, orderInDB.getTotalPrice());
        Assertions.assertEquals(DataStatus.PENDING.getName(), orderInDB.getStatus());
    }

    @Test
    public void getOrderByPageTest() {
        PageResponse<RpcOrderGoods> result = rpcOrderService.getOrder(1L, 2, 1, null);

        Assertions.assertEquals(2, result.getTotalPage());
        Assertions.assertEquals(2, result.getPageNum());
        Assertions.assertEquals(1, result.getPageSize());
        Assertions.assertEquals(1, result.getData().size());

        Order order = result.getData().get(0).getOrder();
        Assertions.assertEquals(2L, order.getId());
        Assertions.assertEquals(700, order.getTotalPrice());
        Assertions.assertEquals(1L, order.getUserId());
        Assertions.assertEquals(1L, order.getShopId());
        Assertions.assertEquals(DataStatus.PENDING.getName(), order.getStatus());
        Assertions.assertEquals("火星", order.getAddress());


        List<GoodsInfo> goodsInfos = result.getData().get(0).getGoods();
        Assertions.assertEquals(Arrays.asList(1L, 2L),
                goodsInfos.stream().map(GoodsInfo::getId).collect(toList()));
        Assertions.assertEquals(Arrays.asList(3, 4),
                goodsInfos.stream().map(GoodsInfo::getNumber).collect(toList()));

    }

    @Test
    public void updateOrderTest() {
        Order order = rpcOrderService.getOrderById(2L);
        order.setExpressCompany("中通");
        order.setExpressId("ZT123333");
        order.setStatus(DataStatus.DELIVERED.getName());

        RpcOrderGoods result = rpcOrderService.updateOrder(order);

        Assertions.assertEquals(2L, result.getOrder().getId());
        Assertions.assertEquals(700, result.getOrder().getTotalPrice());
        Assertions.assertEquals(1L, result.getOrder().getUserId());
        Assertions.assertEquals("中通", result.getOrder().getExpressCompany());
        Assertions.assertEquals("ZT123333", result.getOrder().getExpressId());
        Assertions.assertEquals(DataStatus.DELIVERED.getName(), result.getOrder().getStatus());
        Assertions.assertEquals("火星", result.getOrder().getAddress());

        List<GoodsInfo> goodsInfos = result.getGoods();
        Assertions.assertEquals(Arrays.asList(1L, 2L),
                goodsInfos.stream().map(GoodsInfo::getId).collect(toList()));
        Assertions.assertEquals(Arrays.asList(3, 4),
                goodsInfos.stream().map(GoodsInfo::getNumber).collect(toList()));

    }

    @Test
    public void deleteOrderTest() {
        RpcOrderGoods deleteOrder = rpcOrderService.deleteOrder(2L, 1L);

        Order order = deleteOrder.getOrder();
        Assertions.assertEquals(2L, order.getId());
        Assertions.assertEquals(700, order.getTotalPrice());
        Assertions.assertEquals(1L, order.getUserId());
        Assertions.assertEquals(1L, order.getShopId());
        Assertions.assertEquals(DataStatus.DELETED.getName(), order.getStatus());
        Assertions.assertEquals("火星", order.getAddress());


        List<GoodsInfo> goodsInfos = deleteOrder.getGoods();
        Assertions.assertEquals(Arrays.asList(1L, 2L),
                goodsInfos.stream().map(GoodsInfo::getId).collect(toList()));
        Assertions.assertEquals(Arrays.asList(3, 4),
                goodsInfos.stream().map(GoodsInfo::getNumber).collect(toList()));

    }

    @Test
    public void throwExceptionIfNotAuthorized() {
        HttpException exception = Assertions.assertThrows(HttpException.class, () -> {
            rpcOrderService.deleteOrder(2L, 0L);
        });
        Assertions.assertEquals(HttpURLConnection.HTTP_FORBIDDEN, exception.getStatusCode());
    }

    @Test
    public void canGetEmptyOrderList() {
        PageResponse<RpcOrderGoods> result = rpcOrderService.getOrder(8888L, 2, 1, null);
        Assertions.assertEquals(0, result.getData().size());
        Assertions.assertEquals(0, result.getTotalPage());
    }

}
