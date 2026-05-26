package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.entity.Result;
import com.ycyu.istudyspotbackend.entity.User;
import com.ycyu.istudyspotbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users/me")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public Result<Map<String, Object>> getUserInfo(@RequestAttribute Long userId) {
        try {
            User user = userService.getUserInfo(userId);
            Map<String, Object> safeInfo = new HashMap<>();
            safeInfo.put("id", user.getId());
            safeInfo.put("username", user.getUsername());
            safeInfo.put("nickname", user.getNickname());
            safeInfo.put("avatar", user.getAvatar());
            safeInfo.put("phone", user.getPhone());
            safeInfo.put("email", user.getEmail());
            safeInfo.put("studentId", user.getStudentId());
            safeInfo.put("creditScore", user.getCreditScore());
            safeInfo.put("balance", user.getBalance());
            safeInfo.put("points", user.getPoints());
            return Result.success("获取成功", safeInfo);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping
    public Result<Map<String, Object>> updateUserInfo(@RequestBody Map<String, Object> params, @RequestAttribute Long userId) {
        try {
            User user = new User();
            user.setId(userId);
            if (params.get("nickname") != null) {
                user.setNickname((String) params.get("nickname"));
            }
            if (params.get("avatar") != null) {
                user.setAvatar((String) params.get("avatar"));
            }
            if (params.get("phone") != null) {
                user.setPhone((String) params.get("phone"));
            }
            if (params.get("email") != null) {
                user.setEmail((String) params.get("email"));
            }
            User updated = userService.updateUserInfo(user);
            Map<String, Object> safeInfo = new HashMap<>();
            safeInfo.put("id", updated.getId());
            safeInfo.put("username", updated.getUsername());
            safeInfo.put("nickname", updated.getNickname());
            safeInfo.put("avatar", updated.getAvatar());
            safeInfo.put("phone", updated.getPhone());
            safeInfo.put("email", updated.getEmail());
            safeInfo.put("studentId", updated.getStudentId());
            safeInfo.put("creditScore", updated.getCreditScore());
            safeInfo.put("balance", updated.getBalance());
            safeInfo.put("points", updated.getPoints());
            return Result.success("更新成功", safeInfo);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/password")
    public Result<Void> updatePassword(
            @RequestBody Map<String, String> params,
            @RequestAttribute Long userId) {
        try {
            String oldPassword = params.get("oldPassword");
            String newPassword = params.get("newPassword");
            userService.updatePassword(userId, oldPassword, newPassword);
            return Result.success("密码修改成功", null);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}