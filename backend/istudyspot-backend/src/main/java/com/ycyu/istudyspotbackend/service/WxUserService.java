package com.ycyu.istudyspotbackend.service;

import com.ycyu.istudyspotbackend.entity.User;

import java.util.Map;

public interface WxUserService {
    /**
     * 微信登录
     * @param code 微信登录临时code
     * @return 登录结果
     */
    Map<String, Object> wxLogin(String code);

    /**
     * 获取用户信息
     * @param userId 用户ID
     * @return 用户信息
     */
    User getUserProfile(Long userId);

    /**
     * 更新用户信息
     * @param user 用户信息
     * @return 更新后的用户信息
     */
    User updateUserProfile(User user);

    /**
     * 更新用户头像
     * @param userId 用户ID
     * @param avatarUrl 头像URL
     * @return 头像URL
     */
    String updateAvatar(Long userId, String avatarUrl);

    /**
     * 获取用户首页信息
     * @param userId 用户ID
     * @return 首页信息
     */
    Map<String, Object> getUserHomeInfo(Long userId);
}
