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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WxUserServiceImplTest {

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

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setOpenId("test-openid");
        testUser.setNickname("微信用户");
        testUser.setAvatar("");
        testUser.setStatus(1);
        testUser.setCreditScore(100);
    }

    @Test
    void testWxLoginNewUser() {
        when(wxService.getOpenIdByCode("test_code")).thenReturn("test-openid");
        when(userMapper.findByOpenId("test-openid")).thenReturn(null);
        when(userMapper.insertWxUser(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return 1;
        });
        when(jwtUtils.generateToken(1L)).thenReturn("test-jwt-token");

        Map<String, Object> result = wxUserService.wxLogin("test_code");

        assertNotNull(result);
        assertTrue((Boolean) result.get("isNewUser"));
        assertNotNull(result.get("user"));
        assertEquals("test-jwt-token", result.get("token"));

        verify(wxService, times(1)).getOpenIdByCode("test_code");
        verify(userMapper, times(1)).findByOpenId("test-openid");
        verify(userMapper, times(1)).insertWxUser(any(User.class));
        verify(jwtUtils, times(1)).generateToken(1L);
    }

    @Test
    void testWxLoginExistingUser() {
        when(wxService.getOpenIdByCode("test_code")).thenReturn("test-openid");
        when(userMapper.findByOpenId("test-openid")).thenReturn(testUser);
        when(userMapper.updateLastLoginTime(1L)).thenReturn(1);
        when(jwtUtils.generateToken(1L)).thenReturn("test-jwt-token");

        Map<String, Object> result = wxUserService.wxLogin("test_code");

        assertNotNull(result);
        assertFalse((Boolean) result.get("isNewUser"));
        assertEquals("test-jwt-token", result.get("token"));

        verify(wxService, times(1)).getOpenIdByCode("test_code");
        verify(userMapper, times(1)).findByOpenId("test-openid");
        verify(userMapper, times(1)).updateLastLoginTime(1L);
        verify(userMapper, never()).insertWxUser(any(User.class));
    }

    @Test
    void testWxLoginTestMode() {
        when(wxService.getOpenIdByCode("test_myopenid")).thenReturn("myopenid");
        when(userMapper.findByOpenId("myopenid")).thenReturn(testUser);
        when(userMapper.updateLastLoginTime(1L)).thenReturn(1);
        when(jwtUtils.generateToken(1L)).thenReturn("test-jwt-token");

        Map<String, Object> result = wxUserService.wxLogin("test_myopenid");

        assertNotNull(result);
        assertEquals("test-jwt-token", result.get("token"));
    }

    @Test
    void testGetUserProfile() {
        when(userMapper.findById(1L)).thenReturn(testUser);

        User user = wxUserService.getUserProfile(1L);

        assertNotNull(user);
        assertEquals(1L, user.getId());
        assertEquals("微信用户", user.getNickname());

        verify(userMapper, times(1)).findById(1L);
    }

    @Test
    void testGetUserProfileNotFound() {
        when(userMapper.findById(999L)).thenReturn(null);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            wxUserService.getUserProfile(999L);
        });

        assertEquals("用户不存在", exception.getMessage());
        verify(userMapper, times(1)).findById(999L);
    }

    @Test
    void testUpdateUserProfile() {
        when(userMapper.findById(1L)).thenReturn(testUser);
        when(userMapper.update(any(User.class))).thenReturn(1);

        testUser.setNickname("新昵称");
        User updatedUser = wxUserService.updateUserProfile(testUser);

        assertNotNull(updatedUser);
        assertEquals("新昵称", updatedUser.getNickname());

        verify(userMapper, times(2)).findById(1L);
        verify(userMapper, times(1)).update(any(User.class));
    }

    @Test
    void testUpdateUserProfileNotFound() {
        when(userMapper.findById(999L)).thenReturn(null);

        User unknownUser = new User();
        unknownUser.setId(999L);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            wxUserService.updateUserProfile(unknownUser);
        });

        assertEquals("用户不存在", exception.getMessage());
        verify(userMapper, never()).update(any(User.class));
    }

    @Test
    void testUpdateAvatar() {
        when(userMapper.update(any(User.class))).thenReturn(1);

        String avatarUrl = wxUserService.updateAvatar(1L, "https://example.com/avatar.png");

        assertEquals("https://example.com/avatar.png", avatarUrl);
        verify(userMapper, times(1)).update(any(User.class));
    }

    @Test
    void testGetUserHomeInfo() {
        when(userMapper.findById(1L)).thenReturn(testUser);
        when(orderMapper.findByUserId(1L)).thenReturn(Collections.emptyList());
        when(orderMapper.findCheckinRecordsByUserId(1L)).thenReturn(Collections.emptyList());

        Map<String, Object> result = wxUserService.getUserHomeInfo(1L);

        assertNotNull(result);
        assertNotNull(result.get("user"));
        assertEquals(0, result.get("reservationCount"));
        assertEquals(0, result.get("studyHours"));
        assertEquals(100, result.get("creditScore"));

        verify(userMapper, times(1)).findById(1L);
    }

    @Test
    void testGetUserHomeInfoWithOrders() {
        Order order = new Order();
        order.setPlanStartTime(LocalDateTime.now().minusHours(2));
        order.setPlanEndTime(LocalDateTime.now().minusHours(1));
        order.setActualStartTime(LocalDateTime.now().minusHours(2));
        order.setActualEndTime(LocalDateTime.now().minusHours(1));

        when(userMapper.findById(1L)).thenReturn(testUser);
        when(orderMapper.findByUserId(1L)).thenReturn(Arrays.asList(order));
        when(orderMapper.findCheckinRecordsByUserId(1L)).thenReturn(Arrays.asList(order));

        Map<String, Object> result = wxUserService.getUserHomeInfo(1L);

        assertNotNull(result);
        assertEquals(1, result.get("reservationCount"));
        assertEquals(1, result.get("studyHours"));
    }

    @Test
    void testGetUserHomeInfoUserNotFound() {
        when(userMapper.findById(999L)).thenReturn(null);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            wxUserService.getUserHomeInfo(999L);
        });

        assertEquals("用户不存在", exception.getMessage());
    }
}