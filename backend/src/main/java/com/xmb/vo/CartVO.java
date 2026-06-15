package com.xmb.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 购物车商品VO
 */
@Data
@ApiModel("购物车商品")
public class CartVO implements Serializable {
    
    @ApiModelProperty("购物车ID")
    private Long id;
    
    @ApiModelProperty("商品ID")
    private Long goodsId;
    
    @ApiModelProperty("商品名称")
    private String goodsName;
    
    @ApiModelProperty("商品图片")
    private String goodsImage;
    
    @ApiModelProperty("商品单价")
    private BigDecimal goodsPrice;
    
    @ApiModelProperty("数量")
    private Integer num;
    
    @ApiModelProperty("小计金额")
    private BigDecimal totalPrice;
    
    @ApiModelProperty("是否选中")
    private Integer selected;
    
    @ApiModelProperty("库存")
    private Integer stock;
}
