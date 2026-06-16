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

    @Update("<script>" +
            "UPDATE user SET " +
            "<trim suffixOverrides=\",\">" +
            "<if test=\"nickname != null\">nickname = #{nickname},</if>" +
            "<if test=\"avatar != null\">avatar = #{avatar},</if>" +
            "<if test=\"phone != null\">phone = #{phone},</if>" +
            "<if test=\"email != null\">email = #{email},</if>" +
            "<if test=\"status != null\">status = #{status},</if>" +
            "<if test=\"creditScore != null\">credit_score = #{creditScore},</if>" +
            "<if test=\"violationCount != null\">violation_count = #{violationCount},</if>" +
            "update_time = NOW()" +
            "</trim>" +
            "WHERE id = #{id}" +
            "</script>")
    int update(User user);

    @Update("UPDATE user SET password = #{password} WHERE id = #{id}")
    int updatePassword(@Param("id") Long id, @Param("password") String password);

    @Update("UPDATE user SET last_login_time = NOW() WHERE id = #{id}")
    int updateLastLoginTime(Long id);

    @Update("UPDATE user SET balance = balance + #{amount} WHERE id = #{id}")
    int addBalance(@Param("id") Long id, @Param("amount") BigDecimal amount);


    @Insert("INSERT INTO user(openid, nickname, avatar, status, create_time) " +
            "VALUES(#{openId}, #{nickname}, #{avatar}, #{status}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertWxUser(User user);
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
