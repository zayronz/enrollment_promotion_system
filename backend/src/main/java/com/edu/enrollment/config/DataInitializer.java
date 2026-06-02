package com.edu.enrollment.config;

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
        // 检查并修复管理员账号
        UserEntity admin = userMapper.findByUsername("admin");
        if (admin == null) {
            // 创建默认管理员
            UserEntity user = new UserEntity();
            user.setUsername("admin");
            user.setPassword(passwordEncoder.encode("123456"));
            user.setRealName("系统管理员");
            user.setRole("SCHOOL");
            user.setStatus(1);
            userMapper.insert(user);
            log.info("已创建默认管理员账号: admin / 123456");
        } else if (admin.getPassword() == null
                || admin.getPassword().contains("YourGeneratedBCryptHashHere")
                || !admin.getPassword().startsWith("$2a$")) {
            // 修复无效/占位密码哈希
            admin.setPassword(passwordEncoder.encode("123456"));
            userMapper.updateById(admin);
            log.info("已修复管理员账号密码: admin / 123456");
        }

        // 检查 student 用户是否存在
        UserEntity student = userMapper.findByUsername("student");
        if (student == null) {
            UserEntity user = new UserEntity();
            user.setUsername("student");
            user.setPassword(passwordEncoder.encode("123456"));
            user.setRealName("测试学生");
            user.setRole("STUDENT");
            user.setCollegeId(1L);
            user.setGrade(2023);
            user.setStatus(1);
            userMapper.insert(user);
            log.info("已创建测试学生账号: student / 123456");
        }
    }
}
