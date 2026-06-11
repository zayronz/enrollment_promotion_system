package com.edu.enrollment.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserUpdateDTO {

    private String realName;
    private String email;
    private String phone;
    private String role;
    private Long collegeId;
    private Integer grade;
    private BigDecimal gpa;
    private Integer status;
}
