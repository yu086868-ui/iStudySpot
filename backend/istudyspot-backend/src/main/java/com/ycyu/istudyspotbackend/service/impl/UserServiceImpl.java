package com.ycyu.istudyspotbackend.service.impl;

import com.ycyu.istudyspotbackend.entity.User;
import com.ycyu.istudyspotbackend.mapper.UserMapper;
import com.ycyu.istudyspotbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public User getUserInfo(Long userId) {
        return userMapper.findById(userId);
    }

    @Override
    public Map<String, Object> getUserList(String keyword, Integer status, int page, int pageSize) {
        List<User> users = userMapper.findForAdmin(keyword, status);
        int safePage = Math.max(page, 1);
        int safePageSize = Math.max(pageSize, 1);
        int fromIndex = Math.min((safePage - 1) * safePageSize, users.size());
        int toIndex = Math.min(fromIndex + safePageSize, users.size());

        Map<String, Object> result = new HashMap<>();
        result.put("list", users.subList(fromIndex, toIndex));
        result.put("total", users.size());
        result.put("page", safePage);
        result.put("pageSize", safePageSize);
        return result;
    }

    @Override
    public User updateUserInfo(User user) {
        userMapper.update(user);
        return userMapper.findById(user.getId());
    }

    @Override
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        String encryptedOld = DigestUtils.md5DigestAsHex(oldPassword.getBytes());
        if (!encryptedOld.equals(user.getPassword())) {
            throw new RuntimeException("旧密码错误");
        }

        String encryptedNew = DigestUtils.md5DigestAsHex(newPassword.getBytes());
        userMapper.updatePassword(userId, encryptedNew);
    }
}
