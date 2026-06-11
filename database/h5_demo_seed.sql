-- H5/PC 共用演示数据
-- 使用方式：先执行 schema.sql 并启动后端生成测试账号，再执行本脚本。
-- 账号来自 DataInitializer：admin / student / teacher / college，密码均为 123456。

INSERT INTO college (id, name, code)
SELECT 1, '信息学院', 'CS'
WHERE NOT EXISTS (SELECT 1 FROM college WHERE id = 1);

SET @admin_id = (SELECT id FROM user WHERE username = 'admin' LIMIT 1);
SET @student_id = (SELECT id FROM user WHERE username = 'student' LIMIT 1);
SET @teacher_id = (SELECT id FROM user WHERE username = 'teacher' LIMIT 1);
SET @college_id = (SELECT id FROM user WHERE username = 'college' LIMIT 1);

-- 首页与活动详情：母校行、招宣活动
INSERT INTO activity (
    name, description, type, location,
    activity_start_time, activity_end_time,
    registration_start_time, registration_end_time,
    banner_url, cover_image, video_url,
    status, audit_flow, custom_fields,
    max_student_per_school, max_teacher_per_school,
    auto_group, creator_id, show_on_home
)
SELECT
    '2026年寒假母校行活动',
    '<p>面向优秀学生招募母校行志愿者，返回高中母校开展招生宣传、经验分享和政策宣讲。</p><p>请按要求完成报名，并在活动结束后提交活动总结。</p>',
    1,
    '各生源高中',
    DATE_ADD(NOW(), INTERVAL 10 DAY),
    DATE_ADD(NOW(), INTERVAL 20 DAY),
    DATE_SUB(NOW(), INTERVAL 3 DAY),
    DATE_ADD(NOW(), INTERVAL 8 DAY),
    '/images/banner/banner1.jpg',
    '/images/banner/banner1.jpg',
    '',
    1,
    '["college_audit","school_audit"]',
    '[{"type":"input","label":"高考分数","name":"score","required":true},{"type":"input","label":"意向专业","name":"major","required":false},{"type":"feedback_rule","name":"__feedback_rule__","label":"反馈规则","feedbackDeadline":"2026-12-31T23:59:59"}]',
    5,
    2,
    1,
    @admin_id,
    1
WHERE NOT EXISTS (SELECT 1 FROM activity WHERE name = '2026年寒假母校行活动');

INSERT INTO activity (
    name, description, type, location,
    activity_start_time, activity_end_time,
    registration_start_time, registration_end_time,
    banner_url, cover_image, video_url,
    status, audit_flow, custom_fields,
    max_student_per_school, max_teacher_per_school,
    auto_group, creator_id, show_on_home
)
SELECT
    '2026年春季招宣活动',
    '<p>组织学生和教师赴重点中学开展招生宣传，介绍学校办学特色、专业优势和报考政策。</p>',
    0,
    '线上宣讲',
    DATE_ADD(NOW(), INTERVAL 15 DAY),
    DATE_ADD(NOW(), INTERVAL 18 DAY),
    DATE_SUB(NOW(), INTERVAL 2 DAY),
    DATE_ADD(NOW(), INTERVAL 12 DAY),
    '/images/banner/banner2.jpg',
    '/images/banner/banner2.jpg',
    '',
    1,
    '["college_audit","school_audit"]',
    '[{"type":"input","label":"目标学校联系人","name":"contact","required":false},{"type":"eligibility_rule","name":"__eligibility_rule__","label":"资格条件","minGpa":3.0},{"type":"audience_rule","name":"__audience_rule__","label":"参与人群","allowedCollegeIds":[1],"allowedUsernames":["teacher","测试教师"]},{"type":"feedback_rule","name":"__feedback_rule__","label":"反馈规则","feedbackDeadline":"2026-12-31T23:59:59"}]',
    5,
    2,
    1,
    @admin_id,
    1
WHERE NOT EXISTS (SELECT 1 FROM activity WHERE name = '2026年春季招宣活动');

SET @home_activity_id = (SELECT id FROM activity WHERE name = '2026年寒假母校行活动' LIMIT 1);
SET @promo_activity_id = (SELECT id FROM activity WHERE name = '2026年春季招宣活动' LIMIT 1);

-- 我的报名、我的组员、报名审批进度共用报名记录
INSERT INTO registration (
    activity_id, user_id, user_type, target_school, score, form_data,
    status, current_node, reject_reason, group_name, group_rank
)
SELECT
    @home_activity_id,
    @student_id,
    0,
    '太原理工大学附属中学',
    88,
    JSON_OBJECT(
        'basicInfo', JSON_OBJECT('realName', '测试学生', 'username', 'student', 'collegeName', '信息学院', 'phone', '13800000001', 'email', 'student@example.com'),
        'targetSchool', '太原理工大学附属中学',
        'score', 88,
        'major', '计算机科学与技术'
    ),
    2,
    'completed',
    NULL,
    '太原理工大学附属中学招生组',
    1
WHERE NOT EXISTS (
    SELECT 1 FROM registration WHERE activity_id = @home_activity_id AND user_id = @student_id
);

INSERT INTO registration (
    activity_id, user_id, user_type, target_school, score, form_data,
    status, current_node, reject_reason, group_name, group_rank
)
SELECT
    @home_activity_id,
    @teacher_id,
    1,
    '太原理工大学附属中学',
    95,
    JSON_OBJECT(
        'basicInfo', JSON_OBJECT('realName', '测试教师', 'username', 'teacher', 'collegeName', '信息学院', 'phone', '13800000002', 'email', 'teacher@example.com'),
        'targetSchool', '太原理工大学附属中学'
    ),
    2,
    'completed',
    NULL,
    '太原理工大学附属中学招生组',
    2
WHERE NOT EXISTS (
    SELECT 1 FROM registration WHERE activity_id = @home_activity_id AND user_id = @teacher_id
);

-- 待审核演示数据
INSERT INTO registration (
    activity_id, user_id, user_type, target_school, score, form_data,
    status, current_node, reject_reason, group_name, group_rank
)
SELECT
    @promo_activity_id,
    @student_id,
    0,
    '太原理工大学',
    86,
    JSON_OBJECT(
        'basicInfo', JSON_OBJECT('realName', '测试学生', 'username', 'student', 'collegeName', '信息学院'),
        'targetSchool', '太原理工大学',
        'contact', '高中教务处'
    ),
    0,
    'college_audit',
    NULL,
    NULL,
    NULL
WHERE NOT EXISTS (
    SELECT 1 FROM registration WHERE activity_id = @promo_activity_id AND user_id = @student_id
);

-- 最近活动总结
INSERT INTO feedback (activity_id, user_id, user_role, title, content, attachment_urls, type)
SELECT
    @home_activity_id,
    @student_id,
    'STUDENT',
    '母校行宣讲总结',
    '<p>本次母校行面向高三学生介绍了学校专业特色、招生政策和校园生活，现场互动积极，收集到多名学生的咨询问题。</p>',
    '[]',
    1
WHERE NOT EXISTS (
    SELECT 1 FROM feedback WHERE activity_id = @home_activity_id AND title = '母校行宣讲总结'
);

-- 招宣资料：H5 与 PC 资料页共用
INSERT INTO material (name, category, description, file_name, file_path, file_size, file_type, uploader_id)
SELECT '2026本科招生简章', '招生简章', '学校本科招生政策、专业目录和报考说明。', '2026本科招生简章.pdf', '/uploads/material/2026本科招生简章.pdf', 2048000, 'application/pdf', @admin_id
WHERE NOT EXISTS (SELECT 1 FROM material WHERE name = '2026本科招生简章');

INSERT INTO material (name, category, description, file_name, file_path, file_size, file_type, uploader_id)
SELECT '学校宣传PPT', '宣讲课件', '用于高中宣讲的学校介绍课件。', '学校宣传PPT.pptx', '/uploads/material/学校宣传PPT.pptx', 5120000, 'application/vnd.openxmlformats-officedocument.presentationml.presentation', @admin_id
WHERE NOT EXISTS (SELECT 1 FROM material WHERE name = '学校宣传PPT');

INSERT INTO material (name, category, description, file_name, file_path, file_size, file_type, uploader_id)
SELECT '招生政策问答手册', '政策问答', '常见报考问题、转专业政策和奖助学金说明。', '招生政策问答手册.docx', '/uploads/material/招生政策问答手册.docx', 1048576, 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', @admin_id
WHERE NOT EXISTS (SELECT 1 FROM material WHERE name = '招生政策问答手册');
