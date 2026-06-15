package com.xmb.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xmb.common.Result;
import com.xmb.entity.Goods;
import com.xmb.service.GoodsService;
import com.xmb.vo.GoodsVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 商品控制器
 */
@Api(tags = "商品接口")
@RestController
@RequestMapping("/api/goods")
public class GoodsController {
    
    @Autowired
    private GoodsService goodsService;
    
    @ApiOperation("分页查询商品")
    @GetMapping("/page")
    public Result<Page<GoodsVO>> page(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam("分类ID") @RequestParam(required = false) Long categoryId,
            @ApiParam("关键词") @RequestParam(required = false) String keyword) {
        Page<Goods> page = new Page<>(pageNum, pageSize);
        return Result.success(goodsService.pageGoods(page, categoryId, keyword));
    }
    
    @ApiOperation("获取商品详情")
    @GetMapping("/{id}")
    public Result<GoodsVO> detail(@PathVariable Long id) {
        return Result.success(goodsService.getGoodsDetail(id));
    }
    
    @ApiOperation("获取热销商品")
    @GetMapping("/hot")
    public Result<List<GoodsVO>> hot(@RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(goodsService.getHotGoods(limit));
    }
}
