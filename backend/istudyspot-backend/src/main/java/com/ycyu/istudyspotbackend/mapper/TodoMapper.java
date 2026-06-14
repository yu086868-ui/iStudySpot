package com.ycyu.istudyspotbackend.mapper;

import com.ycyu.istudyspotbackend.entity.Todo;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface TodoMapper {

    @Insert("INSERT INTO todo(user_id, title, priority, status, due_time, order_id, created_at, updated_at) " +
            "VALUES(#{userId}, #{title}, #{priority}, #{status}, #{dueTime}, #{orderId}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Todo todo);

    @Select("SELECT * FROM todo WHERE id = #{id}")
    Todo findById(Long id);

    @Select("SELECT * FROM todo WHERE user_id = #{userId} ORDER BY " +
            "CASE status WHEN 'pending' THEN 0 ELSE 1 END, " +
            "priority ASC, created_at DESC")
    List<Todo> findByUserId(Long userId);

    @Select("SELECT * FROM todo WHERE user_id = #{userId} AND status = #{status} ORDER BY priority ASC, created_at DESC")
    List<Todo> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);

    @Update("UPDATE todo SET title = #{title}, priority = #{priority}, due_time = #{dueTime}, " +
            "order_id = #{orderId}, updated_at = NOW() WHERE id = #{id}")
    int update(Todo todo);

    @Update("UPDATE todo SET status = #{status}, completed_at = #{completedAt}, updated_at = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status, @Param("completedAt") LocalDateTime completedAt);

    @Delete("DELETE FROM todo WHERE id = #{id}")
    int deleteById(Long id);
}
