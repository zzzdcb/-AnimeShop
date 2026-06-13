package com.dong.controller;

import com.dong.common.Result;
import com.dong.dto.LoginDTO;
import com.dong.service.ILoginService;
import com.dong.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class LoginController {

    private final ILoginService loginService ;

    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginDTO loginDto) {
        LoginVO loginVO = loginService.login(loginDto);
        return Result.success(loginVO);
    }
}
