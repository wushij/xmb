package com.xmb.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 收货地址DTO
 */
@Data
@ApiModel("收货地址请求")
public class AddressDTO {
    
    @NotBlank(message = "收货人姓名不能为空")
    @ApiModelProperty(value = "收货人姓名", required = true)
    private String name;
    
    @NotBlank(message = "联系电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @ApiModelProperty(value = "联系电话", required = true)
    private String phone;
    
    @ApiModelProperty(value = "省")
    private String province;
    
    @ApiModelProperty(value = "市")
    private String city;
    
    @ApiModelProperty(value = "区")
    private String district;
    
    @ApiModelProperty(value = "详细地址")
    private String detail;
    
    @ApiModelProperty(value = "是否默认：0-否 1-是")
    private Integer isDefault;
}
