package com.ycyu.istudyspotbackend.mapper;

import com.ycyu.istudyspotbackend.entity.StudyRoomGuide;
import com.ycyu.istudyspotbackend.entity.StudyRoomGuideDetail;
import com.ycyu.istudyspotbackend.entity.StudyRoomGuideSummary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface StudyRoomGuideMapper {

    @Select("""
            SELECT
                sr.id AS study_room_id,
                sr.name AS study_room_name,
                sr.address,
                TIME_FORMAT(sr.open_time, '%H:%i:%s') AS open_time,
                TIME_FORMAT(sr.close_time, '%H:%i:%s') AS close_time,
                sr.description
            FROM study_room sr
            INNER JOIN study_room_guide srg ON srg.study_room_id = sr.id
            WHERE sr.status = 1
            ORDER BY sr.id
            """)
    List<StudyRoomGuideSummary> findGuideSummaries();

    @Select("""
            SELECT
                sr.id AS study_room_id,
                sr.name AS study_room_name,
                sr.address,
                TIME_FORMAT(sr.open_time, '%H:%i:%s') AS open_time,
                TIME_FORMAT(sr.close_time, '%H:%i:%s') AS close_time,
                sr.description,
                srg.contact_info,
                srg.learning_areas,
                srg.convenience_facilities,
                srg.transportation_guide
            FROM study_room sr
            INNER JOIN study_room_guide srg ON srg.study_room_id = sr.id
            WHERE sr.id = #{studyRoomId} AND sr.status = 1
            """)
    StudyRoomGuideDetail findGuideDetailByStudyRoomId(Long studyRoomId);

    @Select("SELECT * FROM study_room_guide WHERE study_room_id = #{studyRoomId}")
    StudyRoomGuide findByStudyRoomId(Long studyRoomId);
}
