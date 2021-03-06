package com.github.kb.wxshop.controller;

import com.github.kb.api.HttpException;
import com.github.kb.api.data.DataStatus;
import com.github.kb.api.data.OrderInfo;
import com.github.kb.api.generate.Order;
import com.github.kb.wxshop.entity.OrderResponse;
import com.github.kb.api.data.PageResponse;
import com.github.kb.wxshop.entity.Response;
import com.github.kb.wxshop.service.OrderService;
import com.github.kb.wxshop.service.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }
    // @formatter:off

    /**
     * @api {get} /order 获取当前用户名下的所有订单
     * @apiName GetOrder
     * @apiGroup 订单
     * @apiHeader {String} Accept application/json
     * @apiParam {Number} pageNum 页数，从1开始
     * @apiParam {Number} pageSize 每页显示的数量
     * @apiParam {String=PENDING/PAID/DELIVERED/RECEIVED} [status] 订单状态：PENDING 待付款 PAID 已付款 DELIVERED 物流中 RECEIVED 已收货
     * @apiSuccess {Number} pageNum 页数，从1开始
     * @apiSuccess {Number} pageSize 每页显示的数量
     * @apiSuccess {Number} totalPage 共有多少页
     * @apiSuccess {Order} data 订单列表
     * @apiSuccessExample Success-Response:
     * HTTP/1.1 200 OK
     * {
     * "pageNum": 1,
     * "pageSize": 10,
     * "totalPage": 5,
     * "data": [
     * {
     * "id": 12345,
     * "expressCompany": null,
     * "expressId": null,
     * "status": "PENDING",
     * "totalPrice": 10000,
     * "address": "XXX",
     * "shop": {
     * "id": 12345,
     * "name": "我的店铺",
     * "description": "我的苹果专卖店",
     * "imgUrl": "https://img.url",
     * "ownerUserId": 12345,
     * "createdAt": "2020-08-22T13:22:03Z",
     * "updatedAt": "2020-08-22T13:22:03Z"
     * },
     * "goods": [
     * {
     * "id": 12345,
     * "name": "肥皂",
     * "description": "纯天然无污染肥皂",
     * "details": "这是一块好肥皂",
     * "imgUrl": "https://img.url",
     * "address": "XXX",
     * "price": 500,
     * "number": 10,
     * "createdAt": "2020-08-22T13:22:03Z",
     * "updatedAt": "2020-08-22T13:22:03Z"
     * },
     * {
     * ...
     * }
     * ]
     * },
     * {
     * ...
     * }
     * ]
     * }
     * @apiError 401 Unauthorized 若用户未登录
     * @apiErrorExample Error-Response:
     * HTTP/1.1 401 Unauthorized
     * {
     * "message": "Unauthorized"
     * }
     */
    /**
     * 获取订单
     *
     * @param pageNum  页数
     * @param pageSize 条数
     * @param status   状态
     * @return 响应
     */
    // @formatter:on
    @GetMapping("/order")
    public PageResponse<OrderResponse> getOrder(@RequestParam("pageNum") Integer pageNum,
                                                @RequestParam("pageSize") Integer pageSize,
                                                @RequestParam(value = "status", required = false) String status) {
        if (status != null && DataStatus.formStatus(status) == null) {
            throw HttpException.badRequest("非法status" + status);
        }
        return orderService.getOrder(UserContext.getCurrentUser().getId(), pageNum, pageSize, DataStatus.formStatus(status));
    }

    // @formatter:off
    /**
     * @api {post} /order 下订单
     * @apiName CreateOrder
     * @apiGroup 订单
     *
     * @apiHeader {String} Accept application/json
     * @apiHeader {String} Content-Type application/json
     * @apiParamExample {json} Request-Example:
     *            {
     *              "goods": [
     *                {
     *                    "id": 12345,
     *                    "number": 10,
     *                },
     *                {
     *                    ...
     *                }
     *            }
     *
     * @apiSuccess {Order} data 刚刚创建完成的订单
     *
     * @apiSuccessExample Success-Response:
     *     HTTP/1.1 201 Created
     *     {
     *       "data": {
     *           "id": 12345,
     *           "expressCompany": null,
     *           "expressId": null,
     *           "status": "PENDING",
     *           "address": "XXX",
     *           "shop": {
     *              "id": 12345,
     *              "name": "我的店铺",
     *              "description": "我的苹果专卖店",
     *              "imgUrl": "https://img.url",
     *              "ownerUserId": 12345,
     *              "createdAt": "2020-08-22T13:22:03Z",
     *              "updatedAt": "2020-08-22T13:22:03Z"
     *            },
     *            "goods": [
     *              {
     *                  "id": 12345,
     *                  "name": "肥皂",
     *                  "description": "纯天然无污染肥皂",
     *                  "details": "这是一块好肥皂",
     *                  "imgUrl": "https://img.url",
     *                  "address": "XXX",
     *                  "price": 500,
     *                  "number": 10,
     *                  "createdAt": "2020-08-22T13:22:03Z",
     *                  "updatedAt": "2020-08-22T13:22:03Z"
     *              },
     *              {
     *                    ...
     *              }
     *           ]
     *         }
     *     }
     *
     * @apiError 400 Bad Request 若用户的请求中包含错误
     * @apiError 401 Unauthorized 若用户未登录
     * @apiError 404 Not Found 若商品未找到
     *
     * @apiErrorExample Error-Response:
     *     HTTP/1.1 400 Bad Request
     *     {
     *       "message": "商品已经售完"
     *     }
     */
    /**
     * @param orderInfo
     * @return 响应
     */
    /**
     * 创建订单
     *
     * @param orderInfo orderInfo
     * @return 响应
     */
    // @formatter:on
    @PostMapping("/order")
    public Response<OrderResponse> createOrder(@RequestBody OrderInfo orderInfo) {
        orderService.deductStock(orderInfo);
        return Response.of(orderService.createOrder(orderInfo, UserContext.getCurrentUser().getId()));
    }

    // @formatter:off

    /**
     * @api {PATCH} /order/:id 更新订单(只能更新物流信息/签收状态)
     * @apiName UpdateOrder
     * @apiGroup 订单
     * @apiHeader {String} Accept application/json
     * @apiHeader {String} Content-Type application/json
     * @apiParam {Number} id 订单ID
     * @apiParamExample {json} Request-Example:
     * {
     * "id": 12345,
     * "expressCompany": "圆通",
     * "expressId": "YTO1234",
     * }
     * {
     * "id": 12345,
     * "status": "RECEIVED"
     * }
     * @apiSuccess {Order} data 更新后的订单
     * @apiSuccessExample Success-Response:
     * HTTP/1.1 200 OK
     * {
     * "data": {
     * "id": 12345,
     * "expressCompany": null,
     * "expressId": null,
     * "status": "PENDING",
     * "address": "XXX",
     * "shop": {
     * "id": 12345,
     * "name": "我的店铺",
     * "description": "我的苹果专卖店",
     * "imgUrl": "https://img.url",
     * "ownerUserId": 12345,
     * "createdAt": "2020-08-22T13:22:03Z",
     * "updatedAt": "2020-08-22T13:22:03Z"
     * },
     * "goods": [
     * {
     * "id": 12345,
     * "name": "肥皂",
     * "description": "纯天然无污染肥皂",
     * "details": "这是一块好肥皂",
     * "imgUrl": "https://img.url",
     * "address": "XXX",
     * "price": 500,
     * "number": 10,
     * "createdAt": "2020-08-22T13:22:03Z",
     * "updatedAt": "2020-08-22T13:22:03Z"
     * },
     * {
     * ...
     * }
     * ]
     * }
     * }
     * @apiError 400 Bad Request 若用户的请求中包含错误
     * @apiError 401 Unauthorized 若用户未登录
     * @apiError 403 Forbidden 若用户修改非自己店铺的订单
     * @apiError 404 Not Found 若订单未找到
     * @apiErrorExample Error-Response:
     * HTTP/1.1 404 Not Found
     * {
     * "message": "Not Found"
     * }
     */
    /**
     * 更新订单
     *
     * @param id    订单编号
     * @param order 订单
     * @return 响应
     */
    // @formatter:on
    @RequestMapping(value = "/order/{id}", method = {RequestMethod.PATCH, RequestMethod.POST})
    public Response<OrderResponse> updateOrder(@PathVariable("id") long id, @RequestBody Order order) {
        if (order.getExpressCompany() != null) {
            return Response.of(orderService.updateExpressInformation(order, UserContext.getCurrentUser().getId()));
        } else {
            return Response.of(orderService.updateOrderStatus(order, UserContext.getCurrentUser().getId()));
        }

    }

    // @formatter:off

    /**
     * @api {DELETE} /order/:id 删除订单
     * @apiName DeleteOrder
     * @apiGroup 订单
     * @apiHeader {String} Accept application/json
     * @apiSuccess {Order} data 刚刚删除的订单
     * @apiSuccessExample Success-Response:
     * HTTP/1.1 204 No Content
     * {
     * "data": {
     * "id": 12345,
     * "expressCompany": null,
     * "expressId": null,
     * "status": "PENDING",
     * "address": "XXX",
     * "shop": {
     * "id": 12345,
     * "name": "我的店铺",
     * "description": "我的苹果专卖店",
     * "imgUrl": "https://img.url",
     * "ownerUserId": 12345,
     * "createdAt": "2020-08-22T13:22:03Z",
     * "updatedAt": "2020-08-22T13:22:03Z"
     * },
     * "goods": [
     * {
     * "id": 12345,
     * "name": "肥皂",
     * "description": "纯天然无污染肥皂",
     * "details": "这是一块好肥皂",
     * "imgUrl": "https://img.url",
     * "address": "XXX",
     * "price": 500,
     * "number": 10,
     * "createdAt": "2020-08-22T13:22:03Z",
     * "updatedAt": "2020-08-22T13:22:03Z"
     * },
     * {
     * ...
     * }
     * ]
     * }
     * }
     * @apiError 400 Bad Request 若用户的请求中包含错误
     * @apiError 401 Unauthorized 若用户未登录
     * @apiError 403 Forbidden 若用户删除非自己订单
     * @apiError 404 Not Found 若订单未找到
     * @apiErrorExample Error-Response:
     * HTTP/1.1 404 Not Found
     * {
     * "message": "Not Found"
     * }
     */
    /**
     * 删除订单
     *
     * @param orderId 订单编号
     * @return 响应
     */
    // @formatter:on
    @DeleteMapping("/order/{id}")
    public Response<OrderResponse> deleteOrder(@PathVariable("id") long orderId) {
        return Response.of(orderService.deleteOrder(orderId, UserContext.getCurrentUser().getId()));
    }
}

