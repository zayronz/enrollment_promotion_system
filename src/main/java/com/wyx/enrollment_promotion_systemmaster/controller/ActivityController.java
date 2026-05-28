package com.wyx.enrollment_promotion_systemmaster.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wyx.enrollment_promotion_systemmaster.dto.ActivityDTO;
import com.wyx.enrollment_promotion_systemmaster.dto.ApiResponse;
import com.wyx.enrollment_promotion_systemmaster.entity.User;
import com.wyx.enrollment_promotion_systemmaster.service.ActivityService;
import com.wyx.enrollment_promotion_systemmaster.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/activities")
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ApiResponse<List<ActivityDTO>> getAllActivities() {
        List<ActivityDTO> activities = activityService.list().stream().map(a -> {
            ActivityDTO dto = new ActivityDTO();
            BeanUtils.copyProperties(a, dto);
            return dto;
        }).toList();
        return ApiResponse.success(activities);
    }

    @GetMapping("/public/list")
    public ApiResponse<Page<ActivityDTO>> getPublicActivityList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer activityType,
            @RequestParam(required = false) Integer status) {
        Page<ActivityDTO> activities = activityService.getActivityList(pageNum, pageSize, activityType, status);
        return ApiResponse.success(activities);
    }

    @GetMapping("/public/detail/{id}")
    public ApiResponse<ActivityDTO> getPublicActivityDetail(@PathVariable Long id) {
        ActivityDTO activity = activityService.getActivityById(id);
        return ApiResponse.success(activity);
    }

    @GetMapping("/available")
    public ApiResponse<List<ActivityDTO>> getAvailableActivities(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long userId,
            @RequestParam Integer role) {
        List<ActivityDTO> activities = activityService.getAvailableActivities(userId, role);
        return ApiResponse.success(activities);
    }

    @GetMapping("/{id}")
    public ApiResponse<ActivityDTO> getActivityDetail(@PathVariable Long id) {
        ActivityDTO activity = activityService.getActivityById(id);
        return ApiResponse.success(activity);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'TEACHER')")
    public ApiResponse<ActivityDTO> createActivity(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody ActivityDTO activityDTO) {
        User user = userService.getByUsername(userDetails.getUsername());
        ActivityDTO created = activityService.createActivity(activityDTO, user.getId(), user.getRealName());
        return ApiResponse.success(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'TEACHER')")
    public ApiResponse<ActivityDTO> updateActivity(
            @PathVariable Long id,
            @RequestBody ActivityDTO activityDTO) {
        ActivityDTO updated = activityService.updateActivity(id, activityDTO);
        return ApiResponse.success(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    public ApiResponse<Void> deleteActivity(@PathVariable Long id) {
        activityService.deleteActivity(id);
        return ApiResponse.success();
    }

    @PutMapping("/publish/{id}")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    public ApiResponse<Void> publishActivity(@PathVariable Long id) {
        activityService.publishActivity(id);
        return ApiResponse.success();
    }
}
