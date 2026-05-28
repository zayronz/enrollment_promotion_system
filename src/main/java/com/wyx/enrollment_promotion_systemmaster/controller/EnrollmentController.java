package com.wyx.enrollment_promotion_systemmaster.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wyx.enrollment_promotion_systemmaster.dto.ApiResponse;
import com.wyx.enrollment_promotion_systemmaster.dto.EnrollmentDTO;
import com.wyx.enrollment_promotion_systemmaster.entity.Enrollment;
import com.wyx.enrollment_promotion_systemmaster.entity.User;
import com.wyx.enrollment_promotion_systemmaster.service.EnrollmentService;
import com.wyx.enrollment_promotion_systemmaster.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/enrollments")
public class EnrollmentController {

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private UserService userService;

    @PostMapping
    public ApiResponse<EnrollmentDTO> enroll(@RequestBody Enrollment enrollment) {
        enrollment.setApprovalStatus(0);
        enrollment.setEnrollmentTime(java.time.LocalDateTime.now());
        enrollmentService.save(enrollment);
        
        EnrollmentDTO dto = new EnrollmentDTO();
        BeanUtils.copyProperties(enrollment, dto);
        return ApiResponse.success(dto);
    }

    @GetMapping
    public ApiResponse<List<EnrollmentDTO>> getAllEnrollments() {
        List<Enrollment> enrollments = enrollmentService.list();
        List<EnrollmentDTO> dtos = enrollments.stream().map(e -> {
            EnrollmentDTO dto = new EnrollmentDTO();
            BeanUtils.copyProperties(e, dto);
            return dto;
        }).toList();
        return ApiResponse.success(dtos);
    }

    @PostMapping("/enroll")
    public ApiResponse<EnrollmentDTO> enroll(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long activityId,
            @RequestParam(required = false) String enrollmentData,
            @RequestParam(required = false) String attachments) {
        User user = userService.getByUsername(userDetails.getUsername());
        EnrollmentDTO enrollment = enrollmentService.enroll(activityId, user.getId(), enrollmentData, attachments);
        return ApiResponse.success(enrollment);
    }

    @GetMapping("/my")
    public ApiResponse<Page<EnrollmentDTO>> getMyEnrollments(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<EnrollmentDTO> enrollments = enrollmentService.getMyEnrollments(userId, pageNum, pageSize);
        return ApiResponse.success(enrollments);
    }

    @GetMapping("/{id}")
    public ApiResponse<EnrollmentDTO> getEnrollmentDetail(@PathVariable Long id) {
        EnrollmentDTO enrollment = enrollmentService.getEnrollmentById(id);
        return ApiResponse.success(enrollment);
    }

    @GetMapping("/activity/{activityId}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN')")
    public ApiResponse<List<EnrollmentDTO>> getEnrollmentsByActivity(@PathVariable Long activityId) {
        List<EnrollmentDTO> enrollments = enrollmentService.getEnrollmentsByActivity(activityId);
        return ApiResponse.success(enrollments);
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN')")
    public ApiResponse<Page<EnrollmentDTO>> getPendingEnrollments(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<EnrollmentDTO> enrollments = new Page<>();
        return ApiResponse.success(enrollments);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN')")
    public ApiResponse<Void> deleteEnrollment(@PathVariable Long id) {
        enrollmentService.removeById(id);
        return ApiResponse.success();
    }
}
