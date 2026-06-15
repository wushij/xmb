package com.xmb.task;

import com.xmb.service.OrdersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 订单超时自动取消定时任务
 */
@Component
public class OrderTimeoutTask {
    
    private static final Logger log = LoggerFactory.getLogger(OrderTimeoutTask.class);
    
    @Autowired
    private OrdersService ordersService;
    
    /**
     * 每分钟执行一次，取消超时未支付订单
     */
    @Scheduled(cron = "0 * * * * ?")
    public void cancelTimeoutOrders() {
        try {
            int count = ordersService.cancelTimeoutOrders();
            if (count > 0) {
                log.info("自动取消超时订单 {} 个", count);
            }
        } catch (Exception e) {
            log.error("取消超时订单失败", e);
        }
    }
}
