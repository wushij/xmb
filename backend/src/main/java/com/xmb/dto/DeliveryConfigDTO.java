package com.xmb.dto;

import lombok.Data;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 配送配置DTO
 */
@Data
public class DeliveryConfigDTO {
    
    /**
     * 配送费
     */
    @NotNull(message = "配送费不能为空")
    @DecimalMin(value = "0", message = "配送费不能为负数")
    private BigDecimal deliveryFee;
    
    /**
     * 满免配送费门槛
     */
    @NotNull(message = "满免配送费门槛不能为空")
    @DecimalMin(value = "0", message = "满免配送费门槛不能为负数")
    private BigDecimal freeDeliveryThreshold;
    
    /**
     * 起送价格
     */
    @NotNull(message = "起送价格不能为空")
    @DecimalMin(value = "0", message = "起送价格不能为负数")
    private BigDecimal minOrderAmount;
    
    /**
     * 自提地址
     */
    private String pickupAddress;
}
