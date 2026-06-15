package com.xmb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xmb.entity.Cart;
import org.apache.ibatis.annotations.Mapper;

/**
 * 购物车Mapper
 */
@Mapper
public interface CartMapper extends BaseMapper<Cart> {
}
