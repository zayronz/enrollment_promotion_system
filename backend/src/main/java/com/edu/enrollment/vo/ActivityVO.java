package com.edu.enrollment.vo;

import cn.hutool.json.JSONArray;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class ActivityVO {

    private Long id;

    private String name;

    private String description;

    private Integer type;  // 0-线上 1-线下

    private String location;

    private LocalDateTime activityStartTime;

    private LocalDateTime activityEndTime;

    private LocalDateTime registrationStartTime;

    private LocalDateTime registrationEndTime;

    private String bannerUrl;

    private String videoUrl;

    private String coverImage;

    private Integer status;  // 0-草稿 1-发布 2-已结束

    private List<String> auditFlow;  // 审批流程

    private JSONArray customFields;  // 自定义收集字段JSON

    private Integer maxStudentPerSchool;

    private Integer maxTeacherPerSchool;

    private Boolean autoGroup;  // 是否自动分组

    private Long creatorId;

    private String creatorName;  // 创建人姓名（可选）

    private Integer registrationCount;  // 报名人数（可选）

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer showOnHome;  // 0-不展示 1-展示

private List<Map<String, Object>> attachments;  // 附件列表
}