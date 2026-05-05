package com.ycyu.istudyspotbackend.service;

import com.ycyu.istudyspotbackend.entity.User;

public interface UserService {
    User getUserInfo(Long userId);
    User updateUserInfo(User user);
    void updatePassword(Long userId, String oldPassword, String newPassword);
}