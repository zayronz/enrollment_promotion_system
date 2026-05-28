package com.wyx.enrollment_promotion_systemmaster.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wyx.enrollment_promotion_systemmaster.entity.Feedback;
import com.wyx.enrollment_promotion_systemmaster.exception.BusinessException;
import com.wyx.enrollment_promotion_systemmaster.mapper.FeedbackMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeedbackService extends ServiceImpl<FeedbackMapper, Feedback> {

    public Feedback submitFeedback(Long enrollmentId, Long activityId, Long userId, String userName,
                                    String content, String attachments, Integer feedbackType) {
        Feedback feedback = new Feedback();
        feedback.setEnrollmentId(enrollmentId);
        feedback.setActivityId(activityId);
        feedback.setUserId(userId);
        feedback.setUserName(userName);
        feedback.setContent(content);
        feedback.setAttachments(attachments);
        feedback.setFeedbackType(feedbackType);

        this.save(feedback);
        return feedback;
    }

    public Page<Feedback> getFeedbacksByEnrollment(Long enrollmentId, Integer pageNum, Integer pageSize) {
        Page<Feedback> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Feedback> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Feedback::getEnrollmentId, enrollmentId)
                .orderByDesc(Feedback::getCreateTime);

        return this.page(page, wrapper);
    }

    public Page<Feedback> getFeedbacksByActivity(Long activityId, Integer pageNum, Integer pageSize) {
        Page<Feedback> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Feedback> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Feedback::getActivityId, activityId)
                .orderByDesc(Feedback::getCreateTime);

        return this.page(page, wrapper);
    }

    public Page<Feedback> getFeedbacksByCollege(Long collegeId, Integer pageNum, Integer pageSize) {
        Page<Feedback> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Feedback> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Feedback::getCreateTime);

        return this.page(page, wrapper);
    }

    public List<Feedback> getAllFeedbacksByActivity(Long activityId) {
        LambdaQueryWrapper<Feedback> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Feedback::getActivityId, activityId)
                .orderByDesc(Feedback::getCreateTime);

        return this.list(wrapper);
    }
}
