package com.itops.security;

/**
 * 登录用户装配接口(由 system 模块实现, 供过滤器与认证服务使用)
 */
public interface SecurityUserService {

    /** 按账号加载登录身份 */
    LoginUser loadByUsername(String username);

    /** 按用户ID加载登录身份 */
    LoginUser loadById(Long userId);
}
