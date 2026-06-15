package com.xmb.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xmb.dto.CartDTO;
import com.xmb.entity.Cart;
import com.xmb.entity.Goods;
import com.xmb.exception.BusinessException;
import com.xmb.mapper.CartMapper;
import com.xmb.mapper.GoodsMapper;
import com.xmb.service.CartService;
import com.xmb.vo.CartVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 购物车服务实现类
 */
@Service
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements CartService {
    
    @Autowired
    private GoodsMapper goodsMapper;
    
    @Override
    public void addCart(CartDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
            
        // 查询商品
        Goods goods = goodsMapper.selectById(dto.getGoodsId());
        if (goods == null || goods.getStatus() != 1) {
            throw new BusinessException("商品不存在或已下架");
        }
            
        // 检查库存
        if (goods.getStock() == null || goods.getStock() <= 0) {
            throw new BusinessException("商品已售罄");
        }
            
        // 查询是否已在购物车
        Cart cart = getOne(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getGoodsId, dto.getGoodsId()));
    
        if (cart != null) {
            // 检查是否超过库存
            int newNum = cart.getNum() + dto.getNum();
            if (newNum > goods.getStock()) {
                throw new BusinessException("库存不足，当前库存: " + goods.getStock() + "件");
            }
            // 更新数量
            cart.setNum(newNum);
            updateById(cart);
        } else {
            // 检查是否超过库存
            if (dto.getNum() > goods.getStock()) {
                throw new BusinessException("库存不足，当前库存: " + goods.getStock() + "件");
            }
            // 新增购物车记录
            cart = new Cart();
            cart.setUserId(userId);
            cart.setGoodsId(dto.getGoodsId());
            cart.setNum(dto.getNum());
            cart.setSelected(1);
            save(cart);
        }
    }
    
    @Override
    public List<CartVO> getCartList() {
        Long userId = StpUtil.getLoginIdAsLong();
        
        List<Cart> cartList = list(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .orderByDesc(Cart::getCreateTime));
        
        return cartList.stream().map(cart -> {
            CartVO vo = new CartVO();
            vo.setId(cart.getId());
            vo.setGoodsId(cart.getGoodsId());
            vo.setNum(cart.getNum());
            vo.setSelected(cart.getSelected());
            
            Goods goods = goodsMapper.selectById(cart.getGoodsId());
            if (goods != null) {
                vo.setGoodsName(goods.getName());
                vo.setGoodsImage(goods.getImage());
                vo.setGoodsPrice(goods.getNowPrice());
                vo.setStock(goods.getStock());
                vo.setTotalPrice(goods.getNowPrice().multiply(new BigDecimal(cart.getNum())));
            }
            
            return vo;
        }).collect(Collectors.toList());
    }
    
    @Override
    public void updateNum(Long cartId, Integer num) {
        Cart cart = getById(cartId);
        if (cart == null) {
            throw new BusinessException("购物车记录不存在");
        }
        
        // 校验是否是当前用户的购物车
        Long userId = StpUtil.getLoginIdAsLong();
        if (!cart.getUserId().equals(userId)) {
            throw new BusinessException("无权操作");
        }

        if (num <= 0) {
            removeById(cartId);
        } else {
            // 检查库存
            Goods goods = goodsMapper.selectById(cart.getGoodsId());
            if (goods != null && num > goods.getStock()) {
                throw new BusinessException("库存不足，当前库存: " + goods.getStock() + "件");
            }
            cart.setNum(num);
            updateById(cart);
        }
    }
    
    @Override
    public void updateSelected(Long cartId, Integer selected) {
        Cart cart = getById(cartId);
        if (cart == null) {
            throw new BusinessException("购物车记录不存在");
        }
        
        Long userId = StpUtil.getLoginIdAsLong();
        if (!cart.getUserId().equals(userId)) {
            throw new BusinessException("无权操作");
        }
        
        cart.setSelected(selected);
        updateById(cart);
    }
    
    @Override
    public void removeCart(Long cartId) {
        Cart cart = getById(cartId);
        if (cart == null) {
            throw new BusinessException("购物车记录不存在");
        }
        
        Long userId = StpUtil.getLoginIdAsLong();
        if (!cart.getUserId().equals(userId)) {
            throw new BusinessException("无权操作");
        }
        
        removeById(cartId);
    }
    
    @Override
    public void clearCart() {
        Long userId = StpUtil.getLoginIdAsLong();
        remove(new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, userId));
    }
    
    @Override
    public Integer getCartCount() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Math.toIntExact(count(new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, userId)));
    }
    
    @Override
    public List<Cart> listByIds(List<Long> ids) {
        return list(new LambdaQueryWrapper<Cart>().in(Cart::getId, ids));
    }
}
