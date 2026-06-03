# 快速修复指南

## 🚨 问题：账号密码不正确

### 🔧 解决方案 1：直接执行 SQL 脚本（推荐）

**步骤：**
1. 打开 MySQL 客户端（MySQL Workbench、Navicat 等）
2. 连接到数据库
3. 先执行 [check_and_init_database.sql](check_and_init_database.sql) 确保表结构完整
4. 再执行 [insert_test_users.sql](insert_test_users.sql) 插入测试账号

**insert_test_users.sql 会创建：**
| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | 123456 | 学校管理员 |
| student | 123456 | 学生 |
| teacher | 123456 | 教师 |
| college | 123456 | 学院管理员 |

---

### 🔧 解决方案 2：修复 avatar 字段问题

如果您已经执行了数据库迁移并添加了 avatar 列，需要修改 [UserEntity.java](file:///d:/code/enrollment_promotion_system/backend/src/main/java/com/edu/enrollment/entity/UserEntity.java)：

找到这一行：
```java
@TableField(exist = false)
private String avatar;
```

**改为：**
```java
private String avatar;
```

（删除 `@TableField(exist = false)` 注解）

---

### 🔧 解决方案 3：检查数据库中实际的用户数据

执行以下 SQL 查看当前用户：
```sql
USE enrollment_db;
SELECT id, username, real_name, role, status FROM user;
```

如果没有数据，执行 [insert_test_users.sql](insert_test_users.sql)。

---

## ✅ 完成后

1. 确保数据库中已有用户数据
2. 重启后端应用
3. 使用 `admin / 123456` 登录测试
