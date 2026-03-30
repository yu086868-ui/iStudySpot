package com.ycyu.istudyspotbackend.entity;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class StudyRoom {
    private Long id;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private LocalTime openTime;
    private LocalTime closeTime;
    private String description;
    private String rules;
    private String imageUrl;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // Getter and Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public LocalTime getOpenTime() { return openTime; }
    public void setOpenTime(LocalTime openTime) { this.openTime = openTime; }

    public LocalTime getCloseTime() { return closeTime; }
    public void setCloseTime(LocalTime closeTime) { this.closeTime = closeTime; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRules() { return rules; }
    public void setRules(String rules) { this.rules = rules; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}