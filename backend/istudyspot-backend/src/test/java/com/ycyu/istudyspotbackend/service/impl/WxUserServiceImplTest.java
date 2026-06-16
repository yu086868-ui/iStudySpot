package com.ycyu.istudyspotbackend.service.impl;

import com.ycyu.istudyspotbackend.entity.Order;
import com.ycyu.istudyspotbackend.entity.User;
import com.ycyu.istudyspotbackend.mapper.OrderMapper;
import com.ycyu.istudyspotbackend.mapper.UserMapper;
import com.ycyu.istudyspotbackend.service.WxService;
import com.ycyu.istudyspotbackend.utils.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WxUserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private WxService wxService;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private WxUserServiceImpl wxUserService;

    @Test
    void testWxLogin_NewUser() {
        when(wxService.getOpenIdByCode(anyString())).thenReturn("openid-123");
        when(userMapper.findByOpenId(anyString())).thenReturn(null);
        when(jwtUtils.generateToken(any())).thenReturn("jwt-token");

        Map<String, Object> result = wxUserService.wxLogin("test_code");

        assertTrue((Boolean) result.get("isNewUser"));
        assertEquals("jwt-token", result.get("token"));
        assertNotNull(result.get("user"));
        verify(userMapper).insertWxUser(any(User.class));
    }

    @Test
    void testWxLogin_ExistingUser() {
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setNickname("老用户");
        existingUser.setAvatar("avatar.jpg");
        existingUser.setStatus(1);

        when(wxService.getOpenIdByCode(anyString())).thenReturn("openid-123");
        when(userMapper.findByOpenId(anyString())).thenReturn(existingUser);
        when(jwtUtils.generateToken(any())).thenReturn("jwt-token");

        Map<String, Object> result = wxUserService.wxLogin("test_code");

        assertFalse((Boolean) result.get("isNewUser"));
        assertEquals("jwt-token", result.get("token"));
        verify(userMapper).updateLastLoginTime(anyLong());
    }

    @Test
    void testGetUserProfile_Success() {
        User user = new User();
        user.setId(1L);
        user.setNickname("测试用户");

        when(userMapper.findById(anyLong())).thenReturn(user);

        User result = wxUserService.getUserProfile(1L);

        assertEquals("测试用户", result.getNickname());
    }

    @Test
    void testGetUserProfile_NotFound() {
        when(userMapper.findById(anyLong())).thenReturn(null);

        assertThrows(RuntimeException.class, () -> wxUserService.getUserProfile(999L));
    }

    @Test
    void testUpdateUserProfile_Success() {
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setNickname("旧昵称");

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setNickname("新昵称");

        when(userMapper.findById(anyLong())).thenReturn(existingUser, updatedUser);

        User result = wxUserService.updateUserProfile(updatedUser);

        assertEquals("新昵称", result.getNickname());
        verify(userMapper).update(any(User.class));
    }

    @Test
    void testUpdateUserProfile_NotFound() {
        User user = new User();
        user.setId(999L);

        when(userMapper.findById(anyLong())).thenReturn(null);

        assertThrows(RuntimeException.class, () -> wxUserService.updateUserProfile(user));
    }

    @Test
    void testUpdateAvatar() {
        String avatarUrl = "/uploads/avatars/test.jpg";
        String result = wxUserService.updateAvatar(1L, avatarUrl);

        assertEquals(avatarUrl, result);
        verify(userMapper).update(any(User.class));
    }

    @Test
    void testGetUserHomeInfo_Success() {
        User user = new User();
        user.setId(1L);
        user.setNickname("测试用户");
        user.setAvatar("avatar.jpg");
        user.setCreditScore(100);

        Order order = new Order();
        order.setActualStartTime(LocalDateTime.now().minusHours(2));
        order.setActualEndTime(LocalDateTime.now());
        order.setPlanStartTime(LocalDateTime.now().minusHours(2));
        order.setPlanEndTime(LocalDateTime.now());

        when(userMapper.findById(anyLong())).thenReturn(user);
        when(orderMapper.findByUserId(anyLong())).thenReturn(List.of(order));
        when(orderMapper.findCheckinRecordsByUserId(anyLong())).thenReturn(List.of(order));

        Map<String, Object> result = wxUserService.getUserHomeInfo(1L);

        assertEquals(1, result.get("reservationCount"));
        assertNotNull(result.get("studyHours"));
        assertEquals(100, result.get("creditScore"));
    }

    @Test
    void testGetUserHomeInfo_UserNotFound() {
        when(userMapper.findById(anyLong())).thenReturn(null);

        assertThrows(RuntimeException.class, () -> wxUserService.getUserHomeInfo(999L));
    }
}