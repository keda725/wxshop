package com.github.kb.api.data;

import java.io.Serializable;
import java.util.List;

public class OrderInfo implements Serializable {
    private List<GoodsInfo> goods;
    private long orderId;

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public List<GoodsInfo> getGoods() {
        return goods;
    }

    public void setGoods(List<GoodsInfo> goods) {
        this.goods = goods;
    }
}
