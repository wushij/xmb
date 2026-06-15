package com.xmb.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单详情VO
 */
@Data
@ApiModel("订单详情")
public class OrderVO implements Serializable {
    
    @ApiModelProperty("订单ID")
    private Long id;
    
    @ApiModelProperty("订单编号")
    private String orderNo;
    
    @ApiModelProperty("订单总价")
    private BigDecimal totalPrice;
    
    @ApiModelProperty("配送费")
    private BigDecimal deliveryFee;
    
    @ApiModelProperty("商品金额")
    private BigDecimal goodsPrice;
    
    @ApiModelProperty("订单状态：0-待付款 1-待发货 2-待收货 3-已完成 4-已取消 5-已退款")
    private Integer status;
    
    @ApiModelProperty("订单状态名称")
    private String statusName;
    
    @ApiModelProperty("收货地址")
    private String address;
    
    @ApiModelProperty("配送方式：0-配送 1-自提")
    private Integer deliveryType;
    
    @ApiModelProperty("配送方式名称")
    private String deliveryTypeName;
    
    @ApiModelProperty("取货地址")
    private String pickupAddress;
    
    @ApiModelProperty("收货人")
    private String receiverName;
    
    @ApiModelProperty("联系电话")
    private String receiverPhone;
    
    @ApiModelProperty("订单备注")
    private String remark;
    
    @ApiModelProperty("下单时间")
    private LocalDateTime createTime;
    
    @ApiModelProperty("支付时间")
    private LocalDateTime payTime;
    
    @ApiModelProperty("发货时间")
    private LocalDateTime deliveryTime;
    
    @ApiModelProperty("收货时间")
    private LocalDateTime receiveTime;
    
    @ApiModelProperty("订单商品列表")
    private List<OrderItemVO> items;
    
    /**
     * 订单商品VO
     */
    @Data
    @ApiModel("订单商品")
    public static class OrderItemVO implements Serializable {
        
        @ApiModelProperty("商品ID")
        private Long goodsId;
        
        @ApiModelProperty("商品名称")
        private String goodsName;
        
        @ApiModelProperty("商品图片")
        private String goodsImage;
        
        @ApiModelProperty("商品单价")
        private BigDecimal goodsPrice;
        
        @ApiModelProperty("购买数量")
        private Integer num;
        
        @ApiModelProperty("小计金额")
        private BigDecimal totalPrice;
    }
}
