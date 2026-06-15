package com.xmb.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 销售对比数据VO
 */
@Data
@ApiModel("销售对比数据")
public class SalesCompareVO implements Serializable {
    
    // ========== 日对比 ==========
    @ApiModelProperty("今日销售额")
    private BigDecimal todayAmount;
    
    @ApiModelProperty("昨日销售额")
    private BigDecimal yesterdayAmount;
    
    @ApiModelProperty("日环比增长率（百分比）")
    private BigDecimal dayGrowthRate;
    
    // ========== 月对比 ==========
    @ApiModelProperty("本月销售额")
    private BigDecimal monthAmount;
    
    @ApiModelProperty("上月销售额")
    private BigDecimal lastMonthAmount;
    
    @ApiModelProperty("月环比增长率（百分比）")
    private BigDecimal monthGrowthRate;
    
    // ========== 年对比 ==========
    @ApiModelProperty("今年销售额")
    private BigDecimal yearAmount;
    
    @ApiModelProperty("去年销售额")
    private BigDecimal lastYearAmount;
    
    @ApiModelProperty("年同比增长率（百分比）")
    private BigDecimal yearGrowthRate;
    
    // ========== 订单数对比 ==========
    @ApiModelProperty("今日订单数")
    private Integer todayOrderCount;
    
    @ApiModelProperty("昨日订单数")
    private Integer yesterdayOrderCount;
    
    @ApiModelProperty("日订单环比增长率（百分比）")
    private BigDecimal dayOrderGrowthRate;
    
    @ApiModelProperty("本月订单数")
    private Integer monthOrderCount;
    
    @ApiModelProperty("上月订单数")
    private Integer lastMonthOrderCount;
    
    @ApiModelProperty("月订单环比增长率（百分比）")
    private BigDecimal monthOrderGrowthRate;
}
