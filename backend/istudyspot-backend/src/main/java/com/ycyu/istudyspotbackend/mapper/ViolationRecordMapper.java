package com.ycyu.istudyspotbackend.mapper;

import com.ycyu.istudyspotbackend.entity.ViolationRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ViolationRecordMapper {

    @Insert("INSERT INTO violation_record (user_id, type, description, related_order_id, status, created_at) " +
            "VALUES (#{userId}, #{type}, #{description}, #{relatedOrderId}, #{status}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ViolationRecord record);

    @Select("SELECT * FROM violation_record WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<ViolationRecord> findByUserId(Long userId);

    @Select("SELECT * FROM violation_record WHERE id = #{id}")
    ViolationRecord findById(Long id);

    @Update("UPDATE violation_record SET appeal_reason = #{appealReason}, appeal_time = NOW(), status = 'appealing' WHERE id = #{id}")
    int submitAppeal(@Param("id") Long id, @Param("appealReason") String appealReason);

    @Select("SELECT COUNT(*) FROM violation_record WHERE user_id = #{userId}")
    int countByUserId(Long userId);
}
