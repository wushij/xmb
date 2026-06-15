package com.xmb.controller;

import com.xmb.common.Result;
import com.xmb.dto.LoginDTO;
import com.xmb.service.UserService;
import com.xmb.vo.LoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

/**
 * 认证控制器
 */
@Api(tags = "认证接口")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @ApiOperation("微信登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success(userService.wxLogin(dto));
    }
    
    @ApiOperation("退出登录")
    @PostMapping("/logout")
    public Result<Void> logout() {
        userService.getCurrentUser(); // 校验登录状态
        return Result.success();
    }
}
