-- =========================================
-- 简单直接的数据库迁移脚本
-- 解决问题：Unknown column 'avatar' in 'field list'
-- =========================================

-- 步骤 1: 选择数据库
USE enrollment_db;

-- 步骤 2: 添加 avatar 列到 user 表
-- 如果列已存在，这条语句会报错，但不影响已有的数据
ALTER TABLE `user` 
ADD COLUMN `avatar` VARCHAR(500) 
COMMENT '头像URL' 
AFTER `status`;

-- 步骤 3: 验证是否添加成功
SELECT 
    COLUMN_NAME, 
    DATA_TYPE, 
    COLUMN_TYPE, 
    COLUMN_COMMENT 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'enrollment_db' 
  AND TABLE_NAME = 'user'
  AND COLUMN_NAME = 'avatar';

-- =========================================
-- 执行说明：
-- 1. 在 MySQL Workbench、Navicat 或其他数据库管理工具中打开此文件
-- 2. 点击执行（Execute/Run）
-- 3. 如果看到 "avatar" 记录，说明添加成功
-- 4. 然后修改 UserEntity.java，删除 @TableField(exist = false) 注解
-- =========================================
