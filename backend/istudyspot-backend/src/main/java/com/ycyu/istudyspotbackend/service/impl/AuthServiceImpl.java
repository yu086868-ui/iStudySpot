package com.ycyu.istudyspotbackend.service.impl;

import com.ycyu.istudyspotbackend.entity.User;
import com.ycyu.istudyspotbackend.mapper.UserMapper;
import com.ycyu.istudyspotbackend.service.AuthService;
import com.ycyu.istudyspotbackend.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public Map<String, Object> login(String username, String password) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        String encryptedPassword = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!encryptedPassword.equals(user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        if (user.getStatus() != 1) {
            throw new RuntimeException("账号已被禁用");
        }

        userMapper.updateLastLoginTime(user.getId());

        String token = jwtUtils.generateToken(user.getId());
        String refreshToken = jwtUtils.generateRefreshToken(user.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("refreshToken", refreshToken);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId().toString());
        userInfo.put("username", user.getUsername());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("avatar", user.getAvatar());
        result.put("user", userInfo);

        return result;
    }

    @Override
    public Map<String, Object> register(String username, String password, String nickname, String phone, String studentId) {
        User existingUser = userMapper.findByUsername(username);
        if (existingUser != null) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(DigestUtils.md5DigestAsHex(password.getBytes()));
        user.setNickname(nickname);
        user.setPhone(phone);
        user.setStudentId(studentId);
        user.setAvatar("https://example.com/default-avatar.png");
        user.setCreditScore(100);
        user.setStatus(1);

        userMapper.insert(user);

        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getId().toString());

        return result;
    }

    @Override
    public Map<String, Object> refreshToken(String refreshToken) {
        Long userId = jwtUtils.getUserIdFromToken(refreshToken);
        if (userId == null) {
            throw new RuntimeException("无效的refreshToken");
        }

        String newToken = jwtUtils.generateToken(userId);
        String newRefreshToken = jwtUtils.generateRefreshToken(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("token", newToken);
        result.put("refreshToken", newRefreshToken);

        return result;
    }

    @Override
    public void logout(Long userId) {
        // 可以实现token黑名单机制
    }
}