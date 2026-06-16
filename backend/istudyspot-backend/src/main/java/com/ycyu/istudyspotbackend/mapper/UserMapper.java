package com.ycyu.istudyspotbackend.mapper;

import com.ycyu.istudyspotbackend.entity.User;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;

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

    @Select("""
            <script>
            SELECT *
            FROM user
            <where>
                <if test="status != null">
                    status = #{status}
                </if>
                <if test="keyword != null and keyword != ''">
                    <if test="status != null">AND</if>
                    (
                        username LIKE CONCAT('%', #{keyword}, '%')
                        OR nickname LIKE CONCAT('%', #{keyword}, '%')
                        OR phone LIKE CONCAT('%', #{keyword}, '%')
                        OR email LIKE CONCAT('%', #{keyword}, '%')
                        OR student_id LIKE CONCAT('%', #{keyword}, '%')
                    )
                </if>
            </where>
            ORDER BY create_time DESC, id DESC
            </script>
            """)
    List<User> findForAdmin(@Param("keyword") String keyword, @Param("status") Integer status);
}
