package com.ycyu.istudyspotbackend.entity;

public class StudyRoomGuideSummary {
    private Long studyRoomId;
    private String studyRoomName;
    private String address;
    private String openTime;
    private String closeTime;
    private String description;

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
}
