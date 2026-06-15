package com.xmb.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xmb.dto.LoginDTO;
import com.xmb.dto.UserProfileDTO;
import com.xmb.entity.User;
import com.xmb.exception.BusinessException;
import com.xmb.mapper.UserMapper;
import com.xmb.service.UserService;
import com.xmb.vo.LoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    
    @Value("${wx.appid:}")
    private String appid;
    
    @Value("${wx.secret:}")
    private String secret;
    
    @Override
    public LoginVO wxLogin(LoginDTO dto) {
        String openid = null;
        
        // 如果有code，尝试获取openid（正式环境使用）
        if (StrUtil.isNotBlank(dto.getCode()) && StrUtil.isNotBlank(appid) && StrUtil.isNotBlank(secret)) {
            String url = String.format("https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                    appid, secret, dto.getCode());
            String result = HttpUtil.get(url);
            JSONObject json = JSONUtil.parseObj(result);
            openid = json.getStr("openid");
            if (StrUtil.isBlank(openid)) {
                log.error("微信登录失败：{}", result);
                throw new BusinessException("微信登录失败");
            }
        }
        
        // 查询或创建用户
        User user;
        if (StrUtil.isNotBlank(openid)) {
            user = getOne(new LambdaQueryWrapper<User>().eq(User::getOpenid, openid));
            if (user == null) {
                user = new User();
                user.setOpenid(openid);
                user.setNickname(dto.getNickname());
                user.setAvatar(dto.getAvatar());
                user.setStatus(1);
                save(user);
            } else {
                // 更新用户信息
                if (StrUtil.isNotBlank(dto.getNickname())) {
                    user.setNickname(dto.getNickname());
                }
                if (StrUtil.isNotBlank(dto.getAvatar())) {
                    user.setAvatar(dto.getAvatar());
                }
                updateById(user);
            }
        } else {
            // 开发环境：使用昵称作为唯一标识
            if (StrUtil.isBlank(dto.getNickname())) {
                throw new BusinessException("请输入昵称");
            }
            user = getOne(new LambdaQueryWrapper<User>().eq(User::getNickname, dto.getNickname()));
            if (user == null) {
                user = new User();
                user.setNickname(dto.getNickname());
                user.setAvatar(dto.getAvatar());
                user.setStatus(1);
                save(user);
            } else {
                // 更新头像
                if (StrUtil.isNotBlank(dto.getAvatar())) {
                    user.setAvatar(dto.getAvatar());
                    updateById(user);
                }
            }
        }
        
        // Sa-Token登录
        StpUtil.login(user.getId());
        
        // 返回登录信息（包含完整用户资料）
        LoginVO vo = new LoginVO();
        vo.setUserId(user.getId());
        vo.setToken(StpUtil.getTokenValue());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setPhone(user.getPhone());
        vo.setGender(user.getGender());
        vo.setBirthday(user.getBirthday());
        return vo;
    }
    
    @Override
    public User getCurrentUser() {
        Long userId = StpUtil.getLoginIdAsLong();
        return getById(userId);
    }
    
    @Override
    public void updateUserProfile(UserProfileDTO dto) {
        User user = getCurrentUser();
        if (StrUtil.isNotBlank(dto.getNickname())) {
            user.setNickname(dto.getNickname());
        }
        if (StrUtil.isNotBlank(dto.getAvatar())) {
            user.setAvatar(dto.getAvatar());
        }
        if (StrUtil.isNotBlank(dto.getPhone())) {
            user.setPhone(dto.getPhone());
        }
        if (StrUtil.isNotBlank(dto.getGender())) {
            user.setGender(dto.getGender());
        }
        if (StrUtil.isNotBlank(dto.getBirthday())) {
            user.setBirthday(dto.getBirthday());
        }
        updateById(user);
    }
}
