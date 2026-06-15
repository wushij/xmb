package com.xmb;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 小卖部后端启动类
 */
@SpringBootApplication
@MapperScan("com.xmb.mapper")
@EnableScheduling
public class XmbApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(XmbApplication.class, args);
        System.out.println("==========================================");
        System.out.println("  小卖部后端服务启动成功！");
        System.out.println("  接口文档地址：http://localhost:8080/doc.html");
        System.out.println("==========================================");
    }
}
