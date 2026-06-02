package com.edu.enrollment.controller;

import com.edu.enrollment.dto.FeedbackSubmitDTO;
import com.edu.enrollment.security.CurrentUserId;
import com.edu.enrollment.service.FeedbackService;
import com.edu.enrollment.vo.ResultVO;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping("/submit")
    public ResultVO<Long> submit(@Valid @RequestBody FeedbackSubmitDTO dto,
                                 @CurrentUserId Long userId) {
        Long id = feedbackService.submit(dto, userId);
        return ResultVO.success(id);
    }

    @GetMapping("/all")
    public ResultVO<?> allFeedbacks() {
        return ResultVO.success(feedbackService.getAllFeedbacks());
    }

    @GetMapping("/activity/{activityId}")
    public ResultVO<?> activityFeedbacks(@PathVariable Long activityId) {
        return ResultVO.success(feedbackService.getActivityFeedbacks(activityId));
    }

    @GetMapping("/my")
    public ResultVO<?> myFeedbacks(@CurrentUserId Long userId,
                                   @RequestParam(required = false) Long activityId) {
        return ResultVO.success(feedbackService.getMyFeedbacks(userId, activityId));
    }
}