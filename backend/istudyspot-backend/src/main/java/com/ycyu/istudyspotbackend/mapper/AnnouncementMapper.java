package com.ycyu.istudyspotbackend.mapper;

import com.ycyu.istudyspotbackend.entity.Announcement;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AnnouncementMapper {

    @Select("SELECT * FROM announcement ORDER BY publish_time DESC, id DESC")
    List<Announcement> findAll();

    @Select("SELECT * FROM announcement WHERE id = #{id}")
    Announcement findById(@Param("id") Long id);

    @Delete("DELETE FROM announcement WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
}
