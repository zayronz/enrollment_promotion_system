package com.wyx.enrollment_promotion_systemmaster.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wyx.enrollment_promotion_systemmaster.dto.ApiResponse;
import com.wyx.enrollment_promotion_systemmaster.entity.ApprovalRecord;
import com.wyx.enrollment_promotion_systemmaster.entity.User;
import com.wyx.enrollment_promotion_systemmaster.service.ApprovalService;
import com.wyx.enrollment_promotion_systemmaster.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/approval")
public class ApprovalController {

    @Autowired
    private ApprovalService approvalService;

    @Autowired
    private UserService userService;

    @PostMapping("/approve")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN')")
    public ApiResponse<Void> approve(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long enrollmentId,
            @RequestParam Integer approvalStatus,
            @RequestParam(required = false) String approvalComment,
            @RequestParam(required = false) String attachments) {
        User user = userService.getByUsername(userDetails.getUsername());
        approvalService.approve(enrollmentId, user.getId(), user.getRealName(),
                approvalStatus, approvalComment, attachments);
        return ApiResponse.success();
    }

    @PostMapping("/batch")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN')")
    public ApiResponse<Void> batchApprove(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody List<Long> enrollmentIds,
            @RequestParam Integer approvalStatus,
            @RequestParam(required = false) String approvalComment) {
        User user = userService.getByUsername(userDetails.getUsername());
        approvalService.batchApprove(enrollmentIds, user.getId(), user.getRealName(),
                approvalStatus, approvalComment);
        return ApiResponse.success();
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN')")
    public ApiResponse<Page<ApprovalRecord>> getPendingApprovals(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        User user = userService.getByUsername(userDetails.getUsername());
        Page<ApprovalRecord> approvals = approvalService.getPendingApprovals(
                user.getId(), user.getRole(), user.getCollegeId(), pageNum, pageSize);
        return ApiResponse.success(approvals);
    }

    @GetMapping("/history/{enrollmentId}")
    public ApiResponse<List<ApprovalRecord>> getApprovalHistory(@PathVariable Long enrollmentId) {
        List<ApprovalRecord> history = approvalService.getApprovalHistory(enrollmentId);
        return ApiResponse.success(history);
    }
}
