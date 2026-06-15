package com.xmb.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 * 用户登录请求DTO
 */
@Data
@ApiModel("用户登录请求")
public class LoginDTO {
    
    @ApiModelProperty(value = "微信code", required = true)
    private String code;
    
    @ApiModelProperty(value = "昵称")
    private String nickname;
    
    @ApiModelProperty(value = "头像")
    private String avatar;
}
