package com.edu.enrollment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("registration")
public class RegistrationEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long activityId;
    private Long userId;
    private Integer userType;
    private String targetSchool;
    private Integer score;
    private String formData;
    private Integer status;
    private String currentNode;
    private String rejectReason;
    private String groupName;
    private Integer groupRank;
}