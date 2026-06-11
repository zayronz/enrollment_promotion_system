-- 确保轮播图表存在
USE enrollment_db;

-- 如果 banner 表不存在，则创建它
CREATE TABLE IF NOT EXISTS `banner` (
                          `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
                          `activity_id` BIGINT COMMENT '关联活动ID',
                          `image_url` VARCHAR(500) NOT NULL COMMENT '图片URL',
                          `link_url` VARCHAR(500) COMMENT '跳转链接',
                          `sort_order` INT DEFAULT 0 COMMENT '排序',
                          `status` TINYINT DEFAULT 1 COMMENT '0-隐藏 1-显示',
                          `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
                          `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 确保 update_time 列存在
SET @dbname = DATABASE();
SET @tablename = 'banner';
SET @columnname = 'update_time';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_schema = @dbname)
      AND (table_name = @tablename)
      AND (column_name = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE `', @tablename, '` ADD COLUMN `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

SELECT 'Banner表检查/创建完成！' AS message;
