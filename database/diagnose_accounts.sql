-- =====================================================
-- 测试账号诊断与修复脚本
-- 如果 teacher / college 账号无法登录，执行此脚本排查
-- =====================================================

USE enrollment_db;

-- 1. 查看当前所有测试账号的状态
SELECT 
    id,
    username,
    real_name,
    role,
    status,
    CASE status 
        WHEN 1 THEN '正常' 
        ELSE '禁用' 
    END AS status_label,
    CASE 
        WHEN password IS NULL THEN '空密码'
        WHEN password = '' THEN '空密码'
        WHEN password LIKE '$2a$%' THEN 'BCrypt加密(正常)'
        ELSE '非BCrypt格式(异常)'
    END AS password_status,
    LEFT(password, 30) AS password_preview,
    create_time
FROM user 
WHERE username IN ('admin', 'student', 'teacher', 'college')
ORDER BY FIELD(role, 'SCHOOL', 'COLLEGE', 'TEACHER', 'STUDENT');

-- 2. 如果 teacher 账号状态异常，启用并修复密码
-- UPDATE user 
-- SET status = 1,
--     password = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO'
-- WHERE username = 'teacher';

-- 3. 如果 college 账号状态异常，启用并修复密码
-- UPDATE user 
-- SET status = 1,
--     password = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO'
-- WHERE username = 'college';

-- 4. 如果账号不存在，直接插入（密码为 "123456" 的 BCrypt 加密值）
-- INSERT INTO user (username, password, real_name, role, college_id, status) VALUES
-- ('teacher', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '测试教师', 'TEACHER', 1, 1),
-- ('college', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '测试学院管理员', 'COLLEGE', 1, 1);
