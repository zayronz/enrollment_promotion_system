package com.edu.enrollment.service;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.edu.enrollment.dto.RegistrationSubmitDTO;
import com.edu.enrollment.entity.ActivityEntity;
import com.edu.enrollment.entity.RegistrationEntity;
import com.edu.enrollment.entity.UserEntity;
import com.edu.enrollment.mapper.RegistrationMapper;
import com.edu.enrollment.utils.SchoolNameNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final RegistrationMapper registrationMapper;
    private final ActivityService activityService;
    private final UserService userService;
    private final SchoolNameNormalizer schoolNameNormalizer;

    @Transactional
    public Long submit(RegistrationSubmitDTO dto, Long userId) {
        // 1. 校验活动是否存在且可报名
        ActivityEntity activity = activityService.getById(dto.getActivityId());
        if (activity == null) {
            throw new RuntimeException("活动不存在");
        }
        if (activity.getStatus() != 1) {
            throw new RuntimeException("活动未发布");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activity.getRegistrationStartTime()) ||
                now.isAfter(activity.getRegistrationEndTime())) {
            throw new RuntimeException("不在报名时间内");
        }

        // 2. 检查是否已报名
        RegistrationEntity existReg = registrationMapper.findByActivityAndUser(dto.getActivityId(), userId);
        if (existReg != null && existReg.getStatus() != 4) { // 4表示已撤回
            throw new RuntimeException("您已报名过此活动");
        }

        // 3. 获取用户信息
        UserEntity user = userService.getById(userId);

        // 4. 标准化学校名称
        String normalizedSchool = schoolNameNormalizer.normalize(dto.getTargetSchool());

        // 5. 创建报名记录
        RegistrationEntity registration = new RegistrationEntity();
        registration.setActivityId(dto.getActivityId());
        registration.setUserId(userId);
        registration.setUserType("student".equals(user.getRole()) ? 0 : 1);
        registration.setTargetSchool(normalizedSchool);
        registration.setScore(dto.getScore());
        registration.setFormData(JSONUtil.toJsonStr(dto.getFormData()));
        registration.setStatus(0); // 待审核
        registration.setCurrentNode("college_audit");

        registrationMapper.insert(registration);

        // 6. 自动分组（如果活动设置开启）
        if (activity.getAutoGroup() == 1) {
            autoGroup(activity.getId(), normalizedSchool);
        }

        return registration.getId();
    }

    /**
     * 自动分组和组内排名
     */
    private void autoGroup(Long activityId, String schoolName) {
        List<RegistrationEntity> sameSchoolRegs = registrationMapper
                .findByActivityAndSchool(activityId, schoolName);

        // 按成绩/绩点降序排列
        sameSchoolRegs.sort(Comparator.comparing(RegistrationEntity::getScore,
                Comparator.nullsLast(Comparator.reverseOrder())));

        String groupName = schoolName + "招生组";
        for (int i = 0; i < sameSchoolRegs.size(); i++) {
            RegistrationEntity reg = sameSchoolRegs.get(i);
            registrationMapper.updateGroupInfo(reg.getId(), reg.getStatus(), groupName, i + 1);
        }
    }

    public List<RegistrationEntity> getMyRegistrations(Long userId) {
        LambdaQueryWrapper<RegistrationEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RegistrationEntity::getUserId, userId);
        wrapper.orderByDesc(RegistrationEntity::getCreateTime);
        return registrationMapper.selectList(wrapper);
    }

    public RegistrationEntity getDetail(Long id) {
        return registrationMapper.selectById(id);
    }

    @Transactional
    public void withdraw(Long id, Long userId) {
        RegistrationEntity registration = registrationMapper.selectById(id);
        if (registration == null) {
            throw new RuntimeException("报名记录不存在");
        }
        if (!registration.getUserId().equals(userId)) {
            throw new RuntimeException("只能撤回自己的报名");
        }
        if (registration.getStatus() != 0) {
            throw new RuntimeException("当前状态无法撤回");
        }
        registration.setStatus(4); // 已撤回
        registrationMapper.updateById(registration);
    }

    public List<RegistrationEntity> getPendingAudit(Long auditorId, String node) {
        UserEntity auditor = userService.getById(auditorId);

        LambdaQueryWrapper<RegistrationEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RegistrationEntity::getStatus, 0)
                .eq(RegistrationEntity::getCurrentNode, node);

        // 学院审核员只能看到本学院用户的报名
        if ("COLLEGE".equals(auditor.getRole())) {
            // 查询本学院所有用户的报名记录
            List<UserEntity> collegeUsers = userService.getByCollegeId(auditor.getCollegeId());
            if (collegeUsers.isEmpty()) {
                return List.of();
            }
            List<Long> collegeUserIds = collegeUsers.stream()
                    .map(UserEntity::getId)
                    .collect(Collectors.toList());
            wrapper.in(RegistrationEntity::getUserId, collegeUserIds);
        }

        wrapper.orderByDesc(RegistrationEntity::getCreateTime);
        return registrationMapper.selectList(wrapper);
    }
}