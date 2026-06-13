package com.dong.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dong.dto.LoginDTO;
import com.dong.entity.User;
import com.dong.vo.LoginVO;


public interface ILoginService extends IService<User> {

    LoginVO login(LoginDTO loginDto);
}
