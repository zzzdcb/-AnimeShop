package com.dong.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dong.entity.User;
import com.dong.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@NullMarked // JSpecify提供注解，整个类默认所有参数和返回值都不能为 null
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserMapper userMapper;

    /**
     * 根据用户名从数据库查询用户（用于登录）
     */
    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        log.info("查询用户：{}", username);

        // 使用 MyBatis-Plus 条件构造器查询
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);

        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            log.warn("用户不存在：{}", username);
            throw new UsernameNotFoundException("用户不存在：" + username);
        }

        log.info("用户查询成功：{}，角色：{}", username, user.getRole());

        return new CustomUserDetails(user);
    }

    /**
     * 根据用户ID查询（用于 JWT 过滤器）
     */
    public UserDetails loadUserById(Long userId) {
        User user = userMapper.selectById(userId);

        if (user == null) {
            throw new UsernameNotFoundException("用户不存在：" + userId);
        }

        return new CustomUserDetails(user);
    }
}
