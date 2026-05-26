package com.ycyu.istudyspotbackend.mapper;

import com.ycyu.istudyspotbackend.entity.Card;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CardMapper {

    void insertCard(Card card);

    Card selectCardByUuid(@Param("uuid") String uuid);

    List<Card> selectCardsByUserId(@Param("userId") String userId);

    void updateCard(Card card);

    void deleteCard(@Param("uuid") String uuid);
}