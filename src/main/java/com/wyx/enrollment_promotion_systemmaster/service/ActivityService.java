package com.wyx.enrollment_promotion_systemmaster.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wyx.enrollment_promotion_systemmaster.dto.ActivityDTO;
import com.wyx.enrollment_promotion_systemmaster.entity.Activity;
import com.wyx.enrollment_promotion_systemmaster.exception.BusinessException;
import com.wyx.enrollment_promotion_systemmaster.mapper.ActivityMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ActivityService extends ServiceImpl<ActivityMapper, Activity> {

    public Page<ActivityDTO> getActivityList(Integer pageNum, Integer pageSize, Integer activityType, Integer status) {
        Page<Activity> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();

        if (activityType != null) {
            wrapper.eq(Activity::getActivityType, activityType);
        }
        if (status != null) {
            wrapper.eq(Activity::getStatus, status);
        }

        wrapper.orderByDesc(Activity::getCreateTime);
        Page<Activity> result = this.page(page, wrapper);

        Page<ActivityDTO> dtoPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        dtoPage.setRecords(result.getRecords().stream().map(this::convertToDTO).toList());

        return dtoPage;
    }

    public List<ActivityDTO> getAvailableActivities(Long userId, Integer role) {
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Activity::getStatus, 1)
                .le(Activity::getRegistrationStartTime, LocalDateTime.now())
                .ge(Activity::getRegistrationEndTime, LocalDateTime.now());

        List<Activity> activities = this.list(wrapper);
        return activities.stream().map(this::convertToDTO).toList();
    }

    public ActivityDTO getActivityById(Long id) {
        Activity activity = this.getById(id);
        if (activity == null) {
            throw new BusinessException("Activity not found");
        }
        return convertToDTO(activity);
    }

    public ActivityDTO createActivity(ActivityDTO dto, Long creatorId, String creatorName) {
        Activity activity = new Activity();
        BeanUtils.copyProperties(dto, activity);
        activity.setCreatorId(creatorId);
        activity.setCreatorName(creatorName);
        activity.setStatus(0);

        this.save(activity);
        return convertToDTO(activity);
    }

    public ActivityDTO updateActivity(Long id, ActivityDTO dto) {
        Activity activity = this.getById(id);
        if (activity == null) {
            throw new BusinessException("Activity not found");
        }

        BeanUtils.copyProperties(dto, activity, "id", "creatorId", "creatorName", "createTime");
        this.updateById(activity);
        return convertToDTO(activity);
    }

    public void deleteActivity(Long id) {
        if (!this.removeById(id)) {
            throw new BusinessException("Activity not found");
        }
    }

    public void publishActivity(Long id) {
        Activity activity = this.getById(id);
        if (activity == null) {
            throw new BusinessException("Activity not found");
        }
        activity.setStatus(1);
        this.updateById(activity);
    }

    private ActivityDTO convertToDTO(Activity activity) {
        ActivityDTO dto = new ActivityDTO();
        BeanUtils.copyProperties(activity, dto);
        if (activity.getStartTime() != null) {
            dto.setStartTime(activity.getStartTime().toString());
        }
        if (activity.getEndTime() != null) {
            dto.setEndTime(activity.getEndTime().toString());
        }
        if (activity.getRegistrationStartTime() != null) {
            dto.setRegistrationStartTime(activity.getRegistrationStartTime().toString());
        }
        if (activity.getRegistrationEndTime() != null) {
            dto.setRegistrationEndTime(activity.getRegistrationEndTime().toString());
        }
        if (activity.getCreateTime() != null) {
            dto.setCreateTime(activity.getCreateTime().toString());
        }
        return dto;
    }
}
