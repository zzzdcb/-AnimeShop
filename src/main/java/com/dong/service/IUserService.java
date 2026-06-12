package com.dong.service;

import com.dong.common.Result;
import com.dong.dto.RegisterDTO;
import com.dong.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.Valid;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author author
 * @since 2026-05-03
 */
public interface IUserService extends IService<User> {

    Result<String> logout();

    Result<String> register(@Valid RegisterDTO registerDto);
}
