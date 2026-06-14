package com.ycyu.istudyspotbackend.service.impl;

import com.ycyu.istudyspotbackend.entity.StudyRoom;
import com.ycyu.istudyspotbackend.mapper.StudyRoomMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudyRoomServiceImplTest {

    @Mock
    private StudyRoomMapper studyRoomMapper;

    @InjectMocks
    private StudyRoomServiceImpl studyRoomService;

    private StudyRoom testStudyRoom;

    @BeforeEach
    void setUp() {
        testStudyRoom = new StudyRoom();
        testStudyRoom.setId(1L);
        testStudyRoom.setName("Quiet Room A");
        testStudyRoom.setStatus(1);
        testStudyRoom.setDescription("Silent study space");
    }

    @Test
    void testGetStudyRoomList() {
        List<StudyRoom> studyRooms = new ArrayList<>();
        studyRooms.add(testStudyRoom);
        when(studyRoomMapper.findAll()).thenReturn(studyRooms);

        Map<String, Object> result = studyRoomService.getStudyRoomList("1", null, "", 1, 20);

        assertNotNull(result);
        assertTrue(result.containsKey("list"));
        assertTrue(result.containsKey("total"));
        assertTrue(result.containsKey("page"));
        assertTrue(result.containsKey("pageSize"));
        assertEquals(1, result.get("total"));
        verify(studyRoomMapper, times(1)).findAll();
    }

    @Test
    void testGetStudyRoomListWithKeywordAndPagination() {
        StudyRoom secondRoom = new StudyRoom();
        secondRoom.setId(2L);
        secondRoom.setName("Quiet Room B");
        secondRoom.setStatus(1);

        List<StudyRoom> searchResult = new ArrayList<>();
        searchResult.add(testStudyRoom);
        searchResult.add(secondRoom);
        when(studyRoomMapper.search("Quiet")).thenReturn(searchResult);

        Map<String, Object> result = studyRoomService.getStudyRoomList("1", null, "Quiet", 2, 1);

        @SuppressWarnings("unchecked")
        List<StudyRoom> pageItems = (List<StudyRoom>) result.get("list");
        assertEquals(2, result.get("total"));
        assertEquals(2, result.get("page"));
        assertEquals(1, result.get("pageSize"));
        assertEquals(1, pageItems.size());
        assertEquals(2L, pageItems.get(0).getId());
        verify(studyRoomMapper, times(1)).search("Quiet");
    }

    @Test
    void testGetStudyRoomDetail() {
        when(studyRoomMapper.findById(1L)).thenReturn(testStudyRoom);

        StudyRoom studyRoom = studyRoomService.getStudyRoomDetail(1L);

        assertNotNull(studyRoom);
        assertEquals(1L, studyRoom.getId());
        assertEquals("Quiet Room A", studyRoom.getName());
        verify(studyRoomMapper, times(1)).findById(1L);
    }

    @Test
    void testGetStudyRoomDetailNotFound() {
        when(studyRoomMapper.findById(1L)).thenReturn(null);

        Exception exception = assertThrows(RuntimeException.class, () -> studyRoomService.getStudyRoomDetail(1L));

        assertEquals("自习室不存在", exception.getMessage());
        verify(studyRoomMapper, times(1)).findById(1L);
    }
}
