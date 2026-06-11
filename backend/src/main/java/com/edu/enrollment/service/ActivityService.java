package com.edu.enrollment.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edu.enrollment.dto.ActivityDTO;
import com.edu.enrollment.entity.ActivityEntity;
import com.edu.enrollment.entity.UserEntity;
import com.edu.enrollment.exception.BusinessException;
import com.edu.enrollment.entity.AttachmentEntity;
import com.edu.enrollment.mapper.ActivityMapper;
import com.edu.enrollment.mapper.AttachmentMapper;
import com.edu.enrollment.vo.ActivityVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityMapper activityMapper;
    private final UserService userService;
    private final AttachmentMapper attachmentMapper;

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

        if ("STUDENT".equals(currentUser.getRole()) || "TEACHER".equals(currentUser.getRole())) {
            List<ActivityEntity> filteredEntities = activityMapper.selectList(wrapper).stream()
                    .filter(activity -> isVisibleForAudience(activity, currentUser))
                    .filter(activity -> !"STUDENT".equals(currentUser.getRole()) || isEligibleForStudent(activity, currentUser))
                    .collect(Collectors.toList());

            int fromIndex = Math.min((page - 1) * size, filteredEntities.size());
            int toIndex = Math.min(fromIndex + size, filteredEntities.size());

            Page<ActivityVO> voPage = new Page<>();
            voPage.setCurrent(page);
            voPage.setSize(size);
            voPage.setTotal(filteredEntities.size());
            voPage.setRecords(filteredEntities.subList(fromIndex, toIndex).stream()
                    .map(this::toVO)
                    .collect(Collectors.toList()));
            return voPage;
        }

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
        ActivityVO vo = toVO(entity);
        // 获取活动相关的附件
        List<AttachmentEntity> attachments = attachmentMapper.selectByRelated(id, "activity");
        vo.setAttachments(attachments.stream().map(this::toAttachmentVO).collect(Collectors.toList()));
        return vo;
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
        entity.setVideoUrl(dto.getVideoUrl());
        entity.setCoverImage(dto.getCoverImage());
        entity.setStatus(0); // 草稿
        entity.setAuditFlow(JSONUtil.toJsonStr(dto.getAuditFlow()));
        entity.setCustomFields(buildCustomFieldsJson(dto));
        entity.setMaxStudentPerSchool(dto.getMaxStudentPerSchool());
        entity.setMaxTeacherPerSchool(dto.getMaxTeacherPerSchool());
        entity.setAutoGroup(dto.getAutoGroup() ? 1 : 0);
        entity.setCreatorId(creatorId);

        activityMapper.insert(entity);

        // 保存附件记录
        if (dto.getAttachments() != null && !dto.getAttachments().isEmpty()) {
            for (String filePath : dto.getAttachments()) {
                AttachmentEntity attachment = new AttachmentEntity();
                attachment.setFileName(filePath.substring(filePath.lastIndexOf('/') + 1));
                attachment.setFilePath(filePath);
                attachment.setRelatedId(entity.getId());
                attachment.setRelatedType("activity");
                attachment.setUploaderId(creatorId);
                attachmentMapper.insert(attachment);
            }
        }

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
        entity.setVideoUrl(dto.getVideoUrl());
        entity.setCoverImage(dto.getCoverImage());
        entity.setAuditFlow(JSONUtil.toJsonStr(dto.getAuditFlow()));
        entity.setCustomFields(buildCustomFieldsJson(dto));
        entity.setMaxStudentPerSchool(dto.getMaxStudentPerSchool());
        entity.setMaxTeacherPerSchool(dto.getMaxTeacherPerSchool());
        entity.setAutoGroup(dto.getAutoGroup() ? 1 : 0);

        activityMapper.updateById(entity);

        // 更新附件记录
        if (dto.getAttachments() != null) {
            // 删除旧的附件记录
            attachmentMapper.delete(new LambdaQueryWrapper<AttachmentEntity>()
                    .eq(AttachmentEntity::getRelatedId, id)
                    .eq(AttachmentEntity::getRelatedType, "activity"));
            // 添加新的附件记录
            for (String filePath : dto.getAttachments()) {
                AttachmentEntity attachment = new AttachmentEntity();
                attachment.setFileName(filePath.substring(filePath.lastIndexOf('/') + 1));
                attachment.setFilePath(filePath);
                attachment.setRelatedId(id);
                attachment.setRelatedType("activity");
                attachment.setUploaderId(userId);
                attachmentMapper.insert(attachment);
            }
        }
    }

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

    /**
     * 获取首页轮播展示的活动：已发布(status=1) 且 show_on_home=1，按开始时间降序
     */
    public List<ActivityVO> getBannerActivities() {
        LambdaQueryWrapper<ActivityEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityEntity::getStatus, 1)
               .eq(ActivityEntity::getShowOnHome, 1)
               .orderByDesc(ActivityEntity::getActivityStartTime);
        List<ActivityEntity> entities = activityMapper.selectList(wrapper);
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
        vo.setVideoUrl(entity.getVideoUrl());
        vo.setCoverImage(entity.getCoverImage());
        vo.setStatus(entity.getStatus());
        // 处理可能为 null 的 JSON 字段
        vo.setAuditFlow(StrUtil.isNotBlank(entity.getAuditFlow())
                ? JSONUtil.toList(entity.getAuditFlow(), String.class) : null);
        JSONArray customFields = StrUtil.isNotBlank(entity.getCustomFields())
                ? JSONUtil.parseArray(entity.getCustomFields()) : new JSONArray();
        applyEligibilityToVO(vo, customFields);
        applyMetaRulesToVO(vo, customFields);
        vo.setCustomFields(filterRegistrationFields(customFields));
        vo.setMaxStudentPerSchool(entity.getMaxStudentPerSchool());
        vo.setMaxTeacherPerSchool(entity.getMaxTeacherPerSchool());
        vo.setAutoGroup(entity.getAutoGroup() == 1);
        vo.setShowOnHome(entity.getShowOnHome() != null ? entity.getShowOnHome() : 0);
        return vo;
    }

    private String buildCustomFieldsJson(ActivityDTO dto) {
        JSONArray fields = new JSONArray();
        if (dto.getCustomFields() != null) {
            fields.addAll(dto.getCustomFields());
        }

        if (dto.getMinGpa() != null || dto.getMinScore() != null) {
            JSONObject eligibility = new JSONObject();
            eligibility.set("type", "eligibility_rule");
            eligibility.set("name", "__eligibility_rule__");
            eligibility.set("label", "资格条件");
            eligibility.set("minGpa", dto.getMinGpa());
            eligibility.set("minScore", dto.getMinScore());
            fields.add(eligibility);
        }

        if ((dto.getAllowedCollegeIds() != null && !dto.getAllowedCollegeIds().isEmpty())
                || (dto.getAllowedUsernames() != null && !dto.getAllowedUsernames().isEmpty())) {
            JSONObject audience = new JSONObject();
            audience.set("type", "audience_rule");
            audience.set("name", "__audience_rule__");
            audience.set("label", "参与人群");
            audience.set("allowedCollegeIds", dto.getAllowedCollegeIds());
            audience.set("allowedUsernames", dto.getAllowedUsernames());
            fields.add(audience);
        }

        if (dto.getFeedbackDeadline() != null) {
            JSONObject feedbackRule = new JSONObject();
            feedbackRule.set("type", "feedback_rule");
            feedbackRule.set("name", "__feedback_rule__");
            feedbackRule.set("label", "反馈规则");
            feedbackRule.set("feedbackDeadline", dto.getFeedbackDeadline().toString());
            fields.add(feedbackRule);
        }
        return JSONUtil.toJsonStr(fields);
    }

    private JSONArray filterRegistrationFields(JSONArray fields) {
        JSONArray result = new JSONArray();
        for (Object item : fields) {
            JSONObject field = JSONUtil.parseObj(item);
            String type = field.getStr("type");
            if (!"eligibility_rule".equals(type)
                    && !"audience_rule".equals(type)
                    && !"feedback_rule".equals(type)) {
                result.add(field);
            }
        }
        return result;
    }

    private JSONObject getEligibilityRule(ActivityEntity entity) {
        if (StrUtil.isBlank(entity.getCustomFields())) {
            return null;
        }
        JSONArray fields = JSONUtil.parseArray(entity.getCustomFields());
        for (Object item : fields) {
            JSONObject field = JSONUtil.parseObj(item);
            if ("eligibility_rule".equals(field.getStr("type"))) {
                return field;
            }
        }
        return null;
    }

    private void applyEligibilityToVO(ActivityVO vo, JSONArray fields) {
        for (Object item : fields) {
            JSONObject field = JSONUtil.parseObj(item);
            if ("eligibility_rule".equals(field.getStr("type"))) {
                vo.setMinGpa(field.getBigDecimal("minGpa"));
                vo.setMinScore(field.getBigDecimal("minScore"));
                return;
            }
        }
    }

    private void applyMetaRulesToVO(ActivityVO vo, JSONArray fields) {
        for (Object item : fields) {
            JSONObject field = JSONUtil.parseObj(item);
            if ("audience_rule".equals(field.getStr("type"))) {
                vo.setAllowedCollegeIds(toLongList(field.getJSONArray("allowedCollegeIds")));
                vo.setAllowedUsernames(toStringList(field.getJSONArray("allowedUsernames")));
            }
            if ("feedback_rule".equals(field.getStr("type"))) {
                String deadline = field.getStr("feedbackDeadline");
                if (StrUtil.isNotBlank(deadline)) {
                    vo.setFeedbackDeadline(LocalDateTime.parse(deadline));
                }
            }
        }
    }

    public LocalDateTime getFeedbackDeadline(Long activityId) {
        ActivityEntity entity = activityMapper.selectById(activityId);
        if (entity == null || StrUtil.isBlank(entity.getCustomFields())) {
            return null;
        }
        JSONArray fields = JSONUtil.parseArray(entity.getCustomFields());
        for (Object item : fields) {
            JSONObject field = JSONUtil.parseObj(item);
            if ("feedback_rule".equals(field.getStr("type"))) {
                String deadline = field.getStr("feedbackDeadline");
                return StrUtil.isBlank(deadline) ? null : LocalDateTime.parse(deadline);
            }
        }
        return null;
    }

    private boolean isVisibleForAudience(ActivityEntity activity, UserEntity user) {
        if (StrUtil.isBlank(activity.getCustomFields())) {
            return true;
        }
        JSONArray fields = JSONUtil.parseArray(activity.getCustomFields());
        for (Object item : fields) {
            JSONObject field = JSONUtil.parseObj(item);
            if ("audience_rule".equals(field.getStr("type"))) {
                List<Long> collegeIds = toLongList(field.getJSONArray("allowedCollegeIds"));
                List<String> usernames = toStringList(field.getJSONArray("allowedUsernames"));
                boolean hasCollegeRule = collegeIds != null && !collegeIds.isEmpty();
                boolean hasUserRule = usernames != null && !usernames.isEmpty();
                boolean collegeMatched = hasCollegeRule && user.getCollegeId() != null && collegeIds.contains(user.getCollegeId());
                boolean userMatched = hasUserRule && usernames.stream().anyMatch(name ->
                        name.equalsIgnoreCase(user.getUsername()) || name.equals(user.getRealName()));
                return (!hasCollegeRule && !hasUserRule) || collegeMatched || userMatched;
            }
        }
        return true;
    }

    private List<Long> toLongList(JSONArray array) {
        if (array == null) {
            return Collections.emptyList();
        }
        return array.stream().map(item -> Long.valueOf(String.valueOf(item))).collect(Collectors.toList());
    }

    private List<String> toStringList(JSONArray array) {
        if (array == null) {
            return Collections.emptyList();
        }
        return array.stream()
                .map(String::valueOf)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toList());
    }

    private boolean isEligibleForStudent(ActivityEntity activity, UserEntity student) {
        JSONObject rule = getEligibilityRule(activity);
        if (rule == null) {
            return true;
        }

        BigDecimal minGpa = rule.getBigDecimal("minGpa");
        if (minGpa != null) {
            BigDecimal studentGpa = student.getGpa();
            if (studentGpa == null || studentGpa.compareTo(minGpa) < 0) {
                return false;
            }
        }

        // 当前系统学生档案暂无独立“成绩”字段，演示场景下成绩条件使用绩点字段进行同源校验
        BigDecimal minScore = rule.getBigDecimal("minScore");
        if (minScore != null) {
            BigDecimal studentScore = student.getGpa();
            if (studentScore == null || studentScore.compareTo(minScore) < 0) {
                return false;
            }
        }
        return true;
    }

    private java.util.Map<String, Object> toAttachmentVO(AttachmentEntity entity) {
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("id", entity.getId());
        map.put("fileName", entity.getFileName());
        map.put("filePath", entity.getFilePath());
        map.put("fileSize", entity.getFileSize());
        map.put("fileType", entity.getFileType());
        // 设置完整的文件访问URL
        String fullUrl = "/api/file/view/" + entity.getFilePath();
        map.put("fullUrl", fullUrl);
        return map;
    }
}
