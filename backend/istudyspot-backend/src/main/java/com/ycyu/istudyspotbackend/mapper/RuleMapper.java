package com.ycyu.istudyspotbackend.mapper;

import com.ycyu.istudyspotbackend.entity.Rule;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RuleMapper {

    @Select("SELECT * FROM rule ORDER BY priority")
    List<Rule> findAll();

    @Select("SELECT * FROM rule WHERE study_room_id = #{studyRoomId} ORDER BY priority")
    List<Rule> findByStudyRoomId(@Param("studyRoomId") Long studyRoomId);

    @Select("SELECT * FROM rule WHERE id = #{id}")
    Rule findById(Long id);

    @Select("SELECT * FROM rule WHERE category = #{category} ORDER BY priority")
    List<Rule> findByCategory(@Param("category") String category);

    @Select("SELECT * FROM rule WHERE study_room_id IS NULL AND category = #{category} ORDER BY priority")
    List<Rule> findGeneralByCategory(@Param("category") String category);
}
