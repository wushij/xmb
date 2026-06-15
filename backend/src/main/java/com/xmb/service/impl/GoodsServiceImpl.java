package com.xmb.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xmb.entity.Category;
import com.xmb.entity.Goods;
import com.xmb.exception.BusinessException;
import com.xmb.mapper.CategoryMapper;
import com.xmb.mapper.GoodsMapper;
import com.xmb.service.GoodsService;
import com.xmb.vo.GoodsVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品服务实现类
 */
@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements GoodsService {
    
    @Autowired
    private CategoryMapper categoryMapper;
    
    @Override
    public Page<GoodsVO> pageGoods(Page<Goods> page, Long categoryId, String keyword) {
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Goods::getStatus, 1);
        if (categoryId != null) {
            wrapper.eq(Goods::getCategoryId, categoryId);
        }
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.like(Goods::getName, keyword);
        }
        wrapper.orderByDesc(Goods::getSales);
        
        Page<Goods> goodsPage = page(page, wrapper);
        
        // 获取分类名称
        List<Long> categoryIds = goodsPage.getRecords().stream()
                .map(Goods::getCategoryId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, String> categoryNameMap = categoryMapper.selectBatchIds(categoryIds).stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));
        
        // 转换为VO
        Page<GoodsVO> voPage = new Page<>(goodsPage.getCurrent(), goodsPage.getSize(), goodsPage.getTotal());
        List<GoodsVO> voList = goodsPage.getRecords().stream().map(goods -> {
            GoodsVO vo = new GoodsVO();
            BeanUtils.copyProperties(goods, vo);
            vo.setCategoryName(categoryNameMap.get(goods.getCategoryId()));
            return vo;
        }).collect(Collectors.toList());
        voPage.setRecords(voList);
        
        return voPage;
    }
    
    @Override
    public GoodsVO getGoodsDetail(Long id) {
        Goods goods = getById(id);
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }
        
        GoodsVO vo = new GoodsVO();
        BeanUtils.copyProperties(goods, vo);
        
        Category category = categoryMapper.selectById(goods.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getName());
        }
        
        return vo;
    }
    
    @Override
    public List<GoodsVO> getHotGoods(Integer limit) {
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Goods::getStatus, 1)
                .orderByDesc(Goods::getSales)
                .last("LIMIT " + limit);
        
        List<Goods> goodsList = list(wrapper);
        
        return goodsList.stream().map(goods -> {
            GoodsVO vo = new GoodsVO();
            BeanUtils.copyProperties(goods, vo);
            return vo;
        }).collect(Collectors.toList());
    }
    
    @Override
    public void deductStock(Long goodsId, Integer num) {
        Goods goods = getById(goodsId);
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }
        if (goods.getStock() < num) {
            throw new BusinessException("商品【" + goods.getName() + "】库存不足");
        }
        goods.setStock(goods.getStock() - num);
        goods.setSales(goods.getSales() + num);
        updateById(goods);
    }
}
