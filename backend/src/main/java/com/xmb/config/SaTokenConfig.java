package com.xmb.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token配置类
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册Sa-Token拦截器
        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
                // 需要登录的接口
                .addPathPatterns(
                        "/api/cart/**",
                        "/api/order/**",
                        "/api/address/**",
                        "/api/user/**",
                        "/api/admin/**" // 后台管理接口
                )
                // 排除不需要登录的接口
                .excludePathPatterns(
                        "/api/auth/**",
                        "/api/goods/**",
                        "/api/category/**",
                        "/api/banner/**",
                        "/api/config/**", // 小程序获取配送配置
                        "/api/admin/login",
                        "/doc.html",
                        "/webjars/**",
                        "/swagger-resources/**",
                        "/v2/api-docs/**"
                );
    }
}
