package com.dong.config;

import com.dong.security.TokenFilter;
import com.dong.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity // 开启 Spring Security
@EnableMethodSecurity(prePostEnabled = true)  // 开启方法级权限控制
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final TokenFilter tokenFilter;

    /**
     * 配置 Spring Security 过滤器链
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. 禁用 CSRF（使用 JWT 不需要CSRF防护）
                .csrf(AbstractHttpConfigurer::disable)

                // 2. 无状态会话,不通过session获取用户信息
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 3. 请求授权配置
                .authorizeHttpRequests(auth -> auth
                        // 公开接口
                        .requestMatchers(
                                "/user/login", // 登录接口
                                "/user/register", // 注册接口
                                "/user/logout", // 登出接口
                                "/products/**", // 商品列表接口
                                "/seckill/{seckillId}/status", // 秒杀状态接口
                                "/categories", // 分类列表接口
                                "/home/**" // 首页接口
                        ).permitAll() // 允许公开访问
                        .anyRequest().authenticated() // 其他所有请求需要认证
                )

                // 4. 配置认证提供者
                .authenticationProvider(authenticationProvider())

                // 5. 添加 JWT 过滤器，把tokenFilter添加到自带的过滤器之前
                .addFilterBefore(tokenFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 配置认证提供者
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        // 创建认证提供者
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        // 设置用户详情服务
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * 配置认证管理器
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();  // 企业标准：BCrypt加密
    }
}
