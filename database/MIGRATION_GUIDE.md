# 数据库迁移指南 - 添加 avatar 字段

## 🚨 当前问题
错误信息：`Unknown column 'avatar' in 'field list'`

## ✅ 临时解决方案（已实施）
我已临时修改了 `UserEntity.java`，将 `avatar` 字段标记为非数据库字段，这样应用可以先正常运行。

---

## 📋 完整修复步骤

### 第一步：执行数据库迁移

**方式 A：使用数据库管理工具（推荐）**
1. 打开 MySQL Workbench、Navicat、phpMyAdmin 或其他工具
2. 连接到 `enrollment_db` 数据库
3. 打开 `simple_migration_add_avatar.sql` 文件并执行

**方式 B：使用命令行**
```bash
mysql -u your_username -p enrollment_db < database/simple_migration_add_avatar.sql
```

**方式 C：手动执行 SQL**
直接在数据库中执行：
```sql
USE enrollment_db;
ALTER TABLE `user` ADD COLUMN `avatar` VARCHAR(500) COMMENT '头像URL' AFTER `status`;
```

### 第二步：验证迁移成功
执行以下 SQL 检查：
```sql
SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'enrollment_db' 
  AND TABLE_NAME = 'user' 
  AND COLUMN_NAME = 'avatar';
```
如果返回 `avatar`，说明迁移成功！

### 第三步：恢复代码
修改 `backend/src/main/java/com/edu/enrollment/entity/UserEntity.java`：
- 删除或注释掉 `@TableField(exist = false)` 注解
- 删除或注释掉 `// TODO: 数据库迁移完成后...` 注释

### 第四步：重启应用
重启您的 Spring Boot 应用程序即可。

---

## 📁 相关文件
- `schema.sql` - 数据库初始化脚本（已更新）
- `simple_migration_add_avatar.sql` - 简单迁移脚本（推荐使用）
- `migration_add_avatar_column.sql` - 带检查的高级迁移脚本
- `UserEntity.java` - 实体类（已临时修复）
