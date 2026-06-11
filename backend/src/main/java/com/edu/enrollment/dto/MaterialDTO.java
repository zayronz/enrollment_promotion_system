package com.edu.enrollment.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class MaterialDTO {

    @NotBlank(message = "资料名称不能为空")
    private String name;

    @NotBlank(message = "资料分类不能为空")
    private String category;

    private String description;
}
