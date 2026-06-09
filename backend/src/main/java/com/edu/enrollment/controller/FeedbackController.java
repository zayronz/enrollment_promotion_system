package com.edu.enrollment.controller;

import com.edu.enrollment.dto.FeedbackSubmitDTO;
import com.edu.enrollment.entity.UserEntity;
import com.edu.enrollment.security.CurrentUserId;
import com.edu.enrollment.service.FeedbackService;
import com.edu.enrollment.service.UserService;
import com.edu.enrollment.vo.ResultVO;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final UserService userService;

    @PostMapping("/submit")
    public ResultVO<Long> submit(@Valid @RequestBody FeedbackSubmitDTO dto,
                                 @CurrentUserId Long userId) {
        Long id = feedbackService.submit(dto, userId);
        return ResultVO.success(id);
    }

    @GetMapping("/all")
    public ResultVO<?> allFeedbacks(@CurrentUserId Long userId) {
        UserEntity user = userService.getById(userId);
        if (user == null) {
            return ResultVO.error("用户不存在");
        }
        if (!"SCHOOL".equals(user.getRole())) {
            return ResultVO.success(feedbackService.getFeedbacksByRole(userId, user.getRole(), null));
        }
        return ResultVO.success(feedbackService.getAllFeedbacks());
    }

    @GetMapping("/activity/{activityId}")
    public ResultVO<?> activityFeedbacks(@PathVariable Long activityId,
                                         @CurrentUserId Long userId) {
        UserEntity user = userService.getById(userId);
        if (user == null) {
            return ResultVO.error("用户不存在");
        }
        return ResultVO.success(feedbackService.getFeedbacksByRole(userId, user.getRole(), activityId));
    }

    @GetMapping("/my")
    public ResultVO<?> myFeedbacks(@CurrentUserId Long userId,
                                   @RequestParam(required = false) Long activityId) {
        return ResultVO.success(feedbackService.getMyFeedbacks(userId, activityId));
    }

    @GetMapping("/college")
    public ResultVO<?> collegeFeedbacks(@CurrentUserId Long userId,
                                        @RequestParam(required = false) Long activityId) {
        UserEntity user = userService.getById(userId);
        if (user == null || !"COLLEGE".equals(user.getRole())) {
            return ResultVO.error("权限不足");
        }
        return ResultVO.success(feedbackService.getCollegeFeedbacks(user.getCollegeId(), activityId));
    }
}
