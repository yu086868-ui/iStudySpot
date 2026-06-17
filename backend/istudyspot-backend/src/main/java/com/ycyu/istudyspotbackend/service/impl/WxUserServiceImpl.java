package com.ycyu.istudyspotbackend.service.impl;

import com.ycyu.istudyspotbackend.entity.Order;
import com.ycyu.istudyspotbackend.entity.User;
import com.ycyu.istudyspotbackend.mapper.OrderMapper;
import com.ycyu.istudyspotbackend.mapper.UserMapper;
import com.ycyu.istudyspotbackend.service.WxService;
import com.ycyu.istudyspotbackend.service.WxUserService;
import com.ycyu.istudyspotbackend.utils.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WxUserServiceImpl implements WxUserService {

    private static final Logger logger = LoggerFactory.getLogger(WxUserServiceImpl.class);

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private WxService wxService;

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    @Transactional
    public Map<String, Object> wxLogin(String code) {
        // 1. 通过code获取openId
        String openId = wxService.getOpenIdByCode(code);

        // 2. 查询用户是否存在
        User user = userMapper.findByOpenId(openId);
        boolean isNewUser = false;

        // 3. 不存在则自动创建用户
        if (user == null) {
            isNewUser = true;
            user = new User();
            user.setOpenId(openId);
            user.setNickname("微信用户");
            user.setAvatar("");
            user.setStatus(1); // 1表示正常状态
            user.setCreditScore(100);
            user.setViolationCount(0);
            userMapper.insertWxUser(user);
            logger.info("新用户注册成功, openId: {}, userId: {}", openId, user.getId());
        } else {
            // 更新最后登录时间
            userMapper.updateLastLoginTime(user.getId());
            logger.info("用户登录成功, userId: {}", user.getId());
        }

        // 4. 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("isNewUser", isNewUser);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("avatarUrl", user.getAvatar());
        userInfo.put("status", user.getStatus() == 1 ? "normal" : "disabled");
        result.put("user", userInfo);

        // 5. 生成JWT token供后续请求使用
        String token = jwtUtils.generateToken(user.getId());
        result.put("token", token);

        return result;
    }

    @Override
    public User getUserProfile(Long userId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return user;
    }

    @Override
    @Transactional
    public User updateUserProfile(User user) {
        User existingUser = userMapper.findById(user.getId());
        if (existingUser == null) {
            throw new RuntimeException("用户不存在");
        }

        userMapper.update(user);
        return userMapper.findById(user.getId());
    }

    @Override
    @Transactional
    public String updateAvatar(Long userId, String avatarUrl) {
        User user = new User();
        user.setId(userId);
        user.setAvatar(avatarUrl);
        userMapper.update(user);
        return avatarUrl;
    }

    @Override
    public Map<String, Object> getUserHomeInfo(Long userId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 获取预约数量
        List<Order> orders = orderMapper.findByUserId(userId);
        int reservationCount = orders.size();

        // 计算学习时长（小时）
        List<Order> checkinRecords = orderMapper.findCheckinRecordsByUserId(userId);
        int totalMinutes = 0;
        for (Order order : checkinRecords) {
            LocalDateTime start = order.getActualStartTime() != null ? order.getActualStartTime() : order.getPlanStartTime();
            LocalDateTime end = order.getActualEndTime() != null ? order.getActualEndTime() : order.getPlanEndTime();
            if (start != null && end != null) {
                totalMinutes += (int) ChronoUnit.MINUTES.between(start, end);
            }
        }
        int studyHours = totalMinutes / 60;

        Map<String, Object> result = new HashMap<>();

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("avatarUrl", user.getAvatar());
        result.put("user", userInfo);

        result.put("reservationCount", reservationCount);
        result.put("studyHours", studyHours);
        result.put("creditScore", user.getCreditScore() != null ? user.getCreditScore() : 100);

        return result;
    }
}
