package com.ycyu.istudyspotbackend.mapper;

import com.ycyu.istudyspotbackend.entity.Announcement;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AnnouncementMapper {

    @Select("SELECT * FROM announcement WHERE status = 'published' ORDER BY publish_time DESC")
    List<Announcement> findAll();

    @Select("SELECT * FROM announcement WHERE id = #{id}")
    Announcement findById(Long id);

    @Select("SELECT * FROM announcement WHERE status = 'published' AND type = #{type} ORDER BY publish_time DESC")
    List<Announcement> findByType(@Param("type") String type);

    @Select("SELECT * FROM announcement WHERE status = 'published' AND priority = #{priority} ORDER BY publish_time DESC")
    List<Announcement> findByPriority(@Param("priority") String priority);
}
