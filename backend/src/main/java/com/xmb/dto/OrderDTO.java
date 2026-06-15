package com.xmb.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 创建订单DTO
 */
@Data
@ApiModel("创建订单请求")
public class OrderDTO {
    
    @NotNull(message = "地址ID不能为空")
    @ApiModelProperty(value = "地址ID", required = true)
    private Long addressId;
    
    @ApiModelProperty(value = "配送方式：0-配送 1-自提，默认0")
    private Integer deliveryType = 0;
    
    @ApiModelProperty(value = "订单备注")
    private String remark;
    
    @ApiModelProperty(value = "购物车ID列表（为空则使用全部选中的购物车商品）")
    private List<Long> cartIds;
}
