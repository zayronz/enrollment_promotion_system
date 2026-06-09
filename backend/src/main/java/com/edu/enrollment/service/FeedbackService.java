package com.edu.enrollment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.edu.enrollment.dto.FeedbackSubmitDTO;
import com.edu.enrollment.entity.FeedbackEntity;
import com.edu.enrollment.entity.RegistrationEntity;
import com.edu.enrollment.entity.UserEntity;
import com.edu.enrollment.exception.BusinessException;
import com.edu.enrollment.mapper.FeedbackMapper;
import com.edu.enrollment.mapper.RegistrationMapper;
import com.edu.enrollment.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackMapper feedbackMapper;
    private final RegistrationMapper registrationMapper;
    private final UserService userService;
    private final UserMapper userMapper;

    @Transactional
    public Long submit(FeedbackSubmitDTO dto, Long userId) {
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
        feedback.setType(dto.getType() != null ? dto.getType() : 0);

        feedbackMapper.insert(feedback);
        return feedback.getId();
    }

    public List<FeedbackEntity> getAllFeedbacks() {
        LambdaQueryWrapper<FeedbackEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(FeedbackEntity::getCreateTime);
        return feedbackMapper.selectList(wrapper);
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

    public List<FeedbackEntity> getCollegeFeedbacks(Long collegeId, Long activityId) {
        List<UserEntity> collegeUsers = userMapper.selectByCollegeId(collegeId);
        List<Long> userIds = new ArrayList<>();
        for (UserEntity user : collegeUsers) {
            userIds.add(user.getId());
        }

        LambdaQueryWrapper<FeedbackEntity> wrapper = new LambdaQueryWrapper<>();
        if (userIds.size() > 0) {
            wrapper.in(FeedbackEntity::getUserId, userIds);
        } else {
            return new ArrayList<>();
        }
        if (activityId != null) {
            wrapper.eq(FeedbackEntity::getActivityId, activityId);
        }
        wrapper.orderByDesc(FeedbackEntity::getCreateTime);
        return feedbackMapper.selectList(wrapper);
    }

    public List<FeedbackEntity> getFeedbacksByRole(Long userId, String role, Long activityId) {
        UserEntity user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if ("SCHOOL".equals(role)) {
            return getSchoolFeedbacks(activityId);
        } else if ("COLLEGE".equals(role)) {
            return getCollegeFeedbacksWithOwn(user.getCollegeId(), userId, activityId);
        } else {
            return getMyFeedbacks(userId, activityId);
        }
    }

    private List<FeedbackEntity> getSchoolFeedbacks(Long activityId) {
        LambdaQueryWrapper<FeedbackEntity> wrapper = new LambdaQueryWrapper<>();
        if (activityId != null) {
            wrapper.eq(FeedbackEntity::getActivityId, activityId);
        }
        wrapper.orderByDesc(FeedbackEntity::getCreateTime);
        return feedbackMapper.selectList(wrapper);
    }

    private List<FeedbackEntity> getCollegeFeedbacksWithOwn(Long collegeId, Long userId, Long activityId) {
        List<UserEntity> collegeUsers = userMapper.selectByCollegeId(collegeId);
        List<Long> userIds = new ArrayList<>();
        userIds.add(userId);
        for (UserEntity user : collegeUsers) {
            if (!user.getId().equals(userId)) {
                userIds.add(user.getId());
            }
        }

        LambdaQueryWrapper<FeedbackEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(FeedbackEntity::getUserId, userIds);
        if (activityId != null) {
            wrapper.eq(FeedbackEntity::getActivityId, activityId);
        }
        wrapper.orderByDesc(FeedbackEntity::getCreateTime);
        return feedbackMapper.selectList(wrapper);
    }
}