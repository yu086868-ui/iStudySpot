package com.ycyu.istudyspotbackend.entity;

import java.time.LocalDateTime;

public class StudyRoomGuide {
    private Long id;
    private Long studyRoomId;
    private String contactInfo;
    private String learningAreas;
    private String convenienceFacilities;
    private String transportationGuide;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getStudyRoomId() { return studyRoomId; }
    public void setStudyRoomId(Long studyRoomId) { this.studyRoomId = studyRoomId; }

    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }

    public String getLearningAreas() { return learningAreas; }
    public void setLearningAreas(String learningAreas) { this.learningAreas = learningAreas; }

    public String getConvenienceFacilities() { return convenienceFacilities; }
    public void setConvenienceFacilities(String convenienceFacilities) { this.convenienceFacilities = convenienceFacilities; }

    public String getTransportationGuide() { return transportationGuide; }
    public void setTransportationGuide(String transportationGuide) { this.transportationGuide = transportationGuide; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
