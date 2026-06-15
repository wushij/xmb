package com.xmb.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;

/**
 * 登录响应VO
 */
@Data
@ApiModel("登录响应")
public class LoginVO implements Serializable {
    
    @ApiModelProperty("用户ID")
    private Long userId;
    
    @ApiModelProperty("访问令牌")
    private String token;
    
    @ApiModelProperty("昵称")
    private String nickname;
    
    @ApiModelProperty("头像")
    private String avatar;
    
    @ApiModelProperty("手机号")
    private String phone;
    
    @ApiModelProperty("性别")
    private String gender;
    
    @ApiModelProperty("生日")
    private String birthday;
}
