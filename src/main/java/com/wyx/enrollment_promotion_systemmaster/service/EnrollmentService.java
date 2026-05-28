package com.wyx.enrollment_promotion_systemmaster.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wyx.enrollment_promotion_systemmaster.dto.EnrollmentDTO;
import com.wyx.enrollment_promotion_systemmaster.entity.Activity;
import com.wyx.enrollment_promotion_systemmaster.entity.Enrollment;
import com.wyx.enrollment_promotion_systemmaster.entity.User;
import com.wyx.enrollment_promotion_systemmaster.exception.BusinessException;
import com.wyx.enrollment_promotion_systemmaster.mapper.EnrollmentMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class EnrollmentService extends ServiceImpl<EnrollmentMapper, Enrollment> {

    private final ActivityService activityService;
    private final UserService userService;

    public EnrollmentService(ActivityService activityService, UserService userService) {
        this.activityService = activityService;
        this.userService = userService;
    }

    public EnrollmentDTO enroll(Long activityId, Long userId, String enrollmentData, String attachments) {
        Activity activity = activityService.getById(activityId);
        if (activity == null) {
            throw new BusinessException("Activity not found");
        }

        if (activity.getStatus() != 1) {
            throw new BusinessException("Activity is not open for enrollment");
        }

        LocalDateTime now = LocalDateTime.now();
        if (activity.getRegistrationStartTime() != null && now.isBefore(activity.getRegistrationStartTime())) {
            throw new BusinessException("Enrollment has not started yet");
        }
        if (activity.getRegistrationEndTime() != null && now.isAfter(activity.getRegistrationEndTime())) {
            throw new BusinessException("Enrollment has ended");
        }

        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException("User not found");
        }

        LambdaQueryWrapper<Enrollment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Enrollment::getActivityId, activityId)
                .eq(Enrollment::getUserId, userId);
        if (this.count(wrapper) > 0) {
            throw new BusinessException("You have already enrolled in this activity");
        }

        if (user.getRole() == 1 && activity.getMaxStudents() != null) {
            wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Enrollment::getActivityId, activityId)
                    .eq(Enrollment::getRole, 1);
            long studentCount = this.count(wrapper);
            if (studentCount >= activity.getMaxStudents()) {
                throw new BusinessException("Student enrollment quota is full");
            }
        } else if (user.getRole() == 2 && activity.getMaxTeachers() != null) {
            wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Enrollment::getActivityId, activityId)
                    .eq(Enrollment::getRole, 2);
            long teacherCount = this.count(wrapper);
            if (teacherCount >= activity.getMaxTeachers()) {
                throw new BusinessException("Teacher enrollment quota is full");
            }
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setActivityId(activityId);
        enrollment.setActivityTitle(activity.getTitle());
        enrollment.setUserId(userId);
        enrollment.setUserName(user.getUsername());
        enrollment.setRealName(user.getRealName());
        enrollment.setRole(user.getRole());
        enrollment.setEnrollmentData(enrollmentData);
        enrollment.setAttachments(attachments);
        enrollment.setApprovalStatus(0);
        enrollment.setCurrentFlowNode(1);

        this.save(enrollment);
        return convertToDTO(enrollment);
    }

    public Page<EnrollmentDTO> getMyEnrollments(Long userId, Integer pageNum, Integer pageSize) {
        Page<Enrollment> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Enrollment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Enrollment::getUserId, userId)
                .orderByDesc(Enrollment::getEnrollmentTime);

        Page<Enrollment> result = this.page(page, wrapper);
        Page<EnrollmentDTO> dtoPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        dtoPage.setRecords(result.getRecords().stream().map(this::convertToDTO).toList());

        return dtoPage;
    }

    public EnrollmentDTO getEnrollmentById(Long id) {
        Enrollment enrollment = this.getById(id);
        if (enrollment == null) {
            throw new BusinessException("Enrollment not found");
        }
        return convertToDTO(enrollment);
    }

    public List<EnrollmentDTO> getEnrollmentsByActivity(Long activityId) {
        LambdaQueryWrapper<Enrollment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Enrollment::getActivityId, activityId)
                .orderByDesc(Enrollment::getEnrollmentTime);

        List<Enrollment> enrollments = this.list(wrapper);
        return enrollments.stream().map(this::convertToDTO).toList();
    }

    public void updateEnrollmentStatus(Long enrollmentId, Integer status, Integer flowNode) {
        Enrollment enrollment = this.getById(enrollmentId);
        if (enrollment == null) {
            throw new BusinessException("Enrollment not found");
        }

        enrollment.setApprovalStatus(status);
        enrollment.setCurrentFlowNode(flowNode);
        this.updateById(enrollment);
    }

    private EnrollmentDTO convertToDTO(Enrollment enrollment) {
        EnrollmentDTO dto = new EnrollmentDTO();
        BeanUtils.copyProperties(enrollment, dto);
        if (enrollment.getEnrollmentTime() != null) {
            dto.setEnrollmentTime(enrollment.getEnrollmentTime().toString());
        }
        if (enrollment.getUpdateTime() != null) {
            dto.setUpdateTime(enrollment.getUpdateTime().toString());
        }
        return dto;
    }
}
