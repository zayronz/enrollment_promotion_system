package com.edu.enrollment.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 轮播图 VO
 */
@Data
public class BannerVO {
    
    private Long id;
    
    /**
     * 关联活动ID
     */
    private Long activityId;
    
    /**
     * 活动名称
     */
    private String activityName;
    
    /**
     * 图片URL
     */
    private String imageUrl;
    
    /**
     * 完整访问URL（包含前缀）
     */
    private String fullImageUrl;
    
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
