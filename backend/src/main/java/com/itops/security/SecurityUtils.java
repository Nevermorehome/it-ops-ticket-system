package com.itops.security;

import com.itops.common.exception.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全上下文工具
 */
public class SecurityUtils {

    private SecurityUtils() {
    }

    public static LoginUser getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        return principal instanceof LoginUser lu ? lu : null;
    }

    public static LoginUser requireLoginUser() {
        LoginUser user = getLoginUser();
        if (user == null) {
            throw new BusinessException(401, "未登录或登录已过期");
        }
        return user;
    }

    public static Long getUserId() {
        LoginUser user = getLoginUser();
        return user == null ? null : user.getUserId();
    }

    public static String getUsername() {
        LoginUser user = getLoginUser();
        return user == null ? null : user.getUsername();
    }

    public static Long getDeptId() {
        LoginUser user = getLoginUser();
        return user == null ? null : user.getDeptId();
    }

    public static boolean isAdmin() {
        LoginUser user = getLoginUser();
        return user != null && user.isAdmin();
    }

    public static boolean hasPermi(String permi) {
        LoginUser user = getLoginUser();
        if (user == null || user.getPermissions() == null) {
            return false;
        }
        return user.isAdmin() || user.getPermissions().contains(permi) || user.getPermissions().contains("*");
    }
}
