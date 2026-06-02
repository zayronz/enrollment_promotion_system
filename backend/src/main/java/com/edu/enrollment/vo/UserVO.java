package com.edu.enrollment.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserVO {

    private Long id;

    private String username;

    private String realName;

    private String email;

    private String phone;

    private String role;

    private Long collegeId;

    private String collegeName;  // 学院名称（关联查询用）

    private Integer grade;

    private BigDecimal gpa;

    private Integer status;

    private String avatar;

    private LocalDateTime createTime;
}