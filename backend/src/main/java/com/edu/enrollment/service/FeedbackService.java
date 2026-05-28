package com.edu.enrollment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.edu.enrollment.dto.FeedbackSubmitDTO;
import com.edu.enrollment.entity.FeedbackEntity;
import com.edu.enrollment.entity.RegistrationEntity;
import com.edu.enrollment.entity.UserEntity;
import com.edu.enrollment.exception.BusinessException;
import com.edu.enrollment.mapper.FeedbackMapper;
import com.edu.enrollment.mapper.RegistrationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackMapper feedbackMapper;
    private final RegistrationMapper registrationMapper;
    private final UserService userService;

    @Transactional
    public Long submit(FeedbackSubmitDTO dto, Long userId) {
        // 校验是否已报名且审批通过
        RegistrationEntity registration = registrationMapper.findByActivityAndUser(dto.getActivityId(), userId);
        if (registration == null || registration.getStatus() != 2) {
            throw new BusinessException("只有报名审批通过的用户才能提交反馈");
        }

        UserEntity user = userService.getById(userId);

        FeedbackEntity feedback = new FeedbackEntity();
        feedback.setActivityId(dto.getActivityId());
        feedback.setUserId(userId);
        feedback.setUserRole(user.getRole());
        feedback.setTitle(dto.getTitle());
        feedback.setContent(dto.getContent());
        feedback.setAttachmentUrls(dto.getAttachmentUrls());
        feedback.setType(dto.getType());

        feedbackMapper.insert(feedback);
        return feedback.getId();
    }

    public List<FeedbackEntity> getActivityFeedbacks(Long activityId) {
        return feedbackMapper.selectByActivityId(activityId);
    }

    public List<FeedbackEntity> getMyFeedbacks(Long userId, Long activityId) {
        if (activityId != null) {
            return feedbackMapper.selectByActivityAndUser(activityId, userId);
        }
        LambdaQueryWrapper<FeedbackEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FeedbackEntity::getUserId, userId);
        wrapper.orderByDesc(FeedbackEntity::getCreateTime);
        return feedbackMapper.selectList(wrapper);
    }
}