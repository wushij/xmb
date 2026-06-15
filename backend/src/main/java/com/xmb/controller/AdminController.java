package com.xmb.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xmb.common.Result;
import com.xmb.dto.AdminLoginDTO;
import com.xmb.dto.CategoryDTO;
import com.xmb.dto.GoodsDTO;
import com.xmb.entity.Category;
import com.xmb.entity.Goods;
import com.xmb.service.AdminService;
import com.xmb.vo.AdminLoginVO;
import com.xmb.vo.SalesCompareVO;
import com.xmb.vo.StatisticsVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 后台管理控制器
 */
@Api(tags = "后台管理接口")
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    // ==================== 登录 ====================

    @ApiOperation("管理员登录")
    @PostMapping("/login")
    public Result<AdminLoginVO> login(@Valid @RequestBody AdminLoginDTO dto) {
        return Result.success(adminService.login(dto));
    }

    // ==================== 数据统计 ====================

    @ApiOperation("获取统计数据")
    @GetMapping("/statistics")
    public Result<StatisticsVO> getStatistics(
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.success(adminService.getStatistics(date, startDate, endDate));
    }

    @ApiOperation("获取销售对比数据")
    @GetMapping("/sales-compare")
    public Result<SalesCompareVO> getSalesCompare(
            @RequestParam(required = false) String date1,
            @RequestParam(required = false) String date2,
            @RequestParam(required = false) String month1,
            @RequestParam(required = false) String month2,
            @RequestParam(required = false) Integer year1,
            @RequestParam(required = false) Integer year2) {
        return Result.success(adminService.getSalesCompare(date1, date2, month1, month2, year1, year2));
    }

    @ApiOperation("获取订单趋势")
    @GetMapping("/order-trend")
    public Result<List<Map<String, Object>>> getOrderTrend() {
        return Result.success(adminService.getOrderTrend());
    }

    @ApiOperation("获取热销商品")
    @GetMapping("/hot-goods")
    public Result<List<Map<String, Object>>> getHotGoods() {
        return Result.success(adminService.getHotGoods());
    }

    // ==================== 分类管理 ====================

    @ApiOperation("分类列表")
    @GetMapping("/category/list")
    public Result<List<Category>> categoryList() {
        return Result.success(adminService.listCategory());
    }

    @ApiOperation("新增分类")
    @PostMapping("/category")
    public Result<Void> addCategory(@Valid @RequestBody CategoryDTO dto) {
        adminService.addCategory(dto);
        return Result.success();
    }

    @ApiOperation("编辑分类")
    @PutMapping("/category")
    public Result<Void> updateCategory(@Valid @RequestBody CategoryDTO dto) {
        adminService.updateCategory(dto);
        return Result.success();
    }

    @ApiOperation("删除分类")
    @DeleteMapping("/category/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        adminService.deleteCategory(id);
        return Result.success();
    }

    // ==================== 商品管理 ====================

    @ApiOperation("商品列表")
    @GetMapping("/goods/list")
    public Result<Page<Goods>> goodsList(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(adminService.pageGoods(name, categoryId, status, pageNum, pageSize));
    }

    @ApiOperation("新增商品")
    @PostMapping("/goods")
    public Result<Void> addGoods(@Valid @RequestBody GoodsDTO dto) {
        adminService.addGoods(dto);
        return Result.success();
    }

    @ApiOperation("编辑商品")
    @PutMapping("/goods")
    public Result<Void> updateGoods(@Valid @RequestBody GoodsDTO dto) {
        adminService.updateGoods(dto);
        return Result.success();
    }

    @ApiOperation("删除商品")
    @DeleteMapping("/goods/{id}")
    public Result<Void> deleteGoods(@PathVariable Long id) {
        adminService.deleteGoods(id);
        return Result.success();
    }

    @ApiOperation("批量删除商品")
    @PostMapping("/goods/batch-delete")
    public Result<Void> batchDeleteGoods(@RequestBody List<Long> ids) {
        adminService.batchDeleteGoods(ids);
        return Result.success();
    }

    @ApiOperation("更新商品状态")
    @PutMapping("/goods/status/{id}")
    public Result<Void> updateGoodsStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        adminService.updateGoodsStatus(id, body.get("status"));
        return Result.success();
    }

    // ==================== 订单管理 ====================

    @ApiOperation("订单列表")
    @GetMapping("/order/list")
    public Result<Page<Map<String, Object>>> orderList(
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(adminService.pageOrders(orderNo, status, startDate, endDate, pageNum, pageSize));
    }

    @ApiOperation("订单详情")
    @GetMapping("/order/{id}")
    public Result<Map<String, Object>> orderDetail(@PathVariable Long id) {
        return Result.success(adminService.getOrderDetail(id));
    }

    @ApiOperation("更新订单状态")
    @PutMapping("/order/status/{id}")
    public Result<Void> updateOrderStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        adminService.updateOrderStatus(id, body.get("status"));
        return Result.success();
    }

    // ==================== 文件上传 ====================

    @ApiOperation("上传文件")
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        return Result.success(adminService.uploadFile(file));
    }
}
