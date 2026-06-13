package com.dong.utils;

import com.dong.exception.BusinessException;
import com.dong.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    /**
     * 从SecurityContextHolder中获取当前登录用户详细信息
     */
    public static CustomUserDetails getCurrentUser() {
        // 从SecurityContextHolder中获取认证对象
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 检查是否已认证且是不是匿名用户
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException("用户未登录");
        }

        // 获取用户信息
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomUserDetails)) {
            throw new BusinessException("用户信息格式错误");
        }

        // 返回用户信息
        return (CustomUserDetails) principal;
    }
}
