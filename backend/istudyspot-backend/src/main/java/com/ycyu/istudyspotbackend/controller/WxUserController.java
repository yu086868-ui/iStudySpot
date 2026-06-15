package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.dto.WxLoginRequestDTO;
import com.ycyu.istudyspotbackend.entity.Result;
import com.ycyu.istudyspotbackend.entity.User;
import com.ycyu.istudyspotbackend.service.WxUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/wx/user")
public class WxUserController {

    @Autowired
    private WxUserService wxUserService;

    @Value("${upload.avatar-path:uploads/avatars}")
    private String avatarPath;

    /**
     * 微信小程序登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> wxLogin(@RequestBody WxLoginRequestDTO wxLoginRequestDTO) {
        try {
            if (wxLoginRequestDTO.getCode() == null || wxLoginRequestDTO.getCode().isEmpty()) {
                return Result.error("code不能为空");
            }
            Map<String, Object> result = wxUserService.wxLogin(wxLoginRequestDTO.getCode());
            return Result.success("登录成功", result);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/profile")
    public Result<Map<String, Object>> getUserProfile(@RequestAttribute Long userId) {
        try {
            User user = wxUserService.getUserProfile(userId);
            Map<String, Object> profile = new HashMap<>();
            profile.put("id", user.getId());
            profile.put("nickname", user.getNickname());
            profile.put("avatarUrl", user.getAvatar());
            profile.put("status", user.getStatus() == 1 ? "normal" : "disabled");
            profile.put("createdAt", user.getCreateTime());
            profile.put("updatedAt", user.getUpdateTime());
            return Result.success("success", profile);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 修改用户信息
     */
    @PutMapping("/profile")
    public Result<Void> updateUserProfile(@RequestBody Map<String, String> params, @RequestAttribute Long userId) {
        try {
            User user = new User();
            user.setId(userId);
            if (params.containsKey("nickname")) {
                user.setNickname(params.get("nickname"));
            }
            wxUserService.updateUserProfile(user);
            return Result.success("更新成功", null);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 修改头像
     */
    @PostMapping("/avatar")
    public Result<Map<String, Object>> updateAvatar(@RequestParam("file") MultipartFile file, @RequestAttribute Long userId) {
        try {
            if (file.isEmpty()) {
                return Result.error("文件不能为空");
            }

            // 创建上传目录（基于项目运行目录）
            Path uploadDir = Paths.get(System.getProperty("user.dir"), avatarPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString() + extension;

            // 保存文件
            File dest = new File(uploadDir.toFile(), fileName);
            file.transferTo(dest);

            // 生成访问URL
            String avatarUrl = "/uploads/avatars/" + fileName;

            // 更新数据库
            wxUserService.updateAvatar(userId, avatarUrl);

            Map<String, Object> result = new HashMap<>();
            result.put("avatarUrl", avatarUrl);
            return Result.success("上传成功", result);
        } catch (IOException e) {
            return Result.error("上传失败: " + e.getMessage());
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取用户首页信息
     */
    @GetMapping("/home")
    public Result<Map<String, Object>> getUserHomeInfo(@RequestAttribute Long userId) {
        try {
            Map<String, Object> result = wxUserService.getUserHomeInfo(userId);
            return Result.success("success", result);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
