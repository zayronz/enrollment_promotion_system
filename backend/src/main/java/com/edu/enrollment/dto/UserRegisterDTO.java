package com.edu.enrollment.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class UserRegisterDTO {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    private String email;

    private String phone;

    @NotBlank(message = "角色不能为空")
    private String role;

    @NotNull(message = "学院不能为空")
    private Long collegeId;

    private Integer grade;

    private BigDecimal gpa;
}