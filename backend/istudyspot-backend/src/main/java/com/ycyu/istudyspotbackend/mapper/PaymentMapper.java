package com.ycyu.istudyspotbackend.mapper;

import com.ycyu.istudyspotbackend.entity.Payment;
import org.apache.ibatis.annotations.*;

@Mapper
public interface PaymentMapper {

    @Insert("INSERT INTO payment(payment_no, order_id, user_id, amount, payment_method, status, payment_url, created_at) " +
            "VALUES(#{paymentNo}, #{orderId}, #{userId}, #{amount}, #{paymentMethod}, #{status}, #{paymentUrl}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Payment payment);

    @Select("SELECT * FROM payment WHERE id = #{id}")
    Payment findById(Long id);

    @Select("SELECT * FROM payment WHERE order_id = #{orderId}")
    Payment findByOrderId(Long orderId);

    @Update("UPDATE payment SET status = 'success', pay_time = NOW(), updated_at = NOW() WHERE id = #{id}")
    int markAsSuccess(Long id);

    @Update("UPDATE payment SET status = 'failed', updated_at = NOW() WHERE id = #{id}")
    int markAsFailed(Long id);
}