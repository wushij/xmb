package com.xmb.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;

/**
 * 管理员登录VO
 */
@Data
@ApiModel("管理员登录响应")
public class AdminLoginVO implements Serializable {
    
    @ApiModelProperty("访问令牌")
    private String token;
    
    @ApiModelProperty("用户名")
    private String username;
}
