package com.xmb.service;

import com.xmb.dto.LoginDTO;
import com.xmb.dto.UserProfileDTO;
import com.xmb.entity.User;
import com.xmb.vo.LoginVO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {
    
    /**
     * 微信登录
     */
    LoginVO wxLogin(LoginDTO dto);
    
    /**
     * 获取当前登录用户
     */
    User getCurrentUser();
    
    /**
     * 更新用户资料
     */
    void updateUserProfile(UserProfileDTO dto);
}
