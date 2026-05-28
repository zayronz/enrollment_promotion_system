package com.wyx.enrollment_promotion_systemmaster.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wyx.enrollment_promotion_systemmaster.dto.ApiResponse;
import com.wyx.enrollment_promotion_systemmaster.entity.Feedback;
import com.wyx.enrollment_promotion_systemmaster.entity.User;
import com.wyx.enrollment_promotion_systemmaster.service.FeedbackService;
import com.wyx.enrollment_promotion_systemmaster.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/feedback")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private UserService userService;

    @PostMapping("/submit")
    public ApiResponse<Feedback> submitFeedback(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long enrollmentId,
            @RequestParam Long activityId,
            @RequestParam String content,
            @RequestParam(required = false) String attachments,
            @RequestParam(defaultValue = "0") Integer feedbackType) {
        User user = userService.getByUsername(userDetails.getUsername());
        Feedback feedback = feedbackService.submitFeedback(
                enrollmentId, activityId, user.getId(), user.getUsername(),
                content, attachments, feedbackType
        );
        return ApiResponse.success(feedback);
    }

    @GetMapping("/enrollment/{enrollmentId}")
    public ApiResponse<Page<Feedback>> getFeedbacksByEnrollment(
            @PathVariable Long enrollmentId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Feedback> feedbacks = feedbackService.getFeedbacksByEnrollment(enrollmentId, pageNum, pageSize);
        return ApiResponse.success(feedbacks);
    }

    @GetMapping("/activity/{activityId}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public ApiResponse<Page<Feedback>> getFeedbacksByActivity(
            @PathVariable Long activityId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Feedback> feedbacks = feedbackService.getFeedbacksByActivity(activityId, pageNum, pageSize);
        return ApiResponse.success(feedbacks);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    public ApiResponse<Page<Feedback>> getAllFeedbacks(
            @RequestParam(required = false) Long activityId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Feedback> feedbacks;
        if (activityId != null) {
            feedbacks = feedbackService.getFeedbacksByActivity(activityId, pageNum, pageSize);
        } else {
            feedbacks = new Page<>(pageNum, pageSize);
        }
        return ApiResponse.success(feedbacks);
    }

    @GetMapping("/college")
    @PreAuthorize("hasRole('COLLEGE_ADMIN')")
    public ApiResponse<Page<Feedback>> getCollegeFeedbacks(
            @RequestParam Long collegeId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Feedback> feedbacks = feedbackService.getFeedbacksByCollege(collegeId, pageNum, pageSize);
        return ApiResponse.success(feedbacks);
    }
}
