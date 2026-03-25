package com.ycyu.istudyspotbackend.mapper;

import com.ycyu.istudyspotbackend.entity.Order;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper {

    @Insert("INSERT INTO `order`(order_no, user_id, seat_id, seat_number, room_id, room_name, " +
            "plan_start_time, plan_end_time, total_amount, status, create_time) " +
            "VALUES(#{orderNo}, #{userId}, #{seatId}, #{seatNumber}, #{roomId}, #{roomName}, " +
            "#{planStartTime}, #{planEndTime}, #{totalAmount}, #{status}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Order order);

    @Select("SELECT * FROM `order` WHERE id = #{id}")
    Order findById(Long id);

    @Select("SELECT * FROM `order` WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Order> findByUserId(Long userId);

    @Update("UPDATE `order` SET status = #{status}, pay_time = NOW(), pay_type = #{payType}, " +
            "pay_amount = #{payAmount}, update_time = NOW() WHERE id = #{id}")
    int updatePayStatus(@Param("id") Long id, @Param("status") Integer status,
                        @Param("payType") Integer payType, @Param("payAmount") BigDecimal payAmount);

    @Update("UPDATE `order` SET status = #{status}, actual_start_time = NOW(), update_time = NOW() " +
            "WHERE id = #{id}")
    int checkin(@Param("id") Long id, @Param("status") Integer status);

    @Update("UPDATE `order` SET status = #{status}, actual_end_time = NOW(), " +
            "total_hours = #{totalHours}, update_time = NOW() WHERE id = #{id}")
    int checkout(@Param("id") Long id, @Param("status") Integer status,
                 @Param("totalHours") BigDecimal totalHours);

    @Select("SELECT COUNT(*) FROM `order` WHERE seat_id = #{seatId} AND status IN (1,2,3) " +
            "AND ((plan_start_time <= #{endTime} AND plan_end_time > #{startTime}) " +
            "OR (plan_start_time < #{endTime} AND plan_end_time >= #{startTime}))")
    int checkTimeConflict(@Param("seatId") Long seatId,
                          @Param("startTime") LocalDateTime startTime,
                          @Param("endTime") LocalDateTime endTime);
}