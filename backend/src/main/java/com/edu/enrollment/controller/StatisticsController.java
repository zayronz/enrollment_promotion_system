package com.edu.enrollment.controller;

import com.edu.enrollment.entity.UserEntity;
import com.edu.enrollment.security.CurrentUserId;
import com.edu.enrollment.service.StatisticsService;
import com.edu.enrollment.service.UserService;
import com.edu.enrollment.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;
    private final UserService userService;

    /**
     * 学校端仪表盘数据
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResultVO<Map<String, Object>> getDashboard() {
        Map<String, Object> stats = statisticsService.getDashboardStats();
        return ResultVO.success(stats);
    }

    /**
     * 学院端统计数据
     */
    @GetMapping("/college")
    @PreAuthorize("hasRole('COLLEGE')")
    public ResultVO<Map<String, Object>> getCollegeStats(@CurrentUserId Long userId) {
        UserEntity user = userService.getById(userId);
        Map<String, Object> stats = statisticsService.getCollegeStats(user.getCollegeId());
        return ResultVO.success(stats);
    }
}
