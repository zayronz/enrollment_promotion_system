package com.wyx.enrollment_promotion_systemmaster.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wyx.enrollment_promotion_systemmaster.entity.Activity;
import com.wyx.enrollment_promotion_systemmaster.entity.ApprovalRecord;
import com.wyx.enrollment_promotion_systemmaster.entity.Enrollment;
import com.wyx.enrollment_promotion_systemmaster.entity.User;
import com.wyx.enrollment_promotion_systemmaster.exception.BusinessException;
import com.wyx.enrollment_promotion_systemmaster.mapper.ApprovalRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ApprovalService extends ServiceImpl<ApprovalRecordMapper, ApprovalRecord> {

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private UserService userService;

    @Transactional
    public void approve(Long enrollmentId, Long approverId, String approverName,
                        Integer approvalStatus, String approvalComment, String attachments) {
        Enrollment enrollment = enrollmentService.getById(enrollmentId);
        if (enrollment == null) {
            throw new BusinessException("Enrollment not found");
        }

        Activity activity = activityService.getById(enrollment.getActivityId());
        if (activity == null) {
            throw new BusinessException("Activity not found");
        }

        ApprovalRecord record = new ApprovalRecord();
        record.setEnrollmentId(enrollmentId);
        record.setActivityId(enrollment.getActivityId());
        record.setApproverId(approverId);
        record.setApproverName(approverName);
        record.setFlowNode(enrollment.getCurrentFlowNode());
        record.setApprovalStatus(approvalStatus);
        record.setApprovalComment(approvalComment);
        record.setAttachments(attachments);

        this.save(record);

        if (approvalStatus == 1) {
            int nextNode = enrollment.getCurrentFlowNode() + 1;
            String[] flowNodes = activity.getApprovalFlow().split(",");

            if (nextNode > flowNodes.length) {
                enrollment.setApprovalStatus(2);
                enrollmentService.updateById(enrollment);
            } else {
                enrollment.setCurrentFlowNode(nextNode);
                enrollmentService.updateById(enrollment);
            }
        } else if (approvalStatus == 3) {
            enrollment.setApprovalStatus(3);
            enrollmentService.updateById(enrollment);
        }
    }

    public Page<ApprovalRecord> getPendingApprovals(Long approverId, Integer role, Long collegeId,
                                                     Integer pageNum, Integer pageSize) {
        Page<ApprovalRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ApprovalRecord> wrapper = new LambdaQueryWrapper<>();

        wrapper.orderByDesc(ApprovalRecord::getApprovalTime);
        return this.page(page, wrapper);
    }

    public List<ApprovalRecord> getApprovalHistory(Long enrollmentId) {
        LambdaQueryWrapper<ApprovalRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApprovalRecord::getEnrollmentId, enrollmentId)
                .orderByAsc(ApprovalRecord::getFlowNode);

        return this.list(wrapper);
    }

    @Transactional
    public void batchApprove(List<Long> enrollmentIds, Long approverId, String approverName,
                            Integer approvalStatus, String approvalComment) {
        for (Long enrollmentId : enrollmentIds) {
            approve(enrollmentId, approverId, approverName, approvalStatus, approvalComment, null);
        }
    }
}
