package com.xmb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xmb.entity.Orders;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单Mapper
 */
@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {
}
