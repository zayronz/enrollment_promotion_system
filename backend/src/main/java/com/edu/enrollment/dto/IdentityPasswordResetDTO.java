package com.edu.enrollment.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class IdentityPasswordResetDTO {

    @NotBlank(message = "注册邮箱或手机号不能为空")
    private String account;

    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
