package com.edu.enrollment.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edu.enrollment.dto.BannerDTO;
import com.edu.enrollment.entity.ActivityEntity;
import com.edu.enrollment.entity.BannerEntity;
import com.edu.enrollment.mapper.ActivityMapper;
import com.edu.enrollment.mapper.BannerMapper;
import com.edu.enrollment.vo.BannerVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 轮播图服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BannerService {
    
    private final BannerMapper bannerMapper;
    private final ActivityMapper activityMapper;
    
    /**
     * 创建轮播图
     */
    @Transactional
    public BannerVO create(BannerDTO dto) {
        log.info("【轮播图服务】创建轮播图: activityId={}, imageUrl={}", dto.getActivityId(), dto.getImageUrl());
        
        BannerEntity entity = new BannerEntity();
        BeanUtils.copyProperties(dto, entity);
        
        // 设置默认排序值
        if (entity.getSortOrder() == null) {
            entity.setSortOrder(0);
        }
        
        // 设置默认状态
        if (entity.getStatus() == null) {
            entity.setStatus(1);
        }
        
        bannerMapper.insert(entity);
        log.info("【轮播图服务】轮播图创建成功: id={}", entity.getId());
        
        return toVO(entity);
    }
    
    /**
     * 更新轮播图
     */
    @Transactional
    public BannerVO update(Long id, BannerDTO dto) {
        log.info("【轮播图服务】更新轮播图: id={}", id);
        
        BannerEntity entity = bannerMapper.selectById(id);
        if (entity == null) {
            throw new RuntimeException("轮播图不存在，ID: " + id);
        }
        
        BeanUtils.copyProperties(dto, entity);
        bannerMapper.updateById(entity);
        log.info("【轮播图服务】轮播图更新成功: id={}", id);
        
        return toVO(entity);
    }
    
    /**
     * 删除轮播图
     */
    @Transactional
    public void delete(Long id) {
        log.info("【轮播图服务】删除轮播图: id={}", id);
        
        BannerEntity entity = bannerMapper.selectById(id);
        if (entity == null) {
            throw new RuntimeException("轮播图不存在，ID: " + id);
        }
        
        bannerMapper.deleteById(id);
        log.info("【轮播图服务】轮播图删除成功: id={}", id);
    }
    
    /**
     * 查询轮播图详情
     */
    public BannerVO getById(Long id) {
        log.info("【轮播图服务】查询轮播图详情: id={}", id);
        
        BannerEntity entity = bannerMapper.selectById(id);
        if (entity == null) {
            throw new RuntimeException("轮播图不存在，ID: " + id);
        }
        
        return toVO(entity);
    }
    
    /**
     * 分页查询轮播图列表
     */
    public Page<BannerVO> listPage(int pageNum, int pageSize, Long activityId, Integer status) {
        log.info("【轮播图服务】分页查询轮播图: pageNum={}, pageSize={}, activityId={}, status={}", 
                pageNum, pageSize, activityId, status);
        
        Page<BannerEntity> page = new Page<>(pageNum, pageSize);
        QueryWrapper<BannerEntity> wrapper = new QueryWrapper<>();
        
        if (activityId != null) {
            wrapper.eq("activity_id", activityId);
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        
        wrapper.orderByAsc("sort_order").orderByDesc("create_time");
        
        Page<BannerEntity> result = bannerMapper.selectPage(page, wrapper);
        
        // 转换为 VO
        Page<BannerVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<BannerVO> voList = new ArrayList<>();
        for (BannerEntity entity : result.getRecords()) {
            voList.add(toVO(entity));
        }
        voPage.setRecords(voList);
        
        return voPage;
    }
    
    /**
     * 获取所有启用的轮播图（用于前端展示）
     */
    public List<BannerVO> getActiveBanners() {
        log.info("【轮播图服务】获取所有启用的轮播图");
        
        QueryWrapper<BannerEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1); // 只查询显示的
        wrapper.orderByAsc("sort_order").orderByDesc("create_time");
        
        List<BannerEntity> entities = bannerMapper.selectList(wrapper);
        
        List<BannerVO> voList = new ArrayList<>();
        for (BannerEntity entity : entities) {
            voList.add(toVO(entity));
        }
        
        return voList;
    }
    
    /**
     * 根据活动ID获取轮播图列表
     */
    public List<BannerVO> getByActivityId(Long activityId) {
        log.info("【轮播图服务】根据活动ID获取轮播图: activityId={}", activityId);
        
        QueryWrapper<BannerEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("activity_id", activityId);
        wrapper.orderByAsc("sort_order").orderByDesc("create_time");
        
        List<BannerEntity> entities = bannerMapper.selectList(wrapper);
        
        List<BannerVO> voList = new ArrayList<>();
        for (BannerEntity entity : entities) {
            voList.add(toVO(entity));
        }
        
        return voList;
    }
    
    /**
     * 批量创建轮播图
     */
    @Transactional
    public List<BannerVO> batchCreate(List<BannerDTO> dtoList) {
        log.info("【轮播图服务】批量创建轮播图: count={}", dtoList.size());
        
        List<BannerVO> result = new ArrayList<>();
        for (BannerDTO dto : dtoList) {
            result.add(create(dto));
        }
        
        return result;
    }
    
    /**
     * 更新轮播图状态
     */
    @Transactional
    public BannerVO updateStatus(Long id, Integer status) {
        log.info("【轮播图服务】更新轮播图状态: id={}, status={}", id, status);
        
        BannerEntity entity = bannerMapper.selectById(id);
        if (entity == null) {
            throw new RuntimeException("轮播图不存在，ID: " + id);
        }
        
        entity.setStatus(status);
        bannerMapper.updateById(entity);
        
        return toVO(entity);
    }
    
    /**
     * 实体转 VO
     */
    private BannerVO toVO(BannerEntity entity) {
        BannerVO vo = new BannerVO();
        BeanUtils.copyProperties(entity, vo);
        
        // 获取活动名称
        if (entity.getActivityId() != null) {
            ActivityEntity activity = activityMapper.selectById(entity.getActivityId());
            if (activity != null) {
                vo.setActivityName(activity.getName());
            }
        }
        
        // 设置完整的图片访问 URL
        if (entity.getImageUrl() != null && !entity.getImageUrl().isEmpty()) {
            String fullUrl = "/api/file/view/" + entity.getImageUrl();
            vo.setFullImageUrl(fullUrl);
        }
        
        return vo;
    }
}
