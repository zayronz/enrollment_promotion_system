package com.edu.enrollment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("feedback")
public class FeedbackEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long activityId;
    private Long userId;
    private String userRole;
    private String title;
    private String content;
    private String attachmentUrls;
    private Integer type;
    
    // 非数据库字段，用于返回活动名称
    @TableField(exist = false)
    private String activityTitle;
    
    // 非数据库字段，用于返回提交人姓名
    @TableField(exist = false)
    private String realName;
}