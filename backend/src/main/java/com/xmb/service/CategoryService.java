package com.xmb.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xmb.entity.Category;
import java.util.List;

/**
 * 分类服务接口
 */
public interface CategoryService extends IService<Category> {
    
    /**
     * 获取所有启用的分类
     */
    List<Category> listEnabled();
}
