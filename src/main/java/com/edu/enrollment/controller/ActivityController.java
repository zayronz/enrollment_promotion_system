package com.edu.enrollment.controller;

import com.edu.enrollment.dto.ActivityDTO;
import com.edu.enrollment.service.ActivityService;
import com.edu.enrollment.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/activity")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    /**
     * 活动列表（带权限过滤）
     */
    @GetMapping("/list")
    public ResultVO list(@RequestParam(defaultValue = "1") Integer page,
                         @RequestParam(defaultValue = "10") Integer size,
                         @RequestParam(required = false) Integer type,
                         @RequestParam(required = false) String keyword) {
        return ResultVO.success(activityService.getActivityList(page, size, type, keyword));
    }

    /**
     * 活动详情
     */
    @GetMapping("/{id}")
    public ResultVO detail(@PathVariable Long id) {
        return ResultVO.success(activityService.getDetail(id));
    }

    /**
     * 创建活动（仅学校端）
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResultVO create(@Valid @RequestBody ActivityDTO dto) {
        return ResultVO.success(activityService.createActivity(dto));
    }

    /**
     * 发布活动（仅学校端）
     */
    @PutMapping("/{id}/publish")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResultVO publish(@PathVariable Long id) {
        activityService.publishActivity(id);
        return ResultVO.success();
    }
}