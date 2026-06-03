-- 为用户表添加 avatar 字段的迁移脚本
-- 执行时间：2026-06-03
-- 用途：修复 "Unknown column 'avatar' in 'field list'" 错误

USE enrollment_db;

-- 检查并添加 avatar 列（如果不存在）
SET @dbname = DATABASE();
SET @tablename = 'user';
SET @columnname = 'avatar';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_schema = @dbname)
      AND (table_name = @tablename)
      AND (column_name = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE `', @tablename, '` ADD COLUMN `', @columnname, '` VARCHAR(500) COMMENT \'头像URL\' AFTER `status`')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 或者直接执行下面的语句（简单直接版本）
-- ALTER TABLE `user` ADD COLUMN `avatar` VARCHAR(500) COMMENT '头像URL' AFTER `status`;

SELECT '数据库迁移完成！avatar 字段已添加到 user 表。' AS message;
