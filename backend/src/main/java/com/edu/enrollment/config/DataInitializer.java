package com.edu.enrollment.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.edu.enrollment.entity.ActivityEntity;
import com.edu.enrollment.entity.FeedbackEntity;
import com.edu.enrollment.entity.MaterialEntity;
import com.edu.enrollment.entity.RegistrationEntity;
import com.edu.enrollment.entity.UserEntity;
import com.edu.enrollment.mapper.ActivityMapper;
import com.edu.enrollment.mapper.FeedbackMapper;
import com.edu.enrollment.mapper.MaterialMapper;
import com.edu.enrollment.mapper.RegistrationMapper;
import com.edu.enrollment.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 数据初始化器
 * 在应用启动时自动创建测试账号，确保密码使用正确的 BCrypt 加密
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final ActivityMapper activityMapper;
    private final RegistrationMapper registrationMapper;
    private final FeedbackMapper feedbackMapper;
    private final MaterialMapper materialMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        try {
            // 强制更新测试账号密码（确保密码正确）
            upsertUser("admin", "系统管理员", "SCHOOL", null, null, null);
            upsertUser("student", "测试学生", "STUDENT", 1L, 2023, 3.5);
            upsertUser("teacher", "测试教师", "TEACHER", 1L, null, null);
            upsertUser("college", "学院管理员", "COLLEGE", 1L, null, null);
            seedH5DemoData();
        } catch (Exception e) {
            log.error("初始化用户数据时出错", e);
        }
    }

    private void upsertUser(String username, String realName, String role,
                           Long collegeId, Integer grade, Double gpa) {
        // 使用安全的查询方式
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getUsername, username);
        wrapper.last("LIMIT 1");

        UserEntity existingUser = userMapper.selectOne(wrapper);

        String encodedPassword = passwordEncoder.encode("123456");

        if (existingUser == null) {
            // 用户不存在，创建新用户
            UserEntity user = new UserEntity();
            user.setUsername(username);
            user.setPassword(encodedPassword);
            user.setRealName(realName);
            user.setRole(role);
            user.setCollegeId(collegeId);
            user.setGrade(grade);
            if (gpa != null) {
                user.setGpa(java.math.BigDecimal.valueOf(gpa));
            }
            user.setStatus(1);
            userMapper.insert(user);
        } else {
            // 用户已存在，更新密码确保可以登录
            existingUser.setPassword(encodedPassword);
            userMapper.updateById(existingUser);
        }
    }

    private void seedH5DemoData() {
        UserEntity admin = getUser("admin");
        UserEntity student = getUser("student");
        UserEntity teacher = getUser("teacher");
        if (admin == null || student == null || teacher == null) {
            return;
        }

        ActivityEntity homeActivity = upsertActivity(
                "2026年寒假母校行活动",
                "面向优秀学生招募母校行志愿者，返回高中母校开展招生宣传、经验分享和政策宣讲。",
                1,
                "各生源高中",
                admin.getId(),
                "/images/banner/banner1.jpg"
        );
        ActivityEntity promoActivity = upsertActivity(
                "2026年春季招宣活动",
                "组织学生和教师赴重点中学开展招生宣传，介绍学校办学特色、专业优势和报考政策。",
                0,
                "线上宣讲",
                admin.getId(),
                "/images/banner/banner2.jpg"
        );

        upsertRegistration(homeActivity.getId(), student, 0, "太原理工大学附属中学", 88, 2,
                "太原理工大学附属中学招生组", 1);
        upsertRegistration(homeActivity.getId(), teacher, 1, "太原理工大学附属中学", 95, 2,
                "太原理工大学附属中学招生组", 2);
        upsertRegistration(promoActivity.getId(), student, 0, "太原理工大学", 86, 0,
                null, null);

        upsertFeedback(homeActivity.getId(), student.getId());
        upsertMaterial("2026本科招生简章", "招生简章", "2026本科招生简章.pdf", "/uploads/material/2026本科招生简章.pdf", admin.getId());
        upsertMaterial("学校宣传PPT", "宣讲课件", "学校宣传PPT.pptx", "/uploads/material/学校宣传PPT.pptx", admin.getId());
        upsertMaterial("招生政策问答手册", "政策问答", "招生政策问答手册.docx", "/uploads/material/招生政策问答手册.docx", admin.getId());
    }

    private UserEntity getUser(String username) {
        return userMapper.selectOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getUsername, username)
                .last("LIMIT 1"));
    }

    private ActivityEntity upsertActivity(String name, String description, Integer type,
                                          String location, Long creatorId, String bannerUrl) {
        ActivityEntity existing = activityMapper.selectOne(new LambdaQueryWrapper<ActivityEntity>()
                .eq(ActivityEntity::getName, name)
                .last("LIMIT 1"));
        if (existing != null) {
            return existing;
        }
        ActivityEntity activity = new ActivityEntity();
        activity.setName(name);
        activity.setDescription(description);
        activity.setType(type);
        activity.setLocation(location);
        activity.setActivityStartTime(LocalDateTime.now().plusDays(10));
        activity.setActivityEndTime(LocalDateTime.now().plusDays(20));
        activity.setRegistrationStartTime(LocalDateTime.now().minusDays(3));
        activity.setRegistrationEndTime(LocalDateTime.now().plusDays(8));
        activity.setBannerUrl(bannerUrl);
        activity.setCoverImage(bannerUrl);
        activity.setStatus(1);
        activity.setAuditFlow("[\"college_audit\",\"school_audit\"]");
        activity.setCustomFields("[{\"type\":\"input\",\"label\":\"高考分数\",\"name\":\"score\",\"required\":true},{\"type\":\"feedback_rule\",\"name\":\"__feedback_rule__\",\"label\":\"反馈规则\",\"feedbackDeadline\":\"2026-12-31T23:59:59\"}]");
        activity.setMaxStudentPerSchool(5);
        activity.setMaxTeacherPerSchool(2);
        activity.setAutoGroup(1);
        activity.setCreatorId(creatorId);
        activity.setShowOnHome(1);
        activityMapper.insert(activity);
        return activity;
    }

    private void upsertRegistration(Long activityId, UserEntity user, Integer userType,
                                    String targetSchool, Integer score, Integer status,
                                    String groupName, Integer groupRank) {
        RegistrationEntity existing = registrationMapper.selectOne(new LambdaQueryWrapper<RegistrationEntity>()
                .eq(RegistrationEntity::getActivityId, activityId)
                .eq(RegistrationEntity::getUserId, user.getId())
                .last("LIMIT 1"));
        if (existing != null) {
            return;
        }
        RegistrationEntity registration = new RegistrationEntity();
        registration.setActivityId(activityId);
        registration.setUserId(user.getId());
        registration.setUserType(userType);
        registration.setTargetSchool(targetSchool);
        registration.setScore(score);
        registration.setStatus(status);
        registration.setCurrentNode(status == 0 ? "college_audit" : "completed");
        registration.setGroupName(groupName);
        registration.setGroupRank(groupRank);
        registration.setFormData("{\"basicInfo\":{\"realName\":\"" + user.getRealName()
                + "\",\"username\":\"" + user.getUsername()
                + "\",\"collegeName\":\"信息学院\"},\"targetSchool\":\"" + targetSchool + "\"}");
        registrationMapper.insert(registration);
    }

    private void upsertFeedback(Long activityId, Long userId) {
        FeedbackEntity existing = feedbackMapper.selectOne(new LambdaQueryWrapper<FeedbackEntity>()
                .eq(FeedbackEntity::getActivityId, activityId)
                .eq(FeedbackEntity::getTitle, "母校行宣讲总结")
                .last("LIMIT 1"));
        if (existing != null) {
            return;
        }
        FeedbackEntity feedback = new FeedbackEntity();
        feedback.setActivityId(activityId);
        feedback.setUserId(userId);
        feedback.setUserRole("STUDENT");
        feedback.setTitle("母校行宣讲总结");
        feedback.setContent("<p>本次母校行面向高三学生介绍了学校专业特色、招生政策和校园生活，现场互动积极。</p>");
        feedback.setAttachmentUrls("[]");
        feedback.setType(1);
        feedbackMapper.insert(feedback);
    }

    private void upsertMaterial(String name, String category, String fileName, String filePath, Long uploaderId) {
        MaterialEntity existing = materialMapper.selectOne(new LambdaQueryWrapper<MaterialEntity>()
                .eq(MaterialEntity::getName, name)
                .last("LIMIT 1"));
        if (existing != null) {
            return;
        }
        MaterialEntity material = new MaterialEntity();
        material.setName(name);
        material.setCategory(category);
        material.setDescription("H5 与 PC 共用的招宣演示资料");
        material.setFileName(fileName);
        material.setFilePath(filePath);
        material.setFileSize(1024L * 1024L);
        material.setFileType("application/octet-stream");
        material.setUploaderId(uploaderId);
        materialMapper.insert(material);
    }
}
