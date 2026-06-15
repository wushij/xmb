package com.xmb.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单商品实体类
 */
@Data
@TableName("order_item")
public class OrderItem implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long orderId;
    
    private Long goodsId;
    
    private String goodsName;
    
    private String goodsImage;
    
    private BigDecimal goodsPrice;
    
    private Integer num;
    
    private BigDecimal totalPrice;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
