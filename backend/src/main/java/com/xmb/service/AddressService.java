package com.xmb.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xmb.dto.AddressDTO;
import com.xmb.entity.Address;
import java.util.List;

/**
 * 地址服务接口
 */
public interface AddressService extends IService<Address> {
    
    /**
     * 获取用户地址列表
     */
    List<Address> listByUser();
    
    /**
     * 获取用户默认地址
     */
    Address getDefaultAddress();
    
    /**
     * 添加地址
     */
    void addAddress(AddressDTO dto);
    
    /**
     * 更新地址
     */
    void updateAddress(Long id, AddressDTO dto);
    
    /**
     * 删除地址
     */
    void removeAddress(Long id);
    
    /**
     * 设置默认地址
     */
    void setDefault(Long id);
}
