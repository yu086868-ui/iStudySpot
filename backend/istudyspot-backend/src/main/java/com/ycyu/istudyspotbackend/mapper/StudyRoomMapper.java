package com.ycyu.istudyspotbackend.mapper;

import com.ycyu.istudyspotbackend.entity.StudyRoom;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface StudyRoomMapper {

    @Select("SELECT * FROM study_room WHERE status = 1 ORDER BY id")
    List<StudyRoom> findAll();

    @Select("SELECT * FROM study_room WHERE id = #{id}")
    StudyRoom findById(Long id);

    @Select("SELECT COUNT(*) FROM study_room WHERE status = 1")
    int count();

    @Select("SELECT * FROM study_room WHERE name LIKE CONCAT('%', #{keyword}, '%') AND status = 1")
    List<StudyRoom> search(@Param("keyword") String keyword);
}