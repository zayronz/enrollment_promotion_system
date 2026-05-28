package com.edu.enrollment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("attachment")
public class AttachmentEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String fileName;      // 原文件名

    private String filePath;      // 存储路径

    private Long fileSize;        // 文件大小(字节)

    private String fileType;      // 文件类型

    private Long relatedId;       // 关联记录ID

    private String relatedType;   // 关联类型(registration/feedback/audit)

    private Long uploaderId;      // 上传人ID
}