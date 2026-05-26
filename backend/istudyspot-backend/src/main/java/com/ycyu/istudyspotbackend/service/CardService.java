package com.ycyu.istudyspotbackend.service;

import com.ycyu.istudyspotbackend.entity.Card;

import java.util.List;
import java.util.Map;

public interface CardService {

    Card generateCard(String userId, Integer studyDuration);

    Card getCardByUuid(String uuid);

    List<Card> getCardsByUserId(String userId);

    Map<String, Object> generateCardData(String rarity);
}