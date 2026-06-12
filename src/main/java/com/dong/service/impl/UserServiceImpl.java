package com.dong.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.dong.common.Result;
import com.dong.dto.RegisterDTO;
import com.dong.entity.User;
import com.dong.mapper.UserMapper;
import com.dong.security.CustomUserDetails;
import com.dong.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dong.utils.RedisConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;


import static com.dong.utils.SecurityUtils.getCurrentUser;

/**
 * 用户表 服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 登出
     */
    @Override
    public Result<String> logout() {
        //1. 调用封装好的方法获取当前用户ID
        CustomUserDetails userDetails = getCurrentUser();
        Long userId = userDetails.getId();

        // 检查 userId 是否为 null
        if (userId == null) {
            log.warn("登出失败：用户ID为空");
            return Result.error("用户信息异常");

        }
        //2. 根据id删除用户缓存
        String loginKey = RedisConstants.LOGIN_USER_KEY + userId;
        stringRedisTemplate.delete(loginKey);

        log.info("用户登出成功：userId={}", userId);

        //3. 返回结果
        return Result.success("登出成功");
    }


    @Override
    public Result<String> register(RegisterDTO registerDto) {
        String username = registerDto.getUsername();

        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = getOne( wrapper);
        if (user != null) {
            return Result.error("用户已存在");
        }

        user = new User();
        user.setUsername(username);
        user.setPassword(registerDto.getPassword());
        user.setNickname(registerDto.getNickname());
        user.setEmail(registerDto.getEmail());

        boolean save = save(user);
        if (!save) {
            return Result.error("注册失败");
        }

        return Result.success("注册成功");
    }

}