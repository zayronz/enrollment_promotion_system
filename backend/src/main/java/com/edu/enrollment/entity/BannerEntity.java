package com.edu.enrollment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 轮播图实体类
 */
@Data
@TableName("banner")
public class BannerEntity {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 关联活动ID
     */
    private Long activityId;
    
    /**
     * 图片URL
     */
    private String imageUrl;
    
    /**
     * 跳转链接
     */
    private String linkUrl;
    
    /**
     * 排序（数字越小越靠前）
     */
    private Integer sortOrder;
    
    /**
     * 状态：0-隐藏，1-显示
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
