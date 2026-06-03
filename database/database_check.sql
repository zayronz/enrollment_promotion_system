-- =========================================
-- 数据库完整检查和初始化脚本
-- =========================================

-- 步骤 1: 检查 MySQL 是否可以连接
SELECT 'MySQL 连接成功' AS status, NOW() AS current_time;

-- 步骤 2: 检查数据库是否存在
SELECT 
    CASE WHEN EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.SCHEMATA 
        WHERE SCHEMA_NAME = 'enrollment_db'
    ) THEN '存在' ELSE '不存在' END AS database_status;

-- 步骤 3: 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS enrollment_db 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- 步骤 4: 使用数据库
USE enrollment_db;

-- 步骤 5: 检查 user 表是否存在
SELECT 
    CASE WHEN EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.TABLES 
        WHERE TABLE_SCHEMA = 'enrollment_db' 
        AND TABLE_NAME = 'user'
    ) THEN '存在' ELSE '不存在' END AS user_table_status;

-- 步骤 6: 检查 user 表的所有列
SELECT 
    COLUMN_NAME, 
    DATA_TYPE, 
    COLUMN_TYPE, 
    IS_NULLABLE, 
    COLUMN_DEFAULT, 
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'enrollment_db' 
  AND TABLE_NAME = 'user'
ORDER BY ORDINAL_POSITION;

-- =========================================
-- 修复脚本：如果表不存在，重新创建所有表
-- =========================================

-- 如果 user 表不存在，创建它（带 avatar 列）
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL COMMENT '用户名/学号/工号',
    `password` VARCHAR(255) NOT NULL COMMENT '加密密码',
    `real_name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
    `email` VARCHAR(100) COMMENT '邮箱',
    `phone` VARCHAR(20) COMMENT '手机号',
    `role` VARCHAR(20) NOT NULL COMMENT '角色: STUDENT, TEACHER, COLLEGE, SCHOOL',
    `college_id` BIGINT COMMENT '所属学院ID',
    `grade` INT COMMENT '年级(学生)',
    `gpa` DECIMAL(3,2) COMMENT '绩点(学生)',
    `status` TINYINT DEFAULT 1 COMMENT '0-禁用 1-启用',
    `avatar` VARCHAR(500) COMMENT '头像URL',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 如果 college 表不存在，创建它
CREATE TABLE IF NOT EXISTS `college` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL COMMENT '学院名称',
    `code` VARCHAR(20) NOT NULL COMMENT '学院代码',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 如果 college 表是空的，插入测试数据
INSERT IGNORE INTO `college` (`name`, `code`) VALUES
('计算机科学与技术学院', '01'),
('信息工程学院', '02'),
('经济学院', '03');

--