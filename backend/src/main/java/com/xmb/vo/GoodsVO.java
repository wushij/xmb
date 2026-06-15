package com.xmb.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品VO
 */
@Data
@ApiModel("商品信息")
public class GoodsVO implements Serializable {
    
    @ApiModelProperty("商品ID")
    private Long id;
    
    @ApiModelProperty("分类ID")
    private Long categoryId;
    
    @ApiModelProperty("分类名称")
    private String categoryName;
    
    @ApiModelProperty("商品名称")
    private String name;
    
    @ApiModelProperty("商品图片")
    private String image;
    
    @ApiModelProperty("现价")
    private BigDecimal nowPrice;
    
    @ApiModelProperty("原价")
    private BigDecimal oldPrice;
    
    @ApiModelProperty("折扣标签")
    private String discount;
    
    @ApiModelProperty("库存")
    private Integer stock;
    
    @ApiModelProperty("销量")
    private Integer sales;
    
    @ApiModelProperty("商品描述")
    private String description;
}
