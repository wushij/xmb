package com.xmb.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xmb.common.OrderStatus;
import com.xmb.dto.OrderDTO;
import com.xmb.entity.*;
import com.xmb.exception.BusinessException;
import com.xmb.mapper.*;
import com.xmb.service.CartService;
import com.xmb.service.GoodsService;
import com.xmb.service.OrdersService;
import com.xmb.service.SystemConfigService;
import com.xmb.vo.OrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单服务实现类
 */
@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {
    
    @Autowired
    private SystemConfigService systemConfigService;
    
    @Autowired
    private AddressMapper addressMapper;
    
    @Autowired
    private CartMapper cartMapper;
    
    @Autowired
    private GoodsMapper goodsMapper;
    
    @Autowired
    private OrderItemMapper orderItemMapper;
    
    @Autowired
    private CartService cartService;
    
    @Autowired
    private GoodsService goodsService;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Orders createOrder(OrderDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 获取收货地址
        Address address = addressMapper.selectById(dto.getAddressId());
        if (address == null) {
            throw new BusinessException("收货地址不存在");
        }
        
        // 获取购物车商品
        List<Cart> cartList;
        if (dto.getCartIds() != null && !dto.getCartIds().isEmpty()) {
            cartList = cartService.listByIds(dto.getCartIds());
        } else {
            cartList = cartMapper.selectList(new LambdaQueryWrapper<Cart>()
                    .eq(Cart::getUserId, userId)
                    .eq(Cart::getSelected, 1));
        }
        
        if (cartList.isEmpty()) {
            throw new BusinessException("请选择要购买的商品");
        }
        
        // 计算商品总价并扣减库存
        BigDecimal goodsTotalPrice = BigDecimal.ZERO;
        for (Cart cart : cartList) {
            Goods goods = goodsMapper.selectById(cart.getGoodsId());
            if (goods == null || goods.getStatus() != 1) {
                throw new BusinessException("商品【" + cart.getGoodsId() + "】不存在或已下架");
            }
            if (goods.getStock() < cart.getNum()) {
                throw new BusinessException("商品【" + goods.getName() + "】库存不足");
            }
            goodsTotalPrice = goodsTotalPrice.add(goods.getNowPrice().multiply(new BigDecimal(cart.getNum())));
            
            // 扣减库存
            goodsService.deductStock(goods.getId(), cart.getNum());
        }
        
        // 从数据库获取配送配置
        BigDecimal deliveryFee = systemConfigService.getDeliveryFee();
        BigDecimal freeDeliveryThreshold = systemConfigService.getFreeDeliveryThreshold();
        BigDecimal minOrderAmount = systemConfigService.getMinOrderAmount();
        
        // 校验起送价格
        if (goodsTotalPrice.compareTo(minOrderAmount) < 0) {
            throw new BusinessException("商品金额未达起送价格¥" + minOrderAmount + "，请继续选购");
        }
        
        // 计算配送费：自提免配送费，配送则根据满减规则
        Integer deliveryType = dto.getDeliveryType() != null ? dto.getDeliveryType() : 0;
        BigDecimal finalDeliveryFee;
        if (deliveryType == 1) {
            // 自提：免配送费
            finalDeliveryFee = BigDecimal.ZERO;
        } else {
            // 配送：满免配送费门槛免配送费
            finalDeliveryFee = goodsTotalPrice.compareTo(freeDeliveryThreshold) >= 0 
                    ? BigDecimal.ZERO : deliveryFee;
        }
        BigDecimal totalPrice = goodsTotalPrice.add(finalDeliveryFee);
        
        // 创建订单
        Orders order = new Orders();
        order.setOrderNo(IdUtil.getSnowflakeNextIdStr());
        order.setUserId(userId);
        order.setAddressId(dto.getAddressId());
        order.setDeliveryType(deliveryType);
        order.setTotalPrice(totalPrice);
        order.setDeliveryFee(finalDeliveryFee);
        order.setPayPrice(totalPrice);
        order.setStatus(OrderStatus.WAIT_PAY);
        order.setRemark(dto.getRemark());
        save(order);
        
        // 创建订单商品
        for (Cart cart : cartList) {
            Goods goods = goodsMapper.selectById(cart.getGoodsId());
            
            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setGoodsId(cart.getGoodsId());
            item.setGoodsName(goods.getName());
            item.setGoodsImage(goods.getImage());
            item.setGoodsPrice(goods.getNowPrice());
            item.setNum(cart.getNum());
            item.setTotalPrice(goods.getNowPrice().multiply(new BigDecimal(cart.getNum())));
            orderItemMapper.insert(item);
            
            // 删除购物车
            cartMapper.deleteById(cart.getId());
        }
        
        return order;
    }
    
    @Override
    public Page<OrderVO> pageOrders(Page<Orders> page, Integer status) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Orders::getUserId, userId);
        if (status != null) {
            wrapper.eq(Orders::getStatus, status);
        }
        wrapper.orderByDesc(Orders::getCreateTime);
        
        Page<Orders> orderPage = page(page, wrapper);
        
        Page<OrderVO> voPage = new Page<>(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal());
        voPage.setRecords(orderPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList()));
        
        return voPage;
    }
    
    @Override
    public OrderVO getOrderDetail(Long id) {
        Orders order = getById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        
        Long userId = StpUtil.getLoginIdAsLong();
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权查看");
        }
        
        return convertToVO(order);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long id) {
        Orders order = getById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        
        Long userId = StpUtil.getLoginIdAsLong();
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权操作");
        }
        
        if (order.getStatus() != OrderStatus.WAIT_PAY) {
            throw new BusinessException("订单状态不允许取消");
        }
        
        order.setStatus(OrderStatus.CANCELLED);
        updateById(order);
        
        // 恢复库存
        restoreStock(id);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payOrder(Long id) {
        Orders order = getById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        
        Long userId = StpUtil.getLoginIdAsLong();
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权操作");
        }
        
        if (order.getStatus() != OrderStatus.WAIT_PAY) {
            throw new BusinessException("订单状态不允许支付");
        }
        
        // 支付后变为已支付(商家备货中)
        order.setStatus(OrderStatus.PAID);
        order.setPayTime(LocalDateTime.now());
        updateById(order);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmReceive(Long id) {
        Orders order = getById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        
        Long userId = StpUtil.getLoginIdAsLong();
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权操作");
        }
        
        if (order.getStatus() != OrderStatus.DELIVERING) {
            throw new BusinessException("订单状态不允许确认收货");
        }
        
        order.setStatus(OrderStatus.COMPLETED);
        order.setReceiveTime(LocalDateTime.now());
        updateById(order);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyRefund(Long id) {
        Orders order = getById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        
        Long userId = StpUtil.getLoginIdAsLong();
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权操作");
        }
        
        // 已支付和配送中/待取货的订单可以申请退款
        if (order.getStatus() != OrderStatus.PAID && order.getStatus() != OrderStatus.DELIVERING) {
            throw new BusinessException("订单状态不允许退款");
        }
        
        // 申请退款变为退款中,等待商家审核
        order.setStatus(OrderStatus.REFUNDING);
        updateById(order);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveRefund(Long id) {
        Orders order = getById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        
        if (order.getStatus() != OrderStatus.REFUNDING) {
            throw new BusinessException("订单状态不允许审核退款");
        }
        
        // 同意退款,恢复库存
        order.setStatus(OrderStatus.REFUNDED);
        updateById(order);
        
        // 恢复库存
        restoreStock(id);
    }
    
    @Override
    public int[] getOrderCount() {
        Long userId = StpUtil.getLoginIdAsLong();
        
        int[] counts = new int[7];
        counts[0] = Math.toIntExact(count(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getUserId, userId)
                .eq(Orders::getStatus, OrderStatus.WAIT_PAY)));
        counts[1] = Math.toIntExact(count(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getUserId, userId)
                .eq(Orders::getStatus, OrderStatus.PAID)));
        counts[2] = Math.toIntExact(count(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getUserId, userId)
                .eq(Orders::getStatus, OrderStatus.DELIVERING)));
        counts[3] = Math.toIntExact(count(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getUserId, userId)
                .eq(Orders::getStatus, OrderStatus.COMPLETED)));
        counts[4] = Math.toIntExact(count(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getUserId, userId)
                .eq(Orders::getStatus, OrderStatus.CANCELLED)));
        counts[5] = Math.toIntExact(count(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getUserId, userId)
                .eq(Orders::getStatus, OrderStatus.REFUNDING)));
        counts[6] = Math.toIntExact(count(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getUserId, userId)
                .eq(Orders::getStatus, OrderStatus.REFUNDED)));
        
        return counts;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cancelTimeoutOrders() {
        // 查询15分钟前创建的待支付订单
        LocalDateTime timeout = LocalDateTime.now().minusMinutes(15);
        
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Orders::getStatus, OrderStatus.WAIT_PAY)
               .lt(Orders::getCreateTime, timeout);
        
        List<Orders> timeoutOrders = list(wrapper);
        
        if (timeoutOrders.isEmpty()) {
            return 0;
        }
        
        int count = 0;
        for (Orders order : timeoutOrders) {
            order.setStatus(OrderStatus.CANCELLED);
            updateById(order);
            
            // 恢复库存
            restoreStock(order.getId());
            
            count++;
        }
        
        return count;
    }
    
    /**
     * 转换为VO
     */
    private OrderVO convertToVO(Orders order) {
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setTotalPrice(order.getTotalPrice());
        vo.setDeliveryFee(order.getDeliveryFee());
        // 计算商品金额 = 总价 - 配送费
        if (order.getDeliveryFee() != null) {
            vo.setGoodsPrice(order.getTotalPrice().subtract(order.getDeliveryFee()));
        } else {
            vo.setGoodsPrice(order.getTotalPrice());
        }
        vo.setStatus(order.getStatus());
        // 用户端显示根据配送方式不同的状态名称
        vo.setStatusName(OrderStatus.getUserStatusName(order.getStatus(), order.getDeliveryType()));
        
        // 设置配送方式
        Integer deliveryType = order.getDeliveryType() != null ? order.getDeliveryType() : 0;
        vo.setDeliveryType(deliveryType);
        vo.setDeliveryTypeName(deliveryType == 1 ? "自提" : "配送");
        
        // 如果是自提订单,设置取货地址
        if (deliveryType == 1) {
            vo.setPickupAddress(systemConfigService.getPickupAddress());
        }
        
        vo.setRemark(order.getRemark());
        vo.setCreateTime(order.getCreateTime());
        vo.setPayTime(order.getPayTime());
        vo.setDeliveryTime(order.getDeliveryTime());
        vo.setReceiveTime(order.getReceiveTime());
        
        // 获取地址信息
        if (order.getAddressId() != null) {
            Address address = addressMapper.selectById(order.getAddressId());
            if (address != null) {
                vo.setReceiverName(address.getName());
                vo.setReceiverPhone(address.getPhone());
                vo.setAddress(address.getProvince() + address.getCity() + address.getDistrict() + address.getDetail());
            }
        }
        
        // 获取订单商品
        List<OrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, order.getId()));
        
        List<OrderVO.OrderItemVO> itemVOList = items.stream().map(item -> {
            OrderVO.OrderItemVO itemVO = new OrderVO.OrderItemVO();
            itemVO.setGoodsId(item.getGoodsId());
            itemVO.setGoodsName(item.getGoodsName());
            itemVO.setGoodsImage(item.getGoodsImage());
            itemVO.setGoodsPrice(item.getGoodsPrice());
            itemVO.setNum(item.getNum());
            itemVO.setTotalPrice(item.getTotalPrice());
            return itemVO;
        }).collect(Collectors.toList());
        
        vo.setItems(itemVOList);
        return vo;
    }
    
    /**
     * 恢复库存
     */
    private void restoreStock(Long orderId) {
        List<OrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, orderId));
        
        for (OrderItem item : items) {
            Goods goods = goodsMapper.selectById(item.getGoodsId());
            if (goods != null) {
                goods.setStock(goods.getStock() + item.getNum());
                goods.setSales(goods.getSales() - item.getNum());
                goodsMapper.updateById(goods);
            }
        }
    }
}
