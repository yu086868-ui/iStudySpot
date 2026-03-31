package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.dto.LoginDTO;
import com.ycyu.istudyspotbackend.dto.RegisterDTO;
import com.ycyu.istudyspotbackend.entity.Result;
import com.ycyu.istudyspotbackend.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginDTO loginDTO) {
        try {
            Map<String, Object> result = authService.login(loginDTO.getUsername(), loginDTO.getPassword());
            return Result.success("登录成功", result);
        } catch (RuntimeException e) {
            return Result.unauthorized(e.getMessage());
        }
    }

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody RegisterDTO registerDTO) {
        try {
            Map<String, Object> result = authService.register(
                    registerDTO.getUsername(),
                    registerDTO.getPassword(),
                    registerDTO.getNickname()
            );
            return Result.created(result);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/refresh")
    public Result<Map<String, String>> refreshToken(@RequestAttribute Long userId) {
        String token = authService.refreshToken(userId);
        return Result.success(Map.of("token", token));
    }
}