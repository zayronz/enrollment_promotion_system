package com.edu.enrollment.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;

@Data
public class AuditRequestDTO {
    @NotNull(message = "报名记录ID不能为空")
    private Long registrationId;

    @NotNull(message = "审核结果不能为空")
    private Boolean passed;

    private String comment;
    private String attachmentUrls;
}