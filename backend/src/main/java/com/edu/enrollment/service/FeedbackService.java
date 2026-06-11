package com.edu.enrollment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.edu.enrollment.dto.FeedbackSubmitDTO;
import com.edu.enrollment.entity.FeedbackEntity;
import com.edu.enrollment.entity.RegistrationEntity;
import com.edu.enrollment.entity.UserEntity;
import com.edu.enrollment.entity.ActivityEntity;
import com.edu.enrollment.exception.BusinessException;
import com.edu.enrollment.mapper.FeedbackMapper;
import com.edu.enrollment.mapper.RegistrationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackMapper feedbackMapper;
    private final RegistrationMapper registrationMapper;
    private final UserService userService;
    private final ActivityService activityService;

    @Transactional
    public Long submit(FeedbackSubmitDTO dto, Long userId) {
        // 校验是否已报名且审批通过（状态1=学院通过，状态2=全部通过）
        RegistrationEntity registration = registrationMapper.findByActivityAndUser(dto.getActivityId(), userId);
        if (registration == null || registration.getStatus() != 1 && registration.getStatus() != 2) {
            throw new BusinessException("只有报名审批通过的用户才能提交反馈");
        }

        LocalDateTime feedbackDeadline = activityService.getFeedbackDeadline(dto.getActivityId());
        if (feedbackDeadline != null && LocalDateTime.now().isAfter(feedbackDeadline)) {
            throw new BusinessException("已过反馈时间，无法提交反馈");
        }

        UserEntity user = userService.getById(userId);

        FeedbackEntity feedback = new FeedbackEntity();
        feedback.setActivityId(dto.getActivityId());
        feedback.setUserId(userId);
        // 转换角色值：student -> STUDENT, teacher -> TEACHER
        String role = user.getRole();
        if ("student".equals(role)) {
            feedback.setUserRole("STUDENT");
        } else if ("teacher".equals(role)) {
            feedback.setUserRole("TEACHER");
        } else {
            feedback.setUserRole(role);
        }
        feedback.setTitle(dto.getTitle());
        feedback.setContent(dto.getContent());
        feedback.setAttachmentUrls(dto.getAttachmentUrls());
        feedback.setType(dto.getType());

        feedbackMapper.insert(feedback);
        return feedback.getId();
    }

    public List<FeedbackEntity> getAllFeedbacks() {
        LambdaQueryWrapper<FeedbackEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(FeedbackEntity::getCreateTime);
        List<FeedbackEntity> feedbacks = feedbackMapper.selectList(wrapper);
        // 填充活动名称和提交人姓名
        for (FeedbackEntity feedback : feedbacks) {
            ActivityEntity activity = activityService.getById(feedback.getActivityId());
            if (activity != null) {
                feedback.setActivityTitle(activity.getName());
            }
            UserEntity user = userService.getById(feedback.getUserId());
            if (user != null) {
                feedback.setRealName(user.getRealName());
            }
        }
        return feedbacks;
    }

    public List<FeedbackEntity> getActivityFeedbacks(Long activityId) {
        List<FeedbackEntity> feedbacks = feedbackMapper.selectByActivityId(activityId);
        // 填充活动名称和提交人姓名
        ActivityEntity activity = activityService.getById(activityId);
        if (activity != null) {
            for (FeedbackEntity feedback : feedbacks) {
                feedback.setActivityTitle(activity.getName());
                UserEntity user = userService.getById(feedback.getUserId());
                if (user != null) {
                    feedback.setRealName(user.getRealName());
                }
            }
        }
        return feedbacks;
    }

    public List<FeedbackEntity> getMyFeedbacks(Long userId, Long activityId) {
        List<FeedbackEntity> feedbacks;
        if (activityId != null) {
            feedbacks = feedbackMapper.selectByActivityAndUser(activityId, userId);
        } else {
            LambdaQueryWrapper<FeedbackEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(FeedbackEntity::getUserId, userId);
            wrapper.orderByDesc(FeedbackEntity::getCreateTime);
            feedbacks = feedbackMapper.selectList(wrapper);
        }
        // 填充活动名称和提交人姓名
        for (FeedbackEntity feedback : feedbacks) {
            ActivityEntity activity = activityService.getById(feedback.getActivityId());
            if (activity != null) {
                feedback.setActivityTitle(activity.getName());
            }
            UserEntity user = userService.getById(feedback.getUserId());
            if (user != null) {
                feedback.setRealName(user.getRealName());
            }
        }
        return feedbacks;
    }

    public List<FeedbackEntity> getCollegeFeedbacks(Long userId, Long activityId) {
        LambdaQueryWrapper<FeedbackEntity> wrapper = new LambdaQueryWrapper<>();
        if (activityId != null) {
            wrapper.eq(FeedbackEntity::getActivityId, activityId);
        }
        wrapper.orderByDesc(FeedbackEntity::getCreateTime);
        List<FeedbackEntity> feedbacks = feedbackMapper.selectList(wrapper);
        // 填充活动名称和提交人姓名
        for (FeedbackEntity feedback : feedbacks) {
            ActivityEntity activity = activityService.getById(feedback.getActivityId());
            if (activity != null) {
                feedback.setActivityTitle(activity.getName());
            }
            UserEntity user = userService.getById(feedback.getUserId());
            if (user != null) {
                feedback.setRealName(user.getRealName());
            }
        }
        return feedbacks;
    }
}
