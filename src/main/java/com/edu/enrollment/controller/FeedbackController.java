package com.edu.enrollment.controller;

import com.edu.enrollment.dto.FeedbackSubmitDTO;
import com.edu.enrollment.service.FeedbackService;
import com.edu.enrollment.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping("/submit")
    public ResultVO<Long> submit(@Valid @RequestBody FeedbackSubmitDTO dto,
                                 @AuthenticationPrincipal Long userId) {
        Long id = feedbackService.submit(dto, userId);
        return ResultVO.success(id);
    }

    @GetMapping("/activity/{activityId}")
    public ResultVO<?> activityFeedbacks(@PathVariable Long activityId) {
        return ResultVO.success(feedbackService.getActivityFeedbacks(activityId));
    }

    @GetMapping("/my")
    public ResultVO<?> myFeedbacks(@AuthenticationPrincipal Long userId,
                                   @RequestParam(required = false) Long activityId) {
        return ResultVO.success(feedbackService.getMyFeedbacks(userId, activityId));
    }
}