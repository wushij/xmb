package com.xmb.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 * 分类DTO
 */
@Data
@ApiModel("分类请求")
public class CategoryDTO {
    
    @ApiModelProperty("分类ID（编辑时必填）")
    private Long id;
    
    @ApiModelProperty(value = "分类名称", required = true)
    @NotBlank(message = "分类名称不能为空")
    private String name;
    
    @ApiModelProperty("分类图标")
    private String icon;
    
    @ApiModelProperty("排序号")
    private Integer sort;
    
    @ApiModelProperty("状态：0-禁用 1-启用")
    private Integer status;
}
