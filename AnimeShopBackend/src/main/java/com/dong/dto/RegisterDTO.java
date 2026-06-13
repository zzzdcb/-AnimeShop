package com.dong.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterDTO {

    @NotBlank(message = "用户名不能为空") // 非空注解
    @Size(min = 4, max = 20 , message = "用户名长度必须在4-20之间") // 长度注解
    private String username; // 用户名

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20 , message = "密码长度必须在6-20之间")
    private String password; // 密码

    private String email; // 邮箱
    private String nickname; // 昵称
}
