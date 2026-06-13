package com.ycyu.istudyspotbackend.entity;

import java.time.LocalDateTime;

public class SeatLayoutItem {
    private Long id;
    private Long roomId;
    private Long areaId;
    private Long seatId;
    private String itemType;
    private String itemKey;
    private String label;
    private Integer rowNum;
    private Integer colNum;
    private Integer widthUnits;
    private Integer heightUnits;
    private Integer rotation;
    private Integer zIndex;
    private String metadata;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }

    public Long getAreaId() { return areaId; }
    public void setAreaId(Long areaId) { this.areaId = areaId; }

    public Long getSeatId() { return seatId; }
    public void setSeatId(Long seatId) { this.seatId = seatId; }

    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }

    public String getItemKey() { return itemKey; }
    public void setItemKey(String itemKey) { this.itemKey = itemKey; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public Integer getRowNum() { return rowNum; }
    public void setRowNum(Integer rowNum) { this.rowNum = rowNum; }

    public Integer getColNum() { return colNum; }
    public void setColNum(Integer colNum) { this.colNum = colNum; }

    public Integer getWidthUnits() { return widthUnits; }
    public void setWidthUnits(Integer widthUnits) { this.widthUnits = widthUnits; }

    public Integer getHeightUnits() { return heightUnits; }
    public void setHeightUnits(Integer heightUnits) { this.heightUnits = heightUnits; }

    public Integer getRotation() { return rotation; }
    public void setRotation(Integer rotation) { this.rotation = rotation; }

    public Integer getZIndex() { return zIndex; }
    public void setZIndex(Integer zIndex) { this.zIndex = zIndex; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
