package com.xmb.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xmb.dto.AddressDTO;
import com.xmb.entity.Address;
import com.xmb.exception.BusinessException;
import com.xmb.mapper.AddressMapper;
import com.xmb.service.AddressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * 地址服务实现类
 */
@Service
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address> implements AddressService {
    
    @Override
    public List<Address> listByUser() {
        Long userId = StpUtil.getLoginIdAsLong();
        return list(new LambdaQueryWrapper<Address>()
                .eq(Address::getUserId, userId)
                .orderByDesc(Address::getIsDefault)
                .orderByDesc(Address::getCreateTime));
    }
    
    @Override
    public Address getDefaultAddress() {
        Long userId = StpUtil.getLoginIdAsLong();
        return getOne(new LambdaQueryWrapper<Address>()
                .eq(Address::getUserId, userId)
                .eq(Address::getIsDefault, 1));
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addAddress(AddressDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        Address address = new Address();
        address.setUserId(userId);
        address.setName(dto.getName());
        address.setPhone(dto.getPhone());
        address.setProvince(dto.getProvince());
        address.setCity(dto.getCity());
        address.setDistrict(dto.getDistrict());
        address.setDetail(dto.getDetail());
        
        // 如果是第一个地址，设为默认
        long count = count(new LambdaQueryWrapper<Address>().eq(Address::getUserId, userId));
        address.setIsDefault(count == 0 ? 1 : (dto.getIsDefault() != null ? dto.getIsDefault() : 0));
        
        // 如果设为默认，先取消其他默认地址
        if (address.getIsDefault() == 1) {
            update(new LambdaUpdateWrapper<Address>()
                    .eq(Address::getUserId, userId)
                    .set(Address::getIsDefault, 0));
        }
        
        save(address);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAddress(Long id, AddressDTO dto) {
        Address address = getById(id);
        if (address == null) {
            throw new BusinessException("地址不存在");
        }
        
        Long userId = StpUtil.getLoginIdAsLong();
        if (!address.getUserId().equals(userId)) {
            throw new BusinessException("无权操作");
        }
        
        address.setName(dto.getName());
        address.setPhone(dto.getPhone());
        address.setProvince(dto.getProvince());
        address.setCity(dto.getCity());
        address.setDistrict(dto.getDistrict());
        address.setDetail(dto.getDetail());
        
        if (dto.getIsDefault() != null && dto.getIsDefault() == 1) {
            // 取消其他默认地址
            update(new LambdaUpdateWrapper<Address>()
                    .eq(Address::getUserId, userId)
                    .set(Address::getIsDefault, 0));
            address.setIsDefault(1);
        }
        
        updateById(address);
    }
    
    @Override
    public void removeAddress(Long id) {
        Address address = getById(id);
        if (address == null) {
            throw new BusinessException("地址不存在");
        }
        
        Long userId = StpUtil.getLoginIdAsLong();
        if (!address.getUserId().equals(userId)) {
            throw new BusinessException("无权操作");
        }
        
        removeById(id);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefault(Long id) {
        Address address = getById(id);
        if (address == null) {
            throw new BusinessException("地址不存在");
        }
        
        Long userId = StpUtil.getLoginIdAsLong();
        if (!address.getUserId().equals(userId)) {
            throw new BusinessException("无权操作");
        }
        
        // 取消其他默认地址
        update(new LambdaUpdateWrapper<Address>()
                .eq(Address::getUserId, userId)
                .set(Address::getIsDefault, 0));
        
        // 设置当前地址为默认
        address.setIsDefault(1);
        updateById(address);
    }
}
