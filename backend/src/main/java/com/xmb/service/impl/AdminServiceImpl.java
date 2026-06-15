package com.xmb.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xmb.common.OrderStatus;
import com.xmb.dto.AdminLoginDTO;
import com.xmb.dto.CategoryDTO;
import com.xmb.dto.GoodsDTO;
import com.xmb.entity.*;
import com.xmb.exception.BusinessException;
import com.xmb.mapper.*;
import com.xmb.service.AdminService;
import com.xmb.service.SystemConfigService;
import com.xmb.vo.AdminLoginVO;
import com.xmb.vo.SalesCompareVO;
import com.xmb.vo.StatisticsVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 后台管理服务实现类
 */
@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private CategoryMapper categoryMapper;
    
    @Autowired
    private GoodsMapper goodsMapper;
    
    @Autowired
    private OrdersMapper ordersMapper;
    
    @Autowired
    private OrderItemMapper orderItemMapper;
    
    @Autowired
    private AddressMapper addressMapper;
    
    @Autowired
    private SystemConfigService systemConfigService;

    // ==================== 登录 ====================

    @Override
    public AdminLoginVO login(AdminLoginDTO dto) {
        // TODO: 实际项目中应该从数据库查询管理员账号密码
        // 这里使用硬编码作为示例
        if (!"admin".equals(dto.getUsername()) || !"admin123".equals(dto.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        
        StpUtil.login(999999L); // 管理员ID
        
        AdminLoginVO vo = new AdminLoginVO();
        vo.setToken(StpUtil.getTokenValue());
        vo.setUsername(dto.getUsername());
        return vo;
    }

    // ==================== 数据统计 ====================

    @Override
    public StatisticsVO getStatistics(String date, String startDate, String endDate) {
        StatisticsVO vo = new StatisticsVO();
        
        LocalDateTime queryStart, queryEnd;
        
        // 优先使用日期范围
        if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
            queryStart = LocalDate.parse(startDate).atStartOfDay();
            queryEnd = LocalDate.parse(endDate).plusDays(1).atStartOfDay();
        } else if (date != null && !date.isEmpty()) {
            // 单日期查询
            LocalDate queryDate = LocalDate.parse(date);
            queryStart = queryDate.atStartOfDay();
            queryEnd = queryDate.plusDays(1).atStartOfDay();
        } else {
            // 默认今天
            LocalDate today = LocalDate.now();
            queryStart = today.atStartOfDay();
            queryEnd = today.plusDays(1).atStartOfDay();
        }
        
        // 指定日期范围的订单数和销售额
        List<Orders> dayOrders = ordersMapper.selectList(new LambdaQueryWrapper<Orders>()
                .ge(Orders::getCreateTime, queryStart)
                .lt(Orders::getCreateTime, queryEnd));
        vo.setTodayOrderCount(dayOrders.size());
        vo.setTodaySalesAmount(dayOrders.stream()
                .filter(o -> o.getStatus() >= OrderStatus.PAID)
                .map(Orders::getPayPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        
        // 总订单数和销售额
        List<Orders> allOrders = ordersMapper.selectList(null);
        vo.setTotalOrderCount(allOrders.size());
        vo.setTotalSalesAmount(allOrders.stream()
                .filter(o -> o.getStatus() >= OrderStatus.PAID)
                .map(Orders::getPayPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        
        // 商品总数和分类总数
        vo.setTotalGoodsCount(Math.toIntExact(goodsMapper.selectCount(null)));
        vo.setTotalCategoryCount(Math.toIntExact(categoryMapper.selectCount(null)));
        
        return vo;
    }

    @Override
    public List<Map<String, Object>> getOrderTrend() {
        List<Map<String, Object>> result = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
            
            long count = ordersMapper.selectCount(new LambdaQueryWrapper<Orders>()
                    .ge(Orders::getCreateTime, startOfDay)
                    .lt(Orders::getCreateTime, endOfDay));
            
            Map<String, Object> map = new HashMap<>();
            map.put("date", date.format(formatter));
            map.put("count", count);
            result.add(map);
        }
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getHotGoods() {
        List<Goods> goodsList = goodsMapper.selectList(new LambdaQueryWrapper<Goods>()
                .orderByDesc(Goods::getSales)
                .last("LIMIT 5"));
        
        return goodsList.stream().map(goods -> {
            Map<String, Object> map = new HashMap<>();
            map.put("name", goods.getName());
            map.put("sales", goods.getSales());
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    public SalesCompareVO getSalesCompare(String date1, String date2, String month1, String month2, Integer year1, Integer year2) {
        SalesCompareVO vo = new SalesCompareVO();
        LocalDate today = LocalDate.now();
        
        // 解析日对比日期
        LocalDate localDate1, localDate2;
        if (date1 != null && !date1.isEmpty()) {
            localDate1 = LocalDate.parse(date1);
        } else {
            localDate1 = today; // 默认今天
        }
        if (date2 != null && !date2.isEmpty()) {
            localDate2 = LocalDate.parse(date2);
        } else {
            localDate2 = today.minusDays(1); // 默认昨天
        }
        
        // 解析月对比日期
        YearMonth compareMonth1, compareMonth2;
        if (month1 != null && !month1.isEmpty()) {
            compareMonth1 = YearMonth.parse(month1);
        } else {
            compareMonth1 = YearMonth.now(); // 默认本月
        }
        if (month2 != null && !month2.isEmpty()) {
            compareMonth2 = YearMonth.parse(month2);
        } else {
            compareMonth2 = YearMonth.now().minusMonths(1); // 默认上月
        }
        
        // 解析年对比年份
        int compareYear1, compareYear2;
        if (year1 != null) {
            compareYear1 = year1;
        } else {
            compareYear1 = today.getYear(); // 默认今年
        }
        if (year2 != null) {
            compareYear2 = year2;
        } else {
            compareYear2 = today.getYear() - 1; // 默认去年
        }
        
        // ========== 日对比 ==========
        // 日期1的销售额和订单数
        LocalDateTime date1Start = localDate1.atStartOfDay();
        LocalDateTime date1End = localDate1.plusDays(1).atStartOfDay();
        List<Orders> date1Orders = ordersMapper.selectList(new LambdaQueryWrapper<Orders>()
                .ge(Orders::getCreateTime, date1Start)
                .lt(Orders::getCreateTime, date1End)
                .ge(Orders::getStatus, OrderStatus.PAID));
        BigDecimal date1Amount = date1Orders.stream()
                .map(Orders::getPayPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setTodayAmount(date1Amount);
        vo.setTodayOrderCount(date1Orders.size());
        
        // 日期2的销售额和订单数
        LocalDateTime date2Start = localDate2.atStartOfDay();
        LocalDateTime date2End = localDate2.plusDays(1).atStartOfDay();
        List<Orders> date2Orders = ordersMapper.selectList(new LambdaQueryWrapper<Orders>()
                .ge(Orders::getCreateTime, date2Start)
                .lt(Orders::getCreateTime, date2End)
                .ge(Orders::getStatus, OrderStatus.PAID));
        BigDecimal date2Amount = date2Orders.stream()
                .map(Orders::getPayPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setYesterdayAmount(date2Amount);
        vo.setYesterdayOrderCount(date2Orders.size());
        
        // 日环比增长率
        vo.setDayGrowthRate(calculateGrowthRate(date1Amount, date2Amount));
        vo.setDayOrderGrowthRate(calculateGrowthRate(
                new BigDecimal(date1Orders.size()), 
                new BigDecimal(date2Orders.size())));
        
        // ========== 月对比 ==========
        LocalDateTime month1Start = compareMonth1.atDay(1).atStartOfDay();
        LocalDateTime month1End = compareMonth1.plusMonths(1).atDay(1).atStartOfDay();
        List<Orders> month1Orders = ordersMapper.selectList(new LambdaQueryWrapper<Orders>()
                .ge(Orders::getCreateTime, month1Start)
                .lt(Orders::getCreateTime, month1End)
                .ge(Orders::getStatus, OrderStatus.PAID));
        BigDecimal month1Amount = month1Orders.stream()
                .map(Orders::getPayPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setMonthAmount(month1Amount);
        vo.setMonthOrderCount(month1Orders.size());
        
        // 月份2的销售额和订单数
        LocalDateTime month2Start = compareMonth2.atDay(1).atStartOfDay();
        LocalDateTime month2End = compareMonth2.plusMonths(1).atDay(1).atStartOfDay();
        List<Orders> month2Orders = ordersMapper.selectList(new LambdaQueryWrapper<Orders>()
                .ge(Orders::getCreateTime, month2Start)
                .lt(Orders::getCreateTime, month2End)
                .ge(Orders::getStatus, OrderStatus.PAID));
        BigDecimal month2Amount = month2Orders.stream()
                .map(Orders::getPayPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setLastMonthAmount(month2Amount);
        vo.setLastMonthOrderCount(month2Orders.size());
        
        // 月环比增长率
        vo.setMonthGrowthRate(calculateGrowthRate(month1Amount, month2Amount));
        vo.setMonthOrderGrowthRate(calculateGrowthRate(
                new BigDecimal(month1Orders.size()), 
                new BigDecimal(month2Orders.size())));
        
        // ========== 年对比 ==========
        // 年份1的销售额
        LocalDateTime year1Start = LocalDate.of(compareYear1, 1, 1).atStartOfDay();
        LocalDateTime year1End = LocalDate.of(compareYear1 + 1, 1, 1).atStartOfDay();
        List<Orders> year1Orders = ordersMapper.selectList(new LambdaQueryWrapper<Orders>()
                .ge(Orders::getCreateTime, year1Start)
                .lt(Orders::getCreateTime, year1End)
                .ge(Orders::getStatus, OrderStatus.PAID));
        BigDecimal year1Amount = year1Orders.stream()
                .map(Orders::getPayPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setYearAmount(year1Amount);
        
        // 年份2的销售额
        LocalDateTime year2Start = LocalDate.of(compareYear2, 1, 1).atStartOfDay();
        LocalDateTime year2End = LocalDate.of(compareYear2 + 1, 1, 1).atStartOfDay();
        List<Orders> year2Orders = ordersMapper.selectList(new LambdaQueryWrapper<Orders>()
                .ge(Orders::getCreateTime, year2Start)
                .lt(Orders::getCreateTime, year2End)
                .ge(Orders::getStatus, OrderStatus.PAID));
        BigDecimal year2Amount = year2Orders.stream()
                .map(Orders::getPayPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setLastYearAmount(year2Amount);
        
        // 年同比增长率
        vo.setYearGrowthRate(calculateGrowthRate(year1Amount, year2Amount));
        
        return vo;
    }
    
    /**
     * 计算增长率
     * @param current 当前值
     * @param previous 对比值
     * @return 增长率（百分比）
     */
    private BigDecimal calculateGrowthRate(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            if (current != null && current.compareTo(BigDecimal.ZERO) > 0) {
                return new BigDecimal(100); // 从0增长视为100%
            }
            return BigDecimal.ZERO;
        }
        return current.subtract(previous)
                .divide(previous, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal(100))
                .setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    // ==================== 分类管理 ====================

    @Override
    public List<Category> listCategory() {
        return categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .orderByAsc(Category::getSort));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addCategory(CategoryDTO dto) {
        Category category = new Category();
        BeanUtils.copyProperties(dto, category);
        if (category.getSort() == null) {
            category.setSort(0);
        }
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        categoryMapper.insert(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCategory(CategoryDTO dto) {
        Category category = categoryMapper.selectById(dto.getId());
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        BeanUtils.copyProperties(dto, category);
        categoryMapper.updateById(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long id) {
        // 检查是否有商品使用该分类
        long count = goodsMapper.selectCount(new LambdaQueryWrapper<Goods>()
                .eq(Goods::getCategoryId, id));
        if (count > 0) {
            throw new BusinessException("该分类下有商品，无法删除");
        }
        categoryMapper.deleteById(id);
    }

    // ==================== 商品管理 ====================

    @Override
    public Page<Goods> pageGoods(String name, Long categoryId, Integer status, Integer pageNum, Integer pageSize) {
        Page<Goods> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        
        if (name != null && !name.isEmpty()) {
            wrapper.like(Goods::getName, name);
        }
        if (categoryId != null) {
            wrapper.eq(Goods::getCategoryId, categoryId);
        }
        if (status != null) {
            wrapper.eq(Goods::getStatus, status);
        }
        wrapper.orderByDesc(Goods::getCreateTime);
        
        Page<Goods> result = goodsMapper.selectPage(page, wrapper);
        
        // 添加分类名称
        result.getRecords().forEach(goods -> {
            Category category = categoryMapper.selectById(goods.getCategoryId());
            if (category != null) {
                goods.setCategoryName(category.getName());
            }
        });
        
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addGoods(GoodsDTO dto) {
        Goods goods = new Goods();
        BeanUtils.copyProperties(dto, goods);
        if (goods.getStatus() == null) {
            goods.setStatus(1);
        }
        if (goods.getSales() == null) {
            goods.setSales(0);
        }
        goodsMapper.insert(goods);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGoods(GoodsDTO dto) {
        Goods goods = goodsMapper.selectById(dto.getId());
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }
        BeanUtils.copyProperties(dto, goods);
        goodsMapper.updateById(goods);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteGoods(Long id) {
        goodsMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteGoods(List<Long> ids) {
        goodsMapper.deleteBatchIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGoodsStatus(Long id, Integer status) {
        Goods goods = goodsMapper.selectById(id);
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }
        goods.setStatus(status);
        goodsMapper.updateById(goods);
    }

    // ==================== 订单管理 ====================

    @Override
    public Page<Map<String, Object>> pageOrders(String orderNo, Integer status, String startDate, String endDate, Integer pageNum, Integer pageSize) {
        Page<Orders> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        
        if (orderNo != null && !orderNo.isEmpty()) {
            wrapper.like(Orders::getOrderNo, orderNo);
        }
        if (status != null) {
            wrapper.eq(Orders::getStatus, status);
        }
        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge(Orders::getCreateTime, startDate + " 00:00:00");
        }
        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le(Orders::getCreateTime, endDate + " 23:59:59");
        }
        wrapper.orderByDesc(Orders::getCreateTime);
        
        Page<Orders> orderPage = ordersMapper.selectPage(page, wrapper);
        
        Page<Map<String, Object>> result = new Page<>(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal());
        result.setRecords(orderPage.getRecords().stream().map(order -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", order.getId());
            map.put("orderNo", order.getOrderNo());
            map.put("payPrice", order.getPayPrice());
            map.put("status", order.getStatus());
            map.put("deliveryType", order.getDeliveryType() != null ? order.getDeliveryType() : 0);
            map.put("createTime", order.getCreateTime());
            
            // 获取收货信息
            if (order.getAddressId() != null) {
                Address address = addressMapper.selectById(order.getAddressId());
                if (address != null) {
                    map.put("receiverName", address.getName());
                    map.put("receiverPhone", address.getPhone());
                }
            }
            
            // 获取商品数量（求和每个商品的数量）
            List<OrderItem> orderItems = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                    .eq(OrderItem::getOrderId, order.getId()));
            int totalNum = orderItems.stream().mapToInt(OrderItem::getNum).sum();
            map.put("totalNum", totalNum);
            map.put("items", orderItems.stream().map(item -> {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("goodsId", item.getGoodsId());
                itemMap.put("goodsName", item.getGoodsName());
                itemMap.put("goodsImage", item.getGoodsImage());
                itemMap.put("goodsPrice", item.getGoodsPrice());
                itemMap.put("num", item.getNum());
                itemMap.put("totalPrice", item.getTotalPrice());
                return itemMap;
            }).collect(Collectors.toList()));
            
            return map;
        }).collect(Collectors.toList()));
        
        return result;
    }

    @Override
    public Map<String, Object> getOrderDetail(Long id) {
        Orders order = ordersMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", order.getId());
        result.put("orderNo", order.getOrderNo());
        result.put("totalPrice", order.getTotalPrice());
        result.put("payPrice", order.getPayPrice());
        result.put("status", order.getStatus());
        result.put("deliveryType", order.getDeliveryType() != null ? order.getDeliveryType() : 0);
        result.put("remark", order.getRemark());
        result.put("createTime", order.getCreateTime());
        result.put("payTime", order.getPayTime());
        result.put("deliveryTime", order.getDeliveryTime());
        
        // 获取地址信息
        if (order.getAddressId() != null) {
            Address address = addressMapper.selectById(order.getAddressId());
            if (address != null) {
                result.put("receiverName", address.getName());
                result.put("receiverPhone", address.getPhone());
                result.put("address", address.getProvince() + address.getCity() + address.getDistrict() + address.getDetail());
            }
        }
        
        // 如果是自提订单，添加取货地址
        if (order.getDeliveryType() != null && order.getDeliveryType() == 1) {
            result.put("pickupAddress", systemConfigService.getPickupAddress());
        }
        
        // 获取订单商品
        List<OrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, order.getId()));
        
        List<Map<String, Object>> itemList = items.stream().map(item -> {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("goodsId", item.getGoodsId());
            itemMap.put("goodsName", item.getGoodsName());
            itemMap.put("goodsImage", item.getGoodsImage());
            itemMap.put("goodsPrice", item.getGoodsPrice());
            itemMap.put("num", item.getNum());
            itemMap.put("totalPrice", item.getTotalPrice());
            return itemMap;
        }).collect(Collectors.toList());
        
        result.put("items", itemList);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOrderStatus(Long id, Integer status) {
        Orders order = ordersMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        order.setStatus(status);
        if (status == OrderStatus.DELIVERING) {
            order.setDeliveryTime(LocalDateTime.now());
        }
        // 同意退款时恢复库存
        if (status == OrderStatus.REFUNDED) {
            List<OrderItem> refundItems = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                    .eq(OrderItem::getOrderId, id));
            for (OrderItem item : refundItems) {
                Goods goods = goodsMapper.selectById(item.getGoodsId());
                if (goods != null) {
                    goods.setStock(goods.getStock() + item.getNum());
                    goods.setSales(goods.getSales() - item.getNum());
                    goodsMapper.updateById(goods);
                }
            }
        }
        ordersMapper.updateById(order);
    }

    // ==================== 文件上传 ====================

    @Override
    public String uploadFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }
        
        try {
            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = IdUtil.simpleUUID() + extension;
            
            // 保存到后端images目录
            String uploadDir = System.getProperty("user.dir") + "/images/goods/";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            File dest = new File(uploadDir + fileName);
            file.transferTo(dest);
            
            // 返回访问URL
            return "/images/goods/" + fileName;
        } catch (IOException e) {
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }
}
