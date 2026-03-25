package com.ycyu.istudyspotbackend.mapper;

import com.ycyu.istudyspotbackend.entity.User;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM user WHERE openid = #{openid}")
    User findByOpenid(String openid);

    @Insert("INSERT INTO user(openid, nickname, avatar_url, create_time) " +
            "VALUES(#{openid}, #{nickname}, #{avatarUrl}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Select("SELECT * FROM user WHERE id = #{id}")
    User findById(Long id);

    @Update("UPDATE user SET nickname = #{nickname}, avatar_url = #{avatarUrl}, " +
            "phone = #{phone}, update_time = NOW() WHERE id = #{id}")
    int update(User user);

    @Update("UPDATE user SET balance = balance + #{amount} WHERE id = #{id}")
    int addBalance(@Param("id") Long id, @Param("amount") BigDecimal amount);
}