package com.dong.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dong.dto.LoginDTO;
import com.dong.entity.User;
import com.dong.exception.BusinessException;
import com.dong.mapper.LoginMapper;
import com.dong.security.CustomUserDetails;
import com.dong.service.ILoginService;
import com.dong.utils.JwtUtils;
import com.dong.utils.RedisConstants;
import com.dong.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Slf4j
@Service
public class LoginServiceImpl extends ServiceImpl<LoginMapper, User> implements ILoginService {

    private final AuthenticationManager authenticationManager; // 认证管理器
    private final JwtUtils jwtUtils; // JWT工具类

    private final StringRedisTemplate stringRedisTemplate; // Redis

    /**
     * 密码登录
     */
    @Override
    public LoginVO login(LoginDTO loginDto) {
        // 1. 认证
        Authentication authentication = authenticate(loginDto);

        // 2. 获取用户详情
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // 3. 生成 Token（不放密码！）
        String token = null;
        if (userDetails != null) {
            token = generateToken(userDetails);
        }
        // 4. 返回登录信息
        return buildLoginVO(userDetails, token);
    }

    /**
     * 执行认证
     */
    private Authentication authenticate(LoginDTO dto) {
        try {
            UsernamePasswordAuthenticationToken authDto =
                    new UsernamePasswordAuthenticationToken(
                            dto.getUsername(),
                            dto.getPassword()
                    );
            return authenticationManager.authenticate(authDto);
        } catch (BadCredentialsException e) {
            log.warn("登录失败：用户名或密码错误 - {}", dto.getUsername());
            throw new BusinessException("用户名或密码错误");
        }
    }

    /**
     * 生成 JWT Token
     */
    private String generateToken(CustomUserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userDetails.getId());
        claims.put("username", userDetails.getUsername());

        return jwtUtils.generateToken(claims);
    }

    /**
     * 构建登录响应 VO ，并把数据存入Redis
     */
    private LoginVO buildLoginVO(CustomUserDetails userDetails, String token) {
        log.info("登录成功：userId={}, username={}",
                userDetails.getId(), userDetails.getUsername());

        // 从token中获取用户id
        Long userId = jwtUtils.getUserIdFromToken(token);

        //数据存入Redis
        String loginKey = RedisConstants.LOGIN_USER_KEY + userId;
        Map<String, Object> userMap = BeanUtil.beanToMap(
                userDetails, //要转换的对象
                "username","nickname","avatar","email" //只转换这些字段
        );
        stringRedisTemplate.opsForHash().putAll(loginKey,userMap);
        stringRedisTemplate.expire(loginKey, RedisConstants.LOGIN_USER_TTL, TimeUnit.HOURS);

        //构建返回数据
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setId(userDetails.getId());
        vo.setUsername(userDetails.getUsername());
        vo.setNickname(userDetails.getNickname());
        vo.setAvatar(userDetails.getAvatar());
        vo.setEmail(userDetails.getEmail());
        return vo;
    }
}
