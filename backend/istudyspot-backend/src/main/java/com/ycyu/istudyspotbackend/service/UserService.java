package com.ycyu.istudyspotbackend.service;

import com.ycyu.istudyspotbackend.entity.User;

import java.util.Map;

public interface UserService {
    User getUserInfo(Long userId);
    Map<String, Object> getUserList(String keyword, Integer status, int page, int pageSize);
    User updateUserInfo(User user);
    void updatePassword(Long userId, String oldPassword, String newPassword);
}
