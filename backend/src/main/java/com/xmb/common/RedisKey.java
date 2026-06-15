package com.xmb.common;

/**
 * Redis缓存Key常量
 */
public class RedisKey {
    
    private static final String PREFIX = "xmb:";
    
    public static final String USER_TOKEN = PREFIX + "user:token:";
    public static final String USER_INFO = PREFIX + "user:info:";
    public static final String GOODS_DETAIL = PREFIX + "goods:detail:";
    public static final String CATEGORY_LIST = PREFIX + "category:list:";
    public static final String CART_USER = PREFIX + "cart:user:";
}
