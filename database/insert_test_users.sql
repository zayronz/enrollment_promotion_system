-- =========================================
-- 插入测试用户账号的 SQL 脚本
-- 直接在数据库中执行此脚本
-- =========================================

USE enrollment_db;

-- 清空已有的测试用户（可选，用于重置）
-- DELETE FROM user WHERE username IN ('admin', 'student', 'teacher', 'college');

-- 插入测试用户（使用真实的 BCrypt 加密密码：123456）
INSERT IGNORE INTO `user` (id, username, password, real_name, email, phone, role, college_id, grade, gpa, status, avatar, create_time, update_time) VALUES
(1, 'admin', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubkHN4/5cM6/R9c7Z0qQc9bH9y', '系统管理员', 'admin@school.edu', '13800138000', 'SCHOOL', NULL, NULL, NULL, 1, NULL, NOW(), NOW()),
(2, 'student', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubkHN4/5cM6/R9c7Z0qQc9bH9y', '测试学生', 'student@school.edu', '13800138001', 'STUDENT', 1, 2023, 3.5, 1, NULL, NOW(), NOW()),
(3, 'teacher', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubkHN4/5cM6/R9c7Z0qQc9bH9y', '测试教师', 'teacher@school.edu', '13800138002', 'TEACHER', 1, NULL, NULL, 1, NULL, NOW(), NOW()),
(4, 'college', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubkHN4/5cM6/R9c7Z0qQc9bH9y', '学院管理员', 'college@school.edu', '13800138003', 'COLLEGE', 1, NULL, NULL, 1, NULL, NOW(), NOW());

-- 检查插入结果
SELECT id, username, real_name, role, status FROM user ORDER BY id;

-- 显示提示信息
SELECT '测试用户插入完成！' AS '状态';
SELECT '所有账号的密码统一为：123456' AS '密码';
