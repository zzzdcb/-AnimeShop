package com.dong.security;

import cn.hutool.core.bean.BeanUtil;
import com.dong.entity.User;
import com.dong.utils.JwtUtils;
import com.dong.utils.RedisConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.TimeoutUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.dong.utils.RedisConstants.LOGIN_USER_TTL;

@Slf4j
@Component  // 使用Spring注解，方便依赖注入
@RequiredArgsConstructor
public class TokenFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils; //JWT令牌工具类
    private final UserDetailsServiceImpl userDetailsService; //用户详情服务

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 过滤器逻辑
     */
    @Override
    // @NonNull注解表示该参数不能为null
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        // 1. 从请求中获取Token
        String token = getTokenFromRequest(request);
        // 2. 验证 Token是否正确
        // 2.1 Token为空，直接放行
        if (!StringUtils.hasText( token)) {
            log.info("Token为空，直接放行");
            filterChain.doFilter(request, response);
            return;
        }
        // 2.2 Token 过期
        if (!jwtUtils.validateToken(token)) {
            log.warn("Token已过期");
            // 返回错误信息
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"code\":401,\"message\":\"Token已过期\"}");
            return;
        }
        // 3 Token 正确，从缓存中获取用户ID并获取用户详情数据
        Long userId = jwtUtils.getUserIdFromToken(token);
        CustomUserDetails userDetails = (CustomUserDetails) getUserDetailsFromRedis(userId);

        // 4. 存入SecurityContextHolder中
        // 4.1 获取认证对象
        UsernamePasswordAuthenticationToken authentication =
                null; // 权限
        if (userDetails != null) {
            userDetails.setId(userId); // 设置用户ID
            authentication = new UsernamePasswordAuthenticationToken(
                    userDetails // 登录用户详情
                    , null // 密码
                    , userDetails.getAuthorities());
        }
        // 4.2 存入SecurityContextHolder中
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 5. 放行
        filterChain.doFilter(request, response);
    }


    /**
     * 从请求中获取Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 获取用户详情
     */
    private UserDetails getUserDetailsFromRedis(Long userId) {
        // 1.从Redis中获取用户信息
        String loginKey = RedisConstants.LOGIN_USER_KEY + userId;
        Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(loginKey);

        // 2.缓存不为空，直接封装成UserDetails对象返回
        if (userMap != null && !userMap.isEmpty()){
            return BeanUtil.toBean(userMap, CustomUserDetails.class);
        }

        // 3.缓存为空，直接返回null，方便登出
        return null;
    }
}