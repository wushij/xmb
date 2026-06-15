package com.xmb.controller;

import com.xmb.common.Result;
import com.xmb.dto.AddressDTO;
import com.xmb.entity.Address;
import com.xmb.service.AddressService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

/**
 * 地址控制器
 */
@Api(tags = "地址接口")
@RestController
@RequestMapping("/api/address")
public class AddressController {
    
    @Autowired
    private AddressService addressService;
    
    @ApiOperation("获取地址列表")
    @GetMapping("/list")
    public Result<List<Address>> list() {
        return Result.success(addressService.listByUser());
    }
    
    @ApiOperation("获取默认地址")
    @GetMapping("/default")
    public Result<Address> getDefault() {
        return Result.success(addressService.getDefaultAddress());
    }
    
    @ApiOperation("获取地址详情")
    @GetMapping("/{id}")
    public Result<Address> detail(@PathVariable Long id) {
        return Result.success(addressService.getById(id));
    }
    
    @ApiOperation("添加地址")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody AddressDTO dto) {
        addressService.addAddress(dto);
        return Result.success();
    }
    
    @ApiOperation("更新地址")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody AddressDTO dto) {
        addressService.updateAddress(id, dto);
        return Result.success();
    }
    
    @ApiOperation("删除地址")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        addressService.removeAddress(id);
        return Result.success();
    }
    
    @ApiOperation("设置默认地址")
    @PutMapping("/default/{id}")
    public Result<Void> setDefault(@PathVariable Long id) {
        addressService.setDefault(id);
        return Result.success();
    }
}
