package com.edu.enrollment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("audit_record")
public class AuditRecordEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long registrationId;
    private String node;
    private Long auditorId;
    private String auditorName;
    private Integer result;
    private String comment;
    private String attachmentUrls;
}