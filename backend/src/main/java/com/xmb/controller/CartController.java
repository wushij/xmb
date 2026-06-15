package com.xmb.controller;

import com.xmb.common.Result;
import com.xmb.dto.CartDTO;
import com.xmb.service.CartService;
import com.xmb.vo.CartVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

/**
 * 购物车控制器
 */
@Api(tags = "购物车接口")
@RestController
@RequestMapping("/api/cart")
public class CartController {
    
    @Autowired
    private CartService cartService;
    
    @ApiOperation("加入购物车")
    @PostMapping("/add")
    public Result<Void> add(@Valid @RequestBody CartDTO dto) {
        cartService.addCart(dto);
        return Result.success();
    }
    
    @ApiOperation("获取购物车列表")
    @GetMapping("/list")
    public Result<List<CartVO>> list() {
        return Result.success(cartService.getCartList());
    }
    
    @ApiOperation("更新商品数量")
    @PutMapping("/num/{id}")
    public Result<Void> updateNum(@PathVariable Long id, @RequestParam Integer num) {
        cartService.updateNum(id, num);
        return Result.success();
    }
    
    @ApiOperation("更新选中状态")
    @PutMapping("/selected/{id}")
    public Result<Void> updateSelected(@PathVariable Long id, @RequestParam Integer selected) {
        cartService.updateSelected(id, selected);
        return Result.success();
    }
    
    @ApiOperation("删除购物车商品")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        cartService.removeCart(id);
        return Result.success();
    }
    
    @ApiOperation("清空购物车")
    @DeleteMapping("/clear")
    public Result<Void> clear() {
        cartService.clearCart();
        return Result.success();
    }
    
    @ApiOperation("获取购物车数量")
    @GetMapping("/count")
    public Result<Integer> count() {
        return Result.success(cartService.getCartCount());
    }
}
