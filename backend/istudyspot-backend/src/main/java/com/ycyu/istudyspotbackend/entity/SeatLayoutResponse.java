package com.ycyu.istudyspotbackend.entity;

import java.util.List;
import java.util.Map;

public class SeatLayoutResponse {
    private Long studyRoomId;
    private String studyRoomName;
    private Integer rows;
    private Integer cols;
    private Integer cellSize;
    private String layoutMode;
    private List<Seat> seats;
    private List<SeatLayoutItem> items;
    private Map<String, Object> legend;

    public Long getStudyRoomId() { return studyRoomId; }
    public void setStudyRoomId(Long studyRoomId) { this.studyRoomId = studyRoomId; }

    public String getStudyRoomName() { return studyRoomName; }
    public void setStudyRoomName(String studyRoomName) { this.studyRoomName = studyRoomName; }

    public Integer getRows() { return rows; }
    public void setRows(Integer rows) { this.rows = rows; }

    public Integer getCols() { return cols; }
    public void setCols(Integer cols) { this.cols = cols; }

    public Integer getCellSize() { return cellSize; }
    public void setCellSize(Integer cellSize) { this.cellSize = cellSize; }

    public String getLayoutMode() { return layoutMode; }
    public void setLayoutMode(String layoutMode) { this.layoutMode = layoutMode; }

    public List<Seat> getSeats() { return seats; }
    public void setSeats(List<Seat> seats) { this.seats = seats; }

    public List<SeatLayoutItem> getItems() { return items; }
    public void setItems(List<SeatLayoutItem> items) { this.items = items; }

    public Map<String, Object> getLegend() { return legend; }
    public void setLegend(Map<String, Object> legend) { this.legend = legend; }
}
