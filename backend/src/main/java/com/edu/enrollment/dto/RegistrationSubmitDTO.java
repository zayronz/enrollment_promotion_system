package com.edu.enrollment.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
public class RegistrationSubmitDTO {
    @NotNull(message = "活动ID不能为空")
    private Long activityId;

    private String targetSchool;   // 目标学校
    private Integer score;         // 成绩/绩点
    private Map<String, Object> formData;  // 自定义表单数据
}