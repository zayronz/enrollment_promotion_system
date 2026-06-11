package com.edu.enrollment.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@Data
public class RegistrationSubmitDTO {
    @NotNull(message = "活动ID不能为空")
    private Long activityId;

    private String targetSchool;   // 目标学校
    private Integer score;         // 成绩/绩点
    private Map<String, Object> formData;  // 自定义表单数据
    private List<Map<String, Object>> customFields;  // 前端动态表单字段
    private List<String> fileIds;   // 附件路径列表
}
