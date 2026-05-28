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
}