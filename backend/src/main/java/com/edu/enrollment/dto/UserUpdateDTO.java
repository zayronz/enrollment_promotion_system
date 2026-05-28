package com.edu.enrollment.dto;

import lombok.Data;

@Data
public class UserUpdateDTO {

    private String realName;
    private String email;
    private String phone;
    private String role;
    private Long collegeId;
    private Integer grade;
    private Integer status;
}
