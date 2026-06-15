package com.xmb.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xmb.entity.Goods;
import com.xmb.vo.GoodsVO;
import java.util.List;

/**
 * 商品服务接口
 */
public interface GoodsService extends IService<Goods> {
    
    /**
     * 分页查询商品
     */
    Page<GoodsVO> pageGoods(Page<Goods> page, Long categoryId, String keyword);
    
    /**
     * 获取商品详情
     */
    GoodsVO getGoodsDetail(Long id);
    
    /**
     * 获取热销商品
     */
    List<GoodsVO> getHotGoods(Integer limit);
    
    /**
     * 扣减库存
     */
    void deductStock(Long goodsId, Integer num);
}
