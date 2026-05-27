package com.edu.enrollment.controller;

import com.edu.enrollment.dto.ActivityDTO;
import com.edu.enrollment.service.ActivityService;
import com.edu.enrollment.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
                            @AuthenticationPrincipal Long userId) {
        return ResultVO.success(activityService.getActivityList(page, size, type, keyword, userId));
    }

    @GetMapping("/open")
    public ResultVO<?> openActivities() {
        return ResultVO.success(activityService.getOpenActivities());
    }

    @GetMapping("/{id}")
    public ResultVO<?> detail(@PathVariable Long id) {
        return ResultVO.success(activityService.getDetail(id));
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResultVO<Long> create(@Valid @RequestBody ActivityDTO dto,
                                 @AuthenticationPrincipal Long userId) {
        Long id = activityService.createActivity(dto, userId);
        return ResultVO.success(id);
    }

    @PutMapping("/{id}/publish")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResultVO<?> publish(@PathVariable Long id,
                               @AuthenticationPrincipal Long userId) {
        activityService.publishActivity(id, userId);
        return ResultVO.success();
    }
}