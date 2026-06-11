package com.edu.enrollment.controller;

import com.edu.enrollment.dto.ActivityDTO;
import com.edu.enrollment.security.CurrentUserId;
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

    @GetMapping("/list")
    public ResultVO<?> list(@RequestParam(defaultValue = "1") Integer page,
                            @RequestParam(defaultValue = "10") Integer size,
                            @RequestParam(required = false) Integer type,
                            @RequestParam(required = false) String keyword,
                            @CurrentUserId Long userId) {
        return ResultVO.success(activityService.getActivityList(page, size, type, keyword, userId));
    }

    @GetMapping("/open")
    public ResultVO<?> openActivities() {
        return ResultVO.success(activityService.getOpenActivities());
    }

    @GetMapping("/banner")
    public ResultVO<?> bannerActivities() {
        return ResultVO.success(activityService.getBannerActivities());
    }

    @GetMapping("/{id}")
    public ResultVO<?> detail(@PathVariable Long id) {
        return ResultVO.success(activityService.getDetail(id));
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResultVO<Long> create(@Valid @RequestBody ActivityDTO dto,
                                 @CurrentUserId Long userId) {
        Long id = activityService.createActivity(dto, userId);
        return ResultVO.success(id);
    }

    @PutMapping("/{id}/publish")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResultVO<?> publish(@PathVariable Long id,
                               @CurrentUserId Long userId) {
        activityService.publishActivity(id, userId);
        return ResultVO.success();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResultVO<?> update(@PathVariable Long id,
                              @Valid @RequestBody ActivityDTO dto,
                              @CurrentUserId Long userId) {
        activityService.updateActivity(id, dto, userId);
        return ResultVO.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResultVO<?> delete(@PathVariable Long id,
                              @CurrentUserId Long userId) {
        activityService.deleteActivity(id, userId);
        return ResultVO.success();
    }

    @PutMapping("/{id}/home-show")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResultVO<?> setHomeShow(@PathVariable Long id,
                                   @RequestParam Boolean show,
                                   @CurrentUserId Long userId) {
        activityService.setHomeShow(id, show, userId);
        return ResultVO.success();
    }
}