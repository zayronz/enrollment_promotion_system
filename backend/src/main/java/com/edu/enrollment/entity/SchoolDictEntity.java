package com.edu.enrollment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("school_dict")
public class SchoolDictEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String standardName;

    private String aliasNames;

    private String province;

    private String city;
}