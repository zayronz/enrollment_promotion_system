package com.edu.enrollment.service;

import com.edu.enrollment.dto.AuditRequestDTO;
import com.edu.enrollment.entity.ActivityEntity;
import com.edu.enrollment.entity.AuditRecordEntity;
import com.edu.enrollment.entity.RegistrationEntity;
import com.edu.enrollment.entity.UserEntity;
import com.edu.enrollment.mapper.AuditRecordMapper;
import com.edu.enrollment.mapper.RegistrationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final RegistrationMapper registrationMapper;
    private final AuditRecordMapper auditRecordMapper;
    private final ActivityService activityService;
    private final UserService userService;

    @Transactional
    public void audit(AuditRequestDTO dto, Long auditorId) {
        RegistrationEntity registration = registrationMapper.selectById(dto.getRegistrationId());
        if (registration == null) {
            throw new RuntimeException("报名记录不存在");
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
                .filter(r -> r.getStatus() == 1 || r.getStatus() == 2) // 学院通过或学校通过
                .count();

        if (registration.getUserType() == 0) { // 学生
            if (activity.getMaxStudentPerSchool() != null
                    && passedCount >= activity.getMaxStudentPerSchool()) {
                throw new RuntimeException("该学校学生名额已满（上限："
                        + activity.getMaxStudentPerSchool() + "人）");
            }
        } else { // 教师
            if (activity.getMaxTeacherPerSchool() != null
                    && passedCount >= activity.getMaxTeacherPerSchool()) {
                throw new RuntimeException("该学校教师名额已满（上限："
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
     * 获取审核历史列表
     */
    public java.util.Map<String, Object> getAuditHistory(Long auditorId, Integer page, Integer size,
                                                          String keyword, Long activityId, String result) {
        UserEntity auditor = userService.getById(auditorId);

        // 查询已审核的记录（status = 2 通过 或 status = 3 拒绝）
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<RegistrationEntity> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.in(RegistrationEntity::getStatus, 2, 3);

        // 学院审核员只能看到本学院用户的报名
        if ("COLLEGE".equals(auditor.getRole())) {
            List<UserEntity> collegeUsers = userService.getByCollegeId(auditor.getCollegeId());
            if (collegeUsers.isEmpty()) {
                java.util.Map<String, Object> emptyResult = new java.util.HashMap<>();
                emptyResult.put("records", List.of());
                emptyResult.put("total", 0L);
                emptyResult.put("current", page);
                emptyResult.put("size", size);
                return emptyResult;
            }
            List<Long> collegeUserIds = collegeUsers.stream()
                    .map(UserEntity::getId)
                    .collect(java.util.stream.Collectors.toList());
            wrapper.in(RegistrationEntity::getUserId, collegeUserIds);
        }

        // 活动筛选
        if (activityId != null) {
            wrapper.eq(RegistrationEntity::getActivityId, activityId);
        }

        wrapper.orderByDesc(RegistrationEntity::getUpdateTime);

        // 分页查询
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<RegistrationEntity> registrationPage =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);
        registrationPage = registrationMapper.selectPage(registrationPage, wrapper);

        // 组装返回结果
        List<java.util.Map<String, Object>> enrichedRecords = registrationPage.getRecords().stream()
                .map(reg -> {
                    java.util.Map<String, Object> map = new java.util.HashMap<>();
                    UserEntity user = userService.getById(reg.getUserId());
                    ActivityEntity activity = activityService.getById(reg.getActivityId());

                    // 获取审核记录
                    List<AuditRecordEntity> auditRecords = auditRecordMapper.selectByRegistrationId(reg.getId());
                    AuditRecordEntity latestAudit = auditRecords.isEmpty() ? null : auditRecords.get(0);

                    map.put("id", reg.getId());
                    map.put("activityId", reg.getActivityId());
                    map.put("activityTitle", activity != null ? activity.getName() : "-");
                    map.put("realName", user != null ? user.getRealName() : "-");
                    map.put("userType", user != null ? ("student".equals(user.getRole()) ? "STUDENT" : "TEACHER") : "-");
                    map.put("targetSchool", reg.getTargetSchool());
                    map.put("score", reg.getScore());
                    map.put("createTime", reg.getCreateTime());
                    map.put("result", reg.getStatus() == 2 ? "APPROVED" : "REJECTED");
                    map.put("comment", latestAudit != null ? latestAudit.getComment() : "-");
                    map.put("auditorName", latestAudit != null ? latestAudit.getAuditorName() : "-");
                    return map;
                })
                .collect(java.util.stream.Collectors.toList());

        // 关键词过滤
        List<java.util.Map<String, Object>> filtered = enrichedRecords;
        if (keyword != null && !keyword.isEmpty()) {
            String kw = keyword.toLowerCase();
            filtered = enrichedRecords.stream()
                    .filter(m -> {
                        String name = (String) m.getOrDefault("realName", "");
                        String school = (String) m.getOrDefault("targetSchool", "");
                        return name.toLowerCase().contains(kw) || school.toLowerCase().contains(kw);
                    })
                    .collect(java.util.stream.Collectors.toList());
        }

        // 结果筛选
        if (result != null && !result.isEmpty()) {
            filtered = filtered.stream()
                    .filter(m -> result.equals(m.get("result")))
                    .collect(java.util.stream.Collectors.toList());
        }

        java.util.Map<String, Object> resultMap = new java.util.HashMap<>();
        resultMap.put("records", filtered);
        resultMap.put("total", (long) filtered.size());
        resultMap.put("current", page);
        resultMap.put("size", size);
        return resultMap;
    }
}