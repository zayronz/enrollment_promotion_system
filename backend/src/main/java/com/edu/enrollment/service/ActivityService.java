package com.edu.enrollment.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edu.enrollment.dto.ActivityDTO;
import com.edu.enrollment.entity.ActivityEntity;
import com.edu.enrollment.entity.UserEntity;
import com.edu.enrollment.exception.BusinessException;
import com.edu.enrollment.mapper.ActivityMapper;
import com.edu.enrollment.vo.ActivityVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityMapper activityMapper;
    private final UserService userService;

    // 根据ID获取活动实体
    public ActivityEntity getById(Long id) {
        return activityMapper.selectById(id);
    }

    public Page<ActivityVO> getActivityList(Integer page, Integer size, Integer type, String keyword, Long userId) {
        UserEntity currentUser = userService.getById(userId);
        LambdaQueryWrapper<ActivityEntity> wrapper = new LambdaQueryWrapper<>();

        if (type != null) {
            wrapper.eq(ActivityEntity::getType, type);
        }
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.like(ActivityEntity::getName, keyword);
        }

        // 非管理员只能看到已发布的活动
        if (!"SCHOOL".equals(currentUser.getRole())) {
            wrapper.eq(ActivityEntity::getStatus, 1);
        }

        wrapper.orderByDesc(ActivityEntity::getActivityStartTime);

        Page<ActivityEntity> entityPage = activityMapper.selectPage(new Page<>(page, size), wrapper);

        Page<ActivityVO> voPage = new Page<>();
        voPage.setCurrent(entityPage.getCurrent());
        voPage.setSize(entityPage.getSize());
        voPage.setTotal(entityPage.getTotal());
        voPage.setRecords(entityPage.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList()));

        return voPage;
    }

    public ActivityVO getDetail(Long id) {
        ActivityEntity entity = activityMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("活动不存在");
        }
        return toVO(entity);
    }

    @Transactional
    public Long createActivity(ActivityDTO dto, Long creatorId) {
        ActivityEntity entity = new ActivityEntity();
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setType(dto.getType());
        entity.setLocation(dto.getLocation());
        entity.setActivityStartTime(dto.getActivityStartTime());
        entity.setActivityEndTime(dto.getActivityEndTime());
        entity.setRegistrationStartTime(dto.getRegistrationStartTime());
        entity.setRegistrationEndTime(dto.getRegistrationEndTime());
        entity.setBannerUrl(dto.getBannerUrl());
        entity.setBannerUrls(JSONUtil.toJsonStr(dto.getBannerUrls()));
        entity.setVideoUrl(dto.getVideoUrl());
        entity.setCoverImage(dto.getCoverImage());
        entity.setStatus(0); // 草稿
        entity.setAuditFlow(JSONUtil.toJsonStr(dto.getAuditFlow()));
        entity.setCustomFields(JSONUtil.toJsonStr(dto.getCustomFields()));
        entity.setMaxStudentPerSchool(dto.getMaxStudentPerSchool());
        entity.setMaxTeacherPerSchool(dto.getMaxTeacherPerSchool());
        entity.setAutoGroup(dto.getAutoGroup() ? 1 : 0);
        entity.setCreatorId(creatorId);

        activityMapper.insert(entity);
        return entity.getId();
    }

    @Transactional
    public void publishActivity(Long id, Long userId) {
        ActivityEntity entity = activityMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("活动不存在");
        }
        if (!entity.getCreatorId().equals(userId)) {
            throw new BusinessException("只有创建者可以发布活动");
        }
        entity.setStatus(1);
        activityMapper.updateById(entity);
    }

    @Transactional
    public void updateActivity(Long id, ActivityDTO dto, Long userId) {
        ActivityEntity entity = activityMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("活动不存在");
        }
        if (!entity.getCreatorId().equals(userId)) {
            throw new BusinessException("只有创建者可以编辑活动");
        }
        // 已发布的活动允许修改

        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setType(dto.getType());
        entity.setLocation(dto.getLocation());
        entity.setActivityStartTime(dto.getActivityStartTime());
        entity.setActivityEndTime(dto.getActivityEndTime());
        entity.setRegistrationStartTime(dto.getRegistrationStartTime());
        entity.setRegistrationEndTime(dto.getRegistrationEndTime());
        entity.setBannerUrl(dto.getBannerUrl());
        entity.setBannerUrls(JSONUtil.toJsonStr(dto.getBannerUrls()));
        entity.setVideoUrl(dto.getVideoUrl());
        entity.setCoverImage(dto.getCoverImage());
        entity.setAuditFlow(JSONUtil.toJsonStr(dto.getAuditFlow()));
        entity.setCustomFields(JSONUtil.toJsonStr(dto.getCustomFields()));
        entity.setMaxStudentPerSchool(dto.getMaxStudentPerSchool());
        entity.setMaxTeacherPerSchool(dto.getMaxTeacherPerSchool());
        entity.setAutoGroup(dto.getAutoGroup() ? 1 : 0);

        activityMapper.updateById(entity);
    }

    @Transactional
    public void deleteActivity(Long id, Long userId) {
        ActivityEntity entity = activityMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("活动不存在");
        }
        // 只有创建者或管理员可以删除活动
        UserEntity currentUser = userService.getById(userId);
        boolean isCreator = entity.getCreatorId().equals(userId);
        boolean isAdmin = "SCHOOL".equals(currentUser.getRole());
        
        if (!isCreator && !isAdmin) {
            throw new BusinessException("只有创建者或管理员可以删除活动");
        }
        activityMapper.deleteById(id);
    }

    @Transactional
    public void setHomeShow(Long id, Boolean show, Long userId) {
        ActivityEntity entity = activityMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("活动不存在");
        }
        entity.setShowOnHome(show ? 1 : 0);
        activityMapper.updateById(entity);
    }

    public List<ActivityVO> getOpenActivities() {
        List<ActivityEntity> entities = activityMapper.selectOpenActivities();
        return entities.stream().map(this::toVO).collect(Collectors.toList());
    }

    private ActivityVO toVO(ActivityEntity entity) {
        ActivityVO vo = new ActivityVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setDescription(entity.getDescription());
        vo.setType(entity.getType());
        vo.setLocation(entity.getLocation());
        vo.setActivityStartTime(entity.getActivityStartTime());
        vo.setActivityEndTime(entity.getActivityEndTime());
        vo.setRegistrationStartTime(entity.getRegistrationStartTime());
        vo.setRegistrationEndTime(entity.getRegistrationEndTime());
        vo.setBannerUrl(entity.getBannerUrl());
        vo.setBannerUrls(StrUtil.isNotBlank(entity.getBannerUrls())
                ? JSONUtil.toList(entity.getBannerUrls(), String.class) : null);
        vo.setVideoUrl(entity.getVideoUrl());
        vo.setCoverImage(entity.getCoverImage());
        vo.setStatus(entity.getStatus());
        // 处理可能为 null 的 JSON 字段
        vo.setAuditFlow(StrUtil.isNotBlank(entity.getAuditFlow())
                ? JSONUtil.toList(entity.getAuditFlow(), String.class) : null);
        vo.setCustomFields(StrUtil.isNotBlank(entity.getCustomFields())
                ? JSONUtil.parseArray(entity.getCustomFields()) : null);
        vo.setMaxStudentPerSchool(entity.getMaxStudentPerSchool());
        vo.setMaxTeacherPerSchool(entity.getMaxTeacherPerSchool());
        vo.setAutoGroup(entity.getAutoGroup() == 1);
        vo.setShowOnHome(entity.getShowOnHome() != null ? entity.getShowOnHome() : 0);
        return vo;
    }
}