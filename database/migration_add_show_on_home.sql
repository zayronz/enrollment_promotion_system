-- 为活动表添加首页展示字段
-- 执行此脚本前请先连接到 enrollment_db 数据库

USE enrollment_db;

-- 添加 show_on_home 字段（如果不存在）
SET @column_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_name = 'activity'
    AND column_name = 'show_on_home'
);

SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `activity` ADD COLUMN `show_on_home` TINYINT DEFAULT 0 COMMENT "0-不展示 1-展示" AFTER `creator_id`',
    'SELECT "Column show_on_home already exists" AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 验证字段是否添加成功
DESC `activity`;
