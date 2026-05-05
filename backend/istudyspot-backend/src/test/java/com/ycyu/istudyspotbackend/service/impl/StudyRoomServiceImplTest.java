package com.ycyu.istudyspotbackend.service.impl;

import com.ycyu.istudyspotbackend.entity.StudyRoom;
import com.ycyu.istudyspotbackend.mapper.StudyRoomMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
public class StudyRoomServiceImplTest {

    @Mock
    private StudyRoomMapper studyRoomMapper;

    @InjectMocks
    private StudyRoomServiceImpl studyRoomService;

    private StudyRoom testStudyRoom;

    @BeforeEach
    void setUp() {
        testStudyRoom = new StudyRoom();
        testStudyRoom.setId(1L);
        testStudyRoom.setName("自习室1");
        testStudyRoom.setStatus(1);
        testStudyRoom.setDescription("安静的自习室");
    }

    @Test
    void testGetStudyRoomList() {
        // 模拟自习室列表查询
        List<StudyRoom> studyRooms = new ArrayList<>();
        studyRooms.add(testStudyRoom);
        when(studyRoomMapper.findAll()).thenReturn(studyRooms);
        when(studyRoomMapper.count()).thenReturn(1);

        // 测试获取自习室列表
        Map<String, Object> result = studyRoomService.getStudyRoomList("1", 1, "", 1, 20);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.containsKey("list"));
        assertTrue(result.containsKey("total"));
        assertTrue(result.containsKey("page"));
        assertTrue(result.containsKey("pageSize"));

        // 验证方法调用
        verify(studyRoomMapper, times(1)).findAll();
        verify(studyRoomMapper, times(1)).count();
    }

    @Test
    void testGetStudyRoomDetail() {
        // 模拟自习室详情查询
        when(studyRoomMapper.findById(1L)).thenReturn(testStudyRoom);

        // 测试获取自习室详情
        StudyRoom studyRoom = studyRoomService.getStudyRoomDetail(1L);

        // 验证结果
        assertNotNull(studyRoom);
        assertEquals(1L, studyRoom.getId());
        assertEquals("自习室1", studyRoom.getName());

        // 验证方法调用
        verify(studyRoomMapper, times(1)).findById(1L);
    }

    @Test
    void testGetStudyRoomDetailNotFound() {
        // 模拟自习室不存在
        when(studyRoomMapper.findById(1L)).thenReturn(null);

        // 测试获取自习室详情
        Exception exception = assertThrows(RuntimeException.class, () -> {
            studyRoomService.getStudyRoomDetail(1L);
        });

        assertEquals("自习室不存在", exception.getMessage());

        // 验证方法调用
        verify(studyRoomMapper, times(1)).findById(1L);
    }
}
