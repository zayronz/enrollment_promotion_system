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

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final ActivityMapper activityMapper;
    private final RegistrationMapper registrationMapper;
    private final UserService userService;

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

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

        List<Map<String, Object>> monthlyTrend = getMonthlyRegistrationTrend();
        stats.put("monthlyTrend", monthlyTrend);

        List<Map<String, Object>> typeDistribution = getActivityTypeDistribution();
        stats.put("typeDistribution", typeDistribution);

        List<ActivityEntity> recentActivities = activityMapper.selectList(
                new LambdaQueryWrapper<ActivityEntity>()
                        .orderByDesc(ActivityEntity::getCreateTime)
                        .last("LIMIT 5"));

        List<Map<String, Object>> recentList = new ArrayList<>();
        for (ActivityEntity a : recentActivities) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", a.getId());
            map.put("name", a.getName());
            map.put("activityStartTime", a.getActivityStartTime());
            map.put("status", a.getStatus());

            LambdaQueryWrapper<RegistrationEntity> regWrapper = new LambdaQueryWrapper<>();
            regWrapper.eq(RegistrationEntity::getActivityId, a.getId());
            long regCount = registrationMapper.selectCount(regWrapper);
            map.put("registrationCount", regCount);
            recentList.add(map);
        }
        stats.put("recentActivities", recentList);

        return stats;
    }

    public Map<String, Object> getCollegeStats(Long collegeId) {
        Map<String, Object> stats = new HashMap<>();

        List<UserEntity> collegeUsers = userService.getByCollegeId(collegeId);
        List<Long> collegeUserIds = new ArrayList<>();
        for (UserEntity user : collegeUsers) {
            collegeUserIds.add(user.getId());
        }

        if (collegeUserIds.isEmpty()) {
            stats.put("totalRegistrations", 0);
            stats.put("passedCount", 0);
            stats.put("pendingAudit", 0);
            stats.put("schoolCount", 0);
            stats.put("schoolStats", new ArrayList<>());
            stats.put("activityStats", new ArrayList<>());
            return stats;
        }

        LambdaQueryWrapper<RegistrationEntity> allRegWrapper = new LambdaQueryWrapper<>();
        allRegWrapper.in(RegistrationEntity::getUserId, collegeUserIds);
        long totalRegistrations = registrationMapper.selectCount(allRegWrapper);

        LambdaQueryWrapper<RegistrationEntity> passedWrapper = new LambdaQueryWrapper<>();
        passedWrapper.in(RegistrationEntity::getUserId, collegeUserIds)
                .eq(RegistrationEntity::getStatus, 2);
        long passedCount = registrationMapper.selectCount(passedWrapper);

        LambdaQueryWrapper<RegistrationEntity> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.in(RegistrationEntity::getUserId, collegeUserIds)
                .eq(RegistrationEntity::getStatus, 0);
        long pendingAudit = registrationMapper.selectCount(pendingWrapper);

        List<RegistrationEntity> allRegs = registrationMapper.selectList(allRegWrapper);

        Set<String> schoolSet = new HashSet<>();
        for (RegistrationEntity reg : allRegs) {
            if (reg.getTargetSchool() != null) {
                schoolSet.add(reg.getTargetSchool());
            }
        }
        long schoolCount = schoolSet.size();

        stats.put("totalRegistrations", totalRegistrations);
        stats.put("passedCount", passedCount);
        stats.put("pendingAudit", pendingAudit);
        stats.put("schoolCount", schoolCount);

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

        Map<Long, Long> activityCountMap = new HashMap<>();
        for (RegistrationEntity reg : allRegs) {
            Long activityId = reg.getActivityId();
            activityCountMap.put(activityId, activityCountMap.getOrDefault(activityId, 0L) + 1);
        }

        List<Map<String, Object>> activityStats = new ArrayList<>();
        for (Map.Entry<Long, Long> entry : activityCountMap.entrySet()) {
            ActivityEntity act = activityMapper.selectById(entry.getKey());
            Map<String, Object> m = new HashMap<>();
            m.put("name", act != null ? act.getName() : "未知活动");
            m.put("total", entry.getValue());
            m.put("status", act != null ? (act.getStatus() == 1 ? "进行中" :
                    act.getStatus() == 2 ? "已结束" : "草稿") : "-");
            activityStats.add(m);
        }
        activityStats.sort((a, b) -> Long.compare((Long) b.get("total"), (Long) a.get("total")));
        if (activityStats.size() > 10) {
            activityStats = activityStats.subList(0, 10);
        }
        stats.put("activityStats", activityStats);

        long studentCount = 0;
        long teacherCount = 0;
        for (RegistrationEntity reg : allRegs) {
            if (reg.getUserType() == 0) {
                studentCount++;
            } else if (reg.getUserType() == 1) {
                teacherCount++;
            }
        }
        long total = studentCount + teacherCount;
        stats.put("studentCount", studentCount);
        stats.put("teacherCount", teacherCount);
        stats.put("studentPercent", total > 0 ? (int) (studentCount * 100 / total) : 0);
        stats.put("teacherPercent", total > 0 ? (int) (teacherCount * 100 / total) : 0);

        return stats;
    }

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

    private List<Map<String, Object>> getActivityTypeDistribution() {
        List<ActivityEntity> allActivities = activityMapper.selectList(null);

        Map<Integer, Long> typeCountMap = new HashMap<>();
        for (ActivityEntity act : allActivities) {
            Integer type = act.getType() != null ? act.getType() : 0;
            typeCountMap.put(type, typeCountMap.getOrDefault(type, 0L) + 1);
        }

        Map<Integer, Long> typeRegCountMap = new HashMap<>();
        for (ActivityEntity act : allActivities) {
            LambdaQueryWrapper<RegistrationEntity> regWrapper = new LambdaQueryWrapper<>();
            regWrapper.eq(RegistrationEntity::getActivityId, act.getId());
            long regCount = registrationMapper.selectCount(regWrapper);
            Integer type = act.getType() != null ? act.getType() : 0;
            typeRegCountMap.put(type, typeRegCountMap.getOrDefault(type, 0L) + regCount);
        }

        List<Map<String, Object>> distribution = new ArrayList<>();
        String[] typeNames = {"校内活动", "线上宣讲", "线下招生", "校园开放日", "校外活动"};

        for (Map.Entry<Integer, Long> entry : typeCountMap.entrySet()) {
            int type = entry.getKey();
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
