package com.edu.enrollment.service;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edu.enrollment.dto.RegistrationSubmitDTO;
import com.edu.enrollment.entity.ActivityEntity;
import com.edu.enrollment.entity.RegistrationEntity;
import com.edu.enrollment.entity.UserEntity;
import com.edu.enrollment.exception.BusinessException;
import com.edu.enrollment.mapper.RegistrationMapper;
import com.edu.enrollment.utils.SchoolNameNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final RegistrationMapper registrationMapper;
    private final ActivityService activityService;
    private final UserService userService;
    private final SchoolNameNormalizer schoolNameNormalizer;

    private final ConcurrentHashMap<Long, Object> submitLocks = new ConcurrentHashMap<>();

    @Transactional
    public Long submit(RegistrationSubmitDTO dto, Long userId) {
        Object lock = submitLocks.computeIfAbsent(userId, k -> new Object());
        synchronized (lock) {
            try {
                return doSubmit(dto, userId);
            } finally {
                submitLocks.remove(userId);
            }
        }
    }

    private Long doSubmit(RegistrationSubmitDTO dto, Long userId) {
        ActivityEntity activity = activityService.getById(dto.getActivityId());
        if (activity == null) {
            throw new BusinessException("活动不存在");
        }
        if (activity.getStatus() != 1) {
            throw new BusinessException("活动未发布");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activity.getRegistrationStartTime()) ||
                now.isAfter(activity.getRegistrationEndTime())) {
            throw new BusinessException("不在报名时间内");
        }

        RegistrationEntity existReg = registrationMapper.findByActivityAndUser(dto.getActivityId(), userId);
        if (existReg != null && existReg.getStatus() != 4) {
            throw new BusinessException("您已报名过此活动");
        }

        UserEntity user = userService.getById(userId);

        String normalizedSchool = schoolNameNormalizer.normalize(dto.getTargetSchool());

        RegistrationEntity registration = new RegistrationEntity();
        registration.setActivityId(dto.getActivityId());
        registration.setUserId(userId);
        registration.setUserType("student".equals(user.getRole()) ? 0 : 1);
        registration.setTargetSchool(normalizedSchool);
        registration.setScore(dto.getScore());
        registration.setFormData(JSONUtil.toJsonStr(dto.getFormData()));
        registration.setStatus(0);
        registration.setCurrentNode("college_audit");

        registrationMapper.insert(registration);

        if (activity.getAutoGroup() == 1) {
            autoGroup(activity.getId(), normalizedSchool);
        }

        return registration.getId();
    }

    private void autoGroup(Long activityId, String schoolName) {
        List<RegistrationEntity> sameSchoolRegs = registrationMapper
                .findByActivityAndSchool(activityId, schoolName);

        sameSchoolRegs.sort(Comparator.comparing(RegistrationEntity::getScore,
                Comparator.nullsLast(Comparator.reverseOrder())));

        String groupName = schoolName + "招生组";
        for (int i = 0; i < sameSchoolRegs.size(); i++) {
            RegistrationEntity reg = sameSchoolRegs.get(i);
            registrationMapper.updateGroupInfo(reg.getId(), groupName, i + 1);
        }
    }

    public Map<String, Object> getMyRegistrations(Long userId, Integer page, Integer size) {
        LambdaQueryWrapper<RegistrationEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RegistrationEntity::getUserId, userId);
        wrapper.orderByDesc(RegistrationEntity::getCreateTime);

        Page<RegistrationEntity> registrationPage = new Page<>(page, size);
        registrationPage = registrationMapper.selectPage(registrationPage, wrapper);

        List<Map<String, Object>> enrichedRecords = new ArrayList<>();
        for (RegistrationEntity reg : registrationPage.getRecords()) {
            Map<String, Object> map = new HashMap<>();
            ActivityEntity activity = activityService.getById(reg.getActivityId());
            map.put("id", reg.getId());
            map.put("activityId", reg.getActivityId());
            map.put("activityTitle", activity != null ? activity.getName() : "-");
            map.put("targetSchool", reg.getTargetSchool());
            map.put("score", reg.getScore());
            map.put("status", reg.getStatus());
            map.put("currentNode", reg.getCurrentNode());
            map.put("createTime", reg.getCreateTime());
            map.put("groupName", reg.getGroupName());
            map.put("groupRank", reg.getGroupRank());
            map.put("rejectReason", reg.getRejectReason());
            enrichedRecords.add(map);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("records", enrichedRecords);
        result.put("total", registrationPage.getTotal());
        result.put("current", page);
        result.put("size", size);
        return result;
    }

    public RegistrationEntity getDetail(Long id) {
        return registrationMapper.selectById(id);
    }

    @Transactional
    public void withdraw(Long id, Long userId) {
        RegistrationEntity registration = registrationMapper.selectById(id);
        if (registration == null) {
            throw new BusinessException("报名记录不存在");
        }
        if (!registration.getUserId().equals(userId)) {
            throw new BusinessException("只能撤回自己的报名");
        }
        if (registration.getStatus() != 0) {
            throw new BusinessException("当前状态无法撤回");
        }
        registration.setStatus(4);
        registrationMapper.updateById(registration);
    }

    public Map<String, Object> getPendingAudit(Long auditorId, String node,
                                                Integer page, Integer size,
                                                String keyword, Long activityId,
                                                Long collegeId) {
        UserEntity auditor = userService.getById(auditorId);

        LambdaQueryWrapper<RegistrationEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RegistrationEntity::getStatus, 0)
                .eq(RegistrationEntity::getCurrentNode, node);

        if ("COLLEGE".equals(auditor.getRole())) {
            List<UserEntity> collegeUsers = userService.getByCollegeId(auditor.getCollegeId());
            if (collegeUsers.isEmpty()) {
                return buildPageResult(new Page<>(page, size));
            }
            List<Long> collegeUserIds = new ArrayList<>();
            for (UserEntity user : collegeUsers) {
                collegeUserIds.add(user.getId());
            }
            wrapper.in(RegistrationEntity::getUserId, collegeUserIds);
        }

        if (activityId != null) {
            wrapper.eq(RegistrationEntity::getActivityId, activityId);
        }

        wrapper.orderByDesc(RegistrationEntity::getCreateTime);

        Page<RegistrationEntity> registrationPage = new Page<>(page, size);
        registrationPage = registrationMapper.selectPage(registrationPage, wrapper);

        List<Map<String, Object>> enrichedRecords = new ArrayList<>();
        for (RegistrationEntity reg : registrationPage.getRecords()) {
            Map<String, Object> map = new HashMap<>();
            UserEntity user = userService.getById(reg.getUserId());
            ActivityEntity activity = activityService.getById(reg.getActivityId());

            String collegeName = "-";
            if (user != null && user.getCollegeId() != null) {
                UserEntity collegeUser = userService.getById(user.getCollegeId());
                if (collegeUser != null) {
                    collegeName = collegeUser.getRealName();
                }
            }

            if (user != null && user.getCollegeId() != null) {
                try {
                    map.put("collegeId", user.getCollegeId());
                } catch (Exception ignored) {}
            }

            map.put("id", reg.getId());
            map.put("activityId", reg.getActivityId());
            map.put("activityTitle", activity != null ? activity.getName() : "-");
            map.put("realName", user != null ? user.getRealName() : "-");
            map.put("userType", user != null ? ("student".equals(user.getRole()) ? "STUDENT" : "TEACHER") : "-");
            map.put("targetSchool", reg.getTargetSchool());
            map.put("score", reg.getScore());
            map.put("createTime", reg.getCreateTime());
            map.put("collegeId", user != null ? user.getCollegeId() : null);
            map.put("status", reg.getStatus());
            map.put("userRole", user != null ? user.getRole() : null);
            map.put("collegeName", collegeName);
            enrichedRecords.add(map);
        }

        List<Map<String, Object>> filtered = enrichedRecords;
        if (keyword != null && !keyword.isEmpty()) {
            String kw = keyword.toLowerCase();
            filtered = new ArrayList<>();
            for (Map<String, Object> m : enrichedRecords) {
                String name = String.valueOf(m.getOrDefault("realName", ""));
                String school = String.valueOf(m.getOrDefault("targetSchool", ""));
                if (name.toLowerCase().contains(kw) || school.toLowerCase().contains(kw)) {
                    filtered.add(m);
                }
            }
        }

        if (collegeId != null) {
            List<Map<String, Object>> temp = new ArrayList<>();
            for (Map<String, Object> m : filtered) {
                if (collegeId.equals(m.get("collegeId"))) {
                    temp.add(m);
                }
            }
            filtered = temp;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("records", filtered);
        result.put("total", (long) filtered.size());
        result.put("current", page);
        result.put("size", size);
        return result;
    }

    private Map<String, Object> buildPageResult(Page<RegistrationEntity> page) {
        Map<String, Object> result = new HashMap<>();
        result.put("records", new ArrayList<>());
        result.put("total", 0L);
        result.put("current", page.getCurrent());
        result.put("size", page.getSize());
        return result;
    }
}
