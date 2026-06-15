package com.ycyu.istudyspotbackend.entity;

import java.time.LocalDateTime;

public class Card {

    private String uuid;
    private String userId;
    private String cardId;
    private LocalDateTime createTime;
    private Integer studyDuration;
    private String rarity;
    private String borderTheme;
    private String cardTheme;
    private String themeCategory;
    private String markdown;
    private String imageUrl;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public Integer getStudyDuration() {
        return studyDuration;
    }

    public void setStudyDuration(Integer studyDuration) {
        this.studyDuration = studyDuration;
    }

    public String getRarity() {
        return rarity;
    }

    public void setRarity(String rarity) {
        this.rarity = rarity;
    }

    public String getBorderTheme() {
        return borderTheme;
    }

    public void setBorderTheme(String borderTheme) {
        this.borderTheme = borderTheme;
    }

    public String getCardTheme() {
        return cardTheme;
    }

    public void setCardTheme(String cardTheme) {
        this.cardTheme = cardTheme;
    }

    public String getThemeCategory() {
        return themeCategory;
    }

    public void setThemeCategory(String themeCategory) {
        this.themeCategory = themeCategory;
    }

    public String getMarkdown() {
        return markdown;
    }

    public void setMarkdown(String markdown) {
        this.markdown = markdown;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}