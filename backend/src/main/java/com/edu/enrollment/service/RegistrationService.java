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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public List<Map<String, Object>> getMyTeams(Long userId) {
        LambdaQueryWrapper<RegistrationEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RegistrationEntity::getUserId, userId)
                .isNotNull(RegistrationEntity::getGroupName)
                .ne(RegistrationEntity::getGroupName, "")
                .orderByDesc(RegistrationEntity::getCreateTime);

        List<RegistrationEntity> myGroupedRegistrations = registrationMapper.selectList(wrapper);

        return myGroupedRegistrations.stream()
                .map(reg -> {
                    ActivityEntity activity = activityService.getById(reg.getActivityId());

                    LambdaQueryWrapper<RegistrationEntity> memberWrapper = new LambdaQueryWrapper<>();
                    memberWrapper.eq(RegistrationEntity::getActivityId, reg.getActivityId())
                            .eq(RegistrationEntity::getGroupName, reg.getGroupName())
                            .orderByAsc(RegistrationEntity::getGroupRank)
                            .orderByAsc(RegistrationEntity::getCreateTime);
                    List<RegistrationEntity> memberRegistrations = registrationMapper.selectList(memberWrapper);

                    List<Map<String, Object>> members = memberRegistrations.stream()
                            .map(memberReg -> {
                                UserEntity user = userService.getById(memberReg.getUserId());
                                Map<String, Object> member = new HashMap<>();
                                member.put("registrationId", memberReg.getId());
                                member.put("userId", memberReg.getUserId());
                                member.put("realName", user != null ? user.getRealName() : "-");
                                member.put("role", user != null ? user.getRole() : "-");
                                member.put("unit", memberReg.getTargetSchool() != null ? memberReg.getTargetSchool() : "-");
                                member.put("phone", user != null && user.getPhone() != null ? user.getPhone() : "-");
                                member.put("email", user != null && user.getEmail() != null ? user.getEmail() : "-");
                                member.put("groupRank", memberReg.getGroupRank());
                                return member;
                            })
                            .collect(Collectors.toList());

                    String leaderName = members.isEmpty() ? "-" : (String) members.get(0).get("realName");
                    String deputyName = members.size() > 1 ? (String) members.get(1).get("realName") : "-";

                    Map<String, Object> team = new HashMap<>();
                    team.put("registrationId", reg.getId());
                    team.put("activityId", reg.getActivityId());
                    team.put("activityTitle", activity != null ? activity.getName() : "-");
                    team.put("groupName", reg.getGroupName());
                    team.put("groupRank", reg.getGroupRank());
                    team.put("targetSchool", reg.getTargetSchool());
                    team.put("leader", leaderName);
                    team.put("deputyLeader", deputyName);
                    team.put("contact", leaderName);
                    team.put("members", members);
                    team.put("createTime", reg.getCreateTime());
                    return team;
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getAvailableTeams(Long userId) {
        LambdaQueryWrapper<RegistrationEntity> myWrapper = new LambdaQueryWrapper<>();
        myWrapper.eq(RegistrationEntity::getUserId, userId)
                .ne(RegistrationEntity::getStatus, 4)
                .orderByDesc(RegistrationEntity::getCreateTime);
        List<RegistrationEntity> myRegistrations = registrationMapper.selectList(myWrapper);

        return myRegistrations.stream()
                .flatMap(myReg -> {
                    LambdaQueryWrapper<RegistrationEntity> groupWrapper = new LambdaQueryWrapper<>();
                    groupWrapper.eq(RegistrationEntity::getActivityId, myReg.getActivityId())
                            .isNotNull(RegistrationEntity::getGroupName)
                            .ne(RegistrationEntity::getGroupName, "")
                            .orderByAsc(RegistrationEntity::getGroupName);

                    List<String> groupNames = registrationMapper.selectList(groupWrapper).stream()
                            .map(RegistrationEntity::getGroupName)
                            .filter(name -> name != null && !name.isEmpty())
                            .distinct()
                            .filter(name -> !name.equals(myReg.getGroupName()))
                            .collect(Collectors.toList());

                    return groupNames.stream().map(groupName -> buildTeamMap(myReg, groupName, true));
                })
                .collect(Collectors.toList());
    }

    private Map<String, Object> buildTeamMap(RegistrationEntity referenceRegistration, String groupName, boolean includeJoinInfo) {
        ActivityEntity activity = activityService.getById(referenceRegistration.getActivityId());

        LambdaQueryWrapper<RegistrationEntity> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(RegistrationEntity::getActivityId, referenceRegistration.getActivityId())
                .eq(RegistrationEntity::getGroupName, groupName)
                .orderByAsc(RegistrationEntity::getGroupRank)
                .orderByAsc(RegistrationEntity::getCreateTime);
        List<RegistrationEntity> memberRegistrations = registrationMapper.selectList(memberWrapper);

        List<Map<String, Object>> members = memberRegistrations.stream()
                .map(memberReg -> {
                    UserEntity user = userService.getById(memberReg.getUserId());
                    Map<String, Object> member = new HashMap<>();
                    member.put("registrationId", memberReg.getId());
                    member.put("userId", memberReg.getUserId());
                    member.put("realName", user != null ? user.getRealName() : "-");
                    member.put("role", user != null ? user.getRole() : "-");
                    member.put("unit", memberReg.getTargetSchool() != null ? memberReg.getTargetSchool() : "-");
                    member.put("phone", user != null && user.getPhone() != null ? user.getPhone() : "-");
                    member.put("email", user != null && user.getEmail() != null ? user.getEmail() : "-");
                    member.put("groupRank", memberReg.getGroupRank());
                    return member;
                })
                .collect(Collectors.toList());

        String leaderName = members.isEmpty() ? "-" : (String) members.get(0).get("realName");
        String deputyName = members.size() > 1 ? (String) members.get(1).get("realName") : "-";

        Map<String, Object> team = new HashMap<>();
        team.put("registrationId", referenceRegistration.getId());
        team.put("activityId", referenceRegistration.getActivityId());
        team.put("activityTitle", activity != null ? activity.getName() : "-");
        team.put("groupName", groupName);
        team.put("groupRank", referenceRegistration.getGroupRank());
        team.put("targetSchool", referenceRegistration.getTargetSchool());
        team.put("leader", leaderName);
        team.put("deputyLeader", deputyName);
        team.put("contact", leaderName);
        team.put("members", members);
        team.put("createTime", referenceRegistration.getCreateTime());
        if (includeJoinInfo) {
            team.put("joinRegistrationId", referenceRegistration.getId());
            team.put("currentGroupName", referenceRegistration.getGroupName());
        }
        return team;
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

    @Transactional
    public void exitTeam(Long id, Long userId) {
        RegistrationEntity registration = registrationMapper.selectById(id);
        if (registration == null) {
            throw new BusinessException("报名记录不存在");
        }
        if (!registration.getUserId().equals(userId)) {
            throw new BusinessException("只能退出自己的分组");
        }
        if (registration.getGroupName() == null || registration.getGroupName().isEmpty()) {
            throw new BusinessException("当前报名尚未分组");
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
            throw new BusinessException("只能调整自己的分组");
        }
        if (registration.getStatus() == 4) {
            throw new BusinessException("已撤回的报名无法加入招宣组");
        }

        LambdaQueryWrapper<RegistrationEntity> targetWrapper = new LambdaQueryWrapper<>();
        targetWrapper.eq(RegistrationEntity::getActivityId, registration.getActivityId())
                .eq(RegistrationEntity::getGroupName, groupName.trim());
        List<RegistrationEntity> targetMembers = registrationMapper.selectList(targetWrapper);
        if (targetMembers.isEmpty()) {
            throw new BusinessException("目标招宣组不存在");
        }

        Integer maxRank = targetMembers.stream()
                .map(RegistrationEntity::getGroupRank)
                .filter(rank -> rank != null)
                .max(Integer::compareTo)
                .orElse(0);

        registration.setGroupName(groupName.trim());
        registration.setGroupRank(maxRank + 1);
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
