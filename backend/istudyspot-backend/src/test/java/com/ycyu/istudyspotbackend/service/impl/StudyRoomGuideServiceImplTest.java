package com.ycyu.istudyspotbackend.service.impl;

import com.ycyu.istudyspotbackend.entity.StudyRoomGuideDetail;
import com.ycyu.istudyspotbackend.entity.StudyRoomGuideSummary;
import com.ycyu.istudyspotbackend.mapper.StudyRoomGuideMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudyRoomGuideServiceImplTest {

    @Mock
    private StudyRoomGuideMapper studyRoomGuideMapper;

    @InjectMocks
    private StudyRoomGuideServiceImpl studyRoomGuideService;

    @Test
    void testGetGuideList() {
        StudyRoomGuideSummary summary = new StudyRoomGuideSummary();
        summary.setStudyRoomId(1L);
        summary.setStudyRoomName("五道口店");

        when(studyRoomGuideMapper.findGuideSummaries()).thenReturn(List.of(summary));

        List<StudyRoomGuideSummary> result = studyRoomGuideService.getGuideList();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getStudyRoomId());
        verify(studyRoomGuideMapper, times(1)).findGuideSummaries();
    }

    @Test
    void testGetGuideDetail() {
        StudyRoomGuideDetail detail = new StudyRoomGuideDetail();
        detail.setStudyRoomId(1L);
        detail.setStudyRoomName("五道口店");

        when(studyRoomGuideMapper.findGuideDetailByStudyRoomId(1L)).thenReturn(detail);

        StudyRoomGuideDetail result = studyRoomGuideService.getGuideDetail(1L);

        assertEquals(1L, result.getStudyRoomId());
        assertEquals("五道口店", result.getStudyRoomName());
        verify(studyRoomGuideMapper, times(1)).findGuideDetailByStudyRoomId(1L);
    }

    @Test
    void testGetGuideDetailNotFound() {
        when(studyRoomGuideMapper.findGuideDetailByStudyRoomId(99L)).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> studyRoomGuideService.getGuideDetail(99L));

        assertEquals("场馆导览不存在", exception.getMessage());
        verify(studyRoomGuideMapper, times(1)).findGuideDetailByStudyRoomId(99L);
    }
}
