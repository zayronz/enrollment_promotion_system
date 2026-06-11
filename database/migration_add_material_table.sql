CREATE TABLE IF NOT EXISTS `material` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(200) NOT NULL COMMENT '资料名称',
    `category` VARCHAR(50) NOT NULL COMMENT '资料分类',
    `description` VARCHAR(500) COMMENT '资料说明',
    `file_name` VARCHAR(200) NOT NULL COMMENT '原文件名',
    `file_path` VARCHAR(500) NOT NULL COMMENT '存储路径',
    `file_size` BIGINT COMMENT '文件大小(字节)',
    `file_type` VARCHAR(100) COMMENT '文件类型',
    `uploader_id` BIGINT COMMENT '上传人ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_category` (`category`),
    INDEX `idx_uploader` (`uploader_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='招宣资料表';
