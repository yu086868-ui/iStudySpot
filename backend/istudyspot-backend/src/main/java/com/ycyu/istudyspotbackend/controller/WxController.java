package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.agent.tool.ReservationRulesProvider;
import com.ycyu.istudyspotbackend.dto.BookingDTO;
import com.ycyu.istudyspotbackend.entity.*;
import com.ycyu.istudyspotbackend.mapper.OrderMapper;
import com.ycyu.istudyspotbackend.mapper.UserMapper;
import com.ycyu.istudyspotbackend.service.OrderService;
import com.ycyu.istudyspotbackend.service.SeatService;
import com.ycyu.istudyspotbackend.service.StudyRoomService;
import com.ycyu.istudyspotbackend.service.UserService;
import com.ycyu.istudyspotbackend.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 微信小程序端控制器
 * 处理 /api/wx/** 路由，与安卓端 /api/** 路由完全隔离
 */
@RestController
@RequestMapping("/api/wx")
public class WxController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserService userService;

    @Autowired
    private StudyRoomService studyRoomService;

    @Autowired
    private SeatService seatService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ReservationRulesProvider reservationRulesProvider;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ==================== 用户相关接口 ====================

    /**
     * 微信登录（无需JWT）
     * 本地测试简化实现：将code直接作为openId使用，用"wx_"前缀存储在username字段
     */
    @PostMapping("/user/login")
    public Result<Map<String, Object>> wxLogin(@RequestBody Map<String, String> params) {
        String code = params.get("code");
        if (code == null || code.isEmpty()) {
            return Result.error("code不能为空");
        }

        // 本地测试简化：将code作为openId，用"wx_"前缀存储在username字段以区分安卓端用户
        String wxUsername = "wx_" + code;
        User user = userMapper.findByUsername(wxUsername);
        boolean isNewUser = false;

        if (user == null) {
            // 自动创建新用户
            user = new User();
            user.setUsername(wxUsername);
            user.setPassword("wx_default"); // 微信用户无需密码
            user.setNickname("微信用户" + code.substring(0, Math.min(code.length(), 6)));
            user.setAvatar(null);
            user.setPhone(null);
            user.setEmail(null);
            userMapper.insert(user);
            isNewUser = true;
            // 重新查询获取完整用户信息
            user = userMapper.findByUsername(wxUsername);
        }

        // 更新最后登录时间
        userMapper.updateLastLoginTime(user.getId());

        // 生成JWT令牌
        String token = jwtUtils.generateToken(user.getId());

        // 构建返回数据
        Map<String, Object> userData = buildSafeUserInfo(user);
        Map<String, Object> data = new HashMap<>();
        data.put("isNewUser", isNewUser);
        data.put("user", userData);
        data.put("token", token);

        return Result.success("登录成功", data);
    }

    /**
     * 获取用户信息（需要JWT）
     */
    @GetMapping("/user/profile")
    public Result<Map<String, Object>> getUserProfile(@RequestAttribute Long userId) {
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        try {
            User user = userService.getUserInfo(userId);
            Map<String, Object> safeInfo = buildSafeUserInfo(user);
            return Result.success("获取成功", safeInfo);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 修改用户信息（需要JWT）
     */
    @PutMapping("/user/profile")
    public Result<Map<String, Object>> updateUserProfile(
            @RequestBody Map<String, Object> params,
            @RequestAttribute Long userId) {
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        try {
            User user = new User();
            user.setId(userId);
            if (params.get("nickname") != null) {
                user.setNickname((String) params.get("nickname"));
            }
            if (params.get("avatar") != null) {
                user.setAvatar((String) params.get("avatar"));
            }
            if (params.get("phone") != null) {
                user.setPhone((String) params.get("phone"));
            }
            if (params.get("email") != null) {
                user.setEmail((String) params.get("email"));
            }
            User updated = userService.updateUserInfo(user);
            Map<String, Object> safeInfo = buildSafeUserInfo(updated);
            return Result.success("更新成功", safeInfo);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 用户首页信息（需要JWT）
     */
    @GetMapping("/user/home")
    public Result<Map<String, Object>> getUserHome(@RequestAttribute Long userId) {
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        try {
            User user = userService.getUserInfo(userId);
            Map<String, Object> homeData = new HashMap<>();
            homeData.put("user", buildSafeUserInfo(user));

            // 获取当前签到状态
            Order currentOrder = orderMapper.findCurrentCheckinByUserId(userId);
            homeData.put("isCheckedIn", currentOrder != null);
            if (currentOrder != null) {
                Map<String, Object> checkinInfo = new HashMap<>();
                checkinInfo.put("reservationId", currentOrder.getId());
                checkinInfo.put("seatPosition", currentOrder.getSeatPosition());
                checkinInfo.put("studyRoomName", currentOrder.getStudyRoomName());
                homeData.put("currentCheckIn", checkinInfo);
            }

            // 获取今日预约数量
            Map<String, Object> orderList = orderService.getOrderList(userId, null, null, null, 1, 1);
            homeData.put("totalReservations", orderList.get("total"));

            return Result.success("获取成功", homeData);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 修改头像（需要JWT）
     */
    @PostMapping("/user/avatar")
    public Result<Map<String, Object>> updateAvatar(
            @RequestBody Map<String, String> params,
            @RequestAttribute Long userId) {
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        String avatar = params.get("avatar");
        if (avatar == null || avatar.isEmpty()) {
            return Result.error("头像地址不能为空");
        }
        try {
            User user = new User();
            user.setId(userId);
            user.setAvatar(avatar);
            User updated = userService.updateUserInfo(user);
            Map<String, Object> safeInfo = buildSafeUserInfo(updated);
            return Result.success("头像修改成功", safeInfo);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    // ==================== 自习室相关接口 ====================

    /**
     * 自习室列表（无需JWT）
     */
    @GetMapping("/studyrooms")
    public Result<Map<String, Object>> getStudyRoomList(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer floor,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        Map<String, Object> result = studyRoomService.getStudyRoomList(status, floor, keyword, page, pageSize);
        return Result.success("success", result);
    }

    /**
     * 自习室详情（无需JWT）
     */
    @GetMapping("/studyrooms/{id}")
    public Result<StudyRoom> getStudyRoomDetail(@PathVariable Long id) {
        try {
            StudyRoom room = studyRoomService.getStudyRoomDetail(id);
            return Result.success("success", room);
        } catch (RuntimeException e) {
            return Result.notFound(e.getMessage());
        }
    }

    // ==================== 座位相关接口 ====================

    /**
     * 座位列表（无需JWT）
     */
    @GetMapping("/studyrooms/{studyRoomId}/seats")
    public Result<List<Seat>> getSeatList(
            @PathVariable Long studyRoomId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer row,
            @RequestParam(required = false) Integer col) {
        List<Seat> seats = seatService.getSeatList(studyRoomId, status, type, row, col);
        return Result.success("success", seats);
    }

    /**
     * 座位布局（无需JWT）
     */
    @GetMapping("/studyrooms/{studyRoomId}/seat-layout")
    public Result<SeatLayoutResponse> getSeatLayout(@PathVariable Long studyRoomId) {
        SeatLayoutResponse layout = seatService.getSeatLayout(studyRoomId);
        return Result.success("success", layout);
    }

    /**
     * 座位详情（无需JWT）
     */
    @GetMapping("/seats/{id}")
    public Result<Seat> getSeatDetail(@PathVariable Long id) {
        Seat seat = seatService.getSeatDetail(id);
        return Result.success("success", seat);
    }

    // ==================== 预约相关接口 ====================

    /**
     * 创建预约（需要JWT）
     */
    @PostMapping("/reservations")
    public Result<Map<String, Object>> createReservation(
            @RequestBody BookingDTO bookingDTO,
            @RequestAttribute Long userId) {
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        Map<String, Object> result = orderService.createOrder(
                userId,
                bookingDTO.getStudyRoomId(),
                bookingDTO.getSeatId(),
                bookingDTO.getStartTime(),
                bookingDTO.getEndTime(),
                bookingDTO.getBookingType()
        );
        return Result.success("预约成功", result);
    }

    /**
     * 我的预约列表（需要JWT）
     */
    @GetMapping("/reservations/my")
    public Result<Map<String, Object>> getMyReservations(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestAttribute Long userId) {
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        Map<String, Object> result = orderService.getOrderList(userId, status, startDate, endDate, page, pageSize);
        return Result.success("success", result);
    }

    /**
     * 预约详情（无需JWT）
     */
    @GetMapping("/reservations/{id}")
    public Result<Order> getReservationDetail(@PathVariable Long id) {
        Order order = orderService.getOrderDetail(id);
        return Result.success("success", order);
    }

    /**
     * 取消预约（需要JWT）
     */
    @PostMapping("/reservations/{id}/cancel")
    public Result<Void> cancelReservation(@PathVariable Long id, @RequestAttribute Long userId) {
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        orderService.cancelOrder(id);
        return Result.success("预约已取消", null);
    }

    /**
     * 预约规则（无需JWT）
     */
    @GetMapping("/reservations/rules")
    public Result<Map<String, Object>> getReservationRules() {
        Map<String, Object> rules = reservationRulesProvider.getRules();
        return Result.success("success", rules);
    }

    // ==================== 签到相关接口 ====================

    /**
     * 签到（需要JWT）
     */
    @PostMapping("/checkin")
    public Result<Map<String, Object>> checkin(
            @RequestBody Map<String, String> params,
            @RequestAttribute Long userId) {
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        try {
            String reservationIdStr = params.get("reservationId");
            String seatIdStr = params.get("seatId");
            if (reservationIdStr == null || reservationIdStr.isEmpty()) {
                return Result.error("预约ID不能为空");
            }
            if (seatIdStr == null || seatIdStr.isEmpty()) {
                return Result.error("座位ID不能为空");
            }
            Long reservationId = Long.valueOf(reservationIdStr);
            Map<String, Object> result = orderService.checkin(reservationId, seatIdStr);
            return Result.success("签到成功", result);
        } catch (NumberFormatException e) {
            return Result.error("预约ID格式错误");
        } catch (Exception e) {
            return Result.internalServerError(e.getMessage());
        }
    }

    /**
     * 签退（需要JWT）
     */
    @PostMapping("/checkout")
    public Result<Map<String, Object>> checkout(
            @RequestBody Map<String, String> params,
            @RequestAttribute Long userId) {
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        try {
            String checkInRecordIdStr = params.get("checkInRecordId");
            if (checkInRecordIdStr == null || checkInRecordIdStr.isEmpty()) {
                return Result.error("签到记录ID不能为空");
            }
            Long checkInRecordId = Long.valueOf(checkInRecordIdStr);
            Map<String, Object> result = orderService.checkout(checkInRecordId);
            return Result.success("签退成功", result);
        } catch (NumberFormatException e) {
            return Result.error("签到记录ID格式错误");
        } catch (Exception e) {
            return Result.internalServerError(e.getMessage());
        }
    }

    /**
     * 签到记录（需要JWT）
     */
    @GetMapping("/checkin/records")
    public Result<Map<String, Object>> getCheckInRecords(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestAttribute Long userId) {
        if (userId == null) {
            return Result.unauthorized("未登录");
        }

        List<Order> allRecords = orderMapper.findCheckinRecordsByUserId(userId);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekStart = now.minusDays(now.getDayOfWeek().getValue() - 1).toLocalDate().atStartOfDay();
        LocalDateTime monthStart = now.withDayOfMonth(1).toLocalDate().atStartOfDay();

        int totalMinutes = 0;
        int weekMinutes = 0;
        int monthMinutes = 0;
        int streakDays = 0;
        Map<String, Integer> seatUsageMap = new HashMap<>();
        Map<Integer, Integer> hourDistribution = new HashMap<>();

        LocalDate lastCheckinDate = null;
        List<Map<String, Object>> recordList = new ArrayList<>();

        for (Order order : allRecords) {
            LocalDateTime start = order.getActualStartTime() != null ? order.getActualStartTime() : order.getPlanStartTime();
            LocalDateTime end = order.getActualEndTime() != null ? order.getActualEndTime() : order.getPlanEndTime();
            if (start == null) start = order.getPlanStartTime();
            if (end == null) end = order.getPlanEndTime();
            if (start == null || end == null) continue;

            long minutes = ChronoUnit.MINUTES.between(start, end);
            totalMinutes += minutes;

            if (!start.isBefore(weekStart)) {
                weekMinutes += minutes;
            }
            if (!start.isBefore(monthStart)) {
                monthMinutes += minutes;
            }

            String seatKey = order.getSeatPosition() != null ? order.getSeatPosition() : "unknown";
            seatUsageMap.merge(seatKey, 1, Integer::sum);

            int hour = start.getHour();
            hourDistribution.merge(hour, 1, Integer::sum);

            LocalDate checkinDate = start.toLocalDate();
            if (lastCheckinDate == null || ChronoUnit.DAYS.between(lastCheckinDate, checkinDate) == 1) {
                streakDays++;
            } else if (ChronoUnit.DAYS.between(lastCheckinDate, checkinDate) > 1) {
                streakDays = 1;
            }
            lastCheckinDate = checkinDate;

            Map<String, Object> record = new HashMap<>();
            record.put("id", order.getId());
            record.put("seatPosition", order.getSeatPosition());
            record.put("studyRoomName", order.getStudyRoomName());
            record.put("startTime", start.format(formatter));
            record.put("endTime", end.format(formatter));
            record.put("duration", minutes);
            record.put("status", order.getStatus());
            recordList.add(record);
        }

        String favoriteSeat = seatUsageMap.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("");

        String peakTime = hourDistribution.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(e -> String.format("%02d:00-%02d:00", e.getKey(), e.getKey() + 1))
                .orElse("");

        double avgDuration = allRecords.isEmpty() ? 0.0 :
                (double) totalMinutes / allRecords.size() / 60.0;

        int startIdx = (page - 1) * pageSize;
        int endIdx = Math.min(startIdx + pageSize, recordList.size());
        List<Map<String, Object>> pagedRecords = startIdx < recordList.size()
                ? recordList.subList(startIdx, endIdx)
                : new ArrayList<>();

        Map<String, Object> result = new HashMap<>();
        result.put("records", pagedRecords);
        result.put("total", allRecords.size());
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("totalHours", totalMinutes / 60);
        result.put("weekHours", weekMinutes / 60);
        result.put("monthHours", monthMinutes / 60);
        result.put("streak", streakDays);
        result.put("avgDuration", Math.round(avgDuration * 10.0) / 10.0);
        result.put("favoriteSeat", favoriteSeat);
        result.put("peakTime", peakTime);
        return Result.success("success", result);
    }

    /**
     * 当前签到状态（需要JWT）
     */
    @GetMapping("/checkin/current")
    public Result<Map<String, Object>> getCurrentCheckInStatus(@RequestAttribute Long userId) {
        if (userId == null) {
            return Result.unauthorized("未登录");
        }

        Order currentOrder = orderMapper.findCurrentCheckinByUserId(userId);

        Map<String, Object> result = new HashMap<>();
        if (currentOrder != null) {
            result.put("isCheckedIn", true);
            Map<String, Object> record = new HashMap<>();
            record.put("id", currentOrder.getId());
            record.put("seatPosition", currentOrder.getSeatPosition());
            record.put("studyRoomName", currentOrder.getStudyRoomName());
            record.put("startTime", currentOrder.getPlanStartTime() != null
                    ? currentOrder.getPlanStartTime().format(formatter) : null);
            record.put("endTime", currentOrder.getPlanEndTime() != null
                    ? currentOrder.getPlanEndTime().format(formatter) : null);
            record.put("status", currentOrder.getStatus());
            result.put("checkInRecord", record);
        } else {
            result.put("isCheckedIn", false);
            result.put("checkInRecord", null);
        }
        return Result.success("success", result);
    }

    // ==================== 公告相关接口 ====================

    /**
     * 公告列表（无需JWT）
     */
    @GetMapping("/announcements")
    public Result<Map<String, Object>> getAnnouncementList(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String priority,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        // TODO: 实现公告列表查询
        Map<String, Object> result = Map.of(
                "list", new ArrayList<>(),
                "total", 0,
                "page", page,
                "pageSize", pageSize
        );
        return Result.success("success", result);
    }

    /**
     * 公告详情（无需JWT）
     */
    @GetMapping("/announcements/{id}")
    public Result<Map<String, Object>> getAnnouncementDetail(@PathVariable Long id) {
        // TODO: 实现公告详情查询
        Map<String, Object> result = Map.of(
                "id", id.toString(),
                "title", "示例公告",
                "content", "这是一个示例公告",
                "type", "notice",
                "priority", "medium",
                "publishTime", "2024-01-01T00:00:00Z",
                "expireTime", "2024-01-31T23:59:59Z",
                "author", "管理员",
                "status", "published"
        );
        return Result.success("success", result);
    }

    // ==================== 规则相关接口 ====================

    /**
     * 规则列表（无需JWT）
     */
    @GetMapping("/rules")
    public Result<List<Map<String, Object>>> getRulesList(
            @RequestParam(required = false) String studyRoomId,
            @RequestParam(required = false) String category) {
        List<Map<String, Object>> rules = buildRules();
        if (category != null && !category.isEmpty()) {
            rules = rules.stream().filter(r -> category.equals(r.get("category"))).toList();
        }
        return Result.success("success", rules);
    }

    /**
     * 规则详情（无需JWT）
     */
    @GetMapping("/rules/{id}")
    public Result<Map<String, Object>> getRuleDetail(@PathVariable Long id) {
        List<Map<String, Object>> rules = buildRules();
        Map<String, Object> rule = rules.stream()
                .filter(r -> id.equals(r.get("id")))
                .findFirst()
                .orElse(null);
        if (rule == null) {
            return Result.error("规则不存在");
        }
        return Result.success("success", rule);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 构建安全的用户信息（不包含密码等敏感字段）
     */
    private Map<String, Object> buildSafeUserInfo(User user) {
        Map<String, Object> safeInfo = new HashMap<>();
        safeInfo.put("id", user.getId());
        safeInfo.put("username", user.getUsername());
        safeInfo.put("nickname", user.getNickname());
        safeInfo.put("avatar", user.getAvatar());
        safeInfo.put("phone", user.getPhone());
        safeInfo.put("email", user.getEmail());
        safeInfo.put("studentId", user.getStudentId());
        safeInfo.put("creditScore", user.getCreditScore());
        safeInfo.put("balance", user.getBalance());
        safeInfo.put("points", user.getPoints());
        safeInfo.put("status", user.getStatus());
        safeInfo.put("violationCount", user.getViolationCount());
        safeInfo.put("lastLoginTime", user.getLastLoginTime());
        return safeInfo;
    }

    /**
     * 构建规则列表（与安卓端RulesController保持一致）
     */
    private List<Map<String, Object>> buildRules() {
        List<Map<String, Object>> rules = new ArrayList<>();

        rules.add(buildRule(1L, "booking", "预约规则", "每位用户每天最多可预约2个座位，预约时间段为开馆至闭馆时间。请在预约成功后30分钟内到达座位签到，否则预约将自动取消。", "主要规则", 1));
        rules.add(buildRule(2L, "checkin", "签到规则", "预约成功后，请在预约时间开始后30分钟内完成签到。签到方式为扫描座位二维码或在APP内点击签到按钮。未按时签到将被记录为违规。", "主要规则", 2));
        rules.add(buildRule(3L, "leave", "离开规则", "暂时离开座位不超过30分钟无需操作。离开超过30分钟需要在APP内申请暂离，每天最多申请3次暂离，每次暂离不超过2小时。", "主要规则", 3));
        rules.add(buildRule(4L, "violation", "违规处理", "累计3次违规将被禁止预约7天，累计5次违规将被禁止预约30天。违规行为包括：未按时签到、恶意占座、转让座位等。", "主要规则", 4));
        rules.add(buildRule(5L, "civilized", "文明使用", "请保持座位及周边环境整洁，不得在学习区域大声喧哗。请勿长时间占用座位而不学习。离开时请带走个人物品。", "主要规则", 5));

        rules.add(buildRule(6L, "faq", "如何预约座位？", "点击首页的【预约座位】功能，选择日期、时间段和座位，确认后即可完成预约。建议提前一天预约。", "常见问题", 6));
        rules.add(buildRule(7L, "faq", "预约后可以取消吗？", "可以。在预约时间开始前2小时可以免费取消预约。2小时内取消或未签到将被记录为违规。", "常见问题", 7));
        rules.add(buildRule(8L, "faq", "忘记签到怎么办？", "预约时间开始后30分钟内都可以签到。超过30分钟未签到，系统将自动取消预约并记录为违规。", "常见问题", 8));
        rules.add(buildRule(9L, "faq", "可以帮朋友预约吗？", "不可以。每个账号只能为本人预约，不得转让或代他人预约。发现此类行为将被记录违规。", "常见问题", 9));
        rules.add(buildRule(10L, "faq", "座位可以续约吗？", "当天使用的座位可以在APP内申请续约，续约需在当前预约结束前30分钟内操作，续约时长最多4小时。", "常见问题", 10));
        rules.add(buildRule(11L, "faq", "违规记录可以申诉吗？", "可以。在【更多】页面的【违规记录】中找到对应记录，点击【申诉】按钮提交申诉理由，管理员会在24小时内处理。", "常见问题", 11));

        return rules;
    }

    private Map<String, Object> buildRule(Long id, String category, String title, String content, String categoryLabel, int priority) {
        Map<String, Object> rule = new LinkedHashMap<>();
        rule.put("id", id);
        rule.put("category", category);
        rule.put("categoryLabel", categoryLabel);
        rule.put("title", title);
        rule.put("content", content);
        rule.put("priority", priority);
        rule.put("type", "faq".equals(category) ? "faq" : "rule");
        return rule;
    }
}
