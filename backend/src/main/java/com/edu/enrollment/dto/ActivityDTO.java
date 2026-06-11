package com.edu.enrollment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class ActivityDTO {
    @NotBlank(message = "活动名称不能为空")
    private String name;

    private String description;

    @NotNull(message = "活动类型不能为空")
    private Integer type;

    private String location;

    @NotNull(message = "活动开始时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime activityStartTime;

    @NotNull(message = "活动结束时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime activityEndTime;

    @NotNull(message = "报名开始时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registrationStartTime;

    @NotNull(message = "报名结束时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registrationEndTime;

    private String bannerUrl;
    private List<String> bannerUrls;  // 多张轮播图
    private String videoUrl;
    private String coverImage;
    private List<String> attachments;  // 附件路径列表

    private List<String> auditFlow;      // 审批流程
    private List<CustomField> customFields;  // 自定义字段

    private Integer maxStudentPerSchool;
    private Integer maxTeacherPerSchool;
    private Boolean autoGroup;

    // 学生端资格可见性条件：低于要求的学生不显示该活动
    private BigDecimal minGpa;
    private BigDecimal minScore;

    // 参与人群规则：为空表示不限制
    private List<Long> allowedCollegeIds;
    private List<String> allowedUsernames;

    // 反馈提交截止时间：超过后不可提交反馈
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime feedbackDeadline;
}
