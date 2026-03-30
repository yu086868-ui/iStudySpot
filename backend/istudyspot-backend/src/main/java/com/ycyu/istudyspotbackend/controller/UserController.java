package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.entity.Result;
import com.ycyu.istudyspotbackend.entity.User;
import com.ycyu.istudyspotbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users/me")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public Result<User> getUserInfo(@RequestAttribute Long userId) {
        User user = userService.getUserInfo(userId);
        return Result.success("获取成功", user);
    }

    @PutMapping
    public Result<User> updateUserInfo(@RequestBody User user, @RequestAttribute Long userId) {
        user.setId(userId);
        User updated = userService.updateUserInfo(user);
        return Result.success("更新成功", updated);
    }

    @PutMapping("/password")
    public Result<Void> updatePassword(
            @RequestBody Map<String, String> params,
            @RequestAttribute Long userId) {
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        userService.updatePassword(userId, oldPassword, newPassword);
        return Result.success("密码修改成功", null);
    }
}