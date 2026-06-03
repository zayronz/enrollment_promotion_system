-- 头像功能修复脚本
-- 执行时间：2026-06-03

USE enrollment_db;

-- 步骤1：检查并添加avatar字段
SET @column_exists = 0;
SELECT COUNT(*) INTO @column_exists
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'enrollment_db'
  AND TABLE_NAME = 'user'
  AND COLUMN_NAME = 'avatar';

SELECT CONCAT('avatar字段是否存在: ', @column_exists) AS check_result;

-- 如果不存在则添加
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `user` ADD COLUMN `avatar` VARCHAR(500) DEFAULT NULL COMMENT "头像URL" AFTER `status`',
    'SELECT "avatar字段已存在，无需添加" AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 步骤2：验证字段添加成功
SELECT '当前user表结构:' AS message;
DESCRIBE `user`;

-- 步骤3：查看现有用户的头像状态
SELECT '现有用户头像状态:' AS message;
SELECT id, username, real_name, avatar FROM `user` LIMIT 10;

-- 步骤4：清理prepared statements
DEALLOCATE PREPARE stmt;

SELECT '数据库修复完成！请重启后端服务。' AS message;
