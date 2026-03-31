package com.ycyu.istudyspotbackend.mapper;

import com.ycyu.istudyspotbackend.entity.Order;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper {

    @Insert("INSERT INTO `order`(order_no, user_id, seat_id, room_id, study_room_name, seat_position, " +
            "start_time, end_time, total_price, status, created_at) " +
            "VALUES(#{orderNo}, #{userId}, #{seatId}, #{roomId}, #{studyRoomName}, #{seatPosition}, " +
            "#{startTime}, #{endTime}, #{totalPrice}, #{status}, NOW())")
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

    @Update("UPDATE `order` SET status = 'paid', updated_at = NOW() WHERE id = #{id}")
    int markAsPaid(Long id);

    @Update("UPDATE `order` SET status = 'in_use', checkin_time = NOW(), updated_at = NOW() WHERE id = #{id}")
    int checkin(Long id);

    @Update("UPDATE `order` SET status = 'completed', checkout_time = NOW(), " +
            "actual_duration = #{duration}, actual_price = #{price}, updated_at = NOW() WHERE id = #{id}")
    int checkout(@Param("id") Long id, @Param("duration") Integer duration, @Param("price") java.math.BigDecimal price);

    @Select("SELECT COUNT(*) FROM `order` WHERE seat_id = #{seatId} AND status IN ('pending', 'paid', 'in_use') " +
            "AND ((start_time <= #{endTime} AND end_time > #{startTime}) " +
            "OR (start_time < #{endTime} AND end_time >= #{startTime}))")
    int checkTimeConflict(@Param("seatId") Long seatId,
                          @Param("startTime") LocalDateTime startTime,
                          @Param("endTime") LocalDateTime endTime);
}