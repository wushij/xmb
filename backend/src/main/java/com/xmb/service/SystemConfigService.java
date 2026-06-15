package com.xmb.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xmb.entity.SystemConfig;
import java.math.BigDecimal;
import java.util.Map;

/**
 * 系统配置服务接口
 */
public interface SystemConfigService extends IService<SystemConfig> {
    
    /**
     * 获取配置值
     */
    String getConfigValue(String key);
    
    /**
     * 获取配置值，带默认值
     */
    String getConfigValue(String key, String defaultValue);
    
    /**
     * 设置配置值
     */
    void setConfigValue(String key, String value);
    
    /**
     * 获取配送费配置
     */
    BigDecimal getDeliveryFee();
    
    /**
     * 获取免配送费门槛
     */
    BigDecimal getFreeDeliveryThreshold();
    
    /**
     * 获取起送价格
     */
    BigDecimal getMinOrderAmount();
    
    /**
     * 获取自提地址
     */
    String getPickupAddress();
    
    /**
     * 设置自提地址
     */
    void setPickupAddress(String address);
    
    /**
     * 获取所有配送相关配置
     */
    Map<String, Object> getDeliveryConfig();
    
    /**
     * 更新配送配置
     */
    void updateDeliveryConfig(BigDecimal deliveryFee, BigDecimal freeDeliveryThreshold, BigDecimal minOrderAmount);
}
