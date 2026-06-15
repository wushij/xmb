package com.xmb.common;

import lombok.Data;
import java.io.Serializable;

/**
 * 分页结果类
 */
@Data
public class PageResult<T> implements Serializable {
    
    private Long total;
    private java.util.List<T> list;
    
    public static <T> PageResult<T> of(Long total, java.util.List<T> list) {
        PageResult<T> result = new PageResult<>();
        result.setTotal(total);
        result.setList(list);
        return result;
    }
}
