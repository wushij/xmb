package com.xmb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xmb.entity.Goods;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品Mapper
 */
@Mapper
public interface GoodsMapper extends BaseMapper<Goods> {
}
