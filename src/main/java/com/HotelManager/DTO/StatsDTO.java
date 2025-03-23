package com.HotelManager.DTO;

import lombok.Data;

import java.util.List;

@Data
public class StatsDTO {
    private int totalIncome;
    private List<RoomStatsDTO> topRoomsByIncome;
    private List<RoomStatsDTO> topRoomsByBookings;
}