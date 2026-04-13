package com.ycyu.istudyspotbackend.service;

import java.util.Map;

public interface AuthService {
    Map<String, Object> login(String username, String password);
    Map<String, Object> register(String username, String password, String nickname, String phone, String studentId);
    Map<String, Object> refreshToken(String refreshToken);
    void logout(Long userId);
}