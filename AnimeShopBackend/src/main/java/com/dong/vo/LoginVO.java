package com.dong.vo;

import lombok.Data;

@Data
public class LoginVO {

    private String token; // 登录成功返回的token
    private Long id; // 用户id
    private String username; // 用户名
    private String nickname; // 昵称
    private String avatar; // 头像
    private String email; // 邮箱
}
