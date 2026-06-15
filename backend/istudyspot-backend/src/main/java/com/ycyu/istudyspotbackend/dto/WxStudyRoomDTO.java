package com.ycyu.istudyspotbackend.dto;

import com.ycyu.istudyspotbackend.entity.StudyRoom;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class WxStudyRoomDTO {
    private String id;
    private String name;
    private String description;
    private String location;
    private Integer floor;
    private Integer capacity;
    private LocalTime openTime;
    private LocalTime closeTime;
    private List<String> facilities;
    private String image;
    private String status;
    private List<WxRuleDTO> rules;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static WxStudyRoomDTO fromEntity(StudyRoom room) {
        WxStudyRoomDTO dto = new WxStudyRoomDTO();
        dto.setId(room.getId() != null ? room.getId().toString() : null);
        dto.setName(room.getName());
        dto.setDescription(room.getDescription());
        dto.setLocation(room.getAddress());
        dto.setFloor(room.getFloor());
        dto.setCapacity(room.getCapacity());
        dto.setOpenTime(room.getOpenTime());
        dto.setCloseTime(room.getCloseTime());
        if (room.getFacilities() != null && !room.getFacilities().isEmpty()) {
            dto.setFacilities(Arrays.asList(room.getFacilities().split(",")));
        } else {
            dto.setFacilities(Collections.emptyList());
        }
        dto.setImage(room.getImageUrl());
        if (room.getStatus() != null) {
            dto.setStatus(room.getStatus() == 1 ? "open" : "closed");
        }
        dto.setCreatedAt(room.getCreateTime());
        dto.setUpdatedAt(room.getUpdateTime());
        return dto;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Integer getFloor() { return floor; }
    public void setFloor(Integer floor) { this.floor = floor; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public LocalTime getOpenTime() { return openTime; }
    public void setOpenTime(LocalTime openTime) { this.openTime = openTime; }

    public LocalTime getCloseTime() { return closeTime; }
    public void setCloseTime(LocalTime closeTime) { this.closeTime = closeTime; }

    public List<String> getFacilities() { return facilities; }
    public void setFacilities(List<String> facilities) { this.facilities = facilities; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<WxRuleDTO> getRules() { return rules; }
    public void setRules(List<WxRuleDTO> rules) { this.rules = rules; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
