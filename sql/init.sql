-- 招生宣传报名系统数据库初始化脚本

-- 创建数据库
CREATE DATABASE IF NOT EXISTS enrollment_promotion DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE enrollment_promotion;

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    real_name VARCHAR(50) COMMENT '真实姓名',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    avatar VARCHAR(255) COMMENT '头像URL',
    role INT NOT NULL COMMENT '角色：1学生 2教师 3学院管理员 4学校管理员',
    college_id BIGINT COMMENT '学院ID',
    college_name VARCHAR(100) COMMENT '学院名称',
    student_no VARCHAR(50) COMMENT '学号',
    major VARCHAR(100) COMMENT '专业',
    gpa DOUBLE COMMENT '绩点',
    score DOUBLE COMMENT '成绩',
    status INT DEFAULT 1 COMMENT '状态：0禁用 1启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_role (role),
    INDEX idx_college_id (college_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 活动表
CREATE TABLE IF NOT EXISTS activity (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '活动ID',
    title VARCHAR(200) NOT NULL COMMENT '活动标题',
    description TEXT COMMENT '活动简介',
    content TEXT COMMENT '活动详情',
    activity_type INT COMMENT '活动类型：1线上 2线下',
    activity_mode INT COMMENT '活动模式：1宣传 2招生',
    start_time DATETIME COMMENT '活动开始时间',
    end_time DATETIME COMMENT '活动结束时间',
    registration_start_time DATETIME COMMENT '报名开始时间',
    registration_end_time DATETIME COMMENT '报名结束时间',
    target_users TEXT COMMENT '目标用户',
    max_students INT COMMENT '最大学生数',
    max_teachers INT COMMENT '最大教师数',
    approval_flow VARCHAR(500) COMMENT '审批流程',
    custom_fields TEXT COMMENT '自定义字段',
    banner_images TEXT COMMENT '轮播图',
    attachments TEXT COMMENT '附件',
    status INT DEFAULT 0 COMMENT '状态：0草稿 1已发布',
    creator_id BIGINT COMMENT '创建者ID',
    creator_name VARCHAR(50) COMMENT '创建者名称',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_status (status),
    INDEX idx_activity_type (activity_type),
    INDEX idx_creator_id (creator_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动表';

-- 报名记录表
CREATE TABLE IF NOT EXISTS enrollment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '报名ID',
    activity_id BIGINT NOT NULL COMMENT '活动ID',
    activity_title VARCHAR(200) COMMENT '活动标题',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    user_name VARCHAR(50) COMMENT '用户名',
    real_name VARCHAR(50) COMMENT '真实姓名',
    role INT NOT NULL COMMENT '用户角色',
    enrollment_data TEXT COMMENT '报名数据',
    attachments TEXT COMMENT '附件',
    approval_status INT DEFAULT 0 COMMENT '审批状态：0待审核 1审核中 2已通过 3已拒绝',
    current_flow_node INT DEFAULT 1 COMMENT '当前审批节点',
    group_name VARCHAR(100) COMMENT '分组名称',
    group_rank INT COMMENT '组内排名',
    enrollment_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_activity_id (activity_id),
    INDEX idx_user_id (user_id),
    INDEX idx_approval_status (approval_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报名记录表';

-- 活动反馈表
CREATE TABLE IF NOT EXISTS feedback (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '反馈ID',
    enrollment_id BIGINT COMMENT '报名ID',
    activity_id BIGINT COMMENT '活动ID',
    user_id BIGINT COMMENT '用户ID',
    user_name VARCHAR(50) COMMENT '用户名',
    content TEXT COMMENT '反馈内容',
    attachments TEXT COMMENT '附件',
    feedback_type INT DEFAULT 0 COMMENT '反馈类型：0工作反馈',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_enrollment_id (enrollment_id),
    INDEX idx_activity_id (activity_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动反馈表';

-- 审批记录表
CREATE TABLE IF NOT EXISTS approval_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '审批记录ID',
    enrollment_id BIGINT NOT NULL COMMENT '报名ID',
    activity_id BIGINT COMMENT '活动ID',
    approver_id BIGINT COMMENT '审批人ID',
    approver_name VARCHAR(50) COMMENT '审批人名称',
    flow_node INT COMMENT '流程节点',
    approval_status INT COMMENT '审批状态：1通过 2不通过',
    approval_comment TEXT COMMENT '审批意见',
    attachments TEXT COMMENT '附件',
    approval_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '审批时间',
    INDEX idx_enrollment_id (enrollment_id),
    INDEX idx_approver_id (approver_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批记录表';

-- 轮播图表
CREATE TABLE IF NOT EXISTS banner (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '轮播图ID',
    title VARCHAR(100) COMMENT '标题',
    image_url VARCHAR(255) NOT NULL COMMENT '图片URL',
    link_url VARCHAR(255) COMMENT '跳转链接',
    activity_id BIGINT COMMENT '关联活动ID',
    sort INT DEFAULT 0 COMMENT '排序',
    status INT DEFAULT 1 COMMENT '状态：0禁用 1启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_status (status),
    INDEX idx_sort (sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='轮播图表';

-- 插入测试数据
INSERT INTO sys_user (username, password, real_name, role, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '系统管理员', 4, 1),
('student1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '张三', 1, 1),
('teacher1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '李老师', 2, 1);

INSERT INTO activity (title, description, activity_type, activity_mode, status, creator_id, creator_name) VALUES
('2024年高校招生宣传活动', '面向全国高中生的招生宣传活动', 2, 2, 1, 1, '系统管理员'),
('线上招生宣讲会', '通过网络直播进行招生宣讲', 1, 1, 1, 1, '系统管理员');

INSERT INTO banner (title, image_url, link_url, sort, status) VALUES
('招生宣传Banner1', '/uploads/banner1.jpg', 'http://example.com/activity/1', 1, 1),
('招生宣传Banner2', '/uploads/banner2.jpg', 'http://example.com/activity/2', 2, 1);

-- 注意：测试密码均为 123456
-- 实际密码已使用BCrypt加密
