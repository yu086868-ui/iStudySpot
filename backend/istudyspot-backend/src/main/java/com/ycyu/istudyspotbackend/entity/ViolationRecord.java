package com.ycyu.istudyspotbackend.entity;

import java.time.LocalDateTime;

public class ViolationRecord {
    private Long id;
    private Long userId;
    private String type;
    private String description;
    private Long relatedOrderId;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime appealTime;
    private String appealReason;
    private String appealResult;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getRelatedOrderId() { return relatedOrderId; }
    public void setRelatedOrderId(Long relatedOrderId) { this.relatedOrderId = relatedOrderId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getAppealTime() { return appealTime; }
    public void setAppealTime(LocalDateTime appealTime) { this.appealTime = appealTime; }
    public String getAppealReason() { return appealReason; }
    public void setAppealReason(String appealReason) { this.appealReason = appealReason; }
    public String getAppealResult() { return appealResult; }
    public void setAppealResult(String appealResult) { this.appealResult = appealResult; }
}
