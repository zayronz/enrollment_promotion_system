-- =========================================
-- 数据库检查与初始化脚本
-- 使用说明：在 MySQL 客户端中执行此脚本
-- =========================================

-- 检查 MySQL 版本
SELECT VERSION() AS 'MySQL 版本';

-- 1. 检查数据库是否存在，如果不存在则创建
SET @dbname = 'enrollment_db';
SET @preparedStatement = CONCAT('CREATE DATABASE IF NOT EXISTS `', @dbname, '` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci');
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

USE enrollment_db;

-- 显示当前数据库
SELECT DATABASE() AS '当前数据库';

-- 2. 检查并创建 user 表（如果不存在）
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 3. 检查并添加 avatar 列（如果不存在）
SET @preparedStatement = NULL;
SELECT CONCAT('ALTER TABLE `user` ADD COLUMN `avatar` VARCHAR(500) COMMENT \'头像URL\' AFTER `status`') 
INTO @preparedStatement
FROM information_schema.tables
WHERE table_schema = 'enrollment_db'
  AND table_name = 'user'
  AND NOT EXISTS (
    SELECT 1 FROM information_schema.columns 
    WHERE table_schema = 'enrollment_db' 
      AND table_name = 'user' 
      AND column_name = 'avatar'
  );

SET @preparedStatement = IFNULL(@preparedStatement, 'SELECT \'avatar 列已存在，无需添加\' AS message');
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4. 检查并创建其他表
CREATE TABLE IF NOT EXISTS `college` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL COMMENT '学院名称',
    `code` VARCHAR(20) NOT NULL COMMENT '学院代码',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学院表';

CREATE TABLE IF NOT EXISTS `activity` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(200) NOT NULL COMMENT '活动名称',
    `description` TEXT COMMENT '活动简介(富文本)',
    `type` TINYINT NOT NULL COMMENT '0-线上 1-线下',
    `location` VARCHAR(200) COMMENT '活动地点',
    `activity_start_time` DATETIME NOT NULL COMMENT '活动开始时间',
    `activity_end_time` DATETIME NOT NULL COMMENT '活动结束时间',
    `registration_start_time` DATETIME NOT NULL COMMENT '报名开始时间',
    `registration_end_time` DATETIME NOT NULL COMMENT '报名结束时间',
    `banner_url` VARCHAR(500) COMMENT '轮播图URL',
    `video_url` VARCHAR(500) COMMENT '视频URL',
    `cover_image` VARCHAR(500) COMMENT '封面图',
    `status` TINYINT DEFAULT 0 COMMENT '0-草稿 1-发布 2-已结束',
    `audit_flow` TEXT COMMENT '审批流程配置JSON',
    `custom_fields` TEXT COMMENT '自定义收集字段JSON',
    `max_student_per_school` INT DEFAULT 10 COMMENT '每所学校最多学生数',
    `max_teacher_per_school` INT DEFAULT 5 COMMENT '每所学校最多教师数',
    `auto_group` TINYINT DEFAULT 1 COMMENT '是否自动分组 0-否 1-是',
    `creator_id` BIGINT COMMENT '创建人ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动表';

CREATE TABLE IF NOT EXISTS `registration` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `activity_id` BIGINT NOT NULL COMMENT '活动ID',
    `user_id` BIGINT NOT NULL COMMENT '报名用户ID',
    `user_type` TINYINT NOT NULL COMMENT '0-学生 1-老师',
    `target_school` VARCHAR(200) COMMENT '招生对象学校(标准化后)',
    `score` INT COMMENT '成绩/绩点(处理后数值)',
    `form_data` TEXT COMMENT '自定义表单填写数据JSON',
    `status` TINYINT DEFAULT 0 COMMENT '0-待审核 1-学院通过 2-学校通过 3-已拒绝 4-已撤回',
    `current_node` VARCHAR(50) COMMENT '当前审批节点',
    `reject_reason` VARCHAR(500) COMMENT '拒绝原因',
    `group_name` VARCHAR(100) COMMENT '分组名称',
    `group_rank` INT COMMENT '组内排名',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_activity_user (activity_id, user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报名记录表';

CREATE TABLE IF NOT EXISTS `audit_record` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `registration_id` BIGINT NOT NULL COMMENT '报名记录ID',
    `node` VARCHAR(50) NOT NULL COMMENT '审核节点',
    `auditor_id` BIGINT NOT NULL COMMENT '审核人ID',
    `auditor_name` VARCHAR(50) COMMENT '审核人姓名',
    `result` TINYINT NOT NULL COMMENT '1-通过 2-拒绝',
    `comment` VARCHAR(500) COMMENT '审核意见',
    `attachment_urls` TEXT COMMENT '附件URL列表JSON',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_registration (registration_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审核记录表';

CREATE TABLE IF NOT EXISTS `feedback` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `activity_id` BIGINT NOT NULL COMMENT '活动ID',
    `user_id` BIGINT NOT NULL COMMENT '提交人ID',
    `user_role` VARCHAR(20) COMMENT '提交人角色',
    `title` VARCHAR(200) NOT NULL COMMENT '反馈标题',
    `content` TEXT COMMENT '反馈内容(富文本)',
    `attachment_urls` TEXT COMMENT '附件URL列表JSON',
    `type` TINYINT DEFAULT 0 COMMENT '0-个人反馈 1-总结报告',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_activity (activity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工作反馈表';

CREATE TABLE IF NOT EXISTS `attachment` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `file_name` VARCHAR(200) NOT NULL COMMENT '原文件名',
    `file_path` VARCHAR(500) NOT NULL COMMENT '存储路径',
    `file_size` BIGINT COMMENT '文件大小(字节)',
    `file_type` VARCHAR(50) COMMENT '文件类型',
    `related_id` BIGINT COMMENT '关联记录ID',
    `related_type` VARCHAR(50) COMMENT '关联类型(registration/feedback/audit)',
    `uploader_id` BIGINT COMMENT '上传人ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='附件表';

CREATE TABLE IF NOT EXISTS `banner` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `activity_id` BIGINT COMMENT '关联活动ID',
    `image_url` VARCHAR(500) NOT NULL COMMENT '图片URL',
    `link_url` VARCHAR(500) COMMENT '跳转链接',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '0-隐藏 1-显示',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='轮播图表';

CREATE TABLE IF NOT EXISTS `school_dict` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `standard_name` VARCHAR(100) NOT NULL COMMENT '标准学校名称',
    `alias_names` TEXT COMMENT '别名列表JSON',
    `province` VARCHAR(50) COMMENT '省份',
    `city` VARCHAR(50) COMMENT '城市',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学校名称标准化词典表';

-- 5. 插入初始数据（如果不存在）
INSERT IGNORE INTO `college` (`id`, `name`, `code`) VALUES
(1, '计算机科学与技术学院', '01'),
(2, '信息工程学院', '02'),
(3, '经济学院', '03');

-- 6. 显示数据库表结构检查结果
SELECT 
    TABLE_NAME AS '表名',
    TABLE_COMMENT AS '注释',
    TABLE_ROWS AS '记录数',
    CREATE_TIME AS '创建时间'
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = 'enrollment_db'
ORDER BY TABLE_NAME;

-- 7. 显示 user 表的列信息
SELECT 
    COLUMN_NAME AS '列名',
    DATA_TYPE AS '数据类型',
    IS_NULLABLE AS '是否可空',
    COLUMN_KEY AS '键类型',
    COLUMN_DEFAULT AS '默认值',
    COLUMN_COMMENT AS '注释'
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = 'enrollment_db' 
  AND TABLE_NAME = 'user'
ORDER BY ORDINAL_POSITION;

-- 8. 完成信息
SELECT '数据库检查与初始化完成！' AS '状态';
