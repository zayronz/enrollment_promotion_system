-- =========================================
-- 清理重复用户数据
-- =========================================

USE enrollment_db;

-- 查看当前所有用户
SELECT id, username, real_name, role, create_time FROM user ORDER BY username, id;

-- 删除重复的用户（保留 ID 最小的）
-- 先查看要删除的记录
SELECT u1.* FROM user u1
INNER JOIN (
    SELECT username, MIN(id) as min_id
    FROM user
    GROUP BY username
    HAVING COUNT(*) > 1
) u2 ON u1.username = u2.username AND u1.id > u2.min_id;

-- 执行删除（如果上面的查询有结果，再执行）
DELETE u1 FROM user u1
INNER JOIN (
    SELECT username, MIN(id) as min_id
    FROM user
    GROUP BY username
    HAVING COUNT(*) > 1
) u2 ON u1.username = u2.username AND u1.id > u2.min_id;

-- 确保 username 字段唯一（添加唯一约束）
-- 先删除可能的重复数据后再执行
-- ALTER TABLE user ADD UNIQUE KEY uk_username (username);

-- 查看清理后的结果
SELECT '清理完成！' AS status;
SELECT id, username, real_name, role FROM user ORDER BY id;
