package com.ycyu.istudyspotbackend.dto;

import com.ycyu.istudyspotbackend.entity.Seat;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class WxSeatDTO {
    private String id;
    private String studyRoomId;
    private Integer row;
    private Integer col;
    private String seatNumber;
    private String type;
    private String status;
    private List<String> facilities;
    private LocalDateTime lastUsedAt;

    public static WxSeatDTO fromEntity(Seat seat) {
        WxSeatDTO dto = new WxSeatDTO();
        dto.setId(seat.getId() != null ? seat.getId().toString() : null);
        dto.setStudyRoomId(seat.getRoomId() != null ? seat.getRoomId().toString() : null);
        dto.setRow(seat.getRowNum());
        dto.setCol(seat.getColNum());
        dto.setSeatNumber(seat.getSeatNumber());
        if (seat.getSeatType() != null) {
            dto.setType(seat.getSeatType() == 2 ? "vip" : "normal");
        } else {
            dto.setType("normal");
        }
        dto.setStatus(seat.getStatus());
        if (seat.getFacilities() != null && !seat.getFacilities().isEmpty()) {
            dto.setFacilities(Arrays.asList(seat.getFacilities().split(",")));
        } else {
            dto.setFacilities(Collections.emptyList());
        }
        dto.setLastUsedAt(seat.getLastUsedAt());
        return dto;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getStudyRoomId() { return studyRoomId; }
    public void setStudyRoomId(String studyRoomId) { this.studyRoomId = studyRoomId; }

    public Integer getRow() { return row; }
    public void setRow(Integer row) { this.row = row; }

    public Integer getCol() { return col; }
    public void setCol(Integer col) { this.col = col; }

    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<String> getFacilities() { return facilities; }
    public void setFacilities(List<String> facilities) { this.facilities = facilities; }

    public LocalDateTime getLastUsedAt() { return lastUsedAt; }
    public void setLastUsedAt(LocalDateTime lastUsedAt) { this.lastUsedAt = lastUsedAt; }
}
