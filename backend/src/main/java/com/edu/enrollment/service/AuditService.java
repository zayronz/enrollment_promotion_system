package com.edu.enrollment.service;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edu.enrollment.dto.AuditRequestDTO;
import com.edu.enrollment.entity.ActivityEntity;
import com.edu.enrollment.entity.AuditRecordEntity;
import com.edu.enrollment.entity.RegistrationEntity;
import com.edu.enrollment.entity.UserEntity;
import com.edu.enrollment.exception.BusinessException;
import com.edu.enrollment.mapper.AuditRecordMapper;
import com.edu.enrollment.mapper.RegistrationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final RegistrationMapper registrationMapper;
    private final AuditRecordMapper auditRecordMapper;
    private final ActivityService activityService;
    private final UserService userService;
    private final RegistrationService registrationService;

    @Transactional
    public void audit(AuditRequestDTO dto, Long auditorId) {
        RegistrationEntity registration = registrationMapper.selectById(dto.getRegistrationId());
        if (registration == null) {
            throw new BusinessException("报名记录不存在");
        }

        ActivityEntity activity = activityService.getById(registration.getActivityId());
        UserEntity auditor = userService.getById(auditorId);

        // 记录审核日志
        AuditRecordEntity auditRecord = new AuditRecordEntity();
        auditRecord.setRegistrationId(dto.getRegistrationId());
        auditRecord.setNode(registration.getCurrentNode());
        auditRecord.setAuditorId(auditorId);
        auditRecord.setAuditorName(auditor.getRealName());
        auditRecord.setResult(dto.getPassed() ? 1 : 2);
        auditRecord.setComment(dto.getComment());
        auditRecord.setAttachmentUrls(dto.getAttachmentUrls());
        auditRecordMapper.insert(auditRecord);

        if (dto.getPassed()) {
            // 通过前校验名额限制
            checkQuotaLimit(registration, activity);

            // 通过：判断是否还有下一级审批
            if (hasNextNode(registration.getCurrentNode(), activity)) {
                // 学院审核通过，设置状态为1（学院通过）
                if ("college_audit".equals(registration.getCurrentNode())) {
                    registration.setStatus(1); // 学院通过
                }
                registration.setCurrentNode(getNextNode(registration.getCurrentNode()));
            } else {
                registration.setStatus(2); // 全部通过
                registration.setCurrentNode("completed");
            }
        } else {
            // 拒绝
            registration.setStatus(3);
            registration.setRejectReason(dto.getComment());
        }

        registrationMapper.updateById(registration);

        if (dto.getPassed() && activity != null && activity.getAutoGroup() != null && activity.getAutoGroup() == 1) {
            registrationService.autoGroup(registration.getActivityId(), registration.getTargetSchool());
        }
    }

    private boolean hasNextNode(String currentNode, ActivityEntity activity) {
        // 根据活动配置的审批流程判断
        // 简化实现：学院审核 -> 学校审核 -> 结束
        return "college_audit".equals(currentNode);
    }

    private String getNextNode(String currentNode) {
        if ("college_audit".equals(currentNode)) {
            return "school_audit";
        }
        return "completed";
    }

    /**
     * 校验名额限制
     */
    private void checkQuotaLimit(RegistrationEntity registration, ActivityEntity activity) {
        // 查找同学校已通过的报名
        List<RegistrationEntity> sameSchoolPassed = registrationMapper.findByActivityAndSchool(
                registration.getActivityId(), registration.getTargetSchool());
        long passedCount = sameSchoolPassed.stream()
                .filter(r -> !r.getId().equals(registration.getId()))
                .filter(r -> r.getUserType() != null && r.getUserType().equals(registration.getUserType()))
                .filter(r -> r.getStatus() == 1 || r.getStatus() == 2) // 学院通过或学校通过
                .count();

        if (registration.getUserType() == 0) { // 学生
            if (activity.getMaxStudentPerSchool() != null
                    && passedCount >= activity.getMaxStudentPerSchool()) {
                throw new BusinessException("该高中名额已满，无法通过（学生上限："
                        + activity.getMaxStudentPerSchool() + "人）");
            }
        } else { // 教师
            if (activity.getMaxTeacherPerSchool() != null
                    && passedCount >= activity.getMaxTeacherPerSchool()) {
                throw new BusinessException("该高中名额已满，无法通过（教师上限："
                        + activity.getMaxTeacherPerSchool() + "人）");
            }
        }
    }

    @Transactional
    public void batchAudit(List<Long> registrationIds, Boolean passed, String comment, Long auditorId) {
        for (Long id : registrationIds) {
            AuditRequestDTO dto = new AuditRequestDTO();
            dto.setRegistrationId(id);
            dto.setPassed(passed);
            dto.setComment(comment);
            audit(dto, auditorId);
        }
    }

    /**
     * 获取审核历史记录
     */
    public Map<String, Object> getAuditHistory(Long auditorId, Integer page, Integer size,
                                                String keyword, Long activityId, String result) {
        // 构建查询条件
        LambdaQueryWrapper<AuditRecordEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuditRecordEntity::getAuditorId, auditorId);

        // 审核结果筛选
        if (result != null && !result.isEmpty()) {
            if ("APPROVED".equals(result)) {
                wrapper.eq(AuditRecordEntity::getResult, 1);
            } else if ("REJECTED".equals(result)) {
                wrapper.eq(AuditRecordEntity::getResult, 2);
            }
        }

        wrapper.orderByDesc(AuditRecordEntity::getCreateTime);

        // 分页查询
        Page<AuditRecordEntity> auditPage = new Page<>(page, size);
        auditPage = auditRecordMapper.selectPage(auditPage, wrapper);

        // 组装返回结果，附加报名和活动信息
        List<Map<String, Object>> enrichedRecords = auditPage.getRecords().stream()
                .map(audit -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", audit.getId());
                    map.put("registrationId", audit.getRegistrationId());
                    map.put("node", audit.getNode());
                    map.put("auditorId", audit.getAuditorId());
                    map.put("auditorName", audit.getAuditorName());
                    // 转换审核结果为字符串
                    map.put("result", audit.getResult() == 1 ? "APPROVED" : "REJECTED");
                    map.put("comment", audit.getComment());
                    map.put("createTime", audit.getCreateTime());

                    // 获取报名信息
                    RegistrationEntity registration = registrationMapper.selectById(audit.getRegistrationId());
                    if (registration != null) {
                        map.put("targetSchool", registration.getTargetSchool());
                        map.put("score", registration.getScore());
                        map.put("attachments", extractAttachments(registration));
                        map.put("formData", parseFormData(registration));

                        // 获取活动信息
                        ActivityEntity activity = activityService.getById(registration.getActivityId());
                        if (activity != null) {
                            map.put("activityId", activity.getId());
                            map.put("activityName", activity.getName());
                            map.put("activityTitle", activity.getName()); // 前端期望的字段名
                        }

                        // 获取用户信息
                        UserEntity user = userService.getById(registration.getUserId());
                        if (user != null) {
                            map.put("userId", user.getId());
                            map.put("userName", user.getRealName());
                            map.put("realName", user.getRealName()); // 前端期望的字段名
                            map.put("userType", "student".equals(user.getRole()) ? "STUDENT" : "TEACHER");
                        }
                    }

                    return map;
                })
                .collect(Collectors.toList());

        // 关键词过滤
        List<Map<String, Object>> filtered = enrichedRecords;
        if (keyword != null && !keyword.isEmpty()) {
            String kw = keyword.toLowerCase();
            filtered = enrichedRecords.stream()
                    .filter(m -> {
                        String activityName = (String) m.getOrDefault("activityName", "");
                        String activityTitle = (String) m.getOrDefault("activityTitle", "");
                        String userName = (String) m.getOrDefault("userName", "");
                        String realName = (String) m.getOrDefault("realName", "");
                        String school = (String) m.getOrDefault("targetSchool", "");
                        return activityName.toLowerCase().contains(kw)
                                || activityTitle.toLowerCase().contains(kw)
                                || userName.toLowerCase().contains(kw)
                                || realName.toLowerCase().contains(kw)
                                || school.toLowerCase().contains(kw);
                    })
                    .collect(Collectors.toList());
        }

        // 活动筛选
        if (activityId != null) {
            filtered = filtered.stream()
                    .filter(m -> activityId.equals(m.get("activityId")))
                    .collect(Collectors.toList());
        }

        Map<String, Object> pageResult = new HashMap<>();
        pageResult.put("records", filtered);
        pageResult.put("total", (long) filtered.size());
        pageResult.put("current", page);
        pageResult.put("size", size);
        return pageResult;
    }

    private Map<String, Object> parseFormData(RegistrationEntity registration) {
        Map<String, Object> formData = new HashMap<>();
        if (registration.getFormData() != null && !registration.getFormData().isBlank()) {
            formData.putAll(JSONUtil.parseObj(registration.getFormData()));
        }
        return formData;
    }

    private Object extractAttachments(RegistrationEntity registration) {
        Map<String, Object> formData = parseFormData(registration);
        return formData.getOrDefault("attachments", List.of());
    }
}
