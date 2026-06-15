package com.xmb.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 商品DTO
 */
@Data
@ApiModel("商品请求")
public class GoodsDTO {
    
    @ApiModelProperty("商品ID（编辑时必填）")
    private Long id;
    
    @ApiModelProperty(value = "商品名称", required = true)
    @NotBlank(message = "商品名称不能为空")
    private String name;
    
    @ApiModelProperty(value = "分类ID", required = true)
    @NotNull(message = "请选择分类")
    private Long categoryId;
    
    @ApiModelProperty(value = "现价", required = true)
    @NotNull(message = "请输入现价")
    private BigDecimal nowPrice;
    
    @ApiModelProperty("原价")
    private BigDecimal oldPrice;
    
    @ApiModelProperty(value = "库存", required = true)
    @NotNull(message = "请输入库存")
    private Integer stock;
    
    @ApiModelProperty("商品图片")
    private String image;
    
    @ApiModelProperty("状态：0-下架 1-上架")
    private Integer status;
    
    @ApiModelProperty("商品描述")
    private String description;
}
