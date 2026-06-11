package com.edu.enrollment.service;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edu.enrollment.dto.RegistrationSubmitDTO;
import com.edu.enrollment.entity.ActivityEntity;
import com.edu.enrollment.entity.AuditRecordEntity;
import com.edu.enrollment.entity.FeedbackEntity;
import com.edu.enrollment.entity.RegistrationEntity;
import com.edu.enrollment.entity.UserEntity;
import com.edu.enrollment.exception.BusinessException;
import com.edu.enrollment.mapper.AuditRecordMapper;
import com.edu.enrollment.mapper.FeedbackMapper;
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
    private final AuditRecordMapper auditRecordMapper;
    private final FeedbackMapper feedbackMapper;

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

        // 2. 检查是否已报名
        RegistrationEntity existReg = findActiveRegistration(dto.getActivityId(), userId);
        if (existReg != null) {
            throw new BusinessException("您已报名过此活动");
        }

        // 3. 获取用户信息
        UserEntity user = userService.getById(userId);

        // 4. 标准化学校名称
        String normalizedSchool = normalizeTargetSchool(dto.getActivityId(), dto.getTargetSchool());

        // 5. 创建报名记录
        RegistrationEntity registration = new RegistrationEntity();
        registration.setActivityId(dto.getActivityId());
        registration.setUserId(userId);
        registration.setUserType("STUDENT".equalsIgnoreCase(user.getRole()) ? 0 : 1);
        registration.setTargetSchool(normalizedSchool);
        registration.setScore(dto.getScore());
        registration.setFormData(JSONUtil.toJsonStr(buildRegistrationFormData(dto, user, normalizedSchool)));
        registration.setStatus(0); // 待审核
        registration.setCurrentNode("college_audit");

        registrationMapper.insert(registration);

        // 6. 自动分组（如果活动设置开启）
        if (activity.getAutoGroup() == 1) {
            autoGroup(activity.getId(), normalizedSchool);
        }

        return registration.getId();
    }

    private Map<String, Object> buildRegistrationFormData(RegistrationSubmitDTO dto, UserEntity user, String normalizedSchool) {
        Map<String, Object> data = new HashMap<>();
        if (dto.getFormData() != null) {
            data.putAll(dto.getFormData());
        }
        data.put("targetSchool", normalizedSchool);
        data.put("basicInfo", Map.of(
                "userId", user.getId(),
                "realName", user.getRealName() != null ? user.getRealName() : "",
                "username", user.getUsername() != null ? user.getUsername() : "",
                "role", user.getRole() != null ? user.getRole() : "",
                "phone", user.getPhone() != null ? user.getPhone() : "",
                "email", user.getEmail() != null ? user.getEmail() : ""
        ));
        if (dto.getCustomFields() != null) {
            data.put("customFields", dto.getCustomFields());
        }
        if (dto.getFileIds() != null) {
            data.put("attachments", dto.getFileIds());
        }
        return data;
    }

    private RegistrationEntity findActiveRegistration(Long activityId, Long userId) {
        List<RegistrationEntity> registrations = registrationMapper.selectList(
                new LambdaQueryWrapper<RegistrationEntity>()
                        .eq(RegistrationEntity::getActivityId, activityId)
                        .eq(RegistrationEntity::getUserId, userId)
                        .ne(RegistrationEntity::getStatus, 4)
                        .orderByDesc(RegistrationEntity::getCreateTime)
        );
        return registrations.isEmpty() ? null : registrations.get(0);
    }

    private String normalizeTargetSchool(Long activityId, String inputSchool) {
        String normalized = schoolNameNormalizer.normalize(inputSchool);
        if (normalized == null || normalized.isBlank()) {
            return normalized;
        }

        List<RegistrationEntity> existingRegistrations = registrationMapper.selectList(
                new LambdaQueryWrapper<RegistrationEntity>()
                        .eq(RegistrationEntity::getActivityId, activityId)
                        .isNotNull(RegistrationEntity::getTargetSchool)
        );

        String trimmed = inputSchool == null ? "" : inputSchool.trim();
        for (RegistrationEntity registration : existingRegistrations) {
            String existingSchool = registration.getTargetSchool();
            if (existingSchool == null || existingSchool.isBlank()) {
                continue;
            }
            if (existingSchool.contains(trimmed) || trimmed.contains(existingSchool)) {
                return existingSchool.length() >= normalized.length() ? existingSchool : normalized;
            }
        }

        return normalized;
    }

    public Map<String, Object> getRegistrationStatus(Long activityId, Long userId) {
        ActivityEntity activity = activityService.getById(activityId);
        if (activity == null) {
            throw new BusinessException("活动不存在");
        }

        RegistrationEntity registration = findActiveRegistration(activityId, userId);
        boolean registered = registration != null;
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

        Set<String> suggestions = new LinkedHashSet<>();
        List<RegistrationEntity> registrations = registrationMapper.selectList(
                new LambdaQueryWrapper<RegistrationEntity>()
                        .eq(activityId != null, RegistrationEntity::getActivityId, activityId)
                        .like(RegistrationEntity::getTargetSchool, keyword.trim())
                        .orderByDesc(RegistrationEntity::getCreateTime)
        );

        registrations.stream()
                .map(RegistrationEntity::getTargetSchool)
                .filter(name -> name != null && !name.isBlank())
                .limit(8)
                .forEach(suggestions::add);

        if (suggestions.size() < 8) {
            List<String> dictSuggestions = schoolNameNormalizer.suggest(keyword, 8 - suggestions.size());
            suggestions.addAll(dictSuggestions);
        }

        return new ArrayList<>(suggestions).stream().limit(8).collect(Collectors.toList());
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

    public Map<String, Object> getDetail(Long id) {
        RegistrationEntity registration = registrationMapper.selectById(id);
        if (registration == null) {
            throw new BusinessException("报名记录不存在");
        }

        Map<String, Object> detail = new HashMap<>();
        ActivityEntity activity = activityService.getById(registration.getActivityId());
        detail.put("id", registration.getId());
        detail.put("activityId", registration.getActivityId());
        detail.put("activityTitle", activity != null ? activity.getName() : "-");
        detail.put("userId", registration.getUserId());
        detail.put("targetSchool", registration.getTargetSchool());
        detail.put("score", registration.getScore());
        detail.put("status", registration.getStatus());
        detail.put("currentNode", registration.getCurrentNode());
        detail.put("rejectReason", registration.getRejectReason());
        detail.put("groupName", registration.getGroupName());
        detail.put("groupRank", registration.getGroupRank());
        detail.put("createTime", registration.getCreateTime());
        detail.put("activityDetail", activity != null ? activityService.getDetail(activity.getId()) : null);
        detail.put("auditProgress", getAuditProgress(registration));
        detail.put("auditLogs", buildAuditLogs(registration.getId()));
        detail.put("teamMembers", buildTeamMembers(registration));
        detail.put("feedbacks", registration.getStatus() == 2 ? buildFeedbacks(registration.getActivityId()) : List.of());

        Map<String, Object> formData = new HashMap<>();
        if (registration.getFormData() != null && !registration.getFormData().isBlank()) {
            formData.putAll(JSONUtil.parseObj(registration.getFormData()));
        }
        detail.put("formData", formData);
        detail.put("basicInfo", formData.getOrDefault("basicInfo", Map.of()));
        detail.put("customFields", formData.getOrDefault("customFields", List.of()));
        detail.put("attachments", formData.getOrDefault("attachments", List.of()));
        return detail;
    }

    private String getAuditProgress(RegistrationEntity registration) {
        if (registration.getStatus() == 0) {
            return "待审核：" + getAuditNodeName(registration.getCurrentNode());
        }
        if (registration.getStatus() == 1) {
            return "学院审核通过，等待学校审核";
        }
        if (registration.getStatus() == 2) {
            return "审核已全部通过";
        }
        if (registration.getStatus() == 3) {
            return "审核已拒绝：" + (registration.getRejectReason() != null ? registration.getRejectReason() : "-");
        }
        if (registration.getStatus() == 4) {
            return "报名已撤回";
        }
        return "未知进度";
    }

    private List<Map<String, Object>> buildAuditLogs(Long registrationId) {
        return auditRecordMapper.selectByRegistrationId(registrationId).stream()
                .map(record -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", record.getId());
                    map.put("node", record.getNode());
                    map.put("nodeName", getAuditNodeName(record.getNode()));
                    map.put("auditorId", record.getAuditorId());
                    map.put("auditorName", record.getAuditorName());
                    map.put("result", record.getResult() == 1 ? "APPROVED" : "REJECTED");
                    map.put("resultText", record.getResult() == 1 ? "通过" : "拒绝");
                    map.put("comment", record.getComment());
                    map.put("attachmentUrls", record.getAttachmentUrls());
                    map.put("createTime", record.getCreateTime());
                    return map;
                })
                .collect(Collectors.toList());
    }

    private String getAuditNodeName(String node) {
        if ("college_audit".equals(node)) {
            return "学院审核";
        }
        if ("school_audit".equals(node)) {
            return "学校审核";
        }
        if ("completed".equals(node)) {
            return "审核完成";
        }
        return node != null ? node : "-";
    }

    private List<Map<String, Object>> buildTeamMembers(RegistrationEntity registration) {
        if (registration.getGroupName() == null || registration.getGroupName().isBlank()) {
            return List.of();
        }

        List<RegistrationEntity> memberRegistrations = registrationMapper.selectList(
                new LambdaQueryWrapper<RegistrationEntity>()
                        .eq(RegistrationEntity::getActivityId, registration.getActivityId())
                        .eq(RegistrationEntity::getGroupName, registration.getGroupName())
                        .ne(RegistrationEntity::getStatus, 4)
                        .orderByAsc(RegistrationEntity::getGroupRank)
                        .orderByAsc(RegistrationEntity::getCreateTime)
        );

        return memberRegistrations.stream()
                .map(memberReg -> {
                    UserEntity user = userService.getById(memberReg.getUserId());
                    Map<String, Object> member = new HashMap<>();
                    member.put("registrationId", memberReg.getId());
                    member.put("userId", memberReg.getUserId());
                    member.put("realName", user != null ? user.getRealName() : "-");
                    member.put("role", user != null ? user.getRole() : "-");
                    member.put("targetSchool", memberReg.getTargetSchool());
                    member.put("groupName", memberReg.getGroupName());
                    member.put("groupRank", memberReg.getGroupRank());
                    member.put("phone", user != null && user.getPhone() != null ? user.getPhone() : "-");
                    member.put("email", user != null && user.getEmail() != null ? user.getEmail() : "-");
                    return member;
                })
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> buildFeedbacks(Long activityId) {
        List<FeedbackEntity> feedbacks = feedbackMapper.selectByActivityId(activityId);
        return feedbacks.stream()
                .map(feedback -> {
                    UserEntity user = userService.getById(feedback.getUserId());
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", feedback.getId());
                    map.put("activityId", feedback.getActivityId());
                    map.put("userId", feedback.getUserId());
                    map.put("userName", user != null ? user.getRealName() : "-");
                    map.put("userRole", feedback.getUserRole());
                    map.put("title", feedback.getTitle());
                    map.put("content", feedback.getContent());
                    map.put("attachmentUrls", feedback.getAttachmentUrls());
                    map.put("type", feedback.getType());
                    map.put("createTime", feedback.getCreateTime());
                    return map;
                })
                .collect(Collectors.toList());
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
                    map.put("userType", user != null ? ("STUDENT".equalsIgnoreCase(user.getRole()) ? "STUDENT" : "TEACHER") : "-");
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
