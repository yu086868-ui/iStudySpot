package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.entity.Achievement;
import com.ycyu.istudyspotbackend.entity.Order;
import com.ycyu.istudyspotbackend.entity.Result;
import com.ycyu.istudyspotbackend.entity.UserAchievement;
import com.ycyu.istudyspotbackend.mapper.AchievementMapper;
import com.ycyu.istudyspotbackend.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/achievements")
public class AchievementController {

    @Autowired
    private AchievementMapper achievementMapper;

    @Autowired
    private OrderMapper orderMapper;

    @GetMapping
    public Result<List<Map<String, Object>>> getAchievements(@RequestAttribute Long userId) {
        List<Achievement> allAchievements = achievementMapper.findAll();
        List<UserAchievement> userAchievements = achievementMapper.findByUserId(userId);
        Set<String> unlockedCodes = userAchievements.stream()
                .map(UserAchievement::getAchievementCode)
                .collect(Collectors.toSet());

        List<Order> checkinRecords = orderMapper.findCheckinRecordsByUserId(userId);
        List<Order> allOrders = orderMapper.findByUserId(userId);

        int totalMinutes = 0;
        int streakDays = 0;
        int earlyBirdDays = 0;
        int nightOwlDays = 0;
        Map<String, Integer> seatUsageMap = new HashMap<>();
        Map<Integer, Integer> hourDistribution = new HashMap<>();
        LocalDateTime lastCheckinDate = null;
        int consecutiveOnTimeDays = 0;

        for (Order order : checkinRecords) {
            LocalDateTime start = order.getActualStartTime() != null ? order.getActualStartTime() : order.getPlanStartTime();
            LocalDateTime end = order.getActualEndTime() != null ? order.getActualEndTime() : order.getPlanEndTime();
            if (start == null) start = order.getPlanStartTime();
            if (end == null) end = order.getPlanEndTime();
            if (start == null || end == null) continue;

            long minutes = ChronoUnit.MINUTES.between(start, end);
            totalMinutes += minutes;

            if (start.getHour() >= 7 && start.getHour() < 8) earlyBirdDays++;
            if (end.getHour() >= 21) nightOwlDays++;

            String seatKey = order.getSeatPosition() != null ? order.getSeatPosition() : "unknown";
            seatUsageMap.merge(seatKey, 1, Integer::sum);

            if (order.getPlanStartTime() != null && order.getActualStartTime() != null) {
                long delayMinutes = ChronoUnit.MINUTES.between(order.getPlanStartTime(), order.getActualStartTime());
                if (delayMinutes <= 5) consecutiveOnTimeDays++;
            }
        }

        int maxStreak = 0;
        int currentStreak = 0;
        List<LocalDateTime> sortedStarts = checkinRecords.stream()
                .map(o -> o.getActualStartTime() != null ? o.getActualStartTime() : o.getPlanStartTime())
                .filter(Objects::nonNull)
                .sorted()
                .collect(Collectors.toList());

        for (int i = 0; i < sortedStarts.size(); i++) {
            if (i == 0) {
                currentStreak = 1;
            } else {
                long daysBetween = ChronoUnit.DAYS.between(sortedStarts.get(i-1).toLocalDate(), sortedStarts.get(i).toLocalDate());
                if (daysBetween == 1) {
                    currentStreak++;
                } else if (daysBetween > 1) {
                    currentStreak = 1;
                }
            }
            maxStreak = Math.max(maxStreak, currentStreak);
        }

        int totalHours = totalMinutes / 60;
        int maxSeatUsage = seatUsageMap.values().stream().max(Integer::compareTo).orElse(0);
        long maxSingleSession = 0;
        for (Order order : checkinRecords) {
            LocalDateTime start = order.getActualStartTime() != null ? order.getActualStartTime() : order.getPlanStartTime();
            LocalDateTime end = order.getActualEndTime() != null ? order.getActualEndTime() : order.getPlanEndTime();
            if (start == null || end == null) continue;
            long sessionMinutes = ChronoUnit.MINUTES.between(start, end);
            maxSingleSession = Math.max(maxSingleSession, sessionMinutes);
        }

        Map<String, Boolean> conditions = new HashMap<>();
        conditions.put("early_bird", earlyBirdDays >= 3);
        conditions.put("night_owl", nightOwlDays >= 3);
        conditions.put("study_master", totalHours >= 100);
        conditions.put("streak_king", maxStreak >= 7);
        conditions.put("punctual", consecutiveOnTimeDays >= 30);
        conditions.put("regular", maxSeatUsage >= 10);
        conditions.put("social", false);
        conditions.put("marathon", maxSingleSession >= 360);

        for (Map.Entry<String, Boolean> entry : conditions.entrySet()) {
            if (entry.getValue() && !unlockedCodes.contains(entry.getKey())) {
                UserAchievement ua = new UserAchievement();
                ua.setUserId(userId);
                ua.setAchievementCode(entry.getKey());
                achievementMapper.insert(ua);
                unlockedCodes.add(entry.getKey());
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Achievement ach : allAchievements) {
            Map<String, Object> item = new HashMap<>();
            item.put("code", ach.getCode());
            item.put("name", ach.getName());
            item.put("description", ach.getDescription());
            item.put("icon", ach.getIcon());
            item.put("category", ach.getCategory());
            item.put("isUnlocked", unlockedCodes.contains(ach.getCode()));
            if (unlockedCodes.contains(ach.getCode())) {
                UserAchievement ua = userAchievements.stream()
                        .filter(u -> u.getAchievementCode().equals(ach.getCode()))
                        .findFirst().orElse(null);
                item.put("unlockedAt", ua != null && ua.getUnlockedAt() != null ? ua.getUnlockedAt().toString() : null);
            }
            result.add(item);
        }

        return Result.success("success", result);
    }
}
