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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final RegistrationMapper registrationMapper;
    private final ActivityService activityService;
    private final UserService userService;
    private final SchoolNameNormalizer schoolNameNormalizer;

    /** 报名提交锁，防止同一用户并发提交 */
    private final ConcurrentHashMap<Long, Object> submitLocks = new ConcurrentHashMap<>();

    @Transactional
    public Long submit(RegistrationSubmitDTO dto, Long userId) {
        // 并发控制：同一用户同一时间只能提交一次
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
        // 1. 校验活动是否存在且可报名
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

        // 2. 检查是否已报名（只查询最新的报名记录）
        RegistrationEntity existReg = registrationMapper.findByActivityAndUser(dto.getActivityId(), userId);
        if (existReg != null) {
            // 状态说明：0=待审核, 1=学院通过, 2=全部通过, 3=已拒绝, 4=已撤回
            // 只有被拒绝(3)或已撤回(4)的情况下才允许重新报名
            if (existReg.getStatus() != 3 && existReg.getStatus() != 4) {
                String statusMsg = existReg.getStatus() == 0 ? "审核中" : 
                                   existReg.getStatus() == 1 ? "学院审核通过" : 
                                   existReg.getStatus() == 2 ? "报名成功" : "已报名";
                throw new BusinessException("您已报名过此活动，当前状态：" + statusMsg);
            }
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
            registrationMapper.updateGroupInfo(reg.getId(), groupName, i + 1);
        }
    }

    public Map<String, Object> getMyRegistrations(Long userId, Integer page, Integer size) {
        LambdaQueryWrapper<RegistrationEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RegistrationEntity::getUserId, userId);
        wrapper.orderByDesc(RegistrationEntity::getCreateTime);

        Page<RegistrationEntity> registrationPage = new Page<>(page, size);
        registrationPage = registrationMapper.selectPage(registrationPage, wrapper);

        // 附加活动名称
        List<Map<String, Object>> enrichedRecords = registrationPage.getRecords().stream()
                .map(reg -> {
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
                    return map;
                })
                .collect(Collectors.toList());

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

    public Map<String, Object> getRegistrationStatus(Long activityId, Long userId) {
        ActivityEntity activity = activityService.getById(activityId);
        if (activity == null) {
            throw new BusinessException("活动不存在");
        }

        RegistrationEntity registration = registrationMapper.findByActivityAndUser(activityId, userId);
        boolean registered = registration != null && registration.getStatus() != 4;
        LocalDateTime now = LocalDateTime.now();
        boolean inTime = activity.getRegistrationStartTime() != null
                && activity.getRegistrationEndTime() != null
                && !now.isBefore(activity.getRegistrationStartTime())
                && !now.isAfter(activity.getRegistrationEndTime());
        boolean published = activity.getStatus() == 1;
        boolean canRegister = published && inTime && !registered;

        String message = "可以报名";
        if (registered) {
            message = "您已报名过该活动";
        } else if (!published) {
            message = "活动未发布";
        } else if (!inTime) {
            message = "当前不在报名时间内";
        }

        Map<String, Object> result = new HashMap<>();
        result.put("registered", registered);
        result.put("canRegister", canRegister);
        result.put("registrationId", registered ? registration.getId() : null);
        result.put("status", registered ? registration.getStatus() : null);
        result.put("message", message);
        return result;
    }

    public List<String> suggestSchools(Long activityId, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }

        String kw = keyword.trim();
        Set<String> suggestions = new LinkedHashSet<>();
        List<RegistrationEntity> registrations = registrationMapper.selectList(
                new LambdaQueryWrapper<RegistrationEntity>()
                        .eq(activityId != null, RegistrationEntity::getActivityId, activityId)
                        .like(RegistrationEntity::getTargetSchool, kw)
                        .orderByDesc(RegistrationEntity::getCreateTime)
        );
        registrations.stream()
                .map(RegistrationEntity::getTargetSchool)
                .filter(name -> name != null && !name.isBlank())
                .limit(8)
                .forEach(suggestions::add);

        if (suggestions.size() < 8) {
            suggestions.addAll(schoolNameNormalizer.suggest(kw, 8 - suggestions.size()));
        }
        return new ArrayList<>(suggestions).stream().limit(8).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getMyTeams(Long userId) {
        List<RegistrationEntity> myRegistrations = registrationMapper.selectList(
                new LambdaQueryWrapper<RegistrationEntity>()
                        .eq(RegistrationEntity::getUserId, userId)
                        .isNotNull(RegistrationEntity::getGroupName)
                        .ne(RegistrationEntity::getStatus, 4)
                        .orderByDesc(RegistrationEntity::getCreateTime)
        );

        return myRegistrations.stream()
                .map(reg -> {
                    Map<String, Object> map = new HashMap<>();
                    ActivityEntity activity = activityService.getById(reg.getActivityId());
                    List<RegistrationEntity> members = registrationMapper.selectList(
                            new LambdaQueryWrapper<RegistrationEntity>()
                                    .eq(RegistrationEntity::getActivityId, reg.getActivityId())
                                    .eq(RegistrationEntity::getGroupName, reg.getGroupName())
                                    .ne(RegistrationEntity::getStatus, 4)
                                    .orderByAsc(RegistrationEntity::getGroupRank)
                    );
                    map.put("registrationId", reg.getId());
                    map.put("activityId", reg.getActivityId());
                    map.put("activityTitle", activity != null ? activity.getName() : "-");
                    map.put("groupName", reg.getGroupName());
                    map.put("groupRank", reg.getGroupRank());
                    map.put("targetSchool", reg.getTargetSchool());
                    map.put("memberCount", members.size());
                    map.put("members", members.stream().map(this::buildTeamMemberInfo).collect(Collectors.toList()));
                    return map;
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getAvailableTeams(Long userId) {
        List<RegistrationEntity> myRegistrations = registrationMapper.selectList(
                new LambdaQueryWrapper<RegistrationEntity>()
                        .eq(RegistrationEntity::getUserId, userId)
                        .ne(RegistrationEntity::getStatus, 4)
        );
        Set<String> myTeamKeys = myRegistrations.stream()
                .filter(reg -> reg.getGroupName() != null)
                .map(reg -> reg.getActivityId() + "::" + reg.getGroupName())
                .collect(Collectors.toSet());

        List<RegistrationEntity> groupedRegistrations = registrationMapper.selectList(
                new LambdaQueryWrapper<RegistrationEntity>()
                        .isNotNull(RegistrationEntity::getGroupName)
                        .ne(RegistrationEntity::getStatus, 4)
                        .orderByDesc(RegistrationEntity::getCreateTime)
        );

        Map<String, List<RegistrationEntity>> grouped = groupedRegistrations.stream()
                .collect(Collectors.groupingBy(reg -> reg.getActivityId() + "::" + reg.getGroupName()));

        return grouped.entrySet().stream()
                .filter(entry -> !myTeamKeys.contains(entry.getKey()))
                .map(entry -> {
                    RegistrationEntity first = entry.getValue().get(0);
                    ActivityEntity activity = activityService.getById(first.getActivityId());
                    Map<String, Object> map = new HashMap<>();
                    map.put("activityId", first.getActivityId());
                    map.put("activityTitle", activity != null ? activity.getName() : "-");
                    map.put("groupName", first.getGroupName());
                    map.put("targetSchool", first.getTargetSchool());
                    map.put("memberCount", entry.getValue().size());
                    return map;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void exitTeam(Long id, Long userId) {
        RegistrationEntity registration = registrationMapper.selectById(id);
        if (registration == null) {
            throw new BusinessException("报名记录不存在");
        }
        if (!registration.getUserId().equals(userId)) {
            throw new BusinessException("只能退出自己的招宣组");
        }
        registration.setGroupName(null);
        registration.setGroupRank(null);
        registrationMapper.updateById(registration);
    }

    @Transactional
    public void joinTeam(Long id, String groupName, Long userId) {
        if (groupName == null || groupName.trim().isEmpty()) {
            throw new BusinessException("请选择要加入的招宣组");
        }
        RegistrationEntity registration = registrationMapper.selectById(id);
        if (registration == null) {
            throw new BusinessException("报名记录不存在");
        }
        if (!registration.getUserId().equals(userId)) {
            throw new BusinessException("只能修改自己的招宣组");
        }

        registration.setGroupName(groupName.trim());
        Integer maxRank = registrationMapper.selectList(
                        new LambdaQueryWrapper<RegistrationEntity>()
                                .eq(RegistrationEntity::getActivityId, registration.getActivityId())
                                .eq(RegistrationEntity::getGroupName, groupName.trim())
                                .isNotNull(RegistrationEntity::getGroupRank)
                ).stream()
                .map(RegistrationEntity::getGroupRank)
                .filter(rank -> rank != null)
                .max(Integer::compareTo)
                .orElse(0);
        registration.setGroupRank(maxRank + 1);
        registrationMapper.updateById(registration);
    }

    private Map<String, Object> buildTeamMemberInfo(RegistrationEntity registration) {
        UserEntity user = userService.getById(registration.getUserId());
        Map<String, Object> map = new HashMap<>();
        map.put("registrationId", registration.getId());
        map.put("userId", registration.getUserId());
        map.put("realName", user != null ? user.getRealName() : "-");
        map.put("role", user != null ? user.getRole() : "-");
        map.put("targetSchool", registration.getTargetSchool());
        map.put("groupRank", registration.getGroupRank());
        map.put("phone", user != null ? user.getPhone() : null);
        map.put("email", user != null ? user.getEmail() : null);
        return map;
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
        registration.setStatus(4); // 已撤回
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

        // 学院审核员只能看到本学院用户的报名
        if ("COLLEGE".equals(auditor.getRole())) {
            List<UserEntity> collegeUsers = userService.getByCollegeId(auditor.getCollegeId());
            if (collegeUsers.isEmpty()) {
                return buildPageResult(new Page<>(page, size));
            }
            List<Long> collegeUserIds = collegeUsers.stream()
                    .map(UserEntity::getId)
                    .collect(Collectors.toList());
            wrapper.in(RegistrationEntity::getUserId, collegeUserIds);
        }

        // 活动筛选
        if (activityId != null) {
            wrapper.eq(RegistrationEntity::getActivityId, activityId);
        }

        wrapper.orderByDesc(RegistrationEntity::getCreateTime);

        // 分页查询
        Page<RegistrationEntity> registrationPage = new Page<>(page, size);
        registrationPage = registrationMapper.selectPage(registrationPage, wrapper);

        // 组装返回结果，附加用户和活动信息
        List<Map<String, Object>> enrichedRecords = registrationPage.getRecords().stream()
                .map(reg -> {
                    Map<String, Object> map = new java.util.HashMap<>();
                    UserEntity user = userService.getById(reg.getUserId());
                    ActivityEntity activity = activityService.getById(reg.getActivityId());
                    // 通过user的collegeId获取学院信息
                    String collegeName = "-";
                    if (user != null && user.getCollegeId() != null) {
                        UserEntity collegeUser = userService.getById(user.getCollegeId());
                        if (collegeUser != null) {
                            collegeName = collegeUser.getRealName();
                        }
                    }
                    // 尝试通过collegeId查找学院 - 如果collegeId是学院用户ID
                    if (user != null && user.getCollegeId() != null) {
                        try {
                            Long realCollegeId = user.getCollegeId();
                            // 查找该学院ID对应的学院管理员或直接使用collegeId
                            map.put("collegeId", realCollegeId);
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
                    return map;
                })
                .collect(Collectors.toList());

        // 关键词过滤（后端过滤）
        List<Map<String, Object>> filtered = enrichedRecords;
        if (keyword != null && !keyword.isEmpty()) {
            String kw = keyword.toLowerCase();
            filtered = enrichedRecords.stream()
                    .filter(m -> {
                        String name = (String) m.getOrDefault("realName", "");
                        String school = (String) m.getOrDefault("targetSchool", "");
                        return name.toLowerCase().contains(kw) || school.toLowerCase().contains(kw);
                    })
                    .collect(Collectors.toList());
        }

        // 学院筛选
        if (collegeId != null) {
            filtered = filtered.stream()
                    .filter(m -> collegeId.equals(m.get("collegeId")))
                    .collect(Collectors.toList());
        }

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("records", filtered);
        result.put("total", (long) filtered.size());
        result.put("current", page);
        result.put("size", size);
        return result;
    }

    private Map<String, Object> buildPageResult(Page<RegistrationEntity> page) {
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("records", List.of());
        result.put("total", 0L);
        result.put("current", page.getCurrent());
        result.put("size", page.getSize());
        return result;
    }
}
