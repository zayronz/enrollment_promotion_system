package com.edu.enrollment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("registration")
public class RegistrationEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long activityId;       // 活动ID
    private Long userId;           // 报名用户ID
    private Integer userType;      // 0-学生 1-老师

    private String targetSchool;   // 招生对象学校（标准化后）
    private Integer score;         // 成绩/绩点
    private String formData;       // 自定义表单数据JSON

    private Integer status;        // 0-待审核 1-学院通过 2-学校通过 3-已拒绝 4-已撤回
    private String rejectReason;   // 拒绝原因

    private String groupName;      // 分组名称（按学校）
    private Integer groupRank;     // 组内排名

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}