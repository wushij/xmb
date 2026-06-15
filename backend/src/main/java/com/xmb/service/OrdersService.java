package com.xmb.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xmb.dto.OrderDTO;
import com.xmb.entity.Orders;
import com.xmb.vo.OrderVO;

/**
 * 订单服务接口
 */
public interface OrdersService extends IService<Orders> {
    
    /**
     * 创建订单
     */
    Orders createOrder(OrderDTO dto);
    
    /**
     * 分页查询订单
     */
    Page<OrderVO> pageOrders(Page<Orders> page, Integer status);
    
    /**
     * 获取订单详情
     */
    OrderVO getOrderDetail(Long id);
    
    /**
     * 取消订单
     */
    void cancelOrder(Long id);
    
    /**
     * 支付订单
     */
    void payOrder(Long id);
    
    /**
     * 确认收货
     */
    void confirmReceive(Long id);
    
    /**
     * 申请退款
     */
    void applyRefund(Long id);
    
    /**
     * 同意退款(商家后台操作)
     */
    void approveRefund(Long id);
    
    /**
     * 获取订单数量统计
     */
    int[] getOrderCount();
    
    /**
     * 取消超时未支付订单
     * @return 取消的订单数量
     */
    int cancelTimeoutOrders();
}
