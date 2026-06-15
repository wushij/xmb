package com.xmb.controller;

import com.xmb.common.Result;
import com.xmb.dto.DeliveryConfigDTO;
import com.xmb.service.SystemConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.Map;

/**
 * 系统配置控制器 - 管理端接口
 */
@Api(tags = "系统配置管理接口")
@RestController
@RequestMapping("/api/admin/config")
public class AdminConfigController {
    
    @Autowired
    private SystemConfigService systemConfigService;
    
    @ApiOperation("获取配送配置")
    @GetMapping("/delivery")
    public Result<Map<String, Object>> getDeliveryConfig() {
        return Result.success(systemConfigService.getDeliveryConfig());
    }
    
    @ApiOperation("更新配送配置")
    @PutMapping("/delivery")
    public Result<Void> updateDeliveryConfig(@Valid @RequestBody DeliveryConfigDTO dto) {
        systemConfigService.updateDeliveryConfig(
                dto.getDeliveryFee(),
                dto.getFreeDeliveryThreshold(),
                dto.getMinOrderAmount()
        );
        
        // 更新自提地址
        if (dto.getPickupAddress() != null && !dto.getPickupAddress().isEmpty()) {
            systemConfigService.setPickupAddress(dto.getPickupAddress());
        }
        
        return Result.success();
    }
}
