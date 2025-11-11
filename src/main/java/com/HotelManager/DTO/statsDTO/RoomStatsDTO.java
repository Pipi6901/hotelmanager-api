package com.HotelManager.DTO.statsDTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RoomStatsDTO {
    private String roomName;
    private int income;
}