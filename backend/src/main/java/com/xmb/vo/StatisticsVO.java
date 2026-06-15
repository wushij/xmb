package com.xmb.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 统计数据VO
 */
@Data
@ApiModel("统计数据")
public class StatisticsVO implements Serializable {
    
    @ApiModelProperty("今日订单数")
    private Integer todayOrderCount;
    
    @ApiModelProperty("今日销售额")
    private BigDecimal todaySalesAmount;
    
    @ApiModelProperty("总订单数")
    private Integer totalOrderCount;
    
    @ApiModelProperty("总销售额")
    private BigDecimal totalSalesAmount;
    
    @ApiModelProperty("商品总数")
    private Integer totalGoodsCount;
    
    @ApiModelProperty("分类总数")
    private Integer totalCategoryCount;
}
