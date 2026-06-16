package com.ycyu.istudyspotbackend.entity;

public class StudyRoomGuideDetail {
    private Long studyRoomId;
    private String studyRoomName;
    private String address;
    private String openTime;
    private String closeTime;
    private String description;
    private String contactInfo;
    private String learningAreas;
    private String convenienceFacilities;
    private String transportationGuide;

    public Long getStudyRoomId() { return studyRoomId; }
    public void setStudyRoomId(Long studyRoomId) { this.studyRoomId = studyRoomId; }

    public String getStudyRoomName() { return studyRoomName; }
    public void setStudyRoomName(String studyRoomName) { this.studyRoomName = studyRoomName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getOpenTime() { return openTime; }
    public void setOpenTime(String openTime) { this.openTime = openTime; }

    public String getCloseTime() { return closeTime; }
    public void setCloseTime(String closeTime) { this.closeTime = closeTime; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }

    public String getLearningAreas() { return learningAreas; }
    public void setLearningAreas(String learningAreas) { this.learningAreas = learningAreas; }

    public String getConvenienceFacilities() { return convenienceFacilities; }
    public void setConvenienceFacilities(String convenienceFacilities) { this.convenienceFacilities = convenienceFacilities; }

    public String getTransportationGuide() { return transportationGuide; }
    public void setTransportationGuide(String transportationGuide) { this.transportationGuide = transportationGuide; }
}
