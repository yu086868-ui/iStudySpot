package com.ycyu.istudyspotbackend.mapper;

import com.ycyu.istudyspotbackend.entity.Announcement;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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
    @Select("SELECT * FROM announcement ORDER BY publish_time DESC, id DESC")
    List<Announcement> findAll();

    @Select("SELECT * FROM announcement WHERE id = #{id}")
    Announcement findById(@Param("id") Long id);

    @Delete("DELETE FROM announcement WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
}
