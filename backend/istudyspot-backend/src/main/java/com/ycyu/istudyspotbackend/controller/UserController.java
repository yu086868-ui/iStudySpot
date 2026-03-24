package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.entity.Result;
import com.ycyu.istudyspotbackend.entity.User;
import com.ycyu.istudyspotbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        String code = params.get("code");
        Map<String, Object> result = userService.wxLogin(code);
        return Result.success(result);
    }

    @GetMapping("/info")
    public Result<User> getUserInfo(@RequestAttribute Long userId) {
        User user = userService.getUserInfo(userId);
        return Result.success(user);
    }

    @PutMapping("/update")
    public Result<User> updateUserInfo(@RequestBody User user, @RequestAttribute Long userId) {
        user.setId(userId);
        User updated = userService.updateUserInfo(user);
        return Result.success(updated);
    }
}