package com.edu.enrollment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("activity")
public class ActivityEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;           // 活动名称
    private String description;    // 活动简介（富文本）
    private Integer type;          // 0-线上 1-线下
    private String location;       // 活动地点

    private LocalDateTime activityStartTime;   // 活动开始时间
    private LocalDateTime activityEndTime;     // 活动结束时间
    private LocalDateTime registrationStartTime; // 报名开始时间
    private LocalDateTime registrationEndTime;   // 报名结束时间

    private String bannerUrl;      // 轮播图URL
    private String videoUrl;       // 视频URL

    private Integer status;        // 0-草稿 1-发布 2-已结束

    private String auditFlow;      // 审批流程配置JSON

    private Integer maxStudentPerSchool; // 每所学校最多学生数
    private Integer maxTeacherPerSchool; // 每所学校最多教师数

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}