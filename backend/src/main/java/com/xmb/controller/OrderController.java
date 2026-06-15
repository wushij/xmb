package com.xmb.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xmb.common.Result;
import com.xmb.dto.OrderDTO;
import com.xmb.entity.Orders;
import com.xmb.service.OrdersService;
import com.xmb.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

/**
 * 订单控制器
 */
@Api(tags = "订单接口")
@RestController
@RequestMapping("/api/order")
public class OrderController {
    
    @Autowired
    private OrdersService ordersService;
    
    @ApiOperation("创建订单")
    @PostMapping
    public Result<Orders> create(@Valid @RequestBody OrderDTO dto) {
        return Result.success(ordersService.createOrder(dto));
    }
    
    @ApiOperation("分页查询订单")
    @GetMapping("/page")
    public Result<Page<OrderVO>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status) {
        Page<Orders> page = new Page<>(pageNum, pageSize);
        return Result.success(ordersService.pageOrders(page, status));
    }
    
    @ApiOperation("获取订单详情")
    @GetMapping("/{id}")
    public Result<OrderVO> detail(@PathVariable Long id) {
        return Result.success(ordersService.getOrderDetail(id));
    }
    
    @ApiOperation("取消订单")
    @PutMapping("/cancel/{id}")
    public Result<Void> cancel(@PathVariable Long id) {
        ordersService.cancelOrder(id);
        return Result.success();
    }
    
    @ApiOperation("支付订单")
    @PutMapping("/pay/{id}")
    public Result<Void> pay(@PathVariable Long id) {
        ordersService.payOrder(id);
        return Result.success();
    }
    
    @ApiOperation("确认收货")
    @PutMapping("/receive/{id}")
    public Result<Void> receive(@PathVariable Long id) {
        ordersService.confirmReceive(id);
        return Result.success();
    }
    
    @ApiOperation("申请退款")
    @PutMapping("/refund/{id}")
    public Result<Void> refund(@PathVariable Long id) {
        ordersService.applyRefund(id);
        return Result.success();
    }
    
    @ApiOperation("获取订单数量统计")
    @GetMapping("/count")
    public Result<int[]> count() {
        return Result.success(ordersService.getOrderCount());
    }
}
