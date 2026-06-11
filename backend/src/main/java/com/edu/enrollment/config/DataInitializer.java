package com.edu.enrollment.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.edu.enrollment.entity.UserEntity;
import com.edu.enrollment.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 数据初始化器
 * 在应用启动时自动创建测试账号，确保密码使用正确的 BCrypt 加密
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        try {
            // 强制更新测试账号密码（确保密码正确）
            upsertUser("admin", "系统管理员", "SCHOOL", null, null, null);
            upsertUser("student", "测试学生", "STUDENT", 1L, 2023, 3.5);
            upsertUser("teacher", "测试教师", "TEACHER", 1L, null, null);
            upsertUser("college", "学院管理员", "COLLEGE", 1L, null, null);
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
}
