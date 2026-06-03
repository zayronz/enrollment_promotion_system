# 数据库检查清单

## 📋 快速诊断流程

### 第一步：检查 MySQL 服务是否运行

**Windows:**
```cmd
# 检查 MySQL 服务状态
sc query MySQL80
# 或
netstat -an | findstr "3306"
```

**Mac/Linux:**
```bash
ps aux | grep mysql
netstat -an | grep 3306
```

### 第二步：测试数据库连接

使用任意 MySQL 客户端工具（MySQL Workbench、Navicat、DBeaver 等）连接：
- Host: `localhost`
- Port: `3306`
- Username: `root`
- Password: `123456`

### 第三步：执行完整的检查脚本

执行 [check_and_init_database.sql](check_and_init_database.sql) 文件：
1. 在 MySQL 客户端中打开此文件
2. 点击执行（Run/Execute）
3. 查看结果，确认所有表都已创建

---

## 🔍 数据库配置检查

### 后端配置文件
[application.yml](file:///d:/code/enrollment_promotion_system/backend/src/main/resources/application.yml)
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/enrollment_db?...
    username: root
    password: 123456
```

**常见问题：**
- ❌ MySQL 密码不是 `123456` → 修改配置文件中的密码
- ❌ 端口不是 `3306` → 修改配置文件中的端口
- ❌ MySQL 服务未启动 → 启动 MySQL 服务

---

## 🛠️ 常见问题解决

### 问题 1：Access denied for user 'root'@'localhost'
**解决方案：**
1. 检查 MySQL root 密码是否正确
2. 如果忘记密码，重置 MySQL root 密码
3. 修改 application.yml 中的密码配置

### 问题 2：Unknown database 'enrollment_db'
**解决方案：**
执行 [check_and_init_database.sql](check_and_init_database.sql) 脚本，会自动创建数据库

### 问题 3：Unknown column 'xxx' in 'field list'
**解决方案：**
1. 执行 [check_and_init_database.sql](check_and_init_database.sql)
2. 该脚本会检查并添加缺失的列

### 问题 4：Communications link failure
**解决方案：**
1. 确认 MySQL 服务正在运行
2. 检查端口 3306 是否被占用
3. 检查防火墙设置

---

## ✅ 确认检查清单

- [ ] MySQL 服务正在运行
- [ ] 可以使用 root/123456 连接到 MySQL
- [ ] enrollment_db 数据库存在
- [ ] 所有表都已创建（user, college, activity, registration 等）
- [ ] user 表包含 avatar 列
- [ ] 后端应用可以成功启动
- [ ] 前端可以正常调用后端 API

---

## 📞 需要帮助？

如果以上步骤仍无法解决问题，请提供：
1. 后端启动日志（完整的错误堆栈）
2. 浏览器控制台的错误信息
3. 浏览器 Network 面板的请求详情
