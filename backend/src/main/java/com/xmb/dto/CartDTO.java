package com.xmb.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 加入购物车请求DTO
 */
@Data
@ApiModel("加入购物车请求")
public class CartDTO {
    
    @NotNull(message = "商品ID不能为空")
    @ApiModelProperty(value = "商品ID", required = true)
    private Long goodsId;
    
    @Min(value = 1, message = "数量必须大于0")
    @ApiModelProperty(value = "数量", required = true)
    private Integer num;
}
