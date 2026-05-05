package com.ycyu.istudyspotbackend.mapper;

import com.ycyu.istudyspotbackend.entity.Order;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper {

    @Insert("INSERT INTO `order`(order_no, user_id, seat_id, room_id, room_name, seat_number, " +
            "plan_start_time, plan_end_time, total_amount, status, create_time) " +
            "VALUES(#{orderNo}, #{userId}, #{seatId}, #{roomId}, #{roomName}, #{seatNumber}, " +
            "#{startTime}, #{endTime}, #{totalAmount}, #{status}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Order order);

    @Select("SELECT * FROM `order` WHERE id = #{id}")
    Order findById(Long id);

    @Select("SELECT * FROM `order` WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<Order> findByUserId(Long userId);

    @Select("SELECT * FROM `order` WHERE user_id = #{userId} AND status = #{status} ORDER BY created_at DESC")
    List<Order> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);

    @Update("UPDATE `order` SET status = #{status}, updated_at = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    @Update("UPDATE `order` SET status = '2', updated_at = NOW() WHERE id = #{id}")
    int markAsPaid(Long id);

    @Update("UPDATE `order` SET status = '3', actual_start_time = NOW(), updated_at = NOW() WHERE id = #{id}")
    int checkin(Long id);

    @Update("UPDATE `order` SET status = '4', actual_end_time = NOW(), " +
            "actual_duration = #{duration}, actual_price = #{price}, updated_at = NOW() WHERE id = #{id}")
    int checkout(@Param("id") Long id, @Param("duration") Integer duration, @Param("price") java.math.BigDecimal price);

    @Select("SELECT COUNT(*) FROM `order` WHERE seat_id = #{seatId} AND status IN (2, 3) " +
            "AND ((plan_start_time <= #{endTime} AND plan_end_time > #{startTime}) " +
            "OR (plan_start_time < #{endTime} AND plan_end_time >= #{startTime}))")
    int checkTimeConflict(@Param("seatId") Long seatId,
                          @Param("startTime") LocalDateTime startTime,
                          @Param("endTime") LocalDateTime endTime);
}