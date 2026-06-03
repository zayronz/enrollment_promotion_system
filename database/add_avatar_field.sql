-- 为用户表添加 avatar 字段（如果不存在）
-- 执行方法：在 MySQL 命令行或数据库工具中执行此脚本

USE enrollment_db;

-- 检查 avatar 列是否存在，如果不存在则添加
SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'enrollment_db'
      AND TABLE_NAME = 'user'
      AND COLUMN_NAME = 'avatar'
);

-- 如果列不存在，则添加
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `user` ADD COLUMN `avatar` VARCHAR(500) DEFAULT NULL COMMENT "头像URL" AFTER `status`',
    'SELECT "avatar column already exists" AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 验证结果
SELECT 'avatar 字段添加完成！当前 user 表结构：' AS message;
DESCRIBE `user`;
