package com.edu.enrollment.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;

@Data
public class FeedbackSubmitDTO {

    @NotNull(message = "活动ID不能为空")
    private Long activityId;

    @NotBlank(message = "反馈标题不能为空")
    private String title;

    private String content;

    private String attachmentUrls;

    private Integer type;  // 0-个人反馈 1-总结报告
}