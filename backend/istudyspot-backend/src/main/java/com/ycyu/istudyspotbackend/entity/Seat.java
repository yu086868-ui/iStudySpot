package com.ycyu.istudyspotbackend.entity;

import java.time.LocalDateTime;

public class Seat {
    private Long id;
    private Long roomId;
    private Long areaId;
    private String seatNumber;
    private Integer seatType;
    private Integer rowNum;
    private Integer colNum;
    private Integer hasPower;
    private Integer hasLamp;
    private Integer isWindow;
    private Integer isQuiet;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public Long getAreaId() {
        return areaId;
    }

    public void setAreaId(Long areaId) {
        this.areaId = areaId;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public Integer getSeatType() {
        return seatType;
    }

    public void setSeatType(Integer seatType) {
        this.seatType = seatType;
    }

    public Integer getRowNum() {
        return rowNum;
    }

    public void setRowNum(Integer rowNum) {
        this.rowNum = rowNum;
    }

    public Integer getColNum() {
        return colNum;
    }

    public void setColNum(Integer colNum) {
        this.colNum = colNum;
    }

    public Integer getHasPower() {
        return hasPower;
    }

    public void setHasPower(Integer hasPower) {
        this.hasPower = hasPower;
    }

    public Integer getHasLamp() {
        return hasLamp;
    }

    public void setHasLamp(Integer hasLamp) {
        this.hasLamp = hasLamp;
    }

    public Integer getIsWindow() {
        return isWindow;
    }

    public void setIsWindow(Integer isWindow) {
        this.isWindow = isWindow;
    }

    public Integer getIsQuiet() {
        return isQuiet;
    }

    public void setIsQuiet(Integer isQuiet) {
        this.isQuiet = isQuiet;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}