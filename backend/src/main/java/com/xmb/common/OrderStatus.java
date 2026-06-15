package com.xmb.common;

/**
 * 订单状态常量
 */
public class OrderStatus {
    
    public static final int WAIT_PAY = 0;       // 待支付
    public static final int PAID = 1;           // 已支付(商家备货中)
    public static final int DELIVERING = 2;     // 配送中/待取货
    public static final int COMPLETED = 3;      // 已完成
    public static final int CANCELLED = 4;      // 已取消
    public static final int REFUNDING = 5;      // 退款中
    public static final int REFUNDED = 6;       // 已退款
    
    public static String getStatusName(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case WAIT_PAY: return "待支付";
            case PAID: return "已支付";
            case DELIVERING: return "配送中";
            case COMPLETED: return "已完成";
            case CANCELLED: return "已取消";
            case REFUNDING: return "退款中";
            case REFUNDED: return "已退款";
            default: return "未知";
        }
    }
    
    /**
     * 根据配送方式获取用户端显示的状态名称
     */
    public static String getUserStatusName(Integer status, Integer deliveryType) {
        if (status == null) return "未知";
        boolean isPickup = deliveryType != null && deliveryType == 1;
        switch (status) {
            case WAIT_PAY: return "待支付";
            case PAID: return "商家备货中";
            case DELIVERING: return isPickup ? "待取货" : "配送中";
            case COMPLETED: return "已完成";
            case CANCELLED: return "已取消";
            case REFUNDING: return "退款中";
            case REFUNDED: return "已退款";
            default: return "未知";
        }
    }
}
