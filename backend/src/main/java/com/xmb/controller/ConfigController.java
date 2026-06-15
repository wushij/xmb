package com.xmb.controller;

import com.xmb.common.Result;
import com.xmb.service.SystemConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Map;

/**
 * 系统配置控制器 - 客户端接口
 */
@Api(tags = "系统配置接口")
@RestController
@RequestMapping("/api/config")
public class ConfigController {
    
    @Autowired
    private SystemConfigService systemConfigService;
    
    @ApiOperation("获取配送配置")
    @GetMapping("/delivery")
    public Result<Map<String, Object>> getDeliveryConfig() {
        return Result.success(systemConfigService.getDeliveryConfig());
    }
}
