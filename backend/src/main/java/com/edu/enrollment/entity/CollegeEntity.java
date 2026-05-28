package com.edu.enrollment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("college")
public class CollegeEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;   // 学院名称

    private String code;   // 学院代码
}