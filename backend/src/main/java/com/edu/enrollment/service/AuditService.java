package com.edu.enrollment.service;

import com.edu.enrollment.dto.AuditRequestDTO;
import com.edu.enrollment.entity.ActivityEntity;
import com.edu.enrollment.entity.AuditRecordEntity;
import com.edu.enrollment.entity.RegistrationEntity;
import com.edu.enrollment.entity.UserEntity;
import com.edu.enrollment.mapper.AuditRecordMapper;
import com.edu.enrollment.mapper.RegistrationMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
     * 查询审核历史记录（分页）
     */
    public Map<String, Object> getAuditHistory(Long auditorId, Integer page, Integer size,
                                                 String keyword, Long activityId, String result) {
        LambdaQueryWrapper<AuditRecordEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuditRecordEntity::getAuditorId, auditorId);

        // 按审核结果过滤
        if ("APPROVED".equals(result)) {
            wrapper.eq(AuditRecordEntity::getResult, 1);
        } else if ("REJECTED".equals(result)) {
            wrapper.eq(AuditRecordEntity::getResult, 2);
        }

        wrapper.orderByDesc(AuditRecordEntity::getCreateTime);

        Page<AuditRecordEntity> auditPage = new Page<>(page, size);
        auditPage = auditRecordMapper.selectPage(auditPage, wrapper);

        // 关联报名记录、活动、用户信息
        List<Map<String, Object>> enrichedRecords = auditPage.getRecords().stream()
                .map(record -> {
                    Map<String, Object> map = new HashMap<>();
                    RegistrationEntity registration = registrationMapper.selectById(record.getRegistrationId());

                    String realName = "-";
                    String userType = "-";
                    String activityTitle = "-";
                    String targetSchool = "-";

                    if (registration != null) {
                        UserEntity user = userService.getById(registration.getUserId());
                        if (user != null) {
                            realName = user.getRealName() != null ? user.getRealName() : user.getUsername();
                            userType = registration.getUserType() == 0 ? "STUDENT" : "TEACHER";
                        }
                        targetSchool = registration.getTargetSchool() != null ? registration.getTargetSchool() : "-";

                        // 按活动ID过滤
                        if (activityId != null && !activityId.equals(registration.getActivityId())) {
                            return null; // 不匹配的活动，过滤掉
                        }

                        ActivityEntity activity = activityService.getById(registration.getActivityId());
                        if (activity != null) {
                            activityTitle = activity.getName();
                            // 按关键词过滤（搜索报名人或活动名称）
                            if (keyword != null && !keyword.isEmpty()) {
                                if (!realName.contains(keyword) && !activityTitle.contains(keyword)
                                        && !targetSchool.contains(keyword)) {
                                    return null;
                                }
                            }
                        }
                    }

                    map.put("id", record.getId());
                    map.put("realName", realName);
                    map.put("userType", userType);
                    map.put("activityTitle", activityTitle);
                    map.put("targetSchool", targetSchool);
                    map.put("result", record.getResult() == 1 ? "APPROVED" : "REJECTED");
                    map.put("comment", record.getComment());
                    map.put("createTime", record.getCreateTime());
                    return map;
                })
                .filter(m -> m != null)
                .collect(Collectors.toList());

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("records", enrichedRecords);
        resultMap.put("total", auditPage.getTotal());
        resultMap.put("current", page);
        resultMap.put("size", size);
        return resultMap;
    }
}