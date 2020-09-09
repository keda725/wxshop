package com.github.kb.wxshop.service;

import com.github.kb.wxshop.entity.DataStatus;
import com.github.kb.wxshop.entity.PageResponse;
import com.github.kb.wxshop.generate.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoodsServiceTest {
    @Mock
    private GoodsMapper goodsMapper;
    @Mock
    private ShopMapper shopMapper;
    @Mock
    private Shop shop;

    @Mock
    private Goods goods;

    @InjectMocks
    private GoodsService goodsService;

    @BeforeEach
    public void setUp() {
        User user = new User();
        user.setId(1L);
        UserContext.setCurrentUser(user);

        lenient().when(shopMapper.selectByPrimaryKey(anyLong())).thenReturn(shop);

    }

    @AfterEach
    public void clear() {
        UserContext.setCurrentUser(null);
    }

    @Test
    public void createGoodsSuccessIfUserIsOwner() {
        when(shop.getOwnerUserId()).thenReturn(1L);
        when(goodsMapper.insert(goods)).thenReturn(123);

        assertEquals(goods, goodsService.createGoods(goods));

        verify(goods).setId(123L);
    }

    @Test
    public void createGoodsSuccessIfUserIsNotOwner() {
        when(shop.getOwnerUserId()).thenReturn(2L);
        assertThrows(GoodsService.NoAuthorizedForShopException.class, () -> {
            goodsService.createGoods(goods);
        });
    }

    @Test
    public void throwExceptionIfGoodsNotFound() {
        long goodsToBeDeleted = 123;
        when(shop.getOwnerUserId()).thenReturn(1L);
        when(goodsMapper.selectByPrimaryKey(goodsToBeDeleted)).thenReturn(null);
        assertThrows(GoodsService.ResourceNotFoundException.class, () -> {
            goodsService.deleteGoodsById(goodsToBeDeleted);
        });
    }

    @Test
    public void deleteGoodsThrowExceptionIfUserIsNotOwner() {
        long goodsToBeDeleted = 123;
        when(shop.getOwnerUserId()).thenReturn(2L);
        assertThrows(GoodsService.NoAuthorizedForShopException.class, () -> {
            goodsService.deleteGoodsById(goodsToBeDeleted);
        });
    }

    @Test
    public void deleteGoodsSucceed() {
        long goodsToBeDeleted = 123;
        when(shop.getOwnerUserId()).thenReturn(1L);
        when(goodsMapper.selectByPrimaryKey(goodsToBeDeleted)).thenReturn(goods);
        goodsService.deleteGoodsById(goodsToBeDeleted);

        verify(goods).setStatus(DataStatus.DELETED.getName());

    }

    @Test
    public void getGoodsSucceedWithNullShopId() {
        int pageNumber = 5;
        int pageSize = 10;

        List<Goods> mockData = Mockito.mock(List.class);

        when(goodsMapper.countByExample(any())).thenReturn(55L);
        when(goodsMapper.selectByExample(any())).thenReturn(mockData);
        PageResponse<Goods> result = goodsService.getGoods(pageNumber, pageSize, null);

        assertEquals(6, result.getTotalPage());
        assertEquals(5, result.getPageNum());
        assertEquals(10, result.getPageSize());
        assertEquals(mockData, result.getData());


    }

    @Test
    public void getGoodsSucceedWithNonNullShopId() {
        int pageNumber = 5;
        int pageSize = 10;

        List<Goods> mockData = Mockito.mock(List.class);

        when(goodsMapper.countByExample(any())).thenReturn(100L);
        when(goodsMapper.selectByExample(any())).thenReturn(mockData);
        PageResponse<Goods> result = goodsService.getGoods(pageNumber, pageSize, 456);

        assertEquals(10, result.getTotalPage());
        assertEquals(5, result.getPageNum());
        assertEquals(10, result.getPageSize());
        assertEquals(mockData, result.getData());

    }

    @Test
    public void updateGoodsSucceed() {
        when(shop.getOwnerUserId()).thenReturn(1L);
        when(goodsMapper.updateByExample(any(), any())).thenReturn(1);
        assertEquals(goods, goodsService.updateGoods(goods));
    }

}
