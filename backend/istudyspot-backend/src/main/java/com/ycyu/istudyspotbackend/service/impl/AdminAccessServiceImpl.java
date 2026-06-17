package com.ycyu.istudyspotbackend.service.impl;

import com.ycyu.istudyspotbackend.entity.User;
import com.ycyu.istudyspotbackend.mapper.UserMapper;
import com.ycyu.istudyspotbackend.service.AdminAccessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminAccessServiceImpl implements AdminAccessService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public void checkAdmin(Long userId) {
        User user = userMapper.findById(userId);
        if (user == null || !"admin".equals(user.getUsername())) {
            throw new RuntimeException("无权限访问管理员接口");
        }
    }
}
