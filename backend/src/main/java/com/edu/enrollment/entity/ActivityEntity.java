package com.edu.enrollment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("activity")
public class ActivityEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private String description;
    private Integer type;
    private String location;
    private LocalDateTime activityStartTime;
    private LocalDateTime activityEndTime;
    private LocalDateTime registrationStartTime;
    private LocalDateTime registrationEndTime;
    private String bannerUrl;
    private String videoUrl;
    private String coverImage;
    private Integer status;
    private String auditFlow;
    private String customFields;
    private Integer maxStudentPerSchool;
    private Integer maxTeacherPerSchool;
    private Integer autoGroup;
    private Long creatorId;
    private Integer showOnHome;  // 0-不展示 1-展示
}