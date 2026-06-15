package com.xmb.dto;

import lombok.Data;
import javax.validation.constraints.Pattern;

/**
 * 用户信息更新DTO
 */
@Data
public class UserProfileDTO {
    
    private String nickname;
    
    private String avatar;
    
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式错误")
    private String phone;
    
    private String gender;
    
    private String birthday;
}
