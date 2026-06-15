package com.xmb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xmb.entity.Address;
import org.apache.ibatis.annotations.Mapper;

/**
 * 地址Mapper
 */
@Mapper
public interface AddressMapper extends BaseMapper<Address> {
}
