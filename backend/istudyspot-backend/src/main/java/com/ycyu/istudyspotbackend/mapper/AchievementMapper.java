package com.ycyu.istudyspotbackend.mapper;

import com.ycyu.istudyspotbackend.entity.Achievement;
import com.ycyu.istudyspotbackend.entity.UserAchievement;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AchievementMapper {

    @Select("SELECT * FROM achievement ORDER BY id")
    List<Achievement> findAll();

    @Select("SELECT * FROM achievement WHERE code = #{code}")
    Achievement findByCode(String code);

    @Select("SELECT * FROM user_achievement WHERE user_id = #{userId}")
    List<UserAchievement> findByUserId(Long userId);

    @Select("SELECT COUNT(*) FROM user_achievement WHERE user_id = #{userId} AND achievement_code = #{code}")
    int countByUserIdAndCode(@Param("userId") Long userId, @Param("code") String code);

    @Insert("INSERT INTO user_achievement (user_id, achievement_code, unlocked_at) VALUES (#{userId}, #{achievementCode}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserAchievement userAchievement);
}
