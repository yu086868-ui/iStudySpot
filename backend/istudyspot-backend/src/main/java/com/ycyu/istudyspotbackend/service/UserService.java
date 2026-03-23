package com.ycyu.istudyspotbackend.service;

import com.ycyu.istudyspotbackend.entity.User;
import java.util.Map;

public interface UserService {
    Map<String, Object> wxLogin(String code);
    User getUserInfo(Long userId);
    User updateUserInfo(User user);
}