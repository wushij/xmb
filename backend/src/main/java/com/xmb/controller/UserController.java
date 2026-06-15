package com.xmb.controller;

import com.xmb.common.Result;
import com.xmb.dto.UserProfileDTO;
import com.xmb.entity.User;
import com.xmb.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

/**
 * 用户控制器
 */
@Api(tags = "用户接口")
@RestController
@RequestMapping("/api/user")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @ApiOperation("获取当前用户信息")
    @GetMapping("/info")
    public Result<User> getUserInfo() {
        return Result.success(userService.getCurrentUser());
    }
    
    @ApiOperation("更新用户资料")
    @PutMapping("/profile")
    public Result<Void> updateProfile(@Valid @RequestBody UserProfileDTO dto) {
        userService.updateUserProfile(dto);
        return Result.success();
    }
}
