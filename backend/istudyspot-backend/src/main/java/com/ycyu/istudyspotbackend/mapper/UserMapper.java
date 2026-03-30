package com.ycyu.istudyspotbackend.mapper;

import com.ycyu.istudyspotbackend.entity.User;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM user WHERE username = #{username}")
    User findByUsername(String username);

    @Select("SELECT * FROM user WHERE id = #{id}")
    User findById(Long id);

    @Insert("INSERT INTO user(username, password, nickname, avatar, phone, email, balance, create_time) " +
            "VALUES(#{username}, #{password}, #{nickname}, #{avatar}, #{phone}, #{email}, 0, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Update("UPDATE user SET nickname = #{nickname}, avatar = #{avatar}, phone = #{phone}, " +
            "email = #{email}, update_time = NOW() WHERE id = #{id}")
    int update(User user);

    @Update("UPDATE user SET password = #{password} WHERE id = #{id}")
    int updatePassword(@Param("id") Long id, @Param("password") String password);

    @Update("UPDATE user SET last_login_time = NOW() WHERE id = #{id}")
    int updateLastLoginTime(Long id);

    @Update("UPDATE user SET balance = balance + #{amount} WHERE id = #{id}")
    int addBalance(@Param("id") Long id, @Param("amount") BigDecimal amount);
}