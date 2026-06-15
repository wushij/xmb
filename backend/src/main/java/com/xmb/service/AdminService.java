package com.xmb.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xmb.dto.AdminLoginDTO;
import com.xmb.dto.CategoryDTO;
import com.xmb.dto.GoodsDTO;
import com.xmb.entity.Category;
import com.xmb.entity.Goods;
import com.xmb.vo.AdminLoginVO;
import com.xmb.vo.SalesCompareVO;
import com.xmb.vo.StatisticsVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 后台管理服务接口
 */
public interface AdminService {
    
    // ==================== 登录 ====================
    AdminLoginVO login(AdminLoginDTO dto);
    
    // ==================== 数据统计 ====================
    StatisticsVO getStatistics(String date, String startDate, String endDate);
    SalesCompareVO getSalesCompare(String date1, String date2, String month1, String month2, Integer year1, Integer year2);
    List<Map<String, Object>> getOrderTrend();
    List<Map<String, Object>> getHotGoods();
    
    // ==================== 分类管理 ====================
    List<Category> listCategory();
    void addCategory(CategoryDTO dto);
    void updateCategory(CategoryDTO dto);
    void deleteCategory(Long id);
    
    // ==================== 商品管理 ====================
    Page<Goods> pageGoods(String name, Long categoryId, Integer status, Integer pageNum, Integer pageSize);
    void addGoods(GoodsDTO dto);
    void updateGoods(GoodsDTO dto);
    void deleteGoods(Long id);
    void batchDeleteGoods(List<Long> ids);
    void updateGoodsStatus(Long id, Integer status);
    
    // ==================== 订单管理 ====================
    Page<Map<String, Object>> pageOrders(String orderNo, Integer status, String startDate, String endDate, Integer pageNum, Integer pageSize);
    Map<String, Object> getOrderDetail(Long id);
    void updateOrderStatus(Long id, Integer status);
    
    // ==================== 文件上传 ====================
    String uploadFile(MultipartFile file);
}
