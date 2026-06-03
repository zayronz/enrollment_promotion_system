package com.edu.enrollment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.edu.enrollment.entity.ActivityEntity;
import com.edu.enrollment.entity.RegistrationEntity;
import com.edu.enrollment.entity.UserEntity;
import com.edu.enrollment.mapper.ActivityMapper;
import com.edu.enrollment.mapper.RegistrationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final ActivityMapper activityMapper;
    private final RegistrationMapper registrationMapper;
    private final UserService userService;

    /**
     * 获取学校端仪表盘数据
     */
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // 基本统计数据
        long totalActivities = activityMapper.selectCount(null);
        long totalRegistrations = registrationMapper.selectCount(null);

        LambdaQueryWrapper<RegistrationEntity> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(RegistrationEntity::getStatus, 0);
        long pendingAudit = registrationMapper.selectCount(pendingWrapper);

        LambdaQueryWrapper<RegistrationEntity> passedWrapper = new LambdaQueryWrapper<>();
        passedWrapper.eq(RegistrationEntity::getStatus, 2);
        long passedCount = registrationMapper.selectCount(passedWrapper);

        stats.put("totalActivities", totalActivities);
        stats.put("totalRegistrations", totalRegistrations);
        stats.put("pendingAudit", pendingAudit);
        stats.put("passedCount", passedCount);

        // 报名趋势（近12个月）
        List<Map<String, Object>> monthlyTrend = getMonthlyRegistrationTrend();
        stats.put("monthlyTrend", monthlyTrend);

        // 活动类型分布
        List<Map<String, Object>> typeDistribution = getActivityTypeDistribution();
        stats.put("typeDistribution", typeDistribution);

        // 最近活动
        List<ActivityEntity> recentActivities = activityMapper.selectList(
                new LambdaQueryWrapper<ActivityEntity>()
                        .orderByDesc(ActivityEntity::getCreateTime)
                        .last("LIMIT 5"));
        List<Map<String, Object>> recentList = recentActivities.stream().map(a -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", a.getId());
            map.put("name", a.getName());
            map.put("activityStartTime", a.getActivityStartTime());
            map.put("status", a.getStatus());

            LambdaQueryWrapper<RegistrationEntity> regWrapper = new LambdaQueryWrapper<>();
            regWrapper.eq(RegistrationEntity::getActivityId, a.getId());
            long regCount = registrationMapper.selectCount(regWrapper);
            map.put("registrationCount", regCount);
            return map;
        }).collect(Collectors.toList());
        stats.put("recentActivities", recentList);

        return stats;
    }

    /**
     * 获取学院端统计数据
     */
    public Map<String, Object> getCollegeStats(Long collegeId) {
        Map<String, Object> stats = new HashMap<>();

        // 获取本学院用户列表
        List<UserEntity> collegeUsers = userService.getByCollegeId(collegeId);
        List<Long> collegeUserIds = collegeUsers.stream()
                .map(UserEntity::getId)
                .collect(Collectors.toList());

        if (collegeUserIds.isEmpty()) {
            stats.put("totalRegistrations", 0);
            stats.put("passedCount", 0);
            stats.put("pendingAudit", 0);
            stats.put("schoolCount", 0);
            stats.put("schoolStats", List.of());
            stats.put("activityStats", List.of());
            return stats;
        }

        // 总报名数
        LambdaQueryWrapper<RegistrationEntity> allRegWrapper = new LambdaQueryWrapper<>();
        allRegWrapper.in(RegistrationEntity::getUserId, collegeUserIds);
        long totalRegistrations = registrationMapper.selectCount(allRegWrapper);

        // 已通过
        LambdaQueryWrapper<RegistrationEntity> passedWrapper = new LambdaQueryWrapper<>();
        passedWrapper.in(RegistrationEntity::getUserId, collegeUserIds)
                .eq(RegistrationEntity::getStatus, 2);
        long passedCount = registrationMapper.selectCount(passedWrapper);

        // 待审核
        LambdaQueryWrapper<RegistrationEntity> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.in(RegistrationEntity::getUserId, collegeUserIds)
                .eq(RegistrationEntity::getStatus, 0);
        long pendingAudit = registrationMapper.selectCount(pendingWrapper);

        // 涉及学校数
        List<RegistrationEntity> allRegs = registrationMapper.selectList(allRegWrapper);
        long schoolCount = allRegs.stream()
                .map(RegistrationEntity::getTargetSchool)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        stats.put("totalRegistrations", totalRegistrations);
        stats.put("passedCount", passedCount);
        stats.put("pendingAudit", pendingAudit);
        stats.put("schoolCount", schoolCount);

        // 各学校报名分布
        Map<String, Map<String, Object>> schoolMap = new LinkedHashMap<>();
        for (RegistrationEntity reg : allRegs) {
            String school = reg.getTargetSchool() != null ? reg.getTargetSchool() : "未知";
            schoolMap.computeIfAbsent(school, k -> {
                Map<String, Object> m = new HashMap<>();
                m.put("name", k);
                m.put("total", 0L);
                m.put("approved", 0L);
                m.put("rejected", 0L);
                return m;
            });
            Map<String, Object> m = schoolMap.get(school);
            m.put("total", (Long) m.get("total") + 1);
            if (reg.getStatus() == 2) {
                m.put("approved", (Long) m.get("approved") + 1);
            } else if (reg.getStatus() == 3) {
                m.put("rejected", (Long) m.get("rejected") + 1);
            }
        }
        stats.put("schoolStats", new ArrayList<>(schoolMap.values()));

        // 各活动报名统计
        List<RegistrationEntity> registrations = registrationMapper.selectList(allRegWrapper);
        Map<Long, Long> activityCountMap = registrations.stream()
                .collect(Collectors.groupingBy(RegistrationEntity::getActivityId, Collectors.counting()));

        List<Map<String, Object>> activityStats = activityCountMap.entrySet().stream()
                .map(entry -> {
                    ActivityEntity act = activityMapper.selectById(entry.getKey());
                    Map<String, Object> m = new HashMap<>();
                    m.put("name", act != null ? act.getName() : "未知活动");
                    m.put("total", entry.getValue());
                    m.put("status", act != null ? (act.getStatus() == 1 ? "进行中" :
                            act.getStatus() == 2 ? "已结束" : "草稿") : "-");
                    return m;
                })
                .sorted((a, b) -> Long.compare((Long) b.get("total"), (Long) a.get("total")))
                .limit(10)
                .collect(Collectors.toList());
        stats.put("activityStats", activityStats);

        // 人员结构
        long studentCount = allRegs.stream()
                .filter(r -> r.getUserType() == 0)
                .count();
        long teacherCount = allRegs.stream()
                .filter(r -> r.getUserType() == 1)
                .count();
        long total = studentCount + teacherCount;
        stats.put("studentCount", studentCount);
        stats.put("teacherCount", teacherCount);
        stats.put("studentPercent", total > 0 ? (int) (studentCount * 100 / total) : 0);
        stats.put("teacherPercent", total > 0 ? (int) (teacherCount * 100 / total) : 0);

        return stats;
    }

    /**
     * 近12个月报名趋势
     */
    private List<Map<String, Object>> getMonthlyRegistrationTrend() {
        List<Map<String, Object>> trend = new ArrayList<>();
        DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("yyyy-MM");

        for (int i = 11; i >= 0; i--) {
            LocalDateTime start = LocalDateTime.now().minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime end = start.plusMonths(1);

            LambdaQueryWrapper<RegistrationEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.ge(RegistrationEntity::getCreateTime, start)
                    .lt(RegistrationEntity::getCreateTime, end);
            long count = registrationMapper.selectCount(wrapper);

            Map<String, Object> monthData = new HashMap<>();
            monthData.put("month", start.format(monthFmt));
            monthData.put("count", count);
            trend.add(monthData);
        }
        return trend;
    }

    /**
     * 活动类型分布统计
     */
    private List<Map<String, Object>> getActivityTypeDistribution() {
        List<ActivityEntity> allActivities = activityMapper.selectList(null);

        Map<Integer, Long> typeCountMap = allActivities.stream()
                .collect(Collectors.groupingBy(ActivityEntity::getType, Collectors.counting()));

        // 统计每种类型活动的报名数
        Map<Integer, Long> typeRegCountMap = new HashMap<>();
        for (ActivityEntity act : allActivities) {
            LambdaQueryWrapper<RegistrationEntity> regWrapper = new LambdaQueryWrapper<>();
            regWrapper.eq(RegistrationEntity::getActivityId, act.getId());
            long regCount = registrationMapper.selectCount(regWrapper);
            typeRegCountMap.merge(act.getType(), regCount, Long::sum);
        }

        List<Map<String, Object>> distribution = new ArrayList<>();
        String[] typeNames = {"校内活动", "线上宣讲", "线下招生", "校园开放日", "校外活动"};

        for (Map.Entry<Integer, Long> entry : typeCountMap.entrySet()) {
            int type = entry.getKey() != null ? entry.getKey() : 0;
            Map<String, Object> item = new HashMap<>();
            item.put("type", type);
            item.put("name", type < typeNames.length ? typeNames[type] : "其他");
            item.put("value", entry.getValue());
            item.put("registrationCount", typeRegCountMap.getOrDefault(type, 0L));
            distribution.add(item);
        }
        return distribution;
    }
}
