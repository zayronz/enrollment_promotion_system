-- 创建数据库
CREATE DATABASE IF NOT EXISTS enrollment_db
    DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE enrollment_db;

-- 1. 用户表
CREATE TABLE `user` (
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
);

-- 2. 学院表
CREATE TABLE `college` (
                           `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
                           `name` VARCHAR(100) NOT NULL COMMENT '学院名称',
                           `code` VARCHAR(20) NOT NULL COMMENT '学院代码',
                           `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
                           `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 3. 活动表
CREATE TABLE `activity` (
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
                            `banner_urls` TEXT COMMENT '轮播图URL列表（JSON格式）',
                            `video_url` VARCHAR(500) COMMENT '视频URL',
                            `cover_image` VARCHAR(500) COMMENT '封面图',
                            `status` TINYINT DEFAULT 0 COMMENT '0-草稿 1-发布 2-已结束',
                            `audit_flow` TEXT COMMENT '审批流程配置JSON',
                            `custom_fields` TEXT COMMENT '自定义收集字段JSON',
                            `max_student_per_school` INT DEFAULT 10 COMMENT '每所学校最多学生数',
                            `max_teacher_per_school` INT DEFAULT 5 COMMENT '每所学校最多教师数',
                            `auto_group` TINYINT DEFAULT 1 COMMENT '是否自动分组 0-否 1-是',
                            `creator_id` BIGINT COMMENT '创建人ID',
                            `show_on_home` TINYINT DEFAULT 0 COMMENT '0-不展示 1-展示',
                            `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
                            `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 4. 报名记录表
CREATE TABLE `registration` (
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
);

-- 5. 审核记录表
CREATE TABLE `audit_record` (
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
);

-- 6. 工作反馈表
CREATE TABLE `feedback` (
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
);

-- 7. 附件表
CREATE TABLE `attachment` (
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
);

-- 8. 轮播图表
CREATE TABLE `banner` (
                          `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
                          `activity_id` BIGINT COMMENT '关联活动ID',
                          `image_url` VARCHAR(500) NOT NULL COMMENT '图片URL',
                          `link_url` VARCHAR(500) COMMENT '跳转链接',
                          `sort_order` INT DEFAULT 0 COMMENT '排序',
                          `status` TINYINT DEFAULT 1 COMMENT '0-隐藏 1-显示',
                          `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
                          `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 9. 学校名称标准化词典表
CREATE TABLE `school_dict` (
                               `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
                               `standard_name` VARCHAR(100) NOT NULL COMMENT '标准学校名称',
                               `alias_names` TEXT COMMENT '别名列表JSON',
                               `province` VARCHAR(50) COMMENT '省份',
                               `city` VARCHAR(50) COMMENT '城市',
                               `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
                               `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 插入测试数据
INSERT INTO `college` (`name`, `code`) VALUES
                                           ('计算机科学与技术学院', '01'),
                                           ('信息工程学院', '02'),
                                           ('经济学院', '03');

-- 注意：测试账号由应用启动时的 DataInitializer 自动创建，
-- 密码通过 BCryptPasswordEncoder 实时加密，确保哈希值正确。
-- 如需手动创建用户，可使用注册接口 POST /api/user/register


ALTER TABLE `college` ADD COLUMN `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
ALTER TABLE `feedback` ADD COLUMN `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
ALTER TABLE `audit_record` ADD COLUMN `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
ALTER TABLE `attachment` ADD COLUMN `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
ALTER TABLE `banner` ADD COLUMN `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
ALTER TABLE `school_dict` ADD COLUMN `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
