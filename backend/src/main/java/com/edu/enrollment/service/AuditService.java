package com.edu.enrollment.service;

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
        String auditFlow = activity.getAuditFlow();
        if (auditFlow == null || auditFlow.isEmpty()) {
            return false;
        }
        // auditFlow 格式: ["college_audit","school_audit"]
        String[] nodes = auditFlow.replace("[", "").replace("]", "")
                .replace("\"", "").split(",");
        for (int i = 0; i < nodes.length - 1; i++) {
            if (currentNode.equals(nodes[i].trim())) {
                return true;
            }
        }
        return false;
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
        // 查找同学校已通过或已进入下一节点审批的报名
        List<RegistrationEntity> sameSchoolPassed = registrationMapper.findByActivityAndSchool(
                registration.getActivityId(), registration.getTargetSchool());
        long passedCount = sameSchoolPassed.stream()
                .filter(r -> r.getStatus() == 1 || r.getStatus() == 2
                        || "school_audit".equals(r.getCurrentNode())) // 已通过学院审核待学校审核的也要算
                .filter(r -> registration.getUserType().equals(r.getUserType())) // 只统计同类型(学生/教师)
                .count();

        if (registration.getUserType() == 0) { // 学生
            if (activity.getMaxStudentPerSchool() != null
                    && passedCount >= activity.getMaxStudentPerSchool()) {
                throw new BusinessException("该学校学生名额已满（上限："
                        + activity.getMaxStudentPerSchool() + "人），当前已有 " + passedCount + " 人通过审核");
            }
        } else { // 教师
            if (activity.getMaxTeacherPerSchool() != null
                    && passedCount >= activity.getMaxTeacherPerSchool()) {
                throw new BusinessException("该学校教师名额已满（上限："
                        + activity.getMaxTeacherPerSchool() + "人），当前已有 " + passedCount + " 人通过审核");
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
     * 获取审核历史记录（分页）
     */
    public Page<Map<String, Object>> getAuditHistory(Integer page, Integer size,
                                                      Long activityId, String result,
                                                      Long collegeId) {
        LambdaQueryWrapper<AuditRecordEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(AuditRecordEntity::getCreateTime);

        Page<AuditRecordEntity> recordPage = auditRecordMapper.selectPage(
                new Page<>(page, size), wrapper);

        Page<Map<String, Object>> resultPage = new Page<>();
        resultPage.setCurrent(recordPage.getCurrent());
        resultPage.setSize(recordPage.getSize());
        resultPage.setTotal(recordPage.getTotal());

        List<Map<String, Object>> records = recordPage.getRecords().stream()
                .map(record -> {
                    Map<String, Object> map = new HashMap<>();
                    RegistrationEntity reg = registrationMapper.selectById(record.getRegistrationId());
                    UserEntity user = reg != null ? userService.getById(reg.getUserId()) : null;
                    ActivityEntity activity = reg != null ? activityService.getById(reg.getActivityId()) : null;

                    map.put("id", record.getId());
                    map.put("registrationId", record.getRegistrationId());
                    map.put("realName", user != null ? user.getRealName() : "-");
                    map.put("userType", user != null ? ("student".equals(user.getRole()) ? "STUDENT" : "TEACHER") : "-");
                    map.put("activityTitle", activity != null ? activity.getName() : "-");
                    map.put("targetSchool", reg != null ? reg.getTargetSchool() : "-");
                    map.put("result", record.getResult() == 1 ? "APPROVED" : "REJECTED");
                    map.put("comment", record.getComment());
                    map.put("auditorName", record.getAuditorName());
                    map.put("node", record.getNode());
                    map.put("createTime", record.getCreateTime());
                    return map;
                })
                .filter(map -> {
                    if (activityId != null && !activityId.equals(
                            registrationMapper.selectById((Long) map.get("registrationId")).getActivityId())) {
                        return false;
                    }
                    if (result != null && !result.equals(map.get("result"))) {
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        resultPage.setRecords(records);
        resultPage.setTotal((long) records.size());
        return resultPage;
    }
}