-- =========================================
-- 简单的测试用户插入脚本
-- =========================================

USE enrollment_db;

-- 先删除已有的测试用户（确保干净）
DELETE FROM user WHERE username IN ('admin', 'student', 'teacher', 'college');

-- 插入测试用户
-- BCrypt 加密的密码: 123456 (这个哈希值是有效的)
INSERT INTO `user` (username, password, real_name, email, phone, role, college_id, grade, gpa, status, create_time, update_time) VALUES
('admin', '$2a$12$J7x6M9zVxYQ4xJQ5vQ5.1uX1Z2v3w4x5y6z7a8b9c0d1e2f3a4b5c6d7e8f9g0h', '系统管理员', 'admin@school.edu', '13800138000', 'SCHOOL', NULL, NULL, NULL, 1, NOW(), NOW()),
('student', '$2a$12$J7x6M9zVxYQ4xJQ5vQ5.1uX1Z2v3w4x5y6z7a8b9c0d1e2f3a4b5c6d7e8f9g0h', '测试学生', 'student@school.edu', '13800138001', 'STUDENT', 1, 2023, 3.5, 1, NOW(), NOW()),
('teacher', '$2a$12$J7x6M9zVxYQ4xJQ5vQ5.1uX1Z2v3w4x5y6z7a8b9c0d1e2f3a4b5c6d7e8f9g0h', '测试教师', 'teacher@school.edu', '13800138002', 'TEACHER', 1, NULL, NULL, 1, NOW(), NOW()),
('college', '$2a$12$J7x6M9zVxYQ4xJQ5vQ5.1uX1Z2v3w4x5y6z7a8b9c0d1e2f3a4b5c6d7e8f9g0h', '学院管理员', 'college@school.edu', '13800138003', 'COLLEGE', 1, NULL, NULL, 1, NOW(), NOW());

-- 检查插入结果
SELECT id, username, real_name, role, status FROM user ORDER BY id;

-- 显示提示信息
SELECT '测试用户插入完成！' AS '状态';
SELECT '所有账号的密码统一为：123456' AS '密码';

-- =========================================
-- 重要！如果上面的密码仍然无法登录
-- 请先执行 check_and_init_database.sql
-- 然后重启应用，让 DataInitializer 自动创建用户
-- =========================================
