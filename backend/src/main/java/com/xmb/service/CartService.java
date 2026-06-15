package com.xmb.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xmb.dto.CartDTO;
import com.xmb.entity.Cart;
import com.xmb.vo.CartVO;
import java.util.List;

/**
 * 购物车服务接口
 */
public interface CartService extends IService<Cart> {
    
    /**
     * 加入购物车
     */
    void addCart(CartDTO dto);
    
    /**
     * 获取用户购物车列表
     */
    List<CartVO> getCartList();
    
    /**
     * 更新购物车商品数量
     */
    void updateNum(Long cartId, Integer num);
    
    /**
     * 更新购物车选中状态
     */
    void updateSelected(Long cartId, Integer selected);
    
    /**
     * 删除购物车商品
     */
    void removeCart(Long cartId);
    
    /**
     * 清空购物车
     */
    void clearCart();
    
    /**
     * 获取购物车数量
     */
    Integer getCartCount();
    
    /**
     * 根据ID列表获取购物车商品
     */
    List<Cart> listByIds(List<Long> ids);
}
