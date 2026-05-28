package com.edu.enrollment.dto;

import lombok.Data;

import java.util.List;

@Data
public class BatchAuditDTO {

    private List<Long> registrationIds;  // 批量审核的报名记录ID列表

    private Boolean passed;               // 审核结果：true-通过，false-拒绝

    private String comment;               // 审核意见
}