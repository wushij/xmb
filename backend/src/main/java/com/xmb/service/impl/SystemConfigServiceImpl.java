package com.xmb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xmb.entity.SystemConfig;
import com.xmb.mapper.SystemConfigMapper;
import com.xmb.service.SystemConfigService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 系统配置服务实现
 */
@Service
public class SystemConfigServiceImpl extends ServiceImpl<SystemConfigMapper, SystemConfig> implements SystemConfigService {
    
    // 配置键常量
    private static final String KEY_DELIVERY_FEE = "delivery_fee";
    private static final String KEY_FREE_DELIVERY_THRESHOLD = "free_delivery_threshold";
    private static final String KEY_MIN_ORDER_AMOUNT = "min_order_amount";
    private static final String KEY_PICKUP_ADDRESS = "pickup_address";
    
    // 默认值
    private static final BigDecimal DEFAULT_DELIVERY_FEE = new BigDecimal("3");
    private static final BigDecimal DEFAULT_FREE_DELIVERY_THRESHOLD = new BigDecimal("19.9");
    private static final BigDecimal DEFAULT_MIN_ORDER_AMOUNT = new BigDecimal("12");
    private static final String DEFAULT_PICKUP_ADDRESS = "小卖部门店(具体地址待补充)";
    
    // 本地缓存
    private final Map<String, String> configCache = new ConcurrentHashMap<>();
    
    @Override
    public String getConfigValue(String key) {
        return getConfigValue(key, null);
    }
    
    @Override
    public String getConfigValue(String key, String defaultValue) {
        // 先从缓存获取
        String cachedValue = configCache.get(key);
        if (cachedValue != null) {
            return cachedValue;
        }
        
        // 从数据库获取
        SystemConfig config = getOne(new LambdaQueryWrapper<SystemConfig>()
                .eq(SystemConfig::getConfigKey, key));
        
        if (config != null && config.getConfigValue() != null) {
            configCache.put(key, config.getConfigValue());
            return config.getConfigValue();
        }
        
        return defaultValue;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setConfigValue(String key, String value) {
        SystemConfig config = getOne(new LambdaQueryWrapper<SystemConfig>()
                .eq(SystemConfig::getConfigKey, key));
        
        if (config != null) {
            config.setConfigValue(value);
            updateById(config);
        } else {
            config = new SystemConfig();
            config.setConfigKey(key);
            config.setConfigValue(value);
            save(config);
        }
        
        // 更新缓存
        configCache.put(key, value);
    }
    
    @Override
    public BigDecimal getDeliveryFee() {
        String value = getConfigValue(KEY_DELIVERY_FEE, "3");
        try {
            return new BigDecimal(value);
        } catch (Exception e) {
            return DEFAULT_DELIVERY_FEE;
        }
    }
    
    @Override
    public BigDecimal getFreeDeliveryThreshold() {
        String value = getConfigValue(KEY_FREE_DELIVERY_THRESHOLD, "19.9");
        try {
            return new BigDecimal(value);
        } catch (Exception e) {
            return DEFAULT_FREE_DELIVERY_THRESHOLD;
        }
    }
    
    @Override
    public BigDecimal getMinOrderAmount() {
        String value = getConfigValue(KEY_MIN_ORDER_AMOUNT, "12");
        try {
            return new BigDecimal(value);
        } catch (Exception e) {
            return DEFAULT_MIN_ORDER_AMOUNT;
        }
    }
    
    @Override
    public String getPickupAddress() {
        return getConfigValue(KEY_PICKUP_ADDRESS, DEFAULT_PICKUP_ADDRESS);
    }
    
    @Override
    public Map<String, Object> getDeliveryConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("deliveryFee", getDeliveryFee());
        config.put("freeDeliveryThreshold", getFreeDeliveryThreshold());
        config.put("minOrderAmount", getMinOrderAmount());
        config.put("pickupAddress", getPickupAddress());
        return config;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDeliveryConfig(BigDecimal deliveryFee, BigDecimal freeDeliveryThreshold, BigDecimal minOrderAmount) {
        if (deliveryFee != null) {
            setConfigValue(KEY_DELIVERY_FEE, deliveryFee.toString());
        }
        if (freeDeliveryThreshold != null) {
            setConfigValue(KEY_FREE_DELIVERY_THRESHOLD, freeDeliveryThreshold.toString());
        }
        if (minOrderAmount != null) {
            setConfigValue(KEY_MIN_ORDER_AMOUNT, minOrderAmount.toString());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setPickupAddress(String address) {
        setConfigValue(KEY_PICKUP_ADDRESS, address);
    }
    
    /**
     * 刷新缓存
     */
    public void refreshCache() {
        configCache.clear();
    }
}
