package com.ycyu.istudyspotbackend.service.impl;

import com.ycyu.istudyspotbackend.entity.User;
import com.ycyu.istudyspotbackend.mapper.UserMapper;
import com.ycyu.istudyspotbackend.service.UserService;
import com.ycyu.istudyspotbackend.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public Map<String, Object> wxLogin(String code) {
        // 简化版：实际需要调用微信接口获取openid
        String openid = "mock_openid_" + UUID.randomUUID().toString().substring(0, 8);

        User user = userMapper.findByOpenid(openid);
        if (user == null) {
            user = new User();
            user.setOpenid(openid);
            user.setNickname("微信用户");
            user.setAvatarUrl("https://example.com/default.jpg");
            userMapper.insert(user);
        }

        user.setLastLoginTime(LocalDateTime.now());
        userMapper.update(user);

        String token = jwtUtils.generateToken(user.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userInfo", user);
        return result;
    }

    @Override
    public User getUserInfo(Long userId) {
        return userMapper.findById(userId);
    }

    @Override
    public User updateUserInfo(User user) {
        userMapper.update(user);
        return userMapper.findById(user.getId());
    }
}