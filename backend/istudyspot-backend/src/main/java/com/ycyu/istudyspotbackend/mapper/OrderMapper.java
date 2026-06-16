package com.ycyu.istudyspotbackend.mapper;

import com.ycyu.istudyspotbackend.entity.Order;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper {

    @Insert("INSERT INTO order(order_no, user_id, seat_id, room_id, study_room_name, room_name, seat_number, seat_position, " +
            "plan_start_time, plan_end_time, total_price, total_amount, status, create_time) " +
            "VALUES(#{orderNo}, #{userId}, #{seatId}, #{roomId}, #{studyRoomName}, #{roomName}, #{seatNumber}, #{seatPosition}, " +
            "#{planStartTime}, #{planEndTime}, #{totalPrice}, #{totalAmount}, #{status}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Order order);

    @Select("SELECT * FROM order WHERE id = #{id}")
    Order findById(Long id);

    @Select("SELECT * FROM order WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Order> findByUserId(Long userId);

    @Select("SELECT * FROM order WHERE user_id = #{userId} AND status = #{status} ORDER BY create_time DESC")
    List<Order> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);

    @Update("UPDATE order SET status = #{status}, updated_at = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    @Update("UPDATE order SET status = 'paid', updated_at = NOW() WHERE id = #{id}")
    int markAsPaid(Long id);

    @Update("UPDATE order SET status = 'in_use', actual_start_time = NOW(), checkin_time = NOW(), updated_at = NOW() WHERE id = #{id}")
    int checkin(Long id);

    @Update("UPDATE order SET status = 'completed', actual_end_time = NOW(), checkout_time = NOW(), " +
            "actual_duration = #{duration}, actual_price = #{price}, updated_at = NOW() WHERE id = #{id}")
    int checkout(@Param("id") Long id, @Param("duration") Integer duration, @Param("price") java.math.BigDecimal price);

    @Select("SELECT COUNT(*) FROM order WHERE seat_id = #{seatId} AND status IN ('paid', 'in_use') " +
            "AND ((plan_start_time <= #{endTime} AND plan_end_time > #{startTime}) " +
            "OR (plan_start_time < #{endTime} AND plan_end_time >= #{startTime}))")
    int checkTimeConflict(@Param("seatId") Long seatId,
                          @Param("startTime") LocalDateTime startTime,
                          @Param("endTime") LocalDateTime endTime);

    @Update("UPDATE order SET plan_end_time = #{planEndTime}, total_price = #{totalPrice}, total_amount = #{totalAmount}, updated_at = NOW() WHERE id = #{id}")
    int updateRenew(@Param("id") Long id, @Param("planEndTime") java.time.LocalDateTime planEndTime, @Param("totalPrice") java.math.BigDecimal totalPrice, @Param("totalAmount") java.math.BigDecimal totalAmount);

    @Select("SELECT * FROM order WHERE room_id = #{roomId} AND status IN ('pending', 'paid', 'in_use')")
    List<Order> findActiveByRoomId(@Param("roomId") Long roomId);

    @Select("SELECT * FROM order WHERE user_id = #{userId} AND status IN ('in_use', 'completed') ORDER BY create_time DESC")
    List<Order> findCheckinRecordsByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM order WHERE user_id = #{userId} AND status = 'in_use' LIMIT 1")
    Order findCurrentCheckinByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM order WHERE user_id = #{userId} AND status IN ('pending', 'paid', 'in_use', 'completed') ORDER BY create_time DESC")
    List<Order> findByUserIdWithCheckin(@Param("userId") Long userId);
}
