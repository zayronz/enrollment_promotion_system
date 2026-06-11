package com.edu.enrollment.dto;

import lombok.Data;

/**
 * 轮播图 DTO
 */
@Data
public class BannerDTO {
    
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
}
